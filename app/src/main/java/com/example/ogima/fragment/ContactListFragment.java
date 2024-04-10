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
import com.example.ogima.adapter.AdapterContactList;
import com.example.ogima.helper.ChatDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ContactDiffDAO;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Chat;
import com.example.ogima.model.Contatos;
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


public class ContactListFragment extends Fragment implements AdapterContactList.RecuperaPosicaoAnterior, AdapterContactList.RemoverContatoListener, AdapterContactList.AnimacaoIntent {

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
    private List<Contatos> listaContatos = new ArrayList<>();
    private Set<String> idsUsuarios = new HashSet<>();
    private ContactDiffDAO contactDiffDAO, contactDAOFiltrado;
    private Query queryInicial, queryLoadMore,
            queryInicialFiltro, queryLoadMorePesquisa, queryInicialFind,
            queryLoadMoreFiltro, newDataRef;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private Set<String> idsFiltrados = new HashSet<>();
    private String nomePesquisado = "";
    private List<Contatos> listaFiltrada = new ArrayList<>();
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
            childEventListenerContatos, childListenerMoreFiltro,
            childListenerInicio, childEventListenerNewData;
    private AdapterContactList adapterContactList;
    private boolean trocarQueryInicial = false;
    private Contatos contatoComparator;
    private int contadorRemocaoListener = 0;
    private FirebaseUtils firebaseUtils;
    private int travar = 0;

    private Set<String> idsListenersNEWDATA = new HashSet<>();
    private HashMap<String, Query> referenceHashMapNEWDATA = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapNEWDATA = new HashMap<>();
    private int contadorRemocaoListenerNEWDATA = 0;
    private static final String TAG = "CONTATOtag";
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
    private int contadorNome = 0;

