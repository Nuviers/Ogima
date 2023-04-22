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
import com.example.ogima.activity.DetalhesComunidadeActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDiffCallback;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Comunidade;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterConviteComunidade extends RecyclerView.Adapter<AdapterConviteComunidade.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private Context context;
    private List<Comunidade> listaComunidades;

    private AdapterConviteComunidade.RemocaoComunidadeConviteListener remocaoComunidadeConviteListener;
    private AdapterConviteComunidade.RecuperaPosicaoConviteListener recuperaPosicaoConviteListener;

    public AdapterConviteComunidade(Context c, List<Comunidade> listComunidades,
                                    RemocaoComunidadeConviteListener listener, RecuperaPosicaoConviteListener listenerPosicao) {
        this.context = c;
        //Essencial sempre fazer o new ArrayList<>(); na lista recebida
        //no construtor do adapter caso eu esteja usando o
        //DiffUtilCallback, sempre se lembre também do equals no model que tem que ser
        //adicionado também. E a ordenação da lista é sempre depois da operação,
        //adição, atualização, remoção.
        //e sempre antes das notificações.
        this.listaComunidades = listComunidades = new ArrayList<>();
        this.remocaoComunidadeConviteListener = listener;
        this.recuperaPosicaoConviteListener = listenerPosicao;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    public void updateComunidadeConviteList(List<Comunidade> listaComunidadesAtualizada) {
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

    public interface RemocaoComunidadeConviteListener {
        void onComunidadeExcluida(Comunidade comunidadeRemovida);
    }


    public interface RecuperaPosicaoConviteListener {
        void onRecuperaPosicao(int posicao);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_convite_comunidade, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Comunidade comunidade = listaComunidades.get(position);

        holder.txtViewCompComunidade.setVisibility(View.GONE);

        VerificaEpilpesia.verificarEpilpesiaSelecionadaComunidade(context, comunidade,
                holder.imgViewConviteComunidade);

        holder.txtViewNomeConviteComunidade.setText(comunidade.getNomeComunidade());
        holder.txtViewDescrConviteComunidade.setText(comunidade.getDescricaoComunidade());
        holder.txtViewNrPartConviteComunidade.setText("" + comunidade.getSeguidores().size());

        holder.btnSairConviteComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recusarConvite(comunidade, holder.btnSairConviteComunidade);
            }
        });

        holder.imgViewConviteComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });

        holder.txtViewNomeConviteComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });

        holder.txtViewDescrConviteComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDetalhes(comunidade, position);
            }
        });

        holder.btnAceitarConviteComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aceitarConvite(comunidade, holder.btnAceitarConviteComunidade);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaComunidades.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewConviteComunidade;
        private TextView txtViewNomeConviteComunidade, txtViewDescrConviteComunidade,
                txtViewNrPartConviteComunidade, txtViewCompComunidade;
        private Button btnSairConviteComunidade, btnAceitarConviteComunidade;
        private LinearLayout linearLayoutTopicosConviteComunidade;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewConviteComunidade = itemView.findViewById(R.id.imgViewConviteComunidade);
            txtViewNomeConviteComunidade = itemView.findViewById(R.id.txtViewNomeConviteComunidade);
            txtViewDescrConviteComunidade = itemView.findViewById(R.id.txtViewDescrConviteComunidade);
            btnSairConviteComunidade = itemView.findViewById(R.id.btnSairConviteComunidade);
            btnAceitarConviteComunidade = itemView.findViewById(R.id.btnAceitarConviteComunidade);
            linearLayoutTopicosConviteComunidade = itemView.findViewById(R.id.linearLayoutTopicosConviteComunidade);
            txtViewCompComunidade = itemView.findViewById(R.id.txtViewCompComunidade);
            txtViewNrPartConviteComunidade = itemView.findViewById(R.id.txtViewNrPartConviteComunidade);
        }
    }

    private void irParaTelaInicial() {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity) context).finish();
    }


    private void aceitarConvite(Comunidade comunidade, Button btnSnack) {

        DatabaseReference aceitarConviteRef = firebaseRef.child("comunidades")
                .child(comunidade.getIdComunidade()).child("seguidores");

        DatabaseReference removerConviteRef = firebaseRef.child("convitesComunidade")
                .child(idUsuarioLogado).child(comunidade.getIdComunidade());

        ArrayList<String> listaParticipantes = new ArrayList<>();

        FirebaseRecuperarUsuario.recuperaComunidade(comunidade.getIdComunidade(), new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                if (comunidadeAtual.getSeguidores() != null
                        && comunidadeAtual.getSeguidores().size() > 0) {
                    listaParticipantes.addAll(comunidadeAtual.getSeguidores());
                    listaParticipantes.add(idUsuarioLogado);
                    aceitarConviteRef.setValue(listaParticipantes).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            SnackbarUtils.showSnackbar(btnSnack, "Agora você é participante da comunidade " + comunidadeAtual.getNomeComunidade());
                            remocaoComunidadeConviteListener.onComunidadeExcluida(comunidade);
                            removerConviteRef.removeValue();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            SnackbarUtils.showSnackbar(btnSnack, "Ocorreu um erro ao aceitar o convite");
                            remocaoComunidadeConviteListener.onComunidadeExcluida(comunidade);
                        }
                    });
                } else {
                    SnackbarUtils.showSnackbar(btnSnack, "Comunidade não existe mais");
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void recusarConvite(Comunidade comunidade, Button btnSnack) {
        DatabaseReference recusarConviteRef = firebaseRef.child("convitesComunidade")
                .child(idUsuarioLogado).child(comunidade.getIdComunidade());

        recusarConviteRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                remocaoComunidadeConviteListener.onComunidadeExcluida(comunidade);
                SnackbarUtils.showSnackbar(btnSnack, "Convite recusado com sucesso");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                SnackbarUtils.showSnackbar(btnSnack, "Ocorreu um erro ao recusar o convite");
            }
        });
    }

    private void irParaDetalhes(Comunidade comunidadeRecebida, int posicao) {

        FirebaseRecuperarUsuario.recuperaComunidade(comunidadeRecebida.getIdComunidade(), new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                recuperaPosicaoConviteListener.onRecuperaPosicao(posicao);
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