package com.example.ogima.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterCurtidasPostagem extends RecyclerView.Adapter<AdapterCurtidasPostagem.MyViewHolder> {

    private List<Postagem> listaCurtidas;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Usuario usuario, usuarioMeu;
    private DatabaseReference analisandoUsuarioRef;

    public AdapterCurtidasPostagem(List<Postagem> lista, Context c) {
        this.listaCurtidas = lista;
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_curtidas_postagem, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        try {
            Postagem postagemCurtida = listaCurtidas.get(position);

            analisandoUsuarioRef = firebaseRef
                    .child("usuarios").child(postagemCurtida.getIdPostador());

            analisandoUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        usuarioMeu = snapshot.getValue(Usuario.class);
                    }
                    analisandoUsuarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.txtViewNomeUserCurtida.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!postagemCurtida.getIdPostador().equals(idUsuarioLogado)) {
                        Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("usuarioSelecionado", usuarioMeu);
                        context.startActivity(intent);
                    }
                }
            });

            holder.imgViewUserCurtida.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!postagemCurtida.getIdPostador().equals(idUsuarioLogado)) {
                        Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("usuarioSelecionado", usuarioMeu);
                        context.startActivity(intent);
                    }
                }
            });

            DatabaseReference dadosUsuarioRef = firebaseRef
                    .child("usuarios").child(postagemCurtida.getIdPostador());

            dadosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        usuario = snapshot.getValue(Usuario.class);

                        if (usuario.getMinhaFoto() != null) {
                            GlideCustomizado.montarGlide(context, usuario.getMinhaFoto(),
                                    holder.imgViewUserCurtida, android.R.color.transparent);
                        }

                        if (usuario.getExibirApelido().equals("não")) {
                            holder.txtViewNomeUserCurtida.setText(usuario.getNomeUsuario());
                        } else {
                            holder.txtViewNomeUserCurtida.setText(usuario.getApelidoUsuario());
                        }

                        holder.txtViewDataCurtida.setText(postagemCurtida.getDataCurtidaPostagem());
                    }
                    dadosUsuarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listaCurtidas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewUserCurtida;
        private TextView txtViewDataCurtida, txtViewNomeUserCurtida;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewUserCurtida = itemView.findViewById(R.id.imgViewUserCurtida);
            txtViewDataCurtida = itemView.findViewById(R.id.txtViewDataCurtida);
            txtViewNomeUserCurtida = itemView.findViewById(R.id.txtViewNomeUserCurtida);
        }
    }
}
