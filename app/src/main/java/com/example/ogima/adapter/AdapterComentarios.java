package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.DenunciaPostagemActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterComentarios extends RecyclerView.Adapter<AdapterComentarios.MyViewHolder> {

    private List<Postagem> listaComentarios;
    private List<Usuario> listaRespotasComentarios;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Usuario usuario, meusDadosUsuario;
    private int contadorCurtidasComentario;
    private Postagem postagemCurtidaComentario, postagemDenuncia;


    private String idDonoPostagem, idPostagem;
    private int contadorComentario, contadorDenunciasComentario;
    private Postagem dadosPostagem, dadosCurtidasComentario,
            dadosComentario, dadosComentarioV2, dadosComentarioV3,
            dadosCurtidasComentarioV2, dadosDenunciaComentario;
    private Usuario dadosUsuarios;
    private String curtidaExistente;
    private DatabaseReference comentarioRef;
    private DatabaseReference ocultarComentarioV1Ref;
    private DatabaseReference removerCurtidaComentarioRef;
    private DatabaseReference excluirComentarioRef;
    private DatabaseReference verificaContadorComentarioRef;
    private DatabaseReference atualizarContadorComentarioRef;
    private DatabaseReference denunciarComentarioRef;
    private DatabaseReference denunciasComentarioRef;
    private DatabaseReference deseocultarComentarioV1Ref;
    private DatabaseReference curtidasComentarioRef;
    private DatabaseReference verificaCurtidaMinhaRef;
    private DatabaseReference removerCurtidasComentarioRef;
    private DatabaseReference atualizarNrComentarioRef;
    private DatabaseReference comentarioV2Ref;
    private DatabaseReference curtidasComentarioV2Ref;
    private DatabaseReference comentarioV3Ref;
    private DatabaseReference salvarCurtidaComentarioV2Ref;

    public AdapterComentarios(List<Postagem> lista, Context c, String iddonoPostagemRecebido, Usuario meusDadosUsuarios, String idPostagemRecebido) {
        this.listaComentarios = lista;
        this.context = c;
        this.idDonoPostagem = iddonoPostagemRecebido;
        this.meusDadosUsuario = meusDadosUsuarios;
        this.idPostagem = idPostagemRecebido;
        listaRespotasComentarios = new ArrayList<>();
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentarios, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Postagem postagemComentario = listaComentarios.get(position);

        //Caminho dos dados do usuário recebido
        DatabaseReference usuariosInterativoRef = firebaseRef
                .child("usuarios").child(postagemComentario.getIdUsuarioInterativo());

        //Caminho dos dados do comentario
        //if(tipoPublicacao != null){}
        comentarioRef = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo());
        ocultarComentarioV1Ref = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo())
                .child("ocultarComentario");
        removerCurtidaComentarioRef = firebaseRef
                .child("curtidasComentarioPostagem").child(idPostagem)
                .child(idUsuarioLogado)
                .child(postagemComentario.getIdUsuarioInterativo());
        excluirComentarioRef = firebaseRef.child("comentariosPostagem")
                .child(idPostagem).child(idUsuarioLogado);
        //if(idDonoPostagem != null) {}

        verificaContadorComentarioRef = firebaseRef
                .child("postagens").child(idDonoPostagem).child(idPostagem);
        atualizarContadorComentarioRef = firebaseRef
                .child("postagens")
                .child(idDonoPostagem).child(idPostagem)
                .child("totalComentarios");
        denunciarComentarioRef = firebaseRef
                .child("comentariosDenunciadosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo())
                .child(idUsuarioLogado);
        denunciasComentarioRef = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo());
        deseocultarComentarioV1Ref = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo())
                .child("ocultarComentario");
        curtidasComentarioRef = firebaseRef
                .child("curtidasComentarioPostagem").child(idPostagem)
                .child(idUsuarioLogado).child(postagemComentario.getIdUsuarioInterativo());
        verificaCurtidaMinhaRef = firebaseRef
                .child("curtidasComentarioPostagem").child(idPostagem)
                .child(idUsuarioLogado)
                .child(postagemComentario.getIdUsuarioInterativo());
        removerCurtidasComentarioRef = firebaseRef
                .child("curtidasComentarioPostagem").child(idPostagem)
                .child(idUsuarioLogado)
                .child(postagemComentario.getIdUsuarioInterativo());
        atualizarNrComentarioRef = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo())
                .child("totalCurtidasComentario");
        comentarioV2Ref = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo());
        curtidasComentarioV2Ref = firebaseRef
                .child("curtidasComentarioPostagem").child(idPostagem)
                .child(idUsuarioLogado).child(postagemComentario.getIdUsuarioInterativo());
        comentarioV3Ref = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo());
        //Caminho para salvar o total de curtidas atual
        salvarCurtidaComentarioV2Ref = firebaseRef
                .child("comentariosPostagem").child(idPostagem)
                .child(postagemComentario.getIdUsuarioInterativo())
                .child("totalCurtidasComentario");

        //Dados do comentário
        comentarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    dadosComentario = snapshot.getValue(Postagem.class);

                    // if (idUsuarioLogado.equals(idDonoPostagem)) {
                    if (idDonoPostagem != null) {
                        holder.imgButtonOcultarComentario.setClickable(true);
                        holder.imgButtonOcultarComentario.setVisibility(View.VISIBLE);
                    } else {
                        holder.imgButtonOcultarComentario.setClickable(false);
                        holder.imgButtonOcultarComentario.setVisibility(View.INVISIBLE);
                    }

                    //Caso o comentário não esteja ocultado ele seguirá essa lógica
                    if (dadosComentario.getOcultarComentario().equals("não")) {
                        holder.linearLayoutComentarios.setVisibility(View.VISIBLE);
                        holder.btnDesocultarComentario.setVisibility(View.GONE);
                        //Exibindo o total de curtidas no comentário
                        holder.txtViewTotalLikesComentario.setText(String.valueOf(dadosComentario.getTotalCurtidasComentario()));
                        //Exibindo conteúdo do comentário
                        holder.txtViewComentario.setText(dadosComentario.getComentarioPostado());
                        holder.txtViewDataComentario.setText(dadosComentario.getDataComentario());

                        //Caso o usuário clique para ocultar o comentário que está sendo exebido
                        //ele seguirá essa lógica o botão de ocultar comentário
                        //Caminho para ocultar comentário
                        holder.imgButtonOcultarComentario.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                holder.imgButtonOcultarComentario.setClickable(false);

                                ocultarComentarioV1Ref.setValue("sim").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            ToastCustomizado.toastCustomizadoCurto("Ocultado com sucesso", context);
                                            holder.imgButtonOcultarComentario.setClickable(true);
                                            //holder.linearLayoutComentarios.setVisibility(View.GONE);
                                            //holder.btnDesocultarComentario.setVisibility(View.VISIBLE);
                                        } else {
                                            ToastCustomizado.toastCustomizadoCurto("Erro ao ocultar, tente novamente", context);
                                            holder.imgButtonOcultarComentario.setClickable(true);
                                        }
                                    }
                                });
                            }
                        });

                        //Lógica para excluir um comentário
                        //Caso o usuário atual seja dono do comentário
                        if (dadosComentario.getIdUsuarioInterativo().equals(idUsuarioLogado)) {
                            holder.imgButtonExcluirComentario.setClickable(true);
                            holder.imgButtonExcluirComentario.setVisibility(View.VISIBLE);
                            holder.imgButtonDenunciarComentario.setVisibility(View.INVISIBLE);
                            holder.imgButtonDenunciarComentario.setClickable(false);
                            holder.imgButtonExcluirComentario.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                                    ProgressDialog progressDialog = new ProgressDialog(view.getRootView().getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setMessage("Excluindo, por favor aguarde...");
                                    builder.setTitle("Deseja excluir seu comentário?");
                                    builder.setMessage("Seu comentário será excluído permanentemente");
                                    builder.setCancelable(true);
                                    builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            progressDialog.show();

                                            //1 - Excluindo curtidas no comentário

                                            removerCurtidaComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //ToastCustomizado.toastCustomizadoCurto("Curtidas removidas", context);

                                                        //2 - Excluindo o comentário

                                                        excluirComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    //ToastCustomizado.toastCustomizadoCurto("Comentário removido com sucesso",context);

                                                                    //3 - Atualizando o contador de comentários na postagem

                                                                    verificaContadorComentarioRef.addValueEventListener(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            if (snapshot.getValue() != null) {
                                                                                dadosPostagem = snapshot.getValue(Postagem.class);
                                                                                ToastCustomizado.toastCustomizadoCurto("Contador " + dadosPostagem.getTotalComentarios(), context);
                                                                                if (dadosPostagem.getTotalComentarios() <= 1) {
                                                                                    contadorComentario = 0;
                                                                                } else {
                                                                                    contadorComentario = dadosPostagem.getTotalComentarios() - 1;
                                                                                }

                                                                                atualizarContadorComentarioRef.setValue(contadorComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            progressDialog.dismiss();
                                                                                            ToastCustomizado.toastCustomizadoCurto("Comentário removido com sucesso", context);
                                                                                            listaComentarios.remove(position);
                                                                                            notifyItemRemoved(position);
                                                                                        } else {
                                                                                            progressDialog.dismiss();
                                                                                            ToastCustomizado.toastCustomizadoCurto("Erro ao remover comentário, tente novamente", context);
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                            verificaContadorComentarioRef.removeEventListener(this);
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });
                                                                } else {
                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao remover comentário", context);
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao remover curtidas", context);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    builder.setNegativeButton("Cancelar", null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
                            //Caso o usuário atual não seja o dono da postagem
                        } else if (!dadosComentario.getIdUsuarioInterativo().equals(idUsuarioLogado)) {
                            holder.imgButtonExcluirComentario.setClickable(false);
                            holder.imgButtonExcluirComentario.setVisibility(View.INVISIBLE);

                            denunciarComentarioRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        holder.imgButtonDenunciarComentario.setVisibility(View.INVISIBLE);
                                        holder.imgButtonDenunciarComentario.setClickable(false);
                                    } else {
                                        holder.imgButtonDenunciarComentario.setVisibility(View.VISIBLE);
                                        holder.imgButtonDenunciarComentario.setClickable(true);
                                    }
                                    denunciarComentarioRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //Lógica para denunciar um comentário
                            holder.imgButtonDenunciarComentario.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    denunciasComentarioRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue() != null) {
                                                dadosDenunciaComentario = snapshot.getValue(Postagem.class);
                                                contadorDenunciasComentario = dadosDenunciaComentario.getTotalDenunciasComentario();
                                            }

                                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                                            builder.setTitle("Deseja realmente denunciar esse comentário?");
                                            builder.setMessage("Denunciar comentário selecionado");
                                            builder.setCancelable(true);
                                            builder.setPositiveButton("Denunciar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(context.getApplicationContext(), DenunciaPostagemActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    intent.putExtra("idPostagem", idPostagem);
                                                    intent.putExtra("idDonoComentario", postagemComentario.getIdUsuarioInterativo());
                                                    if (dadosDenunciaComentario.getTotalDenunciasComentario() >= 1) {
                                                        intent.putExtra("numeroDenuncias", contadorDenunciasComentario);
                                                    } else {
                                                        contadorDenunciasComentario = 0;
                                                        intent.putExtra("numeroDenuncias", contadorDenunciasComentario);
                                                    }
                                                    context.startActivity(intent);
                                                }
                                            });
                                            builder.setNegativeButton("Cancelar", null);
                                            AlertDialog dialog = builder.create();
                                            dialog.show();

                                            denunciasComentarioRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        }

                    } else if (dadosComentario.getOcultarComentario().equals("sim")) {
                        holder.linearLayoutComentarios.setVisibility(View.GONE);
                        holder.btnDesocultarComentario.setVisibility(View.VISIBLE);

                        //Caso o comentário queira desocultar o comentário, ele
                        //seguirá essa lógica
                        holder.btnDesocultarComentario.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                holder.btnDesocultarComentario.setClickable(false);

                                deseocultarComentarioV1Ref.setValue("não").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            ToastCustomizado.toastCustomizadoCurto("Desocultado com sucesso", context);
                                            holder.btnDesocultarComentario.setClickable(true);
                                        } else {
                                            ToastCustomizado.toastCustomizadoCurto("Erro ao desocultar, tente novamente", context);
                                            holder.btnDesocultarComentario.setClickable(true);
                                        }
                                    }
                                });
                            }
                        });
                    }

                    //Dados da curtida no comentário, essa lógica serve para mudar o icone
                    //conforme foi a interação com o comentário.
                    curtidasComentarioRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                dadosCurtidasComentario = snapshot.getValue(Postagem.class);
                                //Caso o dono da postagem tenha comentado na própria postagem
                                if (dadosCurtidasComentario.getIdUsuarioInterativo().equals(idUsuarioLogado)) {
                                    holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_like_comentario_preenchido);
                                } else {
                                    holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_like_comentario);
                                }
                            }
                            //Adicionado as 13:43 - 04/06/2022
                            curtidasComentarioRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    DatabaseReference verificaCurtidaMinhaRef = firebaseRef
                            .child("curtidasComentarioPostagem").child(idPostagem)
                            .child(idUsuarioLogado)
                            .child(postagemComentario.getIdUsuarioInterativo());

                    verificaCurtidaMinhaRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {

                                holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_like_comentario_preenchido);
                                dadosCurtidasComentarioV2 = snapshot.getValue(Postagem.class);
                                holder.imgButtonLikeComentario.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ToastCustomizado.toastCustomizadoCurto("Descurtido " + dadosComentario.getIdUsuarioInterativo(), context);
                                        holder.imgButtonLikeComentario.setVisibility(View.INVISIBLE);
                                        holder.imgButtonLikeComentario.setClickable(false);
                                        //Caso usuário atual tenha curtido esse comentário

                                        ToastCustomizado.toastCustomizadoCurto("Vou descurtir", context);

                                        //ToastCustomizado.toastCustomizadoCurto("Contador atual de comentario " + dadosComentario.getTotalCurtidasComentario(),context);

                                        //Removendo curtidas nesse comentário
                                        DatabaseReference removerCurtidasComentarioRef = firebaseRef
                                                .child("curtidasComentarioPostagem").child(idPostagem)
                                                .child(idUsuarioLogado)
                                                .child(postagemComentario.getIdUsuarioInterativo());
                                        removerCurtidasComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //Recuperando contador de curtidas atual

                                                    DatabaseReference atualizarNrComentarioRef = firebaseRef
                                                            .child("comentariosPostagem").child(idPostagem)
                                                            .child(postagemComentario.getIdUsuarioInterativo())
                                                            .child("totalCurtidasComentario");

                                                    DatabaseReference comentarioV2Ref = firebaseRef
                                                            .child("comentariosPostagem").child(idPostagem)
                                                            .child(postagemComentario.getIdUsuarioInterativo());

                                                    comentarioV2Ref.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.getValue() != null) {
                                                                dadosComentarioV2 = snapshot.getValue(Postagem.class);
                                                                //Zerando contador anterior
                                                                contadorCurtidasComentario = 0;
                                                                if (dadosComentarioV2.getTotalCurtidasComentario() <= 0) {
                                                                    //Zerando para evitar número negativo
                                                                    contadorCurtidasComentario = 0;
                                                                } else {
                                                                    //Diminuindo em 1 o contador atual
                                                                    contadorCurtidasComentario = 0;
                                                                    contadorCurtidasComentario = dadosComentarioV2.getTotalCurtidasComentario() - 1;
                                                                }

                                                                //Atualizando número de curtidas no comentário
                                                                atualizarNrComentarioRef.setValue(contadorCurtidasComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            holder.imgButtonLikeComentario.setClickable(true);
                                                                            holder.imgButtonLikeComentario.setVisibility(View.VISIBLE);
                                                                            ToastCustomizado.toastCustomizadoCurto("Descurtido com sucesso " + contadorCurtidasComentario, context);
                                                                        } else {
                                                                            holder.imgButtonLikeComentario.setClickable(true);
                                                                            holder.imgButtonLikeComentario.setVisibility(View.VISIBLE);
                                                                            ToastCustomizado.toastCustomizado("Ocorreu um erro ao descurtir, tente novamente " + contadorCurtidasComentario, context);
                                                                        }
                                                                    }
                                                                });


                                                            }
                                                            comentarioV2Ref.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_like_comentario);
                                holder.imgButtonLikeComentario.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        holder.imgButtonLikeComentario.setVisibility(View.INVISIBLE);
                                        holder.imgButtonLikeComentario.setClickable(false);
                                        //Usuário atual não curtiu o comentário
                                        ToastCustomizado.toastCustomizadoCurto("Curtido " + dadosComentario.getIdUsuarioInterativo(), context);
                                        ToastCustomizado.toastCustomizadoCurto("Vou curtir", context);
                                        HashMap<String, Object> dadosCurtidaComentario = new HashMap<>();
                                        dadosCurtidaComentario.put("idPostagem", idPostagem);
                                        dadosCurtidaComentario.put("idDonoPostagem", idDonoPostagem);
                                        dadosCurtidaComentario.put("idUsuarioInterativo", idUsuarioLogado);
                                        dadosCurtidaComentario.put("idDonoComentario", postagemComentario.getIdUsuarioInterativo());

                                        DatabaseReference curtidasComentarioV2Ref = firebaseRef
                                                .child("curtidasComentarioPostagem").child(idPostagem)
                                                .child(idUsuarioLogado).child(postagemComentario.getIdUsuarioInterativo());

                                        curtidasComentarioV2Ref.setValue(dadosCurtidaComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    DatabaseReference comentarioV3Ref = firebaseRef
                                                            .child("comentariosPostagem").child(idPostagem)
                                                            .child(postagemComentario.getIdUsuarioInterativo());

                                                    comentarioV3Ref.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.getValue() != null) {
                                                                dadosComentarioV3 = snapshot.getValue(Postagem.class);
                                                                //Recebendo em uma variável o contador atual de curtidas no comentário
                                                                contadorCurtidasComentario = 0;
                                                                if (dadosComentarioV3.getTotalCurtidasComentario() <= 0) {
                                                                    contadorCurtidasComentario = 1;
                                                                } else {
                                                                    contadorCurtidasComentario = 0;
                                                                    contadorCurtidasComentario = dadosComentarioV3.getTotalCurtidasComentario() + 1;
                                                                }

                                                                DatabaseReference salvarCurtidaComentarioV2Ref = firebaseRef
                                                                        .child("comentariosPostagem").child(idPostagem)
                                                                        .child(postagemComentario.getIdUsuarioInterativo())
                                                                        .child("totalCurtidasComentario");

                                                                salvarCurtidaComentarioV2Ref.setValue(contadorCurtidasComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            holder.imgButtonLikeComentario.setClickable(true);
                                                                            holder.imgButtonLikeComentario.setVisibility(View.VISIBLE);
                                                                            ToastCustomizado.toastCustomizadoCurto("Comentário curtido com sucesso", context);
                                                                        } else {
                                                                            holder.imgButtonLikeComentario.setClickable(true);
                                                                            holder.imgButtonLikeComentario.setVisibility(View.VISIBLE);
                                                                            ToastCustomizado.toastCustomizado("Ocorreu um erro ao curtir comentário, tente novamente", context);
                                                                        }
                                                                    }
                                                                });

                                                            }
                                                            comentarioV3Ref.removeEventListener(this);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });

                            }
                            verificaCurtidaMinhaRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usuariosInterativoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    dadosUsuarios = snapshot.getValue(Usuario.class);
                    if (dadosUsuarios.getMinhaFoto() != null) {
                        if (meusDadosUsuario.getEpilepsia().equals("Sim")) {
                            GlideCustomizado.montarGlideEpilepsia(context,
                                    dadosUsuarios.getMinhaFoto(), holder.imgViewUserComentario,
                                    android.R.color.transparent);
                        } else if (meusDadosUsuario.getEpilepsia().equals("Não")) {
                            GlideCustomizado.montarGlide(context, dadosUsuarios.getMinhaFoto(),
                                    holder.imgViewUserComentario, android.R.color.transparent);
                        }
                    }

                    holder.txtViewNomeUserComentario.setText(dadosUsuarios.getNomeUsuario());
                }
                usuariosInterativoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewUserComentario;
        private TextView txtViewDataComentario, txtViewComentario,
                txtViewTotalLikesComentario, txtViewNomeUserComentario;
        private Button btnDesocultarComentario;
        private ImageButton imgButtonLikeComentario, imgButtonExcluirComentario,
                imgButtonOcultarComentario, imgButtonDenunciarComentario;
        private LinearLayout linearLayoutComentarios;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewUserComentario = itemView.findViewById(R.id.imgViewUserComentario);
            txtViewDataComentario = itemView.findViewById(R.id.txtViewDataComentario);
            txtViewComentario = itemView.findViewById(R.id.txtViewComentario);
            txtViewTotalLikesComentario = itemView.findViewById(R.id.txtViewTotalLikesComentario);
            imgButtonLikeComentario = itemView.findViewById(R.id.imgButtonLikeComentario);
            imgButtonExcluirComentario = itemView.findViewById(R.id.imgButtonExcluirComentario);
            txtViewNomeUserComentario = itemView.findViewById(R.id.txtViewNomeUserComentario);
            imgButtonOcultarComentario = itemView.findViewById(R.id.imgButtonOcultarComentario);
            linearLayoutComentarios = itemView.findViewById(R.id.linearLayoutComentarios);
            btnDesocultarComentario = itemView.findViewById(R.id.btnDesocultarComentario);
            imgButtonDenunciarComentario = itemView.findViewById(R.id.imgButtonDenunciarComentario);
        }
    }

}