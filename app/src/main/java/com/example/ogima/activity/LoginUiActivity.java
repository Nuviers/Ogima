package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.example.ogima.ui.cadastro.NumeroActivity;
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
    private final static int RC_SIGN_INS = 18;
    private FirebaseAuth mAuths;
    private Button buttonLoginGoogle;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private GoogleSignInClient mSignInClient;
    private Button buttonProblemaLoginUi;
    private Button buttonLogarNumero;
    private ProgressBar progressBarLoginGoogle;

    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListener;
    private FirebaseUser usuarioAtual;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            usuarioRef.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_login);
        inicializandoComponentes();

        mAuths = FirebaseAuth.getInstance();

        configuracaoLoginGoogle();

        buttonLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarLoginGoogle.setVisibility(View.VISIBLE);
                logarComGoogle();
            }
        });


        buttonProblemaLoginUi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBarLoginGoogle.setVisibility(View.GONE);

                Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });


        buttonLogarNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), NumeroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void inicializandoComponentes() {
        buttonLoginGoogle = findViewById(R.id.buttonLoginGoogle);
        buttonProblemaLoginUi = findViewById(R.id.buttonProblemaLoginUi);
        buttonLogarNumero = findViewById(R.id.buttonLogarNumero);
        progressBarLoginGoogle = findViewById(R.id.progressBarLoginGoogle);
    }

    private void configuracaoLoginGoogle() {
        // Configure Google Sign In
        GoogleSignInOptions gsos = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClients = GoogleSignIn.getClient(this, gsos);
    }

    private void logarComGoogle() {
        Intent signInIntents = mGoogleSignInClients.getSignInIntent();
        startActivityForResult(signInIntents, RC_SIGN_INS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_INS) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                verificaAutenticacaoGoogle(account.getIdToken());
            } catch (Throwable e) {
                try {
                    progressBarLoginGoogle.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void verificaAutenticacaoGoogle(String idToken) {

        AuthCredential credentials = GoogleAuthProvider.getCredential(idToken, null);
        mAuths.signInWithCredential(credentials)
                .addOnCompleteListener(this, new OnCompleteListener() {

                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            progressBarLoginGoogle.setVisibility(View.GONE);

                            verificaUsuario();
                        } else {
                            progressBarLoginGoogle.setVisibility(View.GONE);
                        }
                    }
                });
    }


    public void loginEmail(View view) {
        Intent intent = new Intent(LoginUiActivity.this, LoginEmailActivity.class);
        startActivity(intent);
    }

    private void verificaUsuario() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        valueEventListener = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if (usuario.getEmailUsuario() != null) {
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    ToastCustomizado.toastCustomizado("Conta ainda não cadastrada", getApplicationContext());

                    tratarUsuarioMalAutenticado();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                startActivity(intent);
            }
        });
    }


    private void tratarUsuarioMalAutenticado() {
        //Deletando usuario da autenticação
        usuarioAtual = autenticacao.getCurrentUser();
        usuarioAtual.delete();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        FirebaseAuth.getInstance().signOut();
        mSignInClient.signOut();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}
