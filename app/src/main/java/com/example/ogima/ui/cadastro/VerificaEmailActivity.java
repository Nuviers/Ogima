package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

public class VerificaEmailActivity extends AppCompatActivity {


    private Button btnContinuarCodigo;
    private EditText editCodigo;
    private TextView txtMensagemCodigo;

    private Usuario usuario;
    private TextView textEnviarCodigo, textViewEmailEnviado;

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private FirebaseUser user;
    private String contadorEnvio;
    private String contadorInicio;
    private Timer timer;

    int delay = 5000;   // delay de 5 seg.
    int interval = 3000; // intervalo de 3 seg.
    //int delay = 10000;   // delay de 10 seg.
    //int interval = 2000; // intervalo de 2 seg.

    int delayEnvio = 50000;   // delay para envio de email 50 seg.
    int intervalEnvio = 5000; // intervalo de 5 seg para envio de email.

    //onStart
    @Override
    protected void onStart() {
        super.onStart();

        user.reload();

        if(user.isEmailVerified()){

            timer.purge();
            timer.cancel();

            if(teste != null){
                teste.cancel();
                teste.onFinish();
            }

            Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
            usuario.setStatusEmail("Verificado");
            intent.putExtra("dadosUsuario", usuario);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();


            //delay = 2000;
            //interval = 1000;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_verifica_email);

        btnContinuarCodigo = findViewById(R.id.btnContinuarCodigo);

        txtMensagemCodigo = findViewById(R.id.txtMensagemCodigo);

        textEnviarCodigo = findViewById(R.id.textEnviarCodigo);

        textViewEmailEnviado = findViewById(R.id.textViewEmailEnviado);

        user = autenticacao.getCurrentUser();

        //user.reload();

        usuario = new Usuario();

        //Recebendo Email/Senha
        Bundle dados = getIntent().getExtras();

        //if(dados != null){
            usuario = (Usuario) dados.getSerializable("dadosUsuario");
        //}


        textViewEmailEnviado.setText(usuario.getEmailUsuario());

        //user.reload();

        if(!user.isEmailVerified()){

            initCountDownTimer();
            contadorInicio = "inicio";

        }

        btnContinuarCodigo.setEnabled(false);
        btnContinuarCodigo.setClickable(false);

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // colocar tarefas aqui ...
                user.reload();

                //Verifica se o usuário não é verificado
                if(!user.isEmailVerified()){

                    //txtMensagemCodigo.setText("Verifique seu email para continuar o cadastro");

                    //Verifica se usuário é verificado
                }else{

                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                btnContinuarCodigo.setEnabled(true);
                                btnContinuarCodigo.setText("Continuar");

                                timer.cancel();
                                timer.purge();

                                if(teste != null){
                                    teste.cancel();
                                    teste.onFinish();
                                }

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

                    ToastCustomizado.toastCustomizado(" Link de verificação enviado para o email" +
                            " " + autenticacao.getCurrentUser().getEmail() + " com sucesso", getApplicationContext());
                }else{
                    ToastCustomizado.toastCustomizado("Limite de envio excedido, tente mais tarde", getApplicationContext());
                    txtMensagemCodigo.setText("Limite de envio de email excedido, tente de novo mais tarde!");
                }

            }
        });

        //Se usuário não é verificado o contador inicia
        if(!user.isEmailVerified()){

            teste = new CountDownTimer(50000, 1000) {

                public void onTick(long millisUntilFinished) {
                    txtMensagemCodigo.setText("Espere " + millisUntilFinished / 1000 + " segundos para enviar outro email");
                }

                public void onFinish() {
                    textViewEmailEnviado.setText(usuario.getEmailUsuario());

                    txtMensagemCodigo.setText("");

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


