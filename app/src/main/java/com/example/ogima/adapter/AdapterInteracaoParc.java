package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class AdapterInteracaoParc extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Usuario> listaUsuarios;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private int proximaFoto = 0;
    private retomarExibicao retomarExibicaoListener;
    private CardStackLayoutManager layoutManager;

    public AdapterInteracaoParc(Context c, List<Usuario> listaUsuarioOrigem,
                                RecuperaPosicaoAnterior recuperaPosicaoListener, retomarExibicao retomarExibicao, CardStackLayoutManager layoutManager) {
        this.listaUsuarios = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        this.retomarExibicaoListener = retomarExibicao;
        this.layoutManager = layoutManager;
    }

    public interface retomarExibicao{
        void onRetomar();
    }

    public void updateUsuarioList(List<Usuario> listaUsuariosAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaUsuarios, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaUsuarios.clear();
        listaUsuarios.addAll(listaUsuariosAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_interacao_parc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {

        Usuario usuario = listaUsuarios.get(position);

        if (!payloads.isEmpty()) {

        } else {
            if (holder instanceof ViewHolder) {
                ViewHolder holderPrincipal = (ViewHolder) holder;
                GlideCustomizado.loadUrl(context, usuario.getFotosParc().get(0),
                        holderPrincipal.imgViewUser,
                        android.R.color.transparent,
                        GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia());

                holderPrincipal.imgViewUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        layoutManager.setVisibleCount(1);
                        GlideCustomizado.loadUrl(context, usuario.getFotosParc().get(1),
                                holderPrincipal.imgViewUser,
                                android.R.color.transparent,
                                GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia());
                        retomarExibicaoListener.onRetomar();
                    }
                });
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgViewUser = itemView.findViewById(R.id.imgViewUserIntParc);
        }
    }

    public void teste() {

    }
}
