package com.example.ogima.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterDailyShortsSelecao extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Uri> listaDailyUri;

    private AdapterDailyShortsSelecao.RemoverDailyListener removerDailyListener;

    private ValueEventListener valueEventListenerSinalizador;
    private StorageReference storageRef;

    //Serve para que seja possível recuperar o ArrayList<String> do servidor.
    private GenericTypeIndicator<ArrayList<String>> typeIndicatorArray = new GenericTypeIndicator<ArrayList<String>>() {
    };

    public AdapterDailyShortsSelecao(Context context, List<Uri> listaDaily,
                                     AdapterDailyShortsSelecao.RemoverDailyListener removerListener) {
        this.context = context;
        this.listaDailyUri = listaDaily;
        this.removerDailyListener = removerListener;
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        this.storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        ToastCustomizado.toastCustomizadoCurto("Adapter",context);
    }

    public interface RemoverDailyListener {
        void onDailyRemocao(Uri uriRemovido, int posicao);
    }

    @Override
    public int getItemViewType(int position) {
        Uri tipoDaily = listaDailyUri.get(position);

        String mimeType = getMimeType(context, tipoDaily);

        if (mimeType != null) {
            if (mimeType.equals("image/gif")) {
                // Tipo de mídia é uma imagem
                return DailyShort.DAILY_TYPE_GIF;
            } else if (mimeType.startsWith("image/")) {
                // Tipo de mídia é um GIF
                return DailyShort.DAILY_TYPE_PHOTO;
            }
        }

        throw new IllegalArgumentException("Invalid daily type");
    }

    @Override
    public int getItemCount() {
        return listaDailyUri.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case DailyShort.DAILY_TYPE_PHOTO:
                View photoView = inflater.inflate(R.layout.adapter_exibicao_daily_imagem, parent, false);
                return new AdapterDailyShortsSelecao.PhotoViewHolder(photoView);
            case DailyShort.DAILY_TYPE_GIF:
                View gifView = inflater.inflate(R.layout.adapter_exibicao_daily_imagem, parent, false);
                return new AdapterDailyShortsSelecao.GifViewHolder(gifView);
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }
        //View itemView = inflater.inflate(R.layout.adapter_postagens_comunidade, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        Uri dailyShort = listaDailyUri.get(position);

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {

                switch (viewType) {
                    case DailyShort.DAILY_TYPE_PHOTO:
                        PhotoViewHolder photoHolder = (PhotoViewHolder) holder;
                        photoHolder.exibirPostagemFoto(dailyShort.toString(), epilepsia);
                        break;
                    case DailyShort.DAILY_TYPE_GIF:
                        GifViewHolder gifHolder = (GifViewHolder) holder;
                        gifHolder.exibirPostagemGif(dailyShort.toString(), epilepsia);
                        break;
                    default:
                        throw new IllegalArgumentException("ViewType desconhecido: " + viewType);
                }

            }

            @Override
            public void onError(String mensagem) {

            }
        });
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

    public class GifViewHolder extends RecyclerView.ViewHolder {
        // Referências aos elementos de layout do item de gif
        private ImageView imgViewDailyGif;

        public GifViewHolder(@NonNull View itemView) {
            super(itemView);
            //Componentes do prório layout
            imgViewDailyGif = itemView.findViewById(R.id.imgViewPreviewDaily);
        }

        private void exibirPostagemGif(String urlPostagem, boolean epilepsia) {
            if (epilepsia) {
                Glide.with(context)
                        .asBitmap()
                        .load(urlPostagem)
                        .encodeQuality(100)
                        .centerInside()
                        .placeholder(android.R.color.transparent)
                        .error(android.R.color.transparent)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imgViewDailyGif);
            } else {
                Glide.with(context)
                        .asGif()
                        .load(urlPostagem)
                        .encodeQuality(100)
                        .centerInside()
                        .placeholder(android.R.color.transparent)
                        .error(android.R.color.transparent)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imgViewDailyGif);
            }
        }
    }

    public String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
            mimeType = contentResolver.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
