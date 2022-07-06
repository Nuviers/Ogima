package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFotosPostadas;
import com.example.ogima.adapter.AdapterSeguidores;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FotosPostadasActivity extends AppCompatActivity {

    private ImageButton imageButtonBackFtPostada;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    //Variaveis do recycler
    private RecyclerView recyclerFotosPostadas;
    private AdapterFotosPostadas adapterFotosPostadas;
    private List<Postagem> listaFotosPostadas;
    private int receberPosicao;
    private String idUsuarioRecebido;
    private DatabaseReference baseFotosPostagemRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos_postadas);
        inicializarComponentes();
        Toolbar toolbar = findViewById(R.id.toolbarFotosPostadas);
        setSupportActionBar(toolbar);
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurações iniciais
        setTitle("");
        recyclerFotosPostadas.setLayoutManager(new LinearLayoutManager(this));
        listaFotosPostadas = new ArrayList<>();
        recyclerFotosPostadas.setHasFixedSize(true);

        Bundle dados = getIntent().getExtras();

        if(dados != null){
            receberPosicao = dados.getInt("atualizarEdicao");
            idUsuarioRecebido = dados.getString("idRecebido");
        }

        adapterFotosPostadas = new AdapterFotosPostadas(listaFotosPostadas, getApplicationContext(),idUsuarioRecebido);
        recyclerFotosPostadas.setAdapter(adapterFotosPostadas);

        if(idUsuarioRecebido != null){
            baseFotosPostagemRef = firebaseRef
                    .child("fotosUsuario").child(idUsuarioRecebido);
        }else{
            baseFotosPostagemRef = firebaseRef
                    .child("fotosUsuario").child(idUsuario);
        }

            baseFotosPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for(DataSnapshot ds : snapshot.getChildren()){
                        Postagem postagem = ds.getValue(Postagem.class);

                        if(dados != null && idUsuarioRecebido == null){
                            adapterFotosPostadas.notifyDataSetChanged();
                            recyclerFotosPostadas.smoothScrollToPosition(receberPosicao);
                        }

                        if(snapshot.getValue() != null){
                            if(idUsuarioRecebido != null && !idUsuario.equals(postagem.getIdDonoPostagem())){
                                if(postagem.getPublicoPostagem().equals("Todos")){
                                    listaFotosPostadas.add(postagem);
                                    adapterFotosPostadas.notifyDataSetChanged();
                                }else if (postagem.getPublicoPostagem().equals("Somente amigos")){
                                    DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                            .child(idUsuario).child(idUsuarioRecebido);
                                    analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                listaFotosPostadas.add(postagem);
                                                adapterFotosPostadas.notifyDataSetChanged();
                                            }
                                            analisaAmizadeRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }else if (postagem.getPublicoPostagem().equals("Somente seguidores")){
                                    DatabaseReference analisaSeguidorRef = firebaseRef.child("seguindo")
                                            .child(idUsuario).child(idUsuarioRecebido);
                                    analisaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                listaFotosPostadas.add(postagem);
                                                adapterFotosPostadas.notifyDataSetChanged();
                                            }
                                            analisaSeguidorRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }else if (postagem.getPublicoPostagem().equals("Somente amigos e seguidores")){
                                    DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                            .child(idUsuario).child(idUsuarioRecebido);
                                    analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                DatabaseReference analisaSeguidorRef = firebaseRef.child("seguindo")
                                                        .child(idUsuario).child(idUsuarioRecebido);
                                                analisaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            listaFotosPostadas.add(postagem);
                                                            adapterFotosPostadas.notifyDataSetChanged();
                                                        }
                                                        analisaSeguidorRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                            analisaAmizadeRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }else{
                                listaFotosPostadas.add(postagem);
                                adapterFotosPostadas.notifyDataSetChanged();
                            }
                        }
                        baseFotosPostagemRef.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        imageButtonBackFtPostada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.putExtra("atualize","atualize");
                startActivity(intent);
                finish();
            }
        });
    }

    private void inicializarComponentes() {
        recyclerFotosPostadas = findViewById(R.id.recyclerViewFotosPostadas);
        imageButtonBackFtPostada = findViewById(R.id.imageButtonBackFtPostada);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        intent.putExtra("atualize","atualize");
        startActivity(intent);
        finish();
    }

    public void reterPosicao(Context context, int quantidadeFotos, int ultimaPosicao, String indiceItem){
        if(indiceItem.equals("ultimo")){
            recyclerFotosPostadas.smoothScrollToPosition(ultimaPosicao - 1);
        }else{
            if(quantidadeFotos == 1){
            }else{
                recyclerFotosPostadas.smoothScrollToPosition(quantidadeFotos - 1);
            }
        }
    }
}