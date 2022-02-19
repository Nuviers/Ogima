package com.example.ogima.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterSeguidores extends RecyclerView.Adapter<AdapterSeguidores.ViewHolder>{
    private List<Usuario> listaSeguidores;
    private Context context;


    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;


    public AdapterSeguidores(List<Usuario> listSeguidores, Context c) {
        this.listaSeguidores = listSeguidores;
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_seguidores,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Usuario usuarioSeguidor = listaSeguidores.get(position);

        DatabaseReference verificaBlock = firebaseRef
                .child("blockUser").child(idUsuarioLogado).child(usuarioSeguidor.getIdUsuario());

        verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    holder.fotoSeguidor.setImageResource(R.drawable.avatarfemale);
                }else{
                    if(usuarioSeguidor.getMinhaFoto() != null){
                        Uri uri = Uri.parse(usuarioSeguidor.getMinhaFoto());
                        Glide.with(context).load(uri).centerCrop()
                                .into(holder.fotoSeguidor);
                    }else{
                        holder.fotoSeguidor.setImageResource(R.drawable.avatarfemale);
                    }
                }
                verificaBlock.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.nomeSeguidor.setText(usuarioSeguidor.getNomeUsuario());

        DatabaseReference seguindoRef = firebaseRef.child("seguindo")
                .child( idUsuarioLogado )
                .child( usuarioSeguidor.getIdUsuario() );

        seguindoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if( dataSnapshot.exists() ){
                            //Já está seguindo
                            holder.buttonVerStatus.setText("Parar de seguir");
                        }else {
                            //Ainda não está seguindo
                            holder.buttonVerStatus.setText("Seguir");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );


    }

    @Override
    public int getItemCount() {
        return listaSeguidores.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView nomeSeguidor;
        private ImageView fotoSeguidor;
        Button buttonVerStatus;

        public ViewHolder(View itemView) {
            super(itemView);

            nomeSeguidor = itemView.findViewById(R.id.textNomeSeguidor);
            fotoSeguidor = itemView.findViewById(R.id.imageSeguidor);
            buttonVerStatus = itemView.findViewById(R.id.buttonVerStatus);

        }
    }
}
