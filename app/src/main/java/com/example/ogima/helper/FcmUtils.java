package com.example.ogima.helper;

import android.content.Context;
import android.util.Log;

import com.example.ogima.api.NotificationService;
import com.example.ogima.model.MessageNotificacao;
import com.example.ogima.model.NotifLocal;
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

    public interface VerificaSeEstaEmConversasCallback {
        void onEnviarNotificacao();

        void onNaoEnviar();

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

        if (autenticacao != null && autenticacao.getCurrentUser() != null) {
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

            if (tipoMensagem.equals(MidiaUtils.TEXT)) {
                body = messageNotificacao.getConteudoMensagem();
            } else {
                body = messageNotificacao.getConteudoMensagem();
            }

            //Verifica se o usuário destinatário está online
            UsuarioUtils.verificarOnline(idDestinatario, new UsuarioUtils.VerificaOnlineCallback() {
                @Override
                public void onOnline() {
                    //Usuário está online

                    //Verifica se o usuário está na conversa atual
                    verificaSeEstaNaConversaAtual(messageNotificacao, new VerificaViewConversaAtualCallback() {
                        @Override
                        public void onEstaNaConversaAtual() {
                            //Usuário está na conversa que a lógica se refere, logo
                            //não há mensagens perdidas nessa conversa, não é necessário fazer nada.
                        }

                        @Override
                        public void onNaoEsta() {
                            //Usuário não está na conversa atual então logo ele tem
                            //mensagens perdidas nessa conversa
                            salvarMsgNaoLida(messageNotificacao, new NotificacaoLocalCallback() {
                                @Override
                                public void onMsgNaoLidaSalva() {
                                    verificaSeEstaNasConversas(idDestinatario, new VerificaSeEstaEmConversasCallback() {
                                        @Override
                                        public void onEnviarNotificacao() {
                                            //Usuário está vendo as conversas,
                                            //logo faz sentido enviar a notificação para ele.
                                            enviarNotificacaoPadrao(context, idDestinatario, tipoOperacao, messageNotificacao, body, new NotificacaoCallback() {
                                                @Override
                                                public void onEnviado() {
                                                }

                                                @Override
                                                public void onError(String message) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Error " + message, context);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onNaoEnviar() {
                                            //Usuário não está nas conversas, creio
                                            //que faz sentido não enviar a notificação para não atrapalhar
                                            //a experiência do usuário.
                                            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
                                            DatabaseReference exibirBadgeNewMensagensRef
                                                    = firebaseRef.child("usuarios").child(idDestinatario).child("exibirBadgeNewMensagens");
                                            exibirBadgeNewMensagensRef.setValue(true);
                                        }

                                        @Override
                                        public void onError(String message) {

                                        }
                                    });
                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }

                @Override
                public void onOffline() {

                    salvarMsgNaoLida(messageNotificacao, new NotificacaoLocalCallback() {
                        @Override
                        public void onMsgNaoLidaSalva() {
                            //Usuário está offline, salvar a mensagem perdida e enviar a notificação.
                            enviarNotificacaoPadrao(context, idDestinatario, tipoOperacao, messageNotificacao, body, new NotificacaoCallback() {
                                @Override
                                public void onEnviado() {
                                }

                                @Override
                                public void onError(String message) {
                                    //ToastCustomizado.toastCustomizadoCurto("Error " + message, context);
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {

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

                notificacaoDados = new NotificacaoDados(token, new MessageNotificacao(idRemetente, body, tipoMensagem, timeStampMensagem, fotoRemetente, nomeRemetente, tipoOperacao, idDestinatario));

                NotificationService notificationService = retrofit.create(NotificationService.class);
                Call<NotificacaoDados> call = notificationService.salvarNotificacao(notificacaoDados);

                call.enqueue(new Callback<NotificacaoDados>() {
                    @Override
                    public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {

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

    public static void salvarMsgNaoLida(MessageNotificacao dadosNotificacao, NotificacaoLocalCallback callback) {

        //Salva as mensagens não lidas no contador de mensagens.

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        String idRemetente = dadosNotificacao.getIdRemetente();
        String idDestinatario = dadosNotificacao.getIdDestinatario();
        String tipoMensagem = dadosNotificacao.getTipoMensagem();
        long timeStampMensagem = dadosNotificacao.getTimestampMensagem();
        String fotoRemetente = dadosNotificacao.getFotoRemetente();
        String nomeRemetente = dadosNotificacao.getNomeRemetente();
        String mensagem = dadosNotificacao.getConteudoMensagem();

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

    public static void verificaSeEstaNasConversas(String idDestinatario, VerificaSeEstaEmConversasCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference verificaRef = firebaseRef.child("usuarios")
                .child(idDestinatario).child("nasConversas");

        verificaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    boolean nasConversas = snapshot.getValue(Boolean.class);
                    if (nasConversas) {
                        callback.onEnviarNotificacao();
                    } else {
                        callback.onNaoEnviar();
                    }
                } else {
                    callback.onNaoEnviar();
                }
                verificaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });

    }
}
