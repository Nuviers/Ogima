package com.example.ogima.helper;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ogima.R;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class FriendsUtils {

    public interface DesfazerAmizadeCallback {
        void onAmizadeDesfeita();

        void onError(@NonNull String message);
    }

    public interface VerificaAmizadeCallback {
        void onAmigos();

        void onNaoSaoAmigos();

        void onError(String message);
    }

    public interface VerificaConviteCallback {
        void onConvitePendente(boolean destinatario);

        void onSemConvites();

        void onError(String message);
    }

    public interface RemoverConviteCallback {
        void onRemovido(HashMap<String, Object> operacoes);

        void onError(String message);
    }

    public interface RecuperarTimestampCallback {
        void onRecuperado(long timestampNegativo);

        void onError(String message);
    }

    public interface EnviarConviteCallback {
        void onConviteEnviado();

        void onJaExisteConvite();

        void onError(String message);
    }

    public interface PrepararListaAmigoCallback {
        void onProsseguir(ArrayList<String> listaAtualizada);

        void onExcluidoAnteriormente();

        void onError(String message);
    }

    public interface AtualizarListaAmigosCallback {
        void onAtualizado(ArrayList<String> listaAtualizada);

        void onError(String message);
    }

    public interface LimparLockUnfriendCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface AdicionarAmigoCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface AjustarContatoCallback {
        void onAjustar(String nivelAmizade, long totalMensagens);

        void onError(String message);
    }

    private static void adicionarIdAmigo(@NonNull String idDados, @NonNull String idParaAdicionar, @NonNull AtualizarListaAmigosCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recuperarLista = firebaseRef.child("usuarios")
                .child(idDados).child("listaIdAmigos");
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

    public static void desfazerAmizade(Context context, String idDestinatario, DesfazerAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        HashMap<String, Object> operacoes = new HashMap<>();
        operacoes.put("/lockUnfriend/" + idUsuario + "/" + idDestinatario + "/" + "idRemetente", idUsuario);
        operacoes.put("/lockUnfriend/" + idUsuario + "/" + idDestinatario + "/" + "idDestinatario", idDestinatario);
        operacoes.put("/lockUnfriend/" + idDestinatario + "/" + idUsuario + "/" + "idRemetente", idUsuario);
        operacoes.put("/lockUnfriend/" + idDestinatario + "/" + idUsuario + "/" + "idDestinatario", idDestinatario);
        removerIdAmigoDoUsuario(context, idUsuario, idDestinatario, new PrepararListaAmigoCallback() {
            @Override
            public void onProsseguir(ArrayList<String> listaAtualizada) {
                operacoes.put("/usuarios/" + idUsuario + "/listaIdAmigos/", listaAtualizada);
                operacoesDesfazerAmizade(context, operacoes, idDestinatario, callback);
            }

            @Override
            public void onExcluidoAnteriormente() {
                callback.onAmizadeDesfeita();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private static void operacoesDesfazerAmizade(Context context, HashMap<String, Object> operacoes, String idDestinatario, DesfazerAmizadeCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        removerIdAmigoDoUsuario(context, idDestinatario, idUsuario, new PrepararListaAmigoCallback() {
            @Override
            public void onProsseguir(ArrayList<String> listaAtualizada) {
                operacoes.put("/usuarios/" + idDestinatario + "/listaIdAmigos/", listaAtualizada);
                operacoes.put("/usuarios/" + idUsuario + "/amigosUsuario", ServerValue.increment(-1));
                operacoes.put("/usuarios/" + idDestinatario + "/amigosUsuario", ServerValue.increment(-1));
                operacoes.put("/contatos/" + idUsuario + "/" + idDestinatario, null);
                operacoes.put("/contatos/" + idDestinatario + "/" + idUsuario, null);
                operacoes.put("/friends/" + idUsuario + "/" + idDestinatario, null);
                operacoes.put("/friends/" + idDestinatario + "/" + idUsuario, null);
                salvarHashMapUnfriend(idUsuario, idDestinatario, operacoes, callback);
            }

            @Override
            public void onExcluidoAnteriormente() {
                callback.onAmizadeDesfeita();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private static void limparLockUnfriend(String idUsuario, String idDestinatario, OnDisconnect onDisconnectLockAtual, OnDisconnect onDisconnectLockAlvo, LimparLockUnfriendCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        HashMap<String, Object> limparLock = new HashMap<>();
        limparLock.put("/lockUnfriend/" + idDestinatario + "/" + idUsuario, null);
        limparLock.put("/lockUnfriend/" + idUsuario + "/" + idDestinatario, null);
        firebaseRef.updateChildren(limparLock).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                onDisconnectLockAtual.cancel();
                onDisconnectLockAlvo.cancel();
                callback.onConcluido();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private static void salvarHashMapUnfriend(String idUsuario, String idDestinatario, HashMap<String, Object> operacoes, DesfazerAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference lockAtualRef = firebaseRef.child("lockUnfriend")
                .child(idUsuario).child(idDestinatario);
        DatabaseReference lockDestinatarioRef = firebaseRef.child("lockUnfriend")
                .child(idDestinatario).child(idUsuario);
        OnDisconnect onDisconnectLockAtual = lockAtualRef.onDisconnect();
        OnDisconnect onDisconnectLockAlvo = lockDestinatarioRef.onDisconnect();
        onDisconnectLockAtual.removeValue();
        onDisconnectLockAlvo.removeValue();
        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    //Prosseguir
                    limparLockUnfriend(idUsuario, idDestinatario, onDisconnectLockAtual, onDisconnectLockAlvo, new LimparLockUnfriendCallback() {
                        @Override
                        public void onConcluido() {
                            callback.onAmizadeDesfeita();
                        }

                        @Override
                        public void onError(String message) {
                            callback.onAmizadeDesfeita();
                        }
                    });
                } else {
                    if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                        //A amizade já está sendo desfeita por outro usuário.
                        callback.onAmizadeDesfeita();
                    } else {
                        callback.onError(String.valueOf(error.getCode()));
                    }
                }
            }
        });
    }

    private static void removerIdAmigoDoUsuario(Context context, @NonNull String idDados, String idARemover, @NonNull PrepararListaAmigoCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idDados, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    if (listaIdAmigos.contains(idARemover)) {
                        listaIdAmigos.remove(idARemover);
                        callback.onProsseguir(listaIdAmigos);
                    } else {
                        callback.onExcluidoAnteriormente();
                    }
                } else {
                    callback.onExcluidoAnteriormente();
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

    public static void VerificaAmizade(String idDestinatario, VerificaAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference verificaAmizadeRef = firebaseRef.child("friends")
                .child(idUsuario).child(idDestinatario).child("idUsuario");

        verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onAmigos();
                } else {
                    callback.onNaoSaoAmigos();
                }
                verificaAmizadeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void VerificaConvite(String idDestinatario, VerificaConviteCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference verificaConviteRef = firebaseRef.child("requestsFriendship")
                .child(idUsuario).child(idDestinatario);

        verificaConviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioConvite = snapshot.getValue(Usuario.class);
                    if (usuarioConvite != null) {
                        //Retorna true se o usuário atual for o destinatário e false se for o remetente.
                        callback.onConvitePendente(usuarioConvite.getIdDestinatario().equals(idUsuario));
                    }
                } else {
                    callback.onSemConvites();
                }
                verificaConviteRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public static void adicionarAmigo(Context context, String idAlvo, boolean ignorarConvite, AdicionarAmigoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                .child(idUsuario).child(idAlvo);
        DatabaseReference conviteAmizadeSelecionadoRef = firebaseRef.child("requestsFriendship")
                .child(idAlvo).child(idUsuario);
        OnDisconnectUtils.onDisconnectRemoveValue(conviteAmizadeRef);
        OnDisconnectUtils.onDisconnectRemoveValue(conviteAmizadeSelecionadoRef);

        recuperarTimestampnegativo(context, new RecuperarTimestampCallback() {
            String caminhoFriendsAtual = "/friends/" + idUsuario + "/" + idAlvo + "/";
            String caminhoFriendsAlvo = "/friends/" + idAlvo + "/" + idUsuario + "/";
            String caminhoUsuarioAtual = "/usuarios/" + idUsuario + "/";
            String caminhoUsuarioAlvo = "/usuarios/" + idAlvo + "/";

            @Override
            public void onRecuperado(long timestampNegativo) {
                removerConvites(context, idAlvo, ignorarConvite, false, new RemoverConviteCallback() {
                    @Override
                    public void onRemovido(HashMap<String, Object> operacoes) {
                        operacoes.put(caminhoFriendsAtual + "idUsuario", idAlvo);
                        operacoes.put(caminhoFriendsAlvo + "idUsuario", idUsuario);
                        operacoes.put(caminhoFriendsAtual + "timestampinteracao", timestampNegativo);
                        operacoes.put(caminhoFriendsAlvo + "timestampinteracao", timestampNegativo);
                        adicionarIdAmigo(idUsuario, idAlvo, new AtualizarListaAmigosCallback() {
                            @Override
                            public void onAtualizado(ArrayList<String> listaAtualizada) {
                                operacoes.put(caminhoUsuarioAtual + "listaIdAmigos/", listaAtualizada);
                                adicionarIdAmigo(idAlvo, idUsuario, new AtualizarListaAmigosCallback() {
                                    @Override
                                    public void onAtualizado(ArrayList<String> listaAtualizada) {
                                        operacoes.put(caminhoUsuarioAlvo + "listaIdAmigos/", listaAtualizada);
                                        operacoes.put(caminhoUsuarioAtual + "amigosUsuario", ServerValue.increment(1));
                                        operacoes.put(caminhoUsuarioAlvo + "amigosUsuario", ServerValue.increment(1));
                                        adicionarContato(context, idAlvo, operacoes, callback);
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

                    @Override
                    public void onError(String message) {
                        OnDisconnectUtils.cancelarOnDisconnect(conviteAmizadeRef);
                        OnDisconnectUtils.cancelarOnDisconnect(conviteAmizadeSelecionadoRef);
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

    public static void removerConvites(Context context, String idAlvo, boolean ignorarConvite, boolean recusarConvite, RemoverConviteCallback callback) {
        HashMap<String, Object> operacoes = new HashMap<>();
        if (ignorarConvite) {
            //Usado pela lógica do QRCode onde o convite de amizade não é necessário.
            callback.onRemovido(operacoes);
            return;
        }
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                .child(idUsuario).child(idAlvo);
        conviteAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    if (usuario != null
                            && usuario.getIdDestinatario() != null
                            && !usuario.getIdDestinatario().isEmpty()) {
                        String idDestinatarioConvite = usuario.getIdDestinatario();
                        String caminhoConviteAtual = "/requestsFriendship/" + idUsuario + "/" + idAlvo;
                        String caminhoConviteAlvo = "/requestsFriendship/" + idAlvo + "/" + idUsuario;
                        operacoes.put(caminhoConviteAtual, null);
                        operacoes.put(caminhoConviteAlvo, null);
                        if (recusarConvite) {
                            operacoes.put("/usuarios/" + idUsuario + "/" + "pedidosAmizade", ServerValue.increment(-1));
                            firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    if (error == null) {
                                        //Prosseguir
                                        callback.onRemovido(operacoes);
                                    } else {
                                        if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                                            //A amizade já está sendo desfeita por outro usuário.
                                            callback.onRemovido(operacoes);
                                        } else {
                                            callback.onError(String.valueOf(error.getCode()));
                                        }
                                    }
                                }
                            });
                        } else {
                            operacoes.put("/usuarios/" + idDestinatarioConvite + "/" + "pedidosAmizade", ServerValue.increment(-1));
                            callback.onRemovido(operacoes);
                        }
                    } else {
                        callback.onError(context.getString(R.string.error_recovering_data));
                    }
                }
                conviteAmizadeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void adicionarContato(Context context, String idAlvo, HashMap<String, Object> operacoes, AdicionarAmigoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        String caminhoContatoAtual = "/contatos/" + idUsuario + "/" + idAlvo + "/";
        operacoes.put(caminhoContatoAtual + "idContato", idAlvo);
        operacoes.put(caminhoContatoAtual + "contatoFavorito", false);

        String caminhoContatoAlvo = "/contatos/" + idAlvo + "/" + idUsuario + "/";
        operacoes.put(caminhoContatoAlvo + "idContato", idUsuario);
        operacoes.put(caminhoContatoAlvo + "contatoFavorito", false);

        DatabaseReference contadorMensagemRef = firebaseRef.child("contadorMensagens")
                .child(idUsuario).child(idAlvo);

        DatabaseReference contadorMensagemSelecionadoRef = firebaseRef.child("contadorMensagens")
                .child(idAlvo).child(idUsuario);

        ajustarAmizadeEMsgContato(contadorMensagemRef, new AjustarContatoCallback() {
            @Override
            public void onAjustar(String nivelAmizade, long totalMensagens) {
                operacoes.put(caminhoContatoAtual + "nivelAmizade", nivelAmizade);
                operacoes.put(caminhoContatoAtual + "totalMensagens", totalMensagens);
                ajustarAmizadeEMsgContato(contadorMensagemSelecionadoRef, new AjustarContatoCallback() {
                    @Override
                    public void onAjustar(String nivelAmizade, long totalMensagens) {
                        operacoes.put(caminhoContatoAlvo + "nivelAmizade", nivelAmizade);
                        operacoes.put(caminhoContatoAlvo + "totalMensagens", totalMensagens);
                        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    //Prosseguir
                                    callback.onConcluido();
                                } else {
                                    if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                                        //A amizade já está sendo aceita por outro usuário.
                                        callback.onConcluido();
                                    } else {
                                        callback.onError(String.valueOf(error.getCode()));
                                    }
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

    private static void ajustarAmizadeEMsgContato(DatabaseReference reference, AjustarContatoCallback callback) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Contatos contatoSalvo = snapshot.getValue(Contatos.class);
                    if (contatoSalvo == null || contatoSalvo.getNivelAmizade() == null) {
                        callback.onAjustar(FriendshipLevelUtils.adjustFriendshipLevel(0), 0);
                    } else {
                        callback.onAjustar(contatoSalvo.getNivelAmizade(), contatoSalvo.getTotalMensagens());
                    }
                } else {
                    callback.onAjustar(FriendshipLevelUtils.adjustFriendshipLevel(0), 0);
                }
                reference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
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

    public static void enviarConvite(Context context, String idAlvo, EnviarConviteCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        FriendsUtils.recuperarTimestampnegativo(context, new RecuperarTimestampCallback() {
            @Override
            public void onRecuperado(long timestampNegativo) {
                HashMap<String, Object> operacoes = new HashMap<>();
                operacoes.put("/usuarios/" + idAlvo + "/pedidosAmizade", ServerValue.increment(1));
                String caminhoConviteAtual = "/requestsFriendship/" + idUsuario + "/" + idAlvo + "/";
                String caminhoConviteAlvo = "/requestsFriendship/" + idAlvo + "/" + idUsuario + "/";
                ajustarHashmapConvite(operacoes, caminhoConviteAtual, idUsuario, idAlvo, timestampNegativo);
                ajustarHashmapConvite(operacoes, caminhoConviteAlvo, idUsuario, idAlvo, timestampNegativo);
                firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error == null) {
                            //Prosseguir
                            callback.onConviteEnviado();
                        } else {
                            if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                                //Convite de amizade já existe.
                                callback.onJaExisteConvite();
                            } else {
                                callback.onError(String.valueOf(error.getCode()));
                            }
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

    private static void ajustarHashmapConvite(HashMap<String, Object> operacoes, String caminho, String idUsuario, String idAlvo, long timestampNegativo) {
        operacoes.put(caminho + "idRemetente", idUsuario);
        operacoes.put(caminho + "idDestinatario", idAlvo);
        operacoes.put(caminho + "timestampinteracao", timestampNegativo);
    }
}