package com.example.ogima.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterUsersSelectionCommunity;
import com.example.ogima.adapter.AdapterUsersSelectionGroup;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ContactDiffDAO;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.TimestampUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GroupManagementActivity extends AppCompatActivity implements AdapterUsersSelectionCommunity.MarcarUsuarioCallback, AdapterUsersSelectionCommunity.DesmarcarUsuarioCallback {

    private String idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarManage;
    private ImageButton imgBtnBackManage;
    private RecyclerView recyclerViewManage;
    private TextView txtViewLimiteManage, txtTituloManageGroup;
    private Button btnSalvarManage;
    private long limiteSelecao;
    private String tipoGerenciamento = "";
    private SearchView searchView;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private int mCurrentPosition = -1;
    private boolean isLoading = false;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO, usuarioDAOFiltrado;
    private Query queryInicial, queryLoadMore,
            queryLoadMorePesquisa, queryInicialFind;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private String nomePesquisado = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private boolean pesquisaAtivada = false;
    //private String lastId = null;
    private long lastTimestamp = -1;
    private boolean atualizandoLista = false;
    private Handler searchHandler = new Handler();
    private int queryDelayMillis = 500;
    private int searchCounter = 0;
    private String currentSearchText = "";
    private TextView txtViewTitle;
    private HashMap<String, DatabaseReference> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private HashMap<String, Query> referenceFiltroHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerFiltroHashMap = new HashMap<>();
    private Set<String> idsListeners = new HashSet<>();
    private AdapterUsersSelectionCommunity adapterSelection;
    private boolean trocarQueryInicial = false, trocarQueryInicialFiltro = false;
    private Usuario usuarioComparator;
    private FirebaseUtils firebaseUtils;
    private GroupUtils groupUtils;
    private int contadorRemocaoListenerNEWDATA = 0;
    private static final String TAG = "GroupManagementTAG";
    private Set<String> idsAIgnorarListeners = new HashSet<>();
    private String idUltimoElemento, idUltimoElementoFiltro;
    private Query queryUltimoElemento, queryUltimoElementoFiltro;
    private int controleRemocao = 0;
    private Set<Grupo> idsTempFiltro = new HashSet<>();
    private int aosFiltros = 0;
    private int posicaoChanged = -1;
    private HashMap<String, Bundle> idsParaAtualizar = new HashMap<>();
    private int contadorUpdate = 0;
    private int contadorNome = 0;
    private int totalSelecionado = 0;
    private String idGrupo = "";
    private String idSuperAdmGrupo = "";
    private boolean edicao = false;
    private ProgressDialog progressDialog;
    private int contadorParticipantes = 0;
    private ArrayList<String> idsARemover;
    private int contadorRemocao = 0;
    private ArrayList<String> idsNovosParticipantes;
    private MidiaUtils midiaUtils;
    private DatabaseReference limparLockRef, verificaLockRef;
    private ValueEventListener valueEventListenerLock;
    private OnDisconnect onDisconnect;
    private boolean fundador = false;
    private int operacoesExecutadas = 0;
    private boolean operacoesIgnoradas = false;
    private boolean operacaoComErro = false;
    private boolean operacaoConcluida = false;
    private boolean trocarQueryUltimo = false, trocarQueryUltimoFiltro = false;
    private long timeUltimo = -1;
    private boolean chatComunidade = false;

    private interface RecuperaUltimoElemento {
        void onRecuperado();
    }

    private interface RemoverListenersCallback {
        void onRemovido();
    }

    private interface RecuperarIdsFiltroCallback {
        void onRecuperado(Set<Grupo> listaIdsRecuperados);
    }

    private interface RecuperaUser {
        void onRecuperado(Usuario usuarioAtual);

        void onSemDado();

        void onError(String message);
    }

    private interface RecuperaParticipante {
        void onRecuperado(Grupo dadoParticipante);

        void onSemDado();

        void onError(String message);
    }

    private interface RecuperarGrupoCallback {
        void onConcluido(Grupo grupoRecuperado);

        void onError(String message);
    }

    private interface ExecutarOperacaoCallback {
        void onConcluido();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

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
                    recyclerViewManage.scrollToPosition(mCurrentPosition);
                }
            }, 100);
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limparActivity();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPosition = savedInstanceState.getInt("current_position");
    }

    public GroupManagementActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        usuarioComparator = new Usuario(false, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_group_users);
        inicializarComponentes();
        configInicial();
    }

    private void configInicial() {
        setSupportActionBar(toolbarManage);
        setTitle("");
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        configBundle();
        acompanharGerenciamento();
        recuperarGrupo(new RecuperarGrupoCallback() {
            @Override
            public void onConcluido(Grupo grupoRecuperado) {
                //Verifica se o usuário atual é o fundador.
                if (grupoRecuperado.getIdSuperAdmGrupo().equals(idUsuario)) {
                    fundador = true;
                } else {
                    fundador = false;
                }
                configurarLimite(grupoRecuperado);
                firebaseUtils = new FirebaseUtils();
                progressDialog = new ProgressDialog(GroupManagementActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                groupUtils = new GroupUtils(GroupManagementActivity.this, getApplicationContext());
                midiaUtils = new MidiaUtils(GroupManagementActivity.this, getApplicationContext(), progressDialog);
                idsNovosParticipantes = new ArrayList<>();
                txtTituloManageGroup.setText(FormatarContadorUtils.abreviarTexto("Gerenciar participantes", 20));
                setLoading(true);
                UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                    @Override
                    public void onConcluido(boolean epilepsia) {
                        setPesquisaAtivada(false);
                        configRecycler(epilepsia);
                        configSearchView();
                        usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterSelection);
                        usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterSelection);
                        setLoading(true);
                        recuperarDadosIniciais();
                        configPaginacao();

                        btnSalvarManage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                operacoesExecutadas = 0;
                                if (adapterSelection == null
                                        || tipoGerenciamento == null || tipoGerenciamento.isEmpty()) {
                                    return;
                                }
                                if (adapterSelection.getListaSelecao() == null
                                        || adapterSelection.getListaSelecao().size() <= 0) {
                                    return;
                                }
                                //Executa as operações da melhor maneira possível, ele respeita
                                //a conclusão de cada chamada.
                                exibirProgressDialog(tipoGerenciamento);
                                operacoesExecutadas = 0;
                                realizarOperacoes(adapterSelection.getListaSelecao(), 0);
                            }
                        });
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
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                onBackPressed();
            }
        });
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
                                        usuarioDAOFiltrado.limparListaUsuarios();
                                        adapterSelection.updateUsersList(listaFiltrada, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
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

                                            idUltimoElementoFiltro = null;

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
                    limparFiltragem(true);
                }
                return true;
            }
        });
    }

    private void configBundle() {
        Bundle dados = getIntent().getExtras();
        if (dados == null) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do grupo", getApplicationContext());
            onBackPressed();
            return;
        }
        if (dados.containsKey("idGrupo")) {
            idGrupo = dados.getString("idGrupo");
        }
        if (dados.containsKey("idSuperAdmGrupo")) {
            idSuperAdmGrupo = dados.getString("idSuperAdmGrupo");
        }
        if (dados.containsKey("tipoGerenciamento")) {
            tipoGerenciamento = dados.getString("tipoGerenciamento");
        }
        if (dados.containsKey("chatComunidade")) {
            chatComunidade = dados.getBoolean("chatComunidade");
        }
    }

    private void acompanharGerenciamento() {
        verificaLockRef = firebaseRef.child("lockGroupManagement")
                .child(idGrupo);
        valueEventListenerLock = verificaLockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Intent intent = new Intent(GroupManagementActivity.this, GroupDetailsActivity.class);
                    intent.putExtra("idGrupo", idGrupo);
                    intent.putExtra("chatComunidade", chatComunidade);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        limparLockRef = firebaseRef.child("lockGroupManagement")
                .child(idGrupo);
        onDisconnect = limparLockRef.onDisconnect();
        onDisconnect.removeValue();
    }

    private void recuperarGrupo(RecuperarGrupoCallback callback) {
        FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                callback.onConcluido(grupoAtual);
            }

            @Override
            public void onNaoExiste() {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do grupo", getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do grupo", getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void configurarLimite(Grupo grupoAlvo) {
        switch (tipoGerenciamento) {
            case CommunityUtils.FUNCTION_REMOVE:
                if (grupoAlvo.getIdSuperAdmGrupo().equals(idUsuario)) {
                    setLimiteSelecao(grupoAlvo.getNrParticipantes());
                } else {
                    setLimiteSelecao(grupoAlvo.getNrParticipantes() - 1);
                }
                break;
            case CommunityUtils.FUNCTION_PROMOTE:
                if (grupoAlvo.getNrAdms() > 0) {
                    setLimiteSelecao(CommunityUtils.MAX_NUMBER_ADMS - grupoAlvo.getNrAdms());
                } else {
                    setLimiteSelecao(CommunityUtils.MAX_NUMBER_ADMS);
                }
                break;
            case CommunityUtils.FUNCTION_DEMOTING:
                if (grupoAlvo.getNrAdms() > 0) {
                    setLimiteSelecao(grupoAlvo.getNrAdms());
                } else {
                    setLimiteSelecao(0);
                }
                break;
            case CommunityUtils.FUNCTION_NEW_FOUNDER:
                setLimiteSelecao(1);
                break;
        }
    }

    private void setarLimite() {
        adapterSelection.setLimiteSelecao(getLimiteSelecao());
        txtViewLimiteManage.setText(String.format("%d%s%d", 0, "/", getLimiteSelecao()));
    }

    private void configRecycler(boolean epilepsia) {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewManage.setHasFixedSize(true);
            recyclerViewManage.setLayoutManager(linearLayoutManager);

            adapterSelection = new AdapterUsersSelectionCommunity(getApplicationContext(),
                    listaUsuarios, listaDadosUser,
                    getResources().getColor(R.color.chat_list_color), getLimiteSelecao(), this, this);
            recyclerViewManage.setAdapter(adapterSelection);

            if (edicao) {
                FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
                    @Override
                    public void onGrupoRecuperado(Grupo grupoAtual) {
                        //Recuperar o número de participantes - Max_participantes e setar no limite.
                        if (grupoAtual.getNrParticipantes() > 160) {
                            setLimiteSelecao(GroupUtils.MAX_NUMBER_PARTICIPANTS - grupoAtual.getNrParticipantes());
                        } else {
                            setLimiteSelecao(GroupUtils.MAX_SELECTION);
                        }
                        adapterSelection.setLimiteSelecao(getLimiteSelecao());
                        txtViewLimiteManage.setText(String.format("%d%s%d", 0, "/", getLimiteSelecao()));
                    }

                    @Override
                    public void onNaoExiste() {
                        ToastCustomizado.toastCustomizadoCurto("Esse grupo não existe mais.", getApplicationContext());
                        onBackPressed();
                    }

                    @Override
                    public void onError(String mensagem) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do grupo.", getApplicationContext());
                        onBackPressed();
                    }
                });
            } else {
                setLimiteSelecao(GroupUtils.MAX_SELECTION);
                adapterSelection.setLimiteSelecao(getLimiteSelecao());
            }

            txtViewLimiteManage.setText(String.format("%d%s%d", 0, "/", getLimiteSelecao()));
            adapterSelection.setStatusEpilepsia(epilepsia);
            adapterSelection.setLimiteSelecao(getLimiteSelecao());
        }
    }

    private void recuperarDadosIniciais() {
        if (listaUsuarios != null && listaUsuarios.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }
        exibirProgress();
        if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_DEMOTING)) {
            queryInicial = firebaseRef.child("groupFollowers")
                    .child(idGrupo).orderByChild("administrator")
                    .equalTo(true)
                    .limitToFirst(CommunityUtils.MAX_NUMBER_ADMS);
        } else {
            if (trocarQueryInicial) {
                queryInicial = firebaseRef.child("groupFollowers")
                        .child(idGrupo).orderByChild("timestampinteracao")
                        .startAt(lastTimestamp + 1)
                        .limitToFirst(1);
            } else {
                queryInicial = firebaseRef.child("groupFollowers")
                        .child(idGrupo).orderByChild("timestampinteracao").limitToFirst(1);
            }
        }

        ultimoElemento(new RecuperaUltimoElemento() {
            @Override
            public void onRecuperado() {

                queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Grupo grupoInicial = snapshot1.getValue(Grupo.class);
                                if (grupoInicial != null
                                        && grupoInicial.getIdParticipante() != null
                                        && !grupoInicial.getIdParticipante().isEmpty()) {

                                    if (grupoInicial.getIdParticipante().equals(idSuperAdmGrupo)) {
                                        lastTimestamp = grupoInicial.getTimestampinteracao();
                                        trocarQueryInicial = true;
                                        recuperarDadosIniciais();
                                    }else{
                                        if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                                            adicionarUser(grupoInicial);
                                            lastTimestamp = grupoInicial.getTimestampinteracao();
                                        } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_PROMOTE)) {
                                            if (!grupoInicial.isAdministrator()) {
                                                //Somente exibir usuários que não são adms.
                                                adicionarUser(grupoInicial);
                                                lastTimestamp = grupoInicial.getTimestampinteracao();
                                            } else {
                                                lastTimestamp = grupoInicial.getTimestampinteracao();
                                                trocarQueryInicial = true;
                                                recuperarDadosIniciais();
                                            }
                                        } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_DEMOTING)) {
                                            if (grupoInicial.isAdministrator()) {
                                                //Somente exibir usuários que são adms.
                                                adicionarUser(grupoInicial);
                                                lastTimestamp = grupoInicial.getTimestampinteracao();
                                            } else {
                                                lastTimestamp = grupoInicial.getTimestampinteracao();
                                                trocarQueryInicial = true;
                                                recuperarDadosIniciais();
                                            }
                                        } else if (fundador || !grupoInicial.isAdministrator()) {
                                            adicionarUser(grupoInicial);
                                            lastTimestamp = grupoInicial.getTimestampinteracao();
                                        } else {
                                            lastTimestamp = grupoInicial.getTimestampinteracao();
                                            trocarQueryInicial = true;
                                            recuperarDadosIniciais();
                                        }
                                    }
                                } else {
                                    ocultarProgress();
                                }
                            }
                        } else {
                            ocultarProgress();
                            ToastCustomizado.toastCustomizadoCurto("Não há participantes disponíveis a serem gerenciados pela função escolhida.", getApplicationContext());
                        }
                        queryInicial.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        lastTimestamp = -1;
                    }
                });
            }
        });
    }

    private void adicionarUser(Grupo grupoAlvo) {
        recuperaDadosUser(grupoAlvo.getIdParticipante(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                usuarioDiffDAO.adicionarUsuario(dadosUser);
                usuarioDiffDAO.adicionarIdAoSet(idsUsuarios, dadosUser.getIdUsuario());

                adapterSelection.updateUsersList(listaUsuarios, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        adicionarDadoDoUsuario(dadosUser);
                        ocultarProgress();
                        setLoading(false);
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

    private void configPaginacao() {
        if (recyclerViewManage != null) {
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
                                    if (isPesquisaAtivada()) {
                                        carregarMaisDadosFiltrados(nomePesquisado);
                                    } else {
                                        carregarMaisDados();
                                    }
                                }
                            }
                        }, 100);
                    }
                }
            };
            recyclerViewManage.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados() {
        if (!isPesquisaAtivada()) {
            if (listaUsuarios.size() > 1
                    && idUltimoElemento != null && !idUltimoElemento.isEmpty()
                    && idUltimoElemento.equals(listaUsuarios.get(listaUsuarios.size() - 1).getIdUsuario())) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA CHAT " + idUltimoElemento, getApplicationContext());
                return;
            }

            exibirProgress();

            queryLoadMore = firebaseRef.child("groupFollowers")
                    .child(idGrupo)
                    .orderByChild("timestampinteracao")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);

            queryLoadMore.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Grupo dadoParticipante = snapshot1.getValue(Grupo.class);
                            if (dadoParticipante != null
                                    && dadoParticipante.getIdParticipante() != null
                                    && !dadoParticipante.getIdParticipante().isEmpty()) {

                                if (listaUsuarios != null && listaUsuarios.size() > 1 && idsUsuarios != null && idsUsuarios.size() > 0
                                        && idsUsuarios.contains(dadoParticipante.getIdParticipante())) {
                                    ocultarProgress();
                                    setLoading(false);
                                    return;
                                }

                                if (listaUsuarios != null && listaUsuarios.size() > 1
                                        && dadoParticipante.getTimestampinteracao() < listaUsuarios.get(0).getTimestampinteracao()) {
                                    ocultarProgress();
                                    setLoading(false);
                                    return;
                                }

                                if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                                    prepararListaPaginacao(dadoParticipante);
                                } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_PROMOTE)) {
                                    if (!dadoParticipante.isAdministrator()) {
                                        prepararListaPaginacao(dadoParticipante);
                                    } else {
                                        long key = dadoParticipante.getTimestampinteracao();
                                        if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                            lastTimestamp = key;
                                        }
                                    }
                                } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_DEMOTING)) {
                                    if (dadoParticipante.isAdministrator()) {
                                        prepararListaPaginacao(dadoParticipante);
                                    } else {
                                        long key = dadoParticipante.getTimestampinteracao();
                                        if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                            lastTimestamp = key;
                                        }
                                    }
                                } else if (fundador || !dadoParticipante.isAdministrator()) {
                                    prepararListaPaginacao(dadoParticipante);
                                } else {
                                    long key = dadoParticipante.getTimestampinteracao();
                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                        lastTimestamp = key;
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
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    ocultarProgress();
                    lastTimestamp = -1;
                }
            });
        }
    }

    private void carregarMaisDadosFiltrados(String dadoAnterior) {
        if (isPesquisaAtivada() && listaFiltrada != null) {

            ToastCustomizado.toastCustomizadoCurto("PAGINACAO - LOAD:  " + isLoading, getApplicationContext());

            if (listaFiltrada.size() > 1
                    && idUltimoElementoFiltro != null && !idUltimoElementoFiltro.isEmpty()
                    && idUltimoElementoFiltro.equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdParticipante())) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA ONE " + idUltimoElementoFiltro, getApplicationContext());
                return;
            }

            exibirProgress();

            if (listaFiltrada != null && !listaFiltrada.isEmpty()
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMorePesquisa = firebaseRef.child("group_participants_by_name")
                        .child(idUsuario)
                        .orderByChild("nomeUsuarioPesquisa")
                        .startAt(dadoAnterior).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
                queryLoadMorePesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                                Grupo grupoPesquisa = snapshotChildren.getValue(Grupo.class);
                                if (grupoPesquisa != null && grupoPesquisa.getIdParticipante() != null
                                        && !grupoPesquisa.getIdParticipante().isEmpty()
                                        && !grupoPesquisa.getIdParticipante().equals(idUsuario)) {

                                    if (listaFiltrada != null && listaFiltrada.size() > 1 && idsFiltrados != null && idsUsuarios.size() > 0
                                            && idsFiltrados.contains(grupoPesquisa.getIdParticipante())) {
                                        ocultarProgress();
                                        setLoading(false);
                                        return;
                                    }

                                    if (listaFiltrada != null && listaFiltrada.size() > 1
                                            && grupoPesquisa.getTimestampinteracao() < listaFiltrada.get(0).getTimestampinteracao()) {
                                        ocultarProgress();
                                        setLoading(false);
                                        return;
                                    }

                                    recuperarParticipante(grupoPesquisa.getIdParticipante(), new RecuperaParticipante() {
                                        @Override
                                        public void onRecuperado(Grupo dadoParticipante) {
                                            if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                                                prepararListaPaginacaoFiltro(grupoPesquisa.getNomeUsuarioPesquisa(), dadoParticipante);
                                            } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_PROMOTE)) {
                                                if (!dadoParticipante.isAdministrator()) {
                                                    prepararListaPaginacaoFiltro(grupoPesquisa.getNomeUsuarioPesquisa(), dadoParticipante);
                                                } else {
                                                    long key = dadoParticipante.getTimestampinteracao();
                                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                                        lastTimestamp = key;
                                                    }
                                                }
                                            } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_DEMOTING)) {
                                                if (dadoParticipante.isAdministrator()) {
                                                    prepararListaPaginacaoFiltro(grupoPesquisa.getNomeUsuarioPesquisa(), dadoParticipante);
                                                } else {
                                                    long key = dadoParticipante.getTimestampinteracao();
                                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                                        lastTimestamp = key;
                                                    }
                                                }
                                            } else if (fundador || !dadoParticipante.isAdministrator()) {
                                                prepararListaPaginacaoFiltro(grupoPesquisa.getNomeUsuarioPesquisa(), dadoParticipante);
                                            } else {
                                                long key = dadoParticipante.getTimestampinteracao();
                                                if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                                    lastTimestamp = key;
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
                                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao realizar a pesquisa, tente novamente.", getApplicationContext());
                                        }
                                    });
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
            } else {
                ocultarProgress();
            }
        }
    }

    private void adicionarMaisDados(List<Usuario> newUsuarios, String idUser) {
        if (newUsuarios != null && newUsuarios.size() >= 1) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    for (Usuario usuarioSemNome : newUsuarios) {
                        if (usuarioSemNome.getIdUsuario().equals(dadosUser.getIdUsuario())) {
                            contadorNome++;
                            newUsuarios.get(findPositionNewList(newUsuarios, usuarioSemNome.getIdUsuario())).setNomeUsuario(dadosUser.getNomeUsuario());
                        }
                        if (contadorNome == newUsuarios.size()) {
                            contadorNome = 0;
                            usuarioDiffDAO.carregarMaisUsuario(newUsuarios, idsUsuarios);
                            usuarioDiffDAO.adicionarIdAoSet(idsUsuarios, idUser);
                            Collections.sort(listaUsuarios, usuarioComparator);
                            adapterSelection.updateUsersList(listaUsuarios, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {
                                    ocultarProgress();
                                    adicionarDadoDoUsuario(dadosUser);
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
            setLoading(false);
        }
    }

    private void adicionarMaisDadosFiltrados(List<Usuario> newUsuarios, String idUser) {
        if (newUsuarios != null && !newUsuarios.isEmpty()) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    usuarioDAOFiltrado.carregarMaisUsuario(newUsuarios, idsFiltrados);
                    usuarioDAOFiltrado.adicionarIdAoSet(idsFiltrados, idUser);

                    Collections.sort(listaFiltrada, usuarioComparator);
                    adapterSelection.updateUsersList(listaFiltrada, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadoDoUsuario(dadosUser);
                            setLoading(false);
                        }
                    });
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
            setLoading(false);
        }
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        //*ToastCustomizado.toastCustomizadoCurto("Busca: " + nome, requireContext());

        exibirProgress();

        if (trocarQueryInicialFiltro) {
            queryInicialFind = firebaseRef.child("group_participants_by_name")
                    .child(idUsuario)
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAfter(nome).endAt(nome + "\uf8ff").limitToFirst(1);
        } else {
            queryInicialFind = firebaseRef.child("group_participants_by_name")
                    .child(idUsuario)
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(1);
        }

        queryInicialFind.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (counter != searchCounter) {
                    ocultarProgress();
                    setLoading(false);
                    ToastCustomizado.toastCustomizadoCurto("Return counter != searchCounter", getApplicationContext());
                    return;
                }

                if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                    ocultarProgress();
                    setLoading(false);
                    ToastCustomizado.toastCustomizadoCurto("Return listaFiltrada != null && listaFiltrada.size() >= 1", getApplicationContext());
                    return;
                }

                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Grupo grupoPesquisa = snapshotChildren.getValue(Grupo.class);
                        if (grupoPesquisa != null && grupoPesquisa.getIdParticipante() != null
                                && !grupoPesquisa.getIdParticipante().isEmpty()
                                && !grupoPesquisa.getIdParticipante().equals(idUsuario)) {

                            if (!grupoPesquisa.getIdParticipante().equals(idSuperAdmGrupo)) {
                                ocultarProgress();
                                lastName = grupoPesquisa.getNomeUsuarioPesquisa();
                                trocarQueryInicialFiltro = true;
                                dadoInicialFiltragem(nome, counter);
                            }else{
                                recuperarParticipante(grupoPesquisa.getIdParticipante(), new RecuperaParticipante() {
                                    @Override
                                    public void onRecuperado(Grupo dadoParticipante) {
                                        recuperaDadosUser(grupoPesquisa.getIdParticipante(), new RecuperaUser() {
                                            @Override
                                            public void onRecuperado(Usuario dadosUser) {
                                                lastName = grupoPesquisa.getNomeUsuarioPesquisa();
                                                if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                                                    adicionarUserFiltrado(dadosUser);
                                                } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_PROMOTE)) {
                                                    if (!dadoParticipante.isAdministrator()) {
                                                        //Somente exibir usuários que não são adms.
                                                        adicionarUserFiltrado(dadosUser);
                                                    } else {
                                                        trocarQueryInicialFiltro = true;
                                                        dadoInicialFiltragem(nome, counter);
                                                    }
                                                } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_DEMOTING)) {
                                                    if (dadoParticipante.isAdministrator()) {
                                                        //Somente exibir usuários que são adms.
                                                        adicionarUserFiltrado(dadosUser);
                                                    } else {
                                                        trocarQueryInicialFiltro = true;
                                                        dadoInicialFiltragem(nome, counter);
                                                    }
                                                } else if (fundador || !dadoParticipante.isAdministrator()) {
                                                    adicionarUserFiltrado(dadosUser);
                                                } else {
                                                    trocarQueryInicialFiltro = true;
                                                    dadoInicialFiltragem(nome, counter);
                                                }
                                            }

                                            @Override
                                            public void onSemDado() {
                                                trocarQueryInicialFiltro = true;
                                                dadoInicialFiltragem(nome, counter);
                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onSemDado() {
                                        trocarQueryInicialFiltro = true;
                                        dadoInicialFiltragem(nome, counter);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        ocultarProgress();
                                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao realizar a pesquisa, tente novamente.", getApplicationContext());
                                    }
                                });
                            }
                        } else if (grupoPesquisa != null
                                && grupoPesquisa.getTimestampinteracao() != -1) {
                            ocultarProgress();
                            trocarQueryInicialFiltro = true;
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

    private void adicionarUserFiltrado(Usuario dadosUser) {

        usuarioDAOFiltrado.adicionarUsuario(dadosUser);
        usuarioDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());

        Collections.sort(listaFiltrada, usuarioComparator);
        adapterSelection.updateUsersList(listaFiltrada, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                ocultarProgress();
                adicionarDadoDoUsuario(dadosUser);
                setLoading(false);
            }
        });
    }

    private void recuperaDadosUser(String idUser, RecuperaUser callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUser, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                UsuarioUtils.checkBlockingStatus(getApplicationContext(), idUser, new UsuarioUtils.CheckLockCallback() {
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
    }

    private void limparFiltragem(boolean fecharTeclado) {

        if (searchView != null && fecharTeclado) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            searchView.clearFocus();
        }
        removeValueEventListenerFiltro(null);

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

            setLoading(false);

            Collections.sort(listaUsuarios, usuarioComparator);
            adapterSelection.updateUsersList(listaUsuarios, new AdapterUsersSelectionCommunity.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                    contadorUpdate++;
                    if (idsParaAtualizar != null && !idsParaAtualizar.isEmpty()) {
                        for (String idUpdate : idsParaAtualizar.keySet()) {
                            int index = adapterSelection.findPositionInList(idUpdate);
                            Bundle bundleUpdate = idsParaAtualizar.get(idUpdate);
                            if (index != -1) {
                                adapterSelection.notifyItemChanged(index, bundleUpdate);
                                ToastCustomizado.toastCustomizadoCurto("CODE NOTIFY", getApplicationContext());
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

    private void removeValueEventListenerFiltro(RemoverListenersCallback callback) {
        if (listenerFiltroHashMap != null && referenceFiltroHashMap != null
                && !listenerFiltroHashMap.isEmpty() && !referenceFiltroHashMap.isEmpty()) {

            for (String userId : listenerFiltroHashMap.keySet()) {
                Query userRef = referenceFiltroHashMap.get(userId);
                ChildEventListener listener = listenerFiltroHashMap.get(userId);
                if (userRef != null && listener != null) {
                    ToastCustomizado.toastCustomizado("ListenerRemovido: " + userId, getApplicationContext());
                    userRef.removeEventListener(listener);
                }

                controleRemocao++;
                if (controleRemocao == referenceFiltroHashMap.size()) {
                    referenceFiltroHashMap.clear();
                    listenerFiltroHashMap.clear();
                    controleRemocao = 0;
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

    private void ultimoElemento(RecuperaUltimoElemento callback) {
        if (trocarQueryUltimo) {
            queryUltimoElemento = firebaseRef.child("groupFollowers")
                    .child(idGrupo).orderByChild("timestampinteracao")
                    .endAt(timeUltimo - 1)
                    .limitToLast(1);
        }else{
            queryUltimoElemento = firebaseRef.child("groupFollowers")
                    .child(idGrupo).orderByChild("timestampinteracao").limitToLast(1);
        }

        queryUltimoElemento.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Grupo.class).getIdParticipante().equals(idSuperAdmGrupo)) {
                        trocarQueryUltimo = true;
                        timeUltimo = snapshot1.getValue(Grupo.class).getTimestampinteracao();
                        queryUltimoElemento.removeEventListener(this);
                        ultimoElemento(callback);
                    }else{
                        idUltimoElemento = snapshot1.getValue(Grupo.class).getIdParticipante();
                        setLoading(false);
                        if (callback != null && listaUsuarios != null && listaUsuarios.isEmpty()) {
                            callback.onRecuperado();
                        }
                    }
                }
                queryUltimoElemento.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaUsuarios != null && listaUsuarios.isEmpty()) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void ultimoElementoFiltro(String nome, RecuperaUltimoElemento callback) {
        if (trocarQueryUltimoFiltro) {
            queryUltimoElementoFiltro = firebaseRef.child("group_by_name")
                    .child(idGrupo)
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAfter(nome).endAt(nome + "\uf8ff").limitToLast(1);
        }else{
            queryUltimoElementoFiltro = firebaseRef.child("group_by_name")
                    .child(idGrupo)
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAt(nome).endAt(nome + "\uf8ff").limitToLast(1);
        }

        queryUltimoElementoFiltro.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(Usuario.class).getIdUsuario().equals(idSuperAdmGrupo)) {
                        trocarQueryUltimoFiltro = true;
                        queryUltimoElementoFiltro.removeEventListener(this);
                        ultimoElementoFiltro(nome, callback);
                    }else{
                        idUltimoElementoFiltro = snapshot1.getValue(Usuario.class).getIdUsuario();
                        setLoading(false);
                        if (callback != null && listaFiltrada != null && listaFiltrada.isEmpty()) {
                            callback.onRecuperado();
                        }
                    }
                }
                queryUltimoElementoFiltro.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaFiltrada != null && listaFiltrada.isEmpty()) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void limparActivity() {
        idsAIgnorarListeners.clear();
        removeValueEventListener();
        removeValueEventListenerFiltro(null);
        if (usuarioDiffDAO != null) {
            usuarioDiffDAO.limparListaUsuarios();
        }
        if (listaDadosUser != null) {
            listaDadosUser.clear();
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

        if (limparLockRef != null) {
            limparLockRef.onDisconnect().cancel();
            limparLockRef.removeValue();
        }

        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }

    public void removeValueEventListener() {

        if (verificaLockRef != null) {
            FirebaseUtils firebaseUtils = new FirebaseUtils();
            firebaseUtils.removerValueListener(verificaLockRef, valueEventListenerLock);
        }

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

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, GroupManagementActivity.this);
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, GroupManagementActivity.this);
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
    public void onMarcado() {
        totalSelecionado++;
        txtViewLimiteManage.setText(String.format("%d%s%d", totalSelecionado, "/", getLimiteSelecao()));
    }

    @Override
    public void onDesmarcado() {
        if (totalSelecionado <= 0) {
            totalSelecionado = 0;
        } else {
            totalSelecionado--;
        }
        txtViewLimiteManage.setText(String.format("%d%s%d", totalSelecionado, "/", getLimiteSelecao()));
    }

    public long getLimiteSelecao() {
        return limiteSelecao;
    }

    public void setLimiteSelecao(long limiteSelecao) {
        this.limiteSelecao = limiteSelecao;
    }

    private boolean areFirstThreeItemsVisible(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibleItemPosition = 0;
        if (layoutManager != null) {
            firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        }
        return firstVisibleItemPosition <= 2;
    }

    private void salvarParticipantesEdicao() {
        FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                TimestampUtils.RecuperarTimestamp(getApplicationContext(), new TimestampUtils.RecuperarTimestampCallback() {
                    @Override
                    public void onRecuperado(long timestampNegativo) {
                        HashMap<String, Object> dadosOperacao = new HashMap<>();
                        String caminhoGrupo = "/grupos/" + idGrupo + "/";
                        for (String idParticipante : adapterSelection.getListaSelecao()) {
                            groupUtils.verificaBlock(idParticipante, idGrupo, new GroupUtils.VerificaBlockCallback() {
                                @Override
                                public void onBlock(boolean status) {
                                    if (status) {
                                        //Bloqueado
                                        contadorParticipantes++;
                                        idsARemover.add(idParticipante);
                                    } else {
                                        String caminhoFollowers = "/groupFollowers/" + idGrupo + "/" + idParticipante + "/";
                                        String caminhoFollowing = "/groupFollowing/" + idParticipante + "/" + idGrupo + "/";
                                        dadosOperacao.put(caminhoGrupo + "nrParticipantes", ServerValue.increment(1));
                                        dadosOperacao.put(caminhoFollowers + "timestampinteracao", timestampNegativo);
                                        dadosOperacao.put(caminhoFollowers + "idParticipante", idParticipante);
                                        dadosOperacao.put(caminhoFollowers + "administrator", false);
                                        dadosOperacao.put(caminhoFollowing + "idGrupo", idGrupo);
                                        dadosOperacao.put(caminhoFollowing + "timestampinteracao", timestampNegativo);

                                        contadorParticipantes++;
                                    }

                                    if (contadorParticipantes == adapterSelection.getListaSelecao().size()) {
                                        contadorParticipantes = 0;

                                        if (idsARemover != null && !idsARemover.isEmpty()) {
                                            for (String idRemover : idsARemover) {
                                                contadorRemocao++;
                                                adapterSelection.getListaSelecao().remove(idRemover);
                                                if (contadorRemocao == idsARemover.size()) {
                                                    idsARemover.clear();
                                                    contadorRemocao = 0;
                                                }
                                            }
                                        }
                                        idsNovosParticipantes = grupoAtual.getParticipantes();
                                        idsNovosParticipantes.addAll(adapterSelection.getListaSelecao());
                                        dadosOperacao.put(caminhoGrupo + "participantes", idsNovosParticipantes);
                                        dadosOperacao.put(caminhoGrupo + "nrParticipantes", ServerValue.increment(1));

                                        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                midiaUtils.ocultarProgressDialog();
                                                Intent intent = new Intent(GroupManagementActivity.this, GroupDetailsActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("idGrupo", idGrupo);
                                                intent.putExtra("chatComunidade", chatComunidade);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onNaoExiste() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void removerDaSelecao(String idRemovido) {
        if (idRemovido != null && !idRemovido.isEmpty()
                && adapterSelection.getListaSelecao() != null
                && !adapterSelection.getListaSelecao().isEmpty()
                && adapterSelection.getListaSelecao().contains(idRemovido)) {
            adapterSelection.getListaSelecao().remove(idRemovido);
            if (totalSelecionado <= 0) {
                totalSelecionado = 0;
            } else {
                totalSelecionado--;
            }
            txtViewLimiteManage.setText(String.format("%d%s%d", totalSelecionado, "/", getLimiteSelecao()));
        }
    }

    private void recuperarParticipante(String idAlvo, RecuperaParticipante callback) {
        DatabaseReference recuperarParticipanteRef = firebaseRef.child("groupFollowers")
                .child(idAlvo).child(idGrupo);
        recuperarParticipanteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Grupo dadosParticipante = dataSnapshot.getValue(Grupo.class);
                    callback.onRecuperado(dadosParticipante);
                } else {
                    callback.onSemDado();
                }
                recuperarParticipanteRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    private void prepararListaPaginacao(Grupo dadoParticipante) {
        List<Usuario> newParticipante = new ArrayList<>();
        long key = dadoParticipante.getTimestampinteracao();
        if (lastTimestamp != -1 && key != -1) {
            if (key != lastTimestamp || listaUsuarios.size() > 0 &&
                    !dadoParticipante.getIdParticipante().equals(listaUsuarios.get(listaUsuarios.size() - 1).getIdUsuario())) {
                Usuario usuarioNew = new Usuario();
                usuarioNew.setIdUsuario(dadoParticipante.getIdParticipante());
                if (!dadoParticipante.getIdParticipante().equals(idSuperAdmGrupo)) {
                    newParticipante.add(usuarioNew);
                }
                lastTimestamp = key;
            }
        }
        // Remove a última chave usada
        if (newParticipante.size() > PAGE_SIZE) {
            newParticipante.remove(0);
        }
        if (lastTimestamp != -1) {
            adicionarMaisDados(newParticipante, dadoParticipante.getIdParticipante());
        }
    }

    private void prepararListaPaginacaoFiltro(String nomeParticipante, Grupo dadoParticipante) {
        List<Usuario> newParticipantes = new ArrayList<>();
        if (lastName != null && !lastName.isEmpty() && nomeParticipante != null
                && !nomeParticipante.isEmpty()) {
            if (!nomeParticipante.equals(lastName) || listaFiltrada.size() > 0 &&
                    !dadoParticipante.getIdParticipante()
                            .equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdParticipante())) {
                Usuario usuarioNew = new Usuario();
                usuarioNew.setIdUsuario(dadoParticipante.getIdParticipante());
                if (!usuarioNew.getIdUsuario().equals(idSuperAdmGrupo)) {
                    newParticipantes.add(usuarioNew);
                }
                lastName = nomeParticipante;
                //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
            }
        }
        // Remove a última chave usada
        if (newParticipantes.size() > PAGE_SIZE) {
            newParticipantes.remove(0);
        }
        if (lastName != null && !lastName.isEmpty()) {
            adicionarMaisDadosFiltrados(newParticipantes, dadoParticipante.getIdParticipante());
        }
    }

    public void exibirProgressDialog(String tipoMensagem) {
        switch (tipoMensagem) {
            case CommunityUtils.FUNCTION_REMOVE:
                progressDialog.setMessage("Excluíndo participantes, aguarde....");
                break;
            case CommunityUtils.FUNCTION_PROMOTE:
                progressDialog.setMessage("Promovendo participantes, aguarde....");
                break;
            case CommunityUtils.FUNCTION_DEMOTING:
                progressDialog.setMessage("Despromovendo participantes, aguarde....");
                break;
            case CommunityUtils.FUNCTION_NEW_FOUNDER:
                progressDialog.setMessage("Transferindo cargo de fundador, aguarde....");
                break;
        }
        if (!GroupManagementActivity.this.isFinishing()) {
            progressDialog.show();
        }
    }

    public void ocultarProgressDialog() {
        if (progressDialog != null && !GroupManagementActivity.this.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void realizarOperacoes(List<String> listaIds, int index) {
        operacoesExecutadas++;
        if (index < listaIds.size()) {
            String idSelecao = listaIds.get(index);
            groupUtils.verificaSeEParticipante(idGrupo, idSelecao, new GroupUtils.VerificaParticipanteCallback() {
                @Override
                public void onParticipante(boolean status) {
                    if (!status) {
                        operacoesIgnoradas = true;
                        verificaOperacao(new ExecutarOperacaoCallback() {
                            @Override
                            public void onConcluido() {
                                realizarOperacoes(listaIds, index + 1);
                            }
                        });
                        return;
                    }

                    executarOperacao(idSelecao, new ExecutarOperacaoCallback() {
                        @Override
                        public void onConcluido() {
                            // Chama recursivamente para a próxima iteração
                            realizarOperacoes(listaIds, index + 1);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    operacaoComErro = true;
                    verificaOperacao(null);
                }
            });
        } else {
            // Todas as operações foram concluídas
            verificaOperacao(null);
        }
    }

    private void executarOperacao(String idAlvo, ExecutarOperacaoCallback callback) {
        switch (tipoGerenciamento) {
            case CommunityUtils.FUNCTION_REMOVE:
                groupUtils.sairDoGrupo(idGrupo, idAlvo, new GroupUtils.SairDoGrupoCallback() {
                    @Override
                    public void onConcluido() {
                        verificaOperacao(callback);
                    }

                    @Override
                    public void onError(String message) {
                        operacaoComErro = true;
                        verificaOperacao(callback);
                    }
                });
                break;
            case CommunityUtils.FUNCTION_PROMOTE:
                if (!fundador) {
                    ocultarProgressDialog();
                    return;
                }
                groupUtils.promoverParaAdm(idGrupo, idAlvo, new GroupUtils.PromoverAdmCallback() {
                    @Override
                    public void onConcluido() {
                        verificaOperacao(callback);
                    }

                    @Override
                    public void onNaoParticipa() {
                        operacoesIgnoradas = true;
                        verificaOperacao(callback);
                    }

                    @Override
                    public void onError(String message) {
                        operacaoComErro = true;
                        verificaOperacao(callback);
                    }
                });
                break;
            case CommunityUtils.FUNCTION_DEMOTING:
                if (!fundador) {
                    ocultarProgressDialog();
                    return;
                }
                groupUtils.despromoverAdm(idGrupo, idAlvo, new GroupUtils.DespromoverAdmCallback() {
                    @Override
                    public void onConcluido() {
                        verificaOperacao(callback);
                    }

                    @Override
                    public void onNaoParticipa() {
                        operacoesIgnoradas = true;
                        verificaOperacao(callback);
                    }

                    @Override
                    public void onError(String message) {
                        operacaoComErro = true;
                        verificaOperacao(callback);
                    }
                });
                break;
            case CommunityUtils.FUNCTION_NEW_FOUNDER:
                if (!fundador) {
                    ocultarProgressDialog();
                    return;
                }
                groupUtils.transferirFundador(idGrupo, idAlvo, new GroupUtils.TransferirFundadorCallback() {
                    @Override
                    public void onConcluido() {
                        verificaOperacao(callback);
                    }

                    @Override
                    public void onLimiteMaxAtingido() {
                        ToastCustomizado.toastCustomizadoCurto("Usuário selecionado já possui o número máximo de grupo, escolha outro usuário.", getApplicationContext());
                    }

                    @Override
                    public void onNaoParticipante() {
                        ToastCustomizado.toastCustomizadoCurto("Usuário selecionado não faz mais parte desse grupo, escolha outro usuário.", getApplicationContext());
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao transferir seu cargo. Tente novamente.", getApplicationContext());
                    }
                });
                break;
        }
    }

    private void verificaOperacao(ExecutarOperacaoCallback callback) {
        if (operacaoConcluida) {
            return;
        }
        if (operacoesExecutadas != -1 && adapterSelection != null) {
            if (operacoesExecutadas == adapterSelection.getListaSelecao().size()) {
                operacaoConcluida = true;
                ocultarProgressDialog();
                if (operacaoComErro) {
                    ToastCustomizado.toastCustomizadoCurto("Uma operação ou mais não foram executadas pois houve um erro.", getApplicationContext());
                }
                if (operacoesIgnoradas) {
                    ToastCustomizado.toastCustomizadoCurto("Uma operação ou mais foram ignoradas pois o usuário selecionado não atendia os requisitos para a função escolhida.", getApplicationContext());
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Concluído com sucesso.", getApplicationContext());
                }
                if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                    Intent intent = new Intent(GroupManagementActivity.this, ListaComunidadesActivityNEW.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //*******startActivity(intent);
                    finish();
                    return;
                }
                onBackPressed();
            } else if (callback != null) {
                callback.onConcluido();
            }
        }
    }

    private int findPositionNewList(List<Usuario> listaAlvo, String userId) {
        for (int i = 0; i < listaAlvo.size(); i++) {
            Usuario user = listaAlvo.get(i);
            if (user.getIdUsuario().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }

    private void inicializarComponentes() {
        toolbarManage = findViewById(R.id.toolbarIncBlack);
        imgBtnBackManage = findViewById(R.id.imgBtnIncBackBlack);
        txtTituloManageGroup = findViewById(R.id.txtViewIncTituloToolbarBlack);
        btnSalvarManage = findViewById(R.id.btnSalvarManageGroup);
        recyclerViewManage = findViewById(R.id.recyclerViewManageGroup);
        txtViewLimiteManage = findViewById(R.id.txtViewLimiteManageGroup);
        spinProgress = findViewById(R.id.spinProgressBarRecycler);
        searchView = findViewById(R.id.searchViewUsers);
    }
}