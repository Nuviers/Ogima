package com.example.ogima.fragment.parc;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterTopicosGrupoPublico;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InteressesParceirosFragment extends Fragment implements AdapterTopicosGrupoPublico.QuantidadeSelecaoCallback {

    private DataTransferListener dataTransferListener;
    private FloatingActionButton fabParc;
    private Usuario usuario;
    private RecyclerView recyclerViewHobbies;
    private TextView txtViewNrHobbiesParc;
    private LinearLayoutManager linearLayoutManagerTopicos;
    private AdapterTopicosGrupoPublico adapterTopicosComunidadePublico;
    private final String[] hobbies = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    private final ArrayList<String> listaHobbies = new ArrayList<>(Arrays.asList(hobbies));
    private List<String> listaHobbiesSelecionados = new ArrayList<>();
    private final static int MIN_HOBBIES = 10;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DataTransferListener) {
            dataTransferListener = (DataTransferListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DataTransferListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataTransferListener = null;
    }

    private void onButtonClicked(ArrayList<String> listaInteresses) {
        if (dataTransferListener != null) {
            usuario.setListaInteressesParc(listaInteresses);
            dataTransferListener.onUsuarioParc(usuario, "interesses");
        }
    }

    public void setName(Usuario usuarioParc) {
        usuario = usuarioParc;
    }

    public InteressesParceirosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interesses_parceiros, container, false);
        inicializandoComponentes(view);
        usuario = new Usuario();
        configRecyclerHobbies();
        fabParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receberHobbies();
            }
        });
        return view;
    }


    private void configRecyclerHobbies() {
        if (linearLayoutManagerTopicos == null) {
            linearLayoutManagerTopicos = new LinearLayoutManager(requireContext());
            linearLayoutManagerTopicos.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewHobbies.setHasFixedSize(true);
            // Defina o gerenciador de layout do RecyclerView como ChipsLayoutManager
            ChipsLayoutManager layoutManager = ChipsLayoutManager.newBuilder(requireContext())
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .setMaxViewsInRow(7)
                    .build();
            recyclerViewHobbies.setLayoutManager(layoutManager);
            if (adapterTopicosComunidadePublico == null) {
                adapterTopicosComunidadePublico = new AdapterTopicosGrupoPublico(requireContext(), listaHobbies, this);
                recyclerViewHobbies.setAdapter(adapterTopicosComunidadePublico);
                adapterTopicosComunidadePublico.notifyDataSetChanged();
            }
        }
    }

    private void receberHobbies() {
        if (adapterTopicosComunidadePublico.getListaTopicosSelecionados() != null
                && adapterTopicosComunidadePublico.getListaTopicosSelecionados().size() > 0) {
            listaHobbiesSelecionados = adapterTopicosComunidadePublico.getListaTopicosSelecionados();
            if (listaHobbiesSelecionados.size() < MIN_HOBBIES) {
                ToastCustomizado.toastCustomizadoCurto("É necessário selecionar 10 hobbies para prosseguir", requireContext());
            }else{
                ToastCustomizado.toastCustomizadoCurto("TUDO OKAY 7", requireContext());
                ArrayList<String> listaConfigurada = new ArrayList<>(listaHobbiesSelecionados);
                onButtonClicked(listaConfigurada);
                for (String topicos : listaHobbiesSelecionados) {
                    //ToastCustomizado.toastCustomizadoCurto("Recebido " + topicos, requireContext());
                }
            }
        }else{
            ToastCustomizado.toastCustomizadoCurto("É necessário selecionar 10 hobbies para prosseguir", requireContext());
        }
    }

    private void limparTopicos() {
        if (listaHobbies != null && listaHobbies.size() > 0) {
            adapterTopicosComunidadePublico.limparTopicosFiltrados();
        }
    }

    private void inicializandoComponentes(View view) {
        fabParc = view.findViewById(R.id.fabParc);
        recyclerViewHobbies = view.findViewById(R.id.recyclerViewHobbiesParc);
        txtViewNrHobbiesParc = view.findViewById(R.id.txtViewNrHobbiesParc);
    }

    @Override
    public void onQntSelecionada(int qnt) {
        txtViewNrHobbiesParc.setText(qnt+"/"+"10");
    }

    @Override
    public void onSemSelecao() {
        txtViewNrHobbiesParc.setText("0/10");
    }
}