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
import com.example.ogima.helper.GlideCustomizado;
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
    private Usuario meusDadosUsuario;

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

        DatabaseReference verificarMeusDadosRef = firebaseRef
                .child("usuarios").child(idUsuarioLogado);

        verificarMeusDadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    meusDadosUsuario = snapshot.getValue(Usuario.class);
                }
                verificarMeusDadosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                    verificaUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null){
                                Usuario usuarioFinal = snapshot.getValue(Usuario.class);
                                if(usuarioFinal.getMinhaFoto() != null){
                                    if(meusDadosUsuario.getEpilepsia().equals("Sim")){
                                        GlideCustomizado.montarGlideEpilepsia(context, usuarioFinal.getMinhaFoto(),
                                                holder.fotoViewer, android.R.color.transparent);
                                    }else{
                                        GlideCustomizado.montarGlide(context, usuarioFinal.getMinhaFoto(),
                                                holder.fotoViewer, android.R.color.transparent);
                                    }
                                }else{
                                    holder.fotoViewer.setImageResource(R.drawable.avatarfemale);
                                }
                                if(usuarioFinal.getExibirApelido().equals("sim")){
                                    holder.nomeViewer.setText(usuarioFinal.getApelidoUsuario());
                                }else{
                                    holder.nomeViewer.setText(usuarioFinal.getNomeUsuario());
                                }
                            }
                            verificaUser.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                verificaBlock.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.nomeViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
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

        holder.fotoViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
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
