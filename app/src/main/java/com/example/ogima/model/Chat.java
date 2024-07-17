package com.example.ogima.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class Chat implements Serializable, Comparator<Chat> {

    private String idUsuario;
    private boolean indisponivel;
    private long timestampLastMsg;
    private String tipoMidiaLastMsg;
    private String conteudoLastMsg;
    private long totalMsg;
    private long totalMsgNaoLida;
    private String idGrupo;
    private String idConversa;
    private String dateLastMsg;

    public String getIdConversa() {
        return idConversa;
    }

    public void setIdConversa(String idConversa) {
        this.idConversa = idConversa;
    }

    public String getDateLastMsg() {
        return dateLastMsg;
    }

    public void setDateLastMsg(String dateLastMsg) {
        this.dateLastMsg = dateLastMsg;
    }

    public String getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(String idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public boolean isIndisponivel() {
        return indisponivel;
    }

    public void setIndisponivel(boolean indisponivel) {
        this.indisponivel = indisponivel;
    }

    public long getTimestampLastMsg() {
        return timestampLastMsg;
    }

    public void setTimestampLastMsg(long timestampLastMsg) {
        this.timestampLastMsg = timestampLastMsg;
    }

    public String getTipoMidiaLastMsg() {
        return tipoMidiaLastMsg;
    }

    public void setTipoMidiaLastMsg(String tipoMidiaLastMsg) {
        this.tipoMidiaLastMsg = tipoMidiaLastMsg;
    }

    public String getConteudoLastMsg() {
        return conteudoLastMsg;
    }

    public void setConteudoLastMsg(String conteudoLastMsg) {
        this.conteudoLastMsg = conteudoLastMsg;
    }

    public long getTotalMsg() {
        return totalMsg;
    }

    public void setTotalMsg(long totalMsg) {
        this.totalMsg = totalMsg;
    }

    public long getTotalMsgNaoLida() {
        return totalMsgNaoLida;
    }

    public void setTotalMsgNaoLida(long totalMsgNaoLida) {
        this.totalMsgNaoLida = totalMsgNaoLida;
    }

    @Override
    public int compare(Chat c1, Chat c2) {
        return Long.compare(c1.getTimestampLastMsg(), c2.getTimestampLastMsg());
    }

    //Essencial para o funcionamento do DiffUtilCallback, sem ele a lógica sempre terá erro.
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Chat chat = (Chat) obj;
        return Objects.equals(getIdUsuario(), chat.getIdUsuario());
    }
}
