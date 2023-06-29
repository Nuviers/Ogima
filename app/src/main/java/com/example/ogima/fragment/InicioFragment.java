package com.example.ogima.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.activity.PaginacaoTesteActivity;
import com.example.ogima.adapter.AdapterPostagens;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InicioFragment extends Fragment {

    private ImageView imgViewStickerOne;
    private RecyclerView recyclerPostagensInicio;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private List<Postagem> listaPostagens = new ArrayList<>();
    private AdapterPostagens adapterPostagens;
    private ChipGroup chipGroupPublico;
    private Chip chipSeguindo, chipAmigos, chipParceiros, chipSeguidores;
    private Postagem postagemDetalhe;
    private String verificaRelacao;
    private String idChildren;


    //teste
    private Button buttonTeste;

    public InicioFragment() {

    }

    /*

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (adapterPostagens.exoPlayer != null) {
                adapterPostagens.pausePlayer();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (adapterPostagens.exoPlayer != null) {
                adapterPostagens.startPlayer();
                adapterPostagens.seekTo();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (adapterPostagens.exoPlayer != null) {
                adapterPostagens.pausePlayer();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (adapterPostagens.exoPlayer != null) {
                adapterPostagens.releasePlayer();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
     */


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);
        inicializarComponentes(view);

        buttonTeste = view.findViewById(R.id.buttonTesteAll);
        //Teste
        buttonTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //*Intent intent = new Intent(getContext(), PaginacaoTesteActivity.class);


                DailyShort dailyShortTeste1 = new DailyShort("123", "cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t",
                        "video", "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4",
                        -1687927277594L);

                DatabaseReference salvarDaily1Ref = firebaseRef.child("dailyShorts")
                        .child("cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t").child("123");

                salvarDaily1Ref.setValue(dailyShortTeste1);

                DailyShort dailyShort2 = new DailyShort("457", "cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t",
                        "video", "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/REST+API+Retrofit+MVVM+Course+Summary.mp4",
                        -1687927289603L);

                DatabaseReference salvarDaily2Ref = firebaseRef.child("dailyShorts")
                        .child("cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t").child("457");

                salvarDaily2Ref.setValue(dailyShort2);

                DailyShort dailyShort3 = new DailyShort("854", "cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t",
                        "imagem", "https://images.unsplash.com/photo-1508285296015-c0b524447532?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=401&q=80",
                        -1687927289604L);

                DatabaseReference salvarDaily3Ref = firebaseRef.child("dailyShorts")
                        .child("cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t").child("854");

                salvarDaily3Ref.setValue(dailyShort3);

                /*
                Intent intent = new Intent(getContext(), ComunidadePostagensActivity.class);
                intent.putExtra("idComunidade", "-NTnGKfs7jorAtCoC0tH");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                 */
            }
        });

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerPostagensInicio.setLayoutManager(linearLayoutManager);
        recyclerPostagensInicio.setHasFixedSize(true);

        /* TIRAR O COMENTÁRIO, ISSO É PARA EVITAR GASTO ENQUANTO EU TESTO OS VÍDEOS
        chipGroupPublico.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (chipSeguindo.isChecked()) {
                    ToastCustomizado.toastCustomizadoCurto("Postagens seguindo", getContext());
                    exibirPostagemPorPublico("seguindo");
                } else if (chipAmigos.isChecked()) {
                    exibirPostagemPorPublico("friends");
                    ToastCustomizado.toastCustomizadoCurto("Postagens amigos", getContext());
                } else if (chipParceiros.isChecked()) {
                    exibirPostagemPorPublico("parceiros");
                    ToastCustomizado.toastCustomizadoCurto("Postagens parceiros", getContext());
                } else if (chipSeguidores.isChecked()) {
                    exibirPostagemPorPublico("seguidores");
                    ToastCustomizado.toastCustomizadoCurto("Postagens seguidores", getContext());
                } else if (!chipSeguidores.isChecked() && !chipSeguindo.isChecked()
                        && !chipParceiros.isChecked() && !chipAmigos.isChecked()) {
                    Intent intent = new Intent(getActivity(), NavigationDrawerActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        if (!chipSeguidores.isChecked() && !chipSeguindo.isChecked()
                && !chipParceiros.isChecked() && !chipAmigos.isChecked()) {
            if (adapterPostagens != null) {

            } else {
                adapterPostagens = new AdapterPostagens(listaPostagens, getContext());
            }
            recyclerPostagensInicio.setAdapter(adapterPostagens);
            exibirPostagens();
        }

         */

        return view;
    }

    private void exibirPostagens() {
        DatabaseReference todasPostagensRef = firebaseRef
                .child("postagens");
        todasPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        idChildren = snapshot1.getKey();
                        if (!idChildren.equals(idUsuario)) {
                            DatabaseReference detalhesPostagemRef = firebaseRef
                                    .child("postagens").child(idChildren);
                            detalhesPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                            Postagem postagem = snapshot1.getValue(Postagem.class);
                                            if (postagem.getPublicoPostagem().equals("Todos")) {
                                                listaPostagens.add(postagem);
                                                adapterPostagens.notifyDataSetChanged();
                                            } else if (postagem.getPublicoPostagem().equals("Somente amigos")
                                                    || postagem.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                                DatabaseReference verificaAmizadeRef = firebaseRef
                                                        .child("friends").child(idUsuario).child(postagem.getIdDonoPostagem());
                                                verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.getValue() != null) {
                                                            listaPostagens.add(postagem);
                                                            adapterPostagens.notifyDataSetChanged();
                                                        }
                                                        verificaAmizadeRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            } else if (postagem.getPublicoPostagem().equals("Somente seguidores")
                                                    || postagem.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                                DatabaseReference verificaSeguidorRef = firebaseRef
                                                        .child("seguindo").child(idUsuario).child(idChildren);
                                                verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.getValue() != null) {
                                                            listaPostagens.add(postagem);
                                                            adapterPostagens.notifyDataSetChanged();
                                                        }
                                                        verificaSeguidorRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                    detalhesPostagemRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //ToastCustomizado.toastCustomizadoCurto("Keyy " + idChildren, getContext());
                        }
                    }
                }
                todasPostagensRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exibirPostagemPorPublico(String publicoDefinido) {
        if (listaPostagens != null || listaPostagens.size() > 0) {
            listaPostagens.clear();
            adapterPostagens.notifyDataSetChanged();
        }
        DatabaseReference postagensRef = firebaseRef
                .child(publicoDefinido).child(idUsuario);
        postagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapPostagem : snapshot.getChildren()) {
                        String idChildrenPostagem = snapPostagem.getKey();
                        if (!idChildrenPostagem.equals(idUsuario)) {
                            DatabaseReference detalhesPostagemRef = firebaseRef
                                    .child("postagens").child(idChildrenPostagem);
                            detalhesPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        for (DataSnapshot snapDetalhe : snapshot.getChildren()) {
                                            Postagem postagem = snapDetalhe.getValue(Postagem.class);
                                            if (postagem.getPublicoPostagem().equals("Todos")) {
                                                listaPostagens.add(postagem);
                                                adapterPostagens.notifyDataSetChanged();
                                            } else if (postagem.getPublicoPostagem().equals("Somente amigos")
                                                    || postagem.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                                DatabaseReference verificaAmizadeRef = firebaseRef
                                                        .child("friends").child(idUsuario).child(postagem.getIdDonoPostagem());
                                                verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.getValue() != null) {
                                                            listaPostagens.add(postagem);
                                                            adapterPostagens.notifyDataSetChanged();
                                                        }
                                                        verificaAmizadeRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            } else if (postagem.getPublicoPostagem().equals("Somente seguidores")
                                                    || postagem.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                                DatabaseReference verificaSeguidorRef = firebaseRef
                                                        .child("seguindo").child(idUsuario).child(idChildren);
                                                verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.getValue() != null) {
                                                            listaPostagens.add(postagem);
                                                            adapterPostagens.notifyDataSetChanged();
                                                        }
                                                        verificaSeguidorRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                    detalhesPostagemRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
                postagensRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void inicializarComponentes(View view) {
        recyclerPostagensInicio = view.findViewById(R.id.recyclerPostagensInicio);
        chipGroupPublico = view.findViewById(R.id.chipGroupPublico);
        chipSeguindo = view.findViewById(R.id.chipSeguindo);
        chipAmigos = view.findViewById(R.id.chipAmigos);
        chipParceiros = view.findViewById(R.id.chipParceiros);
        chipSeguidores = view.findViewById(R.id.chipSeguidores);
    }
}