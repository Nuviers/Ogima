package com.example.ogima.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import com.instacart.library.truetime.TrueTimeRx;
import io.reactivex.schedulers.Schedulers;


public class NtpTimestampRepository {
    private final String NTP_URL = "https://time.google.com";

    public interface NtpTimestampCallback {
        void onSuccess(long timestamp);
        void onError(String errorMessage);
    }

    @SuppressLint("CheckResult")
    public void getNtpTimestamp(Context context, NtpTimestampCallback callback) {

        TrueTimeRx.build()
                .withSharedPreferencesCache(context)
                .withLoggingEnabled(true)
                .withConnectionTimeout(31428)
                .initializeRx(NTP_URL)
                .subscribeOn(Schedulers.io())
                .subscribe(date -> {
                    long timestamp = date.getTime();
                    callback.onSuccess(timestamp);
                }, throwable -> {
                    callback.onError("Error fetching timestamp");
                });
    }
}
