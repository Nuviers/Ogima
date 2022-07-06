package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterComentarios;
import com.example.ogima.adapter.AdapterCurtidasPostagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Postagem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurtidasPostagemActivity extends AppCompatActivity {

    private RecyclerView recyclerCurtidasPostagem;
    private ImageButton imgButtonBackLikePostagem;
    private List<Postagem> listaCurtidas = new ArrayList<>();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef, verificarCurtidasRef, exibirCurtidasRef;
    private AdapterCurtidasPostagem adapterCurtidasPostagem;
    private String idPostagem;
    private Postagem postagemCurtida;
    private String tipoPublicacao;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtidas_postagem);
        inicializandoComponentes();

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        Bundle dados = getIntent().getExtras();

        try{
            if(dados != null){
                idPostagem = dados.getString("idPostagem");
                tipoPublicacao = dados.getString("tipoPublicacao");
            }

            if(idPostagem != null){

                if(tipoPublicacao != null){
                    exibirCurtidasRef = firebaseRef
                            .child("curtidasPostagem")
                            .child(idPostagem);
                }else{
                    exibirCurtidasRef = firebaseRef
                            .child("curtidasFoto")
                            .child(idPostagem);
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

        //Configurações do recycler
        recyclerCurtidasPostagem.setLayoutManager(new LinearLayoutManager(this));
        recyclerCurtidasPostagem.setHasFixedSize(true);
        adapterCurtidasPostagem = new AdapterCurtidasPostagem(listaCurtidas, getApplicationContext(), tipoPublicacao);
        recyclerCurtidasPostagem.setAdapter(adapterCurtidasPostagem);

        imgButtonBackLikePostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Adicionando dados ao recyclerView
        exibirCurtidasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapChildren : snapshot.getChildren()){
                    postagemCurtida = snapChildren.getValue(Postagem.class);
                    listaCurtidas.add(postagemCurtida);
                    Collections.sort(listaCurtidas, Postagem.PostagemCurtidaDS);
                    adapterCurtidasPostagem.notifyDataSetChanged();
                }
                exibirCurtidasRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializandoComponentes() {
        recyclerCurtidasPostagem = findViewById(R.id.recyclerCurtidasPostagem);
        imgButtonBackLikePostagem = findViewById(R.id.imgButtonBackLikePostagem);
    }
}