package com.example.ogima.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.DailyShortsActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DailyShortDiffCallback;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterUsersDaily extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaUsuariosDaily;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    //Sempre inicializar o sinalizador de epilepsia como true, assim
    //mesmo que tenha algum problema na consulta no servidor não trará problemas.
    private boolean usuarioComEpilepsia = true;

    private AnimacaoIntent animacaoIntentListener;

    public AdapterUsersDaily(Context c, List<Usuario> listaUsuarioOrigem,
                             RecuperaPosicaoAnterior recuperaPosicaoListener,
                             AnimacaoIntent animacaoIntentListener) {
        this.listaUsuariosDaily = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.emailUsuario = autenticacao.getCurrentUser().getEmail();
        this.idUsuario = Base64Custom.codificarBase64(emailUsuario);
        this.animacaoIntentListener = animacaoIntentListener;

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (epilepsia != null) {
                    usuarioComEpilepsia = epilepsia;
                }else{
                    usuarioComEpilepsia = true;
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    public void updateDailyShortList(List<Usuario> listaUsuariosAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaUsuariosDaily, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaUsuariosDaily.clear();
        listaUsuariosDaily.addAll(listaUsuariosAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_daily_short, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;

            Usuario usuarioDaily = listaUsuariosDaily.get(position);

            String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(usuarioDaily);

            holderPrincipal.txtViewNomeUserDaily.setText(nomeConfigurado);
            holderPrincipal.txtViewLastTimeDaily.setText(usuarioDaily.getDataLastDaily());

            if (usuarioComEpilepsia) {
                GlideCustomizado.montarGlideEpilepsia(context, usuarioDaily.getMinhaFoto(), holderPrincipal.imgViewFotoUserDaily,
                        android.R.color.transparent);

                GlideCustomizado.montarGlideFotoEpilepsia(context, usuarioDaily.getUrlLastDaily(), holderPrincipal.imgViewDailyUser,
                        android.R.color.transparent);
            }else{
                GlideCustomizado.montarGlide(context, usuarioDaily.getMinhaFoto(), holderPrincipal.imgViewFotoUserDaily,
                        android.R.color.transparent);

                GlideCustomizado.montarGlideFoto(context, usuarioDaily.getUrlLastDaily(), holderPrincipal.imgViewDailyUser,
                        android.R.color.transparent);
            }

            if (usuarioDaily.getTipoMidia() != null) {
                if (usuarioDaily.getTipoMidia().equals("video")) {
                    holderPrincipal.imgBtnSinalizaVideo.setVisibility(View.VISIBLE);
                }else{
                    holderPrincipal.imgBtnSinalizaVideo.setVisibility(View.GONE);
                }
            }

            holderPrincipal.txtViewNomeUserDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verDailyShort(usuarioDaily.getIdUsuario());
                }
            });

            holderPrincipal.imgViewFotoUserDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verDailyShort(usuarioDaily.getIdUsuario());
                }
            });

            holderPrincipal.imgViewDailyUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verDailyShort(usuarioDaily.getIdUsuario());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuariosDaily.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgViewFotoUserDaily, imgViewDailyUser;
        private TextView txtViewNomeUserDaily, txtViewLastTimeDaily;
        private ImageButton imgBtnSinalizaVideo;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewFotoUserDaily = itemView.findViewById(R.id.imgViewFotoUserDaily);
            txtViewNomeUserDaily = itemView.findViewById(R.id.txtViewNomeUserDaily);
            txtViewLastTimeDaily = itemView.findViewById(R.id.txtViewLastTimeDaily);
            imgViewDailyUser = itemView.findViewById(R.id.imgViewDailyUser);
            imgBtnSinalizaVideo = itemView.findViewById(R.id.imgBtnSinalizaVideo);
        }
    }

    private void verDailyShort(String idUsuarioDaily){
        Intent intent = new Intent(context, DailyShortsActivity.class);
        intent.putExtra("idUsuarioDaily", idUsuarioDaily);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        animacaoIntentListener.onExecutarAnimacao();
    }
}
