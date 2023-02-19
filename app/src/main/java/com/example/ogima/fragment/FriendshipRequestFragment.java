package com.example.ogima.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterRequest;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FriendshipRequestFragment extends Fragment {

    private String emailUsuario, idUsuarioLogado;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private SearchView searchViewFriendShipRequest;
    private RecyclerView recyclerFriendShipRequest;
    private AdapterRequest adapterRequest;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerOptions<Usuario> options;
    private Query queryRecuperaSolicitacoes;
    private DatabaseReference verificaSolicitacoesRef;
    private ChildEventListener childEventListener;

    public FriendshipRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        adapterRequest.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        adapterRequest.stopListening();

        removerChildListener(verificaSolicitacoesRef, childEventListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendship_request, container, false);
        inicializandoComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

        configucaoQueryInicial();

        //Configurando recycler
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerFriendShipRequest.setLayoutManager(linearLayoutManager);

        if (adapterRequest != null) {

        } else {
            adapterRequest = new AdapterRequest(getContext(), options);
        }
        recyclerFriendShipRequest.setAdapter(adapterRequest);

        return view;
    }


    private void inicializandoComponentes(View view) {
        recyclerFriendShipRequest = view.findViewById(R.id.recyclerFriendShipRequest);
        searchViewFriendShipRequest = view.findViewById(R.id.searchViewFriendShipRequest);
    }

    private void configucaoQueryInicial() {

        Query queryRecuperaSolicitacoes = firebaseRef.child("requestsFriendship")
                .child(idUsuarioLogado);

        options =
                new FirebaseRecyclerOptions.Builder<Usuario>()
                        .setQuery(queryRecuperaSolicitacoes, Usuario.class)
                        .build();
    }

    private void dadosSemFiltro() {

    }

    private void dadosComFiltro() {

    }

    private void configuracaoSearchView() {

    }

    public void removerListener(DatabaseReference reference, ValueEventListener valueEventListener) {
        if (valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    private void removerChildListener(DatabaseReference reference, ChildEventListener childEventListener) {
        if (childEventListener != null) {
            reference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }
}