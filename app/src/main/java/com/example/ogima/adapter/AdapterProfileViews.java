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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
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

import java.util.List;

public class AdapterProfileViews extends RecyclerView.Adapter<AdapterProfileViews.ViewHolder>{
    private List<Usuario> listaViewers;
    private Context context;


    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;


    public AdapterProfileViews(List<Usuario> listViewers, Context c) {
        this.listaViewers = listViewers;
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_profile_views,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Usuario usuarioViewer = listaViewers.get(position);

        DatabaseReference verificaUser = firebaseRef.child("usuarios")
                .child(usuarioViewer.getIdUsuario());

        DatabaseReference verificaBlock = firebaseRef
                .child("blockUser").child(idUsuarioLogado).child(usuarioViewer.getIdUsuario());

        verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    holder.fotoViewer.setImageResource(R.drawable.avatarfemale);
                }else{
                    if(usuarioViewer.getMinhaFoto() != null){
                        Uri uri = Uri.parse(usuarioViewer.getMinhaFoto());
                        Glide.with(context).load(uri).centerCrop()
                                .into(holder.fotoViewer);
                    }else{
                        holder.fotoViewer.setImageResource(R.drawable.avatarfemale);
                    }
                }
                verificaBlock.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.nomeViewer.setText(usuarioViewer.getNomeUsuario());

        holder.nomeViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            Usuario usuarioMeu = snapshot.getValue(Usuario.class);

                            DatabaseReference verificaBlock = firebaseRef
                                    .child("blockUser").child(idUsuarioLogado).child(usuarioMeu.getIdUsuario());

                            verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null){
                                        Intent intentBlock = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                                        intentBlock.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intentBlock.putExtra("blockedUser", "blockedUser");
                                        intentBlock.putExtra("usuarioSelecionado", usuarioMeu);
                                        intentBlock.putExtra("backIntent", "amigosFragment");
                                        context.startActivity(intentBlock);
                                    }else{
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
            }
        });

        holder.fotoViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            Usuario usuarioMeu = snapshot.getValue(Usuario.class);

                            DatabaseReference verificaBlock = firebaseRef
                                    .child("blockUser").child(idUsuarioLogado).child(usuarioMeu.getIdUsuario());

                            verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null){
                                        Intent intentBlock = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                                        intentBlock.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intentBlock.putExtra("blockedUser", "blockedUser");
                                        intentBlock.putExtra("usuarioSelecionado", usuarioMeu);
                                        intentBlock.putExtra("backIntent", "amigosFragment");
                                        context.startActivity(intentBlock);
                                    }else{
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaViewers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView nomeViewer;
        private ImageView fotoViewer;

        public ViewHolder(View itemView) {
            super(itemView);

            nomeViewer = itemView.findViewById(R.id.textNomeViewer);
            fotoViewer = itemView.findViewById(R.id.imageViewer);
        }
    }
}
