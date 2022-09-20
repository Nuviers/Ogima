package com.example.ogima.model;

import java.io.Serializable;

public class Contatos implements Serializable {

    private String idContato;
    private String numeroContato;
    private String idNomeContato;
    private String nivelAmizade;

    public Contatos() {
    }

    public String getIdContato() {
        return idContato;
    }

    public void setIdContato(String idContato) {
        this.idContato = idContato;
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
}
