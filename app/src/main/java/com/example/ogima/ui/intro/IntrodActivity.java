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

import java.time.LocalDateTime;

public class IntrodActivity extends IntroActivity {

    private Button buttonDefinidoLogin;
    private Button buttonDefinidoCadastro;
    private FirebaseAuth mAuth;

    //
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
//

    private String testeEmail;
    private GoogleSignInClient mSignInClient;

    private SQLiteDatabase escreve;
    private SQLiteDatabase le;
    private int contadorA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_home);

        buttonDefinidoLogin = findViewById(R.id.buttonDefinidoLogin);
        buttonDefinidoCadastro = findViewById(R.id.buttonDefinidoCadastro);

        //armazenarContador();
        armazenarContadorDois();
        DataHoraAtualizado dataHoraAtualizado = new DataHoraAtualizado();
        dataHoraAtualizado.novaData();
/*
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // Verifica se usuario está logado ou não
            startActivity(new Intent(this, NavigationDrawerActivity.class));
            finish();
        }


 */

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
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        //finish();
                    }else if(snapshot == null) {

                        Toast.makeText(getApplicationContext(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();

                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Conta não cadastrada", Toast.LENGTH_SHORT).show();

                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_ids))
                            .requestEmail()
                            .build();

                    mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                    FirebaseAuth.getInstance().signOut();
                    mSignInClient.signOut();

                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Ei " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void armazenarContador(){

        ContentValues cv = new ContentValues();
        cv.put("contadorAlteracao", 1);

        DbHelper dbHelper = new DbHelper(getApplicationContext());
        dbHelper.getWritableDatabase().insert(DbHelper.TABLE_NAME,null, cv);

        if(cv.get("contadorAlteracao").equals(1)){
            Toast.makeText(getApplicationContext(), "Igual a 1", Toast.LENGTH_SHORT).show();
            Log.i("INFO DB", "Igual a 1");
        }else{
            Toast.makeText(getApplicationContext(), "Diferente de 1", Toast.LENGTH_SHORT).show();
            Log.i("INFO DB", "Diferente de 1");
        }
    }


    public void armazenarContadorDois(){

        InfoUserDAO infoUserDAO = new InfoUserDAO(getApplicationContext());
        Informacoes informacoes = new Informacoes();

        infoUserDAO.recuperar(informacoes);

        Log.i("INFO DB", "MeuId " + informacoes.getId());
        Log.i("INFO DB", "MeuContador " + informacoes.getContadorAlteracao());

        contadorA = informacoes.getContadorAlteracao();

        if(contadorA >= 1){
            if(contadorA < 5){
                contadorA++;
                informacoes.setContadorAlteracao(contadorA);
                infoUserDAO.atualizar(informacoes);
                //infoUserDAO.salvar(informacoes);
                Log.i("INFO DB", "Contador igual a " + informacoes.getContadorAlteracao());
            }else{
                Log.i("INFO DB", "Limite atingido " + informacoes.getContadorAlteracao());

            }
        }else{
            informacoes.setContadorAlteracao(1);
            infoUserDAO.salvar(informacoes);

            Log.i("INFO DB", "Adicionado valor 1");
        }

        //informacoes.setContadorAlteracao(1);
        //infoUserDAO.salvar(informacoes);

        /*
        if(cv.get("contadorAlteracao").equals(1)){
            cv.put("contadorAlteracao", valorAtual + 1);
            Toast.makeText(getApplicationContext(), "Igual a 2", Toast.LENGTH_SHORT).show();
            Log.i("INFO DB", "Igual a 1");
        }else if(valorAtual >= 2 && valorAtual <= 7){
            Toast.makeText(getApplicationContext(), "Maior ou igual a 2", Toast.LENGTH_SHORT).show();
            Log.i("INFO DB", "Diferente de 1");
        }
         */

    }

}

