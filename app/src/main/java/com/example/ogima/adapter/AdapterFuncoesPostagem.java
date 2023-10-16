package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ogima.R;
import com.example.ogima.activity.ConfigurarFotoActivity;
import com.example.ogima.activity.ConfigurePostActivity;
import com.example.ogima.activity.DetalhesPostagemActivity;
import com.example.ogima.activity.EdicaoFotoActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PostagemDiffCallback;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AdapterFuncoesPostagem extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Postagem> listaPostagens;
    private RemoverPostagemListener removerPostagemListener;
    private RecuperaPosicaoAnterior recuperaPosicaoListener;
    private ExoPlayer exoPlayer;
    private Player.Listener listenerExo;
    private StorageReference storageRef;

    //Serve para que seja possível recuperar o ArrayList<String> do servidor.
    private GenericTypeIndicator<ArrayList<String>> typeIndicatorArray = new GenericTypeIndicator<ArrayList<String>>() {
    };
    private RemoverListenerRecycler removerListenerRecycler;
    private boolean gerenciarPostagem = false;
    private boolean usuarioComEpilepsia = true;
    private final static int MARGIN_TOP = 200;
    private final static int MARGIN_TOP_GIF = 320;
    private boolean visitante = false;

    private String idDonoPerfil = null;

    public AdapterFuncoesPostagem(List<Postagem> listPostagens, Context c, RemoverPostagemListener removerListener,
                                  RecuperaPosicaoAnterior recuperaPosicaoListener, ExoPlayer exoPlayerRecebido,
                                  RemoverListenerRecycler removerListenerRecycler, boolean gerenciarPostagem, boolean visitante,
                                  String idDonoPerfil) {
        this.context = c;
        this.listaPostagens = listPostagens = new ArrayList<>();
        //this.listaPostagens = listPostagens;
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        //Remoção de elemento
        this.removerPostagemListener = removerListener;
        this.recuperaPosicaoListener = recuperaPosicaoListener;
        this.exoPlayer = exoPlayerRecebido;
        this.storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        this.removerListenerRecycler = removerListenerRecycler;
        this.gerenciarPostagem = gerenciarPostagem;
        this.visitante = visitante;
        this.idDonoPerfil = idDonoPerfil;

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

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
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
        Postagem postagemAtual = listaPostagens.get(position);

        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;

                    if (bundle.containsKey("descricaoPostagem")) {
                        String novaDescricao = bundle.getString("descricaoPostagem");
                        postagemAtual.setDescricaoPostagem(novaDescricao);
                        ToastCustomizado.toastCustomizadoCurto("BOA", context);
                        if (holder instanceof VideoViewHolder) {
                            ((VideoViewHolder) holder).atualizarDescricao(novaDescricao);
                        } else if (holder instanceof PhotoViewHolder) {
                            ((PhotoViewHolder) holder).atualizarDescricao(novaDescricao);
                        } else if (holder instanceof GifViewHolder) {
                            ((GifViewHolder) holder).atualizarDescricao(novaDescricao);
                        } else if (holder instanceof TextViewHolder) {
                            ((TextViewHolder) holder).exibirPostagemTexto(novaDescricao);
                        }
                    }
                }
            }
        } else {

            if (!gerenciarPostagem) {
                configurarCard(idUsuarioLogado, postagemAtual, holder);
            }

            if (holder instanceof VideoViewHolder) {
                VideoViewHolder videoHolder = (VideoViewHolder) holder;

                if (gerenciarPostagem && idUsuarioLogado.equals(postagemAtual.getIdDonoPostagem())) {
                    videoHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executarFuncao(postagemAtual, videoHolder.imgBtnEditarPostagem,
                                    videoHolder.imgBtnExcluirPostagem, true, "editar", position, "video", null);
                        }
                    });

                    videoHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //ToastCustomizado.toastCustomizadoCurto("Clicado excluir ", context);

                            executarFuncao(postagemAtual, videoHolder.imgBtnEditarPostagem,
                                    videoHolder.imgBtnExcluirPostagem, true, "excluir",
                                    position, "video", new RemocaoDadosServidor() {
                                        @Override
                                        public void onConcluido() {
                                            //Dados foram removidos do servidor.
                                            videoHolder.pararExoPlayer(this);
                                        }
                                    });
                        }
                    });
                } else {
                    clickListenersVisitarPerfil(videoHolder.txtViewNomePerfil,
                            videoHolder.imgViewFotoPerfil,
                            videoHolder.btnVisitarPerfilUsuario,
                            postagemAtual.getIdDonoPostagem(), position);
                }

                exibirContadorLikeUI(videoHolder.imgBtnLikePostagem,
                        postagemAtual, videoHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemAtual, videoHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemAtual.getDescricaoPostagem(), videoHolder.txtViewDescPostagem);

                clickListenersDetalhesPostagem(videoHolder.imgBtnLikePostagem,
                        videoHolder.imgBtnComentarPostagem, position, postagemAtual);

                clickListenerCurtirPostagem(videoHolder.imgBtnLikePostagem,
                        postagemAtual, videoHolder.txtViewNrLikesPostagem);

            } else if (holder instanceof PhotoViewHolder) {
                PhotoViewHolder photoHolder = (PhotoViewHolder) holder;

                if (gerenciarPostagem && idUsuarioLogado.equals(postagemAtual.getIdDonoPostagem())) {
                    photoHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executarFuncao(postagemAtual, photoHolder.imgBtnEditarPostagem,
                                    photoHolder.imgBtnExcluirPostagem, true, "editar", position, "foto", null);
                        }
                    });

                    photoHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executarFuncao(postagemAtual, photoHolder.imgBtnEditarPostagem, photoHolder.imgBtnExcluirPostagem, true, "excluir", position, "foto", null);
                        }
                    });
                } else {
                    clickListenersVisitarPerfil(photoHolder.txtViewNomePerfil,
                            photoHolder.imgViewFotoPerfil,
                            photoHolder.btnVisitarPerfilUsuario,
                            postagemAtual.getIdDonoPostagem(), position);
                }

                photoHolder.exibirPostagemFoto(postagemAtual.getUrlPostagem(), usuarioComEpilepsia);

                exibirContadorLikeUI(photoHolder.imgBtnLikePostagem,
                        postagemAtual, photoHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemAtual, photoHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemAtual.getDescricaoPostagem(), photoHolder.txtViewDescPostagem);

                clickListenersDetalhesPostagem(photoHolder.imgBtnLikePostagem,
                        photoHolder.imgBtnComentarPostagem, position, postagemAtual);

                clickListenerCurtirPostagem(photoHolder.imgBtnLikePostagem,
                        postagemAtual, photoHolder.txtViewNrLikesPostagem);

                photoHolder.imgViewFotoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemAtual);
                    }
                });

            } else if (holder instanceof GifViewHolder) {
                GifViewHolder gifHolder = (GifViewHolder) holder;

                if (gerenciarPostagem && idUsuarioLogado.equals(postagemAtual.getIdDonoPostagem())) {
                    gifHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executarFuncao(postagemAtual, gifHolder.imgBtnEditarPostagem,
                                    gifHolder.imgBtnExcluirPostagem, true, "editar", position, "gif", null);
                        }
                    });

                    gifHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executarFuncao(postagemAtual, gifHolder.imgBtnEditarPostagem,
                                    gifHolder.imgBtnExcluirPostagem, true,
                                    "excluir", position, "gif", null);
                        }
                    });
                } else {
                    clickListenersVisitarPerfil(gifHolder.txtViewNomePerfil,
                            gifHolder.imgViewFotoPerfil,
                            gifHolder.btnVisitarPerfilUsuario,
                            postagemAtual.getIdDonoPostagem(), position);
                }

                gifHolder.exibirPostagemGif(postagemAtual.getUrlPostagem());

                exibirContadorLikeUI(gifHolder.imgBtnLikePostagem,
                        postagemAtual, gifHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemAtual, gifHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemAtual.getDescricaoPostagem(), gifHolder.txtViewDescPostagem);

                clickListenersDetalhesPostagem(gifHolder.imgBtnLikePostagem,
                        gifHolder.imgBtnComentarPostagem, position, postagemAtual);

                clickListenerCurtirPostagem(gifHolder.imgBtnLikePostagem,
                        postagemAtual, gifHolder.txtViewNrLikesPostagem);

                gifHolder.imgViewGifPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemAtual);
                    }
                });

            } else if (holder instanceof TextViewHolder) {
                TextViewHolder textHolder = (TextViewHolder) holder;

                ((TextViewHolder) holder).exibirPostagemTexto(postagemAtual.getDescricaoPostagem());

                if (gerenciarPostagem && idUsuarioLogado.equals(postagemAtual.getIdDonoPostagem())) {
                    textHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executarFuncao(postagemAtual, textHolder.imgBtnEditarPostagem,
                                    textHolder.imgBtnExcluirPostagem, true, "editar", position, "texto", null);
                        }
                    });

                    textHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executarFuncao(postagemAtual, textHolder.imgBtnEditarPostagem,
                                    textHolder.imgBtnExcluirPostagem, true,
                                    "excluir", position, "texto", null);
                        }
                    });
                } else {
                    clickListenersVisitarPerfil(textHolder.txtViewNomePerfil,
                            textHolder.imgViewFotoPerfil,
                            textHolder.btnVisitarPerfilUsuario,
                            postagemAtual.getIdDonoPostagem(), position);
                }

                exibirContadorLikeUI(textHolder.imgBtnLikePostagem,
                        postagemAtual, textHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemAtual, textHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemAtual.getDescricaoPostagem(), textHolder.txtViewDescPostagem);

                clickListenersDetalhesPostagem(textHolder.imgBtnLikePostagem,
                        textHolder.imgBtnComentarPostagem, position, postagemAtual);

                clickListenerCurtirPostagem(textHolder.imgBtnLikePostagem,
                        postagemAtual, textHolder.txtViewNrLikesPostagem);

                textHolder.txtViewTextoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemAtual);
                    }
                });
            }
        }
        super.onBindViewHolder(holder, position, payloads);
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
        private View incCabecalhoPerfil;

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

            incCabecalhoPerfil = itemView.findViewById(R.id.incCabecalhoPerfil);

            frameVideoPostagem = itemView.findViewById(R.id.frameVideoPostagem);

            if (gerenciarPostagem && !visitante) {
                imgBtnEditarPostagem.setVisibility(View.VISIBLE);
                imgBtnExcluirPostagem.setVisibility(View.VISIBLE);
                ocultarCabecalhoPerfil();
            } else {
                imgBtnEditarPostagem.setVisibility(View.GONE);
                imgBtnExcluirPostagem.setVisibility(View.GONE);
                exibirCabecalhoPerfil();
            }
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

        public void iniciarExoPlayer() {

            int position = getBindingAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                Postagem newPostagem = listaPostagens.get(position);

                if (newPostagem.getTipoPostagem().equals("video")) {

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

                    //ToastCustomizado.toastCustomizadoCurto("Attached", context);
                }
            }
        }

        public void pararExoPlayer(RemocaoDadosServidor callback) {
            //*Detached

            //ToastCustomizado.toastCustomizadoCurto("LIMPAR",context);

            // Verifique se a posição atual é um vídeo
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Postagem postagem = listaPostagens.get(position);
                if (postagem.getTipoPostagem().equals("video")) {

                    //*ToastCustomizado.toastCustomizadoCurto("CLEAN", context);

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
                        ToastCustomizado.toastCustomizadoCurto("LIBERO EXO", context);
                        removerPostagemListener.onPostagemRemocao(postagem, position, imgBtnExcluirPostagem);
                    }
                }
            }
        }

        private void atualizarDescricao(String descricaoAtualizada) {
            if (descricaoAtualizada != null) {
                txtViewDescPostagem.setVisibility(View.VISIBLE);
                txtViewDescPostagem.setText(descricaoAtualizada);
            }
        }

        public void iniciarExoVisivel() {
            // Inicia o exoPlayer somente se estiver completamente visível,
            //método configurado pelo scrollListener na Activity.
            //ToastCustomizado.toastCustomizadoCurto("VISIBLE", context);
            iniciarExoPlayer();
        }

        private void ocultarCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frameVideoPostagem.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            frameVideoPostagem.setLayoutParams(params);
        }

        private void exibirCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.VISIBLE);
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

        private View incCabecalhoPerfil;

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
            incCabecalhoPerfil = itemView.findViewById(R.id.incCabecalhoPerfil);

            if (gerenciarPostagem && !visitante) {
                imgBtnEditarPostagem.setVisibility(View.VISIBLE);
                imgBtnExcluirPostagem.setVisibility(View.VISIBLE);
                ocultarCabecalhoPerfil();
            } else {
                imgBtnEditarPostagem.setVisibility(View.GONE);
                imgBtnExcluirPostagem.setVisibility(View.GONE);
                exibirCabecalhoPerfil();
            }
        }

        private void atualizarDescricao(String descricaoAtualizada) {
            if (descricaoAtualizada != null) {
                txtViewDescPostagem.setText(descricaoAtualizada);
            }
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

        private void ocultarCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgViewFotoPostagem.getLayoutParams();
            params.setMargins(0, MARGIN_TOP, 0, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            imgViewFotoPostagem.setLayoutParams(params);
        }

        private void exibirCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.VISIBLE);
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
        private View incCabecalhoPerfil;

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
            incCabecalhoPerfil = itemView.findViewById(R.id.incCabecalhoPerfil);

            if (gerenciarPostagem && !visitante) {
                imgBtnEditarPostagem.setVisibility(View.VISIBLE);
                imgBtnExcluirPostagem.setVisibility(View.VISIBLE);
                ocultarCabecalhoPerfil();
            } else {
                imgBtnEditarPostagem.setVisibility(View.GONE);
                imgBtnExcluirPostagem.setVisibility(View.GONE);
                exibirCabecalhoPerfil();
            }
        }

        private void atualizarDescricao(String descricaoAtualizada) {
            if (descricaoAtualizada != null) {
                txtViewDescPostagem.setText(descricaoAtualizada);
            }
        }

        private void exibirPostagemGif(String urlPostagem) {

            FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
                @Override
                public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                    if (epilepsia) {

                        ToastCustomizado.toastCustomizadoCurto("Epilepsia", context);

                        GlideCustomizado.montarGlideCenterInsideEpilepsia(context,
                                urlPostagem, imgViewGifPostagem, android.R.color.transparent);
                    } else {
                        GlideCustomizado.montarGlideCenterInside(context,
                                urlPostagem, imgViewGifPostagem, android.R.color.transparent);
                    }
                }

                @Override
                public void onSemDados() {

                }

                @Override
                public void onError(String mensagem) {

                }
            });
        }

        private void ocultarCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgViewGifPostagem.getLayoutParams();
            params.setMargins(0, MARGIN_TOP_GIF, 0, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            imgViewGifPostagem.setLayoutParams(params);
        }

        private void exibirCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.VISIBLE);
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
        private View incCabecalhoPerfil;


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
            incCabecalhoPerfil = itemView.findViewById(R.id.incCabecalhoPerfil);

            if (gerenciarPostagem && !visitante) {
                imgBtnEditarPostagem.setVisibility(View.VISIBLE);
                imgBtnExcluirPostagem.setVisibility(View.VISIBLE);
                ocultarCabecalhoPerfil();
            } else {
                imgBtnEditarPostagem.setVisibility(View.GONE);
                imgBtnExcluirPostagem.setVisibility(View.GONE);
                exibirCabecalhoPerfil();
            }
        }

        private void exibirPostagemTexto(String textoPostagem) {
            //Recuperado pelo atributo descricaoPostagem.
            if (textoPostagem != null && !textoPostagem.isEmpty()) {
                FormatarContadorUtils.abreviarTexto(textoPostagem, 265);
                txtViewTextoPostagem.setText(textoPostagem);
            }
        }

        private void ocultarCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtViewTextoPostagem.getLayoutParams();
            params.setMargins(0, MARGIN_TOP, 0, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            txtViewTextoPostagem.setLayoutParams(params);
        }

        private void exibirCabecalhoPerfil() {
            incCabecalhoPerfil.setVisibility(View.VISIBLE);
        }
    }

    private void configurarCard(String idUsuarioLogado, Postagem postagemSelecionada, RecyclerView.ViewHolder holder) {

        if (holder instanceof VideoViewHolder) {
            VideoViewHolder videoHolder = (VideoViewHolder) holder;
            exibirCardUserDono(usuarioComEpilepsia, postagemSelecionada.getIdDonoPostagem(),
                    videoHolder.imgViewFotoPerfil, videoHolder.imgViewFundoPerfil,
                    videoHolder.txtViewNomePerfil);
        } else if (holder instanceof PhotoViewHolder) {
            PhotoViewHolder photoHolder = (PhotoViewHolder) holder;
            exibirCardUserDono(usuarioComEpilepsia, postagemSelecionada.getIdDonoPostagem(),
                    photoHolder.imgViewFotoPerfil, photoHolder.imgViewFundoPerfil,
                    photoHolder.txtViewNomePerfil);
        } else if (holder instanceof GifViewHolder) {
            GifViewHolder gifHolder = (GifViewHolder) holder;
            exibirCardUserDono(usuarioComEpilepsia, postagemSelecionada.getIdDonoPostagem(),
                    gifHolder.imgViewFotoPerfil, gifHolder.imgViewFundoPerfil,
                    gifHolder.txtViewNomePerfil);
        } else if (holder instanceof TextViewHolder) {
            TextViewHolder textHolder = (TextViewHolder) holder;
            exibirCardUserDono(usuarioComEpilepsia, postagemSelecionada.getIdDonoPostagem(),
                    textHolder.imgViewFotoPerfil, textHolder.imgViewFundoPerfil,
                    textHolder.txtViewNomePerfil);
        }
    }

    private void exibirCardUserDono(Boolean userAtualEpilepsia, String
            idDonoPostagem, ImageView imgViewFoto, ImageView imgViewFundo, TextView txtViewNome) {

        FirebaseRecuperarUsuario.recuperaUsuario(idDonoPostagem, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                //Dados do dono da postagem
                if (epilepsia) {

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

    private void executarFuncao(Postagem postagemRecebida,
                                ImageButton imgBtnEditarPostagem,
                                ImageButton imgBtnExcluirPostagem,
                                boolean realizarAcao, String tipoAcao,
                                int posicao, String tipoPostagem,
                                RemocaoDadosServidor callback) {

        if (idUsuarioLogado.equals(postagemRecebida.getIdDonoPostagem())) {
            if (realizarAcao && tipoAcao != null
                    && !tipoAcao.isEmpty()) {

                if (tipoAcao.equals("editar")) {
                    editarPostagem(imgBtnEditarPostagem, postagemRecebida, posicao, tipoPostagem);
                } else if (tipoAcao.equals("excluir")) {
                    excluirPostagem(postagemRecebida, posicao, imgBtnExcluirPostagem, callback);
                }
            }
        }
    }

    private void editarPostagem(ImageButton imgBtnEditar, Postagem postagemAlvo, int position, String tipoPostagem) {
        recuperaPosicaoListener.onPosicaoAnterior(position);
        Intent intent = new Intent(context, ConfigurePostActivity.class);
        intent.putExtra("edicao", true);
        intent.putExtra("postagemEdicao", postagemAlvo);
        intent.putExtra("tipoMidia", tipoPostagem);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void excluirPostagem(Postagem postagemSelecionada, int posicao, ImageButton imgBtnExcluirPostagem, RemocaoDadosServidor callback) {

        imgBtnExcluirPostagem.setEnabled(false);

        removerListenerRecycler.onRemoverListener();

        String idPostagem = postagemSelecionada.getIdPostagem();
        String tipoPostagem = postagemSelecionada.getTipoPostagem();
        String urlPostagem = postagemSelecionada.getUrlPostagem();


       DatabaseReference excluirPostagemRef = firebaseRef.child("postagens")
                .child(postagemSelecionada.getIdDonoPostagem()).child(idPostagem);


        excluirPostagemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                if (urlPostagem != null && !urlPostagem.isEmpty()
                        && tipoPostagem != null) {
                    if (!tipoPostagem.equals("gif") && !tipoPostagem.equals("texto")) {
                        try {
                            storageRef = storageRef.child("postagens")
                                    .child(tipoPostagem + "s").child(postagemSelecionada.getIdDonoPostagem())
                                    .getStorage()
                                    .getReferenceFromUrl(urlPostagem);
                            storageRef.delete();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onConcluido();
            }
        });

        if (callback != null) {
            callback.onConcluido();
        }

        if (!postagemSelecionada.getTipoPostagem().equals("video")) {
            removerPostagemListener.onPostagemRemocao(postagemSelecionada, posicao, imgBtnExcluirPostagem);
        }
    }

    private void exibirDescricao(String descricaoPostagem, TextView txtViewDescPostagem) {
        if (descricaoPostagem != null) {
            txtViewDescPostagem.setText(descricaoPostagem);
            txtViewDescPostagem.setVisibility(View.VISIBLE);
        } else {
            txtViewDescPostagem.setVisibility(View.GONE);
        }
    }

    private void exibirContadorLikeUI(ImageButton imgBtnLikePostagem,
                                      Postagem postagemSelecionada, TextView txtViewLikes) {

        int nrLikes = postagemSelecionada.getTotalCurtidasPostagem();

        if (nrLikes >= 0) {
            txtViewLikes.setText(String.valueOf(postagemSelecionada.getTotalCurtidasPostagem()));
        } else {
            txtViewLikes.setText("0");
        }

        DatabaseReference verificaCurtidaPostagemRef = firebaseRef
                .child("usuarios").child(idUsuarioLogado).child("idLikePosts");

        verificaCurtidaPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    ArrayList<String> idPostagensCurtidas
                            = new ArrayList<>(snapshot.getValue(typeIndicatorArray));

                    if (idPostagensCurtidas != null && idPostagensCurtidas.size() >= 0) {

                        if (idPostagensCurtidas.contains(postagemSelecionada.getIdPostagem())) {
                            Drawable drawablePreenchido = context.getResources().getDrawable(R.drawable.ic_like_postagem_cheio);
                            imgBtnLikePostagem.setImageDrawable(drawablePreenchido);
                        } else {
                            Drawable drawablePadrao = context.getResources().getDrawable(R.drawable.ic_like_postagem_border);
                            imgBtnLikePostagem.setImageDrawable(drawablePadrao);
                        }
                    }
                } else {
                    Drawable drawablePadrao = context.getResources().getDrawable(R.drawable.ic_like_postagem_border);
                    imgBtnLikePostagem.setImageDrawable(drawablePadrao);
                }
                verificaCurtidaPostagemRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exibirContadorComentario(Postagem postagemSelecionada, TextView txtViewComentarios) {
        int nrComentarios = postagemSelecionada.getTotalComentarios();

        if (nrComentarios >= 0) {
            txtViewComentarios.setText(String.valueOf(nrComentarios));
        } else {
            txtViewComentarios.setText("0");
        }
    }

    private void clickListenersVisitarPerfil(TextView txtViewNomePerfil,
                                             ImageView imgViewFotoPerfil, Button btnVisitarPerfilUsuario,
                                             String idDonoPostagem, int position) {

        txtViewNomePerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicaoAnterior(position);
                visitarPerfilDonoPostagem(idDonoPostagem);
            }
        });

        imgViewFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicaoAnterior(position);
                visitarPerfilDonoPostagem(idDonoPostagem);
            }
        });

        btnVisitarPerfilUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicaoAnterior(position);
                visitarPerfilDonoPostagem(idDonoPostagem);
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

    private void clickListenersDetalhesPostagem(ImageButton imgBtnLikePostagem,
                                                ImageButton imgBtnComentarPostagem, int position, Postagem postagemSelecionada) {

        imgBtnLikePostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicaoAnterior(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });

        imgBtnComentarPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaPosicaoListener.onPosicaoAnterior(position);
                irParaDetalhesPostagem(postagemSelecionada);
            }
        });
    }

    private void clickListenerCurtirPostagem(ImageButton imgBtnLikePostagem,
                                             Postagem postagemSelecionada, TextView
                                                     txtViewNrLikes) {

        imgBtnLikePostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curtirPostagem(postagemSelecionada, imgBtnLikePostagem, txtViewNrLikes);
            }
        });
    }

    private void curtirPostagem(Postagem postagemSelecionada,
                                ImageButton imgBtnLikePostagem,
                                TextView txtViewNrLikes) {

        DatabaseReference verificaCurtidasPostagemRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado).child("idLikePosts");

        verificaCurtidasPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    ArrayList<String> idPostagensCurtidas
                            = new ArrayList<>(snapshot.getValue(typeIndicatorArray));

                    if (idPostagensCurtidas != null && idPostagensCurtidas.size() >= 0) {

                        if (idPostagensCurtidas.contains(postagemSelecionada.getIdPostagem())) {
                            ToastCustomizado.toastCustomizadoCurto("Desfazer curtida", context);
                            atualizarContadorLike(postagemSelecionada, verificaCurtidasPostagemRef,
                                    imgBtnLikePostagem, txtViewNrLikes, true, idPostagensCurtidas);
                        } else {
                            atualizarContadorLike(postagemSelecionada, verificaCurtidasPostagemRef,
                                    imgBtnLikePostagem, txtViewNrLikes, false, idPostagensCurtidas);
                        }

                        //ToastCustomizado.toastCustomizadoCurto("Ids " + idPostagensCurtidas.size(), context);
                    }

                } else {

                    ArrayList<String> idPostagensCurtidas
                            = new ArrayList<>();

                    atualizarContadorLike(postagemSelecionada, verificaCurtidasPostagemRef,
                            imgBtnLikePostagem, txtViewNrLikes, false, idPostagensCurtidas);

                }
                verificaCurtidasPostagemRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void atualizarContadorLike(Postagem postagemSelecionada,
                                       DatabaseReference salvarCurtidaNoUsuarioRef,
                                       ImageButton imgBtnLikePostagem,
                                       TextView txtViewNrLikes,
                                       boolean descurtirPostagem,
                                       ArrayList<String> idPostagensCurtidas) {

        String idPostagem = postagemSelecionada.getIdPostagem();

        DatabaseReference curtirPostagemRef = firebaseRef.child("postagens")
                .child(postagemSelecionada.getIdDonoPostagem())
                .child(idPostagem).child("totalCurtidasPostagem");

        AtualizarContador atualizarContador = new AtualizarContador();

        if (descurtirPostagem) {
            atualizarContador.subtrairContador(curtirPostagemRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    curtirPostagemRef.setValue(contadorAtualizado).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            postagemSelecionada.setTotalCurtidasPostagem(contadorAtualizado);

                            idPostagensCurtidas.remove(postagemSelecionada.getIdPostagem());

                            if (idPostagensCurtidas != null && idPostagensCurtidas.size() > 0) {
                                salvarCurtidaNoUsuarioRef.setValue(idPostagensCurtidas);
                            } else {
                                salvarCurtidaNoUsuarioRef.removeValue();
                            }

                            ToastCustomizado.toastCustomizadoCurto("Postagem desfeita com sucesso " + contadorAtualizado, context);
                            exibirContadorLikeUI(imgBtnLikePostagem, postagemSelecionada, txtViewNrLikes);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao descurtir a postagem " + e.getMessage(), context);
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {

                }
            });
        } else {
            atualizarContador.acrescentarContador(curtirPostagemRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    curtirPostagemRef.setValue(contadorAtualizado).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            postagemSelecionada.setTotalCurtidasPostagem(contadorAtualizado);

                            idPostagensCurtidas.add(postagemSelecionada.getIdPostagem());

                            salvarCurtidaNoUsuarioRef.setValue(idPostagensCurtidas);

                            ToastCustomizado.toastCustomizadoCurto("Postagem curtida com sucesso " + contadorAtualizado, context);
                            exibirContadorLikeUI(imgBtnLikePostagem, postagemSelecionada, txtViewNrLikes);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao curtir a postagem " + e.getMessage(), context);
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {

                }
            });
        }
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
}


