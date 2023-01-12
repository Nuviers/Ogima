package com.example.ogima.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.ChatInicioActivity;
import com.example.ogima.activity.ConversaActivity;
import com.example.ogima.fragment.ContatoFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterContato extends RecyclerView.Adapter<AdapterContato.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private List<Usuario> listaContato;
    private Context context;
    private DatabaseReference verificaContatoRef;
    private ValueEventListener valueEventListenerContato;
    private Boolean verificaFavorito;

    public AdapterContato(List<Usuario> listaContato, Context c) {
        this.context = c;
        this.listaContato = listaContato;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contato,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Collections.sort(listaContato, new Comparator<Usuario>() {
            @Override
            public int compare(Usuario o1, Usuario o2) {
                if (o1.getContatoFavorito().equals("sim") && o2.getContatoFavorito().equals("não")) {
                    return -1;
                } else if (o1.getContatoFavorito().equals("não") && o2.getContatoFavorito().equals("sim")) {
                    return 1;
                } else {
                    return o1.getNomeUsuarioPesquisa().compareTo(o2.getNomeUsuarioPesquisa());
                }
            }
        });

        Usuario usuario = listaContato.get(position);

        if (usuario.getEpilepsia().equals("Sim")) {
            GlideCustomizado.montarGlideEpilepsia(context, usuario.getMinhaFoto(),
                    holder.imgViewFotoPerfilContato, android.R.color.transparent);
        } else {
            GlideCustomizado.montarGlide(context, usuario.getMinhaFoto(),
                    holder.imgViewFotoPerfilContato, android.R.color.transparent);
        }

        if (usuario.getExibirApelido().equals("sim")) {
            holder.txtViewNomePerfilContato.setText(usuario.getApelidoUsuario());
        } else {
            holder.txtViewNomePerfilContato.setText(usuario.getNomeUsuario());
        }

        //holder.txtViewLastMensagemChat.setText("Iaew brow, como que vai, tudo de boa contigo?");
        verificaContatoRef = firebaseRef.child("contatos")
                .child(idUsuarioLogado).child(usuario.getIdUsuario());

        verificaContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Contatos contatoInfo = snapshot.getValue(Contatos.class);

                    if (contatoInfo.getContatoFavorito() != null) {
                        if (contatoInfo.getContatoFavorito().equals("sim")) {
                            //ToastCustomizado.toastCustomizado("sim", context);
                            holder.imgBtnFavoritarContato.setVisibility(View.GONE);
                            holder.imgBtnContatoFavoritado.setVisibility(View.VISIBLE);
                        } else {
                            //ToastCustomizado.toastCustomizado("não", context);
                            holder.imgBtnContatoFavoritado.setVisibility(View.GONE);
                            holder.imgBtnFavoritarContato.setVisibility(View.VISIBLE);
                        }
                    }

                    DatabaseReference verificaConversContadorRef = firebaseRef.child("contadorMensagens")
                            .child(idUsuarioLogado).child(usuario.getIdUsuario());

                    verificaConversContadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Contatos contatosContador = snapshot.getValue(Contatos.class);
                                holder.txtViewNivelAmizadeContato.setText("Nível amizade: " + contatoInfo.getNivelAmizade());
                                holder.btnNumeroMensagemTotal.setText("" + contatosContador.getTotalMensagens());
                            } else {
                                holder.txtViewNivelAmizadeContato.setText("Nível amizade: " + "Ternura");
                                holder.btnNumeroMensagemTotal.setText("0");
                            }
                            verificaConversContadorRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //Eventos de clique
                    holder.btnNumeroMensagemTotal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ConversaActivity.class);
                            intent.putExtra("usuario", usuario);
                            intent.putExtra("contato", contatoInfo);
                            context.startActivity(intent);
                        }
                    });

                    holder.imgViewFotoPerfilContato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ConversaActivity.class);
                            intent.putExtra("usuario", usuario);
                            intent.putExtra("contato", contatoInfo);
                            context.startActivity(intent);
                        }
                    });

                    holder.txtViewNomePerfilContato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ConversaActivity.class);
                            intent.putExtra("usuario", usuario);
                            intent.putExtra("contato", contatoInfo);
                            context.startActivity(intent);
                        }
                    });

                    holder.txtViewNivelAmizadeContato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ConversaActivity.class);
                            intent.putExtra("usuario", usuario);
                            intent.putExtra("contato", contatoInfo);
                            context.startActivity(intent);
                        }
                    });
                }
                verificaContatoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference verificaContatoV2Ref = firebaseRef.child("contatos")
                .child(idUsuarioLogado).child(usuario.getIdUsuario());
        //Favoritando usuário
        holder.imgBtnFavoritarContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaContatoV2Ref.child("contatoFavorito").setValue("sim").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Favoritado com sucesso", context);
                        verificaContatoRef = firebaseRef.child("contatos")
                                .child(idUsuarioLogado).child(usuario.getIdUsuario());

                        verificaContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Contatos contatoInfo = snapshot.getValue(Contatos.class);

                                    if (contatoInfo.getContatoFavorito() != null) {
                                        if (contatoInfo.getContatoFavorito().equals("sim")) {
                                            holder.imgBtnFavoritarContato.setVisibility(View.GONE);
                                            holder.imgBtnContatoFavoritado.setVisibility(View.VISIBLE);
                                        } else {
                                            holder.imgBtnContatoFavoritado.setVisibility(View.GONE);
                                            holder.imgBtnFavoritarContato.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                                verificaContatoRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro " + e.getMessage(), context);
                        //holder.imgBtnContatoFavoritado.setVisibility(View.GONE);
                        //holder.imgBtnFavoritarContato.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        //Removendo o favorito do usuário
        holder.imgBtnContatoFavoritado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaContatoV2Ref.child("contatoFavorito").setValue("não").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Favoritado removido com sucesso", context);
                        verificaContatoRef = firebaseRef.child("contatos")
                                .child(idUsuarioLogado).child(usuario.getIdUsuario());

                        verificaContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Contatos contatoInfo = snapshot.getValue(Contatos.class);

                                    if (contatoInfo.getContatoFavorito() != null) {
                                        if (contatoInfo.getContatoFavorito().equals("sim")) {
                                            holder.imgBtnFavoritarContato.setVisibility(View.GONE);
                                            holder.imgBtnContatoFavoritado.setVisibility(View.VISIBLE);
                                        } else {
                                            holder.imgBtnContatoFavoritado.setVisibility(View.GONE);
                                            holder.imgBtnFavoritarContato.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                                verificaContatoRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastCustomizado.toastCustomizadoCurto("Erro ao remover favorito " + e.getMessage(), context);
                        //holder.imgBtnFavoritarContato.setVisibility(View.GONE);
                        //holder.imgBtnContatoFavoritado.setVisibility(View.VISIBLE);
                    }
                });
            }
        });


        /*
        //@Limitador de exibição do número de mensagens caso precise.
        int numeroMensagens = Integer.parseInt(holder.btnNumeroMensagem.getText().toString());

        if (numeroMensagens >= 999) {
            holder.btnNumeroMensagem.setText("999+");
        }
         */

        /*
        //@Badge - crachá (indicador numérico)
        holder.btnNumeroMensagem.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onGlobalLayout() {
                BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
                badgeDrawable.setNumber(1000);
                badgeDrawable.setBackgroundColor(Color.parseColor("#0000ff"));
                badgeDrawable.setVerticalOffset(20);
                badgeDrawable.setHorizontalOffset(15);
                badgeDrawable.setMaxCharacterCount(999);
                attachBadgeDrawable(badgeDrawable, holder.btnNumeroMensagem, null);
                holder.btnNumeroMensagem.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
         */

    }

    @Override
    public int getItemCount() {
        return listaContato.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPerfilContato;
        private TextView txtViewNomePerfilContato, txtViewNivelAmizadeContato;
        private Button btnNumeroMensagemTotal;
        private ImageButton imgBtnFavoritarContato, imgBtnContatoFavoritado;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoPerfilContato = itemView.findViewById(R.id.imgViewFotoPerfilContato);
            txtViewNomePerfilContato = itemView.findViewById(R.id.txtViewNomePerfilContato);
            txtViewNivelAmizadeContato = itemView.findViewById(R.id.txtViewNivelAmizadeContato);
            btnNumeroMensagemTotal = itemView.findViewById(R.id.btnNumeroMensagemTotal);
            imgBtnFavoritarContato = itemView.findViewById(R.id.imgBtnFavoritarContato);
            imgBtnContatoFavoritado = itemView.findViewById(R.id.imgBtnContatoFavoritado);
        }
    }

    public void adicionarItemContato(Usuario usuarioContato) {
        listaContato.add(usuarioContato);
        notifyItemRangeRemoved(0, listaContato.size());
        notifyItemRangeInserted(0, listaContato.size() - 1);
    }

    public void removerEventListener() {
        verificaContatoRef.removeEventListener(valueEventListenerContato);
    }
}
