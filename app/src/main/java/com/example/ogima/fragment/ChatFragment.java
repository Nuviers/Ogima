package com.example.ogima.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterChat;
import com.example.ogima.adapter.AdapterPostagens;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private ChipGroup chipGroupChat;
    private Chip chipChatFavoritos, chipChatAmigos, chipChatSeguidores, chipChatSeguindo;
    private List<Postagem> listaChat = new ArrayList<>();
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private RecyclerView recyclerChat;
    private AdapterChat adapterChat;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        inicializarComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerChat.setLayoutManager(linearLayoutManager);
        recyclerChat.setHasFixedSize(true);

        if (adapterChat != null) {

        } else {
            adapterChat = new AdapterChat(listaChat, getContext());
        }
        recyclerChat.setAdapter(adapterChat);

        chipGroupChat.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (chipChatFavoritos.isChecked()) {
                    ToastCustomizado.toastCustomizado("Check Favoritos ", getContext());
                } else if (chipChatAmigos.isChecked()) {
                    ToastCustomizado.toastCustomizado("Check Amigos ", getContext());
                } else if (chipChatSeguidores.isChecked()) {
                    ToastCustomizado.toastCustomizado("Check Seguidores ", getContext());
                } else if (chipChatSeguindo.isChecked()) {
                    ToastCustomizado.toastCustomizado("Check Seguindo ", getContext());
                }
            }
        });

        return view;
    }

    private void inicializarComponentes(View view) {
        chipGroupChat = view.findViewById(R.id.chipGroupChat);
        chipChatFavoritos = view.findViewById(R.id.chipChatFavoritos);
        chipChatAmigos = view.findViewById(R.id.chipChatAmigos);
        chipChatSeguindo = view.findViewById(R.id.chipChatSeguindo);
        chipChatSeguidores = view.findViewById(R.id.chipChatSeguidores);
        recyclerChat = view.findViewById(R.id.recyclerChat);
    }

}
