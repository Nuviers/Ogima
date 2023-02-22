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

    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    public FriendshipRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        configucaoQueryInicial();

        adapterRequest.startListening();

        configuracaoSearchView();
    }

    @Override
    public void onStop() {
        super.onStop();

        adapterRequest.stopListening();

        liberarRecursosSearchView();

        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendship_request, container, false);
        inicializandoComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);

        return view;
    }


    private void inicializandoComponentes(View view) {
        recyclerFriendShipRequest = view.findViewById(R.id.recyclerFriendShipRequest);
        searchViewFriendShipRequest = view.findViewById(R.id.searchViewFriendShipRequest);
    }

    private void configucaoQueryInicial() {

        queryRecuperaSolicitacoes = firebaseRef.child("requestsFriendship")
                .child(idUsuarioLogado);

        options =
                new FirebaseRecyclerOptions.Builder<Usuario>()
                        .setQuery(queryRecuperaSolicitacoes, Usuario.class)
                        .build();

        //Configurando recycler
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerFriendShipRequest.setLayoutManager(linearLayoutManager);

        if (adapterRequest != null) {

        } else {
            adapterRequest = new AdapterRequest(getContext(), options);
        }
        recyclerFriendShipRequest.setAdapter(adapterRequest);
    }

    private void dadosSemFiltro() {

        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        mRunnable = new Runnable() {
            @Override
            public void run() {
                Query querySemFiltro = firebaseRef.child("requestsFriendship")
                        .child(idUsuarioLogado);

                options =
                        new FirebaseRecyclerOptions.Builder<Usuario>()
                                .setQuery(querySemFiltro, Usuario.class)
                                .build();

                adapterRequest.updateOptions(options);
            }
        };
        mHandler.postDelayed(mRunnable, 500);
    }

    private void dadosComFiltro(String dadoDigitado) {

        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        mRunnable = new Runnable() {
            @Override
            public void run() {
                Query querySolicitacoesFiltradas = firebaseRef.child("requestsFriendship")
                        .child(idUsuarioLogado);

                querySolicitacoesFiltradas.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Usuario usuarioSolicitante = snapshot1.getValue(Usuario.class);
                            usuarioFiltrado(dadoDigitado, usuarioSolicitante.getIdRemetente());
                        }
                        querySolicitacoesFiltradas.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        mHandler.postDelayed(mRunnable, 500);
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
                if (newText != null) {
                    String dadoDigitado = Normalizer.normalize(newText, Normalizer.Form.NFD);
                    dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    String dadoDigitadoFormatado = dadoDigitado.toUpperCase(Locale.ROOT);

                    if (dadoDigitadoFormatado.isEmpty()) {
                        dadosSemFiltro();
                    } else {
                        dadosComFiltro(dadoDigitadoFormatado);
                    }
                }
                //Chamado a cada mudança
                return true;
            }
        });
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

    private void usuarioFiltrado(String dadoDigitado, String idRemetente) {

        usuarioRemetenteFiltradoRef = firebaseRef.child("usuarios")
                .child(idRemetente);

        usuarioRemetenteFiltradoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Recupera dados pelo idRemetente onde foi pego através do nó
                    //requestsFriendship usando o idUsuarioLogado.
                    Usuario usuarioRemetente = snapshot.getValue(Usuario.class);
                    String nomeRemetente = usuarioRemetente.getNomeUsuarioPesquisa();
                    String apelidoRemetente = usuarioRemetente.getApelidoUsuarioPesquisa();

                    //Verifica se o que foi digitado confere com algum usuário que esteja
                    //nas solicitações.
                    if (nomeRemetente.startsWith(dadoDigitado) || apelidoRemetente.startsWith(dadoDigitado)) {

                        //ToastCustomizado.toastCustomizadoCurto("Nome " + nomeRemetente, getContext());
                        //ToastCustomizado.toastCustomizadoCurto("Apelido " + apelidoRemetente, getContext());

                        //Filtra os dados coletados somente para quem realmente enviou solicitação
                        //para o usuário atual, é feito usando o equalTo, assim usuários que não enviaram
                        //especificamente para o usuário atual são descartados.
                        Query queryComFiltragem = firebaseRef.child("requestsFriendship")
                                .child(idRemetente).orderByChild("idDestinatario")
                                .equalTo(idUsuarioLogado);

                        options =
                                new FirebaseRecyclerOptions.Builder<Usuario>()
                                        .setQuery(queryComFiltragem, Usuario.class)
                                        .build();

                        adapterRequest.updateOptions(options);
                    }
                }
                usuarioRemetenteFiltradoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void liberarRecursosSearchView() {
        searchViewFriendShipRequest.setQuery("", false);
        searchViewFriendShipRequest.setIconified(true);
        if (searchViewFriendShipRequest.getOnFocusChangeListener() != null) {
            searchViewFriendShipRequest.setOnQueryTextListener(null);
        }
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