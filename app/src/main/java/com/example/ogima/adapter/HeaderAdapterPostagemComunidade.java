package com.example.ogima.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.CommunityDetailsActivity;
import com.example.ogima.activity.DetalhesComunidadeActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ButtonUtils;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Usuario;
import com.google.android.exoplayer2.C;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class HeaderAdapterPostagemComunidade extends RecyclerView.Adapter<HeaderAdapterPostagemComunidade.HeaderViewHolder> {

    private Context context;
    private String idComunidade = "";
    private String idUsuarioLogado = "";
    private boolean statusEpilepsia = true;
    private boolean interacaoEmAndamento = false;
    private int corBotaoDesativado = -1;
    private CommunityUtils communityUtils;
    private Activity activity;

    public HeaderAdapterPostagemComunidade(Context c, Activity activity, String idComunidadeRecebida) {
        this.context = c;
        this.idComunidade = idComunidadeRecebida;
        this.activity = activity;
        this.idUsuarioLogado = UsuarioUtils.recuperarIdUserAtual();
        this.communityUtils = new CommunityUtils(context);
        corBotaoDesativado = context.getResources().getColor(R.color.join_community_button_disabled);
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
                        holder.aparenciaBtnInt(true);
                        holder.participarDaComunidade(comunidadeAtual);
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
        private ImageButton imgBtnParticipantes, imgBtnIncOpcoes;
        private TextView txtViewNrParticipantes;
        private Button btnViewEntrarComunidade;
        private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        private DatabaseReference adicionarSeguidorRef, verificaConvitesRef;
        private ProgressBar progressBar;

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
            this.adicionarSeguidorRef = firebaseRef.child("comunidades");
            this.verificaConvitesRef = firebaseRef.child("convitesComunidade");
        }

        private void aparenciaBtnInt(boolean desativarBotao) {
            if (btnViewEntrarComunidade != null) {
                if (desativarBotao) {
                    btnViewEntrarComunidade.setEnabled(false);
                    btnViewEntrarComunidade.setBackgroundTintList(ColorStateList.valueOf(corBotaoDesativado));
                    interacaoEmAndamento = true;
                    ProgressBarUtils.exibirProgressBar(progressBar, activity);
                } else {
                    ButtonUtils.ativarBotao(btnViewEntrarComunidade);
                    interacaoEmAndamento = false;
                    ProgressBarUtils.ocultarProgressBar(progressBar, activity);
                }
            }
        }

        private void preencherCabecalho(Comunidade comunidadeAtual) {
            txtViewIncNomeUser.setText(comunidadeAtual.getNomeComunidade());

            if (comunidadeAtual.getSeguidores() != null && comunidadeAtual.getSeguidores().size() > 0) {
                txtViewNrParticipantes.setText("" + comunidadeAtual.getSeguidores().size());
            } else {
                txtViewNrParticipantes.setText("0");
            }

            if (comunidadeAtual.getTopicos() != null && comunidadeAtual.getTopicos().size() > 0) {
                linearLayoutTopicos.setVisibility(View.VISIBLE);
                configChipsTopicos(comunidadeAtual, linearLayoutTopicos);
            } else {
                linearLayoutTopicos.setVisibility(View.GONE);
            }

            if (comunidadeAtual.getSeguidores() != null
                    && comunidadeAtual.getSeguidores().size() > 0
                    && comunidadeAtual.getSeguidores().contains(idUsuarioLogado)) {
                btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
            } else {
                btnViewEntrarComunidade.setVisibility(View.VISIBLE);
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

        private void participarDaComunidade(Comunidade comunidadeAlvo){
            String idComunidade = comunidadeAlvo.getIdComunidade();
            communityUtils.verificaSeEParticipante(idComunidade, idUsuarioLogado, new CommunityUtils.VerificaParticipanteCallback() {
                @Override
                public void onParticipante(boolean status) {
                    if (status) {
                        //Já participa
                        SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Você já faz parte dessa comunidade!");
                        btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
                        aparenciaBtnInt(false);
                    }else{
                        //Não participa
                        communityUtils.participarDaComunidade(idComunidade, new CommunityUtils.ParticiparDaComunidadeCallback() {
                            @Override
                            public void onConcluido() {
                                SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Agora você faz parte dessa comunidade!");
                                btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
                                aparenciaBtnInt(false);
                                int nrParticipantes = 1;
                                if (comunidadeAlvo.getNrParticipantes() != -1) {
                                    nrParticipantes = (int) (comunidadeAlvo.getNrParticipantes() + 1);
                                }
                                txtViewNrParticipantes.setText(String.valueOf(nrParticipantes));
                            }

                            @Override
                            public void onError(String message) {
                                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s %s", "Ocorreu um erro ao tentar participar da comunidade. Tente novamente.", "Code:", message), context);
                                aparenciaBtnInt(false);
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                    aparenciaBtnInt(false);
                }
            });
        }

        private void configBtnEntrarComunidade(Comunidade comunidadeAtual) {
            if (comunidadeAtual.getSeguidores() != null
                    && comunidadeAtual.getSeguidores().size() > 0
                    && comunidadeAtual.getSeguidores().contains(idUsuarioLogado)) {
                SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Você já faz parte dessa comunidade!");
                btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
            } else if (comunidadeAtual.getSeguidores() != null
                    && comunidadeAtual.getSeguidores().size() > 0) {
                btnViewEntrarComunidade.setVisibility(View.VISIBLE);
                ArrayList<String> seguidores = new ArrayList<>();
                seguidores.addAll(comunidadeAtual.getSeguidores());
                seguidores.add(idUsuarioLogado);
                adicionarSeguidorRef = adicionarSeguidorRef.child(comunidadeAtual.getIdComunidade()).child("seguidores");
                adicionarSeguidorRef.setValue(seguidores).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Agora você faz parte dessa comunidade!");
                            btnViewEntrarComunidade.setVisibility(View.INVISIBLE);

                            int contadorSeguidor = comunidadeAtual.getSeguidores().size() + 1;

                            txtViewNrParticipantes.setText("" + contadorSeguidor);

                            verificaConvitesRef = verificaConvitesRef.child(idUsuarioLogado)
                                    .child(comunidadeAtual.getIdComunidade());

                            verificaConvitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        verificaConvitesRef.removeValue();
                                    }
                                    verificaConvitesRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        SnackbarUtils.showSnackbar(btnViewEntrarComunidade, "Ocorreu um erro ao tentar participar dessa comunidade," +
                                " tente novamente! " + e.getMessage());
                    }
                });
            } else {
                btnViewEntrarComunidade.setVisibility(View.INVISIBLE);
            }
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
