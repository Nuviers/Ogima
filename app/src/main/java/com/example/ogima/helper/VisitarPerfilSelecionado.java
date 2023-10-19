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

    public static void visitarPerfilSelecionadoPerson(Context context, String idSelecionado) {

        //Verifica se o usuário atual está bloqueado, se não então prosseguir para o perfil
        //do usuário selecionado.

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

        if (idSelecionado != null) {

            if (idSelecionado.equals(idUsuarioLogado)) {
                //Usuário selecionado é o usuário atual.
                return;
            }

            UsuarioUtils.verificaBlock(idSelecionado, context, new UsuarioUtils.VerificaBlockCallback() {
                @Override
                public void onBloqueado() {
                }

                @Override
                public void onDisponivel() {
                    Intent intent = new Intent(context, PersonProfileActivity.class);
                    intent.putExtra("idDonoPerfil", idSelecionado);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }
}
