package com.example.ogima.helper;

import android.content.Context;
import android.widget.Adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.adapter.AdapterChatGrupo;
import com.example.ogima.model.Grupo;

import java.util.HashMap;
import java.util.List;

public class GrupoDAO {

    private List<Grupo> listaGrupos;
    private int position;
    private Context context;
    private HashMap<String, Integer> hashMapGrupos;

    public GrupoDAO(List<Grupo> listaGrupos, Context context) {
        this.listaGrupos = listaGrupos;
        this.hashMapGrupos = new HashMap<>();
        this.context = context;

        for (int i = 0; i < listaGrupos.size(); i++) {
            hashMapGrupos.put(listaGrupos.get(i).getIdGrupo(), i);
        }
    }

    public void adicionarGrupo(Grupo grupoRecebido, RecyclerView.Adapter adapterChatGrupo) {

        if (!hashMapGrupos.containsKey(grupoRecebido.getIdGrupo())) {
            listaGrupos.add(grupoRecebido);
            hashMapGrupos.put(grupoRecebido.getIdGrupo(), listaGrupos.size() - 1);
            adapterChatGrupo.notifyDataSetChanged();
        } else {
            listaGrupos.set(hashMapGrupos.get(grupoRecebido.getIdGrupo()), grupoRecebido);
        }
    }

    public void removerGrupo(Grupo grupoRecebido, RecyclerView.Adapter adapterChatGrupo) {

        if (hashMapGrupos.containsKey(grupoRecebido.getIdGrupo())) {
            int posicao = hashMapGrupos.get(grupoRecebido.getIdGrupo());
            if (posicao != -1) {
                listaGrupos.remove(posicao);
                hashMapGrupos.remove(grupoRecebido.getIdGrupo());
                adapterChatGrupo.notifyItemRemoved(posicao);

                // Atualiza as posições dos usuários no HashMap após a remoção
                for (int i = posicao; i < listaGrupos.size(); i++) {
                    Grupo grupoAtualizado = listaGrupos.get(i);
                    hashMapGrupos.put(grupoAtualizado.getIdGrupo(), i);
                }
            }
        }
    }

    public void atualizarGrupo(Grupo grupoAnterior, Grupo grupoAtualizado) {
        int index = listaGrupos.indexOf(grupoAnterior);
        listaGrupos.set(index, grupoAtualizado);
        hashMapGrupos.put(grupoAtualizado.getIdGrupo(), index);
    }

    private int recuperarPosicao(Grupo grupo){
        position = listaGrupos.indexOf(grupo);
        return position;
    }

    public List<Grupo> listarGrupos() {
        return listaGrupos;
    }
}