    private interface VerificaExistenciaCallback {
        void onExistencia(boolean status, Contatos contatoAtualizado);

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
        if (adapterContactList != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                listaContatos != null && listaContatos.size() > 0
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

    public ContactListFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        contatoComparator = new Contatos(false, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        inicializarComponentes(view);
        ToastCustomizado.toastCustomizadoCurto("CREATE", requireContext());
        configInicial();

        txtViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean verificacao = listaContatos.get(0).getTimestampContato() > lastTimestamp;
                //ToastCustomizado.toastCustomizadoCurto("TIME: " + lastTimestamp, requireContext());
                //ToastCustomizado.toastCustomizadoCurto("Id: " + listaContatos.get(0).getIdUsuario() + " Time: " + listaContatos.get(0).getTimestampLastMsg(), requireContext());
                //ToastCustomizado.toastCustomizadoCurto("Maior: " + verificacao, requireContext());
                for(Contatos contatos : listaContatos){
                    ToastCustomizado.toastCustomizadoCurto("Nome contato: " + contatos.getNomeContato(), requireContext());
                }

                if (idUltimoElemento != null && !idUltimoElemento.isEmpty()) {
                    ToastCustomizado.toastCustomizadoCurto("Ultimo: " + idUltimoElemento, requireContext());
                }else{
                    ToastCustomizado.toastCustomizadoCurto("NÃO TEM", requireContext());
                }
            }
        });
        return view;
    }

    private void configInicial() {
        firebaseUtils = new FirebaseUtils();
        txtViewTitle.setText(FormatarContadorUtils.abreviarTexto("Contatos", 20));
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
                contactDiffDAO = new ContactDiffDAO(listaContatos, adapterContactList);
                contactDAOFiltrado = new ContactDiffDAO(listaFiltrada, adapterContactList);
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
                                        contactDAOFiltrado.limparListaContatos();
                                        adapterContactList.updateContatoList(listaFiltrada, new AdapterContactList.ListaAtualizadaCallback() {
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
            adapterContactList = new AdapterContactList(requireContext(),
                    listaContatos, listaDadosUser,
                    getResources().getColor(R.color.chat_list_color), this, this, this);
            recyclerView.setAdapter(adapterContactList);
            adapterContactList.setStatusEpilepsia(epilepsia);
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
        if (listaContatos != null && listaContatos.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }

        if (trocarQueryInicial && lastTimestamp != -1) {
            queryInicial = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("timestampContato")
                    .startAt(lastTimestamp + 1)
                    .limitToFirst(1);
        } else {
            queryInicial = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("timestampContato").limitToFirst(1);
        }
        exibirProgress();

        ultimoElemento(new RecuperaUltimoElemento() {
            @Override
            public void onRecuperado() {
                //*ToastCustomizado.toastCustomizado("INICIO CHAMADO " + idUltimoElemento, requireContext());
                childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getValue() != null) {
                            Contatos contato = snapshot.getValue(Contatos.class);
                            if (contato != null
                                    && contato.getIdContato() != null
                                    && !contato.getIdContato().isEmpty()) {
                                idPrimeiroDado = contato.getIdContato();
                                if (travar == 0) {
                                    lastTimestamp = contato.getTimestampContato();
                                    adicionarContatos(contato, false);
                                } else {
                                    ToastCustomizado.toastCustomizadoCurto("Novo dado pelo inicio " + contato.getIdContato(), requireContext());
                                    //Dado mais recente que o anterior
                                    if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                            && listenerHashMapNEWDATA.containsKey(contato.getIdContato())) {
                                        return;
                                    }
                                    ToastCustomizado.toastCustomizadoCurto("Novo dado pelo inicio " + contato.getIdContato(), requireContext());
                                    anexarNovoDado(contato);
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
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                                return;
                            }
                            ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO INICIO", requireContext());
                            logicaAtualizacao(snapshot, false);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                            if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                    && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                                //O próprio listenernewdata vai cuidar da remoção desse dado.
                                return;
                            }

                            ToastCustomizado.toastCustomizado("DELETE INICIO", requireContext());
                            logicaRemocao(contatoRemovido, true, true);

                            verificaExistencia(contatoRemovido.getIdContato(), new VerificaExistenciaCallback() {
                                @Override
                                public void onExistencia(boolean status, Contatos contatoAtualizado) {
                                    if (status) {
                                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                                && listenerHashMapNEWDATA.containsKey(contatoRemovido.getIdContato())) {
                                        } else {
                                            ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do inicio " + contatoRemovido.getIdContato(), requireContext());
                                            anexarNovoDado(contatoAtualizado);
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
        queryInicialFind = firebaseRef.child("contatos_by_name")
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
                                            adicionarContatosFiltrado(usuarioAtual);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            usuarioAtual.setIndisponivel(true);
                                            adicionarContatosFiltrado(usuarioAtual);
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

    private void adicionarContatos(Contatos contatoAlvo, boolean dadoModificado) {
        recuperaDadosUser(contatoAlvo.getIdContato(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {

                contatoAlvo.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                contactDiffDAO.adicionarContato(contatoAlvo);
                contactDiffDAO.adicionarIdAoSet(idsUsuarios, dadosUser.getIdUsuario());

                List<Contatos> listaAtual = new ArrayList<>();
                if (isPesquisaAtivada()) {
                    listaAtual = listaFiltrada;
                } else {
                    listaAtual = listaContatos;
                }

                Collections.sort(listaAtual, contatoComparator);

                adapterContactList.updateContatoList(listaAtual, new AdapterContactList.ListaAtualizadaCallback() {
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

    private void adicionarContatosFiltrado(Usuario dadosUser) {
        if (listaFiltrada != null && listaFiltrada.size() >= 1) {
            String idContatosInicioFiltro = listaFiltrada.get(0).getIdContato();
            if (idContatosInicioFiltro.equals(dadosUser.getIdUsuario())) {
                ocultarProgress();
                setLoading(false);
                return;
            }
        }


        queryInicialFiltro = firebaseRef.child("contatos")
                .child(idUsuario)
                .orderByChild("idContato")
                .equalTo(dadosUser.getIdUsuario()).limitToFirst(1);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    Contatos contatoAtual = snapshot.getValue(Contatos.class);

                    if (contatoAtual != null) {
                        contatoAtual.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                        contatoAtual.setIndisponivel(dadosUser.isIndisponivel());
                    }

                    if (listaFiltrada != null && listaFiltrada.size() >= 1) {
                        String idContatosInicioFiltro = listaFiltrada.get(0).getIdContato();
                        if (idContatosInicioFiltro.equals(dadosUser.getIdUsuario())) {
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }
                    }

                    lastName = dadosUser.getNomeUsuarioPesquisa();

                    contactDAOFiltrado.adicionarContato(contatoAtual);
                    contactDAOFiltrado.adicionarIdAoSet(idsFiltrados, dadosUser.getIdUsuario());

                    Collections.sort(listaFiltrada, contatoComparator);
                    adapterContactList.updateContatoList(listaFiltrada, new AdapterContactList.ListaAtualizadaCallback() {
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
                        && idsAIgnorarListeners.contains(snapshot.getValue(Contatos.class).getIdContato())) {
                    ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Contatos.class).getIdContato(), requireContext());
                    return;
                }
                if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                        && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                    return;
                }

                if (listenerHashMap != null && listenerHashMap.size() > 0
                        && listenerHashMap.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
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
                    && idUltimoElementoFiltro.equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdContato())) {
                ocultarProgress();
                ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA ONE " + idUltimoElementoFiltro, requireContext());
                return;
            }

            //**ToastCustomizado.toastCustomizadoCurto("Last Name: " + lastName, requireContext());
            if (listaFiltrada != null && !listaFiltrada.isEmpty()
                    && lastName != null && !lastName.isEmpty()) {

                queryLoadMorePesquisa = firebaseRef.child("contatos_by_name")
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
            if (listaContatos.size() > 1
                    && idUltimoElemento != null && !idUltimoElemento.isEmpty()
                    && idUltimoElemento.equals(listaContatos.get(listaContatos.size() - 1).getIdContato())) {
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

            queryLoadMore = firebaseRef.child("contatos")
                    .child(idUsuario)
                    .orderByChild("timestampContato")
                    .startAt(lastTimestamp)
                    .limitToFirst(PAGE_SIZE);
            childEventListenerContatos = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    exibirProgress();
                    if (snapshot.getValue() != null) {
                        Contatos contatoMore = snapshot.getValue(Contatos.class);
                        if (contatoMore != null
                                && contatoMore.getIdContato() != null
                                && !contatoMore.getIdContato().isEmpty()) {
                            Log.d(TAG, "Timestamp key: " + lastTimestamp);
                            Log.d(TAG, "id: " + contatoMore.getIdContato() + " time: " + contatoMore.getTimestampContato());
                            if (listaContatos != null && listaContatos.size() > 1 && idsUsuarios != null && idsUsuarios.size() > 0
                                    && idsUsuarios.contains(contatoMore.getIdContato())) {
                                Log.d(TAG, "Id já existia: " + contatoMore.getIdContato());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            if (listaContatos != null && listaContatos.size() > 1
                                    && contatoMore.getTimestampContato() < listaContatos.get(0).getTimestampContato()) {
                                ToastCustomizado.toastCustomizadoCurto("TIME IGNORADO", requireContext());
                                ocultarProgress();
                                setLoading(false);
                                return;
                            }

                            //*ToastCustomizado.toastCustomizadoCurto("ADICIONADO " + contatoMore.getIdUsuario(), requireContext());
                            List<Contatos> newContatos = new ArrayList<>();
                            long key = contatoMore.getTimestampContato();
                            if (lastTimestamp != -1 && key != -1) {
                                if (key != lastTimestamp || listaContatos.size() > 0 &&
                                        !contatoMore.getIdContato()
                                                .equals(listaContatos.get(listaContatos.size() - 1).getIdContato())) {
                                    newContatos.add(contatoMore);
                                    //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                    lastTimestamp = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newContatos.size() > PAGE_SIZE) {
                                newContatos.remove(0);
                            }
                            if (lastTimestamp != -1) {

                                recuperaDadosUser(contatoMore.getIdContato(), new RecuperaUser() {
                                    @Override
                                    public void onRecuperado(Usuario dadosUser) {
                                        for(Contatos contato : newContatos){
                                            if (contato.getIdContato().equals(dadosUser.getIdUsuario())) {
                                                newContatos.remove(contato);
                                                contato.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                                                newContatos.add(contato);
                                                contadorNome++;
                                            }
                                            if (contadorNome == newContatos.size()) {
                                                contadorNome = 0;
                                                adicionarMaisDados(newContatos, contatoMore.getIdContato(), dadosUser, queryLoadMore);
                                            }
                                        }
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
                                && idsAIgnorarListeners.contains(snapshot.getValue(Contatos.class).getIdContato())) {
                            ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Contatos.class).getIdContato(), requireContext());
                            return;
                        }
                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                            return;
                        }
                        ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO CARREGAR + DADOS", requireContext());
                        logicaAtualizacao(snapshot, false);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                        if (contatoRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(contatoRemovido.getIdContato())
                                || listaContatos != null && listaContatos.size() > 0
                                && listaContatos.get(0).getIdContato().equals(contatoRemovido.getIdContato())) {
                            return;
                        }

                        verificaExistencia(contatoRemovido.getIdContato(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Contatos contatoAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + contatoRemovido.getIdContato(), requireContext());

                                logicaRemocao(contatoRemovido, true, true);

                                if (status) {
                                    boolean menorque = contatoAtualizado.getTimestampContato() <= listaContatos.get(0).getTimestampContato();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + contatoRemovido.getIdContato(), requireContext());
                                        anexarNovoDado(contatoAtualizado);
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

    private void adicionarMaisDados(List<Contatos> newContatos, String idUser, Usuario dadosUser, Query queryAlvo) {
        if (newContatos != null && newContatos.size() >= 1) {
            contactDiffDAO.carregarMaisContato(newContatos, idsUsuarios);
            contactDiffDAO.adicionarIdAoSet(idsUsuarios, idUser);

            Collections.sort(listaContatos, contatoComparator);
            adapterContactList.updateContatoList(listaContatos, new AdapterContactList.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    ocultarProgress();
                    adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerContatos, false);
                    setLoading(false);
                }
            });
        } else {
            ocultarProgress();
        }
    }


    private void adicionarMaisDadosFiltrados(List<Contatos> newContatos, String idUser, Query queryAlvo, ChildEventListener childEventListenerAlvo) {
        if (newContatos != null && !newContatos.isEmpty()) {
            recuperaDadosUser(idUser, new RecuperaUser() {
                @Override
                public void onRecuperado(Usuario dadosUser) {
                    for(Contatos contatos : newContatos){
                        if (contatos.getIdContato().equals(dadosUser.getIdUsuario())) {
                            newContatos.remove(contatos);
                            contatos.setNomeContato(dadosUser.getNomeUsuarioPesquisa());
                            newContatos.add(contatos);
                            contadorNome++;
                        }

                        if (contadorNome == newContatos.size()) {
                            contadorNome = 0;
                            contactDAOFiltrado.carregarMaisContato(newContatos, idsFiltrados);
                            contactDAOFiltrado.adicionarIdAoSet(idsFiltrados, idUser);

                            Collections.sort(listaFiltrada, contatoComparator);
                            adapterContactList.updateContatoList(listaFiltrada, new AdapterContactList.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {
                                    ocultarProgress();
                                    adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerAlvo, false);
                                    setLoading(false);
                                }
                            });
                        }
                    }
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

        ToastCustomizado.toastCustomizadoCurto("Listener add: " + idUser, requireContext());

        referenceFiltroHashMap.put(idUser, queryAlvo);
        listenerFiltroHashMap.put(idUser, childEventListenerAlvo);
    }

    private void limparPeloDestroyView() {
        idsAIgnorarListeners.clear();
        firebaseUtils.removerQueryChildListener(newDataRef, childEventListenerNewData);
        firebaseUtils.removerQueryChildListener(queryInicial, childListenerInicio);
        firebaseUtils.removerQueryChildListener(queryInicialFiltro, childListenerInicioFiltro);
        firebaseUtils.removerQueryChildListener(queryLoadMore, childEventListenerContatos);
        firebaseUtils.removerQueryChildListener(queryLoadMoreFiltro, childListenerMoreFiltro);
        firebaseUtils.removerQueryValueListener(queryUltimoElemento, listenerUltimoElemento);
        removeValueEventListener();
        removeValueEventListenerNEWDATA();
        removeValueEventListenerFiltro(null);
        if (contactDiffDAO != null) {
            contactDiffDAO.limparListaContatos();
        }
        if (listaDadosUser != null) {
            listaDadosUser.clear();
        }

        if (idsUsuarios != null) {
            idsUsuarios.clear();
        }
        if (listaFiltrada != null && listaFiltrada.size() > 0) {
            contactDAOFiltrado.limparListaContatos();
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
        if (contactDAOFiltrado != null) {
            contactDAOFiltrado.limparListaContatos();
        }
        if (listaContatos != null && listaContatos.size() > 0) {

            setLoading(false);

            Collections.sort(listaContatos, contatoComparator);
            adapterContactList.updateContatoList(listaContatos, new AdapterContactList.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    atualizandoLista = false;
                    contadorUpdate++;
                    if (idsParaAtualizar != null && !idsParaAtualizar.isEmpty()) {
                        for (String idUpdate : idsParaAtualizar.keySet()) {
                            int index = adapterContactList.findPositionInList(idUpdate);
                            Bundle bundleUpdate = idsParaAtualizar.get(idUpdate);
                            if (index != -1) {
                                adapterContactList.notifyItemChanged(index, bundleUpdate);
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

    private void verificaVinculo(String idAlvo, VerificaCriterio callback) {
        DatabaseReference verificaVinculoRef = firebaseRef.child("contatos")
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

    private void logicaRemocao(Contatos contatoRemovido, boolean ignorarVerificacao, boolean excluirDaLista) {

        if (contatoRemovido == null) {
            return;
        }


        /*
        if (idsFiltrados != null && idsFiltrados.size() > 0
                && idsFiltrados.contains(contatoRemovido.getIdUsuario())) {
            if (referenceFiltroHashMap != null && referenceFiltroHashMap.size() > 0
                    && listenerFiltroHashMap != null && listenerFiltroHashMap.size() > 0) {
                referenceFiltroHashMap.remove(contatoRemovido.getIdUsuario());
                listenerFiltroHashMap.remove(contatoRemovido.getIdUsuario());
                if (idsListeners != null && idsListeners.size() > 0) {
                    idsListeners.remove(contatoRemovido.getIdUsuario());
                }
            }
        }

         */

        if (idsUsuarios != null && idsUsuarios.size() > 0
                && idsUsuarios.contains(contatoRemovido.getIdContato())) {
            /*
            if (listenerHashMap != null && referenceHashMap != null) {
                Query userRef = referenceHashMap.get(contatoRemovido.getIdUsuario());
                ChildEventListener listener = listenerHashMap.get(contatoRemovido.getIdUsuario());
                if (userRef != null && listener != null) {
                    ToastCustomizado.toastCustomizado("LISTENER REMOVIDO + DADOS " + contatoRemovido.getIdUsuario(), requireContext());
                    userRef.removeEventListener(listener);
                }
            }


                 if (referenceHashMap != null && referenceHashMap.size() > 0
                    && listenerHashMap != null && listenerHashMap.size() > 0) {
                referenceHashMap.remove(contatoRemovido.getIdUsuario());
                listenerHashMap.remove(contatoRemovido.getIdUsuario());
                if (idsListeners != null && idsListeners.size() > 0) {
                    idsListeners.remove(contatoRemovido.getIdUsuario());
                }
            }

             */


        }

     /*
        if (listenerHashMapNEWDATA != null && referenceHashMapNEWDATA != null) {
            if (idsListenersNEWDATA != null && idsListenersNEWDATA.size() > 0) {
                idsListenersNEWDATA.remove(contatoRemovido.getIdUsuario());
            }
            Query userRef = referenceHashMapNEWDATA.get(contatoRemovido.getIdUsuario());
            ChildEventListener listener = listenerHashMapNEWDATA.get(contatoRemovido.getIdUsuario());
            if (userRef != null && listener != null) {
                ToastCustomizado.toastCustomizado("LISTENER REMOVIDO NEW DATA", requireContext());
                userRef.removeEventListener(listener);
            }
        }


        if (referenceHashMapNEWDATA != null && referenceHashMapNEWDATA.size() > 0
                && listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0) {
            referenceHashMapNEWDATA.remove(contatoRemovido.getIdUsuario());
            listenerHashMapNEWDATA.remove(contatoRemovido.getIdUsuario());
            if (idsListenersNEWDATA != null && idsListenersNEWDATA.size() > 0) {
                idsListenersNEWDATA.remove(contatoRemovido.getIdUsuario());
            }
        }
         */

        DatabaseReference verificaExistenciaRef = firebaseRef.child("contatos")
                .child(idUsuario).child(contatoRemovido.getIdContato());
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || ignorarVerificacao) {

                    if (idsFiltrados != null && idsFiltrados.size() > 0
                            && idsFiltrados.contains(contatoRemovido.getIdContato())) {
                        if (listaFiltrada != null && listaFiltrada.size() > 0 && excluirDaLista) {
                            if (idsFiltrados != null && idsFiltrados.size() > 0) {
                                idsFiltrados.remove(contatoRemovido.getIdContato());
                            }
                            contactDAOFiltrado.removerContato(contatoRemovido);
                        }
                    }

                    if (idsUsuarios != null && idsUsuarios.size() > 0
                            && idsUsuarios.contains(contatoRemovido.getIdContato())) {
                        if (listaContatos != null && listaContatos.size() > 0 && excluirDaLista) {
                            if (idsUsuarios != null && idsUsuarios.size() > 0) {
                                idsUsuarios.remove(contatoRemovido.getIdContato());
                            }
                            contactDiffDAO.removerContato(contatoRemovido);
                        }
                    }

                    if (listaDadosUser != null && listaDadosUser.size() > 0 && excluirDaLista) {
                        listaDadosUser.remove(contatoRemovido.getIdContato());
                        int posicao = adapterContactList.findPositionInList(contatoRemovido.getIdContato());
                        if (posicao != -1) {
                            adapterContactList.notifyItemChanged(posicao);
                        }
                    }

                    if (isPesquisaAtivada() && listaFiltrada != null) {
                        adapterContactList.updateContatoList(listaFiltrada, new AdapterContactList.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {

                            }
                        });
                    } else if (!isPesquisaAtivada() && listaContatos != null) {
                        adapterContactList.updateContatoList(listaContatos, new AdapterContactList.ListaAtualizadaCallback() {
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
            Contatos contatoAtualizado = snapshot.getValue(Contatos.class);

            ToastCustomizado.toastCustomizadoCurto("BORA ATUALIZAR", requireContext());

            if (contatoAtualizado == null || contatoAtualizado.getIdContato() == null) {
                return;
            }

            posicaoChanged = adapterContactList.findPositionInList(contatoAtualizado.getIdContato());

            if (posicaoChanged == -1 && isPesquisaAtivada()) {
                //Não existe esse contato na lista filtrada mas existe na lista normal.
                posicaoChanged = findPositionInList(contatoAtualizado.getIdContato());
            }

            if (posicaoChanged != -1) {
                Contatos contatoAnterior = new Contatos();
                if (idsUsuarios != null && idsUsuarios.size() > 0
                        && idsUsuarios.contains(contatoAtualizado.getIdContato())) {
                    //Já existe um listener na listagem normal
                    if (isPesquisaAtivada()
                            && referenceFiltroHashMap != null
                            && !referenceFiltroHashMap.isEmpty()
                            && referenceFiltroHashMap.containsKey(contatoAtualizado.getIdContato())) {
                        contatoAnterior = listaFiltrada.get(posicaoChanged);
                    } else {
                        contatoAnterior = listaContatos.get(posicaoChanged);
                    }
                } else if (isPesquisaAtivada()
                        && listaFiltrada != null && !listaFiltrada.isEmpty()) {
                    //Somente existe um listener desse contato na listagem filtrada.
                    contatoAnterior = listaFiltrada.get(posicaoChanged);
                }
                ToastCustomizado.toastCustomizadoCurto("Alterado: " + contatoAnterior.getIdContato(), requireContext());

                if (contatoAnterior.getTotalMensagens() != contatoAtualizado.getTotalMensagens()) {
                    atualizarPorPayload(contatoAtualizado, "totalMensagens");
                }

                if (contatoAnterior.isContatoFavorito() != contatoAtualizado.isContatoFavorito()) {
                    atualizarPorPayload(contatoAtualizado, "contatoFavorito");
                }

                if (contatoAnterior.getNivelAmizade() != null &&
                        !contatoAnterior.getNivelAmizade().equals(contatoAtualizado.getNivelAmizade())) {
                    atualizarPorPayload(contatoAtualizado, "nivelAmizade");
                }

                if (isPesquisaAtivada() && listaFiltrada != null) {

                    Collections.sort(listaFiltrada, contatoComparator);

                    adapterContactList.updateContatoList(listaFiltrada, new AdapterContactList.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                } else if (!isPesquisaAtivada() && listaContatos != null) {

                    Collections.sort(listaContatos, contatoComparator);
                    adapterContactList.updateContatoList(listaContatos, new AdapterContactList.ListaAtualizadaCallback() {
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

    private void atualizarPorPayload(Contatos contatoAtualizado, String tipoPayload) {
        ToastCustomizado.toastCustomizadoCurto(tipoPayload, requireContext());

        int index = posicaoChanged;

        if (index != -1) {

            if (isPesquisaAtivada() && referenceFiltroHashMap != null
                    && !referenceFiltroHashMap.isEmpty()
                    && referenceFiltroHashMap.containsKey(contatoAtualizado.getIdContato())) {
                ToastCustomizado.toastCustomizadoCurto("CODE NOOOO", requireContext());
                contactDAOFiltrado.atualizarContatoPorPayload(contatoAtualizado, tipoPayload, new ContactDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        adapterContactList.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
            if (idsUsuarios != null && idsUsuarios.size() > 0
                    && idsUsuarios.contains(contatoAtualizado.getIdContato())) {
                ToastCustomizado.toastCustomizadoCurto("CODE OK", requireContext());
                contactDiffDAO.atualizarContatoPorPayload(contatoAtualizado, tipoPayload, new ContactDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        if (!isPesquisaAtivada()) {
                            adapterContactList.notifyItemChanged(index, bundleRecup);
                        } else {
                            idsParaAtualizar.put(contatoAtualizado.getIdContato(), bundleRecup);
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
    public void onRemocao(Contatos contatoAlvo, int posicao) {
        if (contatoAlvo != null) {
            logicaRemocao(contatoAlvo, false, true);
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

    private void verificaExistencia(String idContatos, VerificaExistenciaCallback callback) {
        DatabaseReference verificaExistenciaRef = firebaseRef.child("contatos")
                .child(idUsuario).child(idContatos);
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onExistencia(snapshot.getValue() != null, snapshot.getValue(Contatos.class));
                verificaExistenciaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void anexarNovoDado(Contatos contatoModificado) {
        newDataRef = firebaseRef.child("contatos")
                .child(idUsuario).orderByChild("idContato")
                .equalTo(contatoModificado.getIdContato()).limitToFirst(1);
        idsAIgnorarListeners.add(contatoModificado.getIdContato());
        childEventListenerNewData = newDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Contatos contatoModificado = snapshot.getValue(Contatos.class);
                    if (contatoModificado == null) {
                        return;
                    }
                    String idUserContatos = contatoModificado.getIdContato();
                    adicionarContatos(contatoModificado, true);
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
                    Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                    ToastCustomizado.toastCustomizado("DELETE PELO NEW DATA", requireContext());
                    if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                            && idsAIgnorarListeners.contains(contatoRemovido.getIdContato())) {
                        idsAIgnorarListeners.remove(contatoRemovido.getIdContato());
                    }
                    logicaRemocao(contatoRemovido, true, true);
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

    private void recuperarDetalhes(Set<Usuario> listaIdsRecuperados) {
        for (Usuario usuarioPesquisa : listaIdsRecuperados) {
            aosFiltros++;
            if (aosFiltros > listaIdsRecuperados.size()) {
                aosFiltros = 0;
                return;
            }

            childListenerMoreFiltro = null;
            queryLoadMoreFiltro = null;

            queryLoadMoreFiltro = firebaseRef.child("contatos")
                    .child(idUsuario).orderByChild("idContato").equalTo(usuarioPesquisa.getIdUsuario()).limitToFirst(1);

            //ToastCustomizado.toastCustomizadoCurto("AOS FILTROS: " + usuarioPesquisa.getIdUsuario(), requireContext());
            //ToastCustomizado.toastCustomizadoCurto("NR FILTROS: " + aosFiltros, requireContext());

            ChildEventListener childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Contatos contatoMore = snapshot.getValue(Contatos.class);
                    if (contatoMore != null
                            && contatoMore.getIdContato() != null
                            && !contatoMore.getIdContato().isEmpty()) {

                        //**ToastCustomizado.toastCustomizadoCurto("NEW PESQUISA: " + contatoMore.getIdUsuario(), requireContext());
                        Log.d(TAG, "Timestamp key: " + lastTimestamp);
                        Log.d(TAG, "id: " + contatoMore.getIdContato() + " time: " + contatoMore.getTimestampContato());
                        if (listaFiltrada != null && listaFiltrada.size() > 1 && idsFiltrados != null && idsFiltrados.size() > 0
                                && idsFiltrados.contains(contatoMore.getIdContato())) {
                            Log.d(TAG, "Id já existia: " + contatoMore.getIdContato());
                            ToastCustomizado.toastCustomizadoCurto("ID JÁ EXISTIA " + contatoMore.getIdContato(), requireContext());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        List<Contatos> newContatos = new ArrayList<>();
                        String key = usuarioPesquisa.getNomeUsuarioPesquisa();
                        if (lastName != null && !lastName.isEmpty() && key != null
                                && !key.isEmpty()) {
                            if (!key.equals(lastName) || listaFiltrada.size() > 0 &&
                                    !contatoMore.getIdContato()
                                            .equals(listaFiltrada.get(listaFiltrada.size() - 1).getIdContato())) {
                                newContatos.add(contatoMore);
                                //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                lastName = key;
                            }
                        }
                        // Remove a última chave usada
                        if (newContatos.size() > PAGE_SIZE) {
                            newContatos.remove(0);
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (aosFiltros >= listaIdsRecuperados.size()) {
                                aosFiltros = 0;
                            }
                            adicionarMaisDadosFiltrados(newContatos, contatoMore.getIdContato(), queryLoadMoreFiltro, childListenerMoreFiltro);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() == null) {
                        return;
                    }
                    Contatos contatoUpdate = snapshot.getValue(Contatos.class);

                    if (contatoUpdate == null) {
                        return;
                    }

                    if (idsAIgnorarListeners != null && idsAIgnorarListeners.size() > 0
                            && idsAIgnorarListeners.contains(snapshot.getValue(Contatos.class).getIdContato())) {
                        ToastCustomizado.toastCustomizadoCurto("IGNORAR CHANGED" + snapshot.getValue(Contatos.class).getIdContato(), requireContext());
                        return;
                    }
                    if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                            && listenerHashMapNEWDATA.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                        return;
                    }

                    if (listenerHashMap != null && listenerHashMap.size() > 0
                            && listenerHashMap.containsKey(snapshot.getValue(Contatos.class).getIdContato())) {
                        return;
                    }

                    if (listaFiltrada != null && listaFiltrada.size() > 0
                            && contatoUpdate.getIdContato().equals(listaFiltrada.get(0).getIdContato())) {
                        return;
                    }

                    ToastCustomizado.toastCustomizadoCurto("ATUALIZAR PELO SEARCH + DADOS " + contatoUpdate.getIdContato(), requireContext());
                    logicaAtualizacao(snapshot, false);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Contatos contatoRemovido = snapshot.getValue(Contatos.class);
                        if (contatoRemovido == null) {
                            return;
                        }

                        if (listenerHashMapNEWDATA != null && listenerHashMapNEWDATA.size() > 0
                                && listenerHashMapNEWDATA.containsKey(contatoRemovido.getIdContato())
                                || listaContatos != null && listaContatos.size() > 0
                                && listaContatos.get(0).getIdContato().equals(contatoRemovido.getIdContato())) {
                            return;
                        }

                        verificaExistencia(contatoRemovido.getIdContato(), new VerificaExistenciaCallback() {
                            @Override
                            public void onExistencia(boolean status, Contatos contatoAtualizado) {

                                ToastCustomizado.toastCustomizado("DELETE ++ DADOS " + contatoRemovido.getIdContato(), requireContext());

                                logicaRemocao(contatoRemovido, true, true);

                                if (status) {
                                    boolean menorque = contatoAtualizado.getTimestampContato() <= listaContatos.get(0).getTimestampContato();
                                    if (!menorque) {
                                        ToastCustomizado.toastCustomizadoCurto("Novo dado pela remocao do + dados " + contatoRemovido.getIdContato(), requireContext());
                                        anexarNovoDado(contatoAtualizado);
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
        queryUltimoElemento = firebaseRef.child("contatos")
                .child(idUsuario).orderByChild("timestampContato").limitToLast(1);
        listenerUltimoElemento = queryUltimoElemento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElemento = snapshot1.getValue(Contatos.class).getIdContato();
                    setLoading(false);
                    if (callback != null && listaContatos != null && listaContatos.isEmpty()) {
                        callback.onRecuperado();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaContatos != null && listaContatos.isEmpty()) {
                    callback.onRecuperado();
                }
            }
        });
    }

    private void ultimoElementoFiltro(String nome, RecuperaUltimoElemento callback) {
        queryUltimoElementoFiltro = firebaseRef.child("contatos_by_name")
                .child(idUsuario)
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToLast(1);
        listenerUltimoElementoFiltro = queryUltimoElementoFiltro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idUltimoElementoFiltro = snapshot1.getValue(Usuario.class).getIdUsuario();
                    setLoading(false);
                    if (callback != null && listaFiltrada != null && listaFiltrada.isEmpty()) {
                        callback.onRecuperado();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null && listaFiltrada != null && listaFiltrada.isEmpty()) {
                    callback.onRecuperado();
                }
            }
        });
    }

    public int findPositionInList(String userId) {
        for (int i = 0; i < listaContatos.size(); i++) {
            Contatos contato = listaContatos.get(i);
            if (contato.getIdContato().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}