package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.DailyShort;
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

    public void removerUsuario(Usuario usuario) {

        Log.d("TESTE-Remove Usuario", "A REMOVER: " + usuario.getIdUsuario());
        int position = listaUsuario.indexOf(usuario);
        if (position != -1) {
            listaUsuario.remove(position);
            Log.d("TESTE-Remove Usuario", "Usuario removido com sucesso: " + usuario.getIdUsuario());
            /*
            Collections.sort(listaUsuario, new Comparator<Usuario>() {
                @Override
                public int compare(Usuario u1, Usuario u2) {
                    return u1.getTituloUsuario().compareToIgnoreCase(u2.getTituloUsuario());
                }
            });
             */
            Log.d("TESTE-Ordenar remoção", "Usuario ordenado com sucesso: " + usuario.getIdUsuario());
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

    public void limparListaUsuarios() {
        listaUsuario.clear();
        adapter.notifyDataSetChanged();
    }
}
