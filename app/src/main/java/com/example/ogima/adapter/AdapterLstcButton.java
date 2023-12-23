package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.CommunityActivity;
import com.example.ogima.activity.CommunityDetailsActivity;
import com.example.ogima.activity.UsersInviteCommunityActivity;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.model.Comunidade;

public class AdapterLstcButton extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String tipoTitulo = "";
    private String titulo = "";
    private boolean existemComunidades = false;
    private Context context;
    private AnimacaoIntent animacaoIntentListener;

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    public AdapterLstcButton(String tipoTitulo, boolean existemComunidades, Context c, AnimacaoIntent animacaoIntent) {
        this.tipoTitulo = tipoTitulo;
        this.existemComunidades = existemComunidades;
        this.context = c;
        this.animacaoIntentListener = animacaoIntent;
    }

    public void setExistemComunidades(boolean existemComunidades) {
        this.existemComunidades = existemComunidades;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lstc_button, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder holderPrincipal = (HeaderViewHolder) holder;
            if (existemComunidades) {
                holderPrincipal.linearLayoutLstcButton.setVisibility(View.VISIBLE);
                holderPrincipal.btnViewSeeCommunity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verComunidades();
                    }
                });
                holderPrincipal.cardViewTodasComunidades.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verComunidades();
                    }
                });
                holderPrincipal.btnViewVerTodasComunidades.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verComunidades();
                    }
                });
                holderPrincipal.imgViewVerTodasComunidades.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verComunidades();
                    }
                });
            } else {
                holderPrincipal.linearLayoutLstcButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutLstcButton;
        private Button btnViewSeeCommunity;
        private CardView cardViewTodasComunidades;
        private Button btnViewVerTodasComunidades;
        private ImageView imgViewVerTodasComunidades;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            btnViewSeeCommunity = itemView.findViewById(R.id.btnViewSeeCommunity);
            linearLayoutLstcButton = itemView.findViewById(R.id.linearLayoutLstcButton);
            cardViewTodasComunidades = itemView.findViewById(R.id.cardViewVerTodasComunidades);
            btnViewVerTodasComunidades = itemView.findViewById(R.id.btnViewVerTodasComunidades);
            imgViewVerTodasComunidades = itemView.findViewById(R.id.imgViewVerTodasComunidades);

            if (tipoTitulo == null || tipoTitulo.isEmpty()) {
                return;
            }

            if (!tipoTitulo.equals(CommunityUtils.ALL_COMMUNITIES)) {
                btnViewSeeCommunity.setVisibility(View.VISIBLE);
            }else{
                cardViewTodasComunidades.setVisibility(View.VISIBLE);
            }

            switch (tipoTitulo) {
                case CommunityUtils.MY_COMMUNITIES:
                    titulo = context.getString(R.string.see_all_your_communities);
                    break;
                case CommunityUtils.PUBLIC_COMMUNITIES:
                    titulo = context.getString(R.string.see_all_public_communities);
                    break;
                case CommunityUtils.COMMUNITIES_FOLLOWING:
                    titulo = context.getString(R.string.see_all_communities_following);
                    break;
                case CommunityUtils.RECOMMENDED_COMMUNITIES:
                    titulo = context.getString(R.string.see_all_recommended_communities);
                    break;
                case CommunityUtils.ALL_COMMUNITIES:
                    titulo = context.getString(R.string.see_all_communities);
                    break;
            }
            btnViewSeeCommunity.setText(FormatarContadorUtils.abreviarTexto(titulo, 50));
        }

        private void verComunidades() {
            Intent intent = new Intent(context, CommunityActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("tipoComunidade", tipoTitulo);
            context.startActivity(intent);
            animacaoIntentListener.onExecutarAnimacao();
        }
    }
}
