package com.example.ogima.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.AddGroupUsersActivity;
import com.example.ogima.adapter.AdapterChatGroupList;
import com.example.ogima.helper.ChatDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Chat;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;
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


public class GroupListFragment extends Fragment implements AdapterChatGroupList.RecuperaPosicaoAnterior, AdapterChatGroupList.RemoverChatListener, AdapterChatGroupList.AnimacaoIntent {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private int mCurrentPosition = -1;
    private boolean isLoading = false;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Chat> listaChat = new ArrayList<>();
    private Set<String> idsGrupos = new HashSet<>();
    private ChatDiffDAO chatDiffDAO, chatDAOFiltrado;
    private Query queryInicial, queryLoadMore,
            queryInicialFiltro, queryLoadMorePesquisa, queryInicialFind,
            queryLoadMoreFiltro, newDataRef;
    private HashMap<String, Object> listaDadosGroup = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private String nomePesquisado = "";
    private List<Chat> listaFiltrada = new ArrayList<>();
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
            childEventListenerChat, childListenerMoreFiltro,
            childListenerInicio, childEventListenerNewData;
    private HashMap<String, Object> listaAmigos = new HashMap<>();
    private AdapterChatGroupList adapterChatList;
    private boolean trocarQueryInicial = false;
    private Chat chatComparator;
    private int contadorRemocaoListener = 0;
    private FirebaseUtils firebaseUtils;
    private int travar = 0;

    private Set<String> idsListenersNEWDATA = new HashSet<>();
    private HashMap<String, Query> referenceHashMapNEWDATA = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapNEWDATA = new HashMap<>();
    private int contadorRemocaoListenerNEWDATA = 0;
    private static final String TAG = "CHATGRUPOtag";
    private String idPrimeiroDado = "";
    private Set<String> idsAIgnorarListeners = new HashSet<>();
    private String idUltimoElemento, idUltimoElementoFiltro;
    private Query queryUltimoElemento, queryUltimoElementoFiltro;
    private ValueEventListener listenerUltimoElemento, listenerUltimoElementoFiltro;
    private int controleRemocao = 0;
    private Set<Grupo> idsTempFiltro = new HashSet<>();
    private int aosFiltros = 0;
    private int posicaoChanged = -1;
    private HashMap<String, Bundle> idsParaAtualizar = new HashMap<>();
    private int contadorUpdate = 0;

    private Snackbar snackbarLimiteGrupo;
    private final static int MAX_MY_GROUPS = 5;
    private ImageButton imgButtonCadastroGrupo;

    private interface VerificaExistenciaCallback {
        void onExistencia(boolean status, Chat chatAtualizado);

        void onError(String message);
    }

    private interface RecuperaUltimoElemento {
        void onRecuperado();
    }

    private interface RemoverListenersCallback {
        void onRemovido();
    }

    private interface RecuperarIdsFiltroCallback {
        void onRecuperado(Set<Grupo> listaIdsRecuperados);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterChatList != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                listaChat != null && listaChat.size() > 0
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
    public void onDestroyView() {
        super.onDestroyView();
        limparPeloDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    private interface RecuperaGroup {
        void onRecuperado(Grupo grupoAtual);

        void onSemDado();

        void onError(String message);
    }

    private interface VerificaCriterio {
        void onCriterioAtendido();

        void onSemVinculo();

        void onError(String message);
    }

    public GroupListFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        chatComparator = new Chat();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_grupo, container, false);
        inicializarComponentes(view);
        configInicial();
        clickListeners();
        return view;
    }

