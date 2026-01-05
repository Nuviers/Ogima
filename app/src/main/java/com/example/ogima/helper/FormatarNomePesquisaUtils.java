package com.example.ogima.helper;

import android.text.TextUtils;

import java.text.Normalizer;
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
            // --- PROTEÇÃO CONTRA ESPAÇO DUPLO ---
            if (parte.trim().isEmpty()) continue;
            // ------------------------------------

            sb.append(parte.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(parte.substring(1))
                    .append(" ");
        }
        return sb.toString().trim();
    }

    public static String removeAcentuacao(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        return str.toLowerCase();
    }
}
