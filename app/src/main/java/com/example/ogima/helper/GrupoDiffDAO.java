package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Grupo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GrupoDiffDAO {

    private List<Grupo> listaGrupo;
    private RecyclerView.Adapter adapter;

    public GrupoDiffDAO(List<Grupo> listGrupo, RecyclerView.Adapter adapterRecebido) {
        this.listaGrupo = listGrupo;
        this.adapter = adapterRecebido;
    }

    public interface RetornaBundleCallback {
        void onBundleRecuperado(int index, Bundle bundleRecup);
    }

    public void adicionarGrupo(Grupo grupo) {

        if (listaGrupo != null
                && listaGrupo.size() > 0 && listaGrupo.contains(grupo)) {
            return;
        }

        if (listaGrupo != null && listaGrupo.size() == 0) {
            Log.d("DAO", "INICIO ITEM");
            listaGrupo.add(0, grupo);
        } else if (listaGrupo != null && listaGrupo.size() >= 1) {
            Log.d("DAO", "NOVO ITEM");
            listaGrupo.add(grupo);
        }
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
            listaGrupo.set(index, grupo);
            Log.d("Atualiza Grupo", "Grupo atualizado com sucesso: ");
        } else {
            Log.e("Atualiza Grupo", "Erro ao atualizar Grupo: Grupo nao encontrado na lista");
        }
    }

    public void atualizarGrupoPorPayload(Grupo grupo, String tipoPayload, RetornaBundleCallback callback) {
        int index = -1;
        for (int i = 0; i < listaGrupo.size(); i++) {
            if (listaGrupo.get(i).getIdGrupo().equals(grupo.getIdGrupo())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            if (tipoPayload != null && !tipoPayload.isEmpty()) {
                switch (tipoPayload) {
                    case "nomeGrupo":
                        String nomeGrupo = "";
                        if (grupo.getNomeGrupo() != null && !grupo.getNomeGrupo().isEmpty()) {
                            nomeGrupo = grupo.getNomeGrupo();
                        }
                        listaGrupo.get(index).setNomeGrupo(nomeGrupo);
                        callback.onBundleRecuperado(index, createPayloadString(tipoPayload, nomeGrupo));
                        break;
                    case "descricaoGrupo":
                        String descricaoGrupo = "";
                        if (grupo.getDescricaoGrupo() != null && !grupo.getDescricaoGrupo().isEmpty()) {
                            descricaoGrupo = grupo.getDescricaoGrupo();
                        }
                        listaGrupo.get(index).setDescricaoGrupo(descricaoGrupo);
                        callback.onBundleRecuperado(index, createPayloadString(tipoPayload, descricaoGrupo));
                        break;
                    case "fotoGrupo":
                        String fotoGrupo = "";
                        if (grupo.getFotoGrupo() != null && !grupo.getFotoGrupo().isEmpty()) {
                            fotoGrupo = grupo.getFotoGrupo();
                        }
                        listaGrupo.get(index).setFotoGrupo(fotoGrupo);
                        callback.onBundleRecuperado(index, createPayloadString(tipoPayload, fotoGrupo));
                        break;
                    case "topicosGrupo":
                        ArrayList<String> topicosGrupo = new ArrayList<>();
                        if (grupo.getTopicos() != null && !grupo.getTopicos().isEmpty()) {
                            topicosGrupo = grupo.getTopicos();
                        }
                        listaGrupo.get(index).setTopicos(topicosGrupo);
                        callback.onBundleRecuperado(index, createPayloadArrayListString(tipoPayload, topicosGrupo));
                        break;
                }
            }
            Log.d("Atualiza Grupo", "Grupo atualizado com sucesso: ");
        } else {
            Log.e("Atualiza Grupo", "Erro ao atualizar Grupo: Grupo nao encontrado na lista");
        }
    }


    public void removerGrupo(Grupo grupo) {

        Log.d("TESTE-Remove Grupo", "A REMOVER: " + grupo.getIdGrupo());
        int position = listaGrupo.indexOf(grupo);
        if (position != -1) {
            listaGrupo.remove(position);
            Log.d("TESTE-Remove Grupo", "Grupo removido com sucesso: " + grupo.getIdGrupo());
        } else {
            Log.e("TESTE-Remove Grupo", "Erro ao remover Grupo: Grupo nao encontrado na lista");
        }
    }

    public void carregarMaisGrupo(List<Grupo> newGrupo, Set<String> idsGrupos) {
        if (newGrupo != null && newGrupo.size() >= 1) {
            for (Grupo grupo : newGrupo) {
                if (!idsGrupos.contains(grupo.getIdGrupo())) {
                    listaGrupo.add(grupo);
                    idsGrupos.add(grupo.getIdGrupo());
                    Log.d("PAGIN", "MAIS DADOS");
                }
            }
        }
    }

    public void adicionarIdAoSet(Set<String> idsGrupos, String idAlvo) {
        if (idsGrupos != null && idsGrupos.size() > 0
                && idsGrupos.contains(idAlvo)) {
            return;
        }
        if (idsGrupos != null) {
            idsGrupos.add(idAlvo);
        }
    }

    public void limparListaGrupos() {
        listaGrupo.clear();
        adapter.notifyDataSetChanged();
    }

    private Bundle createPayloadArrayListString(String key, ArrayList<String> dado) {
        Bundle payload = new Bundle();
        payload.putStringArrayList(key, dado);
        return payload;
    }

    private Bundle createPayloadLong(String key, long dado) {
        Bundle payload = new Bundle();
        payload.putLong(key, dado);
        return payload;
    }

    private Bundle createPayloadString(String key, String dado) {
        Bundle payload = new Bundle();
        payload.putString(key, dado);
        return payload;
    }
}