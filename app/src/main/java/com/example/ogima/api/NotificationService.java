package com.example.ogima.api;

import com.example.ogima.BuildConfig;
import com.example.ogima.model.NotificacaoDados;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationService {
    @Headers({
            "Authorization:key="+ BuildConfig.SEND_FCM_API_ACCESS,
            "Content-Type:application/json"
    })
    @POST("send")
    Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);
}
