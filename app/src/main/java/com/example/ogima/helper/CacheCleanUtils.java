package com.example.ogima.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class CacheCleanUtils {

    private static final int CACHE_CLEAN_ALARM_ID = 100;
    private static final long INTERVAL_24_HOURS = 24 * 60 * 60 * 1000;

    public static void scheduleCacheClean(Context context) {
        Log.d("CACHE", "AGENDADO LIMPEZA");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CacheCleanReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CACHE_CLEAN_ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Define o intervalo de repetição de 24 horas
        long triggerTime = Calendar.getInstance().getTimeInMillis() + INTERVAL_24_HOURS;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, INTERVAL_24_HOURS, pendingIntent);
    }

    public static void cancelCacheClean(Context context) {
        Log.d("CACHE", "LIMPEZA CANCELADA");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CacheCleanReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CACHE_CLEAN_ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
