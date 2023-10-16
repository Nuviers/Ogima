package com.example.ogima.activity.daily;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.WindowManager;
import android.widget.ImageButton;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterDailyShorts;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DailyShortDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DailyShort;
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
import java.util.Objects;
import java.util.Set;

public class DailyShortsActivity extends AppCompatActivity implements AdapterDailyShorts.RemoverDailyListener, AdapterDailyShorts.RecuperaPosicaoAnterior, AdapterDailyShorts.RemoverListenerRecycler {

    private RecyclerView recyclerViewDaily;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario, idUsuarioDaily;

    private LinearLayoutManager linearLayoutManager;
    private List<DailyShort> listaDailys = new ArrayList<>();
    private int mCurrentPosition = -1;
    private DailyShortDiffDAO dailyShortDiffDAO;
    private long lastTimestamp;
    private final static int PAGE_SIZE = 10;
    private boolean isLoading = false, isScrolling = false;
    private ChildEventListener childEventListenerInicio, childEventListenerLoadMore;
    private Query queryInicial, queryLoadMore;
    private ExoPlayer exoPlayer;
    private Handler handler = new Handler();
    private Set<String> idsDailys = new HashSet<>();
    private AdapterDailyShorts adapterDailyShorts;
    private boolean gerenciarDaily = false;

    private int ultimoVideoVisivel = -1;
    private int currentVideoVisible = -1;

    private RecyclerView.OnScrollListener scrollListener;
    private ProgressDialog progressDialog;

