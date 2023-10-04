package com.example.ogima.ui.cadastro;

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
import com.example.ogima.activity.CadastroActivity;
import com.example.ogima.activity.LoginUiActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class ViewCadastroActivity extends AppCompatActivity {

    private Button btnCadEmail, btnCadGoogle;
    private TextView txtViewPossuiConta;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Usuario usuario;
    private String emailGo = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private ProgressBar progressBarCadGoogle;
    private GoogleSignInOptions gso;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cadastro);
        inicializandoComponentes();
        configInicial();
        clickListeners();
        resultadoIntent();
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void logarComGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(ViewCadastroActivity.this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            ProgressBarUtils.ocultarProgressBar(progressBarCadGoogle, ViewCadastroActivity.this);
                            verificarUsuario();
                        } else {
                            ProgressBarUtils.ocultarProgressBar(progressBarCadGoogle, ViewCadastroActivity.this);
                            ToastCustomizado.toastCustomizado(getString(R.string.registration_error), getApplicationContext());
                        }
                    }
                });
    }

    private void irParaTelaDeLogin() {
        ProgressBarUtils.ocultarProgressBar(progressBarCadGoogle, ViewCadastroActivity.this);
        Intent intent = new Intent(getApplicationContext(), LoginUiActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void irParaCadastroPorEmail() {
        ProgressBarUtils.ocultarProgressBar(progressBarCadGoogle, ViewCadastroActivity.this);
        Intent intent = new Intent(getApplicationContext(), CadastroUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void verificarUsuario() {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            String emailUsuario = mAuth.getCurrentUser().getEmail();
            if (emailUsuario != null) {
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

                usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                            if (usuarioAtual != null && usuarioAtual.getEmailUsuario() != null
                                    && !usuarioAtual.getEmailUsuario().isEmpty()) {
                                ToastCustomizado.toastCustomizado(getString(R.string.account_already_registered), getApplicationContext());
                                if (FirebaseAuth.getInstance() != null) {
                                    FirebaseAuth.getInstance().signOut();
                                }
                                mGoogleSignInClient.signOut();
                            }
                        } else {
                            emailGo = mAuth.getCurrentUser().getEmail();
                            usuario = new Usuario();
                            usuario.setEmailUsuario(emailGo);
                            irParaTelasDeCadastro(usuario);
                        }
                        usuarioRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), error.getMessage()), getApplicationContext());
                    }
                });
            }
        }
    }

    private void configInicial() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(ViewCadastroActivity.this, gso);
    }

    private void clickListeners() {

        btnCadEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaCadastroPorEmail();
            }
        });

        btnCadGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth != null && mAuth.getCurrentUser() != null
                        && mAuth.getCurrentUser().isEmailVerified()) {
                    mAuth.signOut();
                    mGoogleSignInClient.signOut();
                }
                ProgressBarUtils.exibirProgressBar(progressBarCadGoogle, ViewCadastroActivity.this);
                signInGoogle();
            }
        });

        txtViewPossuiConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaTelaDeLogin();
            }
        });
    }

    private void resultadoIntent() {
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            logarComGoogle(account);
                        } catch (Throwable e) {
                            ProgressBarUtils.ocultarProgressBar(progressBarCadGoogle, ViewCadastroActivity.this);
                            ToastCustomizado.toastCustomizado(
                                    String.format("%s %s %s", getString(R.string.registration_error), ":", e.getMessage()), ViewCadastroActivity.this);
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        ProgressBarUtils.ocultarProgressBar(progressBarCadGoogle, ViewCadastroActivity.this);
                    }
                });
    }

    private void irParaTelasDeCadastro(Usuario usuarioCad) {
        Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
        intent.putExtra("dadosUsuario", usuarioCad);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void inicializandoComponentes() {
        btnCadEmail = findViewById(R.id.btnCadEmail);
        btnCadGoogle = findViewById(R.id.btnCadGoogle);
        txtViewPossuiConta = findViewById(R.id.txtViewPossuiConta);
        progressBarCadGoogle = findViewById(R.id.progressBarCadGoogle);
    }
}
