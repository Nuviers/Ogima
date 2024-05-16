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
import com.example.ogima.activity.GroupActivity;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GroupUtils;

public class AdapterLstGrupoButton extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String tipoTitulo = "";
    private String titulo = "";
    private boolean existemGrupos = false;
    private Context context;
    private AnimacaoIntent animacaoIntentListener;

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    public AdapterLstGrupoButton(String tipoTitulo, boolean existemGrupos, Context c, AnimacaoIntent animacaoIntent) {
        this.tipoTitulo = tipoTitulo;
        this.existemGrupos = existemGrupos;
        this.context = c;
        this.animacaoIntentListener = animacaoIntent;
    }

    public void setExistemGrupos(boolean existemGrupos) {
        this.existemGrupos = existemGrupos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lst_grupo_button, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder holderPrincipal = (HeaderViewHolder) holder;
            if (existemGrupos) {
                holderPrincipal.linearLayoutLstcButton.setVisibility(View.VISIBLE);
                holderPrincipal.btnViewSeeGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verGrupos();
                    }
                });
                holderPrincipal.cardViewTodosGrupos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verGrupos();
                    }
                });
                holderPrincipal.btnViewVerTodosGrupos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verGrupos();
                    }
                });
                holderPrincipal.imgViewVerTodosGrupos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holderPrincipal.verGrupos();
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
        private Button btnViewSeeGroup;
        private CardView cardViewTodosGrupos;
        private Button btnViewVerTodosGrupos;
        private ImageView imgViewVerTodosGrupos;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            btnViewSeeGroup = itemView.findViewById(R.id.btnViewSeeGroup);
            linearLayoutLstcButton = itemView.findViewById(R.id.linearLayoutLstGrupoButton);
            cardViewTodosGrupos = itemView.findViewById(R.id.cardViewVerTodosGrupos);
            btnViewVerTodosGrupos = itemView.findViewById(R.id.btnViewVerTodosGrupos);
            imgViewVerTodosGrupos = itemView.findViewById(R.id.imgViewVerTodosGrupos);

            if (tipoTitulo == null || tipoTitulo.isEmpty()) {
                return;
            }

            if (!tipoTitulo.equals(GroupUtils.PUBLIC_GROUPS)) {
                btnViewSeeGroup.setVisibility(View.VISIBLE);
            }else{
                cardViewTodosGrupos.setVisibility(View.VISIBLE);
            }

            switch (tipoTitulo) {
                case GroupUtils.MY_GROUPS:
                    titulo = context.getString(R.string.see_all_your_communities);
                    break;
                case GroupUtils.PUBLIC_GROUPS:
                    titulo = context.getString(R.string.see_all_public_communities);
                    break;
                case GroupUtils.GROUPS_FOLLOWING:
                    titulo = context.getString(R.string.see_all_communities_following);
                    break;
                case GroupUtils.BLOCKED_GROUPS:
                    titulo = context.getString(R.string.see_all_communities_following);
                    break;
            }
            btnViewSeeGroup.setText(FormatarContadorUtils.abreviarTexto(titulo, 50));
        }

        private void verGrupos() {
            Intent intent = new Intent(context, GroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("tipoGrupo", tipoTitulo);
            context.startActivity(intent);
            animacaoIntentListener.onExecutarAnimacao();
        }
    }
}
