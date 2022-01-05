package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.VerificaEmailActivity;
import com.example.ogima.ui.cadastro.ViewCadastroActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginEmailActivity extends AppCompatActivity {

    private Button buttonProblemConta;
    private EditText edtLoginEmail, edtLoginSenha;

    //
    private FirebaseAuth autenticarUsuario;

    //////
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String apelido;
    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        buttonProblemConta = findViewById(R.id.buttonProblemConta);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginSenha = findViewById(R.id.edtLoginSenha);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        autenticarUsuario = ConfiguracaoFirebase.getFirebaseAutenticacao();

        buttonProblemConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    public void loginUsuario(Usuario usuario){

    autenticarUsuario.signInWithEmailAndPassword(
            usuario.getEmailUsuario(),
            usuario.getSenhaUsuario()
    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {

            if(task.isSuccessful()){

                progressBarLogin.setVisibility(View.GONE);
                testandoLog();

                //Verificar no banco de dados se os dados do usuario estão completos
                // caso não esteja levar pra tela pra continuar o cadastro
                //**Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                //**startActivity(intent);

            }else{
                progressBarLogin.setVisibility(View.GONE);
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
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            try {
                progressBarLogin.setVisibility(View.GONE);
            }catch (Exception ex){
                ex.printStackTrace();
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

            try{
                progressBarLogin.setVisibility(View.VISIBLE);
            }catch (Exception ex){
                ex.printStackTrace();
            }

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

    public void testandoLog(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);


        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    //Log.i("FIREBASE", usuario.getIdUsuario());
                    //Log.i("FIREBASEA", usuario.getNomeUsuario());
                    apelido = usuario.getApelidoUsuario();

                    if(apelido != null){
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        //finish();
                    }else if(snapshot == null) {
                        Toast.makeText(getApplicationContext(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Conta não cadastrada", Toast.LENGTH_SHORT).show();

                    //FirebaseAuth.getInstance().signOut();

                    Usuario usuario = new Usuario();

                    String campoEmail = edtLoginEmail.getText().toString();

                    usuario.setEmailUsuario(campoEmail);

                    Intent intent = new Intent(getApplicationContext(), VerificaEmailActivity.class);
                    intent.putExtra("dadosUsuario", usuario);
                    startActivity(intent);
                    finish();

                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), " Ocorreu um erro " + error.getMessage(), Toast.LENGTH_SHORT).show();

                //Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                //startActivity(intent);

            }
        });



    }
}
