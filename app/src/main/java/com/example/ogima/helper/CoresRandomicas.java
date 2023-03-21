package com.example.ogima.helper;

import android.graphics.Color;

import java.util.Arrays;
import java.util.List;

public class CoresRandomicas {
    private List<Integer> cores;

    public CoresRandomicas() {
        this.cores = Arrays.asList(
                Color.parseColor("#EF5350"), // Vermelho
                Color.parseColor("#EC407A"), // Rosa
                Color.parseColor("#AB47BC"), // Roxo
                Color.parseColor("#7E57C2"), // Lilás
                Color.parseColor("#5C6BC0"), // Azul claro
                Color.parseColor("#42A5F5"), // Azul
                Color.parseColor("#29B6F6"), // Azul claro brilhante
                Color.parseColor("#26C6DA"), // Azul esverdeado
                Color.parseColor("#26A69A"), // Verde azulado
                Color.parseColor("#66BB6A"), // Verde

                Color.parseColor("#9CCC65"), // Verde claro
                Color.parseColor("#D4E157"), // Verde//limão
                Color.parseColor("#FFEE58"), // Amarelo
                Color.parseColor("#FFCA28"), // Amarelo escuro
                Color.parseColor("#FFA726"), // Laranja
                Color.parseColor("#FF7043"), // Laranja claro
                Color.parseColor("#8D6E63"), // Marrom
                Color.parseColor("#78909C"), // Cinza azulado
                Color.parseColor("#FF1744"), // Vermelho brilhante
                Color.parseColor("#F50057"), // Rosa brilhante

                Color.parseColor("#D500F9"), // Rosa violeta
                Color.parseColor("#651FFF"), // Azul violeta
                Color.parseColor("#3D5AFE"), // Azul puro
                Color.parseColor("#2979FF"), // Azul intenso
                Color.parseColor("#00B0FF"), // Azul claro intenso
                Color.parseColor("#00E5FF"), // Azul claro brilhante
                Color.parseColor("#1DE9B6"), // Verde//água brilhante
                Color.parseColor("#00E676"), // Verde brilhante
                Color.parseColor("#76FF03"), // Verde//limão brilhante
                Color.parseColor("#C6FF00"), // Amarelo//limão brilhante

                Color.parseColor("#FFEA00"), // Amarelo brilhante
                Color.parseColor("#FFC400"), // Laranja amarelo
                Color.parseColor("#FF9100"), // Laranja brilhante
                Color.parseColor("#FF3D00"), // Vermelho laranja brilhante
                Color.parseColor("#FF8A80"), // Vermelho rosado claro
                Color.parseColor("#FF80AB"), // Rosa pálido
                Color.parseColor("#EA80FC"), // Rosa violeta claro
                Color.parseColor("#82B1FF"), // Azul claro pálido
                Color.parseColor("#B388FF"), // Lilás pálido
                Color.parseColor("#B9F6CA")); // Verde claro pastel
    }

    public List<Integer> getCores() {
        return cores;
    }
}
