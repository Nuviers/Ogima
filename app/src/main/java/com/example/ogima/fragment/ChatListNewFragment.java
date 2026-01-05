package com.example.ogima.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterChatList;
import com.example.ogima.helper.ChatDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Chat;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
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


public class ChatListNewFragment extends Fragment implements AdapterChatList.RecuperaPosicaoAnterior, AdapterChatList.RemoverChatListener, AdapterChatList.AnimacaoIntent {

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
    private Set<String> idsUsuarios = new HashSet<>();
    private ChatDiffDAO chatDiffDAO, chatDAOFiltrado;
    private Query queryInicial, queryLoadMore,
            queryInicialFiltro, queryLoadMorePesquisa, queryInicialFind,
            queryLoadMoreFiltro, newDataRef;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
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
    private AdapterChatList adapterChatList;
    private boolean trocarQueryInicial = false;
    private Chat chatComparator;
    private int contadorRemocaoListener = 0;
    private FirebaseUtils firebaseUtils;
    private int travar = 0;

    private Set<String> idsListenersNEWDATA = new HashSet<>();
    private HashMap<String, Query> referenceHashMapNEWDATA = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapNEWDATA = new HashMap<>();
    private int contadorRemocaoListenerNEWDATA = 0;
    private static final String TAG = "CHATtag";
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
        void onRecuperado(Set<Usuario> listaIdsRecuperados);
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

