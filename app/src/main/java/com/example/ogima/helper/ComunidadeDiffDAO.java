package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComunidadeDiffDAO {

    private List<Comunidade> listaComunidade;
    private RecyclerView.Adapter adapter;

    public ComunidadeDiffDAO(List<Comunidade> listComunidade, RecyclerView.Adapter adapterRecebido) {
        this.listaComunidade = listComunidade;
        this.adapter = adapterRecebido;
    }

    public void adicionarComunidade(Comunidade comunidade) {

        // Verifica se o Comunidade já está na lista
        if (listaComunidade.contains(comunidade)) {
            return;
        }

        // Adiciona o Comunidade na lista
        listaComunidade.add(comunidade);

        // Ordena a lista em ordem alfabética
        Collections.sort(listaComunidade, new Comparator<Comunidade>() {
            @Override
            public int compare(Comunidade u1, Comunidade u2) {
                return u1.getNomeComunidade().compareToIgnoreCase(u2.getNomeComunidade());
            }
        });

        adapter.notifyDataSetChanged();
    }

    public void atualizarComunidade(Comunidade comunidade) {
        int index = -1;
        for (int i = 0; i < listaComunidade.size(); i++) {
            if (listaComunidade.get(i).getIdComunidade().equals(comunidade.getIdComunidade())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // Atualiza o Comunidade na lista
            listaComunidade.set(index, comunidade);
            Collections.sort(listaComunidade, new Comparator<Comunidade>() {
                @Override
                public int compare(Comunidade u1, Comunidade u2) {
                    return u1.getNomeComunidade().compareToIgnoreCase(u2.getNomeComunidade());
                }
            });
            Log.d("Atualiza Comunidade", "Comunidade atualizado com sucesso: " + comunidade.getNomeComunidade());
        } else {
            Log.e("Atualiza Comunidade", "Erro ao atualizar Comunidade: Comunidade nao encontrado na lista");
        }
    }


    public void removerComunidade(Comunidade comunidade) {

        Log.d("TESTE-Remove Comunidade", "A REMOVER: " + comunidade.getNomeComunidade());
        int position = listaComunidade.indexOf(comunidade);
        if (position != -1) {
            listaComunidade.remove(position);
            Log.d("TESTE-Remove Comunidade", "Comunidade removido com sucesso: " + comunidade.getNomeComunidade());
            Collections.sort(listaComunidade, new Comparator<Comunidade>() {
                @Override
                public int compare(Comunidade u1, Comunidade u2) {
                    return u1.getNomeComunidade().compareToIgnoreCase(u2.getNomeComunidade());
                }
            });
            Log.d("TESTE-Ordenar remoção", "Comunidade ordenado com sucesso: " + comunidade.getNomeComunidade());
        }else {
            Log.e("TESTE-Remove Comunidade", "Erro ao remover Comunidade: Comunidade nao encontrado na lista");
        }
    }

    public void limparListaComunidades() {
        listaComunidade.clear();
        adapter.notifyDataSetChanged();
    }
}