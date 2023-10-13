package com.example.ogima.fragment;


import android.app.ProgressDialog;
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
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFindPeoples;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class AmigosFragment extends Fragment implements AdapterFindPeoples.AnimacaoIntent, AdapterFindPeoples.RecuperaPosicaoAnterior {

    private String idUsuario = "";
    private SearchView searchView;
    private RecyclerView recyclerView;
    private ImageView imgViewProcurarGrupos, imgViewSocialUp;
    private Button btnProcurarGrupos, btnVerComunidades;
    private ImageSlider imageSliderSocial;
    private boolean epilepsia = true;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private ArrayList<SlideModel> slideModels = new ArrayList<>();
    private AdapterFindPeoples adapterFindPeoples;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
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
    private RelativeLayout relativeLayoutSocial;

    public AmigosFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
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
    public void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            setPesquisaAtivada(false);
            configRecycler();
            configSearchView();
            usuarioDAOFiltrado = new UsuarioDiffDAO(listaFiltrada, adapterFindPeoples);
            setLoading(true);
            configPaginacao();
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterFindPeoples != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 &&
                listaFiltrada != null && listaFiltrada.size() > 0
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
        View view = inflater.inflate(R.layout.fragment_amigos, container, false);
        inicializandoComponentes(view);
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean statusEpilepsia) {
                epilepsia = statusEpilepsia;
                configInicial();
                configSlider();
                clickListeners();
            }

            @Override
            public void onSemDado() {

            }

            @Override
            public void onError(String message) {

            }
        });
        return view;
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (adapterFindPeoples == null) {
            adapterFindPeoples = new AdapterFindPeoples(requireContext(),
                    listaFiltrada, this, this, listaDadosUser);
        }
        recyclerView.setAdapter(adapterFindPeoples);
        adapterFindPeoples.setFiltragem(false);
    }

    private void configSearchView() {
        searchView.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {

                    if (listaFiltrada != null && listaFiltrada.size() > 0) {
                        limparFiltragem();
                    }

                    adapterFindPeoples.setFiltragem(true);
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

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    recyclerView.setVisibility(View.VISIBLE);
                    relativeLayoutSocial.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    relativeLayoutSocial.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void dadoInicialFiltragem(String nome) {

        //ToastCustomizado.toastCustomizadoCurto("Pesquisa " + nome, requireContext());

        queryInicialFiltro = firebaseRef.child("usuarios")
                .orderByChild("nomeUsuarioPesquisa")
                .startAt(nome).endAt(nome + "\uf8ff").limitToFirst(2);

        childListenerInicioFiltro = queryInicialFiltro.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioFiltrado = snapshot.getValue(Usuario.class);
                    lastName = usuarioFiltrado.getNomeUsuarioPesquisa();
                    if (usuarioFiltrado != null && usuarioFiltrado.getNomeUsuarioPesquisa() != null) {
                        if (usuarioFiltrado != null &&
                                !usuarioFiltrado.getIdUsuario().equals(idUsuario)) {
                            adicionarUserFiltrado(usuarioFiltrado);
                        }
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
                lastName = null;
            }
        });
    }

    private void adicionarUserFiltrado(Usuario dadosUser) {
        //ToastCustomizado.toastCustomizadoCurto("Nome " + dadosUser.getNomeUsuario(), requireContext());
        if (listaFiltrada != null && listaFiltrada.size() >= 2) {
            setLoading(false);
            return;
        }
        usuarioDAOFiltrado.adicionarUsuario(dadosUser);
        idsFiltrados.add(dadosUser.getIdUsuario());
        adapterFindPeoples.updateUsersList(listaFiltrada);
        adicionarDadoDoUsuario(dadosUser);
        setLoading(false);
    }

    private void limparFiltragem() {
        lastName = null;
        idsFiltrados.clear();
        adapterFindPeoples.setFiltragem(false);
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

        if (listaFiltrada != null) {
            adapterFindPeoples.updateUsersList(listaFiltrada);
        }
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
        if (listaFiltrada != null && listaFiltrada.size() > 0
                && lastName != null && !lastName.isEmpty()) {

            queryLoadMoreFiltro = firebaseRef.child("usuarios")
                    .orderByChild("nomeUsuarioPesquisa")
                    .startAt(lastName).endAt(dadoAnterior + "\uf8ff").limitToFirst(PAGE_SIZE);

            childListenerLoadMoreFiltro = queryLoadMoreFiltro.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.getValue() != null) {
                        ToastCustomizado.toastCustomizadoCurto("Scrolled " + lastName,requireContext());
                        Usuario usuarioMore = snapshot.getValue(Usuario.class);

                        List<Usuario> newUsuario = new ArrayList<>();

                        String key = snapshot.child("nomeUsuarioPesquisa").getValue(String.class);

                        if (lastName != null && key != null && !key.equals(lastName)) {
                            if (usuarioMore != null &&
                                    !usuarioMore.getIdUsuario().equals(idUsuario)) {
                                newUsuario.add(usuarioMore);
                            }
                            lastName = key;
                        }

                        // Remove a última chave usada
                        if (newUsuario.size() > PAGE_SIZE) {
                            newUsuario.remove(0);
                        }

                        if (lastName != null && !lastName.isEmpty()) {
                            adicionarMaisDadosFiltrados(newUsuario, usuarioMore);
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
                    lastName = null;
                }
            });
        }
    }

    private void adicionarMaisDadosFiltrados(List<Usuario> newUsuario, Usuario dadosUser) {
        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDAOFiltrado.carregarMaisUsuario(newUsuario, idsFiltrados);
            //*Usuario usuarioComparator = new Usuario(true, false);
            //*Collections.sort(listaViewers, usuarioComparator);
            adapterFindPeoples.updateUsersList(listaFiltrada);
            adicionarDadoDoUsuario(dadosUser);
            setLoading(false);
        }
    }

    private void configSlider() {
        //Configuração do slider
        slideModels.add(new SlideModel
                (R.drawable.banner_chat_random_final_v1, "Chats com pessoas aleatórias contendo duas categorias (Comum e às cegas)",
                        null));

        slideModels.add(new SlideModel
                (R.drawable.banner_final_chat_comum, "Chat comum - Sua aparência será exibida",
                        null));

        slideModels.add(new SlideModel
                (R.drawable.banner_final_chat_as_cegas, "Chat às cegas - Sua aparência será revelada somente quando os dois usuários quiserem se revelar",
                        null));

        //Setando o arrayList SlideModel no Slider
        imageSliderSocial.setImageList(slideModels, ScaleTypes.CENTER_CROP);

        //Ouvinte do slider
        imageSliderSocial.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == 0) {
                    ToastCustomizado.toastCustomizadoCurto("Zero", getContext());
                    exibirBottomSheet();
                }
                if (i == 1) {
                    ToastCustomizado.toastCustomizadoCurto("Um", getContext());
                }
                if (i == 2) {
                    ToastCustomizado.toastCustomizadoCurto("Dois", getContext());
                }
            }
        });
    }

    private void exibirBottomSheet() {

    }

    private void configInicial() {
        GlideCustomizado.loadGifPorDrawable(requireContext(), R.drawable.ic_gif_grupos_publicos,
                imgViewProcurarGrupos, android.R.color.transparent, epilepsia);
        adapterFindPeoples.setStatusEpilepsia(epilepsia);
    }

    private void clickListeners() {

    }

    private void inicializandoComponentes(View view) {
        searchView = view.findViewById(R.id.searchViewFindPeoples);
        recyclerView = view.findViewById(R.id.recyclerViewFindPeoples);
        imgViewProcurarGrupos = view.findViewById(R.id.imgViewProcurarGrupos);
        btnProcurarGrupos = view.findViewById(R.id.btnProcurarGrupos);
        imgViewSocialUp = view.findViewById(R.id.imgViewSocialUp);
        btnVerComunidades = view.findViewById(R.id.btnVerComunidades);
        imageSliderSocial = view.findViewById(R.id.imageSliderSocial);
        relativeLayoutSocial = view.findViewById(R.id.relativeLayoutSocial);
    }

    @Override
    public void onExecutarAnimacao() {
        requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, requireContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    private void adicionarDadoDoUsuario(Usuario dadosUser) {
        listaDadosUser.put(dadosUser.getIdUsuario(), dadosUser);
    }
}
