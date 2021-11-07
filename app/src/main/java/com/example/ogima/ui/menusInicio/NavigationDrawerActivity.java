package com.example.ogima.ui.menusInicio;

import android.os.Bundle;

import com.example.ogima.R;
import com.example.ogima.fragment.AmigosFragment;
import com.example.ogima.fragment.AssinaturaFragment;
import com.example.ogima.fragment.AtividadesFragment;
import com.example.ogima.fragment.ChatFragment;
import com.example.ogima.fragment.InicioFragment;
import com.example.ogima.fragment.MusicaFragment;
import com.example.ogima.fragment.ParceirosFragment;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.fragment.StickersFragment;
import com.example.ogima.fragment.ViewPerfilFragment;
import com.example.ogima.model.Usuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class NavigationDrawerActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private BottomNavigationView bottomView;
    private InicioFragment inicioFragment = new InicioFragment();
    private FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ///Minhas configurações ao bottomView
        frame = findViewById(R.id.frame);

        bottomView = findViewById(R.id.bottom_nav_view);
        bottomView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, inicioFragment).commit();

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null){

            Toast.makeText(getApplicationContext(), " Logado" + signInAccount.getDisplayName(), Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //Configurações dos botões da toolbar;

        Fragment selected = null;


        switch (item.getItemId()){
            case R.id.menu_viewPerfil:{
                selected = new ViewPerfilFragment();
                break;
            }
            case R.id.menu_stickers:{
                selected = new StickersFragment();
                break;
            }
            case R.id.menu_assinatura:{
                selected = new AssinaturaFragment();
                break;
            }
            case R.id.menu_atividades:{
                selected = new AtividadesFragment();
                break;
            }
            case R.id.menu_musica:{
                selected = new MusicaFragment();
                break;
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, selected).commit();



        return super.onOptionsItemSelected(item);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            Fragment selectedFragment = null;

            switch (menuItem.getItemId()) {

                case R.id.nav_inicio: {
                    selectedFragment = new InicioFragment();
                    //Muda a cor do fundo, porém tem que fazer que a cor não fique
                    //Para as outras telas, fazer com que cada momento volte pro normal.
                    //frame.setBackgroundColor(getResources().getColor(R.color.corInicio));
                    break;
                }
                case R.id.nav_amigos:{
                    selectedFragment = new AmigosFragment();
                    break;
            }
                case R.id.nav_chat:{
                    selectedFragment = new ChatFragment();
                    break;
                }
                case R.id.nav_parceiros:{
                    selectedFragment = new ParceirosFragment();
                    break;
                }
                case R.id.nav_perfil:{
                    selectedFragment = new PerfilFragment();
                    break;
                }

        }
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectedFragment).commit();
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

}
