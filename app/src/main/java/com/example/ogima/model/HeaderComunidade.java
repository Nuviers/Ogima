package com.example.ogima.model;

import java.io.Serializable;
import java.util.ArrayList;

public class HeaderComunidade implements Serializable {

    private String urlImagem;
    private String urlFundo;
    private String nome;
    private int nrParticipantes;
    private ArrayList<String> topicos;

    public HeaderComunidade() {

    }

    public ArrayList<String> getTopicos() {
        return topicos;
    }

    public void setTopicos(ArrayList<String> topicos) {
        this.topicos = topicos;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getUrlFundo() {
        return urlFundo;
    }

    public void setUrlFundo(String urlFundo) {
        this.urlFundo = urlFundo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNrParticipantes() {
        return nrParticipantes;
    }

    public void setNrParticipantes(int nrParticipantes) {
        this.nrParticipantes = nrParticipantes;
    }
}
