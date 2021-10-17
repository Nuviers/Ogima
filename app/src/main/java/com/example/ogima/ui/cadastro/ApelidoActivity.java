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

public class ApelidoActivity extends AppCompatActivity {


    private Button btnContinuarApelido;
    private EditText editApelido;
    //Usuario usuario;

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

                //Bundle receberNome = getIntent().getExtras();
                //String nomeRecebido = receberNome.getString("valorNome");

                if(!textoApelido.isEmpty()){

                //Recebendo Email/Senha/Nome
                Bundle dados = getIntent().getExtras();
                Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");

                Toast.makeText(ApelidoActivity.this, "Email "
                + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                + " Nome " + usuario.getNomeUsuario(), Toast.LENGTH_LONG).show();

                  //Enviando apelido
                  usuario.setApelidoUsuario(textoApelido);

                  Intent intent = new Intent(ApelidoActivity.this, NascimentoActivity.class);
                  intent.putExtra("dadosUsuario", usuario);
                  startActivity(intent);

                    //Toast.makeText(getApplicationContext(), "Seu nome é " + nomeRecebido, Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(ApelidoActivity.this, NascimentoActivity.class);
                    //intent.putExtra("nomeMeu", nomeRecebido);
                    //startActivity(intent);
                    //startActivity(new Intent(ApelidoActivity.this, NascimentoActivity.class));
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


