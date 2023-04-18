package com.example.ogima.helper;

import com.example.ogima.model.Usuario;

import java.util.Comparator;

public class OrdenarUsuarioAlfabeticamente {

    //Leva em consideração o nome e o apelido, melhor forma de ordenar alfabeticamente
    //os usuários indiferente de apelido ou nome.
    public static Comparator<Usuario> comparadorAlfabetico(){
        Comparator<Usuario> comparador = new Comparator<Usuario>() {
            @Override
            public int compare(Usuario p1, Usuario p2) {
                String nome1, nome2;
                if (p1.getExibirApelido().equals("sim")) {
                    nome1 = p1.getApelidoUsuario();
                } else {
                    nome1 = p1.getNomeUsuario();
                }
                if (p2.getExibirApelido().equals("sim")) {
                    nome2 = p2.getApelidoUsuario();
                } else {
                    nome2 = p2.getNomeUsuario();
                }
                return nome1.compareToIgnoreCase(nome2);
            }
        };
        return comparador;
    }
}
