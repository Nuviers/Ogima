package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFotosPostadas;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.PostagemDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FotosPostadasActivity extends AppCompatActivity implements AdapterFotosPostadas.RecuperaPosicaoAnterior, AdapterFotosPostadas.RemocaoPostagemListener, AdapterFotosPostadas.RemoverListenerRecycler {

    private Toolbar toolbar;
    private ImageButton imageButtonBackFtPostada;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    //Variaveis do recycler
    private RecyclerView recyclerViewFotos;
    private AdapterFotosPostadas adapterFotosPostadas;
    private List<Postagem> listaFotos = new ArrayList<>();
    private int mCurrentPosition = -1;
    private PostagemDiffDAO postagemDiffDAO;
    private LinearLayoutManager linearLayoutManager;

    private String irParaProfile = null;
    private String idDonoPerfil = null;
    //Timesamp usado como referência para paginação;
    private long lastTimestamp;
    //Quantidade de elementos por pagina, ou seja, a cada x elementos ele irá fazer
    //uma nova paginação.
    private final static int PAGE_SIZE = 10; // mudar para 10
    //Flag para indicar se já estão sendo carregados novos dados,
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    //Querys responsáveis pela recuperação das postagens.
    private Query queryInicial;
    private Query queryLoadMore;
    private Set<String> idsPostagens = new HashSet<>();
    private RecyclerView.OnScrollListener scrollListener;
    private ProgressDialog progressDialog;
    private boolean primeiraBusca = true;

    private ChildEventListener childEventInicio, childEventMore;

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
    public void onBackPressed() {
        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (primeiraBusca) {
            emailUsuario = autenticacao.getCurrentUser().getEmail();
            idUsuario = Base64Custom.codificarBase64(emailUsuario);

            Bundle dados = getIntent().getExtras();

            if (dados != null) {

                if (dados.containsKey("irParaProfile")) {
                    irParaProfile = dados.getString("irParaProfile");
                }

                if (dados.containsKey("idDonoPerfil")) {
                    idDonoPerfil = dados.getString("idDonoPerfil");
                }
            }

            configRecyclerView();

            postagemDiffDAO = new PostagemDiffDAO(listaFotos, adapterFotosPostadas);

            setLoading(true);
            //*progressBarLoading.setVisibility(View.VISIBLE);

            recuperarDadosIniciais();

            configPaginacao();

            primeiraBusca = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterFotosPostadas != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 &&
                listaFotos != null && listaFotos.size() > 0
                && linearLayoutManager != null) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    recyclerViewFotos.scrollToPosition(mCurrentPosition);
                }
            }, 100);
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (childEventInicio != null) {
            queryInicial.removeEventListener(childEventInicio);
            childEventInicio = null;
        }

        if (childEventMore != null) {
            queryLoadMore.removeEventListener(childEventMore);
            childEventMore = null;
        }

        if (listaFotos != null && listaFotos.size() > 0) {
            postagemDiffDAO.limparListaPostagems();
        }

        mCurrentPosition = -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos_postadas);
        inicializarComponentes();
        setSupportActionBar(toolbar);
        setTitle("");

        progressDialog = new ProgressDialog(FotosPostadasActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Excluindo postagem, aguarde um momento");

        clickListeners();
    }

    private void configRecyclerView() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerViewFotos.setHasFixedSize(true);
        recyclerViewFotos.setLayoutManager(linearLayoutManager);

        if (recyclerViewFotos.getOnFlingListener() == null) {
            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(recyclerViewFotos);
        }

        if (adapterFotosPostadas == null) {
            if (idDonoPerfil != null) {
                adapterFotosPostadas = new AdapterFotosPostadas(listaFotos, getApplicationContext(),
                        idDonoPerfil, true, this, this, this);
            } else {
                adapterFotosPostadas = new AdapterFotosPostadas(listaFotos, getApplicationContext(),
                        null, false, this, this, this);
            }
        }

        recyclerViewFotos.setAdapter(adapterFotosPostadas);
    }

    private void clickListeners() {
        imageButtonBackFtPostada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void recuperarDadosIniciais() {

        if (idDonoPerfil != null && !idDonoPerfil.isEmpty()) {
            queryInicial = firebaseRef.child("fotos")
                    .child(idDonoPerfil).orderByChild("timeStampNegativo")
                    .limitToFirst(1);
        }else{
            queryInicial = firebaseRef.child("fotos")
                    .child(idUsuario).orderByChild("timeStampNegativo")
                    .limitToFirst(1);
        }

        childEventInicio = queryInicial.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Postagem postagemInicial = snapshot.getValue(Postagem.class);
                    if (postagemInicial.getTimeStampNegativo() != -1) {
                        adicionarPostagem(postagemInicial);
                        lastTimestamp = postagemInicial.getTimeStampNegativo();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
            }
        });
    }

    private void configPaginacao() {

        if (recyclerViewFotos != null) {
            isScrolling = true;
            scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true;
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (linearLayoutManager != null) {

                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                        if (isLoading()) {
                            return;
                        }

                        int totalItemCount = linearLayoutManager.getItemCount();

                        if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                            isScrolling = false;

                            //*progressBarLoading.setVisibility(View.VISIBLE);

                            setLoading(true);

                            // o usuário rolou até o final da lista, exibe mais cinco itens
                            carregarMaisDados();
                        }
                    }
                }
            };
            recyclerViewFotos.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados() {

        if (idDonoPerfil != null && !idDonoPerfil.isEmpty()) {
            queryLoadMore = firebaseRef.child("fotos")
                    .child(idDonoPerfil).orderByChild("timeStampNegativo")
                    .startAt(lastTimestamp).limitToFirst(PAGE_SIZE);
        }else{
            queryLoadMore = firebaseRef.child("fotos")
                    .child(idUsuario).orderByChild("timeStampNegativo")
                    .startAt(lastTimestamp).limitToFirst(PAGE_SIZE);
        }

        childEventMore = queryLoadMore.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    List<Postagem> listaNovasPostagens = new ArrayList<>();
                    Postagem novaPostagem = snapshot.getValue(Postagem.class);

                    if (novaPostagem.getTimeStampNegativo() != -1) {
                        long key = novaPostagem.getTimeStampNegativo();

                        if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                            listaNovasPostagens.add(novaPostagem);
                            lastTimestamp = key;
                        }

                        // Remove a última chave usada
                        if (listaNovasPostagens.size() > PAGE_SIZE) {
                            listaNovasPostagens.remove(0);
                        }

                        if (lastTimestamp != -1) {
                            adicionarMaisDados(listaNovasPostagens);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Postagem postagemAtualizada = snapshot.getValue(Postagem.class);
                    postagemDiffDAO.atualizarPostagem(postagemAtualizada, "descricaoPostagem");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void adicionarPostagem(Postagem postagem) {
        postagemDiffDAO.adicionarPostagem(postagem);
        idsPostagens.add(postagem.getIdPostagem());
        adapterFotosPostadas.updatePostagemList(listaFotos, null);
        setLoading(false);
    }

    private void adicionarMaisDados(List<Postagem> newPostagem) {
        if (newPostagem != null && newPostagem.size() >= 1) {
            postagemDiffDAO.carregarMaisPostagem(newPostagem, idsPostagens);
            adapterFotosPostadas.updatePostagemList(listaFotos, null);
            setLoading(false);
        }
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void inicializarComponentes() {
        toolbar = findViewById(R.id.toolbarFotosPostadas);
        recyclerViewFotos = findViewById(R.id.recyclerViewFotosPostadas);
        imageButtonBackFtPostada = findViewById(R.id.imageButtonBackFtPostada);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onPostagemRemocao(Postagem postagemRemovida, int posicao, Button btnExcluir) {
        postagemDiffDAO.removerPostagem(postagemRemovida);
        Log.d("PAG-On", "Postagem removida com sucesso");
        adapterFotosPostadas.updatePostagemList(listaFotos, new AdapterFotosPostadas.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                if (listaFotos != null && listaFotos.size() == 0) {
                    if (progressDialog != null && !isFinishing()
                            && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                   onBackPressed();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (linearLayoutManager != null && recyclerViewFotos != null
                                && posicao != RecyclerView.NO_POSITION
                                && listaFotos != null
                                && listaFotos.size() > 0) {
                        }
                        if (recyclerViewFotos != null) {
                            recyclerViewFotos.addOnScrollListener(scrollListener);
                            recyclerViewFotos.setOnTouchListener(null);
                        }
                        btnExcluir.setEnabled(true);
                        if (progressDialog != null && !isFinishing()
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }, 200);
            }
        });
    }

    @Override
    public void onRemoverListener() {
        if (recyclerViewFotos != null) {
            recyclerViewFotos.removeOnScrollListener(scrollListener);
            recyclerViewFotos.setOnTouchListener((v, event) -> true);
        }

        if (!isFinishing()) {
            progressDialog.show();
        }
    }

    @Override
    public void onError() {
        if (recyclerViewFotos != null && scrollListener != null) {
            recyclerViewFotos.addOnScrollListener(scrollListener);
        }
        if (progressDialog != null && !isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}