package com.example.ogima.helper;

import androidx.annotation.NonNull;

import com.example.ogima.model.Usuario;

import java.util.ArrayList;

public class UsuarioUtils {

    @NonNull
    public static String recuperarNomeConfigurado(@NonNull Usuario usuario){

        String nomeRecuperado;

        if (usuario.getExibirApelido() != null &&
                usuario.getExibirApelido().equals("sim")) {
            nomeRecuperado = usuario.getApelidoUsuario();
        }else{
            nomeRecuperado = usuario.getNomeUsuario();
        }

       return FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeRecuperado);
    }
}
