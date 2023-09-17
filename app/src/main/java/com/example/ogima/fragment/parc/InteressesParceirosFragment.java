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
import com.example.ogima.adapter.AdapterHobbiesParc;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.IntentEdicaoPerfilParc;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InteressesParceirosFragment extends Fragment implements AdapterHobbiesParc.QuantidadeSelecaoCallback {

    private DataTransferListener dataTransferListener;
    private FloatingActionButton fabParc;
    private Usuario usuario;
    private RecyclerView recyclerViewHobbies;
    private TextView txtViewNrHobbiesParc;
    private LinearLayoutManager linearLayoutManagerTopicos;
    private AdapterHobbiesParc adapterHobbiesParc;
    private final String[] hobbies = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    private final ArrayList<String> listaHobbies = new ArrayList<>(Arrays.asList(hobbies));
    private List<String> listaHobbiesSelecionados = new ArrayList<>();
    private final static int MIN_HOBBIES = 10;
    private String idUsuario = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private ArrayList<String> listaHobbiesEdit = new ArrayList<>();

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
        if (listaHobbiesEdit != null
                && listaHobbiesEdit.size() > 0) {
            DatabaseReference atualizarHobbiesRef = firebaseRef.child("usuarioParc")
                    .child(idUsuario);
            Map<String, Object> update = new HashMap<>();
            update.put("listaInteressesParc", listaInteresses);
            atualizarHobbiesRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    ToastCustomizado.toastCustomizadoCurto("Hobbies atualizados com sucesso!", requireContext());
                    IntentEdicaoPerfilParc.irParaEdicao(requireContext(), idUsuario);
                }
            });
            return;
        }
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
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
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
            if (adapterHobbiesParc == null) {
                adapterHobbiesParc = new AdapterHobbiesParc(requireContext(), listaHobbies, this);
                recyclerViewHobbies.setAdapter(adapterHobbiesParc);
                adapterHobbiesParc.notifyDataSetChanged();

                Bundle args = getArguments();
                if (args != null && args.containsKey("edit")) {
                    listaHobbiesEdit = args.getStringArrayList("edit");
                    adapterHobbiesParc.setListaHobbiesEdit(listaHobbiesEdit);
                }
            }
        }
    }

    private void receberHobbies() {
        if (adapterHobbiesParc.getListaTopicosSelecionados() != null
                && adapterHobbiesParc.getListaTopicosSelecionados().size() > 0) {
            listaHobbiesSelecionados = adapterHobbiesParc.getListaTopicosSelecionados();
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
            adapterHobbiesParc.limparTopicosFiltrados();
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