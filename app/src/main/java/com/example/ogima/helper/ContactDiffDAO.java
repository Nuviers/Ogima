package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import com.example.ogima.model.Contatos;

import java.util.List;
import java.util.Set;

public class ContactDiffDAO {

    private List<Contatos> listaContato;
    private RecyclerView.Adapter adapter;

    public ContactDiffDAO(List<Contatos> listContato, RecyclerView.Adapter adapterRecebido) {
        this.listaContato = listContato;
        this.adapter = adapterRecebido;
    }

    public interface RetornaBundleCallback {
        void onBundleRecuperado(int index, Bundle bundleRecup);
    }

    public void adicionarContato(Contatos contatos) {

        if (listaContato != null
                && listaContato.size() > 0 && listaContato.contains(contatos)) {
            return;
        }

        if (listaContato != null && listaContato.size() == 0) {
            Log.d("DAO", "INICIO ITEM");
            listaContato.add(0, contatos);
        } else if (listaContato != null && listaContato.size() >= 1) {
            Log.d("DAO", "NOVO ITEM");
            listaContato.add(contatos);
        }
    }

    public void atualizarContato(Contatos contatos) {
        int index = -1;
        for (int i = 0; i < listaContato.size(); i++) {
            if (listaContato.get(i).getIdContato().equals(contatos.getIdContato())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // Atualiza o Contato na lista
            listaContato.set(index, contatos);
            Log.d("Atualiza Contato", "Contato atualizado com sucesso: ");
        } else {
            Log.e("Atualiza Contato", "Erro ao atualizar Contato: Contato nao encontrado na lista");
        }
    }

    public void atualizarContatoPorPayload(Contatos contatos, String tipoPayload, RetornaBundleCallback callback) {
        int index = -1;
        for (int i = 0; i < listaContato.size(); i++) {
            if (listaContato.get(i).getIdContato().equals(contatos.getIdContato())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            if (tipoPayload != null && !tipoPayload.isEmpty()) {
                switch (tipoPayload) {
                    case "totalMensagens":
                        long totalMsg = 0;
                        if (contatos.getTotalMensagens() > 0) {
                            totalMsg = contatos.getTotalMensagens();
                        }
                        listaContato.get(index).setTotalMensagens(totalMsg);
                        callback.onBundleRecuperado(index, createPayloadLong(tipoPayload, totalMsg));
                        //*adapter.notifyItemChanged(index, createPayloadLong(tipoPayload, totalMsg));
                        break;
                    case "timestampContato":
                        long timestampContato = -1;
                        if (contatos.getTimestampContato() != -1) {
                            timestampContato = contatos.getTimestampContato();
                        }
                        listaContato.get(index).setTimestampContato(timestampContato);
                        callback.onBundleRecuperado(index, createPayloadLong(tipoPayload, timestampContato));
                        //*adapter.notifyItemChanged(index, createPayloadLong(tipoPayload, timestampLastMsg));
                        break;
                    case "contatoFavorito":
                        boolean statusFavorito = false;
                        statusFavorito = contatos.isContatoFavorito();
                        listaContato.get(index).setContatoFavorito(statusFavorito);
                        callback.onBundleRecuperado(index, createPayloadBoolean(tipoPayload, statusFavorito));
                        //*adapter.notifyItemChanged(index, createPayloadBoolean(tipoPayload, statusFavorito));
                        break;
                    case "nivelAmizade":
                        String nivelAmizade = "";
                        if (contatos.getNivelAmizade() != null && !contatos.getNivelAmizade().isEmpty()) {
                            nivelAmizade = contatos.getNivelAmizade();
                        }
                        listaContato.get(index).setNivelAmizade(nivelAmizade);
                        callback.onBundleRecuperado(index, createPayloadString(tipoPayload, nivelAmizade));
                        //*adapter.notifyItemChanged(index, createPayloadString(tipoPayload, nivelAmizade));
                        break;
                }
            }
            Log.d("Atualiza Contato", "Contato atualizado com sucesso: ");
        } else {
            Log.e("Atualiza Contato", "Erro ao atualizar Contato: Contato nao encontrado na lista");
        }
    }


    public void removerContato(Contatos contatos) {

        Log.d("TESTE-Remove Contato", "A REMOVER: " + contatos.getIdContato());
        int position = listaContato.indexOf(contatos);
        if (position != -1) {
            listaContato.remove(position);
            Log.d("TESTE-Remove Contato", "Contato removido com sucesso: " + contatos.getIdContato());
        } else {
            Log.e("TESTE-Remove Contato", "Erro ao remover Contato: Contato nao encontrado na lista");
        }
    }

    public void carregarMaisContato(List<Contatos> newContato, Set<String> idsContatos) {
        if (newContato != null && newContato.size() >= 1) {
            for (Contatos contatos : newContato) {
                if (!idsContatos.contains(contatos.getIdContato())) {
                    listaContato.add(contatos);
                    idsContatos.add(contatos.getIdContato());
                    Log.d("PAGIN", "MAIS DADOS");
                }
            }
        }
    }

    public void adicionarIdAoSet(Set<String> idsContatos, String idAlvo) {
        if (idsContatos != null && idsContatos.size() > 0
                && idsContatos.contains(idAlvo)) {
            return;
        }
        if (idsContatos != null) {
            idsContatos.add(idAlvo);
        }
    }

    public void limparListaContatos() {
        listaContato.clear();
        adapter.notifyDataSetChanged();
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

    private Bundle createPayloadBoolean(String key, boolean dado) {
        Bundle payload = new Bundle();
        payload.putBoolean(key, dado);
        return payload;
    }
}