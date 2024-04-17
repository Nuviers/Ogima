package com.example.ogima.model;

import androidx.annotation.Nullable;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Comunidade implements Serializable, Comparator<Comunidade> {

    @Exclude
    public static final String PUBLIC_COMMUNITY = "Comunidades públicas";
    @Exclude
    public static final String COMMUNITY_FOLLOWING = "Comunidades que você segue";
    @Exclude
    public static final String MY_COMMUNITY = "Suas comunidades";
    @Exclude
    public static final String RECOMMENDED_COMMUNITY = "Comunidades recomendadas";
    @Exclude
    public static final String ALL_COMMUNITIES = "Todas as comunidades";

    @Exclude
    private boolean orderByTimestamp, orderByName;

    private String idComunidade;
    private String idSuperAdmComunidade;
    private String fotoComunidade;
    private String fundoComunidade;
    private String nomeComunidade;
    private String nomeComunidadePesquisa;
    private String descricaoComunidade;
    private ArrayList<String> seguidores;
    private ArrayList<String> admsComunidade;
    private ArrayList<String> topicos;
    private Boolean comunidadePublica;
    private boolean indisponivel;
    private long timestampinteracao;
    private long nrParticipantes;
    private long nrAdms;
    private String idParticipante;
    private boolean administrator;
    private String idChatComunidade;
    private ArrayList<String> participantes;

    public Comunidade() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference comunidadeRef = firebaseRef.child("comunidades");
        String idRandomicoComunidade = comunidadeRef.push().getKey();
        setIdComunidade(idRandomicoComunidade);
    }

    public Comunidade(boolean orderByTimestamp, boolean orderByName) {
        this.orderByTimestamp = orderByTimestamp;
        this.orderByName = orderByName;
    }

    public ArrayList<String> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(ArrayList<String> participantes) {
        this.participantes = participantes;
    }

    public String getIdChatComunidade() {
        return idChatComunidade;
    }

    public void setIdChatComunidade(String idChatComunidade) {
        this.idChatComunidade = idChatComunidade;
    }

    public String getNomeComunidadePesquisa() {
        return nomeComunidadePesquisa;
    }

    public void setNomeComunidadePesquisa(String nomeComunidadePesquisa) {
        this.nomeComunidadePesquisa = nomeComunidadePesquisa;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public String getIdParticipante() {
        return idParticipante;
    }

    public void setIdParticipante(String idParticipante) {
        this.idParticipante = idParticipante;
    }

    public long getNrAdms() {
        return nrAdms;
    }

    public void setNrAdms(long nrAdms) {
        this.nrAdms = nrAdms;
    }

    public long getNrParticipantes() {
        return nrParticipantes;
    }

    public void setNrParticipantes(long nrParticipantes) {
        this.nrParticipantes = nrParticipantes;
    }

    public long getTimestampinteracao() {
        return timestampinteracao;
    }

    public void setTimestampinteracao(long timestampinteracao) {
        this.timestampinteracao = timestampinteracao;
    }

    public boolean isIndisponivel() {
        return indisponivel;
    }

    public void setIndisponivel(boolean indisponivel) {
        this.indisponivel = indisponivel;
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

    public String getFundoComunidade() {
        return fundoComunidade;
    }

    public void setFundoComunidade(String fundoComunidade) {
        this.fundoComunidade = fundoComunidade;
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

    public ArrayList<String> getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(ArrayList<String> seguidores) {
        this.seguidores = seguidores;
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


    @Override
    public int compare(Comunidade c1, Comunidade c2) {
        if (orderByTimestamp) {
            return Long.compare(c1.getTimestampinteracao(), c2.getTimestampinteracao());
        } else if(orderByName) {
            // Comparação com base em outra propriedade
            // Retorne o valor desejado de acordo com o critério de comparação
            String nome1, nome2;

            nome1 = c1.getNomeComunidade();
            nome2 = c2.getNomeComunidade();

            return nome1.compareToIgnoreCase(nome2);
        }else{
            return c1.getIdComunidade().compareToIgnoreCase(c2.getIdComunidade());
        }
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
