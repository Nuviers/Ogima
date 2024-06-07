package com.example.ogima.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterChatList;
import com.example.ogima.adapter.AdapterViewers;
import com.example.ogima.helper.ChatDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
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

public class ViewerDesbloqueadoFragment extends Fragment implements AdapterViewers.RecuperaPosicaoAnterior, AdapterViewers.RemoverViewerListener, AdapterViewers.AnimacaoIntent {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    //Filtragem
    private String idUsuario = "";
    private RecyclerView recyclerView;
    private SpinKitView spinProgress;
    private SearchView searchView;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private int mCurrentPosition = -1;
    private boolean isLoading = false;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Usuario> listaViewers = new ArrayList<>();
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO, usuarioDAOFiltrado;
    private Query queryInicial, queryLoadMore,
            queryInicialFiltro, queryLoadMorePesquisa, queryInicialFind,
            queryLoadMoreFiltro, newDataRef;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
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
    private TextView txtViewTitle;
    private HashMap<String, Query> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private HashMap<String, Query> referenceFiltroHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerFiltroHashMap = new HashMap<>();
    private Set<String> idsListeners = new HashSet<>();
    private ChildEventListener childListenerInicioFiltro,
            childEventListenerViewer, childListenerMoreFiltro,
            childListenerInicio, childEventListenerNewData;
    private AdapterViewers adapterViewers;
    private boolean trocarQueryInicial = false;
    private int contadorRemocaoListener = 0;
    private FirebaseUtils firebaseUtils;
    private int travar = 0;

    private Set<String> idsListenersNEWDATA = new HashSet<>();
    private HashMap<String, Query> referenceHashMapNEWDATA = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapNEWDATA = new HashMap<>();
    private int contadorRemocaoListenerNEWDATA = 0;
    private static final String TAG = "ViewersTAG";
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
        void onExistencia(boolean status, Usuario viewerAtualizado);

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
    public void onStop() {
        super.onStop();
        if (adapterViewers != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                listaViewers != null && listaViewers.size() > 0
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

    public ViewerDesbloqueadoFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewer_desbloqueado, container, false);
        inicializandoComponentes(view);
        configInicial();
        return view;
    }

    private void configInicial() {
        firebaseUtils = new FirebaseUtils();
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
                usuarioDiffDAO = new UsuarioDiffDAO(listaViewers, adapterViewers);
                usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterViewers);
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
                                        adapterViewers.updateViewerList(listaFiltrada, new AdapterViewers.ListaAtualizadaCallback() {
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
            recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                    DividerItemDecoration.VERTICAL));
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapterViewers = new AdapterViewers(requireContext(),
                    listaViewers, listaDadosUser,
                    getResources().getColor(R.color.chat_list_color), this, this, this);
            recyclerView.setAdapter(adapterViewers);
            adapterViewers.setStatusEpilepsia(epilepsia);
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
        if (listaViewers != null && listaViewers.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }

        ultimoElemento(new RecuperaUltimoElemento() {
            @Override
            public void onRecuperado() {
                if (trocarQueryInicial && lastTimestamp != -1) {
                    queryInicial = firebaseRef.child("profileViewsLiberada")
                            .child(idUsuario).orderByChild("timestampRevealed")
                            .startAt(lastTimestamp + 1)
                            .limitToFirst(1);
                } else {
                    queryInicial = firebaseRef.child("profileViewsLiberada")
                            .child(idUsuario).orderByChild("timestampRevealed").limitToFirst(1);
                }
                exibirProgress();
                childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Usuario viewer = snapshot.getValue(Usuario.class);
                            if (viewer != null
                                    && viewer.getIdUsuario() != null
                                    && !viewer.getIdUsuario().isEmpty()) {
                                if (travar == 0) {
                                    lastTimestamp = viewer.getTimestampRevealed();
                                    adicionarViewer(viewer, false);
                                } else {
                                    //Dado mais recente que o anterior
                                    if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                            && listenerHashMapNEWDATA.containsKey(viewer.getIdUsuario())) {
                                        return;
                                    }
                                    ToastCustomizado.toastCustomizadoCurto("Novo dado pelo inicio " + viewer.getIdUsuario(), requireContext());
                                    anexarNovoDado(viewer);
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
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Usuario.class).getIdUsuario())) {
                                return;
                            }
                            ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO INICIO", requireContext());
                            logicaAtualizacao(snapshot, false);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario viewerRemovido = snapshot.getValue(Usuario.class);
                            if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Usuario.class).getIdUsuario())) {
                                //O próprio listenernewdata vai cuidar da remoção desse dado.
                                return;
                            }

                            ToastCustomizado.toastCustomizado("DELETE INICIO", requireContext());
                            logicaRemocao(viewerRemovido, true, true);

                            verificaExistencia(viewerRemovido.getIdUsuario(), new VerificaExistenciaCallback() {
                                @Override
                                public void onExistencia(boolean status, Usuario viewerAtualizado) {
                                    if (status) {
                                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                                && listenerHashMapNEWDATA.containsKey(viewerRemovido.getIdUsuario())) {
                                        } else {
                                            ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do inicio " + viewerRemovido.getIdUsuario(), requireContext());
                                            anexarNovoDado(viewerAtualizado);
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
        queryInicialFind = firebaseRef.child("viewers_by_name")
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

                if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    ocultarProgress();
                    setLoading(false);
                    ToastCustomizado.toastCustomizadoCurto("Return listaFiltrada != null && listaFiltrada.size() >= 1", requireContext());
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

                                    //*ToastCustomizado.toastCustomizadoCurto("INICIO: " + usuarioAtual.getNomeUsuario(), requireContext());
                                    UsuarioUtils.checkBlockingStatus(requireContext(), usuarioAtual.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                                        @Override
                                        public void onBlocked(boolean status) {
                                            usuarioAtual.setIndisponivel(status);
                                            adicionarViewerFiltrado(usuarioAtual);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            usuarioAtual.setIndisponivel(true);
                                            adicionarViewerFiltrado(usuarioAtual);
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

    private void adicionarViewer(Usuario viewerAlvo, boolean dadoModificado) {
        recuperaDadosUser(viewerAlvo.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {

                usuarioDiffDAO.adicionarUsuario(viewerAlvo);
                usuarioDiffDAO.adicionarIdAoSet(idsUsuarios, dadosUser.getIdUsuario());

                List<Usuario> listaAtual = new ArrayList<>();
                if (isPesquisaAtivada()) {
                    listaAtual = listaFiltrada;
                } else {
                    listaAtual = listaViewers;
                }

                adapterViewers.updateViewerList(listaAtual, new AdapterViewers.ListaAtualizadaCallback() {
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

    private void adicionarViewerFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
            String idViewerInicioFiltro = listaFiltrada.get(0).getIdUsuario();
            if (idViewerInicioFiltro.equals(dadosUser.getIdUsuario())) {
                ocultarProgress();
                setLoading(false);
                return;
            }
        }

        queryInicialFiltro = firebaseRef.child("profileViewsLiberada")
                .child(idUsuario)
                .orderByChild("idUsuario")
                .equalTo(dadosUser.getIdUsuario()).limitToFirst(1);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    Usuario viewerAtual = snapshot.getValue(Usuario.class);

                    if (viewerAtual != null) {
                        viewerAtual.setIndisponivel(dadosUser.isIndisponivel());
                    }

                    if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
                        String idViewerInicioFiltro = listaFiltrada.get(0).getIdUsuario();
                        if (idViewerInicioFiltro.equals(dadosUser.getIdUsuario())) {
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }
                    }

                    lastName = dadosUser.getNomeUsuarioPesquisa();

                    usuarioDAOFiltrado.adicionarUsuario(viewerAtual);
                    usuarioDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());
                    adapterViewers.updateViewerList(listaFiltrada, new AdapterViewers.ListaAtualizadaCallback() {
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
                if (idsAIgnorarListeners != null && !idsAIgnorarListeners.isEmpty()
                        && idsAIgnorarListeners.contains(snapshot.getValue(Usuario.class).getIdUsuario())) {
                    ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Usuario.class).getIdUsuario(), requireContext());
                    return;
                }
                if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                        && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Usuario.class).getIdUsuario())) {
                    return;
                }

                if (listenerHashMap != null && !listenerHashMap.isEmpty()
                        && listenerHashMap.containsKey(snapshot.getValue(Usuario.class).getIdUsuario())) {
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
                    && idUltimoElementoFiltro.equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdUsuario())) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA ONE " + idUltimoElementoFiltro, requireContext());
                return;
            }

            //**ToastCustomizado.toastCustomizadoCurto("Last Name: " + lastName, requireContext());
            if (listaFiltrada != null && !listaFiltrada.isEmpty()
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMorePesquisa = firebaseRef.child("viewers_by_name")
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
                                        ToastCustomizado.toastCustomizadoCurto("RETORNO PESQUISA IF " + usuarioPesquisa.getIdUsuario(), requireContext());
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
            if (listaViewers.size() > 1
                    && idUltimoElemento != null && !idUltimoElemento.isEmpty()
                    && idUltimoElemento.equals(listaViewers.get(listaViewers.size() - 1).getIdUsuario())) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA " + idUltimoElemento, requireContext());
                return;
            }

            queryLoadMore = firebaseRef.child("profileViewsLiberada")
                    .child(idUsuario)
                    .orderByChild("timestampRevealed")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);
            childEventListenerViewer = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    exibirProgress();
                    if (snapshot.getValue() != null) {
                        Usuario viewerMore = snapshot.getValue(Usuario.class);
                        if (viewerMore != null
                                && viewerMore.getIdUsuario() != null
                                && !viewerMore.getIdUsuario().isEmpty()) {
                            Log.d(TAG, "Timestamp key: " + lastTimestamp);
                            Log.d(TAG, "id: " + viewerMore.getIdUsuario() + " time: " + viewerMore.getTimestampRevealed());
                            if (listaViewers != null && listaViewers.size() > 1 && idsUsuarios != null && !idsUsuarios.isEmpty()
                                    && idsUsuarios.contains(viewerMore.getIdUsuario())) {
                                Log.d(TAG, "Id já existia: " + viewerMore.getIdUsuario());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            if (listaViewers != null && listaViewers.size() > 1
                                    && viewerMore.getTimestampRevealed() < listaViewers.get(0).getTimestampRevealed()) {
                                ToastCustomizado.toastCustomizadoCurto("TIME IGNORADO", requireContext());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            List<Usuario> newViewer = new ArrayList<>();
                            long key = viewerMore.getTimestampRevealed();
                            if (lastTimestamp != -1 && key != -1) {
                                if (key != lastTimestamp || !listaViewers.isEmpty() &&
                                        !viewerMore.getIdUsuario()
                                                .equals(listaViewers.get(listaViewers.size() - 1).getIdUsuario())) {
                                    newViewer.add(viewerMore);
                                    //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                    lastTimestamp = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newViewer.size() > PAGE_SIZE) {
                                newViewer.remove(0);
                            }
                            if (lastTimestamp != -1) {
                                adicionarMaisDados(newViewer, viewerMore.getIdUsuario(), queryLoadMore);
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
                                && idsAIgnorarListeners.contains(snapshot.getValue(Usuario.class).getIdUsuario())) {
                            ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Usuario.class).getIdUsuario(), requireContext());
                            return;
                        }
                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Usuario.class).getIdUsuario())) {
                            return;
                        }
                        ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO CARREGAR + DADOS", requireContext());
                        logicaAtualizacao(snapshot, false);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario viewerRemovido = snapshot.getValue(Usuario.class);
                        if (viewerRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                && listenerHashMapNEWDATA.containsKey(viewerRemovido.getIdUsuario())
                                || listaViewers != null && !listaViewers.isEmpty()
                                && listaViewers.get(0).getIdUsuario().equals(viewerRemovido.getIdUsuario())) {
                            return;
                        }

                        verificaExistencia(viewerRemovido.getIdUsuario(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Usuario viewerAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + viewerRemovido.getIdUsuario(), requireContext());

                                logicaRemocao(viewerRemovido, true, true);

                                if (status) {
                                    boolean menorque = viewerAtualizado.getTimestampRevealed() <= listaViewers.get(0).getTimestampRevealed();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + viewerRemovido.getIdUsuario(), requireContext());
                                        anexarNovoDado(viewerAtualizado);
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

    private void adicionarMaisDados(List<Usuario> newViewer, String idUser, Query queryAlvo) {
        if (newViewer != null && !newViewer.isEmpty()) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    usuarioDiffDAO.carregarMaisUsuario(newViewer, idsUsuarios);
                    usuarioDiffDAO.adicionarIdAoSet(idsUsuarios, idUser);

                    adapterViewers.updateViewerList(listaViewers, new AdapterViewers.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerViewer, false);
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

    private void adicionarMaisDadosFiltrados(List<Usuario> newViewer, String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (newViewer != null && !newViewer.isEmpty()) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    usuarioDAOFiltrado.carregarMaisUsuario(newViewer, idsFiltrados);
                    usuarioDAOFiltrado.adicionarIdAoSet(idsFiltrados, idUser);
                    adapterViewers.updateViewerList(listaFiltrada, new AdapterViewers.ListaAtualizadaCallback() {
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
        if (idsListeners != null && !idsListeners.isEmpty()
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
        if (idsListenersNEWDATA != null && !idsListenersNEWDATA.isEmpty()
                && idsListenersNEWDATA.contains(idUser)) {
            return;
        }
        if (idsListenersNEWDATA != null) {
            idsListenersNEWDATA.add(idUser);
        }
        referenceHashMapNEWDATA.put(idUser, queryAlvo);
        listenerHashMapNEWDATA.put(idUser, childEventListenerAlvo);
    }

    private void limparPeloDestroyView() {
        idsAIgnorarListeners.clear();
        firebaseUtils.removerQueryChildListener(newDataRef, childEventListenerNewData);
        firebaseUtils.removerQueryChildListener(queryInicial, childListenerInicio);
        firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
        firebaseUtils.removerQueryChildListener(queryLoadMore, childEventListenerViewer);
        firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
        firebaseUtils.removerQueryValueListener(queryUltimoElemento, listenerUltimoElemento);
        removeValueEventListener();
        removeValueEventListenerNEWDATA();
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
        if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
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

    private void limparFiltragem(boolean fecharTeclado) {
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
        if (usuarioDAOFiltrado != null) {
            usuarioDAOFiltrado.limparListaUsuarios();
        }
        if (listaViewers != null && !listaViewers.isEmpty()) {
            setLoading(false);
            adapterViewers.updateViewerList(listaViewers, new AdapterViewers.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                    contadorUpdate++;
                    if (idsParaAtualizar != null && !idsParaAtualizar.isEmpty()) {
                        for (String idUpdate : idsParaAtualizar.keySet()) {
                            int index = adapterViewers.findPositionInList(idUpdate);
                            Bundle bundleUpdate = idsParaAtualizar.get(idUpdate);
                            if (index != -1) {
                                adapterViewers.notifyItemChanged(index, bundleUpdate);
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

    private void logicaRemocao(Usuario viewerRemovido, boolean ignorarVerificacao, boolean excluirDaLista) {

        if (viewerRemovido == null) {
            return;
        }

        DatabaseReference verificaExistenciaRef = firebaseRef.child("profileViewsLiberada")
                .child(idUsuario).child(viewerRemovido.getIdUsuario());
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || ignorarVerificacao) {

                    if (idsFiltrados != null && !idsFiltrados.isEmpty()
                            && idsFiltrados.contains(viewerRemovido.getIdUsuario())) {
                        if (listaFiltrada != null && !listaFiltrada.isEmpty() && excluirDaLista) {
                            if (idsFiltrados != null && !idsFiltrados.isEmpty()) {
                                idsFiltrados.remove(viewerRemovido.getIdUsuario());
                            }
                            usuarioDAOFiltrado.removerUsuario(viewerRemovido);
                        }
                    }

                    if (idsUsuarios != null && !idsUsuarios.isEmpty()
                            && idsUsuarios.contains(viewerRemovido.getIdUsuario())) {
                        if (listaViewers != null && !listaViewers.isEmpty() && excluirDaLista) {
                            if (idsUsuarios != null && !idsUsuarios.isEmpty()) {
                                idsUsuarios.remove(viewerRemovido.getIdUsuario());
                            }
                            usuarioDiffDAO.removerUsuario(viewerRemovido);
                        }
                    }

                    if (listaDadosUser != null && !listaDadosUser.isEmpty() && excluirDaLista) {
                        listaDadosUser.remove(viewerRemovido.getIdUsuario());
                        int posicao = adapterViewers.findPositionInList(viewerRemovido.getIdUsuario());
                        if (posicao != -1) {
                            adapterViewers.notifyItemChanged(posicao);
                        }
                    }

                    if (isPesquisaAtivada() && listaFiltrada != null) {
                        adapterViewers.updateViewerList(listaFiltrada, new AdapterViewers.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {

                            }
                        });
                    } else if (!isPesquisaAtivada() && listaViewers != null) {
                        adapterViewers.updateViewerList(listaViewers, new AdapterViewers.ListaAtualizadaCallback() {
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
            Usuario viewerAtualizado = snapshot.getValue(Usuario.class);
            if (viewerAtualizado == null || viewerAtualizado.getIdUsuario() == null) {
                return;
            }

            posicaoChanged = adapterViewers.findPositionInList(viewerAtualizado.getIdUsuario());

            if (posicaoChanged == -1 && isPesquisaAtivada()) {
                //Não existe esse chat na lista filtrada mas existe na lista normal.
                posicaoChanged = findPositionInList(viewerAtualizado.getIdUsuario());
            }

            if (posicaoChanged != -1) {
                Usuario viewerAnterior = new Usuario();
                if (idsUsuarios != null && !idsUsuarios.isEmpty()
                        && idsUsuarios.contains(viewerAtualizado.getIdUsuario())) {
                    //Já existe um listener na listagem normal
                    if (isPesquisaAtivada()
                            && referenceFiltroHashMap != null
                            && !referenceFiltroHashMap.isEmpty()
                            && referenceFiltroHashMap.containsKey(viewerAtualizado.getIdUsuario())) {
                        viewerAnterior = listaFiltrada.get(posicaoChanged);
                    } else {
                        viewerAnterior = listaViewers.get(posicaoChanged);
                    }
                } else if (isPesquisaAtivada()
                        && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    //Somente existe um listener desse chat na listagem filtrada.
                    viewerAnterior = listaFiltrada.get(posicaoChanged);
                }
                ToastCustomizado.toastCustomizadoCurto("Alterado: " + viewerAnterior.getIdUsuario(), requireContext());

                if (isPesquisaAtivada() && listaFiltrada != null) {
                    adapterViewers.updateViewerList(listaFiltrada, new AdapterViewers.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                } else if (!isPesquisaAtivada() && listaViewers != null) {

                    adapterViewers.updateViewerList(listaViewers, new AdapterViewers.ListaAtualizadaCallback() {
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

    private void atualizarPorPayload(Usuario viewerAtualizado, String tipoPayload) {
        ToastCustomizado.toastCustomizadoCurto(tipoPayload, requireContext());

        int index = posicaoChanged;

        if (index != -1) {

            if (isPesquisaAtivada() && referenceFiltroHashMap != null
                    && !referenceFiltroHashMap.isEmpty()
                    && referenceFiltroHashMap.containsKey(viewerAtualizado.getIdUsuario())) {
                ToastCustomizado.toastCustomizadoCurto("CODE NOOOO", requireContext());
                usuarioDAOFiltrado.atualizarUsuarioPorPayload(viewerAtualizado, tipoPayload, new UsuarioDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        adapterViewers.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
            if (idsUsuarios != null && !idsUsuarios.isEmpty()
                    && idsUsuarios.contains(viewerAtualizado.getIdUsuario())) {
                ToastCustomizado.toastCustomizadoCurto("CODE OK", requireContext());
                usuarioDiffDAO.atualizarUsuarioPorPayload(viewerAtualizado, tipoPayload, new UsuarioDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        if (!isPesquisaAtivada()) {
                            adapterViewers.notifyItemChanged(index, bundleRecup);
                        } else {
                            idsParaAtualizar.put(viewerAtualizado.getIdUsuario(), bundleRecup);
                        }
                    }
                });
            }
        }
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

    private void verificaExistencia(String idViewer, VerificaExistenciaCallback callback) {
        DatabaseReference verificaExistenciaRef = firebaseRef.child("profileViewsLiberada")
                .child(idUsuario).child(idViewer);
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onExistencia(snapshot.getValue() != null, snapshot.getValue(Usuario.class));
                verificaExistenciaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void anexarNovoDado(Usuario viewerModificado) {
        newDataRef = firebaseRef.child("profileViewsLiberada")
                .child(idUsuario).orderByChild("idUsuario")
                .equalTo(viewerModificado.getIdUsuario()).limitToFirst(1);
        idsAIgnorarListeners.add(viewerModificado.getIdUsuario());
        childEventListenerNewData = newDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Usuario viewerModificado = snapshot.getValue(Usuario.class);
                    if (viewerModificado == null) {
                        return;
                    }
                    adicionarViewer(viewerModificado, true);
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
                    Usuario viewerRemovido = snapshot.getValue(Usuario.class);
                    ToastCustomizado.toastCustomizado("DELETE PELO NEW DATA", requireContext());
                    if (idsAIgnorarListeners != null && !idsAIgnorarListeners.isEmpty()
                            && idsAIgnorarListeners.contains(viewerRemovido.getIdUsuario())) {
                        idsAIgnorarListeners.remove(viewerRemovido.getIdUsuario());
                    }
                    logicaRemocao(viewerRemovido, true, true);
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

            queryLoadMoreFiltro = firebaseRef.child("profileViewsLiberada")
                    .child(idUsuario).orderByChild("idUsuario").equalTo(usuarioPesquisa.getIdUsuario()).limitToFirst(1);

            //ToastCustomizado.toastCustomizadoCurto("AOS FILTROS: " + usuarioPesquisa.getIdUsuario(), requireContext());
            //ToastCustomizado.toastCustomizadoCurto("NR FILTROS: " + aosFiltros, requireContext());

            ChildEventListener childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Usuario viewerMore = snapshot.getValue(Usuario.class);
                    if (viewerMore != null
                            && viewerMore.getIdUsuario() != null
                            && !viewerMore.getIdUsuario().isEmpty()) {

                        //**ToastCustomizado.toastCustomizadoCurto("NEW PESQUISA: " + chatMore.getIdUsuario(), requireContext());
                        Log.d(TAG, "Timestamp key: " + lastTimestamp);
                        Log.d(TAG, "id: " + viewerMore.getIdUsuario() + " time: " + viewerMore.getTimestampRevealed());
                        if (listaFiltrada != null && listaFiltrada.size() > 1 && idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(viewerMore.getIdUsuario())) {
                            Log.d(TAG, "Id já existia: " + viewerMore.getIdUsuario());
                            ToastCustomizado.toastCustomizadoCurto("ID JÁ EXISTIA " + viewerMore.getIdUsuario(), requireContext());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        List<Usuario> newViewer = new ArrayList<>();
                        String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                        if (lastName != null && !lastName.isEmpty() && key != null
                                && !key.isEmpty()) {
                            if (!key.equals(lastName) || !listaFiltrada.isEmpty() &&
                                    !viewerMore.getIdUsuario()
                                            .equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdUsuario())) {
                                newViewer.add(viewerMore);
                                //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                lastName = key;
                            }
                        }
                        // Remove a última chave usada
                        if (newViewer.size() > PAGE_SIZE) {
                            newViewer.remove(0);
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (aosFiltros >= listaIdsRecuperados.size()) {
                                aosFiltros = 0;
                            }
                            adicionarMaisDadosFiltrados(newViewer, viewerMore.getIdUsuario(), queryLoadMoreFiltro, childListenerMoreFiltro);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() == null) {
                        return;
                    }
                    Usuario viewerUpdate = snapshot.getValue(Usuario.class);

                    if (viewerUpdate == null) {
                        return;
                    }

                    if (idsAIgnorarListeners != null && !idsAIgnorarListeners.isEmpty()
                            && idsAIgnorarListeners.contains(snapshot.getValue(Usuario.class).getIdUsuario())) {
                        ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Chat.class).getIdUsuario(), requireContext());
                        return;
                    }
                    if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                            && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Usuario.class).getIdUsuario())) {
                        return;
                    }

                    if (listenerHashMap != null && !listenerHashMap.isEmpty()
                            && listenerHashMap.containsKey(snapshot.getValue(Usuario.class).getIdUsuario())) {
                        return;
                    }

                    if (listaFiltrada != null && !listaFiltrada.isEmpty()
                            && viewerUpdate.getIdUsuario().equals(listaFiltrada.get(0).getIdUsuario())) {
                        return;
                    }

                    ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO SEARCH + DADOS " + viewerUpdate.getIdUsuario(), requireContext());
                    logicaAtualizacao(snapshot, false);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario viewerRemovido = snapshot.getValue(Usuario.class);
                        if (viewerRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                && listenerHashMapNEWDATA.containsKey(viewerRemovido.getIdUsuario())
                                || listaViewers != null && !listaViewers.isEmpty()
                                && listaViewers.get(0).getIdUsuario().equals(viewerRemovido.getIdUsuario())) {
                            return;
                        }

                        verificaExistencia(viewerRemovido.getIdUsuario(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Usuario viewerAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + viewerRemovido.getIdUsuario(), requireContext());

                                logicaRemocao(viewerRemovido, true, true);

                                if (status) {
                                    boolean menorque = viewerAtualizado.getTimestampRevealed() <= listaViewers.get(0).getTimestampRevealed();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + viewerRemovido.getIdUsuario(), requireContext());
                                        anexarNovoDado(viewerAtualizado);
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
        queryUltimoElemento = firebaseRef.child("profileViewsLiberada")
                .child(idUsuario).orderByChild("timestampRevealed").limitToLast(1);
        listenerUltimoElemento = queryUltimoElemento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElemento = snapshot1.getValue(Usuario.class).getIdUsuario();
                    setLoading(false);
                    if (callback != null && listaViewers != null) {
                        callback.onRecuperado();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaViewers != null) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void ultimoElementoFiltro(String nome, RecuperaUltimoElemento callback) {
        queryUltimoElementoFiltro = firebaseRef.child("viewers_by_name")
                .child(idUsuario)
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToLast(1);
        listenerUltimoElementoFiltro = queryUltimoElementoFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElementoFiltro = snapshot1.getValue(Usuario.class).getIdUsuario();
                    setLoading(false);
                    if (callback != null && listaViewers != null && !listaViewers.isEmpty()) {
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

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, requireActivity());
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, requireActivity());
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onRemocao(Usuario viewerAlvo, int posicao) {
        if (viewerAlvo != null) {
            logicaRemocao(viewerAlvo, false, true);
        }
    }

    @Override
    public void onExecutarAnimacao() {
        requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public int findPositionInList(String userId) {
        for (int i = 0; i < listaViewers.size(); i++) {
            Usuario viewer = listaViewers.get(i);
            if (viewer.getIdUsuario().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }

    private void inicializandoComponentes(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewViewers);
        searchView = view.findViewById(R.id.searchViewViewers);
        spinProgress = view.findViewById(R.id.spinProgressBarRecycler);
    }
}