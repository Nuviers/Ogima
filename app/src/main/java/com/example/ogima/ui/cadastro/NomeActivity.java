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

public class NomeActivity extends AppCompatActivity {

    private Button btnContinuarNome;
    private EditText editNome;
    //public String textoNome;
    private TextView txtMensagemN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_nome);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNome = findViewById(R.id.btnCadastrar);
        editNome = findViewById(R.id.editNome);
        txtMensagemN = findViewById(R.id.txtMensagemN);


        //Recebendo Email/Senha
        Bundle dados = getIntent().getExtras();
        Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");


        btnContinuarNome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String textoNome = editNome.getText().toString();
                Toast.makeText(NomeActivity.this, usuario.getEmailUsuario() + usuario.getSenhaUsuario(), Toast.LENGTH_SHORT).show();
                if(!textoNome.isEmpty()){

                    if(textoNome.length() > 70){
                        txtMensagemN.setText("Limite de caracteres excedido, limite máximo são 70 caracteres");
                    }else {
                        //Enviando nome pelo objeto
                        usuario.setNomeUsuario(textoNome);

                        Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                        intent.putExtra("dadosUsuario", usuario);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }

                   // Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                   // intent.putExtra("valorNome", textoNome);
                   // startActivity(intent);
                    //startActivity(new Intent(NomeActivity.this, ApelidoActivity.class));
                }
                else{
                    txtMensagemN.setText("Digite seu nome");
                }
            }
        });


    }
    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

        }


