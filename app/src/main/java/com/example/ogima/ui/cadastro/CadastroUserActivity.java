package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class CadastroUserActivity extends AppCompatActivity {

    private Button buttonCadastrarUser, buttonCadGoogle, buttonProblemConta;
    public Usuario usuario;

    private EditText campoEmail, campoSenha;

    private FirebaseAuth autenticacao;

    //

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacaoN = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String apelido;
    private String emailUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        buttonCadastrarUser = findViewById(R.id.buttonCadastrarUser);
        buttonCadGoogle = findViewById(R.id.buttonCadGoogle);
        buttonProblemConta = findViewById(R.id.buttonProblemConta);

        campoEmail = findViewById(R.id.edtLoginEmail);
        campoSenha = findViewById(R.id.edtLoginSenha);

        buttonProblemConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), RecuperarUIActivity.class);
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

                    usuario = new Usuario();

                    usuario.setEmailUsuario(emailConvertido);
                    usuario.setSenhaUsuario(textoSenha);
                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(CadastroUserActivity.this, "Digite seu email e sua senha", Toast.LENGTH_SHORT).show();
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
                            //autenticacao.setLanguageCode("fr");
                            //autenticacao.useAppLanguage();
                            //autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                               // @Override
                                //public void onComplete(@NonNull Task<Void> task) {

                                    //if(task.isSuccessful()){

                                        //Toast.makeText(getApplicationContext(), " Código de verificação enviado para o email" +
                                           //     " " + autenticacao.getCurrentUser().getEmail() + " com sucesso.", Toast.LENGTH_SHORT).show();

                                   // }else{
                                      //  Toast.makeText(getApplicationContext(), "Erro ao enviar o código de verificação " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                   // }

                               // }
                           // });
                            Toast.makeText(CadastroUserActivity.this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();


                            //*Intent intent = new Intent(CadastroUserActivity.this, NomeActivity.class);
                            Intent intent = new Intent(CadastroUserActivity.this, CodigoActivity.class);
                            intent.putExtra("dadosUsuario", usuario);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

