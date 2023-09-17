package com.example.ogima.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterEsconderPerfilParc extends RecyclerView.Adapter<AdapterEsconderPerfilParc.ViewHolder> {

    private boolean statusEpilepsia = true;
    private HashMap<String, Boolean> statusMarcacao;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private List<Usuario> listaUsuarios;
    private Context context;
    private RemoverUsuarioListener removerUsuarioListener;
    public boolean filtragem = false;
    private HashMap<String, Object> listaDadosUser;
    private ArrayList<String> idsMarcadosEdit;
    private boolean idsEditConfigurado = false;
    private int contador = 0;

    public ArrayList<String> getIdsMarcadosEdit() {
        return idsMarcadosEdit;
    }

    public void setIdsMarcadosEdit(ArrayList<String> idsMarcadosEdit) {
        this.idsMarcadosEdit = idsMarcadosEdit;
    }

    public AdapterEsconderPerfilParc(Context c, List<Usuario> listUsuarios, RemoverUsuarioListener removerUsuarioListener, HashMap<String, Object> listDadosUser,
                                     HashMap<String, Boolean> statusSelecao) {
        this.statusMarcacao = statusSelecao;
        this.listaUsuarios = listUsuarios = new ArrayList<>();
        this.context = c;
        this.removerUsuarioListener = removerUsuarioListener;
        this.listaDadosUser = listDadosUser;
    }

    public boolean isFiltragem() {
        return filtragem;
    }

    public void setFiltragem(boolean filtragem) {
        this.filtragem = filtragem;
    }


    public void updateUsuarioList(List<Usuario> listaUsuariosAtualizados, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaUsuarios, listaUsuariosAtualizados);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaUsuarios.clear();
        listaUsuarios.addAll(listaUsuariosAtualizados);

        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public interface RemoverUsuarioListener {
        void onUsuarioRemocao(Usuario usuarioRemovido, int posicao);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View viewHolder = inflater.inflate(R.layout.adapter_esconder_perfil, parent, false);
        return new ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Usuario usuarioAtual = listaUsuarios.get(position);
        String idUser = usuarioAtual.getIdUsuario();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);

        if (!idsEditConfigurado && getIdsMarcadosEdit() != null
                && getIdsMarcadosEdit().size() > 0) {

            if (getIdsMarcadosEdit().contains(idUser)) {
                contador++;
                statusMarcacao.put(idUser, true);
                holder.btnEsconderPerfil.setVisibility(View.GONE);
                holder.btnDesfazerEsconder.setVisibility(View.VISIBLE);
            }

            if (contador >= getIdsMarcadosEdit().size()) {
                idsEditConfigurado = true;
            }
        }

        if (statusMarcacao != null && !statusMarcacao.containsKey(idUser)) {
            ToastCustomizado.toastCustomizadoCurto("Id " + idUser, context);
            statusMarcacao.put(idUser, false);
        }
        if (dadoUser != null) {
            GlideCustomizado.loadUrl(context,
                    dadoUser.getMinhaFoto(), holder.imgViewFoto,
                    android.R.color.transparent, GlideCustomizado.CIRCLE_CROP,
                    false, isStatusEpilepsia());
            String nomeUsuario = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(dadoUser.getNomeUsuario());
            holder.txtViewNome.setText(nomeUsuario);
        }

        if (statusMarcacao != null && statusMarcacao.get(idUser) == true) {
            holder.btnDesfazerEsconder.setVisibility(View.VISIBLE);
            holder.btnEsconderPerfil.setVisibility(View.GONE);
        } else {
            holder.btnEsconderPerfil.setVisibility(View.VISIBLE);
            holder.btnDesfazerEsconder.setVisibility(View.GONE);
        }

        holder.btnEsconderPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusMarcacao.put(idUser, true);
                holder.btnEsconderPerfil.setVisibility(View.GONE);
                holder.btnDesfazerEsconder.setVisibility(View.VISIBLE);
                Log.d("Marcacao", "Id " + statusMarcacao.get(idUser));
            }
        });

        holder.btnDesfazerEsconder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusMarcacao.put(idUser, false);
                holder.btnDesfazerEsconder.setVisibility(View.GONE);
                holder.btnEsconderPerfil.setVisibility(View.VISIBLE);
                Log.d("Marcacao", "Id " + statusMarcacao.get(idUser));
            }
        });

        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFoto;
        private TextView txtViewNome;
        private Button btnEsconderPerfil, btnDesfazerEsconder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFoto = itemView.findViewById(R.id.imgViewFotoPerfilChat);
            txtViewNome = itemView.findViewById(R.id.txtViewNomePerfilChat);
            btnEsconderPerfil = itemView.findViewById(R.id.btnEsconderPerfil);
            btnDesfazerEsconder = itemView.findViewById(R.id.btnDesfazerEsconder);
        }
    }
}
