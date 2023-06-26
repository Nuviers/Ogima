package com.example.ogima.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.ogima.R;
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.activity.ConvitesComunidadeActivity;
import com.example.ogima.activity.GruposPublicosActivity;
import com.example.ogima.activity.ListaComunidadesActivity;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.activity.TesteComDiffGrupoActivity;
import com.example.ogima.adapter.AdapterFindPeoples;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.RecyclerItemClickListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Usuario;
import com.facebook.shimmer.ShimmerFrameLayout;
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


public class AmigosFragment extends Fragment {

    private SearchView searchViewFindPeoples;
    private RecyclerView recyclerViewFindPeoples;
    private List<Usuario> listaUsuarios;
    private DatabaseReference usuarioRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private AdapterFindPeoples adapterFindPeoples;
    private String idUsuarioAtual, idUsuarioAlvo;
    private ShimmerFrameLayout shimmerFindPeople;
    private String emailUsuarioAtual, idUsuarioLogado;
    private ValueEventListener valueEventListener;
    private Handler handler = new Handler();
    //Recursos para exibição dos banner dos chats aleatórios
    private ImageSlider imageSliderAmigos;
    private ArrayList<SlideModel> imagensSlider = new ArrayList<>();

    private LinearLayout linearLayoutRandomFriends;
    private Button btnVerComunidades;

    private ImageView imgViewProcurarGrupos;
    private Button btnProcurarGrupos;

    public AmigosFragment() {
        // Required empty public constructor
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

    }

