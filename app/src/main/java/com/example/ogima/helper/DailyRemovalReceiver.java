package com.example.ogima.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ogima.model.DailyShort;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class DailyRemovalReceiver extends BroadcastReceiver {

    private StorageReference storageRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private HashMap<String, Object> hashMapDadosDaily;
    private final static String TAG = "AGENDAMENTO DAILY";

    @Override
    public void onReceive(Context context, Intent intent) {

        this.storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        String idDailyShort = intent.getStringExtra("idDailyShort");
        String idDonoDailyShort = intent.getStringExtra("idDonoDailyShort");
        hashMapDadosDaily = new HashMap<>();
        hashMapDadosDaily = (HashMap<String, Object>) intent.getSerializableExtra("hashMapDadosDaily");

        String urlMidia = hashMapDadosDaily.get("urlMidia").toString();
        String tipoMidia = hashMapDadosDaily.get("tipoMidia").toString();

        if (idDailyShort != null && idDonoDailyShort != null) {
            // Remove o daily do Firebase Realtime Database usando a referência correta
            DatabaseReference dailyShortRef =
                    firebaseRef.child("dailyShorts")
                            .child(idDonoDailyShort).child(idDailyShort);
            dailyShortRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //Exclui arquivo do storage.
                    if (tipoMidia != null
                            && urlMidia != null && !urlMidia.isEmpty()
                            && !tipoMidia.equals("texto")) {
                        try {
                            storageRef = storageRef.child("dailyShorts")
                                    .child(tipoMidia+"s").child(idDonoDailyShort).getStorage()
                                    .getReferenceFromUrl(urlMidia);
                            Log.d(TAG, "EXCLUIDO DO STORAGE");
                            storageRef.delete();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        //Verifica se existe dailys além desse -
                        DatabaseReference verificaDailysRef = firebaseRef.child("dailyShorts")
                                .child(idDonoDailyShort);

                        verificaDailysRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                }else{
                                    DatabaseReference removerUrlRef = firebaseRef.child("usuarios")
                                            .child(idDonoDailyShort).child("urlLastDaily");

                                    DatabaseReference removerDataRef = firebaseRef.child("usuarios")
                                            .child(idDonoDailyShort).child("dataLastDaily");

                                    DatabaseReference removerStatusRef = firebaseRef.child("usuarios")
                                            .child(idDonoDailyShort).child("dailyShortAtivo");

                                    DatabaseReference removerTipoMidiaRef = firebaseRef.child("usuarios")
                                            .child(idDonoDailyShort).child("tipoMidia");

                                    removerUrlRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            removerDataRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    removerTipoMidiaRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            removerStatusRef.removeValue();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });

                                    DatabaseReference atualizarStatusDailyRef = firebaseRef.child("usuarios")
                                            .child(idDonoDailyShort).child("dailyShortAtivo");
                                    atualizarStatusDailyRef.setValue(false);
                                }
                                verificaDailysRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            });
            Log.d(TAG, "AGENDAMENTO CONCLUIDO");

            // Obtém uma referência para o AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // Cria um PendingIntent para a tarefa
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, idDailyShort.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Cancela o agendamento do PendingIntent
            alarmManager.cancel(pendingIntent);

            Log.d(TAG, "AGENDAMENTO LIMPO");
        }
    }
}
