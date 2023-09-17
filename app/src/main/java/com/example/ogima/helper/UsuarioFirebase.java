package com.example.ogima.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UsuarioFirebase {

    public static String getIdUsuario(){

        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return autenticacao.getCurrentUser().getUid();

    }

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }

    //
    public  static String getIdUsuarioCriptografado(){

        FirebaseAuth usuarioIdentificador = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario = usuarioIdentificador.getCurrentUser().getEmail();
        String identificadorUsuario = Base64Custom.codificarBase64(emailUsuario);

        return identificadorUsuario;
    }

}


