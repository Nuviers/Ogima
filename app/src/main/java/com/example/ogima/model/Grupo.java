package com.example.ogima.model;

import androidx.annotation.Nullable;

import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Grupo implements Serializable {

    private String idGrupo;
    private String idSuperAdmGrupo;
    private String fotoGrupo;
    private String nomeGrupo;
    private String descricaoGrupo;
    private ArrayList<String> participantes;
    private ArrayList<String> admsGrupo;
    //private String dataMensagem;
    //private Date dataMensagemCompleta;
    private ArrayList<String> topicos;
    private Boolean grupoPublico;

    public Grupo() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference grupoRef = firebaseRef.child("grupos");
        String idRandomicoGrupo = grupoRef.push().getKey();
        setIdGrupo(idRandomicoGrupo);
    }

    public ArrayList<String> getAdmsGrupo() {
        return admsGrupo;
    }

    public void setAdmsGrupo(ArrayList<String> admsGrupo) {
        this.admsGrupo = admsGrupo;
    }

    public String getDescricaoGrupo() {
        return descricaoGrupo;
    }

    public void setDescricaoGrupo(String descricaoGrupo) {
        this.descricaoGrupo = descricaoGrupo;
    }

    public ArrayList<String> getTopicos() {
        return topicos;
    }

    public void setTopicos(ArrayList<String> topicos) {
        this.topicos = topicos;
    }

    public String getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(String idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getIdSuperAdmGrupo() {
        return idSuperAdmGrupo;
    }

    public void setIdSuperAdmGrupo(String idSuperAdmGrupo) {
        this.idSuperAdmGrupo = idSuperAdmGrupo;
    }

    public String getFotoGrupo() {
        return fotoGrupo;
    }

    public void setFotoGrupo(String fotoGrupo) {
        this.fotoGrupo = fotoGrupo;
    }

    public String getNomeGrupo() {
        return nomeGrupo;
    }

    public void setNomeGrupo(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }

    public ArrayList<String> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(ArrayList<String> participantes) {
        this.participantes = participantes;
    }

    public Boolean getGrupoPublico() {
        return grupoPublico;
    }

    public void setGrupoPublico(Boolean grupoPublico) {
        this.grupoPublico = grupoPublico;
    }


    //Essencial para o funcionamento do DiffUtilCallback, sem ele a lógica sempre terá erro.
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Grupo)) return false;
        Grupo grupo = (Grupo) obj;
        return Objects.equals(getIdGrupo(), grupo.getIdGrupo());
    }
}
