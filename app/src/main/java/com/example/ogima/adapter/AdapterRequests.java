package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.ButtonUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FriendsUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AdapterRequests extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaUsuarios;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario;
    private boolean statusEpilepsia = true;
    private AnimacaoIntent animacaoIntentListener;
    private HashMap<String, Object> listaDadosUser;
    private AtualizarContador atualizarContador = new AtualizarContador();
    private Handler segHandler = new Handler();
    private int queryDelayMillis = 1000;
    private boolean interacaoEmAndamento = false;
    private int hexImagem = -1;
    private String requests = "", friends = "";
    private static final int MAX_LENGHT = 20;
    private String idDonoPerfil = "";
    private int corBotaoDesativado = -1;
    private RemoverConviteListener removerConviteListener;

    public AdapterRequests(Context c, List<Usuario> listaUsuarioOrigem,
                           RecuperaPosicaoAnterior recuperaPosicaoListener,
                           AnimacaoIntent animacaoIntent,
                           HashMap<String, Object> listDadosUser, int hexImagem, String idDonoPerfil,
                           RemoverConviteListener removerConviteListener) {
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.animacaoIntentListener = animacaoIntent;
        this.listaDadosUser = listDadosUser;
        this.hexImagem = hexImagem;
        this.idDonoPerfil = idDonoPerfil;
        this.removerConviteListener = removerConviteListener;
        this.requests = context.getString(R.string.requests);
        this.friends = context.getString(R.string.friends);
        this.corBotaoDesativado = context.getResources().getColor(R.color.gradient_button_disabled);
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public interface RemoverConviteListener {
        void onRemocao(Usuario usuarioRemetente, int posicao);
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void updateUsersList(List<Usuario> listaUsuariosAtualizada, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaUsuarios, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaUsuarios.clear();
        listaUsuarios.addAll(listaUsuariosAtualizada);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_friend_interactions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        Usuario usuario = listaUsuarios.get(position);
        String idUser = listaUsuarios.get(position).getIdUsuario();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                }
            }
        }
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;

            if (hexImagem != -1) {
                holderPrincipal.imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexImagem));
            }

            if (dadoUser != null) {
                if (dadoUser.getMinhaFoto() != null && !dadoUser.getMinhaFoto().isEmpty()
                        && !usuario.isIndisponivel()) {
                    holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
                    GlideCustomizado.loadUrlComListener(context,
                            dadoUser.getMinhaFoto(), holderPrincipal.imgViewIncPhoto,
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
                    UsuarioUtils.exibirFotoPadrao(context, holderPrincipal.imgViewIncPhoto, UsuarioUtils.FIELD_PHOTO, true);
                }
                String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(dadoUser);
                nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_NAME_LENGHT);
                holderPrincipal.txtViewIncName.setText(nomeConfigurado);

                holderPrincipal.imgViewIncPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        visitarPerfil(usuario, position);
                    }
                });
                holderPrincipal.txtViewIncName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarPerfil(usuario, position);
                    }
                });

                if (!idUsuario.isEmpty() && !idDonoPerfil.isEmpty()
                        && !idUsuario.equals(idDonoPerfil)) {
                    //Ignorar lógica de interações já que o usuário atual
                    //não é dono do perfil.
                    holderPrincipal.btnIntPurple.setVisibility(View.GONE);
                    holderPrincipal.imgBtnRecusarConvite.setVisibility(View.GONE);
                } else {
                    holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto("Aceitar convite", MAX_LENGHT));
                    holderPrincipal.btnIntPurple.setVisibility(View.VISIBLE);
                    holderPrincipal.btnIntPurple.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (interacaoEmAndamento) {
                                ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                                return;
                            }
                            holderPrincipal.aparenciaBtnInt(true);
                            holderPrincipal.analisarConvite(dadoUser, true);
                        }
                    });

                    holderPrincipal.imgBtnRecusarConvite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (interacaoEmAndamento) {
                                ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                                return;
                            }
                            holderPrincipal.aparenciaImgBtn(true);
                            holderPrincipal.analisarConvite(dadoUser, false);
                        }
                    });
                }
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName;
        private SpinKitView spinKitLoadPhoto;
        private Button btnIntPurple;
        private ImageButton imgBtnRecusarConvite;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            btnIntPurple = itemView.findViewById(R.id.btnIntPurple);
            imgBtnRecusarConvite = itemView.findViewById(R.id.imgBtnRecusarConvite);
        }

        private void analisarConvite(Usuario usuarioAlvo, boolean adicionar) {
            String idAlvo = usuarioAlvo.getIdUsuario();
            int posicao = getBindingAdapterPosition();
            FriendsUtils.VerificaConvite(idAlvo, new FriendsUtils.VerificaConviteCallback() {
                @Override
                public void onConvitePendente(boolean destinatario) {
                    if (!adicionar) {
                        FriendsUtils.removerConvites(context, idAlvo, false, true, new FriendsUtils.RemoverConviteCallback() {
                            @Override
                            public void onRemovido(HashMap<String, Object> operacoes) {
                                if (posicao != -1) {
                                    removerConviteListener.onRemocao(usuarioAlvo, posicao);
                                    ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.rejected_friendship_invitation), context);
                                    aparenciaImgBtn(false);
                                }
                            }

                            @Override
                            public void onError(String message) {
                                aparenciaBtnInt(false);
                                ToastCustomizado.toastCustomizado(String.format("%s %s %s", context.getString(R.string.error_declining_friend_invitation), ":", message), context);
                            }
                        });
                    } else {
                        UsuarioUtils.checkBlockingStatus(context, idAlvo, new UsuarioUtils.CheckLockCallback() {
                            @Override
                            public void onBlocked(boolean status) {
                                if (!status) {
                                    FriendsUtils.adicionarAmigo(context, idAlvo, false, new FriendsUtils.AdicionarAmigoCallback() {
                                        @Override
                                        public void onConcluido() {
                                            ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.now_you_are_friends), context);
                                            removerConviteListener.onRemocao(usuarioAlvo, posicao);
                                            aparenciaBtnInt(false);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            aparenciaBtnInt(false);
                                            ToastCustomizado.toastCustomizado(context.getString(R.string.error_adding_friend, message), context);
                                        }
                                    });
                                }else{
                                    ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.user_unavailable), context);
                                }
                            }

                            @Override
                            public void onError(String message) {
                                ToastCustomizado.toastCustomizado(context.getString(R.string.error_adding_friend, message), context);
                            }
                        });
                    }
                }

                @Override
                public void onSemConvites() {
                    aparenciaBtnInt(false);
                    ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.expired_friend_invitation), context);
                    if (posicao != -1) {
                        removerConviteListener.onRemocao(usuarioAlvo, posicao);
                    }
                }

                @Override
                public void onError(String message) {
                    aparenciaBtnInt(false);
                    ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                }
            });
        }

        private void aparenciaBtnInt(boolean desativarBotao) {
            if (btnIntPurple != null) {
                if (desativarBotao) {
                    ButtonUtils.desativarBotaoDegrade(btnIntPurple, corBotaoDesativado);
                    interacaoEmAndamento = true;
                } else {
                    ButtonUtils.ativarBotaoDegrade(btnIntPurple);
                    interacaoEmAndamento = false;
                }
            }
        }

        private void aparenciaImgBtn(boolean desativarBotao) {
            if (imgBtnRecusarConvite != null) {
                if (desativarBotao) {
                    imgBtnRecusarConvite.setEnabled(false);
                    ImageViewCompat.setImageTintList(imgBtnRecusarConvite, ColorStateList.valueOf(Color.WHITE));
                    interacaoEmAndamento = true;
                } else {
                    imgBtnRecusarConvite.setEnabled(true);
                    ImageViewCompat.setImageTintList(imgBtnRecusarConvite, null);
                    interacaoEmAndamento = false;
                }
            }
        }
    }

    private void visitarPerfil(Usuario usuarioAlvo, int posicao) {
        String idDonoPerfil = usuarioAlvo.getIdUsuario();
        recuperaPosicaoAnteriorListener.onPosicaoAnterior(posicao);
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                idDonoPerfil);
        animacaoIntentListener.onExecutarAnimacao();
    }

    public int findPositionInList(String userId) {
        for (int i = 0; i < listaUsuarios.size(); i++) {
            Usuario user = listaUsuarios.get(i);
            if (user.getIdUsuario().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}