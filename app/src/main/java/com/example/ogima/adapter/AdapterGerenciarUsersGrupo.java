package com.example.ogima.adapter;

import android.content.Context;
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
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DadosUserPadrao;
import com.example.ogima.helper.ToastCustomizado;
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

public class AdapterGerenciarUsersGrupo extends RecyclerView.Adapter<AdapterGerenciarUsersGrupo.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private List<Usuario> listaUsuario;
    private Context context;
    private TextView txtViewParticipantes;
    private Button btnEnviarParticipantes;
    private HashSet<String> participantesSelecionados = new HashSet<>();
    private String idNovoFundador;
    private int contadorSelecionado = 0;
    private int limiteSelecao;
    private String tipoOperacao;
    // private HashSet<String> hashSetIdsUsuarios;
    //private Boolean somenteIds;

    public AdapterGerenciarUsersGrupo(List<Usuario> listUsuario, Context c, TextView txtViewSelecaoParticipantes, Button btnProximaEtapaGrupo, int limiteSelecaoUsers, String funcaoEscolhida) {
        this.context = c;
        this.listaUsuario = listUsuario;
        this.txtViewParticipantes = txtViewSelecaoParticipantes;
        this.btnEnviarParticipantes = btnProximaEtapaGrupo;
        this.limiteSelecao = limiteSelecaoUsers;
        this.tipoOperacao = funcaoEscolhida;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_usuarios_grupo,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Collections.sort(listaUsuario, new Comparator<Usuario>() {
            @Override
            public int compare(Usuario usuario, Usuario t1) {
                return usuario.getNomeUsuarioPesquisa().compareToIgnoreCase(t1.getNomeUsuarioPesquisa());
            }
        });

        Usuario usuario = listaUsuario.get(position);

        if (usuario.getIdUsuario().equals(idUsuarioLogado)) {
            holder.linearLayoutParticipantesGrupo.setVisibility(View.GONE);
        } else {
            holder.linearLayoutParticipantesGrupo.setVisibility(View.VISIBLE);
        }

        DadosUserPadrao.preencherDadosUser(context, usuario, holder.txtViewNomePerfilChat, holder.imgViewFotoPerfilChat);

        holder.txtViewNomePerfilChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (tipoOperacao.equals("despromover")) {
                    if (participantesSelecionados.contains(usuario.getIdUsuario())) {
                        // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                        selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, false);
                    } else {
                        // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                        selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, true);
                    }
                } else {
                    //Verifica se o usuário atingiu o limite de seleção e se está tentando
                    //ultrapassar o limite.
                    if (contadorSelecionado == limiteSelecao && !participantesSelecionados.contains(usuario.getIdUsuario())) {
                        ToastCustomizado.toastCustomizadoCurto("Limite de usuários selecionados atingido", context);
                    } else {
                        if (participantesSelecionados.contains(usuario.getIdUsuario())) {
                            // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                            selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, false);
                        } else {
                            // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                            selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, true);
                        }
                    }
                }
            }
        });

        holder.imgViewFotoPerfilChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verifica se o usuário atingiu o limite de seleção e se está tentando
                //ultrapassar o limite.
                if (tipoOperacao.equals("despromover")) {
                    if (participantesSelecionados.contains(usuario.getIdUsuario())) {
                        // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                        selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, false);
                    } else {
                        // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                        selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, true);
                    }
                } else {
                    //Verifica se o usuário atingiu o limite de seleção e se está tentando
                    //ultrapassar o limite.
                    if (contadorSelecionado == limiteSelecao && !participantesSelecionados.contains(usuario.getIdUsuario())) {
                        ToastCustomizado.toastCustomizadoCurto("Limite de usuários selecionados atingido", context);
                    } else {
                        if (participantesSelecionados.contains(usuario.getIdUsuario())) {
                            // Se o usuário já está selecionado, remova-o da lista de usuários selecionados e diminua o contador
                            selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, false);
                        } else {
                            // Caso contrário, adicione-o à lista de usuários selecionados e aumente o contador
                            selecionarUsuario(usuario.getIdUsuario(), holder.linearLayoutParticipantesGrupo, true);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuario.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPerfilChat;
        private TextView txtViewNomePerfilChat;
        private LinearLayout linearLayoutParticipantesGrupo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoPerfilChat = itemView.findViewById(R.id.imgViewFotoPerfilChat);
            txtViewNomePerfilChat = itemView.findViewById(R.id.txtViewNomePerfilChat);
            linearLayoutParticipantesGrupo = itemView.findViewById(R.id.linearLayoutParticipantesGrupo);
        }
    }

    //Retorna para a ShareMessageActivity a lista com os usuários selecionados.
    public HashSet<String> participantesSelecionados() {
        return participantesSelecionados;
    }

    public String retornarIdNovoFundador() {
        return idNovoFundador;
    }

    private void selecionarUsuario(String idUsuarioSelecionado, LinearLayout linearLayout, Boolean marcarUsuario) {

        String hexColor = "#6495ED"; // azul claro
        int greenColor = Color.parseColor(hexColor);

        if (marcarUsuario) {
            //Seleciona
            participantesSelecionados.add(idUsuarioSelecionado);
            contadorSelecionado++;
            linearLayout.setBackgroundColor(greenColor);
        } else {
            //Desmarca
            participantesSelecionados.remove(idUsuarioSelecionado);
            contadorSelecionado--;
            linearLayout.setBackgroundColor(Color.WHITE);
        }

        if (tipoOperacao.equals("despromover")) {
            txtViewParticipantes.setText("" + contadorSelecionado);
        } else {
            txtViewParticipantes.setText("" + contadorSelecionado + "/" + limiteSelecao);
        }

        if (tipoOperacao.equals("novoFundador")) {
            idNovoFundador = idUsuarioSelecionado;
        }
    }
}
