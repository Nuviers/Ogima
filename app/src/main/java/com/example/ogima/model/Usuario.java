package com.example.ogima.model;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Serializable {

    private String idUsuario;
    private String nomeUsuario;
    private String apelidoUsuario;
    private String emailUsuario;
    private String senhaUsuario;
    private String fotoPerfil;
    private String fundoPerfil;
    private String generoUsuario;
    private String opcaoSexualUsuario;
    private String numero;
    private int idade;
    private String dataNascimento;
    private List<String> fotosUsuario;
    private ArrayList<String> interesses;


    public Usuario() {
    }

    public void salvar(){

        DatabaseReference firebaseref = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference usuario = firebaseref.child("usuarios").child(getIdUsuario());

        usuario.setValue(this);

    }

    public ArrayList<String> getInteresses() {
        return interesses;
    }

    public void setInteresses(ArrayList<String> interesses) {
        this.interesses = interesses;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getApelidoUsuario() {
        return apelidoUsuario;
    }

    public void setApelidoUsuario(String apelidoUsuario) {
        this.apelidoUsuario = apelidoUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getSenhaUsuario() {
        return senhaUsuario;
    }

    public void setSenhaUsuario(String senhaUsuario) {
        this.senhaUsuario = senhaUsuario;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getFundoPerfil() {
        return fundoPerfil;
    }

    public void setFundoPerfil(String fundoPerfil) {
        this.fundoPerfil = fundoPerfil;
    }

    public String getGeneroUsuario() {
        return generoUsuario;
    }

    public void setGeneroUsuario(String generoUsuario) {
        this.generoUsuario = generoUsuario;
    }

    public String getOpcaoSexualUsuario() {
        return opcaoSexualUsuario;
    }

    public void setOpcaoSexualUsuario(String opcaoSexualUsuario) {
        this.opcaoSexualUsuario = opcaoSexualUsuario;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public List<String> getFotosUsuario() {
        return fotosUsuario;
    }

    public void setFotosUsuario(List<String> fotosUsuario) {
        this.fotosUsuario = fotosUsuario;
    }
}
