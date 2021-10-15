package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class ApelidoActivity extends AppCompatActivity {


    private Button btnContinuarApelido;
    private EditText editApelido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_apelido);



        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarApelido = findViewById(R.id.btnContinuarApelido);
        editApelido = findViewById(R.id.editApelido);

        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */

        btnContinuarApelido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoApelido = editApelido.getText().toString();

                if(!textoApelido.isEmpty()){
                    startActivity(new Intent(ApelidoActivity.this, NascimentoActivity.class));
                }else{
                    Toast.makeText(ApelidoActivity.this,"Digite seu apelido", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



        public void cadContinuarNascimento (View view){

        }

    public void voltarApelido(View view){
        onBackPressed();
    }







        }


