package com.example.ogima.helper;

import com.example.ogima.model.Usuario;

import java.util.Comparator;

public class OrdenarUsuarioAlfabeticamente {

    //Leva em consideração o nome e o apelido, melhor forma de ordenar alfabeticamente
    //os usuários indiferente de apelido ou nome.
    public static Comparator<Usuario> comparadorAlfabetico() {
        Comparator<Usuario> comparador = new Comparator<Usuario>() {
            @Override
            public int compare(Usuario p1, Usuario p2) {
                String nome1, nome2;
                nome1 = p1.getNomeUsuario();
                nome2 = p2.getNomeUsuario();
                return nome1.compareToIgnoreCase(nome2);
            }
        };
        return comparador;
    }
}
