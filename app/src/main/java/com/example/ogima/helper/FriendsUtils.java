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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

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
                                                salvarIdEmUsuario(context, idDestinatario, callback);
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

    public static void salvarIdEmUsuario(Context context, @NonNull String idDestinatario, @NonNull SalvarIdAmigoCallback callback) {

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

    public static void desfazerAmizade(@NonNull String idDestinatario, @NonNull DesfazerAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));


        DatabaseReference removerAmizadeAtualRef = firebaseRef.child("friends")
                .child(idUsuario).child(idDestinatario);

        DatabaseReference removerAmizadeDestinatarioRef = firebaseRef.child("friends")
                .child(idDestinatario).child(idUsuario);

        removerAmizadeAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                removerAmizadeDestinatarioRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        removerIdAmigoDoUsuario(idDestinatario, callback);
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

    public static void removerIdAmigoDoUsuario(@NonNull String idDestinatario, @NonNull DesfazerAmizadeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference atualizaAmigosAtualRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("listaIdAmigos");

        DatabaseReference atualizaAmigosDestinatarioRef = firebaseRef.child("usuarios")
                .child(idDestinatario).child("listaIdAmigos");

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                //Dados do usuário atual
                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    //Atualiza lista de amigos sem o idDestinatario.
                    if (listaIdAmigos.contains(idDestinatario)) {
                        listaIdAmigos.remove(idDestinatario);
                        atualizaAmigosAtualRef.setValue(listaIdAmigos).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idDestinatario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
                                    @Override
                                    public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                                        //Dados do destinatário
                                        if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                                            //Atualiza lista de amigos sem o idAtual.
                                            if (listaIdAmigos.contains(idUsuario)) {
                                                listaIdAmigos.remove(idUsuario);
                                                atualizaAmigosDestinatarioRef.setValue(listaIdAmigos).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        callback.onAmizadeDesfeita();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        callback.onError(Objects.requireNonNull(e.getMessage()));
                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onSemDados() {

                                    }

                                    @Override
                                    public void onError(String mensagem) {
                                        callback.onError(Objects.requireNonNull(mensagem));
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
                }
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {
                callback.onError(Objects.requireNonNull(mensagem));
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
                callback.onError(error.getMessage());
            }
        });
    }

    public static void RemoverConvites(String idDestinatario, RemoverConviteCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference conviteAmizadeRef = firebaseRef.child("requestsFriendship")
                .child(idUsuario).child(idDestinatario);

        DatabaseReference conviteAmizadeSelecionadoRef = firebaseRef.child("requestsFriendship")
                .child(idDestinatario).child(idUsuario);

        conviteAmizadeRef.onDisconnect().removeValue();
        conviteAmizadeSelecionadoRef.onDisconnect().removeValue();

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
                                        DiminuirContadorConvite(dadosUserDestinatarioRef, callback);
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

    public static void removerContato(String idDestinatario, RemoverContatoCallback callback) {
        String idUsuario = UsuarioUtils.recuperarIdUserAtual();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        if (idUsuario != null && !idUsuario.isEmpty()
                && idDestinatario != null && !idDestinatario.isEmpty()) {
            DatabaseReference contatoUserAtualRef = firebaseRef.child("contatos")
                    .child(idUsuario).child(idDestinatario);
            DatabaseReference contatoUserAlvoRef = firebaseRef.child("contatos")
                    .child(idDestinatario).child(idUsuario);
            contatoUserAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        contatoUserAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                contatoUserAlvoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        callback.onContatoRemovido();
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
                    } else {
                        callback.onContatoNaoExiste();
                    }
                    contatoUserAtualRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        }
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
                    Log.d("FRIENDUTILS", "JÁ EXISTEM DADOS REMETENTE");
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
                    transactionCallback.onError("Ocorreu um erro ao enviar o convite de amizade");
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
                }  else if (error == null) {
                    transactionCallback.onJaExisteConvite();
                } else {
                    transactionCallback.onError("Ocorreu um erro ao enviar o convite de amizade");
                }
            }
        });
    }
}
