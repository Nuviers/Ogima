package com.example.ogima.fragment;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterBasicUser;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
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

public class FollowingFragment extends Fragment implements AdapterBasicUser.AnimacaoIntent, AdapterBasicUser.RecuperaPosicaoAnterior, AdapterBasicUser.DeixouDeSeguirCallback {

    private String idUsuario = "";
    private String idDonoPerfil = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private boolean primeiroCarregamento = true;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private Query queryInicial, queryLoadMore;
    //Dados do usuário
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    //Filtragem
    private Set<String> idsFiltrados = new HashSet<>();
    private UsuarioDiffDAO usuarioDAOFiltrado;
    private Query queryInicialFiltro, queryLoadMoreFiltro;
    private String nomePesquisado = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private AdapterBasicUser adapterBasicUser;
    private boolean pesquisaAtivada = false;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private long lastTimestamp = -1;
    private HashMap<String, Object> listaSeguindo = new HashMap<>();
    private Set<String> idsListeners = new HashSet<>();
    private DatabaseReference recuperarSeguindoRef;
    private boolean atualizandoLista = false;
    private HashMap<String, DatabaseReference> referenceHashMap = new HashMap<>();
    private HashMap<String, ValueEventListener> listenerHashMap = new HashMap<>();
    private HashMap<String, DatabaseReference> referenceFiltroHashMap = new HashMap<>();
    private HashMap<String, ValueEventListener> listenerFiltroHashMap = new HashMap<>();

    private int updateCounter = 0;
    private long lastUpdateTime = 0;
    private static final int MAX_UPDATES_PER_MINUTE = 2;
    private boolean refreshEmAndamento = false;
    private ValueEventListener listenerFiltragem;

    private Handler searchHandler = new Handler();
    private int queryDelayMillis = 500;
    private int searchCounter = 0;
    private String currentSearchText = "";
    private SpinKitView spinProgressBarFoll;

