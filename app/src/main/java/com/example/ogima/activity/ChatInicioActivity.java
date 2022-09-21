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
import com.example.ogima.fragment.ChatFragment;
import com.example.ogima.fragment.ContatoFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_inicio);

        inicializarComponentes();
        toolbarChatContatoInicio.setTitle("");
        setSupportActionBar(toolbarChatContatoInicio);

        //Configurando abas
        FragmentPagerItemAdapter fragmentPagerItemAdapter  = new FragmentPagerItemAdapter(
                 getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Chats", ChatFragment.class)
                .add("Contatos", ContatoFragment.class)
                .create());

        viewpagerChatContatoInicio.setAdapter(fragmentPagerItemAdapter);
        smartChatContatoInicio.setViewPager(viewpagerChatContatoInicio);

        imgBtnBackChatContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void inicializarComponentes() {

        imgBtnBackChatContato = findViewById(R.id.imgBtnBackChatContato);
        toolbarChatContatoInicio = findViewById(R.id.toolbarChatContatoInicio);
        txtTituloToolbar = findViewById(R.id.txtTituloToolbar);
        smartChatContatoInicio = findViewById(R.id.smartChatContatoInicio);
        viewpagerChatContatoInicio = findViewById(R.id.viewpagerChatContatoInicio);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
    }
}