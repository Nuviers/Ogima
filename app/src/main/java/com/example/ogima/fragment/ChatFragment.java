package com.example.ogima.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterChat;
import com.example.ogima.adapter.AdapterPostagens;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private ChipGroup chipGroupChat;
    private Chip chipChatFavoritos, chipChatAmigos, chipChatSeguidores, chipChatSeguindo;
    private List<Usuario> listaChat = new ArrayList<>();
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private RecyclerView recyclerChat;
    private AdapterChat adapterChat;

    private SmartTabLayout smartChatContato;
    private ViewPager viewpagerChatContato;

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

        //Configurando abas
        FragmentPagerItemAdapter fragmentPagerItemAdapter  = new FragmentPagerItemAdapter(
                getActivity().getSupportFragmentManager(), FragmentPagerItems.with(getActivity())
                .add("Chat", ChatFragment.class)
                .add("Contatos", ContatoFragment.class)
                .create());

        viewpagerChatContato.setAdapter(fragmentPagerItemAdapter);
        smartChatContato.setViewPager(viewpagerChatContato);


        DatabaseReference verificarContatoRef = firebaseRef.child("contatos")
                .child(idUsuario);

        verificarContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        Contatos contatosMeus = snapshot1.getValue(Contatos.class);
                        //Capturando dados do usúario
                        DatabaseReference verificaUsuarioRef = firebaseRef.child("usuarios")
                                .child(contatosMeus.getIdContato());

                        verificaUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Usuario usuario = snapshot.getValue(Usuario.class);
                                    listaChat.add(usuario);
                                    adapterChat.notifyDataSetChanged();
                                    verificaUsuarioRef.removeEventListener(this);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ToastCustomizado.toastCustomizadoCurto("IdContato " + contatosMeus.getIdContato(), getContext());
                    }
                }
                verificarContatoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
        smartChatContato= view.findViewById(R.id.smartChatContato);
        viewpagerChatContato = view.findViewById(R.id.viewpagerChatContato);
    }

}
