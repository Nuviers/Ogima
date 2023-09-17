package com.example.ogima.model;

import java.io.Serializable;

public class Wallpaper implements Serializable {

    private String urlWallpaper;
    private String nomeWallpaper;

    public Wallpaper() {
    }

    public String getNomeWallpaper() {
        return nomeWallpaper;
    }

    public void setNomeWallpaper(String nomeWallpaper) {
        this.nomeWallpaper = nomeWallpaper;
    }

    public String getUrlWallpaper() {
        return urlWallpaper;
    }

    public void setUrlWallpaper(String urlWallpaper) {
        this.urlWallpaper = urlWallpaper;
    }

}
