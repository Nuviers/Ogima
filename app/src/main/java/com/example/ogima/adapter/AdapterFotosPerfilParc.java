package com.example.ogima.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterFotosPerfilParc extends RecyclerView.Adapter<AdapterFotosPerfilParc.PhotoViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private ArrayList<String> listaFotosParc;
    private boolean fotoJaExibida = false;

    public AdapterFotosPerfilParc(Context context, ArrayList<String> listFotosParc) {
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
        View photoView = inflater.inflate(R.layout.adapter_exibicao_daily_imagem, parent, false);
        return new AdapterFotosPerfilParc.PhotoViewHolder(photoView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        GlideCustomizado.loadUrl(context, listaFotosParc.get(position),
                holder.imgViewDailyImagem, android.R.color.transparent,
                GlideCustomizado.CENTER_CROP, false, true);
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        //Componentes do próprio layout
        private ImageView imgViewDailyImagem;

        public PhotoViewHolder(View itemView) {
            super(itemView);

            imgViewDailyImagem = itemView.findViewById(R.id.imgViewPreviewDaily);
        }

        private void exibirPostagemFoto(String urlPostagem, boolean epilepsia) {
            if (epilepsia) {
                GlideCustomizado.montarGlideFotoEpilepsia(context, urlPostagem,
                        imgViewDailyImagem, android.R.color.transparent);
            } else {
                GlideCustomizado.montarGlideFoto(context, urlPostagem,
                        imgViewDailyImagem, android.R.color.transparent);
            }
        }
    }
}
