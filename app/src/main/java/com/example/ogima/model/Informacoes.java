package com.example.ogima.model;

import java.util.Date;

public class Informacoes {

    private int contadorAlteracao;
    private Long id;
    private String dataAtual;
    private String dataSalva;

    public Informacoes() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getContadorAlteracao() {
        return contadorAlteracao;
    }

    public void setContadorAlteracao(int contadorAlteracao) {
        this.contadorAlteracao = contadorAlteracao;
    }

    public String getDataAtual() {
        return dataAtual;
    }

    public void setDataAtual(String dataAtual) {
        this.dataAtual = dataAtual;
    }

    public String getDataSalva() {
        return dataSalva;
    }

    public void setDataSalva(String dataSalva) {
        this.dataSalva = dataSalva;
    }
}
