package com.example.ogima.helper;

import android.content.Context;
import android.util.Log;

import com.example.ogima.api.NotificationService;
import com.example.ogima.model.DataModel;
import com.example.ogima.model.Notificacao;
import com.example.ogima.model.NotificacaoDados;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmUtils {

    //Retrofit
    private Retrofit retrofit;
    //API do Firebase:
    private String baseUrl = "https://fcm.googleapis.com/fcm/";
    private Notificacao notificacao;
    private NotificacaoDados notificacaoDados;

    public FcmUtils() {
        //Config retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

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

    public interface NotificacaoCallback{
        void onEnviado();
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

    public void prepararNotificacao(Context context, String tipoOperacao, String idUser, long timeStampOperacao, String tipoMensagem, String title, String body, NotificacaoCallback callback){

        //O to pode ser também para tópicos - /topics/"tópicodesejadosemasaspas";

        UsuarioUtils.verificarOnline(idUser, new UsuarioUtils.VerificaOnlineCallback() {
            @Override
            public void onOnline() {
                //Faz mais sentido fazer aquele tipo de notificação no próprio app
                //em uma lista de notificações em um activity quando o usuário está online.
                if (tipoOperacao.equals("evento")) {
                    //Algo feito por mim mesmo, nesse caso indiferente do status
                    //a notificação será enviada.
                }
            }

            @Override
            public void onOffline() {
                //Config retrofit
                retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                notificacao = new Notificacao(title, body);
                UsuarioUtils.recuperarTokenPeloFirebase(idUser, new UsuarioUtils.RecuperarTokenCallback() {
                    @Override
                    public void onRecuperado(String token) {
                        notificacaoDados = new NotificacaoDados(token, notificacao, new DataModel(idUser, timeStampOperacao, tipoOperacao, tipoMensagem));

                        NotificationService notificationService = retrofit.create(NotificationService.class);
                        Call<NotificacaoDados> call = notificationService.salvarNotificacao(notificacaoDados);

                        call.enqueue(new Callback<NotificacaoDados>() {
                            @Override
                            public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                                ToastCustomizado.toastCustomizado("Chamado " + response.code(), context);

                                if (response.isSuccessful()) {
                                    callback.onEnviado();
                                    ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar notificação", context);
                                }else{
                                    callback.onError("Error code " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<NotificacaoDados> call, Throwable t) {
                                callback.onError(t.getMessage());
                            }
                        });
                    }

                    @Override
                    public void semToken() {
                        callback.onError("Sem token");
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }
}
