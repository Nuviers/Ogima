package com.example.ogima.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.ui.intro.IntrodActivity;
import com.google.firebase.database.DatabaseReference;

public class ProblemasLogin extends AppCompatActivity {

    private Button buttonRecupSenha, buttonRecupEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problemas_login);
        Toolbar toolbar = findViewById(R.id.toolbarlogin);
        setSupportActionBar(toolbar);

        //Titulo da toolbar
        setTitle("Problemas no login");

        //Inicializando componentes
        //buttonRecupEmail = findViewById(R.id.buttonRecupEmail);
        //buttonRecupSenha = findViewById(R.id.buttonRecupSenha);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}