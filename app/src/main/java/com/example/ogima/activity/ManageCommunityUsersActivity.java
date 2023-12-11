package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterUsersSelectionCommunity;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
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
import java.util.Set;

public class ManageCommunityUsersActivity extends AppCompatActivity implements AdapterUsersSelectionCommunity.MarcarUsuarioCallback, AdapterUsersSelectionCommunity.DesmarcarUsuarioCallback {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private TextView txtViewLimiteSelecao;
    private Button btnSalvarGerenciamento;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isScrolling = false;
    private boolean primeiroCarregamento = true;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private Query queryInicial, queryLoadMore;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private long lastTimestamp = -1;
    private int queryDelayMillis = 500;
    private HashMap<String, DatabaseReference> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private AdapterUsersSelectionCommunity adapterSelection;
    private CommunityUtils communityUtils;
    private String idComunidade = "";
    private long limiteSelecao = 0;
    private int totalSelecionado = 0;
    private int operacoesExecutadas = 0;
    private boolean operacoesIgnoradas = false;
    private ProgressDialog progressDialog;
    private boolean operacaoConcluida = false;
    private boolean operacaoComErro = false;
    private String tipoGerenciamento = "";
    private boolean fundador = false;
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

    public ManageCommunityUsersActivity() {
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
        txtViewTitleToolbar.setText("Gerenciar participantes");
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        configBundle();
        recuperarComunidade(new RecuperarComunidadeCallback() {
            @Override
            public void onConcluido(Comunidade comunidadeRecuperada) {
                if (comunidadeRecuperada.getIdSuperAdmComunidade().equals(idUsuario)) {
                    fundador = true;
                } else {
                    fundador = false;
                }
                configurarLimite(comunidadeRecuperada);
                communityUtils = new CommunityUtils(getApplicationContext());
                progressDialog = new ProgressDialog(ManageCommunityUsersActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                configRecycler();
                usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterSelection);
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
        if (dados == null) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade", getApplicationContext());
            onBackPressed();
            return;
        }
        if (dados.containsKey("idComunidade")) {
            idComunidade = dados.getString("idComunidade");
        }
        if (dados.containsKey("tipoGerenciamento")) {
            tipoGerenciamento = dados.getString("tipoGerenciamento");
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

    private void limparPeloDestroyView() {
        removeValueEventListener();
        if (usuarioDiffDAO != null) {
            usuarioDiffDAO.limparListaUsuarios();
        }
        if (listaDadosUser != null) {
            listaDadosUser.clear();
        }
        if (idsUsuarios != null) {
            idsUsuarios.clear();
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
        if (trocarQueryInicial) {
            queryInicial = firebaseRef.child("communityFollowers")
                    .child(idComunidade).orderByChild("timestampinteracao")
                    .startAt(lastTimestamp + 1)
                    .limitToFirst(1);
        } else {
            queryInicial = firebaseRef.child("communityFollowers")
                    .child(idComunidade).orderByChild("timestampinteracao").limitToFirst(1);
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
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Comunidade comunidadeInicial = snapshot1.getValue(Comunidade.class);
                        if (comunidadeInicial != null && !comunidadeInicial.getIdParticipante().isEmpty()) {
                            if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                                adicionarUser(comunidadeInicial);
                                lastTimestamp = comunidadeInicial.getTimestampinteracao();
                            } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_PROMOTE)) {
                                if (!comunidadeInicial.isAdministrator()) {
                                    //Somente exibir usuários que não são adms.
                                    adicionarUser(comunidadeInicial);
                                    lastTimestamp = comunidadeInicial.getTimestampinteracao();
                                } else {
                                    lastTimestamp = comunidadeInicial.getTimestampinteracao();
                                    trocarQueryInicial = true;
                                    recuperarDadosIniciais();
                                }
                            } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_DEMOTING)) {
                                if (comunidadeInicial.isAdministrator()) {
                                    //Somente exibir usuários que são adms.
                                    adicionarUser(comunidadeInicial);
                                    lastTimestamp = comunidadeInicial.getTimestampinteracao();
                                } else {
                                    lastTimestamp = comunidadeInicial.getTimestampinteracao();
                                    trocarQueryInicial = true;
                                    recuperarDadosIniciais();
                                }
                            } else if (fundador || !comunidadeInicial.isAdministrator()) {
                                adicionarUser(comunidadeInicial);
                                lastTimestamp = comunidadeInicial.getTimestampinteracao();
                            } else {
                                lastTimestamp = comunidadeInicial.getTimestampinteracao();
                                trocarQueryInicial = true;
                                recuperarDadosIniciais();
                            }
                        }
                    }
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Não há participantes disponíveis a serem gerenciados pela função escolhida.", getApplicationContext());
                }
                queryInicial.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
            }
        });
    }

    private void adicionarUser(Comunidade participanteAlvo) {
        if (listaUsuarios != null && listaUsuarios.size() >= 1) {
            setLoading(false);
            return;
        }
        recuperaDadosUser(participanteAlvo.getIdParticipante(), new RecuperaUser() {
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
                                    carregarMaisDados();
                                }
                            }
                        }, 100);
                    }
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados() {
        queryLoadMore = firebaseRef.child("communityFollowers")
                .child(idComunidade)
                .orderByChild("timestampinteracao")
                .startAt(lastTimestamp)
                .limitToFirst(PAGE_SIZE);
        queryLoadMore.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Comunidade participanteChildren = snapshotChildren.getValue(Comunidade.class);
                        //**ToastCustomizado.toastCustomizadoCurto("SEM FILTRO " + usuarioChildren.getIdUsuario(), requireContext());
                        if (participanteChildren != null && participanteChildren.getIdParticipante() != null
                                && !participanteChildren.getIdParticipante().isEmpty()) {
                            if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                                List<Usuario> newParticipante = new ArrayList<>();
                                long key = participanteChildren.getTimestampinteracao();
                                if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                    Usuario usuarioNew = new Usuario();
                                    usuarioNew.setIdUsuario(participanteChildren.getIdParticipante());
                                    newParticipante.add(usuarioNew);
                                    lastTimestamp = key;
                                }
                                // Remove a última chave usada
                                if (newParticipante.size() > PAGE_SIZE) {
                                    newParticipante.remove(0);
                                }
                                if (lastTimestamp != -1) {
                                    adicionarMaisDados(newParticipante, participanteChildren.getIdParticipante());
                                }
                            } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_PROMOTE)) {
                                if (!participanteChildren.isAdministrator()) {
                                    //Somente exibir usuários que não são adms.
                                    List<Usuario> newParticipante = new ArrayList<>();
                                    long key = participanteChildren.getTimestampinteracao();
                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                        Usuario usuarioNew = new Usuario();
                                        usuarioNew.setIdUsuario(participanteChildren.getIdParticipante());
                                        newParticipante.add(usuarioNew);
                                        lastTimestamp = key;
                                    }
                                    // Remove a última chave usada
                                    if (newParticipante.size() > PAGE_SIZE) {
                                        newParticipante.remove(0);
                                    }
                                    if (lastTimestamp != -1) {
                                        adicionarMaisDados(newParticipante, participanteChildren.getIdParticipante());
                                    }
                                } else {
                                    long key = participanteChildren.getTimestampinteracao();
                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                        lastTimestamp = key;
                                    }
                                }
                            } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_DEMOTING)) {
                                if (participanteChildren.isAdministrator()) {
                                    //Somente exibir usuários que são adms.
                                    List<Usuario> newParticipante = new ArrayList<>();
                                    long key = participanteChildren.getTimestampinteracao();
                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                        Usuario usuarioNew = new Usuario();
                                        usuarioNew.setIdUsuario(participanteChildren.getIdParticipante());
                                        newParticipante.add(usuarioNew);
                                        lastTimestamp = key;
                                    }
                                    // Remove a última chave usada
                                    if (newParticipante.size() > PAGE_SIZE) {
                                        newParticipante.remove(0);
                                    }
                                    if (lastTimestamp != -1) {
                                        adicionarMaisDados(newParticipante, participanteChildren.getIdParticipante());
                                    }
                                } else {
                                    long key = participanteChildren.getTimestampinteracao();
                                    if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                        lastTimestamp = key;
                                    }
                                }
                            } else if (fundador || !participanteChildren.isAdministrator()) {
                                List<Usuario> newParticipante = new ArrayList<>();
                                long key = participanteChildren.getTimestampinteracao();
                                if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                    Usuario usuarioNew = new Usuario();
                                    usuarioNew.setIdUsuario(participanteChildren.getIdParticipante());
                                    newParticipante.add(usuarioNew);
                                    lastTimestamp = key;
                                }
                                // Remove a última chave usada
                                if (newParticipante.size() > PAGE_SIZE) {
                                    newParticipante.remove(0);
                                }
                                if (lastTimestamp != -1) {
                                    adicionarMaisDados(newParticipante, participanteChildren.getIdParticipante());
                                }
                            } else {
                                long key = participanteChildren.getTimestampinteracao();
                                if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                    lastTimestamp = key;
                                }
                            }
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

    private void adicionarMaisDados(List<Usuario> newParticipante, String idUser) {
        if (newParticipante != null && newParticipante.size() >= 1) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    usuarioDiffDAO.carregarMaisUsuario(newParticipante, idsUsuarios);
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

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, ManageCommunityUsersActivity.this);
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, ManageCommunityUsersActivity.this);
    }

    private void clickListeners() {
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

    private void realizarOperacoes(List<String> listaIds, int index) {
        operacoesExecutadas++;
        if (index < listaIds.size()) {
            String idSelecao = listaIds.get(index);
            communityUtils.verificaSeEParticipante(idComunidade, idSelecao, new CommunityUtils.VerificaParticipanteCallback() {
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
                communityUtils.sairDaComunidade(idComunidade, idAlvo, new CommunityUtils.SairDaComunidadeCallback() {
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
                communityUtils.promoverParaAdm(idComunidade, idAlvo, new CommunityUtils.PromoverAdmCallback() {
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
                communityUtils.despromoverAdm(idComunidade, idAlvo, new CommunityUtils.DespromoverAdmCallback() {
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
                communityUtils.transferirFundador(idComunidade, idAlvo, new CommunityUtils.TransferirFundadorCallback() {
                    @Override
                    public void onConcluido() {
                        verificaOperacao(callback);
                    }

                    @Override
                    public void onLimiteMaxAtingido() {
                        ToastCustomizado.toastCustomizadoCurto("Usuário selecionado já possui o número máximo de comunidade, escolha outro usuário.", getApplicationContext());
                    }

                    @Override
                    public void onNaoParticipante() {
                        ToastCustomizado.toastCustomizadoCurto("Usuário selecionado não faz mais parte dessa comunidade, escolha outro usuário.", getApplicationContext());
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao transferir seu cargo. Tente novamente.", getApplicationContext());
                    }
                });
                break;
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
        if (!ManageCommunityUsersActivity.this.isFinishing()) {
            progressDialog.show();
        }
    }


    public void ocultarProgressDialog() {
        if (progressDialog != null && !ManageCommunityUsersActivity.this.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
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
                    Intent intent = new Intent(ManageCommunityUsersActivity.this, ListaComunidadesActivityNEW.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return;
                }
                onBackPressed();
            } else if (callback != null) {
                callback.onConcluido();
            }
        }
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
        } else {
            totalSelecionado--;
        }
        txtViewLimiteSelecao.setText(String.format("%d%s%d", totalSelecionado, "/", getLimiteSelecao()));
    }

    private void configurarLimite(Comunidade comunidadeAlvo) {
        switch (tipoGerenciamento) {
            case CommunityUtils.FUNCTION_REMOVE:
                if (comunidadeAlvo.getIdSuperAdmComunidade().equals(idUsuario)) {
                    setLimiteSelecao(comunidadeAlvo.getNrParticipantes());
                } else {
                    setLimiteSelecao(comunidadeAlvo.getNrParticipantes() - 1);
                }
                break;
            case CommunityUtils.FUNCTION_PROMOTE:
                //PROBLEMA COM A LISTA POR ESTAR DENTRO DE UM FOR, AS VEZES ELE FUNCIONA BEM
                //AS VEZES NÃO, RESUMINDO POR CAUSA DO FOR AS VEZES ELE NÃO TEM TEMPO
                //DE ALTERAR O ARRAYLIST DA FORMA QUE EU DESEJAVA.
                if (comunidadeAlvo.getNrAdms() > 0) {
                    setLimiteSelecao(CommunityUtils.MAX_NUMBER_ADMS - comunidadeAlvo.getNrAdms());
                } else {
                    setLimiteSelecao(CommunityUtils.MAX_NUMBER_ADMS);
                }
                break;
            case CommunityUtils.FUNCTION_DEMOTING:
                //PROBLEMA COM A LISTA POR ESTAR DENTRO DE UM FOR, AS VEZES ELE FUNCIONA BEM
                //AS VEZES NÃO, RESUMINDO POR CAUSA DO FOR AS VEZES ELE NÃO TEM TEMPO
                //DE ALTERAR O ARRAYLIST DA FORMA QUE EU DESEJAVA.
                if (comunidadeAlvo.getNrAdms() > 0) {
                    setLimiteSelecao(comunidadeAlvo.getNrAdms());
                } else {
                    setLimiteSelecao(0);
                }
                break;
            case CommunityUtils.FUNCTION_NEW_FOUNDER:
                setLimiteSelecao(1);
                break;
        }
        txtViewLimiteSelecao.setText(String.format("%d%s%d", 0, "/", getLimiteSelecao()));
    }

    private void inicializarComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        spinProgress = findViewById(R.id.spinProgressBarRecycler);
        recyclerView = findViewById(R.id.recyclerViewAvailableUsers);
        txtViewLimiteSelecao = findViewById(R.id.txtViewLimiteGerenciamento);
        btnSalvarGerenciamento = findViewById(R.id.btnSalvarGerenciamento);
    }
}