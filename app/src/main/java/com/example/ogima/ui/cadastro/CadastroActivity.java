package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.ui.login.LoginActivity;

public class CadastroActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);



    }

    public void telaLoginEmail(View view){

        Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void cadastrarEmail(View view){

        Intent intent = new Intent(CadastroActivity.this, CadastroEmailActivity.class);
        startActivity(intent);
    }
}
