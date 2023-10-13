package com.example.ogima.fragment;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.activity.PaginacaoComPesquisaActivity;
import com.example.ogima.adapter.AdapterProfileViews;
import com.example.ogima.adapter.AdapterViewersDesbloqueados;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ViewerDesbloqueadoFragment extends Fragment implements AdapterViewersDesbloqueados.AnimacaoIntent, AdapterViewersDesbloqueados.RecuperaPosicaoAnterior {

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;

    private String idUsuario, emailUsuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private RecyclerView recyclerView;
    private AdapterViewersDesbloqueados adapterViewers;
    private List<Usuario> listaViewers = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private long lastTimestamp = -1;
    private static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private Query queryInicial, queryLoadMore;
    private ChildEventListener childListenerInicio, childListenerLoadMore;
    private boolean primeiroCarregamento = true;
    private RecyclerView.OnScrollListener scrollListener;
    private ProgressDialog progressDialog;
    private boolean existemDados = false;

    //Dados do usuário
    private HashMap<String, Object> listaDadosUser = new HashMap<>();

    //Filtragem
    private MaterialSearchView materialSearch;
    private Set<String> idsFiltrados = new HashSet<>();
    private UsuarioDiffDAO usuarioDAOFiltrado;
    private Query queryInicialFiltro, queryLoadMoreFiltro;
    private ChildEventListener childListenerInicioFiltro, childListenerLoadMoreFiltro;
    private boolean pesquisaAtivada = false;
    private String nomePesquisado = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;

    public ViewerDesbloqueadoFragment() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    public boolean isPesquisaAtivada() {
        return pesquisaAtivada;
    }

    public void setPesquisaAtivada(boolean pesquisaAtivada) {
        this.pesquisaAtivada = pesquisaAtivada;
    }

    private interface VerificaCriterio {
        void onCriterioAtendido(Usuario usuarioViewer);

        void onSemVinculo();

        void onError(String message);
    }

    private interface RecuperaUser {
        void onRecuperado(Usuario usuarioAtual);

        void onSemDado();

        void onError(String message);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_searchview_grupo_publico, menu);
        MenuItem item = menu.findItem(R.id.menu_icon_search_grupo_publico);
        materialSearch.setMenuItem(item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            setPesquisaAtivada(false);
            configRecycler();
            configSearchView();
            usuarioDiffDAO = new UsuarioDiffDAO(listaViewers, adapterViewers);
            usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterViewers);
            setLoading(true);
            recuperarDadosIniciais();
            configPaginacao();
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterViewers != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
            //ToastCustomizado.toastCustomizadoCurto("Find " + mCurrentPosition, getApplicationContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
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
    public void onDestroy() {
        super.onDestroy();
        if (childListenerInicio != null) {
            queryInicial.removeEventListener(childListenerInicio);
            childListenerInicio = null;
        }

        if (childListenerLoadMore != null) {
            queryLoadMore.removeEventListener(childListenerLoadMore);
            childListenerLoadMore = null;
        }

        usuarioDiffDAO.limparListaUsuarios();
        listaDadosUser.clear();
        idsUsuarios.clear();

        if (childListenerInicioFiltro != null) {
            queryInicialFiltro.removeEventListener(childListenerInicioFiltro);
            childListenerInicioFiltro = null;
        }

        if (childListenerLoadMoreFiltro != null) {
            queryLoadMoreFiltro.removeEventListener(childListenerLoadMoreFiltro);
            childListenerLoadMoreFiltro = null;
        }

        if (listaFiltrada != null && listaFiltrada.size() > 0) {
            usuarioDAOFiltrado.limparListaUsuarios();
            idsFiltrados.clear();
        }

        setPesquisaAtivada(false);
        nomePesquisado = null;

        if (materialSearch.isSearchOpen()) {
            materialSearch.closeSearch();
        }

        mCurrentPosition = -1;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt("current_position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewer_desbloqueado, container, false);
        inicializandoComponentes(view);
        dadosUserAtual();
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbarIncPadrao);
        ((AppCompatActivity) requireActivity()).setTitle("");
        setHasOptionsMenu(true);
        toolbarIncPadrao.setBackgroundColor(Color.BLACK);
        imgBtnIncBackPadrao.setVisibility(View.INVISIBLE);
        txtViewIncTituloToolbar.setText("Pesquisar por nome");

        return view;
    }

    private void dadosUserAtual() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                adapterViewers.setStatusEpilepsia(epilepsia);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (adapterViewers == null) {
            adapterViewers = new AdapterViewersDesbloqueados(requireContext(),
                    listaViewers, this, this, listaDadosUser);
        }

        recyclerView.setAdapter(adapterViewers);

        adapterViewers.setFiltragem(false);
    }

    private void configSearchView() {
        materialSearch.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    adapterViewers.setFiltragem(true);
                    setLoading(true);
                    setPesquisaAtivada(true);
                    nomePesquisado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
                    nomePesquisado = FormatarNomePesquisaUtils.removeAcentuacao(nomePesquisado).toUpperCase(Locale.ROOT);
                    dadoInicialFiltragem(nomePesquisado);
                } else {
                    limparFiltragem();
                }
                return true;
            }
        });

        materialSearch.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                limparFiltragem();
            }
        });
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void recuperarDadosIniciais() {

        queryInicial = firebaseRef.child("profileViewsDesbloqueados")
                .child(idUsuario)
                .orderByChild("timeStampView").limitToFirst(2);

        childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    if (usuario != null && usuario.getTimeStampView() != -1) {
                        adicionarUser(usuario);
                        lastTimestamp = usuario.getTimeStampView();
                    }
                }
            }

            @Override
            public void onChildChanged(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                lastTimestamp = -1;
            }
        });
    }

    private void adicionarUser(Usuario usuarioViewer) {
        if (listaViewers != null && listaViewers.size() >= 2) {
            return;
        }

        recuperaDadosUser(usuarioViewer.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());
                usuarioDiffDAO.adicionarUsuario(usuarioViewer);
                idsUsuarios.add(usuarioViewer.getIdUsuario());
                adapterViewers.updateViewersList(listaViewers);
                adicionarDadoDoUsuario(dadosUser);
                setLoading(false);
            }

            @Override
            public void onSemDado() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void dadoInicialFiltragem(String nome) {

        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(2);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioFiltrado = snapshot.getValue(Usuario.class);

                    verificaVinculo(usuarioFiltrado.getIdUsuario(), new VerificaCriterio() {
                        @Override
                        public void onCriterioAtendido(Usuario usuarioViewer) {
                            //Usuário está no nó de profileViews;
                            if (usuarioFiltrado != null
                                    && !usuarioFiltrado.getNomeUsuarioPesquisa().isEmpty()) {
                                adicionarUserFiltrado(usuarioFiltrado, usuarioViewer);
                                lastName = usuarioFiltrado.getNomeUsuarioPesquisa();
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

            @Override
            public void onChildChanged(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                lastName = null;
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

                                if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                                    isScrolling = false;

                                    //*progressBarLoading.setVisibility(View.VISIBLE);

                                    setLoading(true);

                                    carregarMaisDados(isPesquisaAtivada(), nomePesquisado);
                                }
                            }
                        }, 100);
                    }
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados(boolean comFiltro, String dadoAnterior) {

        if (comFiltro) {
            if (listaFiltrada != null && listaFiltrada.size() > 0
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMoreFiltro = firebaseRef.child("usuarios")
                        .orderByChild("nomeUsuarioPesquisa")
                        .startAt(lastName).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);

                childListenerLoadMoreFiltro = queryLoadMoreFiltro.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioMore = snapshot.getValue(Usuario.class);
                            verificaVinculo(usuarioMore.getIdUsuario(), new VerificaCriterio() {
                                @Override
                                public void onCriterioAtendido(Usuario usuarioViewer) {

                                    List<Usuario> newUsuario = new ArrayList<>();

                                    String key = snapshot.child("nomeUsuarioPesquisa").getValue(String.class);

                                    if (lastName != null && key != null && !key.equals(lastName)) {
                                        newUsuario.add(usuarioViewer);
                                        lastName = key;
                                    }

                                    // Remove a última chave usada
                                    if (newUsuario.size() > PAGE_SIZE) {
                                        newUsuario.remove(0);
                                    }

                                    if (lastName != null && !lastName.isEmpty()) {
                                        adicionarMaisDadosFiltrados(newUsuario, usuarioViewer, usuarioMore);
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

                    @Override
                    public void onChildChanged(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                        lastName = null;
                    }
                });
            }
        } else {

            queryLoadMore = firebaseRef.child("profileViewsDesbloqueados")
                    .child(idUsuario)
                    .orderByChild("timeStampView")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);

            childListenerLoadMore = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {

                        Usuario usuarioMore = snapshot.getValue(Usuario.class);

                        List<Usuario> newUsuario = new ArrayList<>();

                        long key = usuarioMore.getTimeStampView();

                        //*ToastCustomizado.toastCustomizadoCurto("existe " + key, getApplicationContext());
                        if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                            newUsuario.add(usuarioMore);
                            lastTimestamp = key;
                        }

                        // Remove a última chave usada
                        if (newUsuario.size() > PAGE_SIZE) {
                            newUsuario.remove(0);
                        }

                        if (lastTimestamp != -1) {
                            adicionarMaisDados(newUsuario, usuarioMore.getIdUsuario());
                        }
                    }
                }

                @Override
                public void onChildChanged(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
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
                    adapterViewers.updateViewersList(listaViewers);
                    adicionarDadoDoUsuario(dadosUser);
                    setLoading(false);
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

    private void adicionarMaisDadosFiltrados(List<Usuario> newUsuario, Usuario usuarioViewer, Usuario dadosUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDAOFiltrado.carregarMaisUsuario(newUsuario, idsFiltrados);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterViewers.updateViewersList(listaFiltrada);
            adicionarDadoDoUsuario(dadosUser);
            setLoading(false);
        }
    }

    private void adicionarUserFiltrado(Usuario dadosUser, Usuario usuarioViewer) {

        if (listaFiltrada != null && listaFiltrada.size() >= 2) {
            return;
        }

        usuarioDAOFiltrado.adicionarUsuario(usuarioViewer);
        idsFiltrados.add(usuarioViewer.getIdUsuario());
        adapterViewers.updateViewersList(listaFiltrada);

        adicionarDadoDoUsuario(dadosUser);
        setLoading(false);
    }

    private void verificaVinculo(String idSearch, VerificaCriterio callback) {
        Query verificaViewRef = firebaseRef.child("profileViewsDesbloqueados")
                .child(idUsuario)
                .child(idSearch);

        verificaViewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioViewer = snapshot.getValue(Usuario.class);
                    callback.onCriterioAtendido(usuarioViewer);
                } else {
                    callback.onSemVinculo();
                }
                verificaViewRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void recuperaDadosUser(String idUser, RecuperaUser callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUser, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
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

    private void limparFiltragem() {
        lastName = null;
        idsFiltrados.clear();
        adapterViewers.setFiltragem(false);
        setPesquisaAtivada(false);
        nomePesquisado = "";
        usuarioDAOFiltrado.limparListaUsuarios();

        if (childListenerInicioFiltro != null) {
            queryInicialFiltro.removeEventListener(childListenerInicioFiltro);
            childListenerInicioFiltro = null;
        }

        if (childListenerLoadMoreFiltro != null) {
            queryLoadMoreFiltro.removeEventListener(childListenerLoadMoreFiltro);
            childListenerLoadMoreFiltro = null;
        }

        if (listaViewers != null && listaViewers.size() > 0) {
            adapterViewers.updateViewersList(listaViewers);
        }
    }

    private void inicializandoComponentes(View view) {
        toolbarIncPadrao = view.findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = view.findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = view.findViewById(R.id.txtViewIncTituloToolbarPadrao);
        recyclerView = view.findViewById(R.id.recyclerViewDesbloqueados);
        materialSearch = view.findViewById(R.id.materialSearchViewer);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, requireContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onExecutarAnimacao() {
        requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void adicionarDadoDoUsuario(Usuario dadosUser){
        listaDadosUser.put(dadosUser.getIdUsuario(), dadosUser);
    }
}