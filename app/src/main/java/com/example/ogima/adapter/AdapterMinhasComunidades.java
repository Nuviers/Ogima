
package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.activity.ConversaGrupoActivity;
import com.example.ogima.activity.DetalhesComunidadeActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AdapterMinhasComunidades extends RecyclerView.Adapter<AdapterMinhasComunidades.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Comunidade> listaComunidades;

    public AdapterMinhasComunidades(Context c, List<Comunidade> listComunidades) {
        this.context = c;
        this.listaComunidades = listComunidades;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comunidade, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Collections.sort(listaComunidades, new Comparator<Comunidade>() {
            @Override
            public int compare(Comunidade u1, Comunidade u2) {
                return u1.getNomeComunidade().compareToIgnoreCase(u2.getNomeComunidade());
            }
        });

        Comunidade comunidade = listaComunidades.get(position);

        GlideCustomizado.montarGlide(context, comunidade.getFotoComunidade(), holder.imgViewFotoPerfilChat,
                android.R.color.transparent);

        holder.txtViewNomePerfilChat.setText(comunidade.getNomeComunidade());

        holder.txtViewLastMensagemChat.setText(comunidade.getDescricaoComunidade());

        holder.imgViewFotoPerfilChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToastCustomizado.toastCustomizadoCurto("Nome - " + comunidade.getNomeComunidade(), context);
                verDetalhesComunidade(view, comunidade, position);
            }
        });

        holder.txtViewNomePerfilChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verDetalhesComunidade(view, comunidade, position);
            }
        });

        holder.txtViewLastMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verDetalhesComunidade(view, comunidade, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaComunidades.size();
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

    private void verDetalhesComunidade(View view, Comunidade comunidade, int position){

        DatabaseReference verificaComunidadeRef = firebaseRef.child("comunidades")
                .child(comunidade.getIdComunidade());

        verificaComunidadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Comunidade ainda existe
                    Comunidade comunidadeAtual = snapshot.getValue(Comunidade.class);
                    Intent intent = new Intent(context, ComunidadePostagensActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("idComunidade", comunidadeAtual.getIdComunidade());
                    context.startActivity(intent);
                }else{
                    SnackbarUtils.showSnackbar(view, "Essa comunidade não existe mais");
                    listaComunidades.remove(comunidade);
                    notifyItemRemoved(position);
                }
                verificaComunidadeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
