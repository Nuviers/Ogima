package com.example.ogima.helper;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Grupo;
import com.google.firebase.database.DatabaseReference;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class GrupoTesteDAO {

    private DatabaseReference grupoRef;
    private List<Grupo> listaGrupo;
    private RecyclerView.Adapter adapter;
    private HashMap<String, Integer> hashMapGrupos;

    public interface GrupoDAOInterface {
        void adicionarGrupo(Grupo grupo);

        void atualizarGrupo(Grupo grupo);

        void excluirGrupo(Grupo grupo, int position);
    }

    public GrupoTesteDAO(List<Grupo> listGrupo, RecyclerView.Adapter adapterRecebido) {
        this.listaGrupo = listGrupo;
        this.adapter = adapterRecebido;
        this.hashMapGrupos = new HashMap<>();

        for (int i = 0; i < listaGrupo.size(); i++) {
            hashMapGrupos.put(listaGrupo.get(i).getIdGrupo(), i);
        }
    }

    public void adicionarGrupo(Grupo grupo) {

        if (!hashMapGrupos.containsKey(grupo.getIdGrupo())) {
            listaGrupo.add(grupo);
            hashMapGrupos.put(grupo.getIdGrupo(), listaGrupo.size() - 1);
            adapter.notifyDataSetChanged();
        } else {
            listaGrupo.set(hashMapGrupos.get(grupo.getIdGrupo()), grupo);
        }

    }

    public void limparListaGrupos() {
        listaGrupo.clear();
        adapter.notifyDataSetChanged();
    }

    public void atualizarGrupo(Grupo grupoAtualizado) {

        if (hashMapGrupos.containsKey(grupoAtualizado.getIdGrupo())) {
            int posicao = hashMapGrupos.get(grupoAtualizado.getIdGrupo());
            listaGrupo.set(posicao, grupoAtualizado);
            adapter.notifyItemChanged(posicao);
        } else {
            listaGrupo.add(grupoAtualizado);
            hashMapGrupos.put(grupoAtualizado.getIdGrupo(), listaGrupo.size() - 1);
            adapter.notifyItemInserted(listaGrupo.size() - 1);
        }
    }

    public void excluirGrupo(Grupo grupo) {

        if (hashMapGrupos.containsKey(grupo.getIdGrupo())) {
            int posicao = hashMapGrupos.get(grupo.getIdGrupo());
            if (posicao != -1) {
                listaGrupo.remove(posicao);
                hashMapGrupos.remove(grupo.getIdGrupo());
                adapter.notifyItemRemoved(posicao);

                // Atualiza as posições dos usuários no HashMap após a remoção
                for (int i = posicao; i < listaGrupo.size(); i++) {
                    Grupo grupoAtualizado = listaGrupo.get(i);
                    hashMapGrupos.put(grupoAtualizado.getIdGrupo(), i);
                }
            }
        }
    }

    public void excluirGrupoComDiff(String idRemovido) {
        if (hashMapGrupos.containsKey(idRemovido)) {
            int index = hashMapGrupos.get(idRemovido);
            hashMapGrupos.remove(idRemovido);
            for (String key : hashMapGrupos.keySet()) {
                int value = hashMapGrupos.get(key);
                if (value > index) {
                    hashMapGrupos.put(key, value - 1);
                }
            }
        }
    }

    private void ordenarListaGrupo() {
        Collections.sort(listaGrupo, new Comparator<Grupo>() {
            @Override
            public int compare(Grupo grupoOrdenadot1, Grupo grupoOrdenadot2) {
                return grupoOrdenadot1.getNomeGrupo().compareToIgnoreCase(grupoOrdenadot2.getNomeGrupo());
            }
        });
    }
}
