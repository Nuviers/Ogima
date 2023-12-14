package com.example.ogima.helper;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.widget.Button;

import androidx.core.view.ViewCompat;

public class ButtonUtils {

    public static void desativarBotaoDegrade(Button btnAlvo, int corBackground){
        if (btnAlvo != null) {
            btnAlvo.setEnabled(false);
            btnAlvo.setBackgroundTintList(ColorStateList.valueOf(corBackground));
            ViewCompat.setBackgroundTintMode(btnAlvo, PorterDuff.Mode.SCREEN);
        }
    }

    public static void ativarBotaoDegrade(Button btnAlvo){
        if (btnAlvo != null) {
            btnAlvo.setEnabled(true);
            ViewCompat.setBackgroundTintMode(btnAlvo, null);
            btnAlvo.setBackgroundTintList(null);
        }
    }

    public static void desativarBotao(Button btnAlvo, int corBackground){
        if (btnAlvo != null) {
            btnAlvo.setEnabled(false);
            btnAlvo.setBackgroundTintList(ColorStateList.valueOf(corBackground));
        }
    }

    public static void ativarBotao(Button btnAlvo, int corBackground){
        if (btnAlvo != null) {
            btnAlvo.setEnabled(true);
            btnAlvo.setBackgroundTintList(ColorStateList.valueOf(corBackground));
        }
    }
}
