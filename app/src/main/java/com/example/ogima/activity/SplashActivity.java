package com.example.ogima.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.CacheCleanUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.InfoUserDAO;
import com.example.ogima.helper.NetworkUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Informacoes;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.intro.IntrodActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("MissingPermission")
public class SplashActivity extends AppCompatActivity {

    private int contadorEnvio;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String verificarApelido, testeEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String mensagemToast = "Por favor, conecte seu wifi ou seus dados móveis para acessar sua conta!";
    private Handler handler;
    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef;

    @Override
    protected void onStart() {
        super.onStart();

        //*CacheCleanUtils.scheduleCacheClean(getApplicationContext());

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                limitarEnvio();
                if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
                    verificandoLogin();
                } else {
                    semConexao();
                }
            }
        }, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void limitarEnvio() {

        InfoUserDAO infoUserDAO = new InfoUserDAO(getApplicationContext());
        Informacoes informacoes = new Informacoes();

        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        //DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss");

        //*Passando a data atual para uma string
        String dataRecuperada = f.format(date);

        //Recuperando dados anteriores caso tenha
        infoUserDAO.recuperar(informacoes);

        //Exibindo dados iniciais ou caso tenha recuperado algum serão exibidos

        //Passando valores do contador do DB para um inteiro
        contadorEnvio = informacoes.getContadorAlteracao();

        //Se contador for igual a 10, verifica se a dataSalva é igual a data atual.
        if (contadorEnvio == 10) {
            if (dataRecuperada.equals(informacoes.getDataSalva())) {

            } else {
                //Se as datas forem diferentes, significa que o dado salvo
                //foi antes da data atual assim, resetar o contador.
                informacoes.setContadorAlteracao(1);
                infoUserDAO.atualizar(informacoes);
            }
        }

        //Se o contador já existir e as datas forem diferentes,
        //ele vai reiniciar o contador.
        if (contadorEnvio != 0) {
            if (!dataRecuperada.equals(informacoes.getDataSalva())) {
                informacoes.setContadorAlteracao(1);
                informacoes.setDataSalva(dataRecuperada);
                infoUserDAO.atualizar(informacoes);
            }
        } else {
            //Se o contador não existir, ira inserir um novo dado.
            informacoes.setContadorAlteracao(1);
            informacoes.setDataSalva(dataRecuperada);
            infoUserDAO.salvar(informacoes);
        }
    }

    private void verificandoLogin() {

        // Verifica se o usuário está logado no Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            //Usuário atual não conectado
            usuarioDeslogado();
        } else {
            //Verifica se o usuário atual logado tem dados no firebase
            emailUsuario = autenticacao.getCurrentUser().getEmail();
            idUsuario = Base64Custom.codificarBase64(emailUsuario);
            usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        if (usuario.getIdUsuario() != null) {
                            telaPrincipal();
                        }
                    } else {
                        usuarioDeslogado();
                    }
                    usuarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro: " + error.getMessage(), getApplicationContext());
                }
            });
        }
    }

    private void telaPrincipal() {
        Intent intent = new Intent(SplashActivity.this, NavigationDrawerActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void usuarioDeslogado() {
        Intent intent = new Intent(SplashActivity.this, IntrodActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void semConexao() {
        ToastCustomizado.toastCustomizado(mensagemToast, getApplicationContext());
        Intent intent = new Intent(SplashActivity.this, OfflineActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Para o Handler para liberar os recursos utilizados
        handler.removeCallbacksAndMessages(null);
    }
}