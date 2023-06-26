package com.example.ogima.helper;

import androidx.annotation.NonNull;

import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AdicionarIdAmigoUtils {

    public interface SalvarIdAmigoCallback {
        void onAmigoSalvo();

        void onError(@NonNull String message);
    }

    public static void salvarAmigo(@NonNull String idDestinatario, @NonNull SalvarIdAmigoCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference salvarIdUserAtualRef = firebaseRef.child("friends")
                .child(idUsuario).child(idDestinatario).child("idUsuario");

        DatabaseReference salvarIdUserDestinatarioRef = firebaseRef.child("friends")
                .child(idDestinatario).child(idUsuario).child("idUsuario");

        salvarIdUserAtualRef.setValue(idDestinatario).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                salvarIdUserDestinatarioRef.setValue(idUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        salvarIdEmUsuario(idDestinatario, callback);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(Objects.requireNonNull(e.getMessage()));
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(Objects.requireNonNull(e.getMessage()));
            }
        });
    }

    public static void salvarIdEmUsuario(@NonNull String idDestinatario, @NonNull SalvarIdAmigoCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference recuperaUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        DatabaseReference salvaIdUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("listaIdAmigos");

        DatabaseReference recuperaUserDestinatarioRef = firebaseRef.child("usuarios")
                .child(idDestinatario);

        DatabaseReference salvaIdUserDestinatarioRef = firebaseRef.child("usuarios")
                .child(idDestinatario).child("listaIdAmigos");

        recuperaUserAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    ArrayList<String> listaIdsAmigos = new ArrayList<>();

                    if (usuario.getListaIdAmigos() != null
                            && usuario.getListaIdAmigos().size() > 0) {
                        listaIdsAmigos = usuario.getListaIdAmigos();
                        if (!listaIdsAmigos.contains(idDestinatario)) {
                            //Somente adiciona se não conter tal id na lista.
                            listaIdsAmigos.add(idDestinatario);
                        }
                    } else {
                        listaIdsAmigos.add(idDestinatario);
                    }

                    salvaIdUserAtualRef.setValue(listaIdsAmigos).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            recuperaUserDestinatarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {

                                        Usuario usuarioDestinatario = snapshot.getValue(Usuario.class);

                                        ArrayList<String> listaIdsAmigos = new ArrayList<>();

                                        if (usuarioDestinatario.getListaIdAmigos() != null
                                                && usuarioDestinatario.getListaIdAmigos().size() > 0) {
                                            listaIdsAmigos = usuarioDestinatario.getListaIdAmigos();
                                            if (!listaIdsAmigos.contains(idUsuario)) {
                                                //Somente adiciona se não conter tal id na lista.
                                                listaIdsAmigos.add(idUsuario);
                                            }
                                        } else {
                                            listaIdsAmigos.add(idUsuario);
                                        }

                                        salvaIdUserDestinatarioRef.setValue(listaIdsAmigos).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                callback.onAmigoSalvo();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                callback.onError(Objects.requireNonNull(e.getMessage()));
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError(Objects.requireNonNull(e.getMessage()));
                        }
                    });
                }
                recuperaUserAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
