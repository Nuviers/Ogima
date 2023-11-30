package com.example.ogima.adapter;

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
import com.example.ogima.model.Comunidade;

public class AdapterLstcTitleHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String tipoTitulo = "";
    private String titulo = "";
    private String msgSemComunidade = "";
    private boolean existemComunidades = false;

    public AdapterLstcTitleHeader(String tipoTitulo, boolean existemComunidades) {
        this.tipoTitulo = tipoTitulo;
        this.existemComunidades = existemComunidades;
    }

    public void setExistemComunidades(boolean existemComunidades) {
        this.existemComunidades = existemComunidades;
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
         if(holder instanceof HeaderViewHolder){
             HeaderViewHolder holderPrincipal = (HeaderViewHolder) holder;
             if (existemComunidades) {
                 holderPrincipal.txtViewMsgSemComunidades.setVisibility(View.GONE);
             }else if(tipoTitulo != null && !tipoTitulo.isEmpty() &&
                     !tipoTitulo.equals(Comunidade.RECOMMENDED_COMMUNITY)){
                 holderPrincipal.txtViewMsgSemComunidades.setVisibility(View.VISIBLE);
             }
         }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutLstcTitle;
        private TextView txtViewTitle, txtViewMsgSemComunidades;
        private ImageButton imgBtnLstcPublica;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutLstcTitle = itemView.findViewById(R.id.linearLayoutLstcTitle);
            txtViewTitle = itemView.findViewById(R.id.txtViewLstcTitle);
            imgBtnLstcPublica = itemView.findViewById(R.id.imgBtnLstcPublica);
            txtViewMsgSemComunidades = itemView.findViewById(R.id.txtViewMsgSemComunidades);

            if (tipoTitulo == null || tipoTitulo.isEmpty()) {
                return;
            }

            switch (tipoTitulo) {
                case Comunidade.PUBLIC_COMMUNITY:
                    imgBtnLstcPublica.setVisibility(View.VISIBLE);
                    titulo = Comunidade.PUBLIC_COMMUNITY;
                    msgSemComunidade = "Não existem comunidades públicas no momento.";
                    break;
                case Comunidade.COMMUNITY_FOLLOWING:
                    titulo = Comunidade.COMMUNITY_FOLLOWING;
                    msgSemComunidade = "Você não está seguindo nenhuma comunidade no momento.";
                    break;
                case Comunidade.MY_COMMUNITY:
                    titulo = Comunidade.MY_COMMUNITY;
                    msgSemComunidade = "Você não possui comunidades.";
                    break;
                case Comunidade.RECOMMENDED_COMMUNITY:
                    titulo = Comunidade.RECOMMENDED_COMMUNITY;
                    break;
            }
            txtViewTitle.setText(FormatarContadorUtils.abreviarTexto(titulo, 40));
            txtViewMsgSemComunidades.setText(msgSemComunidade);
        }
    }
}
