package com.example.ogima.helper;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class VisitarPerfilSelecionado {

    private static DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private static FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private static DatabaseReference verificaBloqueioRef;
    private static String emailUsuario;
    private static String idUsuarioLogado;

    public static void visitarPerfilSelecionadoPerson(Context context, Usuario usuarioSelecionado) {

        //Verifica se o usuário atual está bloqueado, se não então prosseguir para o perfil
        //do usuário selecionado.

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

        verificaBloqueioRef = firebaseRef.child("blockUser")
                .child(idUsuarioLogado).child(usuarioSelecionado.getIdUsuario());

        verificaBloqueioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Usuário atual está bloqueado pelo usuário selecionado.
                    ToastCustomizado.toastCustomizadoCurto("Perfil do usuário indisponível!", context);
                } else {
                    //Usuário atual não está bloqueado.
                    Intent intent = new Intent(context, PersonProfileActivity.class);
                    intent.putExtra("usuarioSelecionado", usuarioSelecionado);
                    context.startActivity(intent);
                }
                verificaBloqueioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
