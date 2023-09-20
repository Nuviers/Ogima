package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.example.ogima.ui.intro.IntrodActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginUiActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClients;
    private FirebaseAuth mAuths;
    private Button btnLoginEmail, btnLoginGoogle;
    private TextView txtViewAccountProblem;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private GoogleSignInClient mSignInClient;
    private ProgressBar progressBarLoginGoogle;
    private String idUsuario;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListener;
    private FirebaseUser usuarioAtual;
    private FirebaseUtils firebaseUtils;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseUtils.removerValueListener(usuarioRef, valueEventListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_login);
        inicializandoComponentes();
        firebaseUtils = new FirebaseUtils();
        mAuths = FirebaseAuth.getInstance();
        clickListeners();
        configActivityResult();
        configuracaoLoginGoogle();
    }

    private void clickListeners() {
        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressBarUtils.exibirProgressBar(progressBarLoginGoogle, LoginUiActivity.this);
                logarComGoogle();
            }
        });

        btnLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginComEmail();
            }
        });

        txtViewAccountProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressBarUtils.ocultarProgressBar(progressBarLoginGoogle, LoginUiActivity.this);
                irParaProblemasLogin();
            }
        });
    }

    private void configuracaoLoginGoogle() {
        //Configura de login do Google
        GoogleSignInOptions gsos = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                .requestEmail()
                .build();
        mGoogleSignInClients = GoogleSignIn.getClient(LoginUiActivity.this, gsos);
    }

    private void logarComGoogle() {
        Intent signInIntents = mGoogleSignInClients.getSignInIntent();
        signInLauncher.launch(signInIntents);
    }

    private void configActivityResult() {
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            verificaAutenticacaoGoogle(account.getIdToken());
                        } catch (Throwable e) {
                           ProgressBarUtils.ocultarProgressBar(progressBarLoginGoogle, LoginUiActivity.this);
                        }
                    }
                }
        );
    }

    private void verificaAutenticacaoGoogle(String idToken) {
        AuthCredential credentials = GoogleAuthProvider.getCredential(idToken, null);
        mAuths.signInWithCredential(credentials)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            ProgressBarUtils.ocultarProgressBar(progressBarLoginGoogle, LoginUiActivity.this);
                            verificaUsuario();
                        }else{
                            ProgressBarUtils.ocultarProgressBar(progressBarLoginGoogle, LoginUiActivity.this);
                        }
                    }
                });
    }


    public void loginComEmail() {
        Intent intent = new Intent(LoginUiActivity.this, LoginEmailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void verificaUsuario() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        valueEventListener = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    if (usuario != null && usuario.getEmailUsuario() != null
                            && !usuario.getEmailUsuario().isEmpty()) {
                        firebaseUtils.removerValueListener(usuarioRef, valueEventListener);
                        irParaTelaPrincipal();
                    }
                } else {
                    ToastCustomizado.toastCustomizado(getString(R.string.account_not_registered), getApplicationContext());
                    firebaseUtils.removerValueListener(usuarioRef, valueEventListener);
                    tratarUsuarioMalAutenticado();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                firebaseUtils.removerValueListener(usuarioRef, valueEventListener);
                Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    private void tratarUsuarioMalAutenticado() {
        //Deletando usuario da autenticação
        usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null) {
            usuarioAtual.delete();
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        FirebaseAuth.getInstance().signOut();
        mSignInClient.signOut();
    }

    private void irParaProblemasLogin() {
        Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void irParaTelaPrincipal() {
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializandoComponentes() {
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnLoginEmail = findViewById(R.id.btnLoginEmail);
        txtViewAccountProblem = findViewById(R.id.txtViewAccountProblem);
        progressBarLoginGoogle = findViewById(R.id.progressBarLoginGoogle);
    }
}
