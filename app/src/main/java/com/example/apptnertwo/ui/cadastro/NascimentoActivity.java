package com.example.apptnertwo.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptnertwo.R;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;

public class NascimentoActivity extends AppCompatActivity {


    private Button btnContinuarNascimento;
    private EditText editNascimento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_nascimento);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNascimento = findViewById(R.id.btnContinuarNascimento);
        editNascimento = findViewById(R.id.editNascimento);

        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */



        btnContinuarNascimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoNascimento = editNascimento.getText().toString();

                if(!textoNascimento.isEmpty()){
                    startActivity(new Intent(NascimentoActivity.this, GeneroActivity.class));
                }else{
                    Toast.makeText(NascimentoActivity.this,"Digite sua data de nascimento", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



        public void voltarNascimento (View view){
            onBackPressed();
        }


        }


