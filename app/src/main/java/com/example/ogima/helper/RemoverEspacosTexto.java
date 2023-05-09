package com.example.ogima.helper;

import android.widget.EditText;

public class RemoverEspacosTexto {

    public static String removeExtraSpaces(EditText editText) {
        if (editText != null) {
            String text = editText.getText().toString().trim();
            return text;
        }
        return "";
    }
}
