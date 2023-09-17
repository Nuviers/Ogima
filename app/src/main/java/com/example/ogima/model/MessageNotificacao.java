package com.example.ogima.model;

public class MessageNotificacao {

    private String idRemetente;
    private String idDestinatario;
    private String conteudoMensagem;
    private String tipoMensagem;
    private long timestampMensagem;
    private String fotoRemetente;
    private String nomeRemetente;
    private String tipoInteracao;

    public MessageNotificacao(String idRemetente, String conteudoMensagem, String tipoMensagem, long timestampMensagem, String fotoRemetente, String nomeRemetente, String tipoInteracao, String idDestinatario) {
        this.idRemetente = idRemetente;
        this.conteudoMensagem = conteudoMensagem;
        this.tipoMensagem = tipoMensagem;
        this.timestampMensagem = timestampMensagem;
        this.fotoRemetente = fotoRemetente;
        this.nomeRemetente = nomeRemetente;
        this.tipoInteracao = tipoInteracao;
        this.idDestinatario = idDestinatario;
    }

    public MessageNotificacao() {
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public long getTimestampMensagem() {
        return timestampMensagem;
    }

    public void setTimestampMensagem(long timestampMensagem) {
        this.timestampMensagem = timestampMensagem;
    }

    public String getConteudoMensagem() {
        return conteudoMensagem;
    }

    public void setConteudoMensagem(String conteudoMensagem) {
        this.conteudoMensagem = conteudoMensagem;
    }

    public String getTipoMensagem() {
        return tipoMensagem;
    }

    public void setTipoMensagem(String tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
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

    public String getTipoInteracao() {
        return tipoInteracao;
    }

    public void setTipoInteracao(String tipoInteracao) {
        this.tipoInteracao = tipoInteracao;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }
}
