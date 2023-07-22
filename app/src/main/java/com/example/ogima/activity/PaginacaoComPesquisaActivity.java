package com.example.ogima.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterPaginacaoPesquisa;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Postagem;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.annotations.NonNull;

public class PaginacaoComPesquisaActivity extends AppCompatActivity implements AdapterPaginacaoPesquisa.RecuperaPosicaoAnterior {

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private MaterialSearchView materialSearch;
    private boolean configInicial = true;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private AdapterPaginacaoPesquisa adapterPesquisa;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private List<Usuario> listaDadosUser = new ArrayList<>();

    private long lastTimestamp = -1;
    private static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private UsuarioDiffDAO usuarioDAO;
    private UsuarioDiffDAO usuarioDAOFiltrado;
    private Query queryInicial, queryLoadMore;
    private Query queryInicialFiltro, queryLoadMoreFiltro;
    private ChildEventListener childListenerInicio, childListenerLoadMore;
    private ChildEventListener childListenerInicioFiltro, childListenerLoadMoreFiltro;
    private RecyclerView.OnScrollListener scrollListener;
    private boolean pesquisaAtivada = false;
    private String searchText = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;

    @Override
    public void onBackPressed() {
        if (materialSearch.isSearchOpen()) {
            materialSearch.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchview_grupo_publico, menu);
        MenuItem item = menu.findItem(R.id.menu_icon_search_grupo_publico);
        materialSearch.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (configInicial) {
            dadosUserAtual();
            configurarSearchView();
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterPesquisa);
            usuarioDAO = new UsuarioDiffDAO(listaDadosUser, adapterPesquisa);
            usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterPesquisa);
            setLoading(true);
            recuperarDadosIniciais();
            configPaginacao();
            configInicial = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterPesquisa != null && linearLayoutManager != null
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
        usuarioDAO.limparListaUsuarios();
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

        pesquisaAtivada = false;
        searchText = null;

