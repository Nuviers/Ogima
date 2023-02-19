package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFuncoesPostagem;
import com.example.ogima.adapter.AdapterGridFotosPostagem;
import com.example.ogima.adapter.AdapterGridPostagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PersonProfileActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado, usuarioCurtida;
    private ImageButton imgButtonBlockUser, imgButtonAddFriend, imgButtonDeleteFriend,
            imgButtonPendingFriend, imgButtonRemoveRequest, imgButtonAcceptRequest;
    private Button buttonSeguir, btnTodasFotosOther, btnVerPostagensPerson;
    private TextView nomeProfile, seguidoresProfile, seguindoProfile, amigosProfile,
            txtViewSemFotosPerson, txtViewSemPostagemPerson;
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
    private ValueEventListener valueEventListener, valueEventListenerTwo;
    private Usuario usuarioLogado;
    private DatabaseReference blockRef, denunciaBlockRef, blockSaveRef;
    private String sinalizadorBlocked, receberId;
    //Dados para exibição das fotos do usuário
    private List<Postagem> listaFotoPostagem = new ArrayList<>();
    private List<Postagem> listaPostagens = new ArrayList<>();
    private RecyclerView recyclerGridFotoPostagem, recyclerPostagensPerson;
    private AdapterGridFotosPostagem adapterGridFotosPostagem;
    private AdapterGridPostagem adapterGridPostagem;
    private Usuario usuarioAtual;
    private DatabaseReference complementoPostagemRef;
    private DatabaseReference postagemUsuarioRef;
    private AdapterFuncoesPostagem adapterFuncoesPostagem;
    private ImageButton imgButtonIniciarConversa;
    private ValueEventListener eventListenerAmizade, eventListenerConvite;
    private DatabaseReference verificaAmizadeRef, verificaAmizadeSelecionadoRef, verificaConviteRef, desfazerAmizadeRef,
            desfazerAmizadeSelecionadoRef, dadosUserAtualRef, dadosUserSelecionadoRef,
            conviteAmizadeRef, conviteAmizadeSelecionadoRef, novoContatoRef, novoContatoSelecionadoRef,
            contadorMensagemRef, contadorMensagemSelecionadoRef;
    private HashMap<String, Object> dadosContatoAtual = new HashMap<>();
    private HashMap<String, Object> dadosContatoSelecionado = new HashMap<>();

    @Override
    protected void onStart() {
        super.onStart();
        verificarRelacionamento();
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            receberDadosSelecionado();
            dadosUsuarioLogado();
            removerEventListener(verificaAmizadeRef, eventListenerAmizade);
            removerEventListener(verificaConviteRef, eventListenerConvite);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        blockRef = firebaseRef.child("blockUser");

        //Fazer um verificador para ver se o usuário atual tem alguma postagem ou não

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerGridFotoPostagem.setHasFixedSize(true);
        recyclerGridFotoPostagem.setLayoutManager(layoutManager);

        GridLayoutManager layoutManagers = new GridLayoutManager(this, 2);
        recyclerPostagensPerson.setHasFixedSize(true);
        recyclerPostagensPerson.setLayoutManager(layoutManagers);

        DatabaseReference usuarioAtualRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado);

        usuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    usuarioAtual = snapshot.getValue(Usuario.class);
                    if (adapterGridFotosPostagem != null) {

                    } else {
                        adapterGridFotosPostagem = new AdapterGridFotosPostagem(listaFotoPostagem, getApplicationContext(), usuarioAtual.getEpilepsia());
                    }
                    recyclerGridFotoPostagem.setAdapter(adapterGridFotosPostagem);

                    if (adapterGridPostagem != null) {

                    } else {
                        adapterGridPostagem = new AdapterGridPostagem(listaPostagens, getApplicationContext(), usuarioAtual.getEpilepsia());
                    }
                    recyclerPostagensPerson.setAdapter(adapterGridPostagem);
                }
                usuarioAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //enviarEmail();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            receberId = dados.getString("idEnviado");
            usuarioSelecionado = (Usuario) dados.getSerializable("usuarioSelecionado");
            backIntent = dados.getString("backIntent");
            sinalizadorBlocked = dados.getString("blockedUser");

            buttonSeguir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verificaSegueUsuarioAmigo();
                }
            });

            if (sinalizadorBlocked != null) {
                ToastCustomizado.toastCustomizadoCurto("Perfil do usuário indisponível!", getApplicationContext());
                //onBackPressed();
                finish();
            }
        } else {
            setTitle("Voltar para pesquisa");
        }
        //Configurando toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();
        dadosUsuarioLogado();
        referenciasAmizade();

        //Configurando inicio de conversa
        imgButtonIniciarConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ConversaActivity.class);
                intent.putExtra("usuario", usuarioSelecionado);
                startActivity(intent);
                //finish();
            }
        });

        blockSaveRef = blockRef
                .child(usuarioSelecionado.getIdUsuario())
                .child(idUsuarioLogado);

        //Configurando metódo para bloquear e/ou denunciar usuário
        imgButtonBlockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), imgButtonBlockUser);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_block, popup.getMenu());

                MenuItem bedMenuItem = popup.getMenu().findItem(R.id.blockUser);

                valueEventListener = blockSaveRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            bedMenuItem.setTitle("Desbloquear usuário");
                            //ToastCustomizado.toastCustomizadoCurto("Esse usuário já foi bloqueado por você", getApplicationContext());
                        } else {
                            //ToastCustomizado.toastCustomizadoCurto("Bloqueado",getApplicationContext());
                            bedMenuItem.setTitle("Bloquear usuário");
                        }
                        blockSaveRef.removeEventListener(valueEventListener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.blockUser:
                                valueEventListenerTwo = blockSaveRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getValue() == null) {
                                            //Salvando dados do block
                                            HashMap<String, Object> dadosBlock = new HashMap<>();
                                            dadosBlock.put("idUsuario", usuarioSelecionado.getIdUsuario());
                                            blockSaveRef.setValue(dadosBlock).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        ToastCustomizado.toastCustomizadoCurto("Usuário bloqueado com sucesso", getApplicationContext());
                                                        blockSaveRef.addValueEventListener(valueEventListener);
                                                    } else {
                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao bloquear usuário, tente novamente", getApplicationContext());
                                                    }
                                                }
                                            });
                                        } else {
                                            blockSaveRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        ToastCustomizado.toastCustomizadoCurto("Usuário desbloqueado com sucesso", getApplicationContext());
                                                    } else {
                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao desbloquear usuário, tente novamente", getApplicationContext());
                                                    }
                                                }
                                            });
                                        }
                                        blockSaveRef.removeEventListener(valueEventListenerTwo);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                break;
                            case R.id.denunciaBlockUser:
                                //Talvez seja necessário limitar essa função?
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("message/rfc822");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Denúncia - " + "Informe o nome do usuário denunciado");
                                intent.putExtra(Intent.EXTRA_TEXT, "Descreva sua denúncia nesse campo e anexe as provas no email.");
                                try {
                                    startActivity(Intent.createChooser(intent, "Selecione seu app de envio de email."));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        voltarActivity();
    }

    private void verificaSegueUsuarioAmigo() {

        DatabaseReference seguindoRef = seguidosRef
                .child(idUsuarioLogado)
                .child(usuarioSelecionado.getIdUsuario());

        seguindoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //Já está seguindo
                            buttonSeguir.setText("Parar de seguir");
                            //Toast.makeText(getApplicationContext(), "Seguindo", Toast.LENGTH_SHORT).show();
                            buttonSeguir.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    calcularSeguidor("remover");
                                }
                            });
                        } else {
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

    private void receberDadosSelecionado() {
        usuarioRef.child(usuarioSelecionado.getIdUsuario()).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {

                            Usuario usuarioRecebido = snapshot.getValue(Usuario.class);

                            idUsuarioRecebido = usuarioRecebido.getIdUsuario();
                            nomeRecebido = usuarioRecebido.getNomeUsuario();
                            seguidoresAtual = usuarioRecebido.getSeguidoresUsuario();

                            //Toast.makeText(getApplicationContext(), "Nome recebido " + nomeRecebido, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), "Seguidores recebido " + seguidoresAtual, Toast.LENGTH_SHORT).show();

                            exibirComplementoFoto();
                            exibirPostagens();

                            btnTodasFotosOther.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                    intent.putExtra("idRecebido", idUsuarioRecebido);
                                    startActivity(intent);
                                }
                            });

                            btnVerPostagensPerson.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getApplicationContext(), DetalhesPostagemActivity.class);
                                    intent.putExtra("idRecebido", idUsuarioRecebido);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void calcularSeguidor(String sinalizador) {

        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();
        dadosUsuarioLogado();

        if (sinalizador.equals("adicionar")) {

            //Seguindo
            HashMap<String, Object> dadosSeguindo = new HashMap<>();
            dadosSeguindo.put("idUsuario", usuarioSelecionado.getIdUsuario());
            DatabaseReference seguindoRef = seguidosRef
                    .child(idUsuarioLogado)
                    .child(usuarioSelecionado.getIdUsuario());
            seguindoRef.setValue(dadosSeguindo);

            //Seguidor
            HashMap<String, Object> dadosSeguidor = new HashMap<>();
            dadosSeguidor.put("idUsuario", idUsuarioLogado);

            DatabaseReference seguidorRef = seguidoresRef
                    .child(usuarioSelecionado.getIdUsuario())
                    .child(idUsuarioLogado);
            seguidorRef.setValue(dadosSeguidor);

            usuarioRef.child(usuarioSelecionado.getIdUsuario())
                    .child("seguidoresUsuario").setValue(seguidoresAtual + 1);

            usuarioRef.child(idUsuarioLogado)
                    .child("seguindoUsuario").setValue(seguindoAtual + 1);
        }

        if (sinalizador.equals("remover") && seguidoresAtual > 0) {

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
                    .child("seguidoresUsuario").setValue(seguidoresAtual - 1);

            usuarioRef.child(idUsuarioLogado)
                    .child("seguindoUsuario").setValue(seguindoAtual - 1);
        } else {
            //Toast.makeText(getApplicationContext(), "Menor que 0", Toast.LENGTH_SHORT).show();
        }

        recuperarDadosPerfilAmigo();
        receberDadosSelecionado();
        dadosUsuarioLogado();
    }

    private void dadosUsuarioLogado() {
        usuarioRef.child(idUsuarioLogado).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    usuarioLogado = snapshot.getValue(Usuario.class);

                    seguindoAtual = usuarioLogado.getSeguindoUsuario();
                    nomeAtual = usuarioLogado.getNomeUsuario();
                    fotoAtual = usuarioLogado.getMinhaFoto();
                    pedidosAtuais = usuarioLogado.getPedidosAmizade();

                    //Cria nó para exibir posteriormente na lista de exibições
                    //do perfil do usuário selecionado
                    HashMap<String, Object> dadosViewLogado = new HashMap<>();
                    dadosViewLogado.put("idUsuario", idUsuarioLogado);

                    DatabaseReference profileViewsRef = firebaseRef.child("profileViews")
                            .child(usuarioSelecionado.getIdUsuario())
                            .child(idUsuarioLogado);

                    //Verificando se existe o nó antes de acrescentar novamente a visualização
                    profileViewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() == null) {
                                profileViewsRef.setValue(dadosViewLogado);
                                DatabaseReference salvarViewRef = firebaseRef.child("usuarios")
                                        .child(usuarioSelecionado.getIdUsuario()).child("viewsPerfil");
                                salvarViewRef.setValue(usuarioSelecionado.getViewsPerfil() + 1);
                            }
                            profileViewsRef.removeEventListener(this);
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

    private void recuperarDadosPerfilAmigo() {

        //Toast.makeText(getApplicationContext(), "Chegou aqui", Toast.LENGTH_SHORT).show();

        verificaSegueUsuarioAmigo();

        usuarioAmigoRef = usuarioRef.child(usuarioSelecionado.getIdUsuario());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Usuario usuario = dataSnapshot.getValue(Usuario.class);

                        String amigos = String.valueOf(usuario.getAmigosUsuario());
                        String seguindo = String.valueOf(usuario.getSeguindoUsuario());
                        String seguidores = String.valueOf(usuario.getSeguidoresUsuario());

                        //Configura valores recuperados
                        amigosProfile.setText(amigos);
                        seguidoresProfile.setText(seguidores);
                        seguindoProfile.setText(seguindo);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        try {
            if (usuarioSelecionado.getExibirApelido().equals("sim")) {
                nomeProfile.setText(usuarioSelecionado.getApelidoUsuario());
                setTitle(usuarioSelecionado.getApelidoUsuario());
            } else {
                nomeProfile.setText(usuarioSelecionado.getNomeUsuario());
                setTitle(usuarioSelecionado.getNomeUsuario());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (usuarioSelecionado.getMinhaFoto() != null) {
                if (usuarioSelecionado.getEpilepsia().equals("Sim")) {
                    animacaoShimmer();
                    GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), usuarioSelecionado.getMinhaFoto(), fotoProfile, R.color.gph_transparent);
                }

                if (usuarioSelecionado.getEpilepsia().equals("Não")) {
                    animacaoShimmer();
                    GlideCustomizado.montarGlide(getApplicationContext(), usuarioSelecionado.getMinhaFoto(), fotoProfile, R.color.gph_transparent);
                }
            } else {
                animacaoShimmer();
                Glide.with(PersonProfileActivity.this)
                        .load(R.drawable.testewomamtwo)
                        .placeholder(R.color.gph_transparent)
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
                        GlideCustomizado.fundoGlideEpilepsia(getApplicationContext(), usuarioSelecionado.getMeuFundo(), fundoProfile, R.color.gph_transparent);
                    }

                    if (usuarioSelecionado.getEpilepsia().equals("Não")) {
                        animacaoShimmer();
                        GlideCustomizado.fundoGlide(getApplicationContext(), usuarioSelecionado.getMeuFundo(), fundoProfile, R.color.gph_transparent);
                    }
                } else {
                    animacaoShimmer();
                    Glide.with(PersonProfileActivity.this)
                            .load(R.drawable.placeholderuniverse)
                            .placeholder(R.color.gph_transparent)
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

    private void voltarActivity() {

        if (backIntent != null) {
            if (backIntent.equals("seguidoresActivity")) {
                Intent intent = new Intent(getApplicationContext(), SeguidoresActivity.class);
                intent.putExtra("exibirSeguidores", "exibirSeguidores");
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            if (backIntent.equals("amigosFragment")) {
                finish();
            }

            if (backIntent.equals("seguindoActivity")) {
                Intent intent = new Intent(getApplicationContext(), SeguidoresActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("exibirSeguindo", "exibirSeguindo");
                startActivity(intent);
                finish();
            }
        } else {
            finish();
        }
    }

    private void referenciasAmizade() {

    }

    private void inicializandoComponentes() {
        recyclerGridFotoPostagem = findViewById(R.id.recyclerGridFotoPostagem);
        recyclerPostagensPerson = findViewById(R.id.recyclerPostagensPerson);
        imgButtonBlockUser = findViewById(R.id.imageButtonBlockUser);
        nomeProfile = findViewById(R.id.textNickProfile);
        fotoProfile = findViewById(R.id.imageBordaPeople);
        fundoProfile = findViewById(R.id.imgFundoProfile);
        shimmerFrameLayout = findViewById(R.id.shimmerProfile);
        seguidoresProfile = findViewById(R.id.textSeguidoresProfile);
        seguindoProfile = findViewById(R.id.textSeguindoProfile);
        amigosProfile = findViewById(R.id.textAmigosProfile);
        buttonSeguir = findViewById(R.id.buttonSeguir);
        imgButtonAddFriend = findViewById(R.id.imgButtonAddFriend);
        //Dados para exibir fotos do usuário
        btnTodasFotosOther = findViewById(R.id.btnTodasFotosOther);
        txtViewSemFotosPerson = findViewById(R.id.txtViewSemFotosPerson);
        btnVerPostagensPerson = findViewById(R.id.btnVerPostagensPerson);
        txtViewSemPostagemPerson = findViewById(R.id.txtViewSemPostagemPerson);
        imgButtonIniciarConversa = findViewById(R.id.imgButtonIniciarConversa);
        imgButtonDeleteFriend = findViewById(R.id.imgButtonDeleteFriend);
        imgButtonPendingFriend = findViewById(R.id.imgButtonPendingFriend);
        imgButtonRemoveRequest = findViewById(R.id.imgButtonRemoveRequest);
        imgButtonAcceptRequest = findViewById(R.id.imgButtonAcceptRequest);
    }

    private void enviarEmail() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String titulo = "A senha da sua conta do Ogima foi alterada";
                    String corpo = "Sua senha foi alterada, se você fez essa alteração, por favor desconsidere esse email. " +
                            " Se você não fez essa alteração, altere sua senha imediatamente, caso não consiga entre em contato com nosso suporte nesse mesmo email: fraskbr2@gmail.com " +
                            " Atenciosamente Equipe Ogima";
                    String destinatario = "*******@gmail.com";
                    GMailSender sender = new GMailSender(getString(R.string.REMETENTE), getString(R.string.SENREMETENTE));
                    sender.sendMail(titulo, corpo, getString(R.string.REMETENTE), destinatario);
                    //ToastCustomizado.toastCustomizadoCurto("Email enviado com sucesso", getApplicationContext());
                } catch (Exception e) {
                    //ToastCustomizado.toastCustomizadoCurto("ERRO", getApplicationContext());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void exibirComplementoFoto() {


        DatabaseReference complementoPostagemRefs = firebaseRef
                .child("complementoFoto").child(idUsuarioRecebido);
        DatabaseReference postagemUsuarioRefs = firebaseRef
                .child("postagens").child(idUsuarioRecebido);

        //ToastCustomizado.toastCustomizadoCurto("Id recebido person " + idUsuarioRecebido, getApplicationContext());

        complementoPostagemRefs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    try {
                        Postagem postagem = snapshot.getValue(Postagem.class);

                        if (postagem.getTotalPostagens() <= 0) {
                            recyclerGridFotoPostagem.setVisibility(View.GONE);
                            btnTodasFotosOther.setVisibility(View.GONE);
                            txtViewSemFotosPerson.setVisibility(View.VISIBLE);
                        } else {
                            txtViewSemFotosPerson.setVisibility(View.GONE);
                            recyclerGridFotoPostagem.setVisibility(View.VISIBLE);
                            btnTodasFotosOther.setVisibility(View.VISIBLE);

                            postagemUsuarioRefs.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        listaFotoPostagem.clear();
                                        adapterGridFotosPostagem.notifyDataSetChanged();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            Postagem postagemChildren = dataSnapshot.getValue(Postagem.class);
                                            if (postagemChildren.getTipoPostagem().equals("foto")) {
                                                if (postagemChildren.getPublicoPostagem().equals("Todos")) {
                                                    listaFotoPostagem.add(postagemChildren);
                                                    Collections.sort(listaFotoPostagem, new Comparator<Postagem>() {
                                                        public int compare(Postagem o1, Postagem o2) {
                                                            return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                        }
                                                    });
                                                    adapterGridFotosPostagem.notifyDataSetChanged();
                                                } else if (postagemChildren.getPublicoPostagem().equals("Somente amigos")) {
                                                    DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                                            .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                    analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                listaFotoPostagem.add(postagemChildren);
                                                                Collections.sort(listaFotoPostagem, new Comparator<Postagem>() {
                                                                    public int compare(Postagem o1, Postagem o2) {
                                                                        return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                    }
                                                                });
                                                                adapterGridFotosPostagem.notifyDataSetChanged();
                                                            }
                                                            analisaAmizadeRef.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                } else if (postagemChildren.getPublicoPostagem().equals("Somente seguidores")) {
                                                    DatabaseReference analisaSeguindoRef = firebaseRef.child("seguindo")
                                                            .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                    analisaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                listaFotoPostagem.add(postagemChildren);
                                                                Collections.sort(listaFotoPostagem, new Comparator<Postagem>() {
                                                                    public int compare(Postagem o1, Postagem o2) {
                                                                        return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                    }
                                                                });
                                                                adapterGridFotosPostagem.notifyDataSetChanged();
                                                            }
                                                            analisaSeguindoRef.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                } else if (postagemChildren.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                                    DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                                            .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                    analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                DatabaseReference analisaSeguindoRef = firebaseRef.child("seguindo")
                                                                        .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                                analisaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.exists()) {
                                                                            listaFotoPostagem.add(postagemChildren);
                                                                            Collections.sort(listaFotoPostagem, new Comparator<Postagem>() {
                                                                                public int compare(Postagem o1, Postagem o2) {
                                                                                    return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                                }
                                                                            });
                                                                            adapterGridFotosPostagem.notifyDataSetChanged();
                                                                        }
                                                                        analisaSeguindoRef.removeEventListener(this);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            } else {
                                                                DatabaseReference analisaSeguindoRef = firebaseRef.child("seguindo")
                                                                        .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                                analisaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.exists()) {
                                                                            listaFotoPostagem.add(postagemChildren);
                                                                            Collections.sort(listaFotoPostagem, new Comparator<Postagem>() {
                                                                                public int compare(Postagem o1, Postagem o2) {
                                                                                    return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                                }
                                                                            });
                                                                            adapterGridFotosPostagem.notifyDataSetChanged();
                                                                        }
                                                                        analisaSeguindoRef.removeEventListener(this);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                            analisaAmizadeRef.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                    postagemUsuarioRefs.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                complementoPostagemRefs.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exibirPostagens() {

        complementoPostagemRef = firebaseRef
                .child("complementoPostagem").child(idUsuarioRecebido);
        postagemUsuarioRef = firebaseRef
                .child("postagens").child(idUsuarioRecebido);

        complementoPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    try {
                        Postagem postagem = snapshot.getValue(Postagem.class);

                        if (postagem.getTotalPostagens() <= 0) {
                            recyclerPostagensPerson.setVisibility(View.GONE);
                            btnVerPostagensPerson.setVisibility(View.GONE);
                            txtViewSemPostagemPerson.setVisibility(View.VISIBLE);
                        } else {
                            txtViewSemPostagemPerson.setVisibility(View.GONE);
                            recyclerPostagensPerson.setVisibility(View.VISIBLE);
                            btnVerPostagensPerson.setVisibility(View.VISIBLE);

                            postagemUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        listaPostagens.clear();
                                        adapterGridPostagem.notifyDataSetChanged();
                                        for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                                            Postagem postagemChildren = snapshotChildren.getValue(Postagem.class);
                                            if (!postagemChildren.getTipoPostagem().equals("foto")) {
                                                if (postagemChildren.getPublicoPostagem().equals("Todos")) {
                                                    listaPostagens.add(postagemChildren);
                                                    Collections.sort(listaPostagens, new Comparator<Postagem>() {
                                                        public int compare(Postagem o1, Postagem o2) {
                                                            return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                        }
                                                    });
                                                    adapterGridPostagem.notifyDataSetChanged();
                                                } else if (postagemChildren.getPublicoPostagem().equals("Somente amigos")) {
                                                    DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                                            .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                    analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                listaPostagens.add(postagemChildren);
                                                                Collections.sort(listaPostagens, new Comparator<Postagem>() {
                                                                    public int compare(Postagem o1, Postagem o2) {
                                                                        return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                    }
                                                                });
                                                                adapterGridPostagem.notifyDataSetChanged();
                                                            }
                                                            analisaAmizadeRef.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                } else if (postagemChildren.getPublicoPostagem().equals("Somente seguidores")) {
                                                    DatabaseReference analisaSeguindoRef = firebaseRef.child("seguindo")
                                                            .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                    analisaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                listaPostagens.add(postagemChildren);
                                                                Collections.sort(listaPostagens, new Comparator<Postagem>() {
                                                                    public int compare(Postagem o1, Postagem o2) {
                                                                        return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                    }
                                                                });
                                                                adapterGridPostagem.notifyDataSetChanged();
                                                            }
                                                            analisaSeguindoRef.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                } else if (postagemChildren.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                                    DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                                            .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                    analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                DatabaseReference analisaSeguindoRef = firebaseRef.child("seguindo")
                                                                        .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                                analisaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.exists()) {
                                                                            listaPostagens.add(postagemChildren);
                                                                            Collections.sort(listaPostagens, new Comparator<Postagem>() {
                                                                                public int compare(Postagem o1, Postagem o2) {
                                                                                    return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                                }
                                                                            });
                                                                            adapterGridPostagem.notifyDataSetChanged();
                                                                        }
                                                                        analisaSeguindoRef.removeEventListener(this);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            } else {
                                                                DatabaseReference analisaSeguindoRef = firebaseRef.child("seguindo")
                                                                        .child(idUsuarioLogado).child(idUsuarioRecebido);
                                                                analisaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.exists()) {
                                                                            listaPostagens.add(postagemChildren);
                                                                            Collections.sort(listaPostagens, new Comparator<Postagem>() {
                                                                                public int compare(Postagem o1, Postagem o2) {
                                                                                    return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                                                }
                                                                            });
                                                                            adapterGridPostagem.notifyDataSetChanged();
                                                                        }
                                                                        analisaSeguindoRef.removeEventListener(this);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                            analisaAmizadeRef.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                    postagemUsuarioRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                complementoPostagemRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void verificarRelacionamento() {

        //Verifica se são amigos ou não
        verificaAmizadeRef = firebaseRef.child("friends").child(idUsuarioLogado)
                .child(usuarioSelecionado.getIdUsuario());

        eventListenerAmizade = verificaAmizadeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //São amigos
                    ocultarTodosImgBtnAmizade(imgButtonDeleteFriend);
                    imgButtonDeleteFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Remover amigo
                            desfazerAmizade();
                        }
                    });
                } else {
                    //Não são amigos

                    //Verifica se existe convite de amizade entre eles
                    verificaConvite();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removerEventListener(DatabaseReference reference, ValueEventListener valueEventListener) {
        if (valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    private void verificaConvite() {

        //Verificar se o usuário atual é o remetente do convite, se for exibe só o relógio
        //se não for o remetente então ele deve exibir o de remover o convite

        verificaConviteRef = firebaseRef.child("requestsFriendship").child(idUsuarioLogado)
                .child(usuarioSelecionado.getIdUsuario());

        eventListenerConvite = verificaConviteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Existe convite de amizade
                    Usuario usuarioConvite = snapshot.getValue(Usuario.class);
                    if (usuarioConvite.getIdRemetente().equals(idUsuarioLogado)) {
                        //Usuário atual é remetente
                        ocultarTodosImgBtnAmizade(imgButtonPendingFriend);
                    } else {
                        ocultarTodosImgBtnAmizade(imgButtonAcceptRequest);
                        imgButtonAcceptRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                aceitarAmizade();
                            }
                        });
                    }
                } else {
                    //Não existe convite de amizade
                    ocultarTodosImgBtnAmizade(imgButtonAddFriend);
                    imgButtonAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            enviarConvite();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void desfazerAmizade() {

        //Criar lógica para remover dos contatos de ambos

        desfazerAmizadeRef = firebaseRef.child("friends").child(idUsuarioLogado)
                .child(usuarioSelecionado.getIdUsuario());

        desfazerAmizadeSelecionadoRef = firebaseRef.child("friends")
                .child(usuarioSelecionado.getIdUsuario())
                .child(idUsuarioLogado);

        desfazerAmizadeRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                desfazerAmizadeSelecionadoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Amizade desfeita com sucesso", getApplicationContext());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao desfazer amizade, tente novamente mais tarde", getApplicationContext());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

        //Diminuindo contador de amizade
        dadosUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado);

        dadosUserSelecionadoRef = firebaseRef.child("usuarios")
                .child(usuarioSelecionado.getIdUsuario());

        //Diminuindo contador de amigos no usuário atual
        dadosUserAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                    int contadorAmigosAtuais = usuarioAtual.getAmigosUsuario();
                    contadorAmigosAtuais = contadorAmigosAtuais - 1;
                    dadosUserAtualRef = dadosUserAtualRef.child("amigosUsuario");
                    dadosUserAtualRef.setValue(contadorAmigosAtuais);
                }
                dadosUserAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Diminuindo contador de amigos no usuário selecionado
        dadosUserSelecionadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecebido = snapshot.getValue(Usuario.class);
                    int contadorAmigosAtuais = usuarioRecebido.getAmigosUsuario();
                    contadorAmigosAtuais = contadorAmigosAtuais - 1;
                    dadosUserSelecionadoRef = dadosUserSelecionadoRef.child("amigosUsuario");
                    dadosUserSelecionadoRef.setValue(contadorAmigosAtuais);
                }
                dadosUserSelecionadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        removerContato();
    }

    private void enviarConvite() {

        conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                .child(idUsuarioLogado).child(usuarioSelecionado.getIdUsuario());

        conviteAmizadeSelecionadoRef = firebaseRef.child("requestsFriendship")
                .child(usuarioSelecionado.getIdUsuario()).child(idUsuarioLogado);


        HashMap<String, Object> dadosConvite = new HashMap<>();
        dadosConvite.put("idRemetente", idUsuarioLogado);
        dadosConvite.put("idDestinatario", usuarioSelecionado.getIdUsuario());

        conviteAmizadeRef.setValue(dadosConvite).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                conviteAmizadeSelecionadoRef.setValue(dadosConvite).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Convite de amizade enviado com sucesso", getApplicationContext());

                        //Atualiza contador de convite de amizades para o usuário selecionado.
                        atualizarPedidosAmizade("acrescentar");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao enviar o convite de amizade, tente novamente mais tarde", getApplicationContext());
                    }
                });
            }
        });
    }

    private void atualizarPedidosAmizade(String tipoOperacao) {

        //Atualiza contador de convite de amizades
        if (tipoOperacao != null) {
            if (tipoOperacao.equals("acrescentar")) {
                dadosUserSelecionadoRef = firebaseRef.child("usuarios")
                        .child(usuarioSelecionado.getIdUsuario());
            } else {
                dadosUserSelecionadoRef = firebaseRef.child("usuarios")
                        .child(idUsuarioLogado);
            }
        }

        //Usuário que recebeu o convite e aceitou o convite logo o nó vai ser nos dados
        //do usuário atual.

        //Se for acrescentar então significa que se trata de envio de convite então o nó vai ser
        //nos dados do usuário selecionado.
        dadosUserSelecionadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecebido = snapshot.getValue(Usuario.class);
                    int contadorConvites = usuarioRecebido.getPedidosAmizade();
                    if (tipoOperacao != null) {
                        if (tipoOperacao.equals("acrescentar")) {
                            contadorConvites = contadorConvites + 1;
                        } else {
                            contadorConvites = contadorConvites - 1;
                        }
                    }
                    dadosUserSelecionadoRef = dadosUserSelecionadoRef.child("pedidosAmizade");
                    dadosUserSelecionadoRef.setValue(contadorConvites);
                } else {
                    if (tipoOperacao != null) {
                        if (tipoOperacao.equals("acrescentar")) {
                            dadosUserSelecionadoRef.setValue(1);
                        } else {
                            dadosUserSelecionadoRef.setValue(0);
                        }
                    }
                }
                dadosUserSelecionadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void aceitarAmizade() {

        conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                .child(idUsuarioLogado).child(usuarioSelecionado.getIdUsuario());

        conviteAmizadeSelecionadoRef = firebaseRef.child("requestsFriendship")
                .child(usuarioSelecionado.getIdUsuario()).child(idUsuarioLogado);

        verificaAmizadeRef = firebaseRef.child("friends").child(idUsuarioLogado)
                .child(usuarioSelecionado.getIdUsuario()).child("idUsuario");

        verificaAmizadeSelecionadoRef = firebaseRef.child("friends")
                .child(usuarioSelecionado.getIdUsuario()).child(idUsuarioLogado).child("idUsuario");

        conviteAmizadeRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                conviteAmizadeSelecionadoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Atualizando contador de convites para o usuário selecionado
                        atualizarPedidosAmizade("subtrair");
                        //Adicionando amigo
                        verificaAmizadeRef.setValue(usuarioSelecionado.getIdUsuario()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                verificaAmizadeSelecionadoRef.setValue(idUsuarioLogado).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        atualizarContadorAmizades();
                                        ToastCustomizado.toastCustomizadoCurto("Adicionado com sucesso", getApplicationContext());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao adicionar, tente novamente mais tarde", getApplicationContext());
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        adicionarContato(false, null, null);
    }

    private void atualizarContadorAmizades() {

        //Atualiza contador de amizades

        dadosUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado);

        dadosUserSelecionadoRef = firebaseRef.child("usuarios")
                .child(usuarioSelecionado.getIdUsuario());

        dadosUserAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecebido = snapshot.getValue(Usuario.class);
                    int contadorAmigos = usuarioRecebido.getAmigosUsuario();
                    contadorAmigos = contadorAmigos + 1;
                    dadosUserAtualRef = dadosUserAtualRef.child("amigosUsuario");
                    dadosUserAtualRef.setValue(contadorAmigos);
                } else {
                    dadosUserAtualRef.setValue(1);
                }
                dadosUserAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dadosUserSelecionadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecebido = snapshot.getValue(Usuario.class);
                    int contadorAmigos = usuarioRecebido.getAmigosUsuario();
                    contadorAmigos = contadorAmigos + 1;
                    dadosUserSelecionadoRef = dadosUserSelecionadoRef.child("amigosUsuario");
                    dadosUserSelecionadoRef.setValue(contadorAmigos);
                } else {
                    dadosUserSelecionadoRef.setValue(1);
                }
                dadosUserSelecionadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ocultarTodosImgBtnAmizade(ImageButton imageButton) {
        //Amizade
        imgButtonAddFriend.setVisibility(View.GONE);
        imgButtonDeleteFriend.setVisibility(View.GONE);
        //Convite
        imgButtonAcceptRequest.setVisibility(View.GONE);
        imgButtonRemoveRequest.setVisibility(View.GONE);
        imgButtonPendingFriend.setVisibility(View.GONE);

        //Exibir imageButton desejado
        imageButton.setVisibility(View.VISIBLE);
    }

    public void adicionarContato(Boolean adicionarPeloAdapter, String idRemetenteAdapter, String idUserAtual) {

        if (adicionarPeloAdapter) {
            novoContatoRef = firebaseRef.child("contatos")
                    .child(idUserAtual).child(idRemetenteAdapter);

            novoContatoSelecionadoRef = firebaseRef.child("contatos")
                    .child(idRemetenteAdapter).child(idUserAtual);

            //Contador de mensagens
            contadorMensagemRef = firebaseRef.child("contadorMensagens")
                    .child(idUserAtual).child(idRemetenteAdapter);

            contadorMensagemSelecionadoRef = firebaseRef.child("contadorMensagens")
                    .child(idRemetenteAdapter).child(idUserAtual);

            dadosContatoAtual.put("idContato", idUserAtual);
            dadosContatoAtual.put("contatoFavorito", "não");

            dadosContatoSelecionado.put("idContato", idRemetenteAdapter);
            dadosContatoSelecionado.put("contatoFavorito", "não");
        }else{
            novoContatoRef = firebaseRef.child("contatos")
                    .child(idUsuarioLogado).child(usuarioSelecionado.getIdUsuario());

            novoContatoSelecionadoRef = firebaseRef.child("contatos")
                    .child(usuarioSelecionado.getIdUsuario()).child(idUsuarioLogado);

            //Contador de mensagens
            contadorMensagemRef = firebaseRef.child("contadorMensagens")
                    .child(idUsuarioLogado).child(usuarioSelecionado.getIdUsuario());

            contadorMensagemSelecionadoRef = firebaseRef.child("contadorMensagens")
                    .child(usuarioSelecionado.getIdUsuario()).child(idUsuarioLogado);

            dadosContatoAtual.put("idContato", idUsuarioLogado);
            dadosContatoAtual.put("contatoFavorito", "não");

            dadosContatoSelecionado.put("idContato", usuarioSelecionado.getIdUsuario());
            dadosContatoSelecionado.put("contatoFavorito", "não");
        }

        //Verifica se existiu uma conversa entre os usuários antes de virarem amigos
        contadorMensagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Já existe o contador de mensagens
                    Contatos contatoSalvo = snapshot.getValue(Contatos.class);
                    dadosContatoAtual.put("nivelAmizade", contatoSalvo.getNivelAmizade());
                    dadosContatoAtual.put("totalMensagens", contatoSalvo.getTotalMensagens());
                } else {
                    //Não existe conversa entre eles
                    dadosContatoAtual.put("totalMensagens", 0);
                    dadosContatoAtual.put("nivelAmizade", "Ternura");
                }
                //Adicionando aos contatos com os dados anteriores caso existia se não, com dados novos.
                novoContatoSelecionadoRef.setValue(dadosContatoAtual);
                contadorMensagemRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Mesma lógica porém relacionado ao outro usuário.

        //Verifica se existiu uma conversa entre os usuários antes de virarem amigos
        contadorMensagemSelecionadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Já existe o contador de mensagens
                    Contatos contatoSalvo = snapshot.getValue(Contatos.class);
                    dadosContatoSelecionado.put("nivelAmizade", contatoSalvo.getNivelAmizade());
                    dadosContatoSelecionado.put("totalMensagens", contatoSalvo.getTotalMensagens());
                } else {
                    //Não existe conversa entre eles
                    dadosContatoSelecionado.put("totalMensagens", 0);
                    dadosContatoSelecionado.put("nivelAmizade", "Ternura");
                }
                //Adicionando aos contatos com os dados anteriores caso existia se não, com dados novos.
                novoContatoRef.setValue(dadosContatoSelecionado);
                contadorMensagemSelecionadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removerContato() {

        novoContatoRef = firebaseRef.child("contatos")
                .child(idUsuarioLogado).child(usuarioSelecionado.getIdUsuario());

        novoContatoSelecionadoRef = firebaseRef.child("contatos")
                .child(usuarioSelecionado.getIdUsuario()).child(idUsuarioLogado);

        novoContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Remove contato de ambos
                    novoContatoRef.removeValue();
                    novoContatoSelecionadoRef.removeValue();
                }
                novoContatoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}