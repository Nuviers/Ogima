package com.example.ogima.helper;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ogima.R;
import com.example.ogima.activity.ConversaActivity;
import com.example.ogima.activity.NotificationsTesteActivity;
import com.example.ogima.activity.SplashActivity;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class NotificationsReceiver extends FirebaseMessagingService {

    private static final String TAG = "RECEIVERGFCM";
    private static final String KEY_EXTRA_STRING = "usuario";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //onMessageReceived - lógica executada quando o app está aberto e em primeiro plano.

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Obtém a string extra do payload de dados, se existir
        String minhaString = "";
        String nome = "";
        String idade = "";
        if (remoteMessage.getData() != null) {
            nome = remoteMessage.getData().get("name");
            idade = remoteMessage.getData().get("age");
            //minhaString = remoteMessage.getData().get(KEY_EXTRA_STRING);
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        sendNotificationResult(remoteMessage.getFrom(), remoteMessage.getNotification().getBody(), nome, idade);
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);
        }
    }

    private void sendNotificationResult(String from, String body, String nome, String idade) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ToastCustomizado.toastCustomizado("De: " + from + " " + "conteúdo " + body, NotificationsReceiver.this.getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Nome: " + nome, NotificationsReceiver.this.getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Idade: " + idade, NotificationsReceiver.this.getApplicationContext());
            }
        });
    }


    public void sendNotification(String title, String body) {

        //Somente executado quando o app estiver aberto e em primeiro plano.
        // (recebe os dados que já foram configurados e envia a notificação por esse método)

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto("YWxlc3NhbmRyb21lbG9kZWFuZHJhZGVqdW5pb3JAZ21haWwuY29t", new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                Intent intent = new Intent(NotificationsReceiver.this.getApplicationContext(), ConversaActivity.class);
                intent.putExtra("usuario", usuarioAtual);
                intent.putExtra("notificacao", "conversa");
                //**intent.putExtra(KEY_EXTRA_STRING, minhaString);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                //Configuração do canal
                String canal = getString(R.string.default_notification_channel_id);

                //Som da notificação
                Uri uriSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                PendingIntent pendingIntent = PendingIntent.getActivity(NotificationsReceiver.this.getApplicationContext(),
                        0, intent, PendingIntent.FLAG_ONE_SHOT);

                GlideCustomizado.getSharedGlideInstance(getApplicationContext())
                        .asBitmap()
                        .load(fotoUsuario)
                        .circleCrop()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                                bigPictureStyle.bigPicture(resource);
                                //Texto de quando a notificação é expandida.
                                bigPictureStyle.setSummaryText("Ver mensagens");

                                //Estrutura da notificação
                                NotificationCompat.Builder notificacao = new NotificationCompat.Builder
                                        (NotificationsReceiver.this, canal)
                                        .setContentTitle(title)
                                        .setContentText(body)
                                        .setSmallIcon(R.drawable.gif_ic_sticker_destaque)
                                        .setLargeIcon(resource)
                                        .setStyle(bigPictureStyle)
                                        .setSound(uriSound)
                                        .setAutoCancel(true)
                                        .setContentIntent(pendingIntent);

                                //NotificationManager - responsável pelo envio da notificação
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                //Android >= Oreo, configuração a mais necessária relacionada ao canal.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel(canal,
                                            "canal",
                                            NotificationManager.IMPORTANCE_DEFAULT);
                                    notificationManager.createNotificationChannel(channel);
                                }

                                //Enviar notificação
                                notificationManager.notify(0, notificacao.build());
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

    public void sendNotificationTESTE(String title, String body) {

        //Somente executado quando o app estiver aberto e em primeiro plano.
        // (recebe os dados que já foram configurados e envia a notificação por esse método)

        //Configuração do canal
        String canal = getString(R.string.default_notification_channel_id);

        //Som da notificação
        Uri uriSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //PedingIntent - Responsável por acionar o intent ao clicar na notificação.
        //PendingIntent.FLAG_ONE_SHOT - Intent será executada uma única vez ao clicar na notificação.
        Intent intent = new Intent(NotificationsReceiver.this.getApplicationContext(), NotificationsTesteActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(NotificationsReceiver.this.getApplicationContext(),
                0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Estrutura da notificação
        NotificationCompat.Builder notificacao = new NotificationCompat.Builder
                (NotificationsReceiver.this, canal)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.gif_ic_sticker_destaque)
                .setSound(uriSound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        //NotificationManager - responsável pelo envio da notificação
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Android >= Oreo, configuração a mais necessária relacionada ao canal.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(canal,
                    "canal",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        //Enviar notificação
        notificationManager.notify(0, notificacao.build());
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

    public void sendNotification2607(String messageBody, String minhaString) {

        FirebaseRecuperarUsuario.recuperaUsuario("YWxlc3NhbmRyb21lbG9kZWFuZHJhZGVqdW5pb3JAZ21haWwuY29t", new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {

                Intent intent = new Intent(NotificationsReceiver.this.getApplicationContext(), ConversaActivity.class);
                intent.putExtra("usuario", usuarioAtual);
                intent.putExtra("voltarChatFragment", "ChatInicioActivity.class");
                //**intent.putExtra(KEY_EXTRA_STRING, minhaString);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(NotificationsReceiver.this.getApplicationContext(), 0 /* Request code */, intent,
                        PendingIntent.FLAG_IMMUTABLE);

                String channelId = getString(R.string.default_notification_channel_id);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(NotificationsReceiver.this.getApplicationContext(), channelId)
                                .setSmallIcon(R.drawable.sticker_acamp_excluir)
                                //*.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Novo teste de notificação")
                                .setContentText(messageBody)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            "Channel human readable title",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }
}
