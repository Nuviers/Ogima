package com.example.ogima.model;

import java.io.Serializable;

public class Wallpaper implements Serializable {

    private String urlWallpaper;
    private String urlGlobalWallpaper;
    private String wallpaperGlobal;

    public Wallpaper() {
    }

    public String getUrlWallpaper() {
        return urlWallpaper;
    }

    public void setUrlWallpaper(String urlWallpaper) {
        this.urlWallpaper = urlWallpaper;
    }

    public String getUrlGlobalWallpaper() {
        return urlGlobalWallpaper;
    }

    public void setUrlGlobalWallpaper(String urlGlobalWallpaper) {
        this.urlGlobalWallpaper = urlGlobalWallpaper;
    }

    public String getWallpaperGlobal() {
        return wallpaperGlobal;
    }

    public void setWallpaperGlobal(String wallpaperGlobal) {
        this.wallpaperGlobal = wallpaperGlobal;
    }
}
