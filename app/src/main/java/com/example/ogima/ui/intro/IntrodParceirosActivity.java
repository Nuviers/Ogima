package com.example.ogima.ui.intro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.ogima.R;
import com.example.ogima.activity.LoginUiActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.CadastroEmailTermosActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class IntrodParceirosActivity extends IntroActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.transparent)
                .fragment(R.layout.fragment_intro_parceiros)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.transparent)
                .fragment(R.layout.intro_parceiros_2)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.transparent)
                .fragment(R.layout.intro_parceiros_3)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.transparent)
                .fragment(R.layout.intro_parceiros_4)
                .canGoForward(false)
                .build());

        autoplay(2500, INFINITE);
    }
}



