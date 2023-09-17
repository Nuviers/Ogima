package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.DailyShort;

import java.util.List;
import java.util.Set;

public class DailyShortDiffDAO {

    private List<DailyShort> listaDailyShort;
    private RecyclerView.Adapter adapter;

    public DailyShortDiffDAO(List<DailyShort> listDailyShort, RecyclerView.Adapter adapterRecebido) {
        this.listaDailyShort = listDailyShort;
        this.adapter = adapterRecebido;
    }

    public void adicionarDailyShort(DailyShort dailyShort) {

        // Verifica se o DailyShort já está na lista
        if (listaDailyShort != null
                && listaDailyShort.size() > 0 && listaDailyShort.contains(dailyShort)) {
            return;
        }

        if (listaDailyShort != null && listaDailyShort.size() == 0) {
            Log.d("DAO", "INICIO ITEM");
            listaDailyShort.add(dailyShort);
        } else if (listaDailyShort != null && listaDailyShort.size() >= 1) {
            Log.d("DAO", "NOVO ITEM");
            listaDailyShort.add(listaDailyShort.size(), dailyShort);
        }

        /*
        // Ordena a lista em ordem alfabética
        Collections.sort(listaDailyShort, new Comparator<DailyShort>() {
            @Override
            public int compare(DailyShort u1, DailyShort u2) {
                return u1.getTituloDailyShort().compareToIgnoreCase(u2.getTituloDailyShort());
            }
        });
         */
    }

    public void removerDailyShort(DailyShort dailyShort) {

        Log.d("TESTE-Remove DailyShort", "A REMOVER: " + dailyShort.getIdDailyShort());
        int position = listaDailyShort.indexOf(dailyShort);
        if (position != -1) {
            listaDailyShort.remove(position);
            Log.d("TESTE-Remove DailyShort", "DailyShort removido com sucesso: " + dailyShort.getIdDailyShort());
            /*
            Collections.sort(listaDailyShort, new Comparator<DailyShort>() {
                @Override
                public int compare(DailyShort u1, DailyShort u2) {
                    return u1.getTituloDailyShort().compareToIgnoreCase(u2.getTituloDailyShort());
                }
            });
             */
            Log.d("TESTE-Ordenar remoção", "DailyShort ordenado com sucesso: " + dailyShort.getIdDailyShort());
        } else {
            Log.e("TESTE-Remove DailyShort", "Erro ao remover DailyShort: DailyShort nao encontrado na lista");
        }
    }

    public void carregarMaisDailyShort(List<DailyShort> newDailyShort, Set<String> idsDailyShorts) {
        if (newDailyShort != null && newDailyShort.size() >= 1) {
            for (DailyShort dailyShort : newDailyShort) {
                if (!idsDailyShorts.contains(dailyShort.getIdDailyShort())) {
                    listaDailyShort.add(dailyShort);
                    idsDailyShorts.add(dailyShort.getIdDailyShort());
                    Log.d("PAGIN", "MAIS DADOS");
                }
            }
        }
    }

    public void limparListaDailyShorts() {
        listaDailyShort.clear();
        adapter.notifyDataSetChanged();
    }
}
