package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ComunidadeDiffCallback;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AdapterPreviewCommunity extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Comunidade> listaComunidades;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private AnimacaoIntent animacaoIntentListener;
    private HashMap<String, Object> listaDadosComunidade;
    private int hexImagem = -1;
    private int hexSuperAmd = -1;
    private static final int MAX_LENGHT = 20;
    private RemoverComunidadeListener removerComunidadeListener;
    private String tipoComunidade = "";

    public AdapterPreviewCommunity(Context c, List<Comunidade> listaComunidadeOrigem,
                                   RecuperaPosicaoAnterior recuperaPosicaoListener,
                                   AnimacaoIntent animacaoIntent,
                                   HashMap<String, Object> listDadosComunidade, int hexImagem,
                                   RemoverComunidadeListener removerComunidadeListener, String tipoComunidade) {
        this.listaComunidades = listaComunidadeOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.animacaoIntentListener = animacaoIntent;
        this.listaDadosComunidade = listDadosComunidade;
        this.hexImagem = hexImagem;
        this.removerComunidadeListener = removerComunidadeListener;
        this.tipoComunidade = tipoComunidade;
        this.hexSuperAmd = context.getResources().getColor(R.color.community_super_adm);
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public interface RemoverComunidadeListener {
        void onRemocao(Comunidade comunidadeAlvo, int posicao, String tipoComunidade);
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void updateComunidadeList(List<Comunidade> listaComunidadesAtualizadas, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        ComunidadeDiffCallback diffCallback = new ComunidadeDiffCallback(listaComunidades, listaComunidadesAtualizadas);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaComunidades.clear();
        listaComunidades.addAll(listaComunidadesAtualizadas);
        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_preview_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        Comunidade comunidade = listaComunidades.get(position);
        String idCommunity = listaComunidades.get(position).getIdComunidade();
        Comunidade dadosCommunity = (Comunidade) listaDadosComunidade.get(idCommunity);
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                }
            }
        }
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;
            if (dadosCommunity != null) {

                if (dadosCommunity.getIdSuperAdmComunidade() != null
                        && !dadosCommunity.getIdSuperAdmComunidade().isEmpty()
                        && dadosCommunity.getIdSuperAdmComunidade().equals(idUsuario)
                        && hexSuperAmd != -1) {
                    holderPrincipal.imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexSuperAmd));
                } else if (hexImagem != -1) {
                    holderPrincipal.imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexImagem));
                }

                if (dadosCommunity.getFotoComunidade() != null && !dadosCommunity.getFotoComunidade().isEmpty()
                        && !comunidade.isIndisponivel()) {
                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
                    GlideCustomizado.loadUrlComListener(context,
                            dadosCommunity.getFotoComunidade(), holderPrincipal.imgViewIncPhoto,
                            android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP,
                            false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(String message) {
                                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                                }
                            });
                } else {
                    CommunityUtils.exibirFotoPadrao(context, holderPrincipal.imgViewIncPhoto, UsuarioUtils.FIELD_PHOTO, true);
                }
                String nomeConfigurado = UsuarioUtils.recuperarNomeConfiguradoComunidade(dadosCommunity);
                nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_COMMUNITY_NAME_LENGHT);
                holderPrincipal.txtViewIncName.setText(nomeConfigurado);
                if (dadosCommunity.getDescricaoComunidade() != null && !dadosCommunity.getDescricaoComunidade().isEmpty()) {
                    String descricaoConfigurada = dadosCommunity.getDescricaoComunidade().trim();
                    descricaoConfigurada = FormatarContadorUtils.abreviarTexto(descricaoConfigurada, UsuarioUtils.MAX_COMMUNITY_PREVIEW_DESC_LENGHT);
                    holderPrincipal.txtViewIncDesc.setText(descricaoConfigurada);
                }

                holderPrincipal.imgViewIncPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarComunidade(v, comunidade, position);
                    }
                });
                holderPrincipal.txtViewIncName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarComunidade(v, comunidade, position);
                    }
                });
                holderPrincipal.txtViewIncDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarComunidade(v, comunidade, position);
                    }
                });
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return listaComunidades.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName, txtViewIncDesc;
        private SpinKitView spinKitLoadPhoto;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            txtViewIncDesc = itemView.findViewById(R.id.txtViewIncDesc);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
        }
    }

    private void visitarComunidade(View view, Comunidade comunidade, int position) {

        if (comunidade.isIndisponivel()) {
            ToastCustomizado.toastCustomizadoCurto("Comunidade indisponível", context);
            return;
        }
        FirebaseRecuperarUsuario.recoverCommunity(comunidade.getIdComunidade(), new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                Intent intent = new Intent(context, ComunidadePostagensActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("idComunidade", comunidadeAtual.getIdComunidade());
                context.startActivity(intent);
                recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                animacaoIntentListener.onExecutarAnimacao();
            }

            @Override
            public void onNaoExiste() {
                SnackbarUtils.showSnackbar(view, "Essa comunidade não existe mais");
                removerComunidadeListener.onRemocao(comunidade, position, tipoComunidade);
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao visitar a comunidade selecionada.Tente novamente", context);
            }
        });
    }

    public int findPositionInList(String communityId) {
        for (int i = 0; i < listaComunidades.size(); i++) {
            Comunidade community = listaComunidades.get(i);
            if (community.getIdComunidade().equals(communityId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}