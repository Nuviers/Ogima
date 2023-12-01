package com.example.ogima.activity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterLstcButton;
import com.example.ogima.adapter.AdapterLstcHeader;
import com.example.ogima.adapter.AdapterLstcInvitationHeader;
import com.example.ogima.adapter.AdapterLstcTitleHeader;
import com.example.ogima.adapter.AdapterPreviewCommunity;
import com.example.ogima.helper.ComunidadeDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
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

public class ListaComunidadesActivityNEW extends AppCompatActivity implements AdapterPreviewCommunity.AnimacaoIntent, AdapterPreviewCommunity.RemoverComunidadeListener, AdapterPreviewCommunity.RecuperaPosicaoAnterior {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManagerHeader;
    private AdapterLstcHeader adapterLstcHeader;
    private AdapterLstcTitleHeader adapterTitlePublicas, adapterTitleSeguindo,
            adapterTitleMinhasComunidades, adapterTitleRecomendadas;
    private AdapterLstcButton adapterButtonMyCommunity,
            adapterButtonPublicas, adapterButtonSeguindo, adapterButtonRecomendadas;
    private AdapterLstcInvitationHeader adapterInvitationHeader;
    private ConcatAdapter concatAdapter;
    private ValueEventListener listenerConvites;
    private DatabaseReference conviteRef;
    private String idUsuario = "";
    private AdapterPreviewCommunity adapterMyCommunity, adapterPublicCommunity,
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

    private HashMap<String, Query> referenceHashMapMy = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapMy = new HashMap<>();
    private HashMap<String, Query> referenceHashMapPublic = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapPublic = new HashMap<>();

    private ChildEventListener newChildListenerPublic, newChildListenerMy;

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

