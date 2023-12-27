package com.example.ogima.helper;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimestampUtils {

    public interface RecuperarTimestampCallback {
        void onRecuperado(long timestampNegativo);

        void onError(String message);
    }

    public interface RecuperarHoraTimestampCallback{
        void onConcluido(String horaMinuto);
        void onError(String message);
    }

    public static void RecuperarTimestamp(Context context, RecuperarTimestampCallback callback){
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(context, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        callback.onRecuperado(timestampNegativo);
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

    public static void RecuperarHoraDoTimestamp(Context context, boolean converterParaPositivo, long timestampAlvo, RecuperarHoraTimestampCallback callback){
        if (converterParaPositivo) {
            timestampAlvo = timestampAlvo * -1;
        }
        timestampAlvo = timestampAlvo * 1000L; // Convertendo para milissegundos

        // Criando um objeto Date com o timestamp
        Date date = new Date(timestampAlvo);

        // Obtendo o fuso horário padrão do dispositivo Android
        TimeZone timeZone = TimeZone.getDefault();

        // Criando um objeto SimpleDateFormat para formatar a data com base no fuso horário do usuário
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(timeZone);

        // Convertendo o objeto Date para uma string no formato desejado
        String horaMinuto = sdf.format(date);
        callback.onConcluido(horaMinuto);
    }
}
