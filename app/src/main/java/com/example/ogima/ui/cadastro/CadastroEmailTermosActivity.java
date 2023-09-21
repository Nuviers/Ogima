package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class CadastroEmailTermosActivity extends AppCompatActivity {

    private Button btnIrParaCadastro;
    private TextView txtViewTilePrivacidade, txtViewTitleCookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_termos);
        inicializandoComponentes();
        clickListeners();
    }

    private void clickListeners() {
        btnIrParaCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CadastroEmailTermosActivity.this, ViewCadastroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        txtViewTilePrivacidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        txtViewTitleCookies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void inicializandoComponentes() {
        btnIrParaCadastro = findViewById(R.id.btnIrParaCadastro);
        txtViewTilePrivacidade = findViewById(R.id.txtViewTilePrivacidade);
        txtViewTitleCookies = findViewById(R.id.txtViewTitleCookies);
    }
}


