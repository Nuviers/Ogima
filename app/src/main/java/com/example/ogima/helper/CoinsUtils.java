package com.example.ogima.helper;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CoinsUtils {

    public interface CoinsListener {
        void onChecked();
        void onError(String errorMessage);
    }

    private interface RecuperarTimeStamp{
        void onRecuperado(long timeStampAtual);
        void onError(String message);
    }

    public static void verificaTimeAd(Context context, String idUsuarioAtual, CoinsListener coinsListener) {

        //Verifica se o primeiro anúncio visto anteriormente já passou de 12 horas.

        recuperarTimeStampAtual(context, new RecuperarTimeStamp() {

            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

            DatabaseReference limiteAdsRef = firebaseRef.child("usuarios")
                    .child(idUsuarioAtual).child("timeStampResetarLimiteAds");

            DatabaseReference adsVisualizadasRef = firebaseRef.child("usuarios")
                    .child(idUsuarioAtual).child("nrAdsVisualizadas");

            @Override
            public void onRecuperado(long timeStampAtual) {
                limiteAdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            long timeValidade = snapshot.getValue(Long.class);
                            if (timeStampAtual <= timeValidade) {
                                adsVisualizadasRef.setValue(0);
                                limiteAdsRef.removeValue();
                                coinsListener.onChecked();
                            }else{
                                coinsListener.onChecked();
                            }
                        }else{
                            coinsListener.onChecked();
                        }
                        limiteAdsRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        coinsListener.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private static void recuperarTimeStampAtual(Context context, RecuperarTimeStamp recupTimeStampCallback){
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(context, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        //ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timeStampNegativo, getApplicationContext());
                        recupTimeStampCallback.onRecuperado(timestampNegativo);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, context);
                        recupTimeStampCallback.onError(errorMessage);
                    }
                });
            }
        });
    }
}
