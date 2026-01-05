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
import com.example.ogima.adapter.AdapterLstGrupoButton;
import com.example.ogima.adapter.AdapterLstGrupoHeader;
import com.example.ogima.adapter.AdapterLstGrupoTitleHeader;
import com.example.ogima.adapter.AdapterLstcButton;
import com.example.ogima.adapter.AdapterLstcHeader;
import com.example.ogima.adapter.AdapterLstcInvitationHeader;
import com.example.ogima.adapter.AdapterLstcTitleHeader;
import com.example.ogima.adapter.AdapterPreviewCommunity;
import com.example.ogima.adapter.AdapterPreviewGroup;
import com.example.ogima.adapter.AdapterTesteFirebaseUi;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ComunidadeDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.GrupoDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


public class ListagemGruposActivity extends AppCompatActivity implements AdapterPreviewGroup.AnimacaoIntent, AdapterPreviewGroup.RemoverGrupoListener, AdapterPreviewGroup.RecuperaPosicaoAnterior, AdapterLstGrupoButton.AnimacaoIntent {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManagerHeader;
    private AdapterLstGrupoHeader adapterLstHeader;
    private AdapterLstGrupoTitleHeader adapterTitleSeguindo,
            adapterTitleMeuGrupo, adapterTitleBloqueado, adapterTitleTodos;
    private AdapterLstGrupoButton adapterButtonMeuGrupo,
            adapterButtonSeguindo, adapterButtonBloqueado, adapterButtonTodos;
    private ConcatAdapter concatAdapter;
    private AdapterPreviewGroup adapterMeuGrupo, adapterGrupoBloqueado,
            adapterGrupoSeguindo;
    private Query meusGruposRef, gruposBloqueadosRef, gruposSeguindoRef,
            todosGruposRef;
    private List<Grupo> listaMeusGrupos = new ArrayList<>(), listaGruposBloqueados = new ArrayList<>(),
            listaGruposSeguindo = new ArrayList<>();
    private GrupoDiffDAO meusGruposDiffDAO, gruposBloqueadosDiffDAO,
            gruposSeguindoDiffDAO;
    private HashMap<String, Object> listaDadosMeusGrupos = new HashMap<>(),
            listaDadosGruposBloqueados = new HashMap<>(),
            listaDadosGruposSeguindo = new HashMap<>();
    private int mCurrentPosition = -1;
    private Grupo grupoComparator;
    private HashMap<String, Query> referenceHashMapMy = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapMy = new HashMap<>();
    private HashMap<String, Query> referenceHashMapBlocked = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapBlocked = new HashMap<>();
    private HashMap<String, Query> referenceHashMapFollowing = new HashMap<>();
    private HashMap<String, ChildEventListener> listenerHashMapFollowing = new HashMap<>();
    private ChildEventListener newChildListenerBlocked, newChildListenerMy, newChildListenerFollowing;
    private ValueEventListener listenerTodosGrupos;
    private FirebaseUtils firebaseUtils;
    private int contadorRemocaoMy = 0, contadorRemocaoBlocked = 0, contadorRemocaoFollowing = 0;


