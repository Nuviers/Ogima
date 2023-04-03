package com.example.ogima.helper;

import com.example.ogima.model.Grupo;

import java.util.Comparator;

public class GrupoNomeComparator implements Comparator<Grupo> {
    @Override
    public int compare(Grupo o1, Grupo o2) {
        return o1.getNomeGrupo().compareTo(o2.getNomeGrupo());
    }
}