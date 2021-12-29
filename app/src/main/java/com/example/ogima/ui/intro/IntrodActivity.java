package com.example.ogima.ui.intro;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataHoraAtualizado;
import com.example.ogima.helper.DbHelper;
import com.example.ogima.helper.InfoUserDAO;
import com.example.ogima.model.Informacoes;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.CadastroEmailTermosActivity;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.example.ogima.ui.cadastro.ViewCadastroActivity;
import com.example.ogima.activity.LoginUiActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class IntrodActivity extends IntroActivity {

    private Button buttonDefinidoLogin;
    private Button buttonDefinidoCadastro;
    private FirebaseAuth mAuth;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String testeEmail;
    private GoogleSignInClient mSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_home);

        buttonDefinidoLogin = findViewById(R.id.buttonDefinidoLogin);
        buttonDefinidoCadastro = findViewById(R.id.buttonDefinidoCadastro);

        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide(new FragmentSlide.Builder()
        .background(android.R.color.white)
        .fragment(R.layout.intro_1)
        .build());


        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_2)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_3)
                .canGoForward(false)
                .build());

    }

    public void telaLoginEmail(View view){
        startActivity(new Intent(IntrodActivity.this, LoginUiActivity.class));
    }


    public void telaCadastro(View view){

        Intent intent = new Intent(IntrodActivity.this, CadastroEmailTermosActivity.class);
       startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // Verifica se usuario está logado ou não(Aqui eu coloco se os dados estão completos no usuario)
            //startActivity(new Intent(this, NavigationDrawerActivity.class));
            testandoCad();
           // finish();
        }
    }

    public  void testandoCad(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    //Log.i("FIREBASE", usuario.getIdUsuario());
                    //Log.i("FIREBASEA", usuario.getNomeUsuario());
                    testeEmail = usuario.getEmailUsuario();

                    if(testeEmail != null){

                        //Toast.makeText(getApplicationContext(), "INICIO  ", Toast.LENGTH_SHORT).show();

                        /*
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                         */

                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        finish();

                    }else if(snapshot == null) {

                        Toast.makeText(getApplicationContext(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();

                    }
                }else{
                    //Toast.makeText(getApplicationContext(), "Termine o cadastro da sua conta", Toast.LENGTH_SHORT).show();

                    /*
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_ids))
                            .requestEmail()
                            .build();

                    mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                    FirebaseAuth.getInstance().signOut();
                    mSignInClient.signOut();
                     */

                    //Método acima deslogava até mesmo quem tinha conta.
                    //*FirebaseAuth.getInstance().signOut();
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Ei " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}



