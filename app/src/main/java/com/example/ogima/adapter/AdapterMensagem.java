package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.activity.FotoVideoExpandidoActivity;
import com.example.ogima.activity.PlayerMusicaChatActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AdapterMensagem extends RecyclerView.Adapter<AdapterMensagem.MyViewHolder> {

    private List<Mensagem> listaMensagem;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private String idUsuarioRecebido;
    private static final int LAYOUT_REMETENTE = 0;
    private static final int LAYOUT_DESTINATARIO = 1;
    public ExoPlayer exoPlayerMensagem;

    public void pausePlayer() {
        exoPlayerMensagem.setPlayWhenReady(false);
        exoPlayerMensagem.getPlaybackState();
        ToastCustomizado.toastCustomizadoCurto("Pause", context);
    }

    public void startPlayer() {
        exoPlayerMensagem.setPlayWhenReady(true);
        exoPlayerMensagem.getPlaybackState();
        ToastCustomizado.toastCustomizadoCurto("Play", context);
    }

    public void seekTo() {
        if (exoPlayerMensagem != null) {
            exoPlayerMensagem.seekTo(exoPlayerMensagem.getCurrentPosition());
            //ToastCustomizado.toastCustomizadoCurto("Seek to " + exoPlayer.getCurrentPosition(), context);
        }
    }

    public void releasePlayer() {
        if (exoPlayerMensagem != null) {
            exoPlayerMensagem.release();
        }
    }

    public AdapterMensagem(Context c, List<Mensagem> listMensagem) {
        this.context = c;
        this.listaMensagem = listMensagem;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = listaMensagem.get(position);
        if (idUsuarioLogado.equals(mensagem.getIdRemetente())) {
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
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Mensagem mensagem = listaMensagem.get(position);

        if (mensagem.getTipoMensagem().equals("texto")) {
            holder.txtViewMensagem.setVisibility(View.VISIBLE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.btnExpandirVideo.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.txtViewMensagem.setText(mensagem.getConteudoMensagem());
        } else if (mensagem.getTipoMensagem().equals("imagem")) {
            holder.imgViewMensagem.setVisibility(View.VISIBLE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.btnExpandirVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            GlideCustomizado.montarGlideMensagem(context, mensagem.getConteudoMensagem(),
                    holder.imgViewMensagem, android.R.color.transparent);
        } else if (mensagem.getTipoMensagem().equals("gif")) {
            holder.imgViewGifMensagem.setVisibility(View.VISIBLE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.btnExpandirVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            DatabaseReference usuarioAtualRef = firebaseRef.child("usuarios")
                    .child(idUsuarioLogado);
            usuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        if (usuario.getEpilepsia().equals("Sim")) {
                            GlideCustomizado.montarGlideMensagemEpilepsia(context, mensagem.getConteudoMensagem(),
                                    holder.imgViewGifMensagem, android.R.color.transparent);
                        } else {
                            GlideCustomizado.montarGlideMensagem(context, mensagem.getConteudoMensagem(),
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
        } else if (mensagem.getTipoMensagem().equals("video")) {
            holder.videoMensagem.setVisibility(View.VISIBLE);
            holder.btnExpandirVideo.setVisibility(View.VISIBLE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            exoPlayerMensagem = new ExoPlayer.Builder(context).build();
            holder.videoMensagem.setPlayer(exoPlayerMensagem);
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(mensagem.getConteudoMensagem())
                    .setMediaId("mediaId")
                    .setTag("metadata")
                    .build();
            exoPlayerMensagem.setPlayWhenReady(false);
            exoPlayerMensagem.setMediaItem(mediaItem);
            exoPlayerMensagem.prepare();
        } else if (mensagem.getTipoMensagem().equals("documento")) {
            holder.linearDocumentoChat.setVisibility(View.VISIBLE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.btnExpandirVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            holder.txtViewNomeDocumentoChat.setText(mensagem.getNomeDocumento());
        } else if (mensagem.getTipoMensagem().equals("musica")) {
            holder.linearMusicaChat.setVisibility(View.VISIBLE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.btnExpandirVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.txtViewMusicaChat.setText(mensagem.getNomeDocumento());
            holder.txtViewDuracaoMusicaChat.setText(mensagem.getDuracaoMusica());
        } else if (mensagem.getTipoMensagem().equals("audio")) {
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.VISIBLE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.btnExpandirVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.txtViewAudioChat.setText(mensagem.getNomeDocumento());
        }

        holder.txtViewDataMensagem.setText(mensagem.getDataMensagem());

        holder.imgViewMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //ToastCustomizado.toastCustomizadoCurto("Long",context);
                mostrarOpcoes(view);
                return true;
            }
        });

        holder.linearDocumentoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagem, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "documentos");
                        baixarArquivo(mensagem, caminhoDestino);
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
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagem, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "documentos");
                        baixarArquivo(mensagem, caminhoDestino);
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
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "documentos" + File.separator + mensagem.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagem, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "documentos");
                        baixarArquivo(mensagem, caminhoDestino);
                    }

                } catch (ActivityNotFoundException e) {
                    ToastCustomizado.toastCustomizadoCurto("Não foi possível abrir esse arquivo", context);
                }

            }
        });

        holder.imgViewMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "musicas" + File.separator + mensagem.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "musicas");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.txtViewMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "musicas" + File.separator + mensagem.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "musicas");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.linearMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "musicas" + File.separator + mensagem.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "musicas");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.linearAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "audios" + File.separator + mensagem.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "audios");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.txtViewAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "audios" + File.separator + mensagem.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "audios");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.imgViewAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "audios" + File.separator + mensagem.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "audios");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.imgViewMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("mensagem",mensagem);
                context.startActivity(intent);
            }
        });

        holder.btnExpandirVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("mensagem",mensagem);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMensagem.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem, txtViewDataMensagem, txtViewNomeDocumentoChat,
                txtViewMusicaChat, txtViewAudioChat, txtViewDuracaoMusicaChat;
        private ImageView imgViewMensagem, imgViewGifMensagem, imgViewDocumentoChat,
                imgViewMusicaChat, imgViewAudioChat;
        private Button btnExpandirVideo;
        private StyledPlayerView videoMensagem;
        private LinearLayout linearDocumentoChat, linearMusicaChat, linearAudioChat;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtViewMensagem = itemView.findViewById(R.id.txtViewMensagem);
            txtViewDataMensagem = itemView.findViewById(R.id.txtViewDataMensagem);
            imgViewMensagem = itemView.findViewById(R.id.imgViewMensagem);
            imgViewGifMensagem = itemView.findViewById(R.id.imgViewGifMensagem);
            videoMensagem = itemView.findViewById(R.id.videoMensagem);
            btnExpandirVideo = itemView.findViewById(R.id.btnExpandirVideo);

            linearDocumentoChat = itemView.findViewById(R.id.linearDocumentoChat);
            imgViewDocumentoChat = itemView.findViewById(R.id.imgViewDocumentoChat);
            txtViewNomeDocumentoChat = itemView.findViewById(R.id.txtViewNomeDocumentoChat);

            linearMusicaChat = itemView.findViewById(R.id.linearMusicaChat);
            imgViewMusicaChat = itemView.findViewById(R.id.imgViewMusicaChat);
            txtViewMusicaChat = itemView.findViewById(R.id.txtViewMusicaChat);

            linearAudioChat = itemView.findViewById(R.id.linearAudioChat);
            imgViewAudioChat = itemView.findViewById(R.id.imgViewAudioChat);
            txtViewAudioChat = itemView.findViewById(R.id.txtViewAudioChat);

            txtViewDuracaoMusicaChat = itemView.findViewById(R.id.txtViewDuracaoMusicaChat);
        }
    }

    private void mostrarOpcoes(View v) {
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

        ImageView imgViewExcluirMsg = mBottomSheetDialog.findViewById(R.id.imgViewExcluirMsg);
        ImageView imgViewExcluirMsgTodos = mBottomSheetDialog.findViewById(R.id.imgViewExcluirMsgTodos);
        ImageView imgViewShareMsg = mBottomSheetDialog.findViewById(R.id.imgViewShareMsg);
        ImageView imgViewBaixarMsg = mBottomSheetDialog.findViewById(R.id.imgViewBaixarMsg);

        txtViewExcluirMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastCustomizado.toastCustomizadoCurto("Clicado teste", context);
            }
        });

        mBottomSheetDialog.show();
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
}
