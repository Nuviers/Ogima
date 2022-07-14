package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFotosPostadas;
import com.example.ogima.adapter.AdapterFuncoesPostagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetalhesPostagemActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private RecyclerView recyclerPostagemDetalhe;
    private AdapterFuncoesPostagem adapterFuncoesPostagem;
    private List<Postagem> listaPostagem;
    private int receberPosicao;
    private String idUsuarioRecebido;
    private ImageButton imgButtonBackPerfilPostagem;
    private DatabaseReference dadosPostagemRef;
    private TextView txtViewToolbarDetalhes;

    @Override
    protected void onPause() {

        super.onPause();
        pausePlayer(adapterFuncoesPostagem.exoPlayer);

    }

    @Override
    protected void onStop() {

        super.onStop();
        pausePlayer(adapterFuncoesPostagem.exoPlayer);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        releaseExoPlayer(adapterFuncoesPostagem.exoPlayer);
    }

    @Override
    protected void onResume() {

        super.onResume();
        startPlayer(adapterFuncoesPostagem.exoPlayer);
    }

    public static void startPlayer(ExoPlayer exoPlayer) {

        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);

        }
    }

    public static void pausePlayer(ExoPlayer exoPlayer) {

        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);

        }
    }

    public static void releaseExoPlayer(ExoPlayer exoPlayer) {

        if (exoPlayer != null) {
            exoPlayer.release();

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (idUsuarioRecebido != null) {
            finish();
        }else{
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("intentPerfilFragment", "intentPerfilFragment");
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_postagem);
        inicializandoComponentes();
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        imgButtonBackPerfilPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        recyclerPostagemDetalhe.setLayoutManager(new LinearLayoutManager(this));
        listaPostagem = new ArrayList<>();
        recyclerPostagemDetalhe.setHasFixedSize(true);

        Bundle dados = getIntent().getExtras();

        if(dados != null){
            receberPosicao = dados.getInt("atualizarEdicao");
            idUsuarioRecebido = dados.getString("idRecebido");
        }

            if(idUsuarioRecebido != null && !idUsuarioRecebido.equals(idUsuario)){
                txtViewToolbarDetalhes.setText("Postagens");
                dadosPostagemRef = firebaseRef.child("postagens")
                        .child(idUsuarioRecebido);
            }else {
                txtViewToolbarDetalhes.setText("Suas Postagens");
                dadosPostagemRef = firebaseRef.child("postagens")
                        .child(idUsuario);
            }

                dadosPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            if (dados != null && idUsuarioRecebido == null) {
                                adapterFuncoesPostagem.notifyDataSetChanged();
                                recyclerPostagemDetalhe.smoothScrollToPosition(receberPosicao);
                            }
                            listaPostagem.clear();
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                Postagem postagem = snapshot1.getValue(Postagem.class);
                                listaPostagem.add(postagem);
                            }
                            Collections.sort(listaPostagem, new Comparator<Postagem>() {
                                public int compare(Postagem o1, Postagem o2) {
                                    return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                }
                            });
                            adapterFuncoesPostagem.notifyDataSetChanged();
                        }
                        dadosPostagemRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            if (idUsuarioRecebido != null) {
                adapterFuncoesPostagem = new AdapterFuncoesPostagem(listaPostagem, getApplicationContext(), idUsuarioRecebido);
            }else{
                adapterFuncoesPostagem = new AdapterFuncoesPostagem(listaPostagem, getApplicationContext(), idUsuario);
            }

                recyclerPostagemDetalhe.setAdapter(adapterFuncoesPostagem);

    }

    private void inicializandoComponentes() {
        txtViewToolbarDetalhes = findViewById(R.id.txtViewToolbarDetalhes);
        recyclerPostagemDetalhe = findViewById(R.id.recyclerPostagemDetalhe);
        imgButtonBackPerfilPostagem = findViewById(R.id.imgButtonBackPerfilPostagem);
    }

    public void reterPosicao(Context context, int quantidadeFotos, int ultimaPosicao, String indiceItem) {
        if(indiceItem.equals("ultimo")){
            recyclerPostagemDetalhe.smoothScrollToPosition(ultimaPosicao - 1);
        }else{
            if(quantidadeFotos == 1){
            }else{
                recyclerPostagemDetalhe.smoothScrollToPosition(quantidadeFotos - 1);
            }
        }
    }
}