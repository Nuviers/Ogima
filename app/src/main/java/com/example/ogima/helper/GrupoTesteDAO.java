package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Grupo;
import com.google.firebase.database.DatabaseReference;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class GrupoTesteDAO {

    private List<Grupo> listaGrupo;
    private RecyclerView.Adapter adapter;

    public GrupoTesteDAO(List<Grupo> listGrupo, RecyclerView.Adapter adapterRecebido) {
        this.listaGrupo = listGrupo;
        this.adapter = adapterRecebido;
    }

    public void adicionarGrupo(Grupo grupo) {

        // Verifica se o Grupo já está na lista
        if (listaGrupo.contains(grupo)) {
            return;
        }

        // Adiciona o Grupo na lista
        listaGrupo.add(grupo);

        // Ordena a lista em ordem alfabética
        Collections.sort(listaGrupo, new Comparator<Grupo>() {
            @Override
            public int compare(Grupo u1, Grupo u2) {
                return u1.getNomeGrupo().compareToIgnoreCase(u2.getNomeGrupo());
            }
        });

        adapter.notifyDataSetChanged();
    }

    public void atualizarGrupo(Grupo grupo) {
        int index = -1;
        for (int i = 0; i < listaGrupo.size(); i++) {
            if (listaGrupo.get(i).getIdGrupo().equals(grupo.getIdGrupo())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // Atualiza o Grupo na lista

            //Ignora o nome alterado para não ficar trocando de posição os elementos em tempo real
            //pois isso evita a confusão para o usuário atual.
            if(!listaGrupo.get(index).getNomeGrupo().equals(grupo.getNomeGrupo())){
                Log.d("IGNORAR NOME", "Nome alterado: " + grupo.getNomeGrupo());
                return;
            }

            listaGrupo.set(index, grupo);
            Collections.sort(listaGrupo, new Comparator<Grupo>() {
                @Override
                public int compare(Grupo u1, Grupo u2) {
                    return u1.getNomeGrupo().compareToIgnoreCase(u2.getNomeGrupo());
                }
            });
            Log.d("TESTE-Atualizar Grupo", "Grupo atualizado com sucesso: " + grupo.getNomeGrupo());
        } else {
            Log.e("TESTE-Atualizar Grupo", "Erro ao atualizar Grupo: Grupo nao encontrado na lista");
        }
    }


    public void removerGrupo(Grupo grupo) {

        Log.d("TESTE-Remover Grupo", "A REMOVER: " + grupo.getNomeGrupo());
        int position = listaGrupo.indexOf(grupo);
        if (position != -1) {
            listaGrupo.remove(position);
            Log.d("TESTE-Remover Grupo", "Grupo removido com sucesso: " + grupo.getNomeGrupo());
            Collections.sort(listaGrupo, new Comparator<Grupo>() {
                @Override
                public int compare(Grupo u1, Grupo u2) {
                    return u1.getNomeGrupo().compareToIgnoreCase(u2.getNomeGrupo());
                }
            });
            Log.d("TESTE-Ordenar remoção", "Grupo ordenado com sucesso: " + grupo.getNomeGrupo());
        }else {
            Log.e("TESTE-Remover Grupo", "Erro ao remover Grupo: Grupo nao encontrado na lista");
        }
    }

    public void limparListaGrupos() {
        listaGrupo.clear();
        adapter.notifyDataSetChanged();
    }
}