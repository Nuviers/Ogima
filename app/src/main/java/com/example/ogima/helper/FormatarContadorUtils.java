package com.example.ogima.helper;

import com.google.android.gms.common.util.NumberUtils;

public class FormatarContadorUtils {

    public static final String formatarNumeroAbreviado(int numero) {
        String formattedNumber;
        if (numero < 1000) {
            formattedNumber = String.valueOf((int) numero);
        } else if (numero < 1000000) {
            formattedNumber = String.format("%.1fK", numero / 1000);
        } else if (numero < 1000000000) {
            formattedNumber = String.format("%.1fM", numero / 1000000);
        } else {
            formattedNumber = String.format("%.1fB", numero / 1000000000);
        }
        return formattedNumber;
    }
}
