package com.example.ogima.helper;

import android.text.TextUtils;

import java.util.Locale;

public class FormatarNomePesquisaUtils {


    //Formata cada palavra com a primeira letra dela em maiúsculo
    public static String formatarNomeParaPesquisa(String nome) {
        if (nome == null || nome.isEmpty()) {
            return "";
        }
        String[] partes = nome.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            sb.append(parte.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(parte.substring(1))
                    .append(" ");
        }
        return sb.toString().trim();
    }
}
