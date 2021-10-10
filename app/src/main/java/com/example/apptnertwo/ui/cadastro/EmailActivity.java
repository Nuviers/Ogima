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

public class EmailActivity extends AppCompatActivity {


    private Button btnContinuarEmail;
    private EditText editEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_email);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarEmail = findViewById(R.id.btnContinuarEmail);
        editEmail = findViewById(R.id.editEmail);

        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */

            btnContinuarEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String textoEmail = editEmail.getText().toString();
                    if(!textoEmail.isEmpty()){
                        startActivity(new Intent(EmailActivity.this, SenhaActivity.class));
                    }else{
                        Toast.makeText(EmailActivity.this,"Digite seu email",Toast.LENGTH_SHORT).show();
                    }

                }
            });

    }



        public void voltarEmail (View view){
            onBackPressed();
        }





        }


