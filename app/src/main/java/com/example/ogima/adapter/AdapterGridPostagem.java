package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class AdapterGridPostagem extends RecyclerView.Adapter<AdapterGridPostagem.MyViewHolder> {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private List<Postagem> listaPostagem;
    private Context context;
    private Postagem postagemSelecionada;
    private final int limit = 4;
    private String statusEpilepsia;

    public AdapterGridPostagem(List<Postagem> listPostagem, Context c,
                               String statusEpilepsia) {
        this.listaPostagem = listPostagem;
        this.context = c;
        this.statusEpilepsia = statusEpilepsia;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grid_postagem, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        postagemSelecionada = listaPostagem.get(position);

        if (statusEpilepsia.equals("Sim")) {
            GlideCustomizado.montarGlideEpilepsia(context,
                    postagemSelecionada.getUrlPostagem(), holder.imgViewGridPostagem,
                    android.R.color.white);
        } else if (statusEpilepsia.equals("Não")) {
            GlideCustomizado.montarGlideFoto(context,
                    postagemSelecionada.getUrlPostagem(), holder.imgViewGridPostagem,
                    android.R.color.white);
        }
    }

    @Override
    public int getItemCount() {
        if (listaPostagem.size() > limit) {
            return limit;
        } else {
            return listaPostagem.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewGridPostagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewGridPostagem = itemView.findViewById(R.id.imgViewGridPostagem);
        }
    }
}
