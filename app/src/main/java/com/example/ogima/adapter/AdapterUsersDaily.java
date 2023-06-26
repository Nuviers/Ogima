package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.DailyShortDiffCallback;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class AdapterUsersDaily extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaUsuariosDaily;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;

    public AdapterUsersDaily(Context c, List<Usuario> listaUsuarioOrigem,
                             RecuperaPosicaoAnterior recuperaPosicaoListener) {
        this.listaUsuariosDaily = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
    }

    public void updateDailyShortList(List<Usuario> listaUsuariosAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaUsuariosDaily, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaUsuariosDaily.clear();
        listaUsuariosDaily.addAll(listaUsuariosAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_daily_short, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder holderPrincipal = (ViewHolder) holder;

            Usuario usuarioDaily = listaUsuariosDaily.get(position);


            GlideCustomizado.montarGlide(context, usuarioDaily.getMinhaFoto(), holderPrincipal.imgViewFotoUserDaily,
                    android.R.color.transparent);


            GlideCustomizado.montarGlideFoto(context, usuarioDaily.getUrlLastDaily(), holderPrincipal.imgViewDailyUser,
                    android.R.color.transparent);

            //holderPrincipal.txtViewNomeUserDaily.setText(usuarioDaily.getNomeUser());
            //holderPrincipal.txtViewLastTimeDaily.setText(usuarioDaily.getHoraDaily());

            if (usuarioDaily.getTipoMidia() != null) {
                if (usuarioDaily.getTipoMidia().equals("video")) {
                    holderPrincipal.imgBtnSinalizaVideo.setVisibility(View.VISIBLE);
                }else{
                    holderPrincipal.imgBtnSinalizaVideo.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuariosDaily.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgViewFotoUserDaily, imgViewDailyUser;
        private TextView txtViewNomeUserDaily, txtViewLastTimeDaily;
        private ImageButton imgBtnSinalizaVideo;

        public ViewHolder(View itemView) {
            super(itemView);

            imgViewFotoUserDaily = itemView.findViewById(R.id.imgViewFotoUserDaily);
            txtViewNomeUserDaily = itemView.findViewById(R.id.txtViewNomeUserDaily);
            txtViewLastTimeDaily = itemView.findViewById(R.id.txtViewLastTimeDaily);
            imgViewDailyUser = itemView.findViewById(R.id.imgViewDailyUser);
            imgBtnSinalizaVideo = itemView.findViewById(R.id.imgBtnSinalizaVideo);
        }
    }
}
