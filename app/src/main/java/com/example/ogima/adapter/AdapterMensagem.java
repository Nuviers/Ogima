package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.activity.ConversaActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    //Variáveis para exclusão da mensagem
    private DatabaseReference deleteMessageForMeRef;
    private DatabaseReference contadorMessageForMeRef;

    private DatabaseReference deleteMessageReceiverRef;
    private DatabaseReference contadorMessageReceiverRef;
    private String nomePasta;
    private StorageReference storageRef;
    private StorageReference removerArquivoRef;
    private DatabaseReference contadorMensagemRef;

    public String stringTeste;
    ConversaActivity conversaActivity = new ConversaActivity();

    public AdapterMensagem(Context c, List<Mensagem> listMensagem) {
        this.context = c;
        this.listaMensagem = listMensagem;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

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
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            holder.txtViewMensagem.setText(mensagem.getConteudoMensagem());
        } else if (mensagem.getTipoMensagem().equals("imagem")) {
            holder.imgViewMensagem.setVisibility(View.VISIBLE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            GlideCustomizado.montarGlideMensagem(context, mensagem.getConteudoMensagem(),
                    holder.imgViewMensagem, android.R.color.transparent);
        } else if (mensagem.getTipoMensagem().equals("gif")) {
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
            holder.constraintThumbVideo.setVisibility(View.VISIBLE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            GlideCustomizado.montarGlideFoto(context,
                    mensagem.getConteudoMensagem(),
                    holder.imgViewVideoMensagem,
                    android.R.color.transparent);
        } else if (mensagem.getTipoMensagem().equals("documento")) {
            holder.linearDocumentoChat.setVisibility(View.VISIBLE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            holder.txtViewNomeDocumentoChat.setText(mensagem.getNomeDocumento());
        } else if (mensagem.getTipoMensagem().equals("musica")) {
            holder.linearMusicaChat.setVisibility(View.VISIBLE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.txtViewMusicaChat.setText(mensagem.getNomeDocumento());
            holder.txtViewDuracaoMusicaChat.setText(mensagem.getDuracaoMusica());
        } else if (mensagem.getTipoMensagem().equals("audio")) {
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.VISIBLE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.txtViewAudioChat.setText(mensagem.getNomeDocumento());
            holder.txtViewDuracaoAudioChat.setText(mensagem.getDuracaoMusica());
        }

        //Data mensagem a cada dia
        //Diferencia a data pelo getDay.
        if (listaMensagem.size() >= 1) {
            holder.txtViewDataTrocaMensagens.setVisibility(View.VISIBLE);
            if (position >= 1) {
                if (listaMensagem.get(position - 1).getDataMensagemCompleta().getDay()
                        == mensagem.getDataMensagemCompleta().getDay()) {
                    holder.txtViewDataTrocaMensagens.setVisibility(View.GONE);
                } else {
                    holder.txtViewDataTrocaMensagens.setText("" + mensagem.getDataMensagem());
                }
            } else {
                holder.txtViewDataTrocaMensagens.setText("" + mensagem.getDataMensagem());
            }
        } else {
            holder.txtViewDataTrocaMensagens.setVisibility(View.GONE);
        }

        holder.txtViewDataMensagem.setText(mensagem.getDataMensagem());

        holder.linearDocumentoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos" + File.separator + mensagem.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagem, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos");
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
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos" + File.separator + mensagem.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagem, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos");
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
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos" + File.separator + mensagem.getNomeDocumento());
                    //ToastCustomizado.toastCustomizado("Caminho " + file, context);

                    if (file.exists()) {
                        abrirDocumento(mensagem, file);
                    } else {
                        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos");
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

                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas" + File.separator + mensagem.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.txtViewMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas" + File.separator + mensagem.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.linearMusicaChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas" + File.separator + mensagem.getNomeDocumento());

                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.linearAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios" + File.separator + mensagem.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.txtViewAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios" + File.separator + mensagem.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.imgViewAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios" + File.separator + mensagem.getNomeDocumento());
                if (file.exists()) {
                    abrirArquivo(mensagem, "audio");
                } else {
                    File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios");
                    baixarArquivo(mensagem, caminhoDestino);
                }
            }
        });

        holder.imgViewMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("mensagem", mensagem);
                context.startActivity(intent);
            }
        });

        holder.imgButtonExpandirVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("mensagem", mensagem);
                context.startActivity(intent);
            }
        });

        //Eventos de clique longo.
        holder.imgViewMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //ToastCustomizado.toastCustomizadoCurto("Long",context);
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.txtViewMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.imgViewGifMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.imgButtonExpandirVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.imgViewVideoMensagem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.imgViewMusicaChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.txtViewMusicaChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.imgViewAudioChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.txtViewAudioChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.imgViewDocumentoChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });

        holder.txtViewNomeDocumentoChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mostrarOpcoes(view, mensagem, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMensagem.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem, txtViewDataMensagem, txtViewNomeDocumentoChat,
                txtViewMusicaChat, txtViewAudioChat, txtViewDuracaoMusicaChat,
                txtViewDuracaoAudioChat, txtViewDataTrocaMensagens;
        private ImageView imgViewMensagem, imgViewGifMensagem, imgViewDocumentoChat,
                imgViewMusicaChat, imgViewAudioChat, imgViewVideoMensagem;
        private ImageButton imgButtonExpandirVideo;
        private LinearLayout linearDocumentoChat, linearMusicaChat, linearAudioChat;
        private ConstraintLayout constraintThumbVideo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

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
        }
    }

    private void mostrarOpcoes(View v, Mensagem mensagem, int position) {
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

        LinearLayout uploadLinearLayout = mBottomSheetDialog.findViewById(R.id.uploadLinearLayout);
        LinearLayout deleteForMeLayout = mBottomSheetDialog.findViewById(R.id.deleteForMeLayout);
        LinearLayout deleteForAllLayout = mBottomSheetDialog.findViewById(R.id.deleteForAllLayout);

        if (!idUsuarioLogado.equals(mensagem.getIdRemetente())) {
            deleteForMeLayout.setVisibility(View.GONE);
            deleteForAllLayout.setVisibility(View.GONE);
        } else {
            deleteForMeLayout.setVisibility(View.VISIBLE);
            deleteForAllLayout.setVisibility(View.VISIBLE);
        }

        if (mensagem.getTipoMensagem().equals("texto")
                || mensagem.getTipoMensagem().equals("gif")) {
            uploadLinearLayout.setVisibility(View.GONE);
        } else {
            uploadLinearLayout.setVisibility(View.VISIBLE);
        }

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
            final int recebidoPosition = position;

            deleteMessageForMeRef = firebaseRef.child("conversas")
                    .child(idUsuarioLogado).child(mensagem.getIdDestinatario());

            contadorMessageForMeRef = firebaseRef.child("contadorMensagens")
                    .child(idUsuarioLogado).child(mensagem.getIdDestinatario());


            deleteMessageForMeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Mensagem mensagemSelecionada = snapshot1.getValue(Mensagem.class);
                        if (mensagemSelecionada != null) {
                            if (mensagemSelecionada.getConteudoMensagem()
                                    .equals(mensagem.getConteudoMensagem())
                                    && mensagemSelecionada.getDataMensagemCompleta().equals(mensagem.getDataMensagemCompleta())) {
                                String idConversa = snapshot1.getKey();
                                deleteMessageForMeRef.child(idConversa).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                    if (recebidoPosition < listaMensagem.size()) {
                                                        listaMensagem.remove(recebidoPosition);
                                                        notifyItemRemoved(recebidoPosition);
                                                        notifyItemRangeChanged(recebidoPosition, getItemCount());
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
                        }
                    }
                    deleteMessageForMeRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if (!mensagem.getTipoMensagem().equals("texto")) {
                if (excluirLocalmente.equals("sim")) {
                    excluirArquivoLocal(mensagem);
                }
            }

        } catch (Exception ex) {
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

        //ToastCustomizado.toastCustomizado("Id da pasta " + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + nomePasta + File.separator + mensagem.getNomeDocumento(), context);

        try{
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
        }catch (Exception ex){
            Log.i("App", "Exception while deleting file " + ex.getMessage());
        }
    }


    private void deleteMessageForAll(Mensagem mensagem, int position, String excluirLocalmente) {
        try {
            final int recebidoPosition = position;

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

            deleteMessageForMeRef = firebaseRef.child("conversas")
                    .child(idUsuarioLogado).child(mensagem.getIdDestinatario());

            contadorMessageForMeRef = firebaseRef.child("contadorMensagens")
                    .child(idUsuarioLogado).child(mensagem.getIdDestinatario());

            deleteMessageReceiverRef = firebaseRef.child("conversas")
                    .child(mensagem.getIdDestinatario()).child(idUsuarioLogado);

            contadorMessageReceiverRef = firebaseRef.child("contadorMensagens")
                    .child(mensagem.getIdDestinatario()).child(idUsuarioLogado);

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

            //Removendo primeiro para o próprio usuário
            deleteMessageForMeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Mensagem mensagemSelecionada = snapshot1.getValue(Mensagem.class);
                        if (mensagemSelecionada != null) {
                            if (mensagemSelecionada.getConteudoMensagem()
                                    .equals(mensagem.getConteudoMensagem())
                                    && mensagemSelecionada.getDataMensagemCompleta().equals(mensagem.getDataMensagemCompleta())) {
                                String idConversa = snapshot1.getKey();
                                deleteMessageForMeRef.child(idConversa).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        }
                    }
                    deleteMessageForMeRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Removendo também para o usuário destinatário
            deleteMessageReceiverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Mensagem mensagemSelecionada = snapshot1.getValue(Mensagem.class);
                        if (mensagemSelecionada != null) {
                            if (mensagemSelecionada.getConteudoMensagem()
                                    .equals(mensagem.getConteudoMensagem())
                                    && mensagemSelecionada.getDataMensagemCompleta().equals(mensagem.getDataMensagemCompleta())) {
                                String idConversa = snapshot1.getKey();
                                deleteMessageReceiverRef.child(idConversa).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            }
                        }
                    }
                    deleteMessageReceiverRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //última parte da exclusão
            if (!mensagem.getTipoMensagem().equals("texto")) {
                if (excluirLocalmente.equals("sim")) {
                    excluirArquivoLocal(mensagem);
                }
            }

            //-------------**versão anterior(caso ocorra algum erro, volte para essa lógica)
            /*
            if (recebidoPosition < listaMensagem.size()) {
                listaMensagem.remove(recebidoPosition);
                notifyItemRemoved(recebidoPosition);
                notifyItemRangeChanged(recebidoPosition, getItemCount());
            }
            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
             */

            //Mudado para verificar se muda algo
            if (position < listaMensagem.size()) {
                listaMensagem.remove(position);
                notifyItemRemoved(position);
            }
            //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 20/12/2022 após ver o erro de exclusão,
            //falta verificar, se for por isso arrumar nas outras opções.

        } catch (Exception ex) {
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

                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "imagens");
                baixarArquivo(mensagem, caminhoDestino);
                ToastCustomizado.toastCustomizado("Download com sucesso", context);
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

                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "videos");
                baixarArquivo(mensagem, caminhoDestino);
                ToastCustomizado.toastCustomizado("Download com sucesso", context);
                break;
            }
            case "musica": {
                ToastCustomizado.toastCustomizadoCurto("Musica", context);

                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "musicas");
                baixarArquivo(mensagem, caminhoDestino);
                ToastCustomizado.toastCustomizado("Download com sucesso", context);
                break;
            }
            case "audio": {
                ToastCustomizado.toastCustomizadoCurto("Audio", context);

                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "audios");
                baixarArquivo(mensagem, caminhoDestino);
                ToastCustomizado.toastCustomizado("Download com sucesso", context);
                break;
            }
            case "documento": {
                ToastCustomizado.toastCustomizadoCurto("Documento", context);

                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + "documentos");
                baixarArquivo(mensagem, caminhoDestino);
                ToastCustomizado.toastCustomizado("Download com sucesso", context);
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

    public void adicionarItem(Mensagem novaMensagem) {
        listaMensagem.add(novaMensagem);
        notifyItemRemoved(listaMensagem.size() - 1);
        notifyItemInserted(listaMensagem.size() - 1);

        /*
        contadorMensagemRef = firebaseRef.child("contadorMensagens")
                .child(idUsuarioLogado).child(novaMensagem.getIdDestinatario());

        contadorMensagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem contadorMensagem = snapshot.getValue(Mensagem.class);
                    if (listaMensagem.get(listaMensagem.size() - 1).getIdRemetente().equals(idUsuarioLogado)
                            && contadorMensagem.getTotalMensagens() == listaMensagem.size() - 1) {
                        stringTeste = "sim";
                    }else{
                        stringTeste = null;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
         */

        /////////////////////////
        /*
        if (listaMensagem.get(listaMensagem.size() - 1).getIdRemetente().equals(idUsuarioLogado)) {
            stringTeste = "sim";
            //Dar um jeito de verificar se o foco é na última mensagem
        }else{
            stringTeste = null;
        }

         */
        //notifyItemChanged(listaMensagem.size(), getItemCount());

        //Tester lógica a baixo e alterar caso tenha chance de dar certo.
        /*
        ConversaActivity conversaActivity = new ConversaActivity();
        if (listaMensagem.get(getItemCount() - 1).getIdRemetente()
                .equals(idUsuarioLogado)) {
            conversaActivity.scrollToLast(getItemCount() - 1);
        }
         */
    }

    public void atualizarLista(Mensagem novaMensagem) {
        notifyItemChanged(listaMensagem.size() - 1, getItemCount());
    }

    // public boolean verificaFoco(){
    // }
}
