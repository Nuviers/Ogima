package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.fragment.ChatFragment;
import com.example.ogima.fragment.ListagemGrupoFragment;
import com.example.ogima.fragment.ContatoFragment;
import com.example.ogima.helper.OnChipGroupClearListener;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ChatInicioActivity extends AppCompatActivity {

    private ImageButton imgBtnBackChatContato;
    private Toolbar toolbarChatContatoInicio;
    private TextView txtTituloToolbar;
    private SmartTabLayout smartChatContatoInicio;
    private ViewPager viewpagerChatContatoInicio;
    private Bundle dados;
    private String atualizarContato;
    private Fragment currentFragment;

    private ViewPager.OnPageChangeListener listener;
    private ChatFragment chatFragment = new ChatFragment();
    private ContatoFragment contatoFragment = new ContatoFragment();
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;

    private Button btnTesteFire;

    @Override
    protected void onStart() {
        super.onStart();
        listenerFragment();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            viewpagerChatContatoInicio.removeOnPageChangeListener(listener);
            listener = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_inicio);

        inicializarComponentes();
        toolbarChatContatoInicio.setTitle("");
        setSupportActionBar(toolbarChatContatoInicio);

        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }

        //Configurando abas
        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Chats", ChatFragment.class)
                .add("Contatos", ContatoFragment.class)
                .add("Chat em grupo", ListagemGrupoFragment.class)
                .create());

        dados = getIntent().getExtras();

        if (dados != null) {
            atualizarContato = dados.getString("atualizarContato");
        }

        viewpagerChatContatoInicio.setAdapter(fragmentPagerItemAdapter);
        smartChatContatoInicio.setViewPager(viewpagerChatContatoInicio);

        //Vai para a aba de contato
        if (atualizarContato != null) {
            atualizarContato = null;
            viewpagerChatContatoInicio.setCurrentItem(1);
        }

        imgBtnBackChatContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Apenas para visualização de testes
        btnTesteFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TesteFirebaseUiActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //
    }

    private void inicializarComponentes() {

        imgBtnBackChatContato = findViewById(R.id.imgBtnBackChatContato);
        toolbarChatContatoInicio = findViewById(R.id.toolbarChatContatoInicio);
        txtTituloToolbar = findViewById(R.id.txtTituloToolbar);
        smartChatContatoInicio = findViewById(R.id.smartChatContatoInicio);
        viewpagerChatContatoInicio = findViewById(R.id.viewpagerChatContatoInicio);


        btnTesteFire = findViewById(R.id.btnTesteFire);
    }

    private void listenerFragment() {
        listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //ToastCustomizado.toastCustomizadoCurto("Chat ", getApplicationContext());
                //Limpa os filtros ao trocar de fragment
                currentFragment = fragmentPagerItemAdapter.getPage(position);
                if (currentFragment instanceof OnChipGroupClearListener) {
                    ((OnChipGroupClearListener) currentFragment).onClearChipGroup();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        viewpagerChatContatoInicio.addOnPageChangeListener(listener);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
    }
}