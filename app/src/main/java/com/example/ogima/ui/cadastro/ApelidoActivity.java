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

public class ApelidoActivity extends AppCompatActivity {


    private Button btnContinuarApelido;
    private EditText editApelido;
    private TextView txtMensagemApelido;
    //Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_apelido);

        btnContinuarApelido = findViewById(R.id.btnContinuarApelido);
        editApelido = findViewById(R.id.editApelido);
        txtMensagemApelido = findViewById(R.id.txtMensagemApelido);

        //Recebendo Email/Senha/Nome
        Bundle dados = getIntent().getExtras();
        Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");

        btnContinuarApelido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoApelido = editApelido.getText().toString();

                Toast.makeText(ApelidoActivity.this, "Email "
                        + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                        + " Nome " + usuario.getNomeUsuario(), Toast.LENGTH_LONG).show();

                if(!textoApelido.isEmpty()){

                if(textoApelido.length() > 30){
                    txtMensagemApelido.setText("Limite de caracteres excedido, limite máximo são 30 caracteres");
                }else{
                  //Enviando apelido
                  usuario.setApelidoUsuario(textoApelido);

                  Intent intent = new Intent(ApelidoActivity.this, IdadePessoas.class);
                  intent.putExtra("dadosUsuario", usuario);
                  startActivity(intent);
                }

                }else{
                    txtMensagemApelido.setText("Digite seu apelido");
                }
            }
        });

    }

    public void voltarApelido(View view){
        onBackPressed();
    }

}