    @Override
    public void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            setPesquisaAtivada(false);
            configRecycler();
            configSearchView();
            usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterBasicUser);
            usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterBasicUser);
            setLoading(true);
            recuperarDadosIniciais();
            configPaginacao();
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterBasicUser != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                listaUsuarios != null && listaUsuarios.size() > 0
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

    private interface RecuperarTimeStampCallback {
        void onRecuperado(long timeStamp);

        void onError(String message);
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

    public boolean isRefreshEmAndamento() {
        return refreshEmAndamento;
    }

    public void setRefreshEmAndamento(boolean refreshEmAndamento) {
        this.refreshEmAndamento = refreshEmAndamento;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    public FollowingFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        inicializarComponentes(view);
        receberDados();
        if (idDonoPerfil == null || idDonoPerfil.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), requireContext());
            requireActivity().onBackPressed();
        } else {
            UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                @Override
                public void onConcluido(boolean epilepsia) {
                    adapterBasicUser.setStatusEpilepsia(epilepsia);
                    clickListeners();
                }

                @Override
                public void onSemDado() {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), requireContext());
                    requireActivity().onBackPressed();
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                    requireActivity().onBackPressed();
                }
            });
        }
        return view;
    }

    private void receberDados() {
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("idDonoPerfil")) {
                idDonoPerfil = args.getString("idDonoPerfil");
            }
        }
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (adapterBasicUser == null) {
            adapterBasicUser = new AdapterBasicUser(requireContext(),
                    listaUsuarios, this, this, listaDadosUser, listaSeguindo, this, "#708090");
        }
        recyclerView.setAdapter(adapterBasicUser);
        adapterBasicUser.setFiltragem(false);
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
                    swipeRefresh.setRefreshing(false);
                    searchHandler.removeCallbacksAndMessages(null);
                    searchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (newText != null && !newText.isEmpty()) {
                                if (newText.equals(currentSearchText)) {
                                    exibirProgress();
                                    searchCounter++;
                                    final int counter = searchCounter;
                                    adapterBasicUser.setFiltragem(true);
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

    private void limparFiltragem(boolean fecharTeclado) {
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
        adapterBasicUser.setFiltragem(false);
        setPesquisaAtivada(false);
        nomePesquisado = "";
        ocultarProgress();
        if (usuarioDAOFiltrado != null) {
            usuarioDAOFiltrado.limparListaUsuarios();
        }
        if (listaUsuarios != null && listaUsuarios.size() > 0) {
            adapterBasicUser.updateUsersList(listaUsuarios, new AdapterBasicUser.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                }
            });
        }
    }

    private void recuperarDadosIniciais() {
        queryInicial = firebaseRef.child("seguindo")
                .child(idDonoPerfil)
                .orderByChild("timestampinteracao").limitToFirst(1);
        queryInicial.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Usuario usuarioChildren = snapshotChildren.getValue(Usuario.class);
                        if (usuarioChildren != null && usuarioChildren.getIdUsuario() != null
                                && !usuarioChildren.getIdUsuario().isEmpty()) {
                            adicionarUser(usuarioChildren);
                            lastTimestamp = usuarioChildren.getTimestampinteracao();
                        }
                    }
                }
                if (queryInicial != null) {
                    queryInicial.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
            }
        });
    }

    private void adicionarUser(Usuario usuarioViewer) {
        if (listaUsuarios != null && listaUsuarios.size() >= 1) {
            setLoading(false);
            return;
        }

        recuperaDadosUser(usuarioViewer.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());
                usuarioDiffDAO.adicionarUsuario(usuarioViewer);
                idsUsuarios.add(usuarioViewer.getIdUsuario());
                adapterBasicUser.updateUsersList(listaUsuarios, new AdapterBasicUser.ListaAtualizadaCallback() {
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
                UsuarioUtils.verificaBlock(usuarioAtual.getIdUsuario(), requireContext(), new UsuarioUtils.VerificaBlockCallback() {
                    @Override
                    public void onBloqueado() {
                        usuarioAtual.setIndisponivel(true);
                    }

                    @Override
                    public void onDisponivel() {
                        usuarioAtual.setIndisponivel(false);
                    }

                    @Override
                    public void onError(String message) {
                        usuarioAtual.setIndisponivel(false);
                    }
                });
                callback.onRecuperado(usuarioAtual);
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
        verificaSeguindo(dadosUser);
    }

    private void dadoInicialFiltragem(String nome, int counter) {
        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(1);

        listenerFiltragem = queryInicialFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (counter != searchCounter) {
                    limparFiltragem(false);
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
                                    swipeRefresh.setRefreshing(false);
                                }
                            });
                        }
                    }
                }
                if (queryInicialFiltro != null && listenerFiltragem != null) {
                    queryInicialFiltro.removeEventListener(listenerFiltragem);
                }
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
        DatabaseReference verificaRef = firebaseRef.child("seguindo")
                .child(idDonoPerfil).child(idAlvo);
        verificaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onCriterioAtendido();
                } else {
                    callback.onSemVinculo();
                }
                verificaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
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
                                            if (lastName != null && key != null && !key.equals(lastName)) {
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

                                        }

                                        @Override
                                        public void onError(String message) {

                                        }
                                    });
                                }
                            }
                        }
                        if (queryLoadMoreFiltro != null) {
                            queryLoadMoreFiltro.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        lastName = null;
                    }
                });
            }
        } else {
            queryLoadMore = firebaseRef.child("seguindo")
                    .child(idDonoPerfil)
                    .orderByChild("timestampinteracao")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);
            queryLoadMore.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                            Usuario usuarioChildren = snapshotChildren.getValue(Usuario.class);
                            if (usuarioChildren != null && usuarioChildren.getIdUsuario() != null
                                    && !usuarioChildren.getIdUsuario().isEmpty()) {
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
                    }
                    if (queryLoadMore != null) {
                        queryLoadMore.removeEventListener(this);
                    }
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
                    adapterBasicUser.updateUsersList(listaUsuarios, new AdapterBasicUser.ListaAtualizadaCallback() {
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
            adapterBasicUser.updateUsersList(listaFiltrada, new AdapterBasicUser.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    adicionarDadoDoUsuario(dadosUser);
                    setLoading(false);
                }
            });
        }
    }

    private void adicionarUserFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            return;
        }
        lastName = dadosUser.getNomeUsuarioPesquisa();
        usuarioDAOFiltrado.adicionarUsuario(dadosUser);
        idsFiltrados.add(dadosUser.getIdUsuario());
        adapterBasicUser.updateUsersList(listaFiltrada, new AdapterBasicUser.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                adicionarDadoDoUsuario(dadosUser);
                carregarMaisDados(nomePesquisado);
                setLoading(false);
            }
        });
    }

    private void verificaSeguindo(Usuario usuarioAlvo) {
        if (!isPesquisaAtivada()) {
            idsListeners.add(usuarioAlvo.getIdUsuario());
        }
        recuperarSeguindoRef = firebaseRef.child("seguindo")
                .child(idUsuario).child(usuarioAlvo.getIdUsuario());
        ValueEventListener newListener = recuperarSeguindoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    listaSeguindo.put(usuarioAlvo.getIdUsuario(), usuarioAlvo);
                    int posicao = adapterBasicUser.findPositionInList(usuarioAlvo.getIdUsuario());
                    if (posicao != -1) {
                        adapterBasicUser.notifyItemChanged(adapterBasicUser.findPositionInList(usuarioAlvo.getIdUsuario()));
                    }
                } else {
                    if (listaSeguindo != null && listaSeguindo.size() > 0
                            && listaSeguindo.containsKey(usuarioAlvo.getIdUsuario())) {
                        listaSeguindo.remove(usuarioAlvo.getIdUsuario());
                        int posicao = adapterBasicUser.findPositionInList(usuarioAlvo.getIdUsuario());
                        if (posicao != -1) {
                            adapterBasicUser.notifyItemChanged(adapterBasicUser.findPositionInList(usuarioAlvo.getIdUsuario()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (isPesquisaAtivada()) {
            referenceFiltroHashMap.put(usuarioAlvo.getIdUsuario(), recuperarSeguindoRef);
            listenerFiltroHashMap.put(usuarioAlvo.getIdUsuario(), newListener);
        } else {
            referenceHashMap.put(usuarioAlvo.getIdUsuario(), recuperarSeguindoRef);
            listenerHashMap.put(usuarioAlvo.getIdUsuario(), newListener);
        }
    }

    private void clickListeners() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    private void inicializarComponentes(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefreshFollowers);
        recyclerView = view.findViewById(R.id.recyclerViewFollowers);
        searchView = view.findViewById(R.id.searchViewFollowers);
        spinProgressBarFoll = view.findViewById(R.id.spinProgressBarFoll);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            if (searchView != null) {
                searchView.clearFocus();
            }
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onExecutarAnimacao() {
        requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void removeValueEventListener() {
        if (listenerHashMap != null && referenceHashMap != null) {
            for (String userId : listenerHashMap.keySet()) {
                DatabaseReference userRef = referenceHashMap.get(userId);
                ValueEventListener listener = listenerHashMap.get(userId);
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
                ValueEventListener listener = listenerFiltroHashMap.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                }
            }
            referenceFiltroHashMap.clear();
            listenerFiltroHashMap.clear();
        }
    }

    @Override
    public void onRemover(Usuario usuarioAlvo) {
        //Remover o id do hashmapSeguindo
        if (listaSeguindo != null && listaSeguindo.size() > 0
                && listaSeguindo.containsKey(usuarioAlvo.getIdUsuario())) {
            listaSeguindo.remove(usuarioAlvo.getIdUsuario());
            int posicao = adapterBasicUser.findPositionInList(usuarioAlvo.getIdUsuario());
            if (posicao != -1) {
                adapterBasicUser.notifyItemChanged(adapterBasicUser.findPositionInList(usuarioAlvo.getIdUsuario()));
            }
        }
    }

    private void recuperarTimestamp(RecuperarTimeStampCallback callback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(requireContext(), new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onRecuperado(timestamps);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.connection_error_occurred), errorMessage), requireContext());
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }

    private boolean isAllowedToUpdate(long timeStamp) {
        long timeSinceLastUpdate = timeStamp - lastUpdateTime;

        if (timeSinceLastUpdate > 60000) { // Passou mais de 1 minuto desde a última atualização
            // Reiniciar o contador de atualizações
            updateCounter = 0;
        }
        return updateCounter < MAX_UPDATES_PER_MINUTE;
    }

    private void exibirProgress() {
        spinProgressBarFoll.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgressBarFoll, requireActivity());
    }

    private void ocultarProgress() {
        spinProgressBarFoll.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgressBarFoll, requireActivity());
    }

    private void refreshData() {
        if (isRefreshEmAndamento()) {
            return;
        }
        setRefreshEmAndamento(true);
        recuperarTimestamp(new RecuperarTimeStampCallback() {
            @Override
            public void onRecuperado(long timeStamp) {
                if (isAllowedToUpdate(timeStamp)) {
                    // Atualizar o contador
                    updateCounter++;
                    lastUpdateTime = timeStamp;
                    setLoading(true);
                    if (searchHandler != null) {
                        searchHandler.removeCallbacksAndMessages(null);
                    }
                    setPesquisaAtivada(false);
                    removeValueEventListener();
                    removeValueEventListenerFiltro();
                    if (recyclerView != null) {
                        recyclerView.removeOnScrollListener(scrollListener);
                    }
                    mCurrentPosition = -1;
                    if (listaUsuarios != null && listaUsuarios.size() > 0) {
                        usuarioDiffDAO.limparListaUsuarios();
                        adapterBasicUser.updateUsersList(listaUsuarios, new AdapterBasicUser.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {
                                if (listaSeguindo != null && listaSeguindo.size() > 0) {
                                    listaSeguindo.clear();
                                }
                                if (listaDadosUser != null && listaDadosUser.size() > 0) {
                                    listaDadosUser.clear();
                                }
                                if (idsUsuarios != null && idsUsuarios.size() > 0) {
                                    idsUsuarios.clear();
                                }
                                if (idsListeners != null && idsListeners.size() > 0) {
                                    idsListeners.clear();
                                }
                                queryInicial = null;
                                queryLoadMore = null;
                                queryInicialFiltro = null;
                                queryLoadMoreFiltro = null;
                                nomePesquisado = null;
                                lastName = null;
                                lastTimestamp = -1;
                                recuperarSeguindoRef = null;
                                if (searchView != null) {
                                    searchView.setQuery("", false);
                                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                                    searchView.clearFocus();
                                }
                                if (listaFiltrada != null && listaFiltrada.size() > 0) {
                                    usuarioDAOFiltrado.limparListaUsuarios();
                                    adapterBasicUser.updateUsersList(listaFiltrada, new AdapterBasicUser.ListaAtualizadaCallback() {
                                        @Override
                                        public void onAtualizado() {

                                        }
                                    });
                                }
                                recuperarDadosIniciais();
                                if (isRefreshEmAndamento()) {
                                    adapterBasicUser.setFiltragem(false);
                                    atualizandoLista = false;
                                    configPaginacao();
                                    swipeRefresh.setRefreshing(false);
                                    setRefreshEmAndamento(false);
                                }
                            }
                        });
                    }
                } else {
                    setRefreshEmAndamento(false);
                    swipeRefresh.setRefreshing(false);
                    ToastCustomizado.toastCustomizadoCurto("Aguarde um minuto para a próxima atualização.", requireContext());
                }
            }

            @Override
            public void onError(String message) {

            }
        });
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
        if (listaSeguindo != null) {
            listaSeguindo.clear();
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
}