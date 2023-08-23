package com.example.ogima.helper;

import android.text.InputFilter;
import android.text.Spanned;

public class AlphaNumericInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {

        // Cria uma expressão regular para permitir letras, números e espaços
        String regex = "^[a-zA-Z0-9\\s]+$";

        // Verifica cada caractere inserido
        for (int i = start; i < end; i++) {
            if (!Character.toString(source.charAt(i)).matches(regex)) {
                return "";
            }
        }

        return null; // Permite a entrada
    }
}
