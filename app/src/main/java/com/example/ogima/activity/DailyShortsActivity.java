package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterDailyShorts;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DailyShortDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DailyShort;
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

public class DailyShortsActivity extends AppCompatActivity implements AdapterDailyShorts.RemoverDailyListener, AdapterDailyShorts.RecuperaPosicaoAnterior {

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

        if (mCurrentPosition == -1) {
            emailUsuario = autenticacao.getCurrentUser().getEmail();
            idUsuario = Base64Custom.codificarBase64(emailUsuario);

            Bundle dados = getIntent().getExtras();

            if (dados != null && dados.containsKey("idUsuarioDaily")) {
                idUsuarioDaily = dados.getString("idUsuarioDaily");
            }

            configRecyclerView();
            dailyShortDiffDAO = new DailyShortDiffDAO(listaDailys, adapterDailyShorts);
            setLoading(true);
            recuperarDailyIncial();
            configPaginacao();
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
                    this, this, exoPlayer, gerenciarDaily);
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
            recyclerViewDaily.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                        int lastExoVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                        for (int i = firstVisibleItemPosition; i <= lastExoVisibleItemPosition; i++) {
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                            if (viewHolder instanceof AdapterDailyShorts.VideoViewHolder) {
                                View itemView = viewHolder.itemView;
                                Rect visibleBounds = new Rect();

                                boolean isVisible = itemView.getLocalVisibleRect(visibleBounds)
                                        && visibleBounds.height() == itemView.getHeight();

                                ((AdapterDailyShorts.VideoViewHolder) viewHolder).iniciarOuPararExoPlayer(isVisible);
                            }
                        }

                        if (isLoading()) {
                            return;
                        }

                        int totalItemCount = linearLayoutManager.getItemCount();
                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                        if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                            isScrolling = false;

                            //*progressBarLoading.setVisibility(View.VISIBLE);

                            setLoading(true);

                            // o usuário rolou até o final da lista, exibe mais cinco itens
                            carregarMaisDados();
                        }
                    }
                }
            });
        }
    }

    private void adicionarDaily(DailyShort dailyShort) {
        dailyShortDiffDAO.adicionarDailyShort(dailyShort);
        idsDailys.add(dailyShort.getIdDailyShort());
        adapterDailyShorts.updateDailyList(listaDailys);
        setLoading(false);
    }

    private void adicionarMaisDados(List<DailyShort> newDailys) {

        if (newDailys != null && newDailys.size() >= 1) {
            dailyShortDiffDAO.carregarMaisDailyShort(newDailys, idsDailys);
            adapterDailyShorts.updateDailyList(listaDailys);
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

                    if(newDailys.size() > PAGE_SIZE){
                        newDailys.remove(0);
                    }

                    if(lastTimestamp != -1){
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
    public void onDailyRemocao(@NonNull DailyShort dailyRemovido, int posicao) {

    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }
}