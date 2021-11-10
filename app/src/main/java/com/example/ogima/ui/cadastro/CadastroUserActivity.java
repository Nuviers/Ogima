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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroUserActivity extends AppCompatActivity {

    private Button buttonCadastrarUser, buttonCadGoogle;
    public Usuario usuario;

    private EditText campoEmail, campoSenha;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        buttonCadastrarUser = findViewById(R.id.buttonCadastrarUser);
        buttonCadGoogle = findViewById(R.id.buttonCadGoogle);

        campoEmail = findViewById(R.id.edtLoginEmail);
        campoSenha = findViewById(R.id.edtLoginSenha);



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



    }



    public void cadastrarUsuario (final Usuario usuario){

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
                            //Tratando exceções
                        }else{
                            String excecao = "";
                            try{
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                                excecao = "Digite uma senha mais forte!";
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                excecao = "Digite um e-mail válido";
                            }catch(FirebaseAuthUserCollisionException e){
                                excecao = "Está conta já foi registrada";
                            }catch (Exception e) {
                                excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroUserActivity.this, excecao, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(CadastroUserActivity.this, "Erro ao cadastrar!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        //startActivity(new Intent(FotoPerfilActivity.this, NavigationDrawerActivity.class));

    }

    public void cadGoogle(View view){
        Intent intent = new Intent(getApplicationContext(), ViewCadastroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

}
