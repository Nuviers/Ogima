package com.example.ogima.model;

import com.google.android.exoplayer2.ExoPlayer;

public class ExoPlayerItem {
    public ExoPlayer exoPlayer;
    public int position;

    public ExoPlayerItem(ExoPlayer exoPlayer, int position) {
        this.exoPlayer = exoPlayer;
        this.position = position;
    }
}
