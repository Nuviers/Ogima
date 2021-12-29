package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.InfoUserDAO;
import com.example.ogima.model.Informacoes;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.intro.IntrodActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SplashActivity extends AppCompatActivity {

    private int contadorEnvio;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String testeEmail;
    private String verificarApelido;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        limitarEnvio();

        if (mAuth.getCurrentUser() != null) {
            // Verifica se usuario está logado ou não.
            verificandoLogin();
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, IntrodActivity.class);
                    startActivity(intent);
                    finish();
                }
            },1000);
        }
    }


    public void limitarEnvio(){

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
        if(contadorEnvio == 10){
            if(dataRecuperada.equals(informacoes.getDataSalva())){

            }else{
                //Se as datas forem diferentes, significa que o dado salvo
                //foi antes da data atual assim, resetar o contador.
                informacoes.setContadorAlteracao(1);
                infoUserDAO.atualizar(informacoes);
            }
        }

        //Se o contador já existir e as datas forem diferentes,
        //ele vai reiniciar o contador.
        if(contadorEnvio != 0){
            if(!dataRecuperada.equals(informacoes.getDataSalva())){
                informacoes.setContadorAlteracao(1);
                informacoes.setDataSalva(dataRecuperada);
                infoUserDAO.atualizar(informacoes);
            }
        }else{
            //Se o contador não existir, ira inserir um novo dado.
            informacoes.setContadorAlteracao(1);
            informacoes.setDataSalva(dataRecuperada);
            infoUserDAO.salvar(informacoes);
        }

        /*
        if(contadorA >=1 && contadorA < 10){
            contadorA++;
            informacoes.setContadorAlteracao(contadorA);
            informacoes.setDataSalva(meuTeste);
            infoUserDAO.atualizar(informacoes); //Atualizando data do servidor
        }
         */

    }

    private void verificandoLogin(){
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
                    verificarApelido = usuario.getExibirApelido();
                    testeEmail = usuario.getEmailUsuario();

                    if(verificarApelido != null){
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        finish();
                    }else if(snapshot == null) {
                        Intent intent = new Intent(SplashActivity.this, IntrodActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}