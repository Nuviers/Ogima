package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterPaginacaoPesquisa extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaUsuarios;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private boolean statusEpilepsia = true;
    private List<Usuario> listaDadosUser;

    private boolean filtragem = false;

    public AdapterPaginacaoPesquisa(Context c, List<Usuario> listaUsuarioOrigem,
                                    RecuperaPosicaoAnterior recuperaPosicaoListener,
                                    List<Usuario> listDadosUser) {
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.emailUsuario = autenticacao.getCurrentUser().getEmail();
        this.idUsuario = Base64Custom.codificarBase64(emailUsuario);
        this.listaDadosUser = listDadosUser = new ArrayList<>();
    }

    public void updateUserList(List<Usuario> listaUsuariosAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaUsuarios, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaUsuarios.clear();
        listaUsuarios.addAll(listaUsuariosAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }

    public void updateUserDadoList(List<Usuario> listaUsuariosAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaDadosUser, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaDadosUser.clear();
        listaDadosUser.addAll(listaUsuariosAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_viewers_desbloqueados, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        Usuario usuario = listaUsuarios.get(position);
        Usuario dadoUser = listaDadosUser.get(position);

        if (!payloads.isEmpty()) {

            ToastCustomizado.toastCustomizadoCurto("PAYLOAD", context);

            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                }
            }
        }

        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;

            if (!isFiltragem()) {

                if (dadoUser.getIdUsuario().equals(usuario.getIdUsuario())) {

                    ToastCustomizado.toastCustomizado("BOA",context);

                    holderPrincipal.txtViewDataView.setText(usuario.getDataView());

                    if (dadoUser.getMinhaFoto() != null && !dadoUser.getMinhaFoto().isEmpty()) {
                        GlideCustomizado.loadUrl(context,
                                dadoUser.getMinhaFoto(), holderPrincipal.imgViewFotoProfile,
                                android.R.color.transparent,
                                GlideCustomizado.CIRCLE_CROP,
                                false, isStatusEpilepsia());
                    }

                    if (dadoUser.getMeuFundo() != null && !dadoUser.getMeuFundo().isEmpty()) {
                        GlideCustomizado.loadUrl(context,
                                dadoUser.getMeuFundo(), holderPrincipal.imgViewFundoProfile,
                                android.R.color.transparent,
                                GlideCustomizado.CENTER_CROP,
                                false, isStatusEpilepsia());
                    }

                    String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(dadoUser);

                    holderPrincipal.txtViewNameProfile.setText(nomeConfigurado);
                }
            }else{
                if (usuario.getMinhaFoto() != null && !usuario.getMinhaFoto().isEmpty()) {
                    GlideCustomizado.loadUrl(context,
                            usuario.getMinhaFoto(), holderPrincipal.imgViewFotoProfile,
                            android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP,
                            false, isStatusEpilepsia());
                }

                if (usuario.getMeuFundo() != null && !usuario.getMeuFundo().isEmpty()) {
                    GlideCustomizado.loadUrl(context,
                            usuario.getMeuFundo(), holderPrincipal.imgViewFundoProfile,
                            android.R.color.transparent,
                            GlideCustomizado.CENTER_CROP,
                            false, isStatusEpilepsia());
                }

                String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(usuario);

                holderPrincipal.txtViewNameProfile.setText(nomeConfigurado);
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public boolean isFiltragem() {
        return filtragem;
    }

    public void setFiltragem(boolean filtragem) {
        this.filtragem = filtragem;
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
}
