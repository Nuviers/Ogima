package com.example.apptnertwo.ui.menusInicio;

import android.graphics.Color;
import android.os.Bundle;

import com.example.apptnertwo.R;
import com.example.apptnertwo.ui.fragment.AmigosFragment;
import com.example.apptnertwo.ui.fragment.AssinaturaFragment;
import com.example.apptnertwo.ui.fragment.AtividadesFragment;
import com.example.apptnertwo.ui.fragment.ChatFragment;
import com.example.apptnertwo.ui.fragment.InicioFragment;
import com.example.apptnertwo.ui.fragment.MusicaFragment;
import com.example.apptnertwo.ui.fragment.ParceirosFragment;
import com.example.apptnertwo.ui.fragment.PerfilFragment;
import com.example.apptnertwo.ui.fragment.StickersFragment;
import com.example.apptnertwo.ui.fragment.ViewPerfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.FrameLayout;

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

        //Teste pull através de requisições.

        frame = findViewById(R.id.frame);

        bottomView = findViewById(R.id.bottom_nav_view);
        bottomView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, inicioFragment).commit();

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



}
