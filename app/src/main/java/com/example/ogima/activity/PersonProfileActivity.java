package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PersonProfileActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioAtual;
    private ImageButton denunciarPerfil;
    private Button buttonSeguir;
    private TextView nomeProfile, seguidoresProfile, seguindoProfile, amigosProfile;
    private ImageView fotoProfile, fundoProfile;
    private ShimmerFrameLayout shimmerFrameLayout;
    private String totalSeguidores, totalAmigos, totalSeguindo;
    private DatabaseReference seguidoresRef;
    private DatabaseReference usuarioRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String idUsuarioAtual;
    private String emailUsuarioAtual;

    @Override
    protected void onStart() {
        super.onStart();
        //Verifica se existe dados ou não no DB
        somenteVerificaSeguindo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);

        //Inicializando componentes
        inicializandoComponentes();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            usuarioSelecionado = (Usuario) dados.getSerializable("usuarioSelecionado");
            //Dados do usuário selecionado para exibir na Activity
            recuperarDadosUsuarioSelecionado();

            //Clique do usuário para seguir
            buttonSeguir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verificaSeguindo();
                    //Se não existir dados no DB permitir seguir


                    //Se existir dados no DB, permitir um método que remova esses dados do DB.
                    //E mudar o que está escrito no button para Parar de seguir.
                }
            });
        } else {
            setTitle("Voltar para pesquisa");
        }

        denunciarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //Configurando toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    //Verifica se usuário atual já não está seguindo tal usuário
    private void somenteVerificaSeguindo() {

        //Configurações iniciais
        usuarioRef = firebaseRef.child("usuarios").child(idUsuarioAtual);
        seguidoresRef = firebaseRef.child("seguidores").child(idUsuarioAtual).child(usuarioSelecionado.getIdUsuario());

        //Verifica dentro do id do usuário atual se nele tem o id do usuário selecionado.
        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Se esse dado existir, logo usuário atual está seguindo este usuário.
                    buttonSeguir.setText("Parar de seguir");
                    //Método para parar de seguir
                } else {
                    //Caso não exista dados, logo usuário atual não está seguindo este usuário
                    buttonSeguir.setText("Seguir");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //Verifica se usuário atual já não está seguindo tal usuário
    private void verificaSeguindo() {

        //Configurações iniciais
        usuarioRef = firebaseRef.child("usuarios").child(idUsuarioAtual);
        seguidoresRef = firebaseRef.child("seguidores").child(idUsuarioAtual).child(usuarioSelecionado.getIdUsuario());

        //Toast.makeText(getApplicationContext(), "Id user " + usuarioSelecionado.getIdUsuario(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "Meu id " + idUsuarioAtual, Toast.LENGTH_SHORT).show();

        //DatabaseReference seguidorRef = seguidoresRef.child(usuarioSelecionado.getIdUsuario());

        //Verifica dentro do id do usuário atual se nele tem o id do usuário selecionado.
        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Se esse dado existir, logo usuário atual está seguindo este usuário.
                    buttonSeguir.setText("Parar de seguir");
                    //Método para parar de seguir
                    usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            usuarioAtual = snapshot.getValue(Usuario.class);
                            deixarDeSeguir(usuarioAtual,usuarioSelecionado);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    //Caso não exista dados, logo usuário atual não está seguindo este usuário
                    buttonSeguir.setText("Seguir");
                    //Método para seguir usuário
                    usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            usuarioAtual = snapshot.getValue(Usuario.class);
                            salvarSeguidor(usuarioAtual,usuarioSelecionado);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void salvarSeguidor(Usuario usuarioLogado, Usuario usuarioSeguido) {

        HashMap<String, Object> dadosSeguido = new HashMap<>();
        if(usuarioSeguido.getExibirApelido().equals("sim")){
            dadosSeguido.put("apelido", usuarioSeguido.getApelidoUsuario());
        }else{
            dadosSeguido.put("nome", usuarioSeguido.getNomeUsuario());
        }
            dadosSeguido.put("fotoSeguido", usuarioSeguido.getMinhaFoto());

        DatabaseReference referenciaSeguidores = firebaseRef.child("seguidores")
                .child(usuarioLogado.getIdUsuario())
                .child(usuarioSeguido.getIdUsuario());
                referenciaSeguidores.setValue(dadosSeguido).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            buttonSeguir.setText("Parar de seguir");
                        }
                    }
                });

        /*

        seguidorRef = seguidoresTwoRef
                .child(usuarioLogado.getIdUsuario())
                .child(usuarioSeguido.getIdUsuario());
        seguidorRef.setValue(dadosSeguido);

        buttonSeguir.setText("Parar de seguir");
         */

    }

    private void deixarDeSeguir(Usuario usuarioLogado, Usuario usuarioSeguido){

        DatabaseReference deixarDeSeguirRef = firebaseRef.child("seguidores")
                .child(usuarioLogado.getIdUsuario())
                .child(usuarioSeguido.getIdUsuario());
                deixarDeSeguirRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            buttonSeguir.setText("Seguir");
                        }
                    }
                });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    public void animacaoShimmer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    fotoProfile.setVisibility(View.VISIBLE);
                    fundoProfile.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }

    private void recuperarDadosUsuarioSelecionado(){

        totalSeguidores = String.valueOf(usuarioSelecionado.getSeguidoresUsuario());
        totalAmigos = String.valueOf(usuarioSelecionado.getAmigosUsuario());
        totalSeguindo = String.valueOf(usuarioSelecionado.getSeguindoUsuario());

        seguidoresProfile.setText(totalSeguidores);
        amigosProfile.setText(totalAmigos);
        seguindoProfile.setText(totalSeguindo);

        if (usuarioSelecionado.getExibirApelido().equals("sim")) {
            nomeProfile.setText(usuarioSelecionado.getApelidoUsuario());
            setTitle(usuarioSelecionado.getApelidoUsuario());
        } else {
            nomeProfile.setText(usuarioSelecionado.getNomeUsuario());
            setTitle(usuarioSelecionado.getNomeUsuario());
        }

        try {
            if (usuarioSelecionado.getMinhaFoto() != null) {
                if (usuarioSelecionado.getEpilepsia().equals("Sim")) {
                    animacaoShimmer();
                    GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), usuarioSelecionado.getMinhaFoto(), fotoProfile, R.drawable.testewomamtwo);
                }

                if (usuarioSelecionado.getEpilepsia().equals("Não")) {
                    animacaoShimmer();
                    GlideCustomizado.montarGlide(getApplicationContext(), usuarioSelecionado.getMinhaFoto(), fotoProfile, R.drawable.testewomamtwo);
                }
            } else {
                animacaoShimmer();
                Glide.with(PersonProfileActivity.this)
                        .load(R.drawable.testewomamtwo)
                        .placeholder(R.drawable.testewomamtwo)
                        .error(R.drawable.errorimagem)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .circleCrop()
                        .into(fotoProfile);
            }

            if (usuarioSelecionado.getMeuFundo() != null) {
                if (usuarioSelecionado.getMeuFundo() != null) {
                    if (usuarioSelecionado.getEpilepsia().equals("Sim")) {
                        animacaoShimmer();
                        GlideCustomizado.fundoGlideEpilepsia(getApplicationContext(), usuarioSelecionado.getMeuFundo(), fundoProfile, R.drawable.placeholderuniverse);
                    }

                    if (usuarioSelecionado.getEpilepsia().equals("Não")) {
                        animacaoShimmer();
                        GlideCustomizado.fundoGlide(getApplicationContext(), usuarioSelecionado.getMeuFundo(), fundoProfile, R.drawable.placeholderuniverse);
                    }
                } else {
                    animacaoShimmer();
                    Glide.with(PersonProfileActivity.this)
                            .load(R.drawable.placeholderuniverse)
                            .placeholder(R.drawable.placeholderuniverse)
                            .error(R.drawable.errorimagem)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerCrop()
                            .into(fundoProfile);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void inicializandoComponentes(){
        denunciarPerfil = findViewById(R.id.imageButtonEditarProfile);
        nomeProfile = findViewById(R.id.textNickProfile);
        fotoProfile = findViewById(R.id.imageBordaPeople);
        fundoProfile = findViewById(R.id.imgFundoProfile);
        shimmerFrameLayout = findViewById(R.id.shimmerProfile);
        seguidoresProfile = findViewById(R.id.textSeguidoresProfile);
        seguindoProfile = findViewById(R.id.textSeguindoProfile);
        amigosProfile = findViewById(R.id.textAmigosProfile);
        buttonSeguir = findViewById(R.id.buttonSeguir);
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioAtual = Base64Custom.codificarBase64(emailUsuarioAtual);
    }
}