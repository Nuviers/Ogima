package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFuncoesPostagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.PostagemDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.exoplayer2.ExoPlayer;
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

public class DetalhesPostagemActivity extends AppCompatActivity implements AdapterFuncoesPostagem.RecuperaPosicaoAnterior, AdapterFuncoesPostagem.RemoverPostagemListener, AdapterFuncoesPostagem.RemoverListenerRecycler {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private RecyclerView recyclerViewPostagem;
    private AdapterFuncoesPostagem adapterFuncoesPostagem;
    private List<Postagem> listaPostagens = new ArrayList<>();
    private String irParaProfile = null;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private int mCurrentPosition = -1;
    private PostagemDiffDAO postagemDiffDAO;
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
    //ChildEventListeners para recuperacão das postagens.
    private ChildEventListener childEventListenerInicio, childEventListenerLoadMore;
    //Querys responsáveis pela recuperação das postagens.
    private Query queryInicial;
    private Query queryLoadMore;
    private ExoPlayer exoPlayer;
    private int ultimoVideoVisivel = -1;
    private int currentVideoVisible = -1;
    private RecyclerView.OnScrollListener scrollListener;
    private ProgressDialog progressDialog;
    private boolean primeiraBusca = true;
    private LinearLayoutManager linearLayoutManager;
    private Set<String> idsPostagens = new HashSet<>();

