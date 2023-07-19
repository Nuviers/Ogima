package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.alterac.blurkit.BlurLayout;

public class AdapterViewersDesbloqueados extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaViewers;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private boolean statusEpilepsia = true;
    private AnimacaoIntent animacaoIntentListener;

    public AdapterViewersDesbloqueados(Context c, List<Usuario> listaUsuarioOrigem,
                                       RecuperaPosicaoAnterior recuperaPosicaoListener,
                                       AnimacaoIntent animacaoIntent) {
        this.listaViewers = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.emailUsuario = autenticacao.getCurrentUser().getEmail();
        this.idUsuario = Base64Custom.codificarBase64(emailUsuario);
        this.animacaoIntentListener = animacaoIntent;
    }

    public void updateViewersList(List<Usuario> listaUsuariosAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaViewers, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaViewers.clear();
        listaViewers.addAll(listaUsuariosAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface DadosViewer {
        void onRecuperado(Usuario usuarioViewer, String fotoUsuario, String fundoUsuario);

        void onSemDados();

        void onError(String message);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_viewers_desbloqueados, parent, false);
        return new AdapterViewersDesbloqueados.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        Usuario usuarioViewer = listaViewers.get(position);

        if (!payloads.isEmpty()) {

            ToastCustomizado.toastCustomizadoCurto("PAYLOAD", context);

            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                }
            }
        }

        if (holder instanceof AdapterViewersDesbloqueados.ViewHolder) {
            AdapterViewersDesbloqueados.ViewHolder holderPrincipal = (AdapterViewersDesbloqueados.ViewHolder) holder;

            recuperarUser(usuarioViewer.getIdUsuario(), new DadosViewer() {
                @Override
                public void onRecuperado(Usuario usuarioRecuperado, String fotoUser, String fundoUser) {

                    if (fotoUser != null && !fotoUser.isEmpty()) {
                        GlideCustomizado.loadUrl(context,
                                fotoUser, holderPrincipal.imgViewFotoProfile,
                                android.R.color.transparent,
                                GlideCustomizado.CIRCLE_CROP,
                                false, isStatusEpilepsia());
                    }

                    if (fundoUser != null && !fundoUser.isEmpty()) {
                        GlideCustomizado.loadUrl(context,
                                fundoUser, holderPrincipal.imgViewFundoProfile,
                                android.R.color.transparent,
                                GlideCustomizado.CENTER_CROP,
                                false, isStatusEpilepsia());
                    }

                    String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(usuarioRecuperado);

                    holderPrincipal.txtViewNameProfile.setText(nomeConfigurado);

                    holderPrincipal.btnVisitarPerfil.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            visitarPerfil(usuarioViewer.getIdUsuario(), position);
                        }
                    });
                }

                @Override
                public void onSemDados() {

                }

                @Override
                public void onError(String message) {

                }
            });

            if (usuarioViewer.getDataView() != null
                    && !usuarioViewer.getDataView().isEmpty()) {
                holderPrincipal.txtViewDataView.setText(usuarioViewer.getDataView());
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return listaViewers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFundoProfile, imgViewFotoProfile;
        private TextView txtViewNameProfile, txtViewDataView;
        private Button btnVisitarPerfil;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewFundoProfile = itemView.findViewById(R.id.imgViewIncFundoProfile);
            imgViewFotoProfile = itemView.findViewById(R.id.imgViewIncFotoProfile);
            txtViewNameProfile = itemView.findViewById(R.id.txtViewNameProfile);
            txtViewDataView = itemView.findViewById(R.id.txtViewDataView);
            btnVisitarPerfil = itemView.findViewById(R.id.btnVisitarPerfilDesbloqueado);
        }
    }

    private void visitarPerfil(String idDonoPerfil, int posicao) {
        recuperaPosicaoAnteriorListener.onPosicaoAnterior(posicao);
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                idDonoPerfil);
        animacaoIntentListener.onExecutarAnimacao();
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    private void recuperarUser(String idViewer, DadosViewer callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idViewer, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                callback.onRecuperado(usuarioAtual, fotoUsuario, fundoUsuario);
            }

            @Override
            public void onSemDados() {
                callback.onSemDados();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }
}