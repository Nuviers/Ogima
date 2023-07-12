package com.example.ogima.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFriends;
import com.example.ogima.adapter.AdapterRequest;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FriendsFragment extends Fragment {

    private String emailUsuario, idUsuarioLogado;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private AdapterFriends adapterFriends;
    private RecyclerView recyclerFriends;
    private SearchView searchViewFriends;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerOptions<Usuario> options;
    private Query queryRecuperaFriends;
    private DatabaseReference friendsRef;
    private DatabaseReference dadosUserFriendRef;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private String idDesejado;

    private String idDonoPerfil = null;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        recuperarFriends();

        adapterFriends.startListening();

        configuracaoSearchView();
    }

    @Override
    public void onStop() {
        super.onStop();

        adapterFriends.stopListening();

        if (listaUsuarios != null) {
            listaUsuarios.clear();
        }

        liberarRecursosSearchView();
    }

    private void configuracaoSearchView() {
        //SearchViewChat
        searchViewFriends.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewFriends.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Chamado somente quando o usuário confirma o envio do texto.
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    String dadoDigitado = Normalizer.normalize(newText, Normalizer.Form.NFD);
                    dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    String dadoDigitadoFormatado = dadoDigitado.toUpperCase(Locale.ROOT);
                    dadosComFiltro(dadoDigitadoFormatado);
                } else {
                    dadosSemFiltro();
                }
                //Chamado a cada mudança
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        inicializandoComponentes(view);

        Bundle dados = getArguments();

        if (dados != null) {
            idDonoPerfil = dados.getString("idDonoPerfil");
            ToastCustomizado.toastCustomizado("Conteudo: " + idDonoPerfil, requireContext());
        }

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

        configucaoQueryInicial();

        //Configurando recycler
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerFriends.setLayoutManager(linearLayoutManager);

        if (adapterFriends != null) {

        } else {
            if (idDonoPerfil != null && !idDonoPerfil.isEmpty()) {
                adapterFriends = new AdapterFriends(getContext(), options, true);
            }else{
                adapterFriends = new AdapterFriends(getContext(), options, false);
            }
        }
        recyclerFriends.setAdapter(adapterFriends);

        return view;
    }

    private void inicializandoComponentes(View view) {
        recyclerFriends = view.findViewById(R.id.recyclerFriends);
        searchViewFriends = view.findViewById(R.id.searchViewFriends);
    }

    private void preencherListaFriends(Usuario usuarioCompleto) {

        if (listaUsuarios != null) {
            if (listaUsuarios.size() >= 1) {
                if (!listaUsuarios.contains(usuarioCompleto)) {
                    listaUsuarios.add(usuarioCompleto);
                }
            } else {
                listaUsuarios.add(usuarioCompleto);
            }
        }
    }

    private void recuperarFriends() {
        friendsRef = firebaseRef.child("friends")
                .child(idDonoPerfil);

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Usuario usuarioFriend = snapshot1.getValue(Usuario.class);
                    dadosUsuarioFriend(usuarioFriend.getIdUsuario());
                }
                friendsRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dadosUsuarioFriend(String idFriend) {

        dadosUserFriendRef = firebaseRef.child("usuarios")
                .child(idFriend);

        dadosUserFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioCompleto = snapshot.getValue(Usuario.class);
                    String nomeCompleto = usuarioCompleto.getNomeUsuarioPesquisa();
                    String apelidoCompleto = usuarioCompleto.getApelidoUsuarioPesquisa();
                    usuarioCompleto.setNomeUsuarioPesquisa(nomeCompleto);
                    usuarioCompleto.setApelidoUsuarioPesquisa(apelidoCompleto);
                    preencherListaFriends(usuarioCompleto);
                }
                dadosUserFriendRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configucaoQueryInicial() {

        queryRecuperaFriends = firebaseRef.child("friends")
                .child(idDonoPerfil);

        options =
                new FirebaseRecyclerOptions.Builder<Usuario>()
                        .setQuery(queryRecuperaFriends, Usuario.class)
                        .build();
    }

    private void dadosComFiltro(String dadoDigitado) {
        if (listaUsuarios != null) {
            for (Usuario usuario : listaUsuarios) {
                String nomeUsuario = usuario.getNomeUsuarioPesquisa();
                String apelidoUsuario = usuario.getApelidoUsuarioPesquisa();

                if (nomeUsuario.startsWith(dadoDigitado) || apelidoUsuario.startsWith(dadoDigitado)) {

                    idDesejado = usuario.getIdUsuario();

                    Query queryComFiltro = firebaseRef.child("friends")
                            .child(idDonoPerfil).orderByChild("idUsuario")
                            .equalTo(idDesejado);

                    options =
                            new FirebaseRecyclerOptions.Builder<Usuario>()
                                    .setQuery(queryComFiltro, Usuario.class)
                                    .build();

                    adapterFriends.updateOptions(options);
                }
            }
        }
    }

    private void dadosSemFiltro() {
        Query querySemFiltro = firebaseRef.child("friends")
                .child(idDonoPerfil);
        options =
                new FirebaseRecyclerOptions.Builder<Usuario>()
                        .setQuery(querySemFiltro, Usuario.class)
                        .build();

        adapterFriends.updateOptions(options);
    }

    private void liberarRecursosSearchView() {
        searchViewFriends.setQuery("", false);
        searchViewFriends.setIconified(true);
        if (searchViewFriends.getOnFocusChangeListener() != null) {
            searchViewFriends.setOnQueryTextListener(null);
        }
    }

    //Libera recursos do searchView ao mudar de fragment
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (searchViewFriends != null) {
                searchViewFriends.setQuery("", false);
                searchViewFriends.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchViewFriends.getWindowToken(), 0);
            }
        }
    }
}