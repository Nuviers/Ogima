package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterProfileViews;
import com.example.ogima.fragment.FriendsFragment;
import com.example.ogima.fragment.FriendshipRequestFragment;
import com.example.ogima.fragment.ViewerBloqueadoFragment;
import com.example.ogima.fragment.ViewerDesbloqueadoFragment;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Postagem;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileViewsActivity extends AppCompatActivity{

    private String idUsuario, emailUsuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String irParaProfile = null;

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;

    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;
    private String fragmentDesejado;

    @Override
    protected void onStart() {
        super.onStart();

        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onBackPressed() {


        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            finish();
        }

        super.onBackPressed();
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

        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Configurando abas
        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Bloqueados", ViewerBloqueadoFragment.class)
                .add("Desbloqueados", ViewerDesbloqueadoFragment.class)
                .create());

        viewPager.setAdapter(fragmentPagerItemAdapter);
        smartTabLayout.setViewPager(viewPager);
    }

    private long validade5MinutesTest(long timestampAlvo) {
        //Usado negativo por causa que se trata de um timestamp negativo
        return timestampAlvo - (5 * 60 * 1000);
    }

    private void inicializarComponentes() {
        //inc_toolbar_padrao
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);

        smartTabLayout = findViewById(R.id.smartTabViewers);
        viewPager = findViewById(R.id.viewpagerViewers);
    }
}