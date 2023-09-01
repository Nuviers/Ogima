package com.example.ogima.fragment.parc;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterEsconderPerfilParc;
import com.example.ogima.adapter.AdapterFuncoesPostagem;
import com.example.ogima.adapter.AdapterViewersDesbloqueados;
import com.example.ogima.fragment.ViewerDesbloqueadoFragment;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.IntentEdicaoPerfilParc;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EsconderPerfilParcFragment extends Fragment implements AdapterEsconderPerfilParc.RemoverUsuarioListener {

    private DataTransferListener dataTransferListener;
    private FloatingActionButton fabParc;
    private Usuario usuario;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private MaterialSearchView materialSearch;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private Button btnContinuar;
    private RecyclerView recyclerViewParc;
    private List<Usuario> listaAmigos = new ArrayList<>();
    private static int PAGE_SIZE = 10; // mudar para 10
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private Query queryInicial, queryLoadMore;
    private ChildEventListener childListenerInicio, childListenerLoadMore;
    private boolean primeiroCarregamento = true;

    private Set<String> idsFiltrados = new HashSet<>();
    private UsuarioDiffDAO usuarioDAOFiltrado;
    private Query queryInicialFiltro, queryLoadMoreFiltro;
    private ChildEventListener childListenerInicioFiltro, childListenerLoadMoreFiltro;
    private boolean pesquisaAtivada = false;
    private String nomePesquisado = "";
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private String lastName = null;
    private AdapterEsconderPerfilParc adapterEsconderPerfil;
    private String lastIdFriend = "";
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private HashMap<String, Boolean> statusMarcacao = new HashMap<>();
    private ArrayList<String> idsMarcadosEdit = new ArrayList<>();
    private TextView textView50;

    public boolean isPesquisaAtivada() {
        return pesquisaAtivada;
    }

    public void setPesquisaAtivada(boolean pesquisaAtivada) {
        this.pesquisaAtivada = pesquisaAtivada;
    }

    @Override
    public void onUsuarioRemocao(Usuario usuarioRemovido, int posicao) {

    }

    private interface VerificaCriterio {
        void onCriterioAtendido(Usuario usuarioAlvo);

        void onSemVinculo();

        void onError(String message);
    }

    private interface RecuperaUser {
        void onRecuperado(Usuario usuarioAlvo);

        void onSemDado();

        void onError(String message);
    }

    @Override
    public void onStart() {
        super.onStart();
        imgBtnIncBackPadrao.setVisibility(View.INVISIBLE);
        if (primeiroCarregamento) {
            setPesquisaAtivada(false);
            configRecycler();
            configSearchView();
            usuarioDiffDAO = new UsuarioDiffDAO(listaAmigos, adapterEsconderPerfil);
            usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterEsconderPerfil);
            setLoading(true);
            recuperarDadosIniciais();
            configPaginacao();
            primeiroCarregamento = false;
        }
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
    }

    public EsconderPerfilParcFragment() {
        // Required empty public constructor
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DataTransferListener) {
            dataTransferListener = (DataTransferListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DataTransferListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataTransferListener = null;
    }

    private void onButtonClicked(ArrayList<String> idsEsconderParc) {
        if (idsMarcadosEdit != null) {
            if (idsMarcadosEdit.size() > 0 && idsEsconderParc
            != null && idsEsconderParc.size() > 0) {
                DatabaseReference atualizarIdsRef = firebaseRef.child("usuarioParc")
                        .child(idUsuario).child("idsEsconderParc");
                atualizarIdsRef.setValue(idsEsconderParc).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Atualizado com sucesso!", requireContext());
                        IntentEdicaoPerfilParc.irParaEdicao(requireContext(), idUsuario);
                    }
                });
            } else {
                DatabaseReference atualizarIdsRef = firebaseRef.child("usuarioParc")
                        .child(idUsuario).child("idsEsconderParc");
                atualizarIdsRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Atualizado com sucesso!", requireContext());
                    }
                });
            }
            return;
        }
        if (dataTransferListener != null) {
            usuario.setIdsEsconderParc(idsEsconderParc);
            dataTransferListener.onUsuarioParc(usuario, "esconderPerfil");
        }
    }

    public void setName(Usuario usuarioParc) {
        usuario = usuarioParc;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_esconder_perfil_parc, container, false);
        inicializandoComponentes(view);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbarIncPadrao);
        ((AppCompatActivity) requireActivity()).setTitle("");
        dadosUserAtual();
        setHasOptionsMenu(true);
        toolbarIncPadrao.setBackgroundColor(Color.BLACK);
        txtViewIncTituloToolbar.setText("Pesquisar amigo por nome");

        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (statusMarcacao != null) {
                    Iterator<Map.Entry<String, Boolean>> iterator = statusMarcacao.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Boolean> entry = iterator.next();
                        if (!entry.getValue()) {
                            iterator.remove(); // Remove a entrada com valor falso
                        }
                    }
                    if (statusMarcacao != null && statusMarcacao.size() > 0) {
                        ArrayList<String> listaComIds = new ArrayList<>();
                        for (Map.Entry<String, Boolean> entry : statusMarcacao.entrySet()) {
                            if (entry.getValue()) {
                                listaComIds.add(entry.getKey());
                            }
                        }
                        onButtonClicked(listaComIds);
                        Log.d("Marcacao", "Tamanho recuperado: " + statusMarcacao.size());
                    }else{
                        onButtonClicked(null);
                    }
                }
            }
        });
        return view;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_searchview_grupo_publico, menu);
        MenuItem item = menu.findItem(R.id.menu_icon_search_grupo_publico);
        materialSearch.setMenuItem(item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void dadosUserAtual() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                adapterEsconderPerfil.setStatusEpilepsia(epilepsia);
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

        recyclerViewParc.setHasFixedSize(true);
        recyclerViewParc.setLayoutManager(linearLayoutManager);

        if (adapterEsconderPerfil == null) {
            adapterEsconderPerfil = new AdapterEsconderPerfilParc(requireContext(),
                    listaAmigos, this, listaDadosUser, statusMarcacao);

            Bundle args = getArguments();
            if (args != null && args.containsKey("edit")) {
                idsMarcadosEdit = args.getStringArrayList("edit");
                if (idsMarcadosEdit != null && idsMarcadosEdit.size() > 0) {
                    adapterEsconderPerfil.setIdsMarcadosEdit(idsMarcadosEdit);
                }
            }
        }

        recyclerViewParc.setAdapter(adapterEsconderPerfil);

        adapterEsconderPerfil.setFiltragem(false);
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
                    adapterEsconderPerfil.setFiltragem(true);
                    setLoading(true);
                    setPesquisaAtivada(true);
                    nomePesquisado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
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

        queryInicial = firebaseRef.child("friends")
                .child(idUsuario)
                .orderByChild("idUsuario").limitToFirst(2);

        childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    if (usuario != null && usuario.getIdUsuario() != null
                            && !usuario.getIdUsuario().isEmpty()) {
                        adicionarUser(usuario);
                        lastIdFriend = usuario.getIdUsuario();
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
                lastIdFriend = "";
            }
        });
    }

    private void adicionarUser(Usuario usuarioAmigo) {
        if (listaAmigos != null && listaAmigos.size() >= 2) {
            return;
        }

        recuperaDadosUser(usuarioAmigo.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {
                //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());
                usuarioDiffDAO.adicionarUsuario(usuarioAmigo);
                idsUsuarios.add(usuarioAmigo.getIdUsuario());
                adapterEsconderPerfil.updateUsuarioList(listaAmigos, new AdapterEsconderPerfilParc.ListaAtualizadaCallback() {
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

    private void dadoInicialFiltragem(String nome) {

        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuario")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(2);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioFiltrado = snapshot.getValue(Usuario.class);

                    verificaVinculo(usuarioFiltrado.getIdUsuario(), new VerificaCriterio() {
                        @Override
                        public void onCriterioAtendido(Usuario usuarioFiltro) {
                            //Usuário está no nó de profileViews;
                            if (usuarioFiltrado != null
                                    && !usuarioFiltrado.getNomeUsuario().isEmpty()) {
                                adicionarUserFiltrado(usuarioFiltrado, usuarioFiltro);
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

    private void configPaginacao() {

        if (recyclerViewParc != null) {
            isScrolling = true;

            recyclerViewParc.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true;
                    }
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
            });
        }
    }

    private void carregarMaisDados(boolean comFiltro, String dadoAnterior) {

        if (comFiltro) {
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
                            verificaVinculo(usuarioMore.getIdUsuario(), new VerificaCriterio() {
                                @Override
                                public void onCriterioAtendido(Usuario usuarioFiltro) {

                                    List<Usuario> newUsuario = new ArrayList<>();

                                    String key = snapshot.child("nomeUsuario").getValue(String.class);

                                    if (lastName != null && key != null && !key.equals(lastName)) {
                                        newUsuario.add(usuarioFiltro);
                                        lastName = key;
                                    }

                                    // Remove a última chave usada
                                    if (newUsuario.size() > PAGE_SIZE) {
                                        newUsuario.remove(0);
                                    }

                                    if (lastName != null && !lastName.isEmpty()) {
                                        adicionarMaisDadosFiltrados(newUsuario, usuarioFiltro, usuarioMore);
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

            queryLoadMore = firebaseRef.child("friends")
                    .child(idUsuario)
                    .orderByChild("idUsuario")
                    .startAt(lastIdFriend)
                    .limitToFirst(PAGE_SIZE);

            childListenerLoadMore = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {

                        Usuario usuarioMore = snapshot.getValue(Usuario.class);

                        List<Usuario> newUsuario = new ArrayList<>();

                        String key = usuarioMore.getIdUsuario();

                        //*ToastCustomizado.toastCustomizadoCurto("existe " + key, getApplicationContext());
                        if (lastIdFriend != null
                                && !lastIdFriend.isEmpty() &&
                                key != null && !key.isEmpty() && key != lastIdFriend) {
                            newUsuario.add(usuarioMore);
                            lastIdFriend = key;
                        }

                        // Remove a última chave usada
                        if (newUsuario.size() > PAGE_SIZE) {
                            newUsuario.remove(0);
                        }

                        if (lastIdFriend != null && !lastIdFriend.isEmpty()) {
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
                    lastIdFriend = "";
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
                    adapterEsconderPerfil.updateUsuarioList(listaAmigos, new AdapterEsconderPerfilParc.ListaAtualizadaCallback() {
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

    private void adicionarMaisDadosFiltrados(List<Usuario> newUsuario, Usuario usuarioFriend, Usuario dadosUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDAOFiltrado.carregarMaisUsuario(newUsuario, idsFiltrados);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterEsconderPerfil.updateUsuarioList(listaFiltrada, new AdapterEsconderPerfilParc.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    adicionarDadoDoUsuario(dadosUser);
                    setLoading(false);
                }
            });
        }
    }

    private void adicionarUserFiltrado(Usuario dadosUser, Usuario usuarioViewer) {

        if (listaFiltrada != null && listaFiltrada.size() >= 2) {
            return;
        }

        usuarioDAOFiltrado.adicionarUsuario(usuarioViewer);
        idsFiltrados.add(usuarioViewer.getIdUsuario());
        adapterEsconderPerfil.updateUsuarioList(listaFiltrada, new AdapterEsconderPerfilParc.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                adicionarDadoDoUsuario(dadosUser);
                setLoading(false);
            }
        });
    }

    private void verificaVinculo(String idSearch, VerificaCriterio callback) {
        Query verificaViewRef = firebaseRef.child("friends")
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
        adapterEsconderPerfil.setFiltragem(false);
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

        if (listaAmigos != null && listaAmigos.size() > 0) {
            adapterEsconderPerfil.updateUsuarioList(listaAmigos, new AdapterEsconderPerfilParc.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {

                }
            });
        }
    }

    private void adicionarDadoDoUsuario(Usuario dadosUser) {
        listaDadosUser.put(dadosUser.getIdUsuario(), dadosUser);
    }

    private void inicializandoComponentes(View view) {
        toolbarIncPadrao = view.findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = view.findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = view.findViewById(R.id.txtViewIncTituloToolbarPadrao);
        materialSearch = view.findViewById(R.id.materialSearchAmigParc);
        btnContinuar = view.findViewById(R.id.btnContinuarEsconderPerfilParc);
        recyclerViewParc = view.findViewById(R.id.recyclerViewEsconderParc);

        textView50 = view.findViewById(R.id.textView50);
    }
}