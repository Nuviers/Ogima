package com.example.ogima.adapter;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.CriarPostagemComunidadeActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PostagemDiffCallback;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

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

    private boolean atualizarPrimeiraPostagem = false;
    private boolean usuarioAtualComCargo = false;

    private StorageReference storageRef;

    //Serve para que seja possível recuperar o ArrayList<String> do servidor.
    private GenericTypeIndicator<ArrayList<String>> typeIndicatorArray = new GenericTypeIndicator<ArrayList<String>>() {
    };
    private RemoverListenerRecycler removerListenerRecycler;
    private String idComunidade;

    public AdapterPostagensComunidade(List<Postagem> listPostagens, Context c, RemoverPostagemListener removerListener,
                                      RecuperaPosicaoAnterior recuperaPosicaoListener, ExoPlayer exoPlayerTeste, RemoverListenerRecycler removerListenerRecycler, String idComunidade) {
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
        this.storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        this.removerListenerRecycler = removerListenerRecycler;
        this.idComunidade = idComunidade;
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
        void onComunidadeRemocao(Postagem postagemRemovida, int posicao, ImageButton imgBtnExcluir);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface RemocaoDadosServidor {
        void onConcluido();
    }

    private interface VerificaCargoCallback {
        void onPossuiCargo();

        void onSemCargo();

        void onSemDados();

        void onError();
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
        //View itemView = inflater.inflate(R.layout.adapter_postagens_comunidade, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        Postagem postagemSelecionada = listaPostagens.get(position);

        if (!payloads.isEmpty() && usuarioAtualComCargo) {

            if (position == 0) {
                ToastCustomizado.toastCustomizadoCurto("Bind payload " + position, context);
                atualizarPrimeiraPostagem = true;
            }

            ToastCustomizado.toastCustomizadoCurto("PAYLOAD", context);

            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;

                    if (bundle.containsKey("edicaoAndamento")) {
                        Boolean newEdicao = bundle.getBoolean("edicaoAndamento");
                        postagemSelecionada.setEdicaoEmAndamento(newEdicao);
                        if (holder instanceof VideoViewHolder) {
                            ((VideoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof PhotoViewHolder) {
                            ((PhotoViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof GifViewHolder) {
                            ((GifViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        } else if (holder instanceof TextViewHolder) {
                            ((TextViewHolder) holder).atualizarStatusEdicao(postagemSelecionada);
                        }

                        if (bundle.containsKey("descricaoPostagem")) {
                            String novaDescricao = bundle.getString("descricaoPostagem");
                            postagemSelecionada.setDescricaoPostagem(novaDescricao);

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
            }

        } else {

            configurarCard(idUsuarioLogado, postagemSelecionada, holder);

            if (holder instanceof VideoViewHolder) {

                AdapterPostagensComunidade.VideoViewHolder videoHolder = (AdapterPostagensComunidade.VideoViewHolder) holder;

                executarFuncao(postagemSelecionada, videoHolder.imgBtnEditarPostagem,
                        videoHolder.imgBtnExcluirPostagem, false, null, position, null);

                videoHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executarFuncao(postagemSelecionada, videoHolder.imgBtnEditarPostagem,
                                videoHolder.imgBtnExcluirPostagem, true, "editar", position, null);
                    }
                });

                videoHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //ToastCustomizado.toastCustomizadoCurto("Clicado excluir ", context);

                        executarFuncao(postagemSelecionada, videoHolder.imgBtnEditarPostagem,
                                videoHolder.imgBtnExcluirPostagem, true, "excluir",
                                position, new RemocaoDadosServidor() {
                                    @Override
                                    public void onConcluido() {
                                        //Dados foram removidos do servidor.
                                        videoHolder.pararExoPlayer(this);
                                    }
                                });
                    }
                });

                exibirContadorLikeUI(videoHolder.imgBtnLikePostagem,
                        postagemSelecionada, videoHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemSelecionada, videoHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemSelecionada.getDescricaoPostagem(),
                        videoHolder.txtViewDescPostagem);

                clickListenersVisitarPerfil(videoHolder.txtViewNomePerfil,
                        videoHolder.imgViewFotoPerfil,
                        videoHolder.btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

                clickListenersDetalhesPostagem(videoHolder.imgBtnLikePostagem,
                        videoHolder.imgBtnComentarPostagem, position, postagemSelecionada);

                clickListenerCurtirPostagem(videoHolder.imgBtnLikePostagem,
                        postagemSelecionada, videoHolder.txtViewNrLikesPostagem);

            } else if (holder instanceof PhotoViewHolder) {

                AdapterPostagensComunidade.PhotoViewHolder photoHolder = (AdapterPostagensComunidade.PhotoViewHolder) holder;

                executarFuncao(postagemSelecionada, photoHolder.imgBtnEditarPostagem, photoHolder.imgBtnExcluirPostagem, false, null, position, null);

                photoHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executarFuncao(postagemSelecionada, photoHolder.imgBtnEditarPostagem, photoHolder.imgBtnExcluirPostagem, true, "editar", position, null);
                    }
                });

                photoHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executarFuncao(postagemSelecionada, photoHolder.imgBtnEditarPostagem, photoHolder.imgBtnExcluirPostagem, true, "excluir", position, null);
                    }
                });

                exibirContadorLikeUI(photoHolder.imgBtnLikePostagem,
                        postagemSelecionada, photoHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemSelecionada, photoHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemSelecionada.getDescricaoPostagem(),
                        photoHolder.txtViewDescPostagem);

                clickListenersVisitarPerfil(photoHolder.txtViewNomePerfil,
                        photoHolder.imgViewFotoPerfil,
                        photoHolder.btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

                clickListenersDetalhesPostagem(photoHolder.imgBtnLikePostagem,
                        photoHolder.imgBtnComentarPostagem, position, postagemSelecionada);

                photoHolder.imgViewFotoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                    }
                });

                clickListenerCurtirPostagem(photoHolder.imgBtnLikePostagem,
                        postagemSelecionada, photoHolder.txtViewNrLikesPostagem);

            } else if (holder instanceof GifViewHolder) {

                AdapterPostagensComunidade.GifViewHolder gifHolder = (AdapterPostagensComunidade.GifViewHolder) holder;

                executarFuncao(postagemSelecionada, gifHolder.imgBtnEditarPostagem,
                        gifHolder.imgBtnExcluirPostagem, false,
                        null, position, null);

                gifHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executarFuncao(postagemSelecionada, gifHolder.imgBtnEditarPostagem,
                                gifHolder.imgBtnExcluirPostagem, true,
                                "editar", position, null);
                    }
                });

                gifHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executarFuncao(postagemSelecionada, gifHolder.imgBtnEditarPostagem,
                                gifHolder.imgBtnExcluirPostagem, true,
                                "excluir", position, null);
                    }
                });

                exibirContadorLikeUI(gifHolder.imgBtnLikePostagem,
                        postagemSelecionada, gifHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemSelecionada, gifHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemSelecionada.getDescricaoPostagem(),
                        gifHolder.txtViewDescPostagem);

                clickListenersVisitarPerfil(gifHolder.txtViewNomePerfil,
                        gifHolder.imgViewFotoPerfil,
                        gifHolder.btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

                clickListenersDetalhesPostagem(gifHolder.imgBtnLikePostagem,
                        gifHolder.imgBtnComentarPostagem, position, postagemSelecionada);

                gifHolder.imgViewGifPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                    }
                });

                clickListenerCurtirPostagem(gifHolder.imgBtnLikePostagem,
                        postagemSelecionada, gifHolder.txtViewNrLikesPostagem);

            } else if (holder instanceof TextViewHolder) {

                AdapterPostagensComunidade.TextViewHolder textHolder = (AdapterPostagensComunidade.TextViewHolder) holder;

                executarFuncao(postagemSelecionada, textHolder.imgBtnEditarPostagem,
                        textHolder.imgBtnExcluirPostagem, false,
                        null, position, null);

                textHolder.imgBtnEditarPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executarFuncao(postagemSelecionada, textHolder.imgBtnEditarPostagem,
                                textHolder.imgBtnExcluirPostagem, true,
                                "editar", position, null);
                    }
                });

                textHolder.imgBtnExcluirPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executarFuncao(postagemSelecionada, textHolder.imgBtnEditarPostagem,
                                textHolder.imgBtnExcluirPostagem, true,
                                "excluir", position, null);
                    }
                });

                exibirContadorLikeUI(textHolder.imgBtnLikePostagem,
                        postagemSelecionada, textHolder.txtViewNrLikesPostagem);

                exibirContadorComentario(postagemSelecionada, textHolder.txtViewNrComentariosPostagem);

                exibirDescricao(postagemSelecionada.getDescricaoPostagem(),
                        textHolder.txtViewDescPostagem);

                clickListenersVisitarPerfil(textHolder.txtViewNomePerfil,
                        textHolder.imgViewFotoPerfil,
                        textHolder.btnVisitarPerfilUsuario,
                        postagemSelecionada.getIdDonoPostagem(), position);

                clickListenersDetalhesPostagem(textHolder.imgBtnLikePostagem,
                        textHolder.imgBtnComentarPostagem, position, postagemSelecionada);

                textHolder.txtViewTextoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                        irParaDetalhesPostagem(postagemSelecionada);
                    }
                });

                clickListenerCurtirPostagem(textHolder.imgBtnLikePostagem,
                        postagemSelecionada, textHolder.txtViewNrLikesPostagem);
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

        private void atualizarDescricao(String descricaoAtualizada) {
            if (descricaoAtualizada != null && !descricaoAtualizada.isEmpty()) {
                FormatarContadorUtils.abreviarTexto(descricaoAtualizada, 30);
                txtViewDescPostagem.setVisibility(View.VISIBLE);
                txtViewDescPostagem.setText(descricaoAtualizada);
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
                        removerPostagemListener.onComunidadeRemocao(postagem, position, imgBtnExcluirPostagem);
                    }
                }
            }
        }

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        public void iniciarExoVisivel() {

            if (atualizarPrimeiraPostagem) {
                pararExoPlayer(null);
                atualizarPrimeiraPostagem = false;
            }

            // Inicia o exoPlayer somente se estiver completamente visível,
            //método configurado pelo scrollListener na Activity.
            //ToastCustomizado.toastCustomizadoCurto("VISIBLE", context);
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

        private void atualizarStatusEdicao(Postagem postagemSelecionada) {
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        private void atualizarDescricao(String descricaoAtualizada) {
            if (descricaoAtualizada != null && !descricaoAtualizada.isEmpty()) {
                FormatarContadorUtils.abreviarTexto(descricaoAtualizada, 30);
                txtViewDescPostagem.setVisibility(View.VISIBLE);
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
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        private void atualizarDescricao(String descricaoAtualizada) {
            if (descricaoAtualizada != null && !descricaoAtualizada.isEmpty()) {
                FormatarContadorUtils.abreviarTexto(descricaoAtualizada, 30);
                txtViewDescPostagem.setVisibility(View.VISIBLE);
                txtViewDescPostagem.setText(descricaoAtualizada);
            }
        }

        private void exibirPostagemGif(String urlPostagem, boolean epilepsia) {
            if (epilepsia) {
                GlideCustomizado.montarGlideCenterInsideEpilepsia(context,
                        urlPostagem, imgViewGifPostagem, android.R.color.transparent);
            } else {
                GlideCustomizado.montarGlideCenterInside(context,
                        urlPostagem, imgViewGifPostagem, android.R.color.transparent);
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
            atualizarInterfaceEdicao(postagemSelecionada, imgBtnEditarPostagem);
        }

        private void exibirPostagemTexto(String textoPostagem) {
            //Recuperado pelo atributo descricaoPostagem.
            if (textoPostagem != null && !textoPostagem.isEmpty()) {
                FormatarContadorUtils.abreviarTexto(textoPostagem, 265);
                txtViewTextoPostagem.setText(textoPostagem);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        int position = holder.getBindingAdapterPosition();

        if (holder instanceof VideoViewHolder) {
            if (position != RecyclerView.NO_POSITION
                    && position >= 0 && atualizarPrimeiraPostagem) {
                //Serve para não parar o vídeo que acabou de ter um dado atualizado.
                //ajustar a lógica da activity para levar em conta essa lógica
                //caso seja necessário.
                ToastCustomizado.toastCustomizadoCurto("Posicao " + position, context);
            }
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
                    txtViewNome.setText(FormatarContadorUtils.abreviarTexto(nomeUsuarioAjustado, 20));
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
                                                ImageButton imgBtnComentarPostagem, int position, Postagem postagemSelecionada) {

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
                .child(idUsuarioLogado).child("idPostagensCurtidas");

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

        String idComunidade = postagemSelecionada.getIdComunidade();
        String idPostagem = postagemSelecionada.getIdPostagem();

        DatabaseReference curtirPostagemRef = firebaseRef.child("postagensComunidade")
                .child(idComunidade)
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

    private void exibirContadorLikeUI(ImageButton imgBtnLikePostagem,
                                      Postagem postagemSelecionada, TextView txtViewLikes) {

        int nrLikes = postagemSelecionada.getTotalCurtidasPostagem();

        if (nrLikes >= 0) {
            txtViewLikes.setText(String.valueOf(postagemSelecionada.getTotalCurtidasPostagem()));
        } else {
            txtViewLikes.setText("0");
        }

        DatabaseReference verificaCurtidaPostagemRef = firebaseRef
                .child("usuarios").child(idUsuarioLogado).child("idPostagensCurtidas");

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

    private void exibirDescricao(String descricaoPostagem, TextView txtViewDescPostagem) {
        if (descricaoPostagem != null && !descricaoPostagem.isEmpty()) {
            txtViewDescPostagem.setText(descricaoPostagem);
            txtViewDescPostagem.setVisibility(View.VISIBLE);
        } else {
            txtViewDescPostagem.setVisibility(View.GONE);
        }
    }

    private void exibirContadorComentario(Postagem postagemSelecionada, TextView txtViewComentarios) {
        int nrComentarios = postagemSelecionada.getTotalComentarios();

        if (nrComentarios >= 0) {
            txtViewComentarios.setText(String.valueOf(nrComentarios));
        } else {
            txtViewComentarios.setText("0");
        }
    }

    private void executarFuncao(Postagem postagemRecebida,
                                ImageButton imgBtnEditarPostagem,
                                ImageButton imgBtnExcluirPostagem,
                                boolean realizarAcao, String tipoAcao,
                                int posicao, RemocaoDadosServidor callback) {

        recuperarCargo(idComunidade, new VerificaCargoCallback() {
            @Override
            public void onPossuiCargo() {
                imgBtnEditarPostagem.setVisibility(View.VISIBLE);
                imgBtnExcluirPostagem.setVisibility(View.VISIBLE);

                if (realizarAcao && tipoAcao != null
                        && !tipoAcao.isEmpty()) {

                    if (tipoAcao.equals("editar")) {
                        editarPostagem(imgBtnEditarPostagem, postagemRecebida, posicao);
                    } else if (tipoAcao.equals("excluir")) {
                        excluirPostagem(postagemRecebida, posicao, imgBtnExcluirPostagem, callback);
                    }
                }
                usuarioAtualComCargo = true;
            }

            @Override
            public void onSemCargo() {
                imgBtnEditarPostagem.setVisibility(View.GONE);
                imgBtnExcluirPostagem.setVisibility(View.GONE);
                usuarioAtualComCargo = false;
            }

            @Override
            public void onSemDados() {
                imgBtnEditarPostagem.setVisibility(View.GONE);
                imgBtnExcluirPostagem.setVisibility(View.GONE);
                usuarioAtualComCargo = false;
            }

            @Override
            public void onError() {
                imgBtnEditarPostagem.setVisibility(View.GONE);
                imgBtnExcluirPostagem.setVisibility(View.GONE);
                usuarioAtualComCargo = false;
            }
        });
    }

    private void excluirPostagem(Postagem postagemSelecionada, int posicao, ImageButton imgBtnExcluirPostagem, RemocaoDadosServidor callback) {

        imgBtnExcluirPostagem.setEnabled(false);

        removerListenerRecycler.onRemoverListener();

        String idComunidade = postagemSelecionada.getIdComunidade();
        String idPostagem = postagemSelecionada.getIdPostagem();
        String tipoPostagem = postagemSelecionada.getTipoPostagem();
        String urlPostagem = postagemSelecionada.getUrlPostagem();

        DatabaseReference excluirPostagemRef = firebaseRef.child("postagensComunidade")
                .child(idComunidade).child(idPostagem);

        excluirPostagemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                atualizarContadorPostagens(idComunidade);

                if (urlPostagem != null && !urlPostagem.isEmpty()
                        && tipoPostagem != null) {
                    if (!tipoPostagem.equals("gif") && !tipoPostagem.equals("texto")) {
                        try {
                            storageRef = storageRef.child("postagensComunidade")
                                    .child(tipoPostagem + "s").child(idComunidade).getStorage()
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
            removerPostagemListener.onComunidadeRemocao(postagemSelecionada, posicao, imgBtnExcluirPostagem);
        }
    }

    private void atualizarContadorPostagens(String idComunidade) {

        DatabaseReference contadorPostagensRef = firebaseRef.child("contadorPostagensComunidade")
                .child(idComunidade).child("totalPostagens");

        AtualizarContador atualizarContador = new AtualizarContador();

        atualizarContador.subtrairContador(contadorPostagensRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                contadorPostagensRef.setValue(contadorAtualizado);
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
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

    private void configurarCard(String idUsuarioLogado, Postagem postagemSelecionada, RecyclerView.ViewHolder holder) {
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (holder instanceof VideoViewHolder) {
                    AdapterPostagensComunidade.VideoViewHolder videoHolder = (AdapterPostagensComunidade.VideoViewHolder) holder;
                    exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                            videoHolder.imgViewFotoPerfil, videoHolder.imgViewFundoPerfil,
                            videoHolder.txtViewNomePerfil);
                } else if (holder instanceof PhotoViewHolder) {
                    AdapterPostagensComunidade.PhotoViewHolder photoHolder = (AdapterPostagensComunidade.PhotoViewHolder) holder;
                    exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                            photoHolder.imgViewFotoPerfil, photoHolder.imgViewFundoPerfil,
                            photoHolder.txtViewNomePerfil);

                    photoHolder.exibirPostagemFoto(postagemSelecionada.getUrlPostagem(), epilepsia);

                } else if (holder instanceof GifViewHolder) {
                    AdapterPostagensComunidade.GifViewHolder gifHolder = (AdapterPostagensComunidade.GifViewHolder) holder;

                    exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                            gifHolder.imgViewFotoPerfil, gifHolder.imgViewFundoPerfil,
                            gifHolder.txtViewNomePerfil);

                    gifHolder.exibirPostagemGif(postagemSelecionada.getUrlPostagem(), epilepsia);

                } else if (holder instanceof TextViewHolder) {
                    AdapterPostagensComunidade.TextViewHolder textHolder = (AdapterPostagensComunidade.TextViewHolder) holder;

                    exibirCardUserDono(epilepsia, postagemSelecionada.getIdDonoPostagem(),
                            textHolder.imgViewFotoPerfil, textHolder.imgViewFundoPerfil,
                            textHolder.txtViewNomePerfil);

                    textHolder.exibirPostagemTexto(postagemSelecionada.getDescricaoPostagem());
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void recuperarCargo(String idComunidade, VerificaCargoCallback cargoCallback) {
        //ToastCustomizado.toastCustomizadoCurto("CARGO",context);
        FirebaseRecuperarUsuario.recuperaComunidadeDetalhes(idComunidade, new FirebaseRecuperarUsuario.RecuperaComunidadeDetalhesCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual, String idFundador, ArrayList<String> idsAdms, boolean existemAdms) {
                if (idFundador != null && idFundador.equals(idUsuarioLogado) ||
                        existemAdms && idsAdms.contains(idUsuarioLogado)) {
                    //ToastCustomizado.toastCustomizadoCurto("POSSUI CARGO",context);
                    cargoCallback.onPossuiCargo();
                }
            }

            @Override
            public void semDados(boolean semDados) {
                cargoCallback.onSemCargo();
            }

            @Override
            public void onError(String mensagem) {
                cargoCallback.onError();
            }
        });
    }
}