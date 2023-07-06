package com.example.ogima.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.adapter.AdapterGridPostagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView imgViewFundoProfile, imgViewFotoProfile, imgViewDailyShortInc;
    private TextView txtViewNameProfile, txtViewTitleSeguidores,
            txtViewTitleAmigos, txtViewTitleSeguindo, txtViewNrSeguidores,
            txtViewNrAmigos, txtViewNrSeguindo, txtViewTitleViewsProfile,
            txtViewVerVisualizacoes, txtViewTitleSolicitacao, txtViewNrSolicitacoes,
            txtViewTitleDailyShortInc, txtViewMsgSemFotos, txtViewSemPostagemMsg,
            txtViewTitlePostagem;
    private View viewBarraFundo;
    private ImageButton imgBtnEditarProfile, imgBtnViewsProfile,
            imgBtnSolicitacaoAmizade, imgBtnCamFotoProfile, imgBtnGaleriaFotoProfile,
            imgBtnVideoPostagem, imgBtnGifPostagem, imgBtnGaleriaPostagem, imgBtnCameraPostagem;
    private CardView cardSolicitacaoAmizade;
    private RecyclerView recyclerViewFotos, recyclerViewPostagens;
    private Button btnViewAddPostagens;

    private StorageReference storageRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private long nrSeguidores = 0;
    private boolean atualizarProfile = true;
    private GridLayoutManager gridLayoutManagerFoto;
    private GridLayoutManager gridLayoutManagerPostagem;
    private AdapterGridPostagem adapterGridFoto;
    private AdapterGridPostagem adapterGridPostagem;
    private List<Postagem> listaFotos = new ArrayList<>();
    private List<Postagem> listaPostagens = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();

        if (atualizarProfile) {
            atualizarProfile = false;
            recuperarDadosUsuario();
            configRecyclers();
            recuperarFotos();
            recuperarPostagens();
        }
    }

    public ProfileFragment() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        inicializandoComponentes(view);
        clickListeners();
        return view;
    }

    private void clickListeners() {
        viewBarraFundo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaEdicaoDePerfil();
            }
        });

        imgBtnEditarProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaEdicaoDePerfil();
            }
        });
    }

    private void recuperarDadosUsuario() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (nomeUsuarioAjustado != null) {
                    txtViewNameProfile.setText(nomeUsuarioAjustado);
                }

                exibirFotoFundo(fotoUsuario, fundoUsuario, epilepsia);

                recuperarNrSeguidores();

                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    txtViewNrAmigos.setText(String.valueOf(listaIdAmigos.size()));
                } else {
                    txtViewNrAmigos.setText("0");
                }

                if (listaIdSeguindo != null && listaIdSeguindo.size() > 0) {
                    txtViewNrSeguindo.setText(String.valueOf(listaIdSeguindo.size()));
                } else {
                    txtViewNrSeguindo.setText("0");
                }

                if (usuarioAtual.getViewsPerfil() != -1) {
                    txtViewTitleViewsProfile.setText(String.valueOf(usuarioAtual.getViewsPerfil())
                            + " visualizações no seu perfil hoje!");
                } else {
                    txtViewTitleViewsProfile.setText("0 visualizações no seu perfil hoje");
                }

                if (usuarioAtual.getPedidosAmizade() != -1) {
                    txtViewNrSolicitacoes.setText(String.valueOf(usuarioAtual.getPedidosAmizade()));
                } else {
                    txtViewNrSolicitacoes.setText("0");
                }

                if (usuarioAtual.getUrlLastDaily() != null
                        && !usuarioAtual.getUrlLastDaily().isEmpty()) {
                    exibirUltimoDaily(usuarioAtual.getUrlLastDaily(), epilepsia);
                }
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void exibirUltimoDaily(String urlDaily, boolean epilpesia) {
        if (epilpesia) {
            GlideCustomizado.montarGlideEpilepsia(getContext(),
                    urlDaily, imgViewDailyShortInc, android.R.color.transparent);
        } else {
            GlideCustomizado.montarGlide(getContext(),
                    urlDaily, imgViewDailyShortInc, android.R.color.transparent);
        }
    }

    private void exibirFotoFundo(String fotoUsuario, String fundoUsuario, boolean epilepsia) {

        boolean fotoExistente = false;
        boolean fundoExistente = false;

        if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
            fotoExistente = true;
        }

        if (fundoUsuario != null && !fundoUsuario.isEmpty()) {
            fundoExistente = true;
        }

        if (epilepsia) {

            if (fotoExistente) {
                GlideCustomizado.montarGlideEpilepsia(getContext(),
                        fotoUsuario, imgViewFotoProfile, android.R.color.transparent);
            }

            if (fundoExistente) {
                GlideCustomizado.montarGlideFotoEpilepsia(getContext(),
                        fundoUsuario, imgViewFundoProfile, android.R.color.transparent);
            }

        } else {

            if (fotoExistente) {
                GlideCustomizado.montarGlide(getContext(),
                        fotoUsuario, imgViewFotoProfile, android.R.color.transparent);
            }

            if (fundoExistente) {
                GlideCustomizado.montarGlideFoto(getContext(),
                        fundoUsuario, imgViewFundoProfile, android.R.color.transparent);
            }
        }
    }

    private void recuperarNrSeguidores() {
        DatabaseReference verificaSeguidoresRef = firebaseRef.child("seguidores")
                .child(idUsuario);

        verificaSeguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        nrSeguidores = snapshot1.getChildrenCount();
                    }
                    txtViewNrSeguidores.setText(String.valueOf(nrSeguidores));
                }
                verificaSeguidoresRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void irParaEdicaoDePerfil() {
        Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void configRecyclers() {

        //Fotos
        if (gridLayoutManagerFoto == null) {
            gridLayoutManagerFoto = new GridLayoutManager(getContext(), 2);
        }

        recyclerViewFotos.setHasFixedSize(true);
        recyclerViewFotos.setLayoutManager(gridLayoutManagerFoto);

        if (adapterGridFoto == null) {
            adapterGridFoto = new AdapterGridPostagem(listaFotos, getContext());
        }

        recyclerViewFotos.setAdapter(adapterGridFoto);

        //Postagens
        if (gridLayoutManagerPostagem == null) {
            gridLayoutManagerPostagem = new GridLayoutManager(getContext(), 2);
        }

        recyclerViewPostagens.setHasFixedSize(true);
        recyclerViewPostagens.setLayoutManager(gridLayoutManagerPostagem);

        if (adapterGridPostagem == null) {
            adapterGridPostagem = new AdapterGridPostagem(listaPostagens, getContext());
        }
        recyclerViewPostagens.setAdapter(adapterGridPostagem);
    }

    private void recuperarFotos() {
        Query recuperarFotos = firebaseRef.child("postagens")
                .child(idUsuario).orderByChild("postType").equalTo(false);

        recuperarFotos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Postagem fotoPostagem = snapshot1.getValue(Postagem.class);
                        adicionarFotoNaLista(fotoPostagem);
                    }
                    mudarParametroLayoutFoto();
                    adapterGridFoto.notifyDataSetChanged();
                } else {
                    mudarParametroLayoutFoto();
                }
                recuperarFotos.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarPostagens() {
        Query recuperarPostagens = firebaseRef.child("postagens")
                .child(idUsuario).orderByChild("postType").equalTo(true);

        recuperarPostagens.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        Postagem postagem = snapshot1.getValue(Postagem.class);
                        adicionarPostagemNaLista(postagem);
                    }
                    mudarParametroLayoutPostagem();
                    adapterGridPostagem.notifyDataSetChanged();
                } else {
                    mudarParametroLayoutPostagem();
                }
                recuperarPostagens.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void adicionarFotoNaLista(Postagem fotoPostagem) {
        if (listaFotos != null && listaFotos.size() > 0
                && listaFotos.contains(fotoPostagem)) {
            return;
        }

        listaFotos.add(fotoPostagem);
    }

    private void adicionarPostagemNaLista(Postagem newPostagem) {
        if (listaPostagens != null && listaPostagens.size() > 0
                && listaPostagens.contains(newPostagem)) {
            return;
        }

        listaPostagens.add(newPostagem);
    }

    private void mudarParametroLayoutFoto() {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtViewTitlePostagem.getLayoutParams();

        if (listaFotos != null && listaFotos.size() > 0) {
            // Define uma nova regra de layout_below com um novo componente
            params.addRule(RelativeLayout.BELOW, R.id.recyclerViewFotosProfile);

            // Aplica as alterações nos parâmetros de layout
            txtViewTitlePostagem.setLayoutParams(params);
            recyclerViewFotos.setVisibility(View.VISIBLE);
            txtViewMsgSemFotos.setVisibility(View.INVISIBLE);

        } else {
            // Define uma nova regra de layout_below com um novo componente
            params.addRule(RelativeLayout.BELOW, R.id.txtViewMsgSemFotos);

            // Aplica as alterações nos parâmetros de layout
            txtViewTitlePostagem.setLayoutParams(params);
            recyclerViewFotos.setVisibility(View.GONE);
            txtViewMsgSemFotos.setVisibility(View.VISIBLE);
        }
    }

    private void mudarParametroLayoutPostagem() {

        RelativeLayout.LayoutParams paramsPost = (RelativeLayout.LayoutParams) btnViewAddPostagens.getLayoutParams();

        if (listaPostagens != null && listaPostagens.size() > 0) {
            paramsPost.addRule(RelativeLayout.BELOW, R.id.recyclerViewPostagensProfile);
            btnViewAddPostagens.setLayoutParams(paramsPost);
            txtViewSemPostagemMsg.setVisibility(View.INVISIBLE);
            recyclerViewPostagens.setVisibility(View.VISIBLE);
        } else {
            paramsPost.addRule(RelativeLayout.BELOW, R.id.txtViewSemPostagemMsg);
            btnViewAddPostagens.setLayoutParams(paramsPost);
            txtViewSemPostagemMsg.setVisibility(View.VISIBLE);
            recyclerViewPostagens.setVisibility(View.GONE);
        }
    }

    private void inicializandoComponentes(View view) {
        //Include

        //inc_perfil_cabecalho
        imgViewFundoProfile = view.findViewById(R.id.imgViewIncFundoProfile);
        imgViewFotoProfile = view.findViewById(R.id.imgViewIncFotoProfile);
        txtViewNameProfile = view.findViewById(R.id.txtViewNameProfile);

        //inc_perfil_card_relacoes
        txtViewTitleSeguidores = view.findViewById(R.id.txtViewTitleSeguidores);
        txtViewTitleAmigos = view.findViewById(R.id.txtViewTitleAmigos);
        txtViewTitleSeguindo = view.findViewById(R.id.txtViewTitleSeguindo);
        txtViewNrSeguidores = view.findViewById(R.id.txtViewNrSeguidores);
        txtViewNrAmigos = view.findViewById(R.id.txtViewNrAmigos);
        txtViewNrSeguindo = view.findViewById(R.id.txtViewNrSeguindo);

        //inc_daily_short
        imgViewDailyShortInc = view.findViewById(R.id.imgViewDailyShortInc);
        txtViewTitleDailyShortInc = view.findViewById(R.id.txtViewTitleDailyShortInc);

        //layout
        viewBarraFundo = view.findViewById(R.id.viewBarraFundo);
        imgBtnEditarProfile = view.findViewById(R.id.imgBtnEditarProfile);
        imgBtnViewsProfile = view.findViewById(R.id.imgBtnViewsProfile);
        txtViewTitleViewsProfile = view.findViewById(R.id.txtViewTitleViewsProfile);
        txtViewVerVisualizacoes = view.findViewById(R.id.txtViewVerVisualizacoesProfile);
        cardSolicitacaoAmizade = view.findViewById(R.id.cardSolicitacaoAmizade);
        imgBtnSolicitacaoAmizade = view.findViewById(R.id.imgBtnSolicitacaoAmizade);
        txtViewTitleSolicitacao = view.findViewById(R.id.txtViewTitleSolicitacaoAmizade);
        txtViewNrSolicitacoes = view.findViewById(R.id.txtViewNrSolicitacoesAmizade);
        imgBtnCamFotoProfile = view.findViewById(R.id.imgBtnCamFotoProfile);
        imgBtnGaleriaFotoProfile = view.findViewById(R.id.imgBtnGaleriaFotoProfile);
        txtViewMsgSemFotos = view.findViewById(R.id.txtViewMsgSemFotos);
        txtViewSemPostagemMsg = view.findViewById(R.id.txtViewSemPostagemMsg);
        recyclerViewFotos = view.findViewById(R.id.recyclerViewFotosProfile);
        recyclerViewPostagens = view.findViewById(R.id.recyclerViewPostagensProfile);
        btnViewAddPostagens = view.findViewById(R.id.btnViewAddPostagensProfile);
        imgBtnVideoPostagem = view.findViewById(R.id.imgBtnVideoPostagem);
        imgBtnGifPostagem = view.findViewById(R.id.imgBtnGifPostagem);
        imgBtnGaleriaPostagem = view.findViewById(R.id.imgBtnGaleriaPostagem);
        imgBtnCameraPostagem = view.findViewById(R.id.imgBtnCameraPostagem);
        txtViewTitlePostagem = view.findViewById(R.id.txtViewTitlePostagem);
    }
}