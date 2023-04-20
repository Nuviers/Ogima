package com.example.ogima.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterComunidadesVinculoDiff;
import com.example.ogima.adapter.AdapterTopicosGrupoPublico;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Comunidade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComunidadesComVinculoActivity extends AppCompatActivity implements AdapterComunidadesVinculoDiff.RemocaoComunidadeVinculoListener, AdapterComunidadesVinculoDiff.RecuperaPosicaoListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Toolbar toolbarComunidadesPublicos;
    private ImageButton imgBtnBackComunidadesPublicos;
    private RecyclerView recyclerTopicosComunidade;
    //Mudar para o adapter que vai exibir os comunidades de acordo com o filtro selecionado.
    private AdapterTopicosGrupoPublico adapterTopicosComunidadePublico;
    private final String[] topicosComunidade = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    private final ArrayList<String> listaTopicosComunidade = new ArrayList<>(Arrays.asList(topicosComunidade));
    private Button btnFiltrarComunidades;

    //Comunidades
    private AdapterComunidadesVinculoDiff adapterComunidadesVinculo;
    private RecyclerView recyclerComunidadesPublicos;
    private ComunidadeDiffDAO comunidadeDiffDAO;
    private List<Comunidade> listaComunidades = new ArrayList<>();
    private Query comunidadeRef;
    private ChildEventListener childEventListener;
    private LinearLayoutManager linearLayoutManagerComunidades, linearLayoutManagerTopicos;

    //SearchView
    private FrameLayout frameComunidadesPublicos;
    private MaterialSearchView materialSearch;

    private int mCurrentPosition = 0;

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
    protected void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 && mCurrentPosition > 0) {
            recyclerComunidadesPublicos.scrollToPosition(mCurrentPosition);
            mCurrentPosition = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchview_grupo_publico, menu);
        MenuItem item = menu.findItem(R.id.menu_icon_search_grupo_publico);
        materialSearch.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

        configRecyclerView();

        comunidadeDiffDAO = new ComunidadeDiffDAO(listaComunidades, adapterComunidadesVinculo);

        recuperarComunidades(false, null);

        configurarMaterialSearchView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (materialSearch != null) {
            materialSearch.closeSearch();
        }

        if (childEventListener != null) {
            comunidadeRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
        comunidadeDiffDAO.limparListaComunidades();

        adapterTopicosComunidadePublico.limparTopicosFiltrados();
    }

    private void configurarMaterialSearchView() {

        configFocoComponentes();

        materialSearch.setHint("Pesquisar comunidades que você participa");
        materialSearch.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    String dadoDigitado = Normalizer.normalize(newText, Normalizer.Form.NFD);
                    dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    dadoDigitado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(dadoDigitado);
                    recuperarComunidades(false, dadoDigitado);
                }
                return true;
            }
        });
    }

    private void configFocoComponentes() {
        materialSearch.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                recyclerTopicosComunidade.setVisibility(View.GONE);
                btnFiltrarComunidades.setVisibility(View.GONE);
                limparTopicos();
            }

            @Override
            public void onSearchViewClosed() {
                recyclerTopicosComunidade.setVisibility(View.VISIBLE);
                btnFiltrarComunidades.setVisibility(View.VISIBLE);
                limparTopicos();
                recuperarComunidades(false, null);
            }
        });
    }

    private void recuperarComunidades(Boolean filtragem, String nomeBuscado) {

        if (childEventListener != null) {
            comunidadeRef.removeEventListener(childEventListener);
            childEventListener = null;
        }

        limparListaComunidadePublico();

        if (nomeBuscado != null) {
            comunidadeRef = firebaseRef.child("comunidades").orderByChild("nomeComunidade")
                    .startAt(nomeBuscado)
                    .endAt(nomeBuscado + "\uf8ff");
        }else{
            comunidadeRef = firebaseRef.child("comunidades").orderByChild("nomeComunidade");
        }

        childEventListener = comunidadeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Comunidade novoComunidade = snapshot.getValue(Comunidade.class);

                    if (novoComunidade.getParticipantes() != null && novoComunidade.getParticipantes().contains(idUsuario)
                    && novoComunidade.getIdSuperAdmComunidade() != null && !novoComunidade.getIdSuperAdmComunidade().equals(idUsuario)) {
                        //Somente exibe a comunidade se o usuário atual for participante.
                        if (filtragem) {
                            if (adapterTopicosComunidadePublico.getListaTopicosSelecionados() != null
                                    && adapterTopicosComunidadePublico.getListaTopicosSelecionados().size() > 0) {

                                for (String topicoFiltrado : adapterTopicosComunidadePublico.getListaTopicosSelecionados()) {
                                    if (novoComunidade.getTopicos().contains(topicoFiltrado)) {
                                        // Adiciona o comunidade na lista mantendo a ordenação
                                        comunidadeDiffDAO.adicionarComunidade(novoComunidade);

                                        // Notifica o adapter das mudanças usando o DiffUtil
                                        adapterComunidadesVinculo.updateComunidadeList(listaComunidades);
                                        break;
                                    }
                                }
                            }
                        } else {
                            // Adiciona o comunidade na lista mantendo a ordenação
                            comunidadeDiffDAO.adicionarComunidade(novoComunidade);

                            // Notifica o adapter das mudanças usando o DiffUtil
                            adapterComunidadesVinculo.updateComunidadeList(listaComunidades);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    // Recupera o comunidade do snapshot
                    Comunidade comunidadeAtualizado = snapshot.getValue(Comunidade.class);

                    // Atualiza o comunidade na lista mantendo a ordenação
                    comunidadeDiffDAO.atualizarComunidade(comunidadeAtualizado);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterComunidadesVinculo.updateComunidadeList(listaComunidades);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    // Recupera o comunidade do snapshot
                    Comunidade comunidadeRemovido = snapshot.getValue(Comunidade.class);
                    Log.d("TESTE-On Child Removed", "Usuario removido do snapshot: " + comunidadeRemovido.getNomeComunidade());

                    // Remove o comunidade da lista mantendo a ordenação
                    comunidadeDiffDAO.removerComunidade(comunidadeRemovido);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterComunidadesVinculo.updateComunidadeList(listaComunidades);
                    Log.d("TESTE-On Child Removed", "Adapter notificado com sucesso");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunidade_vinculo);
        inicializandoComponentes();
        setSupportActionBar(toolbarComunidadesPublicos);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        clickListeners();
    }

    private void configRecyclerView() {

        configRecyclerTopicos();

        configRecyclerComunidades();
    }

    private void clickListeners() {

        imgBtnBackComunidadesPublicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnFiltrarComunidades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receberTopicos();
            }
        });
    }

    private void configRecyclerTopicos() {

        if (linearLayoutManagerTopicos != null) {

        } else {
            linearLayoutManagerTopicos = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerTopicos.setOrientation(LinearLayoutManager.HORIZONTAL);
        }

        recyclerTopicosComunidade.setHasFixedSize(true);

        // Defina o gerenciador de layout do RecyclerView como ChipsLayoutManager
        ChipsLayoutManager layoutManager = ChipsLayoutManager.newBuilder(this)
                .setOrientation(ChipsLayoutManager.VERTICAL)
                .setMaxViewsInRow(2)
                .build();

        recyclerTopicosComunidade.setLayoutManager(layoutManager);
        if (adapterTopicosComunidadePublico != null) {

        } else {
            adapterTopicosComunidadePublico = new AdapterTopicosGrupoPublico(getApplicationContext(), listaTopicosComunidade);
        }
        recyclerTopicosComunidade.setAdapter(adapterTopicosComunidadePublico);
        adapterTopicosComunidadePublico.notifyDataSetChanged();
    }

    private void configRecyclerComunidades() {
        //Configuração do recycler de comunidades
        if (linearLayoutManagerComunidades != null) {

        } else {
            linearLayoutManagerComunidades = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerComunidades.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerComunidadesPublicos.setHasFixedSize(true);
        recyclerComunidadesPublicos.setLayoutManager(linearLayoutManagerComunidades);

        if (adapterComunidadesVinculo != null) {

        } else {
            adapterComunidadesVinculo = new AdapterComunidadesVinculoDiff(getApplicationContext(), listaComunidades, adapterTopicosComunidadePublico.getListaTopicosSelecionados(), this, this);
        }
        recyclerComunidadesPublicos.setAdapter(adapterComunidadesVinculo);
    }

    private void receberTopicos() {
        if (adapterTopicosComunidadePublico.getListaTopicosSelecionados() != null
                && adapterTopicosComunidadePublico.getListaTopicosSelecionados().size() > 0) {

            recuperarComunidades(true, null);

            for (String topicos : adapterTopicosComunidadePublico.getListaTopicosSelecionados()) {
                //ToastCustomizado.toastCustomizadoCurto("Recebido " + topicos, getApplicationContext());
            }
        } else {
            recuperarComunidades(false, null);
        }
    }

    private void limparListaComunidadePublico() {
        if (listaComunidades != null && listaComunidades.size() > 0) {
            comunidadeDiffDAO.limparListaComunidades();
            adapterComunidadesVinculo.updateComunidadeList(listaComunidades);
        }
    }

    private void limparTopicos() {
        if (listaTopicosComunidade != null && listaTopicosComunidade.size() > 0) {
            adapterTopicosComunidadePublico.limparTopicosFiltrados();
        }
    }

    private void inicializandoComponentes() {
        toolbarComunidadesPublicos = findViewById(R.id.toolbarComunidadeVinculo);
        imgBtnBackComunidadesPublicos = findViewById(R.id.imgBtnBackComunidadeVinculo);
        recyclerTopicosComunidade = findViewById(R.id.recyclerTopicosComunidade);
        btnFiltrarComunidades = findViewById(R.id.btnFiltrarComunidadeVinculo);
        recyclerComunidadesPublicos = findViewById(R.id.recyclerComunidadeVinculo);
        frameComunidadesPublicos = findViewById(R.id.frameComunidadeVinculo);
        materialSearch = findViewById(R.id.materialSearchComunidadeVinculo);
    }

    @Override
    public void onComunidadeExcluida(Comunidade comunidadeRemovida) {
        // Remove o comunidade da lista mantendo a ordenação
        comunidadeDiffDAO.removerComunidade(comunidadeRemovida);
        Log.d("TESTE-On", "Comunidade removida com sucesso");

        // Notifica o adapter das mudanças usando o DiffUtil
        adapterComunidadesVinculo.updateComunidadeList(listaComunidades);
        Log.d("TESTE-On Child Removed", "Adapter notificado com sucesso");

        SnackbarUtils.showSnackbar(recyclerComunidadesPublicos, "Saída da comunidade bem-sucedida!");
    }

    @Override
    public void onRecuperaPosicao(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }
}