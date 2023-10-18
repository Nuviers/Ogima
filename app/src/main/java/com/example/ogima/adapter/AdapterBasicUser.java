package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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


public class AdapterBasicUser extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    public AdapterBasicUser(Context c, List<Usuario> listaUsuarioOrigem,
                            RecuperaPosicaoAnterior recuperaPosicaoListener,
                            AnimacaoIntent animacaoIntent,
                            HashMap<String, Object> listDadosUser, HashMap<String, Object> listSeguindo) {
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.animacaoIntentListener = animacaoIntent;
        this.listaDadosUser = listDadosUser;
        this.listaSeguindo = listSeguindo;
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
            if (dadoUser != null) {
                if (listaSeguindo != null && listaSeguindo.size() > 0
                        && dadosSeguindo != null) {
                    if (dadosSeguindo.getIdUsuario().equals(dadoUser.getIdUsuario())) {
                        holderPrincipal.btnIntFoll.setText("Deixar de seguir");
                    } else {
                        holderPrincipal.btnIntFoll.setText("Seguir");
                    }
                } else {
                    holderPrincipal.btnIntFoll.setText("Seguir");
                }

                if (dadoUser.getMinhaFoto() != null && !dadoUser.getMinhaFoto().isEmpty()) {
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
                }
                String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(dadoUser);
                nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, 20);
                holderPrincipal.txtViewIncName.setText(nomeConfigurado);
                holderPrincipal.imgViewIncPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        visitarPerfil(usuario.getIdUsuario(), position);
                    }
                });
                holderPrincipal.txtViewIncName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarPerfil(usuario.getIdUsuario(), position);
                    }
                });
                holderPrincipal.btnIntFoll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listaSeguindo != null && listaSeguindo.size() > 0
                                && dadosSeguindo != null) {
                            if (dadosSeguindo.getIdUsuario().equals(dadoUser.getIdUsuario())) {
                                //Deixar de seguir
                                holderPrincipal.deixarDeSeguir(dadoUser.getIdUsuario());
                            } else {
                                //Seguir
                                holderPrincipal.seguir(dadoUser.getIdUsuario());
                            }
                        } else {
                            //Seguir
                            holderPrincipal.seguir(dadoUser.getIdUsuario());
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
        private Button btnIntFoll;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            btnIntFoll = itemView.findViewById(R.id.btnIntFoll);
        }

        private void deixarDeSeguir(String idSeguindo) {
            SeguindoUtils.removerSeguindo(idSeguindo, new SeguindoUtils.RemoverSeguindoCallback() {
                @Override
                public void onRemovido() {
                    ToastCustomizado.toastCustomizadoCurto("Deixou de seguir com sucesso", context);

                    btnIntFoll.setText("Seguir");
                    DatabaseReference atualizarSeguidoresRef
                            = firebaseRef.child("usuarios")
                            .child(idSeguindo).child("seguidoresUsuario");

                    DatabaseReference atualizarSeguindoRef
                            = firebaseRef.child("usuarios")
                            .child(idUsuario).child("seguindoUsuario");

                    atualizarContador.subtrairContador(atualizarSeguidoresRef, new AtualizarContador.AtualizarContadorCallback() {
                        @Override
                        public void onSuccess(int contadorAtualizado) {

                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });

                    atualizarContador.subtrairContador(atualizarSeguindoRef, new AtualizarContador.AtualizarContadorCallback() {
                        @Override
                        public void onSuccess(int contadorAtualizado) {

                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });
                }

                @Override
                public void onError(@NonNull String message) {

                }
            });
        }

        private void seguir(String idSeguir){
            SeguindoUtils.salvarSeguindo(idSeguir, new SeguindoUtils.SalvarSeguindoCallback() {
                @Override
                public void onSeguindoSalvo() {
                    ToastCustomizado.toastCustomizadoCurto("Seguindo com sucesso", context);
                    btnIntFoll.setText("Deixar de seguir");
                }

                @Override
                public void onError(@NonNull String message) {

                }
            });

            DatabaseReference atualizarSeguidoresRef
                    = firebaseRef.child("usuarios")
                    .child(idSeguir).child("seguidoresUsuario");

            DatabaseReference atualizarSeguindoRef
                    = firebaseRef.child("usuarios")
                    .child(idUsuario).child("seguindoUsuario");

            atualizarContador.acrescentarContador(atualizarSeguidoresRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    ToastCustomizado.toastCustomizadoCurto("Seguidores: " + contadorAtualizado, context);
                }

                @Override
                public void onError(String errorMessage) {

                }
            });

            atualizarContador.acrescentarContador(atualizarSeguindoRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    ToastCustomizado.toastCustomizadoCurto("Seguindo: " + contadorAtualizado, context);
                }

                @Override
                public void onError(String errorMessage) {

                }
            });
        }
    }

    private void visitarPerfil(String idDonoPerfil, int posicao) {
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