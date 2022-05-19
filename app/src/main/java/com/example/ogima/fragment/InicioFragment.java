package com.example.ogima.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterPostagensInicio;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment  {

    private ImageView imgViewStickerOne;
    private RecyclerView recyclerFotosPostagensHome;
    private AdapterPostagensInicio adapterPostagensInicio;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference fotosPostagensRef, seguindoRef;
    private List<Postagem> listaFotosPostagens;
    private Postagem postagem;
    private Usuario usuarioSeguindo;


    public InicioFragment() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        listaFotosPostagens = new ArrayList<>();

        seguindoRef = firebaseRef.child("seguindo").child(idUsuario);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);
        inicializarComponentes(view);

        //Configurações iniciais.
        recyclerFotosPostagensHome.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerFotosPostagensHome.setHasFixedSize(true);
        adapterPostagensInicio = new AdapterPostagensInicio(listaFotosPostagens, getActivity());
        recyclerFotosPostagensHome.setAdapter(adapterPostagensInicio);

        verificarSeguindoId();
        //recyclerPostagens();
        exibirStickers();

        return view;
    }

    private void inicializarComponentes(View view){
        imgViewStickerOne = view.findViewById(R.id.imgViewStickerOne);
        recyclerFotosPostagensHome = view.findViewById(R.id.recyclerFotosPostagensHome);
    }

    private void exibirStickers(){
        GlideCustomizado.montarGlideFoto(getContext(), "https://media.giphy.com/media/QmH8OnsBQvC4yn8BnX/giphy.gif",
                imgViewStickerOne, android.R.color.transparent);
    }

    private void recyclerPostagens(String idSeguindo){

    //ToastCustomizado.toastCustomizadoCurto("Id " + idSeguindo, getContext());
    fotosPostagensRef = firebaseRef.child("postagensUsuario").child(idSeguindo);

    fotosPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for(DataSnapshot snapChildren : snapshot.getChildren()){
                postagem = snapChildren.getValue(Postagem.class);
                adapterPostagensInicio.notifyDataSetChanged();
                listaFotosPostagens.add(postagem);
            }
            fotosPostagensRef.removeEventListener(this);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    }

    private void verificarSeguindoId(){

        seguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    for(DataSnapshot snapSeguindo : snapshot.getChildren()){
                        usuarioSeguindo = snapSeguindo.getValue(Usuario.class);
                        recyclerPostagens(usuarioSeguindo.getIdUsuario());
                    }
                    seguindoRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
