package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class OpcaoSActivity extends AppCompatActivity implements View.OnClickListener {


   // private Button btnContinuarOpcaoS;
    private Button buttonH;
    private Button buttonM;
    private Button buttonTodos;
    private Button buttonContinuarTeste;
    private RadioGroup radioGroupTeste;
    private ToggleButton toggleButtonMasculino;
    private ToggleButton toggleButtonFeminino;
    private String exibirPara = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_opcao_s);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //btnContinuarOpcaoS = findViewById(R.id.btnContinuarOpcaoS);
        buttonH = findViewById(R.id.buttonH);
        buttonM = findViewById(R.id.buttonM);
        buttonTodos = findViewById(R.id.buttonTodos);

        buttonH.setOnClickListener(this);
        buttonM.setOnClickListener(this);
        buttonTodos.setOnClickListener(this);


        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */


    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonH: {
                exibirPara = "Homens";
                //startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
                break;
            }
            case R.id.buttonM: {
                exibirPara = "Mulheres";
                //startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
                break;
            }
            case R.id.buttonTodos: {
                exibirPara = "Todos";
                //startActivity(new Intent(GeneroActivity.this, OpcaoSActivity.class));
                break;
            }
        }
        if(exibirPara == "Homens" || exibirPara =="Mulheres" || exibirPara == "Todos"){
            Toast.makeText(OpcaoSActivity.this, "Seu perfil será exibido para " + exibirPara,Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OpcaoSActivity.this, InteresseActivity.class));
        }

    }


        public void voltarOpcaoS (View view){
            onBackPressed();
        }




        }


