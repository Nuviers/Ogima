package com.example.ogima.helper;

import android.content.Context;
import android.os.Handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NtpSyncService {
    private static long ntpOffset = 0; // Offset entre o tempo do dispositivo e o NTP
    private static final long SYNC_INTERVAL = 5 * 60 * 1000; // 5 minutos em milissegundos
    private static boolean isSyncStarted = false; // Flag to prevent multiple syncs

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static synchronized void startSync(final Context context) {
        if (!isSyncStarted) {
            isSyncStarted = true;
            Runnable syncTask = new Runnable() {
                @Override
                public void run() {
                    NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
                    ntpTimestampRepository.getNtpTimestamp(context, new NtpTimestampRepository.NtpTimestampCallback() {
                        @Override
                        public void onSuccess(long ntpTimestamp, String dataFormatada) {
                            long deviceTime = System.currentTimeMillis();
                            ntpOffset = ntpTimestamp - deviceTime;
                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });
                }
            };
            // Schedule the task to run initially and then at fixed intervals
            scheduler.scheduleAtFixedRate(syncTask, 0, SYNC_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    public static long getAdjustedCurrentTime() {
        return System.currentTimeMillis() + ntpOffset;
    }
}
