package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.activity.FriendsRequestsActivity;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFriendsRequests extends RecyclerView.Adapter<AdapterFriendsRequests.ViewHolder> {
    private List<Usuario> listaAmigos;
    private Context context;

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    Usuario usuario;
    Usuario usuarioMeu;
    private DatabaseReference usuarioRef;
    private DatabaseReference amigosTwoRef;
    private DatabaseReference pedidosTwoRef;
    int amigosAtuais, pedidosAtuais;
    private ValueEventListener valueEventListenerDados;

    public AdapterFriendsRequests(List<Usuario> listAmigos, Context c) {
        this.listaAmigos = listAmigos;
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        usuarioRef = firebaseRef.child("usuarios");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_friends_requests, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Usuario usuarioAmigo = listaAmigos.get(position);

        DatabaseReference verificaBlock = firebaseRef
                .child("blockUser").child(idUsuarioLogado).child(usuarioAmigo.getIdUsuario());

        verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    holder.fotoAmigo.setImageResource(R.drawable.avatarfemale);
                }else{
                    if (usuarioAmigo.getMinhaFoto() != null) {
                        Uri uri = Uri.parse(usuarioAmigo.getMinhaFoto());
                        Glide.with(context).load(uri).centerCrop()
                                .into(holder.fotoAmigo);
                    } else {
                        holder.fotoAmigo.setImageResource(R.drawable.avatarfemale);
                    }
                }
                verificaBlock.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference verificaUser = firebaseRef.child("usuarios")
                .child(usuarioAmigo.getIdUsuario());


        holder.nomeAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    verificaUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null){
                                usuarioMeu = snapshot.getValue(Usuario.class);

                                DatabaseReference verificaBlock = firebaseRef
                                        .child("blockUser").child(idUsuarioLogado).child(usuarioMeu.getIdUsuario());

                                verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.getValue() != null){
                                            ToastCustomizado.toastCustomizadoCurto("Perfil do usuário indisponível!", context);
                                        }else if (snapshot.getValue() == null){
                                            Intent intentthree = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                                            intentthree.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intentthree.putExtra("usuarioSelecionado", usuarioMeu);
                                            intentthree.putExtra("backIntent", "amigosFragment");
                                            context.startActivity(intentthree);
                                        }
                                        verificaBlock.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            verificaUser.removeEventListener(this);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        holder.fotoAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    verificaUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null){
                                usuarioMeu = snapshot.getValue(Usuario.class);

                                DatabaseReference verificaBlock = firebaseRef
                                        .child("blockUser").child(idUsuarioLogado).child(usuarioMeu.getIdUsuario());

                                verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.getValue() != null){
                                            ToastCustomizado.toastCustomizadoCurto("Perfil do usuário indisponível!", context);
                                        }else if (snapshot.getValue() == null){
                                            Intent intentthree = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                                            intentthree.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intentthree.putExtra("usuarioSelecionado", usuarioMeu);
                                            intentthree.putExtra("backIntent", "amigosFragment");
                                            context.startActivity(intentthree);
                                        }
                                        verificaBlock.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            verificaUser.removeEventListener(this);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        //ToastCustomizado.toastCustomizadoCurto("teste 1 " + usuarioAmigo.getNomeUsuario(),context);

        amigosTwoRef = firebaseRef.child("friends")
                .child(idUsuarioLogado);

        pedidosTwoRef = firebaseRef.child("pendenciaFriend")
                .child(idUsuarioLogado);

        //Dados do usuário atual
        usuarioRef.child(idUsuarioLogado).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    usuario = snapshot.getValue(Usuario.class);
                    amigosAtuais = usuario.getAmigosUsuario();
                    pedidosAtuais = usuario.getPedidosAmizade();
                }
                usuarioRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference verificaPedido = pedidosTwoRef.child(usuarioAmigo.getIdUsuario());

        verificaPedido.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    //Usuario usuarioCapturado = dataSnapshot.getValue(Usuario.class);
                    holder.btnVerStatusAmigo.setText("Aceitar amizade");
                    holder.btnVerStatusAmigo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            HashMap<String, Object> dadosAddFriend = new HashMap<>();
                            dadosAddFriend.put("nomeUsuario", usuarioAmigo.getNomeUsuario());
                            dadosAddFriend.put("minhaFoto", usuarioAmigo.getMinhaFoto());
                            dadosAddFriend.put("idUsuario", usuarioAmigo.getIdUsuario());
                            dadosAddFriend.put("nomeUsuarioPesquisa", usuarioAmigo.getNomeUsuarioPesquisa());
                            amigosTwoRef.child(usuarioAmigo.getIdUsuario()).setValue(dadosAddFriend).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //Adicionando +1 no amigo
                                        usuarioRef.child(usuarioAmigo.getIdUsuario())
                                                .child("amigosUsuario").setValue(amigosAtuais+1);
                                        //Adicionando + 1 no usuário atual
                                        usuarioRef.child(usuario.getIdUsuario())
                                                .child("amigosUsuario").setValue(amigosAtuais+1);
                                        //Removendo pedido de amizade no usuário
                                        if(pedidosAtuais > 0){
                                            usuarioRef.child(usuario.getIdUsuario())
                                                    .child("pedidosAmizade").setValue(pedidosAtuais-1);
                                        }
                                        verificaPedido.removeValue();
                                        listaAmigos.clear();
                                    }
                                }
                            });
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference verificarAmigo = amigosTwoRef.child(usuarioAmigo.getIdUsuario());

        verificarAmigo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    //Usuario usuarioCapturado = dataSnapshot.getValue(Usuario.class);
                    holder.btnVerStatusAmigo.setText("Desfazer amizade");
                    holder.btnVerStatusAmigo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listaAmigos.clear();
                            verificarAmigo.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        if(amigosAtuais > 0){
                                            //Diminuindo 1 no usuário atual
                                            usuarioRef.child(usuario.getIdUsuario())
                                                    .child("amigosUsuario").setValue(amigosAtuais-1);
                                            usuarioRef.child(usuarioAmigo.getIdUsuario())
                                                    .child("amigosUsuario").setValue(amigosAtuais-1);
                                        }
                                        //Foi colocado esse clear a baixo no dia 17/02/2022
                                        listaAmigos.clear();
                                        ToastCustomizado.toastCustomizadoCurto("Excluido com sucesso", context);
                                    }else{
                                        ToastCustomizado.toastCustomizadoCurto("Erro ao excluir amigo, tente novamente", context);
                                    }
                                }
                            });
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.nomeAmigo.setText(usuarioAmigo.getNomeUsuario());

        holder.imgButtonRejeitarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaAmigos.clear();
                DatabaseReference removerPedidoRef = pedidosTwoRef.child(usuarioAmigo.getIdUsuario());
                removerPedidoRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            if(usuarioAmigo.getPedidosAmizade()> 0){
                                //Diminuindo 1 no usuário atual
                                usuarioRef.child(usuario.getIdUsuario())
                                        .child("pedidosAmizade").setValue(pedidosAtuais-1);
                            }
                            removerPedidoRef.removeValue();
                            ToastCustomizado.toastCustomizadoCurto("Pedido de amizade rejeitado com sucesso!", context);
                        }else{
                            ToastCustomizado.toastCustomizadoCurto("Não existe pedido de amizade desse usuário", context);
                        }
                        removerPedidoRef.removeEventListener(this);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaAmigos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nomeAmigo;
        private ImageView fotoAmigo;
        ImageButton imgButtonRejeitarPedido;
        Button btnVerStatusAmigo;

        public ViewHolder(View itemView) {
            super(itemView);

            nomeAmigo = itemView.findViewById(R.id.textNomeAmigo);
            fotoAmigo = itemView.findViewById(R.id.imageAmigo);
            btnVerStatusAmigo = itemView.findViewById(R.id.btnVerStatusAmigo);
            imgButtonRejeitarPedido = itemView.findViewById(R.id.imgButtonRejeitarPedido);
        }
    }
}
