package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.R;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.model.Usuario;

import java.util.List;

public class ArrayAdapterParc extends ArrayAdapter<Usuario> {
    Context context;
    private boolean statusEpilepsia = false;
    private int currentPosition = 0;

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public ArrayAdapterParc(Context context, int resourceId, List<Usuario> listaUsuarios) {
        super(context, resourceId, listaUsuarios);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Usuario usuario = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_interacao_parc, parent, false);
        }

        ImageView imgViewUser;
        imgViewUser =  convertView.findViewById(R.id.imgViewUserIntParc);

        GlideCustomizado.loadUrl(context, usuario.getFotosParc().get(0),
                imgViewUser,
                android.R.color.transparent,
                GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia());
        return convertView;
    }

    public void mostrarProximaFoto(ImageView imgViewUser, Usuario usuario) {
        // Implemente a lógica para exibir a próxima foto do usuário na ImageView
        // Você pode usar a biblioteca Glide ou outra biblioteca de carregamento de imagem aqui
        GlideCustomizado.loadUrl(context, usuario.getFotosParc().get(1), imgViewUser, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia());
    }
}
