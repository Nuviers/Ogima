package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

public class FotosActivity extends AppCompatActivity {

    private Button btnContinuarFotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_fotos);

        btnContinuarFotos = findViewById(R.id.btnContinuarFotos);


        btnContinuarFotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(FotosActivity.this, NavigationDrawerActivity.class));

            }
        });

    }



    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }
}
