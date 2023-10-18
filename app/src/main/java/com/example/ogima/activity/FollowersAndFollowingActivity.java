package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.fragment.FollowersFragment;
import com.example.ogima.fragment.RecupEmailFragment;
import com.example.ogima.fragment.RecupSmsFragment;
import com.example.ogima.helper.IntentUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class FollowersAndFollowingActivity extends AppCompatActivity {

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private String tipoFragment = "";
    private SmartTabLayout smartTab;
    private ViewPager viewPager;
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;
    private boolean voltarParaProfile = false;
    private String idDonoPerfil = "";

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
        if (voltarParaProfile) {
            IntentUtils.irParaProfile(FollowersAndFollowingActivity.this, getApplicationContext());
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_and_following);
        inicializarComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Ogima");
        receberDados();
        clickListeners();
        configAbas();
    }

    private void receberDados(){
        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            if (dados.containsKey("tipoFragment")) {
                tipoFragment = dados.getString("tipoFragment");
            }
            if (dados.containsKey("voltarParaProfile")) {
                voltarParaProfile = dados.getBoolean("voltarParaProfile");
            }
            if (dados.containsKey("idDonoPerfil")) {
                idDonoPerfil = dados.getString("idDonoPerfil");
            }
        }
    }

    private void configAbas(){

        if (idDonoPerfil == null || idDonoPerfil.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }

        if (tipoFragment != null && !tipoFragment.isEmpty()) {

        }else{
            tipoFragment = "Seguindo";
        }

        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(FollowersAndFollowingActivity.this)
                .add("Seguidores", FollowersFragment.class, enviarIdDonoPerfil())
                .add("Seguindo", RecupSmsFragment.class)
                .create());
        viewPager.setAdapter(fragmentPagerItemAdapter);
        smartTab.setViewPager(viewPager);
    }

    private void clickListeners(){
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private Bundle enviarIdDonoPerfil() {
        Bundle bundle = new Bundle();
        bundle.putString("idDonoPerfil", idDonoPerfil);
        return bundle;
    }

    private void inicializarComponentes(){
        smartTab = findViewById(R.id.smartTabFoll);
        viewPager = findViewById(R.id.viewPagerFoll);
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
    }
}