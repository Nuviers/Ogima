package com.example.ogima.model;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Perfil implements Serializable {

    private String exibirApelido;


    public Perfil() {
    }

    public String getExibirApelido() {
        return exibirApelido;
    }

    public void setExibirApelido(String exibirApelido) {
        this.exibirApelido = exibirApelido;
    }
}

