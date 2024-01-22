package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Chat;

import java.util.List;
import java.util.Set;

public class ChatDiffDAO {

    private List<Chat> listaChat;
    private RecyclerView.Adapter adapter;

    public ChatDiffDAO(List<Chat> listChat, RecyclerView.Adapter adapterRecebido) {
        this.listaChat = listChat;
        this.adapter = adapterRecebido;
    }

    public void adicionarChat(Chat chat) {

        if (listaChat != null
                && listaChat.size() > 0 && listaChat.contains(chat)) {
            return;
        }

        if (listaChat != null && listaChat.size() == 0) {
            Log.d("DAO", "INICIO ITEM");
            listaChat.add(chat);
        } else if (listaChat != null && listaChat.size() >= 1) {
            Log.d("DAO", "NOVO ITEM");
            listaChat.add(0, chat);
        }
    }

    public void atualizarChat(Chat chat) {
        int index = -1;
        for (int i = 0; i < listaChat.size(); i++) {
            if (listaChat.get(i).getIdUsuario().equals(chat.getIdUsuario())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // Atualiza o Chat na lista
            listaChat.set(index, chat);
            Log.d("Atualiza Chat", "Chat atualizado com sucesso: ");
        } else {
            Log.e("Atualiza Chat", "Erro ao atualizar Chat: Chat nao encontrado na lista");
        }
    }

    public void atualizarChatPorPayload(Chat chat, String tipoPayload) {
        int index = -1;
        for (int i = 0; i < listaChat.size(); i++) {
            if (listaChat.get(i).getIdUsuario().equals(chat.getIdUsuario())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            if (tipoPayload != null && !tipoPayload.isEmpty()) {
                switch (tipoPayload) {
                    case "totalMsgNaoLida":
                        long totalMsgNaoLida = 0;
                        if (chat.getTotalMsgNaoLida() > 0) {
                            totalMsgNaoLida = chat.getTotalMsgNaoLida();
                        }
                        listaChat.get(index).setTotalMsgNaoLida(totalMsgNaoLida);
                        adapter.notifyItemChanged(index, createPayloadLong(tipoPayload, totalMsgNaoLida));
                        break;
                    case "totalMsg":
                        long totalMsg = 0;
                        if (chat.getTotalMsg() > 0) {
                            totalMsg = chat.getTotalMsg();
                        }
                        listaChat.get(index).setTotalMsg(totalMsg);
                        adapter.notifyItemChanged(index, createPayloadLong(tipoPayload, totalMsg));
                        break;
                    case "tipoMidiaLastMsg":
                        String tipoMidiaLastMsg = "";
                        if (chat.getTipoMidiaLastMsg() != null && !chat.getTipoMidiaLastMsg().isEmpty()) {
                            tipoMidiaLastMsg = chat.getTipoMidiaLastMsg();
                        }
                        listaChat.get(index).setTipoMidiaLastMsg(tipoMidiaLastMsg);
                        adapter.notifyItemChanged(index, createPayloadString(tipoPayload, tipoMidiaLastMsg));
                        break;
                    case "timestampLastMsg":
                        long timestampLastMsg = -1;
                        if (chat.getTimestampLastMsg() != -1) {
                            timestampLastMsg = chat.getTimestampLastMsg();
                        }
                        listaChat.get(index).setTimestampLastMsg(timestampLastMsg);
                        adapter.notifyItemChanged(index, createPayloadLong(tipoPayload, timestampLastMsg));
                        break;
                    case "conteudoLastMsg":
                        String conteudoLastMsg = "";
                        if (chat.getConteudoLastMsg() != null && !chat.getConteudoLastMsg().isEmpty()) {
                            conteudoLastMsg = chat.getConteudoLastMsg();
                        }
                        listaChat.get(index).setConteudoLastMsg(conteudoLastMsg);
                        adapter.notifyItemChanged(index, createPayloadString(tipoPayload, conteudoLastMsg));
                        break;
                }
            }
            Log.d("Atualiza Chat", "Chat atualizado com sucesso: ");
        } else {
            Log.e("Atualiza Chat", "Erro ao atualizar Chat: Chat nao encontrado na lista");
        }
    }


    public void removerChat(Chat chat) {

        Log.d("TESTE-Remove Chat", "A REMOVER: " + chat.getIdUsuario());
        int position = listaChat.indexOf(chat);
        if (position != -1) {
            listaChat.remove(position);
            Log.d("TESTE-Remove Chat", "Chat removido com sucesso: " + chat.getIdUsuario());
        } else {
            Log.e("TESTE-Remove Chat", "Erro ao remover Chat: Chat nao encontrado na lista");
        }
    }

    public void carregarMaisChat(List<Chat> newChat, Set<String> idsChats) {
        if (newChat != null && newChat.size() >= 1) {
            for (Chat chat : newChat) {
                if (!idsChats.contains(chat.getIdUsuario())) {
                    listaChat.add(chat);
                    idsChats.add(chat.getIdUsuario());
                    Log.d("PAGIN", "MAIS DADOS");
                }
            }
        }
    }

    public void adicionarIdAoSet(Set<String> idsChats, String idAlvo) {
        if (idsChats != null && idsChats.size() > 0
                && idsChats.contains(idAlvo)) {
            return;
        }
        if (idsChats != null) {
            idsChats.add(idAlvo);
        }
    }

    public void limparListaChats() {
        listaChat.clear();
        adapter.notifyDataSetChanged();
    }

    private Bundle createPayloadLong(String key, long dado) {
        Bundle payload = new Bundle();
        payload.putLong(key, dado);
        return payload;
    }

    private Bundle createPayloadString(String key, String dado) {
        Bundle payload = new Bundle();
        payload.putString(key, dado);
        return payload;
    }
}