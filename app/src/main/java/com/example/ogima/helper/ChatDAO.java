package com.example.ogima.helper;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Chat;

import java.util.HashMap;
import java.util.List;

public class ChatDAO {

    private List<Chat> listaChats;
    private int position;
    private Context context;
    private HashMap<String, Integer> hashMapChats;

    public ChatDAO(List<Chat> listaChats, Context context) {
        this.listaChats = listaChats;
        this.hashMapChats = new HashMap<>();
        this.context = context;

        for (int i = 0; i < listaChats.size(); i++) {
            hashMapChats.put(listaChats.get(i).getIdUsuario(), i);
        }
    }

    public void adicionarChat(Chat chatRecebida, RecyclerView.Adapter adapterChat) {

        if (!hashMapChats.containsKey(chatRecebida.getIdUsuario())) {
            listaChats.add(chatRecebida);
            hashMapChats.put(chatRecebida.getIdUsuario(), listaChats.size() - 1);
            adapterChat.notifyDataSetChanged();
        } else {
            listaChats.set(hashMapChats.get(chatRecebida.getIdUsuario()), chatRecebida);
        }
    }

    public void removerChat(Chat chatRecebida, RecyclerView.Adapter adapterChat) {

        if (hashMapChats.containsKey(chatRecebida.getIdUsuario())) {
            int posicao = hashMapChats.get(chatRecebida.getIdUsuario());
            if (posicao != -1) {
                listaChats.remove(posicao);
                hashMapChats.remove(chatRecebida.getIdUsuario());
                adapterChat.notifyItemRemoved(posicao);

                // Atualiza as posições dos usuários no HashMap após a remoção
                for (int i = posicao; i < listaChats.size(); i++) {
                    Chat chatAtualizado = listaChats.get(i);
                    hashMapChats.put(chatAtualizado.getIdUsuario(), i);
                }
            }
        }
    }

    public void atualizarChat(Chat chatAtualizado, RecyclerView.Adapter adapterChat) {
        for (int i = 0; i < listaChats.size(); i++) {
            Chat u = listaChats.get(i);
            if (u.getIdUsuario().equals(chatAtualizado.getIdUsuario())) { // compara o id do Firebase com o id local
                listaChats.set(i, chatAtualizado); // atualiza o objeto na lista local
                adapterChat.notifyItemChanged(i); // notifica o adapter da mudança
                break;
            }
        }
    }

    private int recuperarPosicao(Chat chat){
        position = listaChats.indexOf(chat);
        return position;
    }

    public List<Chat> listarChats() {
        return listaChats;
    }

    public void limparListaChat(RecyclerView.Adapter adapterChat) {
        if (listaChats != null && listaChats.size() > 0) {
            listaChats.clear();
            adapterChat.notifyDataSetChanged();
        }
    }
}