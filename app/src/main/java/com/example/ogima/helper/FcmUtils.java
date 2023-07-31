package com.example.ogima.helper;

import android.content.Context;
import android.util.Log;

import com.example.ogima.api.NotificationService;
import com.example.ogima.model.MessageNotificacao;
import com.example.ogima.model.NotifLocal;
import com.example.ogima.model.Notificacao;
import com.example.ogima.model.NotificacaoDados;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

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

    private String body = "";

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

    public interface RegistrarTopicoCallback {
        void onRegistrado();

        void onError(String message);
    }

    public interface RemoverTopicoCallback {
        void onRemovido();

        void onError(String message);
    }

    public interface NotificacaoCallback {
        void onEnviado();

        void onError(String message);
    }

    public interface NotificacaoLocalCallback {
        void onMsgNaoLidaSalva();

        void onError(String message);
    }

    public interface VerificaViewConversaAtualCallback {
        void onEstaNaConversaAtual();

        void onNaoEsta();

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

    public static void salvarTopicoNoFirebase(String topico, RegistrarTopicoCallback callback) {
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

    public static void removerTopicoNoFirebase(String topico, RemoverTopicoCallback callback) {
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
                } else if (usuarioAtual.getTopicosNotificacoes() != null &&
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
                } else {
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


    public void prepararNotificacaoMensagem(Context context, String tipoOperacao, MessageNotificacao messageNotificacao, NotificacaoCallback callback) {

        //O to pode ser também para tópicos - /topics/"tópicodesejadosemasaspas";
        if (messageNotificacao != null && messageNotificacao.getIdRemetente() != null
                && !messageNotificacao.getIdRemetente().isEmpty()) {

            String idRemetente = messageNotificacao.getIdRemetente();
            String idDestinatario = messageNotificacao.getIdDestinatario();
            String title = messageNotificacao.getNomeRemetente();
            String tipoMensagem = messageNotificacao.getTipoMensagem();
            long timeStampMensagem = messageNotificacao.getTimestampMensagem();
            String fotoRemetente = messageNotificacao.getFotoRemetente();
            String nomeRemetente = messageNotificacao.getNomeRemetente();

            if (tipoMensagem.equals("texto")) {
                body = messageNotificacao.getConteudoMensagem();
            } else {
               body = messageNotificacao.getConteudoMensagem();
            }

            //Verifica se o usuário destinatário está online
            UsuarioUtils.verificarOnline(idDestinatario, new UsuarioUtils.VerificaOnlineCallback() {
                @Override
                public void onOnline() {
                    //Usuário está online
                    ToastCustomizado.toastCustomizadoCurto("Online", context);

                    enviarNotificacaoPadrao(context, idDestinatario, tipoOperacao, messageNotificacao, body, new NotificacaoCallback() {
                        @Override
                        public void onEnviado() {
                            ToastCustomizado.toastCustomizadoCurto("Enviado",context);
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto("Error " + message,context);
                        }
                    });

                    //Verifica se o usuário está na conversa atual
                    verificaSeEstaNaConversaAtual(messageNotificacao, new VerificaViewConversaAtualCallback() {
                        @Override
                        public void onEstaNaConversaAtual() {
                            ToastCustomizado.toastCustomizadoCurto("Está na conversa", context);
                        }

                        @Override
                        public void onNaoEsta() {
                            ToastCustomizado.toastCustomizadoCurto("Não está na conversa", context);
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }

                @Override
                public void onOffline() {
                    ToastCustomizado.toastCustomizadoCurto("Offline", context);

                    enviarNotificacaoPadrao(context, idDestinatario, tipoOperacao, messageNotificacao, body, new NotificacaoCallback() {
                        @Override
                        public void onEnviado() {
                            ToastCustomizado.toastCustomizadoCurto("Enviado",context);
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto("Error " + message,context);
                        }
                    });
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    public static void verificaSeEstaNaConversaAtual(MessageNotificacao messageNotificacao, VerificaViewConversaAtualCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference salvarViewEmConversaRef = firebaseRef.child("viewConversa")
                .child(messageNotificacao.getIdDestinatario()).child(messageNotificacao.getIdRemetente())
                .child("viewConversa");
        salvarViewEmConversaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    boolean viewConversa = snapshot.getValue(Boolean.class);
                    if (viewConversa) {
                        callback.onEstaNaConversaAtual();
                    } else {
                        callback.onNaoEsta();
                    }
                } else {
                    callback.onNaoEsta();
                }
                salvarViewEmConversaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void enviarNotificacaoPadrao(Context context, String idDestinatario, String tipoOperacao, MessageNotificacao messageNotificacao, String body, NotificacaoCallback callback) {
        UsuarioUtils.recuperarTokenPeloFirebase(idDestinatario, new UsuarioUtils.RecuperarTokenCallback() {
            @Override
            public void onRecuperado(String token) {
                //Token recuperado para que seja possível enviar a notificação padrão
                ToastCustomizado.toastCustomizadoCurto("Token recuperado", context);

                String idRemetente = messageNotificacao.getIdRemetente();
                String idDestinatario = messageNotificacao.getIdDestinatario();
                String title = messageNotificacao.getNomeRemetente();
                String tipoMensagem = messageNotificacao.getTipoMensagem();
                long timeStampMensagem = messageNotificacao.getTimestampMensagem();
                String fotoRemetente = messageNotificacao.getFotoRemetente();
                String nomeRemetente = messageNotificacao.getNomeRemetente();

                //Config retrofit
                retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                notificacao = new Notificacao(title, body);

                notificacaoDados = new NotificacaoDados(token, notificacao, new MessageNotificacao(idRemetente, body, tipoMensagem, timeStampMensagem, fotoRemetente, nomeRemetente, tipoOperacao, idDestinatario));

                NotificationService notificationService = retrofit.create(NotificationService.class);
                Call<NotificacaoDados> call = notificationService.salvarNotificacao(notificacaoDados);

                call.enqueue(new Callback<NotificacaoDados>() {
                    @Override
                    public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                        ToastCustomizado.toastCustomizado("Chamado " + response.code(), context);

                        if (response.isSuccessful()) {
                            callback.onEnviado();

                            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
                            DatabaseReference exibirBadgeNewMensagensRef
                                    = firebaseRef.child("usuarios").child(idDestinatario).child("exibirBadgeNewMensagens");
                            exibirBadgeNewMensagensRef.setValue(true);
                            ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar notificação", context);
                        } else {
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

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    public static void salvarMsgNaoLida(MessageNotificacao dadosNotificacao, boolean online, NotificacaoLocalCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        String idRemetente = dadosNotificacao.getIdRemetente();
        String idDestinatario = dadosNotificacao.getIdDestinatario();
        String tipoMensagem = dadosNotificacao.getTipoMensagem();
        long timeStampMensagem = dadosNotificacao.getTimestampMensagem();
        String fotoRemetente = dadosNotificacao.getFotoRemetente();
        String nomeRemetente = dadosNotificacao.getNomeRemetente();
        String mensagem = dadosNotificacao.getConteudoMensagem();

        if (online) {
            //Somente salva a mensagem não lida se o usuário estiver online
            DatabaseReference salvarEmDestinatarioRef = firebaseRef.child("mensagensNaoLidas")
                    .child(idDestinatario).child(idRemetente);

            salvarEmDestinatarioRef.setValue(new NotifLocal(idRemetente,
                    idDestinatario, fotoRemetente, nomeRemetente, tipoMensagem, mensagem, timeStampMensagem, true));
        }

        //Salva o número de mensagens não lidas indiferente de offline ou online.
        DatabaseReference salvarMensagemPerdidaRef = firebaseRef.child("contadorMensagens")
                .child(idDestinatario).child(idRemetente).child("mensagensPerdidas");

        AtualizarContador atualizarContador = new AtualizarContador();
        atualizarContador.acrescentarContador(salvarMensagemPerdidaRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                salvarMensagemPerdidaRef.setValue(contadorAtualizado).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onMsgNaoLidaSalva();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        callback.onMsgNaoLidaSalva();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onMsgNaoLidaSalva();
            }
        });
    }
}
