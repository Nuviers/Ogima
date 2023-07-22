package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

public class AdapterShareMessage extends RecyclerView.Adapter<AdapterShareMessage.MyViewHolder> {

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
    private TextView textViewSelecionados;
    private int contadorSelecionado = 0;
    private int limiteSelecao = 4;

    private HashSet<Usuario> usuariosSelecionados = new HashSet<>();
    private Button btnEnviarMensagem;

    public AdapterShareMessage(HashSet<Usuario> listaContato, Context c, TextView txtViewSelecionados, Button btnShareMessage) {
        this.context = c;
        this.listaContato = listaContato;
        this.textViewSelecionados = txtViewSelecionados;
        this.btnEnviarMensagem = btnShareMessage;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_share_message,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ordenarLista();

        Usuario usuario = (Usuario) treeSetUsuarios.toArray()[position];

        if (usuario.getContatoFavorito() != null) {
            if (usuario.getContatoFavorito().equals("sim")) {
                holder.imgBtnContatoFavoritado.setVisibility(View.VISIBLE);
            } else {
                holder.imgBtnContatoFavoritado.setVisibility(View.GONE);
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
                            //Verifica se o usuário atingiu o limite de seleção e se está tentando
                            //ultrapassar o limite.
                            if (contadorSelecionado == limiteSelecao && !usuariosSelecionados.contains(usuario)) {
                                ToastCustomizado.toastCustomizadoCurto("Limite de usuários selecionados atingido", context);
                            } else {
                                if (usuariosSelecionados.contains(usuario)) {
                                    // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, false);
                                } else {
                                    // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, true);
                                }
                            }
                        }
                    });

                    holder.imgViewFotoPerfilContato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Verifica se o usuário atingiu o limite de seleção e se está tentando
                            //ultrapassar o limite.
                            if (contadorSelecionado == limiteSelecao && !usuariosSelecionados.contains(usuario)) {
                                ToastCustomizado.toastCustomizadoCurto("Limite de usuários selecionados atingido", context);
                            } else {
                                if (usuariosSelecionados.contains(usuario)) {
                                    // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, false);
                                } else {
                                    // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, true);
                                }
                            }
                        }
                    });

                    holder.txtViewNomePerfilContato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Verifica se o usuário atingiu o limite de seleção e se está tentando
                            //ultrapassar o limite.
                            if (contadorSelecionado == limiteSelecao && !usuariosSelecionados.contains(usuario)) {
                                ToastCustomizado.toastCustomizadoCurto("Limite de usuários selecionados atingido", context);
                            } else {
                                if (usuariosSelecionados.contains(usuario)) {
                                    // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, false);
                                } else {
                                    // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, true);
                                }
                            }
                        }
                    });

                    holder.txtViewNivelAmizadeContato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Verifica se o usuário atingiu o limite de seleção e se está tentando
                            //ultrapassar o limite.
                            if (contadorSelecionado == limiteSelecao && !usuariosSelecionados.contains(usuario)) {
                                ToastCustomizado.toastCustomizadoCurto("Limite de usuários selecionados atingido", context);
                            } else {
                                if (usuariosSelecionados.contains(usuario)) {
                                    // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, false);
                                } else {
                                    // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                                    selecionarUsuario(usuario, holder.constraintLayoutShare, true);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listaContato.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPerfilContato;
        private TextView txtViewNomePerfilContato, txtViewNivelAmizadeContato;
        private Button btnNumeroMensagemTotal;
        private ImageButton imgBtnContatoFavoritado;
        private ConstraintLayout constraintLayoutShare;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoPerfilContato = itemView.findViewById(R.id.imgViewFotoPerfilContato);
            txtViewNomePerfilContato = itemView.findViewById(R.id.txtViewNomePerfilContato);
            txtViewNivelAmizadeContato = itemView.findViewById(R.id.txtViewNivelAmizadeContato);
            btnNumeroMensagemTotal = itemView.findViewById(R.id.btnNumeroMensagemTotal);
            imgBtnContatoFavoritado = itemView.findViewById(R.id.imgBtnContatoFavoritado);
            constraintLayoutShare = itemView.findViewById(R.id.constraintLayoutShare);
        }
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

    private void selecionarUsuario(Usuario usuarioSelecionado, ConstraintLayout constraintLayout, Boolean marcarUsuario) {

        String hexColor = "#6495ED"; // azul claro
        int greenColor = Color.parseColor(hexColor);

        if (marcarUsuario) {
            //Seleciona
            usuariosSelecionados.add(usuarioSelecionado);
            contadorSelecionado++;
            constraintLayout.setBackgroundColor(greenColor);
        } else {
            //Desmarca
            usuariosSelecionados.remove(usuarioSelecionado);
            contadorSelecionado--;
            constraintLayout.setBackgroundColor(Color.WHITE);
        }

        textViewSelecionados.setText("" + contadorSelecionado + "/" + "4");
    }

    //Retorna para a ShareMessageActivity a lista com os usuários selecionados.
    public HashSet<Usuario> usuariosSelecionados() {
        return usuariosSelecionados;
    }
}
