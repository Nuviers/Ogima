package com.example.ogima.helper;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Context;

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

public class SeguindoUtils {

    public interface SalvarSeguindoCallback {
        void onSeguindoSalvo();

        void onError(@NonNull String message);
    }

    public interface RemoverSeguindoCallback {
        void onRemovido();

        void onError(@NonNull String message);
    }

    public interface SalvarTimestampCallback {
        void onRecuperado(long timestampnegativo);

        void onError(String message);
    }

    public static void salvarSeguindo(Context context, @NonNull String idSeguindo, @NonNull SalvarSeguindoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference salvarSeguindoAtualRef = firebaseRef.child("seguindo")
                .child(idUsuario).child(idSeguindo).child("idUsuario");

        DatabaseReference salvarSeguidorAtualRef = firebaseRef.child("seguidores")
                .child(idSeguindo).child(idUsuario).child("idUsuario");

        salvarSeguindoAtualRef.setValue(idSeguindo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                salvarSeguidorAtualRef.setValue(idUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        salvarIdSeguindo(context, idSeguindo, callback);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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

    public static void salvarIdSeguindo(Context context, @NonNull String idSeguindo, @NonNull SalvarSeguindoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference recuperaUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        DatabaseReference salvarIdUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("listaIdSeguindo");

        recuperaUserAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    ArrayList<String> listaIdSeguindo = new ArrayList<>();

                    if (usuario.getListaIdSeguindo() != null
                            && usuario.getListaIdSeguindo().size() > 0) {
                        listaIdSeguindo = usuario.getListaIdSeguindo();
                        if (!listaIdSeguindo.contains(idSeguindo)) {
                            //Somente adiciona se não conter tal id na lista.
                            listaIdSeguindo.add(idSeguindo);
                        }
                    } else {
                        listaIdSeguindo.add(idSeguindo);
                    }

                    salvarIdUserAtualRef.setValue(listaIdSeguindo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            salvarTimestampnegativo(context, idSeguindo, callback);
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

    public static void removerSeguindo(@NonNull String idSeguindo, @NonNull RemoverSeguindoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference removerSeguindoRef = firebaseRef.child("seguindo")
                .child(idUsuario).child(idSeguindo);

        DatabaseReference removerSeguidorRef = firebaseRef.child("seguidores")
                .child(idSeguindo).child(idUsuario);

        removerSeguindoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                removerSeguidorRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        removerIdSeguindo(idSeguindo, callback);
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

    public static void removerIdSeguindo(@NonNull String idSeguindo, @NonNull RemoverSeguindoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference atualizaListaSeguindoRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("listaIdSeguindo");

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (listaIdSeguindo != null && listaIdSeguindo.size() > 0) {
                    if (listaIdSeguindo.contains(idSeguindo)) {
                        listaIdSeguindo.remove(idSeguindo);
                        atualizaListaSeguindoRef.setValue(listaIdSeguindo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                callback.onRemovido();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callback.onError(Objects.requireNonNull(e.getMessage()));
                            }
                        });
                    }
                }
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    public static void salvarTimestampnegativo(Context context, String idAlvo, SalvarSeguindoCallback callback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(context, new NtpTimestampRepository.NtpTimestampCallback() {
            String idUsuario = UsuarioUtils.recuperarIdUserAtual();
            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
            DatabaseReference salvarTimestampSeguindoRef = firebaseRef.child("seguindo")
                    .child(idUsuario).child(idAlvo).child("timestampinteracao");
            DatabaseReference salvarTimestampSeguidoresRef = firebaseRef.child("seguidores")
                    .child(idAlvo).child(idUsuario).child("timestampinteracao");
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        salvarTimestampSeguindoRef.setValue(timestampNegativo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                               salvarTimestampSeguidoresRef.setValue(timestampNegativo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void unused) {
                                       callback.onSeguindoSalvo();
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       callback.onError(e.getMessage());
                                   }
                               });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callback.onError(e.getMessage());
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }
}