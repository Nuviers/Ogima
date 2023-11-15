package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFindPeoples;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FindPeopleActivity extends AppCompatActivity implements AdapterFindPeoples.AnimacaoIntent, AdapterFindPeoples.RecuperaPosicaoAnterior {

    private String idUsuario = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private boolean epilepsia = true;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private AdapterFindPeoples adapterFindPeoples;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
    private boolean primeiroCarregamento = true;
    private RecyclerView.OnScrollListener scrollListener;
    private boolean existemDados = false;
    //Dados do usuário
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    //Filtragem
    private Set<String> idsFiltrados = new HashSet<>();
    private UsuarioDiffDAO usuarioDAOFiltrado;
    private Query queryInicialFiltro, queryLoadMoreFiltro;
    private String nomePesquisado = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private FirebaseUtils firebaseUtils = new FirebaseUtils();
    private HashMap<String, Query> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();

    private Handler searchHandler = new Handler();
    private int queryDelayMillis = 500;
    private int searchCounter = 0;
    private String currentSearchText = "";
    private boolean atualizandoLista = false;
    private SpinKitView spinProgressBarFind;


    @Override
    public void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            configRecycler();
            configSearchView();
            usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterFindPeoples);
            setLoading(true);
            configPaginacao();
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterFindPeoples != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza o recyclerView para a posição salva.
        if (mCurrentPosition != -1 &&
                listaFiltrada != null && listaFiltrada.size() > 0
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
        removeChildEventListener();
        listaDadosUser.clear();
        idsUsuarios.clear();
        if (listaFiltrada != null && listaFiltrada.size() > 0) {
            usuarioDAOFiltrado.limparListaUsuarios();
            idsFiltrados.clear();
        }
        nomePesquisado = null;
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

    public FindPeopleActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);
        inicializandoComponentes();
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean statusEpilepsia) {
                epilepsia = statusEpilepsia;
                configInicial();
                clickListeners();
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), getApplicationContext());
                finish();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                finish();
            }
        });
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (adapterFindPeoples == null) {
            adapterFindPeoples = new AdapterFindPeoples(getApplicationContext(),
                    listaFiltrada, this, this, listaDadosUser);
        }
        recyclerView.setAdapter(adapterFindPeoples);
    }

    private void configInicial() {
        adapterFindPeoples.setStatusEpilepsia(epilepsia);
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewTitleToolbar.setText(getString(R.string.hintSearchViewPeople));
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void configSearchView() {
        searchView.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
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
                                    if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                        limparFiltragem();
                                    }
                                    nomePesquisado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
                                    nomePesquisado = FormatarNomePesquisaUtils.removeAcentuacao(nomePesquisado).toUpperCase(Locale.ROOT);
                                    dadoInicialFiltragem(nomePesquisado, counter);
                                }
                            } else {
                                atualizandoLista = true;
                                limparFiltragem();
                            }
                        }
                    }, queryDelayMillis);
                }
                return true;
            }
        });
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(2);
        ChildEventListener childEventListener = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (counter != searchCounter) {
                    limparFiltragem();
                    return;
                }
                if (snapshot.getValue() != null) {
                    Usuario usuarioFiltrado = snapshot.getValue(Usuario.class);
                    Log.d("FINDUTILS", "NOME " + usuarioFiltrado.getNomeUsuario());
                    if (usuarioFiltrado != null && usuarioFiltrado.getNomeUsuarioPesquisa() != null
                            && !usuarioFiltrado.getNomeUsuarioPesquisa().isEmpty()) {
                        lastName = usuarioFiltrado.getNomeUsuarioPesquisa();
                        if (usuarioFiltrado.getIdUsuario() != null
                                && !usuarioFiltrado.getIdUsuario().isEmpty()
                                && !usuarioFiltrado.getIdUsuario().equals(idUsuario)) {
                            referenceHashMap.put(usuarioFiltrado.getIdUsuario(), queryInicialFiltro);
                            listenerHashMap.put(usuarioFiltrado.getIdUsuario(), this);
                            adicionarUserFiltrado(usuarioFiltrado);
                        }
                    } else {
                        removerChildUnico(this, queryInicialFiltro);
                    }
                } else {
                    removerChildUnico(this, queryInicialFiltro);
                }
            }

            @Override
            public void onChildChanged(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ToastCustomizado.toastCustomizadoCurto("CHANGED", getApplicationContext());
            }

            @Override
            public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                ocultarProgress();
                lastName = null;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setLoading(false);
                ocultarProgress();
            }
        }, 500);
    }

    private void adicionarUserFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && listaFiltrada.size() >= 2) {
            setLoading(false);
            return;
        }
        usuarioDAOFiltrado.adicionarUsuario(dadosUser);
        idsFiltrados.add(dadosUser.getIdUsuario());
        adapterFindPeoples.updateUsersList(listaFiltrada);
        adicionarDadoDoUsuario(dadosUser);
        setLoading(false);
    }

    private void limparFiltragem() {
        removeChildEventListener();
        lastName = null;
        idsFiltrados.clear();
        nomePesquisado = "";
        usuarioDAOFiltrado.limparListaUsuarios();
        if (listaFiltrada != null) {
            adapterFindPeoples.updateUsersList(listaFiltrada);
        }
        setLoading(false);
        atualizandoLista = false;
    }

    private void configPaginacao() {
        if (recyclerView != null) {
            isScrolling = true;
            scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@androidx.annotation.NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true;
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
                                if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {
                                    isScrolling = false;
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
        if (listaFiltrada != null && listaFiltrada.size() > 0
                && lastName != null && !lastName.isEmpty()) {
            queryLoadMoreFiltro = firebaseRef.child("usuarios")
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAt(lastName).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
            ChildEventListener childEventListener = queryLoadMoreFiltro.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioMore = snapshot.getValue(Usuario.class);
                        List<Usuario> newUsuario = new ArrayList<>();
                        String key = snapshot.child("nomeUsuarioPesquisa").getValue(String.class);
                        if (lastName != null && key != null && !key.equals(lastName)) {
                            if (usuarioMore != null &&
                                    !usuarioMore.getIdUsuario().equals(idUsuario)) {
                                newUsuario.add(usuarioMore);
                            }
                            lastName = key;
                        }
                        // Remove a última chave usada
                        if (newUsuario.size() > PAGE_SIZE) {
                            newUsuario.remove(0);
                        }
                        if (lastName != null && !lastName.isEmpty() && usuarioMore != null) {
                            referenceHashMap.put(usuarioMore.getIdUsuario(), queryLoadMoreFiltro);
                            listenerHashMap.put(usuarioMore.getIdUsuario(), this);
                            adicionarMaisDadosFiltrados(newUsuario, usuarioMore);
                        }
                    }
                }

                @Override
                public void onChildChanged(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                    lastName = null;
                }
            });
        }
    }

    private void adicionarMaisDadosFiltrados(List<Usuario> newUsuario, Usuario dadosUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDAOFiltrado.carregarMaisUsuario(newUsuario, idsFiltrados);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterFindPeoples.updateUsersList(listaFiltrada);
            adicionarDadoDoUsuario(dadosUser);
            setLoading(false);
        }
    }

    private void adicionarDadoDoUsuario(Usuario dadosUser) {
        listaDadosUser.put(dadosUser.getIdUsuario(), dadosUser);
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void inicializandoComponentes() {
        searchView = findViewById(R.id.searchViewFindPeoples);
        recyclerView = findViewById(R.id.recyclerViewFindPeoples);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
        spinProgressBarFind = findViewById(R.id.spinProgressBarFind);
    }

    private void removeChildEventListener() {
        if (listenerHashMap != null
                && listenerHashMap.size() > 0
                && referenceHashMap != null && referenceHashMap.size() > 0) {
            for (String userId : listenerHashMap.keySet()) {
                Query userRef = referenceHashMap.get(userId);
                ChildEventListener listener = listenerHashMap.get(userId);
                if (userRef != null && listener != null) {
                    Log.d("REMOVIDOLISTENER", "REMOVIDOLISTENER");
                    userRef.removeEventListener(listener);
                }
            }
            referenceHashMap.clear();
            listenerHashMap.clear();
        }
    }

    private void removerChildUnico(ChildEventListener childEventListener, Query queryAlvo){
        if (queryAlvo != null && childEventListener != null) {
            queryAlvo.removeEventListener(childEventListener);
        }
    }

    @Override
    public void onExecutarAnimacao() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            searchView.clearFocus();
            mCurrentPosition = posicaoAnterior;
        }
    }

    private void exibirProgress(){
        spinProgressBarFind.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgressBarFind, FindPeopleActivity.this);
    }

    private void ocultarProgress(){
        spinProgressBarFind.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgressBarFind, FindPeopleActivity.this);
    }
}