package com.example.ogima.helper;

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
            listaChat.add(listaChat.size(), chat);
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


    public void removerChat(Chat chat) {

        Log.d("TESTE-Remove Chat", "A REMOVER: " + chat.getIdUsuario());
        int position = listaChat.indexOf(chat);
        if (position != -1) {
            listaChat.remove(position);
            Log.d("TESTE-Remove Chat", "Chat removido com sucesso: " + chat.getIdUsuario());
        }else {
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

    public void limparListaChats() {
        listaChat.clear();
        adapter.notifyDataSetChanged();
    }
}