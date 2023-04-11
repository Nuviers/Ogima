package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.ToastCustomizado;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterTopicosGrupoPublico extends RecyclerView.Adapter<AdapterTopicosGrupoPublico.MyViewHolder> {

    private Context context;
    private ArrayList<String> listaTopicos;
    private List<String> listaTopicosSelecionados = new ArrayList<>();
    private ArrayList<Boolean> mChipStates;

    public AdapterTopicosGrupoPublico(Context c, ArrayList<String> listTopicos) {
        this.context = c;
        this.listaTopicos = listTopicos;
        mChipStates = new ArrayList<>(Collections.nCopies(listTopicos.size(), false));
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_topicos_grupo, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ordenarTopicosAlfabeticamente();

        String topicoAtual = listaTopicos.get(position);
        Boolean isChecked = mChipStates.get(position);

        holder.chipTopicoGrupo.setText(topicoAtual);
        holder.chipTopicoGrupo.setChecked(isChecked);

        holder.chipTopicoGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChipStates.get(position)) { // se o chip estava marcado
                    mChipStates.set(position, false); // define como não marcado
                    listaTopicosSelecionados.remove(topicoAtual);
                } else {
                    mChipStates.set(position, true); // define como marcado
                    listaTopicosSelecionados.add(topicoAtual);
                }
                notifyItemChanged(position); // atualiza a exibição do item
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTopicos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private Chip chipTopicoGrupo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            chipTopicoGrupo = itemView.findViewById(R.id.chipTopicoGrupo);
        }
    }

    private void ordenarTopicosAlfabeticamente() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            listaTopicos.sort(String::compareToIgnoreCase);
        } else {
            Collections.sort(listaTopicos, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });
        }
    }

    public List<String> getListaTopicosSelecionados() {
        return listaTopicosSelecionados;
    }

    public void limparTopicosFiltrados() {

        ArrayList<Boolean> newChipStates = new ArrayList<>(Collections.nCopies(mChipStates.size(), false));
        // Substitui o ArrayList atual pelo novo
        mChipStates.clear();
        mChipStates.addAll(newChipStates);

        // Limpa a lista de topicos selecionados
        listaTopicosSelecionados.clear();

        // Atualiza a exibição dos chips
        notifyDataSetChanged();
    }
}
