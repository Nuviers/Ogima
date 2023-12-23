package com.example.ogima.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterRequests;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
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

public class FriendshipRequestFragmentNew extends Fragment implements AdapterRequests.RecuperaPosicaoAnterior, AdapterRequests.AnimacaoIntent, AdapterRequests.RemoverConviteListener {

    private String idUsuario = "";
    private String idDonoPerfil = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private static int PAGE_SIZE_MORE = 0;
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
    private AdapterRequests adapterRequests;
    //Filtragem
    private Set<String> idsFiltrados = new HashSet<>();
    private UsuarioDiffDAO usuarioDAOFiltrado;
    private Query queryInicialFiltro, queryLoadMoreFiltro;
    private String nomePesquisado = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private boolean pesquisaAtivada = false;
    private long lastTimestamp = -1;
    private boolean atualizandoLista = false;
    private ValueEventListener listenerFiltragem;
    private Handler searchHandler = new Handler();
    private int queryDelayMillis = 500;
    private int searchCounter = 0;
    private String currentSearchText = "";
    private TextView txtViewTitle;

    @Override
    public void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            setPesquisaAtivada(false);
            configRecycler();
            configSearchView();
            usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterRequests);
            usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterRequests);
            setLoading(true);
            recuperarDadosIniciais();
            configPaginacao();
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterRequests != null && linearLayoutManager != null
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    public void onRemocao(Usuario usuarioRemetente, int posicao) {
        //Remover convite
        if (usuarioDiffDAO != null && listaUsuarios != null
        && listaUsuarios.size() > 0) {
            usuarioDiffDAO.removerUsuario(usuarioRemetente);
        }
        if (usuarioDAOFiltrado != null  && listaFiltrada != null
                && listaFiltrada.size() > 0) {
            usuarioDAOFiltrado.removerUsuario(usuarioRemetente);
        }
        if (listaDadosUser != null && listaDadosUser.size() > 0) {
            listaDadosUser.remove(usuarioRemetente.getIdUsuario());
        }
        adapterRequests.updateUsersList(listaUsuarios, new AdapterRequests.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
            }
        });
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

    public FriendshipRequestFragmentNew() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendship_request, container, false);
        inicializarComponentes(view);
        txtViewTitle.setText(getString(R.string.requests));
        receberDados();
        if (idDonoPerfil == null || idDonoPerfil.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), requireContext());
            requireActivity().onBackPressed();
        } else {
            UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                @Override
                public void onConcluido(boolean epilepsia) {
                    adapterRequests.setStatusEpilepsia(epilepsia);
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

    private void receberDados() {
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("idDonoPerfil")) {
                idDonoPerfil = args.getString("idDonoPerfil");
            }
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
        ProgressBarUtils.exibirProgressBar(spinProgress, requireActivity());
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, requireActivity());
    }

    private void limparFiltragem(boolean fecharTeclado) {
        if (searchView != null && fecharTeclado) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            searchView.clearFocus();
        }
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
            adapterRequests.updateUsersList(listaUsuarios, new AdapterRequests.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                }
            });
        }
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (adapterRequests == null) {
            adapterRequests = new AdapterRequests(requireContext(),
                    listaUsuarios, this, this, listaDadosUser, requireContext().getResources().getColor(R.color.followers_color), idDonoPerfil, this);
        }
        recyclerView.setAdapter(adapterRequests);
    }

    private void recuperarDadosIniciais() {
        queryInicial = firebaseRef.child("requestsFriendship")
                .child(idDonoPerfil).orderByChild("timestampinteracao").limitToFirst(1);

        queryInicial.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listaUsuarios != null && listaUsuarios.size() >= 1) {
                    return;
                }
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Usuario usuarioChildren = snapshotChildren.getValue(Usuario.class);
                        if (usuarioChildren != null && usuarioChildren.getIdDestinatario() != null
                                && !usuarioChildren.getIdRemetente().isEmpty()
                                && !usuarioChildren.getIdRemetente().equals(idDonoPerfil)) {
                            adicionarUser(usuarioChildren);
                            lastTimestamp = usuarioChildren.getTimestampinteracao();
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
        recuperaDadosUser(usuarioAlvo.getIdRemetente(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                usuarioDiffDAO.adicionarUsuario(dadosUser);
                idsUsuarios.add(dadosUser.getIdUsuario());
                adapterRequests.updateUsersList(listaUsuarios, new AdapterRequests.ListaAtualizadaCallback() {
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
                UsuarioUtils.checkBlockingStatus(requireContext(), usuarioAtual.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
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
                                && !usuarioPesquisa.getIdUsuario().equals(idDonoPerfil)) {
                            verificaVinculo(usuarioPesquisa.getIdUsuario(), new VerificaCriterio() {
                                @Override
                                public void onCriterioAtendido() {
                                    UsuarioUtils.checkBlockingStatus(requireContext(), usuarioPesquisa.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                                        @Override
                                        public void onBlocked(boolean status) {
                                            usuarioPesquisa.setIndisponivel(status);
                                            adicionarUserFiltrado(usuarioPesquisa);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            usuarioPesquisa.setIndisponivel(true);
                                            adicionarUserFiltrado(usuarioPesquisa);
                                        }
                                    });
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
        DatabaseReference verificaVinculoRef = firebaseRef.child("requestsFriendship")
                .child(idDonoPerfil).child(idAlvo).child("idDestinatario");
        verificaVinculoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String idDestinatario = snapshot.getValue(String.class);
                    if (idDestinatario != null
                            && idDestinatario.equals(idDonoPerfil)) {
                        callback.onCriterioAtendido();
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
        adapterRequests.updateUsersList(listaFiltrada, new AdapterRequests.ListaAtualizadaCallback() {
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
            ToastCustomizado.toastCustomizadoCurto("More " + dadoAnterior, requireContext());
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
                                        && !usuarioPesquisa.getIdUsuario().equals(idDonoPerfil)) {
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
                                            if(lastName != null && !lastName.isEmpty() && key != null && !key.equals(lastName)){
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
            //**ToastCustomizado.toastCustomizadoCurto("SEM FILTRO", requireContext());
            queryLoadMore = firebaseRef.child("requestsFriendship")
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
                            if (usuarioChildren != null && usuarioChildren.getIdRemetente() != null
                                    && !usuarioChildren.getIdRemetente().isEmpty()
                                    && !usuarioChildren.getIdRemetente().equals(idDonoPerfil)) {
                                usuarioChildren.setIdUsuario(usuarioChildren.getIdRemetente());
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
                                    adicionarMaisDados(newUsuario, usuarioChildren.getIdRemetente());
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
    }

    private void adicionarMaisDados(List<Usuario> newUsuario, String idUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    usuarioDiffDAO.carregarMaisUsuario(newUsuario, idsUsuarios);
                    //*Usuario usuarioComparator = new Usuario(true, false);
                    //*Collections.sort(listaViewers, usuarioComparator);
                    adapterRequests.updateUsersList(listaUsuarios, new AdapterRequests.ListaAtualizadaCallback() {
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
            adapterRequests.updateUsersList(listaFiltrada, new AdapterRequests.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    adicionarDadoDoUsuario(dadosUser);
                    setLoading(false);
                }
            });
        }
    }

    private void inicializarComponentes(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewRequest);
        searchView = view.findViewById(R.id.searchViewRequest);
        spinProgress = view.findViewById(R.id.spinProgressBarRecycler);
        txtViewTitle = view.findViewById(R.id.txtViewTitleRequest);
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

    private void limparPeloDestroyView() {
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
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }
}