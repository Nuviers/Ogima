package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class CadastroEmailTermosActivity extends AppCompatActivity {

    private Button btnInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_termos);

        btnInicio = findViewById(R.id.btnInicio);



    }

    public void telaEmail(View view){

        //startActivity(new Intent(CadastroEmailTermosActivity.this, NascimentoActivity.class));
        startActivity(new Intent(CadastroEmailTermosActivity.this, CadastroUserActivity.class));

        //startActivity(new Intent(CadastroEmailTermosActivity.this, NomeActivity.class));
        //startActivity(new Intent(CadastroEmailTermosActivity.this, EmailActivity.class));
        //startActivity(new Intent(CadastroEmailTermosActivity.this, FotosActivity.class));
        //startActivity(new Intent(CadastroEmailTermosActivity.this, InteresseActivity.class));
        //startActivity(new Intent(CadastroEmailTermosActivity.this, NavigationDrawerActivity.class));
        //startActivity(new Intent(CadastroEmailTermosActivity.this, MainActivity.class));
    }
}


