package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterUsersSelectionCommunity;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UsersInviteCommunityActivity extends AppCompatActivity implements AdapterUsersSelectionCommunity.MarcarUsuarioCallback, AdapterUsersSelectionCommunity.DesmarcarUsuarioCallback {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private TextView txtViewLimiteSelecao;
    private Button btnSalvarGerenciamento;
    private SearchView searchView;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private int mCurrentPosition = -1;
    private boolean isLoading = false;
    private boolean isScrolling = false;
    private boolean primeiroCarregamento = true;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private Query queryInicial, queryLoadMore;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private UsuarioDiffDAO usuarioDAOFiltrado;
    private Query queryInicialFiltro, queryLoadMoreFiltro;
    private String nomePesquisado = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private boolean pesquisaAtivada = false;
    private long lastTimestamp = -1;
    private boolean atualizandoLista = false;
    private Handler searchHandler = new Handler();
    private int queryDelayMillis = 500;
    private int searchCounter = 0;
    private String currentSearchText = "";
    private HashMap<String, DatabaseReference> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private HashMap<String, DatabaseReference> referenceFiltroHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerFiltroHashMap = new HashMap<>();
    private Set<String> idsListeners = new HashSet<>();
    private ValueEventListener listenerFiltragem;
    private DatabaseReference verificaAmizadeRef;
    private AdapterUsersSelectionCommunity adapterSelection;
    private int contadorInicial = 1;
    private CommunityUtils communityUtils;
    private String idComunidade = "";
    private HashMap<String, Object> listaAmigos = new HashMap<>();
    private long limiteSelecao = 0;
    private int totalSelecionado = 0;
    private int convitesEnviados = 0;
    private ProgressDialog progressDialog;
    private boolean operacaoConcluida = false;
    private boolean trocarQueryInicial = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
        limparPeloDestroyView();
    }

    private interface RecuperaUser {
        void onRecuperado(Usuario usuarioAtual);

        void onSemDado();

        void onError(String message);
    }

    private interface VerificaCriterio {
        void onCriterioAtendido();

        void onSemVinculo();

        void onError(String message);
    }

    private interface RecuperarComunidadeCallback {
        void onConcluido(Comunidade comunidadeRecuperada);

        void onError(String message);
    }

    private interface ExecutarOperacaoCallback {
        void onConcluido();
    }

    public UsersInviteCommunityActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_invite_community);
        inicializarComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        configInicial();
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

    public long getLimiteSelecao() {
        return limiteSelecao;
    }

    public void setLimiteSelecao(long limiteSelecao) {
        this.limiteSelecao = limiteSelecao;
    }

    private void configInicial() {
        txtViewTitleToolbar.setText("Convidar amigos");
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        configBundle();
        recuperarComunidade(new RecuperarComunidadeCallback() {
            @Override
            public void onConcluido(Comunidade comunidadeRecuperada) {
                if (comunidadeRecuperada.getNrParticipantes() < 1) {
                    setLimiteSelecao(CommunityUtils.MAX_NUMBER_PARTICIPANTS);
                } else {
                    setLimiteSelecao(CommunityUtils.MAX_NUMBER_PARTICIPANTS - comunidadeRecuperada.getNrParticipantes());
                }
                txtViewLimiteSelecao.setText(String.format("%d%s%d", 0, "/", getLimiteSelecao()));
                communityUtils = new CommunityUtils(getApplicationContext());
                progressDialog = new ProgressDialog(UsersInviteCommunityActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                setPesquisaAtivada(false);
                configRecycler();
                configSearchView();
                usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterSelection);
                usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterSelection);
                setLoading(true);
                recuperarDadosIniciais();
                configPaginacao();
                UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                    @Override
                    public void onConcluido(boolean epilepsia) {
                        adapterSelection.setStatusEpilepsia(epilepsia);
                    }

                    @Override
                    public void onSemDado() {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), getApplicationContext());
                        onBackPressed();
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                        onBackPressed();
                    }
                });
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                onBackPressed();
            }
        });
        clickListeners();
    }

    private void configBundle() {
        Bundle dados = getIntent().getExtras();
        if (dados != null && dados.containsKey("idComunidade")) {
            idComunidade = dados.getString("idComunidade");
        } else {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade", getApplicationContext());
            onBackPressed();
        }
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
                                    setLoading(true);
                                    setPesquisaAtivada(true);
                                    if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                        lastName = null;
                                        idsFiltrados.clear();
                                        nomePesquisado = "";
                                        usuarioDAOFiltrado.limparListaUsuarios();
                                        removeValueEventListenerFiltro();
                                    }
                                    nomePesquisado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
                                    nomePesquisado = FormatarNomePesquisaUtils.removeAcentuacao(nomePesquisado).toUpperCase(Locale.ROOT);

                                    if (listenerFiltragem != null && queryInicialFiltro != null) {
                                        queryInicialFiltro.removeEventListener(listenerFiltragem);
                                    }
                                    dadoInicialFiltragem(nomePesquisado, counter);
                                }
                            } else {
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

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, UsersInviteCommunityActivity.this);
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, UsersInviteCommunityActivity.this);
    }

    private void limparFiltragem(boolean fecharTeclado) {
        if (searchView != null && fecharTeclado) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            searchView.clearFocus();
        }
        removeValueEventListenerFiltro();
        if (listenerFiltragem != null && queryInicialFiltro != null) {
            queryInicialFiltro.removeEventListener(listenerFiltragem);
        }
        lastName = null;
        if (idsFiltrados != null) {
            idsFiltrados.clear();
        }
        setPesquisaAtivada(false);
        nomePesquisado = "";
        ocultarProgress();
        if (usuarioDAOFiltrado != null) {
            usuarioDAOFiltrado.limparListaUsuarios();
        }
        if (listaUsuarios != null && listaUsuarios.size() > 0) {
            adapterSelection.updateUsersList(listaUsuarios, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                }
            });
        }
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (adapterSelection == null) {
            adapterSelection = new AdapterUsersSelectionCommunity(getApplicationContext(),
                    listaUsuarios, listaDadosUser, getResources().getColor(R.color.following_color), getLimiteSelecao(), this, this);
        }
        recyclerView.setAdapter(adapterSelection);
    }

    private void recuperarDadosIniciais() {

        if (listaUsuarios != null && listaUsuarios.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }

        if (trocarQueryInicial && lastTimestamp != -1) {
            queryInicial = firebaseRef.child("friends")
                    .child(idUsuario).orderByChild("timestampinteracao")
                    .startAt(lastTimestamp + 1)
                    .limitToFirst(1);
        }else{
            queryInicial = firebaseRef.child("friends")
                    .child(idUsuario).orderByChild("timestampinteracao").limitToFirst(1);
        }
        queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listaUsuarios != null && listaUsuarios.size() >= 1) {
                    trocarQueryInicial = false;
                    queryInicial.removeEventListener(this);
                    return;
                }
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Usuario usuarioChildren = snapshotChildren.getValue(Usuario.class);
                        if (usuarioChildren != null && !usuarioChildren.getIdUsuario().isEmpty()) {
                            communityUtils.verificaConviteComunidade(idComunidade, usuarioChildren.getIdUsuario(), new CommunityUtils.VerificaConviteCallback() {
                                @Override
                                public void onExiste() {
                                    lastTimestamp = usuarioChildren.getTimestampinteracao();
                                    trocarQueryInicial = true;
                                    recuperarDadosIniciais();
                                }

                                @Override
                                public void onNaoExiste() {
                                    communityUtils.verificaSeEParticipante(idComunidade, usuarioChildren.getIdUsuario(), new CommunityUtils.VerificaParticipanteCallback() {
                                        @Override
                                        public void onParticipante(boolean status) {
                                            if (status) {
                                                //Usuário já participa da comunidade.
                                                lastTimestamp = usuarioChildren.getTimestampinteracao();
                                                trocarQueryInicial = true;
                                                recuperarDadosIniciais();
                                            } else {
                                                trocarQueryInicial = false;
                                                adicionarUser(usuarioChildren);
                                                lastTimestamp = usuarioChildren.getTimestampinteracao();
                                            }
                                        }

                                        @Override
                                        public void onError(String message) {
                                            lastTimestamp = -1;
                                        }
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    lastTimestamp = -1;
                                }
                            });
                        }
                    }
                }
                queryInicial.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
            }
        });
    }

    private void adicionarUser(Usuario usuarioAlvo) {
        if (listaUsuarios != null && listaUsuarios.size() >= 1) {
            setLoading(false);
            return;
        }
        recuperaDadosUser(usuarioAlvo.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                usuarioDiffDAO.adicionarUsuario(dadosUser);
                idsUsuarios.add(dadosUser.getIdUsuario());
                adapterSelection.updateUsersList(listaUsuarios, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        adicionarDadoDoUsuario(dadosUser);
                        setLoading(false);
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

    private void recuperaDadosUser(String idUser, RecuperaUser callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUser, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                UsuarioUtils.checkBlockingStatus(getApplicationContext(), usuarioAtual.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                    @Override
                    public void onBlocked(boolean status) {
                        usuarioAtual.setIndisponivel(status);
                        callback.onRecuperado(usuarioAtual);
                    }

                    @Override
                    public void onError(String message) {
                        usuarioAtual.setIndisponivel(true);
                        callback.onRecuperado(usuarioAtual);
                    }
                });
            }

            @Override
            public void onSemDados() {
                callback.onSemDado();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private void adicionarDadoDoUsuario(Usuario dadosUser) {
        listaDadosUser.put(dadosUser.getIdUsuario(), dadosUser);
        if (!isPesquisaAtivada() && idsListeners != null && idsListeners.size() > 0
                && idsListeners.contains(dadosUser.getIdUsuario())) {
            return;
        }
        verificaAmizade(dadosUser);
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff");
        listenerFiltragem = queryInicialFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (counter != searchCounter) {
                    limparFiltragem(false);
                    return;
                }

                if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                    return;
                }

                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Usuario usuarioPesquisa = snapshotChildren.getValue(Usuario.class);
                        if (usuarioPesquisa != null && usuarioPesquisa.getIdUsuario() != null
                                && !usuarioPesquisa.getIdUsuario().isEmpty()
                                && !usuarioPesquisa.getIdUsuario().equals(idUsuario)) {
                            verificaVinculo(usuarioPesquisa.getIdUsuario(), new VerificaCriterio() {
                                @Override
                                public void onCriterioAtendido() {
                                    adicionarUserFiltrado(usuarioPesquisa);
                                }

                                @Override
                                public void onSemVinculo() {

                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                        }
                    }
                }
                queryInicialFiltro.removeEventListener(listenerFiltragem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocultarProgress();
                lastName = null;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ocultarProgress();
            }
        }, 500);
    }

    private void verificaVinculo(String idAlvo, VerificaCriterio callback) {
        DatabaseReference verificaVinculoRef = firebaseRef.child("friends")
                .child(idUsuario).child(idAlvo).child("idUsuario");
        verificaVinculoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String idAmigo = snapshot.getValue(String.class);
                    if (idAmigo != null && !idAmigo.isEmpty()) {
                        communityUtils.verificaConviteComunidade(idComunidade, idAlvo, new CommunityUtils.VerificaConviteCallback() {
                            @Override
                            public void onExiste() {
                                callback.onSemVinculo();
                            }

                            @Override
                            public void onNaoExiste() {
                                communityUtils.verificaSeEParticipante(idComunidade, idAmigo, new CommunityUtils.VerificaParticipanteCallback() {
                                    @Override
                                    public void onParticipante(boolean status) {
                                        if (status) {
                                            callback.onSemVinculo();
                                        } else {
                                            callback.onCriterioAtendido();
                                        }
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                callback.onError(message);
                            }
                        });
                    } else {
                        callback.onSemVinculo();
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

    private void adicionarUserFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            return;
        }
        lastName = dadosUser.getNomeUsuarioPesquisa();
        usuarioDAOFiltrado.adicionarUsuario(dadosUser);
        idsFiltrados.add(dadosUser.getIdUsuario());
        adapterSelection.updateUsersList(listaFiltrada, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                adicionarDadoDoUsuario(dadosUser);
                carregarMaisDados(nomePesquisado);
                setLoading(false);
            }
        });
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
                                if (lastVisibleItemPosition == totalItemCount - 1) {
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
        if (isPesquisaAtivada()) {
            if (listaFiltrada != null && listaFiltrada.size() > 0
                    && lastName != null && !lastName.isEmpty()) {
                queryLoadMoreFiltro = firebaseRef.child("usuarios")
                        .orderByChild("nomeUsuarioPesquisa")
                        .startAt(lastName).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
                queryLoadMoreFiltro.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                                Usuario usuarioPesquisa = snapshotChildren.getValue(Usuario.class);
                                if (usuarioPesquisa != null && usuarioPesquisa.getIdUsuario() != null
                                        && !usuarioPesquisa.getIdUsuario().isEmpty()
                                        && !usuarioPesquisa.getIdUsuario().equals(idUsuario)) {
                                    verificaVinculo(usuarioPesquisa.getIdUsuario(), new VerificaCriterio() {
                                        @Override
                                        public void onCriterioAtendido() {
                                            List<Usuario> newUsuario = new ArrayList<>();
                                            String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                                            if (lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)) {
                                                newUsuario.add(usuarioPesquisa);
                                                lastName = key;
                                            }
                                            // Remove a última chave usada
                                            if (newUsuario.size() > PAGE_SIZE) {
                                                newUsuario.remove(0);
                                            }
                                            if (lastName != null && !lastName.isEmpty()) {
                                                adicionarMaisDadosFiltrados(newUsuario, usuarioPesquisa);
                                            }
                                        }

                                        @Override
                                        public void onSemVinculo() {
                                            String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                                            if (lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)) {
                                                lastName = key;
                                            }
                                        }

                                        @Override
                                        public void onError(String message) {

                                        }
                                    });
                                }
                            }
                        }
                        queryLoadMoreFiltro.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        lastName = null;
                    }
                });
            }
        } else {
            queryLoadMore = firebaseRef.child("friends")
                    .child(idUsuario)
                    .orderByChild("timestampinteracao")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);
            queryLoadMore.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                            Usuario usuarioChildren = snapshotChildren.getValue(Usuario.class);
                            //**ToastCustomizado.toastCustomizadoCurto("SEM FILTRO " + usuarioChildren.getIdUsuario(), requireContext());
                            if (usuarioChildren != null && usuarioChildren.getIdUsuario() != null
                                    && !usuarioChildren.getIdUsuario().isEmpty()) {
                                communityUtils.verificaConviteComunidade(idComunidade, usuarioChildren.getIdUsuario(), new CommunityUtils.VerificaConviteCallback() {
                                    @Override
                                    public void onExiste() {
                                        long key = usuarioChildren.getTimestampinteracao();
                                        if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                            lastTimestamp = key;
                                        }
                                    }

                                    @Override
                                    public void onNaoExiste() {
                                        communityUtils.verificaSeEParticipante(idComunidade, usuarioChildren.getIdUsuario(), new CommunityUtils.VerificaParticipanteCallback() {
                                            @Override
                                            public void onParticipante(boolean status) {
                                                if (status) {
                                                    //Já participa.
                                                    long key = usuarioChildren.getTimestampinteracao();
                                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                                        lastTimestamp = key;
                                                    }
                                                } else {
                                                    List<Usuario> newUsuario = new ArrayList<>();
                                                    long key = usuarioChildren.getTimestampinteracao();
                                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                                        newUsuario.add(usuarioChildren);
                                                        lastTimestamp = key;
                                                    }
                                                    // Remove a última chave usada
                                                    if (newUsuario.size() > PAGE_SIZE) {
                                                        newUsuario.remove(0);
                                                    }
                                                    if (lastTimestamp != -1) {
                                                        adicionarMaisDados(newUsuario, usuarioChildren.getIdUsuario());
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String message) {
                                    }
                                });
                            }
                        }
                    }
                    queryLoadMore.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    lastTimestamp = -1;
                }
            });
        }
    }

    private void adicionarMaisDados(List<Usuario> newUsuario, String idUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    usuarioDiffDAO.carregarMaisUsuario(newUsuario, idsUsuarios);
                    //*Usuario usuarioComparator = new Usuario(true, false);
                    //*Collections.sort(listaViewers, usuarioComparator);
                    adapterSelection.updateUsersList(listaUsuarios, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            adicionarDadoDoUsuario(dadosUser);
                            setLoading(false);
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

    private void adicionarMaisDadosFiltrados(List<Usuario> newUsuario, Usuario dadosUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDAOFiltrado.carregarMaisUsuario(newUsuario, idsFiltrados);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterSelection.updateUsersList(listaFiltrada, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    adicionarDadoDoUsuario(dadosUser);
                    setLoading(false);
                }
            });
        }
    }

    private void removeValueEventListener() {
        if (listenerHashMap != null && referenceHashMap != null) {
            for (String userId : listenerHashMap.keySet()) {
                DatabaseReference userRef = referenceHashMap.get(userId);
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
            for (String userId : listenerFiltroHashMap.keySet()) {
                DatabaseReference userRef = referenceFiltroHashMap.get(userId);
                ChildEventListener listener = listenerFiltroHashMap.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                }
            }
            referenceFiltroHashMap.clear();
            listenerFiltroHashMap.clear();
        }
    }

    private void verificaAmizade(Usuario usuarioAlvo) {
        if (!isPesquisaAtivada()) {
            idsListeners.add(usuarioAlvo.getIdUsuario());
        }
        verificaAmizadeRef = firebaseRef.child("friends")
                .child(idUsuario).child(usuarioAlvo.getIdUsuario());

        ChildEventListener newChildListener = verificaAmizadeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    listaAmigos.put(usuarioAlvo.getIdUsuario(), usuarioAlvo);
                    int posicao = adapterSelection.findPositionInList(usuarioAlvo.getIdUsuario());
                    if (posicao != -1) {
                        adapterSelection.notifyItemChanged(adapterSelection.findPositionInList(usuarioAlvo.getIdUsuario()));
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (listaAmigos != null && listaAmigos.size() > 0
                        && listaAmigos.containsKey(usuarioAlvo.getIdUsuario())) {
                    listaAmigos.remove(usuarioAlvo.getIdUsuario());
                    int posicao = adapterSelection.findPositionInList(usuarioAlvo.getIdUsuario());
                    if (posicao != -1) {
                        adapterSelection.notifyItemChanged(adapterSelection.findPositionInList(usuarioAlvo.getIdUsuario()));
                    }
                }
                if (usuarioDiffDAO != null && listaUsuarios != null
                        && listaUsuarios.size() > 0) {
                    usuarioDiffDAO.removerUsuario(usuarioAlvo);
                }
                if (usuarioDAOFiltrado != null && listaFiltrada != null
                        && listaFiltrada.size() > 0) {
                    usuarioDAOFiltrado.removerUsuario(usuarioAlvo);
                }
                if (listaDadosUser != null && listaDadosUser.size() > 0) {
                    listaDadosUser.remove(usuarioAlvo.getIdUsuario());
                }
                //Remove o id do hashmapAmigos
                if (listaAmigos != null && listaAmigos.size() > 0
                        && listaAmigos.containsKey(usuarioAlvo.getIdUsuario())) {
                    listaAmigos.remove(usuarioAlvo.getIdUsuario());
                    adapterSelection.notifyItemChanged(adapterSelection.findPositionInList(usuarioAlvo.getIdUsuario()));
                }
                adapterSelection.updateUsersList(listaUsuarios, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                    }
                });
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (isPesquisaAtivada()) {
            referenceFiltroHashMap.put(usuarioAlvo.getIdUsuario(), verificaAmizadeRef);
            listenerFiltroHashMap.put(usuarioAlvo.getIdUsuario(), newChildListener);
        } else {
            referenceHashMap.put(usuarioAlvo.getIdUsuario(), verificaAmizadeRef);
            listenerHashMap.put(usuarioAlvo.getIdUsuario(), newChildListener);
        }
    }

    private void limparPeloDestroyView() {
        removeValueEventListener();
        removeValueEventListenerFiltro();
        if (usuarioDiffDAO != null) {
            usuarioDiffDAO.limparListaUsuarios();
        }
        if (listaDadosUser != null) {
            listaDadosUser.clear();
        }
        if (listaAmigos != null) {
            listaAmigos.clear();
        }
        if (idsUsuarios != null) {
            idsUsuarios.clear();
        }
        if (listaFiltrada != null && listaFiltrada.size() > 0) {
            usuarioDAOFiltrado.limparListaUsuarios();
            idsFiltrados.clear();
        }
        setPesquisaAtivada(false);
        nomePesquisado = null;
        mCurrentPosition = -1;
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }

    private void recuperarComunidade(RecuperarComunidadeCallback callback) {
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                callback.onConcluido(comunidadeAtual);
            }

            @Override
            public void onNaoExiste() {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade", getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade", getApplicationContext());
                onBackPressed();
            }
        });
    }

    @Override
    public void onMarcado() {
        totalSelecionado++;
        txtViewLimiteSelecao.setText(String.format("%d%s%d", totalSelecionado, "/", getLimiteSelecao()));
    }

    @Override
    public void onDesmarcado() {
        if (totalSelecionado <= 0) {
            totalSelecionado = 0;
        }else{
            totalSelecionado--;
        }
        txtViewLimiteSelecao.setText(String.format("%d%s%d", totalSelecionado, "/", getLimiteSelecao()));
    }

    private void clickListeners(){
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocultarProgressDialog();
                onBackPressed();
            }
        });
        btnSalvarGerenciamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapterSelection == null) {
                    return;
                }
                if (adapterSelection.getListaSelecao() == null
                        || adapterSelection.getListaSelecao().size() <= 0) {
                    return;
                }
                exibirProgressDialog("convite");
                convitesEnviados = 0;
                realizarOperacoes(adapterSelection.getListaSelecao(), 0);
            }
        });
    }

    private void realizarOperacoes(List<String> listaIds, int index) {
        convitesEnviados++;
        if (index < listaIds.size()) {
            String idSelecao = listaIds.get(index);
            communityUtils.verificaSeEParticipante(idComunidade, idSelecao, new CommunityUtils.VerificaParticipanteCallback() {
                @Override
                public void onParticipante(boolean status) {
                    if (status) {
                        //Usuário selecionado já participa.
                        verificaOperacao(new ExecutarOperacaoCallback() {
                            @Override
                            public void onConcluido() {
                                realizarOperacoes(listaIds, index + 1);
                            }
                        });
                        return;
                    }

                    //Usuário selecionado não é participante.
                    communityUtils.verificaConviteComunidade(idComunidade, idSelecao, new CommunityUtils.VerificaConviteCallback() {
                        @Override
                        public void onExiste() {
                            verificaOperacao(new ExecutarOperacaoCallback() {
                                @Override
                                public void onConcluido() {
                                    realizarOperacoes(listaIds, index + 1);
                                }
                            });
                        }

                        @Override
                        public void onNaoExiste() {
                            communityUtils.enviarConvite(idComunidade, idSelecao, new CommunityUtils.EnviarConviteCallback() {
                                @Override
                                public void onEnviado() {
                                    verificaOperacao(new ExecutarOperacaoCallback() {
                                        @Override
                                        public void onConcluido() {
                                            // Chama recursivamente para a próxima iteração
                                            realizarOperacoes(listaIds, index + 1);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao enviar o convite de comunidade para um usuário.", getApplicationContext());
                                    verificaOperacao(null);
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao enviar o convite de comunidade para um usuário.", getApplicationContext());
                            verificaOperacao(null);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    verificaOperacao(null);
                }
            });
        }else{
            // Todas as operações foram concluídas
            verificaOperacao(null);
        }
    }

    private void verificaOperacao(ExecutarOperacaoCallback callback){
        if (operacaoConcluida) {
            return;
        }
        if(convitesEnviados != -1 && adapterSelection != null)
        if (convitesEnviados == adapterSelection.getListaSelecao().size()) {
            operacaoConcluida = true;
            ocultarProgressDialog();
            ToastCustomizado.toastCustomizadoCurto("Concluído com sucesso.", getApplicationContext());
            onBackPressed();
        }else if(callback != null){
            callback.onConcluido();
        }
    }

    public void exibirProgressDialog(String tipoMensagem) {
        switch (tipoMensagem) {
            case "convite":
                progressDialog.setMessage("Enviando convites, aguarde....");
                break;
        }
        if (!UsersInviteCommunityActivity.this.isFinishing()) {
            progressDialog.show();
        }
    }


    public void ocultarProgressDialog() {
        if (progressDialog != null && !UsersInviteCommunityActivity.this.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void inicializarComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        spinProgress = findViewById(R.id.spinProgressBarRecycler);
        recyclerView = findViewById(R.id.recyclerViewAvailableUsers);
        txtViewLimiteSelecao = findViewById(R.id.txtViewLimiteGerenciamento);
        btnSalvarGerenciamento = findViewById(R.id.btnSalvarGerenciamento);
        searchView = findViewById(R.id.searchViewUsers);
    }
}