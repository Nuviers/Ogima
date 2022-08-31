package com.example.ogima.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.ogima.R;
import com.example.ogima.helper.ToastCustomizado;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EdicaoVideoActivity extends AppCompatActivity {

    private ImageView videoViewPreview;
    private String uriVideoPostagem;
    private Button testarCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_video);
        inicializandoComponentes();

        testarCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startCropActivity();
            }
        });

        //Bundle dados = getIntent().getExtras();

       // if (dados != null) {
            //uriVideoPostagem = dados.getString("uriVideoPostagem");

            //if (uriVideoPostagem != null) {

                //videoViewPreview.setVideoURI(Uri.parse(uriVideoPostagem));
                //MediaController mediaController = new MediaController(this);
                //mediaController.setAnchorView(linearVideoPostagem);
                //videoViewPreview.setMediaController(mediaController);
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
               // videoViewPreview.start();
           // }
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            //CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //Uri resultUri = GetUriKt.getUri(com.canhub.cropper.CropImage.getPickImageResultUriContent(getApplicationContext(),data));
                //videoViewPreview.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Exception error = result.getError();
                //ToastCustomizado.toastCustomizadoCurto("Erro " + error,getApplicationContext());
            }
        }
    }

    private void inicializandoComponentes() {
        videoViewPreview = findViewById(R.id.videoViewPreview);
        testarCamera = findViewById(R.id.testarCamera);
    }

    private void startCropActivity(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
}