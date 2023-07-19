package com.example.ogima.fragment;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.activity.ProfileViewsActivity;
import com.example.ogima.adapter.AdapterProfileViews;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.model.Usuario;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.annotations.Nullable;

public class ViewerBloqueadoFragment extends Fragment implements AdapterProfileViews.RecuperaPosicaoAnterior, AdapterProfileViews.AtualizarView, AdapterProfileViews.AnimacaoIntent {

    private String idUsuario, emailUsuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private TextView txtViewTitleLiberarViews;
    private ImageView imgViewVerAnuncioView;
    private Button btnDesbloquearView;
    private RecyclerView recyclerView;
    private AdapterProfileViews adapterProfileViews;
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

    private RewardedAd rewardedAd;
    private final static String TAG = "ADSTESTE";

    private ImageButton imgBtnCoins;
    private TextView txtViewCoins;
    private DatabaseReference verificaLimiteAdsRef;
    private ValueEventListener listenerNrAds;
    private DatabaseReference recuperaCoinsRef;
    private ValueEventListener listenerCoins;
    private boolean limiteAdsAtingido = false;
    private AtualizarContador atualizarContador;
    private boolean loadingCoins = false;
    private final static String MESSAGE_ADS = "Veja anúncios e seja recompensado com ogimaCoins: ";
    private final static String MESSAGE_LIMITE_ADS = "Limite de recompensa atingido, você poderá receber mais recompensas daqui a 12 horas.";
    private RecyclerView.OnScrollListener scrollListener;
    private ProgressDialog progressDialog;
    private int qntMore = 0;
    private boolean existemDados = false;

