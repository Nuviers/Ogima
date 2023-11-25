package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.LobbyChatRandomActivity;
import com.example.ogima.activity.daily.AddDailyShortsActivity;
import com.example.ogima.activity.daily.UsersDailyShortsActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Comunidade;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class AdapterLstcTitleHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String tipoTitulo = "";
    private String titulo = "";
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
                 holderPrincipal.linearLayoutLstcTitle.setVisibility(View.VISIBLE);
             }else{
                 holderPrincipal.linearLayoutLstcTitle.setVisibility(View.GONE);
             }
         }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutLstcTitle;
        private TextView txtViewTitle;
        private ImageButton imgBtnLstcPublica;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutLstcTitle = itemView.findViewById(R.id.linearLayoutLstcTitle);
            txtViewTitle = itemView.findViewById(R.id.txtViewLstcTitle);
            imgBtnLstcPublica = itemView.findViewById(R.id.imgBtnLstcPublica);

            if (tipoTitulo == null || tipoTitulo.isEmpty()) {
                return;
            }

            switch (tipoTitulo) {
                case Comunidade.PUBLIC_COMMUNITIES:
                    imgBtnLstcPublica.setVisibility(View.VISIBLE);
                    titulo = Comunidade.PUBLIC_COMMUNITIES;
                    break;
                case Comunidade.COMMUNITIES_FOLLOWING:
                    titulo = Comunidade.COMMUNITIES_FOLLOWING;
                    break;
                case Comunidade.MY_COMMUNITIES:
                    titulo = Comunidade.MY_COMMUNITIES;
                    break;
                case Comunidade.RECOMMENDED_COMMUNITIES:
                    titulo = Comunidade.RECOMMENDED_COMMUNITIES;
                    break;
            }
            txtViewTitle.setText(FormatarContadorUtils.abreviarTexto(titulo, 40));
        }
    }
}
