package com.example.ogima.adapter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.FotoVideoExpandidoActivity;
import com.example.ogima.activity.PlayerMusicaChatActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.util.List;

public class AdapterChatRandom extends FirebaseRecyclerAdapter<Mensagem, AdapterChatRandom.ViewHolder> {

    private Context context;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private static final int LAYOUT_REMETENTE = 0;
    private static final int LAYOUT_DESTINATARIO = 1;
    private boolean statusEpilepsia = true;

    public AdapterChatRandom(Context c, @NonNull FirebaseRecyclerOptions<Mensagem> options) {
        super(options);
        this.context = c;
        idUsuarioLogado = UsuarioUtils.recuperarIdUserAtual();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagemType = getItem(position);

        if (mensagemType.getExibirAviso() != null) {
            return LAYOUT_DESTINATARIO;
        } else {
            if (idUsuarioLogado.equals(mensagemType.getIdRemetente())) {
                return LAYOUT_REMETENTE;
            }
        }
        return LAYOUT_DESTINATARIO;
    }


    @NonNull
    @Override
    public AdapterChatRandom.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;

        if (viewType == LAYOUT_REMETENTE) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);
        } else if (viewType == LAYOUT_DESTINATARIO) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }

        return new ViewHolder(item);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Mensagem mensagemAtual) {
        holder.linearLayoutAvisoGrupo.setVisibility(View.GONE);
        holder.linearLayoutMensagem.setVisibility(View.VISIBLE);
        holder.imgViewRemetenteGrupo.setVisibility(View.GONE);
        holder.txtViewNomeRemetenteGrupo.setVisibility(View.GONE);

        if (mensagemAtual.getTipoMensagem().equals("texto")) {
            holder.txtViewMensagem.setVisibility(View.VISIBLE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            holder.txtViewMensagem.setText(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(mensagemAtual.getConteudoMensagem()));
        }else if (mensagemAtual.getTipoMensagem().equals("gif")) {
            holder.imgViewGifMensagem.setVisibility(View.VISIBLE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            GlideCustomizado.loadUrl(context,
                    mensagemAtual.getConteudoMensagem(),
                    holder.imgViewGifMensagem, android.R.color.transparent,
                    GlideCustomizado.CENTER_INSIDE, false, isStatusEpilepsia());
        }else if (mensagemAtual.getTipoMensagem().equals("audio")) {
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

        holder.txtViewDataMensagem.setText(mensagemAtual.getDataMensagem());

        holder.linearAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.escutarAudio(mensagemAtual);
            }
        });

        holder.txtViewAudioChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.escutarAudio(mensagemAtual);
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
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem, txtViewDataMensagem, txtViewNomeDocumentoChat,
                txtViewMusicaChat, txtViewAudioChat, txtViewDuracaoMusicaChat,
                txtViewDuracaoAudioChat, txtViewDataTrocaMensagens, txtViewNomeRemetenteGrupo,
                txtViewAvisoGrupo;
        private ImageView imgViewMensagem, imgViewGifMensagem, imgViewDocumentoChat,
                imgViewMusicaChat, imgViewAudioChat, imgViewVideoMensagem, imgViewRemetenteGrupo;
        private ImageButton imgButtonExpandirVideo;
        private LinearLayout linearDocumentoChat, linearMusicaChat, linearAudioChat,
                linearLayoutMensagem, linearLayoutAvisoGrupo;
        private ConstraintLayout constraintThumbVideo;

        public ViewHolder(@NonNull View itemView) {
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

            linearLayoutAvisoGrupo = itemView.findViewById(R.id.linearLayoutAvisoGrupo);
            txtViewAvisoGrupo = itemView.findViewById(R.id.txtViewAvisoGrupo);
        }

        public void escutarAudio(Mensagem mensagemAtual){
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios" + File.separator + mensagemAtual.getNomeDocumento());
            if (file.exists()) {
                abrirArquivo(mensagemAtual, "audio");
            } else {
                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios");
                baixarArquivo(mensagemAtual, caminhoDestino);
                abrirArquivo(mensagemAtual, "audio");
            }
        }
    }

    private void abrirArquivo(Mensagem mensagem, String keyName) {
        Intent intent = new Intent(context, PlayerMusicaChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(keyName, mensagem);
        context.startActivity(intent);
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

    @Override
    public void updateOptions(@NonNull FirebaseRecyclerOptions<Mensagem> options) {
        super.updateOptions(options);
    }
}
