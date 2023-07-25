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

    public static String abreviarTexto(String input, int maxLength) {
        if (input.length() > maxLength) {
            // Se a string for maior que o comprimento máximo, corte e adicione "..." no final
            return input.substring(0, maxLength - 3) + "...";
        } else {
            // Caso contrário, retorne a string original
            return input;
        }
    }
}
