package com.example.ogima.adapter;

import android.annotation.SuppressLint;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class AdapterContato extends RecyclerView.Adapter<AdapterContato.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private HashSet<Usuario> listaContato;
    private Context context;
    public DatabaseReference verificaContatoRef, verificaConversaContadorRef;
    public ValueEventListener listenerAdapterContato, listenerConversaContador;
    //TreeSet usado no lugar do hashset pois mantêm a ordenação.
    private TreeSet<Usuario> treeSetUsuarios;

    public AdapterContato(HashSet<Usuario> listaContato, Context c) {
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
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ordenarLista();

        Usuario usuario = (Usuario) treeSetUsuarios.toArray()[position];

        if (usuario.getContatoFavorito() != null) {
            if (usuario.getContatoFavorito().equals("sim")) {
                holder.imgBtnFavoritarContato.setVisibility(View.GONE);
                holder.imgBtnContatoFavoritado.setVisibility(View.VISIBLE);
            } else {
                holder.imgBtnContatoFavoritado.setVisibility(View.GONE);
                holder.imgBtnFavoritarContato.setVisibility(View.VISIBLE);
            }
        }

        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado);

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioLogado = snapshot.getValue(Usuario.class);
                    if (usuarioLogado.getEpilepsia().equals("Sim")) {
                        GlideCustomizado.montarGlideEpilepsia(context, usuario.getMinhaFoto(),
                                holder.imgViewFotoPerfilContato, android.R.color.transparent);
                    } else {
                        GlideCustomizado.montarGlide(context, usuario.getMinhaFoto(),
                                holder.imgViewFotoPerfilContato, android.R.color.transparent);
                    }
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.txtViewNomePerfilContato.setText(usuario.getNomeUsuario());

        //holder.txtViewLastMensagemChat.setText("Iaew brow, como que vai, tudo de boa contigo?");
        verificaContatoRef = firebaseRef.child("contatos")
                .child(idUsuarioLogado).child(usuario.getIdUsuario());

        listenerAdapterContato = verificaContatoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Contatos contatoInfo = snapshot.getValue(Contatos.class);

                    verificaConversaContadorRef = firebaseRef.child("contadorMensagens")
                            .child(idUsuarioLogado).child(usuario.getIdUsuario());

                    listenerConversaContador = verificaConversaContadorRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Contatos contatosContador = snapshot.getValue(Contatos.class);
                                if (contatoInfo.getNivelAmizade() != null) {
                                    holder.txtViewNivelAmizadeContato.setText("Nível amizade: " + contatoInfo.getNivelAmizade());
                                } else {
                                    holder.txtViewNivelAmizadeContato.setText("Nível amizade: " + "Sem definição");
                                }
                                holder.btnNumeroMensagemTotal.setText("" + contatosContador.getTotalMensagens());
                            } else {
                                holder.txtViewNivelAmizadeContato.setText("Nível amizade: " + "Ternura");
                                holder.btnNumeroMensagemTotal.setText("0");
                            }
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Favoritando usuário
        holder.imgBtnFavoritarContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaContatoRef = firebaseRef.child("contatos")
                        .child(idUsuarioLogado).child(usuario.getIdUsuario());
                verificaContatoRef.child("contatoFavorito").setValue("sim").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Favoritado com sucesso", context);

                        usuario.setContatoFavorito("sim");

                        holder.imgBtnFavoritarContato.setVisibility(View.GONE);
                        holder.imgBtnContatoFavoritado.setVisibility(View.VISIBLE);

                        ordenarLista();
                        notifyDataSetChanged();
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
                verificaContatoRef = firebaseRef.child("contatos")
                        .child(idUsuarioLogado).child(usuario.getIdUsuario());
                verificaContatoRef.child("contatoFavorito").setValue("não").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Favoritado removido com sucesso", context);

                        usuario.setContatoFavorito("não");

                        holder.imgBtnContatoFavoritado.setVisibility(View.GONE);
                        holder.imgBtnFavoritarContato.setVisibility(View.VISIBLE);

                        ordenarLista();
                        notifyDataSetChanged();

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

    public void removerItemConversa(Usuario usuarioExcluido) {
        listaContato.remove(usuarioExcluido);
        notifyDataSetChanged();
    }

    private void ordenarLista() {

        treeSetUsuarios = new TreeSet<>(new Comparator<Usuario>() {
            @Override
            public int compare(Usuario o1, Usuario o2) {
                if (o1.getContatoFavorito() != null && o2.getContatoFavorito() != null
                        && o1.getNomeUsuarioPesquisa() != null && o2.getContatoFavorito() != null) {
                    if (o1.getContatoFavorito().equals("sim") && o2.getContatoFavorito().equals("não")) {
                        return -1;
                    } else if (o1.getContatoFavorito().equals("não") && o2.getContatoFavorito().equals("sim")) {
                        return 1;
                    } else {
                        return o1.getNomeUsuarioPesquisa().compareTo(o2.getNomeUsuarioPesquisa());
                    }
                }
                return 0;
            }
        });
        treeSetUsuarios.addAll(listaContato);
    }
}
