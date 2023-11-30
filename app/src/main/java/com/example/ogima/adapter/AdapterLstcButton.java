package com.example.ogima.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.model.Comunidade;

public class AdapterLstcButton extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String tipoTitulo = "";
    private String titulo = "";
    private boolean existemComunidades = false;

    public AdapterLstcButton(String tipoTitulo, boolean existemComunidades) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lstc_button, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
       if(holder instanceof HeaderViewHolder){
           HeaderViewHolder holderPrincipal = (HeaderViewHolder) holder;
           if (existemComunidades) {
               holderPrincipal.linearLayoutLstcButton.setVisibility(View.VISIBLE);
           }else{
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

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            btnViewSeeCommunity = itemView.findViewById(R.id.btnViewSeeCommunity);
            linearLayoutLstcButton = itemView.findViewById(R.id.linearLayoutLstcButton);

            if (tipoTitulo == null || tipoTitulo.isEmpty()) {
                return;
            }

            switch (tipoTitulo) {
                case Comunidade.MY_COMMUNITY:
                    titulo = "Ver todas as suas comunidades";
                    break;
                case Comunidade.PUBLIC_COMMUNITY:
                    titulo = "Ver todas as comunidades públicas";
                    break;
                case Comunidade.COMMUNITY_FOLLOWING:
                    titulo = "Ver todas as comunidades que você segue";
                    break;
                case Comunidade.RECOMMENDED_COMMUNITY:
                    titulo = "Ver todas as comunidades recomendadas";
                    break;
            }
            btnViewSeeCommunity.setText(FormatarContadorUtils.abreviarTexto(titulo, 50));
        }

        private void verComunidades(){

        }
    }
}
