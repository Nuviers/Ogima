package com.example.ogima.helper;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SeguindoUtils {

    public interface SalvarSeguindoCallback {
        void onSeguindoSalvo();

        void onError(@NonNull String message);
    }

    public interface RemoverSeguindoCallback {
        void onRemovido();

        void onError(@NonNull String message);
    }

    public interface AtualizarListaSeguindoCallback {
        void onAtualizado(ArrayList<String> listaAtualizada);

        void onError(String message);
    }

    public interface SalvarTimestampCallback {
        void onRecuperado(long timestampnegativo);

        void onError(String message);
    }

    public interface PrepararListaCallback {
        void onProsseguir(ArrayList<String> listaAtualizada);

        void onIgnorar();

        void onError(String message);
    }

    public interface RecuperarTimestampCallback {
        void onRecuperado(long timestampNegativo);

        void onError(String message);
    }

    public static void salvarSeguindo(Context context, @NonNull String idSeguindo, @NonNull SalvarSeguindoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        recuperarTimestampnegativo(context, new RecuperarTimestampCallback() {
            @Override
            public void onRecuperado(long timestampNegativo) {
                salvarIdSeguindo(idUsuario, idSeguindo, new AtualizarListaSeguindoCallback() {
                    @Override
                    public void onAtualizado(ArrayList<String> listaAtualizada) {
                        HashMap<String, Object> operacoes = new HashMap<>();
                        operacoes.put("/seguindo/" + idUsuario + "/" + idSeguindo + "/timestampinteracao", timestampNegativo);
                        operacoes.put("/seguindo/" + idUsuario + "/" + idSeguindo + "/idUsuario", idSeguindo);
                        operacoes.put("/seguidores/" + idSeguindo + "/" + idUsuario + "/idUsuario", idUsuario);
                        operacoes.put("/seguidores/" + idSeguindo + "/" + idUsuario + "/timestampinteracao", timestampNegativo);
                        operacoes.put("/usuarios/" + idUsuario + "/seguindoUsuario", ServerValue.increment(1));
                        operacoes.put("/usuarios/" + idSeguindo + "/seguidoresUsuario", ServerValue.increment(1));
                        operacoes.put("/usuarios/" + idUsuario + "/listaIdSeguindo/", listaAtualizada);
                        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    callback.onSeguindoSalvo();
                                } else {
                                    callback.onError(String.valueOf(error.getCode()));
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public static void salvarIdSeguindo(@NonNull String idDados, @NonNull String idParaAdicionar, AtualizarListaSeguindoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recuperarLista = firebaseRef.child("usuarios")
                .child(idDados).child("listaIdSeguindo");
        recuperarLista.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    ArrayList<String> listaIds = snapshot.getValue(t);
                    if (listaIds != null
                            && listaIds.size() > 0) {
                        if (!listaIds.contains(idParaAdicionar)) {
                            //Somente adiciona se não conter tal id na lista.
                            listaIds.add(idParaAdicionar);
                            callback.onAtualizado(listaIds);
                        }
                    }
                } else {
                    ArrayList<String> listaIds = new ArrayList<>();
                    listaIds.add(idParaAdicionar);
                    callback.onAtualizado(listaIds);
                }
                recuperarLista.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public static void removerSeguindo(Context context, @NonNull String idAlvo, @NonNull RemoverSeguindoCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        HashMap<String, Object> operacoes = new HashMap<>();
        operacoes.put("/seguindo/" + idUsuario + "/" + idAlvo, null);
        operacoes.put("/seguidores/" + idAlvo + "/" + idUsuario, null);
        operacoes.put("/usuarios/" + idUsuario + "/seguindoUsuario", ServerValue.increment(-1));
        operacoes.put("/usuarios/" + idAlvo + "/seguidoresUsuario", ServerValue.increment(-1));
        removerIdSeguindoDoUsuario(context, idUsuario, idAlvo, new PrepararListaCallback() {
            @Override
            public void onProsseguir(ArrayList<String> listaAtualizada) {
                operacoes.put("/usuarios/" + idUsuario + "/listaIdSeguindo/", listaAtualizada);
                salvarHashMapDeixarDeSeguir(operacoes, callback);
            }

            @Override
            public void onIgnorar() {
                callback.onRemovido();
                salvarHashMapDeixarDeSeguir(operacoes, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private static void salvarHashMapDeixarDeSeguir(HashMap<String, Object> operacoes, RemoverSeguindoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    //Prosseguir
                    callback.onRemovido();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    private static void removerIdSeguindoDoUsuario(Context context, @NonNull String idDados, String idARemover, @NonNull PrepararListaCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idDados, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (listaIdSeguindo != null && listaIdSeguindo.size() > 0) {
                    if (listaIdSeguindo.contains(idARemover)) {
                        listaIdSeguindo.remove(idARemover);
                        callback.onProsseguir(listaIdSeguindo);
                    } else {
                        callback.onIgnorar();
                    }
                } else {
                    callback.onIgnorar();
                }
            }

            @Override
            public void onSemDados() {
                callback.onError(context.getString(R.string.error_recovering_data));
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    public static void recuperarTimestampnegativo(Context context, RecuperarTimestampCallback callback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(context, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        callback.onRecuperado(timestampNegativo);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }
}