package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Usuario;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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
    private ImageView imgViewFotoUser;
    private TextView txtViewDescricaoPostada,txtViewTituloPostado,
            txtViewStatusExibicao;
    private ImageButton imageButtonComentario, imgButtonBackPostagem;

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
                //Exibindo título da postagem
                txtViewTituloPostado.setText(tituloPostagem);
                if(tituloPostagem == null || tituloPostagem.equals("")){
                    txtViewTituloPostado.setVisibility(View.GONE);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        GlideCustomizado.montarGlideFoto(getApplicationContext(),
             fotoPostagem, imgViewFotoPostada, android.R.color.transparent);

        DatabaseReference fotoUsuarioRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        fotoUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    if(snapshot.getValue() != null){
                        Usuario usuarioProfile = snapshot.getValue(Usuario.class);
                        String minhaFoto = usuarioProfile.getMinhaFoto();
                        if(usuarioProfile.getMinhaFoto() != null){
                            if(usuarioProfile.getEpilepsia().equals("Sim")){
                                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), minhaFoto, imgViewFotoUser, R.color.gph_transparent);
                            }else{
                                GlideCustomizado.montarGlide(getApplicationContext(), minhaFoto, imgViewFotoUser, R.color.gph_transparent);
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

        //Passando a descrição da postagem para o textView
        txtViewDescricaoPostada.setText(descricaoPostagem);

        imgButtonBackPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void inicializandoComponentes() {
        imgViewFotoPostada = findViewById(R.id.imgViewFotoPostada);
        imgViewFotoUser = findViewById(R.id.imgViewFotoUser);
        txtViewTituloPostado = findViewById(R.id.txtViewTituloPostado);
        txtViewDescricaoPostada = findViewById(R.id.txtViewDescricaoPostada);
        txtViewStatusExibicao = findViewById(R.id.txtViewStatusExibicao);
        imageButtonComentario = findViewById(R.id.imageButtonComentario);
        imgButtonBackPostagem = findViewById(R.id.imgButtonBackPostagem);
    }
}