        mCurrentPosition = -1;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPosition = savedInstanceState.getInt("current_position");
    }

    private interface VerificaCriterio {
        void onCriterioAtendido();

        void onSemVinculo();

        void onError(String message);
    }

    private interface RecuperaUser {
        void onRecuperado(Usuario usuarioAtual);

        void onSemDado();

        void onError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paginacao_com_pesquisa);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("TESTE");
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);


    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (adapterPesquisa == null) {
            adapterPesquisa = new AdapterPaginacaoPesquisa(getApplicationContext(),
                    listaUsuarios, this, listaDadosUser);
        }

        recyclerView.setAdapter(adapterPesquisa);

        adapterPesquisa.setFiltragem(false);
    }

    private void configurarSearchView() {
        materialSearch.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    adapterPesquisa.setFiltragem(true);
                    setLoading(true);
                    pesquisaAtivada = true;
                    searchText = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
                    dadoInicialFiltragem(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText));
                } else {
                    limparFiltragem();
                }
                return true;
            }
        });

        materialSearch.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                ToastCustomizado.toastCustomizado("SearchViewShown", getApplicationContext());
            }

            @Override
            public void onSearchViewClosed() {
                ToastCustomizado.toastCustomizado("SearchViewClosed", getApplicationContext());
                limparFiltragem();
            }
        });
    }

    private void dadosUserAtual() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                adapterPesquisa.setStatusEpilepsia(epilepsia);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
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
                        recuperarUser(usuario.getIdUsuario(), new RecuperaUser() {
                            @Override
                            public void onRecuperado(Usuario usuarioAtual) {
                                adicionarUser(usuario, usuarioAtual);
                                lastTimestamp = usuario.getTimeStampView();
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

    private void adicionarUser(Usuario usuario, Usuario dadosUser) {

        if (listaUsuarios != null && listaUsuarios.size() >= 2) {
            return;
        }

        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());
        usuarioDiffDAO.adicionarUsuario(usuario);
        idsUsuarios.add(usuario.getIdUsuario());
        adapterPesquisa.updateUserList(listaUsuarios);

        usuarioDAO.adicionarUsuario(dadosUser);
        adapterPesquisa.updateUserDadoList(listaDadosUser);
        setLoading(false);
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

                                    // o usuário rolou até o final da lista, exibe mais cinco itens

                                    carregarMaisDados(pesquisaAtivada, searchText);
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
            ToastCustomizado.toastCustomizado("FILTRO", getApplicationContext());
            if (listaFiltrada != null && listaFiltrada.size() > 0
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMoreFiltro = firebaseRef.child("usuarios")
                        .orderByChild("nomeUsuario")
                        .startAt(lastName).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);

                childListenerLoadMoreFiltro = queryLoadMoreFiltro.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioMore = snapshot.getValue(Usuario.class);
                            buscaIdEmProfileViews(usuarioMore.getIdUsuario(), new VerificaCriterio() {
                                @Override
                                public void onCriterioAtendido() {

                                    List<Usuario> newUsuario = new ArrayList<>();

                                    String key = snapshot.child("nomeUsuario").getValue(String.class);

                                    if (lastName != null && key != null && !key.equals(lastName)) {
                                        newUsuario.add(snapshot.getValue(Usuario.class));
                                        lastName = key;
                                    }

                                    // Remove a última chave usada
                                    if (newUsuario.size() > PAGE_SIZE) {
                                        newUsuario.remove(0);
                                    }

                                    if (lastName != null && !lastName.isEmpty()) {
                                        adicionarMaisDadosFiltrados(newUsuario);
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

                        recuperarUser(usuarioMore.getIdUsuario(), new RecuperaUser() {
                            @Override
                            public void onRecuperado(Usuario usuarioAtual) {

                                List<Usuario> newUsuario = new ArrayList<>();

                                long key = snapshot.child("timeStampView").getValue(Long.class);

                                //*ToastCustomizado.toastCustomizadoCurto("existe " + key, getApplicationContext());
                                if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                    newUsuario.add(snapshot.getValue(Usuario.class));
                                    lastTimestamp = key;
                                }

                                // Remove a última chave usada
                                if (newUsuario.size() > PAGE_SIZE) {
                                    newUsuario.remove(0);
                                }

                                if (lastTimestamp != -1) {
                                    adicionarMaisDados(newUsuario, usuarioAtual);
                                }
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

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void dadoInicialFiltragem(String nome) {

        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuario")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(2);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioFiltrado = snapshot.getValue(Usuario.class);

                    buscaIdEmProfileViews(usuarioFiltrado.getIdUsuario(), new VerificaCriterio() {
                        @Override
                        public void onCriterioAtendido() {
                            if (usuarioFiltrado != null
                                    && !usuarioFiltrado.getNomeUsuario().isEmpty()) {
                                adicionarUserFiltrado(usuarioFiltrado);
                                lastName = usuarioFiltrado.getNomeUsuario();
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

    private void inicializandoComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        materialSearch = findViewById(R.id.materialSearchTeste);
        recyclerView = findViewById(R.id.recyclerViewPaginacaoTeste);
    }

    private void adicionarMaisDados(List<Usuario> newUsuario, Usuario dadosUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDiffDAO.carregarMaisUsuario(newUsuario, idsUsuarios);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterPesquisa.updateUserList(listaUsuarios);

            usuarioDAO.adicionarUsuario(dadosUser);
            adapterPesquisa.updateUserDadoList(listaDadosUser);

            ToastCustomizado.toastCustomizadoCurto("Mais dados", getApplicationContext());
            setLoading(false);
        }
    }

    private void adicionarMaisDadosFiltrados(List<Usuario> newUsuario) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDAOFiltrado.carregarMaisUsuario(newUsuario, idsFiltrados);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterPesquisa.updateUserList(listaFiltrada);
            ToastCustomizado.toastCustomizadoCurto("Mais dados", getApplicationContext());
            setLoading(false);
        }
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {

    }

    private void buscaIdEmProfileViews(String idSearch, VerificaCriterio callback) {
        Query verificaViewRef = firebaseRef.child("profileViewsDesbloqueados")
                .child(idUsuario)
                .orderByChild("idUsuario").equalTo(idSearch);

        verificaViewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onCriterioAtendido();
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

    private void adicionarUserFiltrado(Usuario usuario) {

        if (listaFiltrada != null && listaFiltrada.size() >= 2) {
            return;
        }

        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());
        usuarioDAOFiltrado.adicionarUsuario(usuario);
        idsFiltrados.add(usuario.getIdUsuario());
        adapterPesquisa.updateUserList(listaFiltrada);
        setLoading(false);
    }

    private void recuperarUser(String idUser, RecuperaUser callback) {
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

    private void filtrarUsuariosPorNome(String nome) {
        List<Usuario> listaFiltrada = new ArrayList<>();

        if (nome == null || nome.isEmpty()) {
            adapterPesquisa.updateUserList(listaUsuarios);
        } else {
            for (Usuario usuario : listaUsuarios) {
                // Verifica se o nome do usuário contém o texto de pesquisa (ignorando maiúsculas e minúsculas)

                if (UsuarioUtils.recuperarNomeConfigurado(usuario).toLowerCase().startsWith(nome.toLowerCase())
                        || UsuarioUtils.recuperarNomeConfigurado(usuario).toLowerCase().endsWith(nome.toLowerCase())) {
                    if (listaFiltrada != null && listaFiltrada.size() > 0
                            && !listaFiltrada.contains(usuario)) {
                        listaFiltrada.add(usuario);
                    } else if (listaFiltrada != null && listaFiltrada.size() == 0) {
                        listaFiltrada.add(usuario);
                    }
                }
            }
            if (listaFiltrada != null && listaFiltrada.size() > 0) {
                // Atualiza a lista exibida no RecyclerView com os usuários filtrados
                adapterPesquisa.updateUserList(listaFiltrada);
            }
        }
    }

    private void limparFiltragem() {
        lastName = null;
        idsFiltrados.clear();
        adapterPesquisa.setFiltragem(false);
        pesquisaAtivada = false;
        searchText = "";
        usuarioDAOFiltrado.limparListaUsuarios();

        if (childListenerInicioFiltro != null) {
            queryInicialFiltro.removeEventListener(childListenerInicioFiltro);
            childListenerInicioFiltro = null;
        }

        if (childListenerLoadMoreFiltro != null) {
            queryLoadMoreFiltro.removeEventListener(childListenerLoadMoreFiltro);
            childListenerLoadMoreFiltro = null;
        }

        if (listaUsuarios != null && listaUsuarios.size() > 0) {
            adapterPesquisa.updateUserList(listaUsuarios);
        }
    }
}