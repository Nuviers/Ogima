package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DailyShortDiffCallback;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterDailyShorts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado, emailUsuarioAtual;
    private Context context;
    private List<DailyShort> listaDailys;
    private RemoverDailyListener removerDailyListener;
    private RecuperaPosicaoAnterior recuperaPosicaoListener;
    private ExoPlayer exoPlayer;
    private Player.Listener listenerExo;
    private boolean gerenciarDaily = false;
    private StorageReference storageRef;
    private GenericTypeIndicator<ArrayList<String>> typeIndicatorArray = new GenericTypeIndicator<ArrayList<String>>() {
    };
    private boolean usuarioComEpilepsia = true;

    public AdapterDailyShorts(@NonNull List<DailyShort> listDailys, @NonNull Context c, @NonNull RemoverDailyListener removerListener, @NonNull RecuperaPosicaoAnterior recuperaListener, @NonNull ExoPlayer exoPlayerActivity, boolean gerenciarDaily) {
        this.context = c;
        this.listaDailys = listDailys = new ArrayList<>();
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        this.removerDailyListener = removerListener;
        this.recuperaPosicaoListener = recuperaListener;
        this.exoPlayer = exoPlayerActivity;
        this.gerenciarDaily = gerenciarDaily;
        this.storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (epilepsia != null) {
                    usuarioComEpilepsia = epilepsia;
                } else {
                    usuarioComEpilepsia = true;
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    public interface RemoverDailyListener {
        void onDailyRemocao(@NonNull DailyShort dailyRemovido, int posicao);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public void updateDailyList(@NonNull List<DailyShort> listaDailysAtualizada) {
        DailyShortDiffCallback diffCallback = new DailyShortDiffCallback(listaDailys, listaDailysAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaDailys.clear();
        listaDailys.addAll(listaDailysAtualizada);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {

        String tipoMidia = listaDailys.get(position).getTipoMidia();

        switch (tipoMidia) {
            case "imagem":
                return DailyShort.DAILY_TYPE_PHOTO;
            case "video":
                return DailyShort.DAILY_TYPE_VIDEO;
            case "gif":
                return DailyShort.DAILY_TYPE_GIF;
            case "texto":
                return DailyShort.DAILY_TYPE_TEXT;
        }
        throw new IllegalArgumentException("Invalid post type");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case DailyShort.DAILY_TYPE_PHOTO:
                View photoView = inflater.inflate(R.layout.adapter_daily_imagem, parent, false);
                return new PhotoViewHolder(photoView);
            case DailyShort.DAILY_TYPE_VIDEO:
                View videoView = inflater.inflate(R.layout.adapter_daily_video, parent, false);
                return new VideoViewHolder(videoView);
            case DailyShort.DAILY_TYPE_GIF:
                View gifView = inflater.inflate(R.layout.adapter_daily_gif, parent, false);
                return new GifViewHolder(gifView);
            case DailyShort.DAILY_TYPE_TEXT:
                View textView = inflater.inflate(R.layout.adapter_daily_texto, parent, false);
                return new TextViewHolder(textView);
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DailyShort dailyShort = listaDailys.get(position);

        String tipoMidia = dailyShort.getTipoMidia();
        String urlMidia = dailyShort.getUrlMidia();

        if (holder instanceof PhotoViewHolder) {
            PhotoViewHolder photoHolder = (PhotoViewHolder) holder;
            photoHolder.exibirFoto(urlMidia);
            photoHolder.imgBtnExcluirDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else if (holder instanceof VideoViewHolder) {
            VideoViewHolder videoHolder = (VideoViewHolder) holder;
            videoHolder.imgBtnExcluirDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else if (holder instanceof GifViewHolder) {
            GifViewHolder gifHolder = (GifViewHolder) holder;
            gifHolder.exibirGif(urlMidia);
            gifHolder.imgBtnExcluirDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else if (holder instanceof TextViewHolder) {
            TextViewHolder textHolder = (TextViewHolder) holder;
            textHolder.exibirPostagemTexto(dailyShort.getTextoDaily());
            textHolder.imgBtnExcluirDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaDailys.size();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoDaily;

        //Include - inc_button_excluir
        private ImageButton imgBtnExcluirDaily;

        //Include - inc_views
        private ImageButton imgBtnViews;
        private TextView txtNrViews;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoDaily = itemView.findViewById(R.id.imgViewFotoDaily);
            imgBtnExcluirDaily = itemView.findViewById(R.id.imgBtnExcluirPostagem);
            imgBtnViews = itemView.findViewById(R.id.imgBtnViews);
            txtNrViews = itemView.findViewById(R.id.txtNrViews);
        }

        private void exibirFoto(String urlDaily) {
            if (usuarioComEpilepsia) {
                GlideCustomizado.montarGlideFotoEpilepsia(context, urlDaily,
                        imgViewFotoDaily, android.R.color.transparent);
            } else {
                GlideCustomizado.montarGlideFoto(context, urlDaily,
                        imgViewFotoDaily, android.R.color.transparent);
            }
        }
    }

    public class GifViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewGifDaily;

        //Include - inc_button_excluir
        private ImageButton imgBtnExcluirDaily;

        //Include - inc_views
        private ImageButton imgBtnViews;
        private TextView txtNrViews;

        public GifViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewGifDaily = itemView.findViewById(R.id.imgViewGifDaily);
            imgBtnExcluirDaily = itemView.findViewById(R.id.imgBtnExcluirPostagem);
            imgBtnViews = itemView.findViewById(R.id.imgBtnViews);
            txtNrViews = itemView.findViewById(R.id.txtNrViews);
        }

        private void exibirGif(String urlDaily) {
            if (usuarioComEpilepsia) {
                Glide.with(context)
                        .asBitmap()
                        .load(urlDaily)
                        .encodeQuality(100)
                        .centerInside()
                        .placeholder(android.R.color.transparent)
                        .error(android.R.color.transparent)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imgViewGifDaily);
            } else {
                Glide.with(context)
                        .asGif()
                        .load(urlDaily)
                        .encodeQuality(100)
                        .centerInside()
                        .placeholder(android.R.color.transparent)
                        .error(android.R.color.transparent)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imgViewGifDaily);
            }
        }
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewTextoDaily;

        //Include - inc_button_excluir
        private ImageButton imgBtnExcluirDaily;

        //Include - inc_views
        private ImageButton imgBtnViews;
        private TextView txtNrViews;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);

            txtViewTextoDaily = itemView.findViewById(R.id.txtViewTextoDaily);
            imgBtnExcluirDaily = itemView.findViewById(R.id.imgBtnExcluirPostagem);
            imgBtnViews = itemView.findViewById(R.id.imgBtnViews);
            txtNrViews = itemView.findViewById(R.id.txtNrViews);
        }

        private void exibirPostagemTexto(String textoDaily) {
            if (textoDaily != null && !textoDaily.isEmpty()) {
                txtViewTextoDaily.setText(textoDaily);
            }
        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        //Include - inc_button_excluir
        private ImageButton imgBtnExcluirDaily;

        //Include - inc_views
        private ImageButton imgBtnViews;
        private TextView txtNrViews;

        private FrameLayout frameVideoDaily;
        private StyledPlayerView playerViewInicio;
        private SpinKitView spinProgressBarExo;
        private boolean isControllerVisible = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBtnExcluirDaily = itemView.findViewById(R.id.imgBtnExcluirPostagem);
            imgBtnViews = itemView.findViewById(R.id.imgBtnViews);
            playerViewInicio = itemView.findViewById(R.id.playerViewInicio);
            spinProgressBarExo = itemView.findViewById(R.id.spinProgressBarExo);
            txtNrViews = itemView.findViewById(R.id.txtNrViews);
        }

        private void iniciarExoPlayer() {
            int position = getBindingAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                DailyShort newDaily = listaDailys.get(position);

                if (newDaily.getTipoMidia().equals("video")) {

                    //ToastCustomizado.toastCustomizadoCurto("Attached", context);

                    //Verificação garante que o vídeo não seja montado novamente
                    //se ele já estiver em reprodução.
                    if (exoPlayer != null
                            && playerViewInicio.getPlayer() != null &&
                            exoPlayer.getMediaItemCount() != -1
                            && exoPlayer.getMediaItemCount() > 0) {
                        return;
                    }

                    removerListenerExoPlayer();

                    // Configura o ExoPlayer com a nova fonte de mídia para o vídeo
                    exoPlayer.setMediaItem(MediaItem.fromUri(newDaily.getUrlMidia()));

                    // Vincula o ExoPlayer ao StyledPlayerView
                    playerViewInicio.setPlayer(exoPlayer);

                    // Faz com que o vídeo se repita quando ele acabar
                    exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

                    // Trata do carregamento e da inicialização do vídeo
                    adicionarListenerExoPlayer();

                    // Indica para o exoPlayer que ele está com a view e a mídia configurada.
                    exoPlayer.prepare();

                    //Controla a exibição dos botões do styled.
                    playerViewInicio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isControllerVisible) {
                                playerViewInicio.hideController();
                                playerViewInicio.setUseController(false);
                                isControllerVisible = false;
                            } else {
                                playerViewInicio.setUseController(true);
                                playerViewInicio.showController();
                                isControllerVisible = true;
                            }
                        }
                    });

                    //ToastCustomizado.toastCustomizadoCurto("Attached", context);
                }
            }
        }

        private void pararExoPlayer() {
            //*Detached

            // Verifique se a posição atual é um vídeo
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                DailyShort newDaily = listaDailys.get(position);
                if (newDaily.getTipoMidia().equals("video")) {

                    ToastCustomizado.toastCustomizadoCurto("CLEAN", context);

                    //Remove o listener do exoPlayer
                    removerListenerExoPlayer();
                    //Para a reprodução.
                    exoPlayer.stop();
                    //Limpa a mídia do exoPlayer.
                    exoPlayer.clearMediaItems();
                    //Volta para o início do vídeo.
                    exoPlayer.seekToDefaultPosition();
                    //Diz para o exoPlayer que ele não está pronto.
                    exoPlayer.setPlayWhenReady(false);
                    //Desvincula o exoPlayer anterior.
                    playerViewInicio.setPlayer(null);

                    //Oculta os controladores do styled.
                    playerViewInicio.hideController();
                    playerViewInicio.setUseController(false);
                    isControllerVisible = false;
                }
            }
        }

        public void iniciarOuPararExoPlayer(boolean isVisible) {

            if (isVisible) {
                // Inicia o exoPlayer somente se estiver completamente visível,
                //método configurado pelo scrollListener na Activity.
                //ToastCustomizado.toastCustomizadoCurto("VISIBLE", context);
                iniciarExoPlayer();
            }
        }

        public void adicionarListenerExoPlayer() {
            listenerExo = new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if (playbackState == Player.STATE_READY) {
                        // O vídeo está pronto para reprodução, você pode iniciar a reprodução automática aqui
                        //*ToastCustomizado.toastCustomizadoCurto("READY", context);
                        exoPlayer.setPlayWhenReady(true);
                        spinProgressBarExo.setVisibility(View.GONE);
                    } else if (playbackState == Player.STATE_BUFFERING) {
                        //*ToastCustomizado.toastCustomizadoCurto("BUFFERING", context);
                        // O vídeo está em buffer, você pode mostrar um indicador de carregamento aqui
                        spinProgressBarExo.setVisibility(View.VISIBLE);
                    } else if (playbackState == Player.STATE_ENDED) {
                        //* ToastCustomizado.toastCustomizadoCurto("ENDED", context);
                        // O vídeo chegou ao fim, você pode executar ações após a conclusão do vídeo aqui
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                }
            };

            exoPlayer.addListener(listenerExo);
        }

        public void removerListenerExoPlayer() {
            if (exoPlayer != null && listenerExo != null) {
                //*ToastCustomizado.toastCustomizadoCurto("Removido listener", context);
                exoPlayer.removeListener(listenerExo);
            }
        }

        private void excluirVideo() {

        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).pararExoPlayer();
        }
    }

    public void pauseExoPlayer() {
        if (exoPlayer != null) {
            if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                // Aguardar até que o player esteja pronto para reprodução
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            // O ExoPlayer está pronto para reprodução, então pausar
                            exoPlayer.pause();
                            exoPlayer.setPlayWhenReady(false);
                            exoPlayer.removeListener(this);
                        }
                    }
                });
            } else {
                // O ExoPlayer não está em buffering, então pausar imediatamente
                exoPlayer.pause();
                exoPlayer.setPlayWhenReady(false);
            }
            //ToastCustomizado.toastCustomizadoCurto("Stop exo 7", context);
        }
    }

    public void releaseExoPlayer() {
        if (exoPlayer != null && listenerExo != null) {
            exoPlayer.removeListener(listenerExo);
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void resumeExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.play();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    private void excluirDaily() {

    }
}