    private String idDonoPerfil = null;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

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
    protected void onStart() {
        super.onStart();

        if (primeiraBusca) {
            configRecyclerView();

            postagemDiffDAO = new PostagemDiffDAO(listaPostagens, adapterFuncoesPostagem);

            setLoading(true);
            //*progressBarLoading.setVisibility(View.VISIBLE);

            recuperarPostagensIniciais();

            configPaginacao();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 &&
                listaPostagens != null && listaPostagens.size() > 0
                && linearLayoutManager != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    recyclerViewPostagem.scrollToPosition(mCurrentPosition);
                }
            }, 100);

            if (exoPlayer != null) {
                adapterFuncoesPostagem.resumeExoPlayer();
            }
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (exoPlayer != null && adapterFuncoesPostagem != null) {
            adapterFuncoesPostagem.pauseExoPlayer();
        }

        if (adapterFuncoesPostagem != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
            //ToastCustomizado.toastCustomizadoCurto("Find " + mCurrentPosition, getApplicationContext());
        }

        if (mCurrentPosition == -1) {


            if (childEventListenerInicio != null) {
                queryInicial.removeEventListener(childEventListenerInicio);
                childEventListenerInicio = null;
            }

            if (childEventListenerLoadMore != null) {
                queryLoadMore.removeEventListener(childEventListenerLoadMore);
                childEventListenerLoadMore = null;
            }

            postagemDiffDAO.limparListaPostagems();
            idsPostagens.clear();

            ToastCustomizado.toastCustomizadoCurto("onStop", getApplicationContext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (exoPlayer != null) {
            adapterFuncoesPostagem.releaseExoPlayer();
        }

        if (childEventListenerInicio != null) {
            queryInicial.removeEventListener(childEventListenerInicio);
            childEventListenerInicio = null;
        }

        if (childEventListenerLoadMore != null) {
            queryLoadMore.removeEventListener(childEventListenerLoadMore);
            childEventListenerLoadMore = null;
        }

        postagemDiffDAO.limparListaPostagems();
        idsPostagens.clear();

        mCurrentPosition = -1;

        ToastCustomizado.toastCustomizadoCurto("onDestroy", getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_postagem);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Postagens");

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

        progressDialog = new ProgressDialog(DetalhesPostagemActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Excluindo postagem, aguarde um momento");
    }

    private void configRecyclerView() {

        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();

        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerViewPostagem.setHasFixedSize(true);
        recyclerViewPostagem.setLayoutManager(linearLayoutManager);

        // Configura o SnapHelper para rolagem suave
        if (recyclerViewPostagem.getOnFlingListener() == null) {
            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(recyclerViewPostagem);
        }

        if (adapterFuncoesPostagem == null) {
            if (idDonoPerfil != null && !idDonoPerfil.isEmpty()) {
                adapterFuncoesPostagem = new AdapterFuncoesPostagem(listaPostagens,
                        getApplicationContext(), this, this,
                        exoPlayer, this, false, true, idDonoPerfil);
            }else{
                adapterFuncoesPostagem = new AdapterFuncoesPostagem(listaPostagens,
                        getApplicationContext(), this, this,
                        exoPlayer, this, true, false, null);
            }
            recyclerViewPostagem.setAdapter(adapterFuncoesPostagem);
        }
    }


    private void recuperarPostagensIniciais() {

        if (idDonoPerfil != null && !idDonoPerfil.isEmpty()) {
            queryInicial = firebaseRef.child("postagens")
                    .child(idDonoPerfil).orderByChild("timeStampNegativo")
                    .limitToFirst(1);
        }else{
            queryInicial = firebaseRef.child("postagens")
                    .child(idUsuario).orderByChild("timeStampNegativo")
                    .limitToFirst(1);
        }

        childEventListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Postagem postagemInicio = snapshot.getValue(Postagem.class);

                    if (postagemInicio.getTimeStampNegativo() != -1) {
                        adicionarPostagem(snapshot.getValue(Postagem.class));
                        lastTimestamp = postagemInicio.getTimeStampNegativo();
                    }
                }
                //*progressBarLoading.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //Não será implementado nesse query pois os elementos desse query
                //podem ser trocados muito facilmente e só de estar forá do
                //query o elemento já é considerado como removido.
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //*progressBarLoading.setVisibility(View.GONE);
                lastTimestamp = -1;
            }
        });
    }

    private void configPaginacao() {

        if (recyclerViewPostagem != null) {
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

                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                                int lastExoVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                                for (int i = firstVisibleItemPosition; i <= lastExoVisibleItemPosition; i++) {
                                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                                    if (viewHolder instanceof AdapterFuncoesPostagem.VideoViewHolder) {
                                        View itemView = viewHolder.itemView;

                                        // boolean isVisible = isViewVisibleOnScreen(itemView, 0.75f);
                                        boolean isVisible = isItem75PercentVisibleVertical(recyclerView, itemView);

                                        if (isVisible) {
                                            currentVideoVisible = i;
                                            break;
                                        } else {
                                            currentVideoVisible = -1;
                                            //ToastCustomizado.toastCustomizadoCurto("Pode parar",getApplicationContext());
                                        }
                                    } else {
                                        currentVideoVisible = -1;
                                    }
                                }

                                if (currentVideoVisible != ultimoVideoVisivel) {

                                    if (ultimoVideoVisivel != -1) {
                                        RecyclerView.ViewHolder lastVisibleViewHolder = recyclerView.findViewHolderForAdapterPosition(ultimoVideoVisivel);
                                        if (lastVisibleViewHolder instanceof AdapterFuncoesPostagem.VideoViewHolder) {
                                            ((AdapterFuncoesPostagem.VideoViewHolder) lastVisibleViewHolder).pararExoPlayer(null);
                                        }
                                    }

                                    if (currentVideoVisible != -1) {
                                        RecyclerView.ViewHolder currentVisibleViewHolder = recyclerView.findViewHolderForAdapterPosition(currentVideoVisible);
                                        if (currentVisibleViewHolder instanceof AdapterFuncoesPostagem.VideoViewHolder) {
                                            ((AdapterFuncoesPostagem.VideoViewHolder) currentVisibleViewHolder).iniciarExoVisivel();
                                        }
                                    }

                                    ultimoVideoVisivel = currentVideoVisible;
                                }
                            }
                        }, 100);

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
            recyclerViewPostagem.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados() {

        if (idDonoPerfil != null && !idDonoPerfil.isEmpty()) {
            queryLoadMore = firebaseRef.child("postagens")
                    .child(idDonoPerfil).orderByChild("timeStampNegativo")
                    .startAt(lastTimestamp).limitToFirst(PAGE_SIZE);
        }else{
            queryLoadMore = firebaseRef.child("postagens")
                    .child(idUsuario).orderByChild("timeStampNegativo")
                    .startAt(lastTimestamp).limitToFirst(PAGE_SIZE);
        }

        childEventListenerLoadMore = queryLoadMore.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    //ToastCustomizado.toastCustomizadoCurto("Mais Dados", getApplicationContext());
                    List<Postagem> newPostagem = new ArrayList<>();
                    long key = snapshot.child("timeStampNegativo").getValue(Long.class);
                    //*ToastCustomizado.toastCustomizadoCurto("existe " + key, getApplicationContext());
                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                        newPostagem.add(snapshot.getValue(Postagem.class));
                        lastTimestamp = key;
                    }

                    // Remove a última chave usada
                    if (newPostagem.size() > PAGE_SIZE) {
                        newPostagem.remove(0);
                    }

                    if (lastTimestamp != -1) {
                        adicionarMaisDados(newPostagem);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Postagem postagemAtualizada = snapshot.getValue(Postagem.class);

                    ToastCustomizado.toastCustomizadoCurto("UPDATE", getApplicationContext());

                    //Atualizações granulares use payload na alteração
                    //junto com a config de payload no adaper mas não use o diff
                    //pois o diff só funciona quando é para notificaro objeto inteiro.

                    postagemDiffDAO.atualizarPostagem(postagemAtualizada, "descricaoPostagem");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                /*
                //melhor deixar comentado, somente exclusão pelo
                //próprio usuário faz sentido.
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
                 */
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
                //*progressBarLoading.setVisibility(View.GONE);
            }
        });
    }

    private void adicionarMaisDados(List<Postagem> newPostagem) {

        if (newPostagem != null && newPostagem.size() >= 1) {
            postagemDiffDAO.carregarMaisPostagem(newPostagem, idsPostagens);
            adapterFuncoesPostagem.updatePostagemList(listaPostagens, null);
            //*ToastCustomizado.toastCustomizadoCurto("Mais dados", getApplicationContext());
            setLoading(false);
        }
    }

    private void adicionarPostagem(Postagem postagem) {
        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());
        postagemDiffDAO.adicionarPostagem(postagem);
        idsPostagens.add(postagem.getIdPostagem());
        adapterFuncoesPostagem.updatePostagemList(listaPostagens, null);
        setLoading(false);
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void inicializandoComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        recyclerViewPostagem = findViewById(R.id.recyclerPostagemDetalhe);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onPostagemRemocao(Postagem postagemRemovida, int posicao, ImageButton imgBtnExcluir) {
        postagemDiffDAO.removerPostagem(postagemRemovida);
        Log.d("PAG-On", "Postagem removida com sucesso");

        // Notifica o adapter das mudanças usando o DiffUtil
        adapterFuncoesPostagem.updatePostagemList(listaPostagens, new AdapterFuncoesPostagem.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                if (listaPostagens != null && listaPostagens.size() == 0) {
                    if (progressDialog != null && !isFinishing()
                            && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    onBackPressed();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (linearLayoutManager != null && recyclerViewPostagem != null
                                && posicao != RecyclerView.NO_POSITION
                                && listaPostagens != null
                                && listaPostagens.size() > 0) {

                            int proximaPosicao = posicao;
                            if (posicao == listaPostagens.size()) {
                                proximaPosicao = posicao - 1;
                            }

                            if (proximaPosicao != -1) {
                                RecyclerView.ViewHolder viewHolder = recyclerViewPostagem.findViewHolderForAdapterPosition(proximaPosicao);
                                if (viewHolder instanceof AdapterFuncoesPostagem.VideoViewHolder) {
                                    ToastCustomizado.toastCustomizadoCurto("POSIÇÃO - " + proximaPosicao, getApplicationContext());
                                    AdapterFuncoesPostagem.VideoViewHolder videoViewHolder = (AdapterFuncoesPostagem.VideoViewHolder) viewHolder;
                                    videoViewHolder.iniciarExoVisivel();
                                    currentVideoVisible = proximaPosicao;
                                } else if (postagemRemovida.getTipoPostagem().equals("video")) {
                                    ultimoVideoVisivel = posicao;
                                } else {
                                    currentVideoVisible = -1;
                                }
                            }
                        }
                        if (recyclerViewPostagem != null) {
                            recyclerViewPostagem.addOnScrollListener(scrollListener);
                            recyclerViewPostagem.setOnTouchListener(null);
                            //ToastCustomizado.toastCustomizadoCurto("ADICIONADO LISTENER",getApplicationContext());
                        }
                        imgBtnExcluir.setEnabled(true);
                        if (progressDialog != null && !isFinishing()
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }, 200);
            }
        });
        Log.d("PAG-On Child Removed", "Adapter notificado com sucesso");

        //Verifica se o vídeo atual ocupou o espaço do vídeo excluído, caso
        //eseja visível e atenda o requisito o vídeo será iniciado corretamente.
    }

    @Override
    public void onRemoverListener() {
        if (recyclerViewPostagem != null) {
            recyclerViewPostagem.removeOnScrollListener(scrollListener);
            recyclerViewPostagem.setOnTouchListener((v, event) -> true);
        }

        if (!isFinishing()) {
            progressDialog.show();
        }
    }

    @Override
    public void onError() {
        if (recyclerViewPostagem != null && scrollListener != null) {
            recyclerViewPostagem.addOnScrollListener(scrollListener);
        }
        if (progressDialog != null && !isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean isItem75PercentVisibleVertical(RecyclerView recyclerView, View itemView) {

        //Vertical

        Rect scrollBounds = new Rect();
        recyclerView.getDrawingRect(scrollBounds);

        int top = itemView.getTop();
        int bottom = itemView.getBottom();

        // Calcula a porcentagem visível do item verticalmente.
        float visiblePercentage = 100f * (Math.min(scrollBounds.bottom, bottom) - Math.max(scrollBounds.top, top)) / itemView.getHeight();

        return visiblePercentage >= 75;
    }
}