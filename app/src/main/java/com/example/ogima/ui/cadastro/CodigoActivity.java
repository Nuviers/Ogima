package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.util.Timer;
import java.util.TimerTask;

public class CodigoActivity extends AppCompatActivity {


    private Button btnContinuarCodigo;
    private EditText editCodigo;
    private TextView txtMensagemCodigo;

    private Usuario usuario;
    private TextView textEnviarCodigo, textViewEmailEnviado;

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private FirebaseUser user;
    private String contadorEnvio;
    private String contadorInicio;

    int delay = 10000;   // delay de 10 seg.
    int interval = 2000; // intervalo de 2 seg.

    int delayEnvio = 50000;   // delay para envio de email 50 seg.
    int intervalEnvio = 5000; // intervalo de 5 seg para envio de email.

    @Override
    protected void onStart() {
        super.onStart();

        if(!user.isEmailVerified()){
            user.reload();
        }else{

            Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
            usuario.setStatusEmail("Verificado");
            intent.putExtra("dadosUsuario", usuario);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_codigo);

        btnContinuarCodigo = findViewById(R.id.btnContinuarCodigo);

        txtMensagemCodigo = findViewById(R.id.txtMensagemCodigo);

        textEnviarCodigo = findViewById(R.id.textEnviarCodigo);

        textViewEmailEnviado = findViewById(R.id.textViewEmailEnviado);

        user = autenticacao.getCurrentUser();

        usuario = new Usuario();

        //Recebendo Email/Senha
        Bundle dados = getIntent().getExtras();

        //if(dados != null){
            usuario = (Usuario) dados.getSerializable("dadosUsuario");
        //}


        textViewEmailEnviado.setText(usuario.getEmailUsuario());

        user.reload();

        if(!user.isEmailVerified()){

            initCountDownTimer();
            contadorInicio = "inicio";

        }



        btnContinuarCodigo.setEnabled(false);
        btnContinuarCodigo.setClickable(false);

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // colocar tarefas aqui ...
                user.reload();

                if(!user.isEmailVerified()){

                    //txtMensagemCodigo.setText("Verifique seu email para continuar o cadastro");

                }else{

                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                btnContinuarCodigo.setEnabled(true);
                                btnContinuarCodigo.setText("Continuar");

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    usuario.setStatusEmail("Verificado");
                    intent.putExtra("dadosUsuario", usuario);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                    timer.cancel();
                    timer.purge();

                    teste.cancel();
                    teste.onFinish();
                }

            }
        }, delay, interval);


        // Color um limitador de time de envio de código por tempo com o time
        // em um campo de texto mostrar cronometro
        textEnviarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(contadorInicio.equals("concluiu") && !user.isEmailVerified()){

                    initCountDownTimer();

                }

            }
        });


        if(user.isEmailVerified()){
            btnContinuarCodigo.setEnabled(true);
            Toast.makeText(getApplicationContext(), " Conta verificada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
     }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    CountDownTimer teste = null;

    public void initCountDownTimer() {

        textEnviarCodigo.setClickable(false);

        autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(getApplicationContext(), " Link de verificação enviado para o email" +
                            " " + autenticacao.getCurrentUser().getEmail() + " com sucesso.", Toast.LENGTH_SHORT).show();

                    //Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //startActivity(intent);

                    //contadorEnvio = null;


                }else{
                    Toast.makeText(getApplicationContext(), "Limite de envio excedido, tente mais tarde", Toast.LENGTH_SHORT).show();
                    txtMensagemCodigo.setText("Limite de envio de email excedido, tente de novo mais tarde!");

                }

            }
        });

        if(!user.isEmailVerified()){

            teste = new CountDownTimer(50000, 1000) {

                public void onTick(long millisUntilFinished) {
                    txtMensagemCodigo.setText("Espere " + millisUntilFinished / 1000 + " segundos para enviar outro email");
                }

                public void onFinish() {
                    textViewEmailEnviado.setText(usuario.getEmailUsuario());

                    contadorEnvio = "Okay";

                    contadorInicio = "concluiu";

                    if(teste != null){
                        teste.cancel();
                    }

                    if(contadorEnvio.equals("Okay")){

                        textEnviarCodigo.setClickable(true);
                        //btnContinuarCodigo.setClickable(true);
                    }

                }
            }.start();
        }
        }

}


