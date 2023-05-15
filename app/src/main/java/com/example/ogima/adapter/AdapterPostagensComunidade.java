package com.example.ogima.adapter;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
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
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.TimeZone;

public class AdapterPostagensComunidade extends RecyclerView.Adapter<AdapterPostagensComunidade.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    public ExoPlayer exoPlayer;
    private List<Postagem> listaPostagens;

    private RemoverPostagemListener removerPostagemListener;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;

    private Usuario usuarioCorreto;

    private ValueEventListener valueEventListenerSinalizador;

    public AdapterPostagensComunidade(List<Postagem> listPostagens, Context c, RemoverPostagemListener removerListener,
                                      RecuperaPosicaoAnterior recuperaPosicaoListener) {
        this.context = c;
        this.listaPostagens = listPostagens = new ArrayList<>();
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        //Remoção de elemento
        this.removerPostagemListener = removerListener;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.usuarioCorreto = new Usuario();
        //Funciona somente a última posição se eu colocar no if else do video
    }

    public void updatePostagemList(List<Postagem> listaPostagensAtualizada) {
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaPostagens, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaPostagens.clear();
        listaPostagens.addAll(listaPostagensAtualizada);
        diffResult.dispatchUpdatesTo(this);

        if (listaPostagensAtualizada != null && listaPostagensAtualizada.size() > 0) {
            //ToastCustomizado.toastCustomizadoCurto("Tamanho: " + listaPostagensAtualizada.size(), context);
            for (Postagem postagemExibicao : listaPostagensAtualizada) {
                if (postagemExibicao.getDataPostagem() != null) {
                    //ToastCustomizado.toastCustomizadoCurto("Data: " + postagemExibicao.getDataPostagem(), context);
                }
            }
        }
    }

    public interface RemoverPostagemListener {
        void onComunidadeRemocao(Postagem postagemRemovida);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_postagens_comunidade, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Postagem postagemSelecionada = listaPostagens.get(position);

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
            exoPlayer = new ExoPlayer.Builder(context).build();
            holder.playerViewInicio.setPlayer(exoPlayer);
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(postagemSelecionada.getUrlPostagem())
                    .setMediaId("mediaId")
                    .setTag("metadata")
                    .build();
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
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


        if (idUsuarioLogado.equals(postagemSelecionada.getIdDonoPostagem())) {
            if (postagemSelecionada.getEdicaoEmAndamento() != null
                    && postagemSelecionada.getEdicaoEmAndamento()) {
                holder.imgBtnEditarPostagemComunidade.setVisibility(View.VISIBLE);

                mudarIconeParaEditando(holder.imgBtnEditarPostagemComunidade);
            } else {
                holder.imgBtnEditarPostagemComunidade.setVisibility(View.VISIBLE);

                mudarIconeParaPadrao(holder.imgBtnEditarPostagemComunidade);
            }
        }else{
            holder.imgBtnEditarPostagemComunidade.setVisibility(GONE);
        }

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

        holder.buttonRemoverTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removerPostagemListener.onComunidadeRemocao(postagemSelecionada);
            }
        });


        holder.imgBtnEditarPostagemComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editarPostagem(holder.imgBtnEditarPostagemComunidade, postagemSelecionada, position);
            }
        });
    }

    private void editarPostagem(ImageButton imgBtnEditar, Postagem postagemAlvo, int position) {

        if (postagemAlvo.getEdicaoEmAndamento() != null && postagemAlvo.getEdicaoEmAndamento()) {
            ToastCustomizado.toastCustomizado("Alguém já está editando essa postagem", context);
        }else{
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

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPostagemInicio, imgViewDonoFotoPostagemInicio,
                imgViewFundoUserInicio;
        private TextView txtViewNomeDonoPostagemInicio, txtViewTituloFotoPostadaInicio,
                txtViewDescricaoFotoPostagemInicio, txtViewContadorLikesFotoPostagemInicio,
                txtViewContadorComentarioFotoPostagemInicio, txtViewContadorViewsFotoPostagemInicio;
        private ImageButton imgButtonLikeFotoPostagemInicio, imgButtonComentariosFotoPostagemInicio,
                imgButtonViewsFotoPostagemInicio, imgBtnEditarPostagemComunidade;
        private Button btnVisitarPerfilFotoPostagem, btnExibirVideo;
        private LinearLayout linearTeste1, linearTeste2, linearTeste3, linearTeste4;
        private StyledPlayerView playerViewInicio;
        private ImageView imgViewGifPostagemInicio;
        private Button buttonRemoverTeste;

        public MyViewHolder(@NonNull View itemView) {
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
        }
    }

    private void exibirCardUserDono(Boolean userAtualEpilepsia, String idPostagemAtual, ImageView imgViewFoto, ImageView imgViewFundo, TextView txtViewNome) {

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