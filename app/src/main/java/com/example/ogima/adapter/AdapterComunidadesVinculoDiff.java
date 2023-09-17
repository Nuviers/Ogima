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
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.activity.DetalhesComunidadeActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDiffCallback;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Comunidade;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterComunidadesVinculoDiff extends RecyclerView.Adapter<AdapterComunidadesVinculoDiff.MyViewHolder> {

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
    private AdapterComunidadesVinculoDiff.RemocaoComunidadeVinculoListener remocaoComunidadeVinculoListener;
    private AdapterComunidadesVinculoDiff.RecuperaPosicaoListener recuperaPosicaoListener;

    public AdapterComunidadesVinculoDiff(Context c, List<Comunidade> listComunidades, List<String> listTopicos,
                                         RemocaoComunidadeVinculoListener listener, RecuperaPosicaoListener listenerPosicao) {
        this.context = c;
        //Essencial sempre fazer o new ArrayList<>(); na lista recebida
        //no construtor do adapter caso eu esteja usando o
        //DiffUtilCallback, sempre se lembre também do equals no model que tem que ser
        //adicionado também. E a ordenação da lista é sempre depois da operação,
        //adição, atualização, remoção.
        //e sempre antes das notificações.
        this.listaComunidades = listComunidades = new ArrayList<>();
        this.listaTopicosFiltrados = listTopicos;
        this.remocaoComunidadeVinculoListener = listener;
        this.recuperaPosicaoListener = listenerPosicao;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    public void updateComunidadeList(List<Comunidade> listaComunidadesAtualizada) {
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

    public interface RemocaoComunidadeVinculoListener {
        void onComunidadeExcluida(Comunidade comunidadeRemovida);
    }


    public interface RecuperaPosicaoListener {
        void onRecuperaPosicao(int posicao);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comunidade_vinculo, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Comunidade comunidade = listaComunidades.get(position);

        configuraTopicosComunidade(comunidade, holder.linearLayoutTopicosComunidadeVinculo);

        if (listaTopicosFiltrados != null && listaTopicosFiltrados.size() > 0) {
            Float porcentagem = verificaCompatibilidade(comunidade);
            String porcentagemFormatada = String.format("%.2f", porcentagem);
            if (porcentagemFormatada != null && !porcentagemFormatada.isEmpty()) {
                holder.txtViewCompComunidade.setVisibility(View.VISIBLE);
                holder.txtViewCompComunidade.setText("Compatibilidade: " + porcentagemFormatada + "%");
            }
        } else {
            holder.txtViewCompComunidade.setVisibility(View.GONE);
        }

        VerificaEpilpesia.verificarEpilpesiaSelecionadaComunidade(context, comunidade,
                holder.imgViewComunidadeVinculo);

        holder.txtViewNomeComunidadeVinculo.setText(comunidade.getNomeComunidade());
        holder.txtViewDescrComunidadeVinculo.setText(comunidade.getDescricaoComunidade());
        holder.txtViewNrPartComunidadeVinculo.setText("" + comunidade.getSeguidores().size());

        holder.btnSairComunidadeVinculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exibirAvisoSairComunidade(comunidade, holder.btnSairComunidadeVinculo, position);
            }
        });

        holder.imgViewComunidadeVinculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });

        holder.txtViewNomeComunidadeVinculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });

        holder.txtViewDescrComunidadeVinculo.setOnClickListener(new View.OnClickListener() {
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

        private ImageView imgViewComunidadeVinculo;
        private TextView txtViewNomeComunidadeVinculo, txtViewDescrComunidadeVinculo,
                txtViewNrPartComunidadeVinculo, txtViewCompComunidade;
        private Button btnSairComunidadeVinculo;
        private LinearLayout linearLayoutTopicosComunidadeVinculo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewComunidadeVinculo = itemView.findViewById(R.id.imgViewComunidadeVinculo);
            txtViewNomeComunidadeVinculo = itemView.findViewById(R.id.txtViewNomeComunidadeVinculo);
            txtViewDescrComunidadeVinculo = itemView.findViewById(R.id.txtViewDescrComunidadeVinculo);
            btnSairComunidadeVinculo = itemView.findViewById(R.id.btnSairComunidadeVinculo);
            linearLayoutTopicosComunidadeVinculo = itemView.findViewById(R.id.linearLayoutTopicosComunidadeVinculo);
            txtViewCompComunidade = itemView.findViewById(R.id.txtViewCompComunidade);
            txtViewNrPartComunidadeVinculo = itemView.findViewById(R.id.txtViewNrPartComunidadeVinculo);
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

    private void exibirAvisoSairComunidade(Comunidade comunidade, Button btnSnack, int posicao){
        AlertDialog.Builder builder = new AlertDialog.Builder(btnSnack.getContext());
        builder.setTitle("Sair da comunidade");
        builder.setMessage("Tem certeza que deseja sair da comunidade?");
        builder.setPositiveButton("Sair", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sairDaComunidade(comunidade, btnSnack);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Fecha o AlertDialog
                dialogInterface.dismiss();
            }
        });

        // Exibe o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void sairDaComunidade(Comunidade comunidade, Button btnSnack){
        FirebaseRecuperarUsuario.recuperaComunidade(comunidade.getIdComunidade(), new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                if (comunidadeAtual.getSeguidores() != null &&
                        comunidadeAtual.getSeguidores().size() > 0 &&
                        comunidadeAtual.getSeguidores().contains(idUsuarioLogado)) {

                    //Remove o usuário atual da comunidade
                    listaUsuarioAtualRemovido.clear();
                    listaUsuarioAtualRemovido.addAll(comunidadeAtual.getSeguidores());
                    listaUsuarioAtualRemovido.remove(idUsuarioLogado);

                    comunidadeAtualRef = firebaseRef.child("comunidades").child(comunidadeAtual.getIdComunidade());
                    comunidadeAtualRef.child("seguidores").setValue(listaUsuarioAtualRemovido).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            remocaoComunidadeVinculoListener.onComunidadeExcluida(comunidade);
                            if (comunidadeAtual.getAdmsComunidade() != null
                                    && comunidadeAtual.getAdmsComunidade().size() > 0
                                    && comunidadeAtual.getAdmsComunidade().contains(idUsuarioLogado)) {
                                //Remove o usuário atual dos adms caso seja adm.
                                listaUsuarioAtualRemovido.clear();
                                listaUsuarioAtualRemovido.addAll(comunidadeAtual.getAdmsComunidade());
                                listaUsuarioAtualRemovido.remove(idUsuarioLogado);
                                if (listaUsuarioAtualRemovido != null && listaUsuarioAtualRemovido.size() > 0) {
                                    comunidadeAtualRef.child("admsComunidade").setValue(listaUsuarioAtualRemovido);
                                } else {
                                    comunidadeAtualRef.child("admsComunidade").removeValue();
                                }
                            }
                        }
                    });
                }else{
                    SnackbarUtils.showSnackbar(btnSnack, "Essa comunidade não existe mais!");
                    remocaoComunidadeVinculoListener.onComunidadeExcluida(comunidade);
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
                recuperaPosicaoListener.onRecuperaPosicao(posicao);
                Intent intent = new Intent(context, ComunidadePostagensActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("idComunidade", comunidadeAtual.getIdComunidade());
                context.startActivity(intent);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }
}