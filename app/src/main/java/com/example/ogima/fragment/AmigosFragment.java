package com.example.ogima.fragment;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.ogima.R;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.adapter.AdapterFindPeoples;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.RecyclerItemClickListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AmigosFragment extends Fragment {

    private SearchView searchViewFindPeoples;
    private RecyclerView recyclerViewFindPeoples;
    private List<Usuario> listaUsuarios;
    private DatabaseReference usuarioRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private AdapterFindPeoples adapterFindPeoples;
    private String idUsuarioAtual;
    private ShimmerFrameLayout shimmerFindPeople;


    public AmigosFragment() {
        // Required empty public constructor
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_amigos, container, false);
        inicializandoComponentes(view);

        //Configurações iniciais
        listaUsuarios = new ArrayList<>();
        usuarioRef = firebaseRef.child("usuarios");
        idUsuarioAtual = UsuarioFirebase.getIdUsuarioCriptografado();

        //Configuração do recyclerview
        recyclerViewFindPeoples.setHasFixedSize(true);
        recyclerViewFindPeoples.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapterFindPeoples = new AdapterFindPeoples(listaUsuarios, getActivity());
        recyclerViewFindPeoples.setAdapter(adapterFindPeoples);

        recyclerViewFindPeoples.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        //Configura evento de clique no recyclerView
        recyclerViewFindPeoples.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerViewFindPeoples,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaUsuarios.get(position);

                        DatabaseReference verificaBlock = firebaseRef
                                .child("blockUser").child(idUsuarioAtual).child(usuarioSelecionado.getIdUsuario());

                        verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.getValue() != null){
                                    Intent intentBlock = new Intent(getActivity(), PersonProfileActivity.class);
                                    intentBlock.putExtra("blockedUser", "blockedUser");
                                    intentBlock.putExtra("usuarioSelecionado", usuarioSelecionado);
                                    intentBlock.putExtra("backIntent", "amigosFragment");
                                    startActivity(intentBlock);
                                }else{
                                    Intent intent = new Intent(getActivity(), PersonProfileActivity.class);
                                    intent.putExtra("usuarioSelecionado", usuarioSelecionado);
                                    intent.putExtra("backIntent", "amigosFragment");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                verificaBlock.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));


        //Configuração do searchview
        searchViewFindPeoples.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewFindPeoples.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //Analisa o que foi enviado pelo usuário ao confirmar envio.
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //Analisa o que foi digitado em tempo real.
            @Override
            public boolean onQueryTextChange(String newText) {
                String dadoDigitado = newText.toUpperCase(Locale.ROOT);
                pesquisarPessoas(dadoDigitado);
                return true;
            }
        });

        return view;
    }

    private void pesquisarPessoas(String dadoUsuario) {
        //Limpar lista
        listaUsuarios.clear();

        //Pesquisar usuário caso o campo digitado não esteja vazio.
        if (dadoUsuario.length() > 0) {
            Query query = usuarioRef.orderByChild("nomeUsuarioPesquisa")
                    .startAt(dadoUsuario)
                    .endAt(dadoUsuario + "\uf8ff");
        try{
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Limpa a lista.
                    listaUsuarios.clear();
                    if(snapshot.getValue() == null){
                        ToastCustomizado.toastCustomizadoCurto("Não foi localizado ninguém com esse nome", getContext());
                    }else{
                    }
                    for (DataSnapshot snap : snapshot.getChildren()) {

                        //Verifica se é o usuário logado, caso seja oculte ele da lista
                        Usuario usuario = snap.getValue(Usuario.class);

                        if(idUsuarioAtual.equals(usuario.getIdUsuario()))
                            continue;
                        //Continue serve para voltar ao começo do for e ignora o que está depois dele.
                        listaUsuarios.add(usuario);
                    }

                    adapterFindPeoples.notifyDataSetChanged();

                    query.removeEventListener(this);
                    /*
                    int tamanhoLista = listaUsuarios.size();
                    Log.i("tamanhoLista", "Localizado " + tamanhoLista);
                     */
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
        }else{
            try{
                listaUsuarios.clear();
                adapterFindPeoples.notifyDataSetChanged();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

    private void inicializandoComponentes(View view) {
        searchViewFindPeoples = view.findViewById(R.id.searchViewFindPeoples);
        recyclerViewFindPeoples = view.findViewById(R.id.recyclerFindPeoples);
        shimmerFindPeople = view.findViewById(R.id.shimmerAmigos);
    }

    public void animacaoShimmer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    shimmerFindPeople.stopShimmer();
                    shimmerFindPeople.hideShimmer();
                    shimmerFindPeople.setVisibility(View.GONE);
                    recyclerViewFindPeoples.setVisibility(View.VISIBLE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 1200);
    }

}
