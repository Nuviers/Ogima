package com.example.ogima.helper;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

public class ExoPlayerUtils {

    private ExoPlayer exoPlayer;

    public ExoPlayerUtils(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    public void pauseExoPlayer() {
        if (exoPlayer != null) {
            if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            exoPlayer.pause();
                            exoPlayer.setPlayWhenReady(false);
                            exoPlayer.removeListener(this);
                        }
                    }
                });
            } else {
                exoPlayer.pause();
                exoPlayer.setPlayWhenReady(false);
            }
        }
    }

    public void resumeExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.play();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void releaseExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
