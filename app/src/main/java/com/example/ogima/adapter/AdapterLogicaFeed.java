package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ExoPlayerUtils;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PostagemDiffCallback;
import com.example.ogima.helper.PostagemUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterLogicaFeed extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado, emailUsuarioAtual;
    private Context context;
    private List<Postagem> listaPostagens;
    private boolean statusEpilepsia = true;
    private HashMap<String, Double> interesses;
    private HashMap<String, Object> listaDadosUser;
    private RemoverPostagemListener removerPostagemListener;
    private RecuperaPosicao recuperaPosicaoListener;
    private ExoPlayer exoPlayer;
    private Player.Listener listenerExo;
    private boolean atualizarPrimeiraPostagem = false;
    private StorageReference storageRef;
    private GenericTypeIndicator<ArrayList<String>> typeIndicatorArray = new GenericTypeIndicator<ArrayList<String>>() {
    };
    private RemoverListenerRecycler removerListenerRecycler;
    private ExoPlayerUtils exoPlayerUtils;
    private PostagemUtils postagemUtils;
    private final static double PESO_VIEW = 0.2;
    private final static double PESO_LIKE = 0.5;
    private Set<String> idsPostagens;

    public AdapterLogicaFeed(Context c, List<Postagem> listPostagens, HashMap<String, Double> interesses, HashMap<String, Object> listDadosUser,
                             RemoverPostagemListener removerListener,
                             RecuperaPosicao recuperaPosicaoListener, ExoPlayer exoPlayer, RemoverListenerRecycler removerListenerRecycler) {
        this.context = c;
        this.listaPostagens = listPostagens = new ArrayList<>();
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        this.interesses = interesses;
        this.listaDadosUser = listDadosUser;
        this.removerPostagemListener = removerListener;
        this.recuperaPosicaoListener = recuperaPosicaoListener;
        this.exoPlayer = exoPlayer;
        this.storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        this.removerListenerRecycler = removerListenerRecycler;

        exoPlayerUtils = new ExoPlayerUtils(this.exoPlayer);
        this.postagemUtils = new PostagemUtils(context);
        this.idsPostagens = new HashSet<>();
    }

    @Override
    public int getItemViewType(int position) {
        Postagem tipoPostagem = listaPostagens.get(position);

        if (tipoPostagem.getTipoPostagem().equals("imagem")) {
            return Postagem.POST_TYPE_PHOTO;
        } else if (tipoPostagem.getTipoPostagem().equals("video")) {
            return Postagem.POST_TYPE_VIDEO;
        } else if (tipoPostagem.getTipoPostagem().equals("gif")) {
            return Postagem.POST_TYPE_GIF;
        } else if (tipoPostagem.getTipoPostagem().equals("texto")) {
            return Postagem.POST_TYPE_TEXT;
        }

        throw new IllegalArgumentException("Invalid post type");
    }

    public void updatePostagemList(List<Postagem> listaPostagensAtualizada, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaPostagens, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaPostagens.clear();
        listaPostagens.addAll(listaPostagensAtualizada);

        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
    }

    public interface RemoverPostagemListener {
        void onPostagemRemocao(Postagem postagemRemovida, int posicao, ImageButton imgBtnExcluir);
    }

    public interface RecuperaPosicao {
        void onPosicao(int posicao);
    }

    public interface RemocaoDadosServidor {
        void onConcluido();
    }

    public interface RemoverListenerRecycler {
        void onRemoverListener();

        void onError();
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void pausePlayer() {
        exoPlayerUtils.pauseExoPlayer();
    }

    public void resumePlayer() {
        exoPlayerUtils.resumeExoPlayer();
    }

    public void releasePlayer() {
        exoPlayerUtils.releaseExoPlayer();
    }

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public void setExoPlayer(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case Postagem.POST_TYPE_PHOTO:
                View photoView = inflater.inflate(R.layout.adapter_postagem_foto, parent, false);
                return new PhotoViewHolder(photoView);
            case Postagem.POST_TYPE_GIF:
                View gifView = inflater.inflate(R.layout.adapter_postagem_gif, parent, false);
                return new GifViewHolder(gifView);
            case Postagem.POST_TYPE_VIDEO:
                View videoView = inflater.inflate(R.layout.adapter_postagem_video, parent, false);
                return new VideoViewHolder(videoView);
            case Postagem.POST_TYPE_TEXT:
                View textView = inflater.inflate(R.layout.adapter_postagem_texto, parent, false);
                return new TextViewHolder(textView);
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        Postagem postagemSelecionada = listaPostagens.get(position);
        String idUser = listaPostagens.get(position).getIdDonoPostagem();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);

        if (!payloads.isEmpty()) {
            if (position == 0) {
                atualizarPrimeiraPostagem = true;
            }

            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;

                    if (bundle.containsKey("edicaoAndamento")) {
                        Boolean newEdicao = bundle.getBoolean("edicaoAndamento");
                        postagemSelecionada.setEdicaoEmAndamento(newEdicao);
                        if (holder instanceof VideoViewHolder) {
                            //* ((VideoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof PhotoViewHolder) {
                            //*  ((PhotoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof GifViewHolder) {
                            //* ((GifViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof TextViewHolder) {
                            //* ((TextViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        }
                    }
                }
            }
        } else {

            exibirNrLikes(holder.itemView.findViewById(R.id.imgBtnLikePostagem),
                    postagemSelecionada, holder.itemView.findViewById(R.id.txtViewNrLikesPostagem));

            exibirNrComentario(postagemSelecionada, holder.itemView.findViewById(R.id.txtViewNrComentariosPostagem));

            clickListenerDetalhes(postagemSelecionada, position, holder);

            clickListenerVisitarPerfil(postagemSelecionada, position, holder);

            if (postagemSelecionada.getDescricaoPostagem() != null
                    && !postagemSelecionada.getDescricaoPostagem().isEmpty()) {
                exibirDescricao(postagemSelecionada.getDescricaoPostagem(), holder.itemView.findViewById(R.id.txtViewDescPostagem));
            }

            holder.itemView.findViewById(R.id.imgBtnLikePostagem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    curtirOuDescurtirPostagem(holder.itemView.findViewById(R.id.imgBtnLikePostagem),
                            postagemSelecionada, holder.itemView.findViewById(R.id.txtViewNrLikesPostagem), position);
                }
            });

            if (dadoUser != null) {
                GlideCustomizado.loadUrl(context,
                        dadoUser.getMinhaFoto(), holder.itemView.findViewById(R.id.imgViewFotoCabecalhoPerfil),
                        android.R.color.transparent, GlideCustomizado.CIRCLE_CROP,
                        false, isStatusEpilepsia());
                GlideCustomizado.loadUrl(context,
                        dadoUser.getMeuFundo(), holder.itemView.findViewById(R.id.imgViewFundoCabecalhoPerfil),
                        android.R.color.transparent, GlideCustomizado.CENTER_CROP,
                        false, isStatusEpilepsia());
                String nomeUsuario = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(dadoUser.getNomeUsuario());
                TextView textViewNome = holder.itemView.findViewById(R.id.txtViewNomeCabecalhoPerfil);
                textViewNome.setText(FormatarContadorUtils.abreviarTexto(nomeUsuario, 20));
            }

            if (holder instanceof VideoViewHolder) {
                VideoViewHolder videoHolder = (VideoViewHolder) holder;

            } else if (holder instanceof PhotoViewHolder) {
                PhotoViewHolder photoHolder = (PhotoViewHolder) holder;
                photoHolder.exibirPostagemFoto(postagemSelecionada.getUrlPostagem());
                photoHolder.imgViewFotoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoListener.onPosicao(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                        Log.d("PostagemClicked", postagemSelecionada.getIdPostagem());
                    }
                });
            } else if (holder instanceof GifViewHolder) {
                GifViewHolder gifHolder = (GifViewHolder) holder;
                gifHolder.exibirPostagemGif(postagemSelecionada.getUrlPostagem());

                gifHolder.imgViewGifPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoListener.onPosicao(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                    }
                });

            } else if (holder instanceof TextViewHolder) {
                TextViewHolder textHolder = (TextViewHolder) holder;
                textHolder.exibirPostagemTexto(postagemSelecionada.getDescricaoPostagem());

                textHolder.txtViewTextoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoListener.onPosicao(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                    }
                });
            }
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return listaPostagens.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        //Include buttons_postagem
        private ImageButton imgBtnLikePostagem, imgBtnComentarPostagem,
                imgBtnEditarPostagem, imgBtnViewsPostagem;

        private TextView txtViewVisualizacoesPostagem, txtViewNrLikesPostagem,
                txtViewNrComentariosPostagem;
        //

        //Include inc_cabecalho_perfil
        private ImageView imgViewFundoPerfil, imgViewFotoPerfil;

        private TextView txtViewNomePerfil;

        private Button btnVisitarPerfilUsuario;
        //

        //Include inc_titulo_postagem/ inc_desc_postagem
        private TextView txtViewTitlePostagem, txtViewDescPostagem;
        //

        //Include inc_button_excluir_postagem
        private ImageButton imgBtnExcluirPostagem;
        //

        //Componentes do próprio layout
        private FrameLayout frameVideoPostagem;
        private StyledPlayerView playerViewInicio;
        private SpinKitView spinProgressBarExo;
        //

        private boolean isControllerVisible = false;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBtnLikePostagem = itemView.findViewById(R.id.imgBtnLikePostagem);
            imgBtnComentarPostagem = itemView.findViewById(R.id.imgBtnComentarPostagem);
            imgBtnEditarPostagem = itemView.findViewById(R.id.imgBtnEditarPostagem);
            imgBtnViewsPostagem = itemView.findViewById(R.id.imgBtnViewsPostagem);

            txtViewVisualizacoesPostagem = itemView.findViewById(R.id.txtViewVisualizacoesPostagem);
            txtViewNrLikesPostagem = itemView.findViewById(R.id.txtViewNrLikesPostagem);
            txtViewNrComentariosPostagem = itemView.findViewById(R.id.txtViewNrComentariosPostagem);

            imgViewFundoPerfil = itemView.findViewById(R.id.imgViewFundoCabecalhoPerfil);
            imgViewFotoPerfil = itemView.findViewById(R.id.imgViewFotoCabecalhoPerfil);
            txtViewNomePerfil = itemView.findViewById(R.id.txtViewNomeCabecalhoPerfil);
            btnVisitarPerfilUsuario = itemView.findViewById(R.id.btnVisitarUsuarioCabecalhoPerfil);

            txtViewTitlePostagem = itemView.findViewById(R.id.txtViewTitlePostagem);
            txtViewDescPostagem = itemView.findViewById(R.id.txtViewDescPostagem);

            imgBtnExcluirPostagem = itemView.findViewById(R.id.imgBtnExcluirPostagem);

            //Componentes do próprio layout
            playerViewInicio = itemView.findViewById(R.id.playerViewInicio);
            spinProgressBarExo = itemView.findViewById(R.id.spinProgressBarExo);

        }


        private void adicionarListenerExoPlayer() {
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

        private void removerListenerExoPlayer() {
            if (listenerExo != null) {
                exoPlayer.removeListener(listenerExo);
            }
        }

        public void iniciarExoPlayer() {

            int position = getBindingAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                Postagem newPostagem = listaPostagens.get(position);

                if (newPostagem.getTipoPostagem().equals("video")) {

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
                    exoPlayer.setMediaItem(MediaItem.fromUri(newPostagem.getUrlPostagem()));

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
                }
            }
        }

        public void pararExoPlayer(RemocaoDadosServidor callback) {

            // Verifique se a posição atual é um vídeo
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Postagem postagem = listaPostagens.get(position);
                if (postagem.getTipoPostagem().equals("video")) {

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

                    if (callback != null) {
                        removerPostagemListener.onPostagemRemocao(postagem, position, imgBtnExcluirPostagem);
                    }
                }
            }
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            //atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        public void iniciarExoVisivel() {

            if (atualizarPrimeiraPostagem) {
                pararExoPlayer(null);
                atualizarPrimeiraPostagem = false;
            }

            iniciarExoPlayer();
        }
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        // Referências aos elementos de layout do item de foto

        //Include buttons_postagem
        private ImageButton imgBtnLikePostagem, imgBtnComentarPostagem,
                imgBtnEditarPostagem, imgBtnViewsPostagem;

        private TextView txtViewVisualizacoesPostagem, txtViewNrLikesPostagem,
                txtViewNrComentariosPostagem;
        //

        //Include inc_cabecalho_perfil
        private ImageView imgViewFundoPerfil, imgViewFotoPerfil;

        private TextView txtViewNomePerfil;

        private Button btnVisitarPerfilUsuario;
        //

        //Include inc_titulo_postagem/ inc_desc_postagem
        private TextView txtViewTitlePostagem, txtViewDescPostagem;
        //

        //Include inc_button_excluir_postagem
        private ImageButton imgBtnExcluirPostagem;
        //

        //Componentes do próprio layout
        private ImageView imgViewFotoPostagem;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            // Inicialização das referências aos elementos de layout do item de foto

            imgBtnLikePostagem = itemView.findViewById(R.id.imgBtnLikePostagem);
            imgBtnComentarPostagem = itemView.findViewById(R.id.imgBtnComentarPostagem);
            imgBtnEditarPostagem = itemView.findViewById(R.id.imgBtnEditarPostagem);
            imgBtnViewsPostagem = itemView.findViewById(R.id.imgBtnViewsPostagem);

            txtViewVisualizacoesPostagem = itemView.findViewById(R.id.txtViewVisualizacoesPostagem);
            txtViewNrLikesPostagem = itemView.findViewById(R.id.txtViewNrLikesPostagem);
            txtViewNrComentariosPostagem = itemView.findViewById(R.id.txtViewNrComentariosPostagem);

            imgViewFundoPerfil = itemView.findViewById(R.id.imgViewFundoCabecalhoPerfil);
            imgViewFotoPerfil = itemView.findViewById(R.id.imgViewFotoCabecalhoPerfil);
            txtViewNomePerfil = itemView.findViewById(R.id.txtViewNomeCabecalhoPerfil);
            btnVisitarPerfilUsuario = itemView.findViewById(R.id.btnVisitarUsuarioCabecalhoPerfil);

            txtViewTitlePostagem = itemView.findViewById(R.id.txtViewTitlePostagem);
            txtViewDescPostagem = itemView.findViewById(R.id.txtViewDescPostagem);

            imgBtnExcluirPostagem = itemView.findViewById(R.id.imgBtnExcluirPostagem);

            //Componentes do próprio layout
            imgViewFotoPostagem = itemView.findViewById(R.id.imgViewFotoPostagem);
        }

        private void exibirPostagemFoto(String urlPostagem) {
            GlideCustomizado.loadUrl(context,
                    urlPostagem, imgViewFotoPostagem,
                    android.R.color.transparent,
                    GlideCustomizado.CENTER_CROP,
                    false, isStatusEpilepsia());
        }
    }

    public class GifViewHolder extends RecyclerView.ViewHolder {
        // Referências aos elementos de layout do item de gif

        //Include buttons_postagem
        private ImageButton imgBtnLikePostagem, imgBtnComentarPostagem,
                imgBtnEditarPostagem, imgBtnViewsPostagem;

        private TextView txtViewVisualizacoesPostagem, txtViewNrLikesPostagem,
                txtViewNrComentariosPostagem;
        //

        //Include inc_cabecalho_perfil
        private ImageView imgViewFundoPerfil, imgViewFotoPerfil;

        private TextView txtViewNomePerfil;

        private Button btnVisitarPerfilUsuario;
        //

        //Include inc_titulo_postagem/ inc_desc_postagem
        private TextView txtViewTitlePostagem, txtViewDescPostagem;
        //

        //Include inc_button_excluir_postagem
        private ImageButton imgBtnExcluirPostagem;
        //

        //Componentes do próprio layout
        private ImageView imgViewGifPostagem;

        public GifViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBtnLikePostagem = itemView.findViewById(R.id.imgBtnLikePostagem);
            imgBtnComentarPostagem = itemView.findViewById(R.id.imgBtnComentarPostagem);
            imgBtnEditarPostagem = itemView.findViewById(R.id.imgBtnEditarPostagem);
            imgBtnViewsPostagem = itemView.findViewById(R.id.imgBtnViewsPostagem);

            txtViewVisualizacoesPostagem = itemView.findViewById(R.id.txtViewVisualizacoesPostagem);
            txtViewNrLikesPostagem = itemView.findViewById(R.id.txtViewNrLikesPostagem);
            txtViewNrComentariosPostagem = itemView.findViewById(R.id.txtViewNrComentariosPostagem);

            imgViewFundoPerfil = itemView.findViewById(R.id.imgViewFundoCabecalhoPerfil);
            imgViewFotoPerfil = itemView.findViewById(R.id.imgViewFotoCabecalhoPerfil);
            txtViewNomePerfil = itemView.findViewById(R.id.txtViewNomeCabecalhoPerfil);
            btnVisitarPerfilUsuario = itemView.findViewById(R.id.btnVisitarUsuarioCabecalhoPerfil);

            txtViewTitlePostagem = itemView.findViewById(R.id.txtViewTitlePostagem);
            txtViewDescPostagem = itemView.findViewById(R.id.txtViewDescPostagem);

            imgBtnExcluirPostagem = itemView.findViewById(R.id.imgBtnExcluirPostagem);

            //Componentes do prório layout
            imgViewGifPostagem = itemView.findViewById(R.id.imgViewGifPostagem);
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            //atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        private void exibirPostagemGif(String urlPostagem) {
            GlideCustomizado.loadUrl(context,
                    urlPostagem, imgViewGifPostagem, android.R.color.transparent,
                    GlideCustomizado.CENTER_INSIDE, false,
                    isStatusEpilepsia());
        }
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {
        // Referências aos elementos de layout do item de texto

        //Include buttons_postagem
        private ImageButton imgBtnLikePostagem, imgBtnComentarPostagem,
                imgBtnEditarPostagem, imgBtnViewsPostagem;

        private TextView txtViewVisualizacoesPostagem, txtViewNrLikesPostagem,
                txtViewNrComentariosPostagem;
        //

        //Include inc_cabecalho_perfil
        private ImageView imgViewFundoPerfil, imgViewFotoPerfil;

        private TextView txtViewNomePerfil;

        private Button btnVisitarPerfilUsuario;
        //

        //Include inc_titulo_postagem/ inc_desc_postagem
        private TextView txtViewTitlePostagem, txtViewDescPostagem;
        //

        //Include inc_button_excluir_postagem
        private ImageButton imgBtnExcluirPostagem;
        //

        //Componentes do próprio layout
        private TextView txtViewTextoPostagem;


        public TextViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBtnLikePostagem = itemView.findViewById(R.id.imgBtnLikePostagem);
            imgBtnComentarPostagem = itemView.findViewById(R.id.imgBtnComentarPostagem);
            imgBtnEditarPostagem = itemView.findViewById(R.id.imgBtnEditarPostagem);
            imgBtnViewsPostagem = itemView.findViewById(R.id.imgBtnViewsPostagem);

            txtViewVisualizacoesPostagem = itemView.findViewById(R.id.txtViewVisualizacoesPostagem);
            txtViewNrLikesPostagem = itemView.findViewById(R.id.txtViewNrLikesPostagem);
            txtViewNrComentariosPostagem = itemView.findViewById(R.id.txtViewNrComentariosPostagem);

            imgViewFundoPerfil = itemView.findViewById(R.id.imgViewFundoCabecalhoPerfil);
            imgViewFotoPerfil = itemView.findViewById(R.id.imgViewFotoCabecalhoPerfil);
            txtViewNomePerfil = itemView.findViewById(R.id.txtViewNomeCabecalhoPerfil);
            btnVisitarPerfilUsuario = itemView.findViewById(R.id.btnVisitarUsuarioCabecalhoPerfil);

            txtViewTitlePostagem = itemView.findViewById(R.id.txtViewTitlePostagem);
            txtViewDescPostagem = itemView.findViewById(R.id.txtViewDescPostagem);

            imgBtnExcluirPostagem = itemView.findViewById(R.id.imgBtnExcluirPostagem);

            //Componentes do próprio layout
            txtViewTextoPostagem = itemView.findViewById(R.id.txtViewTextoPostagem);
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            //atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        private void exibirPostagemTexto(String textoPostagem) {
            //Recuperado pelo atributo descricaoPostagem.
            if (textoPostagem != null && !textoPostagem.isEmpty()) {
                txtViewTextoPostagem.setText(textoPostagem);
            }
        }
    }

    private void exibirNrLikes(ImageButton imgBtnLikePostagem,
                               Postagem postagemSelecionada, TextView txtViewLikes) {
        if (postagemSelecionada != null
                && postagemSelecionada.getTotalCurtidasPostagem() != -1
                && postagemSelecionada.getTotalCurtidasPostagem() > 0) {
            txtViewLikes.setText(String.valueOf(postagemSelecionada.getTotalCurtidasPostagem()));
        } else {
            txtViewLikes.setText("0");
        }

        postagemUtils.VerificaCurtida(idUsuarioLogado, postagemSelecionada.getIdPostagem(), new PostagemUtils.VerificaCurtidaCallback() {
            @Override
            public void onJaCurtido() {
                Drawable drawablePreenchido = context.getResources().getDrawable(R.drawable.ic_like_postagem_cheio);
                imgBtnLikePostagem.setImageDrawable(drawablePreenchido);
            }

            @Override
            public void onNaoCurtido() {
                Drawable drawablePadrao = context.getResources().getDrawable(R.drawable.ic_like_postagem_border);
                imgBtnLikePostagem.setImageDrawable(drawablePadrao);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void exibirNrComentario(Postagem postagemSelecionada, TextView txtViewComentarios) {
        int nrComentarios = postagemSelecionada.getTotalComentarios();

        if (nrComentarios >= 0) {
            txtViewComentarios.setText(String.valueOf(nrComentarios));
        } else {
            txtViewComentarios.setText("0");
        }
    }

    private void exibirDescricao(String descricaoPostagem, TextView txtViewDescPostagem) {
        if (descricaoPostagem != null && !descricaoPostagem.isEmpty()) {
            txtViewDescPostagem.setText(descricaoPostagem);
            txtViewDescPostagem.setVisibility(View.VISIBLE);
        } else {
            txtViewDescPostagem.setVisibility(View.GONE);
        }
    }

    private void curtirOuDescurtirPostagem(ImageButton imgBtnLikePostagem,
                                           Postagem postagemSelecionada, TextView
                                                   txtViewNrLikes, int position) {
        postagemUtils.VerificaCurtida(idUsuarioLogado,
                postagemSelecionada.getIdPostagem(), new PostagemUtils.VerificaCurtidaCallback() {
                    @Override
                    public void onJaCurtido() {
                        //Descurtir
                        Drawable drawablePadrao = context.getResources().getDrawable(R.drawable.ic_like_postagem_border);
                        imgBtnLikePostagem.setImageDrawable(drawablePadrao);
                        int nrLike = postagemSelecionada.getTotalCurtidasPostagem() - 1;
                        if (nrLike < 0) {
                            nrLike = 0;
                        }
                        txtViewNrLikes.setText(String.valueOf(nrLike));
                        postagemSelecionada.setTotalCurtidasPostagem(nrLike);
                        //Descurtir
                        postagemUtils.descurtirPostagem(idUsuarioLogado,
                                postagemSelecionada, new PostagemUtils.DescurtirPostagemCallback() {
                                    @Override
                                    public void onDescurtido(int nrLikeAtual) {
                                        diminuirPesoLike(postagemSelecionada, position);
                                    }

                                    @Override
                                    public void onError(String message) {

                                    }
                                });
                    }

                    @Override
                    public void onNaoCurtido() {
                        //Curtir
                        Drawable drawablePreenchido = context.getResources().getDrawable(R.drawable.ic_like_postagem_cheio);
                        imgBtnLikePostagem.setImageDrawable(drawablePreenchido);
                        int nrLike = postagemSelecionada.getTotalCurtidasPostagem() + 1;
                        if (nrLike <= 0) {
                            nrLike = 1;
                        }
                        txtViewNrLikes.setText(String.valueOf(nrLike));
                        postagemSelecionada.setTotalCurtidasPostagem(nrLike);
                        postagemUtils.CurtirPostagem(idUsuarioLogado, postagemSelecionada,
                                new PostagemUtils.CurtirPostagemCallback() {
                                    @Override
                                    public void onCurtido(int nrLikeAtual) {
                                        atualizarPesos(position, "like");
                                    }

                                    @Override
                                    public void onError(String message) {

                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
    }

    private void clickListenerDetalhes(Postagem postagemSelecionada, int position, RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.findViewById(R.id.imgBtnComentarPostagem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicao(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });
        viewHolder.itemView.findViewById(R.id.txtViewNrLikesPostagem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicao(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });
        viewHolder.itemView.findViewById(R.id.txtViewNrComentariosPostagem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicao(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });
        viewHolder.itemView.findViewById(R.id.txtViewDescPostagem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicao(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });
    }

    private void clickListenerVisitarPerfil(Postagem postagemSelecionada, int position, RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.findViewById(R.id.incCabecalhoPerfil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicao(position);
                visitarPerfilDonoPostagem(postagemSelecionada.getIdDonoPostagem());
            }
        });
    }

    private void visitarPerfilDonoPostagem(String idDonoPostagem) {
        FirebaseRecuperarUsuario.recuperaUsuario(idDonoPostagem, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                        usuarioAtual.getIdUsuario());
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void irParaDetalhesPostagem(Postagem postagemSelecionada) {
        Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("titulo", postagemSelecionada.getTituloPostagem());
        intent.putExtra("descricao", postagemSelecionada.getDescricaoPostagem());
        intent.putExtra("foto", postagemSelecionada.getUrlPostagem());
        intent.putExtra("idPostagem", postagemSelecionada.getIdPostagem());
        intent.putExtra("dataPostagem", postagemSelecionada.getDataPostagem());
        intent.putExtra("donoPostagem", postagemSelecionada.getIdDonoPostagem());
        intent.putExtra("publicoPostagem", postagemSelecionada.getPublicoPostagem());
        intent.putExtra("idRecebido", postagemSelecionada.getIdDonoPostagem());
        intent.putExtra("tipoPublicacao", "tipoPublicacao");
        intent.putExtra("tipoPostagem", postagemSelecionada.getTipoPostagem());
        context.startActivity(intent);
    }

    public void atualizarPesos(int position, String tipoInteracao) {

        if (position == -1 || listaPostagens == null || position >= listaPostagens.size()) return;

        int posicaoReal = position;

        if (position > 0 && position <= listaPostagens.size()) {
            posicaoReal = position - 1;
        } else if (position == 0) {
            return;
        }

        if (posicaoReal < 0 || posicaoReal >= listaPostagens.size()) return;

        Postagem postagem = listaPostagens.get(posicaoReal);

        if (tipoInteracao.equals("view")) {
            if (idsPostagens != null && idsPostagens.contains(postagem.getIdPostagem())) {
                return;
            }
            idsPostagens.add(postagem.getIdPostagem());
            salvarIdPostVisualizada(position); // Seu método de salvar histórico
        }

        if (postagem.getListaInteressesPostagem() == null || postagem.getListaInteressesPostagem().isEmpty()) {
            return;
        }

        DatabaseReference salvarRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado).child("listaInteresses");

        FirebaseRecuperarUsuario.recuperarInteresses(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperarInteressesCallback() {
            @Override
            public void onRecuperado(HashMap<String, Double> listaInteressesUsuario) {
                boolean houveAlteracao = false;

                for (String interessePost : postagem.getListaInteressesPostagem()) {
                    double pesoAdicional = recuperarPesoCorreto(tipoInteracao);

                    if (listaInteressesUsuario.containsKey(interessePost)) {
                        double pesoAtual = listaInteressesUsuario.get(interessePost);
                        double novoPeso = Math.round((pesoAtual + pesoAdicional) * 100.0) / 100.0;
                        listaInteressesUsuario.put(interessePost, novoPeso);
                    } else {
                        listaInteressesUsuario.put(interessePost, pesoAdicional);
                    }
                    houveAlteracao = true;
                }

                if (houveAlteracao) {
                    salvarRef.setValue(listaInteressesUsuario);
                }
            }

            @Override
            public void onSemInteresses() {

                HashMap<String, Double> novaLista = new HashMap<>();

                for (String interessePost : postagem.getListaInteressesPostagem()) {
                    double pesoInicial = recuperarPesoCorreto(tipoInteracao);
                    novaLista.put(interessePost, pesoInicial);
                }

                if (!novaLista.isEmpty()) {
                    salvarRef.setValue(novaLista);
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void diminuirPesoLike(Postagem postagem, int position) {
        if (postagem == null || postagem.getListaInteressesPostagem() == null || postagem.getListaInteressesPostagem().isEmpty()) {
            return;
        }

        DatabaseReference salvarRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado).child("listaInteresses");

        FirebaseRecuperarUsuario.recuperarInteresses(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperarInteressesCallback() {
            @Override
            public void onRecuperado(HashMap<String, Double> listaInteressesUsuario) {
                boolean houveAlteracao = false;

                for (String interesse : postagem.getListaInteressesPostagem()) {
                    if (listaInteressesUsuario.containsKey(interesse)) {
                        double pesoAtual = listaInteressesUsuario.get(interesse);
                        double novoPeso = pesoAtual - PESO_LIKE;

                        if (novoPeso < 0) novoPeso = 0;

                        novoPeso = Math.round(novoPeso * 100.0) / 100.0;
                        listaInteressesUsuario.put(interesse, novoPeso);
                        houveAlteracao = true;
                    }
                }

                if (houveAlteracao) {
                    salvarRef.setValue(listaInteressesUsuario);
                }
            }

            @Override
            public void onSemInteresses() {

            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private double recuperarPesoCorreto(String tipoInteracao) {
        switch (tipoInteracao) {
            case "view":
                return PESO_VIEW;
            case "like":
                return PESO_LIKE;
        }
        return 0;
    }

    public void salvarIdPostVisualizada(int position) {
        //Salva o id da postagem no array de postagens já visualizadas,
        //isso garante que nas próximas vezes não exiba uma postagem que já foi vista.
        DatabaseReference salvarViewRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado).child("listaPostagensVisualizadas");
        if (position != -1 && position >= listaPostagens.size()) {
            int posicao = position - 1;
            Postagem postagemAtual = listaPostagens.get(posicao);
            FirebaseRecuperarUsuario.recuperarPostagensVisualizadas(idUsuarioLogado, new FirebaseRecuperarUsuario.PostagensVisualizadasCallback() {
                @Override
                public void onPostagens(ArrayList<String> postagensVisualizadas) {
                    if (!postagensVisualizadas.contains(postagemAtual.getIdPostagem())) {
                        postagensVisualizadas.add(postagemAtual.getIdPostagem());
                        salvarViewRef.setValue(postagensVisualizadas);
                    }
                }

                @Override
                public void onSemPostagens() {
                    ArrayList<String> idsPostagensVisualizadas = new ArrayList<>();
                    idsPostagensVisualizadas.add(postagemAtual.getIdPostagem());
                    salvarViewRef.setValue(idsPostagensVisualizadas);
                }

                @Override
                public void onError(String message) {

                }
            });
        } else if (position != -1) {
            Postagem postagemAtual = listaPostagens.get(position);
            FirebaseRecuperarUsuario.recuperarPostagensVisualizadas(idUsuarioLogado, new FirebaseRecuperarUsuario.PostagensVisualizadasCallback() {
                @Override
                public void onPostagens(ArrayList<String> postagensVisualizadas) {
                    if (!postagensVisualizadas.contains(postagemAtual.getIdPostagem())) {
                        postagensVisualizadas.add(postagemAtual.getIdPostagem());
                        salvarViewRef.setValue(postagensVisualizadas);
                    }
                }

                @Override
                public void onSemPostagens() {
                    ArrayList<String> idsPostagensVisualizadas = new ArrayList<>();
                    idsPostagensVisualizadas.add(postagemAtual.getIdPostagem());
                    salvarViewRef.setValue(idsPostagensVisualizadas);
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }
}
