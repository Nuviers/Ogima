package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterPostagens;
import com.example.ogima.adapter.AdapterPostagensTeste;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.PostagemDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaginacaoTesteActivity extends AppCompatActivity {

    private RecyclerView recyclerTesteAll;
    private ProgressBar progressBarTesteAll;
    private List<Postagem> listaPostagens = new ArrayList<>();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private AdapterPostagensTeste adapterPostagens;
    private LinearLayoutManager linearLayoutManager;
    private String lastKey = null;
    private HashMap<String, Object> lastTimeStamp = null;
    private long timestamp;
    private final static int PAGE_SIZE = 5;

    private boolean isLoading = false;
    private boolean isScrolling = false;

    private GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {};

    private PostagemDiffDAO postagemDiffDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paginacao_teste);
        inicializandoComponentes();

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        configRecycler();

        postagemDiffDAO = new PostagemDiffDAO(listaPostagens, adapterPostagens);

        setLoading(true);
        progressBarTesteAll.setVisibility(View.VISIBLE);

        recupPostagensIniciais();
        configPaginacao();
    }


    private void recupPostagensIniciais() {

        //dar um jeito de ordenar a lista por ordem
        //decrescente

        Query query = firebaseRef.child("postagens")
                .child("cmFmYWJlbmVkZXRmZXJAZ21haWwuY29t")
                .orderByChild("timestampDataPostagem/timestampDataPostagem")
                .limitToFirst(PAGE_SIZE);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    add(snapshot1.getValue(Postagem.class));

                    HashMap<String, Object> timestampMap = snapshot1.child("timestampDataPostagem").getValue(genericTypeIndicator);
                    timestamp = (long) timestampMap.get("timestampDataPostagem");
                    lastKey = snapshot1.getKey();
                }
                //*ToastCustomizado.toastCustomizadoCurto("Last key " + lastKey, getApplicationContext());
                //*ToastCustomizado.toastCustomizadoCurto("Long key " + timestamp, getApplicationContext());
                progressBarTesteAll.setVisibility(View.GONE);
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarTesteAll.setVisibility(View.GONE);
                lastKey = null;
            }
        });

    }


    private void configPaginacao() {

        recyclerTesteAll.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
                //ToastCustomizado.toastCustomizadoCurto("StateChanged " ,getApplicationContext());
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //ToastCustomizado.toastCustomizadoCurto("OnScrolled",getApplicationContext());

                if (isLoading()) {
                    return;
                }

                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                    isScrolling = false;

                    progressBarTesteAll.setVisibility(View.VISIBLE);

                    setLoading(true);

                    // o usuário rolou até o final da lista, exibe mais dois itens
                    //ToastCustomizado.toastCustomizadoCurto("end", getApplicationContext());

                    Query query = firebaseRef.child("postagens")
                            .child("cmFmYWJlbmVkZXRmZXJAZ21haWwuY29t")
                            .orderByChild("timestampDataPostagem/timestampDataPostagem")
                            .startAt(timestamp)
                            .limitToFirst(PAGE_SIZE);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<Postagem> newPostagem = new ArrayList<>();

                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                HashMap<String, Object> timestampMap = snapshot1.child("timestampDataPostagem").getValue(genericTypeIndicator);
                                long key = (long) timestampMap.get("timestampDataPostagem");
                                //*ToastCustomizado.toastCustomizadoCurto("existe " + key, getApplicationContext());
                                if (timestamp != -1 && key != -1 && key != timestamp) {
                                    newPostagem.add(snapshot1.getValue(Postagem.class));
                                    timestamp = key;
                                }
                            }

                            // Remove a última chave usada
                            if (newPostagem.size() > PAGE_SIZE) {
                                newPostagem.remove(0);
                            }

                            if (timestamp != -1) {
                                addAll(newPostagem);
                            }

                            progressBarTesteAll.setVisibility(View.GONE);

                            query.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressBarTesteAll.setVisibility(View.GONE);
                        }
                    });


                }
            }
        });
    }


    private void configRecycler() {

        if (linearLayoutManager != null) {

        } else {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerTesteAll.setHasFixedSize(true);
        recyclerTesteAll.setLayoutManager(linearLayoutManager);


        if (adapterPostagens != null) {

        } else {
            adapterPostagens = new AdapterPostagensTeste(listaPostagens, getApplicationContext());
        }
        recyclerTesteAll.setAdapter(adapterPostagens);

    }


    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void addAll(List<Postagem> newPostagem) {

        if (newPostagem != null && newPostagem.size() > 0) {
            int initSize = listaPostagens.size();
            listaPostagens.addAll(newPostagem);
            adapterPostagens.updatePostagemList(listaPostagens);
            //*adapterPostagens.notifyItemRangeInserted(initSize, newPostagem.size());
        }else{
            progressBarTesteAll.setVisibility(View.GONE);
        }

        setLoading(false);
    }

    public void add(Postagem postagem) {

        listaPostagens.add(postagem);
        adapterPostagens.notifyDataSetChanged();
        setLoading(false);

        adapterPostagens.updatePostagemList(listaPostagens);
    }

    private void inicializandoComponentes() {
        recyclerTesteAll = findViewById(R.id.recyclerTesteAll);
        progressBarTesteAll = findViewById(R.id.progressBarTesteAll);
    }
}