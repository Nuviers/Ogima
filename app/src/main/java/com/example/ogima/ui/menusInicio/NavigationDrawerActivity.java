package com.example.ogima.ui.menusInicio;

import android.content.Intent;
import android.os.Bundle;

import com.example.ogima.R;
import com.example.ogima.activity.ChatInicioActivity;
import com.example.ogima.activity.EdicaoFotoActivity;
import com.example.ogima.activity.SignatureActivity;
import com.example.ogima.fragment.AmigosFragment;
import com.example.ogima.fragment.AssinaturaFragment;
import com.example.ogima.fragment.AtividadesFragment;
import com.example.ogima.fragment.ChatFragment;
import com.example.ogima.fragment.FrameSuporteInicioFragment;
import com.example.ogima.fragment.InicioFragment;
import com.example.ogima.fragment.MusicaFragment;
import com.example.ogima.fragment.ParceirosFragment;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.fragment.StickersFragment;
import com.example.ogima.fragment.ViewPerfilFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataHoraAtualizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private FrameSuporteInicioFragment frameSuporteInicioFragment = new FrameSuporteInicioFragment();
    private PerfilFragment perfilFragment = new PerfilFragment();
    private FrameLayout frame;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String apelido;
    private GoogleSignInClient mSignInClient;
    private FirebaseAuth mAuth;
    private Usuario usuario;
    private String emailUsuario, idUsuario;
    String teste;
    private String irParaPerfil;
    private String intentPerfilFragment;

    private LocalDate dataAtual;
    //Usar o else desse método para deslogar conta excluida, implementar
    //para atender as condições corretas

    /* //IMPORTANTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
    @Override
    protected void onStart() {
        super.onStart();

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
     */  //IMPORTANTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE



    @Override
    protected void onStart() {
        super.onStart();

        //Bundle dadosRecebidos e lógica envolvendo esse bundle foi adicionado
        //no dia 28/06/2022
        Bundle dadosRecebidos = getIntent().getExtras();

        if(dadosRecebidos != null){
            irParaPerfil = dadosRecebidos.getString("irParaPerfil");
            intentPerfilFragment = dadosRecebidos.getString("intentPerfilFragment");

            if (irParaPerfil != null){
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, perfilFragment).commit();
            }else if (intentPerfilFragment != null){
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, perfilFragment).commit();
            }
        }

            DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
            //Mudado de addValue para addListener - 26/05/2022
            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() != null){
                        Postagem usuarioUpdate = snapshot.getValue(Postagem.class);{
                            try{
                                if(usuarioUpdate.getSinalizarRefresh().equals("atualizar")){
                                    DatabaseReference mudarSinalizadorRef = usuarioRef
                                            .child("sinalizarRefresh");
                                    mudarSinalizadorRef.setValue("normal").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                try{
                                                    //Atualiza o Perfil fragment ao excluir foto e voltar para ele.
                                                    Fragment selectedFragment = null;
                                                    selectedFragment = new PerfilFragment();
                                                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectedFragment).commit();

                                                }catch (Exception ex){
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                                }
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                    usuarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        ///Minhas configurações ao bottomView
        frame = findViewById(R.id.frame);

        bottomView = findViewById(R.id.bottom_nav_view);
        bottomView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, frameSuporteInicioFragment).commit();

        try{
            Bundle dadosAtualizados = getIntent().getExtras();

            String dadoNovo = dadosAtualizados.getString("atualize");

            if(dadoNovo.equals("atualize")){
                Fragment selectedFragment = null;
                selectedFragment = new FrameSuporteInicioFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, frameSuporteInicioFragment).commit();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null){

            //Toast.makeText(getApplicationContext(), " Logado " + signInAccount.getDisplayName(), Toast.LENGTH_SHORT).show();

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

            case R.id.menu_signature:{
                //No fragment coloca informações sobre a assinatura e
                // a partir dele levar para uma activity para fazer a assinatura real
                selected = new AssinaturaFragment();
                break;
            }
            case R.id.menu_notifications:{
                selected = new AtividadesFragment();
                break;
            }
            case R.id.menu_music:{
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

            verificaEstado(menuItem);

            switch (menuItem.getItemId()) {

                case R.id.nav_home: {
                    selectedFragment = new FrameSuporteInicioFragment();
                    bottomView.getMenu().getItem(0).setEnabled(false);
                    //Muda a cor do fundo, porém tem que fazer que a cor não fique
                    //Para as outras telas, fazer com que cada momento volte pro normal.
                    //frame.setBackgroundColor(getResources().getColor(R.color.corInicio));
                    break;
                }
                case R.id.nav_friends:{
                    selectedFragment = new AmigosFragment();
                    bottomView.getMenu().getItem(1).setEnabled(false);
                    break;
            }
                case R.id.nav_chat:{
                    Intent intent = new Intent(getApplicationContext(), ChatInicioActivity.class);
                    startActivity(intent);
                    finish();
                    //selectedFragment = new ChatFragment();
                    bottomView.getMenu().getItem(2).setEnabled(false);
                    break;
                }
                case R.id.nav_partners:{
                    selectedFragment = new ParceirosFragment();
                    bottomView.getMenu().getItem(3).setEnabled(false);
                    break;
                }
                case R.id.nav_profile:{
                    selectedFragment = new PerfilFragment();
                    bottomView.getMenu().getItem(4).setEnabled(false);
                    break;
                }
        }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectedFragment).commit();
            }
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

    private void verificaEstado(MenuItem menuItem){

        //Otimizar código com algum laço de repetição tipo for sla
        // ai colocar na toolbar também essa lógica de travar o menu ao clicar

        if(menuItem.getItemId() != R.id.nav_home){
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if(menuItem.getItemId() != R.id.nav_friends){
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if(menuItem.getItemId() != R.id.nav_chat){
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if(menuItem.getItemId() != R.id.nav_partners){
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if(menuItem.getItemId() != R.id.nav_profile){
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
        }
    }

}
