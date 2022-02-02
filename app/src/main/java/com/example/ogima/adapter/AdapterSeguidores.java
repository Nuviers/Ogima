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
import com.example.ogima.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterSeguidores extends RecyclerView.Adapter<AdapterSeguidores.ViewHolder>{
        private List<Usuario> listaSeguidores;
        private Context context;

        public AdapterSeguidores(List<Usuario> listSeguidores, Context c) {
            this.listaSeguidores = listSeguidores;
            this.context = c;
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

            holder.nomeSeguidor.setText(usuarioSeguidor.getNomeUsuario());



        if(usuarioSeguidor.getMinhaFoto() != null){
            Uri uri = Uri.parse(usuarioSeguidor.getMinhaFoto());
            Glide.with(context).load(uri).centerCrop()
                    .into(holder.fotoSeguidor);
        }else{
            holder.fotoSeguidor.setImageResource(R.drawable.avatarfemale);
        }

        }

        @Override
        public int getItemCount() {
            return listaSeguidores.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private TextView txtid,nomeSeguidor,txtmovie;
            private ImageView fotoSeguidor;
             Button buttonAction;
            public ViewHolder(View itemView) {
                super(itemView);

                nomeSeguidor = itemView.findViewById(R.id.textNomeSeguidor);
                fotoSeguidor = itemView.findViewById(R.id.imageSeguidor);
                buttonAction = itemView.findViewById(R.id.buttonAction);

                buttonAction.setOnClickListener((View.OnClickListener) itemView.getContext());
                fotoSeguidor.setOnClickListener((View.OnClickListener) itemView.getContext());
                nomeSeguidor.setOnClickListener((View.OnClickListener) itemView.getContext());
            }
        }
    }