    public ChatListNewFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        chatComparator = new Chat();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        inicializarComponentes(view);
        configInicial();
        return view;
    }

    private void configInicial() {
        firebaseUtils = new FirebaseUtils();
        txtViewTitle.setText(FormatarContadorUtils.abreviarTexto("Chats", 20));
        if (idUsuario == null || idUsuario.isEmpty()) {
            if (isAdded()) {
                ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), requireContext());
            }
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
                if(isAdded()){
                    ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), requireContext());
                }
                requireActivity().onBackPressed();
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    ToastCustomizado.toastCustomizado(String.format("%s%s %s", getString(R.string.error_retrieving_user_data), ":", message), requireContext());
                }
                requireActivity().onBackPressed();
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
                                        adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
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
            adapterChatList = new AdapterChatList(requireContext(),
                    listaChat, listaDadosUser,
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
                    queryInicial = firebaseRef.child("detalhesChat")
                            .child(idUsuario).orderByChild("timestampLastMsg")
                            .startAt(lastTimestamp + 1)
                            .limitToFirst(1);
                } else {
                    queryInicial = firebaseRef.child("detalhesChat")
                            .child(idUsuario).orderByChild("timestampLastMsg").limitToFirst(1);
                }
                exibirProgress();
                childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Chat chat = snapshot.getValue(Chat.class);
                            if (chat != null
                                    && chat.getIdUsuario() != null
                                    && !chat.getIdUsuario().isEmpty()) {
                                if (travar == 0) {
                                    lastTimestamp = chat.getTimestampLastMsg();
                                    adicionarChat(chat, false);
                                } else {
                                    //Dado mais recente que o anterior
                                    if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                            && listenerHashMapNEWDATA.containsKey(chat.getIdUsuario())) {
                                        return;
                                    }
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
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdUsuario())) {
                                return;
                            }
                            logicaAtualizacao(snapshot, false);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Chat chatRemovido = snapshot.getValue(Chat.class);
                            if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdUsuario())) {
                                //O próprio listenernewdata vai cuidar da remoção desse dado.
                                return;
                            }
                            logicaRemocao(chatRemovido, true, true);

                            verificaExistencia(chatRemovido.getIdUsuario(), new VerificaExistenciaCallback() {
                                @Override
                                public void onExistencia(boolean status, Chat chatAtualizado) {
                                    if (status) {
                                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                                && listenerHashMapNEWDATA.containsKey(chatRemovido.getIdUsuario())) {
                                        } else {
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
                        if (isAdded()) {
                            ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao recuperar as suas conversas", "Code:", error.getCode()), requireContext());
                        }
                        requireActivity().onBackPressed();
                    }
                });
            }
        });
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        exibirProgress();
        queryInicialFind = firebaseRef.child("chats_by_name")
                .child(idUsuario)
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(1);

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

                if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                    ocultarProgress();
                    setLoading(false);
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

                            recuperaDadosUser(usuarioPesquisa.getIdUsuario(), new RecuperaUser() {
                                @Override
                                public void onRecuperado(Usuario usuarioAtual) {

                                    UsuarioUtils.checkBlockingStatus(requireContext(), usuarioAtual.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                                        @Override
                                        public void onBlocked(boolean status) {
                                            usuarioAtual.setIndisponivel(status);
                                            adicionarChatFiltrado(usuarioAtual);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            usuarioAtual.setIndisponivel(true);
                                            adicionarChatFiltrado(usuarioAtual);
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

    private void adicionarChat(Chat chatAlvo, boolean dadoModificado) {
        recuperaDadosUser(chatAlvo.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {

                chatDiffDAO.adicionarChat(chatAlvo);
                chatDiffDAO.adicionarIdAoSet(idsUsuarios, dadosUser.getIdUsuario());

                List<Chat> listaAtual = new ArrayList<>();
                if (isPesquisaAtivada()) {
                    listaAtual = listaFiltrada;
                } else {
                    listaAtual = listaChat;
                    Collections.sort(listaChat, chatComparator);
                }

                adapterChatList.updateChatList(listaAtual, new AdapterChatList.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        travar = 1;

                        if (dadoModificado) {
                            adicionarDadoDoUsuario(dadosUser, newDataRef, childEventListenerNewData, dadoModificado);
                        } else {
                            adicionarDadoDoUsuario(dadosUser, null, null, dadoModificado);
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
                if (isAdded()) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar suas conversas.", requireContext());
                }
            }
        });
    }

    private void adicionarChatFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            String idChatInicioFiltro = listaFiltrada.get(0).getIdUsuario();
            if (idChatInicioFiltro.equals(dadosUser.getIdUsuario())) {
                ocultarProgress();
                setLoading(false);
                return;
            }
        }


        queryInicialFiltro = firebaseRef.child("detalhesChat")
                .child(idUsuario)
                .orderByChild("idUsuario")
                .equalTo(dadosUser.getIdUsuario()).limitToFirst(1);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    Chat chatAtual = snapshot.getValue(Chat.class);

                    if (chatAtual != null) {
                        chatAtual.setIndisponivel(dadosUser.isIndisponivel());
                    }

                    if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                        String idChatInicioFiltro = listaFiltrada.get(0).getIdUsuario();
                        if (idChatInicioFiltro.equals(dadosUser.getIdUsuario())) {
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }
                    }

                    lastName = dadosUser.getNomeUsuarioPesquisa();

                    chatDAOFiltrado.adicionarChat(chatAtual);
                    chatDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());
                    adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            referenceFiltroHashMap.put(dadosUser.getIdUsuario(), queryInicialFiltro);
                            listenerFiltroHashMap.put(dadosUser.getIdUsuario(), childListenerInicioFiltro);
                            adicionarDadoDoUsuario(dadosUser, queryInicialFiltro, childListenerInicioFiltro, false);
                            setLoading(false);
                            //carregarMaisDados(nomePesquisado);
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
                        && idsAIgnorarListeners.contains(snapshot.getValue(Chat.class).getIdUsuario())) {
                    return;
                }
                if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                        && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdUsuario())) {
                    return;
                }

                if (listenerHashMap != null && listenerHashMap.size() > 0
                        && listenerHashMap.containsKey(snapshot.getValue(Chat.class).getIdUsuario())) {
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
                if (isAdded()) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro ao realizar a pesquisa.", requireContext());
                }
            }
        });
    }

    private void carregarMaisDadosFiltrados(String dadoAnterior, RecuperarIdsFiltroCallback callback) {
        if (isPesquisaAtivada() && listaFiltrada != null) {
            if (listaFiltrada.size() > 1
                    && idUltimoElementoFiltro != null && !idUltimoElementoFiltro.isEmpty()
                    && idUltimoElementoFiltro.equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdUsuario())) {
                ocultarProgress();
                return;
            }

            if (listaFiltrada != null && !listaFiltrada.isEmpty()
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMorePesquisa = firebaseRef.child("chats_by_name")
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
                                        ocultarProgress();
                                        setLoading(false);
                                    } else {
                                        idsTempFiltro.add(usuarioPesquisa);
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
                    && idUltimoElemento.equals(listaChat.get(listaChat.size() - 1).getIdUsuario())) {
                ocultarProgress();
                return;
            }

            queryLoadMore = firebaseRef.child("detalhesChat")
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
                                && chatMore.getIdUsuario() != null
                                && !chatMore.getIdUsuario().isEmpty()) {
                            Log.d(TAG, "Timestamp key: " + lastTimestamp);
                            Log.d(TAG, "id: " + chatMore.getIdUsuario() + " time: " + chatMore.getTimestampLastMsg());
                            if (listaChat != null && listaChat.size() > 1 && idsUsuarios != null && idsUsuarios.size() > 0
                                    && idsUsuarios.contains(chatMore.getIdUsuario())) {
                                Log.d(TAG, "Id já existia: " + chatMore.getIdUsuario());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            if (listaChat != null && listaChat.size() > 1
                                    && chatMore.getTimestampLastMsg() < listaChat.get(0).getTimestampLastMsg()) {
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            List<Chat> newChat = new ArrayList<>();
                            long key = chatMore.getTimestampLastMsg();
                            if (lastTimestamp != -1 && key != -1) {
                                if (key != lastTimestamp || listaChat.size() > 0 &&
                                        !chatMore.getIdUsuario()
                                                .equals(listaChat.get(listaChat.size() - 1).getIdUsuario())) {
                                    newChat.add(chatMore);
                                    lastTimestamp = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newChat.size() > PAGE_SIZE) {
                                newChat.remove(0);
                            }
                            if (lastTimestamp != -1) {
                                adicionarMaisDados(newChat, chatMore.getIdUsuario(), queryLoadMore);
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
                                && idsAIgnorarListeners.contains(snapshot.getValue(Chat.class).getIdUsuario())) {
                            return;
                        }
                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdUsuario())) {
                            return;
                        }
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
                                && listenerHashMapNEWDATA.containsKey(chatRemovido.getIdUsuario())
                                || listaChat != null && listaChat.size() > 0
                                && listaChat.get(0).getIdUsuario().equals(chatRemovido.getIdUsuario())) {
                            return;
                        }

                        verificaExistencia(chatRemovido.getIdUsuario(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Chat chatAtualizado) {
                                logicaRemocao(chatRemovido, true, true);

                                if (status) {
                                    boolean menorque = chatAtualizado.getTimestampLastMsg() <= listaChat.get(0).getTimestampLastMsg();
                                    if (!menorque) {
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

    private void adicionarMaisDados(List<Chat> newChat, String idUser, Query queryAlvo) {
        if (newChat != null && newChat.size() >= 1) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    chatDiffDAO.carregarMaisChat(newChat, idsUsuarios);
                    chatDiffDAO.adicionarIdAoSet(idsUsuarios, idUser);

                    Collections.sort(listaChat, chatComparator);
                    adapterChatList.updateChatList(listaChat, new AdapterChatList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerChat, false);
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


    private void adicionarMaisDadosFiltrados(List<Chat> newChat, String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (newChat != null && !newChat.isEmpty()) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    chatDAOFiltrado.carregarMaisChat(newChat, idsFiltrados);
                    chatDAOFiltrado.adicionarIdAoSet(idsFiltrados, idUser);
                    adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerAlvo, false);
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

    private void adicionarMaisDadosFiltradosOLD(List<Chat> newChat, Usuario dadosUser) {
        if (newChat != null && newChat.size() >= 1) {
            chatDAOFiltrado.carregarMaisChat(newChat, idsFiltrados);
            chatDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());


            adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                }
            });

            if (idsUsuarios != null && idsUsuarios.size() > 0
                    && idsUsuarios.contains(dadosUser.getIdUsuario())
                    || idsListeners != null && idsListeners.size() > 0
                    && idsListeners.contains(dadosUser.getIdUsuario())) {

                ocultarProgress();
                adicionarDadoDoUsuario(dadosUser, null, null, false);
                setLoading(false);
                return;
            }

            queryLoadMoreFiltro = firebaseRef.child("detalhesChat")
                    .child(idUsuario).orderByChild("idUsuario")
                    .equalTo(dadosUser.getIdUsuario());
            childListenerMoreFiltro = queryLoadMoreFiltro.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        ocultarProgress();
                        adicionarDadoDoUsuario(dadosUser, queryLoadMoreFiltro, childListenerMoreFiltro, false);
                        setLoading(false);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        Chat chatAtualizado = snapshot.getValue(Chat.class);
                        if (chatAtualizado == null ||
                                chatAtualizado.getIdUsuario() == null || chatAtualizado.getIdUsuario().isEmpty()) {
                            return;
                        }
                        logicaAtualizacao(snapshot, false);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Chat chatRemovido = snapshot.getValue(Chat.class);

                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ocultarProgress();
                }
            });
        } else {
            ocultarProgress();
        }
    }

    private void recuperaDadosUser(String idUser, RecuperaUser callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUser, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                UsuarioUtils.checkBlockingStatus(requireContext(), idUser, new UsuarioUtils.CheckLockCallback() {
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

    private void adicionarListenerFiltragem(String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {

        if (listenerFiltroHashMap == null || referenceFiltroHashMap == null) {
            return;
        }

        if (!listenerFiltroHashMap.isEmpty()
                && listenerFiltroHashMap.containsKey(idUser)) {
            return;
        }

        referenceFiltroHashMap.put(idUser, queryAlvo);
        listenerFiltroHashMap.put(idUser, childEventListenerAlvo);
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
        if (listaDadosUser != null) {
            listaDadosUser.clear();
        }
        if (idsUsuarios != null) {
            idsUsuarios.clear();
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
            adapterChatList.updateChatList(listaChat, new AdapterChatList.ListaAtualizadaCallback() {
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
            for (String userId : listenerHashMapNEWDATA.keySet()) {
                Query userRef = referenceHashMapNEWDATA.get(userId);
                ChildEventListener listener = listenerHashMapNEWDATA.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
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

            for (String userId : listenerFiltroHashMap.keySet()) {
                Query userRef = referenceFiltroHashMap.get(userId);
                ChildEventListener listener = listenerFiltroHashMap.get(userId);
                if (userRef != null && listener != null) {
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

    private void verificaVinculo(String idAlvo, VerificaCriterio callback) {
        DatabaseReference verificaVinculoRef = firebaseRef.child("detalhesChat")
                .child(idUsuario).child(idAlvo);
        verificaVinculoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onCriterioAtendido();
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

        DatabaseReference verificaExistenciaRef = firebaseRef.child("detalhesChat")
                .child(idUsuario).child(chatRemovido.getIdUsuario());
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || ignorarVerificacao) {

                    if (idsFiltrados != null && idsFiltrados.size() > 0
                            && idsFiltrados.contains(chatRemovido.getIdUsuario())) {
                        if (listaFiltrada != null && listaFiltrada.size() > 0 && excluirDaLista) {
                            if (idsFiltrados != null && idsFiltrados.size() > 0) {
                                idsFiltrados.remove(chatRemovido.getIdUsuario());
                            }
                            chatDAOFiltrado.removerChat(chatRemovido);
                        }
                    }

                    if (idsUsuarios != null && idsUsuarios.size() > 0
                            && idsUsuarios.contains(chatRemovido.getIdUsuario())) {
                        if (listaChat != null && listaChat.size() > 0 && excluirDaLista) {
                            if (idsUsuarios != null && idsUsuarios.size() > 0) {
                                idsUsuarios.remove(chatRemovido.getIdUsuario());
                            }
                            chatDiffDAO.removerChat(chatRemovido);
                        }
                    }

                    if (listaDadosUser != null && listaDadosUser.size() > 0 && excluirDaLista) {
                        listaDadosUser.remove(chatRemovido.getIdUsuario());
                        int posicao = adapterChatList.findPositionInList(chatRemovido.getIdUsuario());
                        if (posicao != -1) {
                            adapterChatList.notifyItemChanged(posicao);
                        }
                    }

                    if (isPesquisaAtivada() && listaFiltrada != null) {
                        adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {

                            }
                        });
                    } else if (!isPesquisaAtivada() && listaChat != null) {
                        adapterChatList.updateChatList(listaChat, new AdapterChatList.ListaAtualizadaCallback() {
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
            if (chatAtualizado == null || chatAtualizado.getIdUsuario() == null) {
                return;
            }

            posicaoChanged = adapterChatList.findPositionInList(chatAtualizado.getIdUsuario());

            if (posicaoChanged == -1 && isPesquisaAtivada()) {
                //Não existe esse chat na lista filtrada mas existe na lista normal.
                posicaoChanged = findPositionInList(chatAtualizado.getIdUsuario());
            }

            if (posicaoChanged != -1) {
                Chat chatAnterior = new Chat();
                if (idsUsuarios != null && idsUsuarios.size() > 0
                        && idsUsuarios.contains(chatAtualizado.getIdUsuario())) {
                    //Já existe um listener na listagem normal
                    if (isPesquisaAtivada()
                            && referenceFiltroHashMap != null
                            && !referenceFiltroHashMap.isEmpty()
                            && referenceFiltroHashMap.containsKey(chatAtualizado.getIdUsuario())) {
                        chatAnterior = listaFiltrada.get(posicaoChanged);
                    } else {
                        chatAnterior = listaChat.get(posicaoChanged);
                    }
                } else if (isPesquisaAtivada()
                        && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    //Somente existe um listener desse chat na listagem filtrada.
                    chatAnterior = listaFiltrada.get(posicaoChanged);
                }

                if (chatAnterior.getTotalMsgNaoLida() != -1) {
                    if (chatAnterior.getTotalMsgNaoLida() != chatAtualizado.getTotalMsgNaoLida()) {
                        atualizarPorPayload(chatAtualizado, "totalMsgNaoLida");
                    }
                }

                if (chatAnterior.getTotalMsg() != -1) {
                    if (chatAnterior.getTotalMsg() != chatAtualizado.getTotalMsg()) {
                        atualizarPorPayload(chatAtualizado, "totalMsg");
                    }
                }

                if (chatAnterior.getTipoMidiaLastMsg() != null) {
                    if (!chatAnterior.getTipoMidiaLastMsg().equals(chatAtualizado.getTipoMidiaLastMsg())) {
                        atualizarPorPayload(chatAtualizado, "tipoMidiaLastMsg");
                    }
                }

                if (chatAnterior.getConteudoLastMsg() != null) {
                    if (!chatAnterior.getConteudoLastMsg().equals(chatAtualizado.getConteudoLastMsg())) {
                        atualizarPorPayload(chatAtualizado, "conteudoLastMsg");
                    }
                }

                if (chatAnterior.getTimestampLastMsg() != -1) {
                    if (chatAnterior.getTimestampLastMsg() != chatAtualizado.getTimestampLastMsg()) {
                        atualizarPorPayload(chatAtualizado, "timestampLastMsg");
                    }
                }

                if (isPesquisaAtivada() && listaFiltrada != null) {

                    adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                } else if (!isPesquisaAtivada() && listaChat != null) {

                    Collections.sort(listaChat, chatComparator);
                    adapterChatList.updateChatList(listaChat, new AdapterChatList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                }
            }
            posicaoChanged = -1;
        }
    }

    private void atualizarPorPayload(Chat chatAtualizado, String tipoPayload) {
        int index = posicaoChanged;

        if (index != -1) {

            if (isPesquisaAtivada() && referenceFiltroHashMap != null
                    && !referenceFiltroHashMap.isEmpty()
                    && referenceFiltroHashMap.containsKey(chatAtualizado.getIdUsuario())) {
                chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, tipoPayload, new ChatDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        adapterChatList.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
            if (idsUsuarios != null && idsUsuarios.size() > 0
                    && idsUsuarios.contains(chatAtualizado.getIdUsuario())) {
                chatDiffDAO.atualizarChatPorPayload(chatAtualizado, tipoPayload, new ChatDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        if (!isPesquisaAtivada()) {
                            adapterChatList.notifyItemChanged(index, bundleRecup);
                        } else {
                            idsParaAtualizar.put(chatAtualizado.getIdUsuario(), bundleRecup);
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
        recyclerView = view.findViewById(R.id.recyclerViewLstChat);
        searchView = view.findViewById(R.id.searchViewLstChat);
        spinProgress = view.findViewById(R.id.spinProgressBarRecycler);
        txtViewTitle = view.findViewById(R.id.txtViewTitleLstChat);
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
        DatabaseReference verificaExistenciaRef = firebaseRef.child("detalhesChat")
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
        newDataRef = firebaseRef.child("detalhesChat")
                .child(idUsuario).orderByChild("idUsuario")
                .equalTo(chatModificado.getIdUsuario()).limitToFirst(1);
        idsAIgnorarListeners.add(chatModificado.getIdUsuario());
        childEventListenerNewData = newDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Chat chatModificado = snapshot.getValue(Chat.class);
                    if (chatModificado == null) {
                        return;
                    }
                    String idUserChat = chatModificado.getIdUsuario();
                    adicionarChat(chatModificado, true);
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
                    Chat chatRemovido = snapshot.getValue(Chat.class);
                    if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                            && idsAIgnorarListeners.contains(chatRemovido.getIdUsuario())) {
                        idsAIgnorarListeners.remove(chatRemovido.getIdUsuario());
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

    private void recuperarDetalhes(Set<Usuario> listaIdsRecuperados){
        for (Usuario usuarioPesquisa : listaIdsRecuperados) {
            aosFiltros++;
            if (aosFiltros > listaIdsRecuperados.size()) {
                aosFiltros = 0;
                return;
            }

            childListenerMoreFiltro = null;
            queryLoadMoreFiltro = null;

            queryLoadMoreFiltro = firebaseRef.child("detalhesChat")
                    .child(idUsuario).orderByChild("idUsuario").equalTo(usuarioPesquisa.getIdUsuario()).limitToFirst(1);

            ChildEventListener childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Chat chatMore = snapshot.getValue(Chat.class);
                    if (chatMore != null
                            && chatMore.getIdUsuario() != null
                            && !chatMore.getIdUsuario().isEmpty()) {

                        Log.d(TAG, "Timestamp key: " + lastTimestamp);
                        Log.d(TAG, "id: " + chatMore.getIdUsuario() + " time: " + chatMore.getTimestampLastMsg());
                        if (listaFiltrada != null && listaFiltrada.size() > 1 && idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(chatMore.getIdUsuario())) {
                            Log.d(TAG, "Id já existia: " + chatMore.getIdUsuario());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        List<Chat> newChat = new ArrayList<>();
                        String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                        if (lastName != null && !lastName.isEmpty() && key != null
                                && !key.isEmpty()) {
                            if (!key.equals(lastName) || listaFiltrada.size() > 0 &&
                                    !chatMore.getIdUsuario()
                                            .equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdUsuario())) {
                                newChat.add(chatMore);
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
                            adicionarMaisDadosFiltrados(newChat, chatMore.getIdUsuario(), queryLoadMoreFiltro, childListenerMoreFiltro);
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
                            && idsAIgnorarListeners.contains(snapshot.getValue(Chat.class).getIdUsuario())) {
                        return;
                    }
                    if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                            && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Chat.class).getIdUsuario())) {
                        return;
                    }

                    if (listenerHashMap != null && listenerHashMap.size() > 0
                            && listenerHashMap.containsKey(snapshot.getValue(Chat.class).getIdUsuario())) {
                        return;
                    }

                    if (listaFiltrada != null && listaFiltrada.size() > 0
                            && chatUpdate.getIdUsuario().equals(listaFiltrada.get(0).getIdUsuario())) {
                        return;
                    }
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
                                && listenerHashMapNEWDATA.containsKey(chatRemovido.getIdUsuario())
                                || listaChat != null && listaChat.size() > 0
                                && listaChat.get(0).getIdUsuario().equals(chatRemovido.getIdUsuario())) {
                            return;
                        }

                        verificaExistencia(chatRemovido.getIdUsuario(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Chat chatAtualizado) {

                                logicaRemocao(chatRemovido, true, true);

                                if (status) {
                                    boolean menorque = chatAtualizado.getTimestampLastMsg() <= listaChat.get(0).getTimestampLastMsg();
                                    if (!menorque) {
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
            listenerFiltroHashMap.put(usuarioPesquisa.getIdUsuario(), childListener);
            referenceFiltroHashMap.put(usuarioPesquisa.getIdUsuario(), queryLoadMoreFiltro);
        }
    }

    private void ultimoElemento(RecuperaUltimoElemento callback) {
        queryUltimoElemento = firebaseRef.child("detalhesChat")
                .child(idUsuario).orderByChild("timestampLastMsg").limitToLast(1);
        listenerUltimoElemento = queryUltimoElemento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElemento = snapshot1.getValue(Usuario.class).getIdUsuario();
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
        queryUltimoElementoFiltro = firebaseRef.child("chats_by_name")
                .child(idUsuario)
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToLast(1);
        listenerUltimoElementoFiltro = queryUltimoElementoFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElementoFiltro = snapshot1.getValue(Usuario.class).getIdUsuario();
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
            if (chat.getIdUsuario().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}