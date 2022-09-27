package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
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
            holder.txtViewMensagem.setText(mensagem.getConteudoMensagem());
        } else if (mensagem.getTipoMensagem().equals("imagem")) {
            holder.imgViewMensagem.setVisibility(View.VISIBLE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            GlideCustomizado.montarGlideFoto(context, mensagem.getConteudoMensagem(),
                    holder.imgViewMensagem, android.R.color.transparent);
        } else if (mensagem.getTipoMensagem().equals("gif")) {
            holder.imgViewMensagem.setVisibility(View.VISIBLE);
            holder.videoMensagem.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            DatabaseReference usuarioAtualRef = firebaseRef.child("usuarios")
                    .child(idUsuarioLogado);
            usuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        if (usuario.getEpilepsia().equals("Sim")) {
                            GlideCustomizado.montarGlideFotoEpilepsia(context, mensagem.getConteudoMensagem(),
                                    holder.imgViewMensagem, android.R.color.transparent);
                        } else {
                            GlideCustomizado.montarGlideFoto(context, mensagem.getConteudoMensagem(),
                                    holder.imgViewMensagem, android.R.color.transparent);
                        }
                    }
                    usuarioAtualRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            GlideCustomizado.montarGlideFoto(context, mensagem.getConteudoMensagem(),
                    holder.imgViewMensagem, android.R.color.transparent);
        } else if (mensagem.getTipoMensagem().equals("video")) {
            holder.videoMensagem.setVisibility(View.VISIBLE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
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
        }
    }

    @Override
    public int getItemCount() {
        return listaMensagem.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem;
        private ImageView imgViewMensagem;
        private StyledPlayerView videoMensagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtViewMensagem = itemView.findViewById(R.id.txtViewMensagem);
            imgViewMensagem = itemView.findViewById(R.id.imgViewMensagem);
            videoMensagem = itemView.findViewById(R.id.videoMensagem);
        }
    }
}
