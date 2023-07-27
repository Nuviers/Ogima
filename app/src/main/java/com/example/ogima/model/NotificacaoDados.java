package com.example.ogima.model;

import com.google.gson.annotations.SerializedName;

public class NotificacaoDados {

    @SerializedName("to")
    private String to;

    @SerializedName("notification")
    private Notificacao notification;

    @SerializedName("data")
    private DataModel dataModel;

    public NotificacaoDados(String to, Notificacao notification, DataModel dataModel) {
        this.to = to;
        this.notification = notification;
        this.dataModel = dataModel;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notificacao getNotification() {
        return notification;
    }

    public void setNotification(Notificacao notification) {
        this.notification = notification;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }
}
