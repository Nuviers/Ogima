package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterPostagensComunidade;
import com.example.ogima.adapter.AdapterPostagensTeste;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;

import com.example.ogima.helper.PostagemDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PaginacaoTesteActivity extends AppCompatActivity implements AdapterPostagensTeste.RemoverPostagemListener, AdapterPostagensTeste.RecuperaPosicaoAnterior {

    private RecyclerView recyclerTesteAll;
    private ProgressBar progressBarTesteAll;
    private List<Postagem> listaPostagens = new ArrayList<>();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private AdapterPostagensTeste adapterPostagens;
    private LinearLayoutManager linearLayoutManager;
    private long timestamp;
    private final static int PAGE_SIZE = 5; // mudar para 10

    private boolean isLoading = false;
    private boolean isScrolling = false;

    private PostagemDiffDAO postagemDiffDAO;

    private ChildEventListener childEventListenerInicio, childEventListenerLoadMore;

    //Retorna para posição anterior
    private int mCurrentPosition = 0;


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPosition = savedInstanceState.getInt("current_position");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 && mCurrentPosition > 0) {
            recyclerTesteAll.scrollToPosition(mCurrentPosition);
            mCurrentPosition = 0;
        }
    }

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

        recuperarPostagensIniciais();
        configPaginacao();
    }

    private void configPaginacao() {

        recyclerTesteAll.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
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

                    // o usuário rolou até o final da lista, exibe mais cinco itens
                    carregarMaisDados();
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
            adapterPostagens = new AdapterPostagensTeste(listaPostagens, getApplicationContext(), this::onComunidadeRemocao, this::onPosicaoAnterior);
        }
        recyclerTesteAll.setAdapter(adapterPostagens);

    }

    private void recuperarPostagensIniciais() {

        Query query = firebaseRef.child("postagens")
                .child("cmFmYWJlbmVkZXRmZXJAZ21haWwuY29t")
                .orderByChild("timeStampNegativo")
                .limitToFirst(PAGE_SIZE);

        if (childEventListenerInicio != null) {
            query.removeEventListener(childEventListenerInicio);
            childEventListenerInicio = null;
        }

        limparLista();

        childEventListenerInicio = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    adicionarPostagem(snapshot.getValue(Postagem.class));
                    timestamp = snapshot.child("timeStampNegativo").getValue(Long.class);
                }
                //*ToastCustomizado.toastCustomizadoCurto("Last key " + lastKey, getApplicationContext());
                //*ToastCustomizado.toastCustomizadoCurto("Long key " + timestamp, getApplicationContext());
                progressBarTesteAll.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    //ToastCustomizado.toastCustomizadoCurto("Atualizado",getApplicationContext());

                    // Recupera a postagem do snapshot
                    Postagem postagemAtualizada = snapshot.getValue(Postagem.class);

                    // Atualiza a postagem na lista
                    postagemDiffDAO.atualizarPostagem(postagemAtualizada, null);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterPostagens.updatePostagemList(listaPostagens);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    // Recupera a postagem do snapshot
                    Postagem postagemRemovida = snapshot.getValue(Postagem.class);
                    Log.d("TESTE-On Child Removed", "Postagem removida: " + postagemRemovida.getIdPostagem());

                    // Remove a postagem da lista
                    postagemDiffDAO.removerPostagem(postagemRemovida);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterPostagens.updatePostagemList(listaPostagens);
                    Log.d("TESTE-On Child Removed", "Adapter notificado com sucesso");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarTesteAll.setVisibility(View.GONE);
                timestamp = -1;
            }
        });
    }


    private void carregarMaisDados() {

        Query queryMaisDados = firebaseRef.child("postagens")
                .child("cmFmYWJlbmVkZXRmZXJAZ21haWwuY29t")
                .orderByChild("timeStampNegativo")
                .startAt(timestamp)
                .limitToFirst(PAGE_SIZE);


        if (childEventListenerLoadMore != null) {
            queryMaisDados.removeEventListener(childEventListenerLoadMore);
            childEventListenerLoadMore = null;
        }

        //NÃO SEI SE DEVO LIMPAR A LISTA, CREIO QUE AQUI NÃO POIS
        //ESTÁ APROVEITANDO OS DADOS ANTERIORES EU ACHO.



        childEventListenerLoadMore = queryMaisDados.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    List<Postagem> newPostagem = new ArrayList<>();
                    long key = snapshot.child("timeStampNegativo").getValue(Long.class);
                    //*ToastCustomizado.toastCustomizadoCurto("existe " + key, getApplicationContext());
                    if (timestamp != -1 && key != -1 && key != timestamp) {
                        newPostagem.add(snapshot.getValue(Postagem.class));
                        timestamp = key;
                    }

                    // Remove a última chave usada
                    if (newPostagem.size() > PAGE_SIZE) {
                        newPostagem.remove(0);
                    }

                    if (timestamp != -1) {
                        adicionarMaisDados(newPostagem);
                    }
                }

                progressBarTesteAll.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    //ToastCustomizado.toastCustomizadoCurto("Atualizado",getApplicationContext());

                    // Recupera a postagem do snapshot
                    Postagem postagemAtualizada = snapshot.getValue(Postagem.class);

                    // Atualiza a postagem na lista
                    postagemDiffDAO.atualizarPostagem(postagemAtualizada, null);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterPostagens.updatePostagemList(listaPostagens);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Postagem postagemRemovida = snapshot.getValue(Postagem.class);
                    postagemDiffDAO.removerPostagem(postagemRemovida);
                    //ToastCustomizado.toastCustomizadoCurto("Postagem removida com sucesso",getApplicationContext());
                    Log.d("PAG-On", "Postagem removida com sucesso");

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterPostagens.updatePostagemList(listaPostagens);
                    //ToastCustomizado.toastCustomizadoCurto("Adapter notificado com sucesso",getApplicationContext());
                    Log.d("PAG-On Child Removed", "Adapter notificado com sucesso");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarTesteAll.setVisibility(View.GONE);
            }
        });
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void adicionarMaisDados(List<Postagem> newPostagem) {

        if (newPostagem != null && newPostagem.size() > 0) {
            postagemDiffDAO.carregarMaisPostagem(newPostagem, null);
            adapterPostagens.updatePostagemList(listaPostagens);
        } else {
            progressBarTesteAll.setVisibility(View.GONE);
        }

        setLoading(false);
    }

    private void adicionarPostagem(Postagem postagem) {
        postagemDiffDAO.adicionarPostagem(postagem);
        setLoading(false);
        adapterPostagens.updatePostagemList(listaPostagens);
    }

    private void limparLista() {
        if (listaPostagens != null && listaPostagens.size() > 0) {
            postagemDiffDAO.limparListaPostagems();
            adapterPostagens.updatePostagemList(listaPostagens);
        }
    }

    private void inicializandoComponentes() {
        recyclerTesteAll = findViewById(R.id.recyclerTesteAll);
        progressBarTesteAll = findViewById(R.id.progressBarTesteAll);
    }

    @Override
    public void onComunidadeRemocao(Postagem postagemRemovida) {
        postagemDiffDAO.removerPostagem(postagemRemovida);
        Log.d("PAG-On", "Postagem removida com sucesso");

        // Notifica o adapter das mudanças usando o DiffUtil
        adapterPostagens.updatePostagemList(listaPostagens);
        Log.d("PAG-On Child Removed", "Adapter notificado com sucesso");
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            //ToastCustomizado.toastCustomizado("Position: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }
}