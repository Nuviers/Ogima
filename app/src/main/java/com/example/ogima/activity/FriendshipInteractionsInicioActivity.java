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
import com.example.ogima.fragment.FriendsFragment;
import com.example.ogima.fragment.FriendshipRequestFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class FriendshipInteractionsInicioActivity extends AppCompatActivity {

    private Toolbar toolbarFriendsRequests;
    private ImageButton imgBtnBackFriendsRequests;
    private TextView txtTituloToolbarFriends;
    private SmartTabLayout smartTabFriendsRequests;
    private ViewPager viewpagerFriendsRequests;
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;

    private String fragmentDesejado;
    private Boolean retornarAoItem = false;
    private int itemAtual;
    private String irParaProfile = null;
    private String idDonoPerfil = null;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuarioLogado;

    @Override
    public void onBackPressed() {
        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        }else{
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        retornarAoItem = true;
        itemAtual = viewpagerFriendsRequests.getCurrentItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_requests);
        inicializarComponentes();
        toolbarFriendsRequests.setTitle("");
        setSupportActionBar(toolbarFriendsRequests);

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

       Bundle dados = getIntent().getExtras();

        if (dados != null) {
            fragmentDesejado = dados.getString("fragmentEscolhido");

            if (dados.containsKey("idDonoPerfil")) {
                idDonoPerfil = dados.getString("idDonoPerfil");
                //recuperaIdDonoPerfil.onIdRecuperado(idDonoPerfil);
            }

            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }

            if (idDonoPerfil != null && !idDonoPerfil.equals(idUsuarioLogado)) {
                //Configurando abas
                fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), FragmentPagerItems.with(this)
                        .add("Amigos", FriendsFragment.class, enviarIdDonoPerfil(idDonoPerfil))
                        .create());
            }else{
                //Configurando abas
                fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                        getSupportFragmentManager(), FragmentPagerItems.with(this)
                        .add("Solicitações", FriendshipRequestFragment.class, enviarIdDonoPerfil(idDonoPerfil))
                        .add("Amigos", FriendsFragment.class, enviarIdDonoPerfil(idDonoPerfil))
                        .create());
            }

            viewpagerFriendsRequests.setAdapter(fragmentPagerItemAdapter);
            smartTabFriendsRequests.setViewPager(viewpagerFriendsRequests);

            if (retornarAoItem) {
                viewpagerFriendsRequests.setCurrentItem(itemAtual);
                retornarAoItem = false;
            }else{
                if (fragmentDesejado.equals("exibirAmigos")) {
                    viewpagerFriendsRequests.setCurrentItem(1);
                    fragmentDesejado = "";
                } else if (fragmentDesejado.equals("exibirPedidosAmigos")) {
                    viewpagerFriendsRequests.setCurrentItem(0);
                    fragmentDesejado = "";
                }
            }
        }




        imgBtnBackFriendsRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializarComponentes() {
        toolbarFriendsRequests = findViewById(R.id.toolbarFriendsRequests);
        imgBtnBackFriendsRequests = findViewById(R.id.imgBtnBackFriendsRequests);
        txtTituloToolbarFriends = findViewById(R.id.txtTituloToolbarFriends);
        smartTabFriendsRequests = findViewById(R.id.smartTabFriendsRequests);
        viewpagerFriendsRequests = findViewById(R.id.viewpagerFriendsRequests);
    }

    private Bundle enviarIdDonoPerfil(String idDono) {
        Bundle bundle = new Bundle();
        bundle.putString("idDonoPerfil", idDono);
        return bundle;
    }
}