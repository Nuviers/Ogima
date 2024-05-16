package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.DetalhesGrupoActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GroupDiffCallback;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterGruposPublicosDiff extends RecyclerView.Adapter<AdapterGruposPublicosDiff.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Grupo> listaGrupos;
    private List<String> listaTopicosFiltrados;

    private DatabaseReference salvarAvisoRef;
    private String idConversaGrupo;

    public AdapterGruposPublicosDiff(Context c, List<Grupo> listGrupos, List<String> listTopicos) {
        this.context = c;
        //Essencial sempre fazer o new ArrayList<>(); na lista recebida
        //no construtor do adapter caso eu esteja usando o
        //DiffUtilCallback, sempre se lembre também do equals no model que tem que ser
        //adicionado também. E a ordenação da lista é sempre depois da operação,
        //adição, atualização, remoção.
        //e sempre antes das notificações.
        this.listaGrupos = listGrupos = new ArrayList<>();
        this.listaTopicosFiltrados = listTopicos;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    public void updateGroupPublicList(List<Grupo> listaGruposAtualizada) {
        GroupDiffCallback diffCallback = new GroupDiffCallback(listaGrupos, listaGruposAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaGrupos.clear();
        listaGrupos.addAll(listaGruposAtualizada);
        diffResult.dispatchUpdatesTo(this);

        if (listaGruposAtualizada != null && listaGruposAtualizada.size() > 0) {
            //ToastCustomizado.toastCustomizadoCurto("Tamanho: " + listaGruposAtualizada.size(), context);
            for (Grupo grupoExibicao : listaGruposAtualizada) {
                //ToastCustomizado.toastCustomizadoCurto("Nome: " + grupoExibicao.getNomeGrupo(), context);
            }
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grupo_publico, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Grupo grupo = listaGrupos.get(position);

        if (grupo.getParticipantes() != null
                && grupo.getParticipantes().size() > 0 && grupo.getParticipantes().contains(idUsuarioLogado)) {
            holder.btnEntrarGrupoPublico.setVisibility(View.GONE);
        } else {
            holder.btnEntrarGrupoPublico.setVisibility(View.VISIBLE);
        }

        holder.btnEntrarGrupoPublico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retornarGrupoBloqueado(grupo, holder.btnEntrarGrupoPublico);
            }
        });

        configuraTopicosGrupo(grupo, holder.linearLayoutTopicosGrupoPubico);

        if (listaTopicosFiltrados != null && listaTopicosFiltrados.size() > 0) {
            Float porcentagem = verificaCompatibilidade(grupo);
            String porcentagemFormatada = String.format("%.2f", porcentagem);
            if (porcentagemFormatada != null && !porcentagemFormatada.isEmpty()) {
                holder.txtViewCompGrupo.setVisibility(View.VISIBLE);
                holder.txtViewCompGrupo.setText("Compatibilidade: " + porcentagemFormatada + "%");
            }
        } else {
            holder.txtViewCompGrupo.setVisibility(View.GONE);
        }

        VerificaEpilpesia.verificarEpilpesiaSelecionadoGrupo(context, grupo,
                holder.imgViewFotoGrupoPublico);

        holder.txtViewNomeGrupoPublico.setText(grupo.getNomeGrupo());
        holder.txtViewDescricaoGrupoPublico.setText(grupo.getDescricaoGrupo());
        holder.txtViewNrPartGrupoPublico.setText("" + grupo.getParticipantes().size());
    }

    private float verificaCompatibilidade(Grupo grupo) {
        // Criar uma cópia da lista de hobbies selecionados para evitar modificá-la
        List<String> hobbiesSelecionadosCopia = new ArrayList<>(listaTopicosFiltrados);

        // Calcular a interseção entre as duas listas de hobbies
        hobbiesSelecionadosCopia.retainAll(grupo.getTopicos());

        // Calcular a porcentagem de compatibilidade
        return ((float) hobbiesSelecionadosCopia.size() / listaTopicosFiltrados.size()) * 100;
    }

    @Override
    public int getItemCount() {
        return listaGrupos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoGrupoPublico;
        private TextView txtViewNomeGrupoPublico, txtViewDescricaoGrupoPublico, txtViewNrPartGrupoPublico;
        private Button btnEntrarGrupoPublico;
        private LinearLayout linearLayoutTopicosGrupoPubico;
        private TextView txtViewCompGrupo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoGrupoPublico = itemView.findViewById(R.id.imgViewFotoGrupoPublico);
            txtViewNomeGrupoPublico = itemView.findViewById(R.id.txtViewNomeGrupoPublico);
            txtViewDescricaoGrupoPublico = itemView.findViewById(R.id.txtViewDescricaoGrupoPublico);
            btnEntrarGrupoPublico = itemView.findViewById(R.id.btnEntrarGrupoPublico);
            linearLayoutTopicosGrupoPubico = itemView.findViewById(R.id.linearLayoutTopicosGrupoPubico);
            txtViewCompGrupo = itemView.findViewById(R.id.txtViewCompGrupo);
            txtViewNrPartGrupoPublico = itemView.findViewById(R.id.txtViewNrPartGrupoPublico);
        }
    }

    private void irParaTelaInicial() {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    private void retornarGrupoBloqueado(Grupo grupo, Button btnEntrarGrupo) {
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdGruposBloqueados() != null
                        && usuarioAtual.getIdGruposBloqueados().size() > 0 && usuarioAtual.getIdGruposBloqueados().contains(grupo.getIdGrupo())) {
                    ToastCustomizado.toastCustomizado("Não é possível entrar nesse grupo, esse grupo foi bloqueado por você anteriormente!", context);
                } else {
                    entrarNoGrupo(grupo, btnEntrarGrupo);
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void entrarNoGrupo(Grupo grupoRecebido, Button btnEntrarGrupo) {

        DatabaseReference salvarParticipanteRef = firebaseRef.child("grupos")
                .child(grupoRecebido.getIdGrupo()).child("participantes");
        ArrayList<String> listaParticipantes = new ArrayList<>();

        FirebaseRecuperarUsuario.recuperaGrupo(grupoRecebido.getIdGrupo(), new FirebaseRecuperarUsuario.RecuperaGrupoCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {

                if (grupoAtual.getGrupoPublico() != null && grupoAtual.getGrupoPublico().equals(true)) {
                    if (grupoAtual.getParticipantes() != null && grupoAtual.getParticipantes().size() > 0
                            && grupoAtual.getParticipantes().contains(idUsuarioLogado)) {
                        //Usuário atual já é participante.
                        ToastCustomizado.toastCustomizadoCurto("Você já participa desse grupo.", context);
                    } else {
                        //Usuário atual não é participante.
                        listaParticipantes.addAll(grupoAtual.getParticipantes());
                        listaParticipantes.add(idUsuarioLogado);
                        salvarParticipanteRef.setValue(listaParticipantes).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                btnEntrarGrupo.setVisibility(View.GONE);
                                salvarAviso(grupoAtual);
                                SnackbarUtils.showSnackbar(btnEntrarGrupo, "Agora você é participante do grupo: " + grupoAtual.getNomeGrupo());
                                //ToastCustomizado.toastCustomizado("Agora você é participante do grupo: " + grupo.getNomeGrupo(), context);
                            }
                        });
                    }
                } else {
                    btnEntrarGrupo.setVisibility(View.GONE);
                    SnackbarUtils.showSnackbar(btnEntrarGrupo, "Esse grupo não é mais público, não é possível entrar sem convite.");
                }
            }

            @Override
            public void onSemDado() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void salvarAviso(Grupo grupo) {

        salvarAvisoRef = firebaseRef.child("conversas");
        idConversaGrupo = salvarAvisoRef.push().getKey();

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                String conteudoAviso = nomeUsuarioAjustado + " entrou no grupo";

                HashMap<String, Object> dadosMensagem = new HashMap<>();
                dadosMensagem.put("idConversa", idConversaGrupo);
                dadosMensagem.put("exibirAviso", true);
                dadosMensagem.put("conteudoMensagem", conteudoAviso);

                salvarAvisoRef = salvarAvisoRef.child(grupo.getIdGrupo())
                        .child(idConversaGrupo);

                salvarAvisoRef.setValue(dadosMensagem);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void configuraTopicosGrupo(Grupo grupo, LinearLayout linearLayoutTopicosGrupoPubico) {
        // Limpa o layout antes de adicionar os chips
        linearLayoutTopicosGrupoPubico.removeAllViews();

        // Adiciona um chip para cada hobby
        for (String hobby : grupo.getTopicos()) {
            Chip chip = new Chip(linearLayoutTopicosGrupoPubico.getContext());
            chip.setText(hobby);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.DKGRAY));
            chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
            chip.setLayoutParams(params);
            chip.setClickable(false);
            linearLayoutTopicosGrupoPubico.addView(chip);
        }
    }
}