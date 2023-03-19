package com.example.ogima.model;

import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Grupo implements Serializable {


    private String emailUsuario, idUsuario;
    private String idGrupo;
    private String idSuperAdmGrupo;
    private String fotoGrupo;
    private String nomeGrupo;
    private String descricaoGrupo;
    private ArrayList<String> participantes;
    //private String dataMensagem;
    //private Date dataMensagemCompleta;
    private ArrayList<String> topicos;
    private Boolean grupoPublico;

    public Grupo() {

      DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
      FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference grupoRef = firebaseRef.child("grupos");
        String idRandomicoGrupo = grupoRef.push().getKey();
        setIdGrupo(idRandomicoGrupo);
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
}
