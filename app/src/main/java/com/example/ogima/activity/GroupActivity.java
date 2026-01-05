package com.example.ogima.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ogima.R;
import com.example.ogima.adapter.AdapterPreviewCommunity;
import com.example.ogima.adapter.AdapterPreviewGroup;
import com.example.ogima.helper.CommunityFiltersFragment;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.GrupoDiffDAO;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GroupActivity extends AppCompatActivity implements AdapterPreviewGroup.RecuperaPosicaoAnterior, AdapterPreviewGroup.RemoverGrupoListener, AdapterPreviewGroup.AnimacaoIntent, CommunityFiltersFragment.RecuperarFiltrosCallback {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private TextView txtViewTitleToolbarBlack;
    private Toolbar toolbarIncBlack;
    private ImageButton imgBtnIncBackBlack;
    private ImageButton imgBtnExibirFiltros;
    private LinearLayout linearLayoutTopico;
    private static int PAGE_SIZE = 10;
    private int mCurrentPosition = -1;
    private boolean isLoading = false;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Grupo> listaGrupo = new ArrayList<>();
    private Set<String> idsGrupos = new HashSet<>();
    private GrupoDiffDAO grupoDiffDAO, grupoDAOFiltrado;
    private Query queryInicial, queryLoadMore;
    private HashMap<String, Object> listaDadosGrupo = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private Query queryInicialFiltro, queryLoadMoreFiltro, queryLoadMoreGrupo, queryInicialFind,
            newDataRef, queryLoadMorePesquisa;
    private ChildEventListener childListenerInicioFiltro,
            childEventListenerGrupos, childListenerMoreFiltro,
            childListenerInicio, childEventListenerNewData;
    private String nomePesquisado = "";
    private List<Grupo> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private boolean pesquisaAtivada = false;
    //private String lastId = null;
    private long lastTimestamp = -1;
    private boolean atualizandoLista = false;
    private Handler searchHandler = new Handler();
    private int queryDelayMillis = 500;
    private int searchCounter = 0;
    private String currentSearchText = "";
    private HashMap<String, Query> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private HashMap<String, Query> referenceFiltroHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerFiltroHashMap = new HashMap<>();
    private Set<String> idsListeners = new HashSet<>();
    private ValueEventListener listenerFiltragem;
    private AdapterPreviewGroup adapterPreviewGroup;
    private boolean trocarQueryInicial = false;
    private ChildEventListener childEventListenerFiltro;
    private Grupo grupoComparator;
    private int contadorRemocaoListener = 0;
    private FirebaseUtils firebaseUtils;
    private int travar = 0;
    private Set<String> idsListenersNEWDATA = new HashSet<>();
    private HashMap<String, Query> referenceHashMapNEWDATA = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapNEWDATA = new HashMap<>();
    private int contadorRemocaoListenerNEWDATA = 0;
    private static final String TAG = "GroupActivityTAG";
    private String tipoGrupo = "";
    private Set<Grupo> idsTempFiltro = new HashSet<>();
    private String idUltimoElemento, idUltimoElementoFiltro;
    private ValueEventListener listenerUltimoElemento, listenerUltimoElementoFiltro;
    private Query queryUltimoElemento, queryUltimoElementoFiltro;
    private ProgressDialog progressDialog;
    private Chip chip;
    private String idPrimeiroDado = "";
    private Set<String> idsAIgnorarListeners = new HashSet<>();
    private int contadorUpdate = 0;
    private int contadorNome = 0;
    private HashMap<String, Bundle> idsParaAtualizar = new HashMap<>();
    private int controleRemocao = 0;
    private int posicaoChanged = -1;
    private int aosFiltros = 0;
    private CommunityFiltersFragment bottomSheetDialogFragment;
    private ArrayList<String> topicosSelecionados;
    private boolean filtroPorTopico = false;
    private long lastTimeTopico = -1;

    @Override
    public void onStop() {
        super.onStop();
        if (adapterPreviewGroup != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                listaGrupo != null && listaGrupo.size() > 0
                && linearLayoutManager != null) {
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
    public void onDestroy() {
        super.onDestroy();
        limparPeloDestroy();
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
    public void onRemocao(Grupo grupoAlvo, int posicao, String tipoGrupo) {
        if (grupoAlvo != null) {
            logicaRemocao(grupoAlvo, false, true);
        }
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

    @Override
    public void onRecuperado(ArrayList<String> listaFiltrosRecuperados) {
        topicosSelecionados = listaFiltrosRecuperados;
        if (searchView != null) {
            atualizandoLista = true;
            limparFiltragem(true);
        }
        if (bottomSheetDialogFragment != null) {
            bottomSheetDialogFragment.dismiss();
        }
        exibirProgress();
        chip = new Chip(linearLayoutTopico.getContext());
        linearLayoutTopico.addView(chip);
        chip.setText(topicosSelecionados.get(0));
        chip.setChipBackgroundColor(ColorStateList.valueOf(getApplicationContext().getResources().getColor(R.color.friends_color)));
        chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
        chip.setTextSize(17);
        chip.setLayoutParams(params);
        chip.setClickable(false);
        setLoading(true);
        setFiltroPorTopico(true);
        dadoInicialPorTopico();
    }

    @Override
    public void onSemFiltros() {
        setFiltroPorTopico(false);
        ToastCustomizado.toastCustomizado("É necessário selecionar pelo menos um tópico para filtrar os grupos.", getApplicationContext());
    }

    public boolean isFiltroPorTopico() {
        return filtroPorTopico;
    }

    public void setFiltroPorTopico(boolean filtroPorTopico) {
        this.filtroPorTopico = filtroPorTopico;
    }

    private interface VerificaExistenciaCallback {
        void onExistencia(boolean status, Grupo grupoAtualizado);

        void onError(String message);
    }

    private interface RecuperaGrupo {
        void onRecuperado(Grupo grupoAtual);

        void onSemDado();

        void onError(String message);
    }

    private interface VerificaCriterio {
        void onCriterioAtendido();

        void onSemVinculo();

        void onError(String message);
    }

    private interface RecuperarBundleCallback {
        void onRecuperado();

        void onError(String message);
    }

    private interface RemoverListenersCallback {
        void onRemovido();
    }

    private interface RecuperaUltimoElemento {
        void onRecuperado();
    }

    private interface RecuperarIdsFiltroCallback {
        void onRecuperado(Set<Grupo> listaIdsRecuperados);
    }

    public GroupActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        grupoComparator = new Grupo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        inicializarComponentes();
        setSupportActionBar(toolbarIncBlack);
        setTitle("");
        configInicial();
    }

    private void configInicial() {
        firebaseUtils = new FirebaseUtils();
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        recuperarBundle(new RecuperarBundleCallback() {
            @Override
            public void onRecuperado() {
                setLoading(true);
                UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                    @Override
                    public void onConcluido(boolean epilepsia) {
                        setPesquisaAtivada(false);
                        configRecycler(epilepsia);
                        configSearchView();
                        clickListeners();
                        grupoDiffDAO = new GrupoDiffDAO(listaGrupo, adapterPreviewGroup);
                        grupoDAOFiltrado = new GrupoDiffDAO(listaFiltrada, adapterPreviewGroup);
                        progressDialog = new ProgressDialog(GroupActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setCancelable(false);
                        linearLayoutTopico.setVisibility(View.GONE);
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

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(message, getApplicationContext());
            }
        });
    }

    private void clickListeners() {
        imgBtnIncBackBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imgBtnExibirFiltros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    private void showBottomSheetDialog() {
        if (linearLayoutTopico != null && isFiltroPorTopico()) {
            linearLayoutTopico.removeAllViews();
        }
        limparFiltragem(false);
        bottomSheetDialogFragment = new CommunityFiltersFragment(this);
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    private void configSearchView() {
        searchView.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
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
                                    idsTempFiltro.clear();
                                    if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
                                        lastName = null;
                                        idsFiltrados.clear();
                                        nomePesquisado = "";
                                        grupoDAOFiltrado.limparListaGrupos();
                                        adapterPreviewGroup.updateGrupoList(listaFiltrada, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                                            @Override
                                            public void onAtualizado() {

                                            }
                                        });
                                    }

                                    removeValueEventListenerFiltro(new RemoverListenersCallback() {
                                        @Override
                                        public void onRemovido() {
                                            nomePesquisado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
                                            nomePesquisado = FormatarNomePesquisaUtils.removeAcentuacao(nomePesquisado).toUpperCase(Locale.ROOT);

                                            if (childListenerInicioFiltro != null && queryInicialFiltro != null) {
                                                queryInicialFiltro.removeEventListener(childListenerInicioFiltro);
                                            }

                                            idUltimoElementoFiltro = null;

                                            if (listenerUltimoElementoFiltro != null && queryUltimoElementoFiltro != null) {
                                                queryUltimoElementoFiltro.removeEventListener(listenerUltimoElementoFiltro);
                                            }

                                            ultimoElementoFiltro(nomePesquisado, new RecuperaUltimoElemento() {
                                                @Override
                                                public void onRecuperado() {
                                                    dadoInicialFiltragem(nomePesquisado, counter);
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }, queryDelayMillis);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (isPesquisaAtivada() && newText.isEmpty()) {
                    atualizandoLista = true;
                    if (linearLayoutTopico != null && isFiltroPorTopico()) {
                        linearLayoutTopico.removeAllViews();
                    }
                    limparFiltragem(true);
                }
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && isFiltroPorTopico()) {
                    linearLayoutTopico.setVisibility(View.GONE);
                    limparFiltragem(false);
                }
            }
        });
    }

    private void recuperarBundle(RecuperarBundleCallback callback) {
        Bundle dados = getIntent().getExtras();
        if (dados != null && dados.containsKey("tipoGrupo")) {
            tipoGrupo = dados.getString("tipoGrupo");
            if (tipoGrupo != null && !tipoGrupo.isEmpty()) {
                txtViewTitleToolbarBlack.setText(FormatarContadorUtils.abreviarTexto(tipoGrupo, 20));
                callback.onRecuperado();
            } else {
                callback.onError("Não foi possível recuperar os dados dos grupos");
            }
        } else {
            callback.onError("Não foi possível recuperar os dados dos grupos");
        }
    }

    private void configRecycler(boolean epilepsia) {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapterPreviewGroup = new AdapterPreviewGroup(getApplicationContext(),
                    listaGrupo, this, this, listaDadosGrupo,
                    getResources().getColor(R.color.chat_list_color), this, tipoGrupo);
            recyclerView.setAdapter(adapterPreviewGroup);
            adapterPreviewGroup.setStatusEpilepsia(epilepsia);
        }
    }

    private void configPaginacao() {
        if (recyclerView != null) {
            scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
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
                                    if (isFiltroPorTopico()) {
                                        carregarMaisDadosPorTopico();
                                    } else if (isPesquisaAtivada()) {
                                        carregarMaisDadosFiltrados(nomePesquisado, new RecuperarIdsFiltroCallback() {
                                            @Override
                                            public void onRecuperado(Set<Grupo> listaIdsRecuperados) {
                                                recuperarDetalhes(listaIdsRecuperados);
                                            }
                                        });
                                    } else {
                                        carregarMaisDados();
                                    }
                                }
                            }
                        }, 100);
                    }
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
    }

    private void recuperarDadosIniciais() {
        if (listaGrupo != null && !listaGrupo.isEmpty()) {
            trocarQueryInicial = false;
            return;
        }
        ajustarQueryInicial();
        exibirProgress();

        ultimoElemento(new RecuperaUltimoElemento() {
            @Override
            public void onRecuperado() {
                childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Grupo grupo = snapshot.getValue(Grupo.class);
                            if (grupo != null
                                    && grupo.getIdGrupo() != null
                                    && !grupo.getIdGrupo().isEmpty()) {
                                idPrimeiroDado = grupo.getIdGrupo();
                                if (travar == 0) {
                                    lastTimestamp = grupo.getTimestampinteracao();
                                    adicionarGrupos(grupo, false);
                                } else {
                                    //Dado mais recente que o anterior
                                    if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                            && listenerHashMapNEWDATA.containsKey(grupo.getIdGrupo())) {
                                        return;
                                    }
                                    anexarNovoDado(grupo);
                                }
                            }
                        } else {
                            ocultarProgress();
                            //Exibir um textview com essa mensagem.
                            String msgSemConversas = "Você não possui conversas no momento.";
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Grupo.class).getIdGrupo())) {
                                return;
                            }
                            logicaAtualizacao(snapshot, false);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                            if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Grupo.class).getIdGrupo())) {
                                //O próprio listenernewdata vai cuidar da remoção desse dado.
                                return;
                            }

                            logicaRemocao(grupoRemovido, true, true);

                            verificaExistencia(grupoRemovido.getIdGrupo(), new VerificaExistenciaCallback() {
                                @Override
                                public void onExistencia(boolean status, Grupo grupoAtualizado) {
                                    if (status) {
                                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                                && listenerHashMapNEWDATA.containsKey(grupoRemovido.getIdGrupo())) {
                                        } else {
                                            anexarNovoDado(grupoAtualizado);
                                        }
                                    }
                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        lastTimestamp = -1;
                        ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao recuperar as suas conversas", "Code:", error.getCode()), getApplicationContext());
                        onBackPressed();
                    }
                });
            }
        });
    }

    private void dadoInicialFiltragem(String nome, int counter) {

        exibirProgress();
        queryInicialFind = firebaseRef.child("groups_by_name")
                .orderByChild("nomeGrupoPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff");

        queryInicialFind.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (counter != searchCounter) {
                    ocultarProgress();
                    setLoading(false);
                    firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
                    firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
                    return;
                }

                if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    ocultarProgress();
                    setLoading(false);
                    firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
                    firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
                    return;
                }

                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Grupo grupoPesquisa = snapshotChildren.getValue(Grupo.class);
                        if (grupoPesquisa != null && grupoPesquisa.getIdGrupo() != null
                                && !grupoPesquisa.getIdGrupo().isEmpty()) {
                            recuperaDadosGrupo(grupoPesquisa.getIdGrupo(), new RecuperaGrupo() {
                                @Override
                                public void onRecuperado(Grupo grupoAtual) {

                                    GroupUtils.checkBlockingStatus(getApplicationContext(), grupoAtual.getIdGrupo(), new GroupUtils.CheckLockCallback() {
                                        @Override
                                        public void onBlocked(boolean status) {
                                            grupoAtual.setIndisponivel(status);
                                            adicionarGrupoFiltrado(grupoAtual);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            grupoAtual.setIndisponivel(true);
                                            adicionarGrupoFiltrado(grupoAtual);
                                        }
                                    });

                                }

                                @Override
                                public void onSemDado() {

                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                        }
                    }
                } else {
                    ocultarProgress();
                }
                queryInicialFind.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocultarProgress();
                lastName = null;
            }
        });
    }

    private void adicionarGrupos(Grupo grupoAlvo, boolean dadoModificado) {
        recuperaDadosGrupo(grupoAlvo.getIdGrupo(), new RecuperaGrupo() {
            @Override
            public void onRecuperado(Grupo dadosGrupo) {
                grupoAlvo.setNomeGrupo(dadosGrupo.getNomeGrupoPesquisa());
                grupoDiffDAO.adicionarGrupo(grupoAlvo);
                grupoDiffDAO.adicionarIdAoSet(idsGrupos, dadosGrupo.getIdGrupo());

                List<Grupo> listaAtual = new ArrayList<>();
                if (isPesquisaAtivada()) {
                    listaAtual = listaFiltrada;
                } else {
                    listaAtual = listaGrupo;
                }

                Collections.sort(listaAtual, grupoComparator);

                adapterPreviewGroup.updateGrupoList(listaAtual, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        travar = 1;

                        if (dadoModificado) {
                            adicionarDadoDoGrupo(dadosGrupo, newDataRef, childEventListenerNewData, dadoModificado);
                        } else {
                            adicionarDadoDoGrupo(dadosGrupo, null, null, dadoModificado);
                        }
                        ocultarProgress();
                        setLoading(false);

                        if (travar != 0) {
                            if (areFirstThreeItemsVisible(recyclerView)) {
                                int newPosition = 0; // A posição para a qual você deseja rolar
                                recyclerView.scrollToPosition(newPosition);
                            }
                        }
                    }
                });
            }

            @Override
            public void onSemDado() {
                trocarQueryInicial = true;
                recuperarDadosIniciais();
            }

            @Override
            public void onError(String message) {
                ocultarProgress();
                setLoading(false);
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar suas conversas.", getApplicationContext());
            }
        });
    }

    private void adicionarGrupoFiltrado(Grupo dadosGrupo) {
        if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
            String idGrupoInicioFiltro = listaFiltrada.get(0).getIdGrupo();
            if (idGrupoInicioFiltro.equals(dadosGrupo.getIdGrupo())) {
                ocultarProgress();
                setLoading(false);
                return;
            }
        }

        queryInicialFiltro = firebaseRef.child("grupos")
                .orderByChild("idGrupo")
                .equalTo(dadosGrupo.getIdGrupo()).limitToFirst(1);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    Grupo grupoAtual = snapshot.getValue(Grupo.class);

                    if (grupoAtual != null) {
                        grupoAtual.setNomeGrupo(dadosGrupo.getNomeGrupoPesquisa());
                        grupoAtual.setIndisponivel(dadosGrupo.isIndisponivel());
                    }

                    if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
                        String idGrupoInicioFiltro = listaFiltrada.get(0).getIdGrupo();
                        if (idGrupoInicioFiltro.equals(dadosGrupo.getIdGrupo())) {
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }
                    }

                    lastName = dadosGrupo.getNomeGrupoPesquisa();

                    grupoDAOFiltrado.adicionarGrupo(grupoAtual);
                    grupoDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosGrupo.getIdGrupo());

                    Collections.sort(listaFiltrada, grupoComparator);
                    adapterPreviewGroup.updateGrupoList(listaFiltrada, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            referenceFiltroHashMap.put(dadosGrupo.getIdGrupo(), queryInicialFiltro);
                            listenerFiltroHashMap.put(dadosGrupo.getIdGrupo(), childListenerInicioFiltro);
                            adicionarDadoDoGrupo(dadosGrupo, queryInicialFiltro, childListenerInicioFiltro, false);
                            setLoading(false);
                        }
                    });
                } else {
                    ocultarProgress();
                    setLoading(false);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (idsAIgnorarListeners != null && !idsAIgnorarListeners.isEmpty()
                        && idsAIgnorarListeners.contains(snapshot.getValue(Grupo.class).getIdGrupo())) {
                    return;
                }
                if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                        && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Grupo.class).getIdGrupo())) {
                    return;
                }

                if (listenerHashMap != null && !listenerHashMap.isEmpty()
                        && listenerHashMap.containsKey(snapshot.getValue(Grupo.class).getIdGrupo())) {
                    return;
                }
                logicaAtualizacao(snapshot, false);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocultarProgress();
                setLoading(false);
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao realizar a pesquisa.", getApplicationContext());
            }
        });
    }

    private void carregarMaisDadosFiltrados(String dadoAnterior, RecuperarIdsFiltroCallback callback) {
        if (isPesquisaAtivada() && listaFiltrada != null) {

            if (listaFiltrada.size() > 1
                    && idUltimoElementoFiltro != null && !idUltimoElementoFiltro.isEmpty()
                    && idUltimoElementoFiltro.equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdGrupo())) {
                ocultarProgress();
                return;
            }

            if (listaFiltrada != null && !listaFiltrada.isEmpty()
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMorePesquisa = firebaseRef.child("groups_by_name")
                        .orderByChild("nomeGrupoPesquisa")
                        .startAt(dadoAnterior).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
                queryLoadMorePesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        exibirProgress();
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                                Grupo grupoPesquisa = snapshotChildren.getValue(Grupo.class);
                                if (grupoPesquisa != null && grupoPesquisa.getIdGrupo() != null
                                        && !grupoPesquisa.getIdGrupo().isEmpty()) {

                                    if (listenerFiltroHashMap != null && !listenerFiltroHashMap.isEmpty()
                                            && listenerFiltroHashMap.containsKey(grupoPesquisa.getIdGrupo())) {
                                        ocultarProgress();
                                        setLoading(false);
                                    } else {
                                        idsTempFiltro.add(grupoPesquisa);
                                        callback.onRecuperado(idsTempFiltro);
                                    }
                                }
                            }
                        } else {
                            ocultarProgress();
                        }
                        if (queryLoadMorePesquisa != null) {
                            queryLoadMorePesquisa.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ocultarProgress();
                        lastName = null;
                    }
                });
            }
        }
    }

    private void carregarMaisDados() {
        if (!isPesquisaAtivada()) {
            if (listaGrupo.size() > 1
                    && idUltimoElemento != null && !idUltimoElemento.isEmpty()
                    && idUltimoElemento.equals(listaGrupo.get(listaGrupo.size() - 1).getIdGrupo())) {
                ocultarProgress();
                return;
            }
            ajustarQueryMore();
            childEventListenerGrupos = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    exibirProgress();
                    if (snapshot.getValue() != null) {
                        Grupo grupoMore = snapshot.getValue(Grupo.class);
                        if (grupoMore != null
                                && grupoMore.getIdGrupo() != null
                                && !grupoMore.getIdGrupo().isEmpty()) {
                            Log.d(TAG, "Timestamp key: " + lastTimestamp);
                            Log.d(TAG, "id: " + grupoMore.getIdGrupo() + " time: " + grupoMore.getTimestampinteracao());
                            if (listaGrupo != null && listaGrupo.size() > 1 && idsGrupos != null && !idsGrupos.isEmpty()
                                    && idsGrupos.contains(grupoMore.getIdGrupo())) {
                                Log.d(TAG, "Id já existia: " + grupoMore.getIdGrupo());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            if (listaGrupo != null && listaGrupo.size() > 1
                                    && grupoMore.getTimestampinteracao() < listaGrupo.get(0).getTimestampinteracao()) {
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            List<Grupo> newGrupos = new ArrayList<>();
                            long key = grupoMore.getTimestampinteracao();
                            if (lastTimestamp != -1 && key != -1) {
                                if (key != lastTimestamp || !listaGrupo.isEmpty() &&
                                        !grupoMore.getIdGrupo()
                                                .equals(listaGrupo.get(listaGrupo.size() - 1).getIdGrupo())) {
                                    newGrupos.add(grupoMore);
                                    lastTimestamp = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newGrupos.size() > PAGE_SIZE) {
                                newGrupos.remove(0);
                            }
                            if (lastTimestamp != -1) {

                                recuperaDadosGrupo(grupoMore.getIdGrupo(), new RecuperaGrupo() {
                                    @Override
                                    public void onRecuperado(Grupo dadosGrupo) {
                                        for (Grupo grupo : newGrupos) {
                                            if (grupo.getIdGrupo().equals(dadosGrupo.getIdGrupo())) {
                                                newGrupos.remove(grupo);
                                                grupo.setNomeGrupo(dadosGrupo.getNomeGrupoPesquisa());
                                                newGrupos.add(grupo);
                                                contadorNome++;
                                            }
                                            if (contadorNome == newGrupos.size()) {
                                                contadorNome = 0;
                                                adicionarMaisDados(newGrupos, grupoMore.getIdGrupo(), dadosGrupo, queryLoadMore);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onSemDado() {
                                        ocultarProgress();
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
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        if (idsAIgnorarListeners != null && !idsAIgnorarListeners.isEmpty()
                                && idsAIgnorarListeners.contains(snapshot.getValue(Grupo.class).getIdGrupo())) {
                            return;
                        }
                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Grupo.class).getIdGrupo())) {
                            return;
                        }
                        logicaAtualizacao(snapshot, false);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                        if (grupoRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                && listenerHashMapNEWDATA.containsKey(grupoRemovido.getIdGrupo())
                                || listaGrupo != null && !listaGrupo.isEmpty()
                                && listaGrupo.get(0).getIdGrupo().equals(grupoRemovido.getIdGrupo())) {
                            return;
                        }

                        verificaExistencia(grupoRemovido.getIdGrupo(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Grupo grupoAtualizado) {

                                logicaRemocao(grupoRemovido, true, true);

                                if (status) {
                                    boolean menorque = grupoAtualizado.getTimestampinteracao() <= listaGrupo.get(0).getTimestampinteracao();
                                    if (!menorque) {
                                        anexarNovoDado(grupoAtualizado);
                                    }
                                }
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ocultarProgress();
                    lastTimestamp = -1;
                }
            });
        }
    }

    private void adicionarMaisDados(List<Grupo> newGrupos, String idGrupo, Grupo dadosGrupo, Query queryAlvo) {
        if (newGrupos != null && !newGrupos.isEmpty()) {
            grupoDiffDAO.carregarMaisGrupo(newGrupos, idsGrupos);
            grupoDiffDAO.adicionarIdAoSet(idsGrupos, idGrupo);

            Collections.sort(listaGrupo, grupoComparator);
            adapterPreviewGroup.updateGrupoList(listaGrupo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    ocultarProgress();
                    adicionarDadoDoGrupo(dadosGrupo, queryAlvo, childEventListenerGrupos, false);
                    setLoading(false);
                }
            });
        } else {
            ocultarProgress();
        }
    }

    private void adicionarMaisDadosFiltrados(List<Grupo> newGrupos, String idGrupo, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (newGrupos != null && !newGrupos.isEmpty()) {
            recuperaDadosGrupo(idGrupo, new RecuperaGrupo() {
                @Override
                public void onRecuperado(Grupo dadosGrupo) {
                    for (Grupo grupo : newGrupos) {
                        if (grupo.getIdGrupo().equals(dadosGrupo.getIdGrupo())) {
                            newGrupos.remove(grupo);
                            grupo.setNomeGrupo(dadosGrupo.getNomeGrupoPesquisa());
                            newGrupos.add(grupo);
                            contadorNome++;
                        }

                        if (contadorNome == newGrupos.size()) {
                            contadorNome = 0;
                            grupoDAOFiltrado.carregarMaisGrupo(newGrupos, idsFiltrados);
                            grupoDAOFiltrado.adicionarIdAoSet(idsFiltrados, idGrupo);

                            Collections.sort(listaFiltrada, grupoComparator);
                            adapterPreviewGroup.updateGrupoList(listaFiltrada, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {
                                    ocultarProgress();
                                    adicionarDadoDoGrupo(dadosGrupo, queryAlvo, childEventListenerAlvo, false);
                                    setLoading(false);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onSemDado() {
                    ocultarProgress();
                }

                @Override
                public void onError(String message) {
                    ocultarProgress();
                }
            });
        } else {
            ocultarProgress();
        }
    }

    private void recuperaDadosGrupo(String idGrupo, RecuperaGrupo callback) {
        FirebaseRecuperarUsuario.recuperaGrupo(idGrupo, new FirebaseRecuperarUsuario.RecuperaGrupoCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                GroupUtils.checkBlockingStatus(getApplicationContext(), idGrupo, new GroupUtils.CheckLockCallback() {
                    @Override
                    public void onBlocked(boolean status) {
                        grupoAtual.setIndisponivel(status);
                        callback.onRecuperado(grupoAtual);
                    }

                    @Override
                    public void onError(String message) {
                        grupoAtual.setIndisponivel(true);
                        callback.onRecuperado(grupoAtual);
                    }
                });
            }

            @Override
            public void onSemDado() {
                callback.onSemDado();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private void adicionarDadoDoGrupo(Grupo dadosGrupo, Query queryAlvo, ChildEventListener childEventListenerAlvo, boolean dadoModificado) {
        listaDadosGrupo.put(dadosGrupo.getIdGrupo(), dadosGrupo);

        if (childEventListenerAlvo == null || queryAlvo == null) {
            return;
        }

        if (dadoModificado) {
            adicionarListenerNEWDATA(dadosGrupo.getIdGrupo(), queryAlvo, childEventListenerAlvo);
            return;
        }

        if (!isPesquisaAtivada()) {
            adicionarListener(dadosGrupo.getIdGrupo(), queryAlvo, childEventListenerAlvo);
        }
    }

    private void adicionarListener(String idGrupo, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (idsListeners != null && !idsListeners.isEmpty()
                && idsListeners.contains(idGrupo)) {
            return;
        }
        if (idsListeners != null) {
            idsListeners.add(idGrupo);
        }
        referenceHashMap.put(idGrupo, queryAlvo);
        listenerHashMap.put(idGrupo, childEventListenerAlvo);
    }

    private void adicionarListenerNEWDATA(String idGrupo, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (idsListenersNEWDATA != null && !idsListenersNEWDATA.isEmpty()
                && idsListenersNEWDATA.contains(idGrupo)) {
            return;
        }
        if (idsListenersNEWDATA != null) {
            idsListenersNEWDATA.add(idGrupo);
        }
        referenceHashMapNEWDATA.put(idGrupo, queryAlvo);
        listenerHashMapNEWDATA.put(idGrupo, childEventListenerAlvo);
    }

    private void limparPeloDestroy() {
        idsAIgnorarListeners.clear();
        firebaseUtils.removerQueryChildListener(newDataRef, childEventListenerNewData);
        firebaseUtils.removerQueryChildListener(queryInicial, childListenerInicio);
        firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
        firebaseUtils.removerQueryChildListener(queryLoadMore, childEventListenerGrupos);
        firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
        firebaseUtils.removerQueryValueListener(queryUltimoElemento, listenerUltimoElemento);
        removeValueEventListener();
        removeValueEventListenerNEWDATA();
        removeValueEventListenerFiltro(null);
        if (grupoDiffDAO != null) {
            grupoDiffDAO.limparListaGrupos();
        }
        if (listaDadosGrupo != null) {
            listaDadosGrupo.clear();
        }

        if (idsGrupos != null) {
            idsGrupos.clear();
        }
        if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
            grupoDAOFiltrado.limparListaGrupos();
            idsFiltrados.clear();
        }
        setPesquisaAtivada(false);
        nomePesquisado = null;
        mCurrentPosition = -1;
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }

    private void limparFiltragem(boolean fecharTeclado) {
        if (searchView != null && fecharTeclado) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            searchView.clearFocus();
        }
        removeValueEventListenerFiltro(null);
        if (childListenerInicioFiltro != null && queryInicialFiltro != null) {
            queryInicialFiltro.removeEventListener(childListenerInicioFiltro);
        }
        firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);

        if (listenerUltimoElementoFiltro != null && queryUltimoElementoFiltro != null) {
            queryUltimoElementoFiltro.removeEventListener(listenerUltimoElementoFiltro);
        }

        lastName = null;
        if (idsFiltrados != null) {
            idsFiltrados.clear();
        }
        setPesquisaAtivada(false);
        setFiltroPorTopico(false);
        lastTimeTopico = -1;
        nomePesquisado = "";
        ocultarProgress();
        if (grupoDAOFiltrado != null) {
            grupoDAOFiltrado.limparListaGrupos();
        }
        if (listaGrupo != null && !listaGrupo.isEmpty()) {

            setLoading(false);

            Collections.sort(listaGrupo, grupoComparator);
            adapterPreviewGroup.updateGrupoList(listaGrupo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                    contadorUpdate++;
                    if (idsParaAtualizar != null && !idsParaAtualizar.isEmpty()) {
                        for (String idUpdate : idsParaAtualizar.keySet()) {
                            int index = adapterPreviewGroup.findPositionInList(idUpdate);
                            Bundle bundleUpdate = idsParaAtualizar.get(idUpdate);
                            if (index != -1) {
                                adapterPreviewGroup.notifyItemChanged(index, bundleUpdate);
                            }
                        }
                        if (contadorUpdate >= idsParaAtualizar.size()) {
                            idsParaAtualizar.clear();
                            contadorUpdate = 0;
                        }
                    }
                }
            });
        }
    }

    public void removeValueEventListener() {
        if (listenerHashMap != null && referenceHashMap != null) {
            for (String grupoId : listenerHashMap.keySet()) {
                Query grupoRef = referenceHashMap.get(grupoId);
                ChildEventListener listener = listenerHashMap.get(grupoId);
                if (grupoRef != null && listener != null) {
                    grupoRef.removeEventListener(listener);
                }
                contadorRemocaoListener++;
                if (contadorRemocaoListener == referenceHashMap.size()) {
                    referenceHashMap.clear();
                    listenerHashMap.clear();
                }
            }
        }
    }

    public void removeValueEventListenerNEWDATA() {
        if (listenerHashMapNEWDATA != null && referenceHashMapNEWDATA != null) {
            for (String grupoId : listenerHashMapNEWDATA.keySet()) {
                Query grupoRef = referenceHashMapNEWDATA.get(grupoId);
                ChildEventListener listener = listenerHashMapNEWDATA.get(grupoId);
                if (grupoRef != null && listener != null) {
                    grupoRef.removeEventListener(listener);
                }
                contadorRemocaoListenerNEWDATA++;
                if (contadorRemocaoListenerNEWDATA == referenceHashMapNEWDATA.size()) {
                    referenceHashMapNEWDATA.clear();
                    listenerHashMapNEWDATA.clear();
                }
            }
        }
    }

    private void removeValueEventListenerFiltro(RemoverListenersCallback callback) {
        if (listenerFiltroHashMap != null && referenceFiltroHashMap != null
                && !listenerFiltroHashMap.isEmpty() && !referenceFiltroHashMap.isEmpty()) {

            for (String grupoId : listenerFiltroHashMap.keySet()) {
                Query grupoRef = referenceFiltroHashMap.get(grupoId);
                ChildEventListener listener = listenerFiltroHashMap.get(grupoId);
                if (grupoRef != null && listener != null) {
                    grupoRef.removeEventListener(listener);
                }

                controleRemocao++;
                if (controleRemocao == referenceFiltroHashMap.size()) {
                    referenceFiltroHashMap.clear();
                    listenerFiltroHashMap.clear();
                    controleRemocao = 0;
                    if (childListenerMoreFiltro != null && queryLoadMoreFiltro != null) {
                        queryLoadMoreFiltro.removeEventListener(childListenerMoreFiltro);
                    }
                    if (callback != null) {
                        callback.onRemovido();
                    }
                }
            }
        } else {
            if (callback != null) {
                callback.onRemovido();
            }
        }
    }

    private void logicaRemocao(Grupo grupoRemovido, boolean ignorarVerificacao, boolean excluirDaLista) {

        if (grupoRemovido == null) {
            return;
        }

        DatabaseReference verificaExistenciaRef = firebaseRef.child("grupos")
                .child(grupoRemovido.getIdGrupo());
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || ignorarVerificacao) {

                    if (idsFiltrados != null && !idsFiltrados.isEmpty()
                            && idsFiltrados.contains(grupoRemovido.getIdGrupo())) {
                        if (listaFiltrada != null && !listaFiltrada.isEmpty() && excluirDaLista) {
                            if (idsFiltrados != null && !idsFiltrados.isEmpty()) {
                                idsFiltrados.remove(grupoRemovido.getIdGrupo());
                            }
                            grupoDAOFiltrado.removerGrupo(grupoRemovido);
                        }
                    }

                    if (idsGrupos != null && !idsGrupos.isEmpty()
                            && idsGrupos.contains(grupoRemovido.getIdGrupo())) {
                        if (listaGrupo != null && !listaGrupo.isEmpty() && excluirDaLista) {
                            if (idsGrupos != null && !idsGrupos.isEmpty()) {
                                idsGrupos.remove(grupoRemovido.getIdGrupo());
                            }
                            grupoDiffDAO.removerGrupo(grupoRemovido);
                        }
                    }

                    if (listaDadosGrupo != null && !listaDadosGrupo.isEmpty() && excluirDaLista) {
                        listaDadosGrupo.remove(grupoRemovido.getIdGrupo());
                        int posicao = adapterPreviewGroup.findPositionInList(grupoRemovido.getIdGrupo());
                        if (posicao != -1) {
                            adapterPreviewGroup.notifyItemChanged(posicao);
                        }
                    }

                    if (isPesquisaAtivada() && listaFiltrada != null) {
                        adapterPreviewGroup.updateGrupoList(listaFiltrada, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {

                            }
                        });
                    } else if (!isPesquisaAtivada() && listaGrupo != null) {
                        adapterPreviewGroup.updateGrupoList(listaGrupo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {

                            }
                        });
                    }
                }
                verificaExistenciaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void logicaAtualizacao(DataSnapshot snapshot, boolean apenasAtualizar) {
        if (snapshot.getValue() != null) {
            Grupo grupoAtualizado = snapshot.getValue(Grupo.class);

            if (grupoAtualizado == null || grupoAtualizado.getIdGrupo() == null) {
                return;
            }

            posicaoChanged = adapterPreviewGroup.findPositionInList(grupoAtualizado.getIdGrupo());

            if (posicaoChanged == -1 && isPesquisaAtivada()) {
                posicaoChanged = findPositionInList(grupoAtualizado.getIdGrupo());
            }

            if (posicaoChanged != -1) {
                Grupo grupoAnterior = new Grupo();
                if (idsGrupos != null && !idsGrupos.isEmpty()
                        && idsGrupos.contains(grupoAtualizado.getIdGrupo())) {
                    //Já existe um listener na listagem normal
                    if (isPesquisaAtivada()
                            && referenceFiltroHashMap != null
                            && !referenceFiltroHashMap.isEmpty()
                            && referenceFiltroHashMap.containsKey(grupoAtualizado.getIdGrupo())) {
                        grupoAnterior = listaFiltrada.get(posicaoChanged);
                    } else {
                        grupoAnterior = listaGrupo.get(posicaoChanged);
                    }
                } else if (isPesquisaAtivada()
                        && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    grupoAnterior = listaFiltrada.get(posicaoChanged);
                }

                if (!grupoAnterior.getNomeGrupo().equals(grupoAtualizado.getNomeGrupo())) {
                    atualizarPorPayload(grupoAtualizado, "nomeGrupo");
                }
                if (!grupoAnterior.getDescricaoGrupo().equals(grupoAtualizado.getDescricaoGrupo())) {
                    atualizarPorPayload(grupoAtualizado, "descricaoGrupo");
                }
                if (!grupoAnterior.getFotoGrupo().equals(grupoAtualizado.getFotoGrupo())) {
                    atualizarPorPayload(grupoAtualizado, "fotoGrupo");
                }
                if (!grupoAnterior.getTopicos().equals(grupoAtualizado.getTopicos())) {
                    atualizarPorPayload(grupoAtualizado, "topicosGrupo");
                }

                if (isPesquisaAtivada() && listaFiltrada != null) {

                    Collections.sort(listaFiltrada, grupoComparator);

                    adapterPreviewGroup.updateGrupoList(listaFiltrada, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                } else if (!isPesquisaAtivada() && listaGrupo != null) {

                    Collections.sort(listaGrupo, grupoComparator);
                    adapterPreviewGroup.updateGrupoList(listaGrupo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                }
            }
            posicaoChanged = -1;
        }
    }

    private void atualizarPorPayload(Grupo grupoAtualizado, String tipoPayload) {

        int index = posicaoChanged;

        if (index != -1) {

            if (isPesquisaAtivada() && referenceFiltroHashMap != null
                    && !referenceFiltroHashMap.isEmpty()
                    && referenceFiltroHashMap.containsKey(grupoAtualizado.getIdGrupo())) {
                grupoDAOFiltrado.atualizarGrupoPorPayload(grupoAtualizado, tipoPayload, new GrupoDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        adapterPreviewGroup.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
            if (idsGrupos != null && !idsGrupos.isEmpty()
                    && idsGrupos.contains(grupoAtualizado.getIdGrupo())) {
                grupoDiffDAO.atualizarGrupoPorPayload(grupoAtualizado, tipoPayload, new GrupoDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        if (!isPesquisaAtivada()) {
                            adapterPreviewGroup.notifyItemChanged(index, bundleRecup);
                        } else {
                            idsParaAtualizar.put(grupoAtualizado.getIdGrupo(), bundleRecup);
                        }
                    }
                });
            }
        }
    }

    private void verificaExistencia(String idGrupos, VerificaExistenciaCallback callback) {
        DatabaseReference verificaExistenciaRef = firebaseRef.child("grupos")
                .child(idGrupos);
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onExistencia(snapshot.getValue() != null, snapshot.getValue(Grupo.class));
                verificaExistenciaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void anexarNovoDado(Grupo grupoModificado) {
        newDataRef = firebaseRef.child("grupos")
                .orderByChild("idGrupo")
                .equalTo(grupoModificado.getIdGrupo()).limitToFirst(1);
        idsAIgnorarListeners.add(grupoModificado.getIdGrupo());
        childEventListenerNewData = newDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo grupo = snapshot.getValue(Grupo.class);
                    if (grupo == null) {
                        return;
                    }
                    String idGrupoModificado = grupo.getIdGrupo();
                    adicionarGrupos(grupo, true);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    logicaAtualizacao(snapshot, true);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                    if (idsAIgnorarListeners != null && !idsAIgnorarListeners.isEmpty()
                            && idsAIgnorarListeners.contains(grupoRemovido.getIdGrupo())) {
                        idsAIgnorarListeners.remove(grupoRemovido.getIdGrupo());
                    }
                    logicaRemocao(grupoRemovido, true, true);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarDetalhes(Set<Grupo> listaIdsRecuperados) {
        for (Grupo grupoPesquisa : listaIdsRecuperados) {
            aosFiltros++;
            if (aosFiltros > listaIdsRecuperados.size()) {
                aosFiltros = 0;
                return;
            }

            childListenerMoreFiltro = null;
            queryLoadMoreFiltro = null;

            queryLoadMoreFiltro = firebaseRef.child("grupos")
                    .orderByChild("idGrupo")
                    .equalTo(grupoPesquisa.getIdGrupo()).limitToFirst(1);

            ChildEventListener childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Grupo grupoMore = snapshot.getValue(Grupo.class);
                    if (grupoMore != null
                            && grupoMore.getIdGrupo() != null
                            && !grupoMore.getIdGrupo().isEmpty()) {
                        Log.d(TAG, "Timestamp key: " + lastTimestamp);
                        Log.d(TAG, "id: " + grupoMore.getIdGrupo() + " time: " + grupoMore.getTimestampinteracao());
                        if (listaFiltrada != null && listaFiltrada.size() > 1 && idsFiltrados != null && !idsFiltrados.isEmpty()
                                && idsFiltrados.contains(grupoMore.getIdGrupo())) {
                            Log.d(TAG, "Id já existia: " + grupoMore.getIdGrupo());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        List<Grupo> newGrupos = new ArrayList<>();
                        String key = grupoPesquisa.getNomeGrupoPesquisa();
                        if (lastName != null && !lastName.isEmpty() && key != null
                                && !key.isEmpty()) {
                            if (!key.equals(lastName) || !listaFiltrada.isEmpty() &&
                                    !grupoMore.getIdGrupo()
                                            .equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdGrupo())) {
                                newGrupos.add(grupoMore);
                                lastName = key;
                            }
                        }
                        // Remove a última chave usada
                        if (newGrupos.size() > PAGE_SIZE) {
                            newGrupos.remove(0);
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (aosFiltros >= listaIdsRecuperados.size()) {
                                aosFiltros = 0;
                            }
                            adicionarMaisDadosFiltrados(newGrupos, grupoMore.getIdGrupo(), queryLoadMoreFiltro, childListenerMoreFiltro);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() == null) {
                        return;
                    }
                    Grupo grupoUpdate = snapshot.getValue(Grupo.class);

                    if (grupoUpdate == null) {
                        return;
                    }

                    if (idsAIgnorarListeners != null && !idsAIgnorarListeners.isEmpty()
                            && idsAIgnorarListeners.contains(snapshot.getValue(Grupo.class).getIdGrupo())) {
                        return;
                    }
                    if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                            && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Grupo.class).getIdGrupo())) {
                        return;
                    }

                    if (listenerHashMap != null && !listenerHashMap.isEmpty()
                            && listenerHashMap.containsKey(snapshot.getValue(Grupo.class).getIdGrupo())) {
                        return;
                    }

                    if (listaFiltrada != null && !listaFiltrada.isEmpty()
                            && grupoUpdate.getIdGrupo().equals(listaFiltrada.get(0).getIdGrupo())) {
                        return;
                    }

                    logicaAtualizacao(snapshot, false);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                        if (grupoRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                && listenerHashMapNEWDATA.containsKey(grupoRemovido.getIdGrupo())
                                || listaGrupo != null && !listaGrupo.isEmpty()
                                && listaGrupo.get(0).getIdGrupo().equals(grupoRemovido.getIdGrupo())) {
                            return;
                        }

                        verificaExistencia(grupoRemovido.getIdGrupo(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Grupo grupoAtualizado) {

                                logicaRemocao(grupoRemovido, true, true);

                                if (status) {
                                    boolean menorque = grupoAtualizado.getTimestampinteracao() <= listaGrupo.get(0).getTimestampinteracao();
                                    if (!menorque) {
                                        anexarNovoDado(grupoAtualizado);
                                    }
                                }
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            queryLoadMoreFiltro.addChildEventListener(childListener);
            listenerFiltroHashMap.put(grupoPesquisa.getIdGrupo(), childListener);
            referenceFiltroHashMap.put(grupoPesquisa.getIdGrupo(), queryLoadMoreFiltro);
        }
    }

    private void ultimoElemento(RecuperaUltimoElemento callback) {
        ajustarQueryLast();
        listenerUltimoElemento = queryUltimoElemento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElemento = snapshot1.getValue(Grupo.class).getIdGrupo();
                    setLoading(false);
                    if (callback != null && listaGrupo != null) {
                        callback.onRecuperado();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaGrupo != null) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void ultimoElementoFiltro(String nome, RecuperaUltimoElemento callback) {
        queryUltimoElementoFiltro = firebaseRef.child("groups_by_name")
                .orderByChild("nomeGrupoPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToLast(1);
        listenerUltimoElementoFiltro = queryUltimoElementoFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElementoFiltro = snapshot1.getValue(Grupo.class).getIdGrupo();
                    setLoading(false);
                    if (callback != null && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                        callback.onRecuperado();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    callback.onRecuperado();
                }
            }
        });
    }

    public int findPositionInList(String grupoId) {
        for (int i = 0; i < listaGrupo.size(); i++) {
            Grupo grupo = listaGrupo.get(i);
            if (grupo.getIdGrupo().equals(grupoId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }

    private boolean areFirstThreeItemsVisible(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibleItemPosition = 0;
        if (layoutManager != null) {
            firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        }
        return firstVisibleItemPosition <= 2;
    }

    private void ajustarQueryInicial() {
        switch (tipoGrupo) {
            case GroupUtils.MY_GROUPS:
                queryInicial = firebaseRef.child("grupos")
                        .orderByChild("idSuperAdmGrupo").equalTo(idUsuario).limitToFirst(1);
                break;
            case GroupUtils.PUBLIC_GROUPS:
                imgBtnExibirFiltros.setVisibility(View.VISIBLE);
                if (trocarQueryInicial) {
                    queryInicial = firebaseRef.child("publicGroups")
                            .orderByChild("timestampinteracao")
                            .startAt(lastTimestamp + 1).limitToFirst(1);
                } else {
                    queryInicial = firebaseRef.child("publicGroups")
                            .orderByChild("timestampinteracao").limitToFirst(1);
                }
                break;
            case GroupUtils.GROUPS_FOLLOWING:
                if (trocarQueryInicial) {
                    queryInicial = firebaseRef.child("groupFollowing")
                            .child(idUsuario).orderByChild("timestampinteracao")
                            .startAt(lastTimestamp + 1).limitToFirst(1);
                } else {
                    queryInicial = firebaseRef.child("groupFollowing")
                            .child(idUsuario).orderByChild("timestampinteracao").limitToFirst(1);
                }
                break;
        }
    }

    private void ajustarQueryMore() {
        switch (tipoGrupo) {
            case GroupUtils.MY_GROUPS:
                queryLoadMore = firebaseRef.child("grupos")
                        .orderByChild("idSuperAdmGrupo").equalTo(idUsuario);
                break;
            case GroupUtils.PUBLIC_GROUPS:
                queryLoadMore = firebaseRef.child("publicGroups")
                        .orderByChild("timestampinteracao")
                        .startAt(lastTimestamp)
                        .limitToFirst(PAGE_SIZE);
                break;
            case GroupUtils.GROUPS_FOLLOWING:
                queryLoadMore = firebaseRef.child("groupFollowing")
                        .child(idUsuario).orderByChild("timestampinteracao")
                        .startAt(lastTimestamp)
                        .limitToFirst(PAGE_SIZE);
                break;
        }
    }

    private void ajustarQueryLast() {
        switch (tipoGrupo) {
            case GroupUtils.MY_GROUPS:
                queryUltimoElemento = firebaseRef.child("grupos")
                        .orderByChild("idSuperAdmGrupo").equalTo(idUsuario).limitToLast(1);
                break;
            case GroupUtils.PUBLIC_GROUPS:
                queryUltimoElemento = firebaseRef.child("publicGroups")
                        .orderByChild("timestampinteracao")
                        .limitToLast(1);
                break;
            case GroupUtils.GROUPS_FOLLOWING:
                queryUltimoElemento = firebaseRef.child("groupFollowing")
                        .child(idUsuario).orderByChild("timestampinteracao")
                        .limitToLast(1);
                break;
        }
    }

    private void dadoInicialPorTopico() {
        queryInicialFiltro = firebaseRef.child("groupInterests")
                .child(topicosSelecionados.get(0))
                .orderByChild("timestampinteracao")
                .limitToFirst(1);
        queryInicialFiltro.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    linearLayoutTopico.setVisibility(View.VISIBLE);
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Grupo grupoPorTopico = snapshotChildren.getValue(Grupo.class);
                        if (grupoPorTopico != null && grupoPorTopico.getIdGrupo() != null
                                && !grupoPorTopico.getIdGrupo().isEmpty()) {
                            lastTimeTopico = grupoPorTopico.getTimestampinteracao();
                            adicionarGrupoPorTopico(grupoPorTopico);
                        }
                    }
                } else {
                    linearLayoutTopico.setVisibility(View.GONE);
                    ToastCustomizado.toastCustomizado(String.format("%s %s", "Não existem grupos no momento que tenham o seguinte interesse:", topicosSelecionados.get(0)), getApplicationContext());
                    ocultarProgress();
                }
                queryInicialFiltro.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocultarProgress();
                lastTimeTopico = -1;
            }
        });
    }

    private void adicionarGrupoPorTopico(Grupo grupoAlvo) {
        if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
            ocultarProgress();
            setLoading(false);
            return;
        }
        FirebaseRecuperarUsuario.recoverGroup(grupoAlvo.getIdGrupo(), new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo dadosGrupo) {
                grupoDAOFiltrado.adicionarGrupo(dadosGrupo);
                grupoDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosGrupo.getIdGrupo());
                adapterPreviewGroup.updateGrupoList(listaFiltrada, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        listaDadosGrupo.put(dadosGrupo.getIdGrupo(), dadosGrupo);
                        if (idsListeners != null && !idsListeners.isEmpty()
                                && !idsListeners.contains(dadosGrupo.getIdGrupo())
                                || idsListeners != null && idsListeners.isEmpty()) {
                            idsListeners.add(dadosGrupo.getIdGrupo());
                        }
                        ocultarProgress();
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

    private void carregarMaisDadosPorTopico() {
        queryLoadMoreFiltro = firebaseRef.child("groupInterests")
                .child(topicosSelecionados.get(0))
                .orderByChild("timestampinteracao")
                .startAt(lastTimeTopico)
                .limitToFirst(PAGE_SIZE);
        ajustarQueryMore();
        queryLoadMoreFiltro.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Grupo grupoMore = snapshotChildren.getValue(Grupo.class);
                        if (grupoMore == null) {
                            if (queryLoadMoreFiltro != null) {
                                queryLoadMoreFiltro.removeEventListener(this);
                            }
                            ocultarProgress();
                            return;
                        }
                        if (grupoMore.getIdGrupo() != null
                                && !grupoMore.getIdGrupo().isEmpty()) {
                            List<Grupo> newGrupo = new ArrayList<>();
                            long key = grupoMore.getTimestampinteracao();
                            if (lastTimeTopico != -1 && key != -1) {
                                if (key != lastTimeTopico || !listaFiltrada.isEmpty() &&
                                        !grupoMore.getIdGrupo().equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdGrupo())) {
                                    newGrupo.add(grupoMore);
                                    lastTimeTopico = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newGrupo.size() > PAGE_SIZE) {
                                newGrupo.remove(0);
                            }
                            if (lastTimeTopico != -1) {
                                adicionarMaisDadosPorTopico(newGrupo, grupoMore.getIdGrupo());
                            }
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
                lastTimeTopico = -1;
            }
        });
    }

    private void adicionarMaisDadosPorTopico(List<Grupo> newGrupo, String idGrupo) {
        if (newGrupo != null && !newGrupo.isEmpty()) {
            grupoDAOFiltrado.carregarMaisGrupo(newGrupo, idsFiltrados);
            grupoDAOFiltrado.adicionarIdAoSet(idsFiltrados, idGrupo);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
                @Override
                public void onGrupoRecuperado(Grupo dadosGrupo) {
                    adapterPreviewGroup.updateGrupoList(listaFiltrada, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            listaDadosGrupo.put(dadosGrupo.getIdGrupo(), dadosGrupo);
                            if (idsListeners != null && !idsListeners.isEmpty()
                                    && !idsListeners.contains(dadosGrupo.getIdGrupo())
                                    || idsListeners != null && idsListeners.isEmpty()) {
                                idsListeners.add(dadosGrupo.getIdGrupo());
                            }
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
        } else {
            ocultarProgress();
        }
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isPesquisaAtivada() {
        return pesquisaAtivada;
    }

    public void setPesquisaAtivada(boolean pesquisaAtivada) {
        this.pesquisaAtivada = pesquisaAtivada;
    }

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, GroupActivity.this);
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, GroupActivity.this);
    }

    private void inicializarComponentes() {
        toolbarIncBlack = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackBlack = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbarBlack = findViewById(R.id.txtViewIncTituloToolbarBlack);
        recyclerView = findViewById(R.id.recyclerViewLstGroup);
        searchView = findViewById(R.id.searchViewGrupos);
        spinProgress = findViewById(R.id.spinProgressBarRecycler);
        imgBtnExibirFiltros = findViewById(R.id.imgBtnExibirFiltrosGrupo);
        linearLayoutTopico = findViewById(R.id.linearLayoutTopicoSelecionado);
    }
}