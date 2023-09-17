package com.example.ogima.helper;

import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;
import static com.google.firebase.messaging.Constants.MessageNotificationKeys.DEFAULT_SOUND;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ogima.R;
import com.example.ogima.activity.ConversaActivity;
import com.example.ogima.activity.NotificationsTesteActivity;
import com.example.ogima.activity.SplashActivity;
import com.example.ogima.model.MessageNotificacao;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsReceiver extends FirebaseMessagingService {

    private static final String TAG = "RECEIVERGFCM";
    private static List<MessageNotificacao> listaMensagens = new ArrayList<>();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuarioLogado;
    private static int nrNotificacoes;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

        //onMessageReceived - lógica executada quando o app está aberto e em primeiro plano.

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Obtém a string extra do payload de dados, se existir
        String idRemetente = "";
        String idDestinatario = "";
        long timestampInteracao = 0;
        String tipoInteracao = "";
        String tipoMensagem = "";
        String fotoRemetente = "";
        String nomeRemetente = "";
        String conteudoMsg = "";

        if (remoteMessage.getData() != null) {
            if (remoteMessage.getData().containsKey("idRemetente")) {
                idRemetente = remoteMessage.getData().get("idRemetente");
            }

            if (remoteMessage.getData().containsKey("idDestinatario")) {
                idDestinatario = remoteMessage.getData().get("idDestinatario");

                if (idDestinatario != null && !idDestinatario.equals(idUsuarioLogado)) {
                    //Usuário que está tentando ver a notificação não é o usuário correto.
                    return;
                }
            }

            if (remoteMessage.getData().containsKey("timestampMensagem")) {
                timestampInteracao = Long.parseLong(remoteMessage.getData().get("timestampMensagem"));
            }

            if (remoteMessage.getData().containsKey("tipoInteracao")) {
                tipoInteracao = remoteMessage.getData().get("tipoInteracao");
            }

            if (remoteMessage.getData().containsKey("tipoMensagem")) {
                tipoMensagem = remoteMessage.getData().get("tipoMensagem");
            }

            if (remoteMessage.getData().containsKey("fotoRemetente")) {
                fotoRemetente = remoteMessage.getData().get("fotoRemetente");
            }

            if (remoteMessage.getData().containsKey("nomeRemetente")) {
                nomeRemetente = remoteMessage.getData().get("nomeRemetente");
            }

            if (remoteMessage.getData().containsKey("conteudoMensagem")) {
                conteudoMsg = remoteMessage.getData().get("conteudoMensagem");
            }
        }

        if (idDestinatario != null && idDestinatario.equals(idUsuarioLogado)) {
            sendNotificationResult(remoteMessage.getFrom(), conteudoMsg, idRemetente, timestampInteracao, tipoInteracao, tipoMensagem);
            sendNotification(nomeRemetente, conteudoMsg, idRemetente, timestampInteracao, tipoInteracao, tipoMensagem, fotoRemetente, nomeRemetente, idDestinatario);
        }
    }

    private void sendNotificationResult(String from, String body, String idUser, long timestampInteracao, String tipoInteracao, String tipoMensagem) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ToastCustomizado.toastCustomizado("De: " + from + " " + "conteúdo " + body, NotificationsReceiver.this.getApplicationContext());
                /*
                ToastCustomizado.toastCustomizado("De: " + from + " " + "conteúdo " + body, NotificationsReceiver.this.getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Id: " + idUser, NotificationsReceiver.this.getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Timestamp: " + timestampInteracao, NotificationsReceiver.this.getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("TipoOperacao: " + tipoInteracao, NotificationsReceiver.this.getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("TipoMensagem: " + tipoMensagem, NotificationsReceiver.this.getApplicationContext());
                 */
            }
        });
    }

    public void sendNotification(String title, String body, String idRemetente, long timestampInteracao, String tipoInteracao, String tipoMensagem, String fotoRemetente, String nomeRemetente, String idDestinatario) {
        if (tipoInteracao != null && !tipoInteracao.isEmpty()) {
            switch (tipoInteracao) {
                case "mensagem":
                    // Adicione a nova mensagem à lista de mensagens

                    if (listaMensagens.size() >= 5) {
                        // Remova a mensagem mais antiga da lista
                        listaMensagens.remove(0);
                    }

                    if (tipoMensagem.equals("texto")) {
                        listaMensagens.add(new MessageNotificacao(idRemetente, body, tipoMensagem, timestampInteracao, fotoRemetente, nomeRemetente, tipoInteracao, idDestinatario));
                    } else {
                        String midiaMsg = "{Mídia} " + tipoMensagem.toUpperCase(Locale.ROOT);
                        listaMensagens.add(new MessageNotificacao(idRemetente, midiaMsg, tipoMensagem, timestampInteracao, fotoRemetente, nomeRemetente, tipoInteracao, idDestinatario));
                    }
                    nrNotificacoes++;

                    configNotificacaoMensagem(title, body, idRemetente, timestampInteracao, tipoInteracao, tipoMensagem, fotoRemetente, nomeRemetente, listaMensagens, nrNotificacoes, idDestinatario);
                    break;
            }
        }
    }

    private void configNotificacaoMensagem(String title, String body, String idUser, long timestampInteracao, String tipoInteracao, String tipoMensagem, String fotoRemetente, String nomeRemetente, List<MessageNotificacao> mensagensParaNotificacao, int nrNotificacoes, String idDestinatario) {

        //Necessário verificar se o token remetente é igual ao token destinatário
        //se for significa que é o mesmo dispositivo então não prosseguir com o resto
        //do código a baixo.

        if (idUser != null && !idUser.isEmpty()) {

            FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUser, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
                @Override
                public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {

                    Intent intent = new Intent(NotificationsReceiver.this.getApplicationContext(), ConversaActivity.class);
                    intent.putExtra("usuario", usuarioAtual);
                    intent.putExtra("notificacao", "conversa");
                    intent.putExtra("idNotificacao", 0);
                    //**intent.putExtra(KEY_EXTRA_STRING, minhaString);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    //Configuração do canal
                    String canal = getString(R.string.default_notification_channel_id);

                    //Som da notificação
                    Uri uriSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    PendingIntent pendingIntent = PendingIntent.getActivity(NotificationsReceiver.this.getApplicationContext(),
                            0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    GlideCustomizado.getSharedGlideInstance(getApplicationContext())
                            .asBitmap()
                            .load(fotoRemetente)
                            .circleCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                    NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                                    bigPictureStyle.bigPicture(resource);
                                    //Texto de quando a notificação é expandida.
                                    bigPictureStyle.setSummaryText("Ver mensagens");

                                    NotificationCompat.MessagingStyle messagingStyle =
                                            new NotificationCompat.MessagingStyle("Você");
                                    messagingStyle.setConversationTitle(nomeRemetente);


                                    for (MessageNotificacao mensagemTeste : mensagensParaNotificacao) {
                                        //ToastCustomizado.toastCustomizadoCurto("TESTE " + mensagemTeste.getConteudoMensagem(), NotificationsReceiver.this.getApplicationContext());
                                        NotificationCompat.MessagingStyle.Message notificationMessage =
                                                new NotificationCompat.MessagingStyle.Message(
                                                        mensagemTeste.getConteudoMensagem(),
                                                        mensagemTeste.getTimestampMensagem(),
                                                        mensagemTeste.getNomeRemetente()
                                                );
                                        messagingStyle.addMessage(notificationMessage);
                                    }

                                    // Crie um RemoteViews para o layout personalizado da notificação heads-up
                                    RemoteViews customHeadsUpView = new RemoteViews(getPackageName(), R.layout.custom_heads_up_notification_layout);

                                    if (tipoMensagem.equals("texto")) {
                                        //Estrutura da notificação
                                        NotificationCompat.Builder notificacao = new NotificationCompat.Builder
                                                (NotificationsReceiver.this, canal)
                                                .setSmallIcon(R.drawable.gif_ic_sticker_destaque)
                                                .setLargeIcon(resource)
                                                .setNumber(nrNotificacoes)
                                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                                                .setColor(Color.BLUE)
                                                .addAction(R.drawable.gif_ic_sticker_destaque, "Ver mensagens", pendingIntent)
                                                .setStyle(messagingStyle)
                                                .setSound(uriSound)
                                                .setAutoCancel(true)
                                                .setDefaults(Notification.DEFAULT_ALL)
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(pendingIntent);


                                        //NotificationManager - responsável pelo envio da notificação
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                        //Android >= Oreo, configuração a mais necessária relacionada ao canal.
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            NotificationChannel channel = new NotificationChannel(canal,
                                                    "canal",
                                                    NotificationManager.IMPORTANCE_HIGH);
                                            channel.enableVibration(true); // Ativar a vibração
                                            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null); // Definir o som da notificação
                                            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                                            notificationManager.createNotificationChannel(channel);
                                        }

                                        //Enviar notificação
                                        notificationManager.notify(0, notificacao.build());

                                    } else {

                                        RemoteViews customCollapsedView = new RemoteViews(getPackageName(), R.layout.custom_heads_up_notification_layout);

                                        GlideCustomizado.loadUrlBITMAP(NotificationsReceiver.this.getApplicationContext(),
                                                body, new GlideCustomizado.OnBitmapLoadedListener() {
                                                    @Override
                                                    public void onBitmapLoaded(Bitmap bitmap) {

                                                        Date correctedDate = new Date(timestampInteracao);

                                                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                                        String formattedDate = sdf.format(correctedDate);

                                                        customCollapsedView.setImageViewBitmap(R.id.imgViewUserNotifLocal, resource);
                                                        customCollapsedView.setImageViewBitmap(R.id.imageViewMedia, bitmap);
                                                        customCollapsedView.setTextViewText(R.id.txtViewNomeUserNotifLocal, nomeRemetente);
                                                        customCollapsedView.setTextViewText(R.id.txtViewTimeNotifLocal, formattedDate);

                                                        NotificationCompat.BigPictureStyle bigPictureStyleMidia = new NotificationCompat.BigPictureStyle();
                                                        bigPictureStyleMidia.bigPicture(bitmap);
                                                        bigPictureStyleMidia.setBigContentTitle("Nova mensagem recebida");
                                                        //Texto de quando a notificação é expandida.
                                                        bigPictureStyleMidia.setSummaryText("  Ver mídia " + "{" + tipoMensagem.toUpperCase(Locale.ROOT) + "}");

                                                        NotificationCompat.Builder notificacao = new NotificationCompat.Builder
                                                                (NotificationsReceiver.this, canal)
                                                                .setSmallIcon(R.drawable.gif_ic_sticker_destaque)
                                                                .setContentText("{MÍDIA - " + tipoMensagem.toUpperCase(Locale.ROOT) + "}")
                                                                .setLargeIcon(resource)
                                                                .setNumber(nrNotificacoes)
                                                                .setCustomHeadsUpContentView(customCollapsedView)
                                                                .setCustomContentView(customCollapsedView)
                                                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                                                                .setColor(Color.BLUE)
                                                                .addAction(R.drawable.gif_ic_sticker_destaque, "Ver mensagens", pendingIntent)
                                                                .setStyle(bigPictureStyleMidia)
                                                                .setSound(uriSound)
                                                                .setAutoCancel(true)
                                                                .setDefaults(Notification.DEFAULT_ALL)
                                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                                .setContentIntent(pendingIntent);

                                                        //NotificationManager - responsável pelo envio da notificação
                                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                                        //Android >= Oreo, configuração a mais necessária relacionada ao canal.
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            NotificationChannel channel = new NotificationChannel(canal,
                                                                    "canal",
                                                                    NotificationManager.IMPORTANCE_HIGH);
                                                            channel.enableVibration(true); // Ativar a vibração
                                                            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null); // Definir o som da notificação
                                                            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                                                            notificationManager.createNotificationChannel(channel);
                                                        }

                                                        //Enviar notificação
                                                        notificationManager.notify(0, notificacao.build());
                                                    }
                                                });
                                    }

                                    switch (tipoMensagem) {
                                        case ("texto"):
                                            //addAction está ignorando o drawable.
                                            //notificacao.addAction(R.drawable.gph_ic_text,"",pendingIntent);
                                            break;
                                    }
                                }
                            });
                }

                @Override
                public void onSemDados() {

                }

                @Override
                public void onError(String mensagem) {

                }
            });
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //Somente é chamado quando o usuário instala o app
        //É atribuido ao usuário esse token enquanto o app estiver instalado.
        FcmUtils.salvarTokenAtualNoUserAtual(new FcmUtils.SalvarTokenCallback() {
            @Override
            public void onSalvo(String token) {
                //Salvo com sucesso.
            }

            @Override
            public void onError(String message) {
                //Salvar dado no SQLite para salvar posteriormente no firebase;
            }
        });
    }
}
