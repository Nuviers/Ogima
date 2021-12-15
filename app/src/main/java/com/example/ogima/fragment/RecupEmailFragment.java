package com.example.ogima.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.ProblemasLogin;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.InfoUserDAO;
import com.example.ogima.model.Informacoes;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecupEmailFragment extends Fragment {

    private EditText editTextEmail;
    private Button buttonContinuarEmail;
    private ImageView imageViewFotoUser;
    private String recuperarDado, emailCriptografado, emailConvertido, fotoUsuario;
    private TextView textViewMensagem;
    private ProgressBar progressBarRecup;

    CountDownTimer teste = null;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    //Armazena as tentativas de alterações no Db.
    private int contadorEnvio;

    private Timer timer;
    int delay = 5000;   // delay de 5 seg.
    int interval = 3000; // intervalo de 3 seg.

    public RecupEmailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_recup_email, container, false);

        editTextEmail = view.findViewById(R.id.editTextEmail);
        buttonContinuarEmail = view.findViewById(R.id.buttonContinuarEmail);
        imageViewFotoUser = view.findViewById(R.id.imageViewFotoUser);
        textViewMensagem = view.findViewById(R.id.textViewMensagem);
        progressBarRecup = view.findViewById(R.id.progressBarRecup);


        buttonContinuarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recuperarDado = editTextEmail.getText().toString();
                emailConvertido =  recuperarDado.toLowerCase(Locale.ROOT);

                if(!recuperarDado.isEmpty()){

                    progressBarRecup.setVisibility(View.VISIBLE);
                    emailCriptografado = Base64Custom.codificarBase64(emailConvertido);
                    procurandoUsuario();
                }

            }
        });
        return view;
    }


    public void procurandoUsuario(){
        try{
            DatabaseReference userEmailRef = firebaseRef.child("usuarios").child(emailCriptografado);
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()) {

                        progressBarRecup.setVisibility(View.INVISIBLE);
                        textViewMensagem.setText("Nenhuma conta correspondente a esse email foi localizado");

                        Glide.with(RecupEmailFragment.this)
                                .load(R.drawable.avatarfemale)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .centerCrop()
                                .circleCrop()
                                .into(imageViewFotoUser);

                    }else{
                        progressBarRecup.setVisibility(View.VISIBLE);

                        Toast.makeText(getActivity(), "Dado localizado com sucesso!", Toast.LENGTH_SHORT).show();
                        textViewMensagem.setText("Conta localizada com sucesso");

                        testandoLog();

                        //Trabalha em função de limitar alterações disparadamente.
                        limiteEnvio();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Mudar pra log de erro e tentar armazenar ele
                    Toast.makeText(getActivity(), "Erro " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            userEmailRef.addListenerForSingleValueEvent(eventListener);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void testandoLog(){

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(emailCriptografado);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    progressBarRecup.setVisibility(View.INVISIBLE);

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    fotoUsuario = usuario.getMinhaFoto();

                    if (fotoUsuario != null) {

                        Glide.with(RecupEmailFragment.this)
                                .load(fotoUsuario)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .centerCrop()
                                .circleCrop()
                                .into(imageViewFotoUser);

                    } else {

                    }

                } else if (snapshot == null) {
                    progressBarRecup.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                progressBarRecup.setVisibility(View.INVISIBLE);

                Toast.makeText(getActivity(), "Erro " + error.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void limiteEnvio(){

        InfoUserDAO infoUserDAO = new InfoUserDAO(getActivity());
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
                //Log.i("INFO DB", "Espere 24 horas");
                textViewMensagem.setText("Conta localizada com sucesso, limite de envios " +
                        "atingido, espere até amanhã para poder alterar novamente!");
                Toast.makeText(getActivity(), "Espere 24 horas", Toast.LENGTH_SHORT).show();
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
        }

        if(contadorEnvio >=1 && contadorEnvio < 10){
            contadorEnvio++;
            informacoes.setContadorAlteracao(contadorEnvio);
            informacoes.setDataSalva(dataRecuperada);
            infoUserDAO.atualizar(informacoes); //Atualizando data do servidor

            FirebaseAuth.getInstance().sendPasswordResetEmail(emailConvertido)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                textViewMensagem.setText("Link para redefinição de senha enviado para o email " + emailConvertido);
                                exibirContador();
                            }else{
                                textViewMensagem.setText("Ocorreu um erro ao enviar o link para redefinição de senha, tente novamente");
                            }
                        }
                  });
             }
          }


    public void exibirContador(){

        try{
            buttonContinuarEmail.setClickable(false);
            buttonContinuarEmail.setText("Aguarde para enviar outro email");
        }catch (Exception e){
            e.printStackTrace();
        }

        teste = new CountDownTimer(50000, 1000) {

            public void onTick(long millisUntilFinished) {
                textViewMensagem.setText("Espere " + millisUntilFinished / 1000 + " segundos para enviar outro email");
                buttonContinuarEmail.setEnabled(false);
            }

            public void onFinish() {

                try{
                    buttonContinuarEmail.setClickable(true);
                    buttonContinuarEmail.setEnabled(true);
                    textViewMensagem.setText(" ");
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(teste != null){
                    teste.cancel();
                }
            }
        }.start();
    }
}