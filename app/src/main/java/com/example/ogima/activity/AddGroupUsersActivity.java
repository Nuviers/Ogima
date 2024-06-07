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
import com.example.ogima.adapter.AdapterUsersSelectionGroup;
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
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

public class AddGroupUsersActivity extends AppCompatActivity implements AdapterUsersSelectionGroup.AnimacaoIntent, AdapterUsersSelectionGroup.RemoverContatoListener, AdapterUsersSelectionGroup.MarcarUsuarioCallback, AdapterUsersSelectionGroup.DesmarcarUsuarioCallback {

    private String idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarManage;
    private ImageButton imgBtnBackManage;
    private RecyclerView recyclerViewManage;
    private TextView txtViewLimiteManage, txtTituloManageGroup;
    private Button btnSalvarManage;
    private long limiteSelecao;
    private String tipoGerenciamento;
    private SearchView searchView;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private int mCurrentPosition = -1;
    private boolean isLoading = false;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Contatos> listaContatos = new ArrayList<>();
    private Set<String> idsUsuarios = new HashSet<>();
    private ContactDiffDAO contactDiffDAO, contactDAOFiltrado;
    private Query queryInicial, queryLoadMore,
            queryInicialFiltro, queryLoadMorePesquisa, queryInicialFind,
            queryLoadMoreFiltro, newDataRef;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private String nomePesquisado = "";
    private List<Contatos> listaFiltrada = new ArrayList<>();
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
    private HashMap<String, Query> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private HashMap<String, Query> referenceFiltroHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerFiltroHashMap = new HashMap<>();
    private Set<String> idsListeners = new HashSet<>();
    private ChildEventListener childListenerInicioFiltro,
            childEventListenerContatos, childListenerMoreFiltro,
            childListenerInicio, childEventListenerNewData;
    private AdapterUsersSelectionGroup adapterSelection;
    private boolean trocarQueryInicial = false, trocarQueryInicialFiltro = false,
            trocarQueryMaisDados = false, trocarQueryMaisDadosFiltro = false;
    private long timeUltimo = -1, nameUltimo = -1;
    private Contatos contatoComparator;
    private int contadorRemocaoListener = 0;
    private FirebaseUtils firebaseUtils;
    private GroupUtils groupUtils;
    private Set<String> idsListenersNEWDATA = new HashSet<>();
    private HashMap<String, Query> referenceHashMapNEWDATA = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapNEWDATA = new HashMap<>();
    private int contadorRemocaoListenerNEWDATA = 0;
    private static final String TAG = "CONTATOtag";
    private String idPrimeiroDado = "";
    private Set<String> idsAIgnorarListeners = new HashSet<>();
    private String idUltimoElemento, idUltimoElementoFiltro;
    private Query queryUltimoElemento, queryUltimoElementoFiltro;
    private ValueEventListener listenerUltimoElemento, listenerUltimoElementoFiltro;
    private int controleRemocao = 0;
    private Set<Usuario> idsTempFiltro = new HashSet<>();
    private int aosFiltros = 0;
    private int posicaoChanged = -1;
    private HashMap<String, Bundle> idsParaAtualizar = new HashMap<>();
    private int contadorUpdate = 0;
    private int contadorNome = 0;
    private int totalSelecionado = 0;
    private String idGrupo = "";
    private boolean edicao = false;
    private ProgressDialog progressDialog;
    private int contadorParticipantes = 0;
    private ArrayList<String> idsARemover;
    private int contadorRemocao = 0;
    private ArrayList<String> idsNovosParticipantes;
    private MidiaUtils midiaUtils;
    private boolean trocarQueryUltimo = false, trocarQueryUltimoFiltro = false;
    private boolean grupoPublico = true;

    private interface VerificaExistenciaCallback {
        void onExistencia(boolean status, Contatos contatoAtualizado);

        void onError(String message);
    }

    private interface RecuperaUltimoElemento {
        void onRecuperado();
    }

    private interface RemoverListenersCallback {
        void onRemovido();
    }

    private interface RecuperarIdsFiltroCallback {
        void onRecuperado(Set<Usuario> listaIdsRecuperados);
    }

    private interface RecuperaUser {
        void onRecuperado(Usuario usuarioAtual);

        void onSemDado();

        void onError(String message);
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

    public AddGroupUsersActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        contatoComparator = new Contatos(false, true);
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
        firebaseUtils = new FirebaseUtils();
        progressDialog = new ProgressDialog(AddGroupUsersActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        groupUtils = new GroupUtils(AddGroupUsersActivity.this, getApplicationContext());
        midiaUtils = new MidiaUtils(AddGroupUsersActivity.this, getApplicationContext(), progressDialog);
        idsNovosParticipantes = new ArrayList<>();
        txtTituloManageGroup.setText(FormatarContadorUtils.abreviarTexto("Adicionar participantes", 20));
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }

        Bundle dados = getIntent().getExtras();
        edicao = dados != null && dados.containsKey("idGrupo");
        if (edicao) {
            idGrupo = dados.getString("idGrupo");
        }

        if (dados != null && dados.containsKey("grupoPublico")) {
            grupoPublico = dados.getBoolean("grupoPublico");
        }

        setLoading(true);
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean epilepsia) {
                setPesquisaAtivada(false);
                configRecycler(epilepsia);
                configSearchView();
                contactDiffDAO = new ContactDiffDAO(listaContatos, adapterSelection);
                contactDAOFiltrado = new ContactDiffDAO(listaFiltrada, adapterSelection);
                setLoading(true);
                recuperarDadosIniciais();
                configPaginacao();


                btnSalvarManage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adapterSelection == null) {
                            return;
                        }
                        if (adapterSelection.getListaSelecao() == null
                                || adapterSelection.getListaSelecao().size() <= 0) {
                            return;
                        }

