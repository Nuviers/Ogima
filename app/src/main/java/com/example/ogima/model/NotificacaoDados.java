package com.example.ogima.model;

import com.google.gson.annotations.SerializedName;

public class NotificacaoDados {

    @SerializedName("to")
    private String to;

    @SerializedName("data")
    private MessageNotificacao messageNotificacao;
    private DataModel dataModel;

    public NotificacaoDados(String to, MessageNotificacao messageNotificacao) {
        this.to = to;
        this.messageNotificacao = messageNotificacao;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public MessageNotificacao getMessageNotificacao() {
        return messageNotificacao;
    }

    public void setMessageNotificacao(MessageNotificacao messageNotificacao) {
        this.messageNotificacao = messageNotificacao;
    }
}
