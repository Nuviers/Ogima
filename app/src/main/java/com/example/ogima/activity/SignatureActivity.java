package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;

import java.io.Serializable;

public class SignatureActivity extends AppCompatActivity implements Serializable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);


        Bundle dados = getIntent().getExtras();

        //Salvar também o id no nó de seguidor algo do tipo ou pegar o já salvo. Pois está passando o id como nulo

        if(dados != null){

        }

    }
}