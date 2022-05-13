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
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.DenunciaPostagemActivity;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
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
import com.iab.omid.library.giphy.adsession.video.Position;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AdapterComentarios extends RecyclerView.Adapter<AdapterComentarios.MyViewHolder> {

    private List<Postagem> listaComentarios;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Usuario usuario;
    private int contadorCurtidasComentario;
    private Postagem postagemCurtidaComentario, postagemDenuncia;
    private String donoPostagem;

    public AdapterComentarios(List<Postagem> lista, Context c, String donoPostagemStatus) {
        this.listaComentarios = lista;
        this.context = c;
        this.donoPostagem = donoPostagemStatus;
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

        DatabaseReference ocultarComentarioRef = firebaseRef
                .child("comentarios").child(postagemComentario.getIdPostagem())
                .child(postagemComentario.getIdUsuarioInterativo())
                .child("ocultarComentario");

        try {
            //Se o comentário do usuário não estiver ocultado
            if (!postagemComentario.getOcultarComentario().equals("sim")) {
                DatabaseReference dadosUsuarioRef = firebaseRef
                        .child("usuarios").child(postagemComentario.getIdUsuarioInterativo());

                DatabaseReference curtirComentarioRef = firebaseRef
                        .child("curtidasComentario").child(postagemComentario.getIdPostagem())
                        .child(idUsuarioLogado).child(postagemComentario.getIdUsuarioInterativo());

                DatabaseReference salvarCurtidaComentarioRef = firebaseRef
                        .child("comentarios").child(postagemComentario.getIdPostagem())
                        .child(postagemComentario.getIdUsuarioInterativo())
                        .child("totalCurtidasComentario");

                holder.txtViewTotalLikesComentario.setText(String.valueOf(postagemComentario.getTotalCurtidasComentario()));

                if (postagemComentario.getTotalCurtidasComentario() <= 0) {
                    contadorCurtidasComentario = 1;
                } else {
                    contadorCurtidasComentario = postagemComentario.getTotalCurtidasComentario();
                    contadorCurtidasComentario = contadorCurtidasComentario + 1;
                }

                dadosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            usuario = snapshot.getValue(Usuario.class);

                            GlideCustomizado.montarGlide(context, usuario.getMinhaFoto(),
                                    holder.imgViewUserComentario, android.R.color.transparent);

                            if (usuario.getExibirApelido().equals("não")) {
                                holder.txtViewNomeUserComentario.setText(usuario.getNomeUsuario());
                            } else {
                                holder.txtViewNomeUserComentario.setText(usuario.getApelidoUsuario());
                            }

                            //Levando usuário atual ao usuário selecionado
                            holder.txtViewNomeUserComentario.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!postagemComentario.getIdUsuarioInterativo().equals(idUsuarioLogado)) {
                                        Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("usuarioSelecionado", usuario);
                                        context.startActivity(intent);
                                    }
                                }
                            });

                            holder.imgViewUserComentario.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!postagemComentario.getIdUsuarioInterativo().equals(idUsuarioLogado)) {
                                        Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("usuarioSelecionado", usuario);
                                        context.startActivity(intent);
                                    }
                                }
                            });

                            holder.txtViewDataComentario.setText(postagemComentario.getDataComentario());
                            holder.txtViewComentario.setText(postagemComentario.getComentarioPostado());

                            //Ocultar comentário caso o usuário atual seja dono da postagem
                            if (donoPostagem == null) {
                                holder.imgButtonOcultarComentario.setVisibility(View.GONE);
                            } else if (!postagemComentario.getIdUsuarioInterativo().equals(idUsuarioLogado)) {
                                holder.imgButtonOcultarComentario.setVisibility(View.VISIBLE);
                            }

                            holder.imgButtonOcultarComentario.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ocultarComentarioRef.setValue("sim").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                ToastCustomizado.toastCustomizadoCurto("Comentário ocultado com sucesso", context.getApplicationContext());
                                                ((Activity) view.getContext()).finish();
                                                context.startActivity(((Activity) view.getContext()).getIntent());
                                            } else {
                                                ToastCustomizado.toastCustomizadoCurto("Erro ao ocultar comentário, tente novamente!", context.getApplicationContext());
                                            }
                                        }
                                    });
                                }
                            });

                            if (postagemComentario.getIdUsuarioInterativo().equals(idUsuarioLogado)) {
                                holder.imgButtonExcluirComentario.setVisibility(View.VISIBLE);
                                holder.imgButtonDenunciarComentario.setVisibility(View.GONE);
                                holder.imgButtonLikeComentario.setClickable(false);
                                holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_dono_postagem);
                                holder.btnViewResponderComentario.setVisibility(View.GONE);

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
                                                DatabaseReference excluirComentarioRef = firebaseRef.child("comentarios")
                                                        .child(postagemComentario.getIdPostagem()).child(idUsuarioLogado);
                                                DatabaseReference excluirCurtidaComentarioRef = firebaseRef
                                                        .child("curtidasComentario").child(postagemComentario.getIdPostagem());

                                                excluirCurtidaComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            excluirComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        progressDialog.dismiss();
                                                                        //Verificar se realmente está deixando a lista
                                                                        //em ordem correta ao remover dessa forma.
                                                                        listaComentarios.remove(position);
                                                                        notifyItemRemoved(position);
                                                                        ToastCustomizado.toastCustomizadoCurto("Comentário excluído com sucesso.", context.getApplicationContext());
                                                                    } else {
                                                                        progressDialog.dismiss();
                                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao excluir comentário, tente novamente!", context.getApplicationContext());
                                                                    }
                                                                }
                                                            });
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
                            } else {
                                holder.imgButtonExcluirComentario.setVisibility(View.GONE);
                                holder.imgButtonLikeComentario.setClickable(true);
                                holder.btnViewResponderComentario.setVisibility(View.VISIBLE);
                                holder.imgButtonDenunciarComentario.setVisibility(View.VISIBLE);

                                try {
                                    DatabaseReference verificarDenunciaComentarioRef = firebaseRef
                                            .child("comentariosDenunciados")
                                            .child(postagemComentario.getIdPostagem())
                                            .child(postagemComentario.getIdUsuarioInterativo());

                                    verificarDenunciaComentarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue() != null) {
                                                postagemDenuncia = snapshot.getValue(Postagem.class);
                                                holder.imgButtonDenunciarComentario.setVisibility(View.GONE);
                                            }else{
                                                holder.imgButtonDenunciarComentario.setVisibility(View.VISIBLE);
                                            }
                                            verificarDenunciaComentarioRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    holder.imgButtonDenunciarComentario.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                                                builder.setTitle("Deseja realmente denunciar esse comentário?");
                                                builder.setMessage("Denunciar comentário selecionado");
                                                builder.setCancelable(true);
                                                builder.setPositiveButton("Denunciar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent intent = new Intent(context.getApplicationContext(), DenunciaPostagemActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent.putExtra("idPostagem", postagemComentario.getIdPostagem());
                                                        intent.putExtra("idDonoComentario", postagemComentario.getIdUsuarioInterativo());
                                                        intent.putExtra("numeroDenuncias", postagemComentario.getTotalDenunciasComentario());
                                                        context.startActivity(intent);
                                                    }
                                                });
                                                builder.setNegativeButton("Cancelar", null);
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                        }
                                    });
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        dadosUsuarioRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Atualizando icone do imgButton caso já tenha curtido o usuário.
                curtirComentarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            postagemCurtidaComentario = snapshot.getValue(Postagem.class);
                            if (postagemCurtidaComentario.getIdUsuarioInterativo().equals(postagemCurtidaComentario.getIdUsuarioInterativo())) {
                                holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_like_comentario_preenchido);
                            }
                        }
                        curtirComentarioRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.imgButtonLikeComentario.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Removendo a curtida caso o usuário clique no
                        //imgButton mesmo já tendo curtido
                        if (postagemCurtidaComentario != null) {
                            curtirComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        int atualizaCurtida = postagemComentario.getTotalCurtidasComentario() - 1;
                                        if (atualizaCurtida <= -1) {
                                            atualizaCurtida = 0;
                                        }
                                        salvarCurtidaComentarioRef.setValue(atualizaCurtida).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_like_comentario);
                                                    ((Activity) view.getContext()).finish();
                                                    context.startActivity(((Activity) view.getContext()).getIntent());
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {

                            HashMap<String, Object> dadosCurtidaComentario = new HashMap<>();
                            dadosCurtidaComentario.put("idPostagem", postagemComentario.getIdPostagem());
                            dadosCurtidaComentario.put("idDonoPostagem", postagemComentario.getIdDonoPostagem());
                            dadosCurtidaComentario.put("idUsuarioInterativo", idUsuarioLogado);
                            dadosCurtidaComentario.put("idDonoComentario", postagemComentario.getIdUsuarioInterativo());
                            curtirComentarioRef.setValue(dadosCurtidaComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        salvarCurtidaComentarioRef.setValue(contadorCurtidasComentario).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    holder.imgButtonLikeComentario.setImageResource(R.drawable.ic_heart_like_comentario_preenchido);
                                                    ((Activity) view.getContext()).finish();
                                                    context.startActivity(((Activity) view.getContext()).getIntent());
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            } else {
                holder.linearLayoutComentarios.setVisibility(View.GONE);
                holder.btnDesocultarComentario.setVisibility(View.VISIBLE);
                holder.btnDesocultarComentario.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ocultarComentarioRef.setValue("não").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ToastCustomizado.toastCustomizadoCurto("Comentário desocultado com sucesso", context.getApplicationContext());
                                    ((Activity) view.getContext()).finish();
                                    context.startActivity(((Activity) view.getContext()).getIntent());
                                } else {
                                    ToastCustomizado.toastCustomizadoCurto("Erro ao desocultar comentário, tente novamente!", context.getApplicationContext());
                                }
                            }
                        });
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewUserComentario;
        private TextView txtViewDataComentario, txtViewComentario,
                txtViewTotalLikesComentario, txtViewNomeUserComentario;
        private Button btnViewResponderComentario, btnDesocultarComentario;
        private ImageButton imgButtonLikeComentario, imgButtonExcluirComentario,
                imgButtonOcultarComentario, imgButtonDenunciarComentario;
        private LinearLayout linearLayoutComentarios;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewUserComentario = itemView.findViewById(R.id.imgViewUserComentario);
            txtViewDataComentario = itemView.findViewById(R.id.txtViewDataComentario);
            txtViewComentario = itemView.findViewById(R.id.txtViewComentario);
            txtViewTotalLikesComentario = itemView.findViewById(R.id.txtViewTotalLikesComentario);
            btnViewResponderComentario = itemView.findViewById(R.id.btnViewResponderComentario);
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
