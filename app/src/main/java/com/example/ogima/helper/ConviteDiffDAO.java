package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Convite;
import com.example.ogima.model.Usuario;

import java.util.List;
import java.util.Set;

public class ConviteDiffDAO {

    private List<Convite> listaConvite;
    private RecyclerView.Adapter adapter;

    public ConviteDiffDAO(List<Convite> listConvite, RecyclerView.Adapter adapterRecebido) {
        this.listaConvite = listConvite;
        this.adapter = adapterRecebido;
    }

    public void adicionarConvite(Convite convite) {

        // Verifica se o Convite já está na lista
        if (listaConvite != null
                && listaConvite.size() > 0 && listaConvite.contains(convite)) {
            return;
        }

        if (listaConvite != null && listaConvite.size() == 0) {
            listaConvite.add(convite);
        } else if (listaConvite != null && listaConvite.size() >= 1) {
            listaConvite.add(listaConvite.size(), convite);
        }
    }

    public void removerConvite(Convite convite) {
        int position = listaConvite.indexOf(convite);
        if (position != -1) {
            listaConvite.remove(position);
        }
    }

    public void carregarMaisConvite(List<Convite> newConvite, Set<String> idsConvites) {
        if (newConvite != null && newConvite.size() >= 1) {
            for (Convite convite : newConvite) {
                if (!idsConvites.contains(convite.getIdComunidade())) {
                    listaConvite.add(convite);
                    idsConvites.add(convite.getIdComunidade());
                }
            }
        }
    }

    public void limparListaConvites() {
        listaConvite.clear();
        adapter.notifyDataSetChanged();
    }
}