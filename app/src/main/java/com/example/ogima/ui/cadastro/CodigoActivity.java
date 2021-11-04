package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;

public class CodigoActivity extends AppCompatActivity {


    private Button btnContinuarCodigo;
    private EditText editCodigo;
    private TextView txtMensagemCodigo;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_codigo);

       // getSupportActionBar().hide();
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarCodigo = findViewById(R.id.btnContinuarCodigo);
        editCodigo = findViewById(R.id.editCodigo);
        txtMensagemCodigo = findViewById(R.id.txtMensagemCodigo);

        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */

        //Recebendo Email/Senha
        Bundle dados = getIntent().getExtras();

        if(dados != null){
            usuario = (Usuario) dados.getSerializable("dadosUsuario");
        }

        btnContinuarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoCodigo = editCodigo.getText().toString();

                if(!textoCodigo.isEmpty()){

                    Toast.makeText(getApplicationContext(), usuario.getNumero() + usuario.getNomeUsuario(), Toast.LENGTH_SHORT).show();

                    //Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    //startActivity(intent);
                }else{
                    txtMensagemCodigo.setText("Digite o código recebido por sms");
                }
            }
        });

    }



        public void voltarCodigo (View view){
            onBackPressed();
        }





        }


