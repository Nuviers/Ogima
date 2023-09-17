package com.example.ogima.activity;

import android.app.Application;
import android.os.Build;
import android.os.Handler;

import com.example.ogima.helper.AppLifecycleObserver;
import com.example.ogima.helper.AppLifecycleObserverLegacy;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.annotations.NonNull;

public class GerenciarStatusOnline extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerActivityLifecycleCallbacks(new AppLifecycleObserver(getApplicationContext()));
        }else{
            // Para versões anteriores ao Android Q
            AppLifecycleObserverLegacy observerLegacy = new AppLifecycleObserverLegacy();
            observerLegacy.register(this);
        }
    }
}
