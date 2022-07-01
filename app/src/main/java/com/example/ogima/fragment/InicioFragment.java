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

public class InicioFragment extends Fragment  {

    private ImageView imgViewStickerOne;
    private RecyclerView recyclerFotosPostagensHome;
    private AdapterPostagensInicio adapterPostagensInicio;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference fotosPostagensRef, seguindoRef, usuarioFotoNomeRef,
            todasFotosPostagensRef;
    private List<Postagem> listaFotosPostagens = new ArrayList<>();
    private List<Usuario> listaUsuarioFotosPostagens = new ArrayList<>();
    private Postagem postagem;
    private Usuario usuarioSeguindo;


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


        recyclerFotosPostagensHome.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerFotosPostagensHome.setHasFixedSize(true);
        verificarSeguindoId();
        adapterPostagensInicio = new AdapterPostagensInicio(listaFotosPostagens, getActivity());
        recyclerFotosPostagensHome.setAdapter(adapterPostagensInicio);
        return view;
    }

    private void inicializarComponentes(View view){
        recyclerFotosPostagensHome = view.findViewById(R.id.recyclerFotosPostagensHome);
    }

    private void exibirStickers(){

    }

    private void recyclerPostagens(String idChildrenRecebido){

        try{

            if(idChildrenRecebido.equals(idUsuario)){

            }else{
                fotosPostagensRef = firebaseRef.child("postagensUsuario").child(idChildrenRecebido);

                fotosPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snapChildren : snapshot.getChildren()){
                            postagem = snapChildren.getValue(Postagem.class);

                            //Verificando postagem detalhada
                            DatabaseReference postagemDetalhadaRef = firebaseRef.child("postagensUsuario")
                                    .child(postagem.getIdDonoPostagem()).child(postagem.getIdPostagem());

                            postagemDetalhadaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null){
                                        Postagem postagemDetalhada = snapshot.getValue(Postagem.class);
                                        //ToastCustomizado.toastCustomizadoCurto("Público V2 - " + postagemDetalhada.getIdPostagem(),getContext());
                                        if(postagemDetalhada.getPublicoPostagem().equals("Somente amigos")){
                                            DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                                    .child(idUsuario).child(postagemDetalhada.getIdDonoPostagem());
                                            analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        listaFotosPostagens.add(postagemDetalhada);
                                                        adapterPostagensInicio.notifyDataSetChanged();
                                                        Usuario usuarioAmigo = snapshot.getValue(Usuario.class);
                                                        //ToastCustomizado.toastCustomizadoCurto("Existe - " + usuarioAmigo.getIdUsuario(), getContext());
                                                    }
                                                    analisaAmizadeRef.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }else if (postagemDetalhada.getPublicoPostagem().equals("Todos")){
                                            listaFotosPostagens.add(postagemDetalhada);
                                            adapterPostagensInicio.notifyDataSetChanged();
                                        }else if (postagemDetalhada.getPublicoPostagem().equals("Somente amigos e seguidores")){
                                            DatabaseReference analisaAmizadeRef = firebaseRef.child("friends")
                                                    .child(idUsuario).child(postagemDetalhada.getIdDonoPostagem());
                                            analisaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        DatabaseReference analisaSeguidorRef = firebaseRef.child("seguindo")
                                                                .child(idUsuario).child(postagemDetalhada.getIdDonoPostagem());
                                                        analisaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if(snapshot.exists()){
                                                                    listaFotosPostagens.add(postagemDetalhada);
                                                                    adapterPostagensInicio.notifyDataSetChanged();
                                                                }
                                                                analisaSeguidorRef.removeEventListener(this);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                    analisaAmizadeRef.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }else if (postagemDetalhada.getPublicoPostagem().equals("Somente seguidores")){
                                            DatabaseReference analisaSeguidorRef = firebaseRef.child("seguindo")
                                                    .child(idUsuario).child(postagemDetalhada.getIdDonoPostagem());
                                            analisaSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        listaFotosPostagens.add(postagemDetalhada);
                                                        adapterPostagensInicio.notifyDataSetChanged();
                                                    }
                                                    analisaSeguidorRef.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                    postagemDetalhadaRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        fotosPostagensRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void verificarSeguindoId(){


        todasFotosPostagensRef = firebaseRef.child("postagensUsuario");

        todasFotosPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    for(DataSnapshot snapChildren : snapshot.getChildren()){
                        String idChildren = snapChildren.getKey();
                        recyclerPostagens(idChildren);
                    }
                }
                todasFotosPostagensRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}