package com.example.ogima.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Date;

public class Mensagem implements Serializable {

    private String idRemetente;
    private String idDestinatario;
    private String dataMensagem;
    private Date dataMensagemCompleta;
    private String totalMensagensRemetente;
    private String totalMensagensDestinatario;
    private int totalMensagens;
    private String tipoMensagem;
    private String conteudoMensagem;
    private String nomeDocumento;

    public Mensagem() {
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

    public String getNomeDocumento() {
        return nomeDocumento;
    }

    public void setNomeDocumento(String nomeDocumento) {
        this.nomeDocumento = nomeDocumento;
    }

    public String getDataMensagem() {
        return dataMensagem;
    }

    public void setDataMensagem(String dataMensagem) {
        this.dataMensagem = dataMensagem;
    }

    public Date getDataMensagemCompleta() {
        return dataMensagemCompleta;
    }

    public void setDataMensagemCompleta(Date dataMensagemCompleta) {
        this.dataMensagemCompleta = dataMensagemCompleta;
    }

    public String getTotalMensagensRemetente() {
        return totalMensagensRemetente;
    }

    public void setTotalMensagensRemetente(String totalMensagensRemetente) {
        this.totalMensagensRemetente = totalMensagensRemetente;
    }

    public String getTotalMensagensDestinatario() {
        return totalMensagensDestinatario;
    }

    public void setTotalMensagensDestinatario(String totalMensagensDestinatario) {
        this.totalMensagensDestinatario = totalMensagensDestinatario;
    }


    public int getTotalMensagens() {
        return totalMensagens;
    }


    public void setTotalMensagens(int totalMensagens) {
        this.totalMensagens = totalMensagens;
    }

    public String getTipoMensagem() {
        return tipoMensagem;
    }

    public void setTipoMensagem(String tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
    }

    public String getConteudoMensagem() {
        return conteudoMensagem;
    }

    public void setConteudoMensagem(String conteudoMensagem) {
        this.conteudoMensagem = conteudoMensagem;
    }
}
