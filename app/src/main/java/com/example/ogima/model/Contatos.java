package com.example.ogima.model;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

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
    private String nomeContato;

    @Exclude
    private boolean orderByTimeStamp, orderByName;

    public Contatos() {
    }

    public Contatos(boolean orderByTimeStamp, boolean orderByName) {
        this.orderByTimeStamp = orderByTimeStamp;
        this.orderByName = orderByName;
    }

    public String getIdContato() {
        return idContato;
    }

    public void setIdContato(String idContato) {
        this.idContato = idContato;
    }

    public String getNomeContato() {
        return nomeContato;
    }

    public void setNomeContato(String nomeContato) {
        this.nomeContato = nomeContato;
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
        if (orderByTimeStamp) {
            return Long.compare(c1.getTimestampContato(), c2.getTimestampContato());
        } else {
            // Verifica se um dos elementos está como favoritado e o outro não.
            if (c1.isContatoFavorito() && !c2.isContatoFavorito()) {
                return -1; // Primeiro elemento vem antes.
            } else if (!c1.isContatoFavorito() && c2.isContatoFavorito()) {
                return 1; // Segundo Elemento vem antes.
            } else {
                // Ambos são favoritados ou ambos não são.
                if (c1.isContatoFavorito()) {
                    // Ordenados por nome.
                    return c1.getNomeContato().compareTo(c2.getNomeContato());
                } else {
                    // Ordenados por nome.
                    return c1.getNomeContato().compareTo(c2.getNomeContato());
                }
            }
        }
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
