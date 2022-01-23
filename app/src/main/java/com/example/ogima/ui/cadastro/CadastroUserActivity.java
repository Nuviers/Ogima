package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.ProblemasLogin;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;

import java.util.Locale;

public class CadastroUserActivity extends AppCompatActivity {

    private Button buttonCadastrarUser, buttonCadGoogle;

    private TextView buttonProblemConta;

    public Usuario usuario;

    private EditText campoEmail, campoSenha;

    private FirebaseAuth autenticacao;

    //

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacaoN = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String apelido;
    private String emailUser;
    private ProgressBar progressBarRegistroEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        buttonCadastrarUser = findViewById(R.id.buttonCadastrarUser);
        buttonCadGoogle = findViewById(R.id.buttonCadGoogle);
        buttonProblemConta = findViewById(R.id.buttonProblemConta);
        progressBarRegistroEmail = findViewById(R.id.progressBarRegistroEmail);

        campoEmail = findViewById(R.id.edtLoginEmail);
        campoSenha = findViewById(R.id.edtLoginSenha);

        buttonProblemConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    progressBarRegistroEmail.setVisibility(View.GONE);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });


        buttonCadastrarUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                //Convertendo email em letras maiúsculas para minúsculas
                String emailConvertido = textoEmail.toLowerCase(Locale.ROOT);

                if(!textoEmail.isEmpty() && !textoSenha.isEmpty()){

                    try{
                        progressBarRegistroEmail.setVisibility(View.VISIBLE);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                    usuario = new Usuario();

                    if(isValidEmail(emailConvertido)){

                        usuario.setEmailUsuario(emailConvertido);
                        usuario.setSenhaUsuario(textoSenha);
                        cadastrarUsuario(usuario);

                    }else{
                        try{
                            progressBarRegistroEmail.setVisibility(View.GONE);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                        ToastCustomizado.toastCustomizado("Digite um email válido, por favor!", getApplicationContext());
                    }

                }else{
                    try{
                        progressBarRegistroEmail.setVisibility(View.GONE);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    ToastCustomizado.toastCustomizado("Digite seu email e sua senha", getApplicationContext());
                }
            }
        });
    }


    public void cadastrarUsuario (final Usuario usuario){
    //public void cadastrarUsuario (Usuario usuario){

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

                            try{
                                progressBarRegistroEmail.setVisibility(View.GONE);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }

                            ToastCustomizado.toastCustomizado("Cadastrado com sucesso", getApplicationContext());

                            //Intent intent = new Intent(CadastroUserActivity.this, NomeActivity.class);
                            Intent intent = new Intent(CadastroUserActivity.this, VerificaEmailActivity.class);
                            intent.putExtra("dadosUsuario", usuario);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            //Tratando exceções
                        }else{

                            try{
                                progressBarRegistroEmail.setVisibility(View.GONE);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }

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
                            ToastCustomizado.toastCustomizado(excecao, getApplicationContext());
                        }
                    }
                }
        );

        //startActivity(new Intent(FotoPerfilActivity.this, NavigationDrawerActivity.class));

    }

    public void cadGoogle(View view){
        Intent intent = new Intent(getApplicationContext(), ViewCadastroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Verificando se email é valido.
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}

