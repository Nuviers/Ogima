package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
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
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.SeguindoUtils;
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


public class AdapterFoll extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaUsuarios;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario;
    private boolean statusEpilepsia = true;
    private AnimacaoIntent animacaoIntentListener;
    private HashMap<String, Object> listaDadosUser;
    private HashMap<String, Object> listaSeguindo;
    public boolean filtragem = false;
    private AtualizarContador atualizarContador = new AtualizarContador();
    private DeixouDeSeguirCallback deixouDeSeguirCallback;
    private boolean interacaoEmAndamento = false;
    private int hexImagem = -1;
    private String follow = "", unfollow = "";
    private static final int MAX_LENGHT = 20;
    private int corBotaoDesativado = -1;

    public AdapterFoll(Context c, List<Usuario> listaUsuarioOrigem,
                       RecuperaPosicaoAnterior recuperaPosicaoListener,
                       AnimacaoIntent animacaoIntent,
                       HashMap<String, Object> listDadosUser, HashMap<String, Object> listSeguindo,
                       DeixouDeSeguirCallback deixouDeSeguirCallback, int hexImagem) {
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.animacaoIntentListener = animacaoIntent;
        this.listaDadosUser = listDadosUser;
        this.listaSeguindo = listSeguindo;
        this.hexImagem = hexImagem;
        this.deixouDeSeguirCallback = deixouDeSeguirCallback;
        this.follow = context.getString(R.string.follow);
        this.unfollow = context.getString(R.string.unfollow);
        this.corBotaoDesativado = context.getResources().getColor(R.color.gradient_button_disabled);
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public boolean isFiltragem() {
        return filtragem;
    }

    public void setFiltragem(boolean filtragem) {
        this.filtragem = filtragem;
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

    public interface DeixouDeSeguirCallback {
        void onRemover(Usuario usuarioAlvo);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_basic_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        Usuario usuario = listaUsuarios.get(position);
        String idUser = listaUsuarios.get(position).getIdUsuario();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);
        Usuario dadosSeguindo = (Usuario) listaSeguindo.get(idUser);
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
                if (listaSeguindo != null && listaSeguindo.size() > 0
                        && dadosSeguindo != null) {
                    if (dadosSeguindo.getIdUsuario().equals(dadoUser.getIdUsuario())) {
                        holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(unfollow, MAX_LENGHT));
                    } else {
                        holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(follow, MAX_LENGHT));
                    }
                } else {
                    holderPrincipal.btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(follow, MAX_LENGHT));
                }

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

                if (dadoUser.getIdUsuario() != null
                        && !dadoUser.getIdUsuario().isEmpty()
                        && idUsuario != null && !idUsuario.isEmpty()
                        && dadoUser.getIdUsuario().equals(idUsuario)) {
                    holderPrincipal.btnIntPurple.setVisibility(View.GONE);
                } else {
                    holderPrincipal.btnIntPurple.setVisibility(View.VISIBLE);
                }

                holderPrincipal.btnIntPurple.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (interacaoEmAndamento) {
                            ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                            return;
                        }
                        holderPrincipal.aparenciaBtnInt(true);
                        if (listaSeguindo != null && listaSeguindo.size() > 0
                                && dadosSeguindo != null) {
                            if (dadosSeguindo.getIdUsuario().equals(dadoUser.getIdUsuario())) {
                                //Deixar de seguir
                                holderPrincipal.deixarDeSeguir(dadoUser, holderPrincipal.btnIntPurple);
                            } else {
                                //Seguir
                                holderPrincipal.seguir(dadoUser, holderPrincipal.btnIntPurple);
                            }
                        } else {
                            //Seguir
                            holderPrincipal.seguir(dadoUser, holderPrincipal.btnIntPurple);
                        }
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

        private void deixarDeSeguir(Usuario usuarioAlvo, Button btnAlvo) {
            String idSeguindo = usuarioAlvo.getIdUsuario();
            SeguindoUtils.removerSeguindo(context, idSeguindo, new SeguindoUtils.RemoverSeguindoCallback() {
                @Override
                public void onRemovido() {
                    //ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.unfollowed_successfully), context);
                    //btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(follow, MAX_LENGHT));
                    deixouDeSeguirCallback.onRemover(usuarioAlvo);
                    aparenciaBtnInt(false);
                }

                @Override
                public void onError(@NonNull String message) {
                    ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.error_when_unfollowing), context);
                    aparenciaBtnInt(false);
                }
            });
        }

        private void seguir(Usuario usuarioAlvo, Button btnAlvo) {
            UsuarioUtils.checkBlockingStatus(context, usuarioAlvo.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                @Override
                public void onBlocked(boolean status) {
                    if (!status) {
                        String idSeguir = usuarioAlvo.getIdUsuario();
                        SeguindoUtils.salvarSeguindo(context, idSeguir, new SeguindoUtils.SalvarSeguindoCallback() {
                            @Override
                            public void onSeguindoSalvo() {
                                //ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.successfully_following), context);
                                //btnIntPurple.setText(FormatarContadorUtils.abreviarTexto(unfollow, MAX_LENGHT));
                                aparenciaBtnInt(false);
                            }

                            @Override
                            public void onError(@NonNull String message) {
                                ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.error_when_following), context);
                                aparenciaBtnInt(false);
                            }
                        });
                    } else {
                        ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.user_unavailable), context);
                        aparenciaBtnInt(false);
                    }
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.error_when_following), context);
                    aparenciaBtnInt(false);
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