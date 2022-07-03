package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterFuncoesPostagem extends RecyclerView.Adapter {

    private List<Postagem> listaPostagemImagem;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private StorageReference storage;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private String idUsuarioRecebido;
    private Postagem postagemImagem;
    //
    private static final String TAG = "RecyclerAdapter";


    public AdapterFuncoesPostagem(List<Postagem> listPostagemImagem, Context c, String idRecebido) {
        this.context = c;
        this.idUsuarioRecebido = idRecebido;
        this.listaPostagemImagem = listPostagemImagem;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        storage = ConfiguracaoFirebase.getFirebaseStorage();
    }

    @Override
    public int getItemViewType(int position) {
        if(listaPostagemImagem.get(position).getTipoPostagem().equals("Gif")){
            return 1;
        }else if (listaPostagemImagem.get(position).getTipoPostagem().equals("Video")){
            return  2;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

         if (viewType == 1){
            view = layoutInflater.inflate(R.layout.adapter_postagem_gif, parent, false);
            return new ViewHolderGif(view);
        }else if (viewType == 2){
            view = layoutInflater.inflate(R.layout.adapter_postagem_video, parent, false);
            return new ViewHolderVideo(view);
        }
        view = layoutInflater.inflate(R.layout.adapter_fotos_postadas, parent, false);
        return new ViewHolderImagem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if(listaPostagemImagem.get(position).getTipoPostagem().equals("Gif")){
                ViewHolderGif viewHolderGif = (ViewHolderGif) holder;
            }else if (listaPostagemImagem.get(position).getTipoPostagem().equals("Video")){
                ViewHolderVideo viewHolderVideo = (ViewHolderVideo) holder;
            }else if (listaPostagemImagem.get(position).getTipoPostagem().equals("imagem")){
                ViewHolderImagem viewHolderImagem = (ViewHolderImagem) holder;
                Postagem postagemImagem = listaPostagemImagem.get(position);
                GlideCustomizado.montarGlideFoto(context, postagemImagem.getUrlPostagem(),
                        viewHolderImagem.imageAdFotoPostada, android.R.color.transparent);
            }
    }

    @Override
    public int getItemCount() {
        return listaPostagemImagem.size();
    }

    class ViewHolderImagem extends RecyclerView.ViewHolder{

        //Layout - adapter_fotos_postadas
        private TextView textAdDataPostada, textViewTituloFoto, textViewDescricaoFoto,
                txtViewPublicoPostagem;
        private PhotoView imageAdFotoPostada;
        private Button buttonEditarFotoPostagem, buttonExcluirFotoPostagem;
        private ImageButton imgButtonDetalhesPostagem;

        public ViewHolderImagem(@NonNull View itemView) {
            super(itemView);

            textAdDataPostada = itemView.findViewById(R.id.textAdDataPostada);
            imageAdFotoPostada = itemView.findViewById(R.id.imageAdFotoPostada);
            textViewTituloFoto = itemView.findViewById(R.id.textViewTituloFoto);
            textViewDescricaoFoto = itemView.findViewById(R.id.textViewDescricaoFoto);
            txtViewPublicoPostagem = itemView.findViewById(R.id.txtViewPublicoPostagem);
            buttonEditarFotoPostagem = itemView.findViewById(R.id.buttonEditarFotoPostagem);
            buttonExcluirFotoPostagem = itemView.findViewById(R.id.buttonExcluirFotoPostagem);
            imgButtonDetalhesPostagem = itemView.findViewById(R.id.imgButtonDetalhesPostagem);
        }
    }

    class ViewHolderGif extends RecyclerView.ViewHolder{

        //Layout - adapter_postagem_gif
        private TextView txtViewPostagemGif;

        public ViewHolderGif(@NonNull View itemView) {
            super(itemView);

            txtViewPostagemGif = itemView.findViewById(R.id.txtViewPostagemGif);
        }
    }

    class ViewHolderVideo extends RecyclerView.ViewHolder{

        //Layout - adapter_postagem_video
        private TextView txtViewPostagemVideo;

        public ViewHolderVideo(@NonNull View itemView) {
            super(itemView);

            txtViewPostagemVideo = itemView.findViewById(R.id.txtViewPostagemVideo);
        }
    }
}


