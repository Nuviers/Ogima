package com.example.ogima.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.activity.AddDailyShortsActivity;
import com.example.ogima.activity.UsersDailyShortsActivity;
import com.example.ogima.adapter.AdapterHeaderInicio;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FrameSuporteInicioFragment extends Fragment {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private RecyclerView recyclerViewInicial;
    private boolean dadosCarregados = false;
    private AdapterHeaderInicio adapterHeader;
    private LinearLayoutManager linearLayoutManager;

    private interface DadosUserLogado{
        void onRecuperado(boolean epilepsia);
        void onSemDados();
        void onError(String message);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!dadosCarregados) {
            dadosUserAtual(new DadosUserLogado() {
                @Override
                public void onRecuperado(boolean epilepsia) {
                    configRecyclerView();
                    adapterHeader.setStatusEpilepsia(epilepsia);
                    dadosCarregados = true;
                }

                @Override
                public void onSemDados() {
                    dadosCarregados = true;
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizadoCurto("Error: " + message, requireActivity());
                }
            });
        }
    }

    public FrameSuporteInicioFragment() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frame_suporte_inicio, container, false);
        inicializandoComponentes(view);
        return view;
    }

    private void configRecyclerView(){
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerViewInicial.setHasFixedSize(true);
        recyclerViewInicial.setLayoutManager(linearLayoutManager);

        if (recyclerViewInicial.getOnFlingListener() == null) {
            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(recyclerViewInicial);
        }

        if (adapterHeader == null) {
            adapterHeader = new AdapterHeaderInicio(requireContext());
        }

        ConcatAdapter concatAdapter = new ConcatAdapter(adapterHeader);
        recyclerViewInicial.setAdapter(concatAdapter);
    }

    private void dadosUserAtual(DadosUserLogado callback){
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                callback.onRecuperado(epilepsia);
            }

            @Override
            public void onSemDados() {
                callback.onSemDados();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }


    private void inicializandoComponentes(View view) {
        recyclerViewInicial = view.findViewById(R.id.recyclerViewInicial);
    }
}