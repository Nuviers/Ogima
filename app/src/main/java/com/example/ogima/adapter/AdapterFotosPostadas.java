package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterFotosPostadas  extends RecyclerView.Adapter<AdapterFotosPostadas.ViewHolder> {

    private List<Usuario> listaFotosPostadas;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;

    public AdapterFotosPostadas(List<Usuario> listFotosPostadas, Context c) {
        //Configura os paramêtros do construtor.
        this.listaFotosPostadas = listFotosPostadas;
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Captura os componentes do layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_fotos_postadas,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //A lógica é executada aqui.

        Usuario usuarioFotosPostadas = listaFotosPostadas.get(position);

        DatabaseReference fotosUsuarioRef = firebaseRef.child("fotosUsuario")
                .child(idUsuarioLogado);

        fotosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    Usuario usuarioFotos = snapshot.getValue(Usuario.class);

                    ArrayList<String> listaFotos = new ArrayList<>();
                    listaFotos = usuarioFotos.getListaFotosUsuario();
                    //Configurações para ordenação
                    ArrayList<Integer> listaOrdem = new ArrayList<>();
                    listaOrdem = usuarioFotos.getListaOrdenacaoFotoPostada();
                    Comparator<Integer> comparatorOrdem = Collections.reverseOrder();
                    Collections.sort(listaOrdem, comparatorOrdem);

                    if(usuarioFotos.getContadorFotos() > 0){
                        Uri uri = Uri.parse(String.valueOf(listaFotos.get(listaOrdem.get(position))));
                        Glide.with(context).load(uri).centerCrop()
                                .into(holder.imageAdFotoPostada);
                        holder.textAdDataPostada.setText(usuarioFotos.getListaDatasFotos().get(listaOrdem.get(position)));
                        holder.textViewTituloFoto.setText(usuarioFotos.getListaTituloFotoPostada().get(listaOrdem.get(position)));
                        holder.textViewDescricaoFoto.setText(usuarioFotos.getListaDescricaoFotoPostada().get(listaOrdem.get(position)));
                    }else{
                        holder.imageAdFotoPostada.setImageResource(R.drawable.avatarfemale);
                        holder.textAdDataPostada.setText("Sem postagens");
                    }
                }
                fotosUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        //Retorna o tamanho da lista
        return listaFotosPostadas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //Inicializa os componentes do layout
        private TextView textAdDataPostada,textViewTituloFoto,textViewDescricaoFoto;
        private ImageView imageAdFotoPostada;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textAdDataPostada = itemView.findViewById(R.id.textAdDataPostada);
            imageAdFotoPostada = itemView.findViewById(R.id.imageAdFotoPostada);
            textViewTituloFoto = itemView.findViewById(R.id.textViewTituloFoto);
            textViewDescricaoFoto = itemView.findViewById(R.id.textViewDescricaoFoto);

        }
    }
}
