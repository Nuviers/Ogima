package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.example.ogima.R;
import com.example.ogima.fragment.AmigosFragment;
import com.example.ogima.fragment.ContatoFragment;
import com.example.ogima.fragment.DailyShortsFragment;
import com.example.ogima.fragment.FaqFragment;
import com.example.ogima.fragment.InteracoesParcFragment;
import com.example.ogima.fragment.ParceirosFragment;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.UsuarioUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class ParceirosActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String idUsuario;
    private FrameLayout frameLayout;
    private BottomNavigationBar bottomNavigationBar;

    public ParceirosActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parceiros);
        inicializandoComponentes();
        // Adicione os itens da barra de navegação

        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_parceiros_heart, ""))
                .addItem(new BottomNavigationItem(R.drawable.ic_favoritar_contato, ""))
                .addItem(new BottomNavigationItem(R.drawable.icon_chat_black, ""))
                .addItem(new BottomNavigationItem(R.drawable.ic_perfil_black, ""))
                .setFirstSelectedPosition(0) // Defina a posição do item selecionado por padrão
                .initialise(); // Inicialize a barra de navegação

        // Defina um ouvinte para lidar com as seleções de item
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                // Lide com a seleção de item e atualize o fragmento
                switch (position) {
                    case 0:
                        replaceFragment(new InteracoesParcFragment());
                        break;
                    case 1:
                        replaceFragment(new AmigosFragment());
                        break;
                    case 2:
                        replaceFragment(new FaqFragment());
                        break;
                    case 3:
                        replaceFragment(new ParceirosFragment());
                        break;
                }
            }

            @Override
            public void onTabUnselected(int position) {
                // Lidar com a desseleção de item, se necessário
            }

            @Override
            public void onTabReselected(int position) {
                // Lidar com o item selecionado novamente, se necessário
            }
        });

        // Mostre o fragmento inicial
        replaceFragment(new InteracoesParcFragment());
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutParc, fragment)
                .commit();
    }

    private void inicializandoComponentes() {
        frameLayout = findViewById(R.id.frameLayoutParc);
        bottomNavigationBar = findViewById(R.id.bottom_navigation_parc);
    }
}