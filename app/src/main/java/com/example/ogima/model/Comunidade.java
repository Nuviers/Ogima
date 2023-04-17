package com.example.ogima.model;

import androidx.annotation.Nullable;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Comunidade implements Serializable {

    private String idComunidade;
    private String idSuperAdmComunidade;
    private String fotoComunidade;
    private String nomeComunidade;
    private String descricaoComunidade;
    private ArrayList<String> participantes;
    private ArrayList<String> admsComunidade;
    private ArrayList<String> topicos;
    private Boolean comunidadePublica;

    public Comunidade() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference comunidadeRef = firebaseRef.child("comunidades");
        String idRandomicoComunidade = comunidadeRef.push().getKey();
        setIdComunidade(idRandomicoComunidade);
    }

    public String getIdComunidade() {
        return idComunidade;
    }

    public void setIdComunidade(String idComunidade) {
        this.idComunidade = idComunidade;
    }

    public String getIdSuperAdmComunidade() {
        return idSuperAdmComunidade;
    }

    public void setIdSuperAdmComunidade(String idSuperAdmComunidade) {
        this.idSuperAdmComunidade = idSuperAdmComunidade;
    }

    public String getFotoComunidade() {
        return fotoComunidade;
    }

    public void setFotoComunidade(String fotoComunidade) {
        this.fotoComunidade = fotoComunidade;
    }

    public String getNomeComunidade() {
        return nomeComunidade;
    }

    public void setNomeComunidade(String nomeComunidade) {
        this.nomeComunidade = nomeComunidade;
    }

    public String getDescricaoComunidade() {
        return descricaoComunidade;
    }

    public void setDescricaoComunidade(String descricaoComunidade) {
        this.descricaoComunidade = descricaoComunidade;
    }

    public ArrayList<String> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(ArrayList<String> participantes) {
        this.participantes = participantes;
    }

    public ArrayList<String> getAdmsComunidade() {
        return admsComunidade;
    }

    public void setAdmsComunidade(ArrayList<String> admsComunidade) {
        this.admsComunidade = admsComunidade;
    }

    public ArrayList<String> getTopicos() {
        return topicos;
    }

    public void setTopicos(ArrayList<String> topicos) {
        this.topicos = topicos;
    }

    public Boolean getComunidadePublica() {
        return comunidadePublica;
    }

    public void setComunidadePublica(Boolean comunidadePublica) {
        this.comunidadePublica = comunidadePublica;
    }

    //Essencial para o funcionamento do DiffUtilCallback, sem ele a lógica sempre terá erro.
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Comunidade)) return false;
        Comunidade comunidade = (Comunidade) obj;
        return Objects.equals(getIdComunidade(), comunidade.getIdComunidade());
    }
}
