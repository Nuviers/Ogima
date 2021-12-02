package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.fragment.AmigosFragment;
import com.example.ogima.fragment.MusicaFragment;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.fragment.RecupEmailFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.CodigoActivity;
import com.example.ogima.ui.intro.IntrodActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Locale;

public class ProblemasLogin extends AppCompatActivity {

    private Button buttonRecupSenha, buttonRecupEmail, buttonContinuar;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private EditText editTextEmailOrSenha;
    private String recuperarDado, emailConvertido, criptografado, minhaFoto;
    private ImageView imageViewFoto;

    //TabLayout
    private TabLayout tabLayout;
    private TabItem tabItemEmail, tabItemSMS;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajuda_login);
        Toolbar toolbar = findViewById(R.id.toolbarlogin);
        setSupportActionBar(toolbar);

        //Inicializando componentes
        inicializandoComponentes();

        //Titulo da toolbar
        setTitle("Problemas no login");

        //Configurando toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Abas
        configurandoAba();

        /*
        buttonContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recuperarDado = editTextEmailOrSenha.getText().toString();
                emailConvertido =  recuperarDado.toLowerCase(Locale.ROOT);

                if(!recuperarDado.isEmpty()){

                criptografado = Base64Custom.codificarBase64(emailConvertido);
                    //procurandoUsuario();
                }

            }
        });

 */
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void procurandoUsuario(){

        try{
            DatabaseReference userEmailRef = firebaseRef.child("usuarios").child(criptografado);
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()) {

                        Toast.makeText(getApplicationContext(), "Dado não localizado", Toast.LENGTH_SHORT).show();

                        Glide.with(ProblemasLogin.this)
                                .load(R.drawable.avatarfemale)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .centerCrop()
                                .circleCrop()
                                .into(imageViewFoto);

                    }else{
                        Toast.makeText(getApplicationContext(), "Dado localizado com sucesso!", Toast.LENGTH_SHORT).show();

                        testandoLog();

                        FirebaseAuth.getInstance().sendPasswordResetEmail(emailConvertido)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //progressBarTeste.setVisibility(View.VISIBLE);
                                        if (task.isSuccessful()) {
                                            //progressBarTeste.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext()," Link para redifinição de senha enviado para o email " + emailConvertido,
                                                    Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getApplicationContext(), " Ocorreu um erro" + " " +  task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Erro " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            userEmailRef.addListenerForSingleValueEvent(eventListener);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void testandoLog(){

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(criptografado);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    Log.i("FIREBASE", usuario.getIdUsuario());
                    Log.i("FIREBASEA", usuario.getNomeUsuario());
                    minhaFoto = usuario.getMinhaFoto();

                    if (minhaFoto != null) {

                        Toast.makeText(getApplicationContext(), " Okay", Toast.LENGTH_SHORT).show();

                        Glide.with(ProblemasLogin.this)
                                .load(minhaFoto)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .centerCrop()
                                .circleCrop()
                                .into(imageViewFoto);

                        Log.i("IMAGEM", "Sucesso ao atualizar foto de perfil");
                    } else {
                        Log.i("IMAGEM", "Falha ao atualizar foto de perfil");
                    }

                } else if (snapshot == null) {

                    Toast.makeText(getApplicationContext(), " Dados nulos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void inicializandoComponentes(){
        //editTextEmailOrSenha = findViewById(R.id.editTextEmailOrSenha);
        //buttonContinuar = findViewById(R.id.buttonContinuar);
        //imageViewFoto = findViewById(R.id.imageViewFoto);
        //tabLayout = findViewById(R.id.tabLayout);
        //tabItemEmail = findViewById(R.id.tabItemEmail);
        //tabItemSMS = findViewById(R.id.tabItemSMS);
        //SmartTabLayout - Abas
        smartTabLayout = findViewById(R.id.viewPagerTab);
        viewPager = findViewById(R.id.viewPager);
    }

    public void configurandoAba(){

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Email", RecupEmailFragment.class)
                .add("SMS", AmigosFragment.class)
                .create());

        viewPager.setAdapter(adapter);
        smartTabLayout.setViewPager(viewPager);
    }


}