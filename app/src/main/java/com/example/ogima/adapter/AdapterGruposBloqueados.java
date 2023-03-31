
package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GrupoBloqueadoDAO;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterGruposBloqueados extends RecyclerView.Adapter<AdapterGruposBloqueados.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Grupo> listaGrupos;
    private ArrayList<String> listaIdsBloqueados = new ArrayList<>();
    private GrupoBloqueadoDAO grupoBloqueadoDAO;
    private RemocaoGrupoBloqueadoListener remocaoGrupoBloqueadoListener;

    public AdapterGruposBloqueados(Context c, List<Grupo> listGrupos, RemocaoGrupoBloqueadoListener listener) {
        this.context = c;
        this.listaGrupos = listGrupos;
        this.remocaoGrupoBloqueadoListener = listener;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    public interface RemocaoGrupoBloqueadoListener {
        void onGrupoExcluido(Grupo grupoRemovido);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grupo_bloqueado, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Collections.sort(listaGrupos, new Comparator<Grupo>() {
            @Override
            public int compare(Grupo grupoOrdenadot1, Grupo grupoOrdenadot2) {
                return grupoOrdenadot1.getNomeGrupo().compareToIgnoreCase(grupoOrdenadot2.getNomeGrupo());
            }
        });

        Grupo grupo = listaGrupos.get(position);

        holder.txtViewLastMensagemChat.setVisibility(View.GONE);

        holder.btnNumeroMensagem.setText("Desbloquear grupo");

        DatabaseReference usuarioGruposBloqueadosRef = firebaseRef.child("usuarios")
                        .child(idUsuarioLogado).child("idGruposBloqueados");

        GlideCustomizado.montarGlide(context, grupo.getFotoGrupo(), holder.imgViewFotoPerfilChat,
                android.R.color.transparent);

        holder.txtViewNomePerfilChat.setText(grupo.getNomeGrupo());

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                listaIdsBloqueados.clear();

                if (usuarioAtual.getIdGruposBloqueados() != null && usuarioAtual.getIdGruposBloqueados().size() > 0) {
                    listaIdsBloqueados.addAll(usuarioAtual.getIdGruposBloqueados());
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });

        holder.btnNumeroMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               listaIdsBloqueados.remove(grupo.getIdGrupo());
                if (listaIdsBloqueados != null && listaIdsBloqueados.size() > 0) {
                    usuarioGruposBloqueadosRef.setValue(listaIdsBloqueados).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            remocaoGrupoBloqueadoListener.onGrupoExcluido(grupo);
                        }
                    });
                }else{
                    usuarioGruposBloqueadosRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            remocaoGrupoBloqueadoListener.onGrupoExcluido(grupo);
                            irParaTelaInicial();
                        }
                    });
                }
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

    private void irParaTelaInicial() {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
