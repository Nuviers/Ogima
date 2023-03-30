package com.example.ogima.adapter;

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


    public AdapterTopicosGrupoPublico(Context c, ArrayList<String> listTopicos) {
        this.context = c;
        this.listaTopicos = listTopicos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_topicos_grupo, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ordenarTopicosAlfabeticamente();

        String topicoAtual = listaTopicos.get(position);

        holder.chipTopicoGrupo.setText(topicoAtual);

        holder.chipTopicoGrupo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ToastCustomizado.toastCustomizadoCurto("Checked " + compoundButton.getText().toString(), context);
                    listaTopicosSelecionados.add(compoundButton.getText().toString());
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Not Checked " + compoundButton.getText().toString(), context);
                    listaTopicosSelecionados.remove(compoundButton.getText().toString());
                }
                if (listaTopicosSelecionados != null && listaTopicosSelecionados.size() > 0) {
                    ToastCustomizado.toastCustomizadoCurto("Topico tamanho " + listaTopicosSelecionados.size(), context);
                }
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
}
