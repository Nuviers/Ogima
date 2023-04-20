package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.ComunidadesPublicasActivity;
import com.example.ogima.activity.DetalhesComunidadeActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDiffCallback;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Comunidade;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterComunidadesPublicasDiff extends RecyclerView.Adapter<AdapterComunidadesPublicasDiff.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Comunidade> listaComunidades;
    private List<String> listaTopicosFiltrados;

    private DatabaseReference salvarAvisoRef;
    private String idConversaComunidade;
    private ArrayList<String> listaUsuarioAtualRemovido = new ArrayList<>();
    private DatabaseReference comunidadeAtualRef;
    private AdapterComunidadesPublicasDiff.OperacaoComunidadePublicaListener operacaoComunidadePublicaListener;
    private AdapterComunidadesPublicasDiff.posicaoAnteriorComunidadePublica posicaoAnteriorComunidadePublica;
    private int nrParticipantes = 0;

    public AdapterComunidadesPublicasDiff(Context c, List<Comunidade> listComunidades, List<String> listTopicos, OperacaoComunidadePublicaListener listener,
                                          posicaoAnteriorComunidadePublica listenerPosicao) {
        this.context = c;
        //Essencial sempre fazer o new ArrayList<>(); na lista recebida
        //no construtor do adapter caso eu esteja usando o
        //DiffUtilCallback, sempre se lembre também do equals no model que tem que ser
        //adicionado também. E a ordenação da lista é sempre depois da operação,
        //adição, atualização, remoção.
        //e sempre antes das notificações.
        this.listaComunidades = listComunidades = new ArrayList<>();
        this.listaTopicosFiltrados = listTopicos;
        this.operacaoComunidadePublicaListener = listener;
        this.posicaoAnteriorComunidadePublica = listenerPosicao;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    public void updateComunidadePublicaList(List<Comunidade> listaComunidadesAtualizada) {
        ComunidadeDiffCallback diffCallback = new ComunidadeDiffCallback(listaComunidades, listaComunidadesAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaComunidades.clear();
        listaComunidades.addAll(listaComunidadesAtualizada);
        diffResult.dispatchUpdatesTo(this);

        if (listaComunidadesAtualizada != null && listaComunidadesAtualizada.size() > 0) {
            //ToastCustomizado.toastCustomizadoCurto("Tamanho: " + listaComunidadesAtualizada.size(), context);
            for (Comunidade comunidadeExibicao : listaComunidadesAtualizada) {
                //ToastCustomizado.toastCustomizadoCurto("Nome: " + comunidadeExibicao.getNomeComunidade(), context);
            }
        }
    }

    public interface OperacaoComunidadePublicaListener {
        void onComunidadeOperacao(Comunidade comunidadeRemovida, String tipoOperacao);
    }

    public interface posicaoAnteriorComunidadePublica {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comunidade_publica, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Comunidade comunidade = listaComunidades.get(position);

        configuraTopicosComunidade(comunidade, holder.linearLayoutTopicosComunidadePublica);

        if (listaTopicosFiltrados != null && listaTopicosFiltrados.size() > 0) {
            Float porcentagem = verificaCompatibilidade(comunidade);
            String porcentagemFormatada = String.format("%.2f", porcentagem);
            if (porcentagemFormatada != null && !porcentagemFormatada.isEmpty()) {
                holder.txtViewCompComunidadePublica.setVisibility(View.VISIBLE);
                holder.txtViewCompComunidadePublica.setText("Compatibilidade: " + porcentagemFormatada + "%");
            }
        } else {
            holder.txtViewCompComunidadePublica.setVisibility(View.GONE);
        }

        if (comunidade.getParticipantes() != null && comunidade.getParticipantes().size() > 0
                && comunidade.getParticipantes().contains(idUsuarioLogado)) {
            holder.btnEntrarComunidadePublica.setVisibility(View.GONE);
        } else {
            holder.btnEntrarComunidadePublica.setVisibility(View.VISIBLE);
        }

        VerificaEpilpesia.verificarEpilpesiaSelecionadaComunidade(context, comunidade,
                holder.imgViewComunidadePublica);

        holder.txtViewNomeComunidadePublica.setText(comunidade.getNomeComunidade());
        holder.txtViewDescrComunidadePublica.setText(comunidade.getDescricaoComunidade());
        holder.txtViewNrPartComunidadePublica.setText("" + comunidade.getParticipantes().size());

        holder.btnEntrarComunidadePublica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                participarDaComunidade(comunidade, holder.btnEntrarComunidadePublica, holder.txtViewNrPartComunidadePublica);
            }
        });

        holder.imgViewComunidadePublica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });

        holder.txtViewNomeComunidadePublica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });

        holder.txtViewDescrComunidadePublica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });
    }

    private float verificaCompatibilidade(Comunidade comunidade) {
        // Criar uma cópia da lista de hobbies selecionados para evitar modificá-la
        List<String> hobbiesSelecionadosCopia = new ArrayList<>(listaTopicosFiltrados);

        // Calcular a interseção entre as duas listas de hobbies
        hobbiesSelecionadosCopia.retainAll(comunidade.getTopicos());

        // Calcular a porcentagem de compatibilidade
        return ((float) hobbiesSelecionadosCopia.size() / listaTopicosFiltrados.size()) * 100;
    }

    @Override
    public int getItemCount() {
        return listaComunidades.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewComunidadePublica;
        private TextView txtViewNomeComunidadePublica, txtViewDescrComunidadePublica,
                txtViewNrPartComunidadePublica, txtViewCompComunidadePublica;
        private Button btnEntrarComunidadePublica;
        private LinearLayout linearLayoutTopicosComunidadePublica;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewComunidadePublica = itemView.findViewById(R.id.imgViewComunidadePublica);
            txtViewNomeComunidadePublica = itemView.findViewById(R.id.txtViewNomeComunidadePublica);
            txtViewDescrComunidadePublica = itemView.findViewById(R.id.txtViewDescrComunidadePublica);
            btnEntrarComunidadePublica = itemView.findViewById(R.id.btnEntrarComunidadePublica);
            linearLayoutTopicosComunidadePublica = itemView.findViewById(R.id.linearLayoutTopicosComunidadePublica);
            txtViewCompComunidadePublica = itemView.findViewById(R.id.txtViewCompComunidadePublica);
            txtViewNrPartComunidadePublica = itemView.findViewById(R.id.txtViewNrPartComunidadePublica);
        }
    }

    private void irParaTelaInicial() {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    private void configuraTopicosComunidade(Comunidade comunidade, LinearLayout linearLayoutTopicos) {
        // Limpa o layout antes de adicionar os chips
        linearLayoutTopicos.removeAllViews();

        // Adiciona um chip para cada hobby
        for (String hobby : comunidade.getTopicos()) {
            Chip chip = new Chip(linearLayoutTopicos.getContext());
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
            linearLayoutTopicos.addView(chip);
        }
    }

    private void participarDaComunidade(Comunidade comunidade, Button btnSnack, TextView txtNrParticipantes) {

        nrParticipantes = 0;

        DatabaseReference salvarParticipanteRef = firebaseRef.child("comunidades")
                .child(comunidade.getIdComunidade()).child("participantes");
        ArrayList<String> listaParticipantes = new ArrayList<>();

        FirebaseRecuperarUsuario.recuperaComunidade(comunidade.getIdComunidade(), new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                if (comunidadeAtual.getComunidadePublica() != null
                        && comunidadeAtual.getComunidadePublica().equals(true)) {

                    if (comunidadeAtual.getParticipantes() != null
                            && comunidadeAtual.getParticipantes().size() > 0
                            && !comunidadeAtual.getParticipantes().contains(idUsuarioLogado)) {
                        //Usuário atual não é participante.
                        listaParticipantes.addAll(comunidade.getParticipantes());
                        listaParticipantes.add(idUsuarioLogado);
                        salvarParticipanteRef.setValue(listaParticipantes).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                btnSnack.setVisibility(View.GONE);
                                SnackbarUtils.showSnackbar(btnSnack, "Agora você é participante da comunidade: " + comunidadeAtual.getNomeComunidade());
                                nrParticipantes = comunidadeAtual.getParticipantes().size() + 1;
                                txtNrParticipantes.setText("" + nrParticipantes);
                            }
                        });
                    } else {
                        SnackbarUtils.showSnackbar(btnSnack, "Você já é participante dessa comunidade!");
                    }

                } else if (comunidadeAtual.getComunidadePublica().equals(false)) {
                    SnackbarUtils.showSnackbar(btnSnack, "Essa comunidade não é mais pública!");
                    operacaoComunidadePublicaListener.onComunidadeOperacao(comunidade, "remover");
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void irParaDetalhes(Comunidade comunidadeRecebida, int posicao) {

        FirebaseRecuperarUsuario.recuperaComunidade(comunidadeRecebida.getIdComunidade(), new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                posicaoAnteriorComunidadePublica.onPosicaoAnterior(posicao);
                Intent intent = new Intent(context, DetalhesComunidadeActivity.class);
                //Garante que não haja duplicações na pilha de activity - Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("comunidadeAtual", comunidadeAtual);
                intent.putExtra("voltar", "voltar");
                context.startActivity(intent);
                //((Activity) context).finish();
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }
}