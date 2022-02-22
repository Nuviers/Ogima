package com.example.ogima.model;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.UsuarioFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Usuario implements Serializable {

    private String idUsuario;
    private String nomeUsuario;
    private String nomeUsuarioPesquisa;
    private String apelidoUsuario;
    private String apelidoUsuarioPesquisa;
    private String emailUsuario;
    private String senhaUsuario;
    private String fotoPerfil;
    private String fundoPerfil;
    private String generoUsuario;
    private String opcaoSexualUsuario;
    private String numero;
    private int idade;
    private int seguidoresUsuario;
    private int seguindoUsuario;
    private int amigosUsuario;
    private int pedidosAmizade;
    private int viewsPerfil;
    private String dataNascimento;
    private List<String> fotosUsuario;
    private ArrayList<String> interesses;

    private String caminhoFotoPerfil;
    private String caminhoFotoPerfilFundo;

    private String minhaFoto; //Foto de perfil
    private String meuFundo; // Foto de fundo

    private String statusEmail;
    private String exibirApelido;
    private String epilepsia;

    public Usuario() {
    }


    public void salvar(){

        DatabaseReference firebaseref = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference usuario = firebaseref.child("usuarios").child(getIdUsuario());

        usuario.setValue(this);

    }

    //Aula do whats 230, o id também tá como criptografado
    //public void atualizar(){

    //String identificadorUsuario = UsuarioFirebase.getIdUsuarioCriptografado();
    // DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();

    //  DatabaseReference usuariosRef = database.child("usuarios")
    //    .child(identificadorUsuario);

    //Map<String, Object> valoresUsuario = converterparaMap();

    //usuariosRef.updateChildren(valoresUsuario);
    // }
/*
    @Exclude
    public Map<String, Object> converterparaMap(){

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("emailUsuario", getEmailUsuario());
        usuarioMap.put("apelidoUsuario", getApelidoUsuario());
        usuarioMap.put("nomeUsuario", getNomeUsuario());
        usuarioMap.put("generoUsuario", getGeneroUsuario());
        usuarioMap.put("dataNascimento", getDataNascimento());
        usuarioMap.put("idade", getIdade());
        usuarioMap.put("interesses", getInteresses());
        usuarioMap.put("fotoPerfil", getCaminhoFotoPerfil());
        usuarioMap.put("fotoPerfilFundo", getCaminhoFotoPerfilFundo());

        return usuarioMap;
    }

 */

    public int getViewsPerfil() {
        return viewsPerfil;
    }

    public void setViewsPerfil(int viewsPerfil) {
        this.viewsPerfil = viewsPerfil;
    }

    public int getPedidosAmizade() {
        return pedidosAmizade;
    }

    public void setPedidosAmizade(int pedidosAmizade) {
        this.pedidosAmizade = pedidosAmizade;
    }

    public int getSeguidoresUsuario() {
        return seguidoresUsuario;
    }

    public void setSeguidoresUsuario(int seguidoresUsuario) {
        this.seguidoresUsuario = seguidoresUsuario;
    }

    public int getSeguindoUsuario() {
        return seguindoUsuario;
    }

    public void setSeguindoUsuario(int seguindoUsuario) {
        this.seguindoUsuario = seguindoUsuario;
    }

    public int getAmigosUsuario() {
        return amigosUsuario;
    }

    public void setAmigosUsuario(int amigosUsuario) {
        this.amigosUsuario = amigosUsuario;
    }

    public String getNomeUsuarioPesquisa() {
        return nomeUsuarioPesquisa;
    }

    public void setNomeUsuarioPesquisa(String nomeUsuarioPesquisa) {
        this.nomeUsuarioPesquisa = nomeUsuarioPesquisa;
    }

    public String getApelidoUsuarioPesquisa() {
        return apelidoUsuarioPesquisa;
    }

    public void setApelidoUsuarioPesquisa(String apelidoUsuarioPesquisa) {
        this.apelidoUsuarioPesquisa = apelidoUsuarioPesquisa;
    }

    public String getEpilepsia() {
        return epilepsia;
    }

    public void setEpilepsia(String epilepsia) {
        this.epilepsia = epilepsia;
    }

    public String getExibirApelido() {
        return exibirApelido;
    }

    public void setExibirApelido(String exibirApelido) {
        this.exibirApelido = exibirApelido;
    }

    public String getStatusEmail() {
        return statusEmail;
    }

    public void setStatusEmail(String statusEmail) {
        this.statusEmail = statusEmail;
    }

    public String getMeuFundo() {
        return meuFundo;
    }

    public void setMeuFundo(String meuFundo) {
        this.meuFundo = meuFundo;
    }

    public String getMinhaFoto() {
        return minhaFoto;
    }

    public void setMinhaFoto(String minhaFoto) {
        this.minhaFoto = minhaFoto;
    }

    public String getCaminhoFotoPerfilFundo() {
        return caminhoFotoPerfilFundo;
    }

    public void setCaminhoFotoPerfilFundo(String caminhoFotoPerfilFundo) {
        this.caminhoFotoPerfilFundo = caminhoFotoPerfilFundo;
    }

    public String getCaminhoFotoPerfil() {
        return caminhoFotoPerfil;
    }

    public void setCaminhoFotoPerfil(String caminhoFotoPerfil) {
        this.caminhoFotoPerfil = caminhoFotoPerfil;
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

    //Tirar o exclude somente depois de cripotagrafar a senha do usuario
    @Exclude
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