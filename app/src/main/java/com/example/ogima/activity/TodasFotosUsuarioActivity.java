package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterComentarios;
import com.example.ogima.fragment.FrameSuporteInicioFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TodasFotosUsuarioActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef;
    private String tituloPostagem, descricaoPostagem, fotoPostagem,
            posicaoOriginal, idPostagem, publicoPostagem;
    private int posicaoRecebida;

    //Componentes
    private PhotoView imgViewFotoPostada, imgViewGifPostada;
    private ImageView imgViewFotoUser, imgViewUserPostador;
    private TextView txtViewDescricaoPostada, txtViewTituloPostado,
            txtViewStatusExibicao, txtViewContadorComentario, txtViewTextoPostado;
    private ImageButton imageButtonComentario, imgButtonBackPostagem,
            imgButtonLikePostagem, imgButtonDenunciarPostagem;
    private EditText edtTextComentarPostagem;
    private Button btnEnviarComentarioPostagem, btnComentariosPostagem,
            btnCurtidasPostagem;
    private RecyclerView recyclerComentarioPostagem;
    private ScrollView scrollViewPostagem;
    private String localUsuario;
    private Locale localAtual;
    private DateFormat dateFormat;
    private Date date;
    private List<Postagem> listaComentariosPostados;
    private AdapterComentarios adapterComentarios;
    private String idUsuarioRecebido;
    private DatabaseReference fotoUsuarioRef, contagemComentariosRef,
    curtirPostagemRef, atualizandoContadorComentarioRef, curtidasFotoRef,
    salvarVisualizacaoRef, atualizarContadorPostagemRef, dadosComentariosRef;
    private String idAtualExistente, donoPostagem;
    private Postagem postagemComentario, postagemViews;
    private int contagemComentario, contagemCurtidas, contagemDenuncias;
    private  Usuario usuarioProfile, usuarioMeu;

    //
    private String irParaPerfil;
    private String tipoPublicacao;
    private DatabaseReference comentariosRef;
    private DatabaseReference verificarDenunciaRef;
    private String tipoPostagem;
    private ExoPlayer exoPlayer;
    private PlayerView videoPlayerViewPostagem;

    @Override
    public void onBackPressed() {
        if(irParaPerfil != null){
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("intentPerfilFragment", "intentPerfilFragment");
            startActivity(intent);
            finish();
        }else{
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarCurtida();
        verificarDenuncia();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todas_fotos_usuario);
        inicializandoComponentes();

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        //Configurações do recycler
        recyclerComentarioPostagem.setLayoutManager(new LinearLayoutManager(this));
        listaComentariosPostados = new ArrayList<>();
        recyclerComentarioPostagem.setHasFixedSize(true);


        Bundle dados = getIntent().getExtras();
        try {
            if (dados != null) {
                //Dados da exibição da postagem
                tituloPostagem = dados.getString("titulo");
                descricaoPostagem = dados.getString("descricao");
                fotoPostagem = dados.getString("foto");
                posicaoOriginal = String.valueOf(dados.getInt("posicao"));
                posicaoRecebida = Integer.parseInt(posicaoOriginal);
                idPostagem = dados.getString("idPostagem");
                idUsuarioRecebido = dados.getString("idRecebido");
                donoPostagem = dados.getString("donoPostagem");
                publicoPostagem = dados.getString("publicoPostagem");
                irParaPerfil = dados.getString("irParaPerfil");
                tipoPublicacao = dados.getString("tipoPublicacao");
                tipoPostagem = dados.getString("tipoPostagem");

                if (tituloPostagem == null || tituloPostagem.isEmpty()) {
                    txtViewTituloPostado.setVisibility(View.GONE);
                }else{
                    //Exibindo título da postagem
                    txtViewTituloPostado.setVisibility(View.VISIBLE);
                    txtViewTituloPostado.setText(tituloPostagem);
                }

                if (tipoPostagem != null) {
                    if (tipoPostagem.equals("texto")) {
                        videoPlayerViewPostagem.setVisibility(View.GONE);
                        imgViewGifPostada.setVisibility(View.GONE);
                        imgViewFotoPostada.setVisibility(View.GONE);
                        txtViewTextoPostado.setVisibility(View.VISIBLE);
                        txtViewTextoPostado.setText(descricaoPostagem);
                    } else if(tipoPostagem.equals("imagem") || tipoPostagem.equals("foto")){
                        videoPlayerViewPostagem.setVisibility(View.GONE);
                        imgViewGifPostada.setVisibility(View.GONE);
                        imgViewFotoPostada.setVisibility(View.VISIBLE);
                        GlideCustomizado.montarGlideFoto(getApplicationContext(),
                                fotoPostagem, imgViewFotoPostada, android.R.color.transparent);
                    }else if (tipoPostagem.equals("gif")){
                        videoPlayerViewPostagem.setVisibility(View.GONE);
                        imgViewFotoPostada.setVisibility(View.GONE);
                        imgViewGifPostada.setVisibility(View.VISIBLE);
                        GlideCustomizado.montarGlideCenterInside(getApplicationContext(),
                                fotoPostagem, imgViewGifPostada, android.R.color.transparent);
                    }else if (tipoPostagem.equals("video")){
                        imgViewFotoPostada.setVisibility(View.GONE);
                        imgViewGifPostada.setVisibility(View.GONE);
                        videoPlayerViewPostagem.setVisibility(View.VISIBLE);
                        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
                        videoPlayerViewPostagem.setPlayer(exoPlayer);
                        MediaItem mediaItem = MediaItem.fromUri(fotoPostagem);
                        exoPlayer.addMediaItem(mediaItem);
                        exoPlayer.prepare();
                        exoPlayer.setPlayWhenReady(false);
                    }
                }else{
                    videoPlayerViewPostagem.setVisibility(View.GONE);
                    imgViewGifPostada.setVisibility(View.GONE);
                    imgViewFotoPostada.setVisibility(View.VISIBLE);
                    //Exibe a foto da postagem
                    GlideCustomizado.montarGlideFoto(getApplicationContext(),
                            fotoPostagem, imgViewFotoPostada, android.R.color.transparent);
                }
            }

           usuarioRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if(snapshot.getValue() != null){
                       usuarioMeu = snapshot.getValue(Usuario.class);

                       adapterComentarios = new AdapterComentarios(listaComentariosPostados, getApplicationContext(), donoPostagem, usuarioMeu, idPostagem);

                       recyclerComentarioPostagem.setAdapter(adapterComentarios);
                   }
                   usuarioRef.removeEventListener(this);
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });

            //Evento de clique ao clicar para denunciar a postagem
            imgButtonDenunciarPostagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    denunciarPostagem();
                }
            });


            if(tipoPublicacao != null){
                curtidasFotoRef = firebaseRef.child("curtidasPostagem")
                        .child(idPostagem).child(idUsuario);
                dadosComentariosRef = firebaseRef.child("comentariosPostagem")
                        .child(idPostagem);
            }

            //Ao clicar no editText ele vai descer até o botão de enviar comentário
            edtTextComentarPostagem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        scrollViewPostagem.smoothScrollTo(0, btnEnviarComentarioPostagem.getBottom());
                    }
                }
            });

            localAtual = getResources().getConfiguration().locale;
            localUsuario = localUsuario.valueOf(localAtual);

            //Estrutura para o adapter recuperar os dados

            dadosComentariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Postagem postagemChildren = ds.getValue(Postagem.class);

                        if (postagemChildren.getIdUsuarioInterativo().equals(idUsuario)) {
                            edtTextComentarPostagem.setVisibility(View.GONE);
                            btnEnviarComentarioPostagem.setVisibility(View.GONE);
                            txtViewContadorComentario.setVisibility(View.GONE);
                            imgViewFotoUser.setVisibility(View.GONE);
                            listaComentariosPostados.add(0, postagemChildren);
                            adapterComentarios.notifyDataSetChanged();
                            idAtualExistente = "sim";
                            continue;
                        }

                        listaComentariosPostados.add(postagemChildren);
                        if (idAtualExistente != null && listaComentariosPostados.size() > 1) {
                            //Ordena a partir da posição 1 da lista se existir
                            //algum comentário do usuário atual
                            Collections.sort(listaComentariosPostados.subList(1, listaComentariosPostados.size()), Postagem.PostagemComentarioDS);
                            //Ordena a lista inteira caso não possua comentário
                            //do usuário atual
                        } else {
                            Collections.sort(listaComentariosPostados, Postagem.PostagemComentarioDS);
                        }
                        adapterComentarios.notifyDataSetChanged();
                    }
                    dadosComentariosRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        edtTextComentarPostagem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                txtViewContadorComentario.setText(charSequence.length() + "/300");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (idUsuarioRecebido != null) {

            fotoUsuarioRef = firebaseRef.child("usuarios")
                    .child(idUsuarioRecebido);

            if(tipoPublicacao != null){
                contagemComentariosRef = firebaseRef
                        .child("postagens").child(idUsuarioRecebido).child(idPostagem);
                curtirPostagemRef = firebaseRef
                        .child("postagens").child(idUsuarioRecebido)
                        .child(idPostagem).child("totalCurtidasPostagem");
                atualizandoContadorComentarioRef = firebaseRef
                        .child("postagens").child(idUsuarioRecebido).child(idPostagem)
                        .child("totalComentarios");
                salvarVisualizacaoRef = firebaseRef
                        .child("visualizacoesPostagem")
                        .child(idPostagem).child(idUsuarioRecebido).child(idUsuario);
                atualizarContadorPostagemRef = firebaseRef
                        .child("postagens").child(idUsuarioRecebido)
                        .child(idPostagem);
            }

            salvarVisualizacaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() != null){
                        postagemViews = snapshot.getValue(Postagem.class);
                    }else{
                        salvarVisualizacaoRef = salvarVisualizacaoRef.child("idUsuarioInterativo");
                        salvarVisualizacaoRef.setValue(idUsuario);

                        atualizarContadorPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.getValue() != null){
                                    Postagem postagemViews = snapshot.getValue(Postagem.class);
                                    if(postagemViews.getTotalViewsFotoPostagem() <= 0){
                                        atualizarContadorPostagemRef = atualizarContadorPostagemRef.child("totalViewsFotoPostagem");
                                        atualizarContadorPostagemRef.setValue(1);
                                    }else{
                                        atualizarContadorPostagemRef = atualizarContadorPostagemRef.child("totalViewsFotoPostagem");
                                        int viewsTotaisPostagem = postagemViews.getTotalViewsFotoPostagem() + 1;
                                        atualizarContadorPostagemRef.setValue(viewsTotaisPostagem);

                                    }
                                }
                                atualizarContadorPostagemRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    salvarVisualizacaoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {

            imgButtonDenunciarPostagem.setVisibility(View.GONE);

            fotoUsuarioRef = firebaseRef.child("usuarios")
                    .child(idUsuario);

            if(tipoPublicacao != null){
                contagemComentariosRef = firebaseRef
                        .child("postagens").child(idUsuario).child(idPostagem);
                curtirPostagemRef = firebaseRef
                        .child("postagens").child(idUsuario)
                        .child(idPostagem).child("totalCurtidasPostagem");
                atualizandoContadorComentarioRef = firebaseRef
                        .child("postagens").child(idUsuario).child(idPostagem)
                        .child("totalComentarios");
            }
        }


        //Recuperando foto do postador
        fotoUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.getValue() != null) {
                        usuarioProfile = snapshot.getValue(Usuario.class);
                        String fotoUsuarioPostador = usuarioProfile.getMinhaFoto();
                        if (usuarioProfile.getMinhaFoto() != null) {
                            if (usuarioProfile.isStatusEpilepsia()) {
                                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), fotoUsuarioPostador, imgViewUserPostador, R.color.gph_transparent);
                            } else {
                                GlideCustomizado.montarGlide(getApplicationContext(), fotoUsuarioPostador, imgViewUserPostador, R.color.gph_transparent);
                            }
                        }else{
                            imgViewUserPostador.setImageResource(R.drawable.avatarfemale);
                        }
                    }
                    fotoUsuarioRef.removeEventListener(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(idUsuarioRecebido != null && !idUsuarioRecebido.equals(idUsuario)){
            imgViewUserPostador.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Desativado intent para parar de duplica a mesma activity
                    //Intent intent = new Intent(getApplicationContext(), PersonProfileActivity.class);
                    //intent.putExtra("usuarioSelecionado", usuarioProfile);
                    //startActivity(intent);
                    finish();
                }
            });
        }

        DatabaseReference fotoUsuarioAtualRef = firebaseRef
                .child("usuarios").child(idUsuario);

        //Recuperando foto do usuário atual
        fotoUsuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                        String fotoUsuarioAtual = usuarioAtual.getMinhaFoto();
                        if (usuarioAtual.getMinhaFoto() != null) {
                            if (usuarioAtual.isStatusEpilepsia()) {
                                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), fotoUsuarioAtual, imgViewFotoUser, R.color.gph_transparent);
                            } else {
                                GlideCustomizado.montarGlide(getApplicationContext(), fotoUsuarioAtual, imgViewFotoUser, R.color.gph_transparent);
                            }
                        }else{
                            imgViewFotoUser.setImageResource(R.drawable.avatarfemale);
                        }
                    }
                    fotoUsuarioAtualRef.removeEventListener(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Passando a descrição da postagem para o textView
        txtViewDescricaoPostada.setText(descricaoPostagem);

        imgButtonBackPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });

        //Evento de clique do imgButton que tem a função de curtir postagem.
        imgButtonLikePostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curtirPostagem();
            }
        });

        btnEnviarComentarioPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarComentario();
            }
        });

        contagemComentariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    postagemComentario = snapshot.getValue(Postagem.class);
                    contagemComentario = postagemComentario.getTotalComentarios();
                    contagemCurtidas = postagemComentario.getTotalCurtidasPostagem();

                    verificarCurtida();

                    if (contagemComentario > 1) {
                        btnComentariosPostagem.setText(String.valueOf(contagemComentario) + " comentários");
                    } else {
                        btnComentariosPostagem.setText(String.valueOf(contagemComentario) + " comentário");
                    }
                    if (contagemCurtidas > 1) {
                        btnCurtidasPostagem.setText(String.valueOf(contagemCurtidas) + " curtidas");
                    } else {
                        btnCurtidasPostagem.setText(String.valueOf(contagemCurtidas) + " curtida");
                    }

                    if (contagemComentario <= 0) {
                        btnComentariosPostagem.setText("Sem comentários");
                    }
                    if (contagemCurtidas <= 0) {
                        btnCurtidasPostagem.setText("Sem curtidas");
                    }

                    contagemComentario = contagemComentario + 1;
                    contagemCurtidas = contagemCurtidas + 1;

                } else {
                    btnComentariosPostagem.setText("Sem comentários");
                    btnCurtidasPostagem.setText("Sem curtidas");
                }
                contagemComentariosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnComentariosPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contagemComentario > 0) {
                    scrollViewPostagem.smoothScrollTo(0, recyclerComentarioPostagem.getBottom());
                }
            }
        });
    }

    private void inicializandoComponentes() {
        imgViewFotoPostada = findViewById(R.id.imgViewFotoPostada);
        imgViewGifPostada = findViewById(R.id.imgViewGifPostada);
        videoPlayerViewPostagem = findViewById(R.id.videoPlayerViewPostagem);
        imgViewFotoUser = findViewById(R.id.imgViewFotoUser);
        imgViewUserPostador = findViewById(R.id.imgViewUserPostador);
        txtViewTituloPostado = findViewById(R.id.txtViewTituloPostado);
        txtViewTextoPostado = findViewById(R.id.txtViewTextoPostado);
        txtViewDescricaoPostada = findViewById(R.id.txtViewDescricaoPostada);
        txtViewStatusExibicao = findViewById(R.id.txtViewStatusExibicao);
        imageButtonComentario = findViewById(R.id.imageButtonComentario);
        imgButtonBackPostagem = findViewById(R.id.imgButtonBackPostagem);
        edtTextComentarPostagem = findViewById(R.id.edtTextComentarPostagem);
        btnEnviarComentarioPostagem = findViewById(R.id.btnEnviarComentarioPostagem);
        txtViewContadorComentario = findViewById(R.id.txtViewContadorComentario);
        recyclerComentarioPostagem = findViewById(R.id.recyclerComentarioPostagem);
        scrollViewPostagem = findViewById(R.id.scrollViewPostagem);
        imgButtonLikePostagem = findViewById(R.id.imgButtonLikePostagem);
        btnComentariosPostagem = findViewById(R.id.btnComentariosPostagem);
        btnCurtidasPostagem = findViewById(R.id.btnCurtidasPostagem);
        imgButtonDenunciarPostagem = findViewById(R.id.imgButtonDenunciarPostagem);
    }

    private void curtirPostagem() {
        if (contagemCurtidas <= 0) {
            contagemCurtidas = 1;
            curtirPostagemRef.setValue(contagemCurtidas).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                }
            });
        } else {
            curtirPostagemRef.setValue(contagemCurtidas).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                }
            });
        }

        HashMap<String, Object> dadosCurtida = new HashMap<>();

        if (localUsuario.equals("pt_BR")) {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
            date = new Date();
            String novaData = dateFormat.format(date);
            dadosCurtida.put("dataCurtidaPostagem", novaData);
        } else {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
            date = new Date();
            String novaData = dateFormat.format(date);
            dadosCurtida.put("dataCurtidaPostagem", novaData);
        }

        dadosCurtida.put("idPostagem", idPostagem);
        dadosCurtida.put("idUsuarioInterativo", idUsuario);
        if(idUsuarioRecebido != null){
            dadosCurtida.put("idDonoPostagem", idUsuarioRecebido);
        }
        curtidasFotoRef.setValue(dadosCurtida);

    }

    private void enviarComentario() {

        String comentarioDigitado = edtTextComentarPostagem.getText().toString();

        if(tipoPublicacao != null){
            comentariosRef = firebaseRef.child("comentariosPostagem")
                    .child(idPostagem).child(idUsuario);
        }
        
        HashMap<String, Object> dadosComentario = new HashMap<>();

        try {
            if (contagemComentario <= 0) {
                contagemComentario = 1;
            }

            if (!comentarioDigitado.isEmpty()) {
                if (comentarioDigitado.length() <= 300) {

                    if (localUsuario.equals("pt_BR")) {
                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                        date = new Date();
                        String novaData = dateFormat.format(date);
                        dadosComentario.put("dataComentario", novaData);
                    } else {
                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                        date = new Date();
                        String novaData = dateFormat.format(date);
                        dadosComentario.put("dataComentario", novaData);
                    }

                    //Aqui se muda para o id do usuário que fez a postagem
                    dadosComentario.put("idPostagem", idPostagem);
                    dadosComentario.put("idUsuarioInterativo", idUsuario);
                    if(idUsuarioRecebido != null){
                        dadosComentario.put("idDonoPostagem", idUsuarioRecebido);
                    }
                    dadosComentario.put("totalCurtidasComentario", 0);
                    dadosComentario.put("comentarioPostado", comentarioDigitado);
                    dadosComentario.put("ocultarComentario", "não");
                    dadosComentario.put("totalDenunciasComentario", 0);
                    comentariosRef.setValue(dadosComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                atualizandoContadorComentarioRef.setValue(contagemComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //Limpa o conteúdo do editText
                                            edtTextComentarPostagem.setText("");
                                            //Limpa a interação do editText
                                            edtTextComentarPostagem.clearFocus();

                                            finish();
                                            overridePendingTransition(0, 0);
                                            startActivity(getIntent());
                                            overridePendingTransition(0, 0);
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Limite máximo de caractes atingido", getApplicationContext());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void verificarCurtida() {
        try {
            curtidasFotoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() != null){
                        imgButtonLikePostagem.setImageResource(R.drawable.ic_heartpreenchido);
                        imgButtonLikePostagem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TodasFotosUsuarioActivity.this);
                                builder.setTitle("Deseja remover sua curtida da postagem?");
                                builder.setMessage("Desfazer curtida");
                                builder.setCancelable(true);
                                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                       curtidasFotoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if(task.isSuccessful()){
                                                   int atualizarCurtida = postagemComentario.getTotalCurtidasPostagem() - 1;
                                                   curtirPostagemRef.setValue(atualizarCurtida).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if(task.isSuccessful()){
                                                               finish();
                                                               overridePendingTransition(0, 0);
                                                               startActivity(getIntent());
                                                               overridePendingTransition(0, 0);
                                                           }
                                                       }
                                                   });
                                               }
                                           }
                                       });
                                    }
                                });
                                builder.setNegativeButton("Cancelar", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }
                    curtidasFotoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Intent para activity responsável pela exibição
            //das curtidas
            btnCurtidasPostagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), CurtidasPostagemActivity.class);
                    intent.putExtra("idPostagem", idPostagem);
                    if(tipoPublicacao != null){
                        intent.putExtra("tipoPublicacao", tipoPublicacao);
                    }
                    startActivity(intent);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void denunciarPostagem(){

        AlertDialog.Builder builder = new AlertDialog.Builder(TodasFotosUsuarioActivity.this);
        builder.setTitle("Deseja denunciar essa postagem?");
        builder.setMessage("Prosseguir com a denúncia");
        builder.setCancelable(true);
        builder.setPositiveButton("Denunciar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getApplicationContext(), DenunciaPostagemActivity.class);
                intent.putExtra("numeroDenuncias", postagemComentario.getTotalDenunciasPostagem());
                intent.putExtra("idDonoPostagem", idUsuarioRecebido);
                intent.putExtra("idPostagem", idPostagem);
                if(tipoPublicacao != null){
                    intent.putExtra("tipoPublicacao", tipoPublicacao);
                }
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void verificarDenuncia(){

        if(tipoPublicacao != null){
            verificarDenunciaRef = firebaseRef
                    .child("postagensDenunciadas").child(idPostagem);
        }

        verificarDenunciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapChildren : snapshot.getChildren()){
                    Postagem postagemDenuncia = snapChildren.getValue(Postagem.class);
                    if(postagemDenuncia.getIdDenunciador().equals(idUsuario)){
                        imgButtonDenunciarPostagem.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}