package com.example.ogima.model;

import androidx.annotation.Nullable;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class DailyShort implements Serializable {

    private String idDailyShort;
    private String idDonoDailyShort;
    private String tipoMidia;
    private ArrayList<String> listaIdsVisualizadores;
    private String urlMidia;
    private int visualizacoes;
    private long timestampCriacaoDaily;

    public DailyShort() {

    }

    public String getIdDailyShort() {
        return idDailyShort;
    }

    public void setIdDailyShort(String idDailyShort) {
        this.idDailyShort = idDailyShort;
    }

    public String getIdDonoDailyShort() {
        return idDonoDailyShort;
    }

    public void setIdDonoDailyShort(String idDonoDailyShort) {
        this.idDonoDailyShort = idDonoDailyShort;
    }

    public String getTipoMidia() {
        return tipoMidia;
    }

    public void setTipoMidia(String tipoMidia) {
        this.tipoMidia = tipoMidia;
    }

    public ArrayList<String> getListaIdsVisualizadores() {
        return listaIdsVisualizadores;
    }

    public void setListaIdsVisualizadores(ArrayList<String> listaIdsVisualizadores) {
        this.listaIdsVisualizadores = listaIdsVisualizadores;
    }

    public String getUrlMidia() {
        return urlMidia;
    }

    public void setUrlMidia(String urlMidia) {
        this.urlMidia = urlMidia;
    }

    public int getVisualizacoes() {
        return visualizacoes;
    }

    public void setVisualizacoes(int visualizacoes) {
        this.visualizacoes = visualizacoes;
    }

    public long getTimestampCriacaoDaily() {
        return timestampCriacaoDaily;
    }

    public void setTimestampCriacaoDaily(long timestampCriacaoDaily) {
        this.timestampCriacaoDaily = timestampCriacaoDaily;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DailyShort)) return false;
        DailyShort dailyShort = (DailyShort) obj;
        return Objects.equals(getIdDailyShort(), dailyShort.getIdDailyShort());
    }

    @Override
    public int hashCode() {
        //Adicionado para ajudar a verificar igualdade.
        return Objects.hash(getIdDailyShort());
    }
}
