package com.example.ogima.helper;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.adapter.AdapterParticipantesComunidade;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;

import java.util.HashMap;
import java.util.List;

public class ComunidadeDAO {

    private List<Comunidade> listaComunidades;
    private int position;
    private Context context;
    private HashMap<String, Integer> hashMapComunidades;

    public ComunidadeDAO(List<Comunidade> listaComunidades, Context context) {
        this.listaComunidades = listaComunidades;
        this.hashMapComunidades = new HashMap<>();
        this.context = context;

        for (int i = 0; i < listaComunidades.size(); i++) {
            hashMapComunidades.put(listaComunidades.get(i).getIdComunidade(), i);
        }
    }

    public void adicionarComunidade(Comunidade comunidadeRecebida, RecyclerView.Adapter adapterComunidade) {

        if (!hashMapComunidades.containsKey(comunidadeRecebida.getIdComunidade())) {
            listaComunidades.add(comunidadeRecebida);
            hashMapComunidades.put(comunidadeRecebida.getIdComunidade(), listaComunidades.size() - 1);
            adapterComunidade.notifyDataSetChanged();
        } else {
            listaComunidades.set(hashMapComunidades.get(comunidadeRecebida.getIdComunidade()), comunidadeRecebida);
        }
    }

    public void removerComunidade(Comunidade comunidadeRecebida, RecyclerView.Adapter adapterComunidade) {

        if (hashMapComunidades.containsKey(comunidadeRecebida.getIdComunidade())) {
            int posicao = hashMapComunidades.get(comunidadeRecebida.getIdComunidade());
            if (posicao != -1) {
                listaComunidades.remove(posicao);
                hashMapComunidades.remove(comunidadeRecebida.getIdComunidade());
                adapterComunidade.notifyItemRemoved(posicao);

                // Atualiza as posições dos usuários no HashMap após a remoção
                for (int i = posicao; i < listaComunidades.size(); i++) {
                    Comunidade comunidadeAtualizado = listaComunidades.get(i);
                    hashMapComunidades.put(comunidadeAtualizado.getIdComunidade(), i);
                }
            }
        }
    }

    public void atualizarComunidade(Comunidade comunidadeAtualizado, RecyclerView.Adapter adapterComunidade) {
        for (int i = 0; i < listaComunidades.size(); i++) {
            Comunidade u = listaComunidades.get(i);
            if (u.getIdComunidade().equals(comunidadeAtualizado.getIdComunidade())) { // compara o id do Firebase com o id local
                listaComunidades.set(i, comunidadeAtualizado); // atualiza o objeto na lista local
                adapterComunidade.notifyItemChanged(i); // notifica o adapter da mudança
                break;
            }
        }
    }

    private int recuperarPosicao(Comunidade comunidade){
        position = listaComunidades.indexOf(comunidade);
        return position;
    }

    public List<Comunidade> listarComunidades() {
        return listaComunidades;
    }

    public void limparListaComunidade(RecyclerView.Adapter adapterComunidade) {
        if (listaComunidades != null && listaComunidades.size() > 0) {
            listaComunidades.clear();
            adapterComunidade.notifyDataSetChanged();
        }
    }
}