    private void configInicial() {
        firebaseUtils = new FirebaseUtils();
        txtViewTitle.setText(FormatarContadorUtils.abreviarTexto("Chats em grupo", 20));
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), requireContext());
            requireActivity().onBackPressed();
            return;
        }
        setLoading(true);
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean epilepsia) {
                setPesquisaAtivada(false);
                configRecycler(epilepsia);
                configSearchView();
                chatDiffDAO = new ChatDiffDAO(listaChat, adapterChatList);
                chatDAOFiltrado = new ChatDiffDAO(listaFiltrada, adapterChatList);
                setLoading(true);
                recuperarDadosIniciais();
                configPaginacao();
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), requireContext());
                requireActivity().onBackPressed();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizado(String.format("%s%s %s", getString(R.string.error_retrieving_user_data), ":", message), requireContext());
                requireActivity().onBackPressed();
            }
        });
    }

    private void clickListeners() {
        imgButtonCadastroGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cadastrarGrupo();
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
                                        chatDAOFiltrado.limparListaChats();
                                        adapterChatList.updateChatList(listaFiltrada, new AdapterChatGroupList.ListaAtualizadaCallback() {
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
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapterChatList = new AdapterChatGroupList(requireContext(),
                    listaChat, listaDadosGroup,
                    getResources().getColor(R.color.chat_list_color), this, this, this);
            recyclerView.setAdapter(adapterChatList);
            adapterChatList.setStatusEpilepsia(epilepsia);
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
                                    if (isPesquisaAtivada()) {
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
        if (listaChat != null && listaChat.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }

        ultimoElemento(new RecuperaUltimoElemento() {
            @Override
            public void onRecuperado() {
                if (trocarQueryInicial && lastTimestamp != -1) {
                    queryInicial = firebaseRef.child("detalhesChatGrupo")
                            .child(idUsuario).orderByChild("timestampLastMsg")
                            .startAt(lastTimestamp + 1)
                            .limitToFirst(1);
                } else {
                    queryInicial = firebaseRef.child("detalhesChatGrupo")
                            .child(idUsuario).orderByChild("timestampLastMsg").limitToFirst(1);
                }
                exibirProgress();
                childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Chat chat = snapshot.getValue(Chat.class);
                            if (chat != null
                                    && chat.getIdGrupo() != null
                                    && !chat.getIdGrupo().isEmpty()) {
                                idPrimeiroDado = chat.getIdGrupo();
                                if (travar == 0) {
                                    lastTimestamp = chat.getTimestampLastMsg();
                                    adicionarChat(chat, false);
                                } else {
                                    //Dado mais recente que o anterior
                                    if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                            && listenerHashMapNEWDATA.containsKey(chat.getIdGrupo())) {
                                        return;
                                    }
                                    ToastCustomizado.toastCustomizadoCurto("Novo dado pelo inicio " + chat.getIdGrupo(), requireContext());
                                    anexarNovoDado(chat);
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
                            if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdGrupo())) {
                                return;
                            }
                            ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO INICIO", requireContext());
                            logicaAtualizacao(snapshot, false);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Chat chatRemovido = snapshot.getValue(Chat.class);
                            if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdGrupo())) {
                                //O próprio listenernewdata vai cuidar da remoção desse dado.
                                return;
                            }

                            ToastCustomizado.toastCustomizado("DELETE INICIO", requireContext());
                            logicaRemocao(chatRemovido, true, true);

                            verificaExistencia(chatRemovido.getIdGrupo(), new VerificaExistenciaCallback() {
                                @Override
                                public void onExistencia(boolean status, Chat chatAtualizado) {
                                    if (status) {
                                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                                && listenerHashMapNEWDATA.containsKey(chatRemovido.getIdGrupo())) {
                                        } else {
                                            ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do inicio " + chatRemovido.getIdGrupo(), requireContext());
                                            anexarNovoDado(chatAtualizado);
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
                        ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao recuperar as suas conversas", "Code:", error.getCode()), requireContext());
                        requireActivity().onBackPressed();
                    }
                });
            }
        });
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        //*ToastCustomizado.toastCustomizadoCurto("Busca: " + nome, requireContext());

        exibirProgress();
        queryInicialFind = firebaseRef.child("group_chats_by_name")
                .child(idUsuario)
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(1);

        queryInicialFind.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (counter != searchCounter) {
                    ocultarProgress();
                    setLoading(false);
                    ToastCustomizado.toastCustomizadoCurto("Return counter != searchCounter", requireContext());
                    firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
                    firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
                    return;
                }

                if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                    ocultarProgress();
                    setLoading(false);
                    ToastCustomizado.toastCustomizadoCurto("Return listaFiltrada != null && listaFiltrada.size() >= 1", requireContext());
                    firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
                    firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
                    return;
                }

                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Grupo grupoPesquisa = snapshotChildren.getValue(Grupo.class);
                        if (grupoPesquisa != null && grupoPesquisa.getIdGrupo() != null
                                && !grupoPesquisa.getIdGrupo().isEmpty()
                                && !grupoPesquisa.getIdGrupo().equals(idUsuario)) {
                            recuperaDadosGrupo(grupoPesquisa.getIdGrupo(), new RecuperaGroup() {
                                @Override
                                public void onRecuperado(Grupo grupoAtual) {
                                    adicionarChatFiltrado(grupoAtual);
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

    private void adicionarChat(Chat chatAlvo, boolean dadoModificado) {
        recuperaDadosGrupo(chatAlvo.getIdGrupo(), new RecuperaGroup() {
            @Override
            public void onRecuperado(Grupo dadosGrupo) {

                ToastCustomizado.toastCustomizadoCurto("Grupo: " + dadosGrupo.getIdGrupo(), requireContext());

                chatDiffDAO.adicionarChat(chatAlvo);
                chatDiffDAO.adicionarIdAoSet(idsGrupos, dadosGrupo.getIdGrupo());

                List<Chat> listaAtual = new ArrayList<>();
                if (isPesquisaAtivada()) {
                    listaAtual = listaFiltrada;
                } else {
                    listaAtual = listaChat;
                    Collections.sort(listaChat, chatComparator);
                }

                adapterChatList.updateChatList(listaAtual, new AdapterChatGroupList.ListaAtualizadaCallback() {
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
                                //*ToastCustomizado.toastCustomizadoCurto("SCROLL", requireContext());
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
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar suas conversas.", requireContext());
            }
        });
    }

    private void adicionarChatFiltrado(Grupo dadosGroup) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            String idChatInicioFiltro = listaFiltrada.get(0).getIdGrupo();
            if (idChatInicioFiltro.equals(dadosGroup.getIdGrupo())) {
                ocultarProgress();
                setLoading(false);
                return;
            }
        }


        queryInicialFiltro = firebaseRef.child("detalhesChatGrupo")
                .child(idUsuario)
                .orderByChild("idUsuario")
                .equalTo(dadosGroup.getIdGrupo()).limitToFirst(1);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    Chat chatAtual = snapshot.getValue(Chat.class);

                    if (chatAtual != null) {
                        chatAtual.setIndisponivel(dadosGroup.isIndisponivel());
                    }

                    if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                        String idChatInicioFiltro = listaFiltrada.get(0).getIdGrupo();
                        if (idChatInicioFiltro.equals(dadosGroup.getIdGrupo())) {
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }
                    }

                    lastName = dadosGroup.getNomeUsuarioPesquisa();

                    chatDAOFiltrado.adicionarChat(chatAtual);
                    chatDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosGroup.getIdGrupo());
                    adapterChatList.updateChatList(listaFiltrada, new AdapterChatGroupList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            referenceFiltroHashMap.put(dadosGroup.getIdGrupo(), queryInicialFiltro);
                            listenerFiltroHashMap.put(dadosGroup.getIdGrupo(), childListenerInicioFiltro);
                            adicionarDadoDoGrupo(dadosGroup, queryInicialFiltro, childListenerInicioFiltro, false);
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
                        && idsAIgnorarListeners.contains(snapshot.getValue(Chat.class).getIdGrupo())) {
                    ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Chat.class).getIdGrupo(), requireContext());
                    return;
                }
                if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                        && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdGrupo())) {
                    return;
                }

                if (listenerHashMap != null && listenerHashMap.size() > 0
                        && listenerHashMap.containsKey(snapshot.getValue(Chat.class).getIdGrupo())) {
                    return;
                }

                ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO SEARCH INICIO", requireContext());
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
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao realizar a pesquisa.", requireContext());
            }
        });
    }

    private void carregarMaisDadosFiltrados(String dadoAnterior, RecuperarIdsFiltroCallback callback) {
        if (isPesquisaAtivada() && listaFiltrada != null) {

            ToastCustomizado.toastCustomizadoCurto("PAGINACAO - LOAD:  " + isLoading, requireContext());

            if (listaFiltrada.size() > 1
                    && idUltimoElementoFiltro != null && !idUltimoElementoFiltro.isEmpty()
                    && idsFiltrados != null && !idsFiltrados.isEmpty()
                    && idsFiltrados.contains(idUltimoElementoFiltro)) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA ONE " + idUltimoElementoFiltro, requireContext());
                return;
            }

            //**ToastCustomizado.toastCustomizadoCurto("Last Name: " + lastName, requireContext());
            if (listaFiltrada != null && !listaFiltrada.isEmpty()
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMorePesquisa = firebaseRef.child("group_chats_by_name")
                        .child(idUsuario)
                        .orderByChild("nomeUsuarioPesquisa")
                        .startAt(dadoAnterior).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
                queryLoadMorePesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        exibirProgress();
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                                Grupo grupoPesquisa = snapshotChildren.getValue(Grupo.class);
                                if (grupoPesquisa != null && grupoPesquisa.getIdGrupo() != null
                                        && !grupoPesquisa.getIdGrupo().isEmpty()
                                        && !grupoPesquisa.getIdGrupo().equals(idUsuario)) {

                                    if (listenerFiltroHashMap != null && !listenerFiltroHashMap.isEmpty()
                                            && listenerFiltroHashMap.containsKey(grupoPesquisa.getIdGrupo())) {
                                        ToastCustomizado.toastCustomizadoCurto("RETORNO PESQUISA IF " + grupoPesquisa.getIdGrupo(), requireContext());
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
            if (listaChat.size() > 1
                    && idUltimoElemento != null && !idUltimoElemento.isEmpty()
                    && idsGrupos != null && !idsGrupos.isEmpty()
                    && idsGrupos.contains(idUltimoElemento)) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA CHAT " + idUltimoElemento, requireContext());
                //NO LUGAR DE COMPARAR COM O ÚLTIMO ITEM ADICIONADO NA LISTA
                //O CORRETO É TER UM MÉTODO SEPARADO QUE PEGA O ÚLTIMO ELEMENTO
                //NO SERVIDOR COM O LISTENER ATIVO SEMPRE ASSIM EU COMPARO COM O ID
                //DESSE DADO, ASSIM EU TENHO COMO SABER QUANDO A PAGINAÇÃO NÃO DEVE CONTINUAR
                //JÁ QUE SABERIA QUE A LISTA JÁ PEGOU TODOS OS DADOS POSSÍVEIS DO SERVIDOR
                //E NÃO TERIA MAIS DADOS ALÉM DELE, POSSO COLOCAR ESSA LÓGICA NO SCROLLISTENER
                //E RETIRAR ESSE CÓDIGO ANTERIOR DO MÉTODO CARREGARMAISDADOS.
                return;
            }

            queryLoadMore = firebaseRef.child("detalhesChatGrupo")
                    .child(idUsuario)
                    .orderByChild("timestampLastMsg")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);
            childEventListenerChat = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    exibirProgress();
                    if (snapshot.getValue() != null) {
                        Chat chatMore = snapshot.getValue(Chat.class);
                        if (chatMore != null
                                && chatMore.getIdGrupo() != null
                                && !chatMore.getIdGrupo().isEmpty()) {
                            Log.d(TAG, "Timestamp key: " + lastTimestamp);
                            Log.d(TAG, "id: " + chatMore.getIdGrupo() + " time: " + chatMore.getTimestampLastMsg());
                            if (listaChat != null && listaChat.size() > 1 && idsGrupos != null && idsGrupos.size() > 0
                                    && idsGrupos.contains(chatMore.getIdGrupo())) {
                                Log.d(TAG, "Id já existia: " + chatMore.getIdGrupo());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            if (listaChat != null && listaChat.size() > 1
                                    && chatMore.getTimestampLastMsg() < listaChat.get(0).getTimestampLastMsg()) {
                                ToastCustomizado.toastCustomizadoCurto("TIME IGNORADO", requireContext());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            //*ToastCustomizado.toastCustomizadoCurto("ADICIONADO " + chatMore.getIdGrupo(), requireContext());
                            List<Chat> newChat = new ArrayList<>();
                            long key = chatMore.getTimestampLastMsg();
                            if (lastTimestamp != -1 && key != -1) {
                                if (key != lastTimestamp || listaChat.size() > 0 &&
                                        !chatMore.getIdGrupo()
                                                .equals(listaChat.get(listaChat.size() - 1).getIdGrupo())) {
                                    newChat.add(chatMore);
                                    //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                    lastTimestamp = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newChat.size() > PAGE_SIZE) {
                                newChat.remove(0);
                            }
                            if (lastTimestamp != -1) {
                                adicionarMaisDados(newChat, chatMore.getIdGrupo(), queryLoadMore);
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
                                && idsAIgnorarListeners.contains(snapshot.getValue(Chat.class).getIdGrupo())) {
                            ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Chat.class).getIdGrupo(), requireContext());
                            return;
                        }
                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdGrupo())) {
                            return;
                        }
                        ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO CARREGAR + DADOS", requireContext());
                        logicaAtualizacao(snapshot, false);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Chat chatRemovido = snapshot.getValue(Chat.class);
                        if (chatRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(chatRemovido.getIdGrupo())
                                || listaChat != null && listaChat.size() > 0
                                && listaChat.get(0).getIdGrupo().equals(chatRemovido.getIdGrupo())) {
                            return;
                        }

                        verificaExistencia(chatRemovido.getIdGrupo(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Chat chatAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + chatRemovido.getIdGrupo(), requireContext());

                                logicaRemocao(chatRemovido, true, true);

                                if (status) {
                                    boolean menorque = chatAtualizado.getTimestampLastMsg() <= listaChat.get(0).getTimestampLastMsg();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + chatRemovido.getIdGrupo(), requireContext());
                                        anexarNovoDado(chatAtualizado);
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

    private void adicionarMaisDados(List<Chat> newChat, String idGroup, Query queryAlvo) {
        if (newChat != null && newChat.size() >= 1) {
            recuperaDadosGrupo(idGroup, new RecuperaGroup() {
                @Override
                public void onRecuperado(Grupo dadosGroup) {
                    chatDiffDAO.carregarMaisChat(newChat, idsGrupos);
                    chatDiffDAO.adicionarIdAoSet(idsGrupos, idGroup);

                    Collections.sort(listaChat, chatComparator);
                    adapterChatList.updateChatList(listaChat, new AdapterChatGroupList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadoDoGrupo(dadosGroup, queryAlvo, childEventListenerChat, false);
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
        }
    }


    private void adicionarMaisDadosFiltrados(List<Chat> newChat, String idGroup, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (newChat != null && !newChat.isEmpty()) {
            recuperaDadosGrupo(idGroup, new RecuperaGroup() {
                @Override
                public void onRecuperado(Grupo dadosGroup) {
                    chatDAOFiltrado.carregarMaisChat(newChat, idsFiltrados);
                    chatDAOFiltrado.adicionarIdAoSet(idsFiltrados, idGroup);
                    adapterChatList.updateChatList(listaFiltrada, new AdapterChatGroupList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadoDoGrupo(dadosGroup, queryAlvo, childEventListenerAlvo, false);
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
        }
    }

    private void recuperaDadosGrupo(String idGroup, RecuperaGroup callback) {
        FirebaseRecuperarUsuario.recoverGroup(idGroup, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                GroupUtils.checkBlockingStatus(requireContext(), idGroup, new GroupUtils.CheckLockCallback() {
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
            public void onNaoExiste() {
                callback.onSemDado();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private void adicionarDadoDoGrupo(Grupo dadosGroup, Query queryAlvo, ChildEventListener childEventListenerAlvo, boolean dadoModificado) {
        listaDadosGroup.put(dadosGroup.getIdGrupo(), dadosGroup);

        if (childEventListenerAlvo == null || queryAlvo == null) {
            return;
        }

        if (dadoModificado) {
            adicionarListenerNEWDATA(dadosGroup.getIdGrupo(), queryAlvo, childEventListenerAlvo);
            return;
        }

        if (!isPesquisaAtivada()) {
            adicionarListener(dadosGroup.getIdGrupo(), queryAlvo, childEventListenerAlvo);
        }
    }

    private void adicionarListener(String idGroup, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (idsListeners != null && idsListeners.size() > 0
                && idsListeners.contains(idGroup)) {
            return;
        }
        if (idsListeners != null) {
            idsListeners.add(idGroup);
        }
        referenceHashMap.put(idGroup, queryAlvo);
        listenerHashMap.put(idGroup, childEventListenerAlvo);
    }

    private void adicionarListenerNEWDATA(String idGroup, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (idsListenersNEWDATA != null && idsListenersNEWDATA.size() > 0
                && idsListenersNEWDATA.contains(idGroup)) {
            return;
        }
        if (idsListenersNEWDATA != null) {
            idsListenersNEWDATA.add(idGroup);
        }
        referenceHashMapNEWDATA.put(idGroup, queryAlvo);
        listenerHashMapNEWDATA.put(idGroup, childEventListenerAlvo);
    }

    private void adicionarListenerFiltragem(String idGroup, Query queryAlvo, ChildEventListener childEventListenerAlvo) {

        if (listenerFiltroHashMap == null || referenceFiltroHashMap == null) {
            return;
        }

        if (!listenerFiltroHashMap.isEmpty()
                && listenerFiltroHashMap.containsKey(idGroup)) {
            return;
        }

        ToastCustomizado.toastCustomizadoCurto("Listener add: " + idGroup, requireContext());

        referenceFiltroHashMap.put(idGroup, queryAlvo);
        listenerFiltroHashMap.put(idGroup, childEventListenerAlvo);
    }

    private void limparPeloDestroyView() {
        idsAIgnorarListeners.clear();
        firebaseUtils.removerQueryChildListener(newDataRef, childEventListenerNewData);
        firebaseUtils.removerQueryChildListener(queryInicial, childListenerInicio);
        firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
        firebaseUtils.removerQueryChildListener(queryLoadMore, childEventListenerChat);
        firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
        firebaseUtils.removerQueryValueListener(queryUltimoElemento, listenerUltimoElemento);
        removeValueEventListener();
        removeValueEventListenerNEWDATA();
        removeValueEventListenerFiltro(null);
        if (chatDiffDAO != null) {
            chatDiffDAO.limparListaChats();
        }
        if (listaDadosGroup != null) {
            listaDadosGroup.clear();
        }
        if (listaAmigos != null) {
            listaAmigos.clear();
        }
        if (idsGrupos != null) {
            idsGrupos.clear();
        }
        if (listaFiltrada != null && listaFiltrada.size() > 0) {
            chatDAOFiltrado.limparListaChats();
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
        /*
        //Comentado por causa de crash que acontecia quando se utilizava o search por submit.
        if (searchView != null) {
            searchView.setQuery("", false);
        }
         */
        if (searchView != null && fecharTeclado) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        if (chatDAOFiltrado != null) {
            chatDAOFiltrado.limparListaChats();
        }
        if (listaChat != null && listaChat.size() > 0) {

            setLoading(false);

            Collections.sort(listaChat, chatComparator);
            adapterChatList.updateChatList(listaChat, new AdapterChatGroupList.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                    contadorUpdate++;
                    if (idsParaAtualizar != null && !idsParaAtualizar.isEmpty()) {
                        for (String idUpdate : idsParaAtualizar.keySet()) {
                            int index = adapterChatList.findPositionInList(idUpdate);
                            Bundle bundleUpdate = idsParaAtualizar.get(idUpdate);
                            if (index != -1) {
                                adapterChatList.notifyItemChanged(index, bundleUpdate);
                                ToastCustomizado.toastCustomizadoCurto("CODE NOTIFY", requireContext());
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
                    ToastCustomizado.toastCustomizadoCurto("LIMPO", requireContext());
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
                    ToastCustomizado.toastCustomizadoCurto("LIMPO NEW DATA", requireContext());
                }
            }
        }
    }

    private void removeValueEventListenerFiltro(RemoverListenersCallback callback) {
        if (listenerFiltroHashMap != null && referenceFiltroHashMap != null
                && !listenerFiltroHashMap.isEmpty() && !referenceFiltroHashMap.isEmpty()) {

            for (String userId : listenerFiltroHashMap.keySet()) {
                Query userRef = referenceFiltroHashMap.get(userId);
                ChildEventListener listener = listenerFiltroHashMap.get(userId);
                if (userRef != null && listener != null) {
                    ToastCustomizado.toastCustomizado("ListenerRemovido: " + userId, requireContext());
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

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, requireActivity());
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, requireActivity());
    }

    private void logicaRemocao(Chat chatRemovido, boolean ignorarVerificacao, boolean excluirDaLista) {

        if (chatRemovido == null) {
            return;
        }


        /*
        if (idsFiltrados != null && idsFiltrados.size() > 0
                && idsFiltrados.contains(chatRemovido.getIdGrupo())) {
            if (referenceFiltroHashMap != null && referenceFiltroHashMap.size() > 0
                    && listenerFiltroHashMap != null && listenerFiltroHashMap.size() > 0) {
                referenceFiltroHashMap.remove(chatRemovido.getIdGrupo());
                listenerFiltroHashMap.remove(chatRemovido.getIdGrupo());
                if (idsListeners != null && idsListeners.size() > 0) {
                    idsListeners.remove(chatRemovido.getIdGrupo());
                }
            }
        }

         */

        if (idsGrupos != null && idsGrupos.size() > 0
                && idsGrupos.contains(chatRemovido.getIdGrupo())) {
            /*
            if (listenerHashMap != null && referenceHashMap != null) {
                Query userRef = referenceHashMap.get(chatRemovido.getIdGrupo());
                ChildEventListener listener = listenerHashMap.get(chatRemovido.getIdGrupo());
                if (userRef != null && listener != null) {
                    ToastCustomizado.toastCustomizado("LISTENER REMOVIDO + DADOS " + chatRemovido.getIdGrupo(), requireContext());
                    userRef.removeEventListener(listener);
                }
            }


                 if (referenceHashMap != null && referenceHashMap.size() > 0
                    && listenerHashMap != null && listenerHashMap.size() > 0) {
                referenceHashMap.remove(chatRemovido.getIdGrupo());
                listenerHashMap.remove(chatRemovido.getIdGrupo());
                if (idsListeners != null && idsListeners.size() > 0) {
                    idsListeners.remove(chatRemovido.getIdGrupo());
                }
            }

             */


        }

     /*
        if (listenerHashMapNEWDATA != null && referenceHashMapNEWDATA != null) {
            if (idsListenersNEWDATA != null && idsListenersNEWDATA.size() > 0) {
                idsListenersNEWDATA.remove(chatRemovido.getIdGrupo());
            }
            Query userRef = referenceHashMapNEWDATA.get(chatRemovido.getIdGrupo());
            ChildEventListener listener = listenerHashMapNEWDATA.get(chatRemovido.getIdGrupo());
            if (userRef != null && listener != null) {
                ToastCustomizado.toastCustomizado("LISTENER REMOVIDO NEW DATA", requireContext());
                userRef.removeEventListener(listener);
            }
        }


        if (referenceHashMapNEWDATA != null && referenceHashMapNEWDATA.size() > 0
                && listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0) {
            referenceHashMapNEWDATA.remove(chatRemovido.getIdGrupo());
            listenerHashMapNEWDATA.remove(chatRemovido.getIdGrupo());
            if (idsListenersNEWDATA != null && idsListenersNEWDATA.size() > 0) {
                idsListenersNEWDATA.remove(chatRemovido.getIdGrupo());
            }
        }
         */

        DatabaseReference verificaExistenciaRef = firebaseRef.child("detalhesChatGrupo")
                .child(idUsuario).child(chatRemovido.getIdGrupo());
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || ignorarVerificacao) {

                    if (idsFiltrados != null && idsFiltrados.size() > 0
                            && idsFiltrados.contains(chatRemovido.getIdGrupo())) {
                        if (listaFiltrada != null && listaFiltrada.size() > 0 && excluirDaLista) {
                            if (idsFiltrados != null && idsFiltrados.size() > 0) {
                                idsFiltrados.remove(chatRemovido.getIdGrupo());
                            }
                            chatDAOFiltrado.removerChat(chatRemovido);
                        }
                    }

                    if (idsGrupos != null && idsGrupos.size() > 0
                            && idsGrupos.contains(chatRemovido.getIdGrupo())) {
                        if (listaChat != null && listaChat.size() > 0 && excluirDaLista) {
                            if (idsGrupos != null && idsGrupos.size() > 0) {
                                idsGrupos.remove(chatRemovido.getIdGrupo());
                            }
                            chatDiffDAO.removerChat(chatRemovido);
                        }
                    }

                    if (listaDadosGroup != null && listaDadosGroup.size() > 0 && excluirDaLista) {
                        listaDadosGroup.remove(chatRemovido.getIdGrupo());
                        int posicao = adapterChatList.findPositionInList(chatRemovido.getIdGrupo());
                        if (posicao != -1) {
                            adapterChatList.notifyItemChanged(posicao);
                        }
                    }

                    if (isPesquisaAtivada() && listaFiltrada != null) {
                        adapterChatList.updateChatList(listaFiltrada, new AdapterChatGroupList.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {

                            }
                        });
                    } else if (!isPesquisaAtivada() && listaChat != null) {
                        adapterChatList.updateChatList(listaChat, new AdapterChatGroupList.ListaAtualizadaCallback() {
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
            Chat chatAtualizado = snapshot.getValue(Chat.class);
            if (chatAtualizado == null || chatAtualizado.getIdGrupo() == null) {
                return;
            }

            posicaoChanged = adapterChatList.findPositionInList(chatAtualizado.getIdGrupo());

            if (posicaoChanged == -1 && isPesquisaAtivada()) {
                //Não existe esse chat na lista filtrada mas existe na lista normal.
                posicaoChanged = findPositionInList(chatAtualizado.getIdGrupo());
            }

            if (posicaoChanged != -1) {
                Chat chatAnterior = new Chat();
                if (idsGrupos != null && idsGrupos.size() > 0
                        && idsGrupos.contains(chatAtualizado.getIdGrupo())) {
                    //Já existe um listener na listagem normal
                    if (isPesquisaAtivada()
                            && referenceFiltroHashMap != null
                            && !referenceFiltroHashMap.isEmpty()
                            && referenceFiltroHashMap.containsKey(chatAtualizado.getIdGrupo())) {
                        chatAnterior = listaFiltrada.get(posicaoChanged);
                    } else {
                        chatAnterior = listaChat.get(posicaoChanged);
                    }
                } else if (isPesquisaAtivada()
                        && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    //Somente existe um listener desse chat na listagem filtrada.
                    chatAnterior = listaFiltrada.get(posicaoChanged);
                }
                ToastCustomizado.toastCustomizadoCurto("Alterado: " + chatAnterior.getIdGrupo(), requireContext());

                if (chatAnterior.getTotalMsgNaoLida() != chatAtualizado.getTotalMsgNaoLida()) {
                    atualizarPorPayload(chatAtualizado, "totalMsgNaoLida");
                }

                if (chatAnterior.getTotalMsg() != chatAtualizado.getTotalMsg()) {
                    atualizarPorPayload(chatAtualizado, "totalMsg");
                }

                if (!chatAnterior.getTipoMidiaLastMsg().equals(chatAtualizado.getTipoMidiaLastMsg())) {
                    atualizarPorPayload(chatAtualizado, "tipoMidiaLastMsg");
                }

                if (!chatAnterior.getConteudoLastMsg().equals(chatAtualizado.getConteudoLastMsg())) {
                    atualizarPorPayload(chatAtualizado, "conteudoLastMsg");
                }

                if (chatAnterior.getTimestampLastMsg() != chatAtualizado.getTimestampLastMsg()) {
                    atualizarPorPayload(chatAtualizado, "timestampLastMsg");
                }

                if (isPesquisaAtivada() && listaFiltrada != null) {


                    adapterChatList.updateChatList(listaFiltrada, new AdapterChatGroupList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                } else if (!isPesquisaAtivada() && listaChat != null) {

                    Collections.sort(listaChat, chatComparator);
                    adapterChatList.updateChatList(listaChat, new AdapterChatGroupList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                }
            } else {
                ToastCustomizado.toastCustomizadoCurto("Hello code -1", requireContext());
            }
            posicaoChanged = -1;
        }
    }

    private void atualizarPorPayload(Chat chatAtualizado, String tipoPayload) {
        ToastCustomizado.toastCustomizadoCurto(tipoPayload, requireContext());

        int index = posicaoChanged;

        if (index != -1) {

            if (isPesquisaAtivada() && referenceFiltroHashMap != null
                    && !referenceFiltroHashMap.isEmpty()
                    && referenceFiltroHashMap.containsKey(chatAtualizado.getIdGrupo())) {
                ToastCustomizado.toastCustomizadoCurto("CODE NOOOO", requireContext());
                chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, tipoPayload, new ChatDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        adapterChatList.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
            if (idsGrupos != null && idsGrupos.size() > 0
                    && idsGrupos.contains(chatAtualizado.getIdGrupo())) {
                ToastCustomizado.toastCustomizadoCurto("CODE OK", requireContext());
                chatDiffDAO.atualizarChatPorPayload(chatAtualizado, tipoPayload, new ChatDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        if (!isPesquisaAtivada()) {
                            adapterChatList.notifyItemChanged(index, bundleRecup);
                        } else {
                            idsParaAtualizar.put(chatAtualizado.getIdGrupo(), bundleRecup);
                        }
                    }
                });
            }
        }
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
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onRemocao(Chat chatAlvo, int posicao) {
        if (chatAlvo != null) {
            logicaRemocao(chatAlvo, false, true);
        }
    }

    @Override
    public void onExecutarAnimacao() {
        requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void inicializarComponentes(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewLstChatGrupo);
        searchView = view.findViewById(R.id.searchViewLstChatGrupo);
        spinProgress = view.findViewById(R.id.spinProgressBarRecycler);
        txtViewTitle = view.findViewById(R.id.txtViewTitleLstChatGrupo);
        imgButtonCadastroGrupo = view.findViewById(R.id.imgButtonCadastroGrupo);
    }

    // Método para verificar se os 3 primeiros itens estão visíveis
    private boolean areFirstThreeItemsVisible(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibleItemPosition = 0;
        if (layoutManager != null) {
            firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        }
        return firstVisibleItemPosition <= 2;
    }

    private void verificaExistencia(String idChat, VerificaExistenciaCallback callback) {
        DatabaseReference verificaExistenciaRef = firebaseRef.child("detalhesChatGrupo")
                .child(idUsuario).child(idChat);
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onExistencia(snapshot.getValue() != null, snapshot.getValue(Chat.class));
                verificaExistenciaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void anexarNovoDado(Chat chatModificado) {
        newDataRef = firebaseRef.child("detalhesChatGrupo")
                .child(idUsuario).orderByChild("idUsuario")
                .equalTo(chatModificado.getIdGrupo()).limitToFirst(1);
        idsAIgnorarListeners.add(chatModificado.getIdGrupo());
        childEventListenerNewData = newDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Chat chatModificado = snapshot.getValue(Chat.class);
                    if (chatModificado == null) {
                        return;
                    }
                    String idGroupChat = chatModificado.getIdGrupo();
                    adicionarChat(chatModificado, true);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    ToastCustomizado.toastCustomizadoCurto("Alterado pelo newdata", requireContext());
                    logicaAtualizacao(snapshot, true);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Chat chatRemovido = snapshot.getValue(Chat.class);
                    ToastCustomizado.toastCustomizado("DELETE PELO NEW DATA", requireContext());
                    if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                            && idsAIgnorarListeners.contains(chatRemovido.getIdGrupo())) {
                        idsAIgnorarListeners.remove(chatRemovido.getIdGrupo());
                    }
                    logicaRemocao(chatRemovido, true, true);
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

            queryLoadMoreFiltro = firebaseRef.child("detalhesChatGrupo")
                    .child(idUsuario).orderByChild("idUsuario").equalTo(grupoPesquisa.getIdGrupo()).limitToFirst(1);

            //ToastCustomizado.toastCustomizadoCurto("AOS FILTROS: " + usuarioPesquisa.getIdGrupo(), requireContext());
            //ToastCustomizado.toastCustomizadoCurto("NR FILTROS: " + aosFiltros, requireContext());

            ChildEventListener childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Chat chatMore = snapshot.getValue(Chat.class);
                    if (chatMore != null
                            && chatMore.getIdGrupo() != null
                            && !chatMore.getIdGrupo().isEmpty()) {

                        //**ToastCustomizado.toastCustomizadoCurto("NEW PESQUISA: " + chatMore.getIdGrupo(), requireContext());
                        Log.d(TAG, "Timestamp key: " + lastTimestamp);
                        Log.d(TAG, "id: " + chatMore.getIdGrupo() + " time: " + chatMore.getTimestampLastMsg());
                        if (listaFiltrada != null && listaFiltrada.size() > 1 && idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(chatMore.getIdGrupo())) {
                            Log.d(TAG, "Id já existia: " + chatMore.getIdGrupo());
                            ToastCustomizado.toastCustomizadoCurto("ID JÁ EXISTIA " + chatMore.getIdGrupo(), requireContext());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        List<Chat> newChat = new ArrayList<>();
                        String key = grupoPesquisa.getNomeUsuarioPesquisa();
                        if (lastName != null && !lastName.isEmpty() && key != null
                                && !key.isEmpty()) {
                            if (!key.equals(lastName) || listaFiltrada.size() > 0 &&
                                    !chatMore.getIdGrupo()
                                            .equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdGrupo())) {
                                newChat.add(chatMore);
                                //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                lastName = key;
                            }
                        }
                        // Remove a última chave usada
                        if (newChat.size() > PAGE_SIZE) {
                            newChat.remove(0);
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (aosFiltros >= listaIdsRecuperados.size()) {
                                aosFiltros = 0;
                            }
                            adicionarMaisDadosFiltrados(newChat, chatMore.getIdGrupo(), queryLoadMoreFiltro, childListenerMoreFiltro);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() == null) {
                        return;
                    }
                    Chat chatUpdate = snapshot.getValue(Chat.class);

                    if (chatUpdate == null) {
                        return;
                    }

                    if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                            && idsAIgnorarListeners.contains(snapshot.getValue(Chat.class).getIdGrupo())) {
                        ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Chat.class).getIdGrupo(), requireContext());
                        return;
                    }
                    if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                            && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdGrupo())) {
                        return;
                    }

                    if (listenerHashMap != null && listenerHashMap.size() > 0
                            && listenerHashMap.containsKey(snapshot.getValue(Chat.class).getIdGrupo())) {
                        return;
                    }

                    if (listaFiltrada != null && listaFiltrada.size() > 0
                            && chatUpdate.getIdGrupo().equals(listaFiltrada.get(0).getIdGrupo())) {
                        return;
                    }

                    ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO SEARCH + DADOS " + chatUpdate.getIdGrupo(), requireContext());
                    logicaAtualizacao(snapshot, false);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Chat chatRemovido = snapshot.getValue(Chat.class);
                        if (chatRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(chatRemovido.getIdGrupo())
                                || listaChat != null && listaChat.size() > 0
                                && listaChat.get(0).getIdGrupo().equals(chatRemovido.getIdGrupo())) {
                            return;
                        }

                        verificaExistencia(chatRemovido.getIdGrupo(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Chat chatAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + chatRemovido.getIdGrupo(), requireContext());

                                logicaRemocao(chatRemovido, true, true);

                                if (status) {
                                    boolean menorque = chatAtualizado.getTimestampLastMsg() <= listaChat.get(0).getTimestampLastMsg();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + chatRemovido.getIdGrupo(), requireContext());
                                        anexarNovoDado(chatAtualizado);
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
        queryUltimoElemento = firebaseRef.child("detalhesChatGrupo")
                .child(idUsuario).orderByChild("timestampLastMsg").limitToLast(1);
        listenerUltimoElemento = queryUltimoElemento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElemento = snapshot1.getValue(Usuario.class).getIdGrupo();
                    setLoading(false);
                    if (callback != null && listaChat != null) {
                        callback.onRecuperado();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaChat != null) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void ultimoElementoFiltro(String nome, RecuperaUltimoElemento callback) {
        queryUltimoElementoFiltro = firebaseRef.child("group_chats_by_name")
                .child(idUsuario)
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToLast(1);
        listenerUltimoElementoFiltro = queryUltimoElementoFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElementoFiltro = snapshot1.getValue(Usuario.class).getIdGrupo();
                    setLoading(false);
                    if (callback != null && listaChat != null && !listaChat.isEmpty()) {
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

    public int findPositionInList(String userId) {
        for (int i = 0; i < listaChat.size(); i++) {
            Chat chat = listaChat.get(i);
            if (chat.getIdGrupo().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }

    private void cadastrarGrupo() {
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdMeusGrupos() != null
                        && usuarioAtual.getIdMeusGrupos().size() >= MAX_MY_GROUPS) {
                    snackbarLimiteGrupo = Snackbar.make(imgButtonCadastroGrupo, "Limite de criação de grupos atingido, por favor exclua um deles para que seja possível criar um novo grupo", Snackbar.LENGTH_LONG);
                    View snackbarView = snackbarLimiteGrupo.getView();
                    TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    textView.setMaxLines(5); // altera o número máximo de linhas exibidas
                    snackbarLimiteGrupo.show();
                    ToastCustomizado.toastCustomizado("Você já atingiu o limite de grupos que são 5 grupos por usuário, por favor exclua um deles para que seja possível a criação de um novo grupo", getContext());
                } else {
                    Intent intent = new Intent(getContext(), AddGroupUsersActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }
}