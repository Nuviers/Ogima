package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.parser.ColorParser;
import com.example.ogima.R;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.List;

public class AdapterAdmsComunidade extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaUsuarios;
    private Context context;
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private int hexAdm = -1;

    public AdapterAdmsComunidade(Context c, List<Usuario> listaUsuarioOrigem) {
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        hexAdm = context.getResources().getColor(R.color.friends_color);
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
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

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_adms_comunidade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;
            Usuario usuario = listaUsuarios.get(position);
            if (usuario.getMinhaFoto() != null && !usuario.getMinhaFoto().isEmpty()
                    && !usuario.isIndisponivel()) {
                holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
                GlideCustomizado.loadUrlComListener(context,
                        usuario.getMinhaFoto(), holderPrincipal.imgViewIncPhoto,
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
            String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(usuario);
            nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_NAME_LENGHT);
            holderPrincipal.txtViewIncName.setText(nomeConfigurado);

            holderPrincipal.imgViewIncPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    visitarPerfil(usuario, position);
                }
            });

            holderPrincipal.txtViewIncName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    visitarPerfil(usuario, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName;
        private SpinKitView spinKitLoadPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);

            if (hexAdm != -1) {
                imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexAdm));
            }
            txtViewIncName.setTextColor(Color.WHITE);
        }
    }

    private void visitarPerfil(Usuario usuarioAlvo, int posicao){
        String idDonoPerfil = usuarioAlvo.getIdUsuario();
        if (idDonoPerfil != null
                && !idDonoPerfil.isEmpty()
                && idUsuario != null && !idUsuario.isEmpty()
                && idDonoPerfil.equals(idUsuario)) {
            return;
        }
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                idDonoPerfil);
    }
}
