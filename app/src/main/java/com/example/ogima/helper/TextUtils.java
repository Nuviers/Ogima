package com.example.ogima.helper;

import android.widget.EditText;

public class TextUtils {
    public static boolean isEditTextNotEmpty(EditText editText) {
        if (editText != null) {
            String text = editText.getText().toString().trim();
            return !text.isEmpty();
        }
        return false;
    }

    public static boolean isStringNotEmpty(String texto) {
        if (texto != null) {
            String textoFormatado = texto.trim();
            return !textoFormatado.isEmpty();
        }
        return false;
    }
}
