package com.example.ogima.helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterCommunityFilters;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment implements AdapterCommunityFilters.MarcarTopicoCallback, AdapterCommunityFilters.DesmarcarTopicoCallback {

    private RecyclerView recyclerView;
    private AdapterCommunityFilters adapter;
    private String[] topicosComunidade;
    private ArrayList<String> listaTopicosComunidade;
    private ArrayList<String> listaSelecao;
    public RecuperarFiltrosCallback recuperarFiltrosCallback;
    private Button btnViewFiltrar;

    public interface RecuperarFiltrosCallback {
        void onRecuperado(ArrayList<String> listaFiltrosRecuperados);

        void onSemFiltros();
    }

    public CustomBottomSheetDialogFragment(RecuperarFiltrosCallback recuperarFiltrosCallback) {
        this.listaSelecao = new ArrayList<>();
        this.recuperarFiltrosCallback = recuperarFiltrosCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_community_filters, container, false);
        inicializarComponentes(view);
        topicosComunidade = getResources().getStringArray(R.array.interests_array);
        listaTopicosComunidade = new ArrayList<>(Arrays.asList(topicosComunidade));
        configRecycler();
        clickListeners();
        return view;
    }

    private void configRecycler() {
        if (adapter == null) {
            adapter = new AdapterCommunityFilters(requireContext(),
                    listaTopicosComunidade, 900, this, this);
            recyclerView.setHasFixedSize(true);
            ChipsLayoutManager layoutManager = ChipsLayoutManager.newBuilder(requireContext())
                    .setOrientation(ChipsLayoutManager.VERTICAL)
                    .setMaxViewsInRow(3)
                    .build();
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        }
    }

    private void clickListeners() {
        btnViewFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter == null ||
                        listaSelecao == null
                        || listaSelecao.size() <= 0) {
                    recuperarFiltrosCallback.onSemFiltros();
                    return;
                }
                recuperarFiltrosCallback.onRecuperado(listaSelecao);
            }
        });
    }

    @Override
    public void onMarcado() {
        if (adapter != null
                && adapter.getListaSelecao() != null) {
            listaSelecao = adapter.getListaSelecao();
            ToastCustomizado.toastCustomizadoCurto("Marcado: " + listaSelecao.size(), requireContext());
        }
    }

    @Override
    public void onDesmarcado() {
        if (adapter != null
                && adapter.getListaSelecao() != null) {
            listaSelecao = adapter.getListaSelecao();
            ToastCustomizado.toastCustomizadoCurto("Desmarcado: " + listaSelecao.size(), requireContext());
        }
    }

    private void inicializarComponentes(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewCommunityFilters);
        btnViewFiltrar = view.findViewById(R.id.btnViewFiltrarComunidade);
    }
}
