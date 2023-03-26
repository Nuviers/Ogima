package com.example.ogima.helper;

import androidx.annotation.NonNull;

import com.example.ogima.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseRecuperarUsuario {

    public interface MontarAvisoChatCallback {
        void onNomesAvisoConfigurado(String nomeAfetado, String nomeLogado);

        void onError(String mensagem);
    }

    public static void montarAvisoChat(String idAfetado, String idAtual, MontarAvisoChatCallback callback) {
        DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(idAfetado);
        DatabaseReference usuarioAtualRef = FirebaseDatabase.getInstance().getReference("usuarios").child(idAtual);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);
                    usuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Usuario usuarioLogado = snapshot.getValue(Usuario.class);

                                String nomeUserRemovido;
                                String nomeUserAtual;

                                if (usuarioRecuperado.getExibirApelido().equals("sim")) {
                                    nomeUserRemovido = usuarioRecuperado.getApelidoUsuario();
                                }else{
                                    nomeUserRemovido = usuarioRecuperado.getNomeUsuario();
                                }

                                if (usuarioLogado.getExibirApelido().equals("sim")) {
                                    nomeUserAtual = usuarioLogado.getApelidoUsuario();
                                }else{
                                    nomeUserAtual = usuarioLogado.getNomeUsuario();
                                }
                                callback.onNomesAvisoConfigurado(nomeUserRemovido, nomeUserAtual);
                            }
                            usuarioAtualRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
