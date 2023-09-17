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
import com.example.ogima.helper.DadosUserPadrao;
import com.example.ogima.helper.OrdenarUsuarioAlfabeticamente;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class AdapterParticipantesComunidade extends RecyclerView.Adapter<AdapterParticipantesComunidade.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private HashSet<String> listaParticipantes;
    private Comunidade comunidade;
    private static final int LAYOUT_ADM = 0;
    private static final int LAYOUT_PADRAO = 1;
    private Boolean exibirDetalhes;
    private List<Usuario> listaUsuariosParticipantes;

    public AdapterParticipantesComunidade(HashSet<String> hashSetParticipantes, Context c, Comunidade comunidadeDetalhes, Boolean isDetalhesComunidade, List<Usuario> listaUsuarios) {
        this.context = c;
        this.listaParticipantes = hashSetParticipantes;
        this.comunidade = comunidadeDetalhes;
        this.exibirDetalhes = isDetalhesComunidade;
        this.listaUsuariosParticipantes = listaUsuarios;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    public int getItemViewType(int position) {
        if (exibirDetalhes) {
            Usuario usuarioParticipante = listaUsuariosParticipantes.get(position);
            if (comunidade.getAdmsComunidade() != null) {
                if (comunidade.getAdmsComunidade().contains(usuarioParticipante.getIdUsuario())) {
                    return LAYOUT_ADM;
                }
            }
        }
        return LAYOUT_PADRAO;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        if (viewType == LAYOUT_ADM) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_adms_grupo, parent, false);
        } else if (viewType == LAYOUT_PADRAO) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_usuarios_grupo, parent, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        if (exibirDetalhes) {

            Collections.sort(listaUsuariosParticipantes, OrdenarUsuarioAlfabeticamente.comparadorAlfabetico());

            Usuario usuarioParticipante = listaUsuariosParticipantes.get(position);

            DadosUserPadrao.preencherDadosUser(context, usuarioParticipante, holder.txtViewNomePerfilChat, holder.imgViewFotoPerfilChat);
        }else{
            DatabaseReference usuarioRecebidoRef = firebaseRef.child("usuarios")
                    .child((String) listaParticipantes.toArray()[position]);

            usuarioRecebidoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        DadosUserPadrao.preencherDadosUser(context, usuario, holder.txtViewNomePerfilChat, holder.imgViewFotoPerfilChat);
                    }
                    usuarioRecebidoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (exibirDetalhes) {
            return listaUsuariosParticipantes.size();
        }else{
            return listaParticipantes.size();
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPerfilChat;
        private TextView txtViewNomePerfilChat;
        private LinearLayout linearLayoutParticipantesComunidade;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoPerfilChat = itemView.findViewById(R.id.imgViewFotoPerfilChat);
            txtViewNomePerfilChat = itemView.findViewById(R.id.txtViewNomePerfilChat);
            linearLayoutParticipantesComunidade = itemView.findViewById(R.id.linearLayoutParticipantesGrupo);
        }
    }
}
