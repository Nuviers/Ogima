package com.example.ogima.helper;

import android.util.Log;

import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import io.reactivex.annotations.NonNull;

public class FcmUtils {

    public interface RecuperarTokenCallback {
        void onRecuperado(String token);

        void onError(String message);
    }

    public interface SalvarTokenCallback {
        void onSalvo(String token);
        void onError(String message);
    }

    public interface RegistrarTopicoCallback{
        void onRegistrado();
        void onError(String message);
    }

    public interface RemoverTopicoCallback{
        void onRemovido();
        void onError(String message);
    }

    private static final String TAG = "FcmUtils";

    public static void recuperarTokenAtual(RecuperarTokenCallback callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            callback.onError(task.getException().getMessage());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        callback.onRecuperado(token);

                        // Log and toast
                        Log.d(TAG, "Token device " + token);
                    }
                });
    }

    public static void salvarTokenAtualNoUserAtual(SalvarTokenCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            callback.onError(task.getException().getMessage());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        DatabaseReference salvarTokenRef = firebaseRef.child("usuarios")
                                .child(idUsuario).child("token");

                        salvarTokenRef.onDisconnect().setValue(token);

                        salvarTokenRef.setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                callback.onSalvo(token);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                callback.onError(e.getMessage());
                            }
                        });
                        // Log and toast
                        Log.d(TAG, "Token device " + token);
                    }
                });
    }

    public static void registrarNovoTopico(String topico, RegistrarTopicoCallback callback) {
        FirebaseMessaging.getInstance().subscribeToTopic(topico).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                salvarTopicoNoFirebase(topico, callback);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public static void salvarTopicoNoFirebase(String topico, RegistrarTopicoCallback callback){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference salvaTopicoRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("topicosNotificacoes");

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                ArrayList<String> listaTopicos = new ArrayList<>();

                if (usuarioAtual.getTopicosNotificacoes() != null &&
                        usuarioAtual.getTopicosNotificacoes().size() > 0) {
                    listaTopicos = usuarioAtual.getTopicosNotificacoes();
                    if (!listaTopicos.contains(topico)) {
                        //Somente adiciona se não conter tal tópico na lista.
                        listaTopicos.add(topico);
                    }
                } else {
                    listaTopicos.add(topico);
                }

                salvaTopicoRef.setValue(listaTopicos).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onRegistrado();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
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

    public static void removerTopico(String topico, RemoverTopicoCallback callback) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topico).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                removerTopicoNoFirebase(topico, callback);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public static void removerTopicoNoFirebase(String topico, RemoverTopicoCallback callback){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference removerTopicoRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("topicosNotificacoes");

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                ArrayList<String> listaTopicos = new ArrayList<>();

                if (usuarioAtual.getTopicosNotificacoes() != null &&
                        usuarioAtual.getTopicosNotificacoes().size() > 1) {
                    listaTopicos = usuarioAtual.getTopicosNotificacoes();
                    if (listaTopicos.contains(topico)) {

                        listaTopicos.remove(topico);

                        removerTopicoRef.setValue(listaTopicos).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                callback.onRemovido();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                callback.onError(e.getMessage());
                            }
                        });
                    }
                } else if(usuarioAtual.getTopicosNotificacoes() != null &&
                        usuarioAtual.getTopicosNotificacoes().size() == 1) {
                    removerTopicoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            callback.onRemovido();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {
                            callback.onError(e.getMessage());
                        }
                    });
                }else{
                    callback.onRemovido();
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
}
