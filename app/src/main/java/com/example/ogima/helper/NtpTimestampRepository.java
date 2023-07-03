package com.example.ogima.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import com.instacart.library.truetime.TrueTimeRx;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.reactivex.schedulers.Schedulers;


public class NtpTimestampRepository {
    private final String NTP_URL = "time.google.com";

    public interface NtpTimestampCallback {
        void onSuccess(long timestamp, String dataFormatada);
        void onError(String errorMessage);
    }

    @SuppressLint("CheckResult")
    public void getNtpTimestamp(Context context, NtpTimestampCallback callback) {
        if (!TrueTimeRx.isInitialized()) {
            TrueTimeRx.clearCachedInfo();
            TrueTimeRx.build()
                    .withSharedPreferencesCache(context)
                    .withLoggingEnabled(true)
                    .withConnectionTimeout(31428)
                    .initializeRx(NTP_URL)
                    .subscribeOn(Schedulers.io())
                    .subscribe(date -> {
                        Date currentDateTime = TrueTimeRx.now();
                        long correctedTimestamp = currentDateTime.getTime();
                        Date correctedDate = new Date(correctedTimestamp);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        String formattedDate = sdf.format(correctedDate);

                        callback.onSuccess(correctedTimestamp, formattedDate);
                    }, throwable -> {
                        callback.onError("Error initializing TrueTimeRx");
                    });
        } else {
            Date currentDateTime = TrueTimeRx.now();
            long correctedTimestamp = currentDateTime.getTime();
            Date correctedDate = new Date(correctedTimestamp);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(correctedDate);

            callback.onSuccess(correctedTimestamp, formattedDate);
        }
    }
}

