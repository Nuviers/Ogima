package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterViewerUsersDaily;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewsDailyShortActivity extends AppCompatActivity implements AdapterViewerUsersDaily.RecuperaPosicaoAnterior, AdapterViewerUsersDaily.AnimacaoIntent {

    private DailyShort dailyShortAtual = new DailyShort();
    private String idDailyShortAtual = null;
    private String idDonoDailyShortAtual = null;
    private Toolbar toolbarInc;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private RecyclerView recyclerView;
    private Query queryLoadMore;
    private Query queryInicial;
    private int mCurrentPosition = -1;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private final static int PAGE_SIZE = 10;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private ArrayList<String> listaIdViewers = new ArrayList<>();
    private boolean dadosExistentes = false;
    private boolean dadosCarregados = false;
    private int indexFirst = 0;
    private AdapterViewerUsersDaily adapterViewerUsersDaily;
    private final static String TAG = "VIEWSDAILYUTILS";

    @Override
    public void onExecutarAnimacao() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private interface RecuperarListaViews {
        void onRecuperado(ArrayList<String> listaIdsVisualizadores);

        void onSemViews();

        void onErroAoRecuperar(String message);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentPosition == -1 && !dadosCarregados) {
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterViewerUsersDaily);
            setLoading(true);
            buscarDadosIniciais();
        }
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
        setContentView(R.layout.activity_views_daily_short);
        inicializandoComponentes();
        setSupportActionBar(toolbarInc);
        setTitle("");
        txtViewIncTituloToolbar.setText("Visualizações DailyShort");
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null && dados.containsKey("dailyShortAtual")) {
            dailyShortAtual = (DailyShort) dados.getSerializable("dailyShortAtual");

            if (dailyShortAtual.getIdDailyShort() != null) {
                idDailyShortAtual = dailyShortAtual.getIdDailyShort();
            }

            if (dailyShortAtual.getIdDonoDailyShort() != null) {
                idDonoDailyShortAtual = dailyShortAtual.getIdDonoDailyShort();
            }
        }

        clickListeners();
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(linearLayoutManager);

        if (adapterViewerUsersDaily == null) {
            adapterViewerUsersDaily = new AdapterViewerUsersDaily(getApplicationContext(),
                    listaUsuarios, this, this);
        }
        recyclerView.setAdapter(adapterViewerUsersDaily);
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void inicializandoComponentes() {
        toolbarInc = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        recyclerView = findViewById(R.id.recyclerViewsDaily);
    }

    private void adicionarDaily(Usuario usuario) {
        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());

        usuarioDiffDAO.adicionarUsuario(usuario);
        idsUsuarios.add(usuario.getIdUsuario());
        adapterViewerUsersDaily.updateDailyShortList(listaUsuarios);
        recuperarUsuariosIniciais();
        //ToastCustomizado.toastCustomizado("Size lista: " + listaUsuarios.size(), getApplicationContext());
    }

    private void adicionarMaisDados(List<Usuario> newUsuario) {

        //ToastCustomizado.toastCustomizadoCurto("dados novos", getApplicationContext());

        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDiffDAO.carregarMaisUsuario(newUsuario, idsUsuarios);
            adapterViewerUsersDaily.updateDailyShortList(listaUsuarios);
            //*ToastCustomizado.toastCustomizadoCurto("Mais dados", getApplicationContext());
            setLoading(false);
        }
    }

    private void configPaginacao() {
        if (recyclerView != null) {
            isScrolling = true;

            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                                isScrolling = true;
                            }
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (linearLayoutManager != null) {
                                if (isLoading()) {
                                    return;
                                }

                                int totalItemCount = linearLayoutManager.getItemCount();
                                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                                //ToastCustomizado.toastCustomizadoCurto("Scrolled",getApplicationContext());

                                if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                                    isScrolling = false;

                                    //*progressBarLoading.setVisibility(View.VISIBLE);

                                    setLoading(true);

                                    carregarMaisDados(0);
                                }
                            }
                        }
                    });
                }
            }, 100);
        }
    }

    private void buscarDadosIniciais() {
        if (idDailyShortAtual != null) {
            recuperarIdsViews(new RecuperarListaViews() {
                @Override
                public void onRecuperado(ArrayList<String> listaIdsVisualizadores) {
                    dadosExistentes = true;
                    dadosCarregados = true;
                    listaIdViewers = listaIdsVisualizadores;
                    recuperarUsuariosIniciais();
                }

                @Override
                public void onSemViews() {
                    ToastCustomizado.toastCustomizado("Não existem visualizações para esse dailyShort no momento", getApplicationContext());
                    finish();
                }

                @Override
                public void onErroAoRecuperar(String message) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar as visualizações, tente novamente mais tarde.", getApplicationContext());
                    finish();
                }
            });
        } else {
            finish();
        }
    }

    private void recuperarIdsViews(RecuperarListaViews callback) {

        DatabaseReference dadosDailyAtualRef = firebaseRef.child("dailyShorts")
                .child(idDonoDailyShortAtual).child(idDailyShortAtual);

        dadosDailyAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    DailyShort dailyShortAtual = snapshot.getValue(DailyShort.class);
                    if (dailyShortAtual.getListaIdsVisualizadores() != null
                            && dailyShortAtual.getListaIdsVisualizadores().size() > 0) {
                        callback.onRecuperado(dailyShortAtual.getListaIdsVisualizadores());
                    } else {
                        callback.onSemViews();
                    }
                } else {
                    callback.onSemViews();
                }
                dadosDailyAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onErroAoRecuperar(error.getMessage());
            }
        });
    }

    private void recuperarUsuariosIniciais() {
        if (listaUsuarios != null && listaUsuarios.size() == 2) {
            //Dados iniciais recuperados com sucesso, seguir com a paginação.
            setLoading(false);
            configPaginacao();
            return;
        }

        if (listaIdViewers != null && listaIdViewers.size() > 0
                && indexFirst >= listaIdViewers.size()) {
            //Não há mais dados para serem carregados.
            return;
        }

        if (listaIdViewers != null && listaIdViewers.size() > 0) {
            String idUsuarioBuscado = listaIdViewers.get(indexFirst);

            queryInicial = firebaseRef.child("usuarios")
                    .child(idUsuarioBuscado);

            queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        ToastCustomizado.toastCustomizadoCurto("Inicio", getApplicationContext());
                        Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                        indexFirst++;
                        adicionarDaily(usuarioAtual);
                        Log.d(TAG, "RECUPERADO id: " + usuarioAtual.getIdUsuario());
                    } else {
                        indexFirst++;
                        recuperarUsuariosIniciais();
                    }
                    queryInicial.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void carregarMaisDados(int nrUsuariosAdicionados) {

        if (nrUsuariosAdicionados == PAGE_SIZE) {
            //10 itens atuais foram recuperados.
            indexFirst--;
            setLoading(false);
            return;
        }

        if (indexFirst != -1 && listaIdViewers != null
                && listaIdViewers.size() > 0 && indexFirst >= listaIdViewers.size()) {
            //Não existem mais itens a serem carregados.
            dadosExistentes = false;
            return;
        }

        if (dadosExistentes && listaIdViewers != null
                && listaIdViewers.size() > 0) {
            for (String idRecuperado : listaIdViewers) {
                Log.d(TAG, "Id recuperado " + idRecuperado);
            }

            Log.d(TAG, "INDEX MORE " + indexFirst);
            Log.d(TAG, "Tamanho lista " + listaIdViewers);

            if (indexFirst < listaIdViewers.size()) {
                ToastCustomizado.toastCustomizadoCurto("INDEX: " + indexFirst, getApplicationContext());

                String idUsuarioBuscado = listaIdViewers.get(indexFirst);

                queryLoadMore = firebaseRef.child("usuarios")
                        .child(idUsuarioBuscado);

                queryLoadMore.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                            List<Usuario> listaNovosUsuarios = new ArrayList<>();
                            listaNovosUsuarios.add(usuarioAtual);
                            indexFirst++;
                            adicionarMaisDados(listaNovosUsuarios);
                            carregarMaisDados(nrUsuariosAdicionados + 1);
                        } else {
                            indexFirst++;
                            carregarMaisDados(nrUsuariosAdicionados);
                        }
                        queryLoadMore.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }
}