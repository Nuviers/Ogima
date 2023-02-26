package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import com.example.ogima.R;
import com.example.ogima.fragment.ChatFragment;
import com.example.ogima.fragment.ContatoFragment;
import com.example.ogima.fragment.FriendsFragment;
import com.example.ogima.fragment.FriendshipRequestFragment;
import com.example.ogima.helper.ToastCustomizado;
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

    private Bundle dados;
    private String fragmentDesejado;
    private Boolean retornarAoItem = false;
    private int itemAtual;

    @Override
    protected void onStart() {
        super.onStart();

        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }

        //Configurando abas
        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Solicitações", FriendshipRequestFragment.class)
                .add("Amigos", FriendsFragment.class)
                .create());

        viewpagerFriendsRequests.setAdapter(fragmentPagerItemAdapter);
        smartTabFriendsRequests.setViewPager(viewpagerFriendsRequests);

        dados = getIntent().getExtras();

        if (dados != null) {
            fragmentDesejado = dados.getString("fragmentEscolhido");

            if (retornarAoItem) {
                viewpagerFriendsRequests.setCurrentItem(itemAtual);
                retornarAoItem = false;
            }else{
                if (fragmentDesejado.equals("exibirAmigos")) {
                    viewpagerFriendsRequests.setCurrentItem(1);
                    fragmentDesejado = "";
                }
            }
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

        imgBtnBackFriendsRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
}