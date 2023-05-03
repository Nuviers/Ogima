package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Postagem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PostagemDiffDAO {

    private List<Postagem> listaPostagem;
    private RecyclerView.Adapter adapter;

    public PostagemDiffDAO(List<Postagem> listPostagem, RecyclerView.Adapter adapterRecebido) {
        this.listaPostagem = listPostagem;
        this.adapter = adapterRecebido;
    }

    public void adicionarPostagem(Postagem postagem) {

        // Verifica se o Postagem já está na lista
        if (listaPostagem.contains(postagem)) {
            return;
        }

        // Adiciona o Postagem na lista
        listaPostagem.add(postagem);

        /*
        // Ordena a lista em ordem alfabética
        Collections.sort(listaPostagem, new Comparator<Postagem>() {
            @Override
            public int compare(Postagem u1, Postagem u2) {
                return u1.getTituloPostagem().compareToIgnoreCase(u2.getTituloPostagem());
            }
        });
         */

        adapter.notifyDataSetChanged();
    }

    public void atualizarPostagem(Postagem postagem) {
        int index = -1;
        for (int i = 0; i < listaPostagem.size(); i++) {
            if (listaPostagem.get(i).getIdPostagem().equals(postagem.getIdPostagem())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // Atualiza o Postagem na lista

            //Ignora o nome alterado para não ficar trocando de posição os elementos em tempo real
            //pois isso evita a confusão para o usuário atual.
            if(!listaPostagem.get(index).getTituloPostagem().equals(postagem.getTituloPostagem())
            || !listaPostagem.get(index).getDescricaoPostagem().equals(postagem.getDescricaoPostagem())){
                Log.d("IGNORAR NOME", "Nome alterado: " + postagem.getTituloPostagem());
                Log.d("IGNORAR DESC", "Nome alterado: " + postagem.getDescricaoPostagem());
                return;
            }

            listaPostagem.set(index, postagem);
            /*
            Collections.sort(listaPostagem, new Comparator<Postagem>() {
                @Override
                public int compare(Postagem u1, Postagem u2) {
                    return u1.getTituloPostagem().compareToIgnoreCase(u2.getTituloPostagem());
                }
            });
             */
            Log.d("Atualiza Postagem", "Postagem atualizado com sucesso: " + postagem.getTituloPostagem());
        } else {
            Log.e("Atualiza Postagem", "Erro ao atualizar Postagem: Postagem nao encontrado na lista");
        }
    }


    public void removerPostagem(Postagem postagem) {

        Log.d("TESTE-Remove Postagem", "A REMOVER: " + postagem.getTituloPostagem());
        int position = listaPostagem.indexOf(postagem);
        if (position != -1) {
            listaPostagem.remove(position);
            Log.d("TESTE-Remove Postagem", "Postagem removido com sucesso: " + postagem.getTituloPostagem());
            /*
            Collections.sort(listaPostagem, new Comparator<Postagem>() {
                @Override
                public int compare(Postagem u1, Postagem u2) {
                    return u1.getTituloPostagem().compareToIgnoreCase(u2.getTituloPostagem());
                }
            });
             */
            Log.d("TESTE-Ordenar remoção", "Postagem ordenado com sucesso: " + postagem.getTituloPostagem());
        }else {
            Log.e("TESTE-Remove Postagem", "Erro ao remover Postagem: Postagem nao encontrado na lista");
        }
    }

    public void carregarMaisPostagem(List<Postagem> newPostagem){
        if (newPostagem != null && newPostagem.size() > 0) {
            //*int initSize = listaPostagem.size();
            listaPostagem.addAll(newPostagem);
            //*adapterPostagens.notifyItemRangeInserted(initSize, newPostagem.size());
        }
    }

    public void limparListaPostagems() {
        listaPostagem.clear();
        adapter.notifyDataSetChanged();
    }
}