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

public class CadastroUserActivity extends AppCompatActivity {

    private Button buttonCadastrarUser, buttonCadGoogle;
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

                            Toast.makeText(CadastroUserActivity.this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();

                            //startActivity(new Intent(CadastroUserActivity.this, NavigationDrawerActivity.class));
                            Intent intent = new Intent(CadastroUserActivity.this, NomeActivity.class);
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


    public void testandoLog(){
        String emailUsuario = autenticacaoN.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);


        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    Log.i("FIREBASE", usuario.getIdUsuario());
                    Log.i("FIREBASEA", usuario.getNomeUsuario());
                    apelido = usuario.getApelidoUsuario();


                    if(apelido != null){
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        //finish();
                    }else if(snapshot == null) {

                        Toast.makeText(getApplicationContext(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();


                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Continue o cadastro da sua conta", Toast.LENGTH_SHORT).show();


                    emailUser = usuario.getEmailUsuario();

                    usuario.setEmailUsuario(emailUser);

                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    intent.putExtra("dadosUsuario", usuario);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });



    }

}
