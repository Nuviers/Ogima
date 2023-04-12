package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GroupDiffCallback;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterGruposPublicosDiff extends RecyclerView.Adapter<AdapterGruposPublicosDiff.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Grupo> listaGrupos;
    private List<String> listaTopicosFiltrados;

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
            //holder.btnEntrarGrupoPublico.setVisibility(View.GONE);
        } else {
            //holder.btnEntrarGrupoPublico.setVisibility(View.VISIBLE);
        }

        // Obtém o FlowLayout do layout do item de lista


        // Limpa o layout antes de adicionar os chips
        holder.linearLayoutTopicosGrupoPubico.removeAllViews();

        // Adiciona um chip para cada hobby
        for (String hobby : grupo.getTopicos()) {
            Chip chip = new Chip(holder.linearLayoutTopicosGrupoPubico.getContext());
            chip.setText(hobby);
            chip.setClickable(false);
            holder.linearLayoutTopicosGrupoPubico.addView(chip);
        }

        //preencherTopicos(grupo, holder.linearLayoutTopicosGrupoPubico);

        if (listaTopicosFiltrados != null && listaTopicosFiltrados.size() > 0) {
            Float porcentagem = verificaCompatibilidade(grupo);
            String porcentagemFormatada = String.format("%.2f", porcentagem);
            holder.txtViewCompGrupo.setText("Compatibilidade: " + porcentagemFormatada + "%");
        }

        VerificaEpilpesia.verificarEpilpesiaSelecionadoGrupo(context, grupo,
                holder.imgViewFotoGrupoPublico);

        holder.txtViewNomeGrupoPublico.setText(grupo.getNomeGrupo());
        holder.txtViewDescricaoGrupoPublico.setText(grupo.getDescricaoGrupo());
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
        private TextView txtViewNomeGrupoPublico, txtViewDescricaoGrupoPublico;
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
        }
    }

    private void irParaTelaInicial() {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity) context).finish();
    }

    private void preencherTopicos(Grupo grupo, LinearLayout linearLayoutTopico) {
        // Adiciona um chip para cada hobby do usuário
        for (String hobby : grupo.getTopicos()) {
            Chip chip = new Chip(context);
            chip.setText(hobby);
            chip.setClickable(false);
            // chip.setChipBackgroundColorResource(R.color.chip_background_color);
            // chip.setTextColor(context.getResources().getColor(R.color.chip_text_color));
            linearLayoutTopico.addView(chip);
        }
    }
}