package com.example.ogima.fragment;


import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.LogicaFeedTesteActivity;
import com.example.ogima.adapter.AdapterHeaderInicio;
import com.example.ogima.adapter.AdapterLogicaFeed;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.PostagemDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FrameSuporteInicioFragment extends Fragment implements AdapterLogicaFeed.RemoverListenerRecycler, AdapterLogicaFeed.RemoverPostagemListener, AdapterLogicaFeed.RecuperaPosicao {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private RecyclerView recyclerViewInicial;
    private boolean dadosCarregados = false;
    private AdapterHeaderInicio adapterHeader;
    private LinearLayoutManager linearLayoutManagerFeed;
    private List<Postagem> listaPostagens = new ArrayList<>();
    private final static int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isScrolling = false;
    private ChildEventListener childEventListenerInicio, childEventListenerLoadMore;
    private Query queryInicial, queryLoadMore, queryPostPopular;
    private Set<String> idsPostagens = new HashSet<>();
    private Set<String> idsDonoPostagens = new HashSet<>();
    private boolean primeiroCarregamento = true;
    private PostagemDiffDAO postagemDiffDAO;
    private AdapterLogicaFeed adapterLogicaFeed;
    private HashMap<String, Double> interessesUserAtual = new HashMap<>();
    private boolean semInteresses = false;
    private Handler handler, handlerLoad;
    private int qntNovaBusca = 0;
    private List<String> interessesRelevantes = new ArrayList<>();
    private int INTERVALO_POSTAGEM_POPULAR = 5;
    private int lastNrLike = 0;
    //Serve como parâmetro para a lógica que verifica o intervalo de postagens.
    private int itensExibidos = 0;
    int previousListSize = 0;
    private boolean loadPostPop = false;
    //Retorna para posição anterior
    private int mCurrentPosition = -1;
    private ExoPlayer exoPlayer;
    private RecyclerView.OnScrollListener scrollListener;
    private HashMap<String, Object> listaDadosUser = new HashMap<>();

    private int ultimoVideoVisivel = -1;
    private int currentVideoVisible = -1;
    private boolean paginacaoChamada = false;
    //Serve como parâmetro para que o query de busca de postagens populares
    //saiba a quantidade de postagens que deve trazer.
    private int nrBuscaPop = 1;
    private boolean buscaPop = false;

    @Override
    public void onPostagemRemocao(Postagem postagemRemovida, int posicao, ImageButton imgBtnExcluir) {

    }

    @Override
    public void onPosicao(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, requireContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onRemoverListener() {

    }

    @Override
    public void onError() {

    }

    public interface RecuperaDadoEpilpesia {
        void onRecuperado(boolean epilepsia);

        void onError(String message);
    }

    public interface InteressesRecuperadosCallback {
        void onRecuperado();

        void onError(String message);
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
    public void onStart() {
        super.onStart();

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        if (primeiroCarregamento) {
            postagemDiffDAO = new PostagemDiffDAO(listaPostagens, adapterLogicaFeed);
            configRecyclerView();
            setLoading(true);
            recuperarInteressesUserAtual(new FrameSuporteInicioFragment.InteressesRecuperadosCallback() {
                @Override
                public void onRecuperado() {
                    if (semInteresses) {
                        return;
                    }
                    //Comentado para evitar gastos desnecessários durante a fase de teste.
                    //**** recuperarPostagensIniciais();
                    primeiroCarregamento = false;
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 &&
                listaPostagens != null && listaPostagens.size() > 0
                && linearLayoutManagerFeed != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    recyclerViewInicial.scrollToPosition(mCurrentPosition);
                }
            }, 100);

            if (exoPlayer != null) {
                adapterLogicaFeed.resumePlayer();
            }
        }
        mCurrentPosition = -1;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (exoPlayer != null && adapterLogicaFeed != null) {
            adapterLogicaFeed.pausePlayer();
        }

        if (adapterLogicaFeed != null && linearLayoutManagerFeed != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManagerFeed.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try{
            if (exoPlayer != null) {
                adapterLogicaFeed.releasePlayer();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (handlerLoad != null) {
            handlerLoad.removeCallbacksAndMessages(null);
        }

        if (childEventListenerLoadMore != null) {
            queryLoadMore.removeEventListener(childEventListenerLoadMore);
            childEventListenerLoadMore = null;
        }

        if (adapterLogicaFeed != null && recyclerViewInicial != null) {
            listaPostagens.clear();
        }
        idsPostagens.clear();

        mCurrentPosition = -1;
    }

    public FrameSuporteInicioFragment() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frame_suporte_inicio, container, false);
        inicializandoComponentes(view);
        handler = new Handler();
        handlerLoad = new Handler();
        return view;
    }

    private void recuperarInteressesUserAtual(InteressesRecuperadosCallback callback) {
        FirebaseRecuperarUsuario.recuperarInteresses(idUsuario, new FirebaseRecuperarUsuario.RecuperarInteressesCallback() {
            @Override
            public void onRecuperado(HashMap<String, Double> listaInteresses) {
                interessesUserAtual = listaInteresses;
                semInteresses = false;
                callback.onRecuperado();
            }

            @Override
            public void onSemInteresses() {
                semInteresses = true;
                callback.onRecuperado();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void configRecyclerView() {


        if (linearLayoutManagerFeed == null) {

            exoPlayer = new ExoPlayer.Builder(requireContext()).build();

            linearLayoutManagerFeed = new LinearLayoutManager(requireContext());
            linearLayoutManagerFeed.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewInicial.setHasFixedSize(true);
            recyclerViewInicial.setLayoutManager(linearLayoutManagerFeed);

            if (recyclerViewInicial.getOnFlingListener() == null) {
                PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                pagerSnapHelper.attachToRecyclerView(recyclerViewInicial);
            }

            if (adapterLogicaFeed == null) {
                recuperaDadoEpilepsia(new FrameSuporteInicioFragment.RecuperaDadoEpilpesia() {
                    @Override
                    public void onRecuperado(boolean epilepsia) {

                        FirebaseRecuperarUsuario.recuperarInteresses(idUsuario, new FirebaseRecuperarUsuario.RecuperarInteressesCallback() {
                            @Override
                            public void onRecuperado(HashMap<String, Double> listaInteresses) {
                                adapterLogicaFeed = new AdapterLogicaFeed(requireContext(),
                                        listaPostagens, listaInteresses, listaDadosUser, FrameSuporteInicioFragment.this::onPostagemRemocao, FrameSuporteInicioFragment.this::onPosicao, exoPlayer, FrameSuporteInicioFragment.this);
                                adapterLogicaFeed.setStatusEpilepsia(epilepsia);
                                if (adapterHeader == null) {
                                    adapterHeader = new AdapterHeaderInicio(requireContext());
                                }
                                ConcatAdapter concatAdapter = new ConcatAdapter(adapterHeader, adapterLogicaFeed);
                                recyclerViewInicial.setAdapter(concatAdapter);
                            }

                            @Override
                            public void onSemInteresses() {

                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        }
    }

    private void recuperaDadoEpilepsia(RecuperaDadoEpilpesia callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                callback.onRecuperado(epilepsia);
            }

            @Override
            public void onSemDados() {
                callback.onRecuperado(true);
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void adicionarPostagem(Postagem postagem, List<Postagem> newPostagem) {

        UsuarioUtils.postagemJaVisualizada(idUsuario, postagem.getIdPostagem(), new UsuarioUtils.PostagemVisualizadaCallback() {
            @Override
            public void onVisualizacao(boolean result) {
                //Verifica se a postagem já foi visualizada antes de exibir ela ao usuário,
                //com essa verificação o usuário sempre irá ver postagens que ele não viu.
                if (!result) {
                    //Postagem não visualizada
                    FirebaseRecuperarUsuario.recuperarInteresses(idUsuario, new FirebaseRecuperarUsuario.RecuperarInteressesCallback() {
                        @Override
                        public void onRecuperado(HashMap<String, Double> listaInteresses) {
                            //ToastCustomizado.toastCustomizadoCurto("Nova postagem", LogicaFeedTesteActivity.this);
                            itensExibidos++;

                            if (newPostagem != null && newPostagem.size() > 0) {
                                adicionarMaisDados(newPostagem);
                            } else {
                                postagemDiffDAO.adicionarPostagem(postagem);
                                idsPostagens.add(postagem.getIdPostagem());
                                //ORDENAÇÃO ESTAVA AQUI
                                adapterLogicaFeed.updatePostagemList(listaPostagens, new AdapterLogicaFeed.ListaAtualizadaCallback() {
                                    @Override
                                    public void onAtualizado() {
                                        setLoading(false);
                                        if (!paginacaoChamada) {
                                            paginacaoChamada = true;
                                            configPaginacao();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onSemInteresses() {

                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }
            }
        });

        verificaSeNaoExistePostagensPorInteresse(500);
    }

    private void recuperarPostagensIniciais() {

        ordenarHashMapInteresses();

        for (String nomeDoInteresse : interessesRelevantes) {
            queryInicial = firebaseRef.child("interessesPostagens")
                    .orderByChild(nomeDoInteresse.toLowerCase(Locale.ROOT))
                    .equalTo(true).limitToFirst(qntNovaBusca + 2);

            queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Postagem postagem = snapshot1.getValue(Postagem.class);
                        recuperarPostagemPorId(postagem.getIdPostagem(), postagem.getIdDonoPostagem(), false);
                    }
                    queryInicial.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        verificaSeNaoExistePostagensPorInteresse(1000);
    }

    private void recuperarPostagemPorId(String idPost, String idDonoPost, boolean paginacao) {

        if (idDonoPost.equals(idUsuario)) {
            setLoading(false);
            verificaSeNaoExistePostagensPorInteresse(800);
            return;
        }

        recuperarCard(idDonoPost);

        DatabaseReference recuperarPostRef = firebaseRef.child("postagens")
                .child(idDonoPost)
                .child(idPost);
        recuperarPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Postagem postagem = snapshot.getValue(Postagem.class);
                    if (paginacao) {
                        List<Postagem> newPostagem = new ArrayList<>();
                        Log.d("KEYPOP", postagem.getIdPostagem());
                        int key = postagem.getTotalCurtidasPostagem();
                        if (lastNrLike != -1 && key != -1 && key != lastNrLike) {
                            newPostagem.add(snapshot.getValue(Postagem.class));
                            lastNrLike = key;
                        }
                        // Remove a última chave usada
                        if (newPostagem.size() > PAGE_SIZE) {
                            newPostagem.remove(0);
                        }

                        if (lastNrLike != -1) {
                            adicionarPostagem(postagem, newPostagem);
                        }
                    } else {
                        adicionarPostagem(postagem, null);
                    }
                }
                recuperarPostRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configPaginacao() {
        if (recyclerViewInicial != null) {
            isScrolling = true;

            recyclerViewInicial.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isScrolling = true;
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (linearLayoutManagerFeed != null) {

                        recyclerViewInicial.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                int firstVisibleItemPosition = linearLayoutManagerFeed.findFirstVisibleItemPosition();
                                int lastExoVisibleItemPosition = linearLayoutManagerFeed.findLastVisibleItemPosition();

                                for (int i = firstVisibleItemPosition; i <= lastExoVisibleItemPosition; i++) {
                                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                                    if (viewHolder instanceof AdapterLogicaFeed.VideoViewHolder) {
                                        View itemView = viewHolder.itemView;

                                        boolean isVisible = isItem75PercentVisibleVertical(recyclerView, itemView);

                                        if (isVisible) {
                                            currentVideoVisible = i;
                                            break;
                                        } else {
                                            currentVideoVisible = -1;
                                        }
                                    } else {
                                        currentVideoVisible = -1;
                                    }
                                }

                                if (currentVideoVisible != ultimoVideoVisivel) {

                                    if (ultimoVideoVisivel != -1) {
                                        RecyclerView.ViewHolder lastVisibleViewHolder = recyclerView.findViewHolderForAdapterPosition(ultimoVideoVisivel);
                                        if (lastVisibleViewHolder instanceof AdapterLogicaFeed.VideoViewHolder) {
                                            ((AdapterLogicaFeed.VideoViewHolder) lastVisibleViewHolder).pararExoPlayer(null);
                                        }
                                    }

                                    if (currentVideoVisible != -1) {
                                        RecyclerView.ViewHolder currentVisibleViewHolder = recyclerView.findViewHolderForAdapterPosition(currentVideoVisible);
                                        if (currentVisibleViewHolder instanceof AdapterLogicaFeed.VideoViewHolder) {
                                            ((AdapterLogicaFeed.VideoViewHolder) currentVisibleViewHolder).iniciarExoVisivel();
                                        }
                                    }

                                    ultimoVideoVisivel = currentVideoVisible;
                                }
                            }
                        }, 100);

                        previousListSize = listaPostagens.size();

                        int firstVisibleItemPosition = linearLayoutManagerFeed.findFirstVisibleItemPosition();
                        int lastVisibleItemPosition = linearLayoutManagerFeed.findLastVisibleItemPosition();
                        for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                            if (viewHolder != null) {
                                View itemView = viewHolder.itemView;
                                boolean isVisible = isItem75PercentVisibleVertical(recyclerView, itemView);
                                if (isVisible) {
                                    atualizarPesos(viewHolder, i);
                                    break;
                                } else {
                                    handler.removeCallbacksAndMessages(null); // Cancela o atraso se o item não estiver mais visível
                                }
                            }
                        }

                        int ultimoItemVisivel = linearLayoutManagerFeed.findLastVisibleItemPosition();
                        int totalItemCount = linearLayoutManagerFeed.getItemCount();

                        verificaIntervaloPostagens(ultimoItemVisivel);

                        if (isLoading()) {
                            //ToastCustomizado.toastCustomizadoCurto("Verifica termino", LogicaFeedTesteActivity.this);
                            //Caso as postagens tenham acabado ele começara a trazer as postagens populares.
                            verificaSeAcabouAsPostagens(lastVisibleItemPosition, totalItemCount);
                            return;
                        }

                        if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                            isScrolling = false;

                            setLoading(true);

                            // o usuário rolou até o final da lista, exibe mais dez itens
                            carregarMaisDadosPelosInteresses();
                        }
                    }
                }
            });
        }
    }

    private void carregarMaisDadosPelosInteresses() {
        //Basicamente é a paginação por interesses.
        ToastCustomizado.toastCustomizadoCurto("Mais dados", requireContext());

        qntNovaBusca += PAGE_SIZE;

        for (String nomeInteresse : interessesRelevantes) {
            queryLoadMore = firebaseRef.child("interessesPostagens")
                    .orderByChild(nomeInteresse.toLowerCase(Locale.ROOT))
                    .equalTo(true)
                    .limitToFirst(qntNovaBusca + PAGE_SIZE);
            childEventListenerLoadMore = queryLoadMore.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Postagem postagem = snapshot.getValue(Postagem.class);
                    recuperarPostagemPorId(postagem.getIdPostagem(), postagem.getIdDonoPostagem(), false);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void recuperarPostsPopulares(int nrBusca) {

        buscaPop = true;

        if (lastNrLike == 0) {
            //Recupera a postagem com mais curtida
            queryPostPopular = firebaseRef.child("totalCurtidas")
                    .orderByChild("totalCurtidasPostagem")
                    .limitToLast(nrBusca);
        } else {
            //Recupera as postagens mais curtidas antes da postagem com mais curtida.
            queryPostPopular = firebaseRef.child("totalCurtidas")
                    .orderByChild("totalCurtidasPostagem")
                    .endBefore(lastNrLike)
                    .limitToLast(nrBusca);
        }

        queryPostPopular.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotPop : snapshot.getChildren()) {
                    //ToastCustomizado.toastCustomizadoCurto("Blabla", LogicaFeedTesteActivity.this);
                    Postagem postagemPop = snapshotPop.getValue(Postagem.class);
                    recuperarPostagemPorId(postagemPop.getIdPostagem(), postagemPop.getIdDonoPostagem(), true);
                }
                queryPostPopular.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void verificaIntervaloPostagens(int ultimoItemVisivel) {
        //ToastCustomizado.toastCustomizadoCurto("INTERVALO", LogicaFeedTesteActivity.this);
        // Verifica se o último item visível é um múltiplo de INTERVALO_EXIBICAO_TOAST
        if ((ultimoItemVisivel + 1) % INTERVALO_POSTAGEM_POPULAR == 0) {
            // Garante que seja feito a recuperação das postagens populares uma vez
            //a cada intervalo.
            if (itensExibidos != ultimoItemVisivel + 1) {
                // Exibe o Toast
                ToastCustomizado.toastCustomizadoCurto("5", requireContext());
                recuperarPostsPopulares(1);
                setLoading(true);
                itensExibidos = ultimoItemVisivel + 1;
            }
        }
    }

    private void verificaSeAcabouAsPostagens(int lastVisibleItemPosition, int totalItemCounts) {
        //Verifica se todas postagens de interesse do usuário acabaram
        //(Se passar de 2 segundos e o tamanho da lista não mudou, logo dá para
        //supor que não há mais postagens relacionado aos interesses do usuário.
        if (!loadPostPop && lastVisibleItemPosition == totalItemCounts - 1) {
            loadPostPop = true;
            setLoading(true);

            handlerLoad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (totalItemCounts == previousListSize) {
                        // Tamanho da lista não mudou, não há mais itens
                        recuperarPostsPopulares(1);
                        setLoading(true);
                        itensExibidos = lastVisibleItemPosition + 1;
                    } else {
                        previousListSize = totalItemCounts;
                    }
                    loadPostPop = false;
                }
            }, 2000); // Espera 2 segundos antes de verificar o tamanho da lista
        }
    }

    private void verificaSeNaoExistePostagensPorInteresse(long time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (listaPostagens != null && listaPostagens.size() <= 0) {
                    nrBuscaPop++;
                    recuperarPostsPopulares(nrBuscaPop);
                }
            }
        }, time);
    }

    private boolean isItem75PercentVisibleVertical(RecyclerView recyclerView, View itemView) {
        //Vertical
        Rect scrollBounds = new Rect();
        recyclerView.getDrawingRect(scrollBounds);

        int top = itemView.getTop();
        int bottom = itemView.getBottom();

        // Calcula a porcentagem visível do item verticalmente.
        float visiblePercentage = 100f * (Math.min(scrollBounds.bottom, bottom) - Math.max(scrollBounds.top, top)) / itemView.getHeight();

        return visiblePercentage >= 75;
    }

    private void ordenarHashMapInteresses() {
        for (Map.Entry<String, Double> entry : interessesUserAtual.entrySet()) {
            String interesse = entry.getKey();
            Double pesoImportancia = entry.getValue();

            if (pesoImportancia >= 0.2) {
                interessesRelevantes.add(interesse);
            }
        }
    }

    private void atualizarPesos(RecyclerView.ViewHolder viewHolder, int position) {
        adapterLogicaFeed.salvarIdPostVisualizada(position);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapterLogicaFeed.atualizarPesos(position, "view");
            }
        }, 2000); // Atraso de 2 segundos para atualizar os pesos
    }

    private void recuperarCard(String idDonoPost) {
        if (idsDonoPostagens != null
                && idsDonoPostagens.size() > 0
                && idsDonoPostagens.contains(idDonoPost)) {
            return;
        }
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idDonoPost, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                listaDadosUser.put(usuarioAtual.getIdUsuario(), usuarioAtual);
                idsDonoPostagens.add(idDonoPost);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void adicionarMaisDados(List<Postagem> newPostagem) {
        if (newPostagem != null && newPostagem.size() >= 1) {
            postagemDiffDAO.carregarMaisPostagem(newPostagem, idsPostagens);
            adapterLogicaFeed.updatePostagemList(listaPostagens, null);
            ToastCustomizado.toastCustomizadoCurto("Mais dados PAG", requireContext());
            setLoading(false);
        }
    }

    private void inicializandoComponentes(View view) {
        recyclerViewInicial = view.findViewById(R.id.recyclerViewInicial);
    }
}