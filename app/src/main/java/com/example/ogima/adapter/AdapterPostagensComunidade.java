package com.example.ogima.adapter;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import com.example.ogima.model.ExoPlayerItem;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class AdapterPostagensComunidade extends RecyclerView.Adapter<AdapterPostagensComunidade.ItemViewHolder> {

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
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaPostagens, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaPostagens.clear();
        listaPostagens.addAll(listaPostagensAtualizada);

        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                ToastCustomizado.toastCustomizadoCurto("INSERTED",context);
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                ToastCustomizado.toastCustomizadoCurto("REMOVED",context);
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                ToastCustomizado.toastCustomizadoCurto("MOVED",context);
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
                //Diff com problema não importa a forma do dispatch
                //ele não entende que o dado mudou, verificar em uma classe sem paginação.
                ToastCustomizado.toastCustomizadoCurto("Chamado",context);
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

    public interface RemoverPostagemListener {
        void onComunidadeRemocao(Postagem postagemRemovida);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_postagens_comunidade, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        Postagem postagemSelecionada = listaPostagens.get(position);

        if (!payloads.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("PAYLOAD", context);

            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                    if (bundle.containsKey("edicaoAndamento")) {
                        Boolean newEdicao = bundle.getBoolean("edicaoAndamento");
                        postagemSelecionada.setEdicaoEmAndamento(newEdicao);
                        ToastCustomizado.toastCustomizadoCurto("BOA", context);
                        holder.verificaEdicao(postagemSelecionada);
                    }
                }
            }

        } else {

            if (postagemSelecionada.getTipoPostagem().equals("imagem")) {

                holder.imgViewGifPostagemInicio.setVisibility(GONE);
                holder.playerViewInicio.setVisibility(GONE);
                holder.btnExibirVideo.setVisibility(GONE);
                holder.imgViewFotoPostagemInicio.setVisibility(View.VISIBLE);
                holder.linearTeste1.setBackgroundColor(Color.parseColor("#000000"));

            } else if (postagemSelecionada.getTipoPostagem().equals("gif")) {

                holder.imgViewFotoPostagemInicio.setVisibility(GONE);
                holder.playerViewInicio.setVisibility(GONE);
                holder.btnExibirVideo.setVisibility(GONE);
                holder.imgViewGifPostagemInicio.setVisibility(View.VISIBLE);
                holder.linearTeste1.setBackgroundColor(Color.parseColor("#ffffff"));

            } else if (postagemSelecionada.getTipoPostagem().equals("video")) {
                holder.imgViewGifPostagemInicio.setVisibility(GONE);
                holder.imgViewFotoPostagemInicio.setVisibility(GONE);
                holder.playerViewInicio.setVisibility(View.VISIBLE);
                holder.btnExibirVideo.setVisibility(View.VISIBLE);
            }

            FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
                @Override
                public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                    if (epilepsia) {
                        if (postagemSelecionada.getTipoPostagem().equals("gif")) {
                            Glide.with(context)
                                    .asBitmap()
                                    .load(postagemSelecionada.getUrlPostagem())
                                    .encodeQuality(100)
                                    .centerInside()
                                    .placeholder(android.R.color.transparent)
                                    .error(android.R.color.transparent)
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .into(holder.imgViewGifPostagemInicio);
                        } else if (postagemSelecionada.getTipoPostagem().equals("imagem")) {
                            GlideCustomizado.montarGlideFotoEpilepsia(context, postagemSelecionada.getUrlPostagem(),
                                    holder.imgViewFotoPostagemInicio, android.R.color.transparent);
                        }
                        exibirCardUserDono(true, postagemSelecionada.getIdDonoPostagem(), holder.imgViewDonoFotoPostagemInicio,
                                holder.imgViewFundoUserInicio, holder.txtViewNomeDonoPostagemInicio);
                    } else {
                        if (postagemSelecionada.getTipoPostagem().equals("gif")) {
                            Glide.with(context)
                                    .asGif()
                                    .load(postagemSelecionada.getUrlPostagem())
                                    .encodeQuality(100)
                                    .centerInside()
                                    .placeholder(android.R.color.transparent)
                                    .error(android.R.color.transparent)
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .into(holder.imgViewGifPostagemInicio);
                        } else if (postagemSelecionada.getTipoPostagem().equals("imagem")) {
                            GlideCustomizado.montarGlideFoto(context, postagemSelecionada.getUrlPostagem(),
                                    holder.imgViewFotoPostagemInicio, android.R.color.transparent);
                        }
                        exibirCardUserDono(false, postagemSelecionada.getIdDonoPostagem(), holder.imgViewDonoFotoPostagemInicio,
                                holder.imgViewFundoUserInicio, holder.txtViewNomeDonoPostagemInicio);
                    }
                }

                @Override
                public void onError(String mensagem) {

                }
            });

            holder.verificaEdicao(postagemSelecionada);

            holder.txtViewNomeDonoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);

                    DatabaseReference recuperarUserCorretoRef = firebaseRef
                            .child("usuarios").child(postagemSelecionada.getIdDonoPostagem());
                    recuperarUserCorretoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                usuarioCorreto = snapshot.getValue(Usuario.class);
                                intent.putExtra("usuarioSelecionado", usuarioCorreto);
                                context.startActivity(intent);
                            }
                            recuperarUserCorretoRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            holder.imgViewDonoFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);

                    DatabaseReference recuperarUserCorretoRef = firebaseRef
                            .child("usuarios").child(postagemSelecionada.getIdDonoPostagem());
                    recuperarUserCorretoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                usuarioCorreto = snapshot.getValue(Usuario.class);
                                intent.putExtra("usuarioSelecionado", usuarioCorreto);
                                context.startActivity(intent);
                            }
                            recuperarUserCorretoRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            holder.btnVisitarPerfilFotoPostagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                    DatabaseReference recuperarUserCorretoRef = firebaseRef
                            .child("usuarios").child(postagemSelecionada.getIdDonoPostagem());
                    recuperarUserCorretoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                usuarioCorreto = snapshot.getValue(Usuario.class);
                                intent.putExtra("usuarioSelecionado", usuarioCorreto);
                                //Não sei se precissa desse putExtra tipoPublicacao
                                context.startActivity(intent);
                                //ToastCustomizado.toastCustomizadoCurto("Nome ANTES " + usuarioCorreto.getNomeUsuario(), context);
                            }
                            recuperarUserCorretoRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            //Eventos de botões para ir na postagem
            holder.imgViewFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            });

            holder.btnExibirVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            });

            holder.imgViewGifPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            });

            //Fazer uma interface diferente para video, um botão para
            //que seja possível ir para a postagem do video, clicando
            //sob ele não funciona,pq já é usado para pausar e despausar.

            holder.txtViewContadorViewsFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            });

            holder.imgButtonComentariosFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            });

            holder.txtViewContadorComentarioFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            });

            holder.txtViewContadorLikesFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            });

            holder.buttonRemoverTeste.setVisibility(View.VISIBLE);

            holder.buttonRemoverTeste.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //*removerPostagemListener.onComunidadeRemocao(postagemSelecionada);
                    if (!saveTeste) {
                        Postagem postagemTeste = new Postagem();
                        postagemTeste.setDataPostagem("30/05/2023 14:54");
                        postagemTeste.setIdComunidade(postagemSelecionada.getIdComunidade());
                        postagemTeste.setIdDonoPostagem(postagemSelecionada.getIdDonoPostagem());
                        postagemTeste.setIdPostagem(postagemSelecionada.getIdPostagem() + "teste3");
                        long timeteste = -1685492041070L;
                        postagemTeste.setTimestampNegativo(timeteste);
                        postagemTeste.setTipoPostagem("gif");
                        postagemTeste.setUrlPostagem("https://media.tenor.com/mSWD-MGgfjMAAAAC/anime-love.gif");
                        DatabaseReference saveTesteRef = firebaseRef.child("postagensComunidade")
                                .child(postagemSelecionada.getIdComunidade())
                                .child(postagemSelecionada.getIdPostagem() + "teste3");
                        saveTesteRef.setValue(postagemTeste).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                ToastCustomizado.toastCustomizadoCurto("SALVO", context);
                                saveTeste = true;
                            }
                        });
                    } else {
                        Postagem postagemTeste = new Postagem();
                        postagemTeste.setDataPostagem("30/05/2023 16:20");
                        postagemTeste.setIdComunidade(postagemSelecionada.getIdComunidade());
                        postagemTeste.setIdDonoPostagem(postagemSelecionada.getIdDonoPostagem());
                        postagemTeste.setIdPostagem(postagemSelecionada.getIdPostagem() + "teste4");
                        long timeteste = -1685492041071L;
                        postagemTeste.setTimestampNegativo(timeteste);
                        postagemTeste.setTipoPostagem("gif");
                        postagemTeste.setUrlPostagem("https://media.tenor.com/Uo9zS27fkqQAAAAC/gankyōkūrubiyūtei-joshiraku.gif");
                        DatabaseReference saveTesteRef = firebaseRef.child("postagensComunidade")
                                .child(postagemSelecionada.getIdComunidade())
                                .child(postagemSelecionada.getIdPostagem() + "teste4");
                        saveTesteRef.setValue(postagemTeste).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                ToastCustomizado.toastCustomizadoCurto("SALVO2", context);
                                saveTeste = true;
                            }
                        });
                    }
                }
            });


            holder.imgBtnEditarPostagemComunidade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editarPostagem(holder.imgBtnEditarPostagemComunidade, postagemSelecionada, position);
                }
            });

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

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPostagemInicio, imgViewDonoFotoPostagemInicio,
                imgViewFundoUserInicio;
        private TextView txtViewNomeDonoPostagemInicio, txtViewTituloFotoPostadaInicio,
                txtViewDescricaoFotoPostagemInicio, txtViewContadorLikesFotoPostagemInicio,
                txtViewContadorComentarioFotoPostagemInicio, txtViewContadorViewsFotoPostagemInicio;
        private ImageButton imgButtonLikeFotoPostagemInicio, imgButtonComentariosFotoPostagemInicio,
                imgButtonViewsFotoPostagemInicio, imgBtnEditarPostagemComunidade;
        private Button btnVisitarPerfilFotoPostagem, btnExibirVideo;
        private LinearLayout linearTeste1, linearTeste2, linearTeste3, linearTeste4;
        private ImageView imgViewGifPostagemInicio;
        private Button buttonRemoverTeste;

        private StyledPlayerView playerViewInicio;
        private SpinKitView progressBarExo;
        private boolean isControllerVisible = false;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            btnExibirVideo = itemView.findViewById(R.id.btnExibirVideo);
            imgViewGifPostagemInicio = itemView.findViewById(R.id.imgViewGifPostagemInicio);
            playerViewInicio = itemView.findViewById(R.id.playerViewInicio);
            linearTeste1 = itemView.findViewById(R.id.linearTeste1);
            linearTeste2 = itemView.findViewById(R.id.linearTeste2);
            linearTeste3 = itemView.findViewById(R.id.linearTeste3);
            linearTeste4 = itemView.findViewById(R.id.linearTeste4);
            imgButtonViewsFotoPostagemInicio = itemView.findViewById(R.id.imgButtonViewsFotoPostagemInicio);
            //Foto da postagem;
            imgViewFotoPostagemInicio = itemView.findViewById(R.id.imgViewFotoPostagemInicio);
            //Foto do dono da fotoPostagem;
            imgViewDonoFotoPostagemInicio = itemView.findViewById(R.id.imgViewDonoFotoPostagemInicio);
            //Fundo do dono da fotoPostagem
            imgViewFundoUserInicio = itemView.findViewById(R.id.imgViewFundoUserInicio);
            //Nome do dono da fotoPostagem;
            txtViewNomeDonoPostagemInicio = itemView.findViewById(R.id.txtViewNomeDonoPostagemInicio);
            txtViewTituloFotoPostadaInicio = itemView.findViewById(R.id.txtViewTituloFotoPostadaInicio);
            txtViewDescricaoFotoPostagemInicio = itemView.findViewById(R.id.txtViewDescricaoFotoPostagemInicio);
            imgButtonLikeFotoPostagemInicio = itemView.findViewById(R.id.imgButtonLikeFotoPostagemInicio);
            txtViewContadorLikesFotoPostagemInicio = itemView.findViewById(R.id.txtViewContadorLikesFotoPostagemInicio);
            txtViewContadorComentarioFotoPostagemInicio = itemView.findViewById(R.id.txtViewContadorComentarioFotoPostagemInicio);
            txtViewContadorViewsFotoPostagemInicio = itemView.findViewById(R.id.txtViewContadorViewsFotoPostagemInicio);
            //Button para visitar perfil do usuário selecionado
            btnVisitarPerfilFotoPostagem = itemView.findViewById(R.id.btnVisitarPerfilFotoPostagem);
            //Buttons para ver as postagens
            imgButtonComentariosFotoPostagemInicio = itemView.findViewById(R.id.imgButtonComentariosFotoPostagemInicio);

            buttonRemoverTeste = itemView.findViewById(R.id.buttonRemoverTeste);
            imgBtnEditarPostagemComunidade = itemView.findViewById(R.id.imgBtnEditarPostagemComunidade);

            progressBarExo = itemView.findViewById(R.id.progressBarExo);
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
                        progressBarExo.setVisibility(View.GONE);
                    } else if (playbackState == Player.STATE_BUFFERING) {
                        //*ToastCustomizado.toastCustomizadoCurto("BUFFERING", context);
                        // O vídeo está em buffer, você pode mostrar um indicador de carregamento aqui
                        progressBarExo.setVisibility(View.VISIBLE);
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

        private void verificaEdicao(Postagem postagemSelecionada) {
            if (idUsuarioLogado.equals(postagemSelecionada.getIdDonoPostagem())) {
                if (postagemSelecionada.getEdicaoEmAndamento() != null
                        && postagemSelecionada.getEdicaoEmAndamento()) {
                    imgBtnEditarPostagemComunidade.setVisibility(View.VISIBLE);

                    mudarIconeParaEditando(imgBtnEditarPostagemComunidade);
                } else {
                    imgBtnEditarPostagemComunidade.setVisibility(View.VISIBLE);

                    mudarIconeParaPadrao(imgBtnEditarPostagemComunidade);
                }
            } else {
                imgBtnEditarPostagemComunidade.setVisibility(GONE);
            }
        }
    }


    @Override
    public void onViewAttachedToWindow(@NonNull ItemViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        int position = holder.getBindingAdapterPosition();

        if (position != RecyclerView.NO_POSITION) {
            Postagem newPostagem = listaPostagens.get(position);
            if (newPostagem.getTipoPostagem().equals("video")) {

                holder.removerListenerExoPlayer();

                // Configura o ExoPlayer com a nova fonte de mídia para o vídeo
                exoPlayer.setMediaItem(MediaItem.fromUri(newPostagem.getUrlPostagem()));

                // Vincula o ExoPlayer ao StyledPlayerView
                holder.playerViewInicio.setPlayer(exoPlayer);

                // Faz com que o vídeo se repita quando ele acabar
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

                // Trata do carregamento e da inicialização do vídeo
                holder.adicionarListenerExoPlayer();

                // Indica para o exoPlayer que ele está com a view e a mídia configurada.
                exoPlayer.prepare();

                //Controla a exibição dos botões do styled.
                holder.playerViewInicio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.isControllerVisible) {
                            holder.playerViewInicio.hideController();
                            holder.playerViewInicio.setUseController(false);
                            holder.isControllerVisible = false;
                        } else {
                            holder.playerViewInicio.setUseController(true);
                            holder.playerViewInicio.showController();
                            holder.isControllerVisible = true;
                        }
                    }
                });

                //*ToastCustomizado.toastCustomizadoCurto("Attached", context);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ItemViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        // Verifique se a posição atual é um vídeo
        int position = holder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            Postagem newPostagem = listaPostagens.get(position);
            if (newPostagem.getTipoPostagem().equals("video")) {
                //Remove o listener do exoPlayer
                holder.removerListenerExoPlayer();
                //Para a reprodução.
                exoPlayer.stop();
                //Limpa a mídia do exoPlayer.
                exoPlayer.clearMediaItems();
                //Volta para o início do vídeo.
                exoPlayer.seekToDefaultPosition();
                //Diz para o exoPlayer que ele não está pronto.
                exoPlayer.setPlayWhenReady(false);
                //Desvincula o exoPlayer anterior.
                holder.playerViewInicio.setPlayer(null);

                //Oculta os controladores do styled.
                holder.playerViewInicio.hideController();
                holder.playerViewInicio.setUseController(false);
                holder.isControllerVisible = false;

                //*ToastCustomizado.toastCustomizadoCurto("CLEAN", context);
            }
        }
    }

    private void exibirCardUserDono(Boolean userAtualEpilepsia, String
            idPostagemAtual, ImageView imgViewFoto, ImageView imgViewFundo, TextView txtViewNome) {

        FirebaseRecuperarUsuario.recuperaUsuario(idPostagemAtual, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
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
}