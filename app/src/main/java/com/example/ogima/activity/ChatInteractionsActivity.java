package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.fragment.ChatListFragment;
import com.example.ogima.fragment.ChatListNewFragment;
import com.example.ogima.fragment.ContatoFragment;
import com.example.ogima.fragment.ListagemGrupoFragment;
import com.example.ogima.fragment.TesteSearchFragment;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.IntentUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.google.firebase.database.DatabaseReference;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ChatInteractionsActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private SmartTabLayout smartTab;
    private ViewPager viewPager;
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;
    private String idUsuario = "";
    private static final String PREFS_NOTIFICATION = "Notification";

    @Override
    protected void onStart() {
        super.onStart();
        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }

        configParaNotificacao();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseReference salvarEmConversasRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("nasConversas");
        salvarEmConversasRef.setValue(false);
        salvarEmConversasRef.onDisconnect().setValue(false);
    }

    @Override
    public void onBackPressed() {
        DatabaseReference salvarEmConversasRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("nasConversas");
        salvarEmConversasRef.setValue(false);
        salvarEmConversasRef.onDisconnect().setValue(false);
        IntentUtils.irParaNavigation(ChatInteractionsActivity.this, getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_interactions);
        inicializarComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Ogima");
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        clickListeners();
        configAbas();
        //Verifica se as notificações no dispositivo do usuário estão ativadas ou não.
        if (!checkNotificationPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            // Permissão não concedida, solicita ao usuário que habilite a permissão
            solicitarPermissaoNotificacao();
        }
    }

    private void solicitarPermissaoNotificacao() {
        alertDialogPermissao();
    }

    private void alertDialogPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissão de notificações flutuantes");
        builder.setMessage("Para exibir notificações flutuantes, é necessário permitir a exibição delas. Clique em 'Permitir' para ir às configurações e conceder a permissão.");

        builder.setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                abrirConfigNotificacoes();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void abrirConfigNotificacoes() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        try {
            salvarPermissaoEmShared();
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            abrirConfigGeraisApp();
        }
    }

    private void abrirConfigGeraisApp() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        try {
            salvarPermissaoEmShared();
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean checkNotificationPermission() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NOTIFICATION, MODE_PRIVATE);
        return sharedPreferences.getBoolean("notification_permission", false);
    }

    private void salvarPermissaoEmShared() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NOTIFICATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notification_permission", true);
        editor.apply();
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void configAbas() {
        if (idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }

        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(ChatInteractionsActivity.this)
                .add("CHATS", ChatListNewFragment.class)
                //.add("CHATS", TesteSearchFragment.class)
                .add("CONTATOS", ContatoFragment.class)
                .add("GRUPOS", ListagemGrupoFragment.class)
                .create());

        viewPager.setAdapter(fragmentPagerItemAdapter);
        smartTab.setViewPager(viewPager);
    }

    private void configParaNotificacao() {
        DatabaseReference salvarEmConversasRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("nasConversas");
        salvarEmConversasRef.setValue(true);
        salvarEmConversasRef.onDisconnect().setValue(false);

        //Ocultar badge no menu
        DatabaseReference ocultarBadgeNoMenuRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("exibirBadgeNewMensagens");
        ocultarBadgeNoMenuRef.setValue(false);
        ocultarBadgeNoMenuRef.onDisconnect().setValue(false);
    }

    private void inicializarComponentes() {
        smartTab = findViewById(R.id.smartTabChatInteractions);
        viewPager = findViewById(R.id.viewPagerChatInteractions);
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
    }
}