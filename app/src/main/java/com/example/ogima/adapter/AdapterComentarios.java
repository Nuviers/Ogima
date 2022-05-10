package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class AdapterComentarios extends RecyclerView.Adapter<AdapterComentarios.MyViewHolder> {

    private List<Postagem> listaComentarios;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Usuario usuario;

    public AdapterComentarios(List<Postagem> lista, Context c) {
        this.listaComentarios = lista;
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentarios,parent,false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {



        Postagem postagemComentario = listaComentarios.get(position);

        DatabaseReference dadosUsuarioRef = firebaseRef
                .child("usuarios").child(postagemComentario.getIdPostador());

        dadosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null){
                    usuario = snapshot.getValue(Usuario.class);

                    GlideCustomizado.montarGlide(context, usuario.getMinhaFoto(),
                            holder.imgViewUserComentario, android.R.color.transparent);

                    holder.txtViewDataComentario.setText(postagemComentario.getDataComentario());
                    holder.txtViewComentario.setText(postagemComentario.getComentarioPostado());

                }
                dadosUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try{

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgViewUserComentario;
        private TextView txtViewDataComentario, txtViewComentario,
                txtViewTotalLikesComentario;
        private Button btnViewResponderComentario;
        private ImageButton imgButtonLikeComentario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewUserComentario = itemView.findViewById(R.id.imgViewUserComentario);
            txtViewDataComentario = itemView.findViewById(R.id.txtViewDataComentario);
            txtViewComentario = itemView.findViewById(R.id.txtViewComentario);
            txtViewTotalLikesComentario = itemView.findViewById(R.id.txtViewTotalLikesComentario);
            btnViewResponderComentario = itemView.findViewById(R.id.btnViewResponderComentario);
            imgButtonLikeComentario = itemView.findViewById(R.id.imgButtonLikeComentario);
        }
    }

}
