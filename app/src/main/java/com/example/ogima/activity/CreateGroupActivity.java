package com.example.ogima.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ogima.R;

public class CreateGroupActivity extends AppCompatActivity {

    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        inicializarComponentes();
        setSupportActionBar(toolbarIncPadrao);
        configInicial();
    }

    private void configInicial(){
        setTitle("");
    }

    private void inicializarComponentes() {
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
    }
}