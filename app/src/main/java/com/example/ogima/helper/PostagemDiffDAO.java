package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Postagem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PostagemDiffDAO {

    private List<Postagem> listaPostagem;
    private RecyclerView.Adapter adapter;

    public PostagemDiffDAO(List<Postagem> listPostagem, RecyclerView.Adapter adapterRecebido) {
        this.listaPostagem = listPostagem;
        this.adapter = adapterRecebido;
    }

    public void adicionarPostagem(Postagem postagem) {

        // Verifica se o Postagem já está na lista
        if (listaPostagem != null
                && listaPostagem.size() > 0 && listaPostagem.contains(postagem)) {
            return;
        }

        if (listaPostagem != null && listaPostagem.size() == 0) {
            Log.d("DAO", "INICIO ITEM");
            listaPostagem.add(postagem);
        } else if (listaPostagem != null && listaPostagem.size() >= 1) {
            Log.d("DAO", "NOVO ITEM");
            listaPostagem.add(listaPostagem.size(), postagem);
        }

        /*
        // Ordena a lista em ordem alfabética
        Collections.sort(listaPostagem, new Comparator<Postagem>() {
            @Override
            public int compare(Postagem u1, Postagem u2) {
                return u1.getTituloPostagem().compareToIgnoreCase(u2.getTituloPostagem());
            }
        });
         */
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
            Log.d("PostagemDAO", "Id alteracao: " + listaPostagem.get(index).getIdPostagem());
            //Ignora o nome alterado para não ficar trocando de posição os elementos em tempo real
            //pois isso evita a confusão para o usuário atual.

            if (listaPostagem.get(index).getTituloPostagem() != null && !listaPostagem.get(index).getTituloPostagem().equals(postagem.getTituloPostagem())
                    || listaPostagem.get(index).getDescricaoPostagem() != null && !listaPostagem.get(index).getDescricaoPostagem().equals(postagem.getDescricaoPostagem())) {
                Log.d("IGNORAR NOME", "Nome alterado: " + postagem.getTituloPostagem());
                Log.d("IGNORAR DESC", "Descrição alterada: " + postagem.getDescricaoPostagem());
                return;
            }

            if (listaPostagem.get(index).getEdicaoEmAndamento() != null) {
                Log.d("Edicao", "dado mudado: " + postagem.getEdicaoEmAndamento());
            }

            //útil somente se precisar notificar o objeto inteiro ai
            //faz sentido usar o diffcallback.
            //****listaPostagem.set(index, postagem);

            //Somente necessário fazer o set explicitamente se indiferente da visibilidade
            //do item ele será notificado.
            //* listaPostagem.get(index).setEdicaoEmAndamento(postagem.getEdicaoEmAndamento());
            //

            //FUNCIONA COM PAYLOAD
            listaPostagem.get(index).setEdicaoEmAndamento(postagem.getEdicaoEmAndamento());
            adapter.notifyItemChanged(index, createPayload(postagem.getEdicaoEmAndamento()));
            //FUNCIONA COM PAYLOAD

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
        } else {
            Log.e("TESTE-Remove Postagem", "Erro ao remover Postagem: Postagem nao encontrado na lista");
        }
    }

    public void carregarMaisPostagem(List<Postagem> newPostagem, Set<String> idsPostagens) {
        if (newPostagem != null && newPostagem.size() >= 1) {
            for (Postagem postagem : newPostagem) {
                if (!idsPostagens.contains(postagem.getIdPostagem())) {
                    listaPostagem.add(postagem);
                    idsPostagens.add(postagem.getIdPostagem());
                    Log.d("PAGIN", "MAIS DADOS");
                }
            }
        }
    }

    public void limparListaPostagems() {
        listaPostagem.clear();
        adapter.notifyDataSetChanged();
    }

    private Bundle createPayload(Boolean newEdicao) {
        //Criar uma utils para vários tipos de bundle.
        Bundle payload = new Bundle();
        payload.putBoolean("edicaoAndamento", newEdicao);
        return payload;
    }
}