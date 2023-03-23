package com.example.ogima.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.activity.ConversaActivity;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.activity.FotoVideoExpandidoActivity;
import com.example.ogima.activity.PlayerMusicaChatActivity;
import com.example.ogima.activity.ShareMessageActivity;
import com.example.ogima.activity.TestesActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.CoresRandomicas;
import com.example.ogima.helper.DadosUserPadrao;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AdapterMensagem extends FirebaseRecyclerAdapter<Mensagem, AdapterMensagem.MyViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;

    private static final int LAYOUT_REMETENTE = 0;
    private static final int LAYOUT_DESTINATARIO = 1;

    //Variáveis para exclusão da mensagem
    private DatabaseReference deleteMessageForMeRef;
    private DatabaseReference contadorMessageForMeRef;

    private DatabaseReference deleteMessageReceiverRef;
    private DatabaseReference contadorMessageReceiverRef;
    private String nomePasta;
    private StorageReference storageRef;
    private StorageReference removerArquivoRef;

    public String stringTeste;
    private ConversaActivity conversaActivity = new ConversaActivity();
    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();
    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            //Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };
    private Activity activity;
    private String filtrarSomenteTexto;
    private List<Mensagem> listaSelecionados = new ArrayList<>();
    private Boolean chatGrupo;

    private CoresRandomicas coresRandomicas = new CoresRandomicas();
    private List<Integer> coresRandom = coresRandomicas.getCores();

    private int corPadraoMensagemDestinatario = Color.parseColor("#7EC2E1");
    private int corPadraoNome = Color.WHITE;
    private Grupo grupoRecebido;

    public AdapterMensagem(Context c, @NonNull FirebaseRecyclerOptions<Mensagem> options, Activity activityRecebida, Boolean chatGrupo, Grupo grupoAtual) {
        super(options);
        this.context = c;
        this.activity = activityRecebida;
        this.chatGrupo = chatGrupo;
        this.grupoRecebido = grupoAtual;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagemType = getItem(position);
        if (idUsuarioLogado.equals(mensagemType.getIdRemetente())) {
            return LAYOUT_REMETENTE;
        }
        return LAYOUT_DESTINATARIO;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;

        if (viewType == LAYOUT_REMETENTE) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);
        } else if (viewType == LAYOUT_DESTINATARIO) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Mensagem mensagemAtual) {

        if (chatGrupo) {
            if (mensagemAtual.getIdRemetente().equals(idUsuarioLogado)) {
                holder.imgViewRemetenteGrupo.setVisibility(View.GONE);
                holder.txtViewNomeRemetenteGrupo.setVisibility(View.GONE);
            } else {
                holder.imgViewRemetenteGrupo.setVisibility(View.VISIBLE);
                holder.txtViewNomeRemetenteGrupo.setVisibility(View.VISIBLE);
                DatabaseReference recuperaUserGrupoRef = firebaseRef.child("usuarios")
                        .child(mensagemAtual.getIdRemetente());
                recuperaUserGrupoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioGrupo = snapshot.getValue(Usuario.class);
                            //ToastCustomizado.toastCustomizadoCurto("Caiu aqui " + usuarioGrupo.getNomeUsuario(),context);
                            DadosUserPadrao.preencherDadosUser(context,
                                    usuarioGrupo, holder.txtViewNomeRemetenteGrupo, holder.imgViewRemetenteGrupo);

                            // Calcule um número a partir do id do usuário usando a função hashCode()
                            int numero = Math.abs(usuarioGrupo.getIdUsuario().hashCode());
                            // Selecione uma cor a partir da lista de cores usando o operador % (resto da divisão)
                            int cor = coresRandom.get(numero % coresRandom.size());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                holder.txtViewMensagem.setBackgroundTintList(ColorStateList.valueOf(cor));
                                holder.txtViewNomeRemetenteGrupo.setTextColor(cor);
                                holder.linearMusicaChat.setBackgroundTintList(ColorStateList.valueOf(cor));
                                holder.linearDocumentoChat.setBackgroundTintList(ColorStateList.valueOf(cor));
                                holder.linearAudioChat.setBackgroundTintList(ColorStateList.valueOf(cor));
                            }
                        }
                        recuperaUserGrupoRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        } else {
            holder.imgViewRemetenteGrupo.setVisibility(View.GONE);
            holder.txtViewNomeRemetenteGrupo.setVisibility(View.GONE);
        }

        if (filtrarSomenteTexto != null) {
            if (filtrarSomenteTexto.equals("sim")) {
                if (mensagemAtual.getTipoMensagem().equals("texto")) {
                    holder.linearLayoutMensagem.setVisibility(View.VISIBLE);
                    holder.txtViewMensagem.setVisibility(View.VISIBLE);
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                    holder.txtViewMensagem.setText(mensagemAtual.getConteudoMensagem());
                } else {
                    holder.linearLayoutMensagem.setVisibility(View.GONE);
                }
            }
        } else {
            holder.linearLayoutMensagem.setVisibility(View.VISIBLE);
            if (mensagemAtual.getTipoMensagem().equals("texto")) {
                holder.txtViewMensagem.setVisibility(View.VISIBLE);
                holder.constraintThumbVideo.setVisibility(View.GONE);
                holder.imgViewMensagem.setVisibility(View.GONE);
                holder.imgViewGifMensagem.setVisibility(View.GONE);
                holder.linearDocumentoChat.setVisibility(View.GONE);
                holder.linearMusicaChat.setVisibility(View.GONE);
                holder.linearAudioChat.setVisibility(View.GONE);
                holder.txtViewMensagem.setText(mensagemAtual.getConteudoMensagem());
            } else if (mensagemAtual.getTipoMensagem().equals("imagem")) {
                holder.imgViewMensagem.setVisibility(View.VISIBLE);
                holder.constraintThumbVideo.setVisibility(View.GONE);
                holder.txtViewMensagem.setVisibility(View.GONE);
                holder.imgViewGifMensagem.setVisibility(View.GONE);
                holder.linearDocumentoChat.setVisibility(View.GONE);
                holder.linearMusicaChat.setVisibility(View.GONE);
                holder.linearAudioChat.setVisibility(View.GONE);
                GlideCustomizado.montarGlideMensagem(context, mensagemAtual.getConteudoMensagem(),
                        holder.imgViewMensagem, android.R.color.transparent);
            } else if (mensagemAtual.getTipoMensagem().equals("gif")) {
                holder.imgViewGifMensagem.setVisibility(View.VISIBLE);
                holder.imgViewMensagem.setVisibility(View.GONE);
                holder.constraintThumbVideo.setVisibility(View.GONE);
                holder.txtViewMensagem.setVisibility(View.GONE);
                holder.linearDocumentoChat.setVisibility(View.GONE);
                holder.linearMusicaChat.setVisibility(View.GONE);
                holder.linearAudioChat.setVisibility(View.GONE);
                DatabaseReference usuarioAtualRef = firebaseRef.child("usuarios")
                        .child(idUsuarioLogado);
                usuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario usuario = snapshot.getValue(Usuario.class);
                            if (usuario.getEpilepsia().equals("Sim")) {
                                GlideCustomizado.montarGlideMensagemEpilepsia(context, mensagemAtual.getConteudoMensagem(),
                                        holder.imgViewGifMensagem, android.R.color.transparent);
                            } else {
                                GlideCustomizado.montarGlideMensagem(context, mensagemAtual.getConteudoMensagem(),
                                        holder.imgViewGifMensagem, android.R.color.transparent);
                            }
                        }
                        usuarioAtualRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //GlideCustomizado.montarGlideMensagem(context, mensagem.getConteudoMensagem(),
                // holder.imgViewMensagem, android.R.color.transparent);
            } else if (mensagemAtual.getTipoMensagem().equals("video")) {
                holder.constraintThumbVideo.setVisibility(View.VISIBLE);
                holder.imgViewMensagem.setVisibility(View.GONE);
                holder.txtViewMensagem.setVisibility(View.GONE);
                holder.imgViewGifMensagem.setVisibility(View.GONE);
                holder.linearDocumentoChat.setVisibility(View.GONE);
                holder.linearMusicaChat.setVisibility(View.GONE);
                holder.linearAudioChat.setVisibility(View.GONE);
                GlideCustomizado.montarGlideFoto(context,
                        mensagemAtual.getConteudoMensagem(),
                        holder.imgViewVideoMensagem,
                        android.R.color.transparent);
            } else if (mensagemAtual.getTipoMensagem().equals("documento")) {
                holder.linearDocumentoChat.setVisibility(View.VISIBLE);
                holder.linearMusicaChat.setVisibility(View.GONE);
                holder.imgViewGifMensagem.setVisibility(View.GONE);
                holder.imgViewMensagem.setVisibility(View.GONE);
                holder.constraintThumbVideo.setVisibility(View.GONE);
                holder.txtViewMensagem.setVisibility(View.GONE);
                holder.linearAudioChat.setVisibility(View.GONE);
                holder.txtViewNomeDocumentoChat.setText(mensagemAtual.getNomeDocumento());
            } else if (mensagemAtual.getTipoMensagem().equals("musica")) {
                holder.linearMusicaChat.setVisibility(View.VISIBLE);
                holder.linearDocumentoChat.setVisibility(View.GONE);
                holder.linearAudioChat.setVisibility(View.GONE);
                holder.imgViewGifMensagem.setVisibility(View.GONE);
                holder.imgViewMensagem.setVisibility(View.GONE);
                holder.constraintThumbVideo.setVisibility(View.GONE);
                holder.txtViewMensagem.setVisibility(View.GONE);
                holder.txtViewMusicaChat.setText(mensagemAtual.getNomeDocumento());
                holder.txtViewDuracaoMusicaChat.setText(mensagemAtual.getDuracaoMusica());
            } else if (mensagemAtual.getTipoMensagem().equals("audio")) {
                holder.linearMusicaChat.setVisibility(View.GONE);
                holder.linearDocumentoChat.setVisibility(View.GONE);
                holder.linearAudioChat.setVisibility(View.VISIBLE);
                holder.imgViewGifMensagem.setVisibility(View.GONE);
                holder.imgViewMensagem.setVisibility(View.GONE);
                holder.constraintThumbVideo.setVisibility(View.GONE);
                holder.txtViewMensagem.setVisibility(View.GONE);
                holder.txtViewAudioChat.setText(mensagemAtual.getNomeDocumento());
                holder.txtViewDuracaoAudioChat.setText(mensagemAtual.getDuracaoMusica());
            }
        }

        //Data mensagem a cada dia
        //Diferencia a data pelo getDay.

        //Não é possível usar o position do firebaseAdapter ele é invertido
        //qualquer lógica com a postion dele vai ser errado, utilizar a
        //listaReordenada no lugar se possível porém a position vai atrapalhar.

        //

        holder.txtViewDataMensagem.setText(mensagemAtual.getDataMensagem());

        holder.linearDocumentoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "documentos" + File.separator + mensagemAtual.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagemAtual, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "documentos");
                        baixarArquivo(mensagemAtual, caminhoDestino);
                        abrirDocumento(mensagemAtual, file);
                    }

                } catch (ActivityNotFoundException e) {
                    ToastCustomizado.toastCustomizadoCurto("Não foi possível abrir esse arquivo", context);
                }
            }
        });

        holder.txtViewNomeDocumentoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "documentos" + File.separator + mensagemAtual.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagemAtual, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "documentos");
                        baixarArquivo(mensagemAtual, caminhoDestino);
                        abrirDocumento(mensagemAtual, file);
                    }
                } catch (ActivityNotFoundException e) {
                    ToastCustomizado.toastCustomizadoCurto("Não foi possível abrir esse arquivo", context);
                }
            }
        });

        holder.imgViewDocumentoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "documentos" + File.separator + mensagemAtual.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagemAtual, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "documentos");
                        baixarArquivo(mensagemAtual, caminhoDestino);
                        abrirDocumento(mensagemAtual, file);
                    }

                } catch (ActivityNotFoundException e) {
                    ToastCustomizado.toastCustomizadoCurto("Não foi possível abrir esse arquivo", context);
                }

            }
        });

        holder.imgViewMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "musicas" + File.separator + mensagemAtual.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagemAtual, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "musicas");
                    baixarArquivo(mensagemAtual, caminhoDestino);
                    abrirArquivo(mensagemAtual, "audio");
                }
            }
        });

        holder.txtViewMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "musicas" + File.separator + mensagemAtual.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagemAtual, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "musicas");
                    baixarArquivo(mensagemAtual, caminhoDestino);
                    abrirArquivo(mensagemAtual, "audio");
                }
            }
        });

        holder.linearMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "musicas" + File.separator + mensagemAtual.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagemAtual, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "musicas");
                    baixarArquivo(mensagemAtual, caminhoDestino);
                    abrirArquivo(mensagemAtual, "audio");
                }
            }
        });

        holder.linearAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios" + File.separator + mensagemAtual.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagemAtual, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios");
                    baixarArquivo(mensagemAtual, caminhoDestino);
                    abrirArquivo(mensagemAtual, "audio");
                }
            }
        });

        holder.txtViewAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios" + File.separator + mensagemAtual.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagemAtual, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios");
                    baixarArquivo(mensagemAtual, caminhoDestino);
                    abrirArquivo(mensagemAtual, "audio");
                }
            }
        });

        holder.imgViewAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios" + File.separator + mensagemAtual.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagemAtual, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios");
                    baixarArquivo(mensagemAtual, caminhoDestino);
                    abrirArquivo(mensagemAtual, "audio");
                }
            }
        });

        holder.imgViewMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("mensagem", mensagemAtual);
                context.startActivity(intent);
            }
        });

        holder.imgButtonExpandirVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("mensagem", mensagemAtual);
                context.startActivity(intent);
            }
        });

        //Eventos de clique longo.
        holder.imgViewMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //ToastCustomizado.toastCustomizadoCurto("Long",context);
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.txtViewMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, holder.txtViewMensagem);
                return true;
            }
        });

        holder.imgViewGifMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.imgButtonExpandirVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.imgViewVideoMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.imgViewMusicaChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.txtViewMusicaChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.imgViewAudioChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.txtViewAudioChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.imgViewDocumentoChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });

        holder.txtViewNomeDocumentoChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagemAtual, position, null);
                return true;
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem, txtViewDataMensagem, txtViewNomeDocumentoChat,
                txtViewMusicaChat, txtViewAudioChat, txtViewDuracaoMusicaChat,
                txtViewDuracaoAudioChat, txtViewDataTrocaMensagens, txtViewNomeRemetenteGrupo;
        private ImageView imgViewMensagem, imgViewGifMensagem, imgViewDocumentoChat,
                imgViewMusicaChat, imgViewAudioChat, imgViewVideoMensagem, imgViewRemetenteGrupo;
        private ImageButton imgButtonExpandirVideo;
        private LinearLayout linearDocumentoChat, linearMusicaChat, linearAudioChat,
                linearLayoutMensagem;
        private ConstraintLayout constraintThumbVideo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutMensagem = itemView.findViewById(R.id.linearLayoutMensagem);

            txtViewMensagem = itemView.findViewById(R.id.txtViewMensagem);
            txtViewDataMensagem = itemView.findViewById(R.id.txtViewDataMensagem);
            imgViewMensagem = itemView.findViewById(R.id.imgViewMensagem);
            imgViewGifMensagem = itemView.findViewById(R.id.imgViewGifMensagem);
            imgViewVideoMensagem = itemView.findViewById(R.id.imgViewVideoMensagem);
            constraintThumbVideo = itemView.findViewById(R.id.constraintThumbVideo);
            imgButtonExpandirVideo = itemView.findViewById(R.id.imgButtonExpandirVideo);

            linearDocumentoChat = itemView.findViewById(R.id.linearDocumentoChat);
            imgViewDocumentoChat = itemView.findViewById(R.id.imgViewDocumentoChat);
            txtViewNomeDocumentoChat = itemView.findViewById(R.id.txtViewNomeDocumentoChat);

            linearMusicaChat = itemView.findViewById(R.id.linearMusicaChat);
            imgViewMusicaChat = itemView.findViewById(R.id.imgViewMusicaChat);
            txtViewMusicaChat = itemView.findViewById(R.id.txtViewMusicaChat);

            linearAudioChat = itemView.findViewById(R.id.linearAudioChat);
            imgViewAudioChat = itemView.findViewById(R.id.imgViewAudioChat);
            txtViewAudioChat = itemView.findViewById(R.id.txtViewAudioChat);
            txtViewDuracaoAudioChat = itemView.findViewById(R.id.txtViewDuracaoAudioChat);

            txtViewDuracaoMusicaChat = itemView.findViewById(R.id.txtViewDuracaoMusicaChat);
            txtViewDataTrocaMensagens = itemView.findViewById(R.id.txtViewDataTrocaMensagens);

            imgViewRemetenteGrupo = itemView.findViewById(R.id.imgViewRemetenteGrupo);
            txtViewNomeRemetenteGrupo = itemView.findViewById(R.id.txtViewNomeRemetenteGrupo);
        }
    }

    private void mostrarOpcoes(View v, Mensagem mensagem, int position, TextView txtViewMensagem) {
        Context context = v.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.bottom_sheet_dialog_opcoes_mensagem, null);
        final Dialog mBottomSheetDialog = new Dialog(context);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

        TextView txtViewExcluirMsg = mBottomSheetDialog.findViewById(R.id.txtViewExcluirMsg);
        TextView txtViewExcluirMsgTodos = mBottomSheetDialog.findViewById(R.id.txtViewExcluirMsgTodos);
        TextView txtViewShareMsg = mBottomSheetDialog.findViewById(R.id.txtViewShareMsg);
        TextView txtViewBaixarMsg = mBottomSheetDialog.findViewById(R.id.txtViewBaixarMsg);
        TextView txtViewCopiarTexto = mBottomSheetDialog.findViewById(R.id.txtViewCopiarTexto);

        ImageView imgViewExcluirMsg = mBottomSheetDialog.findViewById(R.id.imgViewExcluirMsg);
        ImageView imgViewExcluirMsgTodos = mBottomSheetDialog.findViewById(R.id.imgViewExcluirMsgTodos);
        ImageView imgViewShareMsg = mBottomSheetDialog.findViewById(R.id.imgViewShareMsg);
        ImageView imgViewBaixarMsg = mBottomSheetDialog.findViewById(R.id.imgViewBaixarMsg);
        ImageView imgViewCopiarTexto = mBottomSheetDialog.findViewById(R.id.imgViewCopiarTexto);

        LinearLayout uploadLinearLayout = mBottomSheetDialog.findViewById(R.id.uploadLinearLayout);
        LinearLayout deleteForMeLayout = mBottomSheetDialog.findViewById(R.id.deleteForMeLayout);
        LinearLayout deleteForAllLayout = mBottomSheetDialog.findViewById(R.id.deleteForAllLayout);
        LinearLayout copiarTextoLinearLayout = mBottomSheetDialog.findViewById(R.id.copiarTextoLinearLayout);
        LinearLayout shareLinearLayout = mBottomSheetDialog.findViewById(R.id.shareLinearLayout);


        if (chatGrupo) {
            deleteForMeLayout.setVisibility(View.GONE);
            if (!idUsuarioLogado.equals(mensagem.getIdRemetente())) {
                deleteForAllLayout.setVisibility(View.GONE);
            } else {
                deleteForAllLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (!idUsuarioLogado.equals(mensagem.getIdRemetente())) {
                deleteForMeLayout.setVisibility(View.VISIBLE);
                deleteForAllLayout.setVisibility(View.GONE);
            } else {
                deleteForMeLayout.setVisibility(View.VISIBLE);
                deleteForAllLayout.setVisibility(View.VISIBLE);
            }
        }

        if (mensagem.getTipoMensagem().equals("texto")) {
            shareLinearLayout.setVisibility(View.GONE);
            copiarTextoLinearLayout.setVisibility(View.VISIBLE);
        } else {
            copiarTextoLinearLayout.setVisibility(View.GONE);
            shareLinearLayout.setVisibility(View.VISIBLE);
        }

        if (mensagem.getTipoMensagem().equals("texto")
                || mensagem.getTipoMensagem().equals("gif")) {
            uploadLinearLayout.setVisibility(View.GONE);
        } else {
            uploadLinearLayout.setVisibility(View.VISIBLE);
        }

        imgViewCopiarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copiarTexto(mensagem, txtViewMensagem, mBottomSheetDialog);
            }
        });

        txtViewCopiarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copiarTexto(mensagem, txtViewMensagem, mBottomSheetDialog);
            }
        });

        copiarTextoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copiarTexto(mensagem, txtViewMensagem, mBottomSheetDialog);
            }
        });

        txtViewBaixarMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                baixarPeloSheet(mensagem);
            }
        });

        imgViewBaixarMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                baixarPeloSheet(mensagem);
            }
        });

        imgViewShareMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compartilharMensagem(mensagem);
            }
        });

        txtViewShareMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compartilharMensagem(mensagem);
            }
        });

        txtViewExcluirMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                if (!mensagem.getTipoMensagem().equals("texto")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para mim");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, "sim");
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, "não");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (mensagem.getTipoMensagem().equals("texto")) {
                    deleteMessageForMe(mensagem, position, "não");
                }
            }
        });

        imgViewExcluirMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                if (!mensagem.getTipoMensagem().equals("texto")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para mim");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, "sim");
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, "não");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (mensagem.getTipoMensagem().equals("texto")) {
                    deleteMessageForMe(mensagem, position, "não");
                }
            }
        });

        //Evento de clique para excluir mensagem para todos
        txtViewExcluirMsgTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                if (!mensagem.getTipoMensagem().equals("texto")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para todos");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, "sim");
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, "não");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (mensagem.getTipoMensagem().equals("texto")) {
                    deleteMessageForAll(mensagem, position, "não");
                }
            }
        });

        imgViewExcluirMsgTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                if (!mensagem.getTipoMensagem().equals("texto")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para todos");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, "sim");
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, "não");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (mensagem.getTipoMensagem().equals("texto")) {
                    deleteMessageForAll(mensagem, position, "não");
                }
            }
        });

        mBottomSheetDialog.show();
    }

    //Exclui a mensagem somente para o próprio usuário
    private void deleteMessageForMe(Mensagem mensagem, int position, String excluirLocalmente) {
        try {

            //Somente se for chat comum que pode excluir a mensagem para si mesmo.
            if (!chatGrupo) {
                if (idUsuarioLogado.equals(mensagem.getIdRemetente())) {
                    deleteMessageForMeRef = firebaseRef.child("conversas")
                            .child(idUsuarioLogado).child(mensagem.getIdDestinatario())
                            .child(mensagem.getIdConversa());

                    contadorMessageForMeRef = firebaseRef.child("contadorMensagens")
                            .child(idUsuarioLogado).child(mensagem.getIdDestinatario());
                } else {
                    //Se o usuário atual não for o remetente, ele mesmo assim
                    //irá poder remover a mensagem para si mesmo.
                    deleteMessageForMeRef = firebaseRef.child("conversas")
                            .child(idUsuarioLogado).child(mensagem.getIdRemetente())
                            .child(mensagem.getIdConversa());

                    contadorMessageForMeRef = firebaseRef.child("contadorMensagens")
                            .child(idUsuarioLogado).child(mensagem.getIdRemetente());
                }

                deleteMessageForMeRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Diminuindo contador
                        contadorMessageForMeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Mensagem mensagemContador = snapshot.getValue(Mensagem.class);
                                    if (mensagemContador.getTotalMensagens() == 1 ||
                                            mensagemContador.getTotalMensagens() <= 0) {
                                        contadorMessageForMeRef.removeValue();
                                    } else {
                                        int contador = mensagemContador.getTotalMensagens();
                                        contador = contador - 1;
                                        contadorMessageForMeRef.child("totalMensagens").setValue(contador);
                                    }
                                }
                                contadorMessageForMeRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        ToastCustomizado.toastCustomizado("Excluido com sucesso", context);
                    }
                });

                //ToastCustomizado.toastCustomizadoCurto("Mensagem selecionada " + mensagemSelecionada.getConteudoMensagem(), context);
            }


            if (!mensagem.getTipoMensagem().equals("texto")) {
                if (excluirLocalmente.equals("sim")) {
                    excluirArquivoLocal(mensagem);
                }
            }

        } catch (
                Exception ex) {
            ToastCustomizado.toastCustomizadoCurto("Erro " + ex.getMessage(), context);
        }

    }

    private void excluirArquivoLocal(Mensagem mensagem) {

        nomePasta = null;

        if (mensagem.getTipoMensagem().equals("imagem")) {
            nomePasta = "imagens";
        } else if (mensagem.getTipoMensagem().equals("gif")) {
            nomePasta = "gifs";
        } else if (mensagem.getTipoMensagem().equals("video")) {
            nomePasta = "videos";
        } else if (mensagem.getTipoMensagem().equals("musica")) {
            nomePasta = "musicas";
        } else if (mensagem.getTipoMensagem().equals("audio")) {
            nomePasta = "audios";
        } else if (mensagem.getTipoMensagem().equals("documento")) {
            nomePasta = "documentos";
        }

        solicitaPermissoes("permissoesDelete");

        //ToastCustomizado.toastCustomizado("Id da pasta " + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + nomePasta + File.separator + mensagem.getNomeDocumento(), context);
        if (!solicitaPermissoes.exibirPermissaoNegada) {
            try {
                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + nomePasta + File.separator + mensagem.getNomeDocumento());
                boolean caminhoexiste = caminhoDestino.exists();
                boolean canread = caminhoDestino.canRead();
                boolean canwrite = caminhoDestino.canWrite();
                ToastCustomizado.toastCustomizadoCurto("Caminho Existe " + caminhoexiste, context);
                ToastCustomizado.toastCustomizadoCurto("CanRead " + canread, context);
                ToastCustomizado.toastCustomizadoCurto("CanWrite " + canwrite, context);
                if (caminhoDestino.exists()) {
                    caminhoDestino.delete();
                    ToastCustomizado.toastCustomizadoCurto("Arquivo excluído de seu dispositivo com sucesso", context);
                    //boolean deleted = caminhoDestino.delete();
                    //ToastCustomizado.toastCustomizadoCurto("File Delete " + deleted, context);
                    //caminhoDestino.deleteOnExit();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Arquivo não localizado em seu dispositivo", context);
                }
            } catch (Exception ex) {
                Log.i("App", "Exception while deleting file " + ex.getMessage());
            }
        } else {
            ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", context);
        }
    }


    private void deleteMessageForAll(Mensagem mensagem, int position, String excluirLocalmente) {
        try {

            if (mensagem.getTipoMensagem().equals("imagem")) {
                nomePasta = "fotos";
            } else if (mensagem.getTipoMensagem().equals("gif")) {
                nomePasta = "gifs";
            } else if (mensagem.getTipoMensagem().equals("video")) {
                nomePasta = "videos";
            } else if (mensagem.getTipoMensagem().equals("musica")) {
                nomePasta = "musicas";
            } else if (mensagem.getTipoMensagem().equals("audio")) {
                nomePasta = "audios";
            } else if (mensagem.getTipoMensagem().equals("documento")) {
                nomePasta = "documentos";
            }


            if (!mensagem.getTipoMensagem().equals("gif")
                    && !mensagem.getTipoMensagem().equals("texto")) {
                //Remover primeiro do storage
                removerArquivoRef = storageRef.child("mensagens")
                        .child(nomePasta)
                        .child(idUsuarioLogado)
                        .child(mensagem.getIdDestinatario())
                        .getStorage().getReferenceFromUrl(mensagem.getConteudoMensagem());
                removerArquivoRef.delete();
            }

            if (chatGrupo) {

                deleteMessageForMeRef = firebaseRef.child("conversas").child(grupoRecebido.getIdGrupo())
                        .child(mensagem.getIdConversa());

                contadorMessageForMeRef = firebaseRef.child("contadorMensagens")
                        .child(grupoRecebido.getIdGrupo());

                deleteMessageForMeRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Diminuindo contador
                        contadorMessageForMeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Mensagem mensagemContador = snapshot.getValue(Mensagem.class);
                                    if (mensagemContador.getTotalMensagens() == 1 ||
                                            mensagemContador.getTotalMensagens() <= 0) {
                                        contadorMessageForMeRef.removeValue();
                                    } else {
                                        int contador = mensagemContador.getTotalMensagens();
                                        contador = contador - 1;
                                        contadorMessageForMeRef.child("totalMensagens").setValue(contador);
                                    }
                                }
                                contadorMessageForMeRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });

            } else {

                deleteMessageForMeRef = firebaseRef.child("conversas")
                        .child(idUsuarioLogado).child(mensagem.getIdDestinatario())
                        .child(mensagem.getIdConversa());

                contadorMessageForMeRef = firebaseRef.child("contadorMensagens")
                        .child(idUsuarioLogado).child(mensagem.getIdDestinatario());

                deleteMessageReceiverRef = firebaseRef.child("conversas")
                        .child(mensagem.getIdDestinatario()).child(idUsuarioLogado)
                        .child(mensagem.getIdConversa());

                contadorMessageReceiverRef = firebaseRef.child("contadorMensagens")
                        .child(mensagem.getIdDestinatario()).child(idUsuarioLogado);


                //Removendo primeiro para o próprio usuário
                deleteMessageForMeRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Diminuindo contador
                        contadorMessageForMeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Mensagem mensagemContador = snapshot.getValue(Mensagem.class);
                                    if (mensagemContador.getTotalMensagens() == 1 ||
                                            mensagemContador.getTotalMensagens() <= 0) {
                                        contadorMessageForMeRef.removeValue();
                                    } else {
                                        int contador = mensagemContador.getTotalMensagens();
                                        contador = contador - 1;
                                        contadorMessageForMeRef.child("totalMensagens").setValue(contador);
                                    }
                                }
                                contadorMessageForMeRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }


            //Removendo também para o usuário destinatário
            deleteMessageReceiverRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //Diminuindo contador
                    contadorMessageReceiverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Mensagem mensagemContador = snapshot.getValue(Mensagem.class);
                                if (mensagemContador.getTotalMensagens() == 1 ||
                                        mensagemContador.getTotalMensagens() <= 0) {
                                    contadorMessageReceiverRef.removeValue();
                                } else {
                                    int contador = mensagemContador.getTotalMensagens();
                                    contador = contador - 1;
                                    contadorMessageReceiverRef.child("totalMensagens").setValue(contador);
                                }
                            }
                            contadorMessageReceiverRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            if (!mensagem.getTipoMensagem().equals("texto")) {
                if (excluirLocalmente.equals("sim")) {
                    excluirArquivoLocal(mensagem);
                }
            }

        } catch (
                Exception ex) {
            ToastCustomizado.toastCustomizadoCurto("Erro " + ex.getMessage(), context);
        }

    }

    private void abrirArquivo(Mensagem mensagem, String keyName) {
        Intent intent = new Intent(context, PlayerMusicaChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(keyName, mensagem);
        context.startActivity(intent);
    }

    private void abrirDocumento(Mensagem mensagem, File file) {
        Uri destinationUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        ToastCustomizado.toastCustomizado("Caminho " + destinationUri, context);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(destinationUri, mensagem.getTipoArquivo());
        Intent intent = Intent.createChooser(target, "Abrir arquivo");
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void baixarPeloSheet(Mensagem mensagem) {
        switch (mensagem.getTipoMensagem()) {
            case "imagem": {
                ToastCustomizado.toastCustomizadoCurto("Imagem", context);
                solicitaPermissoes("imagem");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "imagens");
                    baixarArquivo(mensagem, caminhoDestino);
                    ToastCustomizado.toastCustomizado("Download com sucesso", context);
                }
                break;
            }
            /*
            case "gif":{
                ToastCustomizado.toastCustomizadoCurto("Gif",context);

                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "gifs");
                baixarArquivo(mensagem, caminhoDestino);
                ToastCustomizado.toastCustomizado("Download com sucesso",context);
                break;
            }
             */
            case "video": {
                ToastCustomizado.toastCustomizadoCurto("Video", context);
                solicitaPermissoes("video");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "videos");
                    baixarArquivo(mensagem, caminhoDestino);
                    ToastCustomizado.toastCustomizado("Download com sucesso", context);
                }
                break;
            }
            case "musica": {
                ToastCustomizado.toastCustomizadoCurto("Musica", context);
                solicitaPermissoes("musica");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas");
                    baixarArquivo(mensagem, caminhoDestino);
                    ToastCustomizado.toastCustomizado("Download com sucesso", context);
                }
                break;
            }
            case "audio": {
                ToastCustomizado.toastCustomizadoCurto("Audio", context);
                solicitaPermissoes("audio");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios");
                    baixarArquivo(mensagem, caminhoDestino);
                    ToastCustomizado.toastCustomizado("Download com sucesso", context);
                }
                break;
            }
            case "documento": {
                ToastCustomizado.toastCustomizadoCurto("Documento", context);
                solicitaPermissoes("documento");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos");
                    baixarArquivo(mensagem, caminhoDestino);
                    ToastCustomizado.toastCustomizado("Download com sucesso", context);
                }
                break;
            }
        }
    }

    private void baixarArquivo(Mensagem mensagem, File caminhoDestino) {
        //Fazer o download pela url do arquivo
        DownloadManager.Request requestDocumento = new DownloadManager.Request(Uri.parse(mensagem.getConteudoMensagem()));
        //Verificando permissões
        requestDocumento.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE);
        //Título
        requestDocumento.setTitle(mensagem.getNomeDocumento());
        //Permissão para acessar os arquivos
        requestDocumento.allowScanningByMediaScanner();
        //Deixando visível o progresso de download
        requestDocumento.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //Salvando arquivo
        caminhoDestino.mkdirs();
        Uri trasnformarUri = Uri.fromFile(new File(caminhoDestino, mensagem.getNomeDocumento()));
        requestDocumento.setDestinationUri(trasnformarUri);
        DownloadManager managerDocumento = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        managerDocumento.enqueue(requestDocumento);
    }

    private void compartilharMensagem(Mensagem mensagemRecebida) {

        solicitaPermissoes("galeria");
        //Verifica se usuário tem as permissões necessárias para compartilhar.
        if (!solicitaPermissoes.exibirPermissaoNegada) {
            Intent intent = new Intent(context, ShareMessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("mensagemCompartilhada", mensagemRecebida);
            context.startActivity(intent);
        }
    }

    private void solicitaPermissoes(String permissao) {
        //Se alguma permissão não foi aceita, então a seguinte lógica é acionada.
        if (!solicitaPermissoes.verificaPermissoes(permissoesNecessarias, activity, permissao)) {
            if (permissao != null) {
                solicitaPermissoes.tratarResultadoPermissoes(permissao, activity);
            }
        }
    }

    @Override
    public void updateOptions(@NonNull FirebaseRecyclerOptions<Mensagem> options) {
        super.updateOptions(options);
    }

    public void verificarFiltragem(String verificaFiltragem) {
        //Quando a lista está filtrada o adapter será sinalizado
        //por essa string recebida pelo parâmetro.
        if (verificaFiltragem != null) {
            if (verificaFiltragem.equals("comFiltro")) {
                filtrarSomenteTexto = "sim";
            } else {
                filtrarSomenteTexto = null;
            }
        }
    }

    private void copiarTexto(Mensagem mensagem, TextView textView, Dialog dialog) {
        String text = textView.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Texto copiado", text);
        clipboard.setPrimaryClip(clip);
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        ToastCustomizado.toastCustomizadoCurto("Texto copiado com sucesso", context);
    }
}


