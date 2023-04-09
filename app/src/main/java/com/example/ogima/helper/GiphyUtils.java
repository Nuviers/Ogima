package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.BuildConfig;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.Locale;
import java.util.Objects;

public class GiphyUtils{

    private GiphyDialogFragment gdl;
    private String gif_url_P, gif_url_M, gif_url_O;
    private Image imageP, imageM, imageO;

    public interface GifSelectionListener {
         void onGifSelected(String gifPequena, String gifMedio, String gifOriginal);
    }

    public void selectGif(Context context, GifSelectionListener listener) {

        Giphy.INSTANCE.configure(context, BuildConfig.SEND_GIPHY_ACCESS, false);

        GPHSettings settings = new GPHSettings();

        settings.setTheme(GPHTheme.Dark);
        settings.setMediaTypeConfig(new GPHContentType[]{GPHContentType.gif, GPHContentType.recents});

        gdl = GiphyDialogFragment.Companion.newInstance(settings);
        gdl.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
            @Override
            public void onGifSelected(@NonNull Media media, @Nullable String s, @NonNull GPHContentType gphContentType) {
                onGifSelected(media);
            }

            private void onGifSelected(Media media) {

                imageP = media.getImages().getDownsized();
                gif_url_P = imageP.getGifUrl();

                imageM = media.getImages().getDownsizedMedium();
                gif_url_M = imageM.getGifUrl();

                imageO = media.getImages().getFixedWidth();
                gif_url_O = imageO.getGifUrl();

                listener.onGifSelected(gif_url_P, gif_url_M, gif_url_O);
            }

            @Override
            public void onDismissed(@NonNull GPHContentType gphContentType) {

            }

            @Override
            public void didSearchTerm(@NonNull String s) {

            }
        });
    }

    public GiphyDialogFragment retornarGiphyDialog(){
        return gdl;
    }
}
