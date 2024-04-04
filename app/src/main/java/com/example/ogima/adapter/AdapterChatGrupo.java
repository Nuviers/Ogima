
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
import com.example.ogima.activity.GroupDetailsActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdapterChatGrupo extends RecyclerView.Adapter<AdapterChatGrupo.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Grupo> listaGrupos;
    public ValueEventListener valueEventListenerLastMsg;
    public DatabaseReference verificaUltimaMsgRef;
    private List<Mensagem> listaMensagens = new ArrayList<>();

    public AdapterChatGrupo(Context c, List<Grupo> listGrupos) {
        this.context = c;
        this.listaGrupos = listGrupos;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat_grupo, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Grupo grupo = listaGrupos.get(position);

        GlideCustomizado.montarGlide(context, grupo.getFotoGrupo(), holder.imgViewFotoPerfilChat,
                android.R.color.transparent);
        holder.txtViewNomePerfilChat.setText(grupo.getNomeGrupo());

        holder.txtViewLastMensagemChat.setText(grupo.getDescricaoGrupo());

        holder.imgViewFotoPerfilChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastCustomizado.toastCustomizadoCurto("Nome - " + grupo.getNomeGrupo(), context);
                abrirConversa(grupo);
            }
        });

        holder.txtViewNomePerfilChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirConversa(grupo);
            }
        });

        holder.txtViewLastMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirConversa(grupo);
            }
        });


        DatabaseReference contadorMensagensRef = firebaseRef.child("contadorMensagens")
                .child(grupo.getIdGrupo());

        contadorMensagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem contadorMensagem = snapshot.getValue(Mensagem.class);
                    holder.btnNumeroMensagem.setText("" + contadorMensagem.getTotalMensagens());
                }
                contadorMensagensRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listaMensagens.clear();

        verificaUltimaMsgRef = firebaseRef
                .child("conversas")
                .child(grupo.getIdGrupo());

        valueEventListenerLastMsg = verificaUltimaMsgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Mensagem mensagemCompleta = dataSnapshot.getValue(Mensagem.class);

                    if (mensagemCompleta.getExibirAviso() != null) {

                    } else {
                        listaMensagens.add(mensagemCompleta);

                        String tipoMidiaUltimaMensagem = listaMensagens.get(listaMensagens.size() - 1).getTipoMensagem();
                        if (!tipoMidiaUltimaMensagem.equals("texto")) {
                            holder.txtViewLastMensagemChat.setTextColor(Color.BLUE);
                            holder.txtViewLastMensagemChat.setText("Mídia - " + tipoMidiaUltimaMensagem);
                        } else {
                            holder.txtViewLastMensagemChat.setTextColor(Color.BLACK);
                            holder.txtViewLastMensagemChat.setText("" + listaMensagens.get(listaMensagens.size() - 1).getConteudoMensagem());
                        }
                        Date horarioUltimaMensagem = mensagemCompleta.getDataMensagemCompleta();
                        holder.txtViewHoraMensagem.setText("" + horarioUltimaMensagem.getHours() + ":" + horarioUltimaMensagem.getMinutes());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        //**Intent intent = new Intent(context, ConversaGrupoActivity.class);
        //**intent.putExtra("grupo", grupoSelecionado);
        //**intent.putExtra("voltarChatFragment", "ChatInicioActivity.class");
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.putExtra("idGrupo", grupoSelecionado.getIdGrupo());
        context.startActivity(intent);
    }
}
