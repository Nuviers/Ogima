package com.example.ogima.helper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class UsuarioUtils {

    @NonNull
    public static String recuperarNomeConfigurado(@NonNull Usuario usuario) {
        String nomeRecuperado;
        nomeRecuperado = usuario.getNomeUsuario();
        return FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeRecuperado);
    }

    public static void AtualizarStatusOnline(boolean statusOnline){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference salvarStatusOnlineRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("online");

        final String TAG = "StatusOnline";

        if (statusOnline) {
            salvarStatusOnlineRef.onDisconnect().setValue(false);
            Log.d(TAG, "Online");
        }else{
            Log.d(TAG, "Offline");
        }

        salvarStatusOnlineRef.setValue(statusOnline).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Atualizado status - " + statusOnline);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error " + e.getMessage());
            }
        });
    }
}
