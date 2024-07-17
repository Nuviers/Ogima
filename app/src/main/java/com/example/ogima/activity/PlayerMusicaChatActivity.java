package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

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
    private CircleLineVisualizer audioVisualizer;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioVisualizer != null){
            audioVisualizer.release();
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
                    boolean permissionsGranted = PermissionUtils.requestGalleryPermissions(PlayerMusicaChatActivity.this);
                    if (permissionsGranted) {
                        configInicialPlay();
                    }
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
            Uri contentUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                String diretorio = String.valueOf(retornarDiretorioDestino(mensagem));
                Uri baseUri = MediaStore.Files.getContentUri("external");

                Cursor cursor = resolver.query(baseUri, null,
                        MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                        new String[]{mensagem.getNomeDocumento(), diretorio + "/"}, null);

                if (cursor != null && cursor.moveToFirst()) {
                    contentUri = ContentUris.withAppendedId(baseUri, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                    cursor.close();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Falha ao localizar arquivo", getApplicationContext());
                    return;
                }
            }else{
                File fileMidia = retornarDiretorioOrigem(mensagem);
                contentUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", fileMidia);
            }

            mediaPlayerChat.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayerChat.setDataSource(getApplicationContext(), contentUri);

            UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                @Override
                public void onConcluido(boolean epilepsia) {
                    if (!epilepsia) {
                        int audioSessionId = mediaPlayerChat.getAudioSessionId();
                        if (audioSessionId != -1)
                            audioVisualizer.setAudioSessionId(audioSessionId);
                    }
                }
                @Override
                public void onSemDado() {
                }
                @Override
                public void onError(String message) {
                }
            });
            mediaPlayerChat.prepareAsync();
        } catch (Exception ex) {
            Log.d("TESTEPLAYER", "ERRO: " + ex.getMessage());
        }
    }

    private File retornarDiretorioOrigem(Mensagem mensagem) {
        //Diretório onde está localizado o arquivo alvo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.MUSIC:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.AUDIO:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios" + File.separator + mensagem.getNomeDocumento());
            }
        } else {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.MUSIC:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.AUDIO:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios" + File.separator + mensagem.getNomeDocumento());
            }
        }
        return null;
    }

    private File retornarDiretorioDestino(Mensagem mensagem) {
        //Diretório onde está localizado o arquivo alvo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.MUSIC:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima");
                case MidiaUtils.AUDIO:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios");
            }
        } else {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.MUSIC:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima");
                case MidiaUtils.AUDIO:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios");
            }
        }
        return null;
    }

    private String retornarIdAlvo(Mensagem mensagem) {
        if (idUsuario.equals(mensagem.getIdRemetente())) {
            //Se trata de uma mídia enviada pelo usuário atual.
            return idUsuario;
        }
        return mensagem.getIdDestinatario();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.checkPermissionResult(grantResults)) {
            configInicialPlay();
        } else {
            // Permissões negadas.
            PermissionUtils.openAppSettings(PlayerMusicaChatActivity.this, getApplicationContext());
        }
    }

    private void configInicialPlay() {
        imgViewPlayMusicaChat.setVisibility(View.GONE);
        imgViewPauseMusicaChat.setVisibility(View.VISIBLE);
        mediaPlayerChat.start();
        atualizarSeekBar();
        txtViewTempoAtualMusica.setVisibility(View.VISIBLE);
        txtViewDuracaoMusica.setVisibility(View.VISIBLE);
        txtViewDuracaoMusica.setText(formatarTimer(mediaPlayerChat.getDuration()));
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
        audioVisualizer = findViewById(R.id.blast);
    }
}