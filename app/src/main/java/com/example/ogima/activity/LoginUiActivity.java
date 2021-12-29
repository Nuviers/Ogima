package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.example.ogima.ui.cadastro.NumeroActivity;
import com.example.ogima.ui.cadastro.VerificaEmailActivity;
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

    private Button google_signIn;
    private GoogleSignInClient mGoogleSignInClients;
    private final static int RC_SIGN_INS = 18;
    private FirebaseAuth mAuths;
    //private Usuario usuario;
    private String emailGo;
    private Button buttonLoginGoogle;
    //13_11
    private Usuario usuario;
    private String apelidoUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
//

    private String testeEmail;
    private GoogleSignInClient mSignInClient;
    private Button buttonProblemaLoginUi;
    private Button buttonLogarNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_login);


        buttonLoginGoogle = findViewById(R.id.buttonLoginGoogle);
        buttonProblemaLoginUi = findViewById(R.id.buttonProblemaLoginUi);

        buttonLogarNumero = findViewById(R.id.buttonLogarNumero);

        mAuths = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gsos = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_ids))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClients = GoogleSignIn.getClient(this, gsos);


        buttonLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIns();
            }
        });


        buttonProblemaLoginUi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    private void signIns() {
        Intent signInIntents = mGoogleSignInClients.getSignInIntent();
        startActivityForResult(signInIntents, RC_SIGN_INS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_INS) {
            Task task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount accounts = (GoogleSignInAccount) task.getResult(ApiException.class);
                //task.getResult(ApiException.class);
                firebaseAuthWithGoogles(accounts);
            } catch (Throwable e) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogles(GoogleSignInAccount accts) {

        AuthCredential credentials = GoogleAuthProvider.getCredential(accts.getIdToken(), null);
        mAuths.signInWithCredential(credentials)
                .addOnCompleteListener(this, new OnCompleteListener() {


                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser users = mAuths.getCurrentUser();
                            testandoCad();
                        }
                    }
                });
    }


    public void loginEmail(View view) {
        Intent intent = new Intent(LoginUiActivity.this, LoginEmailActivity.class);
        startActivity(intent);
    }

    public void testandoCad() {
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    testeEmail = usuario.getEmailUsuario();

                    if (testeEmail != null) {
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        finish();
                        //*Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        //*startActivity(intent);
                        //finish();
                    } else if (snapshot == null) {
                        Toast.makeText(getApplicationContext(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Conta ainda não cadastrada", Toast.LENGTH_SHORT).show();

                    FirebaseUser usuarioAtual = autenticacao.getCurrentUser();

                    //Deletando usuario da autenticação
                    usuarioAtual.delete();

                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_ids))
                            .requestEmail()
                            .build();

                    mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                    FirebaseAuth.getInstance().signOut();
                    mSignInClient.signOut();

                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                startActivity(intent);
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}