    @Override
    public void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            //*verificaLimiteAds();
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaViewers, adapterProfileViews);
            setLoading(true);
            recuperarCoins();
            recuperarDadosIniciais();
            configPaginacao();
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterProfileViews != null && linearLayoutManager != null
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
        if (listenerNrAds != null) {
            verificaLimiteAdsRef.removeEventListener(listenerNrAds);
            listenerNrAds = null;
        }

        if (listenerCoins != null) {
            recuperaCoinsRef.removeEventListener(listenerCoins);
            listenerCoins = null;
        }

        if (childListenerInicio != null) {
            queryInicial.removeEventListener(childListenerInicio);
            childListenerInicio = null;
        }

        if (childListenerLoadMore != null) {
            queryLoadMore.removeEventListener(childListenerLoadMore);
            childListenerLoadMore = null;
        }

        usuarioDiffDAO.limparListaUsuarios();
        idsUsuarios.clear();

        mCurrentPosition = -1;
    }

    private interface AtualizarLimite {
        void onAtualizado();

        void onError(String message);
    }

    private interface RecuperarTimeStamp {
        void onRecuperado(long timeStampNegativo);

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

    public ViewerBloqueadoFragment() {
        atualizarContador = new AtualizarContador();
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewer_bloqueado, container, false);
        inicializandoComponentes(view);

        inicializarAds();
        loadRewardedAd();
        dadosUserAtual();
        clickListeners();

        return view;
    }

    private void clickListeners() {
        btnDesbloquearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRewardedVideo();
            }
        });
    }

    private void dadosUserAtual() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (epilepsia) {
                    GlideCustomizado.loadDrawableImageEpilepsia(requireContext(),
                            R.drawable.adsview, imgViewVerAnuncioView, android.R.color.transparent);
                } else {
                    GlideCustomizado.loadDrawableImage(requireContext(),
                            R.drawable.adsview, imgViewVerAnuncioView, android.R.color.transparent);
                }

                adapterProfileViews.setStatusEpilepsia(epilepsia);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void inicializarAds() {
        MobileAds.initialize(requireContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void loadRewardedAd() {
        if (rewardedAd == null && !loadingCoins && !limiteAdsAtingido) {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(requireContext(), "ca-app-pub-3940256099942544/5224354917",
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.toString());
                            ToastCustomizado.toastCustomizado("Erro: " + loadAdError.toString(), requireContext());
                            rewardedAd = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            rewardedAd = ad;
                            Log.d(TAG, "Ad was loaded.");
                            if (!loadingCoins && !limiteAdsAtingido) {
                                btnDesbloquearView.setVisibility(View.VISIBLE);
                            }
                            ToastCustomizado.toastCustomizadoCurto("Ad was loaded", requireContext());
                        }
                    });
        }
    }

    private void addCoins(int moreCoins) {
        loadingCoins = true;
        recuperarTimestampNegativo(new RecuperarTimeStamp() {
            @Override
            public void onRecuperado(long timeStampNegativo) {
                atualizarLimiteAds(new AtualizarLimite() {
                    @Override
                    public void onAtualizado() {
                        //coins += moreCoins;
                        salvarCoins(moreCoins, timeStampNegativo);
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

    private void showRewardedVideo() {

        if (rewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }
        btnDesbloquearView.setVisibility(View.INVISIBLE);

        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "onAdShowedFullScreenContent");
                        ToastCustomizado.toastCustomizado("onAdShowedFullScreenContent", requireContext());
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        ToastCustomizado.toastCustomizado("onAdFailedToShowFullScreenContent", requireContext());
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
                        ToastCustomizado.toastCustomizado("onAdDismissedFullScreenContent", requireContext());
                        // Preload the next rewarded ad.
                        loadRewardedAd();
                    }
                });
        Activity activityContext = requireActivity();
        rewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("TAG", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();

                        ToastCustomizado.toastCustomizado("rewardAmount: " + rewardAmount, requireContext());
                        ToastCustomizado.toastCustomizado("rewardType: " + rewardType, requireContext());

                        addCoins(rewardAmount);
                    }
                });
    }

    private void verificaLimiteAds() {
        if (verificaLimiteAdsRef == null) {
            verificaLimiteAdsRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("nrAdsVisualizadas");

            listenerNrAds = verificaLimiteAdsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        int adsVisualizadas = snapshot.getValue(Integer.class);
                        if (adsVisualizadas != -1
                                && adsVisualizadas > 0) {
                            txtViewTitleLiberarViews.setText(String.valueOf(MESSAGE_ADS + adsVisualizadas + "/5"));
                            if (adsVisualizadas >= 5) {
                                btnDesbloquearView.setVisibility(View.INVISIBLE);
                                btnDesbloquearView.setClickable(false);
                                limiteAdsAtingido = true;
                                txtViewTitleLiberarViews.setText(String.valueOf(MESSAGE_LIMITE_ADS));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void atualizarLimiteAds(AtualizarLimite callback) {
        DatabaseReference atualizaLimiteAdsRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("nrAdsVisualizadas");
        atualizarContador.acrescentarContador(atualizaLimiteAdsRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                atualizaLimiteAdsRef.setValue(contadorAtualizado).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onAtualizado();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

    private void salvarCoins(int newCoins, long timeStamp) {

        DatabaseReference salvarTimeStampRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("timeStampResetarLimiteAds");

        DatabaseReference salvarCoinsRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("ogimaCoins");

        atualizarContador.adicionarCoins(salvarCoinsRef, newCoins, new AtualizarContador.AtualizarCoinsCallback() {
            @Override
            public void onSuccess(int coinsAtualizado) {
                salvarCoinsRef.setValue(coinsAtualizado).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        loadingCoins = false;
                        ToastCustomizado.toastCustomizadoCurto("Recompensa recebida com sucesso", requireContext());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {

            }
        });

        salvarTimeStampRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Já existe uma visualização de hoje, ignorar o timestamp.
                } else {
                    salvarTimeStampRef.setValue(timeStamp);
                }
                salvarTimeStampRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarCoins() {
        recuperaCoinsRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("ogimaCoins");

        listenerCoins = recuperaCoinsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    int coinsAtuais = snapshot.getValue(Integer.class);
                    if (coinsAtuais != -1 && coinsAtuais >= 1) {
                        txtViewCoins.setText(String.valueOf(coinsAtuais));
                    } else {
                        txtViewCoins.setText("0");
                    }
                } else {
                    txtViewCoins.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarTimestampNegativo(RecuperarTimeStamp recupTimeStampCallback) {

        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(requireContext(), new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        //ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timeStampNegativo, getApplicationContext());
                        recupTimeStampCallback.onRecuperado(validade12Hours(timestampNegativo));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, requireContext());
                        recupTimeStampCallback.onError(errorMessage);
                    }
                });
            }
        });
    }

    private long validade12Hours(long timestampAlvo) {
        //Usado negativo por causa que se trata de um timestamp negativo
        return timestampAlvo - (12 * 60 * 60 * 1000);
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (adapterProfileViews == null) {
            adapterProfileViews = new AdapterProfileViews(requireContext(),
                    listaViewers, this, this, this);
        }

        recyclerView.setAdapter(adapterProfileViews);
    }

    private void recuperarDadosIniciais() {

        if (listaViewers != null && listaViewers.size() >= 1) {
            return;
        }

        queryInicial = firebaseRef.child("profileViews")
                .child(idUsuario).orderByChild("viewLiberada")
                .equalTo(false);

        queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Usuario usuarioInicial = snapshot1.getValue(Usuario.class);
                        adicionarViewer(usuarioInicial);
                        lastTimestamp = usuarioInicial.getTimeStampView();
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

    private void adicionarViewer(Usuario usuario) {
        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());

        if (listaViewers != null && listaViewers.size() >= 1) {
            return;
        }

        usuarioDiffDAO.adicionarUsuario(usuario);
        idsUsuarios.add(usuario.getIdUsuario());
        adapterProfileViews.updateViewersList(listaViewers);
        existemDados = true;
        setLoading(false);
        //ToastCustomizado.toastCustomizado("Size lista: " + listaUsuarios.size(), getApplicationContext());
    }


    private void configPaginacao() {

        if (recyclerView != null) {
            isScrolling = true;
            scrollListener = new RecyclerView.OnScrollListener() {
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

                                    qntMore = 0;

                                    // o usuário rolou até o final da lista, exibe mais cinco itens
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

    private void carregarMaisDados() {
        if (existemDados) {
            if (lastTimestamp == -1) {
                return;
            }

            if (listaViewers != null && qntMore >= 10) {
                PAGE_SIZE += qntMore;
                setLoading(false);
                return;
            }

            setLoading(true);

            ToastCustomizado.toastCustomizado("LOAD MORE", requireContext());

            queryLoadMore = firebaseRef.child("profileViews")
                    .child(idUsuario).orderByChild("viewLiberada")
                    .equalTo(false).limitToFirst(PAGE_SIZE + qntMore);

            queryLoadMore.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.getValue() != null) {
                                Usuario usuarioMore = snapshot1.getValue(Usuario.class);
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
                                    adicionarMaisDados(newUsuario);
                                }
                            }else{
                                qntMore++;
                            }
                        }
                    } else {
                        qntMore++;
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

    private void adicionarMaisDados(List<Usuario> newUsuario) {

        if (listaViewers != null && qntMore >= 10) {
            PAGE_SIZE += qntMore;
            setLoading(false);
            return;
        }

        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDiffDAO.carregarMaisUsuario(newUsuario, idsUsuarios);
            Usuario usuarioComparator = new Usuario(true, false);
            Collections.sort(listaViewers, usuarioComparator);
            adapterProfileViews.updateViewersList(listaViewers);
            ToastCustomizado.toastCustomizadoCurto("Mais dados", requireContext());
            setLoading(false);
        }
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void inicializandoComponentes(View view) {
        imgViewVerAnuncioView = view.findViewById(R.id.imgViewVerAnuncioView);
        btnDesbloquearView = view.findViewById(R.id.btnVerAdsPorView);
        recyclerView = view.findViewById(R.id.recyclerViewVisualizacoes);


        imgBtnCoins = view.findViewById(R.id.imgBtnCoins);
        txtViewCoins = view.findViewById(R.id.txtViewCoins);
        txtViewTitleLiberarViews = view.findViewById(R.id.txtViewTitleLiberarViews);
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

    @Override
    public void onAtualizar(Usuario usuarioAlvo, boolean newStatus) {
        if (usuarioAlvo != null) {
            usuarioDiffDAO.atualizarUsuario(usuarioAlvo, "viewLiberada");
        }
    }
}