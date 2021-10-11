package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class NomeActivity extends AppCompatActivity {


    private Button btnContinuarNome;
    private EditText editNome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_nome);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNome = findViewById(R.id.btnContinuarFotos);
        editNome = findViewById(R.id.editNome);




        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */


        btnContinuarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoNome = editNome.getText().toString();

                if(!textoNome.isEmpty()){
                    startActivity(new Intent(NomeActivity.this, ApelidoActivity.class));
                }else{
                    Toast.makeText(NomeActivity.this,"Digite seu nome", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    public void voltarNome(View view){
        onBackPressed();
    }

        }


