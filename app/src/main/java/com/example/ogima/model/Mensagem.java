package com.example.ogima.model;

import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Comparator;
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
    private String tipoArquivo;
    private String duracaoMusica;
    private String idConversa;
    private String talkKey;

    public Mensagem() {
    }

    public String getTalkKey() {
        return talkKey;
    }

    public void setTalkKey(String talkKey) {
        this.talkKey = talkKey;
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

    public String getDuracaoMusica() {
        return duracaoMusica;
    }

    public void setDuracaoMusica(String duracaoMusica) {
        this.duracaoMusica = duracaoMusica;
    }

    public String getIdConversa() {
        return idConversa;
    }

    public void setIdConversa(String idConversa) {
        this.idConversa = idConversa;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }

    public void setTipoArquivo(String tipoArquivo) {
        this.tipoArquivo = tipoArquivo;
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

    public static Comparator<Mensagem> ultimaMensagem = new Comparator<Mensagem>() {
        @Override
        public int compare(Mensagem t2n, Mensagem t1n) {
            return t1n.getDataMensagem().compareTo(t2n.getDataMensagem());
        }
    };
}
