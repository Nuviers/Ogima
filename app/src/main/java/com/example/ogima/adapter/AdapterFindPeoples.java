package com.example.ogima.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFindPeoples extends RecyclerView.Adapter<AdapterFindPeoples.MyViewHolder> {

    private List<Usuario> listaUsuario;
    private Context context;

    public AdapterFindPeoples(List<Usuario> lista, Context c) {
        this.listaUsuario = lista;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_find_peoples, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Usuario usuario = listaUsuario.get(position);

        holder.nome.setText(usuario.getNomeUsuario());

        if(usuario.getMinhaFoto() != null){
            Uri uri = Uri.parse(usuario.getMinhaFoto());
            Glide.with(context).load(uri).into(holder.userImage);
        }else{
            holder.userImage.setImageResource(R.drawable.avatarfemale);
        }

    }

    @Override
    public int getItemCount() {
        return listaUsuario.size();
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder{

        TextView nome;
        TextView descricao;
        CircleImageView userImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textNomeFindPeople);
            descricao = itemView.findViewById(R.id.textDescFindPeople);
            userImage = itemView.findViewById(R.id.imageFindPeople);
        }
    }
}
