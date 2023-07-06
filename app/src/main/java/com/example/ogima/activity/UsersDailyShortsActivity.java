package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterUsersDaily;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsersDailyShortsActivity extends AppCompatActivity implements AdapterUsersDaily.RecuperaPosicaoAnterior, AdapterUsersDaily.AnimacaoIntent {

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private ChipGroup chipGroupDailyShorts;
    private Chip chipDailyShortsFavoritos, chipDailyShortsAmigos,
            chipDailyShortsSeguidores, chipDailyShortsSeguindo;
    private MaterialSearchView materialSearchDailyShorts;
    private RecyclerView recyclerViewDailyShorts;
    private AdapterUsersDaily adapterUsersDaily;
    private List<Usuario> listaUsuarios = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private boolean filtrado = false;
    private long lastTimestamp;
    private final static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Handler handler = new Handler();
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private Query queryLoadMore;
    private Query queryInicial;

    private ArrayList<String> idsComVinculo = new ArrayList<>();
    private HashSet<String> idsComVinculoSemDuplicata = new HashSet<>();

    private boolean dadosExistentes = false;
    private boolean encontrouUsuarioComDaily = false;
    private int nrUsuariosAdicionados = 0;
    private boolean dadosCarregados = false;
    private int indexAtual = 0;
    private int indexFirst = 0;

    @Override
    public void onExecutarAnimacao() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public interface RecuperaIdsComVinculo {
        void onRecuperacaoCompleta(@NonNull ArrayList<String> idsRecuperados, boolean existemDados);

        void onError(@NonNull String message);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentPosition == -1 && !dadosCarregados) {
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaUsuarios, adapterUsersDaily);
            setLoading(true);
            recuperarDadosIniciais();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_daily_shorts);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Ogima");
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastCustomizado.toastCustomizadoCurto("Load more teste", getApplicationContext());
            }
        });
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

    private void testeDaily() {

        /*Teste de visualização
        listaDailys.add(new DailyShort("https://media.tenor.com/r0IRrRJqMIwAAAAd/dance-anime.gif",
                "https://images.unsplash.com/photo-1525915473429-44eb9efc26ad?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1yZWxhdGVkfDV8fHxlbnwwfHx8fHw%3D&auto=format&fit=crop&w=500&q=60",
                "Laura Anier", "12:27", "imagem"));
        listaDailys.add(new DailyShort("https://media.tenor.com/aYmIBD8B_b8AAAAC/anime-dance.gif",
                "https://images.unsplash.com/photo-1508285296015-c0b524447532?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=401&q=80",
                "Bruna Slad", "14:17", "imagem"));
        listaDailys.add(new DailyShort("https://media.tenor.com/36U-DfUl5MoAAAAC/anime-dance.gif",
                "https://firebasestorage.googleapis.com/v0/b/ogima-7.appspot.com/o/dailyShorts%2Fvideos%2FcmFmYWJlbmVkZXRmZXJAZ21haWwuY29t%2Fvideoc8f9170d-2a63-4ec9-ab66-d5e41a220fff.mp4?alt=media&token=6473d1d2-69c6-44c6-9c5c-c78670be0302",
                "Jennifer Stilson", "17:17", "video"));
         */
    }

    private void configPaginacao() {
        if (recyclerViewDailyShorts != null) {
            isScrolling = true;

            recyclerViewDailyShorts.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerViewDailyShorts.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                                    carregarMaisDados( 0);
                                }
                            }
                        }
                    });
                }
            }, 100);
        }
    }

    private void recuperarDadosIniciais() {
        recuperarIds(new RecuperaIdsComVinculo() {
            @Override
            public void onRecuperacaoCompleta(@NonNull ArrayList<String> idsRecuperados, boolean existemDados) {

                dadosExistentes = existemDados;
                dadosCarregados = true;


                if (existemDados) {
                    recuperarPrimeiroUsuario();
                }
            }

            @Override
            public void onError(@NonNull String message) {

            }
        });
    }

    private void recuperarPrimeiroUsuario() {

        if (listaUsuarios != null && listaUsuarios.size() == 2) {
            setLoading(false);
            configPaginacao();
            return;
        }

        if (idsComVinculo != null && idsComVinculo.size() > 0
                && indexFirst >= idsComVinculo.size()) {
            return;
        }

        if (idsComVinculo != null && idsComVinculo.size() > 0) {
            String idUsuarioBuscado = idsComVinculo.get(indexFirst);

            queryInicial = firebaseRef.child("usuarios")
                    .child(idUsuarioBuscado);

            DatabaseReference verificaSeExisteDailyRef = firebaseRef.child("dailyShorts")
                    .child(idUsuarioBuscado);

            verificaSeExisteDailyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    ToastCustomizado.toastCustomizadoCurto("Inicio", getApplicationContext());
                                    Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                                    indexFirst++;
                                    adicionarDaily(usuarioAtual);
                                    Log.d("AMIGOSUTILS", "RECUPERADO id: " + usuarioAtual.getIdUsuario());
                                } else {
                                    indexFirst++;
                                    recuperarPrimeiroUsuario();
                                }
                                queryInicial.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        indexFirst++;
                        recuperarPrimeiroUsuario();
                    }
                    verificaSeExisteDailyRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void configRecycler() {
        if (linearLayoutManager != null) {

        } else {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerViewDailyShorts.setHasFixedSize(true);
        recyclerViewDailyShorts.setLayoutManager(linearLayoutManager);

        if (adapterUsersDaily == null) {
            adapterUsersDaily = new AdapterUsersDaily(getApplicationContext(), listaUsuarios,
                    this::onPosicaoAnterior, this::onExecutarAnimacao);
        }

        recyclerViewDailyShorts.setAdapter(adapterUsersDaily);
    }

    private void inicializandoComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        chipGroupDailyShorts = findViewById(R.id.chipGroupDailyShorts);
        chipDailyShortsFavoritos = findViewById(R.id.chipDailyShortsFavoritos);
        chipDailyShortsAmigos = findViewById(R.id.chipDailyShortsAmigos);
        chipDailyShortsSeguidores = findViewById(R.id.chipDailyShortsSeguidores);
        chipDailyShortsSeguindo = findViewById(R.id.chipDailyShortsSeguindo);
        materialSearchDailyShorts = findViewById(R.id.materialSearchDailyShorts);
        recyclerViewDailyShorts = findViewById(R.id.recyclerViewDailyShorts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchview_grupo_publico, menu);
        MenuItem item = menu.findItem(R.id.menu_icon_search_grupo_publico);
        materialSearchDailyShorts.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void adicionarDaily(Usuario usuario) {
        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());

        usuarioDiffDAO.adicionarUsuario(usuario);
        idsUsuarios.add(usuario.getIdUsuario());
        adapterUsersDaily.updateDailyShortList(listaUsuarios);
        recuperarPrimeiroUsuario();
        //ToastCustomizado.toastCustomizado("Size lista: " + listaUsuarios.size(), getApplicationContext());
    }

    private void adicionarMaisDados(List<Usuario> newUsuario) {

        //ToastCustomizado.toastCustomizadoCurto("dados novos", getApplicationContext());

        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDiffDAO.carregarMaisUsuario(newUsuario, idsUsuarios);
            adapterUsersDaily.updateDailyShortList(listaUsuarios);
            //*ToastCustomizado.toastCustomizadoCurto("Mais dados", getApplicationContext());
            setLoading(false);
        }
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    private void recuperarIds(RecuperaIdsComVinculo callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {

                boolean dadosAtuaisExistentes = false;

                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    idsComVinculoSemDuplicata.addAll(listaIdAmigos);
                    idsComVinculo.addAll(idsComVinculoSemDuplicata);
                    dadosAtuaisExistentes = true;
                }

                if (listaIdSeguindo != null && listaIdSeguindo.size() > 0) {
                    idsComVinculoSemDuplicata.addAll(listaIdSeguindo);
                    idsComVinculo.addAll(idsComVinculoSemDuplicata);
                    dadosAtuaisExistentes = true;
                }

                idsComVinculoSemDuplicata.clear();

                callback.onRecuperacaoCompleta(idsComVinculo, dadosAtuaisExistentes);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void carregarMaisDados(int nrUsuariosAdicionados) {

        if (nrUsuariosAdicionados == 10) {
            indexFirst--;
            setLoading(false);
            return;
        }

        if (indexFirst != -1 && idsComVinculo != null
                && idsComVinculo.size() > 0 && indexFirst >= idsComVinculo.size()) {
            //Busca finalizada.
            dadosExistentes = false;
            return;
        }

        if (dadosExistentes && idsComVinculo != null && idsComVinculo.size() > 0) {
            //Prosseguir com a busca de novos dados, pois agora já
            //foi removido dados que já estão na lista.

            for (String idVinc : idsComVinculo) {
                Log.d("VINCULOUTILS", "Id armazenado " + idVinc);
            }

            Log.d("VINCULOUTILS", "INDEX MORE " + indexFirst);
            Log.d("VINCULOUTILS", "Lista vinc " + idsComVinculo.size());

            if (indexFirst < idsComVinculo.size()) {
                ToastCustomizado.toastCustomizadoCurto("INDEX: " + indexFirst, getApplicationContext());
                //Log.d("VINCULOUTILS", "Id " + idsComVinculo.get(index));
                //Log.d("VINCULOUTILS", "Adicionados " + nrUsuariosAdicionados);


                String idUsuarioBuscado = idsComVinculo.get(indexFirst);

                queryLoadMore = firebaseRef.child("usuarios")
                        .child(idUsuarioBuscado);

                DatabaseReference verificaSeExisteDailyRef = firebaseRef.child("dailyShorts")
                        .child(idUsuarioBuscado);

                verificaSeExisteDailyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //Usuário buscado tem daily.
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
                        } else {
                            indexFirst++;
                            carregarMaisDados(nrUsuariosAdicionados);
                        }
                        verificaSeExisteDailyRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    private void removerIdUtilizado(String idUtilizado) {
        if (idsComVinculo != null && idsComVinculo.size() > 0
                && idsComVinculo.contains(idUtilizado)) {
            idsComVinculo.remove(idUtilizado);
            Log.d("AMIGOSUTILS", "Remoção concluída");
        }
    }
}