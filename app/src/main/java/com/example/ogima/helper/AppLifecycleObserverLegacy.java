package com.example.ogima.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AppLifecycleObserverLegacy implements Application.ActivityLifecycleCallbacks {

    private int numActivities = 0;
    private boolean isAppInForeground = false;

    public void register(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (numActivities == 0) {
            // O aplicativo estava em segundo plano, o usuário está voltando
            isAppInForeground = true;
            // Definir o status como "online" (true)
            setUserOnlineStatus(true);
        }
        numActivities++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        numActivities--;
        if (numActivities == 0) {
            // A última atividade está saindo do primeiro plano, o usuário está fechando o app
            isAppInForeground = false;
            // Aguardar um pequeno intervalo de tempo antes de definir como "offline"
            // para lidar com a transição entre atividades
            new Handler().postDelayed(() -> {
                if (!isAppInForeground) {
                    // O aplicativo não está em primeiro plano, definir o status como "offline" (false)
                    setUserOnlineStatus(false);
                }
            }, 1000);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    private void setUserOnlineStatus(boolean isOnline) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            UsuarioUtils.AtualizarStatusOnline(isOnline);
        }
    }
}