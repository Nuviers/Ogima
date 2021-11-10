package com.example.ogima.activity;

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
import com.example.ogima.ui.cadastro.CadastroUserActivity;
import com.example.ogima.ui.cadastro.ViewCadastroActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class LoginEmailActivity extends AppCompatActivity {

    private Button buttonProblemConta;
    private EditText edtLoginEmail, edtLoginSenha;

    //
    private FirebaseAuth autenticarUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        buttonProblemConta = findViewById(R.id.buttonProblemConta);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginSenha = findViewById(R.id.edtLoginSenha);

        autenticarUsuario = ConfiguracaoFirebase.getFirebaseAutenticacao();


    }

    public void loginUsuario(Usuario usuario){

    autenticarUsuario.signInWithEmailAndPassword(
            usuario.getEmailUsuario(),
            usuario.getSenhaUsuario()
    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {

            if(task.isSuccessful()){

                //Verificar no banco de dados se os dados do usuario estão completos
                // caso não esteja levar pra tela pra continuar o cadastro
                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                startActivity(intent);

            }else{
                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Esta conta não existe";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não correspondem a um usuário cadastrado";
                    }catch (Exception e) {
                        excecao = "Erro ao logar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(), excecao, Toast.LENGTH_SHORT).show();

               // Toast.makeText(getApplicationContext(), "Erro ao efetuar login", Toast.LENGTH_SHORT).show();
            }

        }
    });

    }

        public void validarCredenciaisUsuario(View view){

            String campoEmail = edtLoginEmail.getText().toString();
            String campoSenha = edtLoginSenha.getText().toString();

        if(!campoEmail.isEmpty()){

        }else{
            Toast.makeText(getApplicationContext(), "Digite seu email!",Toast.LENGTH_LONG).show();
        }if(!campoSenha.isEmpty()){

            }else{
                Toast.makeText(getApplicationContext(), "Digite sua senha!",Toast.LENGTH_LONG).show();
            }

        if(!campoEmail.isEmpty() && !campoSenha.isEmpty()){

            Usuario usuario = new Usuario();
            usuario.setEmailUsuario(campoEmail);
            usuario.setSenhaUsuario(campoSenha);

            loginUsuario(usuario);

            //startActivity(new Intent(LoginEmailActivity.this, NavigationDrawerActivity.class));
        }

        }

        public void telaCadastro(View view){
            startActivity(new Intent(LoginEmailActivity.this, ViewCadastroActivity.class));
        }
}
