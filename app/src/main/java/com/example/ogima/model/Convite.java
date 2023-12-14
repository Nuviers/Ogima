package com.example.ogima.model;

import androidx.annotation.Nullable;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class Convite implements Serializable {

    private String idConvite;
    private String idComunidade;
    private String idRemetente;
    private String idDestinatario;
    private Map<String, Object> timeStampConvite;
    private long timestampinteracao;

    public Convite() {

    }

    public long getTimestampinteracao() {
        return timestampinteracao;
    }

    public void setTimestampinteracao(long timestampinteracao) {
        this.timestampinteracao = timestampinteracao;
    }

    public String getIdConvite() {
        return idConvite;
    }

    public void setIdConvite(String idConvite) {
        this.idConvite = idConvite;
    }

    public String getIdComunidade() {
        return idComunidade;
    }

    public void setIdComunidade(String idComunidade) {
        this.idComunidade = idComunidade;
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public Map<String, Object> getTimeStampConvite() {
        return timeStampConvite;
    }

    public void setTimeStampConvite(Map<String, Object> timeStampConvite) {
        this.timeStampConvite = timeStampConvite;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Convite)) return false;
        Convite convite = (Convite) obj;
        return Objects.equals(getIdComunidade(), convite.getIdComunidade());
    }
}
