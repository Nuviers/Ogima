package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CodigoActivity extends AppCompatActivity {


    private Button btnContinuarCodigo;
    private EditText editCodigo;
    private TextView txtMensagemCodigo;

    private Usuario usuario;
    private TextView textEnviarCodigo;

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private FirebaseUser user;


    @Override
    protected void onStart() {
        super.onStart();

        if(!user.isEmailVerified()){
            user.reload();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_codigo);

       // getSupportActionBar().hide();
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarCodigo = findViewById(R.id.btnContinuarCodigo);

        txtMensagemCodigo = findViewById(R.id.txtMensagemCodigo);

        textEnviarCodigo = findViewById(R.id.textEnviarCodigo);

        user = autenticacao.getCurrentUser();

        usuario = new Usuario();


        //Recebendo Email/Senha
        Bundle dados = getIntent().getExtras();

        //if(dados != null){
            usuario = (Usuario) dados.getSerializable("dadosUsuario");
        //}

        btnContinuarCodigo.setEnabled(true);


        user.reload();

        btnContinuarCodigo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                user.reload();

                if(!user.isEmailVerified()){

                    autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(getApplicationContext(), " Código de verificação enviado para o email" +
                                        " " + autenticacao.getCurrentUser().getEmail() + " com sucesso.", Toast.LENGTH_SHORT).show();

                                //Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                //startActivity(intent);

                            }else{
                                Toast.makeText(getApplicationContext(), "Erro ao enviar o código de verificação " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }else{
                    user.reload();
                    btnContinuarCodigo.setEnabled(true);

                    Toast.makeText(getApplicationContext(), " Conta verificada", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    usuario.setStatusLogin("Logado");
                    intent.putExtra("dadosUsuario", usuario);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }
        });



        // Color um limitador de time de envio de código por tempo com o time
        // em um campo de texto mostrar cronometro
        textEnviarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            Toast.makeText(getApplicationContext(), " Link de verificação enviado para o email" +
                                    " " + autenticacao.getCurrentUser().getEmail() + " com sucesso.", Toast.LENGTH_SHORT).show();

                            //Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //startActivity(intent);

                        }else{
                            Toast.makeText(getApplicationContext(), "Erro ao enviar o código de verificação " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }
        });

        /*
        btnContinuarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoCodigo = editCodigo.getText().toString();

                if(textoCodigo.isEmpty()){

                    //Toast.makeText(getApplicationContext(), usuario.getNumero() + usuario.getNomeUsuario(), Toast.LENGTH_SHORT).show();

                    autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                    Toast.makeText(getApplicationContext(), " Código de verificação enviado para o email" +
                         " " + autenticacao.getCurrentUser().getEmail() + " com sucesso.", Toast.LENGTH_SHORT).show();

                    //Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //startActivity(intent);

                     }else{
                      Toast.makeText(getApplicationContext(), "Erro ao enviar o código de verificação " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                     }

                     }
                     });
                    //Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    //startActivity(intent);
                }
            }
        });


         */


        if(!user.isEmailVerified()){
           // while(!user.isEmailVerified()){

           // user.reload();

            //}

        }else {

            btnContinuarCodigo.setEnabled(true);

            Toast.makeText(getApplicationContext(), " Conta verificada", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        }



        //if(mAuth.getCurrentUser().isEmailVerified()){

            //Toast.makeText(getApplicationContext(), " Email verificado com sucesso ", Toast.LENGTH_SHORT).show();

       // }else{

            //Toast.makeText(getApplicationContext(), " Email não verificado", Toast.LENGTH_SHORT).show();



    //}


        public void voltarCodigo (View view){
            onBackPressed();
        }



        }


