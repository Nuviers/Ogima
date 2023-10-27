package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.ogima.R;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FriendsUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EndLobbyActivity extends AppCompatActivity {

    private String idUsuario = "", idUserD = "";
    private Usuario usuarioD;
    private boolean statusEpilepsia = true;
    private ImageView imgViewEndLobbyUserAtual, imgViewEndLobbyUserD;
    private Button btnAddAmigoRandom, btnSairChatRandom;
    private LinearLayout linearLayoutHobbiesEmComum;
    private ArrayList<String> interessesUserAtual = new ArrayList<>();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private boolean statusAddRandom = false;
    private AtualizarContador atualizarContador = new AtualizarContador();
    private ProgressDialog progressDialog;
    private StorageReference storageRef, audioAtualRef, audioDRef;
    private ValueEventListener valueEventListener;
    private DatabaseReference verificaAddRandomRef;
    private AlertDialog.Builder builder;

    @Override
    public void onBackPressed() {
        exibirAlertDialog();
        super.onBackPressed();
    }

    public EndLobbyActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        usuarioD = new Usuario();
    }

    public interface DadosDestinatarioCallback {
        void onRecuperado(Usuario usuarioDestinatario);

        void onError(String message);
    }

    public interface DadosUserLogadoCallback {
        void onRecuperado(Usuario usuarioLogado);

        void onError(String message);
    }

    public interface LimparConversaCallback {
        void onConversaExcluida();

        void onContadorLimpo();

        void onAddRandomLimpo();

        void onError(String message);
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removerValueEventListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_lobby);
        inicializandoComponentes();

        builder = new AlertDialog.Builder(EndLobbyActivity.this);

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        recuperarDadosUserAtual(new DadosUserLogadoCallback() {
            @Override
            public void onRecuperado(Usuario usuarioLogado) {
                exibirImagens(usuarioLogado.getMinhaFoto(), imgViewEndLobbyUserAtual);
                interessesUserAtual = usuarioLogado.getInteresses();
            }

            @Override
            public void onError(String message) {

            }
        });

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("idUserD")) {
                idUserD = dados.getString("idUserD");
                recuperarDestinatario(new DadosDestinatarioCallback() {
                    @Override
                    public void onRecuperado(Usuario usuarioDestinatario) {
                        usuarioD = usuarioDestinatario;
                        exibirImagens(usuarioD.getMinhaFoto(), imgViewEndLobbyUserD);
                        exibirHobbies(interessesUserAtual);
                        verificaSeSaoAmigos();
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        }

        verificaAddRandomRef = firebaseRef.child("addRandom")
                .child(idUsuario)
                .child(idUserD)
                .child("contadorAddRandom");

        DatabaseReference verificaAddRandomDRef = firebaseRef.child("addRandom")
                .child(idUserD)
                .child(idUsuario)
                .child("contadorAddRandom");

        btnAddAmigoRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!statusAddRandom) {
                    atualizarContador.acrescentarContador(verificaAddRandomRef, new AtualizarContador.AtualizarContadorCallback() {
                        @Override
                        public void onSuccess(int contadorAtualizado) {
                            atualizarContador.acrescentarContador(verificaAddRandomDRef, new AtualizarContador.AtualizarContadorCallback() {
                                @Override
                                public void onSuccess(int contadorAtualizado) {
                                }

                                @Override
                                public void onError(String errorMessage) {

                                }
                            });
                            statusAddRandom = true;
                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });
                } else {
                    atualizarContador.subtrairContador(verificaAddRandomRef, new AtualizarContador.AtualizarContadorCallback() {
                        @Override
                        public void onSuccess(int contadorAtualizado) {
                            atualizarContador.subtrairContador(verificaAddRandomDRef, new AtualizarContador.AtualizarContadorCallback() {
                                @Override
                                public void onSuccess(int contadorAtualizado) {
                                }

                                @Override
                                public void onError(String errorMessage) {

                                }
                            });
                            statusAddRandom = false;
                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });
                }
            }
        });

        valueEventListener = verificaAddRandomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    int nrSolicitacao = snapshot.getValue(Integer.class);
                    btnAddAmigoRandom.setText("Adicionar aos amigos " + nrSolicitacao + "/2");
                    if (nrSolicitacao >= 2) {
                        tratarAmizade();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnSairChatRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendsUtils.VerificaAmizade(idUserD, new FriendsUtils.VerificaAmizadeCallback() {
                    @Override
                    public void onAmigos() {
                        progressDialog.setMessage("Ajustando chat random, aguarde um momento...");
                        if (!isFinishing()) {
                            progressDialog.show();
                        }
                        limparConversa(new LimparConversaCallback() {
                            @Override
                            public void onConversaExcluida() {
                                limparContador(this);
                            }

                            @Override
                            public void onContadorLimpo() {
                                limparAddRandom(this);
                            }

                            @Override
                            public void onAddRandomLimpo() {
                                ocultarProgressDialog();
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                ocultarProgressDialog();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onNaoSaoAmigos() {
                        exibirAlertDialog();
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        });
    }

    private void exibirImagens(String url, ImageView imgViewAlvo) {
        GlideCustomizado.loadUrl(getApplicationContext(),
                url, imgViewAlvo, android.R.color.transparent,
                GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia());
    }

    private void recuperarDadosUserAtual(DadosUserLogadoCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                setStatusEpilepsia(epilepsia);
                callback.onRecuperado(usuarioAtual);
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

    private void recuperarDestinatario(DadosDestinatarioCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUserD, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                callback.onRecuperado(usuarioAtual);

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

    private void exibirHobbies(ArrayList<String> meusInteresses) {
        // Adiciona um chip para cada hobby
        for (String hobby : usuarioD.getInteresses()) {
            if (meusInteresses.contains(hobby)) {
                Chip chip = new Chip(linearLayoutHobbiesEmComum.getContext());
                chip.setText(hobby);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.DKGRAY));
                chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
                chip.setLayoutParams(params);
                chip.setClickable(false);
                linearLayoutHobbiesEmComum.addView(chip);
            }
        }
    }

    private void verificaSeSaoAmigos() {
        FriendsUtils.VerificaAmizade(idUserD, new FriendsUtils.VerificaAmizadeCallback() {
            @Override
            public void onAmigos() {
                ToastCustomizado.toastCustomizado("Amigos", getApplicationContext());
                btnAddAmigoRandom.setVisibility(View.GONE);
            }

            @Override
            public void onNaoSaoAmigos() {
                ToastCustomizado.toastCustomizado("Não são amigos", getApplicationContext());
                btnAddAmigoRandom.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                btnAddAmigoRandom.setVisibility(View.GONE);
            }
        });
    }

    private void tratarAmizade() {
        progressDialog.setMessage("Adicionando aos amigos, aguarde um momento...");
        if (!isFinishing()) {
            progressDialog.show();
        }
        FriendsUtils.VerificaConvite(idUserD, new FriendsUtils.VerificaConviteCallback() {
            @Override
            public void onConvitePendente() {
                //Remover convites antes de adicionar o amigo.
                FriendsUtils.RemoverConvites(idUserD, new FriendsUtils.RemoverConviteCallback() {
                    @Override
                    public void onRemovido() {
                        //Convite de amizade removido e contador de convite diminuido.
                        adicionarAmigo();
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto("Erro ao verifica Convite " + message, getApplicationContext());
                    }
                });
            }

            @Override
            public void onSemConvites() {
                //Adicionar normalmente em friends.
                adicionarAmigo();
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Erro ao verifica Convite " + message, getApplicationContext());
            }
        });
    }

    private void adicionarAmigo() {
        FriendsUtils.salvarAmigo(getApplicationContext(), idUserD, new FriendsUtils.SalvarIdAmigoCallback() {
            @Override
            public void onAmigoSalvo() {
                FriendsUtils.AtualizarContadorAmigos(idUserD, true, new FriendsUtils.AtualizarContadorAmigosCallback() {
                    @Override
                    public void onConcluido() {
                        FriendsUtils.AdicionarContato(idUserD, new FriendsUtils.AdicionarContatoCallback() {
                            @Override
                            public void onContatoAdicionado() {
                                limparConversa(new LimparConversaCallback() {
                                    @Override
                                    public void onConversaExcluida() {
                                        limparContador(this);
                                    }

                                    @Override
                                    public void onContadorLimpo() {
                                        limparAddRandom(this);
                                    }

                                    @Override
                                    public void onAddRandomLimpo() {
                                        ToastCustomizado.toastCustomizadoCurto("TUDO CONCLUÍDO", getApplicationContext());
                                        ocultarProgressDialog();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        ocultarProgressDialog();
                                    }
                                });
                                ToastCustomizado.toastCustomizadoCurto("Agora vocês são amigos", getApplicationContext());
                            }

                            @Override
                            public void onError(String message) {
                                ocultarProgressDialog();
                                ToastCustomizado.toastCustomizadoCurto("Erro ao adicionar amigo " + message, getApplicationContext());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto("Erro ao adicionar amigo " + message, getApplicationContext());
                    }
                });
            }

            @Override
            public void onError(@NonNull String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Erro ao adicionar amigo " + message, getApplicationContext());
            }
        });
    }

    private void ocultarProgressDialog() {
        if (progressDialog != null && !isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void limparConversa(LimparConversaCallback callback) {
        DatabaseReference removerConversaAtualRef = firebaseRef.child("chatRandom")
                .child(idUsuario).child(idUserD);
        DatabaseReference removerConversaDRef = firebaseRef.child("chatRandom")
                .child(idUserD).child(idUsuario);
        removerConversaAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Remover do destinatário
                removerConversaDRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        removerDadosStorage(callback);
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

    private void limparContador(LimparConversaCallback callback) {
        DatabaseReference removerContadorAtualRef = firebaseRef.child("contadorMensagensRandom")
                .child(idUsuario).child(idUserD);
        DatabaseReference removerContadorDRef = firebaseRef.child("contadorMensagensRandom")
                .child(idUserD).child(idUsuario);
        removerContadorAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Remover do destinatário
                removerContadorDRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onContadorLimpo();
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

    private void removerDadosStorage(LimparConversaCallback callback) {
        try {
            audioAtualRef = storageRef.child("chatRandom").child("audios")
                    .child(idUsuario).child(idUserD);
            audioDRef = storageRef.child("chatRandom").child("audios")
                    .child(idUserD).child(idUsuario);

            audioAtualRef.listAll().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    for (StorageReference item : listResult.getItems()) {
                        item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Arquivo excluído com sucesso
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Ocorreu um erro ao excluir o arquivo
                                callback.onError(e.getMessage());
                            }
                        });
                    }
                }
            });

            audioDRef.listAll().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    for (StorageReference item : listResult.getItems()) {
                        item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Arquivo excluído com sucesso
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Ocorreu um erro ao excluir o arquivo
                                callback.onError(e.getMessage());
                            }
                        });
                    }
                }
            });

            // Após excluir todos os arquivos no diretório, exclua o diretório em si
            audioAtualRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Diretório excluído com sucesso
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Ocorreu um erro ao excluir o diretório
                    callback.onError(e.getMessage());
                }
            });

            // Após excluir todos os arquivos no diretório, exclua o diretório em si
            audioDRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Diretório excluído com sucesso
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Ocorreu um erro ao excluir o diretório
                    callback.onError(e.getMessage());
                }
            });

            callback.onConversaExcluida();

        } catch (Exception ex) {
            callback.onConversaExcluida();
            ex.printStackTrace();
        }
    }

    private void limparAddRandom(LimparConversaCallback callback) {
        removerValueEventListener();
        DatabaseReference limparContadorRef = firebaseRef.child("addRandom")
                .child(idUsuario).child(idUserD);
        DatabaseReference limparContadorDRef = firebaseRef.child("addRandom")
                .child(idUserD).child(idUsuario);
        limparContadorRef.removeValue().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                limparContadorDRef.removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onAddRandomLimpo();
                    }
                });
            }
        });
    }

    private void exibirAlertDialog(){
        builder.setTitle("Sair da conversa aleatória")
                .setMessage("Você não poderá voltar a essa conversa aleatória posteriormente. Se o usuário clicar para adicionar você aos amigos por esse chat o pedido será negado automaticamente.")
                .setPositiveButton("Sair e não adicionar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog.setMessage("Ajustando chat random, aguarde um momento...");
                        if (!isFinishing()) {
                            progressDialog.show();
                        }
                        limparConversa(new LimparConversaCallback() {
                            @Override
                            public void onConversaExcluida() {
                                limparContador(this);
                            }

                            @Override
                            public void onContadorLimpo() {
                                limparAddRandom(this);
                            }

                            @Override
                            public void onAddRandomLimpo() {
                                ocultarProgressDialog();
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                ocultarProgressDialog();
                                finish();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Ação a ser executada quando o botão "Negative" for clicado
                        // Por exemplo, você pode cancelar alguma operação aqui
                        dialog.dismiss();
                    }
                });

        // Crie o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        // Exiba o AlertDialog
        alertDialog.show();
    }

    private void removerValueEventListener(){
        if (valueEventListener != null) {
            verificaAddRandomRef.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    private void inicializandoComponentes() {
        imgViewEndLobbyUserAtual = findViewById(R.id.imgViewEndLobbyUserAtual);
        imgViewEndLobbyUserD = findViewById(R.id.imgViewEndLobbyUserD);

        btnAddAmigoRandom = findViewById(R.id.btnAddAmigoRandom);
        btnSairChatRandom = findViewById(R.id.btnSairChatRandom);

        linearLayoutHobbiesEmComum = findViewById(R.id.linearLayoutHobbiesEmComum);
    }
}