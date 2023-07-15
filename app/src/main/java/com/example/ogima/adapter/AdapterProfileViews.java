package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterProfileViews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaViewers;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    //Sempre inicializar o sinalizador de epilepsia como true, assim
    //mesmo que tenha algum problema na consulta no servidor não trará problemas.
    private boolean dadosUserAtualRecuperado = false;

    public AdapterProfileViews(Context c, List<Usuario> listaUsuarioOrigem,
                               RecuperaPosicaoAnterior recuperaPosicaoListener) {
        this.listaViewers = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.emailUsuario = autenticacao.getCurrentUser().getEmail();
        this.idUsuario = Base64Custom.codificarBase64(emailUsuario);
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

    public interface DadosUsuarioAtual {
        void onRecuperado(boolean epilepsia);

        void onError(String message);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_profile_views, parent, false);
        return new AdapterProfileViews.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AdapterProfileViews.ViewHolder) {
            AdapterProfileViews.ViewHolder holderPrincipal = (AdapterProfileViews.ViewHolder) holder;

            Usuario usuarioViewer = listaViewers.get(position);

            recuperarDadosUserAtual(new DadosUsuarioAtual() {
                @Override
                public void onRecuperado(boolean epilepsia) {

                    FirebaseRecuperarUsuario.recuperaUsuarioCompleto(usuarioViewer.getIdUsuario(), new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
                        @Override
                        public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                            if (epilepsia) {
                                if (usuarioAtual.getMinhaFoto() != null
                                        && !usuarioAtual.getMinhaFoto().isEmpty()) {
                                    GlideCustomizado.montarGlideEpilepsia(context,
                                            usuarioAtual.getMinhaFoto(), holderPrincipal.imgViewFotoProfile,
                                            android.R.color.transparent);
                                }


                                if (usuarioAtual.getMeuFundo() != null
                                        && !usuarioAtual.getMeuFundo().isEmpty()) {
                                    GlideCustomizado.montarGlideFotoEpilepsia(context,
                                            usuarioAtual.getMeuFundo(), holderPrincipal.imgViewFundoProfile,
                                            android.R.color.transparent);
                                }

                            } else {
                                if (usuarioAtual.getMinhaFoto() != null
                                        && !usuarioAtual.getMinhaFoto().isEmpty()) {
                                    GlideCustomizado.montarGlide(context,
                                            usuarioAtual.getMinhaFoto(), holderPrincipal.imgViewFotoProfile,
                                            android.R.color.transparent);
                                }

                                if (usuarioAtual.getMeuFundo() != null
                                        && !usuarioAtual.getMeuFundo().isEmpty()) {
                                    GlideCustomizado.montarGlideFoto(context,
                                            usuarioAtual.getMeuFundo(), holderPrincipal.imgViewFundoProfile,
                                            android.R.color.transparent);
                                }
                            }

                            String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(usuarioAtual);

                            holderPrincipal.txtViewNameProfile.setText(nomeConfigurado);
                            holderPrincipal.txtViewDataView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onSemDados() {

                        }

                        @Override
                        public void onError(String mensagem) {

                        }
                    });
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
    }

    @Override
    public int getItemCount() {
        return listaViewers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFundoProfile, imgViewFotoProfile;
        private TextView txtViewNameProfile, txtViewDataView;
        private Button btnDesbloquearView;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewFundoProfile = itemView.findViewById(R.id.imgViewIncFundoProfile);
            imgViewFotoProfile = itemView.findViewById(R.id.imgViewIncFotoProfile);
            txtViewNameProfile = itemView.findViewById(R.id.txtViewNameProfile);
            txtViewDataView = itemView.findViewById(R.id.txtViewDataView);
            btnDesbloquearView = itemView.findViewById(R.id.btnDesbloquearView);

            txtViewNameProfile.setTextColor(Color.parseColor("#F5CCD9E1"));
        }
    }

    private void visitarPerfil(String idDonoPerfil) {
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                idDonoPerfil);
    }

    private void recuperarDadosUserAtual(DadosUsuarioAtual callback) {
        if (!dadosUserAtualRecuperado) {
            FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
                @Override
                public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                    if (epilepsia != null) {
                        ToastCustomizado.toastCustomizado("Recuperado", context);
                        callback.onRecuperado(epilepsia);
                        dadosUserAtualRecuperado = true;
                    } else {
                        ToastCustomizado.toastCustomizado("Recuperado", context);
                        callback.onRecuperado(true);
                        dadosUserAtualRecuperado = true;
                    }
                }

                @Override
                public void onError(String mensagem) {
                    callback.onError(mensagem);
                }
            });
        }
    }
}