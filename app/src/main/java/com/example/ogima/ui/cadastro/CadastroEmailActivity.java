package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class CadastroEmailActivity extends AppCompatActivity {

    private Button btnInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_email);

        btnInicio = findViewById(R.id.btnInicio);



    }

    public void telaEmail(View view){

        //startActivity(new Intent(CadastroEmailActivity.this, EmailActivity.class));
        startActivity(new Intent(CadastroEmailActivity.this, FotosActivity.class));
        //startActivity(new Intent(CadastroEmailActivity.this, InteresseActivity.class));
        //startActivity(new Intent(CadastroEmailActivity.this, NavigationDrawerActivity.class));
        //startActivity(new Intent(CadastroEmailActivity.this, MainActivity.class));
    }
}


