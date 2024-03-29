package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.ogima.R;
import com.example.ogima.model.Grupo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupUtils {
    private Activity activity;
    private Context context;
    public static final int MAX_NUMBER_PARTICIPANTS = 200;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    public GroupUtils(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public GroupUtils(Context context) {
        this.context = context;
    }

    public interface ConfigBundleCallback {
        void onCadastro();

        void onEdicao(Grupo dadosEdicao);

        void onSemDado();
    }

    public interface TopicosAnterioresCallback {
        void onConcluido(ArrayList<String> topicosAnteriores);

        void onError(String message);
    }

    public interface RecuperarTimestampCriacaoCallback {
        void onConcluido(long timestamp);

        void onError(String message);
    }

    public void configurarBundle(Bundle dados, ConfigBundleCallback callback) {
        if (dados != null) {
            if (dados.containsKey("edit")) {
                boolean edicao;
                edicao = dados.getBoolean("edit");
                if (edicao) {
                    if (dados.containsKey("dadosEdicao")
                            && dados.getSerializable("dadosEdicao") != null) {
                        Grupo grupoEdicao = (Grupo) dados.getSerializable("dadosEdicao");
                        callback.onEdicao(grupoEdicao);
                    } else {
                        callback.onSemDado();
                    }
                    return;
                }
                callback.onCadastro();
            } else {
                callback.onSemDado();
            }
        } else {
            callback.onSemDado();
        }
    }

    public void recuperarTopicosAnteriores(String idGrupo, TopicosAnterioresCallback callback) {
        if (idGrupo == null || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarTopicosRef = firebaseRef.child("grupos")
                .child(idGrupo).child("topicos");
        recuperarTopicosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    ArrayList<String> listaTopicos = snapshot.getValue(t);
                    if (listaTopicos != null
                            && listaTopicos.size() > 0) {
                        callback.onConcluido(listaTopicos);
                    }
                }
                recuperarTopicosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public void recuperarTimestampCriacao(String idGrupo, RecuperarTimestampCriacaoCallback callback) {
        if (idGrupo == null || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarTimestampRef = firebaseRef.child("grupos")
                .child(idGrupo).child("timestampinteracao");
        recuperarTimestampRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    long timestamp = snapshot.getValue(Long.class);
                    callback.onConcluido(timestamp);
                } else {
                    TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                        @Override
                        public void onRecuperado(long timestampNegativo) {
                            callback.onConcluido(timestampNegativo);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                }
                recuperarTimestampRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

}
