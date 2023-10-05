package com.example.ogima.helper;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DadosUserPadrao {

    private static DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private static FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private static DatabaseReference usuarioRef;
    private static String emailUsuario;
    private static String idUsuarioLogado;

    public static void preencherDadosUser(Context context, Usuario usuarioSelecionado, TextView txtViewAlvo, ImageView imgViewAlvo) {

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

        usuarioRef = firebaseRef.child("usuarios").child(idUsuarioLogado);

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                    if (usuarioAtual.isStatusEpilepsia()) {

                        GlideCustomizado.montarGlideEpilepsia(context,
                                usuarioSelecionado.getMinhaFoto(),
                                imgViewAlvo,
                                android.R.color.transparent);
                    } else {
                        GlideCustomizado.montarGlide(context,
                                usuarioSelecionado.getMinhaFoto(),
                                imgViewAlvo,
                                android.R.color.transparent);
                    }
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        txtViewAlvo.setText(usuarioSelecionado.getNomeUsuario());
    }
}
