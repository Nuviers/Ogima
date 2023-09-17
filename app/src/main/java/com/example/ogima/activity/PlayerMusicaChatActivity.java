package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.Permissao;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class PlayerMusicaChatActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Mensagem mensagem;
    private TextView txtTituloToolbarChatMusica, txtViewTempoAtualMusica,
            txtViewDuracaoMusica;
    private Toolbar toolbarChatMusica;
    private ImageButton imgBtnBackChatMusica;
    private ImageView imgViewGifMusica, imgViewPlayMusicaChat,
            imgViewPauseMusicaChat, imgViewStopMusicaChat, imgViewCapaMusica;
    private SeekBar seekBarControlMusicaChat;
    private MediaPlayer mediaPlayerChat;
    private Handler handler = new Handler();
    private Runnable runnable;
    private Usuario usuarioAtual;
    private File caminhoDestino;

    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayerChat != null) {
            if (mediaPlayerChat.isPlaying()) {
                seekBarControlMusicaChat.setProgress(0);
                imgViewPauseMusicaChat.setVisibility(View.GONE);
                imgViewPlayMusicaChat.setVisibility(View.VISIBLE);
                txtViewTempoAtualMusica.setText("0:00");
                txtViewDuracaoMusica.setText("0:00");
                mediaPlayerChat.stop();
                mediaPlayerChat.reset();
                prepararMediaPlayer();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_musica_chat);
        inicializandoComponentes();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Validar permissões necessárias para adição de fotos.
        Permissao.validarPermissoes(permissoesNecessarias, PlayerMusicaChatActivity.this, 22);

        toolbarChatMusica.setTitle("");

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            mensagem = (Mensagem) dados.getSerializable("audio");

            txtTituloToolbarChatMusica.setText(mensagem.getNomeDocumento());

            mediaPlayerChat = new MediaPlayer();

            seekBarControlMusicaChat.setMax(100);

            imgViewPlayMusicaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Solicitar permissão de uso
                    //exibeGifWave();
                    //Solicitar permissão de uso
                    imgViewPlayMusicaChat.setVisibility(View.GONE);
                    imgViewPauseMusicaChat.setVisibility(View.VISIBLE);
                    mediaPlayerChat.start();
                    atualizarSeekBar();
                    txtViewTempoAtualMusica.setVisibility(View.VISIBLE);
                    txtViewDuracaoMusica.setVisibility(View.VISIBLE);
                    txtViewDuracaoMusica.setText(formatarTimer(mediaPlayerChat.getDuration()));
                }
            });

            imgViewPauseMusicaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgViewPauseMusicaChat.setVisibility(View.GONE);
                    imgViewPlayMusicaChat.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(updater);
                    mediaPlayerChat.pause();
                }
            });

            imgViewStopMusicaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mediaPlayerChat.isPlaying()) {
                        seekBarControlMusicaChat.setProgress(0);
                        imgViewPauseMusicaChat.setVisibility(View.GONE);
                        imgViewPlayMusicaChat.setVisibility(View.VISIBLE);
                        txtViewTempoAtualMusica.setText("0:00");
                        txtViewDuracaoMusica.setText("0:00");
                        mediaPlayerChat.stop();
                        mediaPlayerChat.reset();
                        prepararMediaPlayer();
                    }
                }
            });

            prepararMediaPlayer();

            seekBarControlMusicaChat.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    SeekBar seekBar = (SeekBar) view;
                    int playPosition = (mediaPlayerChat.getDuration() / 100) *
                            seekBar.getProgress();
                    mediaPlayerChat.seekTo(playPosition);
                    txtViewTempoAtualMusica.setText(formatarTimer(mediaPlayerChat.getCurrentPosition()));
                    return false;
                }
            });

            mediaPlayerChat.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    seekBarControlMusicaChat.setSecondaryProgress(i);
                }
            });

            mediaPlayerChat.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    seekBarControlMusicaChat.setProgress(0);
                    imgViewPauseMusicaChat.setVisibility(View.GONE);
                    imgViewPlayMusicaChat.setVisibility(View.VISIBLE);
                    txtViewTempoAtualMusica.setText("0:00");
                    txtViewDuracaoMusica.setText("0:00");
                    mediaPlayerChat.reset();
                    prepararMediaPlayer();
                }
            });

            imgBtnBackChatMusica.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mediaPlayerChat != null) {
                        if (mediaPlayerChat.isPlaying()) {
                            seekBarControlMusicaChat.setProgress(0);
                            imgViewPauseMusicaChat.setVisibility(View.GONE);
                            imgViewPlayMusicaChat.setVisibility(View.VISIBLE);
                            txtViewTempoAtualMusica.setText("0:00");
                            txtViewDuracaoMusica.setText("0:00");
                            mediaPlayerChat.stop();
                            mediaPlayerChat.reset();
                            prepararMediaPlayer();
                        }
                    }
                    onBackPressed();
                }
            });
        }
    }

    @SuppressLint("DefaultLocale")
    private String convertFormat(int duracaoMusica) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duracaoMusica),
                TimeUnit.MILLISECONDS.toSeconds(duracaoMusica) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duracaoMusica)));
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            atualizarSeekBar();
            long currentDuration = mediaPlayerChat.getCurrentPosition();
            txtViewTempoAtualMusica.setText(formatarTimer(currentDuration));
        }
    };

    private void prepararMediaPlayer() {
        try {
            mediaPlayerChat.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayerChat.setDataSource(executarMusica(mensagem));
            mediaPlayerChat.prepareAsync();
            mediaPlayerChat.prepare();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String executarMusica(Mensagem mensagemRecebida) {

        if (mensagemRecebida.getTipoMensagem().equals("musica")) {
            caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemRecebida.getIdDestinatario() + File.separator + "musicas");
        } else if (mensagemRecebida.getTipoMensagem().equals("audio")) {
            caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemRecebida.getIdDestinatario() + File.separator + "audios");
        }
        File file = new File(caminhoDestino, mensagemRecebida.getNomeDocumento());
        return file.getPath();
    }

    private String formatarTimer(long milliSeconds) {
        String timerString = "";
        String secondString;

        int hours = (int) (milliSeconds / (1000 * 60 * 60));
        int minutes = (int) (milliSeconds % (1000 * 60 * 60) / (1000 * 60));
        int seconds = (int) (milliSeconds % (1000 * 60 * 60) % (1000 * 60) / 1000);

        if (hours > 0) {
            timerString = hours + ":";
        }

        if (seconds < 10) {
            secondString = "0" + seconds;
        } else {
            secondString = "" + seconds;
        }

        timerString = timerString + minutes + ":" + secondString;

        return timerString;
    }

    private void atualizarSeekBar() {
        if (mediaPlayerChat.isPlaying()) {
            seekBarControlMusicaChat.setProgress((int) (((float) mediaPlayerChat.getCurrentPosition()
                    / mediaPlayerChat.getDuration()) * 100));
            handler.postDelayed(updater, 1000);
        }
    }

    private void inicializandoComponentes() {
        txtTituloToolbarChatMusica = findViewById(R.id.txtTituloToolbarChatMusica);
        txtViewTempoAtualMusica = findViewById(R.id.txtViewTempoAtualMusica);
        txtViewDuracaoMusica = findViewById(R.id.txtViewDuracaoMusica);
        toolbarChatMusica = findViewById(R.id.toolbarChatMusica);
        imgBtnBackChatMusica = findViewById(R.id.imgBtnBackChatMusica);
        imgViewGifMusica = findViewById(R.id.imgViewGifMusica);
        imgViewPlayMusicaChat = findViewById(R.id.imgViewPlayMusicaChat);
        imgViewPauseMusicaChat = findViewById(R.id.imgViewPauseMusicaChat);
        imgViewStopMusicaChat = findViewById(R.id.imgViewStopMusicaChat);
        imgViewCapaMusica = findViewById(R.id.imgViewCapaMusica);
        seekBarControlMusicaChat = findViewById(R.id.seekBarControlMusicaChat);
    }
}