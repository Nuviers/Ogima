package com.example.ogima.helper;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public interface SalvarIdAmigoCallback {
        void onAmigoSalvo();

        void onError(@NonNull String message);
    }

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
        void onRemovido();

        void onError(String message);
    }

    public interface AtualizarContadorAmigosCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface AdicionarContatoCallback {
        void onContatoAdicionado();

        void onError(String message);
    }

    public interface RecuperarTimestampCallback {
        void onRecuperado(long timestampNegativo);

        void onError(String message);
    }

    public interface RemoverContatoCallback {
        void onContatoRemovido();

        void onContatoNaoExiste();

        void onError(String message);
    }

    public interface EnviarConviteCallback {
        void onConviteEnviado();

        void onJaExisteConvite();

        void onError(String message);
    }

    public interface TransactionCallback {
        void onConcluido();

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

    public interface TransactionExclusaoCallback {
        void onConcluido();

        void onExcluidoAnteriormente();

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
        void onAjustar(String nivelAmizade, int totalMensagens);

        void onError(String message);
    }

    public static void salvarAmigo(Context context, @NonNull String idDestinatario, @NonNull SalvarIdAmigoCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference salvarIdUserAtualRef = firebaseRef.child("friends")
                .child(idUsuario).child(idDestinatario).child("idUsuario");

        DatabaseReference salvarIdUserDestinatarioRef = firebaseRef.child("friends")
                .child(idDestinatario).child(idUsuario).child("idUsuario");

        salvarIdUserAtualRef.setValue(idDestinatario).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                salvarIdUserDestinatarioRef.setValue(idUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Salvar timestamp
                        recuperarTimestampnegativo(context, new RecuperarTimestampCallback() {

                            DatabaseReference salvarTimestampAtualRef = firebaseRef
                                    .child("friends").child(idUsuario).child(idDestinatario)
                                    .child("timestampinteracao");

                            DatabaseReference salvarTimestampAlvoRef = firebaseRef
                                    .child("friends").child(idDestinatario).child(idUsuario)
                                    .child("timestampinteracao");

                            @Override
                            public void onRecuperado(long timestampNegativo) {
                                salvarTimestampAtualRef.setValue(timestampNegativo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        salvarTimestampAlvoRef.setValue(timestampNegativo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //adicionarIdAmigo(context, idDestinatario, callback);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                callback.onError(e.getMessage());
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        callback.onError(e.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(Objects.requireNonNull(e.getMessage()));
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(Objects.requireNonNull(e.getMessage()));
            }
        });
    }

    public static void adicionarIdAmigo(@NonNull String idDados, @NonNull String idParaAdicionar, @NonNull AtualizarListaAmigosCallback callback) {
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

    public static void salvarIdEmUsuarioOLD(Context context, @NonNull String idDestinatario, @NonNull SalvarIdAmigoCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference recuperaUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        DatabaseReference salvaIdUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("listaIdAmigos");

        DatabaseReference recuperaUserDestinatarioRef = firebaseRef.child("usuarios")
                .child(idDestinatario);

        DatabaseReference salvaIdUserDestinatarioRef = firebaseRef.child("usuarios")
                .child(idDestinatario).child("listaIdAmigos");

        recuperaUserAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    ArrayList<String> listaIdsAmigos = new ArrayList<>();

                    if (usuario.getListaIdAmigos() != null
                            && usuario.getListaIdAmigos().size() > 0) {
                        listaIdsAmigos = usuario.getListaIdAmigos();
                        if (!listaIdsAmigos.contains(idDestinatario)) {
                            //Somente adiciona se não conter tal id na lista.
                            listaIdsAmigos.add(idDestinatario);
                        }
                    } else {
                        listaIdsAmigos.add(idDestinatario);
                    }

                    salvaIdUserAtualRef.setValue(listaIdsAmigos).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            recuperaUserDestinatarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {

                                        Usuario usuarioDestinatario = snapshot.getValue(Usuario.class);

                                        ArrayList<String> listaIdsAmigos = new ArrayList<>();

                                        if (usuarioDestinatario.getListaIdAmigos() != null
                                                && usuarioDestinatario.getListaIdAmigos().size() > 0) {
                                            listaIdsAmigos = usuarioDestinatario.getListaIdAmigos();
                                            if (!listaIdsAmigos.contains(idUsuario)) {
                                                //Somente adiciona se não conter tal id na lista.
                                                listaIdsAmigos.add(idUsuario);
                                            }
                                        } else {
                                            listaIdsAmigos.add(idUsuario);
                                        }

                                        salvaIdUserDestinatarioRef.setValue(listaIdsAmigos).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                callback.onAmigoSalvo();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                callback.onError(Objects.requireNonNull(e.getMessage()));
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError(Objects.requireNonNull(e.getMessage()));
                        }
                    });
                }
                recuperaUserAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void desfazerAmizade(Context context, String idDestinatario, DesfazerAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        HashMap<String, Object> operacoes = new HashMap<>();
        HashMap<String, Object> operacaoLock = new HashMap<>();

        operacaoLock.put("/lockUnfriend/" + idUsuario + "/" + idDestinatario + "/" + "idRemetente", idUsuario);
        operacaoLock.put("/lockUnfriend/" + idUsuario + "/" + idDestinatario + "/" + "idDestinatario", idDestinatario);

        operacaoLock.put("/lockUnfriend/" + idDestinatario + "/" + idUsuario + "/" + "idRemetente", idUsuario);
        operacaoLock.put("/lockUnfriend/" + idDestinatario + "/" + idUsuario + "/" + "idDestinatario", idDestinatario);

        firebaseRef.updateChildren(operacaoLock, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    //Prosseguir
                    continuarDesfazerAmizade(idUsuario, idDestinatario, operacoes, callback);
                } else {
                    if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                        //A amizade já está sendo desfeita por outro usuário.
                        callback.onAmizadeDesfeita();
                    } else {
                        Log.d("ERRODESFAZER", error.getMessage() + " : " + error.getCode());
                        callback.onError(String.valueOf(error.getCode()));
                    }
                }
            }
        });
    }

    private static void continuarDesfazerAmizade(String idUsuario, String idDestinatario, HashMap<String, Object> operacoes, DesfazerAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference lockAtualRef = firebaseRef.child("lockUnfriend")
                .child(idUsuario).child(idDestinatario);
        DatabaseReference lockDestinatarioRef = firebaseRef.child("lockUnfriend")
                .child(idDestinatario).child(idUsuario);
        OnDisconnect onDisconnectLockAtual = lockAtualRef.onDisconnect();
        OnDisconnect onDisconnectLockAlvo = lockDestinatarioRef.onDisconnect();
        onDisconnectLockAtual.removeValue();
        onDisconnectLockAlvo.removeValue();
        removerIdAmigoDoUsuario(idUsuario, idDestinatario, new PrepararListaAmigoCallback() {
            @Override
            public void onProsseguir(ArrayList<String> listaAtualizada) {
                operacoes.put("/usuarios/" + idUsuario + "/listaIdAmigos/", listaAtualizada);
                operacoesDesfazerAmizade(operacoes, idDestinatario, callback);
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

    private static void operacoesDesfazerAmizade(HashMap<String, Object> operacoes, String idDestinatario, DesfazerAmizadeCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        removerIdAmigoDoUsuario(idDestinatario, idUsuario, new PrepararListaAmigoCallback() {
            @Override
            public void onProsseguir(ArrayList<String> listaAtualizada) {
                operacoes.put("/usuarios/" + idDestinatario + "/listaIdAmigos/", listaAtualizada);
                operacoes.put("/usuarios/" + idUsuario + "/amigosUsuario", ServerValue.increment(-1));
                operacoes.put("/usuarios/" + idDestinatario + "/amigosUsuario", ServerValue.increment(-1));
                operacoes.put("/contatos/" + idUsuario + "/" + idDestinatario, null);
                operacoes.put("/contatos/" + idDestinatario + "/" + idUsuario, null);
                operacoes.put("/friends/" + idUsuario + "/" + idDestinatario, null);
                operacoes.put("/friends/" + idDestinatario + "/" + idUsuario, null);
                operacoes.put("/lockUnfriend/" + idDestinatario + "/" + idUsuario, null);
                operacoes.put("/lockUnfriend/" + idUsuario + "/" + idDestinatario, null);
                salvarHashMapUnfriend(operacoes, callback);
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

    private static void salvarHashMapUnfriend(HashMap<String, Object> operacoes, DesfazerAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    //Prosseguir
                    callback.onAmizadeDesfeita();
                } else {
                    Log.d("ERRODESFAZER", error.getMessage() + " : " + error.getCode());
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    public static void removerIdAmigoDoUsuario(@NonNull String idDados, String idARemover, @NonNull PrepararListaAmigoCallback callback) {
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
                callback.onError("Erro ao recuperar dados");
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    public static void verificaListaAmigoAtual(String idUsuario, String idAlvo, PrepararListaAmigoCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    if (listaIdAmigos.contains(idAlvo)) {
                        listaIdAmigos.remove(idAlvo);
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
                callback.onError("Erro ao recuperar dados");
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    public static void verificaListaAmigoAlvo(String idUsuario, String idAlvo, PrepararListaAmigoCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idAlvo, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    if (listaIdAmigos.contains(idUsuario)) {
                        listaIdAmigos.remove(idUsuario);
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
                callback.onError("Erro ao recuperar dados");
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    public static void atualizarListaAmigos(DatabaseReference reference, ArrayList<String> listaAmigos, TransactionExclusaoCallback transactionCallback) {

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("TESTEUTILS", "Existe lista");
                    reference.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            // Se existir dados salvar a lista atualizada
                            currentData.setValue(listaAmigos);
                            Log.d("TESTEUTILS", "SETADO LISTA");
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                            if (error == null && committed) {
                                transactionCallback.onConcluido();
                            } else if (error == null) {
                                transactionCallback.onExcluidoAnteriormente();
                            } else {
                                transactionCallback.onError("Ocorreu um erro ao desfazer amizade");
                            }
                        }
                    });
                } else {
                    Log.d("TESTEUTILS", "Não existe lista");
                    transactionCallback.onExcluidoAnteriormente();
                }
                reference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TESTEUTILS", "Erro: " + error.getMessage());
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

    public static void adicionarAmigo(Context context, String idAlvo, AdicionarAmigoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError("falha ao recuperar dados");
            return;
        }
        DatabaseReference conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                .child(idUsuario).child(idAlvo);
        DatabaseReference conviteAmizadeSelecionadoRef = firebaseRef.child("requestsFriendship")
                .child(idAlvo).child(idUsuario);
        OnDisconnectUtils.onDisconnectRemoveValue(conviteAmizadeRef);
        OnDisconnectUtils.onDisconnectRemoveValue(conviteAmizadeSelecionadoRef);
        removerConvites(idAlvo, new RemoverConviteCallback() {
            @Override
            public void onRemovido() {
                recuperarTimestampnegativo(context, new RecuperarTimestampCallback() {
                    HashMap<String, Object> operacoes = new HashMap<>();
                    String caminhoFriendsAtual = "/friends/" + idUsuario + "/" + idAlvo + "/";
                    String caminhoFriendsAlvo = "/friends/" + idAlvo + "/" + idUsuario + "/";
                    String caminhoUsuarioAtual = "/usuarios/" + idUsuario + "/";
                    String caminhoUsuarioAlvo = "/usuarios/" + idAlvo + "/";

                    @Override
                    public void onRecuperado(long timestampNegativo) {
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
                                        adicionarContato(idAlvo, operacoes, callback);
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

    public static void removerConvites(String idAlvo, RemoverConviteCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError("falha ao recuperar dados");
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
                        HashMap<String, Object> operacoes = new HashMap<>();
                        String caminhoConviteAtual = "/requestsFriendship/" + idUsuario + "/" + idAlvo;
                        String caminhoConviteAlvo = "/requestsFriendship/" + idAlvo + "/" + idUsuario;
                        operacoes.put(caminhoConviteAtual, null);
                        operacoes.put(caminhoConviteAlvo, null);
                        operacoes.put("/usuarios/" + idDestinatarioConvite + "/" + "pedidosAmizade", ServerValue.increment(-1));
                        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    //Prosseguir
                                    callback.onRemovido();
                                } else {
                                    if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                                        //A amizade já está sendo desfeita por outro usuário.
                                        callback.onRemovido();
                                    } else {
                                        Log.d("ERRODESFAZER", error.getMessage() + " : " + error.getCode());
                                        callback.onError(String.valueOf(error.getCode()));
                                    }
                                }
                            }
                        });
                    } else {
                        callback.onError("falha ao recuperar dados");
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

    public static void RemoverConvitesOLD(String idAlvo, RemoverConviteCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                .child(idUsuario).child(idAlvo);

        DatabaseReference conviteAmizadeSelecionadoRef = firebaseRef.child("requestsFriendship")
                .child(idAlvo).child(idUsuario);

        OnDisconnect onDisconnect = conviteAmizadeRef.onDisconnect();
        onDisconnect.removeValue();
        OnDisconnect onDisconnectSelecionado = conviteAmizadeSelecionadoRef.onDisconnect();
        onDisconnectSelecionado.removeValue();

        //Verifica quem é o destinatário do convite.
        conviteAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    String idDestinatarioDoConvite = usuario.getIdDestinatario();

                    if (idDestinatarioDoConvite != null
                            && !idDestinatarioDoConvite.isEmpty()) {

                        //Convites
                        DatabaseReference dadosUserDestinatarioRef = firebaseRef.child("usuarios")
                                .child(idDestinatarioDoConvite).child("pedidosAmizade");

                        conviteAmizadeRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                conviteAmizadeSelecionadoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        onDisconnect.cancel();
                                        onDisconnectSelecionado.cancel();
                                        DiminuirContadorConvite(dadosUserDestinatarioRef, callback);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        onDisconnect.cancel();
                                        onDisconnectSelecionado.cancel();
                                        callback.onError(e.getMessage());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callback.onError(e.getMessage());
                            }
                        });
                    }
                }
                conviteAmizadeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void DiminuirContadorConvite(DatabaseReference contadorConviteRef, RemoverConviteCallback callback) {
        AtualizarContador atualizarContador = new AtualizarContador();
        atualizarContador.subtrairContador(contadorConviteRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                callback.onRemovido();
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public static void AtualizarContadorAmigos(String idDestinatario, boolean acrescentar, AtualizarContadorAmigosCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        AtualizarContador atualizarContador = new AtualizarContador();

        DatabaseReference dadosUserAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("amigosUsuario");

        DatabaseReference dadosUserDestinatarioRef = firebaseRef.child("usuarios")
                .child(idDestinatario).child("amigosUsuario");

        if (acrescentar) {
            atualizarContador.acrescentarContador(dadosUserAtualRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    atualizarContador.acrescentarContador(dadosUserDestinatarioRef, new AtualizarContador.AtualizarContadorCallback() {
                        @Override
                        public void onSuccess(int contadorAtualizado) {
                            callback.onConcluido();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError(errorMessage);
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            });
        } else {
            atualizarContador.subtrairContador(dadosUserAtualRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    atualizarContador.subtrairContador(dadosUserDestinatarioRef, new AtualizarContador.AtualizarContadorCallback() {
                        @Override
                        public void onSuccess(int contadorAtualizado) {
                            callback.onConcluido();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError(errorMessage);
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            });
        }
    }

    public static void adicionarContato(String idAlvo, HashMap<String, Object> operacoes, AdicionarAmigoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null) {
            callback.onError("falha ao recuperar dados");
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
            public void onAjustar(String nivelAmizade, int totalMensagens) {
                operacoes.put(caminhoContatoAtual + "nivelAmizade", nivelAmizade);
                operacoes.put(caminhoContatoAtual + "totalMensagens", totalMensagens);
                ajustarAmizadeEMsgContato(contadorMensagemSelecionadoRef, new AjustarContatoCallback() {
                    @Override
                    public void onAjustar(String nivelAmizade, int totalMensagens) {
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
                                        Log.d("ERRODESFAZER", error.getMessage() + " : " + error.getCode());
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

    public static void AdicionarContato(String idDestinatario, AdicionarContatoCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference novoContatoRef, novoContatoSelecionadoRef,
                contadorMensagemRef, contadorMensagemSelecionadoRef;

        HashMap<String, Object> dadosContatoSelecionado = new HashMap<>();
        HashMap<String, Object> dadosContatoAtual = new HashMap<>();

        novoContatoRef = firebaseRef.child("contatos")
                .child(idUsuario).child(idDestinatario);

        novoContatoSelecionadoRef = firebaseRef.child("contatos")
                .child(idDestinatario).child(idUsuario);

        //Contador de mensagens
        contadorMensagemRef = firebaseRef.child("contadorMensagens")
                .child(idUsuario).child(idDestinatario);

        contadorMensagemSelecionadoRef = firebaseRef.child("contadorMensagens")
                .child(idDestinatario).child(idUsuario);

        dadosContatoAtual.put("idContato", idUsuario);
        dadosContatoAtual.put("contatoFavorito", "não");

        dadosContatoSelecionado.put("idContato", idDestinatario);
        dadosContatoSelecionado.put("contatoFavorito", "não");

        //Verifica se existiu uma conversa entre os usuários antes de virarem amigos
        contadorMensagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Já existe o contador de mensagens
                    Contatos contatoSalvo = snapshot.getValue(Contatos.class);
                    dadosContatoAtual.put("nivelAmizade", contatoSalvo.getNivelAmizade());
                    dadosContatoAtual.put("totalMensagens", contatoSalvo.getTotalMensagens());
                } else {
                    //Não existe conversa entre eles
                    dadosContatoAtual.put("totalMensagens", 0);
                    dadosContatoAtual.put("nivelAmizade", "Ternura");
                }
                //Adicionando aos contatos com os dados anteriores caso existia se não, com dados novos.
                novoContatoSelecionadoRef.setValue(dadosContatoAtual);
                contadorMensagemRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Verifica se existiu uma conversa entre os usuários antes de virarem amigos
        contadorMensagemSelecionadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Já existe o contador de mensagens
                    Contatos contatoSalvo = snapshot.getValue(Contatos.class);
                    dadosContatoSelecionado.put("nivelAmizade", contatoSalvo.getNivelAmizade());
                    dadosContatoSelecionado.put("totalMensagens", contatoSalvo.getTotalMensagens());
                } else {
                    //Não existe conversa entre eles
                    dadosContatoSelecionado.put("totalMensagens", 0);
                    dadosContatoSelecionado.put("nivelAmizade", "Ternura");
                }
                //Adicionando aos contatos com os dados anteriores caso existia se não, com dados novos.
                novoContatoRef.setValue(dadosContatoSelecionado);
                callback.onContatoAdicionado();
                contadorMensagemSelecionadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    public static void removerContato(DatabaseReference reference, TransactionExclusaoCallback transactionCallback) {
        //**reference.keepSynced(true); //testar
        Log.d("EXCLUIRUTILS", "DELL CONTATO");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Log.d("TESTEUTILS", "EXISTE CONTATO");
                    reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            transactionCallback.onConcluido();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            transactionCallback.onError(e.getMessage());
                        }
                    });
                } else {
                    transactionCallback.onExcluidoAnteriormente();
                }
                reference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                transactionCallback.onError(error.getMessage());
            }
        });
    }

    public static void enviarConvite(Context context, String idDestinatario, EnviarConviteCallback callback) {
        FriendsUtils.recuperarTimestampnegativo(context, new FriendsUtils.RecuperarTimestampCallback() {
            String idUsuario = UsuarioUtils.recuperarIdUserAtual();
            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
            DatabaseReference conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                    .child(idUsuario).child(idDestinatario);
            DatabaseReference conviteAmizadeSelecionadoRef = firebaseRef.child("requestsFriendship")
                    .child(idDestinatario).child(idUsuario);
            DatabaseReference contadorPedidosAmizadeRef = firebaseRef.child("usuarios")
                    .child(idDestinatario).child("pedidosAmizade");

            @Override
            public void onRecuperado(long timestampNegativo) {

                ToastCustomizado.toastCustomizadoCurto("id " + idUsuario, context);

                HashMap<String, Object> dadosConvite = new HashMap<>();
                dadosConvite.put("idRemetente", idUsuario);
                dadosConvite.put("idDestinatario", idDestinatario);
                dadosConvite.put("timestampinteracao", timestampNegativo);

                conviteEmRemetente(conviteAmizadeRef, dadosConvite, new TransactionCallback() {
                    @Override
                    public void onConcluido() {
                        conviteEmDestinatario(conviteAmizadeSelecionadoRef, dadosConvite, contadorPedidosAmizadeRef, new TransactionCallback() {
                            @Override
                            public void onConcluido() {
                                //Concluído toda operação.
                                callback.onConviteEnviado();
                            }

                            @Override
                            public void onJaExisteConvite() {
                                callback.onJaExisteConvite();
                            }

                            @Override
                            public void onError(String message) {
                                callback.onError(message);
                            }
                        });
                    }

                    @Override
                    public void onJaExisteConvite() {
                        callback.onJaExisteConvite();
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

    public static void conviteEmRemetente(DatabaseReference reference, HashMap<String, Object> dadosConvite, TransactionCallback transactionCallback) {
        reference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    // Se os dados atuais não existirem, crie um novo nó
                    currentData.setValue(dadosConvite);
                    return Transaction.success(currentData);
                } else {
                    // Dados já existem, a transação será cancelada
                    Log.d("TESTEUTILS", "JÁ EXISTE CONVITE");
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error == null && committed) {
                    transactionCallback.onConcluido();
                } else if (error == null) {
                    transactionCallback.onJaExisteConvite();
                } else {
                    transactionCallback.onError(String.format("%s %s %s", "Ocorreu um erro ao enviar o convite de amizade, tente novamente", ":", error.getCode()));
                }
            }
        });
    }

    public static void conviteEmDestinatario(DatabaseReference reference, HashMap<String, Object> dadosConvite, DatabaseReference contadorPedidoRef, TransactionCallback transactionCallback) {
        reference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    // Se os dados atuais não existirem, crie um novo nó
                    currentData.setValue(dadosConvite);
                    return Transaction.success(currentData);
                } else {
                    // Dados já existem, a transação será cancelada
                    Log.d("FRIENDUTILS", "JÁ EXISTEM DADOS DESTINATARIO");
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error == null && committed) {
                    AtualizarContador atualizarContador = new AtualizarContador();
                    atualizarContador.acrescentarContador(contadorPedidoRef, new AtualizarContador.AtualizarContadorCallback() {
                        @Override
                        public void onSuccess(int contadorAtualizado) {
                            transactionCallback.onConcluido();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            transactionCallback.onError(errorMessage);
                        }
                    });
                } else if (error == null) {
                    transactionCallback.onJaExisteConvite();
                } else {
                    transactionCallback.onError(String.format("%s %s %s", "Ocorreu um erro ao enviar o convite de amizade, tente novamente.", "Error code:", error.getCode()));
                }
            }
        });
    }
}