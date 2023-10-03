package com.example.ogima.fragment.cad;

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
import com.example.ogima.helper.DataCadListener;
import com.example.ogima.helper.IntentEdicaoPerfilParc;
import com.example.ogima.helper.IrParaEdicaoDePerfil;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteressesFragment extends Fragment implements AdapterHobbiesParc.QuantidadeSelecaoCallback {

    private DataCadListener dataTransferListener;
    private FloatingActionButton fabProximo;
    private Usuario usuario;
    private RecyclerView recyclerViewInteresses;
    private TextView txtViewNrInteresses;
    private LinearLayoutManager linearLayoutManager;
    private AdapterHobbiesParc adapterHobbiesParc;
    private String[] interesses;
    private ArrayList<String> listaInteresses;
    private List<String> interessesSelecionados = new ArrayList<>();
    private final static int MIN_INTERESTS = 10;
    private String idUsuario = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private ArrayList<String> listaInteressesEdit = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DataCadListener) {
            dataTransferListener = (DataCadListener) context;
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

    public InteressesFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interesses, container, false);
        inicializandoComponentes(view);
        interesses = getResources().getStringArray(R.array.interests_array);
        listaInteresses = new ArrayList<>(Arrays.asList(interesses));
        usuario = new Usuario();
        configRecycler();
        clickListeners();
        return view;
    }

    private void onButtonClicked(ArrayList<String> listaInteresses) {
        if (listaInteressesEdit != null
                && listaInteressesEdit.size() > 0) {
            DatabaseReference atualizarInteressesRef = firebaseRef.child("usuarios")
                    .child(idUsuario);
            Map<String, Object> update = new HashMap<>();
            update.put("interesses", listaInteresses);
            atualizarInteressesRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.changed_interests), requireContext());
                    IrParaEdicaoDePerfil.intentEdicao(requireActivity());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizado(String.format("%s %s", R.string.an_error_has_occurred,e.getMessage()), requireContext());
                    IrParaEdicaoDePerfil.intentEdicao(requireActivity());
                }
            });
            return;
        }
        if (dataTransferListener != null) {
            usuario.setInteresses(listaInteresses);
            dataTransferListener.onUsuario(usuario, "interesses");
        }
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewInteresses.setHasFixedSize(true);
            // Defina o gerenciador de layout do RecyclerView como ChipsLayoutManager
            ChipsLayoutManager layoutManager = ChipsLayoutManager.newBuilder(requireContext())
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .setMaxViewsInRow(7)
                    .build();
            recyclerViewInteresses.setLayoutManager(layoutManager);
            if (adapterHobbiesParc == null) {
                adapterHobbiesParc = new AdapterHobbiesParc(requireContext(), listaInteresses, this);
                recyclerViewInteresses.setAdapter(adapterHobbiesParc);
                adapterHobbiesParc.notifyDataSetChanged();

                Bundle args = getArguments();
                if (args != null && args.containsKey("edit")) {
                    listaInteressesEdit = args.getStringArrayList("edit");
                    if (listaInteressesEdit != null && listaInteressesEdit.size() > 0) {
                        txtViewNrInteresses.setText(String.format("%d%s%d", listaInteressesEdit.size(),"/",MIN_INTERESTS));
                        adapterHobbiesParc.setListaHobbiesEdit(listaInteressesEdit);
                    }
                }
            }
        }
    }

    private void receberInteresses() {
        if (adapterHobbiesParc.getListaTopicosSelecionados() != null
                && adapterHobbiesParc.getListaTopicosSelecionados().size() > 0) {
            interessesSelecionados = adapterHobbiesParc.getListaTopicosSelecionados();
            if (interessesSelecionados.size() < MIN_INTERESTS) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.minimum_threshold_of_interests, MIN_INTERESTS), requireContext());
            }else{
                ArrayList<String> listaConfigurada = new ArrayList<>(interessesSelecionados);
                onButtonClicked(listaConfigurada);
            }
        }else{
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.minimum_threshold_of_interests, MIN_INTERESTS), requireContext());
        }
    }

    private void limparTopicos() {
        if (listaInteresses != null && listaInteresses.size() > 0) {
            adapterHobbiesParc.limparTopicosFiltrados();
        }
    }

    @Override
    public void onQntSelecionada(int qnt) {
        txtViewNrInteresses.setText(String.format("%d%s%d", qnt,"/",MIN_INTERESTS));
    }

    @Override
    public void onSemSelecao() {
        txtViewNrInteresses.setText(String.format("%s%d", "0/",MIN_INTERESTS));
    }

    public void setUserCad(Usuario usuarioCad) {
        usuario = usuarioCad;
    }

    private void clickListeners(){
        fabProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receberInteresses();
            }
        });
    }

    private void inicializandoComponentes(View view) {
        fabProximo = view.findViewById(R.id.fabParc);
        recyclerViewInteresses = view.findViewById(R.id.recyclerViewInteressesCad);
        txtViewNrInteresses = view.findViewById(R.id.txtViewNrInteressesCad);
    }
}