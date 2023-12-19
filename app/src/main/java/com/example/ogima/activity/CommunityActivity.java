package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterCommunityFilters;
import com.example.ogima.adapter.AdapterCommunityInvitations;
import com.example.ogima.adapter.AdapterCommunityParticipants;
import com.example.ogima.adapter.AdapterFriends;
import com.example.ogima.adapter.AdapterPreviewCommunity;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ComunidadeDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.CustomBottomSheetDialogFragment;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommunityActivity extends AppCompatActivity implements AdapterPreviewCommunity.RecuperaPosicaoAnterior, AdapterPreviewCommunity.RemoverComunidadeListener, AdapterPreviewCommunity.AnimacaoIntent {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private CommunityUtils communityUtils;
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private SpinKitView spinProgress;
    private String tipoComunidade = "";
    private SearchView searchView;
    private String idUsuario = "";
    private LinearLayoutManager linearLayoutManager;
    private int mCurrentPosition = -1;
    private AdapterPreviewCommunity adapterCommunity;
    private FirebaseUtils firebaseUtils;
    private static int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Comunidade> listaComunidades, listaFiltrada;
    private HashMap<String, Object> listaDadosComunidades;
    private Set<String> idsComunidades, idsFiltrados;
    private ComunidadeDiffDAO comunidadeDiffDAO, comunidadeDiffDAOFiltrado;
    private Query queryInicial, queryLoadMore, queryInicialFiltro, queryLoadMoreFiltro;
    private boolean trocarQueryInicial = false;
    private String nomePesquisado = "";
    private String lastName = null;
    private boolean pesquisaAtivada = false;
    private boolean atualizandoLista = false;
    private Handler searchHandler = new Handler();
    private String currentSearchText = "";
    private ProgressDialog progressDialog;
    private int searchCounter = 0;
    private HashMap<String, Query> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private HashMap<String, Query> referenceFiltroHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerFiltroHashMap = new HashMap<>();
    private int queryDelayMillis = 500;
    private long lastTimestamp = -1;
    private Comunidade comunidadeComparator;
    private Set<String> idsListeners = new HashSet<>();
    private DatabaseReference verificaVinculoRef;

    @Override
    protected void onStop() {
        super.onStop();
        if (linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                linearLayoutManager != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    recyclerView.scrollToPosition(mCurrentPosition);
                }
            }, 100);
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastCustomizado.toastCustomizadoCurto("ONDESTROY COMUNIDADE",getApplicationContext());
        removeValueEventListener();
        removeValueEventListenerFiltro();
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
        mCurrentPosition = -1;
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

    private interface ConfigInicialCallback {
        void onConcluido();
    }

    private interface RecuperarComunidadeCallback {
        void onConcluido(Comunidade comunidadeRecuperada);
    }

    private interface VerificaCriterioCallback {
        void onCriterioAtendido();

        void onSemVinculo();

        void onError(String message);
    }

    public interface VerificaBlockCallback {
        void onAjustado(Comunidade comunidadeAjustada);
    }

    public CommunityActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        comunidadeComparator = new Comunidade(false, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        inicializarComponentes();
        if (idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        configInicial(new ConfigInicialCallback() {
            @Override
            public void onConcluido() {
                UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                    @Override
                    public void onConcluido(boolean epilepsia) {
                        configRecycler(epilepsia);
                        comunidadeDiffDAO = new ComunidadeDiffDAO(listaComunidades, adapterCommunity);
                        comunidadeDiffDAOFiltrado = new ComunidadeDiffDAO(listaFiltrada, adapterCommunity);
                        setLoading(true);
                        configSearchView();
                        configurarQueryInicial();
                        recuperarDadosIniciais();
                        configPaginacao();
                    }

                    @Override
                    public void onSemDado() {
                        ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
                        onBackPressed();
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizado(String.format("%s%s %s", getString(R.string.error_retrieving_user_data), ":", message), getApplicationContext());
                        onBackPressed();
                    }
                });
            }
        });
    }

    private void configInicial(ConfigInicialCallback callback) {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        firebaseUtils = new FirebaseUtils();
        listaComunidades = new ArrayList<>();
        listaFiltrada = new ArrayList<>();
        listaDadosComunidades = new HashMap<>();
        idsComunidades = new HashSet<>();
        idsFiltrados = new HashSet<>();
        progressDialog = new ProgressDialog(CommunityActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        setLoading(true);
        setPesquisaAtivada(false);
        Bundle dados = getIntent().getExtras();
        if (dados == null || !dados.containsKey("tipoComunidade")) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade.", getApplicationContext());
            onBackPressed();
            return;
        }
        tipoComunidade = dados.getString("tipoComunidade");
        if (tipoComunidade == null || tipoComunidade.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade.", getApplicationContext());
            onBackPressed();
            return;
        }
        communityUtils = new CommunityUtils(getApplicationContext());
        txtViewTitleToolbar.setText(tipoComunidade);
        callback.onConcluido();
    }

    private void configSearchView() {
        searchView.setQueryHint("Pesquisar comunidade pelo nome:");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!atualizandoLista) {
                    currentSearchText = newText;
                    searchHandler.removeCallbacksAndMessages(null);
                    searchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (newText != null && !newText.isEmpty()) {
                                if (newText.equals(currentSearchText)) {
                                    exibirProgress();
                                    searchCounter++;
                                    final int counter = searchCounter;
                                    setLoading(true);
                                    setPesquisaAtivada(true);
                                    if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                        lastName = null;
                                        idsFiltrados.clear();
                                        nomePesquisado = "";
                                        comunidadeDiffDAOFiltrado.limparListaComunidades();
                                        removeValueEventListenerFiltro();
                                    }
                                    nomePesquisado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
                                    nomePesquisado = FormatarNomePesquisaUtils.removeAcentuacao(nomePesquisado).toUpperCase(Locale.ROOT);

                                    dadoInicialFiltragem(nomePesquisado, counter);
                                }
                            } else {
                                ToastCustomizado.toastCustomizadoCurto("LIMPAR", getApplicationContext());
                                atualizandoLista = true;
                                limparFiltragem(true);
                            }
                        }
                    }, queryDelayMillis);
                }
                return true;
            }
        });
    }

    private void configRecycler(boolean epilepsia) {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapterCommunity = new AdapterPreviewCommunity(getApplicationContext(),
                    listaComunidades, this, this, listaDadosComunidades,
                    getResources().getColor(R.color.colorAccent), this, tipoComunidade);
            recyclerView.setAdapter(adapterCommunity);
            adapterCommunity.setStatusEpilepsia(epilepsia);
        }
    }

    private void recuperarDadosIniciais() {
        exibirProgress();
        queryInicial.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listaComunidades != null && listaComunidades.size() >= 1) {
                    ocultarProgress();
                    queryInicial.removeEventListener(this);
                    return;
                }
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Comunidade comunidade = snapshotChildren.getValue(Comunidade.class);
                        if (comunidade != null && comunidade.getIdComunidade() != null
                                && !comunidade.getIdComunidade().isEmpty()) {
                            adicionarComunidade(comunidade);
                            lastTimestamp = comunidade.getTimestampinteracao();
                        }
                    }
                } else {
                    queryInicial.removeEventListener(this);
                    ToastCustomizado.toastCustomizadoCurto(msgSemComunidades(), getApplicationContext());
                    onBackPressed();
                }
                queryInicial.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
                ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao recuperar as comunidades.", "Code:", error.getCode()), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void adicionarComunidade(Comunidade comunidadeAlvo) {
        if (listaComunidades != null && listaComunidades.size() >= 1) {
            ocultarProgress();
            setLoading(false);
            return;
        }
        if (tipoComunidade.equals(CommunityUtils.COMMUNITIES_FOLLOWING)
                || tipoComunidade.equals(CommunityUtils.PUBLIC_COMMUNITIES)) {
            //único caso que é necessário recuperar a comunidade pelo id.
            FirebaseRecuperarUsuario.recoverCommunity(comunidadeAlvo.getIdComunidade(), new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
                @Override
                public void onComunidadeRecuperada(Comunidade dadosComunidade) {
                    comunidadeDiffDAO.adicionarComunidade(dadosComunidade);
                    idsComunidades.add(dadosComunidade.getIdComunidade());
                    adapterCommunity.updateComunidadeList(listaComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            adicionarDadosDaComunidade(dadosComunidade);
                            ocultarProgress();
                            setLoading(false);
                        }
                    });
                }

                @Override
                public void onNaoExiste() {
                    ocultarProgress();
                    setLoading(false);
                }

                @Override
                public void onError(String mensagem) {
                    ocultarProgress();
                    setLoading(false);
                }
            });
        } else {
            comunidadeDiffDAO.adicionarComunidade(comunidadeAlvo);
            idsComunidades.add(comunidadeAlvo.getIdComunidade());
            adapterCommunity.updateComunidadeList(listaComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    adicionarDadosDaComunidade(comunidadeAlvo);
                    ocultarProgress();
                    setLoading(false);
                }
            });
        }
    }

    private void adicionarDadosDaComunidade(Comunidade dadosComunidade) {
        listaDadosComunidades.put(dadosComunidade.getIdComunidade(), dadosComunidade);
        if (!isPesquisaAtivada() && idsListeners != null && idsListeners.size() > 0
                && idsListeners.contains(dadosComunidade.getIdComunidade())) {
            return;
        }
        if (!isPesquisaAtivada()) {
            idsListeners.add(dadosComunidade.getIdComunidade());
        }
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        exibirProgress();
        queryInicialFiltro = firebaseRef.child("comunidades")
                .orderByChild("nomeComunidadePesquisa")
                .startAt(nome).endAt(nome + "\uf8ff");
        queryInicialFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (counter != searchCounter) {
                    queryInicialFiltro.removeEventListener(this);
                    ocultarProgress();
                    limparFiltragem(false);
                    return;
                }

                if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                    queryInicialFiltro.removeEventListener(this);
                    ocultarProgress();
                    return;
                }

                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Comunidade comunidadePesquisa = snapshotChildren.getValue(Comunidade.class);
                        if (comunidadePesquisa != null && comunidadePesquisa.getIdComunidade() != null
                                && !comunidadePesquisa.getIdComunidade().isEmpty()) {
                            verificaVinculo(comunidadePesquisa, new VerificaCriterioCallback() {
                                @Override
                                public void onCriterioAtendido() {
                                    adicionarComunidadeFiltrada(comunidadePesquisa);
                                }

                                @Override
                                public void onSemVinculo() {
                                    ocultarProgress();
                                    lastName = null;
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarProgress();
                                    lastName = null;
                                }
                            });
                        }
                    }
                }
                queryInicialFiltro.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocultarProgress();
                lastName = null;
            }
        });
    }

    private void verificaVinculo(Comunidade comunidadeAlvo, VerificaCriterioCallback callback) {
        configurarReferenceVinculo(comunidadeAlvo);
        if (verificaVinculoRef != null) {
            verificaVinculoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        switch (tipoComunidade) {
                            case CommunityUtils.MY_COMMUNITIES:
                                String idFundador = snapshot.getValue(String.class);
                                if (idFundador != null &&
                                        !idFundador.isEmpty() &&
                                        idFundador.equals(idUsuario)) {
                                    callback.onCriterioAtendido();
                                } else {
                                    callback.onSemVinculo();
                                }
                                break;
                            case CommunityUtils.PUBLIC_COMMUNITIES:
                            case CommunityUtils.COMMUNITIES_FOLLOWING:
                                callback.onCriterioAtendido();
                                break;
                        }
                    } else {
                        callback.onSemVinculo();
                    }
                    verificaVinculoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        }
    }

    private void configurarReferenceVinculo(Comunidade comunidadeAlvo) {
        verificaVinculoRef = null;
        switch (tipoComunidade) {
            case CommunityUtils.MY_COMMUNITIES:
                verificaVinculoRef = firebaseRef.child("comunidades")
                        .child(comunidadeAlvo.getIdComunidade())
                        .child("idSuperAdmComunidade");
                break;
            case CommunityUtils.PUBLIC_COMMUNITIES:
                verificaVinculoRef = firebaseRef.child("publicCommunities")
                        .child(comunidadeAlvo.getIdComunidade());
                break;
            case CommunityUtils.COMMUNITIES_FOLLOWING:
                verificaVinculoRef = firebaseRef.child("communityFollowing")
                        .child(idUsuario)
                        .child(comunidadeAlvo.getIdComunidade());
                break;
        }
    }

    private void adicionarComunidadeFiltrada(Comunidade dadosComunidade) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            ocultarProgress();
            setLoading(false);
            return;
        }
        lastName = dadosComunidade.getNomeComunidadePesquisa();
        comunidadeDiffDAOFiltrado.adicionarComunidade(dadosComunidade);
        idsFiltrados.add(dadosComunidade.getIdComunidade());
        adapterCommunity.updateComunidadeList(listaFiltrada, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                adicionarDadosDaComunidade(dadosComunidade);
                ocultarProgress();
                carregarMaisDados(nomePesquisado);
                setLoading(false);
            }
        });
    }

    private void configPaginacao() {
        if (recyclerView != null) {
            scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@androidx.annotation.NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    }
                }

                @Override
                public void onScrolled(@androidx.annotation.NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (linearLayoutManager != null) {
                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isLoading()) {
                                    return;
                                }
                                int totalItemCount = linearLayoutManager.getItemCount();
                                if (lastVisibleItemPosition == totalItemCount - 1) {
                                    setLoading(true);
                                    carregarMaisDados(nomePesquisado);
                                }
                            }
                        }, 100);
                    }
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados(String dadoAnterior) {
        exibirProgress();
        if (isPesquisaAtivada()) {
            if (listaFiltrada != null && listaFiltrada.size() > 0
                    && lastName != null && !lastName.isEmpty()) {
                queryLoadMoreFiltro = firebaseRef.child("comunidades")
                        .orderByChild("nomeComunidadePesquisa")
                        .startAt(lastName).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
                queryLoadMoreFiltro.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                                Comunidade comunidadePesquisa = snapshotChildren.getValue(Comunidade.class);
                                if (comunidadePesquisa != null
                                        && comunidadePesquisa.getIdComunidade() != null
                                        && !comunidadePesquisa.getIdComunidade().isEmpty()) {
                                    verificaVinculo(comunidadePesquisa, new VerificaCriterioCallback() {
                                        @Override
                                        public void onCriterioAtendido() {
                                            List<Comunidade> newComunidade = new ArrayList<>();
                                            String key = comunidadePesquisa.getNomeComunidadePesquisa();
                                            if (lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)) {
                                                newComunidade.add(comunidadePesquisa);
                                                lastName = key;
                                            }
                                            // Remove a última chave usada
                                            if (newComunidade.size() > PAGE_SIZE) {
                                                newComunidade.remove(0);
                                            }
                                            if (lastName != null && !lastName.isEmpty()) {
                                                adicionarMaisDadosFiltrados(newComunidade, comunidadePesquisa);
                                            }
                                        }

                                        @Override
                                        public void onSemVinculo() {
                                            ocultarProgress();
                                            String key = comunidadePesquisa.getNomeComunidadePesquisa();
                                            if (lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)) {
                                                lastName = key;
                                            }
                                        }

                                        @Override
                                        public void onError(String message) {
                                            ocultarProgress();
                                        }
                                    });
                                }
                            }
                        } else {
                            ocultarProgress();
                        }
                        queryLoadMoreFiltro.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ocultarProgress();
                        lastName = null;
                    }
                });
            }
        } else {
            configurarQueryMore();
            queryLoadMore.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                            Comunidade comunidadeMore = snapshotChildren.getValue(Comunidade.class);
                            if (comunidadeMore == null) {
                                if (queryLoadMore != null) {
                                    queryLoadMore.removeEventListener(this);
                                }
                                ocultarProgress();
                                return;
                            }
                            if (comunidadeMore.getIdComunidade() != null
                                    && !comunidadeMore.getIdComunidade().isEmpty()) {
                                List<Comunidade> newComunidade = new ArrayList<>();
                                long key = comunidadeMore.getTimestampinteracao();
                                if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                    newComunidade.add(comunidadeMore);
                                    lastTimestamp = key;
                                }
                                // Remove a última chave usada
                                if (newComunidade.size() > PAGE_SIZE) {
                                    newComunidade.remove(0);
                                }
                                if (lastTimestamp != -1) {
                                    if (tipoComunidade.equals(CommunityUtils.MY_COMMUNITIES)) {
                                        adicionarMaisDados(newComunidade, comunidadeMore.getIdComunidade(), comunidadeMore);
                                    } else {
                                        adicionarMaisDados(newComunidade, comunidadeMore.getIdComunidade(), null);
                                    }
                                }
                            }
                        }
                    } else {
                        ocultarProgress();
                    }
                    queryLoadMore.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ocultarProgress();
                    lastTimestamp = -1;
                }
            });
        }
    }

    private void adicionarMaisDados(List<Comunidade> newComunidade, String idComunidade, Comunidade minhaComunidade) {
        if (newComunidade != null && newComunidade.size() >= 1) {
            if (tipoComunidade.equals(CommunityUtils.MY_COMMUNITIES)) {
                //Já está com o objeto comunidade completo.
                comunidadeDiffDAO.carregarMaisComunidade(newComunidade, idsComunidades);
                adapterCommunity.updateComunidadeList(listaComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        ocultarProgress();
                        adicionarDadosDaComunidade(minhaComunidade);
                        setLoading(false);
                    }
                });
            } else {
                FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
                    @Override
                    public void onComunidadeRecuperada(Comunidade dadosComunidade) {
                        comunidadeDiffDAO.carregarMaisComunidade(newComunidade, idsComunidades);
                        adapterCommunity.updateComunidadeList(listaComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {
                                ocultarProgress();
                                adicionarDadosDaComunidade(dadosComunidade);
                                setLoading(false);
                            }
                        });
                    }

                    @Override
                    public void onNaoExiste() {
                        ocultarProgress();
                    }

                    @Override
                    public void onError(String mensagem) {
                        ocultarProgress();
                    }
                });
            }
        } else {
            ocultarProgress();
        }
    }

    private void adicionarMaisDadosFiltrados(List<Comunidade> newComunidade, Comunidade dadosComunidade) {
        if (newComunidade != null && newComunidade.size() >= 1) {
            comunidadeDiffDAOFiltrado.carregarMaisComunidade(newComunidade, idsFiltrados);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterCommunity.updateComunidadeList(listaFiltrada, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    ocultarProgress();
                    adicionarDadosDaComunidade(dadosComunidade);
                    setLoading(false);
                }
            });
        } else {
            ocultarProgress();
        }
    }

    private void limparFiltragem(boolean fecharTeclado) {
        if (searchView != null && fecharTeclado) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            searchView.clearFocus();
        }
        removeValueEventListenerFiltro();
        lastName = null;
        if (idsFiltrados != null) {
            idsFiltrados.clear();
        }
        setPesquisaAtivada(false);
        nomePesquisado = "";
        ocultarProgress();
        if (comunidadeDiffDAOFiltrado != null) {
            comunidadeDiffDAOFiltrado.limparListaComunidades();
        }
        if (listaComunidades != null && listaComunidades.size() > 0) {
            adapterCommunity.updateComunidadeList(listaComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                }
            });
        }
    }

    private void removeValueEventListener() {
        if (listenerHashMap != null && referenceHashMap != null) {
            for (String userId : listenerHashMap.keySet()) {
                Query userRef = referenceHashMap.get(userId);
                ChildEventListener listener = listenerHashMap.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                }
            }
            referenceHashMap.clear();
            listenerHashMap.clear();
        }
    }

    private void removeValueEventListenerFiltro() {
        if (listenerFiltroHashMap != null && referenceFiltroHashMap != null) {
            for (String comunidadeId : listenerFiltroHashMap.keySet()) {
                Query comunidadeRef = referenceFiltroHashMap.get(comunidadeId);
                ChildEventListener listener = listenerFiltroHashMap.get(comunidadeId);
                if (comunidadeRef != null && listener != null) {
                    comunidadeRef.removeEventListener(listener);
                }
            }
            referenceFiltroHashMap.clear();
            listenerFiltroHashMap.clear();
        }
    }

    private void configurarQueryInicial() {
        switch (tipoComunidade) {
            case CommunityUtils.MY_COMMUNITIES:
                queryInicial = firebaseRef.child("comunidades")
                        .orderByChild("idSuperAdmComunidade").equalTo(idUsuario).limitToFirst(1);
                break;
            case CommunityUtils.PUBLIC_COMMUNITIES:
                queryInicial = firebaseRef.child("publicCommunities")
                        .orderByChild("timestampinteracao").limitToFirst(1);
                break;
            case CommunityUtils.COMMUNITIES_FOLLOWING:
                queryInicial = firebaseRef.child("communityFollowing")
                        .child(idUsuario).limitToFirst(1);
                break;
        }
    }

    private void configurarQueryMore() {
        switch (tipoComunidade) {
            case CommunityUtils.MY_COMMUNITIES:
                queryLoadMore = firebaseRef.child("comunidades")
                        .orderByChild("idSuperAdmComunidade")
                        .equalTo(idUsuario).limitToFirst(PAGE_SIZE);
                break;
            case CommunityUtils.PUBLIC_COMMUNITIES:
                queryLoadMore = firebaseRef.child("publicCommunities")
                        .orderByChild("timestampinteracao")
                        .startAt(lastTimestamp)
                        .limitToFirst(PAGE_SIZE);
                break;
            case CommunityUtils.COMMUNITIES_FOLLOWING:
                queryLoadMore = firebaseRef.child("communityFollowing")
                        .child(idUsuario)
                        .orderByChild("timestampinteracao")
                        .startAt(lastTimestamp)
                        .limitToFirst(PAGE_SIZE);
                break;
        }
    }

    private String msgSemComunidades() {
        switch (tipoComunidade) {
            case Comunidade.MY_COMMUNITY:
                return getString(R.string.you_dont_have_community);
            case Comunidade.PUBLIC_COMMUNITY:
                return getString(R.string.no_public_community);
            case Comunidade.COMMUNITY_FOLLOWING:
                return getString(R.string.not_following_community);
        }
        return "";
    }

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, CommunityActivity.this);
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, CommunityActivity.this);
    }

    public boolean isPesquisaAtivada() {
        return pesquisaAtivada;
    }

    public void setPesquisaAtivada(boolean pesquisaAtivada) {
        this.pesquisaAtivada = pesquisaAtivada;
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    @Override
    public void onRemocao(Comunidade comunidadeAlvo, int posicao, String tipoComunidade) {

    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onExecutarAnimacao() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void inicializarComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        searchView = findViewById(R.id.searchViewComunidades);
        recyclerView = findViewById(R.id.recyclerViewComunidades);
        spinProgress = findViewById(R.id.spinProgressBarRecycler);
    }
}