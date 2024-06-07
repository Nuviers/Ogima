package com.example.ogima.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterHeaderHiddenViews;
import com.example.ogima.adapter.AdapterHiddenViews;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ContactDiffDAO;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.TimestampUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewerBloqueadoFragment extends Fragment implements AdapterHiddenViews.RemoverViewerListener, AdapterHiddenViews.AnimacaoIntent, AdapterHiddenViews.RecuperaPosicaoAnterior {

    private String idUsuario = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private ImageView imgViewIncFundoProfile, imgViewIncFotoProfile;
    private RecyclerView recyclerView;
    private AdapterHeaderHiddenViews adapterHeader;
    private LinearLayoutManager linearLayoutManagerHeader;
    private SpinKitView spinProgress;
    private int mCurrentPosition = -1;
    private List<Usuario> listaViewers = new ArrayList<>();
    private AdapterHiddenViews adapterHiddenViews;
    private FirebaseUtils firebaseUtils;
    private boolean isLoading = false;
    private ConcatAdapter concatAdapter;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private boolean trocarQueryInicial = false;
    private Query queryInicial, queryLoadMore, queryUltimoElemento, newDataRef;
    private static int PAGE_SIZE = 10;
    private long lastTimestamp = -1;
    private ValueEventListener listenerUltimoElemento;
    private String idUltimoElemento = "";
    private ChildEventListener childListenerInicio, childEventListenerNewData,
            childEventListenerViewers;
    private String idPrimeiroDado = "";
    private int travar = 0;
    private HashMap<String, ChildEventListener> listenerHashMapNEWDATA = new HashMap<>();
    private Set<String> idsListenersNEWDATA = new HashSet<>();
    private HashMap<String, Query> referenceHashMapNEWDATA = new HashMap<>();
    private int contadorRemocaoListenerNEWDATA = 0;
    private Set<String> idsUsuarios = new HashSet<>();
    private RecyclerView.OnScrollListener scrollListener;
    private static final String TAG = "ViewerBloqueadotag";
    private Set<String> idsAIgnorarListeners = new HashSet<>();
    private Set<String> idsListeners = new HashSet<>();
    private HashMap<String, Query> referenceHashMap = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMap = new HashMap<>();
    private int contadorRemocaoListener = 0;
    private int posicaoChanged = -1;

    private DatabaseReference verificaAdsRef, verificaNrCoinsRef;
    private ValueEventListener listenerVerificaAds, listenerNrCoins;

    @Override
    public void onStop() {
        super.onStop();
        if (adapterHeader != null && linearLayoutManagerHeader != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManagerHeader.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                listaViewers != null && !listaViewers.isEmpty()
                && linearLayoutManagerHeader != null) {
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

    private interface RecuperaUltimoElemento {
        void onRecuperado();
    }

    private interface VerificaExistenciaCallback {
        void onExistencia(boolean status, Usuario viewerAtualizado);

        void onError(String message);
    }

    public ViewerBloqueadoFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewer_bloqueado, container, false);
        inicializandoComponentes(view);
        configInicial();
       /*
        GlideCustomizado.loadUrl(requireContext(), "https://media1.tenor.com/m/J101CRtx7-gAAAAC/jinshi-jinxi.gif",
                imgViewIncFotoProfile, android.R.color.transparent, GlideCustomizado.CIRCLE_CROP, false, false);
        GlideCustomizado.loadUrl(requireContext(), "https://media1.tenor.com/m/qdidAJiZjOMAAAAC/wuthering-waves-yanyan.gif",
                imgViewIncFundoProfile, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false, false);
        */
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
                configRecycler(epilepsia);
                verificaAdsVencidas();
                atualizarNrAdsVisualizadas();
                atualizarNrCoins();
                usuarioDiffDAO = new UsuarioDiffDAO(listaViewers, adapterHiddenViews);
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

    private void configRecycler(boolean statusEpilepsia) {
        if (linearLayoutManagerHeader == null) {
            linearLayoutManagerHeader = new LinearLayoutManager(requireContext());
            linearLayoutManagerHeader.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManagerHeader);
            if (adapterHeader == null) {
                adapterHeader = new AdapterHeaderHiddenViews(requireActivity(), requireContext());
                adapterHiddenViews = new AdapterHiddenViews(requireContext(),
                        listaViewers, listaDadosUser, getResources().getColor(R.color.chat_list_color), this,
                        this, this);
            }
            concatAdapter = new ConcatAdapter(adapterHeader, adapterHiddenViews);
            recyclerView.setAdapter(concatAdapter);
            adapterHiddenViews.setStatusEpilepsia(statusEpilepsia);
        }
    }

    private void clickListeners() {

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
                    if (linearLayoutManagerHeader != null) {
                        int lastVisibleItemPosition = linearLayoutManagerHeader.findLastVisibleItemPosition();
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isLoading()) {
                                    return;
                                }
                                int totalItemCount = linearLayoutManagerHeader.getItemCount();
                                if (lastVisibleItemPosition == totalItemCount - 1) {
                                    setLoading(true);
                                    carregarMaisDados();
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
        if (listaViewers != null && !listaViewers.isEmpty()) {
            trocarQueryInicial = false;
            return;
        }
        if (trocarQueryInicial && lastTimestamp != -1) {
            queryInicial = firebaseRef.child("profileViews")
                    .child(idUsuario).orderByChild("timeStampView")
                    .startAt(lastTimestamp + 1)
                    .limitToFirst(1);
        } else {
            queryInicial = firebaseRef.child("profileViews")
                    .child(idUsuario).orderByChild("timeStampView").limitToFirst(1);
        }
        exibirProgress();
        ultimoElemento(new RecuperaUltimoElemento() {
            @Override
            public void onRecuperado() {
                childListenerInicio = queryInicial.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                        if (snapshot.getValue() != null) {
                            Usuario viewer = snapshot.getValue(Usuario.class);
                            if (viewer != null
                                    && viewer.getIdUsuario() != null
                                    && !viewer.getIdUsuario().isEmpty()) {
                                if (travar == 0) {
                                    lastTimestamp = viewer.getTimeStampView();
                                    adicionarViewer(viewer, false);
                                } else {
                                    ToastCustomizado.toastCustomizadoCurto("Novo dado pelo inicio " + viewer.getIdUsuario(), requireContext());
                                    //Dado mais recente que o anterior
                                    if (listenerHashMapNEWDATA != null && !listenerHashMapNEWDATA.isEmpty()
                                            && listenerHashMapNEWDATA.containsKey(viewer.getIdUsuario())) {
                                        return;
                                    }
                                    ToastCustomizado.toastCustomizadoCurto("Novo dado pelo inicio " + viewer.getIdUsuario(), requireContext());
                                    anexarNovoDado(viewer);
                                }
                            } else {
                                ocultarProgress();
                                //Exibir um textview com essa mensagem.
                                String msgSemConversas = "Você não possui visualizações no seu perfil no momento.";
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {
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
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    private void ultimoElemento(RecuperaUltimoElemento callback) {
        queryUltimoElemento = firebaseRef.child("profileViews")
                .child(idUsuario).orderByChild("timeStampView").limitToLast(1);
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

    private void verificaExistencia(String idViewer, VerificaExistenciaCallback callback) {
        DatabaseReference verificaExistenciaRef = firebaseRef.child("profileViews")
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

    private void adicionarViewer(Usuario viewerAlvo, boolean dadoModificado) {
        recuperaDadosUser(viewerAlvo.getIdUsuario(), new RecuperaUser() {
            @Override
            public void onRecuperado(Usuario dadosUser) {

                usuarioDiffDAO.adicionarUsuario(viewerAlvo);
                usuarioDiffDAO.adicionarIdAoSet(idsUsuarios, dadosUser.getIdUsuario());

                List<Usuario> listaAtual = new ArrayList<>();
                listaAtual = listaViewers;

                //Collections.sort(listaAtual, contatoComparator);

                adapterHiddenViews.updateViewerList(listaAtual, new AdapterHiddenViews.ListaAtualizadaCallback() {
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

    private void carregarMaisDados() {
        if (listaViewers.size() > 1
                && idUltimoElemento != null && !idUltimoElemento.isEmpty()
                && idUltimoElemento.equals(listaViewers.get(listaViewers.size() - 1).getIdUsuario())) {
            ocultarProgress();
            ToastCustomizado.toastCustomizadoCurto("RETORNO ANTI DUPLICATA CHAT " + idUltimoElemento, requireContext());
            return;
        }

        queryLoadMore = firebaseRef.child("profileViews")
                .child(idUsuario)
                .orderByChild("timeStampView")
                .startAt(lastTimestamp)
                .limitToFirst(PAGE_SIZE);
        childEventListenerViewers = queryLoadMore.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                exibirProgress();
                if (snapshot.getValue() != null) {
                    Usuario viewerMore = snapshot.getValue(Usuario.class);
                    if (viewerMore != null
                            && viewerMore.getIdUsuario() != null
                            && !viewerMore.getIdUsuario().isEmpty()) {
                        Log.d(TAG, "Timestamp key: " + lastTimestamp);
                        Log.d(TAG, "id: " + viewerMore.getIdUsuario() + " time: " + viewerMore.getTimeStampView());
                        if (listaViewers != null && listaViewers.size() > 1 && idsUsuarios != null && !idsUsuarios.isEmpty()
                                && idsUsuarios.contains(viewerMore.getIdUsuario())) {
                            Log.d(TAG, "Id já existia: " + viewerMore.getIdUsuario());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        if (listaViewers != null && listaViewers.size() > 1
                                && viewerMore.getTimeStampView() < listaViewers.get(0).getTimeStampView()) {
                            ToastCustomizado.toastCustomizadoCurto("TIME IGNORADO", requireContext());
                            ocultarProgress();
                            setLoading(false);
                            return;
                        }

                        //*ToastCustomizado.toastCustomizadoCurto("ADICIONADO " + contatoMore.getIdUsuario(), requireContext());
                        List<Usuario> newViewers = new ArrayList<>();
                        long key = viewerMore.getTimeStampView();
                        if (lastTimestamp != -1 && key != -1) {
                            if (key != lastTimestamp || !listaViewers.isEmpty() &&
                                    !viewerMore.getIdUsuario()
                                            .equals(listaViewers.get(listaViewers.size() - 1).getIdUsuario())) {
                                newViewers.add(viewerMore);
                                //ToastCustomizado.toastCustomizado("TIMESTAMP MAIS DADOS: " + lastTimestamp, requireContext());
                                lastTimestamp = key;
                            }
                        }
                        // Remove a última chave usada
                        if (newViewers.size() > PAGE_SIZE) {
                            newViewers.remove(0);
                        }
                        if (lastTimestamp != -1) {

                            recuperaDadosUser(viewerMore.getIdUsuario(), new RecuperaUser() {
                                @Override
                                public void onRecuperado(Usuario dadosUser) {
                                    adicionarMaisDados(newViewers, viewerMore.getIdUsuario(), dadosUser, queryLoadMore);
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
                                boolean menorque = viewerAtualizado.getTimeStampView() <= listaViewers.get(0).getTimeStampView();
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

    private void adicionarMaisDados(List<Usuario> newViewers, String idUser, Usuario dadosUser, Query queryAlvo) {
        if (newViewers != null && !newViewers.isEmpty()) {
            usuarioDiffDAO.carregarMaisUsuario(newViewers, idsUsuarios);
            usuarioDiffDAO.adicionarIdAoSet(idsUsuarios, idUser);

            //Collections.sort(listaViewers, contatoComparator);
            adapterHiddenViews.updateViewerList(listaViewers, new AdapterHiddenViews.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                    ocultarProgress();
                    adicionarDadoDoUsuario(dadosUser, queryAlvo, childEventListenerViewers, false);
                    setLoading(false);
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

        adicionarListener(dadosUser.getIdUsuario(), queryAlvo, childEventListenerAlvo);
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

    private void limparPeloDestroyView() {
        idsAIgnorarListeners.clear();
        firebaseUtils.removerQueryChildListener(newDataRef, childEventListenerNewData);
        firebaseUtils.removerQueryChildListener(queryInicial, childListenerInicio);
        firebaseUtils.removerQueryChildListener(queryLoadMore, childEventListenerViewers);
        firebaseUtils.removerQueryValueListener(queryUltimoElemento, listenerUltimoElemento);
        firebaseUtils.removerValueListener(verificaAdsRef, listenerVerificaAds);
        firebaseUtils.removerValueListener(verificaNrCoinsRef, listenerNrCoins);
        removeValueEventListener();
        removeValueEventListenerNEWDATA();
        if (usuarioDiffDAO != null) {
            usuarioDiffDAO.limparListaUsuarios();
        }
        if (listaDadosUser != null) {
            listaDadosUser.clear();
        }
        if (idsUsuarios != null) {
            idsUsuarios.clear();
        }
        mCurrentPosition = -1;
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

    private void logicaRemocao(Usuario viewerRemovido, boolean ignorarVerificacao, boolean excluirDaLista) {

        if (viewerRemovido == null) {
            return;
        }

        DatabaseReference verificaExistenciaRef = firebaseRef.child("profileViews")
                .child(idUsuario).child(viewerRemovido.getIdUsuario());
        verificaExistenciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || ignorarVerificacao) {
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
                        int posicao = adapterHiddenViews.findPositionInList(viewerRemovido.getIdUsuario());
                        if (posicao != -1) {
                            adapterHiddenViews.notifyItemChanged(posicao);
                        }
                    }

                    if (listaViewers != null) {
                        adapterHiddenViews.updateViewerList(listaViewers, new AdapterHiddenViews.ListaAtualizadaCallback() {
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

            ToastCustomizado.toastCustomizadoCurto("BORA ATUALIZAR", requireContext());

            if (viewerAtualizado == null || viewerAtualizado.getIdUsuario() == null) {
                return;
            }

            posicaoChanged = adapterHiddenViews.findPositionInList(viewerAtualizado.getIdUsuario());

            if (posicaoChanged != -1) {
                Usuario viewerAnterior = new Usuario();
                if (idsUsuarios != null && !idsUsuarios.isEmpty()
                        && idsUsuarios.contains(viewerAtualizado.getIdUsuario())) {
                    //Já existe um listener na listagem normal
                    viewerAnterior = listaViewers.get(posicaoChanged);
                }
                ToastCustomizado.toastCustomizadoCurto("Alterado: " + viewerAnterior.getIdUsuario(), requireContext());

                if (viewerAnterior.isViewLiberada() != viewerAtualizado.isViewLiberada()) {
                    atualizarPorPayload(viewerAtualizado, "viewLiberada");
                }

                if (listaViewers != null) {

                    //Collections.sort(listaViewers, contatoComparator);
                    adapterHiddenViews.updateViewerList(listaViewers, new AdapterHiddenViews.ListaAtualizadaCallback() {
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
            if (idsUsuarios != null && !idsUsuarios.isEmpty()
                    && idsUsuarios.contains(viewerAtualizado.getIdUsuario())) {
                ToastCustomizado.toastCustomizadoCurto("CODE OK", requireContext());
                usuarioDiffDAO.atualizarUsuarioPorPayload(viewerAtualizado, tipoPayload, new UsuarioDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        adapterHiddenViews.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
        }
    }

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, requireActivity());
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, requireActivity());
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
    public void onRemocao(Usuario viewerAlvo, int posicao) {
        if (viewerAlvo != null) {
            logicaRemocao(viewerAlvo, false, true);
        }
    }

    @Override
    public void onExecutarAnimacao() {
        requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private boolean areFirstThreeItemsVisible(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibleItemPosition = 0;
        if (layoutManager != null) {
            firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        }
        return firstVisibleItemPosition <= 2;
    }

    private void anexarNovoDado(Usuario viewerModificado) {
        newDataRef = firebaseRef.child("profileViews")
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

    private void atualizarNrAdsVisualizadas() {
        verificaAdsRef = firebaseRef.child("adsPrevious")
                .child(idUsuario).child("nrAdsVisualizadas");
        listenerVerificaAds = verificaAdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    int nrAdsVisualizadas = snapshot.getValue(Integer.class);
                    if (nrAdsVisualizadas != -1) {
                        adapterHeader.setNrAds(nrAdsVisualizadas);
                    }
                }else{
                    adapterHeader.setNrAds(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void atualizarNrCoins() {
        verificaNrCoinsRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("ogimaCoins");
        listenerNrCoins = verificaNrCoinsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    long nrCoins = snapshot.getValue(Long.class);
                    adapterHeader.setNrCoins(nrCoins);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void verificaAdsVencidas(){
        TimestampUtils.RecuperarTimestamp(requireContext(), new TimestampUtils.RecuperarTimestampCallback() {
            @Override
            public void onRecuperado(long timestampNegativo) {
                long timestampAtual = Math.abs(timestampNegativo);
                DatabaseReference queryAdAnterior = firebaseRef.child("adsPrevious")
                        .child(idUsuario);
                queryAdAnterior.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario detalhesAd = snapshot.getValue(Usuario.class);
                            if (timestampAtual >= detalhesAd.getTimestampValidity()) {
                                //>= 12 horas referente ao último anúncio visto (Resetar).
                                snapshot.getRef().removeValue();
                                adapterHeader.setInteracaoEmAndamento(false);
                            }else{
                                adapterHeader.setInteracaoEmAndamento(false);
                            }
                        } else {
                            adapterHeader.setInteracaoEmAndamento(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        adapterHeader.setInteracaoEmAndamento(false);
                    }
                });
            }

            @Override
            public void onError(String message) {
                adapterHeader.setInteracaoEmAndamento(false);
            }
        });
    }

    private void inicializandoComponentes(View view) {
        imgViewIncFotoProfile = view.findViewById(R.id.imgViewIncFotoProfile);
        imgViewIncFundoProfile = view.findViewById(R.id.imgViewIncFundoProfile);
        recyclerView = view.findViewById(R.id.recyclerViewHiddenViews);
        spinProgress = view.findViewById(R.id.spinProgressBarRecycler);
    }
}