package com.example.ogima.helper;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import com.example.ogima.R;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.utils.ActivityCompatHelper;

public class GlideEngineCustomizado implements ImageEngine {

    @Override
    public void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        GlideCustomizado.montarGlideCenterInside(context,
                url, imageView, android.R.color.transparent);
    }

    @Override
    public void loadImage(@NonNull Context context, @NonNull ImageView imageView, @NonNull String url, int maxWidth, int maxHeight) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        GlideCustomizado.engineCenterInside(context,
                url, imageView, android.R.color.transparent, maxWidth, maxHeight);
    }

    @Override
    public void loadAlbumCover(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        GlideCustomizado.engineAlbumCover(context,
                url, imageView, R.drawable.ps_image_placeholder, 180, 180);
    }

    @Override
    public void loadGridImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        GlideCustomizado.engineGridImage(context,
                url, imageView, R.drawable.ps_image_placeholder, 200, 200);
    }

    @Override
    public void pauseRequests(@NonNull Context context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        GlideCustomizado.eginePauseRequest(context);
    }

    @Override
    public void resumeRequests(@NonNull Context context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        GlideCustomizado.egineResumeRequest(context);
    }

    GlideEngineCustomizado() {
    }

    private static final class InstanceHolder {
        static final GlideEngineCustomizado instance = new GlideEngineCustomizado();
    }

    @NonNull
    public static GlideEngineCustomizado createGlideEngine() {
        return InstanceHolder.instance;
    }
}