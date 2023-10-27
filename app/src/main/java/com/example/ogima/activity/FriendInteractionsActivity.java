package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.fragment.FriendsFragment;
import com.example.ogima.fragment.FriendshipRequestFragment;
import com.example.ogima.fragment.FriendshipRequestFragmentNew;
import com.example.ogima.helper.IntentUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class FriendInteractionsActivity extends AppCompatActivity {

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private String tipoFragment = "";
    private SmartTabLayout smartTab;
    private ViewPager viewPager;
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;
    private boolean voltarParaProfile = false;
    private String idDonoPerfil = "";
    private String idUsuario = "";

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
            IntentUtils.irParaProfile(FriendInteractionsActivity.this, getApplicationContext());
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_requests);
        inicializarComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Ogima");
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
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

        if (idDonoPerfil == null || idDonoPerfil.isEmpty() || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }

        if (!idDonoPerfil.equals(idUsuario)) {
            //Não é o dono do perfil
            fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                    getSupportFragmentManager(), FragmentPagerItems.with(FriendInteractionsActivity.this)
                    .add(getString(R.string.friends), FriendsFragment.class, enviarIdDonoPerfil())
                    .create());
        }else{
            fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                    getSupportFragmentManager(), FragmentPagerItems.with(FriendInteractionsActivity.this)
                    .add(getString(R.string.requests), FriendshipRequestFragmentNew.class, enviarIdDonoPerfil())
                    .add(getString(R.string.friends), FriendsFragment.class, enviarIdDonoPerfil())
                    .create());
        }
        viewPager.setAdapter(fragmentPagerItemAdapter);
        smartTab.setViewPager(viewPager);

        if (tipoFragment != null && !tipoFragment.isEmpty()
                && tipoFragment.equals(getString(R.string.friends))) {
            viewPager.setCurrentItem(1);
        }
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
        smartTab = findViewById(R.id.smartTabFriend);
        viewPager = findViewById(R.id.viewPagerFriend);
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
    }
}