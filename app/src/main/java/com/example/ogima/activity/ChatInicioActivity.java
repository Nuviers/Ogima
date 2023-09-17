package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.fragment.ChatFragment;
import com.example.ogima.fragment.ListagemGrupoFragment;
import com.example.ogima.fragment.ContatoFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.OnChipGroupClearListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ChatInicioActivity extends AppCompatActivity {

    private ImageButton imgBtnBackChatContato;
    private Toolbar toolbarChatContatoInicio;
    private TextView txtTituloToolbar;
    private SmartTabLayout smartChatContatoInicio;
    private ViewPager viewpagerChatContatoInicio;
    private Bundle dados;
    private String atualizarContato;
    private Fragment currentFragment;

    private ViewPager.OnPageChangeListener listener;
    private ChatFragment chatFragment = new ChatFragment();
    private ContatoFragment contatoFragment = new ContatoFragment();
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;

    private Button btnTesteFire;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String emailUsuario, idUsuario;
    private static final String PREFS_NOTIFICATION = "Notification";

    public ChatInicioActivity() {
        this.emailUsuario = autenticacao.getCurrentUser().getEmail();
        this.idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenerFragment();

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

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            viewpagerChatContatoInicio.removeOnPageChangeListener(listener);
            listener = null;
        }

        DatabaseReference salvarEmConversasRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("nasConversas");
        salvarEmConversasRef.setValue(false);
        salvarEmConversasRef.onDisconnect().setValue(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_inicio);

        inicializarComponentes();
        toolbarChatContatoInicio.setTitle("");
        setSupportActionBar(toolbarChatContatoInicio);

        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }

        //Configurando abas
        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Chats", ChatFragment.class)
                .add("Contatos", ContatoFragment.class)
                .add("Chat em grupo", ListagemGrupoFragment.class)
                .create());

        dados = getIntent().getExtras();

        if (dados != null) {
            atualizarContato = dados.getString("atualizarContato");
        }

        viewpagerChatContatoInicio.setAdapter(fragmentPagerItemAdapter);
        smartChatContatoInicio.setViewPager(viewpagerChatContatoInicio);

        //Vai para a aba de contato
        if (atualizarContato != null) {
            atualizarContato = null;
            viewpagerChatContatoInicio.setCurrentItem(1);
        }

        imgBtnBackChatContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Apenas para visualização de testes
        btnTesteFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TesteFirebaseUiActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //

        //Verifica se as notificações no dispositivo do usuário estão ativadas ou não
        if(checkNotificationPermission()){
            //Aviso já foi exibido anteriormente
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // Permissão não concedida, solicita ao usuário que habilite a permissão
                solicitarPermissaoNotificacao();
            }
        }
    }

    private void inicializarComponentes() {

        imgBtnBackChatContato = findViewById(R.id.imgBtnBackChatContato);
        toolbarChatContatoInicio = findViewById(R.id.toolbarChatContatoInicio);
        txtTituloToolbar = findViewById(R.id.txtTituloToolbar);
        smartChatContatoInicio = findViewById(R.id.smartChatContatoInicio);
        viewpagerChatContatoInicio = findViewById(R.id.viewpagerChatContatoInicio);


        btnTesteFire = findViewById(R.id.btnTesteFire);
    }

    private void listenerFragment() {
        listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //ToastCustomizado.toastCustomizadoCurto("Chat ", getApplicationContext());
                //Limpa os filtros ao trocar de fragment
                currentFragment = fragmentPagerItemAdapter.getPage(position);
                if (currentFragment instanceof OnChipGroupClearListener) {
                    ((OnChipGroupClearListener) currentFragment).onClearChipGroup();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        viewpagerChatContatoInicio.addOnPageChangeListener(listener);
    }

    private void solicitarPermissaoNotificacao() {
        alertDialogPermissaoNotificacao();
    }

    private void alertDialogPermissaoNotificacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissão de Notificações Flutuantes");
        builder.setMessage("Para exibir notificações flutuantes, é necessário permitir a exibição de notificações. Clique em 'Permitir' para ir às configurações e conceder a permissão.");

        // Configuração do botão "Permitir"
        builder.setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                abrirConfigNotificacoes();
            }
        });

        // Configuração do botão "Cancelar"
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Exibe o AlertDialog
        builder.show();
    }

    private void abrirConfigNotificacoes() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        try {
            salvarPermissaoEmShared();
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Caso a Intent não seja suportada, você pode lidar com a situação aqui.
            // Por exemplo, abrir as configurações gerais do aplicativo usando:
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
            // Caso a Intent não seja suportada, você pode lidar com a situação aqui.
        }
    }

    @Override
    public void onBackPressed() {
        DatabaseReference salvarEmConversasRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("nasConversas");
        salvarEmConversasRef.setValue(false);
        salvarEmConversasRef.onDisconnect().setValue(false);
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private boolean checkNotificationPermission() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NOTIFICATION, MODE_PRIVATE);
        return sharedPreferences.getBoolean("notification_permission", false);
    }

    private void salvarPermissaoEmShared(){
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NOTIFICATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notification_permission", true);
        editor.apply();
    }
}