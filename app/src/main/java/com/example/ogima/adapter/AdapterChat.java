package com.example.ogima.adapter;

import static com.google.android.material.badge.BadgeUtils.attachBadgeDrawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Modifier;
import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private List<Usuario> listaChat;
    private Context context;

    public AdapterChat(List<Usuario> listChat, Context c) {
        this.context = c;
        this.listaChat = listChat;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Usuario usuario = listaChat.get(position);

        Glide.with(context)
                .load(usuario.getMinhaFoto())
                .placeholder(android.R.color.transparent)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .circleCrop()
                .encodeQuality(100)
                .into(holder.imgViewFotoPerfilChat);

        holder.txtViewNomePerfilChat.setText(usuario.getNomeUsuario());
        //holder.txtViewLastMensagemChat.setText("Iaew brow, como que vai, tudo de boa contigo?");
        DatabaseReference verificaContatoRef = firebaseRef.child("contatos")
                .child(idUsuarioLogado).child(usuario.getIdUsuario());

        verificaContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Contatos contatoInfo = snapshot.getValue(Contatos.class);
                    holder.txtViewLastMensagemChat.setText("Nível amizade: " + contatoInfo.getNivelAmizade());
                    verificaContatoRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.txtViewHoraMensagem.setText("20:17");

        /*
        //@Limitador de exibição do número de mensagens caso precise.
        int numeroMensagens = Integer.parseInt(holder.btnNumeroMensagem.getText().toString());

        if (numeroMensagens >= 999) {
            holder.btnNumeroMensagem.setText("999+");
        }
         */

        /*
        //@Badge - crachá (indicador numérico)
        holder.btnNumeroMensagem.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onGlobalLayout() {
                BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
                badgeDrawable.setNumber(1000);
                badgeDrawable.setBackgroundColor(Color.parseColor("#0000ff"));
                badgeDrawable.setVerticalOffset(20);
                badgeDrawable.setHorizontalOffset(15);
                badgeDrawable.setMaxCharacterCount(999);
                attachBadgeDrawable(badgeDrawable, holder.btnNumeroMensagem, null);
                holder.btnNumeroMensagem.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
         */

    }

    @Override
    public int getItemCount() {
        return listaChat.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPerfilChat;
        private TextView txtViewNomePerfilChat, txtViewLastMensagemChat,
                txtViewHoraMensagem;
        private Button btnNumeroMensagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoPerfilChat = itemView.findViewById(R.id.imgViewFotoPerfilChat);
            txtViewNomePerfilChat = itemView.findViewById(R.id.txtViewNomePerfilChat);
            txtViewLastMensagemChat = itemView.findViewById(R.id.txtViewLastMensagemChat);
            txtViewHoraMensagem = itemView.findViewById(R.id.txtViewHoraMensagem);
            btnNumeroMensagem = itemView.findViewById(R.id.btnNumeroMensagem);
        }
    }
}
