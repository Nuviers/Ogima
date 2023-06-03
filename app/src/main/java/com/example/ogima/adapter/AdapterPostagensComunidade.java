package com.example.ogima.adapter;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.CriarPostagemComunidadeActivity;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PostagemDiffCallback;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdapterPostagensComunidade extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Postagem> listaPostagens;

    private RemoverPostagemListener removerPostagemListener;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;

    private Usuario usuarioCorreto;

    private ValueEventListener valueEventListenerSinalizador;
    private ExoPlayer exoPlayer;

    private Player.Listener listenerExo;

    private boolean saveTeste = false;

    public AdapterPostagensComunidade(List<Postagem> listPostagens, Context c, RemoverPostagemListener removerListener,
                                      RecuperaPosicaoAnterior recuperaPosicaoListener, ExoPlayer exoPlayerTeste) {
        this.context = c;
        this.listaPostagens = listPostagens = new ArrayList<>();
        //this.listaPostagens = listPostagens;
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        //Remoção de elemento
        this.removerPostagemListener = removerListener;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.usuarioCorreto = new Usuario();
        this.exoPlayer = exoPlayerTeste;
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

    public void resumeExoPlayer() {
        //ToastCustomizado.toastCustomizadoCurto("resume exoPlayer 7", context);
        if (exoPlayer != null) {
            exoPlayer.play();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void releaseExoPlayer() {
        if (exoPlayer != null) {
            if (listenerExo != null) {
                exoPlayer.removeListener(listenerExo);
            }
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public void setExoPlayer(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    public void updatePostagemList(List<Postagem> listaPostagensAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaPostagens, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaPostagens.clear();
        listaPostagens.addAll(listaPostagensAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }


    public void updatePostagemListTESTE(List<Postagem> listaAnterior, List<Postagem> listaPostagensAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaAnterior, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaPostagens.clear();
        listaPostagens.addAll(listaPostagensAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }


    public interface RemoverPostagemListener {
        void onComunidadeRemocao(Postagem postagemRemovida);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
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
        //View itemView = inflater.inflate(R.layout.adapter_postagens_comunidade, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        Postagem postagemSelecionada = listaPostagens.get(position);

        if (!payloads.isEmpty()) {

            //ToastCustomizado.toastCustomizadoCurto("Bind payload", context);

            ToastCustomizado.toastCustomizadoCurto("PAYLOAD", context);

            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                    if (bundle.containsKey("edicaoAndamento")) {
                        Boolean newEdicao = bundle.getBoolean("edicaoAndamento");
                        postagemSelecionada.setEdicaoEmAndamento(newEdicao);
                        ToastCustomizado.toastCustomizadoCurto("BOA", context);
                        if (holder instanceof VideoViewHolder) {
                            ((VideoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof PhotoViewHolder) {
                            ((PhotoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof GifViewHolder) {
                            ((GifViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof TextViewHolder) {
                            ((TextViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        }
                    }
                }
            }

        } else {

            FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
                @Override
                public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {

                    if (holder instanceof VideoViewHolder) {
                        exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                                ((VideoViewHolder) holder).imgViewFotoPerfil, ((VideoViewHolder) holder).imgViewFundoPerfil,
                                ((VideoViewHolder) holder).txtViewNomePerfil);
                    } else if (holder instanceof PhotoViewHolder) {
                        exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                                ((PhotoViewHolder) holder).imgViewFotoPerfil, ((PhotoViewHolder) holder).imgViewFundoPerfil,
                                ((PhotoViewHolder) holder).txtViewNomePerfil);

                        ((PhotoViewHolder) holder).exibirPostagemFoto(postagemSelecionada.getUrlPostagem(), epilepsia);

                    } else if (holder instanceof GifViewHolder) {
                        exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                                ((GifViewHolder) holder).imgViewFotoPerfil, ((GifViewHolder) holder).imgViewFundoPerfil,
                                ((GifViewHolder) holder).txtViewNomePerfil);

                        ((GifViewHolder) holder).exibirPostagemGif(postagemSelecionada.getUrlPostagem(), epilepsia);

                    } else if (holder instanceof TextViewHolder) {
                        exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                                ((TextViewHolder) holder).imgViewFotoPerfil, ((TextViewHolder) holder).imgViewFundoPerfil,
                                ((TextViewHolder) holder).txtViewNomePerfil);

                        ((TextViewHolder) holder).exibirPostagemTexto(postagemSelecionada.getDescricaoPostagem());
                    }

                }

                @Override
                public void onError(String mensagem) {

                }
            });

            if (holder instanceof VideoViewHolder) {
                ((VideoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                clickListenersVisitarPerfil(((VideoViewHolder) holder).txtViewNomePerfil,
                        ((VideoViewHolder) holder).imgViewFotoPerfil,
                        ((VideoViewHolder) holder).btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

                clickListenersDetalhesPostagem(((VideoViewHolder) holder).imgBtnLikePostagem,
                        ((VideoViewHolder) holder).imgBtnComentarPostagem, position, postagemSelecionada);

                clickListenerEditarPostagem(((VideoViewHolder) holder).imgBtnEditarPostagem,
                        postagemSelecionada, position);

            } else if (holder instanceof PhotoViewHolder) {
                ((PhotoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                clickListenersVisitarPerfil(((PhotoViewHolder) holder).txtViewNomePerfil,
                        ((PhotoViewHolder) holder).imgViewFotoPerfil,
                        ((PhotoViewHolder) holder).btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

              clickListenersDetalhesPostagem(((PhotoViewHolder) holder).imgBtnLikePostagem,
                      ((PhotoViewHolder) holder).imgBtnComentarPostagem, position, postagemSelecionada);

              ((PhotoViewHolder) holder).imgViewFotoPostagem.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                      irParaDetalhesPostagem(postagemSelecionada);
                  }
              });

              clickListenerEditarPostagem(((PhotoViewHolder) holder).imgBtnEditarPostagem,
                      postagemSelecionada, position);

            } else if (holder instanceof GifViewHolder) {
                ((GifViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                clickListenersVisitarPerfil(((GifViewHolder) holder).txtViewNomePerfil,
                        ((GifViewHolder) holder).imgViewFotoPerfil,
                        ((GifViewHolder) holder).btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

                clickListenersDetalhesPostagem(((GifViewHolder) holder).imgBtnLikePostagem,
                        ((GifViewHolder) holder).imgBtnComentarPostagem, position, postagemSelecionada);

                ((GifViewHolder) holder).imgViewGifPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                    }
                });

                clickListenerEditarPostagem(((GifViewHolder) holder).imgBtnEditarPostagem,
                        postagemSelecionada, position);

            } else if (holder instanceof TextViewHolder) {
                ((TextViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                clickListenersVisitarPerfil(((TextViewHolder) holder).txtViewNomePerfil,
                        ((TextViewHolder) holder).imgViewFotoPerfil,
                        ((TextViewHolder) holder).btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

                clickListenersDetalhesPostagem(((TextViewHolder) holder).imgBtnLikePostagem,
                        ((TextViewHolder) holder).imgBtnComentarPostagem, position, postagemSelecionada);

                ((TextViewHolder) holder).txtViewTextoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                    }
                });

                clickListenerEditarPostagem(((TextViewHolder) holder).imgBtnEditarPostagem,
                        postagemSelecionada, position);
            }

            //ToastCustomizado.toastCustomizadoCurto("Bind padrão", context);

            //Fazer uma interface diferente para video, um botão para
            //que seja possível ir para a postagem do video, clicando
            //sob ele não funciona,pq já é usado para pausar e despausar.

            super.onBindViewHolder(holder, position, payloads);
        }
    }

    private void editarPostagem(ImageButton imgBtnEditar, Postagem postagemAlvo, int position) {

        if (postagemAlvo.getEdicaoEmAndamento() != null && postagemAlvo.getEdicaoEmAndamento()) {
            ToastCustomizado.toastCustomizado("Alguém já está editando essa postagem", context);
        } else {
            ToastCustomizado.toastCustomizado("Livre para edição", context);
            recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);

            Intent intent = new Intent(context, CriarPostagemComunidadeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("idComunidade", postagemAlvo.getIdComunidade());
            intent.putExtra("postagemEdicao", postagemAlvo);
            intent.putExtra("idPostagem", postagemAlvo.getIdPostagem());
            intent.putExtra("tipoPostagem", postagemAlvo.getTipoPostagem());
            intent.putExtra("editarPostagem", true);
            context.startActivity(intent);
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
                //*ToastCustomizado.toastCustomizadoCurto("Removido listener", context);
                exoPlayer.removeListener(listenerExo);
            }
        }

        private void iniciarExoPlayer() {

            //*Attached

            int position = getBindingAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                Postagem newPostagem = listaPostagens.get(position);
                if (newPostagem.getTipoPostagem().equals("video")) {

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

                    //*ToastCustomizado.toastCustomizadoCurto("Attached", context);
                }
            }
        }

        private void pararExoPlayer() {
            //*Detached

            // Verifique se a posição atual é um vídeo
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Postagem newPostagem = listaPostagens.get(position);
                if (newPostagem.getTipoPostagem().equals("video")) {
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

                    //*ToastCustomizado.toastCustomizadoCurto("CLEAN", context);
                }
            }
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
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

            //Componentes do próprio layout
            imgViewFotoPostagem = itemView.findViewById(R.id.imgViewFotoPostagem);
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        private void exibirPostagemFoto(String urlPostagem, boolean epilepsia) {
            if (epilepsia) {
                GlideCustomizado.montarGlideFotoEpilepsia(context, urlPostagem,
                        imgViewFotoPostagem, android.R.color.transparent);
            } else {
                GlideCustomizado.montarGlideFoto(context, urlPostagem,
                        imgViewFotoPostagem, android.R.color.transparent);
            }
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

            //Componentes do prório layout
            imgViewGifPostagem = itemView.findViewById(R.id.imgViewGifPostagem);
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
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
                        .into(imgViewGifPostagem);
            } else {
                Glide.with(context)
                        .asGif()
                        .load(urlPostagem)
                        .encodeQuality(100)
                        .centerInside()
                        .placeholder(android.R.color.transparent)
                        .error(android.R.color.transparent)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imgViewGifPostagem);
            }
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

            //Componentes do próprio layout
            txtViewTextoPostagem = itemView.findViewById(R.id.txtViewTextoPostagem);
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        private void exibirPostagemTexto(String textoPostagem) {
            //Recuperado pelo atributo descricaoPostagem.
            if (textoPostagem != null && !textoPostagem.isEmpty()) {
                txtViewTextoPostagem.setText(textoPostagem);
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).iniciarExoPlayer();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).pararExoPlayer();
        }
    }

    private void exibirCardUserDono(Boolean userAtualEpilepsia, String
            idDonoPostagem, ImageView imgViewFoto, ImageView imgViewFundo, TextView txtViewNome) {

        FirebaseRecuperarUsuario.recuperaUsuario(idDonoPostagem, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                //Dados do dono da postagem
                if (userAtualEpilepsia) {

                    if (usuarioAtual.getMinhaFoto() != null) {
                        ToastCustomizado.toastCustomizadoCurto("Foto", context);
                        GlideCustomizado.montarGlideEpilepsia(context,
                                usuarioAtual.getMinhaFoto(), imgViewFoto, android.R.color.transparent);
                    }

                    if (usuarioAtual.getMeuFundo() != null) {
                        GlideCustomizado.fundoGlideEpilepsia(context, usuarioAtual.getMeuFundo(),
                                imgViewFundo, android.R.color.transparent);
                    }


                } else {

                    if (usuarioAtual.getMinhaFoto() != null) {
                        GlideCustomizado.montarGlide(context,
                                usuarioAtual.getMinhaFoto(), imgViewFoto, android.R.color.transparent);
                    }

                    if (usuarioAtual.getMeuFundo() != null) {
                        GlideCustomizado.fundoGlide(context, usuarioAtual.getMeuFundo(),
                                imgViewFundo, android.R.color.transparent);
                    }
                }

                if (nomeUsuarioAjustado != null && !nomeUsuarioAjustado.isEmpty()) {
                    txtViewNome.setText(nomeUsuarioAjustado);
                }

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void mudarIconeParaEditando(ImageButton imageButton) {
        // Define a cor do tint
        int color = ContextCompat.getColor(context, R.color.corInicio); // Substitua "R.color.my_tint_color" pela cor desejada

// Cria um ColorStateList com a cor desejada
        ColorStateList colorStateList = ColorStateList.valueOf(color);

// Aplica o tint ao ImageButton
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageButton.setBackgroundTintList(colorStateList);
        }
    }

    private void mudarIconeParaPadrao(ImageButton imageButton) {

// Define o valor hexadecimal da cor
        String hexColor = "#DA0369E5"; // Substitua pelo valor hexadecimal desejado

// Converte o valor hexadecimal em um inteiro representando a cor
        int color = Color.parseColor(hexColor);

// Cria um ColorStateList com a cor desejada
        ColorStateList colorStateList = ColorStateList.valueOf(color);

// Aplica o tint ao ImageButton
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageButton.setBackgroundTintList(colorStateList);
        }
    }

    private void atualizarInterfaceEdicao(Postagem postagemSelecionada, ImageButton imgBtnEditarPostagem) {
        if (idUsuarioLogado.equals(postagemSelecionada.getIdDonoPostagem())) {
            if (postagemSelecionada.getEdicaoEmAndamento() != null
                    && postagemSelecionada.getEdicaoEmAndamento()) {
                imgBtnEditarPostagem.setVisibility(View.VISIBLE);

                mudarIconeParaEditando(imgBtnEditarPostagem);
            } else {
                imgBtnEditarPostagem.setVisibility(View.VISIBLE);

                mudarIconeParaPadrao(imgBtnEditarPostagem);
            }
        } else {
            imgBtnEditarPostagem.setVisibility(GONE);
        }
    }

    private void clickListenersVisitarPerfil(TextView txtViewNomePerfil,
                                             ImageView imgViewFotoPerfil, Button btnVisitarPerfilUsuario,
                                             String idDonoPostagem, int position) {

        txtViewNomePerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                visitarPerfilDonoPostagem(idDonoPostagem);
            }
        });

        imgViewFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                visitarPerfilDonoPostagem(idDonoPostagem);
            }
        });

        btnVisitarPerfilUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                visitarPerfilDonoPostagem(idDonoPostagem);
            }
        });

    }

    private void clickListenersDetalhesPostagem(ImageButton imgBtnLikePostagem,
                 ImageButton imgBtnComentarPostagem, int position, Postagem postagemSelecionada){

        imgBtnLikePostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });

        imgBtnComentarPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });
    }

    private void clickListenerEditarPostagem(ImageButton imgBtnEditarPostagem,
                  Postagem postagemSelecionada, int position){

        imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editarPostagem(imgBtnEditarPostagem, postagemSelecionada, position);
            }
        });

    }

    private void visitarPerfilDonoPostagem(String idDonoPostagem) {

        Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        FirebaseRecuperarUsuario.recuperaUsuario(idDonoPostagem, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                intent.putExtra("usuarioSelecionado", usuarioAtual);
                context.startActivity(intent);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void irParaDetalhesPostagem(Postagem postagemSelecionada){
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

    public void updatePostagemListALTERNATIVO(List<Postagem> listaPostagensAtualizada) {
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaPostagens, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaPostagens.clear();
        listaPostagens.addAll(listaPostagensAtualizada);

        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                ToastCustomizado.toastCustomizadoCurto("INSERTED", context);
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                ToastCustomizado.toastCustomizadoCurto("REMOVED", context);
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                ToastCustomizado.toastCustomizadoCurto("MOVED", context);
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
                //Diff com problema não importa a forma do dispatch
                //ele não entende que o dado mudou, verificar em uma classe sem paginação.
                ToastCustomizado.toastCustomizadoCurto("Chamado", context);
            }
        });


        if (listaPostagensAtualizada != null && listaPostagensAtualizada.size() > 0) {
            //ToastCustomizado.toastCustomizadoCurto("Tamanho: " + listaPostagensAtualizada.size(), context);
            for (Postagem postagemExibicao : listaPostagensAtualizada) {
                //ToastCustomizado.toastCustomizadoCurto("Edicao: " + postagemExibicao.getEdicaoEmAndamento(), context);
            }
        }

        /*
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaPostagens, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        //**listaPostagens.clear();
        //**listaPostagens.addAll(listaPostagensAtualizada);
        diffResult.dispatchUpdatesTo(this);

        listaPostagens = listaPostagensAtualizada;

        if (listaPostagensAtualizada != null && listaPostagensAtualizada.size() > 0) {
            //ToastCustomizado.toastCustomizadoCurto("Tamanho: " + listaPostagensAtualizada.size(), context);
            for (Postagem postagemExibicao : listaPostagensAtualizada) {

            }
        }
         */
    }

}