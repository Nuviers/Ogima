
package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.ConversaGrupoActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AdapterChatGrupo extends FirebaseRecyclerAdapter<Grupo, AdapterChatGrupo.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;

    public AdapterChatGrupo(Context c, @NonNull FirebaseRecyclerOptions<Grupo> options) {
        super(options);
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Grupo model) {


        GlideCustomizado.montarGlide(context, model.getFotoGrupo(), holder.imgViewFotoPerfilChat,
                android.R.color.transparent);
        holder.txtViewNomePerfilChat.setText(model.getNomeGrupo());
        holder.txtViewLastMensagemChat.setText(model.getDescricaoGrupo());

        /*
        //Correto
        if (!model.getParticipantes().contains(idUsuarioLogado)) {
            //
         */

        /*
        if (model.getParticipantes().contains(idUsuarioLogado)) {
            holder.linearLayoutChat.setVisibility(View.GONE);
            holder.linearLayoutChat.getLayoutParams().height = 0;
            holder.txtViewNomePerfilChat.setVisibility(View.GONE);
            holder.txtViewLastMensagemChat.setVisibility(View.GONE);
        }else{

            holder.linearLayoutChat.setVisibility(View.VISIBLE);
            holder.linearLayoutChat.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT; // define a altura como WRAP_CONTENT (altura padrão)
            holder.txtViewNomePerfilChat.setVisibility(View.VISIBLE);
            holder.txtViewLastMensagemChat.setVisibility(View.VISIBLE);

            GlideCustomizado.montarGlide(context, model.getFotoGrupo(), holder.imgViewFotoPerfilChat,
                    android.R.color.transparent);
            holder.txtViewNomePerfilChat.setText(model.getNomeGrupo());
            holder.txtViewLastMensagemChat.setText(model.getDescricaoGrupo());

        }
         */
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat, parent, false);
        return new MyViewHolder(view);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPerfilChat;
        private TextView txtViewNomePerfilChat, txtViewLastMensagemChat,
                txtViewHoraMensagem;
        private Button btnNumeroMensagem;
        private LinearLayout linearLayoutChat;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoPerfilChat = itemView.findViewById(R.id.imgViewFotoPerfilChat);
            txtViewNomePerfilChat = itemView.findViewById(R.id.txtViewNomePerfilChat);
            txtViewLastMensagemChat = itemView.findViewById(R.id.txtViewLastMensagemChat);
            txtViewHoraMensagem = itemView.findViewById(R.id.txtViewHoraMensagem);
            btnNumeroMensagem = itemView.findViewById(R.id.btnNumeroMensagem);
            linearLayoutChat = itemView.findViewById(R.id.linearLayoutChat);
        }
    }

    private void abrirConversa(Grupo grupoSelecionado) {
        Intent intent = new Intent(context, ConversaGrupoActivity.class);
        intent.putExtra("grupo", grupoSelecionado);
        intent.putExtra("voltarChatFragment", "ChatInicioActivity.class");
        context.startActivity(intent);
    }
}
