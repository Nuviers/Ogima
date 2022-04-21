package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

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
    private TextView txtViewDescricaoPostada,txtViewTituloPostado,
            txtViewStatusExibicao;
    private CollapsingToolbarLayout collapsingToolbarPostada;
    private ImageButton imageButtonComentario;
    private AppBarLayout appBarLayoutPostagem;

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
                    appBarLayoutPostagem.setPadding(0,100,0,0);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        GlideCustomizado.montarGlideFoto(getApplicationContext(),
             fotoPostagem, imgViewFotoPostada, android.R.color.transparent);

        //Passando a descrição da postagem para o textView
        txtViewDescricaoPostada.setText(descricaoPostagem);

    }

    private void inicializandoComponentes() {
        imgViewFotoPostada = findViewById(R.id.imgViewFotoPostada);
        txtViewTituloPostado = findViewById(R.id.txtViewTituloPostado);
        txtViewDescricaoPostada = findViewById(R.id.txtViewDescricaoPostada);
        txtViewStatusExibicao = findViewById(R.id.txtViewStatusExibicao);
        //collapsingToolbarPostada = findViewById(R.id.collapsingToolbarPostada);
        imageButtonComentario = findViewById(R.id.imageButtonComentario);
        //appBarLayoutPostagem = findViewById(R.id.appBarLayoutPostagem);
    }
}