package com.example.ogima.model;

import java.io.Serializable;
import java.util.Comparator;

public class Contatos implements Serializable {

    private String idContato;
    private String numeroContato;
    private String idNomeContato;
    private String nivelAmizade;
    private int totalMensagens;

    public Contatos() {
    }

    public String getIdContato() {
        return idContato;
    }

    public void setIdContato(String idContato) {
        this.idContato = idContato;
    }

    public int getTotalMensagens() {
        return totalMensagens;
    }

    public void setTotalMensagens(int totalMensagens) {
        this.totalMensagens = totalMensagens;
    }

    public String getNivelAmizade() {
        return nivelAmizade;
    }

    public void setNivelAmizade(String nivelAmizade) {
        this.nivelAmizade = nivelAmizade;
    }

    public String getNumeroContato() {
        return numeroContato;
    }

    public void setNumeroContato(String numeroContato) {
        this.numeroContato = numeroContato;
    }

    public String getIdNomeContato() {
        return idNomeContato;
    }

    public void setIdNomeContato(String idNomeContato) {
        this.idNomeContato = idNomeContato;
    }


    public static Comparator<Usuario> nomeCS = new Comparator<Usuario>() {
        @Override
        public int compare(Usuario t2n, Usuario t1n) {
            return t2n.getNomeUsuarioPesquisa().compareTo(t1n.getNomeUsuarioPesquisa());
        }
    };

}
