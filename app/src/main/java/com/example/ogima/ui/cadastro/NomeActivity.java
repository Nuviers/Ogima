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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class NomeActivity extends AppCompatActivity {

    private Button btnContinuarNome;
    private EditText editNome;
    //public String textoNome;
    private TextView txtMensagemN;

    private Usuario usuario;

    public String capturedName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_nome);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNome = findViewById(R.id.btnCadastrar);
        editNome = findViewById(R.id.editNome);
        txtMensagemN = findViewById(R.id.txtMensagemN);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null){
            editNome.setText(signInAccount.getDisplayName());
            capturedName = signInAccount.getDisplayName();

            usuario = new Usuario();

            // Pegar o valor dentro do edit se ele for diferente de nulo
            // e colocar nas regras, não se esqueça de instanciar o usuario
            usuario.setNomeUsuario(capturedName);
        }

        Toast.makeText(getApplicationContext(), " Nome " + capturedName , Toast.LENGTH_SHORT).show();


        //Recebendo Email/Senha
        Bundle dados = getIntent().getExtras();

        if(dados != null){
            //usuario = (Usuario) dados.getSerializable("dadosUsuario");
        }

        btnContinuarNome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String textoNome = editNome.getText().toString();

                Toast.makeText(getApplicationContext(), " Teste " +  editNome.getText(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), " Teste2 " +  textoNome, Toast.LENGTH_SHORT).show();
                /*
                Toast.makeText(NomeActivity.this, " Email "
                        +  usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                        + " Número " + usuario.getNumero(), Toast.LENGTH_SHORT).show();

                 */

                if(!textoNome.isEmpty()){

                    if(textoNome.length() > 70){
                        txtMensagemN.setText("Limite de caracteres excedido, limite máximo são 70 caracteres");
                    }else {

                        //Enviando nome pelo objeto
                        //usuario.setNomeUsuario(textoNome);
                        Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                        intent.putExtra("dadosUsuario", usuario);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);


/*
                        Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                        intent.putExtra("dadosUsuario", usuario);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);

 */
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

