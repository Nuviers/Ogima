package com.example.ogima.helper;

import com.google.android.exoplayer2.ExoPlayer;

public class ExoPlayerUnicoUtils {
    private ExoPlayer exoPlayer;

    public ExoPlayerUnicoUtils(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    public void resumeExo(){
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
            exoPlayer.seekTo(exoPlayer.getCurrentPosition());
        }
    }

    public void pauseExo(){
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.getPlaybackState();
        }
    }

    public void releaseExo(){
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
}
