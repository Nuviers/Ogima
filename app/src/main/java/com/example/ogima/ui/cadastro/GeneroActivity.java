package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class GeneroActivity extends AppCompatActivity implements View.OnClickListener {


    //private Button btnContinuarGenero;
    private Button buttonHomem;
    private Button buttonMulher;
    private Button buttonOutros;
    private String euSou;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_genero);


        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //btnContinuarGenero = findViewById(R.id.btnContinuarGenero);
        buttonHomem = findViewById(R.id.buttonHomem);
        buttonMulher = findViewById(R.id.buttonMulher);
        buttonOutros = findViewById(R.id.buttonOutros);


        buttonHomem.setOnClickListener(this);
        buttonMulher.setOnClickListener(this);
        buttonOutros.setOnClickListener(this);
        //btnContinuarGenero.setOnClickListener(this);

        //euSou = "";

        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */


    }




    public void cadContinuarOpcaoS(View view) {

        }
/*
        if(euSou == "Homem" || euSou =="Mulher" || euSou == "Outros"){
            startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
        }


 */



    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonHomem: {
                    euSou = "Homem";
                //startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
                break;
            }
            case R.id.buttonMulher: {
                euSou = "Mulher";
                //startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
                break;
            }
            case R.id.buttonOutros: {
                euSou = "Outros";
                //startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
                break;
            }
        }
        if(euSou == "Homem" || euSou =="Mulher" || euSou == "Outros"){
            Toast.makeText(GeneroActivity.this, "Salvo genêro: " + euSou + " com sucesso",Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
            startActivity(new Intent(GeneroActivity.this, InteresseActivity.class));
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void voltarGenero(View view){
        onBackPressed();
    }
}








