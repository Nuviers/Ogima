package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class DenunciaPostagemActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario, idPostagem;
    private Bundle dados;
    private int numeroDenuncias;
    private String idDonoPostagem, descricaoDenuncia;
    private ImageButton imgButtonBackDenunciaPostagem;
    private EditText edtTextDescricaoDenunciaPostagem;
    private TextView txtViewCaracteresDenuncia;
    private Button btnEnviarDenunciaPostagem;
    private String localUsuario, idDonoComentario;
    private Locale localAtual;
    private DateFormat dateFormat;
    private Date date;
    private String tipoPublicacao;
    private DatabaseReference denunciarPostagemRef;
    private DatabaseReference contadorDenunciaRef;
    private DatabaseReference denunciarComentarioRef;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia_postagem);
        inicializandoComponentes();

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        localAtual = getResources().getConfiguration().locale;
        localUsuario = localUsuario.valueOf(localAtual);

        imgButtonBackDenunciaPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        dados = getIntent().getExtras();

        if(dados != null){
            numeroDenuncias = dados.getInt("numeroDenuncias");
            idDonoPostagem = dados.getString("idDonoPostagem");
            idPostagem = dados.getString("idPostagem");
            //Caso seja denúncia de comentário.
            idDonoComentario = dados.getString("idDonoComentario");
            tipoPublicacao = dados.getString("tipoPublicacao");
        }

        if(numeroDenuncias <= 0){
            numeroDenuncias = 1;
        }else{
            numeroDenuncias = numeroDenuncias + 1;
        }

        edtTextDescricaoDenunciaPostagem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                txtViewCaracteresDenuncia.setText(charSequence.length() + "/1.200");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnEnviarDenunciaPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(idDonoComentario != null){
                    enviarDenunciaComentario();
                }else{
                    enviarDenuncia();
                }
            }
        });


    }

    private void inicializandoComponentes() {
        imgButtonBackDenunciaPostagem = findViewById(R.id.imgButtonBackDenunciaPostagem);
        edtTextDescricaoDenunciaPostagem = findViewById(R.id.edtTextDescricaoDenunciaPostagem);
        txtViewCaracteresDenuncia = findViewById(R.id.txtViewCaracteresDenuncia);
        btnEnviarDenunciaPostagem = findViewById(R.id.btnEnviarDenunciaPostagem);
    }

    private void enviarDenuncia(){
        descricaoDenuncia = edtTextDescricaoDenunciaPostagem.getText().toString();
        if(!descricaoDenuncia.isEmpty()){
            if(descricaoDenuncia.length() > 1200){
                ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido.", getApplicationContext());
            }else{

                if(tipoPublicacao != null){
                    denunciarPostagemRef = firebaseRef
                            .child("postagensDenunciadas").child(idPostagem)
                            .child(idDonoPostagem);
                }else{
                    denunciarPostagemRef = firebaseRef
                            .child("fotosDenunciadas").child(idPostagem)
                            .child(idDonoPostagem);
                }

                HashMap<String, Object> dadosDenuncia = new HashMap<>();

                if (localUsuario.equals("pt_BR")) {
                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                    date = new Date();
                    String novaData = dateFormat.format(date);
                    dadosDenuncia.put("dataDenuncia", novaData);
                } else {
                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                    date = new Date();
                    String novaData = dateFormat.format(date);
                    dadosDenuncia.put("dataDenuncia", novaData);
                }

                dadosDenuncia.put("idDenunciador", idUsuario);
                dadosDenuncia.put("idDenunciado", idDonoPostagem);
                dadosDenuncia.put("idPostagem", idPostagem);
                dadosDenuncia.put("descricaoDenuncia", descricaoDenuncia);
                denunciarPostagemRef.setValue(dadosDenuncia).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            if(tipoPublicacao != null){
                                contadorDenunciaRef = firebaseRef
                                        .child("postagens").child(idDonoPostagem)
                                        .child(idPostagem).child("totalDenuncias");
                            }else{
                                contadorDenunciaRef = firebaseRef
                                        .child("fotosUsuario").child(idDonoPostagem)
                                        .child(idPostagem).child("totalDenuncias");
                            }
                            contadorDenunciaRef.setValue(numeroDenuncias).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        ToastCustomizado.toastCustomizadoCurto("Denúncia enviada com sucesso",getApplicationContext());
                                        finish();
                                    }else{
                                        ToastCustomizado.toastCustomizadoCurto("Erro ao enviar a denúncia, tente novamente!",getApplicationContext());
                                    }
                                }
                            });
                        }else{
                            ToastCustomizado.toastCustomizadoCurto("Erro ao enviar a denúncia, tente novamente!",getApplicationContext());
                        }
                    }
                });
            }
        }else{
            ToastCustomizado.toastCustomizadoCurto("Informe o motivo da denúncia.", getApplicationContext());
        }
    }

    private void enviarDenunciaComentario(){
        descricaoDenuncia = edtTextDescricaoDenunciaPostagem.getText().toString();
        if(!descricaoDenuncia.isEmpty()){
            if(descricaoDenuncia.length() > 1200){
                ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido.", getApplicationContext());
            }else{

                if(tipoPublicacao != null){
                    denunciarComentarioRef = firebaseRef
                            .child("comentariosDenunciadosPostagem").child(idPostagem)
                            .child(idDonoComentario).child(idUsuario);
                }else{
                    denunciarComentarioRef = firebaseRef
                            .child("comentariosDenunciadosFoto").child(idPostagem)
                            .child(idDonoComentario).child(idUsuario);
                }

                HashMap<String, Object> dadosDenunciaComentario = new HashMap<>();

                if (localUsuario.equals("pt_BR")) {
                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                    date = new Date();
                    String novaData = dateFormat.format(date);
                    dadosDenunciaComentario.put("dataDenunciaComentario", novaData);
                } else {
                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                    date = new Date();
                    String novaData = dateFormat.format(date);
                    dadosDenunciaComentario.put("dataDenunciaComentario", novaData);
                }

                dadosDenunciaComentario.put("idDenunciador", idUsuario);
                dadosDenunciaComentario.put("idDenunciado", idDonoComentario);
                dadosDenunciaComentario.put("idPostagem", idPostagem);
                dadosDenunciaComentario.put("descricaoDenunciaComentario", descricaoDenuncia);
                denunciarComentarioRef.setValue(dadosDenunciaComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            if (tipoPublicacao != null) {
                                contadorDenunciaRef = firebaseRef
                                        .child("comentariosPostagem").child(idPostagem)
                                        .child(idDonoComentario).child("totalDenunciasComentario");
                            }else{
                                contadorDenunciaRef = firebaseRef
                                        .child("comentariosFoto").child(idPostagem)
                                        .child(idDonoComentario).child("totalDenunciasComentario");
                            }

                            contadorDenunciaRef.setValue(numeroDenuncias).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        ToastCustomizado.toastCustomizadoCurto("Denúncia enviada com sucesso",getApplicationContext());
                                        finish();
                                    }else{
                                        ToastCustomizado.toastCustomizadoCurto("Erro ao enviar a denúncia, tente novamente!",getApplicationContext());
                                    }
                                }
                            });
                        }else{
                            ToastCustomizado.toastCustomizadoCurto("Erro ao enviar a denúncia, tente novamente!",getApplicationContext());
                        }
                    }
                });
            }
        }else{
            ToastCustomizado.toastCustomizadoCurto("Informe o motivo da denúncia.", getApplicationContext());
        }
    }
}