package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterHeaderInicio;
import com.example.ogima.adapter.AdapterLstcButton;
import com.example.ogima.adapter.AdapterLstcHeader;
import com.example.ogima.adapter.AdapterLstcInvitationHeader;
import com.example.ogima.adapter.AdapterLstcTitleHeader;
import com.example.ogima.adapter.AdapterMinhasComunidades;
import com.example.ogima.adapter.AdapterPreviewCommunities;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDAO;
import com.example.ogima.helper.ComunidadeDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Usuario;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ListaComunidadesActivityNEW extends AppCompatActivity implements AdapterPreviewCommunities.AnimacaoIntent, AdapterPreviewCommunities.RemoverComunidadeListener, AdapterPreviewCommunities.RecuperaPosicaoAnterior {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManagerHeader;
    private AdapterLstcHeader adapterLstcHeader;
    private AdapterLstcTitleHeader adapterTitlePublicas, adapterTitleSeguindo,
            adapterTitleMinhasComunidades, adapterTitleRecomendadas;
    private AdapterLstcButton adapterButtonMyCommunities,
            adapterButtonPublicas, adapterButtonSeguindo, adapterButtonRecomendadas;
    private AdapterLstcInvitationHeader adapterInvitationHeader;
    private ConcatAdapter concatAdapter;
    private ValueEventListener listenerConvites;
    private DatabaseReference conviteRef;
    private String idUsuario = "";
    private AdapterPreviewCommunities adapterMyCommunities, adapterPublicCommunities,
            adapterCommunityFollowing;
    private Query minhasComunidadesRef, comunidadesPublicasRef, comunidadesSeguindoRef;
    private List<Comunidade> listaMinhasComunidades = new ArrayList<>(), listaComunidadesPublicas = new ArrayList<>(),
            listaComunidadesSeguindo = new ArrayList<>();
    private ComunidadeDiffDAO minhasComunidadeDiffDAO, comunidadesPublicasDiffDAO,
            comunidadesSeguindoDiffDAO;
    private HashMap<String, Object> listaDadosMinhasComunidades = new HashMap<>(),
            listaDadosComunidadesPublicas = new HashMap<>(),
            listaDadosComunidadesSeguindo = new HashMap<>();

    private int mCurrentPosition = -1;
    private Comunidade comunidadeComparator;

    public ListaComunidadesActivityNEW() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        comunidadeComparator = new Comunidade(false, true);
    }

    public interface VerificaBlockCallback {
        void onAjustado(Comunidade comunidadeAjustada);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (linearLayoutManagerHeader != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManagerHeader.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Desliza ao recyclerView até a posição salva
        if (mCurrentPosition != -1 &&
                linearLayoutManagerHeader != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    //recyclerView.scrollToPosition(mCurrentPosition);
                }
            }, 100);
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listenerConvites != null) {
            conviteRef.removeEventListener(listenerConvites);
            listenerConvites = null;
        }

        mCurrentPosition = -1;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_communities);
        inicializarComponentes();
        if (idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar as comunidades. Tente novamente mais tarde", getApplicationContext());
            return;
        }
        configInicial();
        configRecycler();
    }

    private void configInicial() {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewTitleToolbar.setText("Comunidades");
        minhasComunidadeDiffDAO = new ComunidadeDiffDAO(listaMinhasComunidades, adapterMyCommunities);
        comunidadesPublicasDiffDAO = new ComunidadeDiffDAO(listaComunidadesPublicas, adapterPublicCommunities);
        comunidadesSeguindoDiffDAO = new ComunidadeDiffDAO(listaComunidadesSeguindo, adapterCommunityFollowing);
    }

    private void configRecycler() {
        if (linearLayoutManagerHeader == null) {
            linearLayoutManagerHeader = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerHeader.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManagerHeader);

            if (adapterLstcHeader == null) {
                adapterLstcHeader = new AdapterLstcHeader(ListaComunidadesActivityNEW.this);
                adapterTitlePublicas = new AdapterLstcTitleHeader(Comunidade.PUBLIC_COMMUNITIES, false);
                adapterTitleSeguindo = new AdapterLstcTitleHeader(Comunidade.COMMUNITIES_FOLLOWING, false);
                adapterTitleMinhasComunidades = new AdapterLstcTitleHeader(Comunidade.MY_COMMUNITIES, false);
                adapterTitleRecomendadas = new AdapterLstcTitleHeader(Comunidade.RECOMMENDED_COMMUNITIES, false);
                adapterButtonMyCommunities = new AdapterLstcButton(Comunidade.MY_COMMUNITIES, false);
                adapterButtonPublicas = new AdapterLstcButton(Comunidade.PUBLIC_COMMUNITIES, false);
                adapterButtonSeguindo = new AdapterLstcButton(Comunidade.COMMUNITIES_FOLLOWING, false);
                adapterButtonRecomendadas = new AdapterLstcButton(Comunidade.RECOMMENDED_COMMUNITIES, false);
                adapterInvitationHeader = new AdapterLstcInvitationHeader(getApplicationContext(), false);
                adapterMyCommunities = new AdapterPreviewCommunities(getApplicationContext(),
                        listaMinhasComunidades, this, this, listaDadosMinhasComunidades,
                        getResources().getColor(R.color.my_communities), this, Comunidade.MY_COMMUNITIES);
                adapterPublicCommunities = new AdapterPreviewCommunities(getApplicationContext(),
                        listaComunidadesPublicas, this, this, listaDadosComunidadesPublicas,
                        getResources().getColor(R.color.public_communities), this, Comunidade.PUBLIC_COMMUNITIES);
                adapterCommunityFollowing = new AdapterPreviewCommunities(getApplicationContext(),
                        listaComunidadesSeguindo, this, this, listaDadosComunidadesSeguindo,
                        getResources().getColor(R.color.community_following), this, Comunidade.COMMUNITIES_FOLLOWING);
            }
            concatAdapter = new ConcatAdapter(adapterLstcHeader, adapterInvitationHeader, adapterTitleMinhasComunidades,
                    adapterMyCommunities, adapterButtonMyCommunities, adapterTitlePublicas, adapterPublicCommunities, adapterButtonPublicas,
                    adapterTitleSeguindo, adapterCommunityFollowing, adapterButtonSeguindo);
            recyclerView.setAdapter(concatAdapter);
            verificaConvites();
            minhasComunidades();
            comunidadesPublicas();
            comunidadesSeguindo();
        }
    }

    private void verificaConvites() {
        conviteRef = firebaseRef.child("convitesComunidade")
                .child(idUsuario);
        listenerConvites = conviteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapterInvitationHeader.setExisteConvite(true);
                } else {
                    adapterInvitationHeader.setExisteConvite(false);
                    adapterInvitationHeader.notifyItemRemoved(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                adapterInvitationHeader.setExisteConvite(false);
                adapterInvitationHeader.notifyItemRemoved(0);
            }
        });
    }

    private void minhasComunidades() {
        minhasComunidadesRef = firebaseRef.child("comunidades")
                .orderByChild("idSuperAdmComunidade").equalTo(idUsuario).limitToLast(3);
        minhasComunidadesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                    if (snapshot.getValue() != null) {
                        Comunidade minhaComunidade = snapshotChildren.getValue(Comunidade.class);
                        adapterTitleMinhasComunidades.setExistemComunidades(true);
                        adapterButtonMyCommunities.setExistemComunidades(true);
                        minhasComunidadeDiffDAO.adicionarComunidade(minhaComunidade);
                        Collections.sort(listaMinhasComunidades, comunidadeComparator);
                        adapterMyCommunities.updateComunidadeList(listaMinhasComunidades, new AdapterPreviewCommunities.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {
                                listaDadosMinhasComunidades.put(minhaComunidade.getIdComunidade(), minhaComunidade);
                                int posicao = adapterMyCommunities.findPositionInList(minhaComunidade.getIdComunidade());
                                if (posicao != -1) {
                                    adapterMyCommunities.notifyItemChanged(adapterMyCommunities.findPositionInList(minhaComunidade.getIdComunidade()));
                                }
                            }
                        });
                    } else {
                        adapterTitleMinhasComunidades.setExistemComunidades(false);
                        adapterTitleMinhasComunidades.notifyItemRemoved(0);
                        adapterButtonMyCommunities.setExistemComunidades(false);
                        adapterButtonMyCommunities.notifyItemRemoved(0);
                    }
                }
                minhasComunidadesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar as suas comunidades. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void comunidadesPublicas() {
        comunidadesPublicasRef = firebaseRef.child("comunidades")
                .orderByChild("comunidadePublica").equalTo(true).limitToLast(3);
        comunidadesPublicasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChild : snapshot.getChildren()) {
                        Comunidade comunidadePublica = snapshotChild.getValue(Comunidade.class);
                        verificaBlock(comunidadePublica, new VerificaBlockCallback() {
                            @Override
                            public void onAjustado(Comunidade comunidadeAjustada) {
                                adapterTitlePublicas.setExistemComunidades(true);
                                adapterButtonPublicas.setExistemComunidades(true);
                                comunidadesPublicasDiffDAO.adicionarComunidade(comunidadeAjustada);
                                Collections.sort(listaComunidadesPublicas, comunidadeComparator);
                                adapterPublicCommunities.updateComunidadeList(listaComunidadesPublicas, new AdapterPreviewCommunities.ListaAtualizadaCallback() {
                                    @Override
                                    public void onAtualizado() {
                                        listaDadosComunidadesPublicas.put(comunidadeAjustada.getIdComunidade(), comunidadeAjustada);
                                        int posicao = adapterPublicCommunities.findPositionInList(comunidadeAjustada.getIdComunidade());
                                        if (posicao != -1) {
                                            adapterPublicCommunities.notifyItemChanged(adapterPublicCommunities.findPositionInList(comunidadeAjustada.getIdComunidade()));
                                        }
                                    }
                                });
                            }
                        });
                    }
                } else {
                    adapterTitlePublicas.setExistemComunidades(false);
                    adapterTitlePublicas.notifyItemRemoved(0);
                    adapterButtonPublicas.setExistemComunidades(false);
                    adapterButtonPublicas.notifyItemRemoved(0);
                }
                comunidadesPublicasRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar as comunidades públicas. Tente novamente", getApplicationContext());
            }
        });
    }

    private void comunidadesSeguindo() {
        comunidadesSeguindoRef = firebaseRef.child("communityFollowing")
                .child(idUsuario).limitToLast(3);
        comunidadesSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChild : snapshot.getChildren()) {
                        Comunidade comunidadeIncompleta = snapshotChild.getValue(Comunidade.class);
                        recuperarDadosComunidade(comunidadeIncompleta.getIdComunidade());
                    }
                } else {
                    adapterTitleSeguindo.setExistemComunidades(false);
                    adapterTitleSeguindo.notifyItemRemoved(0);
                    adapterButtonSeguindo.setExistemComunidades(false);
                    adapterButtonSeguindo.notifyItemRemoved(0);
                }
                comunidadesSeguindoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar as comunidades que você segue. Tente novamente", getApplicationContext());
            }
        });
    }

    private void recuperarDadosComunidade(String idComunidade) {
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                verificaBlock(comunidadeAtual, new VerificaBlockCallback() {
                    @Override
                    public void onAjustado(Comunidade comunidadeAjustada) {
                        adapterTitleSeguindo.setExistemComunidades(true);
                        adapterButtonSeguindo.setExistemComunidades(true);
                        comunidadesSeguindoDiffDAO.adicionarComunidade(comunidadeAjustada);
                        Collections.sort(listaComunidadesSeguindo, comunidadeComparator);
                        adapterCommunityFollowing.updateComunidadeList(listaComunidadesSeguindo, new AdapterPreviewCommunities.ListaAtualizadaCallback() {
                            @Override
                            public void onAtualizado() {
                                listaDadosComunidadesSeguindo.put(comunidadeAjustada.getIdComunidade(), comunidadeAjustada);
                                int posicao = adapterCommunityFollowing.findPositionInList(comunidadeAjustada.getIdComunidade());
                                if (posicao != -1) {
                                    adapterCommunityFollowing.notifyItemChanged(adapterCommunityFollowing.findPositionInList(comunidadeAjustada.getIdComunidade()));
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onNaoExiste() {
                //Não sei se essa é a melhor abordagem, verificar.
                HashMap<String, Object> operacaoRemoverSeguindo = new HashMap<>();
                operacaoRemoverSeguindo.put("/communityFollowing/" + idUsuario + "/" + idComunidade, null);
                firebaseRef.updateChildren(operacaoRemoverSeguindo);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void verificaBlock(Comunidade comunidadeAlvo, VerificaBlockCallback callback) {
        UsuarioUtils.checkBlockingStatus(getApplicationContext(), comunidadeAlvo.getIdSuperAdmComunidade(), new UsuarioUtils.CheckLockCallback() {
            @Override
            public void onBlocked(boolean status) {
                comunidadeAlvo.setIndisponivel(status);
                callback.onAjustado(comunidadeAlvo);
            }

            @Override
            public void onError(String message) {
                comunidadeAlvo.setIndisponivel(true);
                callback.onAjustado(comunidadeAlvo);
            }
        });
    }

    private void inicializarComponentes() {
        recyclerView = findViewById(R.id.recyclerViewListCommunities);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
    }

    @Override
    public void onRemocao(Comunidade comunidadeAlvo, int position, String tipoComunidade) {
        switch (tipoComunidade) {
            case Comunidade.MY_COMMUNITIES:
                logicaRemocao(comunidadeAlvo, listaDadosMinhasComunidades, tipoComunidade, listaMinhasComunidades);
                break;
            case Comunidade.PUBLIC_COMMUNITIES:
                logicaRemocao(comunidadeAlvo, listaDadosComunidadesPublicas, tipoComunidade, listaComunidadesPublicas);
                break;
            case Comunidade.COMMUNITIES_FOLLOWING:
                logicaRemocao(comunidadeAlvo, listaDadosComunidadesSeguindo, tipoComunidade, listaComunidadesSeguindo);
                break;
        }
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onExecutarAnimacao() {

    }

    private void logicaRemocao(Comunidade comunidadeAlvo, HashMap<String, Object> hashMapAlvo, String tipoComunidade, List<Comunidade> listaComunidadeAlvo) {
        if (hashMapAlvo != null && hashMapAlvo.size() > 0
                && hashMapAlvo.containsKey(comunidadeAlvo.getIdComunidade())) {
            hashMapAlvo.remove(comunidadeAlvo.getIdComunidade());
            int posicao = -1;
            switch (tipoComunidade) {
                case Comunidade.MY_COMMUNITIES:
                    posicao = adapterMyCommunities.findPositionInList(comunidadeAlvo.getIdComunidade());
                    if (posicao != -1) {
                        adapterMyCommunities.notifyItemChanged(adapterMyCommunities.findPositionInList(comunidadeAlvo.getIdComunidade()));
                    }
                    if (minhasComunidadeDiffDAO != null && listaComunidadeAlvo != null
                            && listaComunidadeAlvo.size() > 0) {
                        minhasComunidadeDiffDAO.removerComunidade(comunidadeAlvo);
                    }
                    adapterMyCommunities.updateComunidadeList(listaComunidadeAlvo, new AdapterPreviewCommunities.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                    break;
                case Comunidade.PUBLIC_COMMUNITIES:
                    posicao = adapterPublicCommunities.findPositionInList(comunidadeAlvo.getIdComunidade());
                    if (posicao != -1) {
                        adapterPublicCommunities.notifyItemChanged(adapterPublicCommunities.findPositionInList(comunidadeAlvo.getIdComunidade()));
                    }
                    if (comunidadesPublicasDiffDAO != null && listaComunidadeAlvo != null
                            && listaComunidadeAlvo.size() > 0) {
                        comunidadesPublicasDiffDAO.removerComunidade(comunidadeAlvo);
                    }
                    adapterPublicCommunities.updateComunidadeList(listaComunidadeAlvo, new AdapterPreviewCommunities.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                    break;
                case Comunidade.COMMUNITIES_FOLLOWING:
                    posicao = adapterCommunityFollowing.findPositionInList(comunidadeAlvo.getIdComunidade());
                    if (posicao != -1) {
                        adapterCommunityFollowing.notifyItemChanged(adapterCommunityFollowing.findPositionInList(comunidadeAlvo.getIdComunidade()));
                    }
                    if (comunidadesSeguindoDiffDAO != null && listaComunidadeAlvo != null
                            && listaComunidadeAlvo.size() > 0) {
                        comunidadesSeguindoDiffDAO.removerComunidade(comunidadeAlvo);
                    }
                    adapterCommunityFollowing.updateComunidadeList(listaComunidadeAlvo, new AdapterPreviewCommunities.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                    break;
            }
        }
    }
}