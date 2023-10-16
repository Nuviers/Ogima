package com.example.ogima.helper;

import android.app.Activity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseUtils {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private FirebaseUser user;

    public interface VerificarEmailCallback{
        void onVerificado(boolean verificado);
        void onError(String message);
    }

    public void removerRefChildListener(DatabaseReference reference, ChildEventListener childEventListener) {
        if (childEventListener != null) {
            reference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    public void removerQueryChildListener(Query reference, ChildEventListener childEventListener) {
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

    public void verificarStatusEmail(VerificarEmailCallback callback){
        if (autenticacao != null && autenticacao.getCurrentUser() != null) {
            user = autenticacao.getCurrentUser();
            user.reload()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Recarregamento do usuário bem-sucedido
                                user = autenticacao.getCurrentUser();
                                if (user != null && !user.isEmailVerified()) {
                                    callback.onVerificado(false);
                                } else if (user != null && user.isEmailVerified()) {
                                    // O email está verificado
                                    callback.onVerificado(true);
                                } else {
                                    // O usuário é nulo (não autenticado)
                                    callback.onVerificado(false);
                                }
                            } else {
                                // Falha no recarregamento do usuário
                                callback.onVerificado(false);
                            }
                        }
                    });
        } else {
            // O usuário é nulo (não autenticado)
            callback.onVerificado(false);
        }
    }
}
