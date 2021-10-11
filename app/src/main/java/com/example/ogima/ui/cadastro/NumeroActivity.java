package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class NumeroActivity extends AppCompatActivity {


    private Button btnContinuarNumero;
    private EditText editNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_numero);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNumero = findViewById(R.id.btnContinuarNumero);
        editNumero = findViewById(R.id.editNumero);

        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */

        btnContinuarNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoNumero = editNumero.getText().toString();

                if(!textoNumero.isEmpty()){
                    startActivity(new Intent(NumeroActivity.this, CodigoActivity.class));
                }else{
                    Toast.makeText(NumeroActivity.this,"Digite seu número de telefone", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



        public void voltarNumero (View view){
            onBackPressed();
        }





        }