    public ListagemGruposActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        grupoComparator = new Grupo(false, true);
    }

    public interface VerificaBlockCallback {
        void onAjustado(Grupo grupoAjustado);
    }

    private interface RecuperaGrupoCallback {
        void onRecuperado(Grupo grupoAtual);

        void onSemDado();

        void onError(String message);
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
                    recyclerView.scrollToPosition(mCurrentPosition);
                }
            }, 100);
        }
        mCurrentPosition = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeValueEventListenerMy();
        removeValueEventListenerBlocked();
        removeValueEventListenerFollowing();
        firebaseUtils.removerQueryValueListener(todosGruposRef, listenerTodosGrupos);
        mCurrentPosition = -1;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPosition = savedInstanceState.getInt("current_position");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_community);
        inicializandoComponentes();
        firebaseUtils = new FirebaseUtils();
        if (idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
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
                ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizado(String.format("%s%s %s", getString(R.string.error_retrieving_user_data), ":", message), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void configInicial() {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewTitleToolbar.setText("Grupos/Chat de comunidade");
        meusGruposDiffDAO = new GrupoDiffDAO(listaMeusGrupos, adapterMeuGrupo);
        gruposBloqueadosDiffDAO = new GrupoDiffDAO(listaGruposBloqueados, adapterGrupoBloqueado);
        gruposSeguindoDiffDAO = new GrupoDiffDAO(listaGruposSeguindo, adapterGrupoSeguindo);
    }

    private void configRecycler(boolean statusEpilepsia) {
        if (linearLayoutManagerHeader == null) {
            linearLayoutManagerHeader = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerHeader.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManagerHeader);

            if (adapterLstHeader == null) {
                adapterLstHeader = new AdapterLstGrupoHeader(ListagemGruposActivity.this);
                adapterTitleBloqueado = new AdapterLstGrupoTitleHeader(Grupo.BLOCKED_GROUP, false, getApplicationContext());
                adapterTitleSeguindo = new AdapterLstGrupoTitleHeader(Grupo.GROUP_FOLLOWING, false, getApplicationContext());
                adapterTitleMeuGrupo = new AdapterLstGrupoTitleHeader(Grupo.MY_GROUP, false, getApplicationContext());
                adapterTitleTodos = new AdapterLstGrupoTitleHeader(Grupo.PUBLIC_GROUP, false, getApplicationContext());
                adapterButtonMeuGrupo = new AdapterLstGrupoButton(Grupo.MY_GROUP, false, getApplicationContext(), this);
                adapterButtonBloqueado = new AdapterLstGrupoButton(Grupo.BLOCKED_GROUP, false, getApplicationContext(), this);
                adapterButtonSeguindo = new AdapterLstGrupoButton(Grupo.GROUP_FOLLOWING, false, getApplicationContext(), this);
                adapterButtonTodos = new AdapterLstGrupoButton(Grupo.PUBLIC_GROUP, false, getApplicationContext(), this);
                adapterMeuGrupo = new AdapterPreviewGroup(getApplicationContext(),
                        listaMeusGrupos, this, this, listaDadosMeusGrupos,
                        getResources().getColor(R.color.my_community), this, Grupo.MY_GROUP);
                adapterGrupoBloqueado = new AdapterPreviewGroup(getApplicationContext(),
                        listaGruposBloqueados, this, this, listaDadosGruposBloqueados,
                        getResources().getColor(R.color.group_blocked), this, Grupo.BLOCKED_GROUP);
                adapterGrupoSeguindo = new AdapterPreviewGroup(getApplicationContext(),
                        listaGruposSeguindo, this, this, listaDadosGruposSeguindo,
                        getResources().getColor(R.color.community_following), this, Grupo.GROUP_FOLLOWING);
            }
            concatAdapter = new ConcatAdapter(adapterLstHeader, adapterTitleMeuGrupo,
                    adapterMeuGrupo, adapterButtonMeuGrupo, adapterTitleBloqueado, adapterGrupoBloqueado, adapterButtonBloqueado,
                    adapterTitleSeguindo, adapterGrupoSeguindo, adapterButtonSeguindo, adapterTitleTodos, adapterButtonTodos);
            recyclerView.setAdapter(concatAdapter);
            adapterMeuGrupo.setStatusEpilepsia(statusEpilepsia);
            adapterGrupoBloqueado.setStatusEpilepsia(statusEpilepsia);
            adapterGrupoSeguindo.setStatusEpilepsia(statusEpilepsia);
            meusGrupos();
            gruposBloqueados();
            gruposSeguindo();
            verificaSeExisteGrupos();
        }
    }

    private void meusGrupos() {
        //Oculta os componentes relacionado a esse tipo de dados antes da consulta no
        //banco de dados.
        adapterTitleMeuGrupo.setExistemGrupos(false);
        adapterButtonMeuGrupo.setExistemGrupos(false);
        adapterButtonMeuGrupo.notifyItemRemoved(0);

        meusGruposRef = firebaseRef.child("grupos")
                .orderByChild("idSuperAdmGrupo").equalTo(idUsuario).limitToLast(3);

        newChildListenerMy = meusGruposRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                if (snapshot.getValue() != null) {
                    Grupo meuGrupo = snapshot.getValue(Grupo.class);
                    if (meuGrupo == null ||
                            meuGrupo.getIdGrupo() == null || meuGrupo.getIdGrupo().isEmpty()) {
                        return;
                    }
                    adapterTitleMeuGrupo.setExistemGrupos(true);
                    adapterButtonMeuGrupo.setExistemGrupos(true);
                    meusGruposDiffDAO.adicionarGrupo(meuGrupo);
                    Collections.sort(listaMeusGrupos, grupoComparator);
                    adapterMeuGrupo.updateGrupoList(listaMeusGrupos, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            listaDadosMeusGrupos.put(meuGrupo.getIdGrupo(), meuGrupo);
                            int posicao = adapterMeuGrupo.findPositionInList(meuGrupo.getIdGrupo());
                            if (posicao != -1) {
                                adapterMeuGrupo.notifyItemChanged(adapterMeuGrupo.findPositionInList(meuGrupo.getIdGrupo()));
                            }
                        }
                    });
                    addListener(meuGrupo.getIdGrupo(), meusGruposRef, newChildListenerMy, "my");
                } else {
                    adapterTitleMeuGrupo.setExistemGrupos(false);
                    adapterButtonMeuGrupo.setExistemGrupos(false);
                    adapterButtonMeuGrupo.notifyItemRemoved(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {
                if (snapshot.getValue() != null) {
                    logicaAtualizacao("my", snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                    logicaRemocao(grupoRemovido, listaDadosMeusGrupos, Grupo.MY_GROUP, listaMeusGrupos);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado(String.format("%s%s %s", getString(R.string.error_recovering_my_groups), ":", error.getCode()), getApplicationContext());
            }
        });
    }

    private void gruposBloqueados() {
        adapterTitleBloqueado.setExistemGrupos(false);
        adapterTitleBloqueado.setExistemGrupos(false);
        adapterTitleBloqueado.notifyItemRemoved(0);

        gruposBloqueadosRef = firebaseRef.child("blockGroup")
                .child(idUsuario)
                .orderByChild("idGrupo")
                .limitToLast(3);
        newChildListenerBlocked = gruposBloqueadosRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                if (snapshot.getValue() != null) {
                    Grupo grupoBloqueado = snapshot.getValue(Grupo.class);
                    if (grupoBloqueado == null ||
                            grupoBloqueado.getIdGrupo() == null || grupoBloqueado.getIdGrupo().isEmpty()) {
                        return;
                    }
                    adapterTitleBloqueado.setExistemGrupos(true);
                    adapterButtonBloqueado.setExistemGrupos(true);
                    adicionarGrupo(grupoBloqueado, "blocked");
                    addListener(grupoBloqueado.getIdGrupo(), gruposBloqueadosRef, newChildListenerBlocked, "blocked");
                } else {
                    adapterTitleBloqueado.setExistemGrupos(false);
                    adapterButtonBloqueado.setExistemGrupos(false);
                    adapterButtonBloqueado.notifyItemRemoved(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {
                if (snapshot.getValue() != null) {
                    logicaAtualizacao("blocked", snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                    logicaRemocao(grupoRemovido, listaDadosGruposBloqueados, Grupo.BLOCKED_GROUP, listaGruposBloqueados);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado(String.format("%s%s %s", getString(R.string.error_recovering_my_groups), ":", error.getCode()), getApplicationContext());
            }
        });
    }

    private void gruposSeguindo() {
        adapterTitleSeguindo.setExistemGrupos(false);
        adapterButtonSeguindo.setExistemGrupos(false);
        adapterButtonSeguindo.notifyItemRemoved(0);

        gruposSeguindoRef = firebaseRef.child("groupFollowing")
                .child(idUsuario)
                .orderByChild("idGrupo")
                .limitToLast(3);
        newChildListenerFollowing = gruposSeguindoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                if (snapshot.getValue() != null) {
                    Grupo grupoSeguido = snapshot.getValue(Grupo.class);
                    if (grupoSeguido == null ||
                            grupoSeguido.getIdGrupo() == null || grupoSeguido.getIdGrupo().isEmpty()) {
                        return;
                    }
                    adapterTitleSeguindo.setExistemGrupos(true);
                    adapterButtonSeguindo.setExistemGrupos(true);
                    adicionarGrupo(grupoSeguido, "following");
                    addListener(grupoSeguido.getIdGrupo(), gruposSeguindoRef, newChildListenerFollowing, "following");
                } else {
                    adapterTitleSeguindo.setExistemGrupos(false);
                    adapterButtonSeguindo.setExistemGrupos(false);
                    adapterButtonSeguindo.notifyItemRemoved(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {
                if (snapshot.getValue() != null) {
                    logicaAtualizacao("following", snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                    logicaRemocao(grupoRemovido, listaDadosGruposSeguindo, Grupo.GROUP_FOLLOWING, listaGruposSeguindo);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado(String.format("%s%s %s", getString(R.string.error_recovering_my_groups), ":", error.getCode()), getApplicationContext());
            }
        });
    }

    private void verificaSeExisteGrupos() {
        todosGruposRef = firebaseRef.child("grupos")
                .orderByChild("idGrupo").limitToFirst(1);
        listenerTodosGrupos = todosGruposRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    adapterTitleTodos.setExistemGrupos(false);
                    adapterButtonTodos.setExistemGrupos(false);
                    adapterButtonTodos.notifyItemRemoved(0);
                } else {
                    adapterTitleTodos.setExistemGrupos(true);
                    adapterButtonTodos.setExistemGrupos(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addListener(String idAlvo, Query referenceAlvo, ChildEventListener childListenerAlvo, String tipoComunidade) {
        if (tipoComunidade.equals("my")) {
            if (referenceHashMapMy == null ||
                    !referenceHashMapMy.isEmpty()
                            && referenceHashMapMy.containsKey(idAlvo)) {
                return;
            }
            referenceHashMapMy.put(idAlvo, referenceAlvo);
            listenerHashMapMy.put(idAlvo, childListenerAlvo);
        } else if (tipoComunidade.equals("blocked")) {
            if (referenceHashMapBlocked == null ||
                    !referenceHashMapBlocked.isEmpty()
                            && referenceHashMapBlocked.containsKey(idAlvo)) {
                return;
            }
            referenceHashMapBlocked.put(idAlvo, referenceAlvo);
            listenerHashMapBlocked.put(idAlvo, childListenerAlvo);
        } else if (tipoComunidade.equals("following")) {
            if (referenceHashMapFollowing == null
                    || !referenceHashMapFollowing.isEmpty()
                    && referenceHashMapFollowing.containsKey(idAlvo)) {
                return;
            }
            referenceHashMapFollowing.put(idAlvo, referenceAlvo);
            listenerHashMapFollowing.put(idAlvo, childListenerAlvo);
        }
    }

    private void logicaAtualizacao(String tipoOperacao, DataSnapshot snapshot) {
        if (snapshot.getValue() != null) {
            Grupo grupoAtualizado = snapshot.getValue(Grupo.class);
            if (grupoAtualizado == null || grupoAtualizado.getIdGrupo() == null) {
                return;
            }
            int posicaoAtualizada = -1;
            if (tipoOperacao.equals("my")) {
                posicaoAtualizada = adapterMeuGrupo.findPositionInList(grupoAtualizado.getIdGrupo());
            } else if (tipoOperacao.equals("blocked")) {
                posicaoAtualizada = adapterGrupoBloqueado.findPositionInList(grupoAtualizado.getIdGrupo());
            } else if (tipoOperacao.equals("following")) {
                posicaoAtualizada = adapterGrupoSeguindo.findPositionInList(grupoAtualizado.getIdGrupo());
            }
            if (posicaoAtualizada != -1) {
                Grupo grupoAnterior = new Grupo();
                if (tipoOperacao.equals("my")
                        && verificaRelacao(referenceHashMapMy, grupoAtualizado.getIdGrupo())) {
                    grupoAnterior = listaMeusGrupos.get(posicaoAtualizada);
                } else if (tipoOperacao.equals("blocked")
                        && verificaRelacao(referenceHashMapBlocked, grupoAtualizado.getIdGrupo())) {
                    grupoAnterior = listaGruposBloqueados.get(posicaoAtualizada);
                } else if (tipoOperacao.equals("following")
                        && verificaRelacao(referenceHashMapFollowing, grupoAtualizado.getIdGrupo())) {
                    grupoAnterior = listaGruposSeguindo.get(posicaoAtualizada);
                }
                if (!grupoAnterior.getNomeGrupo().equals(grupoAtualizado.getNomeGrupo())) {
                    atualizarPorPayload(posicaoAtualizada, grupoAtualizado, "nomeGrupo", tipoOperacao);
                }
                if (!grupoAnterior.getDescricaoGrupo().equals(grupoAtualizado.getDescricaoGrupo())) {
                    atualizarPorPayload(posicaoAtualizada, grupoAtualizado, "descricaoGrupo", tipoOperacao);
                }
                if (!grupoAnterior.getFotoGrupo().equals(grupoAtualizado.getFotoGrupo())) {
                    atualizarPorPayload(posicaoAtualizada, grupoAtualizado, "fotoGrupo", tipoOperacao);
                }
                if (!grupoAnterior.getTopicos().equals(grupoAtualizado.getTopicos())) {
                    atualizarPorPayload(posicaoAtualizada, grupoAtualizado, "topicosGrupo", tipoOperacao);
                }
                if (tipoOperacao.equals("my")
                        && verificaRelacao(referenceHashMapMy, grupoAtualizado.getIdGrupo())) {
                    atualizarLista(tipoOperacao, listaMeusGrupos);
                } else if (tipoOperacao.equals("blocked")
                        && verificaRelacao(referenceHashMapBlocked, grupoAtualizado.getIdGrupo())) {
                    atualizarLista(tipoOperacao, listaGruposBloqueados);
                } else if (tipoOperacao.equals("following")
                        && verificaRelacao(referenceHashMapFollowing, grupoAtualizado.getIdGrupo())) {
                    atualizarLista(tipoOperacao, listaGruposSeguindo);
                }
            }
        }
    }

    private void logicaRemocao(Grupo grupoAlvo, HashMap<String, Object> hashMapAlvo, String tipoGrupo, List<Grupo> listaGrupoAlvo) {
        if (verificaRelacaoDados(hashMapAlvo, grupoAlvo.getIdGrupo())) {
            hashMapAlvo.remove(grupoAlvo.getIdGrupo());
            int posicao = -1;
            switch (tipoGrupo) {
                case Grupo.MY_GROUP:
                    posicao = adapterMeuGrupo.findPositionInList(grupoAlvo.getIdGrupo());
                    if (posicao != -1) {
                        adapterMeuGrupo.notifyItemChanged(adapterMeuGrupo.findPositionInList(grupoAlvo.getIdGrupo()));
                    }
                    if (meusGruposDiffDAO != null && listaGrupoAlvo != null
                            && !listaGrupoAlvo.isEmpty()) {
                        meusGruposDiffDAO.removerGrupo(grupoAlvo);
                    }
                    adapterMeuGrupo.updateGrupoList(listaGrupoAlvo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            if(listaMeusGrupos != null && listaMeusGrupos.isEmpty()){
                                adapterTitleMeuGrupo.setExistemGrupos(false);
                                adapterButtonMeuGrupo.setExistemGrupos(false);
                                adapterButtonMeuGrupo.notifyItemRemoved(0);
                            }
                        }
                    });
                    break;
                case Grupo.BLOCKED_GROUP:
                    posicao = adapterGrupoBloqueado.findPositionInList(grupoAlvo.getIdGrupo());
                    if (posicao != -1) {
                        adapterGrupoBloqueado.notifyItemChanged(adapterGrupoBloqueado.findPositionInList(grupoAlvo.getIdGrupo()));
                    }
                    if (gruposBloqueadosDiffDAO != null && listaGrupoAlvo != null
                            && !listaGrupoAlvo.isEmpty()) {
                        gruposBloqueadosDiffDAO.removerGrupo(grupoAlvo);
                    }
                    adapterGrupoBloqueado.updateGrupoList(listaGrupoAlvo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            if(listaGruposBloqueados != null && listaGruposBloqueados.isEmpty()){
                                adapterTitleBloqueado.setExistemGrupos(false);
                                adapterButtonBloqueado.setExistemGrupos(false);
                                adapterButtonBloqueado.notifyItemRemoved(0);
                            }
                        }
                    });
                    break;
                case Grupo.GROUP_FOLLOWING:
                    posicao = adapterGrupoSeguindo.findPositionInList(grupoAlvo.getIdGrupo());
                    if (posicao != -1) {
                        adapterGrupoSeguindo.notifyItemChanged(adapterGrupoSeguindo.findPositionInList(grupoAlvo.getIdGrupo()));
                    }
                    if (gruposSeguindoDiffDAO != null && listaGrupoAlvo != null
                            && !listaGrupoAlvo.isEmpty()) {
                        gruposSeguindoDiffDAO.removerGrupo(grupoAlvo);
                    }
                    adapterGrupoSeguindo.updateGrupoList(listaGrupoAlvo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                           if(listaGruposSeguindo != null && listaDadosGruposSeguindo.isEmpty()){
                               adapterTitleSeguindo.setExistemGrupos(false);
                               adapterButtonSeguindo.setExistemGrupos(false);
                               adapterButtonSeguindo.notifyItemRemoved(0);
                           }
                        }
                    });
                    break;
            }
        }
    }

    private boolean verificaRelacao(HashMap<String, Query> hashMapAlvo, String idAlvo) {
        return hashMapAlvo != null && !hashMapAlvo.isEmpty() && hashMapAlvo.containsKey(idAlvo);
    }

    private boolean verificaRelacaoDados(HashMap<String, Object> hashMapAlvo, String idAlvo) {
        return hashMapAlvo != null && !hashMapAlvo.isEmpty() && hashMapAlvo.containsKey(idAlvo);
    }

    private void atualizarPorPayload(int posicaoAtualizada, Grupo grupoAtualizado, String tipoPayload, String tipoOperacao) {
        if (posicaoAtualizada != -1) {
            if (tipoOperacao.equals("my")
                    && verificaRelacao(referenceHashMapMy, grupoAtualizado.getIdGrupo())) {
                meusGruposDiffDAO.atualizarGrupoPorPayload(grupoAtualizado, tipoPayload, new GrupoDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        listaDadosMeusGrupos.put(grupoAtualizado.getIdGrupo(), grupoAtualizado);
                        //Fazer esse put no payload do adapter também
                        adapterMeuGrupo.notifyItemChanged(index, bundleRecup);
                    }
                });
            } else if (tipoOperacao.equals("blocked")
                    && verificaRelacao(referenceHashMapBlocked, grupoAtualizado.getIdGrupo())) {
                gruposBloqueadosDiffDAO.atualizarGrupoPorPayload(grupoAtualizado, tipoPayload, new GrupoDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        listaDadosGruposBloqueados.put(grupoAtualizado.getIdGrupo(), grupoAtualizado);
                        //Fazer esse put no payload do adapter também
                        adapterGrupoBloqueado.notifyItemChanged(index, bundleRecup);
                    }
                });
            } else if (tipoOperacao.equals("following")
                    && verificaRelacao(referenceHashMapFollowing, grupoAtualizado.getIdGrupo())) {
                gruposSeguindoDiffDAO.atualizarGrupoPorPayload(grupoAtualizado, tipoPayload, new GrupoDiffDAO.RetornaBundleCallback() {
                    @Override
                    public void onBundleRecuperado(int index, Bundle bundleRecup) {
                        listaDadosGruposSeguindo.put(grupoAtualizado.getIdGrupo(), grupoAtualizado);
                        //Fazer esse put no payload do adapter também
                        adapterGrupoSeguindo.notifyItemChanged(index, bundleRecup);
                    }
                });
            }
        }
    }

    private void atualizarLista(String tipoOperacao, List<Grupo> listaAlvo) {
        Collections.sort(listaAlvo, grupoComparator);
        if (tipoOperacao.equals("my")) {
            adapterMeuGrupo.updateGrupoList(listaAlvo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                }
            });
        } else if (tipoOperacao.equals("blocked")) {
            adapterGrupoBloqueado.updateGrupoList(listaAlvo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                }
            });
        } else if (tipoOperacao.equals("following")) {
            adapterGrupoSeguindo.updateGrupoList(listaAlvo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                @Override
                public void onAtualizado() {
                }
            });
        }
    }

    private void removeValueEventListenerMy() {
        if (listenerHashMapMy != null && referenceHashMapMy != null) {
            for (String userId : listenerHashMapMy.keySet()) {
                Query userRef = referenceHashMapMy.get(userId);
                ChildEventListener listener = listenerHashMapMy.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                    //**ToastCustomizado.toastCustomizadoCurto("Clear", requireContext());
                }
                contadorRemocaoMy++;
                if (contadorRemocaoMy == referenceHashMapMy.size()) {
                    referenceHashMapMy.clear();
                    listenerHashMapMy.clear();
                }
            }
        }
    }

    private void removeValueEventListenerBlocked() {
        if (listenerHashMapBlocked != null && referenceHashMapBlocked != null) {
            for (String userId : listenerHashMapBlocked.keySet()) {
                Query userRef = referenceHashMapBlocked.get(userId);
                ChildEventListener listener = listenerHashMapBlocked.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                    //**ToastCustomizado.toastCustomizadoCurto("Clear", requireContext());
                }
                contadorRemocaoBlocked++;
                if (contadorRemocaoBlocked == referenceHashMapBlocked.size()) {
                    referenceHashMapBlocked.clear();
                    listenerHashMapBlocked.clear();
                }
            }
        }
    }

    private void removeValueEventListenerFollowing() {
        if (listenerHashMapFollowing != null && referenceHashMapFollowing != null) {
            for (String userId : listenerHashMapFollowing.keySet()) {
                Query userRef = referenceHashMapFollowing.get(userId);
                ChildEventListener listener = listenerHashMapFollowing.get(userId);
                if (userRef != null && listener != null) {
                    userRef.removeEventListener(listener);
                    //**ToastCustomizado.toastCustomizadoCurto("Clear", requireContext());
                }
                contadorRemocaoFollowing++;
                if (contadorRemocaoFollowing == referenceHashMapFollowing.size()) {
                    referenceHashMapFollowing.clear();
                    listenerHashMapFollowing.clear();
                }
            }
        }
    }

    private void adicionarGrupo(Grupo grupoAlvo, String tipoDado) {
        recuperarDadosGrupo(grupoAlvo.getIdGrupo(), new RecuperaGrupoCallback() {
            @Override
            public void onRecuperado(Grupo grupoAtual) {
                if (tipoDado.equals("blocked")) {
                    gruposBloqueadosDiffDAO.adicionarGrupo(grupoAtual);
                    Collections.sort(listaGruposBloqueados, grupoComparator);
                    adapterGrupoBloqueado.updateGrupoList(listaGruposBloqueados, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            listaDadosGruposBloqueados.put(grupoAtual.getIdGrupo(), grupoAtual);
                            int posicao = adapterGrupoBloqueado.findPositionInList(grupoAtual.getIdGrupo());
                            if (posicao != -1) {
                                adapterGrupoBloqueado.notifyItemChanged(adapterGrupoBloqueado.findPositionInList(grupoAtual.getIdGrupo()));
                            }
                        }
                    });
                } else if (tipoDado.equals("following")) {
                    gruposSeguindoDiffDAO.adicionarGrupo(grupoAtual);
                    Collections.sort(listaGruposSeguindo, grupoComparator);
                    adapterGrupoSeguindo.updateGrupoList(listaGruposSeguindo, new AdapterPreviewGroup.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            listaDadosGruposSeguindo.put(grupoAtual.getIdGrupo(), grupoAtual);
                            int posicao = adapterGrupoSeguindo.findPositionInList(grupoAtual.getIdGrupo());
                            if (posicao != -1) {
                                adapterGrupoSeguindo.notifyItemChanged(adapterGrupoSeguindo.findPositionInList(grupoAtual.getIdGrupo()));
                            }
                        }
                    });
                }
            }

            @Override
            public void onSemDado() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void recuperarDadosGrupo(String idGrupo, RecuperaGrupoCallback callback) {
        FirebaseRecuperarUsuario.recuperaGrupo(idGrupo, new FirebaseRecuperarUsuario.RecuperaGrupoCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                GroupUtils.checkBlockingStatus(getApplicationContext(), idGrupo, new GroupUtils.CheckLockCallback() {
                    @Override
                    public void onBlocked(boolean status) {
                        grupoAtual.setIndisponivel(status);
                        callback.onRecuperado(grupoAtual);
                    }

                    @Override
                    public void onError(String message) {
                        grupoAtual.setIndisponivel(true);
                        callback.onRecuperado(grupoAtual);
                    }
                });
            }

            @Override
            public void onSemDado() {
                callback.onSemDado();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    @Override
    public void onRemocao(Grupo grupoAlvo, int posicao, String tipoGrupo) {
        switch (tipoGrupo) {
            case Grupo.MY_GROUP:
                logicaRemocao(grupoAlvo, listaDadosMeusGrupos, tipoGrupo, listaMeusGrupos);
                break;
            case Grupo.BLOCKED_GROUP:
                logicaRemocao(grupoAlvo, listaDadosGruposBloqueados, tipoGrupo, listaGruposBloqueados);
                break;
            case Grupo.GROUP_FOLLOWING:
                logicaRemocao(grupoAlvo, listaDadosGruposSeguindo, tipoGrupo, listaGruposSeguindo);
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
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void inicializandoComponentes() {
        recyclerView = findViewById(R.id.recyclerViewListCommunity);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
    }
}