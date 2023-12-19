package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ComunidadeDiffDAO {

    private List<Comunidade> listaComunidade;
    private RecyclerView.Adapter adapter;

    public ComunidadeDiffDAO(List<Comunidade> listComunidade, RecyclerView.Adapter adapterRecebido) {
        this.listaComunidade = listComunidade;
        this.adapter = adapterRecebido;
    }

    public void adicionarComunidade(Comunidade comunidade) {

        if (listaComunidade != null
                && listaComunidade.size() > 0 && listaComunidade.contains(comunidade)) {
            return;
        }

        if (listaComunidade != null && listaComunidade.size() == 0) {
            Log.d("DAO", "INICIO ITEM");
            listaComunidade.add(comunidade);
        } else if (listaComunidade != null && listaComunidade.size() >= 1) {
            Log.d("DAO", "NOVO ITEM");
            listaComunidade.add(listaComunidade.size(), comunidade);
        }
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

            //Ignora o nome alterado para não ficar trocando de posição os elementos em tempo real
            //pois isso evita a confusão para o usuário atual.
            if(!listaComunidade.get(index).getNomeComunidade().equals(comunidade.getNomeComunidade())){
                Log.d("IGNORAR NOME", "Nome alterado: " + comunidade.getNomeComunidade());
                return;
            }

            listaComunidade.set(index, comunidade);

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

            Log.d("TESTE-Ordenar remoção", "Comunidade ordenado com sucesso: " + comunidade.getNomeComunidade());
        }else {
            Log.e("TESTE-Remove Comunidade", "Erro ao remover Comunidade: Comunidade nao encontrado na lista");
        }
    }

    public void carregarMaisComunidade(List<Comunidade> newComunidade, Set<String> idsComunidades) {
        if (newComunidade != null && newComunidade.size() >= 1) {
            for (Comunidade comunidade : newComunidade) {
                if (!idsComunidades.contains(comunidade.getIdComunidade())) {
                    listaComunidade.add(comunidade);
                    idsComunidades.add(comunidade.getIdComunidade());
                    Log.d("PAGIN", "MAIS DADOS");
                }
            }
        }
    }

    public void limparListaComunidades() {
        listaComunidade.clear();
        adapter.notifyDataSetChanged();
    }
}