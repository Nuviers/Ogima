package com.example.ogima.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.ogima.R;
import com.example.ogima.activity.ConversaActivity;
import com.example.ogima.adapter.AdapterChat;
import com.example.ogima.adapter.AdapterContato;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.RecyclerItemClickListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ContatoFragment extends Fragment {

    private ChipGroup chipGroupContato;
    private Chip chipContatoFavoritos, chipContatoAmigos, chipContatoSeguidores,
            chipContatoSeguindo;
    private List<Usuario> listaContato = new ArrayList<>();
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private RecyclerView recyclerContato;
    private AdapterContato adapterContato;

    //Reajuste
    private ValueEventListener valueEventListenerContato, valueEventListenerUsuario;
    private ChildEventListener childEventListenerContato;
    private DatabaseReference recuperarContatosRef, verificaUsuarioRef;

    public ContatoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        buscarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        recuperarContatosRef.removeEventListener(childEventListenerContato);
        verificaUsuarioRef.removeEventListener(valueEventListenerUsuario);
        listaContato.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contato, container, false);

        inicializarComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        chipGroupContato.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (chipContatoFavoritos.isChecked()) {
                    ToastCustomizado.toastCustomizado("Check Favoritos ", getContext());
                }
            }
        });

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerContato.setLayoutManager(linearLayoutManager);
        recyclerContato.setHasFixedSize(true);

        if (adapterContato != null) {

        } else {
            adapterContato = new AdapterContato(listaContato, getContext());
        }
        recyclerContato.setAdapter(adapterContato);

        recuperarContatosRef = firebaseRef.child("contatos")
                .child(idUsuario);

        return view;
    }


    private void buscarContatos() {

        childEventListenerContato = recuperarContatosRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Contatos contatos = snapshot.getValue(Contatos.class);
                    //Caso exista algum contato
                    verificaUsuarioRef = firebaseRef.child("usuarios")
                            .child(contatos.getIdContato());
                    valueEventListenerUsuario = verificaUsuarioRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Usuario usuario = snapshot.getValue(Usuario.class);
                                listaContato.add(usuario);
                                adapterContato.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*
        valueEventListenerContato = recuperarContatosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        Contatos contatos = snapshot1.getValue(Contatos.class);
                        verificaUsuarioRef = firebaseRef.child("usuarios")
                                .child(contatos.getIdContato());
                        valueEventListenerUsuario = verificaUsuarioRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Usuario usuario = snapshot.getValue(Usuario.class);
                                    adapterContato.adicionarItemContato(usuario);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
         */
    }


    private void buscarContatosAmigos() {

        DatabaseReference verificarContatoRef = firebaseRef.child("contatos")
                .child(idUsuario);

        verificarContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Contatos contatosMeus = snapshot1.getValue(Contatos.class);
                        //Capturando dados do usúario
                        DatabaseReference verificaUsuarioRef = firebaseRef.child("usuarios")
                                .child(contatosMeus.getIdContato());

                        verificaUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Usuario usuario = snapshot.getValue(Usuario.class);
                                    listaContato.add(usuario);
                                    adapterContato.notifyDataSetChanged();
                                    verificaUsuarioRef.removeEventListener(this);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                verificarContatoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void inicializarComponentes(View view) {
        chipGroupContato = view.findViewById(R.id.chipGroupContato);
        chipContatoFavoritos = view.findViewById(R.id.chipContatoFavoritos);
        chipContatoAmigos = view.findViewById(R.id.chipContatoAmigos);
        chipContatoSeguindo = view.findViewById(R.id.chipContatoSeguindo);
        chipContatoSeguidores = view.findViewById(R.id.chipContatoSeguidores);
        recyclerContato = view.findViewById(R.id.recyclerContato);
    }
}