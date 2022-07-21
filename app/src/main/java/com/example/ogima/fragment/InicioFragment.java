package com.example.ogima.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterPostagens;
import com.example.ogima.adapter.AdapterPostagensInicio;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InicioFragment extends Fragment {

    private ImageView imgViewStickerOne;
    private RecyclerView recyclerFotosPostagensHome, recyclerPostagensInicio;
    private AdapterPostagensInicio adapterPostagensInicio;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference fotosPostagensRef, seguindoRef, usuarioFotoNomeRef,
            todasFotosPostagensRef;
    private List<Postagem> listaFotos = new ArrayList<>();
    private List<Postagem> listaPostagens = new ArrayList<>();
    private List<Usuario> listaUsuarioFotosPostagens = new ArrayList<>();
    private Postagem postagem;
    private Usuario usuarioSeguindo;

    private AdapterPostagens adapterPostagens;
    private DatabaseReference exibirPostagemRef;
    private Postagem postagemInicio;
    private List<Postagem> todasPostagens = new ArrayList<>();
    private DatabaseReference postagensRef, verificaAmizadeRef,
            verificaSeguidorRef, fotosRef, exibirFotosRef;
    private String idChildrenPostagem, idChildrenFoto;

    public InicioFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);
        inicializarComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        seguindoRef = firebaseRef.child("seguindo").child(idUsuario);

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerPostagensInicio.setLayoutManager(linearLayoutManager);
        recyclerPostagensInicio.setHasFixedSize(true);

        //todasPostagens = listaPostagens;
        //Collections.copy(todasPostagens, listaFotosPostagens);


        //Verificando todas postagens
        postagensRef = firebaseRef.child("postagens");
        postagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapChildren : snapshot.getChildren()) {
                        idChildrenPostagem = snapChildren.getKey();
                        exibirPostagemRef = firebaseRef.child("postagens").child(idChildrenPostagem);
                    }
                    if (!idChildrenPostagem.equals(idUsuario)) {
                        exibirPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        Postagem postagem = snapshot1.getValue(Postagem.class);
                                        if (postagem.getPublicoPostagem().equals("Todos")) {
                                            listaPostagens.add(postagem);
                                        } else if (postagem.getPublicoPostagem().equals("Somente amigos")) {
                                            verificaAmizadeRef = firebaseRef.child("friends")
                                                    .child(idUsuario).child(postagem.getIdDonoPostagem());
                                            verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.getValue() != null) {
                                                        listaPostagens.add(postagem);
                                                    }
                                                    verificaAmizadeRef.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        } else if (postagem.getPublicoPostagem().equals("Somente seguidores")) {
                                            verificaSeguidorRef = firebaseRef.child("seguindo")
                                                    .child(idUsuario).child(postagem.getIdDonoPostagem());
                                            verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.getValue() != null) {
                                                        listaPostagens.add(postagem);
                                                    }
                                                    verificaSeguidorRef.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        } else if (postagem.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                            verificaAmizadeRef = firebaseRef.child("friends")
                                                    .child(idUsuario).child(postagem.getIdDonoPostagem());
                                            verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.getValue() != null) {
                                                        verificaSeguidorRef = firebaseRef.child("seguindo")
                                                                .child(idUsuario).child(postagem.getIdDonoPostagem());
                                                        verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.getValue() != null) {
                                                                    listaPostagens.add(postagem);
                                                                }
                                                                verificaSeguidorRef.removeEventListener(this);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }else{
                                                        verificaSeguidorRef = firebaseRef.child("seguindo")
                                                                .child(idUsuario).child(postagem.getIdDonoPostagem());
                                                        verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.getValue() != null) {
                                                                    listaPostagens.add(postagem);
                                                                }
                                                                verificaSeguidorRef.removeEventListener(this);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                    verificaAmizadeRef.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        } else if (postagem.getPublicoPostagem().equals("Privado")){}

                                        //ToastCustomizado.toastCustomizadoCurto("Postagem " + postagem.getTipoPostagem(), getContext());
                                    }
                                }
                                exibirStickers(listaPostagens);
                                exibirPostagemRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                postagensRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void exibirStickers(List<Postagem> listaNova) {

        //ToastCustomizado.toastCustomizadoCurto("Size lista " + listaNova.size(), getContext());

        //Caso não tenha nenhuma postagem
        if(listaNova.size() <= 0 || listaNova == null){

            //ToastCustomizado.toastCustomizadoCurto("Lista zerada", getContext());
            //Não existe postagens
            fotosRef = firebaseRef.child("fotosUsuario");
            fotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for(DataSnapshot snapshotChildren : snapshot.getChildren()){
                            idChildrenFoto = snapshotChildren.getKey();
                            exibirFotosRef = firebaseRef.child("fotosUsuario").child(idChildrenFoto);
                        }
                        if (!idChildrenFoto.equals(idUsuario)) {
                            exibirFotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                       for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                           Postagem postagemFoto = snapshot1.getValue(Postagem.class);
                                           if (postagemFoto.getPublicoPostagem().equals("Todos")) {
                                               listaFotos.add(postagemFoto);
                                           } else if (postagemFoto.getPublicoPostagem().equals("Somente amigos")) {
                                               verificaAmizadeRef = firebaseRef.child("friends")
                                                       .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                               verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                       if (snapshot.getValue() != null) {
                                                           listaFotos.add(postagemFoto);
                                                       }
                                                       verificaAmizadeRef.removeEventListener(this);
                                                   }

                                                   @Override
                                                   public void onCancelled(@NonNull DatabaseError error) {

                                                   }
                                               });
                                           } else if (postagemFoto.getPublicoPostagem().equals("Somente seguidores")) {
                                               verificaSeguidorRef = firebaseRef.child("seguindo")
                                                       .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                               verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                       if (snapshot.getValue() != null) {
                                                           listaFotos.add(postagemFoto);
                                                       }
                                                       verificaSeguidorRef.removeEventListener(this);
                                                   }

                                                   @Override
                                                   public void onCancelled(@NonNull DatabaseError error) {

                                                   }
                                               });
                                           } else if (postagemFoto.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                               verificaAmizadeRef = firebaseRef.child("friends")
                                                       .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                               verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                       if (snapshot.getValue() != null) {
                                                           verificaSeguidorRef = firebaseRef.child("seguindo")
                                                                   .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                                           verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                               @Override
                                                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                   if (snapshot.getValue() != null) {
                                                                       listaFotos.add(postagemFoto);
                                                                   }
                                                                   verificaSeguidorRef.removeEventListener(this);
                                                               }

                                                               @Override
                                                               public void onCancelled(@NonNull DatabaseError error) {

                                                               }
                                                           });
                                                       }else{
                                                           verificaSeguidorRef = firebaseRef.child("seguindo")
                                                                   .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                                           verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                               @Override
                                                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                   if (snapshot.getValue() != null) {
                                                                       listaFotos.add(postagemFoto);
                                                                   }
                                                                   verificaSeguidorRef.removeEventListener(this);
                                                               }

                                                               @Override
                                                               public void onCancelled(@NonNull DatabaseError error) {

                                                               }
                                                           });
                                                       }
                                                       verificaAmizadeRef.removeEventListener(this);
                                                   }

                                                   @Override
                                                   public void onCancelled(@NonNull DatabaseError error) {

                                                   }
                                               });
                                           } else if (postagemFoto.getPublicoPostagem().equals("Privado")){}

                                           //ToastCustomizado.toastCustomizadoCurto("Fotos " + postagemFoto.getTipoPostagem(), getContext());
                                       }
                                    }
                                    if (adapterPostagens != null) {
                                    } else {
                                        adapterPostagens = new AdapterPostagens(listaFotos, getActivity());
                                    }
                                    recyclerPostagensInicio.setAdapter(adapterPostagens);
                                    exibirFotosRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    fotosRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            //Caso exista postagens
            fotosRef = firebaseRef.child("fotosUsuario");
            fotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for(DataSnapshot snapshotChildren : snapshot.getChildren()){
                            idChildrenFoto = snapshotChildren.getKey();
                            exibirFotosRef = firebaseRef.child("fotosUsuario").child(idChildrenFoto);
                        }
                        if (!idChildrenFoto.equals(idUsuario)) {
                            exibirFotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                            Postagem postagemFoto = snapshot1.getValue(Postagem.class);
                                            if (postagemFoto.getPublicoPostagem().equals("Todos")) {
                                                listaFotos.add(postagemFoto);
                                            } else if (postagemFoto.getPublicoPostagem().equals("Somente amigos")) {
                                                verificaAmizadeRef = firebaseRef.child("friends")
                                                        .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                                verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.getValue() != null) {
                                                            listaFotos.add(postagemFoto);
                                                        }
                                                        verificaAmizadeRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            } else if (postagemFoto.getPublicoPostagem().equals("Somente seguidores")) {
                                                verificaSeguidorRef = firebaseRef.child("seguindo")
                                                        .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                                verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.getValue() != null) {
                                                            listaFotos.add(postagemFoto);
                                                        }
                                                        verificaSeguidorRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            } else if (postagemFoto.getPublicoPostagem().equals("Somente amigos e seguidores")) {
                                                verificaAmizadeRef = firebaseRef.child("friends")
                                                        .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                                verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.getValue() != null) {
                                                            verificaSeguidorRef = firebaseRef.child("seguindo")
                                                                    .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                                            verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.getValue() != null) {
                                                                        listaFotos.add(postagemFoto);
                                                                    }
                                                                    verificaSeguidorRef.removeEventListener(this);
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }else{
                                                            verificaSeguidorRef = firebaseRef.child("seguindo")
                                                                    .child(idUsuario).child(postagemFoto.getIdDonoPostagem());
                                                            verificaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.getValue() != null) {
                                                                        listaFotos.add(postagemFoto);
                                                                    }
                                                                    verificaSeguidorRef.removeEventListener(this);
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }
                                                        verificaAmizadeRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            } else if (postagemFoto.getPublicoPostagem().equals("Privado")){}

                                            ///ToastCustomizado.toastCustomizadoCurto("Fotos " + postagemFoto.getTipoPostagem(), getContext());
                                        }
                                    }
                                    todasPostagens = listaNova;
                                    todasPostagens.addAll(listaFotos);
                                    if (adapterPostagens != null) {
                                    } else {
                                        adapterPostagens = new AdapterPostagens(todasPostagens, getActivity());
                                    }
                                    recyclerPostagensInicio.setAdapter(adapterPostagens);
                                    exibirFotosRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }else{
                        //Caso só exista postagens e não exista
                        //fotos
                        todasPostagens = listaNova;
                        if (adapterPostagens != null) {
                        } else {
                            adapterPostagens = new AdapterPostagens(todasPostagens, getActivity());
                        }
                        recyclerPostagensInicio.setAdapter(adapterPostagens);
                    }
                    fotosRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void inicializarComponentes(View view) {
        recyclerFotosPostagensHome = view.findViewById(R.id.recyclerFotosPostagensHome);
        recyclerPostagensInicio = view.findViewById(R.id.recyclerPostagensInicio);
    }
}