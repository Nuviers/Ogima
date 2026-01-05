package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.model.Grupo;

public class AdapterLstGrupoTitleHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String tipoTitulo = "";
    private String titulo = "";
    private String msgSemGrupo = "";
    private boolean existemGrupos = false;
    private Context context;

    public AdapterLstGrupoTitleHeader(String tipoTitulo, boolean existemGrupos, Context c) {
        this.tipoTitulo = tipoTitulo;
        this.existemGrupos = existemGrupos;
        this.context = c;
    }

    public void setExistemGrupos(boolean existemGrupos) {
        this.existemGrupos = existemGrupos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lstc_title_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder holderPrincipal = (HeaderViewHolder) holder;
            if (existemGrupos) {
                holderPrincipal.txtViewMsgSemGrupos.setVisibility(View.GONE);
            } else  {
                holderPrincipal.txtViewMsgSemGrupos.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutLstcTitle;
        private TextView txtViewTitle, txtViewMsgSemGrupos;
        private ImageButton imgBtnLstcPublica;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutLstcTitle = itemView.findViewById(R.id.linearLayoutLstcTitle);
            txtViewTitle = itemView.findViewById(R.id.txtViewLstcTitle);
            imgBtnLstcPublica = itemView.findViewById(R.id.imgBtnLstcPublica);
            txtViewMsgSemGrupos = itemView.findViewById(R.id.txtViewMsgSemComunidades);

            if (tipoTitulo == null || tipoTitulo.isEmpty()) {
                return;
            }

            switch (tipoTitulo) {
                case Grupo.PUBLIC_GROUP:
                    imgBtnLstcPublica.setVisibility(View.VISIBLE);
                    titulo = Grupo.PUBLIC_GROUP;
                    msgSemGrupo = context.getString(R.string.no_public_group);
                    break;
                case Grupo.GROUP_FOLLOWING:
                    titulo = Grupo.GROUP_FOLLOWING;
                    msgSemGrupo = context.getString(R.string.not_following_group);
                    break;
                case Grupo.MY_GROUP:
                    titulo = Grupo.MY_GROUP;
                    msgSemGrupo = context.getString(R.string.you_dont_have_group);
                    break;
                case Grupo.BLOCKED_GROUP:
                    titulo = Grupo.BLOCKED_GROUP;
                    msgSemGrupo = context.getString(R.string.not_following_group);
                    break;
                case Grupo.ALL_GROUPS:
                    titulo = Grupo.ALL_GROUPS;
                    msgSemGrupo = context.getString(R.string.there_are_no_group);
                    break;
            }
            txtViewTitle.setText(FormatarContadorUtils.abreviarTexto(titulo, 40));
            txtViewMsgSemGrupos.setText(msgSemGrupo);
        }
    }
}
