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
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataHoraAtualizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NavigationDrawerActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private BottomNavigationView bottomView;
    private InicioFragment inicioFragment = new InicioFragment();
    private FrameLayout frame;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String apelido;
    private GoogleSignInClient mSignInClient;
    private FirebaseAuth mAuth;
    private Usuario usuario;

    private LocalDate dataAtual;
    //Usar o else desse método para deslogar conta excluida, implementar
    //para atender as condições corretas

    @Override
    protected void onStart() {
        super.onStart();

        //verificarDataHora();

        //DataHoraAtualizado.novaData();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // Verifica se usuario está logado ou não(Aqui eu coloco se os dados estão completos no usuario)
            //startActivity(new Intent(this, NavigationDrawerActivity.class));
            //Toast.makeText(getApplicationContext(), " Diferente de nulo", Toast.LENGTH_SHORT).show();
            // finish();
        } else{

            //Toast.makeText(getApplicationContext(), " Espere as funções serem carregadas, por favor", Toast.LENGTH_SHORT).show();

            onResume();
        }
    }


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

            Toast.makeText(getApplicationContext(), " Logado " + signInAccount.getDisplayName(), Toast.LENGTH_SHORT).show();

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
