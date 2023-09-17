package com.example.ogima.adapter;

import android.content.Context;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;

public class AdapterFotosPerfilParcEdicao extends RecyclerView.Adapter<AdapterFotosPerfilParcEdicao.PhotoViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private ArrayList<String> listaFotosParc;
    private boolean fotoJaExibida = false;

    public AdapterFotosPerfilParcEdicao(Context context, ArrayList<String> listFotosParc) {
        this.context = context;
        this.listaFotosParc = listFotosParc;
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    public int getItemCount() {
        return listaFotosParc.size();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View photoView = inflater.inflate(R.layout.adapter_grid_foto_parc, parent, false);
        return new AdapterFotosPerfilParcEdicao.PhotoViewHolder(photoView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        GlideCustomizado.loadUrl(context, listaFotosParc.get(position),
                holder.imgViewGridPostagem, android.R.color.transparent,
                GlideCustomizado.CENTER_CROP, false, true);
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        //Componentes do próprio layout
        private ImageView imgViewGridPostagem;

        public PhotoViewHolder(View itemView) {
            super(itemView);

            imgViewGridPostagem = itemView.findViewById(R.id.imgViewGridPostagem);
        }

        private void exibirPostagemFoto(String urlPostagem, boolean epilepsia) {
            if (epilepsia) {
                GlideCustomizado.montarGlideFotoEpilepsia(context, urlPostagem,
                        imgViewGridPostagem, android.R.color.transparent);
            } else {
                GlideCustomizado.montarGlideFoto(context, urlPostagem,
                        imgViewGridPostagem, android.R.color.transparent);
            }
        }
    }

    public void swapItems(int fromPosition, int toPosition) {
        Collections.swap(listaFotosParc, fromPosition, toPosition);
        ToastCustomizado.toastCustomizadoCurto("Posição anterior " + fromPosition + " posição nova " + toPosition, context);
        notifyDataSetChanged();
        DatabaseReference salvarOrdemNovaRef = firebaseRef.child("usuarioParc")
                .child(idUsuarioLogado).child("fotosParc");
        salvarOrdemNovaRef.setValue(listaFotosParc);
    }
}
