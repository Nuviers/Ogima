package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Usuario;

import java.util.List;
import java.util.Set;

public class UsuarioDiffDAO {

    private List<Usuario> listaUsuario;
    private RecyclerView.Adapter adapter;

    public UsuarioDiffDAO(List<Usuario> listUsuario, RecyclerView.Adapter adapterRecebido) {
        this.listaUsuario = listUsuario;
        this.adapter = adapterRecebido;
    }

    public void adicionarUsuario(Usuario usuario) {

        // Verifica se o Usuario já está na lista
        if (listaUsuario != null
                && listaUsuario.size() > 0 && listaUsuario.contains(usuario)) {
            return;
        }

        if (listaUsuario != null && listaUsuario.size() == 0) {
            Log.d("DAO", "INICIO ITEM");
            listaUsuario.add(usuario);
        } else if (listaUsuario != null && listaUsuario.size() >= 1) {
            Log.d("DAO", "NOVO ITEM");
            listaUsuario.add(listaUsuario.size(), usuario);
        }

        /*
        // Ordena a lista em ordem alfabética
        Collections.sort(listaUsuario, new Comparator<Usuario>() {
            @Override
            public int compare(Usuario u1, Usuario u2) {
                return u1.getTituloUsuario().compareToIgnoreCase(u2.getTituloUsuario());
            }
        });
         */
    }

    public void atualizarUsuario(Usuario usuario, String dadoAlvo) {
        int index = -1;
        for (int i = 0; i < listaUsuario.size(); i++) {
            if (listaUsuario.get(i).getIdUsuario().equals(usuario.getIdUsuario())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // Atualiza o Usuario na lista
            Log.d("UsuarioDAO", "Id alteracao: " + listaUsuario.get(index).getIdUsuario());
            //Ignora o nome alterado para não ficar trocando de posição os elementos em tempo real
            //pois isso evita a confusão para o usuário atual.

            if (listaUsuario.get(index).isViewLiberada()) {
                Log.d("Edicao", "dado mudado: " + usuario.isViewLiberada());
            }

            //útil somente se precisar notificar o objeto inteiro ai
            //faz sentido usar o diffcallback.
            //****listaUsuario.set(index, usuario);

            //Somente necessário fazer o set explicitamente se indiferente da visibilidade
            //do item ele será notificado.
            //* listaUsuario.get(index).setEdicaoEmAndamento(usuario.getEdicaoEmAndamento());
            //

            if (dadoAlvo != null) {
                if (dadoAlvo.equals("viewLiberada")) {
                    //FUNCIONA COM PAYLOAD
                    if (usuario.isViewLiberada()) {
                        //FUNCIONA COM PAYLOAD
                        listaUsuario.get(index).setViewLiberada(usuario.isViewLiberada());
                        adapter.notifyItemChanged(index, createPayloadViewLiberada(usuario.isViewLiberada()));
                    }
                }
            }

            /*
            Collections.sort(listaUsuario, new Comparator<Usuario>() {
                @Override
                public int compare(Usuario u1, Usuario u2) {
                    return u1.getTituloUsuario().compareToIgnoreCase(u2.getTituloUsuario());
                }
            });
             */
        } else {
            Log.e("Atualiza Usuario", "Erro ao atualizar Usuario: Usuario nao encontrado na lista");
        }
    }


    public void removerUsuario(Usuario usuario) {

        int position = listaUsuario.indexOf(usuario);
        if (position != -1) {
            listaUsuario.remove(position);
            Log.d("TESTE-Remove Usuario", "A REMOVER: " + usuario.getIdUsuario());
            /*
            Collections.sort(listaUsuario, new Comparator<Usuario>() {
                @Override
                public int compare(Usuario u1, Usuario u2) {
                    return u1.getTituloUsuario().compareToIgnoreCase(u2.getTituloUsuario());
                }
            });
             */
        } else {
            Log.e("TESTE-Remove Usuario", "Erro ao remover Usuario: Usuario nao encontrado na lista");
        }
    }

    public void carregarMaisUsuario(List<Usuario> newUsuario, Set<String> idsUsuarios) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            for (Usuario usuario : newUsuario) {
                if (!idsUsuarios.contains(usuario.getIdUsuario())) {
                    listaUsuario.add(usuario);
                    idsUsuarios.add(usuario.getIdUsuario());
                    Log.d("PAGIN", "MAIS DADOS");
                }
            }
        }
    }

    public void adicionarIdAoSet(Set<String> idsUsers, String idAlvo) {
        if (idsUsers != null && idsUsers.size() > 0
                && idsUsers.contains(idAlvo)) {
            return;
        }
        if (idsUsers != null) {
            idsUsers.add(idAlvo);
        }
    }

    public void limparListaUsuarios() {
        listaUsuario.clear();
        adapter.notifyDataSetChanged();
    }

    private Bundle createPayloadViewLiberada(boolean newViewLiberada) {
        //Criar uma utils para vários tipos de bundle.
        Bundle payload = new Bundle();
        payload.putBoolean("viewLiberada", newViewLiberada);
        return payload;
    }
}