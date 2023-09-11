package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LobbyChatRandomActivity extends AppCompatActivity {

    private TextView txtViewTimerLobby;
    private long tempoDecorrido = 0;
    private Handler handler = new Handler();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private AtualizarContador atualizarContador = new AtualizarContador();
    private String generoFiltrado = "mulher";
    private Set<String> idsUsuariosEmparelhados = new HashSet<>();
    private String generoUserLogado = "";
    private int idadeUserLogado = -1;
    private int idadeMaxDesejada = 25;

    public interface DadosUserAtualCallback {
        void onRecuperado(String genero, int idadeAtual);

        void onError(String message);
    }

    public interface RemocaoDaFilaCallback {
        void onRemovido();

        void onError(String message);
    }

    public interface VerificaoInicialCallback {
        void onConcluido();
    }

    public interface VerificaFilaCallback {
        void onExiste();

        void onNaoExiste();

        void onError(String message);
    }

    public interface UserAtualEstaNaFilaCallback {
        void onNaFila(int posicao);

        void onNaoEsta();

        void onError(String message);
    }

    public LobbyChatRandomActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararTimer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_chat);
        inicializandoComponentes();
        recuperarGeneroAtual(new DadosUserAtualCallback() {
            @Override
            public void onRecuperado(String generoAtual, int idadeAtual) {
                generoUserLogado = generoAtual;
                idadeUserLogado = idadeAtual;
                iniciarTimer();
                entrarNaFila(new VerificaoInicialCallback() {
                    @Override
                    public void onConcluido() {
                        Query verificaNrUsersNaFilaRef = firebaseRef.child("matchmaking")
                                .orderByChild("posicao");
                        verificaNrUsersNaFilaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    long nrUsersNaFila = snapshot.getChildrenCount();
                                    ToastCustomizado.toastCustomizadoCurto("Total " + nrUsersNaFila, getApplicationContext());
                                    if (nrUsersNaFila >= 2) {
                                        List<String> usuariosNaFila = new ArrayList<>();
                                        // Coleta os ids dos usuarios na fila
                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                            String uid = snapshot1.getKey();

                                            if (generoFiltrado != null && !generoFiltrado.isEmpty()) {
                                                String genero = snapshot1.getValue(Usuario.class).getGeneroUsuario().toLowerCase(Locale.ROOT);
                                                String generoDesejado = snapshot1.getValue(Usuario.class).getGeneroDesejado().toLowerCase(Locale.ROOT);
                                                int idade = snapshot1.getValue(Usuario.class).getIdade();
                                                int idadeMax = snapshot1.getValue(Usuario.class).getIdadeMaxDesejada();
                                                if (genero != null && !genero.isEmpty()
                                                        && genero.equals(generoFiltrado)) {
                                                    ToastCustomizado.toastCustomizadoCurto("IGUAL", getApplicationContext());
                                                    if (generoUserLogado.equals(generoDesejado)) {
                                                        if (idade <= idadeMaxDesejada
                                                                && idadeUserLogado <= idadeMax
                                                        && !uid.equals(idUsuario)) {
                                                            //Idade do usuário comparado está dentros dos limites de idade
                                                            usuariosNaFila.add(0,uid);
                                                            usuariosNaFila.add(1,idUsuario);
                                                        }
                                                    }
                                                }
                                            } else {
                                                //Não há filtros
                                                usuariosNaFila.add(uid);
                                            }
                                        }

                                        if (usuariosNaFila.size() >= 2) {
                                            // Emparelhe os dois primeiros jogadores da fila
                                            String user1Id = usuariosNaFila.get(0);
                                            String user2Id = usuariosNaFila.get(1);

                                            // Crie uma sala para os usuários e direciona eles da fila para a sala.
                                            transferirParaSala(user1Id);
                                        }
                                    }
                                }
                                verificaNrUsersNaFilaRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private Runnable atualizarCronometro = new Runnable() {
        public void run() {
            tempoDecorrido += 1000; // Adicionar 1 segundo ao tempo decorrido
            int segundos = (int) (tempoDecorrido / 1000);
            int minutos = segundos / 60;
            segundos = segundos % 60;
            txtViewTimerLobby.setText(String.format("%02d:%02d", minutos, segundos));
            handler.postDelayed(this, 1000); // Atualizar a cada 1 segundo
        }
    };

    private void iniciarTimer() {
        handler.post(atualizarCronometro);
    }

    private void resetarTimer() {
        handler.removeCallbacks(atualizarCronometro);
        tempoDecorrido = 0;
        txtViewTimerLobby.setText("00:00");
    }

    private void pararTimer() {
        if (handler != null) {
            handler.removeCallbacks(atualizarCronometro);
            handler = null;
        }
    }

    private void entrarNaFila(VerificaoInicialCallback callback) {

        verificaSeExisteAlguemNaFila(new VerificaFilaCallback() {
            @Override
            public void onExiste() {
                //Posição tem que ser definida com base na última posição + 1 em tempo real;

                verificaSeUserEstaNaFila(new UserAtualEstaNaFilaCallback() {
                    @Override
                    public void onNaFila(int posicao) {
                        callback.onConcluido();
                    }

                    @Override
                    public void onNaoEsta() {
                        Query ultimaPosicaoRef = firebaseRef.child("matchmaking")
                                .orderByChild("posicao").limitToLast(1);
                        ultimaPosicaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        int posicao = snapshot1.getValue(Usuario.class).getPosicao();
                                        DatabaseReference atualizarPosicaoRef = firebaseRef.child("matchmaking")
                                                .child(idUsuario).child("posicao");
                                        atualizarContador.acrescentarContadorPorValor(atualizarPosicaoRef, posicao, new AtualizarContador.AtualizarContadorCallback() {
                                            @Override
                                            public void onSuccess(int contadorAtualizado) {
                                                DatabaseReference salvarIdRef = firebaseRef.child("matchmaking")
                                                        .child(idUsuario).child("idUsuario");
                                                salvarIdRef.setValue(idUsuario).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        DatabaseReference salvarGeneroRef = firebaseRef.child("matchmaking")
                                                                .child(idUsuario).child("generoUsuario");
                                                        salvarGeneroRef.setValue(generoUserLogado).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                            }
                                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                if (generoFiltrado != null && !generoFiltrado.isEmpty()) {
                                                                    DatabaseReference salvarGeneroDesejadoRef = firebaseRef.child("matchmaking")
                                                                            .child(idUsuario).child("generoDesejado");
                                                                    salvarGeneroDesejadoRef.setValue(generoFiltrado).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                        }
                                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            DatabaseReference salvarIdadeRef = firebaseRef.child("matchmaking")
                                                                                    .child(idUsuario).child("idade");
                                                                            salvarIdadeRef.setValue(idadeUserLogado).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {

                                                                                }
                                                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    DatabaseReference salvarIdadeMaxRef = firebaseRef.child("matchmaking")
                                                                                            .child(idUsuario).child("idadeMaxDesejada");
                                                                                    salvarIdadeMaxRef.setValue(idadeMaxDesejada).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {

                                                                                        }
                                                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void unused) {
                                                                                            callback.onConcluido();
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                } else {
                                                                    callback.onConcluido();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(String errorMessage) {

                                            }
                                        });
                                        break;
                                    }
                                } else {
                                    DatabaseReference atualizarPosicaoRef = firebaseRef.child("matchmaking")
                                            .child(idUsuario).child("posicao");
                                    atualizarContador.acrescentarContadorPorValor(atualizarPosicaoRef, 0, new AtualizarContador.AtualizarContadorCallback() {
                                        @Override
                                        public void onSuccess(int contadorAtualizado) {
                                            DatabaseReference salvarIdRef = firebaseRef.child("matchmaking")
                                                    .child(idUsuario).child("idUsuario");
                                            salvarIdRef.setValue(idUsuario).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    DatabaseReference salvarGeneroRef = firebaseRef.child("matchmaking")
                                                            .child(idUsuario).child("generoUsuario");
                                                    salvarGeneroRef.setValue(generoUserLogado).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            if (generoFiltrado != null && !generoFiltrado.isEmpty()) {
                                                                DatabaseReference salvarGeneroDesejadoRef = firebaseRef.child("matchmaking")
                                                                        .child(idUsuario).child("generoDesejado");
                                                                salvarGeneroDesejadoRef.setValue(generoFiltrado).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                    }
                                                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        DatabaseReference salvarIdadeRef = firebaseRef.child("matchmaking")
                                                                                .child(idUsuario).child("idade");
                                                                        salvarIdadeRef.setValue(idadeUserLogado).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {

                                                                            }
                                                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                DatabaseReference salvarIdadeMaxRef = firebaseRef.child("matchmaking")
                                                                                        .child(idUsuario).child("idadeMaxDesejada");
                                                                                salvarIdadeMaxRef.setValue(idadeMaxDesejada).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                    }
                                                                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        callback.onConcluido();
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            } else {
                                                                callback.onConcluido();
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String errorMessage) {

                                        }
                                    });
                                }
                                ultimaPosicaoRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onNaoExiste() {
                //Posição deve ser a primeira da fila
                DatabaseReference atualizarMinhaPosicaoRef = firebaseRef.child("matchmaking")
                        .child(idUsuario);
                HashMap<String, Object> dados = new HashMap<>();
                dados.put("idUsuario", idUsuario);
                dados.put("posicao", 1);
                dados.put("generoUsuario", generoUserLogado);
                dados.put("idade", idadeUserLogado);
                dados.put("idadeMaxDesejada", idadeMaxDesejada);
                if (generoFiltrado != null && !generoFiltrado.isEmpty()) {
                    dados.put("generoDesejado", generoFiltrado);
                }
                atualizarMinhaPosicaoRef.setValue(dados).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onConcluido();
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void verificaSeExisteAlguemNaFila(VerificaFilaCallback callback) {
        Query analisarFilaRef = firebaseRef.child("matchmaking")
                .orderByChild("idUsuario");
        analisarFilaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onExiste();
                } else {
                    callback.onNaoExiste();
                }
                analisarFilaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void verificaSeUserEstaNaFila(UserAtualEstaNaFilaCallback callback) {
        DatabaseReference verificaFilaRef = firebaseRef.child("matchmaking")
                .child(idUsuario);
        verificaFilaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onNaFila(snapshot.getValue(Usuario.class).getPosicao());
                } else {
                    callback.onNaoEsta();
                }
                verificaFilaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void transferirParaSala(String idUserD) {
        ToastCustomizado.toastCustomizadoCurto("Id 1 " + idUserD, getApplicationContext());

        irParaSala(idUserD);
        //Remover usuários da fila
        /*
        removerUsersDaFila(user1Id, user2Id, new RemocaoDaFilaCallback() {
            @Override
            public void onRemovido() {
            irParaSala(user1Id, user2Id);
                ToastCustomizado.toastCustomizadoCurto("Tudo removido", getApplicationContext());
            }

            @Override
            public void onError(String message) {

            }
        });
         */
    }

    private void removerUsersDaFila(String user1Id, String user2Id, RemocaoDaFilaCallback callback) {
        DatabaseReference removerFilaUser1Ref = firebaseRef.child("matchmaking")
                .child(user1Id);
        DatabaseReference removerFilaUser2Ref = firebaseRef.child("matchmaking")
                .child(user2Id);
        removerFilaUser1Ref.removeValue().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                removerFilaUser2Ref.removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onRemovido();
                    }
                });
            }
        });
    }

    private void irParaSala(String idUserD) {
        pararTimer();
        Intent intent = new Intent(LobbyChatRandomActivity.this, ChatRandomActivity.class);
        intent.putExtra("idUserD", idUserD);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void recuperarGeneroAtual(DadosUserAtualCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                callback.onRecuperado(usuarioAtual.getGeneroUsuario().toLowerCase(Locale.ROOT), usuarioAtual.getIdade());
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private void inicializandoComponentes() {
        txtViewTimerLobby = findViewById(R.id.txtViewTimerLobby);
    }
}