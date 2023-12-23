package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.ToastCustomizado;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterCommunityFilters extends RecyclerView.Adapter<AdapterCommunityFilters.ViewHolder> {

    private Context context;
    private ArrayList<String> listaFiltros;
    public ArrayList<String> listaSelecao;
    private MarcarTopicoCallback marcarTopicoCallback;
    private DesmarcarTopicoCallback desmarcarTopicoCallback;
    private long limiteSelecao = 0;
    private boolean primeiroCarregamento = true;

    public interface MarcarTopicoCallback {
        void onMarcado();
    }

    public interface DesmarcarTopicoCallback {
        void onDesmarcado();
    }

    public ArrayList<String> getListaSelecao() {
        return listaSelecao;
    }

    public void setListaSelecao(ArrayList<String> listaSelecao) {
        this.listaSelecao = listaSelecao;
    }

    public AdapterCommunityFilters(Context c, ArrayList<String> listFiltros, long limiteSelecao,
                                   MarcarTopicoCallback marcarTopico,
                                   DesmarcarTopicoCallback desmarcarTopico) {
        this.context = c;
        this.limiteSelecao = limiteSelecao;
        this.listaFiltros = listFiltros;
        this.marcarTopicoCallback = marcarTopico;
        this.desmarcarTopicoCallback = desmarcarTopico;
        this.listaSelecao = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_topicos_grupo, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ordenarTopicosAlfabeticamente();
        String topicoAtual = listaFiltros.get(position);
        holder.chipFiltro.setText(topicoAtual);
        holder.chipFiltro.setChecked(listaSelecao.contains(topicoAtual));
        holder.chipFiltro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaSelecao.contains(topicoAtual)) {
                    //Desmarcar
                    listaSelecao.remove(topicoAtual);
                    desmarcarTopicoCallback.onDesmarcado();
                } else {
                    if(listaSelecao.size() >= limiteSelecao){
                        ToastCustomizado.toastCustomizadoCurto("Somente um filtro por vez pode ser selecionado.", context.getApplicationContext());
                        return;
                    }
                    //Marcar
                    listaSelecao.add(topicoAtual);
                    marcarTopicoCallback.onMarcado();
                }
                holder.chipFiltro.setChecked(listaSelecao.contains(topicoAtual));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltros.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Chip chipFiltro;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chipFiltro = itemView.findViewById(R.id.chipTopicoGrupo);
        }
    }

    private void ordenarTopicosAlfabeticamente() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            listaFiltros.sort(String::compareToIgnoreCase);
        } else {
            Collections.sort(listaFiltros, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });
        }
    }
}
