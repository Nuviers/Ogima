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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

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
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent,false);
        }else if (viewType == LAYOUT_DESTINATARIO){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent,false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = listaMensagem.get(position);

        if (mensagem.getTipoMensagem().equals("texto")) {
            holder.txtViewMensagem.setVisibility(View.VISIBLE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.txtViewMensagem.setText(mensagem.getConteudoMensagem());
        } else if (mensagem.getTipoMensagem().equals("imagem")) {
            holder.imgViewMensagem.setVisibility(View.VISIBLE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            GlideCustomizado.montarGlideFoto(context, mensagem.getConteudoMensagem(),
                    holder.imgViewMensagem, android.R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return listaMensagem.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView txtViewMensagem;
        private ImageView imgViewMensagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtViewMensagem = itemView.findViewById(R.id.txtViewMensagem);
            imgViewMensagem = itemView.findViewById(R.id.imgViewMensagem);
        }
    }
}
