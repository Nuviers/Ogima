package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFotosPostadas;
import com.example.ogima.adapter.AdapterFuncoesPostagem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_postagem);
        inicializandoComponentes();
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        recyclerPostagemDetalhe.setLayoutManager(new LinearLayoutManager(this));
        listaPostagem = new ArrayList<>();
        recyclerPostagemDetalhe.setHasFixedSize(true);

        if(adapterFuncoesPostagem != null){

        }else{
            if(idUsuarioRecebido != null){
                adapterFuncoesPostagem = new AdapterFuncoesPostagem(listaPostagem, getApplicationContext(), idUsuarioRecebido);
            }else{

                listaPostagem.clear();

                DatabaseReference dadosPostagemRef = firebaseRef.child("postagens")
                        .child(idUsuario);

                dadosPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                Postagem postagem = snapshot1.getValue(Postagem.class);
                                listaPostagem.add(postagem);
                                Collections.sort(listaPostagem, new Comparator<Postagem>() {
                                    public int compare(Postagem o1, Postagem o2) {
                                        return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                    }
                                });
                                adapterFuncoesPostagem.notifyDataSetChanged();
                            }
                        }
                        dadosPostagemRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                adapterFuncoesPostagem = new AdapterFuncoesPostagem(listaPostagem, getApplicationContext(), idUsuario);
                recyclerPostagemDetalhe.setAdapter(adapterFuncoesPostagem);
            }
        }
    }

    private void inicializandoComponentes() {
        recyclerPostagemDetalhe = findViewById(R.id.recyclerPostagemDetalhe);
    }
}