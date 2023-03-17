
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

public class AdapterChatGrupo extends RecyclerView.Adapter<AdapterChatGrupo.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Grupo> listaGrupos;

    public AdapterChatGrupo(Context c, List<Grupo> listGrupos) {
        this.context = c;
        this.listaGrupos = listGrupos;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Grupo grupo = listaGrupos.get(position);

        GlideCustomizado.montarGlide(context, grupo.getFotoGrupo(), holder.imgViewFotoPerfilChat,
                android.R.color.transparent);
        holder.txtViewNomePerfilChat.setText(grupo.getNomeGrupo());
        holder.txtViewLastMensagemChat.setText(grupo.getDescricaoGrupo());
    }

    @Override
    public int getItemCount() {
        return listaGrupos.size();
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
