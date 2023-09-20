package com.example.ogima.helper;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class FirebaseUtils {

    public void removerChildListener(DatabaseReference reference, ChildEventListener childEventListener) {
        if (childEventListener != null) {
            reference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    public void removerValueListener(DatabaseReference reference, ValueEventListener valueEventListener) {
        if (valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }
}