        removeValueEventListenerMy();
        removeValueEventListenerPublic();
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
        setContentView(R.layout.activity_listing_community);
        inicializarComponentes();
        ToastCustomizado.toastCustomizado("CREATE", getApplicationContext());
        if (idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar as comunidades. Tente novamente mais tarde", getApplicationContext());
            return;
        }
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean epilepsia) {
                configInicial();
                configRecycler(epilepsia);
            }

            @Override
            public void onSemDado() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void configInicial() {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewTitleToolbar.setText("Comunidades");
        minhasComunidadeDiffDAO = new ComunidadeDiffDAO(listaMinhasComunidades, adapterMyCommunity);
        comunidadesPublicasDiffDAO = new ComunidadeDiffDAO(listaComunidadesPublicas, adapterPublicCommunity);
        comunidadesSeguindoDiffDAO = new ComunidadeDiffDAO(listaComunidadesSeguindo, adapterCommunityFollowing);
    }

    private void configRecycler(boolean statusEpilepsia) {
        if (linearLayoutManagerHeader == null) {
            linearLayoutManagerHeader = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerHeader.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManagerHeader);

            if (adapterLstcHeader == null) {
                adapterLstcHeader = new AdapterLstcHeader(ListaComunidadesActivityNEW.this);
                adapterTitlePublicas = new AdapterLstcTitleHeader(Comunidade.PUBLIC_COMMUNITY, false);
                adapterTitleSeguindo = new AdapterLstcTitleHeader(Comunidade.COMMUNITY_FOLLOWING, false);
                adapterTitleMinhasComunidades = new AdapterLstcTitleHeader(Comunidade.MY_COMMUNITY, false);
                adapterTitleRecomendadas = new AdapterLstcTitleHeader(Comunidade.RECOMMENDED_COMMUNITY, false);
                adapterButtonMyCommunity = new AdapterLstcButton(Comunidade.MY_COMMUNITY, false);
                adapterButtonPublicas = new AdapterLstcButton(Comunidade.PUBLIC_COMMUNITY, false);
                adapterButtonSeguindo = new AdapterLstcButton(Comunidade.COMMUNITY_FOLLOWING, false);
                adapterButtonRecomendadas = new AdapterLstcButton(Comunidade.RECOMMENDED_COMMUNITY, false);
                adapterInvitationHeader = new AdapterLstcInvitationHeader(getApplicationContext(), false);
                adapterMyCommunity = new AdapterPreviewCommunity(getApplicationContext(),
                        listaMinhasComunidades, this, this, listaDadosMinhasComunidades,
                        getResources().getColor(R.color.my_community), this, Comunidade.MY_COMMUNITY);
                adapterPublicCommunity = new AdapterPreviewCommunity(getApplicationContext(),
                        listaComunidadesPublicas, this, this, listaDadosComunidadesPublicas,
                        getResources().getColor(R.color.public_community), this, Comunidade.PUBLIC_COMMUNITY);
                adapterCommunityFollowing = new AdapterPreviewCommunity(getApplicationContext(),
                        listaComunidadesSeguindo, this, this, listaDadosComunidadesSeguindo,
                        getResources().getColor(R.color.community_following), this, Comunidade.COMMUNITY_FOLLOWING);
            }
            concatAdapter = new ConcatAdapter(adapterLstcHeader, adapterInvitationHeader, adapterTitleMinhasComunidades,
                    adapterMyCommunity, adapterButtonMyCommunity, adapterTitlePublicas, adapterPublicCommunity, adapterButtonPublicas,
                    adapterTitleSeguindo, adapterCommunityFollowing, adapterButtonSeguindo);
            recyclerView.setAdapter(concatAdapter);
            adapterMyCommunity.setStatusEpilepsia(statusEpilepsia);
            adapterPublicCommunity.setStatusEpilepsia(statusEpilepsia);
            adapterCommunityFollowing.setStatusEpilepsia(statusEpilepsia);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                adapterInvitationHeader.setExisteConvite(false);
            }
        });
    }

    private void minhasComunidades() {
        minhasComunidadesRef = firebaseRef.child("comunidades")
                .orderByChild("idSuperAdmComunidade").equalTo(idUsuario).limitToLast(3);

        if (newChildListenerMy != null) {
            newChildListenerMy = null;
        }

        newChildListenerMy = minhasComunidadesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Comunidade minhaComunidade = snapshot.getValue(Comunidade.class);
                    adapterTitleMinhasComunidades.setExistemComunidades(true);
                    adapterButtonMyCommunity.setExistemComunidades(true);
                    minhasComunidadeDiffDAO.adicionarComunidade(minhaComunidade);
                    Collections.sort(listaMinhasComunidades, comunidadeComparator);
                    adapterMyCommunity.updateComunidadeList(listaMinhasComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            listaDadosMinhasComunidades.put(minhaComunidade.getIdComunidade(), minhaComunidade);
                            int posicao = adapterMyCommunity.findPositionInList(minhaComunidade.getIdComunidade());
                            if (posicao != -1) {
                                adapterMyCommunity.notifyItemChanged(adapterMyCommunity.findPositionInList(minhaComunidade.getIdComunidade()));
                            }
                        }
                    });
                    addListener(minhaComunidade.getIdComunidade(), minhasComunidadesRef, newChildListenerMy, "my");
                } else {
                    adapterTitleMinhasComunidades.setExistemComunidades(false);
                    adapterButtonMyCommunity.setExistemComunidades(false);
                    adapterButtonMyCommunity.notifyItemRemoved(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    // Recupera o comunidade do snapshot
                    Comunidade comunidadeAtualizada = snapshot.getValue(Comunidade.class);

                    // Atualiza o comunidade na lista mantendo a ordenação
                    minhasComunidadeDiffDAO.atualizarComunidade(comunidadeAtualizada);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterMyCommunity.updateComunidadeList(listaMinhasComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            listaDadosMinhasComunidades.put(comunidadeAtualizada.getIdComunidade(), comunidadeAtualizada);
                            int posicao = adapterMyCommunity.findPositionInList(comunidadeAtualizada.getIdComunidade());
                            if (posicao != -1) {
                                adapterMyCommunity.notifyItemChanged(adapterMyCommunity.findPositionInList(comunidadeAtualizada.getIdComunidade()));
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Comunidade comunidadeRemovida = snapshot.getValue(Comunidade.class);
                    if (minhasComunidadeDiffDAO != null && listaMinhasComunidades != null
                            && listaMinhasComunidades.size() > 0) {
                        minhasComunidadeDiffDAO.removerComunidade(comunidadeRemovida);
                    }
                    if (listaDadosMinhasComunidades != null && listaDadosMinhasComunidades.size() > 0
                            && listaDadosMinhasComunidades.containsKey(comunidadeRemovida.getIdComunidade())) {
                        listaDadosMinhasComunidades.remove(comunidadeRemovida.getIdComunidade());
                        adapterMyCommunity.notifyItemChanged(adapterMyCommunity.findPositionInList(comunidadeRemovida.getIdComunidade()));
                    }
                    if (listaMinhasComunidades != null && listaMinhasComunidades.size() > 0) {
                        Collections.sort(listaMinhasComunidades, comunidadeComparator);
                    }
                    adapterMyCommunity.updateComunidadeList(listaMinhasComunidades, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

        if (newChildListenerPublic != null) {
            newChildListenerPublic = null;
        }

        newChildListenerPublic = comunidadesPublicasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Comunidade comunidadePublica = snapshot.getValue(Comunidade.class);
                    verificaBlock(comunidadePublica, new VerificaBlockCallback() {
                        @Override
                        public void onAjustado(Comunidade comunidadeAjustada) {
                            adapterTitlePublicas.setExistemComunidades(true);
                            adapterButtonPublicas.setExistemComunidades(true);
                            comunidadesPublicasDiffDAO.adicionarComunidade(comunidadeAjustada);
                            Collections.sort(listaComunidadesPublicas, comunidadeComparator);
                            adapterPublicCommunity.updateComunidadeList(listaComunidadesPublicas, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {
                                    listaDadosComunidadesPublicas.put(comunidadeAjustada.getIdComunidade(), comunidadeAjustada);
                                    int posicao = adapterPublicCommunity.findPositionInList(comunidadeAjustada.getIdComunidade());
                                    if (posicao != -1) {
                                        adapterPublicCommunity.notifyItemChanged(adapterPublicCommunity.findPositionInList(comunidadeAjustada.getIdComunidade()));
                                    }
                                }
                            });
                            addListener(comunidadePublica.getIdComunidade(), comunidadesPublicasRef, newChildListenerPublic, "public");
                        }
                    });
                } else {
                    adapterTitlePublicas.setExistemComunidades(false);
                    adapterButtonPublicas.setExistemComunidades(false);
                    adapterButtonPublicas.notifyItemRemoved(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    // Recupera o comunidade do snapshot
                    Comunidade comunidadeAtualizada = snapshot.getValue(Comunidade.class);

                    // Atualiza o comunidade na lista mantendo a ordenação
                    comunidadesPublicasDiffDAO.atualizarComunidade(comunidadeAtualizada);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterPublicCommunity.updateComunidadeList(listaComunidadesPublicas, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            listaDadosComunidadesPublicas.put(comunidadeAtualizada.getIdComunidade(), comunidadeAtualizada);
                            int posicao = adapterPublicCommunity.findPositionInList(comunidadeAtualizada.getIdComunidade());
                            if (posicao != -1) {
                                adapterPublicCommunity.notifyItemChanged(adapterPublicCommunity.findPositionInList(comunidadeAtualizada.getIdComunidade()));
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Comunidade comunidadeRemovida = snapshot.getValue(Comunidade.class);
                    if (comunidadesPublicasDiffDAO != null && listaComunidadesPublicas != null
                            && listaComunidadesPublicas.size() > 0) {
                        comunidadesPublicasDiffDAO.removerComunidade(comunidadeRemovida);
                    }
                    if (listaDadosComunidadesPublicas != null && listaDadosComunidadesPublicas.size() > 0
                            && listaDadosComunidadesPublicas.containsKey(comunidadeRemovida.getIdComunidade())) {
                        listaDadosComunidadesPublicas.remove(comunidadeRemovida.getIdComunidade());
                        adapterPublicCommunity.notifyItemChanged(adapterPublicCommunity.findPositionInList(comunidadeRemovida.getIdComunidade()));
                    }
                    if (listaComunidadesPublicas != null && listaComunidadesPublicas.size() > 0) {
                        Collections.sort(listaComunidadesPublicas, comunidadeComparator);
                    }
                    adapterPublicCommunity.updateComunidadeList(listaComunidadesPublicas, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
                        adapterCommunityFollowing.updateComunidadeList(listaComunidadesSeguindo, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
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
        recyclerView = findViewById(R.id.recyclerViewListCommunity);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
    }

    @Override
    public void onRemocao(Comunidade comunidadeAlvo, int position, String tipoComunidade) {
        switch (tipoComunidade) {
            case Comunidade.MY_COMMUNITY:
                logicaRemocao(comunidadeAlvo, listaDadosMinhasComunidades, tipoComunidade, listaMinhasComunidades);
                break;
            case Comunidade.PUBLIC_COMMUNITY:
                logicaRemocao(comunidadeAlvo, listaDadosComunidadesPublicas, tipoComunidade, listaComunidadesPublicas);
                break;
            case Comunidade.COMMUNITY_FOLLOWING:
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
                case Comunidade.MY_COMMUNITY:
                    posicao = adapterMyCommunity.findPositionInList(comunidadeAlvo.getIdComunidade());
                    if (posicao != -1) {
                        adapterMyCommunity.notifyItemChanged(adapterMyCommunity.findPositionInList(comunidadeAlvo.getIdComunidade()));
                    }
                    if (minhasComunidadeDiffDAO != null && listaComunidadeAlvo != null
                            && listaComunidadeAlvo.size() > 0) {
                        minhasComunidadeDiffDAO.removerComunidade(comunidadeAlvo);
                    }
                    adapterMyCommunity.updateComunidadeList(listaComunidadeAlvo, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                    break;
                case Comunidade.PUBLIC_COMMUNITY:
                    posicao = adapterPublicCommunity.findPositionInList(comunidadeAlvo.getIdComunidade());
                    if (posicao != -1) {
                        adapterPublicCommunity.notifyItemChanged(adapterPublicCommunity.findPositionInList(comunidadeAlvo.getIdComunidade()));
                    }
                    if (comunidadesPublicasDiffDAO != null && listaComunidadeAlvo != null
                            && listaComunidadeAlvo.size() > 0) {
                        comunidadesPublicasDiffDAO.removerComunidade(comunidadeAlvo);
                    }
                    adapterPublicCommunity.updateComunidadeList(listaComunidadeAlvo, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                    break;
                case Comunidade.COMMUNITY_FOLLOWING:
                    posicao = adapterCommunityFollowing.findPositionInList(comunidadeAlvo.getIdComunidade());
                    if (posicao != -1) {
                        adapterCommunityFollowing.notifyItemChanged(adapterCommunityFollowing.findPositionInList(comunidadeAlvo.getIdComunidade()));
                    }
                    if (comunidadesSeguindoDiffDAO != null && listaComunidadeAlvo != null
                            && listaComunidadeAlvo.size() > 0) {
                        comunidadesSeguindoDiffDAO.removerComunidade(comunidadeAlvo);
                    }
                    adapterCommunityFollowing.updateComunidadeList(listaComunidadeAlvo, new AdapterPreviewCommunity.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {

                        }
                    });
                    break;
            }
        }
    }

    public void addListener(String idAlvo, Query referenceAlvo, ChildEventListener childListenerAlvo, String tipoComunidade) {
        if (tipoComunidade.equals("my")) {
            if (referenceHashMapMy != null && referenceHashMapMy.size() > 0
                    && referenceHashMapMy.containsKey(idAlvo)) {
                return;
            }
            referenceHashMapMy.put(idAlvo, referenceAlvo);
            listenerHashMapMy.put(idAlvo, childListenerAlvo);
        } else if (tipoComunidade.equals("public")) {
            if (referenceHashMapPublic != null && referenceHashMapPublic.size() > 0
                    && referenceHashMapPublic.containsKey(idAlvo)) {
                return;
            }
            referenceHashMapPublic.put(idAlvo, referenceAlvo);
            listenerHashMapPublic.put(idAlvo, childListenerAlvo);
        }
    }

    public void removeValueEventListenerMy() {
        if (listenerHashMapMy != null && referenceHashMapMy != null) {
            for (String userId : listenerHashMapMy.keySet()) {
                Query userRef = referenceHashMapMy.get(userId);
                ChildEventListener listener = listenerHashMapMy.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                }
            }
            referenceHashMapMy.clear();
            listenerHashMapMy.clear();
        }
    }

    public void removeValueEventListenerPublic() {
        if (listenerHashMapPublic != null && referenceHashMapPublic != null) {
            for (String userId : listenerHashMapPublic.keySet()) {
                Query userRef = referenceHashMapPublic.get(userId);
                ChildEventListener listener = listenerHashMapPublic.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                }
            }
            referenceHashMapPublic.clear();
            listenerHashMapPublic.clear();
        }
    }
}