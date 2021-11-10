package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;

public class SenhaActivity extends AppCompatActivity {


    private Button btnContinuarSenha;
    private EditText editSenha;
    //private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_senha);
        ;

       // getSupportActionBar().hide();
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarSenha = findViewById(R.id.btnContinuarSenha);
        editSenha = findViewById(R.id.editSenha);

        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */

       // btnContinuarSenha.setEnabled(false);

            btnContinuarSenha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String textoSenha = editSenha.getText().toString();

                    if(!textoSenha.isEmpty()){
                        //Intent intent = new Intent(getApplicationContext(), NumeroActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        //startActivity(intent);
                    }else{
                        Toast.makeText(SenhaActivity.this,"Digite sua senha",Toast.LENGTH_SHORT).show();
                    }

                }
            });

    }



        public void voltarSenha (View view){
            onBackPressed();
        }





        }


