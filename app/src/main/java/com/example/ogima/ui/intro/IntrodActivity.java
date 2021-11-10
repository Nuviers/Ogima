package com.example.ogima.ui.intro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.CadastroEmailTermosActivity;
import com.example.ogima.ui.cadastro.ViewCadastroActivity;
import com.example.ogima.activity.LoginUiActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class IntrodActivity extends IntroActivity {

    private Button buttonDefinidoLogin;
    private Button buttonDefinidoCadastro;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_home);

        buttonDefinidoLogin = findViewById(R.id.buttonDefinidoLogin);
        buttonDefinidoCadastro = findViewById(R.id.buttonDefinidoCadastro);

/*
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // Verifica se usuario está logado ou não
            startActivity(new Intent(this, NavigationDrawerActivity.class));
            finish();
        }


 */

        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide(new FragmentSlide.Builder()
        .background(android.R.color.white)
        .fragment(R.layout.intro_1)
        .build());


        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_2)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_3)
                .canGoForward(false)
                .build());

    }

    public void telaLoginEmail(View view){
        startActivity(new Intent(IntrodActivity.this, LoginUiActivity.class));
    }


    public void telaCadastro(View view){

        Intent intent = new Intent(IntrodActivity.this, CadastroEmailTermosActivity.class);
       startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // Verifica se usuario está logado ou não(Aqui eu coloco se os dados estão completos no usuario)
            startActivity(new Intent(this, NavigationDrawerActivity.class));
            finish();
        }
    }
}

