package com.example.ogima.helper;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class DadosUserUtils {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    public interface RecuperarNomeCallback{
        void onRecuperado(String nome);
        void onSemDado();
        void onError(String message);
    }

    public interface RecuperarGeneroCallback{
        void onRecuperado(String genero);
        void onSemDado();
        void onError(String message);
    }

    public interface RecuperarInteressesCallback {
        void onRecuperado(ArrayList<String> listaInteresses);
        void onSemDado();
        void onError(String message);
    }

    public void recuperarNome(String idUser, RecuperarNomeCallback callback){
        DatabaseReference nomeRef = firebaseRef.child("usuarios")
                .child(idUser).child("nomeUsuario");
        nomeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String nome = snapshot.getValue(String.class);
                    if (nome != null
                            && !nome.isEmpty()) {
                        callback.onRecuperado(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nome));
                    }else{
                        callback.onSemDado();
                    }
                }else{
                    callback.onSemDado();
                }
                nomeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void recuperarGenero(String idUser, RecuperarGeneroCallback callback){
        DatabaseReference generoRef = firebaseRef.child("usuarios")
                .child(idUser).child("generoUsuario");
        generoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String genero = snapshot.getValue(String.class);
                    if (genero != null
                            && !genero.isEmpty()) {
                        callback.onRecuperado(genero.toLowerCase(Locale.ROOT));
                    }else{
                        callback.onSemDado();
                    }
                }else{
                    callback.onSemDado();
                }
                generoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void recuperarInteresses(String idUser, RecuperarInteressesCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference interessesRef = firebaseRef.child("usuarios")
                .child(idUser).child("interesses");
        interessesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> typeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                    ArrayList<String> listaInteresses = snapshot.getValue(typeIndicator);
                    if (listaInteresses != null && listaInteresses.size() > 0) {
                        callback.onRecuperado(listaInteresses);
                    } else {
                        callback.onSemDado();
                    }
                } else {
                    callback.onSemDado();
                }
                interessesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
