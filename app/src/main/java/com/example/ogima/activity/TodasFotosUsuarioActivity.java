package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            posicaoOriginal, idPostagem;
    private int  posicaoRecebida;

    //Componentes
    private PhotoView imgViewFotoPostada;
    private ImageView imgViewFotoUser, imgViewUserPostador;
    private TextView txtViewDescricaoPostada,txtViewTituloPostado,
            txtViewStatusExibicao, txtViewContadorComentario;
    private ImageButton imageButtonComentario, imgButtonBackPostagem;
    private EditText edtTextComentarPostagem;
    private Button btnEnviarComentarioPostagem;
    private RecyclerView recyclerComentarioPostagem;
    private ScrollView scrollViewPostagem;
    private String localUsuario;
    private Locale localAtual;
    private DateFormat dateFormat;
    private Date date;
    private List<Postagem> listaComentariosPostados;
    private AdapterComentarios adapterComentarios;
    private String idUsuarioRecebido;
    private DatabaseReference fotoUsuarioRef;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
        adapterComentarios = new AdapterComentarios(listaComentariosPostados, getApplicationContext());
        recyclerComentarioPostagem.setAdapter(adapterComentarios);

        Bundle dados = getIntent().getExtras();
        try{
            if(dados != null){
                //Dados da exibição da postagem
                tituloPostagem = dados.getString("titulo");
                descricaoPostagem = dados.getString("descricao");
                fotoPostagem = dados.getString("foto");
                posicaoOriginal = String.valueOf(dados.getInt("posicao"));
                posicaoRecebida = Integer.parseInt(posicaoOriginal);
                idPostagem = dados.getString("idPostagem");
                idUsuarioRecebido = dados.getString("idRecebido");

                ToastCustomizado.toastCustomizadoCurto("IdRecebido " + idUsuarioRecebido,getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("IdAtual " + idUsuario,getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("IdPostagem " + idPostagem,getApplicationContext());

                //Exibindo título da postagem
                txtViewTituloPostado.setText(tituloPostagem);
                if(tituloPostagem == null || tituloPostagem.equals("")){
                    txtViewTituloPostado.setVisibility(View.GONE);
                }

                //Exibe a foto da postagem
                GlideCustomizado.montarGlideFoto(getApplicationContext(),
                        fotoPostagem, imgViewFotoPostada, android.R.color.transparent);
            }

            //Ao clicar no editText ele vai descer até o botão de enviar comentário
            edtTextComentarPostagem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(b){
                        scrollViewPostagem.smoothScrollTo(0,btnEnviarComentarioPostagem.getBottom());
                    }
                }
            });

            localAtual = getResources().getConfiguration().locale;
            localUsuario = localUsuario.valueOf(localAtual);

            //Estrutura para o adapter recuperar os dados
            DatabaseReference dadosComentariosRef = firebaseRef.child("comentarios")
                    .child(idPostagem);

            dadosComentariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Postagem postagemChildren = ds.getValue(Postagem.class);
                        //if(snapshot.getValue() != null){
                            //Postagem postagem = snapshot.getValue(Postagem.class);
                            adapterComentarios.notifyDataSetChanged();
                            listaComentariosPostados.add(postagemChildren);
                       // }
                    }

                    dadosComentariosRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }catch (Exception ex){
            ex.printStackTrace();
        }

        edtTextComentarPostagem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                txtViewContadorComentario.setText(charSequence.length() + "/7.000");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if(idUsuarioRecebido != null){
            fotoUsuarioRef = firebaseRef.child("usuarios")
                    .child(idUsuarioRecebido);
        }else{
            fotoUsuarioRef = firebaseRef.child("usuarios")
                    .child(idUsuario);
        }


        //Recuperando foto do postador
        fotoUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    if(snapshot.getValue() != null){
                        Usuario usuarioProfile = snapshot.getValue(Usuario.class);
                        String fotoUsuarioPostador = usuarioProfile.getMinhaFoto();
                        if(usuarioProfile.getMinhaFoto() != null){
                            if(usuarioProfile.getEpilepsia().equals("Sim")){
                                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), fotoUsuarioPostador, imgViewUserPostador, R.color.gph_transparent);
                            }else{
                                GlideCustomizado.montarGlide(getApplicationContext(), fotoUsuarioPostador, imgViewUserPostador, R.color.gph_transparent);
                            }
                        }
                    }
                    fotoUsuarioRef.removeEventListener(this);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference fotoUsuarioAtualRef = firebaseRef
                .child("usuarios").child(idUsuario);

        //Recuperando foto do usuário atual
        fotoUsuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    if(snapshot.getValue() != null){
                        Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                        String fotoUsuarioAtual = usuarioAtual.getMinhaFoto();
                        if(usuarioAtual.getMinhaFoto() != null){
                            if(usuarioAtual.getEpilepsia().equals("Sim")){
                                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), fotoUsuarioAtual,imgViewFotoUser, R.color.gph_transparent);
                            }else{
                                GlideCustomizado.montarGlide(getApplicationContext(), fotoUsuarioAtual, imgViewFotoUser, R.color.gph_transparent);
                            }
                        }
                    }
                    fotoUsuarioAtualRef.removeEventListener(this);
                }catch (Exception ex){
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
                finish();
            }
        });


        btnEnviarComentarioPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarComentario();
            }
        });
    }

    private void inicializandoComponentes() {
        imgViewFotoPostada = findViewById(R.id.imgViewFotoPostada);
        imgViewFotoUser = findViewById(R.id.imgViewFotoUser);
        imgViewUserPostador = findViewById(R.id.imgViewUserPostador);
        txtViewTituloPostado = findViewById(R.id.txtViewTituloPostado);
        txtViewDescricaoPostada = findViewById(R.id.txtViewDescricaoPostada);
        txtViewStatusExibicao = findViewById(R.id.txtViewStatusExibicao);
        imageButtonComentario = findViewById(R.id.imageButtonComentario);
        imgButtonBackPostagem = findViewById(R.id.imgButtonBackPostagem);
        edtTextComentarPostagem = findViewById(R.id.edtTextComentarPostagem);
        btnEnviarComentarioPostagem = findViewById(R.id.btnEnviarComentarioPostagem);
        txtViewContadorComentario = findViewById(R.id.txtViewContadorComentario);
        recyclerComentarioPostagem = findViewById(R.id.recyclerComentarioPostagem);
        scrollViewPostagem = findViewById(R.id.scrollViewPostagem);
    }

    private void enviarComentario(){

        String comentarioDigitado = edtTextComentarPostagem.getText().toString();

        DatabaseReference comentariosRef = firebaseRef.child("comentarios")
                .child(idPostagem).child(idUsuario);
        try {
            if (!comentarioDigitado.isEmpty()) {
                if (comentarioDigitado.length() <= 7000) {
                    HashMap<String, Object> dadosComentario = new HashMap<>();

                    if(localUsuario.equals("pt_BR")){
                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                        date = new Date();
                        String novaData = dateFormat.format(date);
                        dadosComentario.put("dataComentario", novaData);
                    }else{
                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                        date = new Date();
                        String novaData = dateFormat.format(date);
                        dadosComentario.put("dataComentario", novaData);
                    }

                    //Aqui se muda para o id do usuário que fez a postagem
                    dadosComentario.put("idPostagem", idPostagem);
                    dadosComentario.put("idPostador", idUsuario);
                    dadosComentario.put("comentarioPostado", comentarioDigitado);
                    comentariosRef.setValue(dadosComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //Limpa o conteúdo do editText
                                edtTextComentarPostagem.setText("");
                                //Limpa a interação do editText
                                edtTextComentarPostagem.clearFocus();

                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);

                                //scrollViewPostagem.smoothScrollTo(0,recyclerComentarioPostagem.getTop());
                                //Aqui faz a atualização do recycler
                            }
                        }
                    });
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Limite máximo de caractes atingido", getApplicationContext());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}