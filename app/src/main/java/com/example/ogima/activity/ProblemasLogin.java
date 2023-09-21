package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.fragment.RecupEmailFragment;
import com.example.ogima.fragment.RecupSmsFragment;
import com.example.ogima.ui.intro.IntrodActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ProblemasLogin extends AppCompatActivity {

    private SmartTabLayout smartTabProblem;
    private ViewPager viewPagerProblem;
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;
    private Button buttonFaq;
    private String alterarPass;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;

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
        if (alterarPass != null) {
            Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
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
        setContentView(R.layout.activity_ajuda_login);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Recuperar conta");
        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            alterarPass = dados.getString("changePass");
        }
        clickListeners();
        configAbas();
    }

    private void configAbas() {
        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Email", RecupEmailFragment.class)
                .add("SMS", RecupSmsFragment.class)
                .create());
        viewPagerProblem.setAdapter(fragmentPagerItemAdapter);
        smartTabProblem.setViewPager(viewPagerProblem);
    }

    private void clickListeners() {
        buttonFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FaqSuporteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        buttonFaq = findViewById(R.id.buttonFaq);
        smartTabProblem = findViewById(R.id.smartTabProblem);
        viewPagerProblem = findViewById(R.id.viewPagerProblem);
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
    }
}