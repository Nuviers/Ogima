package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.activity.LoginUiActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class ViewCadastroActivity extends AppCompatActivity {

    private Button google_signIn;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 17;
    private FirebaseAuth mAuth;
    private Usuario usuario;
    private String emailGo;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
//

    private String testeEmail;
    private ProgressBar progressBarRegistroGoogle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cadastro);

        google_signIn = findViewById(R.id.google_signIn);
        progressBarRegistroGoogle = findViewById(R.id.progressBarRegistroGoogle);

        usuario = new Usuario();

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        google_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    if(autenticacao.getCurrentUser().isEmailVerified()){
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                                .requestEmail()
                                .build();

                        GoogleSignInClient mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                        FirebaseAuth.getInstance().signOut();
                        mSignInClient.signOut();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                try{
                    progressBarRegistroGoogle.setVisibility(View.VISIBLE);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                signIn();

            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = (GoogleSignInAccount) task.getResult(ApiException.class);
                        //task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (Throwable e) {
                try{
                    progressBarRegistroGoogle.setVisibility(View.GONE);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                // Google Sign In failed, update UI appropriately
                // ...
                //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener() {

                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            try{
                                progressBarRegistroGoogle.setVisibility(View.GONE);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }

                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                       //***VAI SER POR AQUI QUE VOCÊ VAI CHAMAR O MÉTODO PRA RESOLVER AS INTENT E VER SE EXISTE A CONTA
                            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                            verificandoUsuario();
                        } else {
                            try{
                                progressBarRegistroGoogle.setVisibility(View.GONE);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                            ToastCustomizado.toastCustomizado("Erro ao efetuar cadastro", getApplicationContext());
                        }

                    }
                });
    }

    public void telaLoginEmail(View view){

        try{
            progressBarRegistroGoogle.setVisibility(View.GONE);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        Intent intent = new Intent(getApplicationContext(), LoginUiActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void cadastrarEmail(View view){

        try{
            progressBarRegistroGoogle.setVisibility(View.GONE);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        Intent intent = new Intent(getApplicationContext(), CadastroUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public  void verificandoUsuario(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    testeEmail = usuario.getEmailUsuario();

                    if(testeEmail != null){
                        //Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        //startActivity(intent);

                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                                .requestEmail()
                                .build();

                        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                        ToastCustomizado.toastCustomizado("Essa conta já foi registrada", getApplicationContext());

                        FirebaseAuth.getInstance().signOut();
                        mGoogleSignInClient.signOut();

                        usuarioRef.removeEventListener(this);

                    }else if(snapshot == null) {
                        ToastCustomizado.toastCustomizado("Conta falta ser cadastrada", getApplicationContext());
                    }
                }else{

                    //Toast.makeText(getApplicationContext(), " Novo usuario", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    emailGo = mAuth.getCurrentUser().getEmail();

                    // emailGo = signInAccount.getEmail();

                    usuario.setEmailUsuario(emailGo);

                    intent.putExtra("dadosUsuario", usuario);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    usuarioRef.removeEventListener(this);
                   // Toast.makeText(getApplicationContext(), "Conta não cadastrada", Toast.LENGTH_SHORT).show();

                    //GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            //.requestIdToken("998572659584-tt3hhp5fb3qtvhctv129536mlgsg3v16.apps.googleusercontent.com")
                           // .requestEmail()
                           // .build();

                    //mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                    //FirebaseAuth.getInstance().signOut();
                    //mGoogleSignInClient.signOut();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });



    }



}