    private String irParaProfile = null;

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
        }else{
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentPosition == -1) {
            emailUsuario = autenticacao.getCurrentUser().getEmail();
            idUsuario = Base64Custom.codificarBase64(emailUsuario);

            Bundle dados = getIntent().getExtras();

            if (dados != null && dados.containsKey("idUsuarioDaily")) {
                idUsuarioDaily = dados.getString("idUsuarioDaily");
            }

            if (dados != null) {
                if (dados.containsKey("irParaProfile")) {
                    irParaProfile = dados.getString("irParaProfile");
                }
            }

            configRecyclerView();
            dailyShortDiffDAO = new DailyShortDiffDAO(listaDailys, adapterDailyShorts);
            setLoading(true);
            recuperarDailyIncial();
            configPaginacao();

            progressDialog = new ProgressDialog(DailyShortsActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Excluindo dailyShort, aguarde um momento");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 &&
                listaDailys != null && listaDailys.size() > 0
                && linearLayoutManager != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    recyclerViewDaily.scrollToPosition(mCurrentPosition);
                }
            }, 100);

            if (exoPlayer != null) {
                adapterDailyShorts.resumeExoPlayer();
            }
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (exoPlayer != null && adapterDailyShorts != null) {
            adapterDailyShorts.pauseExoPlayer();
        }

        if (adapterDailyShorts != null && linearLayoutManager != null
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

            dailyShortDiffDAO.limparListaDailyShorts();
            idsDailys.clear();

            ToastCustomizado.toastCustomizadoCurto("onStop", getApplicationContext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (exoPlayer != null) {
            adapterDailyShorts.releaseExoPlayer();
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

        dailyShortDiffDAO.limparListaDailyShorts();
        idsDailys.clear();

        mCurrentPosition = -1;

        ToastCustomizado.toastCustomizadoCurto("onDestroy", getApplicationContext());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_shorts);
        // Define a janela em modo de tela cheia
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        inicializandoComponentes();
    }

    private void configRecyclerView() {
        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        }
        recyclerViewDaily.setHasFixedSize(true);
        recyclerViewDaily.setLayoutManager(linearLayoutManager);
        if (recyclerViewDaily != null) {
            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(recyclerViewDaily);
        }

        if (idUsuario.equals(idUsuarioDaily)) {
            gerenciarDaily = true;
        } else {
            gerenciarDaily = false;
        }
        if (adapterDailyShorts == null) {
            adapterDailyShorts = new AdapterDailyShorts(listaDailys, getApplicationContext(),
                    this, this, exoPlayer, gerenciarDaily, this);
        }
        Objects.requireNonNull(recyclerViewDaily).setAdapter(adapterDailyShorts);
    }

    private void recuperarDailyIncial() {
        queryInicial = firebaseRef.child("dailyShorts")
                .child(idUsuarioDaily).orderByChild("timestampCriacaoDaily")
                .limitToFirst(1);

        childEventListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    adicionarDaily(snapshot.getValue(DailyShort.class));
                    lastTimestamp = snapshot.child("timestampCriacaoDaily").getValue(Long.class);
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
        if (recyclerViewDaily != null) {
            isScrolling = true;
            scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
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
                                    if (viewHolder instanceof AdapterDailyShorts.VideoViewHolder) {
                                        View itemView = viewHolder.itemView;

                                        // boolean isVisible = isViewVisibleOnScreen(itemView, 0.75f);
                                        boolean isVisible = isItem75PercentVisibleHorizontal(recyclerView, itemView);

                                        if (isVisible) {
                                            currentVideoVisible = i;
                                            break;
                                        } else {
                                            //currentVideoVisible = i - 1; - funciona para parar
                                            //o video quando tem outra mídia, porém dá problema
                                            //na exclusão de vídeo seguido do outro com essa
                                            //linha de código ^^

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
                                        if (lastVisibleViewHolder instanceof AdapterDailyShorts.VideoViewHolder) {
                                            ((AdapterDailyShorts.VideoViewHolder) lastVisibleViewHolder).pararExoPlayer(null);
                                        }
                                    }

                                    if (currentVideoVisible != -1) {
                                        RecyclerView.ViewHolder currentVisibleViewHolder = recyclerView.findViewHolderForAdapterPosition(currentVideoVisible);
                                        if (currentVisibleViewHolder instanceof AdapterDailyShorts.VideoViewHolder) {
                                            ((AdapterDailyShorts.VideoViewHolder) currentVisibleViewHolder).iniciarExoVisivel(true);
                                        }
                                    }

                                    ultimoVideoVisivel = currentVideoVisible;
                                }
                            }
                        }, 100);


                        /*logica do teste exo 2
                        for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                            if (viewHolder instanceof AdapterDailyShorts.VideoViewHolder) {
                                View itemView = viewHolder.itemView;

                                boolean isVisible = isViewVisibleOnScreen(itemView, 0.75f);

                                if (isVisible) {
                                    ((AdapterDailyShorts.VideoViewHolder) viewHolder).iniciarExoVisivel(true);
                                } else {
                                    ((AdapterDailyShorts.VideoViewHolder) viewHolder).pararExoPlayer(null);
                                }
                            }
                        }
                         */ //logica do teste exo 2

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
            recyclerViewDaily.addOnScrollListener(scrollListener);
        }
    }

    private void adicionarDaily(DailyShort dailyShort) {
        dailyShortDiffDAO.adicionarDailyShort(dailyShort);
        idsDailys.add(dailyShort.getIdDailyShort());
        adapterDailyShorts.updateDailyList(listaDailys, new AdapterDailyShorts.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                setLoading(false);
            }
        });
    }

    private void adicionarMaisDados(List<DailyShort> newDailys) {

        if (newDailys != null && newDailys.size() >= 1) {
            dailyShortDiffDAO.carregarMaisDailyShort(newDailys, idsDailys);
            adapterDailyShorts.updateDailyList(listaDailys, null);
            //*ToastCustomizado.toastCustomizadoCurto("Mais dados", getApplicationContext());
            setLoading(false);
        }
    }

    private void carregarMaisDados() {
        queryLoadMore = firebaseRef.child("dailyShorts")
                .child(idUsuarioDaily).orderByChild("timestampCriacaoDaily")
                .startAt(lastTimestamp).limitToFirst(PAGE_SIZE);

        childEventListenerLoadMore = queryLoadMore.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    List<DailyShort> newDailys = new ArrayList<>();
                    long key = snapshot.child("timestampCriacaoDaily").getValue(Long.class);
                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                        newDailys.add(snapshot.getValue(DailyShort.class));
                        lastTimestamp = key;
                    }

                    if (newDailys.size() > PAGE_SIZE) {
                        newDailys.remove(0);
                    }

                    if (lastTimestamp != -1) {
                        adicionarMaisDados(newDailys);
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

            }
        });
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void inicializandoComponentes() {
        recyclerViewDaily = findViewById(R.id.recyclerViewDaily);
    }

    @Override
    public void onDailyRemocao(@NonNull DailyShort dailyRemovido, int posicao, ImageButton imgBtnExcluir) {
        //ToastCustomizado.toastCustomizadoCurto("REMOVIDO LISTENER",getApplicationContext());
        dailyShortDiffDAO.removerDailyShort(dailyRemovido);
        adapterDailyShorts.updateDailyList(listaDailys, new AdapterDailyShorts.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {

                if (listaDailys != null && listaDailys.size() == 0) {
                    if (progressDialog != null && !isFinishing()
                            && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    onBackPressed();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (linearLayoutManager != null && recyclerViewDaily != null
                                && posicao != RecyclerView.NO_POSITION
                                && listaDailys != null
                                && listaDailys.size() > 0) {

                            int proximaPosicao = posicao;
                            if (posicao == listaDailys.size()) {
                                proximaPosicao = posicao - 1;
                            }

                            if (proximaPosicao != -1) {
                                RecyclerView.ViewHolder viewHolder = recyclerViewDaily.findViewHolderForAdapterPosition(proximaPosicao);
                                if (viewHolder instanceof AdapterDailyShorts.VideoViewHolder) {
                                    AdapterDailyShorts.VideoViewHolder videoViewHolder = (AdapterDailyShorts.VideoViewHolder) viewHolder;
                                    videoViewHolder.iniciarExoVisivel(true);
                                    currentVideoVisible = proximaPosicao;
                                } else if (dailyRemovido.getTipoMidia().equals("video")) {
                                    ultimoVideoVisivel = posicao;
                                } else {
                                    currentVideoVisible = -1;
                                }
                            }
                        }
                        if (recyclerViewDaily != null) {
                            recyclerViewDaily.addOnScrollListener(scrollListener);
                            recyclerViewDaily.setOnTouchListener(null);
                            //ToastCustomizado.toastCustomizadoCurto("ADICIONADO LISTENER",getApplicationContext());
                        }
                        imgBtnExcluir.setEnabled(true);
                        if (progressDialog != null && !isFinishing()
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }, 100);
            }
        });

        Log.d("EXOUTILS", "Posição recebida " + posicao);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    // Lógica para verificar se o item está pelo menos 75% visível horizontalmente na tela
    private boolean isItem75PercentVisibleHorizontal(RecyclerView recyclerView, View itemView) {
        Rect scrollBounds = new Rect();
        recyclerView.getDrawingRect(scrollBounds);

        int left = itemView.getLeft();
        int right = itemView.getRight();

        // Calcula a porcentagem visível do item horizontalmente
        float visiblePercentage = 100f * (Math.min(scrollBounds.right, right) - Math.max(scrollBounds.left, left)) / itemView.getWidth();

        return visiblePercentage >= 75;
    }

    @Override
    public void onRemoverListener() {

        if (recyclerViewDaily != null) {
            recyclerViewDaily.removeOnScrollListener(scrollListener);
            recyclerViewDaily.setOnTouchListener((v, event) -> true);
        }

        if (!isFinishing()) {
            progressDialog.show();
        }
    }

    @Override
    public void onError() {

        if (recyclerViewDaily != null && scrollListener != null) {
            recyclerViewDaily.addOnScrollListener(scrollListener);
        }
        if (progressDialog != null && !isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}