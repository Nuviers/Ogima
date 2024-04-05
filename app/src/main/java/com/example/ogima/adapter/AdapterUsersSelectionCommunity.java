package com.example.ogima.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AdapterUsersSelectionCommunity extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaUsuarios;
    private Context context;
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private HashMap<String, Object> listaDadosUser;
    private int hexSelecao = -1;
    private static final int MAX_LENGHT = 20;
    private HashMap<String, Object> listaAmigos;
    private long limiteSelecao = 0;
    private MarcarUsuarioCallback marcarUsuarioCallback;
    private DesmarcarUsuarioCallback desmarcarUsuarioCallback;
    public ArrayList<String> listaSelecao;

    public interface MarcarUsuarioCallback {
        void onMarcado();
    }

    public interface DesmarcarUsuarioCallback {
        void onDesmarcado();
    }

    public ArrayList<String> getListaSelecao() {
        return listaSelecao;
    }

    public void setListaSelecao(ArrayList<String> listaSelecao) {
        this.listaSelecao = listaSelecao;
    }

    public long getLimiteSelecao() {
        return limiteSelecao;
    }

    public void setLimiteSelecao(long limiteSelecao) {
        this.limiteSelecao = limiteSelecao;
        notifyDataSetChanged();
    }

    public AdapterUsersSelectionCommunity(Context c, List<Usuario> listaUsuarioOrigem,
                                          HashMap<String, Object> listaDadosUser, int hexSelecao, long limiteSelecao,
                                          MarcarUsuarioCallback listenerMarcarUsuario, DesmarcarUsuarioCallback listenerDesmarcarUsuario) {
        this.context = c;
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.listaDadosUser = listaDadosUser;
        this.hexSelecao = hexSelecao;
        this.limiteSelecao = limiteSelecao;
        this.marcarUsuarioCallback = listenerMarcarUsuario;
        this.desmarcarUsuarioCallback = listenerDesmarcarUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        listaSelecao = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_preview_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;
            Usuario usuario = listaUsuarios.get(position);
            String idUser = listaUsuarios.get(position).getIdUsuario();
            Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);
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

                if (listaSelecao != null && !listaSelecao.isEmpty()
                        && listaSelecao.contains(dadoUser.getIdUsuario())) {
                    String hexMarcado = "#6495ED"; // azul claro
                    int corMarcado = Color.parseColor(hexMarcado);
                    holderPrincipal.relativeLayoutPreview.setBackgroundColor(corMarcado);
                }else{
                    holderPrincipal.relativeLayoutPreview.setBackgroundColor(Color.WHITE);
                }

                holderPrincipal.relativeLayoutPreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String hexMarcado = "#6495ED"; // azul claro
                        int corMarcado = Color.parseColor(hexMarcado);
                        if (listaSelecao != null
                                && listaSelecao.size() > 0
                                && listaSelecao.contains(idUser)) {
                            //Desmarcar
                            listaSelecao.remove(idUser);
                            holderPrincipal.relativeLayoutPreview.setBackgroundColor(Color.WHITE);
                            desmarcarUsuarioCallback.onDesmarcado();
                        } else if (listaSelecao != null) {
                            //Marcar
                            if (listaSelecao.size() >= limiteSelecao) {
                                ToastCustomizado.toastCustomizadoCurto("Limite máximo de seleção de usuários atingido.", context);
                                return;
                            }
                            listaSelecao.add(idUser);
                            holderPrincipal.relativeLayoutPreview.setBackgroundColor(corMarcado);
                            marcarUsuarioCallback.onMarcado();
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName, txtViewIncDesc;
        private SpinKitView spinKitLoadPhoto;
        private RelativeLayout relativeLayoutPreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            txtViewIncDesc = itemView.findViewById(R.id.txtViewIncDesc);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            relativeLayoutPreview = itemView.findViewById(R.id.relativeLayoutPreview);
        }
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
