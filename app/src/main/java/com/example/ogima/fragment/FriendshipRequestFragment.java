package com.example.ogima.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FriendshipRequestFragment extends Fragment {

    private String emailUsuario, idUsuarioLogado;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private SearchView searchViewFriendShipRequest;
    private RecyclerView recyclerFriendShipRequest;
    private AdapterRequest adapterRequest;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerOptions<Usuario> options;
    private Query queryRecuperaSolicitacoes, queryRequestsFiltradas;
    private DatabaseReference usuarioRemetenteFiltradoRef;

    private DatabaseReference buscarSolicitacoesRef;

    private List<Usuario> listaUsuarios = new ArrayList<>();
    private String idDesejado;
    private String idDonoPerfil = null;

    public FriendshipRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        preencherLista();

        adapterRequest.startListening();

        configuracaoSearchView();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (listaUsuarios != null) {
            listaUsuarios.clear();
        }

        adapterRequest.stopListening();

        liberarRecursosSearchView();
    }

    private void configuracaoSearchView() {
        //SearchViewChat
        searchViewFriendShipRequest.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewFriendShipRequest.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        View view = inflater.inflate(R.layout.fragment_friendship_request, container, false);
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
        recyclerFriendShipRequest.setLayoutManager(linearLayoutManager);

        if (adapterRequest != null) {

        } else {
            if (idDonoPerfil != null && !idDonoPerfil.isEmpty()
                    && !idDonoPerfil.equals(idUsuarioLogado)) {
                adapterRequest = new AdapterRequest(getContext(), options, true);
            }else{
                adapterRequest = new AdapterRequest(getContext(), options, false);
            }
        }
        recyclerFriendShipRequest.setAdapter(adapterRequest);

        return view;
    }


    private void inicializandoComponentes(View view) {
        //recyclerFriendShipRequest = view.findViewById(R.id.recyclerFriendShipRequest);
        //searchViewFriendShipRequest = view.findViewById(R.id.searchViewFriendShipRequest);
    }

    private void configucaoQueryInicial() {

        queryRecuperaSolicitacoes = firebaseRef.child("requestsFriendship")
                .child(idDonoPerfil).orderByChild("idDestinatario")
                .equalTo(idDonoPerfil);

        options =
                new FirebaseRecyclerOptions.Builder<Usuario>()
                        .setQuery(queryRecuperaSolicitacoes, Usuario.class)
                        .build();
    }

    private void dadosSemFiltro() {

        //ToastCustomizado.toastCustomizadoCurto("Sem filtro", getContext());

        Query querySemFiltro = firebaseRef.child("requestsFriendship")
                .child(idDonoPerfil).orderByChild("idDestinatario")
                .equalTo(idDonoPerfil);

        options =
                new FirebaseRecyclerOptions.Builder<Usuario>()
                        .setQuery(querySemFiltro, Usuario.class)
                        .build();

        adapterRequest.updateOptions(options);
    }

    private void dadosComFiltro(String dadoDigitado) {

        //ToastCustomizado.toastCustomizadoCurto("Com filtro", getContext());

        if (listaUsuarios != null) {
            for (Usuario usuario : listaUsuarios) {

                String nomeUsuario = usuario.getNomeUsuarioPesquisa();

                if (nomeUsuario.startsWith(dadoDigitado)) {

                    idDesejado = usuario.getIdRemetente();

                    Query queryComFiltro = firebaseRef.child("requestsFriendship")
                            .child(idDonoPerfil).orderByChild("idRemetente")
                            .equalTo(idDesejado);

                    options =
                            new FirebaseRecyclerOptions.Builder<Usuario>()
                                    .setQuery(queryComFiltro, Usuario.class)
                                    .build();

                    adapterRequest.updateOptions(options);
                }
            }
        }
    }

    private void liberarRecursosSearchView() {
        searchViewFriendShipRequest.setQuery("", false);
        searchViewFriendShipRequest.setIconified(true);
        if (searchViewFriendShipRequest.getOnFocusChangeListener() != null) {
            searchViewFriendShipRequest.setOnQueryTextListener(null);
        }
    }

    private void preencherLista() {

        buscarSolicitacoesRef = firebaseRef.child("requestsFriendship")
                .child(idDonoPerfil);

        buscarSolicitacoesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                    Usuario usuarioSolicitante = snapshot1.getValue(Usuario.class);

                    if (usuarioSolicitante.getIdDestinatario().equals(idDonoPerfil)) {
                        //Recuperar dados dos usuários solicitantes
                        DatabaseReference usuarioPeloNome = firebaseRef.child("usuarios")
                                .child(usuarioSolicitante.getIdRemetente());

                        usuarioPeloNome.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    //Dados dos solicitantes
                                    Usuario usuarioEncontrado = snapshot.getValue(Usuario.class);
                                    String nomePesquisa = usuarioEncontrado.getNomeUsuarioPesquisa();
                                    String apelidoPesquisa = usuarioEncontrado.getApelidoUsuarioPesquisa();
                                    usuarioSolicitante.setNomeUsuarioPesquisa(nomePesquisa);
                                    usuarioSolicitante.setApelidoUsuarioPesquisa(apelidoPesquisa);
                                    listaUsuarios.add(usuarioSolicitante);
                                }
                                usuarioPeloNome.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                buscarSolicitacoesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Libera recursos do searchView ao mudar de fragment
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (searchViewFriendShipRequest != null) {
                searchViewFriendShipRequest.setQuery("", false);
                searchViewFriendShipRequest.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchViewFriendShipRequest.getWindowToken(), 0);
            }
        }
    }
}