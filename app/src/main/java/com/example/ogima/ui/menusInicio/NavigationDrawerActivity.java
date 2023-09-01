package com.example.ogima.ui.menusInicio;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.ogima.R;
import com.example.ogima.activity.ChatInicioActivity;
import com.example.ogima.activity.ProfileViewsActivity;
import com.example.ogima.fragment.AmigosFragment;
import com.example.ogima.fragment.AssinaturaFragment;
import com.example.ogima.fragment.AtividadesFragment;
import com.example.ogima.fragment.FrameSuporteInicioFragment;
import com.example.ogima.fragment.MusicaFragment;
import com.example.ogima.fragment.ParceirosFragment;
import com.example.ogima.fragment.ProfileFragment;
import com.example.ogima.fragment.StickersFragment;
import com.example.ogima.helper.AutoStartHelper;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.CoinsUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FcmUtils;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.intro.IntrodParceirosActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import java.time.LocalDate;
import java.util.ArrayList;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class NavigationDrawerActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private BottomNavigationView bottomView;
    private FrameSuporteInicioFragment frameSuporteInicioFragment = new FrameSuporteInicioFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
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
    private String irParaProfile;
    private String intentPerfilFragment;

    private LocalDate dataAtual;
    private DatabaseReference limiteAdsRef;
    private DatabaseReference connectedRef;
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
    protected void onStop() {
        super.onStop();
        if (valueEventListenerNewMensagens != null) {
            verificaNewMensagensRef.removeEventListener(valueEventListenerNewMensagens);
            valueEventListenerNewMensagens = null;
            verificaNewMensagensRef = null;
        }
    }

    private DatabaseReference verificaNewMensagensRef;
    private ValueEventListener valueEventListenerNewMensagens;

    private interface RecuperarTimeStamp {
        void onRecuperado(long timeStampNegativo);

        void onError(String message);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (idUsuario != null) {
            //*verificaResetAds();
            listenerNewMensagens();
        }

        //Bundle dadosRecebidos e lógica envolvendo esse bundle foi adicionado
        //no dia 28/06/2022
        Bundle dadosRecebidos = getIntent().getExtras();

        if (dadosRecebidos != null) {
            irParaPerfil = dadosRecebidos.getString("irParaPerfil");
            intentPerfilFragment = dadosRecebidos.getString("intentPerfilFragment");

            if (irParaPerfil != null) {

            } else if (intentPerfilFragment != null) {

            }

            if (dadosRecebidos.containsKey("irParaProfile")) {
                bottomView.setSelectedItemId(R.id.nav_profile);
            }
        }
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

        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame, frameSuporteInicioFragment)
                .addToBackStack(null).commit();

        try {
            Bundle dadosAtualizados = getIntent().getExtras();

            String dadoNovo = dadosAtualizados.getString("atualize");

            if (dadoNovo.equals("atualize")) {
                Fragment selectedFragment = null;
                selectedFragment = new FrameSuporteInicioFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, frameSuporteInicioFragment)
                        .addToBackStack(null).commit();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null) {

            //Toast.makeText(getApplicationContext(), " Logado " + signInAccount.getDisplayName(), Toast.LENGTH_SHORT).show();

        }

        atualizarStatusOnline();

        FcmUtils.salvarTokenAtualNoUserAtual(new FcmUtils.SalvarTokenCallback() {
            @Override
            public void onSalvo(String token) {
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Error - " + message, getApplicationContext());
            }
        });
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

        switch (item.getItemId()) {
            case R.id.menu_viewPerfil: {
                //*selected = new ViewPerfilFragment();
                Intent intent = new Intent(getApplicationContext(), ProfileViewsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            }
            case R.id.menu_stickers: {
                selected = new StickersFragment();
                break;
            }

            case R.id.menu_signature: {
                //No fragment coloca informações sobre a assinatura e
                // a partir dele levar para uma activity para fazer a assinatura real
                selected = new AssinaturaFragment();
                break;
            }
            case R.id.menu_notifications: {
                selected = new AtividadesFragment();
                break;
            }
            case R.id.menu_music: {
                selected = new MusicaFragment();
                break;
            }
        }

        if (selected != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, selected)
                    .addToBackStack(null).commit();
        }
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
                case R.id.nav_friends: {
                    selectedFragment = new AmigosFragment();
                    bottomView.getMenu().getItem(1).setEnabled(false);
                    break;
                }
                case R.id.nav_chat: {
                    Intent intent = new Intent(getApplicationContext(), ChatInicioActivity.class);
                    startActivity(intent);
                    finish();
                    //selectedFragment = new ChatFragment();
                    bottomView.getMenu().getItem(2).setEnabled(false);
                    break;
                }
                case R.id.nav_partners: {
                    //**selectedFragment = new ParceirosFragment();
                    verificaCaminhoParc();
                    break;
                }
                case R.id.nav_profile: {
                    ToastCustomizado.toastCustomizadoCurto("PROFILE", getApplicationContext());
                    //**selectedFragment = new PerfilFragment();
                    selectedFragment = new ProfileFragment();
                    bottomView.getMenu().getItem(4).setEnabled(false);
                    break;
                }
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectedFragment)
                        .addToBackStack(null).commit();
            }
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

    private void verificaEstado(MenuItem menuItem) {

        //Otimizar código com algum laço de repetição tipo for sla
        // ai colocar na toolbar também essa lógica de travar o menu ao clicar

        if (menuItem.getItemId() != R.id.nav_home) {
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if (menuItem.getItemId() != R.id.nav_friends) {
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if (menuItem.getItemId() != R.id.nav_chat) {
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if (menuItem.getItemId() != R.id.nav_partners) {
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(4).setEnabled(true);
        }

        if (menuItem.getItemId() != R.id.nav_profile) {
            bottomView.getMenu().getItem(0).setEnabled(true);
            bottomView.getMenu().getItem(1).setEnabled(true);
            bottomView.getMenu().getItem(2).setEnabled(true);
            bottomView.getMenu().getItem(3).setEnabled(true);
        }
    }

    private void verificaResetAds() {
        CoinsUtils.verificaTimeAd(getApplicationContext(), idUsuario, new CoinsUtils.CoinsListener() {
            @Override
            public void onChecked() {
                ToastCustomizado.toastCustomizado("Checado", getApplicationContext());
            }

            @Override
            public void onError(String errorMessage) {
                ToastCustomizado.toastCustomizado("Error: " + errorMessage, getApplicationContext());
            }
        });
    }

    private void recuperarTimestampNegativo(RecuperarTimeStamp recupTimeStampCallback) {

        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(this, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        //ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timeStampNegativo, getApplicationContext());
                        recupTimeStampCallback.onRecuperado(timestampNegativo);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, getApplicationContext());
                        recupTimeStampCallback.onError(errorMessage);
                    }
                });
            }
        });
    }

    private void atualizarStatusOnline() {
        // Configura a presença do usuário no Realtime Database - não é necessário remover
        //esse listener, pois ele será útil em grande parte do app.
        if (connectedRef == null) {
            connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        // O dispositivo está online, então o usuário está ativo.
                        UsuarioUtils.AtualizarStatusOnline(true);
                    } else {
                        //Dipositivo offline. - AppLifeCycle cuida da lógica da parte de desconexão.
                        ToastCustomizado.toastCustomizado("Sem conexão à internet. Mudado para navegação offline.", getApplicationContext());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Tratar erros, se necessário.
                }
            });
        }
    }

    private void listenerNewMensagens() {
        if (verificaNewMensagensRef == null) {
            verificaNewMensagensRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("exibirBadgeNewMensagens");
            valueEventListenerNewMensagens = verificaNewMensagensRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        boolean novaMensagem = snapshot.getValue(Boolean.class);
                        if (novaMensagem) {
                            MenuItem itemRef = bottomView.getMenu().findItem(R.id.nav_chat);
                            View itemIconView = bottomView.findViewById(itemRef.getItemId());
                            Badge badge = new QBadgeView(NavigationDrawerActivity.this).bindTarget(itemIconView);
                            badge.setBadgeBackgroundColor(Color.BLUE);
                            badge.setBadgeTextSize(12, true);
                            badge.setBadgeText("");
                        } else {
                            MenuItem itemRef = bottomView.getMenu().findItem(R.id.nav_chat);
                            itemRef.setIcon(R.drawable.ic_menu_chat);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void verificaCaminhoParc(){
        ParceiroUtils.recuperarDados(idUsuario, new ParceiroUtils.RecuperarUserParcCallback() {
            @Override
            public void onRecuperado(Usuario usuario, String nome, String orientacao, String exibirPerfilPara, String idUserParc, ArrayList<String> listaHobbies, ArrayList<String> listaFotos, ArrayList<String> listaIdsAEsconder) {
                Fragment selectedFragment = new ParceirosFragment();
                bottomView.getMenu().getItem(3).setEnabled(false);
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectedFragment)
                            .addToBackStack(null).commit();
                }
            }

            @Override
            public void onSemDados() {
                Intent intent = new Intent(getApplicationContext(), IntrodParceirosActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {

            }
        });
    }
}