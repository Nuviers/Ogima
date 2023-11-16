package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AdapterFriends extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
    private static final int MAX_LENGHT = 20;
    private String idDonoPerfil = "";
    private int corBotaoDesativado = -1;
    private RemoverAmigoListener removerAmigoListener;
    private HashMap<String, Object> listaAmigos;
    private String add = "", friends = "", unfriend = "";
    private boolean desfazerAmizade = false;

    public AdapterFriends(Context c, List<Usuario> listaUsuarioOrigem,
                          RecuperaPosicaoAnterior recuperaPosicaoListener,
                          AnimacaoIntent animacaoIntent,
                          HashMap<String, Object> listDadosUser, int hexImagem, String idDonoPerfil,
                          RemoverAmigoListener removerAmigoListener, HashMap<String, Object> listaAmigos) {
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.animacaoIntentListener = animacaoIntent;
        this.listaDadosUser = listDadosUser;
        this.hexImagem = hexImagem;
        this.idDonoPerfil = idDonoPerfil;
        this.removerAmigoListener = removerAmigoListener;
        this.listaAmigos = listaAmigos;
        this.add = "Add friend";
        this.friends = "Are already friends";
        this.unfriend = "Unfriend";
        this.corBotaoDesativado = context.getResources().getColor(R.color.gradient_button_disabled);
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public interface RemoverAmigoListener {
        void onRemocao(Usuario usuarioAlvo, int posicao);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_friends, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        Usuario usuario = listaUsuarios.get(position);
        String idUser = listaUsuarios.get(position).getIdUsuario();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);
        Usuario dadosAmizade = (Usuario) listaAmigos.get(idUser);
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

                if (listaAmigos != null && listaAmigos.size() > 0
                        && dadosAmizade != null) {
                    if (dadosAmizade.getIdUsuario().equals(dadoUser.getIdUsuario())) {
                        if (idDonoPerfil != null
                                && !idDonoPerfil.isEmpty()
                                && idUsuario != null && !idUsuario.isEmpty()
                                && idDonoPerfil.equals(idUsuario)) {
                            holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(unfriend, MAX_LENGHT));
                            desfazerAmizade = true;
                        } else {
                            holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(friends, MAX_LENGHT));
                            desfazerAmizade = false;
                        }
                    } else {
                        holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(add, MAX_LENGHT));
                        desfazerAmizade = false;
                    }
                } else if (idUsuario != null
                        && !idUsuario.isEmpty() &&
                        idDonoPerfil != null && !idDonoPerfil.isEmpty() &&
                        !idUsuario.equals(idDonoPerfil)) {
                    holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(add, MAX_LENGHT));
                    desfazerAmizade = false;
                } else {
                    holderPrincipal.btnIntPurple.setVisibility(View.GONE);
                    desfazerAmizade = false;
                }

                if (dadoUser.getMinhaFoto() != null && !dadoUser.getMinhaFoto().isEmpty()
                        && !usuario.isIndisponivel()) {
                    holderPrincipal.btnIntPurple.setVisibility(View.VISIBLE);
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
                    holderPrincipal.btnIntPurple.setVisibility(View.GONE);
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

                if (dadoUser.getIdUsuario() != null
                        && !dadoUser.getIdUsuario().isEmpty()
                        && idUsuario != null && !idUsuario.isEmpty()
                        && dadoUser.getIdUsuario().equals(idUsuario)) {
                    holderPrincipal.btnIntPurple.setVisibility(View.GONE);
                } else {
                    holderPrincipal.btnIntPurple.setVisibility(View.VISIBLE);
                    holderPrincipal.btnIntPurple.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (interacaoEmAndamento) {
                                ToastCustomizado.toastCustomizadoCurto("Aguarde um momento", context);
                                return;
                            }
                            holderPrincipal.aparenciaBtnInt(true);
                            holderPrincipal.analisarAmizade(dadoUser);
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

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            btnIntPurple = itemView.findViewById(R.id.btnIntPurple);
        }

        private void analisarAmizade(Usuario usuarioAlvo) {
            String idAlvo = usuarioAlvo.getIdUsuario();
            int posicao = getBindingAdapterPosition();
            FriendsUtils.VerificaAmizade(idAlvo, new FriendsUtils.VerificaAmizadeCallback() {
                @Override
                public void onAmigos() {
                    if (desfazerAmizade) {
                        //Desfazer amizade
                        FriendsUtils.desfazerAmizade(context, idAlvo, new FriendsUtils.DesfazerAmizadeCallback() {
                            @Override
                            public void onAmizadeDesfeita() {
                                aparenciaBtnInt(false);
                                ToastCustomizado.toastCustomizadoCurto("Amizade desfeita com sucesso", context);
                                removerAmigoListener.onRemocao(usuarioAlvo, posicao);
                            }

                            @Override
                            public void onError(@NonNull String message) {
                                aparenciaBtnInt(false);
                                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", "Ocorreu um erro ao desfazer a amizade. Tente novamente mais tarde:", message), context);
                            }
                        });
                    } else {
                        //Já são amigos
                        aparenciaBtnInt(false);
                        ToastCustomizado.toastCustomizadoCurto("Vocês já são amigos", context);
                        btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(friends, MAX_LENGHT));
                        desfazerAmizade = false;
                    }
                }

                @Override
                public void onNaoSaoAmigos() {
                    tratarConvite(idAlvo, posicao);
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                }
            });
        }

        private void tratarConvite(String idAlvo, int posicao) {
            if (!desfazerAmizade) {
                //Verificar se existe convite de amizade se não existir, enviar.
                FriendsUtils.VerificaConvite(idAlvo, new FriendsUtils.VerificaConviteCallback() {
                    @Override
                    public void onConvitePendente(boolean destinatario) {
                        if (destinatario) {
                            if (posicao != -1) {
                                FriendsUtils.adicionarAmigo(context, idAlvo, false, new FriendsUtils.AdicionarAmigoCallback() {
                                    @Override
                                    public void onConcluido() {
                                        ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.now_you_are_friends), context);
                                        aparenciaBtnInt(false);
                                        btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(friends, MAX_LENGHT));
                                        desfazerAmizade = false;
                                    }

                                    @Override
                                    public void onError(String message) {
                                        aparenciaBtnInt(false);
                                        ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.error_adding_friend, message), context);
                                    }
                                });
                            }
                        } else {
                            //Usuário atual já enviou um convite de amizade anteriormente
                            //não fazer nada além de mostrar um aviso.
                            ToastCustomizado.toastCustomizadoCurto("Convite de amizade já existe", context);
                        }
                    }

                    @Override
                    public void onSemConvites() {
                        aparenciaBtnInt(true);
                        //Enviar o convite
                        FriendsUtils.enviarConvite(context, idAlvo, new FriendsUtils.EnviarConviteCallback() {
                            @Override
                            public void onConviteEnviado() {
                                ToastCustomizado.toastCustomizadoCurto("Convite de amizade enviado com sucesso", context);
                                aparenciaBtnInt(false);
                            }

                            @Override
                            public void onJaExisteConvite() {
                                tratarConvite(idAlvo, posicao);
                            }

                            @Override
                            public void onError(String message) {
                                aparenciaBtnInt(false);
                                ToastCustomizado.toastCustomizado(message, context);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizado(String.format("%s %s", context.getString(R.string.an_error_has_occurred), message), context);
                    }
                });
            }
        }

        private void aparenciaBtnInt(boolean desativarBotao) {
            if (btnIntPurple != null) {
                if (desativarBotao) {
                    ButtonUtils.desativarBotao(btnIntPurple, corBotaoDesativado);
                    interacaoEmAndamento = true;
                } else {
                    ButtonUtils.ativarBotao(btnIntPurple);
                    interacaoEmAndamento = false;
                }
            }
        }
    }

    private void visitarPerfil(Usuario usuarioAlvo, int posicao) {
        String idDonoPerfil = usuarioAlvo.getIdUsuario();
        if (idDonoPerfil != null
                && !idDonoPerfil.isEmpty()
                && idUsuario != null && !idUsuario.isEmpty()
                && idDonoPerfil.equals(idUsuario)) {
            return;
        }
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