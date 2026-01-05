package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.ConversationActivity;
import com.example.ogima.helper.ChatDiffCallback;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.TimestampUtils;
import com.example.ogima.helper.UsuarioUtils;
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
    private Chat chatComparator = new Chat();

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        ViewHolder holderPrincipal = (ViewHolder) holder;
        Chat chat = listaChats.get(position);
        String idUser = listaChats.get(position).getIdUsuario();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);

        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                    if (bundle.containsKey("totalMsgNaoLida")) {
                        long totalMsgNaoLida = bundle.getLong("totalMsgNaoLida");
                        chat.setTotalMsgNaoLida(totalMsgNaoLida);
                    } else if (bundle.containsKey("totalMsg")) {
                        long totalMsg = bundle.getLong("totalMsg");
                        chat.setTotalMsg(totalMsg);
                    } else if (bundle.containsKey("tipoMidiaLastMsg")) {
                        String tipoMidiaLastMsg = bundle.getString("tipoMidiaLastMsg");
                        chat.setTipoMidiaLastMsg(tipoMidiaLastMsg);
                    } else if (bundle.containsKey("timestampLastMsg")) {
                        long timestampLastMsg = bundle.getLong("timestampLastMsg");
                        chat.setTimestampLastMsg(timestampLastMsg);
                    } else if (bundle.containsKey("conteudoLastMsg")) {
                        String conteudoLastMsg = bundle.getString("conteudoLastMsg");
                        chat.setConteudoLastMsg(conteudoLastMsg);
                    }
                }
            }
        }

        //Falta criar um tipo de FormatarContadorUtils
        //para números para exibir um + quando tiver atingido o limite de caracteres numéricos.
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

            if (chat.getTipoMidiaLastMsg() != null
                    && !chat.getTipoMidiaLastMsg().isEmpty()
                    && chat.getConteudoLastMsg() != null
                    && !chat.getTipoMidiaLastMsg().isEmpty()) {
                String tipoMidia = chat.getTipoMidiaLastMsg();
                String conteudo = "";
                if (tipoMidia.equals(MidiaUtils.TEXT)) {
                    holderPrincipal.txtViewDescChat.setTextColor(Color.BLACK);
                    conteudo = chat.getConteudoLastMsg();
                } else {
                    if (tipoMidia.equals(MidiaUtils.NOTICE)) {
                        holderPrincipal.txtViewDescChat.setTextColor(Color.GRAY);
                        conteudo = String.format("%s%s%s","[",chat.getConteudoLastMsg(),"]");
                    }else{
                        holderPrincipal.txtViewDescChat.setTextColor(Color.BLUE);
                        conteudo = String.format("%s %s %s", "Mídia", "-", chat.getTipoMidiaLastMsg());
                    }
                }

                holderPrincipal.txtViewDescChat.setText(FormatarContadorUtils.abreviarTexto(conteudo, 28));
            }

            long totalMsg = 0;
            if (chat.getTotalMsg() > 0) {
                totalMsg = chat.getTotalMsg();
            }

            holderPrincipal.btnViewTodasMsgs.setText(FormatarContadorUtils.abreviarTexto(String.valueOf(totalMsg), 8));

            if (chat.getTotalMsgNaoLida() > 0) {
                holderPrincipal.btnViewMsgsPerdidas.setVisibility(View.VISIBLE);
                holderPrincipal.btnViewMsgsPerdidas.setText(FormatarContadorUtils.abreviarTexto(String.valueOf(chat.getTotalMsgNaoLida()), 8));
            } else {
                holderPrincipal.btnViewMsgsPerdidas.setVisibility(View.INVISIBLE);
            }

            if (chat.getTimestampLastMsg() != -1) {
                TimestampUtils.RecuperarHoraDoTimestamp(context, true, chat.getTimestampLastMsg(), new TimestampUtils.RecuperarHoraTimestampCallback() {
                    @Override
                    public void onConcluido(String horaMinuto) {
                        holderPrincipal.txtViewHoraLastMsg.setText(horaMinuto);
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
            }

            holderPrincipal.relativeLayoutPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirChat(dadoUser, position);
                }
            });
        }

        super.onBindViewHolder(holder, position, payloads);
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
        private TextView txtViewIncName, txtViewDescChat, txtViewHoraLastMsg;
        private SpinKitView spinKitLoadPhoto;
        private RelativeLayout relativeLayoutPreview;
        private Button btnViewTodasMsgs, btnViewMsgsPerdidas;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            txtViewDescChat = itemView.findViewById(R.id.txtViewDescChat);
            txtViewHoraLastMsg = itemView.findViewById(R.id.txtViewHoraLastMsg);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            relativeLayoutPreview = itemView.findViewById(R.id.relativeLayoutPreview);
            btnViewTodasMsgs = itemView.findViewById(R.id.btnViewTodasMsgs);
            btnViewMsgsPerdidas = itemView.findViewById(R.id.btnViewMsgsPerdidas);

            imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexCircle));
        }

        private void atualizarMsgNaoLida(long dado) {
            btnViewMsgsPerdidas.setText(FormatarContadorUtils.formatarNumeroAbreviado(Math.toIntExact(dado)));
        }

        private void atualizarMsg(long dado) {
            btnViewTodasMsgs.setText(FormatarContadorUtils.formatarNumeroAbreviado(Math.toIntExact(dado)));
        }

        private void atualizarTipoMidiaLastMsg(String dado) {
            if (dado.isEmpty()) {

                return;
            }
        }
    }

    private void abrirChat(Usuario usuarioAlvo, int posicao) {
        String idDonoPerfil = usuarioAlvo.getIdUsuario();
        if (idDonoPerfil != null
                && !idDonoPerfil.isEmpty()
                && idUsuario != null && !idUsuario.isEmpty()
                && idDonoPerfil.equals(idUsuario)) {
            return;
        }
        recuperaPosicaoAnteriorListener.onPosicaoAnterior(posicao);
        intentNew(usuarioAlvo);
        animacaoIntentListener.onExecutarAnimacao();
    }

    private void intentNew(Usuario usuarioAlvo){
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("usuarioDestinatario", usuarioAlvo);
        intent.putExtra("onBack", "ChatInteractionsActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
