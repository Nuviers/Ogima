package com.example.ogima.helper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;

public class OnDisconnectUtils {
    public static void onDisconnectRemoveValue(DatabaseReference ref) {
        OnDisconnect onDisconnect = ref.onDisconnect();
        onDisconnect.removeValue();
    }

    public static void cancelarOnDisconnect(DatabaseReference ref) {
        OnDisconnect onDisconnect = ref.onDisconnect();
        onDisconnect.cancel();
    }
}
