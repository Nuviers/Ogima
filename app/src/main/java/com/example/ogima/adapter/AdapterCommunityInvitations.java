package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.helper.ButtonUtils;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ConviteDiffCallback;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Convite;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AdapterCommunityInvitations extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Convite> listaConvites;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private AnimacaoIntent animacaoIntentListener;
    private HashMap<String, Object> listaDadosComunidade;
    private int hexImagem = -1;
    private static final int MAX_LENGHT = 20;
    private RemoverConviteListener removerConviteListener;
    private boolean interacaoEmAndamento = false;
    private int corBtnRecusarDesativado = -1;
    private int corBtnAceitarDesativado = -1;
    private int corBtnRecusar = -1;
    private int corBtnAceitar = -1;
    private String wait = "";
    private Activity activityAtual;
    private CommunityUtils communityUtils;

    public AdapterCommunityInvitations(Context c, List<Convite> listaConviteOrigem,
                                       RecuperaPosicaoAnterior recuperaPosicaoListener,
                                       AnimacaoIntent animacaoIntent,
                                       HashMap<String, Object> listDadosComunidade, int hexImagem,
                                       RemoverConviteListener removerComunidadeListener, Activity activityAtual) {
        this.listaConvites = listaConviteOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.animacaoIntentListener = animacaoIntent;
        this.listaDadosComunidade = listDadosComunidade;
        this.hexImagem = hexImagem;
        this.removerConviteListener = removerComunidadeListener;
        this.corBtnRecusarDesativado = context.getResources().getColor(R.color.community_invite_decline_disabled);
        this.corBtnAceitarDesativado = context.getResources().getColor(R.color.community_invite_accept_disabled);
        this.corBtnRecusar = context.getResources().getColor(R.color.community_invite_decline_enabled);
        this.corBtnAceitar = context.getResources().getColor(R.color.community_invite_accept_enabled);
        this.wait = context.getString(R.string.wait_a_moment);
        this.activityAtual = activityAtual;
        this.communityUtils = new CommunityUtils(context);
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public interface RemoverConviteListener {
        void onRemocao(Convite conviteAlvo, int posicao);
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void updateConviteList(List<Convite> listaComunidadesAtualizadas, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        ConviteDiffCallback diffCallback = new ConviteDiffCallback(listaConvites, listaComunidadesAtualizadas);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaConvites.clear();
        listaConvites.addAll(listaComunidadesAtualizadas);
        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_community_invitations, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        Convite convite = listaConvites.get(position);
        String idCommunity = listaConvites.get(position).getIdComunidade();
        Comunidade dadosCommunity = (Comunidade) listaDadosComunidade.get(idCommunity);
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                }
            }
        }
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;
            if (dadosCommunity != null) {

                holderPrincipal.imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexImagem));

                if (dadosCommunity.getFotoComunidade() != null && !dadosCommunity.getFotoComunidade().isEmpty()) {
                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
                    GlideCustomizado.loadUrlComListener(context,
                            dadosCommunity.getFotoComunidade(), holderPrincipal.imgViewIncPhoto,
                            android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP,
                            false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(String message) {
                                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                                }
                            });
                }
                String nomeConfigurado = UsuarioUtils.recuperarNomeConfiguradoComunidade(dadosCommunity);
                nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_COMMUNITY_NAME_LENGHT);
                holderPrincipal.txtViewIncName.setText(nomeConfigurado);
                if (dadosCommunity.getDescricaoComunidade() != null && !dadosCommunity.getDescricaoComunidade().isEmpty()) {
                    String descricaoConfigurada = dadosCommunity.getDescricaoComunidade().trim();
                    descricaoConfigurada = FormatarContadorUtils.abreviarTexto(descricaoConfigurada, UsuarioUtils.MAX_COMMUNITY_PREVIEW_DESC_LENGHT);
                    holderPrincipal.txtViewIncDesc.setText(descricaoConfigurada);
                }

                if (dadosCommunity.getTopicos() != null
                        && dadosCommunity.getTopicos().size() > 0) {
                    if (holderPrincipal.linearLayoutTopicos.getChildCount() == 0) {
                        for (String hobby : dadosCommunity.getTopicos()) {
                            Chip chip = new Chip(holderPrincipal.linearLayoutTopicos.getContext());
                            chip.setText(hobby);
                            chip.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(R.color.friends_color)));
                            chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
                            chip.setLayoutParams(params);
                            chip.setClickable(false);
                            holderPrincipal.linearLayoutTopicos.addView(chip);
                        }
                    }
                }

                long nrParticipantes;
                if (dadosCommunity.getNrParticipantes() <= -1) {
                    nrParticipantes = 0;
                } else {
                    nrParticipantes = dadosCommunity.getNrParticipantes();
                }

                holderPrincipal.txtViewNrParticipantes.setText(FormatarContadorUtils.abreviarTexto(String.valueOf(nrParticipantes), 20));

                holderPrincipal.imgViewIncPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.visitarComunidade(v, convite, position);
                    }
                });
                holderPrincipal.txtViewIncName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.visitarComunidade(v, convite, position);
                    }
                });
                holderPrincipal.txtViewIncDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.visitarComunidade(v, convite, position);
                    }
                });
                holderPrincipal.btnRecusarConvite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (interacaoEmAndamento) {
                            ToastCustomizado.toastCustomizadoCurto(wait, context);
                            return;
                        }
                        holderPrincipal.aparenciaBtnRecusar(true);
                        holderPrincipal.tratarConvite("recusar", dadosCommunity, position);
                    }
                });
                holderPrincipal.btnAceitarConvite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (interacaoEmAndamento) {
                            ToastCustomizado.toastCustomizadoCurto(wait, context);
                            return;
                        }
                        holderPrincipal.aparenciaBtnAceitar(true);
                        holderPrincipal.tratarConvite("aceitar", dadosCommunity, position);
                    }
                });
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return listaConvites.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName, txtViewIncDesc, txtViewNrParticipantes;
        private SpinKitView spinKitLoadPhoto;
        private LinearLayout linearLayoutTopicos;
        private Button btnRecusarConvite, btnAceitarConvite;
        private SpinKitView spinProgressBarInt;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            txtViewIncDesc = itemView.findViewById(R.id.txtViewIncDesc);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            linearLayoutTopicos = itemView.findViewById(R.id.linearLayoutTopicosComunidade);
            btnRecusarConvite = itemView.findViewById(R.id.btnRecusarConviteComunidade);
            btnAceitarConvite = itemView.findViewById(R.id.btnAceitarConviteComunidade);
            txtViewNrParticipantes = itemView.findViewById(R.id.txtViewNrPartComunidade);
            spinProgressBarInt = itemView.findViewById(R.id.spinProgressBarRecyclerInt);
        }

        private void exibirProgress() {
            spinProgressBarInt.setVisibility(View.VISIBLE);
            ProgressBarUtils.exibirProgressBar(spinProgressBarInt, activityAtual);
        }

        private void ocultarProgress() {
            spinProgressBarInt.setVisibility(View.INVISIBLE);
            ProgressBarUtils.ocultarProgressBar(spinProgressBarInt, activityAtual);
        }

        private void aparenciaBtnRecusar(boolean desativar) {
            if (btnRecusarConvite != null) {
                if (desativar) {
                    exibirProgress();
                    ButtonUtils.desativarBotao(btnRecusarConvite, corBtnRecusarDesativado);
                    interacaoEmAndamento = true;
                } else {
                    ocultarProgress();
                    ButtonUtils.ativarBotao(btnRecusarConvite, corBtnRecusar);
                    interacaoEmAndamento = false;
                }
            }
        }

        private void aparenciaBtnAceitar(boolean desativar) {
            if (btnAceitarConvite != null) {
                if (desativar) {
                    exibirProgress();
                    ButtonUtils.desativarBotao(btnAceitarConvite, corBtnAceitarDesativado);
                    interacaoEmAndamento = true;
                } else {
                    ocultarProgress();
                    ButtonUtils.ativarBotao(btnAceitarConvite, corBtnAceitar);
                    interacaoEmAndamento = false;
                }
            }
        }

        private void tratarConvite(String tipoOperacao, Comunidade comunidadeAlvo, int position) {
            if (tipoOperacao == null || tipoOperacao.isEmpty()) {
                return;
            }
            if (comunidadeAlvo == null
                    || comunidadeAlvo.getIdComunidade() == null
                    || comunidadeAlvo.getIdComunidade().isEmpty()) {
                ajustarAparenciaBtn(false, tipoOperacao);
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao verificar o convite selecionado.", context);
                return;
            }
            String idComunidade = comunidadeAlvo.getIdComunidade();
            communityUtils.verificaSeComunidadeExiste(idComunidade, new CommunityUtils.VerificaSeComunidadeExisteCallback() {
                @Override
                public void onStatus(boolean comunidadeExiste) {
                    if (!comunidadeExiste) {
                        ajustarAparenciaBtn(false, tipoOperacao);
                        ToastCustomizado.toastCustomizadoCurto("Essa comunidade não existe mais.", context);
                        int posicao = getBindingAdapterPosition();
                        if (posicao != -1) {
                            removerConviteListener.onRemocao(listaConvites.get(posicao), posicao);
                        }
                        return;
                    }
                    if (tipoOperacao.equals("recusar")) {
                        recusarConvite(comunidadeAlvo, position);
                    } else if (tipoOperacao.equals("aceitar")) {
                        aceitarConvite(comunidadeAlvo, position);
                    }
                }

                @Override
                public void onError(String message) {
                    ajustarAparenciaBtn(false, tipoOperacao);
                    ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao verificar o convite. Tente novamente.", context);
                }
            });
        }

        private void recusarConvite(Comunidade comunidadeAlvo, int position) {
            String idComunidade = comunidadeAlvo.getIdComunidade();
            communityUtils.recusarConvite(idComunidade, new CommunityUtils.RecusarConviteCallback() {
                @Override
                public void onConcluido() {
                    aparenciaBtnRecusar(false);
                    int posicao = getBindingAdapterPosition();
                    if (posicao != -1) {
                        removerConviteListener.onRemocao(listaConvites.get(posicao), posicao);
                    }
                    ToastCustomizado.toastCustomizadoCurto("Convite recusado com sucesso", context);
                }

                @Override
                public void onNaoExiste() {
                    aparenciaBtnRecusar(false);
                    int posicao = getBindingAdapterPosition();
                    if (posicao != -1) {
                        removerConviteListener.onRemocao(listaConvites.get(posicao), posicao);
                    }
                    ToastCustomizado.toastCustomizadoCurto("Esse convite não existe mais.", context);
                }

                @Override
                public void onError(String message) {
                    aparenciaBtnRecusar(false);
                    ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recusar o convite. Tente novamente.", context);
                }
            });
        }

        private void aceitarConvite(Comunidade comunidadeAlvo, int position) {
            String idComunidade = comunidadeAlvo.getIdComunidade();
            communityUtils.aceitarConvite(idComunidade, new CommunityUtils.AceitarConviteCallback() {
                @Override
                public void onConcluido() {
                    aparenciaBtnAceitar(false);
                    int posicao = getBindingAdapterPosition();
                    if (posicao != -1) {
                        removerConviteListener.onRemocao(listaConvites.get(posicao), posicao);
                    }
                    ToastCustomizado.toastCustomizadoCurto("Convite aceito com sucesso " + posicao, context);
                }

                @Override
                public void onBlocked() {
                    aparenciaBtnAceitar(false);
                    ToastCustomizado.toastCustomizado("Você precisa desbloquear essa comunidade para poder aceitar o convite.", context);
                }

                @Override
                public void onNaoExiste() {
                    aparenciaBtnAceitar(false);
                    int posicao = getBindingAdapterPosition();
                    if (posicao != -1) {
                        removerConviteListener.onRemocao(listaConvites.get(posicao), posicao);
                    }
                    ToastCustomizado.toastCustomizadoCurto("Esse convite não existe mais.", context);
                }

                @Override
                public void onError(String message) {
                    aparenciaBtnAceitar(false);
                    ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao aceitar o convite. Tente novamente.", context);
                }
            });
        }

        private void ajustarAparenciaBtn(boolean desativar, String tipoButton) {
            if (tipoButton.equals("recusar")) {
                aparenciaBtnRecusar(desativar);
            } else if (tipoButton.equals("aceitar")) {
                aparenciaBtnAceitar(desativar);
            }
        }

        private void visitarComunidade(View view, Convite convite, int position) {
            FirebaseRecuperarUsuario.recoverCommunity(convite.getIdComunidade(), new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
                @Override
                public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                    Intent intent = new Intent(context, ComunidadePostagensActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("idComunidade", comunidadeAtual.getIdComunidade());
                    context.startActivity(intent);
                    recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                    animacaoIntentListener.onExecutarAnimacao();
                }

                @Override
                public void onNaoExiste() {
                    SnackbarUtils.showSnackbar(view, context.getString(R.string.community_does_not_exist));
                    int posicao = getBindingAdapterPosition();
                    if (posicao != -1) {
                        removerConviteListener.onRemocao(listaConvites.get(posicao), posicao);
                    }
                }

                @Override
                public void onError(String mensagem) {
                    ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.error_visiting_community), context);
                }
            });
        }
    }

    public int findPositionInList(String communityId) {
        for (int i = 0; i < listaConvites.size(); i++) {
            Convite convite = listaConvites.get(i);
            if (convite.getIdComunidade().equals(communityId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}