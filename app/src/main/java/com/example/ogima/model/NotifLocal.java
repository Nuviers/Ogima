package com.example.ogima.model;

import java.io.Serializable;

public class NotifLocal implements Serializable {

    private String idRemetente, idDestinatario, fotoRemetente, nomeRemetente, tipoMensagem, mensagem;
    private long timeStampMensagem;
    private boolean executarAudio;

    public NotifLocal() {
    }

    public NotifLocal(String idRemetente, String idDestinatario, String fotoRemetente, String nomeRemetente, String tipoMensagem, String mensagem, long timeStampMensagem, boolean executarAudio) {
        this.idRemetente = idRemetente;
        this.idDestinatario = idDestinatario;
        this.fotoRemetente = fotoRemetente;
        this.nomeRemetente = nomeRemetente;
        this.tipoMensagem = tipoMensagem;
        this.mensagem = mensagem;
        this.timeStampMensagem = timeStampMensagem;
        this.executarAudio = executarAudio;
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

    public String getFotoRemetente() {
        return fotoRemetente;
    }

    public void setFotoRemetente(String fotoRemetente) {
        this.fotoRemetente = fotoRemetente;
    }

    public String getNomeRemetente() {
        return nomeRemetente;
    }

    public void setNomeRemetente(String nomeRemetente) {
        this.nomeRemetente = nomeRemetente;
    }

    public String getTipoMensagem() {
        return tipoMensagem;
    }

    public void setTipoMensagem(String tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
    }

    public long getTimeStampMensagem() {
        return timeStampMensagem;
    }

    public void setTimeStampMensagem(long timeStampMensagem) {
        this.timeStampMensagem = timeStampMensagem;
    }

    public boolean isExecutarAudio() {
        return executarAudio;
    }

    public void setExecutarAudio(boolean executarAudio) {
        this.executarAudio = executarAudio;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
