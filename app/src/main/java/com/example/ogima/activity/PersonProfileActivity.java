package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PersonProfileActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private ImageButton denunciarPerfil;
    private Button buttonSeguir;
    private TextView nomeProfile, seguidoresProfile, seguindoProfile, amigosProfile;
    private ImageView fotoProfile, fundoProfile;
    private ShimmerFrameLayout shimmerFrameLayout;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference usuarioRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference seguidoresRef;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private ValueEventListener valueEventListenerPerfilAmigo;

    private String nomeRecebido;
    private int seguidoresAtual;
    private int seguidoresDepois;
    private String idUsuarioRecebido;


    @Override
    protected void onStart() {
        super.onStart();
        //Recuperar dados do amigo selecionado
        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();
        //Ver o estado de seguindo e seguidor
        //verificaSegueUsuarioAmigo();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);

        //Inicializando componentes
        inicializandoComponentes();

        //Configurações iniciais
        usuarioRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);


        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            usuarioSelecionado = (Usuario) dados.getSerializable("usuarioSelecionado");

            buttonSeguir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verificaSegueUsuarioAmigo();
                }
            });

        } else {
            setTitle("Voltar para pesquisa");
        }
        //Configurando toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void verificaSegueUsuarioAmigo(){

        DatabaseReference seguidorRef = seguidoresRef
                .child( idUsuarioLogado )
                .child( usuarioSelecionado.getIdUsuario() );

        seguidorRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if( dataSnapshot.exists() ){
                            //Já está seguindo
                            buttonSeguir.setText("Parar de seguir");
                            //Toast.makeText(getApplicationContext(), "Seguindo", Toast.LENGTH_SHORT).show();
                            buttonSeguir.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                        calcularSeguidor("remover");
                                }
                            });
                        }else {
                            //Ainda não está seguindo
                            buttonSeguir.setText("Seguir");
                            //Toast.makeText(getApplicationContext(), "Não seguindo", Toast.LENGTH_SHORT).show();
                            buttonSeguir.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                        calcularSeguidor("adicionar");
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    private void receberDadosSelecionado(){
        usuarioRef.child(usuarioSelecionado.getIdUsuario()).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){

                    Usuario usuarioRecebido = snapshot.getValue(Usuario.class);

                    idUsuarioRecebido = usuarioRecebido.getIdUsuario();
                    nomeRecebido = usuarioRecebido.getNomeUsuario();
                    seguidoresAtual = usuarioRecebido.getSeguidoresUsuario();

                    //Toast.makeText(getApplicationContext(), "Nome recebido " + nomeRecebido, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), "Seguidores recebido " + seguidoresAtual, Toast.LENGTH_SHORT).show();

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void calcularSeguidor(String sinalizador){

        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();

        if(sinalizador.equals("adicionar")){

            HashMap<String, Object> dadosAmigo = new HashMap<>();
            dadosAmigo.put("nomeUsuario", usuarioSelecionado.getNomeUsuario() );
            dadosAmigo.put("minhaFoto", usuarioSelecionado.getMinhaFoto() );
            DatabaseReference seguidorRef = seguidoresRef
                    .child(idUsuarioLogado)
                    .child(usuarioSelecionado.getIdUsuario());
            seguidorRef.setValue( dadosAmigo );

            usuarioRef.child(usuarioSelecionado.getIdUsuario())
                    .child("seguidoresUsuario").setValue(seguidoresAtual+1);
        }

        if(sinalizador.equals("remover") && seguidoresAtual > 0){

            DatabaseReference deixarDeSeguirRef = firebaseRef.child("seguidores")
                    .child(idUsuarioLogado)
                    .child(usuarioSelecionado.getIdUsuario());
            deixarDeSeguirRef.removeValue();

            usuarioRef.child(usuarioSelecionado.getIdUsuario())
                    .child("seguidoresUsuario").setValue(seguidoresAtual-1);
        }else{
            //Toast.makeText(getApplicationContext(), "Menor que 0", Toast.LENGTH_SHORT).show();
        }

        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();
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

    private void recuperarDadosPerfilAmigo(){

        //Toast.makeText(getApplicationContext(), "Chegou aqui", Toast.LENGTH_SHORT).show();

        verificaSegueUsuarioAmigo();

        usuarioAmigoRef = usuarioRef.child( usuarioSelecionado.getIdUsuario() );
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Usuario usuario = dataSnapshot.getValue( Usuario.class );

                        String amigos = String.valueOf( usuario.getAmigosUsuario() );
                        String seguindo = String.valueOf( usuario.getSeguindoUsuario() );
                        String seguidores = String.valueOf( usuario.getSeguidoresUsuario() );

                        //Configura valores recuperados
                        amigosProfile.setText( amigos );
                        seguidoresProfile.setText( seguidores );
                        seguindoProfile.setText( seguindo );
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

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

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener( valueEventListenerPerfilAmigo );
        receberDadosSelecionado();
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
    }
}