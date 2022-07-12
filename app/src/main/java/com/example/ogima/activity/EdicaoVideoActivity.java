package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.ogima.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

public class EdicaoVideoActivity extends AppCompatActivity {

    private VideoView videoViewPreview;
    private String uriVideoPostagem;
    private LinearLayout linearVideoPostagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_video);
        inicializandoComponentes();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            uriVideoPostagem = dados.getString("uriVideoPostagem");

            if (uriVideoPostagem != null) {

                videoViewPreview.setVideoURI(Uri.parse(uriVideoPostagem));
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(linearVideoPostagem);
                videoViewPreview.setMediaController(mediaController);
                /*
                videoViewPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.setVolume(0,0);
                    }
                });
                videoViewPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.setVolume(0,0);
                    }
                });
                 */
                videoViewPreview.start();
            }
        }
    }

    private void inicializandoComponentes() {
        videoViewPreview = findViewById(R.id.videoViewPreview);
        linearVideoPostagem = findViewById(R.id.linearVideoPostagem);
    }
}