                        /*
                        for(String idSelecionado : adapterSelection.getListaSelecao()){
                            ToastCustomizado.toastCustomizadoCurto("Selecionado: " + idSelecionado, getApplicationContext());
                        }
                         */

                        if (edicao) {
                            midiaUtils.exibirProgressDialog("", "salvarParticipantes");
                            salvarParticipantesEdicao();
                        } else {
                            Intent intent = new Intent(AddGroupUsersActivity.this, CreateGroupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("idParticipantes", adapterSelection.getListaSelecao());
                            intent.putExtra("grupoPublico", grupoPublico);
                            intent.putExtra("edit", false);
                            startActivity(intent);
                            finish();
                        }
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
                                        contactDAOFiltrado.limparListaContatos();
                                        adapterSelection.updateContatoList(listaFiltrada, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
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
                    limparFiltragem(true);
                }
                return true;
            }
        });
    }

    private void configRecycler(boolean epilepsia) {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewManage.setHasFixedSize(true);
            recyclerViewManage.setLayoutManager(linearLayoutManager);

            adapterSelection = new AdapterUsersSelectionGroup(getApplicationContext(),
                    listaContatos, listaDadosUser,
                    getResources().getColor(R.color.chat_list_color), getLimiteSelecao(), this, this, this, this);
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
                adapterSelection.setLimiteSelecao(limiteSelecao);
            }

            txtViewLimiteManage.setText(String.format("%d%s%d", 0, "/", getLimiteSelecao()));
            adapterSelection.setStatusEpilepsia(epilepsia);
        }
    }

    private void recuperarDadosIniciais() {
        if (listaContatos != null && listaContatos.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }

        if (trocarQueryInicial && lastTimestamp != -1) {
            queryInicial = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("timestampContato")
                    .startAt(lastTimestamp + 1)
                    .limitToFirst(1);
        } else {
            exibirProgress();
            queryInicial = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("timestampContato").limitToFirst(1);
        }

        ultimoElemento(new RecuperaUltimoElemento() {
            @Override
            public void onRecuperado() {
                ocultarProgress();
                //ToastCustomizado.toastCustomizado("INICIO CHAMADO " + idUltimoElemento, getApplicationContext());
                childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Contatos contato = snapshot.getValue(Contatos.class);
                            if (contato != null
                                    && contato.getIdContato() != null
                                    && !contato.getIdContato().isEmpty()) {

                                if (listaContatos != null &&
                                        !listaContatos.isEmpty() && listaContatos.get(listaContatos.size() - 1).getIdContato()
                                        .equals(contato.getIdContato())) {
                                    ocultarProgress();
                                    return;
                                }
                                groupUtils.verificaSeEParticipante(idGrupo, contato.getIdContato(), new GroupUtils.VerificaParticipanteCallback() {
                                    @Override
                                    public void onParticipante(boolean status) {
                                        if (status) {
                                            queryInicial.removeEventListener(childListenerInicio);
                                            lastTimestamp = contato.getTimestampContato();
                                            ocultarProgress();
                                            trocarQueryInicial = true;
                                            recuperarDadosIniciais();
                                        } else {
                                            idPrimeiroDado = contato.getIdContato();
                                            lastTimestamp = contato.getTimestampContato();
                                            adicionarContatos(contato, false);
                                        }
                                    }

                                    @Override
                                    public void onError(String message) {

                                    }
                                });
                            }
                        } else {
                            ocultarProgress();
                            //Exibir um textview com essa mensagem.
                            String msgSemConversas = "Você não possui contatos no momento.";
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                                return;
                            }
                            ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO INICIO", getApplicationContext());
                            logicaAtualizacao(snapshot, false);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                            if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                                //O próprio listenernewdata vai cuidar da remoção desse dado.
                                return;
                            }

                            ToastCustomizado.toastCustomizado("DELETE INICIO", getApplicationContext());
                            logicaRemocao(contatoRemovido, true, true);

                            verificaExistencia(contatoRemovido.getIdContato(), new VerificaExistenciaCallback() {
                                @Override
                                public void onExistencia(boolean status, Contatos contatoAtualizado) {
                                    if (status) {
                                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                                && listenerHashMapNEWDATA.containsKey(contatoRemovido.getIdContato())) {
                                        } else {
                                            ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do inicio " + contatoRemovido.getIdContato(), getApplicationContext());
                                            anexarNovoDado(contatoAtualizado);
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

    private void adicionarContatos(Contatos contatoAlvo, boolean dadoModificado) {
        recuperaDadosUser(contatoAlvo.getIdContato(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                contatoAlvo.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                contactDiffDAO.adicionarContato(contatoAlvo);
                contactDiffDAO.adicionarIdAoSet(idsUsuarios, dadosUser.getIdUsuario());

                List<Contatos> listaAtual = new ArrayList<>();
                if (isPesquisaAtivada()) {
                    listaAtual = listaFiltrada;
                } else {
                    listaAtual = listaContatos;
                }

                adapterSelection.updateContatoList(listaAtual, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {

                        if (dadoModificado) {
                            adicionarDadoDoUsuario(dadosUser, newDataRef, childEventListenerNewData, dadoModificado);
                        } else {
                            adicionarDadoDoUsuario(dadosUser, null, null, dadoModificado);
                        }
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

    private void verificaExistencia(String idContatos, VerificaExistenciaCallback callback) {
        DatabaseReference verificaExistenciaRef = firebaseRef.child("contatos")
                .child(idUsuario).child(idContatos);
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onExistencia(snapshot.getValue() != null, snapshot.getValue(Contatos.class));
                verificaExistenciaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
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
                                        carregarMaisDadosFiltrados(nomePesquisado, new RecuperarIdsFiltroCallback() {
                                            @Override
                                            public void onRecuperado(Set<Usuario> listaIdsRecuperados) {
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
            recyclerViewManage.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados() {
        if (!isPesquisaAtivada()) {
            exibirProgress();
            ToastCustomizado.toastCustomizadoCurto("Mais dados -_- " + idUltimoElemento, getApplicationContext());
            if (listaContatos.size() > 1) {
                ToastCustomizado.toastCustomizadoCurto("IdLista: " + listaContatos.get(listaContatos.size() - 1).getIdContato(), getApplicationContext());
            }
            if (listaContatos.size() > 1
                    && idUltimoElemento != null && !idUltimoElemento.isEmpty()
                    && idsUsuarios != null && !idsUsuarios.isEmpty()
                    && idsUsuarios.contains(idUltimoElemento)) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA CHAT " + idUltimoElemento, getApplicationContext());
                //NO LUGAR DE COMPARAR COM O ÚLTIMO ITEM ADICIONADO NA LISTA
                //O CORRETO É TER UM MÉTODO SEPARADO QUE PEGA O ÚLTIMO ELEMENTO
                //NO SERVIDOR COM O LISTENER ATIVO SEMPRE ASSIM EU COMPARO COM O ID
                //DESSE DADO, ASSIM EU TENHO COMO SABER QUANDO A PAGINAÇÃO NÃO DEVE CONTINUAR
                //JÁ QUE SABERIA QUE A LISTA JÁ PEGOU TODOS OS DADOS POSSÍVEIS DO SERVIDOR
                //E NÃO TERIA MAIS DADOS ALÉM DELE, POSSO COLOCAR ESSA LÓGICA NO SCROLLISTENER
                //E RETIRAR ESSE CÓDIGO ANTERIOR DO MÉTODO CARREGARMAISDADOS.
                return;
            }

            queryLoadMore = firebaseRef.child("contatos")
                    .child(idUsuario)
                    .orderByChild("timestampContato")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);

            childEventListenerContatos = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        Contatos contatoMore = snapshot.getValue(Contatos.class);
                        if (contatoMore != null
                                && contatoMore.getIdContato() != null
                                && !contatoMore.getIdContato().isEmpty()) {
                            Log.d(TAG, "Timestamp key: " + lastTimestamp);
                            Log.d(TAG, "id: " + contatoMore.getIdContato() + " time: " + contatoMore.getTimestampContato());
                            if (listaContatos != null && listaContatos.size() > 1 && idsUsuarios != null && idsUsuarios.size() > 0
                                    && idsUsuarios.contains(contatoMore.getIdContato())) {
                                Log.d(TAG, "Id já existia: " + contatoMore.getIdContato());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            if (listaContatos != null && listaContatos.size() > 1
                                    && contatoMore.getTimestampContato() < listaContatos.get(0).getTimestampContato()) {
                                ToastCustomizado.toastCustomizadoCurto("TIME IGNORADO", getApplicationContext());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            //*ToastCustomizado.toastCustomizadoCurto("ADICIONADO " + contatoMore.getIdUsuario(), requireContext());
                            List<Contatos> newContatos = new ArrayList<>();
                            long key = contatoMore.getTimestampContato();
                            if (lastTimestamp != -1 && key != -1) {
                                if (key != lastTimestamp || listaContatos.size() > 0 &&
                                        !contatoMore.getIdContato()
                                                .equals(listaContatos.get(listaContatos.size() - 1).getIdContato())) {
                                    groupUtils.verificaSeEParticipante(idGrupo, contatoMore.getIdContato(), new GroupUtils.VerificaParticipanteCallback() {
                                        @Override
                                        public void onParticipante(boolean status) {
                                            if (!status) {
                                                newContatos.add(contatoMore);
                                            }

                                            //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                            lastTimestamp = key;

                                            // Remove a última chave usada
                                            if (newContatos.size() > PAGE_SIZE) {
                                                newContatos.remove(0);
                                            }
                                            if (lastTimestamp != -1) {

                                                recuperaDadosUser(contatoMore.getIdContato(), new RecuperaUser() {
                                                    @Override
                                                    public void onRecuperado(Usuario dadosUser) {
                                                        ocultarProgress();
                                                        for (Contatos contato : newContatos) {
                                                            if (contato.getIdContato().equals(dadosUser.getIdUsuario())) {
                                                                newContatos.remove(contato);
                                                                contato.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                                                                newContatos.add(contato);
                                                                contadorNome++;
                                                            }
                                                            if (contadorNome == newContatos.size()) {
                                                                contadorNome = 0;
                                                                adicionarMaisDados(newContatos, contatoMore.getIdContato(), dadosUser, queryLoadMore);
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

                                        @Override
                                        public void onError(String message) {
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        ocultarProgress();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                                && idsAIgnorarListeners.contains(snapshot.getValue(Contatos.class).getIdContato())) {
                            ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Contatos.class).getIdContato(), getApplicationContext());
                            return;
                        }
                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                            return;
                        }
                        ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO CARREGAR + DADOS", getApplicationContext());
                        logicaAtualizacao(snapshot, false);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                        if (contatoRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(contatoRemovido.getIdContato())
                                || listaContatos != null && listaContatos.size() > 0
                                && listaContatos.get(0).getIdContato().equals(contatoRemovido.getIdContato())) {
                            return;
                        }

                        verificaExistencia(contatoRemovido.getIdContato(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Contatos contatoAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + contatoRemovido.getIdContato(), getApplicationContext());

                                logicaRemocao(contatoRemovido, true, true);

                                if (status) {
                                    boolean menorque = contatoAtualizado.getTimestampContato() <= listaContatos.get(0).getTimestampContato();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + contatoRemovido.getIdContato(), getApplicationContext());
                                        anexarNovoDado(contatoAtualizado);
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

    private void carregarMaisDadosFiltrados(String dadoAnterior, RecuperarIdsFiltroCallback callback) {
        if (isPesquisaAtivada() && listaFiltrada != null) {

            ToastCustomizado.toastCustomizadoCurto("PAGINACAO - LOAD:  " + isLoading, getApplicationContext());

            if (listaFiltrada.size() > 1
                    && idUltimoElementoFiltro != null && !idUltimoElementoFiltro.isEmpty()
                    && idsFiltrados != null && !idsFiltrados.isEmpty()
                    && idsFiltrados.contains(idUltimoElementoFiltro)) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA ONE " + idUltimoElementoFiltro, getApplicationContext());
                return;
            }

            //**ToastCustomizado.toastCustomizadoCurto("Last Name: " + lastName, requireContext());
            if (listaFiltrada != null && !listaFiltrada.isEmpty()
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMorePesquisa = firebaseRef.child("contatos_by_name")
                        .child(idUsuario)
                        .orderByChild("nomeUsuarioPesquisa")
                        .startAt(dadoAnterior).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
                queryLoadMorePesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        exibirProgress();
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                                Usuario usuarioPesquisa = snapshotChildren.getValue(Usuario.class);
                                if (usuarioPesquisa != null && usuarioPesquisa.getIdUsuario() != null
                                        && !usuarioPesquisa.getIdUsuario().isEmpty()
                                        && !usuarioPesquisa.getIdUsuario().equals(idUsuario)) {

                                    if (listenerFiltroHashMap != null && !listenerFiltroHashMap.isEmpty()
                                            && listenerFiltroHashMap.containsKey(usuarioPesquisa.getIdUsuario())) {
                                        ToastCustomizado.toastCustomizadoCurto("RETORNO PESQUISA IF " + usuarioPesquisa.getIdUsuario(), getApplicationContext());
                                        ocultarProgress();
                                        setLoading(false);
                                    } else {
                                        groupUtils.verificaSeEParticipante(idGrupo, usuarioPesquisa.getIdUsuario(), new GroupUtils.VerificaParticipanteCallback() {
                                            @Override
                                            public void onParticipante(boolean status) {
                                                if (!status) {
                                                    idsTempFiltro.add(usuarioPesquisa);
                                                }
                                                callback.onRecuperado(idsTempFiltro);
                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
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

    private void adicionarMaisDados(List<Contatos> newContatos, String idUser, Usuario dadosUser, Query queryAlvo) {
        if (newContatos != null && newContatos.size() >= 1) {
            contactDiffDAO.carregarMaisContato(newContatos, idsUsuarios);
            contactDiffDAO.adicionarIdAoSet(idsUsuarios, idUser);
            Collections.sort(listaContatos, contatoComparator);
            adapterSelection.updateContatoList(listaContatos, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    ocultarProgress();
                    adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerContatos, false);
                    setLoading(false);
                }
            });
        } else {
            ocultarProgress();
        }
    }

    private void adicionarMaisDadosFiltrados(List<Contatos> newContatos, String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (newContatos != null && !newContatos.isEmpty()) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    for (Contatos contatos : newContatos) {
                        if (contatos.getIdContato().equals(dadosUser.getIdUsuario())) {
                            newContatos.remove(contatos);
                            contatos.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                            newContatos.add(contatos);
                            contadorNome++;
                        }

                        if (contadorNome == newContatos.size()) {
                            contadorNome = 0;
                            contactDAOFiltrado.carregarMaisContato(newContatos, idsFiltrados);
                            contactDAOFiltrado.adicionarIdAoSet(idsFiltrados, idUser);

                            Collections.sort(listaFiltrada, contatoComparator);
                            adapterSelection.updateContatoList(listaFiltrada, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {
                                    ocultarProgress();
                                    adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerAlvo, false);
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

    private void dadoInicialFiltragem(String nome, int counter) {
        //*ToastCustomizado.toastCustomizadoCurto("Busca: " + nome, requireContext());

        exibirProgress();

        if (trocarQueryInicialFiltro) {
            queryInicialFind = firebaseRef.child("contatos_by_name")
                    .child(idUsuario)
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAfter(nome).endAt(nome + "\uf8ff").limitToFirst(1);
        } else {
            queryInicialFind = firebaseRef.child("contatos_by_name")
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
                    firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
                    firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
                    return;
                }

                if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                    ocultarProgress();
                    setLoading(false);
                    ToastCustomizado.toastCustomizadoCurto("Return listaFiltrada != null && listaFiltrada.size() >= 1", getApplicationContext());
                    firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
                    firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
                    return;
                }

                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Usuario usuarioPesquisa = snapshotChildren.getValue(Usuario.class);
                        if (usuarioPesquisa != null && usuarioPesquisa.getIdUsuario() != null
                                && !usuarioPesquisa.getIdUsuario().isEmpty()
                                && !usuarioPesquisa.getIdUsuario().equals(idUsuario)) {

                            groupUtils.verificaSeEParticipante(idGrupo, usuarioPesquisa.getIdUsuario(), new GroupUtils.VerificaParticipanteCallback() {
                                @Override
                                public void onParticipante(boolean status) {
                                    if (status) {
                                        ocultarProgress();
                                        trocarQueryInicialFiltro = true;
                                        dadoInicialFiltragem(nome, counter);
                                    } else {
                                        recuperaDadosUser(usuarioPesquisa.getIdUsuario(), new RecuperaUser() {
                                            @Override
                                            public void onRecuperado(Usuario usuarioAtual) {
                                                adicionarContatosFiltrado(usuarioAtual);
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

    private void adicionarContatosFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            String idContatosInicioFiltro = listaFiltrada.get(0).getIdContato();
            if (idContatosInicioFiltro.equals(dadosUser.getIdUsuario())) {
                ocultarProgress();
                setLoading(false);
                return;
            }
        }


        queryInicialFiltro = firebaseRef.child("contatos")
                .child(idUsuario)
                .orderByChild("idContato")
                .equalTo(dadosUser.getIdUsuario()).limitToFirst(1);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    Contatos contatoAtual = snapshot.getValue(Contatos.class);

                    if (contatoAtual != null) {
                        contatoAtual.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                        contatoAtual.setIndisponivel(dadosUser.isIndisponivel());
                    }

                    if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                        String idContatosInicioFiltro = listaFiltrada.get(0).getIdContato();
                        if (idContatosInicioFiltro.equals(dadosUser.getIdUsuario())) {
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }
                    }

                    lastName = dadosUser.getNomeUsuarioPesquisa();

                    contactDAOFiltrado.adicionarContato(contatoAtual);
                    contactDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());

                    Collections.sort(listaFiltrada, contatoComparator);
                    adapterSelection.updateContatoList(listaFiltrada, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            referenceFiltroHashMap.put(dadosUser.getIdUsuario(), queryInicialFiltro);
                            listenerFiltroHashMap.put(dadosUser.getIdUsuario(), childListenerInicioFiltro);
                            adicionarDadoDoUsuario(dadosUser, queryInicialFiltro, childListenerInicioFiltro, false);
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
                if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                        && idsAIgnorarListeners.contains(snapshot.getValue(Contatos.class).getIdContato())) {
                    ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Contatos.class).getIdContato(), getApplicationContext());
                    return;
                }
                if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                        && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                    return;
                }

                if (listenerHashMap != null && listenerHashMap.size() > 0
                        && listenerHashMap.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                    return;
                }

                ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO SEARCH INICIO", getApplicationContext());
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

    private void adicionarDadoDoUsuario(Usuario dadosUser, Query queryAlvo, ChildEventListener childEventListenerAlvo, boolean dadoModificado) {
        listaDadosUser.put(dadosUser.getIdUsuario(), dadosUser);

        if (childEventListenerAlvo == null || queryAlvo == null) {
            return;
        }

        if (dadoModificado) {
            adicionarListenerNEWDATA(dadosUser.getIdUsuario(), queryAlvo, childEventListenerAlvo);
            return;
        }

        if (!isPesquisaAtivada()) {
            adicionarListener(dadosUser.getIdUsuario(), queryAlvo, childEventListenerAlvo);
        }
    }

    private void anexarNovoDado(Contatos contatoModificado) {
        newDataRef = firebaseRef.child("contatos")
                .child(idUsuario).orderByChild("idContato")
                .equalTo(contatoModificado.getIdContato()).limitToFirst(1);
        idsAIgnorarListeners.add(contatoModificado.getIdContato());
        childEventListenerNewData = newDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Contatos contatoModificado = snapshot.getValue(Contatos.class);
                    if (contatoModificado == null) {
                        return;
                    }
                    String idUserContatos = contatoModificado.getIdContato();
                    adicionarContatos(contatoModificado, true);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    ToastCustomizado.toastCustomizadoCurto("Alterado pelo newdata", getApplicationContext());
                    logicaAtualizacao(snapshot, true);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                    ToastCustomizado.toastCustomizado("DELETE PELO NEW DATA", getApplicationContext());
                    if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                            && idsAIgnorarListeners.contains(contatoRemovido.getIdContato())) {
                        idsAIgnorarListeners.remove(contatoRemovido.getIdContato());
                    }
                    logicaRemocao(contatoRemovido, true, true);
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

    private void adicionarListener(String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (idsListeners != null && idsListeners.size() > 0
                && idsListeners.contains(idUser)) {
            return;
        }
        if (idsListeners != null) {
            idsListeners.add(idUser);
        }
        referenceHashMap.put(idUser, queryAlvo);
        listenerHashMap.put(idUser, childEventListenerAlvo);
    }

    private void adicionarListenerNEWDATA(String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (idsListenersNEWDATA != null && idsListenersNEWDATA.size() > 0
                && idsListenersNEWDATA.contains(idUser)) {
            return;
        }
        if (idsListenersNEWDATA != null) {
            idsListenersNEWDATA.add(idUser);
        }
        referenceHashMapNEWDATA.put(idUser, queryAlvo);
        listenerHashMapNEWDATA.put(idUser, childEventListenerAlvo);
    }

    private void recuperarDetalhes(Set<Usuario> listaIdsRecuperados) {
        for (Usuario usuarioPesquisa : listaIdsRecuperados) {
            aosFiltros++;
            if (aosFiltros > listaIdsRecuperados.size()) {
                aosFiltros = 0;
                return;
            }

            childListenerMoreFiltro = null;
            queryLoadMoreFiltro = null;

            queryLoadMoreFiltro = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("idContato").equalTo(usuarioPesquisa.getIdUsuario()).limitToFirst(1);

            //ToastCustomizado.toastCustomizadoCurto("AOS FILTROS: " + usuarioPesquisa.getIdUsuario(), requireContext());
            //ToastCustomizado.toastCustomizadoCurto("NR FILTROS: " + aosFiltros, requireContext());

            ChildEventListener childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Contatos contatoMore = snapshot.getValue(Contatos.class);
                    if (contatoMore != null
                            && contatoMore.getIdContato() != null
                            && !contatoMore.getIdContato().isEmpty()) {

                        //**ToastCustomizado.toastCustomizadoCurto("NEW PESQUISA: " + contatoMore.getIdUsuario(), requireContext());
                        Log.d(TAG, "Timestamp key: " + lastTimestamp);
                        Log.d(TAG, "id: " + contatoMore.getIdContato() + " time: " + contatoMore.getTimestampContato());
                        if (listaFiltrada != null && listaFiltrada.size() > 1 && idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(contatoMore.getIdContato())) {
                            Log.d(TAG, "Id já existia: " + contatoMore.getIdContato());
                            ToastCustomizado.toastCustomizadoCurto("ID JÁ EXISTIA " + contatoMore.getIdContato(), getApplicationContext());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        List<Contatos> newContatos = new ArrayList<>();
                        String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                        if (lastName != null && !lastName.isEmpty() && key != null
                                && !key.isEmpty()) {
                            if (!key.equals(lastName) || listaFiltrada.size() > 0 &&
                                    !contatoMore.getIdContato()
                                            .equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdContato())) {
                                newContatos.add(contatoMore);
                                //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                lastName = key;
                            }
                        }
                        // Remove a última chave usada
                        if (newContatos.size() > PAGE_SIZE) {
                            newContatos.remove(0);
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (aosFiltros >= listaIdsRecuperados.size()) {
                                aosFiltros = 0;
                            }
                            adicionarMaisDadosFiltrados(newContatos, contatoMore.getIdContato(), queryLoadMoreFiltro, childListenerMoreFiltro);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() == null) {
                        return;
                    }
                    Contatos contatoUpdate = snapshot.getValue(Contatos.class);

                    if (contatoUpdate == null) {
                        return;
                    }

                    if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                            && idsAIgnorarListeners.contains(snapshot.getValue(Contatos.class).getIdContato())) {
                        ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Contatos.class).getIdContato(), getApplicationContext());
                        return;
                    }
                    if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                            && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                        return;
                    }

                    if (listenerHashMap != null && listenerHashMap.size() > 0
                            && listenerHashMap.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                        return;
                    }

                    if (listaFiltrada != null && listaFiltrada.size() > 0
                            && contatoUpdate.getIdContato().equals(listaFiltrada.get(0).getIdContato())) {
                        return;
                    }

                    ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO SEARCH + DADOS " + contatoUpdate.getIdContato(), getApplicationContext());
                    logicaAtualizacao(snapshot, false);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                        if (contatoRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(contatoRemovido.getIdContato())
                                || listaContatos != null && listaContatos.size() > 0
                                && listaContatos.get(0).getIdContato().equals(contatoRemovido.getIdContato())) {
                            return;
                        }

                        verificaExistencia(contatoRemovido.getIdContato(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Contatos contatoAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + contatoRemovido.getIdContato(), getApplicationContext());

                                logicaRemocao(contatoRemovido, true, true);

                                if (status) {
                                    boolean menorque = contatoAtualizado.getTimestampContato() <= listaContatos.get(0).getTimestampContato();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + contatoRemovido.getIdContato(), getApplicationContext());
                                        anexarNovoDado(contatoAtualizado);
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
            listenerFiltroHashMap.put(usuarioPesquisa.getIdUsuario(), childListener);
            referenceFiltroHashMap.put(usuarioPesquisa.getIdUsuario(), queryLoadMoreFiltro);
        }
    }


    private void logicaRemocao(Contatos contatoRemovido, boolean ignorarVerificacao, boolean excluirDaLista) {

        if (contatoRemovido == null) {
            return;
        }

        DatabaseReference verificaExistenciaRef = firebaseRef.child("contatos")
                .child(idUsuario).child(contatoRemovido.getIdContato());
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || ignorarVerificacao) {
                    if (idsFiltrados != null && idsFiltrados.size() > 0
                            && idsFiltrados.contains(contatoRemovido.getIdContato())) {
                        if (listaFiltrada != null && listaFiltrada.size() > 0 && excluirDaLista) {
                            if (idsFiltrados != null && idsFiltrados.size() > 0) {
                                idsFiltrados.remove(contatoRemovido.getIdContato());
                            }
                            removerDaSelecao(contatoRemovido.getIdContato());
                            contactDAOFiltrado.removerContato(contatoRemovido);
                        }
                    }

                    if (idsUsuarios != null && idsUsuarios.size() > 0
                            && idsUsuarios.contains(contatoRemovido.getIdContato())) {
                        if (listaContatos != null && listaContatos.size() > 0 && excluirDaLista) {
                            if (idsUsuarios != null && idsUsuarios.size() > 0) {
                                idsUsuarios.remove(contatoRemovido.getIdContato());
                            }
                            removerDaSelecao(contatoRemovido.getIdContato());
                            contactDiffDAO.removerContato(contatoRemovido);
                        }
                    }

                    if (listaDadosUser != null && listaDadosUser.size() > 0 && excluirDaLista) {
                        listaDadosUser.remove(contatoRemovido.getIdContato());
                        int posicao = adapterSelection.findPositionInList(contatoRemovido.getIdContato());
                        if (posicao != -1) {
                            adapterSelection.notifyItemChanged(posicao);
                        }
                    }

                    if (isPesquisaAtivada() && listaFiltrada != null) {
                        adapterSelection.updateContatoList(listaFiltrada, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {

                            }
                        });
                    } else if (!isPesquisaAtivada() && listaContatos != null) {
                        adapterSelection.updateContatoList(listaContatos, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
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
            Contatos contatoAtualizado = snapshot.getValue(Contatos.class);

            ToastCustomizado.toastCustomizadoCurto("BORA ATUALIZAR", getApplicationContext());

            if (contatoAtualizado == null || contatoAtualizado.getIdContato() == null) {
                return;
            }

            posicaoChanged = adapterSelection.findPositionInList(contatoAtualizado.getIdContato());

            if (posicaoChanged == -1 && isPesquisaAtivada()) {
                //Não existe esse contato na lista filtrada mas existe na lista normal.
                posicaoChanged = findPositionInList(contatoAtualizado.getIdContato());
            }

            if (posicaoChanged != -1) {
                Contatos contatoAnterior = new Contatos();
                if (idsUsuarios != null && idsUsuarios.size() > 0
                        && idsUsuarios.contains(contatoAtualizado.getIdContato())) {
                    //Já existe um listener na listagem normal
                    if (isPesquisaAtivada()
                            && referenceFiltroHashMap != null
                            && !referenceFiltroHashMap.isEmpty()
                            && referenceFiltroHashMap.containsKey(contatoAtualizado.getIdContato())) {
                        contatoAnterior = listaFiltrada.get(posicaoChanged);
                    } else {
                        contatoAnterior = listaContatos.get(posicaoChanged);
                    }
                } else if (isPesquisaAtivada()
                        && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    //Somente existe um listener desse contato na listagem filtrada.
                    contatoAnterior = listaFiltrada.get(posicaoChanged);
                }
                ToastCustomizado.toastCustomizadoCurto("Alterado: " + contatoAnterior.getIdContato(), getApplicationContext());

                if (contatoAnterior.getTotalMensagens() != contatoAtualizado.getTotalMensagens()) {
                    atualizarPorPayload(contatoAtualizado, "totalMensagens");
                }

                if (contatoAnterior.isContatoFavorito() != contatoAtualizado.isContatoFavorito()) {
                    atualizarPorPayload(contatoAtualizado, "contatoFavorito");
                }

                if (contatoAnterior.getNivelAmizade() != null &&
                        !contatoAnterior.getNivelAmizade().equals(contatoAtualizado.getNivelAmizade())) {
                    atualizarPorPayload(contatoAtualizado, "nivelAmizade");
                }

                if (isPesquisaAtivada() && listaFiltrada != null) {

                    Collections.sort(listaFiltrada, contatoComparator);

                    adapterSelection.updateContatoList(listaFiltrada, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                } else if (!isPesquisaAtivada() && listaContatos != null) {

                    Collections.sort(listaContatos, contatoComparator);
                    adapterSelection.updateContatoList(listaContatos, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                }
            } else {
                ToastCustomizado.toastCustomizadoCurto("Hello code -1", getApplicationContext());
            }
            posicaoChanged = -1;
        }
    }

    private void atualizarPorPayload(Contatos contatoAtualizado, String tipoPayload) {
        ToastCustomizado.toastCustomizadoCurto(tipoPayload, getApplicationContext());

        int index = posicaoChanged;

        if (index != -1) {

            if (isPesquisaAtivada() && referenceFiltroHashMap != null
                    && !referenceFiltroHashMap.isEmpty()
                    && referenceFiltroHashMap.containsKey(contatoAtualizado.getIdContato())) {
                ToastCustomizado.toastCustomizadoCurto("CODE NOOOO", getApplicationContext());
                contactDAOFiltrado.atualizarContatoPorPayload(contatoAtualizado, tipoPayload, new ContactDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        adapterSelection.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
            if (idsUsuarios != null && idsUsuarios.size() > 0
                    && idsUsuarios.contains(contatoAtualizado.getIdContato())) {
                ToastCustomizado.toastCustomizadoCurto("CODE OK", getApplicationContext());
                contactDiffDAO.atualizarContatoPorPayload(contatoAtualizado, tipoPayload, new ContactDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        if (!isPesquisaAtivada()) {
                            adapterSelection.notifyItemChanged(index, bundleRecup);
                        } else {
                            idsParaAtualizar.put(contatoAtualizado.getIdContato(), bundleRecup);
                        }
                    }
                });
            }
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
        nomePesquisado = "";
        ocultarProgress();
        if (contactDAOFiltrado != null) {
            contactDAOFiltrado.limparListaContatos();
        }
        if (listaContatos != null && listaContatos.size() > 0) {

            setLoading(false);

            Collections.sort(listaContatos, contatoComparator);
            adapterSelection.updateContatoList(listaContatos, new AdapterUsersSelectionGroup.ListaAtualizadaCallback() {
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

    private void ultimoElemento(RecuperaUltimoElemento callback) {
        if (trocarQueryUltimo) {
            queryUltimoElemento = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("timestampContato")
                    .endAt(timeUltimo - 1)
                    .limitToLast(1);
        } else {
            queryUltimoElemento = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("timestampContato").limitToLast(1);
        }

        listenerUltimoElemento = queryUltimoElemento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    groupUtils.verificaSeEParticipante(idGrupo, snapshot1.getValue(Contatos.class).getIdContato(), new GroupUtils.VerificaParticipanteCallback() {
                        @Override
                        public void onParticipante(boolean status) {
                            if (status) {
                                trocarQueryUltimo = true;
                                timeUltimo = snapshot1.getValue(Contatos.class).getTimestampContato();
                                queryUltimoElemento.removeEventListener(listenerUltimoElemento);
                                ultimoElemento(callback);
                            } else {
                                idUltimoElemento = snapshot1.getValue(Contatos.class).getIdContato();
                                setLoading(false);
                                if (callback != null && listaContatos != null) {
                                    callback.onRecuperado();
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
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaContatos != null) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void ultimoElementoFiltro(String nome, RecuperaUltimoElemento callback) {

        if (trocarQueryUltimoFiltro) {
            queryUltimoElementoFiltro = firebaseRef.child("contatos_by_name")
                    .child(idUsuario)
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAfter(nome).endAt(nome + "\uf8ff").limitToLast(1);
        } else {
            queryUltimoElementoFiltro = firebaseRef.child("contatos_by_name")
                    .child(idUsuario)
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAt(nome).endAt(nome + "\uf8ff").limitToLast(1);
        }

        listenerUltimoElementoFiltro = queryUltimoElementoFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    groupUtils.verificaSeEParticipante(idGrupo, snapshot1.getValue(Usuario.class).getIdUsuario(), new GroupUtils.VerificaParticipanteCallback() {
                        @Override
                        public void onParticipante(boolean status) {
                            if (status) {
                                queryUltimoElementoFiltro.removeEventListener(listenerUltimoElementoFiltro);
                                trocarQueryUltimoFiltro = true;
                                ultimoElementoFiltro(nome, callback);
                            } else {
                                idUltimoElementoFiltro = snapshot1.getValue(Usuario.class).getIdUsuario();
                                setLoading(false);
                                if (callback != null && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                                    callback.onRecuperado();
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
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void limparActivity() {
        idsAIgnorarListeners.clear();
        firebaseUtils.removerQueryChildListener(newDataRef, childEventListenerNewData);
        firebaseUtils.removerQueryChildListener(queryInicial, childListenerInicio);
        firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
        firebaseUtils.removerQueryChildListener(queryLoadMore, childEventListenerContatos);
        firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
        firebaseUtils.removerQueryValueListener(queryUltimoElemento, listenerUltimoElemento);
        removeValueEventListener();
        removeValueEventListenerNEWDATA();
        removeValueEventListenerFiltro(null);
        if (contactDiffDAO != null) {
            contactDiffDAO.limparListaContatos();
        }
        if (listaDadosUser != null) {
            listaDadosUser.clear();
        }

        if (idsUsuarios != null) {
            idsUsuarios.clear();
        }
        if (listaFiltrada != null && listaFiltrada.size() > 0) {
            contactDAOFiltrado.limparListaContatos();
            idsFiltrados.clear();
        }
        setPesquisaAtivada(false);
        nomePesquisado = null;
        mCurrentPosition = -1;
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }

    public void removeValueEventListener() {
        if (listenerHashMap != null && referenceHashMap != null) {
            for (String userId : listenerHashMap.keySet()) {
                Query userRef = referenceHashMap.get(userId);
                ChildEventListener listener = listenerHashMap.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                    //**ToastCustomizado.toastCustomizadoCurto("Clear", requireContext());
                }
                contadorRemocaoListener++;
                if (contadorRemocaoListener == referenceHashMap.size()) {
                    referenceHashMap.clear();
                    listenerHashMap.clear();
                    ToastCustomizado.toastCustomizadoCurto("LIMPO", getApplicationContext());
                }
            }
        }
    }


    public void removeValueEventListenerNEWDATA() {
        if (listenerHashMapNEWDATA != null && referenceHashMapNEWDATA != null) {
            for (String userId : listenerHashMapNEWDATA.keySet()) {
                Query userRef = referenceHashMapNEWDATA.get(userId);
                ChildEventListener listener = listenerHashMapNEWDATA.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                    //**ToastCustomizado.toastCustomizadoCurto("Clear", requireContext());
                }
                contadorRemocaoListenerNEWDATA++;
                if (contadorRemocaoListenerNEWDATA == referenceHashMapNEWDATA.size()) {
                    referenceHashMapNEWDATA.clear();
                    listenerHashMapNEWDATA.clear();
                    ToastCustomizado.toastCustomizadoCurto("LIMPO NEW DATA", getApplicationContext());
                }
            }
        }
    }

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, AddGroupUsersActivity.this);
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, AddGroupUsersActivity.this);
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
    public void onRemocao(Contatos contatoAlvo, int posicao) {
        if (contatoAlvo != null) {
            logicaRemocao(contatoAlvo, false, true);
        }
    }

    @Override
    public void onExecutarAnimacao() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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

    public int findPositionInList(String userId) {
        for (int i = 0; i < listaContatos.size(); i++) {
            Contatos contato = listaContatos.get(i);
            if (contato.getIdContato().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
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
                            GroupUtils.ajustarDadoParaPesquisa(getApplicationContext(),
                                    dadosOperacao, idGrupo, idParticipante, new GroupUtils.AjustarDadoParaPesquisaCallback() {
                                        @Override
                                        public void onConcluido(HashMap<String, Object> dadosOperacao) {
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
                                                                Intent intent = new Intent(AddGroupUsersActivity.this, GroupDetailsActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                intent.putExtra("idGrupo", idGrupo);
                                                                intent.putExtra("chatComunidade", false);
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

    private boolean contemUltimoElemento() {
        for (Contatos contatoAtual : listaContatos) {
            if (idUltimoElemento.equals(contatoAtual.getIdContato())) {
                return true;
            }
        }
        return false;
    }
}