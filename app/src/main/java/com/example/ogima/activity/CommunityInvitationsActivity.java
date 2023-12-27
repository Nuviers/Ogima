package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterCommunityInvitations;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ConviteDiffDAO;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Convite;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommunityInvitationsActivity extends AppCompatActivity implements AdapterCommunityInvitations.AnimacaoIntent, AdapterCommunityInvitations.RecuperaPosicaoAnterior, AdapterCommunityInvitations.RemoverConviteListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private SpinKitView spinProgress;
    private LinearLayoutManager linearLayoutManager;
    private static int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private RecyclerView.OnScrollListener scrollListener;
    private List<Convite> listaConvites = new ArrayList<>();
    private Set<String> idsComunidades = new HashSet<>();
    private ConviteDiffDAO conviteDiffDAO;
    private Query queryInicial, queryLoadMore;
    private HashMap<String, Object> listaDadosComunidade = new HashMap<>();
    private long lastTimestamp = -1;
    private int queryDelayMillis = 500;
    private AdapterCommunityInvitations adapterInvitations;
    private boolean trocarQueryInicial = false;
    private int mCurrentPosition = -1;
    private ProgressDialog progressDialog;

    @Override
    public void onResume() {
        super.onResume();
        // Desliza o recyclerView para a posição salva.
        if (mCurrentPosition != -1 &&
                listaConvites != null && listaConvites.size() > 0
                && linearLayoutManager != null) {
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
    public void onStop() {
        super.onStop();
        if (adapterInvitations != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        limparPeloOnDestroy();
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

    public CommunityInvitationsActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_invitations);
        inicializarComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        configInicial();
    }

    private interface RecuperaUser {
        void onRecuperado(Usuario usuarioAtual);

        void onSemDado();

        void onError(String message);
    }

    private interface RecuperarComunidadeCallback {
        void onConcluido(Comunidade comunidadeRecuperada);
    }

    private interface VerificaCriterio {
        void onCriterioAtendido();

        void onSemVinculo();

        void onError(String message);
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void configInicial() {
        txtViewTitleToolbar.setText("Convites");
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        progressDialog = new ProgressDialog(CommunityInvitationsActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        setLoading(true);
        configRecycler();
        conviteDiffDAO = new ConviteDiffDAO(listaConvites, adapterInvitations);
        clickListeners();
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean epilepsia) {
                adapterInvitations.setStatusEpilepsia(epilepsia);
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                onBackPressed();
            }
        });
        verificaNrConvites();
        recuperarDadosIniciais();
        configPaginacao();
    }

    private void verificaNrConvites() {
        DatabaseReference nrConvitesRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("nrConvitesComunidade");
        nrConvitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    long nrConvites = snapshot.getValue(Long.class);
                    if (nrConvites <= -1) {
                        nrConvites = 0;
                    }
                    String conteudo = String.format("%s  %s", "Convites:", nrConvites);
                    txtViewTitleToolbar.setText(FormatarContadorUtils.abreviarTexto(conteudo, 30));
                } else {
                    txtViewTitleToolbar.setText(String.format("%s  %s", "Convites:", 0));
                }
                nrConvitesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (adapterInvitations == null) {
            adapterInvitations = new AdapterCommunityInvitations(getApplicationContext(),
                    listaConvites, this, this, listaDadosComunidade,
                    getResources().getColor(R.color.community_invitations), this, CommunityInvitationsActivity.this);
        }
        recyclerView.setAdapter(adapterInvitations);
    }

    private void recuperarDadosIniciais() {
        if (listaConvites != null && listaConvites.size() >= 1) {
            trocarQueryInicial = false;
            return;
        }
        if (trocarQueryInicial && lastTimestamp != -1) {
            queryInicial = firebaseRef.child("convitesComunidade")
                    .child(idUsuario).orderByChild("timestampinteracao")
                    .startAt(lastTimestamp + 1)
                    .limitToFirst(1);
        } else {
            queryInicial = firebaseRef.child("convitesComunidade")
                    .child(idUsuario).orderByChild("timestampinteracao").limitToFirst(1);
        }
        exibirProgress();
        queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Convite convite = snapshot1.getValue(Convite.class);
                        if (convite != null
                                && convite.getIdComunidade() != null
                                && !convite.getIdComunidade().isEmpty()) {
                            adicionarConvite(convite);
                            lastTimestamp = convite.getTimestampinteracao();
                        }
                    }
                } else {
                    ocultarProgress();
                    ToastCustomizado.toastCustomizadoCurto("Não existem convites de comunidade no momento.", getApplicationContext());
                }
                queryInicial.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
                ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao recuperar os convites de comunidades.", "Code:", error.getCode()), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void adicionarConvite(Convite conviteAlvo) {
        if (listaConvites != null && listaConvites.size() >= 1) {
            ocultarProgress();
            setLoading(false);
            return;
        }
        String idComunidade = conviteAlvo.getIdComunidade();
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade dadosComunidade) {
                conviteDiffDAO.adicionarConvite(conviteAlvo);
                conviteDiffDAO.adicionarIdAoSet(idsComunidades, idComunidade);
                adapterInvitations.updateConviteList(listaConvites, new AdapterCommunityInvitations.ListaAtualizadaCallback() {
                    @Override
                    public void onAtualizado() {
                        adicionarDadosDaComunidade(dadosComunidade);
                        ocultarProgress();
                        setLoading(false);
                    }
                });
            }

            @Override
            public void onNaoExiste() {
                trocarQueryInicial = true;
                recuperarDadosIniciais();
            }

            @Override
            public void onError(String mensagem) {
                ocultarProgress();
                setLoading(false);
            }
        });
    }

    private void adicionarDadosDaComunidade(Comunidade dadosComunidade) {
        listaDadosComunidade.put(dadosComunidade.getIdComunidade(), dadosComunidade);
    }

    private void configPaginacao() {
        if (recyclerView != null) {
            scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@androidx.annotation.NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

                    }
                }

                @Override
                public void onScrolled(@androidx.annotation.NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (linearLayoutManager != null) {
                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isLoading()) {
                                    return;
                                }
                                int totalItemCount = linearLayoutManager.getItemCount();
                                if (lastVisibleItemPosition == totalItemCount - 1) {
                                    setLoading(true);
                                    carregarMaisDados();
                                }
                            }
                        }, 100);
                    }
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados() {
        exibirProgress();
        queryLoadMore = firebaseRef.child("convitesComunidade")
                .child(idUsuario)
                .orderByChild("timestampinteracao")
                .startAt(lastTimestamp)
                .limitToFirst(PAGE_SIZE);
        queryLoadMore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Convite conviteMore = snapshotChildren.getValue(Convite.class);
                        if (conviteMore != null
                                && conviteMore.getIdComunidade() != null
                                && !conviteMore.getIdComunidade().isEmpty()) {
                            List<Convite> newConvite = new ArrayList<>();
                            long key = conviteMore.getTimestampinteracao();
                            if (lastTimestamp != -1 && key != -1) {
                                if(key != lastTimestamp || listaConvites.size() > 0 &&
                                        !conviteMore.getIdComunidade().equals(listaConvites.get(listaConvites.size() - 1).getIdComunidade())){
                                    newConvite.add(conviteMore);
                                    lastTimestamp = key;
                                }
                            }
                            // Remove a última chave usada
                            if (newConvite.size() > PAGE_SIZE) {
                                newConvite.remove(0);
                            }
                            if (lastTimestamp != -1) {
                                adicionarMaisDados(newConvite, conviteMore.getIdComunidade());
                            }
                        }
                    }
                } else {
                    ocultarProgress();
                }
                queryLoadMore.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocultarProgress();
                lastTimestamp = -1;
            }
        });
    }

    private void adicionarMaisDados(List<Convite> newConvite, String idComunidade) {
        if (newConvite != null && newConvite.size() >= 1) {
            FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
                @Override
                public void onComunidadeRecuperada(Comunidade dadosComunidade) {
                    conviteDiffDAO.carregarMaisConvite(newConvite, idsComunidades);
                    conviteDiffDAO.adicionarIdAoSet(idsComunidades, dadosComunidade.getIdComunidade());
                    adapterInvitations.updateConviteList(listaConvites, new AdapterCommunityInvitations.ListaAtualizadaCallback() {
                        @Override
                        public void onAtualizado() {
                            ocultarProgress();
                            adicionarDadosDaComunidade(dadosComunidade);
                            setLoading(false);
                        }
                    });
                }

                @Override
                public void onNaoExiste() {
                    ocultarProgress();
                }

                @Override
                public void onError(String mensagem) {
                    ocultarProgress();
                }
            });
        } else {
            ocultarProgress();
        }
    }

    private void limparPeloOnDestroy() {
        ocultarProgress();
        if (conviteDiffDAO != null) {
            conviteDiffDAO.limparListaConvites();
        }
        if (listaDadosComunidade != null) {
            listaDadosComunidade.clear();
        }
        if (idsComunidades != null) {
            idsComunidades.clear();
        }
        mCurrentPosition = -1;
    }

    private void exibirProgress() {
        spinProgress.setVisibility(View.VISIBLE);
        ProgressBarUtils.exibirProgressBar(spinProgress, CommunityInvitationsActivity.this);
    }

    private void ocultarProgress() {
        spinProgress.setVisibility(View.GONE);
        ProgressBarUtils.ocultarProgressBar(spinProgress, CommunityInvitationsActivity.this);
    }

    @Override
    public void onRemocao(Convite conviteAlvo, int posicao) {
        if (listaConvites != null
                && listaConvites.size() > 0
                && posicao < listaConvites.size()) {
            conviteDiffDAO.removerConvite(conviteAlvo);
        }
        if (listaDadosComunidade != null
                && listaDadosComunidade.size() > 0) {
            listaDadosComunidade.remove(conviteAlvo.getIdComunidade());
        }
        adapterInvitations.updateConviteList(listaConvites, new AdapterCommunityInvitations.ListaAtualizadaCallback() {
            @Override
            public void onAtualizado() {
                int posicaoComunidade = adapterInvitations.findPositionInList(conviteAlvo.getIdComunidade());
                if (posicaoComunidade != -1) {
                    adapterInvitations.notifyItemChanged(adapterInvitations.findPositionInList(conviteAlvo.getIdComunidade()));
                }
            }
        });
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

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void inicializarComponentes() {
        spinProgress = findViewById(R.id.spinProgressBarRecycler);
        recyclerView = findViewById(R.id.recyclerViewCommunityInvitations);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
    }
}