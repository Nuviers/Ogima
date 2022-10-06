package com.example.ogima.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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

    public void pausePlayer(){
        exoPlayerMensagem.setPlayWhenReady(false);
        exoPlayerMensagem.getPlaybackState();
        ToastCustomizado.toastCustomizadoCurto("Pause", context);
    }
    public void startPlayer(){
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
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = listaMensagem.get(position);

        if (mensagem.getTipoMensagem().equals("texto")) {
            holder.txtViewMensagem.setVisibility(View.VISIBLE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.txtViewMensagem.setText(mensagem.getConteudoMensagem());
        } else if (mensagem.getTipoMensagem().equals("imagem")) {
            holder.imgViewMensagem.setVisibility(View.VISIBLE);
            holder.videoMensagem.setVisibility(View.GONE);
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
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            exoPlayerMensagem = new ExoPlayer.Builder(context).build();
            holder.videoMensagem.setPlayer(exoPlayerMensagem);
            MediaItem mediaItem =  new MediaItem.Builder()
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
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.txtViewMusicaChat.setText(mensagem.getNomeDocumento());
        } else if (mensagem.getTipoMensagem().equals("audio")) {
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.VISIBLE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.videoMensagem.setVisibility(View.GONE);
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
    }

    @Override
    public int getItemCount() {
        return listaMensagem.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem, txtViewDataMensagem, txtViewNomeDocumentoChat,
                txtViewMusicaChat, txtViewAudioChat;
        private ImageView imgViewMensagem, imgViewGifMensagem, imgViewDocumentoChat,
                imgViewMusicaChat, imgViewAudioChat;
        private StyledPlayerView videoMensagem;
        private LinearLayout linearDocumentoChat, linearMusicaChat, linearAudioChat;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtViewMensagem = itemView.findViewById(R.id.txtViewMensagem);
            txtViewDataMensagem = itemView.findViewById(R.id.txtViewDataMensagem);
            imgViewMensagem = itemView.findViewById(R.id.imgViewMensagem);
            imgViewGifMensagem = itemView.findViewById(R.id.imgViewGifMensagem);
            videoMensagem = itemView.findViewById(R.id.videoMensagem);

            linearDocumentoChat = itemView.findViewById(R.id.linearDocumentoChat);
            imgViewDocumentoChat = itemView.findViewById(R.id.imgViewDocumentoChat);
            txtViewNomeDocumentoChat = itemView.findViewById(R.id.txtViewNomeDocumentoChat);

            linearMusicaChat = itemView.findViewById(R.id.linearMusicaChat);
            imgViewMusicaChat = itemView.findViewById(R.id.imgViewMusicaChat);
            txtViewMusicaChat = itemView.findViewById(R.id.txtViewMusicaChat);

            linearAudioChat = itemView.findViewById(R.id.linearAudioChat);
            imgViewAudioChat = itemView.findViewById(R.id.imgViewAudioChat);
            txtViewAudioChat = itemView.findViewById(R.id.txtViewAudioChat);
        }
    }

    private void mostrarOpcoes(View v){
        Context context=v.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate (R.layout.bottom_sheet_dialog_opcoes_mensagem, null);
        final Dialog mBottomSheetDialog = new Dialog (context);
        mBottomSheetDialog.setContentView (view);
        mBottomSheetDialog.setCancelable (true);
        mBottomSheetDialog.getWindow ().setLayout (LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow ().setGravity (Gravity.BOTTOM);

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
                ToastCustomizado.toastCustomizadoCurto("Clicado teste",context);
            }
        });

        mBottomSheetDialog.show();
    }
}
