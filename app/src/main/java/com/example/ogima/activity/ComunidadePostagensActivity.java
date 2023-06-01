package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterMinhasComunidades;
import com.example.ogima.adapter.AdapterPostagens;
import com.example.ogima.adapter.AdapterPostagensComunidade;
import com.example.ogima.adapter.HeaderAdapterPostagemComunidade;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PostagemDiffDAO;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.ExoPlayerItem;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComunidadePostagensActivity extends AppCompatActivity implements View.OnClickListener, AdapterPostagensComunidade.RecuperaPosicaoAnterior, AdapterPostagensComunidade.RemoverPostagemListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    //Componentes - inc_toolbar_padrao
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;

    private String idComunidade;
    private RecyclerView recyclerViewPostagensComunidade;
    //private ViewPager2 recyclerViewPostagensComunidade;

    private LinearLayoutManager linearLayoutManagerComunidade;

    private List<Postagem> listaPostagens = new ArrayList<>();
    private AdapterPostagensComunidade adapterPostagens;

    //adapter do header
    private HeaderAdapterPostagemComunidade headerAdapter;

    //config fab
    private FloatingActionButton fabVideoComunidadePostagem, fabGaleriaComunidadePostagem,
            fabGifComunidadePostagem, fabTextComunidadePostagem;
    private ImageButton imgBtnOpcoesPostagem;
    private Float translationY = 100f;
    private Boolean isMenuOpen = false;
    private OvershootInterpolator interpolator = new OvershootInterpolator();
    private ProgressBar progressBarComunidadePostagem;

    //Retorna para posição anterior
    private int mCurrentPosition = -1;
    private PostagemDiffDAO postagemDiffDAO;
    private String idPostagemParaTeste = "";

    //Paginação
    //Responsável por exibir o carregamento das postagens.
    private ProgressBar progressBarLoading;
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

    //Refresh
    private SwipeRefreshLayout swipeRefresh;

    private ExoPlayer exoPlayer;

    private Handler handler = new Handler();

    private boolean novaPostagem = false;

    private Set<String> idsPostagens = new HashSet<>();

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
        outState.putBoolean("nova_postagem", novaPostagem);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPosition = savedInstanceState.getInt("current_position");
        novaPostagem = savedInstanceState.getBoolean("nova_postagem");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentPosition == -1 || novaPostagem) {

            ToastCustomizado.toastCustomizadoCurto("OnStart", getApplicationContext());

            configRecyclerView();

            postagemDiffDAO = new PostagemDiffDAO(listaPostagens, adapterPostagens);

            setLoading(true);
            //*progressBarLoading.setVisibility(View.VISIBLE);

            recuperarPostagensIniciais();

            configPaginacao();

            novaPostagem = false;
        }

        configRefresh();

        imgBtnOpcoesPostagem.setOnClickListener(this);
        fabVideoComunidadePostagem.setOnClickListener(this);
        fabGaleriaComunidadePostagem.setOnClickListener(this);
        fabGifComunidadePostagem.setOnClickListener(this);
        fabTextComunidadePostagem.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 &&
                listaPostagens != null && listaPostagens.size() > 0
                && linearLayoutManagerComunidade != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    recyclerViewPostagensComunidade.scrollToPosition(mCurrentPosition);
                }
            }, 100);

            if (exoPlayer != null) {
                adapterPostagens.resumeExoPlayer();
            }
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (exoPlayer != null && adapterPostagens != null) {
            adapterPostagens.pauseExoPlayer();
        }

        if (adapterPostagens != null && linearLayoutManagerComunidade != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManagerComunidade.findFirstVisibleItemPosition();
            //ToastCustomizado.toastCustomizadoCurto("Find " + mCurrentPosition, getApplicationContext());
        }

        if (mCurrentPosition == -1 || novaPostagem) {


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

        fecharFabMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (exoPlayer != null) {
            adapterPostagens.releaseExoPlayer();
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
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
        setContentView(R.layout.activity_comunidade_postagens);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Postagens da comunidade");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null && dados.containsKey("idComunidade")) {
            idComunidade = dados.getString("idComunidade");
        }
    }


    private void configRecyclerView() {
        //Configuração do recycler de comunidades

        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();

        if (linearLayoutManagerComunidade != null) {

        } else {
            linearLayoutManagerComunidade = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerComunidade.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerViewPostagensComunidade.setHasFixedSize(true);
        recyclerViewPostagensComunidade.setLayoutManager(linearLayoutManagerComunidade);

        // Configura o SnapHelper para rolagem suave - igual o comportamento do viewpager2

        if (recyclerViewPostagensComunidade.getOnFlingListener() == null) {
            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(recyclerViewPostagensComunidade);
        }

        if (adapterPostagens != null) {

        } else {
            adapterPostagens = new AdapterPostagensComunidade(listaPostagens, getApplicationContext(), this::onComunidadeRemocao, this::onPosicaoAnterior, exoPlayer);
        }

        if (headerAdapter != null) {

        } else {
            headerAdapter = new HeaderAdapterPostagemComunidade(getApplicationContext(), idComunidade);
        }

        //concatena os dois adapter, respeitando a ordem dos parâmetros,
        //nesse caso o primeiro parâmetro é o adapter do cabeçalho e o segundo
        //são as postagens.
        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, adapterPostagens);

        recyclerViewPostagensComunidade.setAdapter(concatAdapter);
    }

    private void recuperarPostagensIniciais() {

        queryInicial = firebaseRef.child("postagensComunidade")
                .child(idComunidade).orderByChild("timestampNegativo")
                .limitToFirst(1);

        childEventListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    adicionarPostagem(snapshot.getValue(Postagem.class));
                    lastTimestamp = snapshot.child("timestampNegativo").getValue(Long.class);

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

        /*  Funciona perfeitamente, porém esse é usando viewpager2

        recyclerViewPostagensComunidade.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isScrolling = true;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (isLoading()) {
                    return;
                }

                int totalItemCount = adapterPostagens.getItemCount();
                int lastVisibleItemPosition = position;

                if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {
                    isScrolling = false;
                    setLoading(true);
                    carregarMaisDados();
                }
            }
        });
         */

        recyclerViewPostagensComunidade.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                int totalItemCount = linearLayoutManagerComunidade.getItemCount();
                int lastVisibleItemPosition = linearLayoutManagerComunidade.findLastVisibleItemPosition();

                if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                    isScrolling = false;

                    //*progressBarLoading.setVisibility(View.VISIBLE);

                    setLoading(true);

                    // o usuário rolou até o final da lista, exibe mais cinco itens
                    carregarMaisDados();
                }
            }
        });

    }

    private void carregarMaisDados() {

        queryLoadMore = firebaseRef.child("postagensComunidade")
                .child(idComunidade).orderByChild("timestampNegativo")
                .startAt(lastTimestamp).limitToFirst(PAGE_SIZE);

        childEventListenerLoadMore = queryLoadMore.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    //ToastCustomizado.toastCustomizadoCurto("Mais Dados", getApplicationContext());
                    List<Postagem> newPostagem = new ArrayList<>();
                    long key = snapshot.child("timestampNegativo").getValue(Long.class);
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

                    postagemDiffDAO.atualizarPostagem(postagemAtualizada);
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
            adapterPostagens.updatePostagemList(listaPostagens);
            //*ToastCustomizado.toastCustomizadoCurto("Mais dados", getApplicationContext());
            setLoading(false);
        }
    }

    private void adicionarPostagem(Postagem postagem) {
        ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());
        postagemDiffDAO.adicionarPostagem(postagem);
        idsPostagens.add(postagem.getIdPostagem());
        adapterPostagens.updatePostagemList(listaPostagens);
        setLoading(false);
    }

    private void limparLista() {
        if (listaPostagens != null && listaPostagens.size() > 0) {
            postagemDiffDAO.limparListaPostagems();
            adapterPostagens.updatePostagemList(listaPostagens);
        }
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void configRefresh() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                ToastCustomizado.toastCustomizadoCurto("Teste refresh", getApplicationContext());
            }
        });
    }

    private void inicializandoComponentes() {

        //inc_toolbar_padrao
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        //

        //Componentes do próprio layout atual
        recyclerViewPostagensComunidade = findViewById(R.id.recyclerViewPostagensComunidade);

        imgBtnOpcoesPostagem = findViewById(R.id.imgBtnOpcoesPostagemComunidade);
        fabVideoComunidadePostagem = findViewById(R.id.fabVideoComunidadePostagem);
        fabGaleriaComunidadePostagem = findViewById(R.id.fabGaleriaComunidadePostagem);
        fabGifComunidadePostagem = findViewById(R.id.fabGifComunidadePostagem);
        fabTextComunidadePostagem = findViewById(R.id.fabTextComunidadePostagem);

        //Paginação
        //*progressBarLoading = findViewById(R.id.progressBarLoadPostagensComunidade);

        //Refresh
        swipeRefresh = findViewById(R.id.swipeRefreshComunidadePostagem);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtnOpcoesPostagemComunidade:
                ToastCustomizado.toastCustomizadoCurto("Clicado fab", getApplicationContext());
                if (isMenuOpen) {
                    fecharFabMenu();
                } else {
                    abrirFabMenu();
                }
                break;
            case R.id.fabVideoComunidadePostagem:
                irParaCriacaoDaPostagem("video");
                //irParaEdicaoDaPostagem("video");
                break;
            case R.id.fabGaleriaComunidadePostagem:
                irParaCriacaoDaPostagem("imagem");
                //irParaEdicaoDaPostagem("imagem");
                break;
            case R.id.fabGifComunidadePostagem:
                irParaCriacaoDaPostagem("gif");
                //irParaEdicaoDaPostagem("gif");
                break;
            case R.id.fabTextComunidadePostagem:
                irParaCriacaoDaPostagem("texto");
                //irParaEdicaoDaPostagem("texto");
                break;
        }
    }

    private void testeMenu() {

        fabVideoComunidadePostagem.setAlpha(0f);
        fabGaleriaComunidadePostagem.setAlpha(0f);
        fabGifComunidadePostagem.setAlpha(0f);
        fabTextComunidadePostagem.setAlpha(0f);

        fabVideoComunidadePostagem.setVisibility(View.GONE);
        fabGaleriaComunidadePostagem.setVisibility(View.GONE);
        fabGifComunidadePostagem.setVisibility(View.GONE);
        fabTextComunidadePostagem.setVisibility(View.GONE);

        fabVideoComunidadePostagem.setTranslationY(translationY);
        fabGaleriaComunidadePostagem.setTranslationY(translationY);
        fabGifComunidadePostagem.setTranslationY(translationY);
        fabTextComunidadePostagem.setTranslationY(translationY);


    }

    private void abrirFabMenu() {
        isMenuOpen = !isMenuOpen;

        fabVideoComunidadePostagem.setVisibility(View.VISIBLE);
        fabGaleriaComunidadePostagem.setVisibility(View.VISIBLE);
        fabGifComunidadePostagem.setVisibility(View.VISIBLE);
        fabTextComunidadePostagem.setVisibility(View.VISIBLE);

        imgBtnOpcoesPostagem.animate().setInterpolator(interpolator)
                .rotationBy(45f).setDuration(300).start();

        fabVideoComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fabGaleriaComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fabGifComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fabTextComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    private void fecharFabMenu() {
        isMenuOpen = !isMenuOpen;

        imgBtnOpcoesPostagem.animate().setInterpolator(interpolator)
                .rotation(0f).setDuration(300).start();

        fabVideoComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fabGaleriaComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fabGifComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fabTextComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();


        fabVideoComunidadePostagem.setVisibility(View.GONE);
        fabGaleriaComunidadePostagem.setVisibility(View.GONE);
        fabGifComunidadePostagem.setVisibility(View.GONE);
        fabTextComunidadePostagem.setVisibility(View.GONE);
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
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    private void irParaCriacaoDaPostagem(String tipoPostagem) {
        Intent intent = new Intent(getApplicationContext(), CriarPostagemComunidadeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("idComunidade", idComunidade);
        intent.putExtra("tipoPostagem", tipoPostagem);
        novaPostagem = true;
        startActivity(intent);
    }

    private void irParaEdicaoDaPostagem(String tipoPostagem) {

        if (tipoPostagem.equals("imagem")) {
            idPostagemParaTeste = "";
        } else if (tipoPostagem.equals("gif")) {
            idPostagemParaTeste = "";
        } else if (tipoPostagem.equals("video")) {
            idPostagemParaTeste = "";
        } else if (tipoPostagem.equals("texto")) {
            idPostagemParaTeste = "";
        }

        Intent intent = new Intent(getApplicationContext(), CriarPostagemComunidadeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        FirebaseRecuperarUsuario.recuperaPostagemComunidade(idComunidade, idPostagemParaTeste, new FirebaseRecuperarUsuario.RecuperaPostagemComunidadeCallback() {
            @Override
            public void onPostagemComunidadeRecuperada(Postagem postagemAtual) {
                intent.putExtra("idComunidade", idComunidade);
                intent.putExtra("postagemEdicao", postagemAtual);
                intent.putExtra("idPostagem", idPostagemParaTeste);
                intent.putExtra("tipoPostagem", tipoPostagem);
                intent.putExtra("editarPostagem", true);
                novaPostagem = true;
                startActivity(intent);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }
}