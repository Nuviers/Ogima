package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.ChatDiffCallback;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Chat;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> listaChats;
    private Context context;
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private HashMap<String, Object> listaDadosUser;
    private int hexCircle = -1;
    private static final int MAX_LENGHT = 20;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private AnimacaoIntent animacaoIntentListener;
    private RemoverChatListener removerChatListener;

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface RemoverChatListener {
        void onRemocao(Chat chatAlvo, int posicao);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    public AdapterChatList(Context c, List<Chat> listaChatOrigem,
                           HashMap<String, Object> listaDadosUser, int hexCircle,
                           RecuperaPosicaoAnterior recuperaPosicaoListener,
                           RemoverChatListener removerChatListener,
                           AnimacaoIntent animacaoIntent) {
        this.context = c;
        this.listaChats = listaChatOrigem = new ArrayList<>();
        this.listaDadosUser = listaDadosUser;
        this.hexCircle = hexCircle;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.removerChatListener = removerChatListener;
        this.animacaoIntentListener = animacaoIntent;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_preview_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;
            Chat chat = listaChats.get(position);
            String idUser = listaChats.get(position).getIdUsuario();
            Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);
            if (dadoUser != null) {
                if (dadoUser.getMinhaFoto() != null && !dadoUser.getMinhaFoto().isEmpty()
                        && !chat.isIndisponivel()) {
                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
                    GlideCustomizado.loadUrlComListener(context,
                            dadoUser.getMinhaFoto(), holderPrincipal.imgViewIncPhoto,
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
                    UsuarioUtils.exibirFotoPadrao(context, holderPrincipal.imgViewIncPhoto, UsuarioUtils.FIELD_PHOTO, true);
                }
                String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(dadoUser);
                nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_NAME_LENGHT);
                holderPrincipal.txtViewIncName.setText(nomeConfigurado);

                holderPrincipal.relativeLayoutPreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarPerfil(dadoUser, position);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaChats.size();
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void updateChatList(List<Chat> listaChatsAtualizada, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        ChatDiffCallback diffCallback = new ChatDiffCallback(listaChats, listaChatsAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaChats.clear();
        listaChats.addAll(listaChatsAtualizada);
        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName, txtViewIncDesc;
        private SpinKitView spinKitLoadPhoto;
        private RelativeLayout relativeLayoutPreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            txtViewIncDesc = itemView.findViewById(R.id.txtViewIncDesc);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            relativeLayoutPreview = itemView.findViewById(R.id.relativeLayoutPreview);

            imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexCircle));
        }
    }

    private void visitarPerfil(Usuario usuarioAlvo, int posicao){
        String idDonoPerfil = usuarioAlvo.getIdUsuario();
        if (idDonoPerfil != null
                && !idDonoPerfil.isEmpty()
                && idUsuario != null && !idUsuario.isEmpty()
                && idDonoPerfil.equals(idUsuario)) {
            return;
        }
        recuperaPosicaoAnteriorListener.onPosicaoAnterior(posicao);
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                idDonoPerfil);
        animacaoIntentListener.onExecutarAnimacao();
    }

    public int findPositionInList(String userId) {
        for (int i = 0; i < listaChats.size(); i++) {
            Chat chat = listaChats.get(i);
            if (chat.getIdUsuario().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}
