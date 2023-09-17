package com.example.ogima.helper;

import android.content.Context;
import android.content.Intent;

import com.example.ogima.activity.EdicaoGeralParcActivity;
import com.example.ogima.activity.EditarPerfilParcActivity;
import com.example.ogima.activity.ProfileParcActivity;
import com.example.ogima.model.Usuario;

import java.util.ArrayList;

public class IntentEdicaoPerfilParc {
    public static void irParaEdicao(Context context, String idUsuario){
        ParceiroUtils.recuperarDados(idUsuario, new ParceiroUtils.RecuperarUserParcCallback() {
            @Override
            public void onRecuperado(Usuario usuario, String nome, String orientacao, String exibirPerfilPara, String idUserParc, ArrayList<String> listaHobbies, ArrayList<String> listaFotos, ArrayList<String> listaIdsAEsconder) {
                Intent intent = new Intent(context, EditarPerfilParcActivity.class);
                intent.putExtra("usuarioParc", usuario);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }
}
