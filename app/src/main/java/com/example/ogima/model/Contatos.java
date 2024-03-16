package com.example.ogima.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class Contatos implements Serializable, Comparator<Contatos> {

    private String idContato;
    private String numeroContato;
    private String idNomeContato;
    private String nivelAmizade;
    private long totalMensagens;
    private int mensagensPerdidas;
    private boolean contatoFavorito;
    private long timestampContato;
    private boolean indisponivel;

    public Contatos() {
    }

    public String getIdContato() {
        return idContato;
    }

    public void setIdContato(String idContato) {
        this.idContato = idContato;
    }

    public boolean isIndisponivel() {
        return indisponivel;
    }

    public void setIndisponivel(boolean indisponivel) {
        this.indisponivel = indisponivel;
    }

    public long getTimestampContato() {
        return timestampContato;
    }

    public void setTimestampContato(long timestampContato) {
        this.timestampContato = timestampContato;
    }

    public boolean isContatoFavorito() {
        return contatoFavorito;
    }

    public void setContatoFavorito(boolean contatoFavorito) {
        this.contatoFavorito = contatoFavorito;
    }

    public long getTotalMensagens() {
        return totalMensagens;
    }

    public void setTotalMensagens(long totalMensagens) {
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

    public int getMensagensPerdidas() {
        return mensagensPerdidas;
    }

    public void setMensagensPerdidas(int mensagensPerdidas) {
        this.mensagensPerdidas = mensagensPerdidas;
    }

    @Override
    public int compare(Contatos c1, Contatos c2) {
        return Long.compare(c1.getTimestampContato(), c2.getTimestampContato());
    }

    //Essencial para o funcionamento do DiffUtilCallback, sem ele a lógica sempre terá erro.
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Contatos contato = (Contatos) obj;
        return Objects.equals(getIdContato(), contato.getIdContato());
    }
}
