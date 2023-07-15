package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterProfileViews;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileViewsActivity extends AppCompatActivity implements AdapterProfileViews.RecuperaPosicaoAnterior {

    private String idUsuario, emailUsuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String irParaProfile = null;

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private ImageView imgViewVerAnuncioView;
    private Button btnDesbloquearView;
    private RecyclerView recyclerView;
    private AdapterProfileViews adapterProfileViews;
    private List<Usuario> listaViewers = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private long lastTimestamp;
    private final static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private Query queryLoadMore;
    private Query queryInicial;
    private boolean primeiroCarregamento = true;

    private RewardedAd rewardedAd;
    private int coins;
    private final static String TAG = "ADSTESTE";

    private ImageButton imgBtnCoins;
    private TextView txtViewCoins;

    @Override
    protected void onStart() {
        super.onStart();

        if (primeiroCarregamento) {
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaViewers, adapterProfileViews);
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_views);
        setSupportActionBar(toolbarIncPadrao);
        inicializarComponentes();
        setTitle("");
        txtViewIncTituloToolbar.setText("Visualizações no perfil");
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);


        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }
        }

        recuperarDadosIniciais();

        inicializarAds();

        loadRewardedAd();

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (epilepsia) {
                    GlideCustomizado.loadDrawableImageEpilepsia(getApplicationContext(),
                            R.drawable.adsview, imgViewVerAnuncioView, android.R.color.transparent);
                } else {
                    GlideCustomizado.loadDrawableImage(getApplicationContext(),
                            R.drawable.adsview, imgViewVerAnuncioView, android.R.color.transparent);
                }
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });

        btnDesbloquearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRewardedVideo();
            }
        });
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (adapterProfileViews == null) {
            adapterProfileViews = new AdapterProfileViews(getApplicationContext(),
                    listaViewers, this);
        }

        recyclerView.setAdapter(adapterProfileViews);
    }

    private void recuperarDadosIniciais() {
        queryInicial = firebaseRef.child("profileViews")
                .child(idUsuario).orderByChild("timeStampView").limitToFirst(1);

        queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Usuario usuarioInicial = snapshot1.getValue(Usuario.class);
                        adicionarViewer(usuarioInicial);
                    }
                }
                queryInicial.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void adicionarViewer(Usuario usuario) {
        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());

        usuarioDiffDAO.adicionarUsuario(usuario);
        idsUsuarios.add(usuario.getIdUsuario());
        adapterProfileViews.updateViewersList(listaViewers);

        //ToastCustomizado.toastCustomizado("Size lista: " + listaUsuarios.size(), getApplicationContext());
    }

    private void inicializarAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void loadRewardedAd(){
        if (rewardedAd == null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.toString());
                            ToastCustomizado.toastCustomizado("Erro: " + loadAdError.toString(), getApplicationContext());
                            rewardedAd = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            rewardedAd = ad;
                            Log.d(TAG, "Ad was loaded.");
                            btnDesbloquearView.setVisibility(View.VISIBLE);
                            ToastCustomizado.toastCustomizadoCurto("Ad was loaded",getApplicationContext());
                        }
                    });
        }
    }

    private void addCoins(int moreCoins) {
        coins += moreCoins;
        txtViewCoins.setText(String.valueOf(coins));
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
                        ToastCustomizado.toastCustomizado("onAdShowedFullScreenContent", getApplicationContext());
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        ToastCustomizado.toastCustomizado("onAdFailedToShowFullScreenContent", getApplicationContext());
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
                        ToastCustomizado.toastCustomizado("onAdDismissedFullScreenContent", getApplicationContext());
                        // Preload the next rewarded ad.
                        loadRewardedAd();
                    }
                });
        Activity activityContext = ProfileViewsActivity.this;
        rewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("TAG", "The user earned the reward.");
                        ToastCustomizado.toastCustomizado("The user earned the reward.", getApplicationContext());
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();

                        ToastCustomizado.toastCustomizado("rewardAmount: " + rewardAmount, getApplicationContext());
                        ToastCustomizado.toastCustomizado("rewardType: " + rewardType, getApplicationContext());

                        addCoins(rewardAmount);
                    }
                });
    }

    private void inicializarComponentes() {
        //inc_toolbar_padrao
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgViewVerAnuncioView = findViewById(R.id.imgViewVerAnuncioView);
        btnDesbloquearView = findViewById(R.id.btnVerAdsPorView);
        recyclerView = findViewById(R.id.recyclerViewVisualizacoes);


        imgBtnCoins = findViewById(R.id.imgBtnCoins);
        txtViewCoins = findViewById(R.id.txtViewCoins);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {

    }
}