package com.example.ogima.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.CommunityDetailsActivity;
import com.example.ogima.helper.ButtonUtils;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.TimestampUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Comunidade;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;

public class HeaderAdapterPostagemComunidade extends RecyclerView.Adapter<HeaderAdapterPostagemComunidade.HeaderViewHolder> {

    private Context context;
    private String idComunidade = "";
    private String idUsuarioLogado = "";
    private boolean statusEpilepsia = true;
    private boolean interacaoEmAndamento = false;
    private int corBotaoDesativado = -1;
    private CommunityUtils communityUtils;
    private GroupUtils groupUtils;
    private Activity activity;
    public boolean exibirBtnEntrar = false;
    public boolean exibirEntrarChat = false;
    public boolean jaParticipaDoGrupo = false;

    public HeaderAdapterPostagemComunidade(Context c, Activity activity, String idComunidadeRecebida) {
        this.context = c;
        this.idComunidade = idComunidadeRecebida;
        this.activity = activity;
        this.idUsuarioLogado = UsuarioUtils.recuperarIdUserAtual();
        this.communityUtils = new CommunityUtils(context);
        this.groupUtils = new GroupUtils(context);
        corBotaoDesativado = context.getResources().getColor(R.color.join_community_button_disabled);
    }

    public boolean isExibirBtnEntrar() {
        return exibirBtnEntrar;
    }

    public void setExibirBtnEntrar(boolean exibirBtnEntrar) {
        this.exibirBtnEntrar = exibirBtnEntrar;
        notifyDataSetChanged();
    }

    public boolean isExibirEntrarChat() {
        return exibirEntrarChat;
    }

    public boolean isJaParticipaDoGrupo() {
        return jaParticipaDoGrupo;
    }

    public void setJaParticipaDoGrupo(boolean jaParticipaDoGrupo) {
        this.jaParticipaDoGrupo = jaParticipaDoGrupo;
        notifyDataSetChanged();
    }

    public void setExibirEntrarChat(boolean exibirEntrarChat) {
        this.exibirEntrarChat = exibirEntrarChat;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    // Implemente os métodos necessários para o adapter do cabeçalho
    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_header_postagem_comunidade, parent, false);
        return new HeaderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {

        if (isExibirBtnEntrar()) {
            holder.btnViewEntrarComunidade.setVisibility(View.VISIBLE);
        } else {
            holder.btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
        }

        if (isExibirEntrarChat()) {
            holder.linearLayoutChatComunidade.setVisibility(View.VISIBLE);
            if (isJaParticipaDoGrupo()) {
                holder.txtViewChatComunidade.setText("CHAT");
            }else{
                holder.txtViewChatComunidade.setText("Participar do chat");
            }
        } else {
            holder.linearLayoutChatComunidade.setVisibility(View.INVISIBLE);
        }

        FirebaseRecuperarUsuario.recuperaComunidade(idComunidade, new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {

                holder.preencherCabecalho(comunidadeAtual);

                holder.btnViewEntrarComunidade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (interacaoEmAndamento) {
                            ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                            return;
                        }
                        holder.aparenciaBtnInt(true, true);
                        holder.participarDaComunidade(comunidadeAtual);
                    }
                });

