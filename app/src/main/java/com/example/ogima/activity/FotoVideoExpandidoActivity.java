package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Mensagem;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class FotoVideoExpandidoActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private PhotoView photoViewFotoExpandida;
    private Toolbar toolbarExpandido;
    private ImageButton imgBtnBackExpandido;
    private StyledPlayerView videoExpandido;
    private Mensagem mensagem;
    private ExoPlayer exoPlayerExpandido;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayerExpandido != null) {
            startPlayer();
            seekTo();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayerExpandido != null) {
            pausePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayerExpandido != null) {
            releasePlayer();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_video_expandido);
        inicializandoComponentes();
        toolbarExpandido.setTitle("");

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            mensagem = (Mensagem) dados.getSerializable("mensagem");

            if (mensagem.getTipoMensagem().equals("imagem")) {
                //Exibe foto expandida e com zoom implementado.
                exibirFoto();
            } else if (mensagem.getTipoMensagem().equals("video")) {
                //Exibe o video em tela cheia.
                exibirVideo();
            }
        }

        imgBtnBackExpandido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void exibirVideo() {
        videoExpandido.setVisibility(View.VISIBLE);
        exoPlayerExpandido = new ExoPlayer.Builder(getApplicationContext()).build();
        videoExpandido.setPlayer(exoPlayerExpandido);
        MediaItem mediaItem = MediaItem.fromUri(mensagem.getConteudoMensagem());
        exoPlayerExpandido.addMediaItem(mediaItem);
        exoPlayerExpandido.prepare();
        exoPlayerExpandido.setPlayWhenReady(true);
    }

    private void exibirFoto() {
        photoViewFotoExpandida.setVisibility(View.VISIBLE);
        GlideCustomizado.montarGlideFoto(getApplicationContext(),
                mensagem.getConteudoMensagem(),
                photoViewFotoExpandida,
                android.R.color.transparent);
    }

    public void startPlayer() {
        exoPlayerExpandido.setPlayWhenReady(true);
        exoPlayerExpandido.getPlaybackState();
    }

    public void seekTo() {
        exoPlayerExpandido.seekTo(exoPlayerExpandido.getCurrentPosition());
    }

    public void pausePlayer() {
        exoPlayerExpandido.setPlayWhenReady(false);
        exoPlayerExpandido.getPlaybackState();
    }

    public void releasePlayer() {
        exoPlayerExpandido.release();
    }

    private void inicializandoComponentes() {
        photoViewFotoExpandida = findViewById(R.id.photoViewFotoExpandida);
        toolbarExpandido = findViewById(R.id.toolbarExpandido);
        imgBtnBackExpandido = findViewById(R.id.imgBtnBackExpandido);
        videoExpandido = findViewById(R.id.videoExpandido);
    }

}