package com.example.ogima.model;

public class DataModel {
    //Dados que serão enviado junto da notificação

    private String idUsuario;
    private long timestampInteracao;
    private String tipoInteracao;
    private String tipoMensagem;

    public DataModel(String idUsuario, long timestampInteracao, String tipoInteracao, String tipoMensagem) {
        this.idUsuario = idUsuario;
        this.timestampInteracao = timestampInteracao;
        this.tipoInteracao = tipoInteracao;
        this.tipoMensagem = tipoMensagem;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public long getTimestampInteracao() {
        return timestampInteracao;
    }

    public void setTimestampInteracao(long timestampInteracao) {
        this.timestampInteracao = timestampInteracao;
    }

    public String getTipoInteracao() {
        return tipoInteracao;
    }

    public void setTipoInteracao(String tipoInteracao) {
        this.tipoInteracao = tipoInteracao;
    }

    public String getTipoMensagem() {
        return tipoMensagem;
    }

    public void setTipoMensagem(String tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
    }
}