                holder.imgBtnChatComunidade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (interacaoEmAndamento) {
                            ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                            return;
                        }
                        holder.aparenciaBtnInt(true, false);
                        holder.participarDoChat(comunidadeAtual);
                    }
                });

                holder.txtViewChatComunidade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (interacaoEmAndamento) {
                            ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                            return;
                        }
                        holder.aparenciaBtnInt(true, false);
                        holder.participarDoChat(comunidadeAtual);
                    }
                });

                holder.configBtnDetalhes(comunidadeAtual);
            }

            @Override
            public void onError(String mensagem) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return 1; // Apenas um item para o cabeçalho
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutTopicos;
        private ImageView imgViewIncFotoUser, imgViewIncFundoUser;
        private View viewIncBackOpcoes;
        private TextView txtViewIncNomeUser;
        private ImageButton imgBtnParticipantes, imgBtnIncOpcoes, imgBtnChatComunidade;
        private TextView txtViewNrParticipantes, txtViewChatComunidade;
        private Button btnViewEntrarComunidade;
        private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        private DatabaseReference adicionarSeguidorRef, verificaConvitesRef;
        private ProgressBar progressBar;
        private LinearLayout linearLayoutChatComunidade;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Realize a vinculação dos elementos do layout do cabeçalho, se necessário
            linearLayoutTopicos = itemView.findViewById(R.id.linearLayoutTopicosHeaderComunidadePostagem);
            imgViewIncFotoUser = itemView.findViewById(R.id.imgViewIncFotoUser);
            imgViewIncFundoUser = itemView.findViewById(R.id.imgViewIncFundoUser);
            viewIncBackOpcoes = itemView.findViewById(R.id.viewIncBackOpcoes);
            txtViewIncNomeUser = itemView.findViewById(R.id.txtViewIncNomeUser);
            imgBtnParticipantes = itemView.findViewById(R.id.imgBtnParticipantesComunidade);
            txtViewNrParticipantes = itemView.findViewById(R.id.txtViewNrParticipantesComunidade);
            btnViewEntrarComunidade = itemView.findViewById(R.id.btnViewEntrarComunidade);
            imgBtnIncOpcoes = itemView.findViewById(R.id.imgBtnIncOpcoes);
            progressBar = itemView.findViewById(R.id.progressBarParticipar);
            imgBtnChatComunidade = itemView.findViewById(R.id.imgBtnChatComunidade);
            txtViewChatComunidade = itemView.findViewById(R.id.txtViewChatComunidade);
            linearLayoutChatComunidade = itemView.findViewById(R.id.linearLayoutChatComunidade);
            this.adicionarSeguidorRef = firebaseRef.child("comunidades");
            this.verificaConvitesRef = firebaseRef.child("convitesComunidade");
        }

        private void aparenciaBtnInt(boolean desativarBotao, boolean participarDaComunidade) {
            if (participarDaComunidade) {
                if (btnViewEntrarComunidade != null) {
                    if (desativarBotao) {
                        btnViewEntrarComunidade.setEnabled(false);
                        btnViewEntrarComunidade.setBackgroundTintList(ColorStateList.valueOf(corBotaoDesativado));
                        interacaoEmAndamento = true;
                        ProgressBarUtils.exibirProgressBar(progressBar, activity);
                    } else {
                        ButtonUtils.ativarBotaoDegrade(btnViewEntrarComunidade);
                        interacaoEmAndamento = false;
                        ProgressBarUtils.ocultarProgressBar(progressBar, activity);
                    }
                }
            }else{
                if (imgBtnChatComunidade != null && txtViewChatComunidade != null) {
                    if (desativarBotao) {
                        imgBtnChatComunidade.setEnabled(false);
                        txtViewChatComunidade.setEnabled(false);
                        interacaoEmAndamento = true;
                        ProgressBarUtils.exibirProgressBar(progressBar, activity);
                    } else {
                        ButtonUtils.ativarImgBtn(imgBtnChatComunidade);
                        txtViewChatComunidade.setEnabled(true);
                        interacaoEmAndamento = false;
                        ProgressBarUtils.ocultarProgressBar(progressBar, activity);
                    }
                }
            }
        }

        private void preencherCabecalho(Comunidade comunidadeAtual) {
            txtViewIncNomeUser.setText(comunidadeAtual.getNomeComunidade());

            long nrParticipantes = 0;
            if (comunidadeAtual.getNrParticipantes() > 0) {
                nrParticipantes = comunidadeAtual.getNrParticipantes();
            }

            txtViewNrParticipantes.setText(String.valueOf(nrParticipantes));

            if (comunidadeAtual.getTopicos() != null && comunidadeAtual.getTopicos().size() > 0) {
                linearLayoutTopicos.setVisibility(View.VISIBLE);
                configChipsTopicos(comunidadeAtual, linearLayoutTopicos);
            } else {
                linearLayoutTopicos.setVisibility(View.GONE);
            }

            GlideCustomizado.loadUrlComListener(context, comunidadeAtual.getFotoComunidade(),
                    imgViewIncFotoUser, android.R.color.transparent, GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {

                        }

                        @Override
                        public void onError(String message) {

                        }
                    });

            GlideCustomizado.loadUrlComListener(context, comunidadeAtual.getFundoComunidade(),
                    imgViewIncFundoUser, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {

                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
        }

        private void configChipsTopicos(Comunidade comunidade, LinearLayout linearLayoutTopicos) {
            linearLayoutTopicos.removeAllViews();

            for (String topico : comunidade.getTopicos()) {
                Chip chip = new Chip(linearLayoutTopicos.getContext());
                chip.setText(topico);
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

        private void participarDaComunidade(Comunidade comunidadeAlvo) {
            String idComunidade = comunidadeAlvo.getIdComunidade();
            communityUtils.verificaSeEParticipante(idComunidade, idUsuarioLogado, new CommunityUtils.VerificaParticipanteCallback() {
                @Override
                public void onParticipante(boolean status) {
                    if (status) {
                        //Já participa
                        SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Você já faz parte dessa comunidade!");
                        btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
                        aparenciaBtnInt(false, true);
                    } else {
                        //Não participa
                        communityUtils.participarDaComunidade(idComunidade, new CommunityUtils.ParticiparDaComunidadeCallback() {
                            @Override
                            public void onConcluido() {
                                SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Agora você faz parte dessa comunidade!");
                                btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
                                aparenciaBtnInt(false, true);
                                int nrParticipantes = 1;
                                if (comunidadeAlvo.getNrParticipantes() != -1) {
                                    nrParticipantes = (int) (comunidadeAlvo.getNrParticipantes() + 1);
                                }
                                txtViewNrParticipantes.setText(String.valueOf(nrParticipantes));
                            }

                            @Override
                            public void onError(String message) {
                                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s %s", "Ocorreu um erro ao tentar participar da comunidade. Tente novamente.", "Code:", message), context);
                                aparenciaBtnInt(false, true);
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                    aparenciaBtnInt(false, true);
                }
            });
        }

        private void participarDoChat(Comunidade comunidadeAlvo){
            String idComunidade = comunidadeAlvo.getIdComunidade();
            String idChat = comunidadeAlvo.getIdChatComunidade();
            groupUtils.verificaSeEParticipante(idChat, idUsuarioLogado, new GroupUtils.VerificaParticipanteCallback() {
                @Override
                public void onParticipante(boolean participante) {
                    if (participante) {
                        //Já participa
                        aparenciaBtnInt(false, false);
                        VisitarPerfilSelecionado.visitarGrupoSelecionado(context, idChat, true);
                    }else{
                        communityUtils.verificaBlock(idUsuarioLogado, idComunidade, new CommunityUtils.VerificaBlockCallback() {
                            @Override
                            public void onBlock(boolean status) {
                                if (status) {
                                    //Usuário está bloqueado pela comunidade ou vice-versa.
                                    SnackbarUtils.showSnackbar(imgBtnChatComunidade, "Chat indisponível!");
                                    linearLayoutChatComunidade.setVisibility(View.GONE);
                                    setExibirEntrarChat(false);
                                    aparenciaBtnInt(false, false);
                                }else{
                                    HashMap<String, Object> dadosOperacao = new HashMap<>();
                                    GroupUtils.ajustarDadoParaPesquisa(context, dadosOperacao, idChat, idUsuarioLogado, new GroupUtils.AjustarDadoParaPesquisaCallback() {
                                        String caminhoGrupo = "/grupos/" + idChat + "/";
                                        String caminhoFollowers = "/groupFollowers/" + idChat + "/" + idUsuarioLogado + "/";
                                        String caminhoFollowing = "/groupFollowing/" + idUsuarioLogado + "/" + idChat + "/";
                                        @Override
                                        public void onConcluido(HashMap<String, Object> dadosOperacao) {
                                            TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                                                @Override
                                                public void onRecuperado(long timestampNegativo) {
                                                    groupUtils.recuperarListaParticipantes(idChat, new GroupUtils.RecuperarListaParticipantesCallback() {
                                                        @Override
                                                        public void onConcluido(ArrayList<String> idsParticipantes) {
                                                            if (idsParticipantes != null && !idsParticipantes.isEmpty()) {
                                                                idsParticipantes.add(idUsuarioLogado);
                                                            }else if (idsParticipantes == null){
                                                                idsParticipantes = new ArrayList<>();
                                                            }
                                                            dadosOperacao.put(caminhoGrupo + "nrParticipantes", ServerValue.increment(1));
                                                            dadosOperacao.put(caminhoGrupo + "participantes", idsParticipantes);
                                                            dadosOperacao.put(caminhoFollowers + "timestampinteracao", timestampNegativo);
                                                            dadosOperacao.put(caminhoFollowers + "idParticipante", idUsuarioLogado);
                                                            dadosOperacao.put(caminhoFollowers + "administrator", false);
                                                            dadosOperacao.put(caminhoFollowing + "idGrupo", idChat);
                                                            dadosOperacao.put(caminhoFollowing + "timestampinteracao", timestampNegativo);
                                                            firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                    SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Agora você faz parte do chat!");
                                                                    aparenciaBtnInt(false, false);
                                                                    setJaParticipaDoGrupo(true);
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onError(String message) {
                                                            ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                                                            aparenciaBtnInt(false, false);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onError(String message) {
                                                    ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                                                    aparenciaBtnInt(false, false);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String message) {
                                            ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                                            aparenciaBtnInt(false, false);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(String message) {
                                ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                                aparenciaBtnInt(false, false);
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                    aparenciaBtnInt(false, false);
                }
            });
        }

        private void configBtnDetalhes(Comunidade comunidadeAtual) {
            imgBtnIncOpcoes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irParaDetalhes(comunidadeAtual);
                }
            });
            viewIncBackOpcoes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irParaDetalhes(comunidadeAtual);
                }
            });
        }

        private void irParaDetalhes(Comunidade comunidadeAtual) {
            FirebaseRecuperarUsuario.recuperaComunidade(comunidadeAtual.getIdComunidade(), new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
                @Override
                public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                    if (comunidadeAtual.getIdComunidade() != null) {
                        Intent intent = new Intent(context, CommunityDetailsActivity.class);
                        //Intent intent = new Intent(context, DetalhesComunidadeActivity.class);
                        //intent.putExtra("comunidadeAtual", comunidadeAtual);
                        intent.putExtra("idComunidade", comunidadeAtual.getIdComunidade());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                }

                @Override
                public void onError(String mensagem) {

                }
            });
        }
    }
}
