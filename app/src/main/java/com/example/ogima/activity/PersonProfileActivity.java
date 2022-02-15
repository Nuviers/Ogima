package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
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
import com.example.ogima.fragment.AmigosFragment;
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
    private ImageButton denunciarPerfil, imgButtonAddFriend;
    private Button buttonSeguir;
    private TextView nomeProfile, seguidoresProfile, seguindoProfile, amigosProfile;
    private ImageView fotoProfile, fundoProfile;
    private ShimmerFrameLayout shimmerFrameLayout;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference usuarioRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference seguidosRef;
    private DatabaseReference seguidoresRef;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private String nomeRecebido;
    private int seguidoresAtual;
    private int seguidoresDepois;
    private int seguindoAtual;
    private int seguindoDepois;
    private int pedidosAtuais;
    private String idUsuarioRecebido;

    private String nomeAtual, fotoAtual, backIntent;

    private Usuario usuarioLogado;
    private DatabaseReference friendsRef;

    @Override
    protected void onStart() {
        super.onStart();
        //Recuperar dados do amigo selecionado
        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();
        dadosUsuarioLogado();
        verificarAmizade();
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
        seguidosRef = firebaseRef.child("seguindo");
        seguidoresRef = firebaseRef.child("seguidores");
        friendsRef = firebaseRef.child("pendenciaFriend");
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);


        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            usuarioSelecionado = (Usuario) dados.getSerializable("usuarioSelecionado");
            backIntent = dados.getString("backIntent");

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        voltarActivity();
    }

    private void verificaSegueUsuarioAmigo(){

        DatabaseReference seguindoRef = seguidosRef
                .child( idUsuarioLogado )
                .child( usuarioSelecionado.getIdUsuario() );

        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getIdUsuario())
                .child(idUsuarioLogado);

        seguindoRef.addListenerForSingleValueEvent(
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
        dadosUsuarioLogado();

        if(sinalizador.equals("adicionar")){

            //Seguindo
            HashMap<String, Object> dadosSeguindo = new HashMap<>();
            dadosSeguindo.put("nomeUsuario", usuarioSelecionado.getNomeUsuario() );
            dadosSeguindo.put("minhaFoto", usuarioSelecionado.getMinhaFoto() );
            dadosSeguindo.put("idUsuario", usuarioSelecionado.getIdUsuario() );
            dadosSeguindo.put("nomeUsuarioPesquisa", usuarioSelecionado.getNomeUsuarioPesquisa() );
            DatabaseReference seguindoRef = seguidosRef
                    .child(idUsuarioLogado)
                    .child(usuarioSelecionado.getIdUsuario());
            seguindoRef.setValue( dadosSeguindo );

            //Seguidor
            HashMap<String, Object> dadosSeguidor = new HashMap<>();
            dadosSeguidor.put("nomeUsuario", nomeAtual );
            dadosSeguidor.put("minhaFoto", fotoAtual );
            dadosSeguidor.put("idUsuario", idUsuarioLogado);
            dadosSeguidor.put("nomeUsuarioPesquisa", usuarioLogado.getNomeUsuarioPesquisa() );
            DatabaseReference seguidorRef = seguidoresRef
                    .child(usuarioSelecionado.getIdUsuario())
                    .child(idUsuarioLogado);
            seguidorRef.setValue( dadosSeguidor );

            usuarioRef.child(usuarioSelecionado.getIdUsuario())
                    .child("seguidoresUsuario").setValue(seguidoresAtual+1);

            usuarioRef.child(idUsuarioLogado)
                    .child("seguindoUsuario").setValue(seguindoAtual+1);
        }

        if(sinalizador.equals("remover") && seguidoresAtual > 0){

            //Remover Seguindo
            DatabaseReference deixarDeSeguirRef = firebaseRef.child("seguindo")
                    .child(idUsuarioLogado)
                    .child(usuarioSelecionado.getIdUsuario());
            deixarDeSeguirRef.removeValue();

            //Remover Seguidor
            DatabaseReference deixarSeguidorRef = firebaseRef.child("seguidores")
                    .child(usuarioSelecionado.getIdUsuario())
                    .child(idUsuarioLogado);
            deixarSeguidorRef.removeValue();

            usuarioRef.child(usuarioSelecionado.getIdUsuario())
                    .child("seguidoresUsuario").setValue(seguidoresAtual-1);

            usuarioRef.child(idUsuarioLogado)
                    .child("seguindoUsuario").setValue(seguindoAtual-1);
        }else{
            //Toast.makeText(getApplicationContext(), "Menor que 0", Toast.LENGTH_SHORT).show();
        }

        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();
        dadosUsuarioLogado();
    }

    private void dadosUsuarioLogado(){
        usuarioRef.child(idUsuarioLogado).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    usuarioLogado = snapshot.getValue(Usuario.class);

                    seguindoAtual = usuarioLogado.getSeguindoUsuario();
                    nomeAtual = usuarioLogado.getNomeUsuario();
                    fotoAtual = usuarioLogado.getMinhaFoto();
                    pedidosAtuais = usuarioLogado.getPedidosAmizade();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        voltarActivity();
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
        dadosUsuarioLogado();
    }

    private void voltarActivity(){
        if(backIntent.equals("seguidoresActivity")){
            Intent intent = new Intent(getApplicationContext(), SeguidoresActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if(backIntent.equals("amigosFragment")){
            finish();
        }

        if(backIntent.equals("seguindoActivity")){
            Intent intent = new Intent(getApplicationContext(), SeguidoresActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("exibirSeguindo", "exibirSeguindo");
            startActivity(intent);
            finish();
        }
    }

    private void verificarAmizade(){











        ////////////////////////////////////////////

        DatabaseReference addFriendRef = friendsRef
                .child(usuarioSelecionado.getIdUsuario())
                .child(idUsuarioLogado);

        DatabaseReference addFriendTwoRef = friendsRef
                .child(usuarioSelecionado.getIdUsuario())
                .child(idUsuarioLogado);

        //DatabaseReference pelo usuário logado
        DatabaseReference amizadeLogado = firebaseRef.child("friends")
                .child(idUsuarioLogado).child(usuarioSelecionado.getIdUsuario());

        //DatabaseReference pelo amigo
        DatabaseReference amizadeAmigo = firebaseRef.child("friends")
                .child(usuarioSelecionado.getIdUsuario()).child(idUsuarioLogado);

        //DatabaseReference da tabela usuario logado
        DatabaseReference tableLogado = usuarioRef.child(idUsuarioLogado);

        //DatabaseReference da tabela usuario amigo
        DatabaseReference tableAmigo = usuarioRef.child(usuarioSelecionado.getIdUsuario());

        //Verificar amizade pelo usuário logado
        amizadeLogado.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    //ToastCustomizado.toastCustomizadoCurto("Amizade existente",getApplicationContext());
                    try{
                        imgButtonAddFriend.setVisibility(View.VISIBLE);
                        imgButtonAddFriend.setImageResource(R.drawable.iconremovethree);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    imgButtonAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try{
                                amizadeLogado.removeValue();
                                amizadeAmigo.removeValue();
                                addFriendRef.removeValue();
                                addFriendTwoRef.removeValue();
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                            tableLogado.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null){
                                        if(usuarioLogado.getAmigosUsuario() >=0){
                                            tableLogado.child("amigosUsuario").setValue(usuarioLogado.getAmigosUsuario() - 1);
                                        }
                                        if(usuarioLogado.getPedidosAmizade() >=0){
                                            tableLogado.child("pedidosAmizade").setValue(usuarioLogado.getPedidosAmizade() - 1);
                                        }
                                        if(usuarioSelecionado.getAmigosUsuario() >=0){
                                            tableAmigo.child("amigosUsuario").setValue(usuarioSelecionado.getAmigosUsuario() - 1);
                                        }
                                        ToastCustomizado.toastCustomizadoCurto("Amizade desfeita com sucesso", getApplicationContext());
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                    }
                                    tableLogado.removeEventListener(this);
                                    //tableAmigo.removeEventListener(this);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                }else{
                    //Verificar amizade pelo amigo
                    amizadeAmigo.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null){
                                //ToastCustomizado.toastCustomizadoCurto("Amizade pelo amigo",getApplicationContext());
                                try{
                                    imgButtonAddFriend.setVisibility(View.VISIBLE);
                                    imgButtonAddFriend.setImageResource(R.drawable.iconremovethree);
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                                imgButtonAddFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try{
                                            amizadeLogado.removeValue();
                                            amizadeAmigo.removeValue();
                                        }catch (Exception ex){
                                            ex.printStackTrace();
                                        }
                                        tableLogado.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.getValue() != null){
                                                    tableLogado.child("amigosUsuario").setValue(usuarioLogado.getAmigosUsuario() - 1);
                                                    tableAmigo.child("amigosUsuario").setValue(usuarioSelecionado.getAmigosUsuario() - 1);
                                                    ToastCustomizado.toastCustomizadoCurto("Amizade desfeita com sucesso", getApplicationContext());
                                                    finish();
                                                    overridePendingTransition(0, 0);
                                                    startActivity(getIntent());
                                                    overridePendingTransition(0, 0);
                                                }
                                                tableLogado.removeEventListener(this);
                                                //tableAmigo.removeEventListener(this);
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                });
                            }else{
                                addFriendRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.getValue() != null){
                                            try{
                                                imgButtonAddFriend.setVisibility(View.GONE);
                                            }catch (Exception ex){
                                                ex.printStackTrace();
                                            }
                                        }else{
                                            try{
                                                imgButtonAddFriend.setVisibility(View.VISIBLE);
                                            }catch (Exception ex){
                                                ex.printStackTrace();
                                            }
                                        }
                                        addFriendRef.removeEventListener(this);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                //ToastCustomizado.toastCustomizadoCurto("Sem amizade pelo amigo",getApplicationContext());
                                imgButtonAddFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        recuperarDadosPerfilAmigo();
                                        receberDadosSelecionado();
                                        dadosUsuarioLogado();
                                        //Seguidor
                                        HashMap<String, Object> dadosAddFriend = new HashMap<>();
                                        dadosAddFriend.put("nomeUsuario", usuarioLogado.getNomeUsuario() );
                                        dadosAddFriend.put("minhaFoto", usuarioLogado.getMinhaFoto() );
                                        dadosAddFriend.put("idUsuario", usuarioLogado.getIdUsuario() );
                                        dadosAddFriend.put("nomeUsuarioPesquisa", usuarioLogado.getNomeUsuarioPesquisa() );
                                        addFriendRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.getValue() != null){
                                                    //Arrumar uma lógica pra ver se existe o dado do usuario atual com o do amigo
                                                    ToastCustomizado.toastCustomizadoCurto("Pedido de amizade já foi enviado", getApplicationContext());
                                                }else{
                                                    addFriendRef.setValue( dadosAddFriend ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                ToastCustomizado.toastCustomizadoCurto("Pedido de amizade enviado com sucesso!",getApplicationContext());
                                                                usuarioRef.child(usuarioSelecionado.getIdUsuario())
                                                                        .child("pedidosAmizade").setValue(usuarioSelecionado.getPedidosAmizade()+1);
                                                            }else{
                                                                ToastCustomizado.toastCustomizadoCurto("Erro ao enviar solicitação, tente novamente!", getApplicationContext());
                                                            }
                                                        }
                                                    });
                                                }
                                                addFriendRef.removeEventListener(this);
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                });
                            }
                            amizadeLogado.removeEventListener(this);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //ToastCustomizado.toastCustomizadoCurto("Vocês não são amigos",getApplicationContext());
                }
                amizadeLogado.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //ToastCustomizado.toastCustomizadoCurto("Verificador atual " , getApplicationContext());

        //ToastCustomizado.toastCustomizadoCurto("Id meu " + idUsuarioLogado, getApplicationContext());
        //ToastCustomizado.toastCustomizadoCurto("Id amigo " + usuarioSelecionado.getIdUsuario(), getApplicationContext());

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
        imgButtonAddFriend = findViewById(R.id.imgButtonAddFriend);
    }
}