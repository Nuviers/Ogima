package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GroupDiffCallback;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AdapterPreviewGroup extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Grupo> listaGrupos;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private AnimacaoIntent animacaoIntentListener;
    private HashMap<String, Object> listaDadosGrupo;
    private int hexImagem = -1;
    private int hexSuperAmd = -1;
    private static final int MAX_LENGHT = 20;
    private RemoverGrupoListener removerGrupoListener;
    private String tipoGrupo = "";

    public AdapterPreviewGroup(Context c, List<Grupo> listaGrupoOrigem,
                               RecuperaPosicaoAnterior recuperaPosicaoListener,
                               AnimacaoIntent animacaoIntent,
                               HashMap<String, Object> listaDadosGrupo, int hexImagem,
                               RemoverGrupoListener removerGrupoListener, String tipoGrupo) {
        this.listaGrupos = listaGrupoOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.animacaoIntentListener = animacaoIntent;
        this.listaDadosGrupo = listaDadosGrupo;
        this.hexImagem = hexImagem;
        this.removerGrupoListener = removerGrupoListener;
        this.tipoGrupo = tipoGrupo;
        this.hexSuperAmd = context.getResources().getColor(R.color.group_super_adm);
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public interface RemoverGrupoListener {
        void onRemocao(Grupo grupoAlvo, int posicao, String tipoGrupo);
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void updateGrupoList(List<Grupo> listaGruposAtualizados, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        GroupDiffCallback diffCallback = new GroupDiffCallback(listaGrupos, listaGruposAtualizados);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaGrupos.clear();
        listaGrupos.addAll(listaGruposAtualizados);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_preview_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        Grupo grupo = listaGrupos.get(position);
        String idGroup = listaGrupos.get(position).getIdGrupo();
        Grupo dadosGroup = (Grupo) listaDadosGrupo.get(idGroup);
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                }
            }
        }
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;
            if (dadosGroup != null) {

                if (dadosGroup.getIdSuperAdmGrupo() != null
                        && !dadosGroup.getIdSuperAdmGrupo().isEmpty()
                        && dadosGroup.getIdSuperAdmGrupo().equals(idUsuario)
                        && hexSuperAmd != -1) {
                    holderPrincipal.imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexSuperAmd));
                } else if (!dadosGroup.getGrupoPublico()) {
                    holderPrincipal.imgBtnComunidadePrivada.setVisibility(View.VISIBLE);
                } else if (hexImagem != -1) {
                    holderPrincipal.imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexImagem));
                }

                if (dadosGroup.getTopicos() != null
                        && dadosGroup.getTopicos().size() > 0) {
                    //Otimizar pois desse jeito dá umas travadas.
                    if (holderPrincipal.linearLayoutTopicos.getChildCount() == 0) {
                        //Evita que os chips sejam recriados mais de uma vez,
                        //assim evitando travamentos.
                        for (String hobby : dadosGroup.getTopicos()) {
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

                if (dadosGroup.getFotoGrupo() != null && !dadosGroup.getFotoGrupo().isEmpty()
                        && !grupo.isIndisponivel()) {
                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
                    GlideCustomizado.loadUrlComListener(context,
                            dadosGroup.getFotoGrupo(), holderPrincipal.imgViewIncPhoto,
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
                } else {
                    CommunityUtils.exibirFotoPadrao(context, holderPrincipal.imgViewIncPhoto, UsuarioUtils.FIELD_PHOTO, true);
                }
                String nomeConfigurado = UsuarioUtils.recuperarNomeConfiguradoGrupo(dadosGroup);
                nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_COMMUNITY_NAME_LENGHT);
                holderPrincipal.txtViewIncName.setText(nomeConfigurado);
                if (dadosGroup.getDescricaoGrupo() != null && !dadosGroup.getDescricaoGrupo().isEmpty()) {
                    String descricaoConfigurada = dadosGroup.getDescricaoGrupo().trim();
                    descricaoConfigurada = FormatarContadorUtils.abreviarTexto(descricaoConfigurada, UsuarioUtils.MAX_COMMUNITY_PREVIEW_DESC_LENGHT);
                    holderPrincipal.txtViewIncDesc.setText(descricaoConfigurada);
                }

                holderPrincipal.imgViewIncPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarGrupo(v, grupo, position);
                    }
                });
                holderPrincipal.txtViewIncName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarGrupo(v, grupo, position);
                    }
                });
                holderPrincipal.txtViewIncDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarGrupo(v, grupo, position);
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
        return listaGrupos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName, txtViewIncDesc;
        private SpinKitView spinKitLoadPhoto;
        private ImageButton imgBtnComunidadePrivada;
        private LinearLayout linearLayoutTopicos;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            txtViewIncDesc = itemView.findViewById(R.id.txtViewIncDesc);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            imgBtnComunidadePrivada = itemView.findViewById(R.id.imgBtnComunidadePrivada);
            linearLayoutTopicos = itemView.findViewById(R.id.linearLayoutTopicosComunidade);
        }
    }

    private void visitarGrupo(View view, Grupo grupo, int position) {

        if (grupo.isIndisponivel()) {
            ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.community_unavailable), context);
            return;
        }
        FirebaseRecuperarUsuario.recoverGroup(grupo.getIdGrupo(), new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                Intent intent = new Intent(context, ComunidadePostagensActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("idGrupo", grupoAtual.getIdGrupo());
                context.startActivity(intent);
                recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
                animacaoIntentListener.onExecutarAnimacao();
            }

            @Override
            public void onNaoExiste() {
                SnackbarUtils.showSnackbar(view, context.getString(R.string.community_does_not_exist));
                removerGrupoListener.onRemocao(grupo, position, tipoGrupo);
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.error_visiting_community), context);
            }
        });
    }

    public int findPositionInList(String groupId) {
        for (int i = 0; i < listaGrupos.size(); i++) {
            Grupo group = listaGrupos.get(i);
            if (group.getIdGrupo().equals(groupId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}