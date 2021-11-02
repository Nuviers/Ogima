package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CadastroUserActivity extends AppCompatActivity {

    private Button buttonCadastrarUser, buttonCadTelefone;
    public Usuario usuario;

    private EditText campoEmail, campoSenha;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        buttonCadastrarUser = findViewById(R.id.buttonCadastrarUser);
        buttonCadTelefone = findViewById(R.id.buttonCadTelefone);

        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);



        buttonCadastrarUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();



                if(!textoEmail.isEmpty() && !textoSenha.isEmpty()){

                    usuario = new Usuario();

                    usuario.setEmailUsuario(textoEmail);
                    usuario.setSenhaUsuario(textoSenha);
                    cadastrarUsuario(usuario);

                    Intent intent = new Intent(CadastroUserActivity.this, NomeActivity.class);
                    intent.putExtra("dadosUsuario", usuario);
                    startActivity(intent);

                }else{
                    Toast.makeText(CadastroUserActivity.this, "Digite seu email e sua senha", Toast.LENGTH_SHORT).show();
                }
            }
        });


        buttonCadTelefone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NumeroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });



    }



    public void cadastrarUsuario (Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmailUsuario(),
                usuario.getSenhaUsuario()
        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            Toast.makeText(CadastroUserActivity.this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();

                            //startActivity(new Intent(CadastroUserActivity.this, NavigationDrawerActivity.class));
                            finish();
                        }else{

                            Toast.makeText(CadastroUserActivity.this, "Erro ao cadastrar!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        //startActivity(new Intent(FotoPerfilActivity.this, NavigationDrawerActivity.class));

    }


    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

}
