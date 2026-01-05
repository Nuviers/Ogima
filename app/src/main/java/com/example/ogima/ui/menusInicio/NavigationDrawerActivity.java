package com.example.ogima.ui.menusInicio;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.ogima.R;
import com.example.ogima.activity.ChatInteractionsActivity;
import com.example.ogima.activity.ProfileViewsActivity;
import com.example.ogima.activity.parc.ParceirosActivity;
import com.example.ogima.fragment.AssinaturaFragment;
import com.example.ogima.fragment.AtividadesFragment;
import com.example.ogima.fragment.FrameSuporteInicioFragment;
import com.example.ogima.fragment.MusicaFragment;
import com.example.ogima.fragment.ProfileFragment;
import com.example.ogima.fragment.SocialFragment;
import com.example.ogima.fragment.StickersFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.CoinsUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FcmUtils;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.intro.IntrodParceirosActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class NavigationDrawerActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private BottomNavigationView bottomView;
    private FrameSuporteInicioFragment frameSuporteInicioFragment;
    private ProfileFragment profileFragment;
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

    private DatabaseReference testeUsuarioRef;
    private StorageReference storageRef;

    private DatabaseReference verificaNewMensagensRef;
    private ValueEventListener valueEventListenerNewMensagens;

    @Override
    protected void onStop() {
        super.onStop();
        if (valueEventListenerNewMensagens != null) {
            verificaNewMensagensRef.removeEventListener(valueEventListenerNewMensagens);
            valueEventListenerNewMensagens = null;
            verificaNewMensagensRef = null;
        }
    }

    private interface RecuperarTimeStamp {
        void onRecuperado(long timeStampNegativo);
        void onError(String message);
    }

    public interface PrepararListaAmigoCallback {
        void onProsseguir(ArrayList<String> listaAtualizada);
        void onExcluidoAnteriormente();
        void onError(String message);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (idUsuario != null) {
            verificaResetAds();
            listenerNewMensagens();
        }

        Bundle dadosRecebidos = getIntent().getExtras();

        if (dadosRecebidos != null) {
            irParaPerfil = dadosRecebidos.getString("irParaPerfil");
            intentPerfilFragment = dadosRecebidos.getString("intentPerfilFragment");

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

        if (autenticacao.getCurrentUser() != null) {
            emailUsuario = autenticacao.getCurrentUser().getEmail();
            idUsuario = Base64Custom.codificarBase64(emailUsuario);
            testeUsuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        }

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testedecomstorageemap();
            }
        });

        frame = findViewById(R.id.frame);
        bottomView = findViewById(R.id.bottom_nav_view);
        bottomView.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {

            // Verifica se tem algum pedido de atualização vindo da Intent
            Bundle dadosAtualizados = getIntent().getExtras();
            boolean forcarAtualizacao = false;

            try {
                if (dadosAtualizados != null && dadosAtualizados.getString("atualize") != null) {
                    if (dadosAtualizados.getString("atualize").equals("atualize")) {
                        forcarAtualizacao = true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            bottomView.setSelectedItemId(R.id.nav_home);
        }

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        atualizarStatusOnline();

        FcmUtils.salvarTokenAtualNoUserAtual(new FcmUtils.SalvarTokenCallback() {
            @Override
            public void onSalvo(String token) {}
            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Error - " + message, getApplicationContext());
            }
        });
    }

    private void testedecomstorageemap() {
        Intent intent = new Intent(getApplicationContext(), ChatInteractionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment selected = null;

        switch (item.getItemId()) {
            case R.id.menu_viewPerfil: {
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
                    break;
                }
                case R.id.nav_friends: {
                    selectedFragment = new SocialFragment();
                    bottomView.getMenu().getItem(1).setEnabled(false);
                    break;
                }
                case R.id.nav_chat: {
                    Intent intent = new Intent(getApplicationContext(), ChatInteractionsActivity.class);
                    startActivity(intent);
                    finish();
                    bottomView.getMenu().getItem(2).setEnabled(false);
                    break;
                }
                case R.id.nav_partners: {
                    verificaCaminhoParc();
                    break;
                }
                case R.id.nav_profile: {
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

    }

    private void verificaEstado(MenuItem menuItem) {
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

            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void recuperarTimestampNegativo(RecuperarTimeStamp recupTimeStampCallback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(this, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(() -> {
                    long timestampNegativo = -1 * timestamps;
                    recupTimeStampCallback.onRecuperado(timestampNegativo);
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, getApplicationContext());
                    recupTimeStampCallback.onError(errorMessage);
                });
            }
        });
    }

    private void atualizarStatusOnline() {
        if (connectedRef == null) {
            connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class);
                    if (connected) {
                        UsuarioUtils.AtualizarStatusOnline(true);
                    } else {
                        ToastCustomizado.toastCustomizado("Sem conexão à internet. Mudado para navegação offline.", getApplicationContext());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void listenerNewMensagens() {
        if (verificaNewMensagensRef == null) {
            verificaNewMensagensRef = firebaseRef.child("usuarios").child(idUsuario).child("exibirBadgeNewMensagens");
            valueEventListenerNewMensagens = verificaNewMensagensRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        boolean novaMensagem = snapshot.getValue(Boolean.class);
                        MenuItem itemRef = bottomView.getMenu().findItem(R.id.nav_chat);
                        if (novaMensagem) {
                            View itemIconView = bottomView.findViewById(itemRef.getItemId());
                            Badge badge = new QBadgeView(NavigationDrawerActivity.this).bindTarget(itemIconView);
                            badge.setBadgeBackgroundColor(Color.BLUE);
                            badge.setBadgeTextSize(12, true);
                            badge.setBadgeText("");
                        } else {
                            itemRef.setIcon(R.drawable.ic_menu_chat);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void verificaCaminhoParc() {
        ParceiroUtils.recuperarDados(idUsuario, new ParceiroUtils.RecuperarUserParcCallback() {
            @Override
            public void onRecuperado(Usuario usuario, String nome, String orientacao, String exibirPerfilPara, String idUserParc, ArrayList<String> listaHobbies, ArrayList<String> listaFotos, ArrayList<String> listaIdsAEsconder) {
                Intent intent = new Intent(getApplicationContext(), ParceirosActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            @Override
            public void onSemDados() {
                Intent intent = new Intent(getApplicationContext(), IntrodParceirosActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void removerIdDaListaDeAmigos(String idAlvo, String idARemover, PrepararListaAmigoCallback callback){
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idAlvo, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    if (listaIdAmigos.contains(idARemover)) {
                        listaIdAmigos.remove(idARemover);
                        callback.onProsseguir(listaIdAmigos);
                    } else {
                        callback.onExcluidoAnteriormente();
                    }
                } else {
                    callback.onExcluidoAnteriormente();
                }
            }
            @Override
            public void onSemDados() { callback.onError("Erro ao recuperar dados"); }
            @Override
            public void onError(String mensagem) { callback.onError(mensagem); }
        });
    }

    private void ajustarExclusao(HashMap<String, Object> operacoes, String idDestinatario){
        removerIdDaListaDeAmigos(idDestinatario, idUsuario, new PrepararListaAmigoCallback() {
            @Override
            public void onProsseguir(ArrayList<String> listaAtualizada) {
                operacoes.put("/usuarios/"+idDestinatario+"/listaIdAmigos/", listaAtualizada);
                operacoes.put("/usuarios/"+idUsuario+"/amigosUsuario", ServerValue.increment(-1));
                operacoes.put("/usuarios/"+idDestinatario+"/amigosUsuario", ServerValue.increment(-1));
                operacoes.put("/contatos/"+idUsuario+"/"+idDestinatario, null);
                operacoes.put("/contatos/"+idDestinatario+"/"+idUsuario, null);
                operacoes.put("/friends/"+idUsuario+"/"+idDestinatario, null);
                operacoes.put("/friends/"+idDestinatario+"/"+idUsuario, null);
                salvarDadosDoMap(operacoes);
            }
            @Override
            public void onExcluidoAnteriormente() { salvarDadosDoMap(operacoes); }
            @Override
            public void onError(String message) {}
        });
    }

    private void salvarDadosDoMap(HashMap<String, Object> operacoes){
        firebaseRef.updateChildren(operacoes).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                ToastCustomizado.toastCustomizadoCurto("Amizade desfeita com sucesso", getApplicationContext());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", "Ocorreu um erro ao desfazer amizade, tente novamente:", e.getMessage()), getApplicationContext());
            }
        });
    }
}