    @Override
    public void onStart() {
        super.onStart();

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
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                }
                if (newText != null && !newText.isEmpty()) {
                    String dadoDigitado = Normalizer.normalize(newText, Normalizer.Form.NFD);
                    dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    String dadoDigitadoOk = dadoDigitado.toUpperCase(Locale.ROOT);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pesquisarPessoas(dadoDigitadoOk);
                        }
                    }, 400);
                }
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        listaUsuarios.clear();
        searchViewFindPeoples.setQuery("", false);
        searchViewFindPeoples.setIconified(true);
        searchViewFindPeoples.setOnQueryTextListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_amigos, container, false);
        inicializandoComponentes(view);

        //Configurações iniciais

        usuarioRef = firebaseRef.child("usuarios");
        idUsuarioAtual = UsuarioFirebase.getIdUsuarioCriptografado();

        //Configuração do slider
        imagensSlider.add(new SlideModel
                (R.drawable.banner_chat_random_final_v1, "Chats com pessoas aleatórias contendo duas categorias (Comum e às cegas)",
                        null));

        imagensSlider.add(new SlideModel
                (R.drawable.banner_final_chat_comum, "Chat comum - Sua aparência será exibida",
                        null));

        imagensSlider.add(new SlideModel
                (R.drawable.banner_final_chat_as_cegas, "Chat às cegas - Sua aparência será revelada somente quando os dois usuários quiserem se revelar",
                        null));

        //Setando o arrayList SlideModel no Slider
        imageSliderAmigos.setImageList(imagensSlider, ScaleTypes.CENTER_CROP);

        //Ouvinte do slider
        imageSliderAmigos.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == 0) {
                    ToastCustomizado.toastCustomizadoCurto("Zero", getContext());
                }
                if (i == 1) {
                    ToastCustomizado.toastCustomizadoCurto("Um", getContext());
                }
                if (i == 2) {
                    ToastCustomizado.toastCustomizadoCurto("Dois", getContext());
                }
            }
        });

        //Configuração do recyclerview
        recyclerViewFindPeoples.setHasFixedSize(true);
        recyclerViewFindPeoples.setLayoutManager(new LinearLayoutManager(getActivity()));
        listaUsuarios = new ArrayList<>();
        // Tava aqui o config do adapter
        adapterFindPeoples = new AdapterFindPeoples(listaUsuarios, getActivity());
        recyclerViewFindPeoples.setAdapter(adapterFindPeoples);
        //

        recyclerViewFindPeoples.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        // aqui tinha o clique do recycler


        btnVerComunidades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Leva até a lista de comunidades
                Intent intent = new Intent(getContext(), ListaComunidadesActivity.class);
                //Intent intent = new Intent(getContext(), TesteComDiffGrupoActivity.class);
                startActivity(intent);
            }
        });

        //Oculta a interface e exibe o recycler caso o foco seja o search e caso não seja,
        // será o efeito contrário..
        searchViewFindPeoples.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    recyclerViewFindPeoples.setVisibility(View.VISIBLE);
                    linearLayoutRandomFriends.setVisibility(View.GONE);
                } else {
                    recyclerViewFindPeoples.setVisibility(View.GONE);
                    linearLayoutRandomFriends.setVisibility(View.VISIBLE);
                }
            }
        });

        VerificaEpilpesia.verificarEpilpesiaExibeGifLocal(getContext(),
                R.drawable.ic_gif_grupos_publicos, imgViewProcurarGrupos);

        imgViewProcurarGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaGruposPublicos();
            }
        });

        btnProcurarGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaGruposPublicos();
            }
        });

        return view;
    }

    private void recuperarPessoa(String idPessoa) {

        DatabaseReference recuperarValor = firebaseRef.child("usuarios")
                .child(idPessoa);

        valueEventListener = recuperarValor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    try {
                        //Adicionado ouvinte de mudanças
                        //adapterFindPeoples.notifyDataSetChanged();

                        Usuario usuarioFinal = snapshot.getValue(Usuario.class);
                        listaUsuarios.add(usuarioFinal);
                        adapterFindPeoples.notifyDataSetChanged();

                        //Configura evento de clique no recyclerView
                        recyclerViewFindPeoples.addOnItemTouchListener(new RecyclerItemClickListener(
                                getActivity(),
                                recyclerViewFindPeoples,
                                new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        try {
                                            Usuario usuarioSelecionado = listaUsuarios.get(position);
                                            recuperarValor.removeEventListener(valueEventListener);
                                            listaUsuarios.clear();
                                            DatabaseReference verificaBlock = firebaseRef
                                                    .child("blockUser").child(idUsuarioAtual).child(usuarioSelecionado.getIdUsuario());
                                            verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.getValue() != null) {
                                                        ToastCustomizado.toastCustomizadoCurto("Perfil do usuário indisponível!", getContext());
                                                    } else {
                                                        handler.removeCallbacksAndMessages(null);
                                                        searchViewFindPeoples.setQuery("", false);
                                                        searchViewFindPeoples.setIconified(true);
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
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onLongItemClick(View view, int position) {

                                    }

                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    }
                                }
                        ));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                recuperarValor.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void pesquisarPessoas(String s) {
        //Limpar lista
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);


        try {
            listaUsuarios.clear();
            adapterFindPeoples.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //Pesquisar usuário caso o campo digitado não esteja vazio.
        if (s.length() > 0) {
            DatabaseReference searchUsuarioRef = usuarioRef;
            Query query = searchUsuarioRef.orderByChild("nomeUsuarioPesquisa")
                    .startAt(s)
                    .endAt(s + "\uf8ff");
            try {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Limpa a lista.
                        listaUsuarios.clear();


                        if (snapshot.getValue() == null) {

                        } else {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                //Verifica se é o usuário logado, caso seja oculte ele da lista
                                Usuario usuarioQuery = snap.getValue(Usuario.class);
                                idUsuarioAlvo = usuarioQuery.getIdUsuario();

                                DatabaseReference verificaNome = firebaseRef.child("usuarios")
                                        .child(idUsuarioAlvo);

                                verificaNome.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshotVerifica) {
                                        if (snapshotVerifica.getValue() != null) {
                                            Usuario usuarioReceptNome = snapshotVerifica.getValue(Usuario.class);

                                            if (usuarioReceptNome.getExibirApelido().equals("sim")) {

                                            } else {
                                                if (idUsuarioLogado.equals(usuarioReceptNome.getIdUsuario())) {

                                                } else {
                                                    recuperarPessoa(usuarioReceptNome.getIdUsuario());
                                                    //listaUsuarios.add(usuarioRecept);
                                                    //adapterFindPeoples.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                        verificaNome.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        query.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Query queryApelido = usuarioRef.orderByChild("apelidoUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");

                queryApelido.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotApelido) {
                        //Limpa a lista.
                        listaUsuarios.clear();

                        if (snapshotApelido.getValue() == null) {

                        } else {
                            for (DataSnapshot snapApelido : snapshotApelido.getChildren()) {
                                //Verifica se é o usuário logado, caso seja oculte ele da lista
                                Usuario usuarioApelido = snapApelido.getValue(Usuario.class);
                                idUsuarioAlvo = usuarioApelido.getIdUsuario();

                                DatabaseReference verificaApelido = firebaseRef.child("usuarios")
                                        .child(idUsuarioAlvo);

                                verificaApelido.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshotApelido) {
                                        if (snapshotApelido.getValue() != null) {
                                            Usuario usuarioReceptApelido = snapshotApelido.getValue(Usuario.class);
                                            if (usuarioReceptApelido.getExibirApelido().equals("não")) {

                                            } else {
                                                if (idUsuarioLogado.equals(usuarioReceptApelido.getIdUsuario())) {

                                                } else {
                                                    recuperarPessoa(usuarioReceptApelido.getIdUsuario());
                                                    //listaUsuarios.add(usuarioRecept);
                                                    //adapterFindPeoples.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                        verificaApelido.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        queryApelido.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                listaUsuarios.clear();
                adapterFindPeoples.notifyDataSetChanged();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private void inicializandoComponentes(View view) {
        searchViewFindPeoples = view.findViewById(R.id.searchViewFindPeoples);
        recyclerViewFindPeoples = view.findViewById(R.id.recyclerFindPeoples);
        shimmerFindPeople = view.findViewById(R.id.shimmerAmigos);
        imageSliderAmigos = view.findViewById(R.id.imageSliderAmigos);
        linearLayoutRandomFriends = view.findViewById(R.id.linearLayoutRandomFriends);
        btnVerComunidades = view.findViewById(R.id.btnVerComunidades);
        imgViewProcurarGrupos = view.findViewById(R.id.imgViewProcurarGrupos);
        btnProcurarGrupos = view.findViewById(R.id.btnProcurarGrupos);
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

    private void irParaGruposPublicos() {
        Intent intent = new Intent(getActivity(), GruposPublicosActivity.class);
        startActivity(intent);
    }
}
