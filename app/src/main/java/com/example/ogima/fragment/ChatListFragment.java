package com.example.ogima.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

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

public class ChatListFragment extends Fragment implements AdapterChatList.RecuperaPosicaoAnterior, AdapterChatList.RemoverChatListener, AdapterChatList.AnimacaoIntent {

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
    private Query queryInicial, queryLoadMore;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private Query queryInicialFiltro, queryLoadMoreFiltro, queryLoadMoreChat;
    private String nomePesquisado = "";
    private List<Chat> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private boolean pesquisaAtivada = false;
    private String lastId = null;
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
    private ValueEventListener listenerFiltragem;
    private HashMap<String, Object> listaAmigos = new HashMap<>();
    private AdapterChatList adapterChatList;
    private boolean trocarQueryInicial = false;
    private ChildEventListener childEventListenerChat, childEventListenerFiltro;
    private Chat chatComparator;
    private int contadorRemocaoListener = 0;
    private FirebaseUtils firebaseUtils;

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

    public ChatListFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        chatComparator = new Chat();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        inicializarComponentes(view);
        ToastCustomizado.toastCustomizadoCurto("CREATE", requireContext());
        configInicial();
        return view;
    }

    private void configInicial() {
        firebaseUtils = new FirebaseUtils();
        txtViewTitle.setText(FormatarContadorUtils.abreviarTexto("Chats", 20));
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
                                        chatDAOFiltrado.limparListaChats();
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
                public void onScrollStateChanged(@androidx.annotation.NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
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

    private void recuperarDadosIniciais() {
        if (listaChat != null && listaChat.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }
        if (trocarQueryInicial && lastId != null && !lastId.isEmpty()) {
            queryInicial = firebaseRef.child("detalhesChat")
                    .child(idUsuario).orderByChild("idUsuario")
                    .startAt(lastId + 1)
                    .limitToFirst(1);
        } else {
            queryInicial = firebaseRef.child("detalhesChat")
                    .child(idUsuario).orderByChild("idUsuario").limitToFirst(1);
        }
        exibirProgress();
        queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Chat chat = snapshot1.getValue(Chat.class);
                        if (chat != null
                                && chat.getIdUsuario() != null
                                && !chat.getIdUsuario().isEmpty()) {
                            adicionarChat(chat);
                            lastId = chat.getIdUsuario();
                        }
                    }
                } else {
                    ocultarProgress();
                    //Exibir um textview com essa mensagem.
                    String msgSemConversas = "Você não possui conversas no momento.";
                }
                queryInicial.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastId = null;
                ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao recuperar as suas conversas", "Code:", error.getCode()), requireContext());
                requireActivity().onBackPressed();
            }
        });
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        exibirProgress();
        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff");
        queryInicialFiltro.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    UsuarioUtils.checkBlockingStatus(requireContext(), usuarioPesquisa.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                                        @Override
                                        public void onBlocked(boolean status) {
                                            usuarioPesquisa.setIndisponivel(status);
                                            adicionarChatFiltrado(usuarioPesquisa);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            usuarioPesquisa.setIndisponivel(true);
                                            adicionarChatFiltrado(usuarioPesquisa);
                                        }
                                    });
                                }

                                @Override
                                public void onSemVinculo() {
                                    ocultarProgress();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocultarProgress();
                lastName = null;
            }
        });
    }

    private void adicionarChat(Chat chatAlvo) {
        if (listaChat != null && listaChat.size() >= 1) {
            ocultarProgress();
            setLoading(false);
            return;
        }
        recuperaDadosUser(chatAlvo.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                chatDiffDAO.adicionarChat(chatAlvo);
                chatDiffDAO.adicionarIdAoSet(idsUsuarios, dadosUser.getIdUsuario());
                if (listaChat != null && listaChat.size() > 0) {
                    Collections.sort(listaChat, chatComparator);
                }
                adapterChatList.updateChatList(listaChat, new AdapterChatList.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        adicionarDadoDoUsuario(dadosUser, null, null);
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
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar suas conversas.", requireContext());
            }
        });
    }

    private void adicionarChatFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            ocultarProgress();
            return;
        }
        lastName = dadosUser.getNomeUsuarioPesquisa();
        FirebaseRecuperarUsuario.recuperarDetalhesChat(requireContext(), dadosUser.getIdUsuario(), new FirebaseRecuperarUsuario.RecuperarDetalhesChatCallback() {
            @Override
            public void onDetalheChatRecuperado(Chat chatAtual) {
                chatAtual.setIndisponivel(dadosUser.isIndisponivel());
                chatDAOFiltrado.adicionarChat(chatAtual);
                chatDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());
                if (listaFiltrada != null && listaFiltrada.size() > 0) {
                    Collections.sort(listaFiltrada, chatComparator);
                }
                adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        ocultarProgress();
                        adicionarDadoDoUsuario(dadosUser, null, null);
                        setLoading(false);
                    }
                });
            }

            @Override
            public void onSemDados() {
                ocultarProgress();
                setLoading(false);
            }

            @Override
            public void onError(String mensagem) {
                ocultarProgress();
                setLoading(false);
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao realizar a pesquisa.", requireContext());
            }
        });
    }

    private void carregarMaisDados(String dadoAnterior) {
        exibirProgress();
        if (isPesquisaAtivada()) {
            if (listaFiltrada != null && listaFiltrada.size() > 0
                    && lastName != null && !lastName.isEmpty()) {
                queryLoadMoreFiltro = firebaseRef.child("usuarios")
                        .orderByChild("nomeUsuarioPesquisa")
                        .startAt(lastName).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);
                queryLoadMoreFiltro.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                            FirebaseRecuperarUsuario.recuperarDetalhesChat(requireContext(), usuarioPesquisa.getIdUsuario(), new FirebaseRecuperarUsuario.RecuperarDetalhesChatCallback() {
                                                @Override
                                                public void onDetalheChatRecuperado(Chat chatAtual) {
                                                    UsuarioUtils.checkBlockingStatus(requireContext(), usuarioPesquisa.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                                                        @Override
                                                        public void onBlocked(boolean status) {
                                                            usuarioPesquisa.setIndisponivel(status);
                                                            chatAtual.setIndisponivel(status);
                                                            List<Chat> newChat = new ArrayList<>();
                                                            String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                                                            if (lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)) {
                                                                newChat.add(chatAtual);
                                                                lastName = key;
                                                            }
                                                            // Remove a última chave usada
                                                            if (newChat.size() > PAGE_SIZE) {
                                                                newChat.remove(0);
                                                            }
                                                            if (lastName != null && !lastName.isEmpty()) {
                                                                adicionarMaisDadosFiltrados(newChat, usuarioPesquisa);
                                                            }
                                                        }

                                                        @Override
                                                        public void onError(String message) {
                                                            ocultarProgress();
                                                            String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                                                            if (lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)) {
                                                                lastName = key;
                                                            }
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onSemDados() {
                                                    String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                                                    if (lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)) {
                                                        lastName = key;
                                                    }
                                                }

                                                @Override
                                                public void onError(String mensagem) {
                                                    ocultarProgress();
                                                    lastName = null;
                                                }
                                            });
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
            ToastCustomizado.toastCustomizadoCurto("Query: " + lastId, requireContext());
            queryLoadMore = firebaseRef.child("detalhesChat")
                    .child(idUsuario)
                    .orderByChild("idUsuario")
                    .startAt(lastId)
                    .limitToFirst(PAGE_SIZE);
            childEventListenerChat = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        Chat chatMore = snapshot.getValue(Chat.class);
                        if (chatMore != null
                                && chatMore.getIdUsuario() != null
                                && !chatMore.getIdUsuario().isEmpty()) {
                            ToastCustomizado.toastCustomizadoCurto("Add: " + lastId, requireContext());
                            List<Chat> newChat = new ArrayList<>();
                            String key = chatMore.getIdUsuario();
                            if (lastId != null && key != null) {
                                if (!key.equals(lastId) || listaChat.size() > 0 &&
                                        !chatMore.getIdUsuario()
                                                .equals(listaChat.get(listaChat.size() - 1).getIdUsuario())) {
                                    newChat.add(chatMore);
                                    lastId = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newChat.size() > PAGE_SIZE) {
                                newChat.remove(0);
                            }
                            if (lastId != null) {
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
                        Chat chatAtualizado = snapshot.getValue(Chat.class);
                        if (chatAtualizado == null || chatAtualizado.getIdUsuario() == null) {
                            return;
                        }

                        if (idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(chatAtualizado.getIdUsuario())) {
                            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                int posicao = adapterChatList.findPositionInList(chatAtualizado.getIdUsuario());
                                if (posicao != -1) {
                                    Chat chatAnterior = listaFiltrada.get(posicao);
                                    if (chatAnterior.getTotalMsgNaoLida() != chatAtualizado.getTotalMsgNaoLida()) {
                                        ToastCustomizado.toastCustomizadoCurto("MsgNaoLida", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "totalMsgNaoLida");
                                    }
                                    if (chatAnterior.getTotalMsg() != chatAtualizado.getTotalMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TotalMsg", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "totalMsg");
                                    }
                                    if (!chatAnterior.getTipoMidiaLastMsg().equals(chatAtualizado.getTipoMidiaLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("TipoMidia", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "tipoMidiaLastMsg");
                                    }

                                    if (!chatAnterior.getConteudoLastMsg().equals(chatAtualizado.getConteudoLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("ConteudoMsg", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "conteudoLastMsg");
                                    }

                                    if (chatAnterior.getTimestampLastMsg() != chatAtualizado.getTimestampLastMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TimestampLastMsg", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "timestampLastMsg");
                                    }

                                    if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                        Collections.sort(listaFiltrada, chatComparator);
                                    }
                                }
                            }
                        }

                        if (idsUsuarios != null && idsUsuarios.size() > 0
                                && idsUsuarios.contains(chatAtualizado.getIdUsuario())) {
                            if (listaChat != null && listaChat.size() > 0) {
                                int posicao = adapterChatList.findPositionInList(chatAtualizado.getIdUsuario());
                                if (posicao != -1) {
                                    Chat chatAnterior = listaChat.get(posicao);
                                    if (chatAnterior.getTotalMsgNaoLida() != chatAtualizado.getTotalMsgNaoLida()) {
                                        ToastCustomizado.toastCustomizadoCurto("MsgNaoLida", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "totalMsgNaoLida");
                                    }
                                    if (chatAnterior.getTotalMsg() != chatAtualizado.getTotalMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TotalMsg", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "totalMsg");
                                    }
                                    if (!chatAnterior.getTipoMidiaLastMsg().equals(chatAtualizado.getTipoMidiaLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("TipoMidia", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "tipoMidiaLastMsg");
                                    }

                                    if (!chatAnterior.getConteudoLastMsg().equals(chatAtualizado.getConteudoLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("ConteudoMsg", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "conteudoLastMsg");
                                    }

                                    if (chatAnterior.getTimestampLastMsg() != chatAtualizado.getTimestampLastMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TimestampLastMsg", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "timestampLastMsg");
                                    }

                                    if (listaChat != null && listaChat.size() > 0) {
                                        Collections.sort(listaChat, chatComparator);
                                    }
                                }
                            }
                        }

                        if (isPesquisaAtivada() && listaFiltrada != null) {
                            adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {
                                    ToastCustomizado.toastCustomizadoCurto("Filtrado: " + listaFiltrada.size(), requireContext());
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
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Chat chatRemovido = snapshot.getValue(Chat.class);
                        if (chatRemovido == null) {
                            return;
                        }

                        if (idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(chatRemovido.getIdUsuario())) {
                            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                chatDAOFiltrado.removerChat(chatRemovido);
                            }
                            if (listaDadosUser != null && listaDadosUser.size() > 0) {
                                listaDadosUser.remove(chatRemovido.getIdUsuario());
                                int posicao = adapterChatList.findPositionInList(chatRemovido.getIdUsuario());
                                if (posicao != -1) {
                                    adapterChatList.notifyItemChanged(posicao);
                                }
                            }

                            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                Collections.sort(listaFiltrada, chatComparator);
                            }

                            if (referenceFiltroHashMap != null && referenceFiltroHashMap.size() > 0
                                    && listenerFiltroHashMap != null && listenerFiltroHashMap.size() > 0) {
                                referenceFiltroHashMap.remove(chatRemovido.getIdUsuario());
                                listenerFiltroHashMap.remove(chatRemovido.getIdUsuario());
                                if (idsListeners != null && idsListeners.size() > 0) {
                                    idsListeners.remove(chatRemovido.getIdUsuario());
                                }
                                if (idsFiltrados != null && idsFiltrados.size() > 0) {
                                    idsFiltrados.remove(chatRemovido.getIdUsuario());
                                }
                                ToastCustomizado.toastCustomizadoCurto("Removido", requireContext());
                            }
                        }

                        if (idsUsuarios != null && idsUsuarios.size() > 0
                                && idsUsuarios.contains(chatRemovido.getIdUsuario())) {
                            if (listaChat != null && listaChat.size() > 0) {
                                chatDiffDAO.removerChat(chatRemovido);
                            }
                            if (listaDadosUser != null && listaDadosUser.size() > 0) {
                                listaDadosUser.remove(chatRemovido.getIdUsuario());
                                int posicao = adapterChatList.findPositionInList(chatRemovido.getIdUsuario());
                                if (posicao != -1) {
                                    adapterChatList.notifyItemChanged(posicao);
                                }
                            }

                            if (listaChat != null && listaChat.size() > 0) {
                                Collections.sort(listaChat, chatComparator);
                            }

                            if (referenceHashMap != null && referenceHashMap.size() > 0
                                    && listenerHashMap != null && listenerHashMap.size() > 0) {
                                referenceHashMap.remove(chatRemovido.getIdUsuario());
                                listenerHashMap.remove(chatRemovido.getIdUsuario());
                                if (idsListeners != null && idsListeners.size() > 0) {
                                    idsListeners.remove(chatRemovido.getIdUsuario());
                                }
                                if (idsUsuarios != null && idsUsuarios.size() > 0) {
                                    idsUsuarios.remove(chatRemovido.getIdUsuario());
                                }
                                ToastCustomizado.toastCustomizadoCurto("Removido", requireContext());
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
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ocultarProgress();
                    lastId = null;
                }
            });
        }
    }

    private void adicionarMaisDados(List<Chat> newChat, String idUser, Query queryAlvo) {
        if (newChat != null && newChat.size() >= 1) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    ToastCustomizado.toastCustomizadoCurto("New last: " + dadosUser.getNomeUsuario(), requireContext());
                    chatDiffDAO.carregarMaisChat(newChat, idsUsuarios);
                    chatDiffDAO.adicionarIdAoSet(idsUsuarios, idUser);
                    if (listaChat != null && listaChat.size() > 0) {
                        Collections.sort(listaChat, chatComparator);
                    }
                    adapterChatList.updateChatList(listaChat, new AdapterChatList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerChat);
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

    private void adicionarMaisDadosFiltrados(List<Chat> newChat, Usuario dadosUser) {
        if (newChat != null && newChat.size() >= 1) {
            chatDAOFiltrado.carregarMaisChat(newChat, idsFiltrados);
            chatDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());
            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                Collections.sort(listaFiltrada, chatComparator);
            }
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            if (idsUsuarios != null && idsUsuarios.size() > 0
                    && idsUsuarios.contains(dadosUser.getIdUsuario())) {
                ocultarProgress();
                adicionarDadoDoUsuario(dadosUser, null, null);
                setLoading(false);
                return;
            }

            queryLoadMoreChat = firebaseRef.child("detalhesChat")
                    .child(idUsuario).child(dadosUser.getIdUsuario());
            childEventListenerFiltro = queryLoadMoreChat.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        ocultarProgress();
                        adicionarDadoDoUsuario(dadosUser, queryLoadMoreChat, childEventListenerFiltro);
                        setLoading(false);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        Chat chatAtualizado = snapshot.getValue(Chat.class);
                        if (chatAtualizado == null || chatAtualizado.getIdUsuario() == null) {
                            return;
                        }

                        if (idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(chatAtualizado.getIdUsuario())) {
                            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                int posicao = adapterChatList.findPositionInList(chatAtualizado.getIdUsuario());
                                if (posicao != -1) {
                                    Chat chatAnterior = listaFiltrada.get(posicao);
                                    if (chatAnterior.getTotalMsgNaoLida() != chatAtualizado.getTotalMsgNaoLida()) {
                                        ToastCustomizado.toastCustomizadoCurto("MsgNaoLida", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "totalMsgNaoLida");
                                    }
                                    if (chatAnterior.getTotalMsg() != chatAtualizado.getTotalMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TotalMsg", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "totalMsg");
                                    }
                                    if (!chatAnterior.getTipoMidiaLastMsg().equals(chatAtualizado.getTipoMidiaLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("TipoMidia", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "tipoMidiaLastMsg");
                                    }

                                    if (!chatAnterior.getConteudoLastMsg().equals(chatAtualizado.getConteudoLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("ConteudoMsg", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "conteudoLastMsg");
                                    }

                                    if (chatAnterior.getTimestampLastMsg() != chatAtualizado.getTimestampLastMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TimestampLastMsg", requireContext());
                                        chatDAOFiltrado.atualizarChatPorPayload(chatAtualizado, "timestampLastMsg");
                                    }

                                    if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                        Collections.sort(listaFiltrada, chatComparator);
                                    }
                                }
                            }
                        }

                        if (idsUsuarios != null && idsUsuarios.size() > 0
                                && idsUsuarios.contains(chatAtualizado.getIdUsuario())) {
                            if (listaChat != null && listaChat.size() > 0) {
                                int posicao = adapterChatList.findPositionInList(chatAtualizado.getIdUsuario());
                                if (posicao != -1) {
                                    Chat chatAnterior = listaChat.get(posicao);
                                    if (chatAnterior.getTotalMsgNaoLida() != chatAtualizado.getTotalMsgNaoLida()) {
                                        ToastCustomizado.toastCustomizadoCurto("MsgNaoLida", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "totalMsgNaoLida");
                                    }
                                    if (chatAnterior.getTotalMsg() != chatAtualizado.getTotalMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TotalMsg", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "totalMsg");
                                    }
                                    if (!chatAnterior.getTipoMidiaLastMsg().equals(chatAtualizado.getTipoMidiaLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("TipoMidia", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "tipoMidiaLastMsg");
                                    }

                                    if (!chatAnterior.getConteudoLastMsg().equals(chatAtualizado.getConteudoLastMsg())) {
                                        ToastCustomizado.toastCustomizadoCurto("ConteudoMsg", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "conteudoLastMsg");
                                    }

                                    if (chatAnterior.getTimestampLastMsg() != chatAtualizado.getTimestampLastMsg()) {
                                        ToastCustomizado.toastCustomizadoCurto("TimestampLastMsg", requireContext());
                                        chatDiffDAO.atualizarChatPorPayload(chatAtualizado, "timestampLastMsg");
                                    }

                                    if (listaChat != null && listaChat.size() > 0) {
                                        Collections.sort(listaChat, chatComparator);
                                    }
                                }
                            }
                        }

                        if (isPesquisaAtivada() && listaFiltrada != null) {
                            adapterChatList.updateChatList(listaFiltrada, new AdapterChatList.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {
                                    ToastCustomizado.toastCustomizadoCurto("Filtrado: " + listaFiltrada.size(), requireContext());
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
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Chat chatRemovido = snapshot.getValue(Chat.class);
                        if (chatRemovido == null) {
                            return;
                        }

                        if (idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(chatRemovido.getIdUsuario())) {
                            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                chatDAOFiltrado.removerChat(chatRemovido);
                            }
                            if (listaDadosUser != null && listaDadosUser.size() > 0) {
                                listaDadosUser.remove(chatRemovido.getIdUsuario());
                                int posicao = adapterChatList.findPositionInList(chatRemovido.getIdUsuario());
                                if (posicao != -1) {
                                    adapterChatList.notifyItemChanged(posicao);
                                }
                            }

                            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                Collections.sort(listaFiltrada, chatComparator);
                            }

                            if (referenceFiltroHashMap != null && referenceFiltroHashMap.size() > 0
                                    && listenerFiltroHashMap != null && listenerFiltroHashMap.size() > 0) {
                                referenceFiltroHashMap.remove(chatRemovido.getIdUsuario());
                                listenerFiltroHashMap.remove(chatRemovido.getIdUsuario());
                                if (idsListeners != null && idsListeners.size() > 0) {
                                    idsListeners.remove(chatRemovido.getIdUsuario());
                                }
                                if (idsFiltrados != null && idsFiltrados.size() > 0) {
                                    idsFiltrados.remove(chatRemovido.getIdUsuario());
                                }
                                ToastCustomizado.toastCustomizadoCurto("Removido", requireContext());
                            }
                        }

                        if (idsUsuarios != null && idsUsuarios.size() > 0
                                && idsUsuarios.contains(chatRemovido.getIdUsuario())) {
                            if (listaChat != null && listaChat.size() > 0) {
                                chatDiffDAO.removerChat(chatRemovido);
                            }
                            if (listaDadosUser != null && listaDadosUser.size() > 0) {
                                listaDadosUser.remove(chatRemovido.getIdUsuario());
                                int posicao = adapterChatList.findPositionInList(chatRemovido.getIdUsuario());
                                if (posicao != -1) {
                                    adapterChatList.notifyItemChanged(posicao);
                                }
                            }

                            if (listaChat != null && listaChat.size() > 0) {
                                Collections.sort(listaChat, chatComparator);
                            }

                            if (referenceHashMap != null && referenceHashMap.size() > 0
                                    && listenerHashMap != null && listenerHashMap.size() > 0) {
                                referenceHashMap.remove(chatRemovido.getIdUsuario());
                                listenerHashMap.remove(chatRemovido.getIdUsuario());
                                if (idsListeners != null && idsListeners.size() > 0) {
                                    idsListeners.remove(chatRemovido.getIdUsuario());
                                }
                                if (idsUsuarios != null && idsUsuarios.size() > 0) {
                                    idsUsuarios.remove(chatRemovido.getIdUsuario());
                                }
                                ToastCustomizado.toastCustomizadoCurto("Removido", requireContext());
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

    private void adicionarDadoDoUsuario(Usuario dadosUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        listaDadosUser.put(dadosUser.getIdUsuario(), dadosUser);

        if (childEventListenerAlvo == null || queryAlvo == null) {
            return;
        }

        if (isPesquisaAtivada()) {
            adicionarListenerFiltragem(dadosUser.getIdUsuario(), queryAlvo, childEventListenerAlvo);
        } else {
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

    private void adicionarListenerFiltragem(String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (idsFiltrados != null && idsFiltrados.size() > 0
                && idsFiltrados.contains(idUser)) {
            return;
        }
        referenceFiltroHashMap.put(idUser, queryAlvo);
        listenerFiltroHashMap.put(idUser, childEventListenerAlvo);
    }

    private void limparPeloDestroyView() {
        firebaseUtils.removerQueryChildListener(queryLoadMore, childEventListenerChat);
        firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childEventListenerFiltro);
        removeValueEventListener();
        removeValueEventListenerFiltro();
        if (chatDiffDAO != null) {
            chatDiffDAO.limparListaChats();
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
        if (searchView != null) {
            searchView.setQuery("", false);
        }
        if (searchView != null && fecharTeclado) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        if (chatDAOFiltrado != null) {
            chatDAOFiltrado.limparListaChats();
        }
        if (listaChat != null && listaChat.size() > 0) {
            adapterChatList.updateChatList(listaChat, new AdapterChatList.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
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
                    ToastCustomizado.toastCustomizadoCurto("LIMPO", requireContext());
                }
            }
        }
    }

    private void removeValueEventListenerFiltro() {
        if (listenerFiltroHashMap != null && referenceFiltroHashMap != null) {
            for (String userId : listenerFiltroHashMap.keySet()) {
                Query userRef = referenceFiltroHashMap.get(userId);
                ChildEventListener listener = listenerFiltroHashMap.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                }
            }
            referenceFiltroHashMap.clear();
            listenerFiltroHashMap.clear();
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
}