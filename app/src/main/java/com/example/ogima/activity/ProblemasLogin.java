package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.fragment.RecupEmailFragment;
import com.example.ogima.fragment.RecupSmsFragment;
import com.example.ogima.ui.intro.IntrodActivity;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ProblemasLogin extends AppCompatActivity {

    //TabLayout
    private TabLayout tabLayout;
    private TabItem tabItemEmail, tabItemSMS;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private Button buttonFaq;
    private String alterarPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajuda_login);
        Toolbar toolbar = findViewById(R.id.toolbarlogin);
        setSupportActionBar(toolbar);

        //Inicializando componentes
        inicializandoComponentes();

        //Configurando toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Abas
        configurandoAba();

        buttonFaq = findViewById(R.id.buttonFaq);

        buttonFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FaqSuporteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        Bundle dados = getIntent().getExtras();
        if(dados != null){
            alterarPass = dados.getString("changePass");
            //Titulo da toolbar
            setTitle(alterarPass);
        }else{
            //Titulo da toolbar
            setTitle("Problemas no login");
        }
    }

    @Override
    public void onBackPressed() {
        if(alterarPass != null){
            Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
            startActivity(intent);
            finish();
        }else{
            finish();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void inicializandoComponentes(){
        smartTabLayout = findViewById(R.id.viewPagerTab);
        viewPager = findViewById(R.id.viewPager);
    }

    public void configurandoAba(){

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Email", RecupEmailFragment.class)
                .add("SMS", RecupSmsFragment.class)
                .create());

        viewPager.setAdapter(adapter);
        smartTabLayout.setViewPager(viewPager);
    }


}