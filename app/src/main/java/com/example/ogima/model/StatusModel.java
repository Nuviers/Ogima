package com.example.ogima.model;

public class StatusModel {

    Integer resourceStatus;
    String nameUser;

    public Integer getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(Integer resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public StatusModel (Integer resourceStatus, String nameUser){

        this.resourceStatus = resourceStatus;
        this.nameUser = nameUser;

    }
}
