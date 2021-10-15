package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.LoginUiActivity;

public class ViewCadastroActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cadastro);



    }

    public void telaLoginEmail(View view){

        Intent intent = new Intent(ViewCadastroActivity.this, LoginUiActivity.class);
        startActivity(intent);
    }

    public void cadastrarEmail(View view){

        Intent intent = new Intent(ViewCadastroActivity.this, CadastroEmailTermosActivity.class);
        startActivity(intent);
    }
}
