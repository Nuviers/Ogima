package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGruposPublicosDiff;
import com.example.ogima.adapter.AdapterTopicosGrupoPublico;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GrupoTesteDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Grupo;
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

public class GruposPublicosActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Toolbar toolbarGruposPublicos;
    private ImageButton imgBtnBackGruposPublicos;
    private RecyclerView recyclerTopicosGrupo;
    //Mudar para o adapter que vai exibir os grupos de acordo com o filtro selecionado.
    private AdapterTopicosGrupoPublico adapterTopicosGrupoPublico;
    private final String[] topicosGrupo = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    private final ArrayList<String> listaTopicosGrupo = new ArrayList<>(Arrays.asList(topicosGrupo));
    private Button btnFiltrarGrupos;

    //Grupos
    private AdapterGruposPublicosDiff adapterGruposPublicos;
    private RecyclerView recyclerGruposPublicos;
    private GrupoTesteDAO grupoTesteDAO;
    private List<Grupo> listaGrupos = new ArrayList<>();
    private Query grupoRef;
    private ChildEventListener childEventListener;
    private LinearLayoutManager linearLayoutManagerGrupos, linearLayoutManagerTopicos;

    //SearchView
    private FrameLayout frameGruposPublicos;
    private MaterialSearchView materialSearch;

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

        grupoTesteDAO = new GrupoTesteDAO(listaGrupos, adapterGruposPublicos);

        recuperarGrupos(false, null);

        configurarMaterialSearchView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (materialSearch != null) {
            materialSearch.closeSearch();
        }

        if (childEventListener != null) {
            grupoRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
        grupoTesteDAO.limparListaGrupos();

        adapterTopicosGrupoPublico.limparTopicosFiltrados();
    }

    private void configurarMaterialSearchView() {

        configFocoComponentes();

        materialSearch.setHint("Pesquisar grupos públicos pelo nome");
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
                    recuperarGrupos(false, dadoDigitado);
                }
                return true;
            }
        });
    }

    private void configFocoComponentes() {
        materialSearch.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                recyclerTopicosGrupo.setVisibility(View.GONE);
                btnFiltrarGrupos.setVisibility(View.GONE);
                limparTopicos();
            }

            @Override
            public void onSearchViewClosed() {
                recyclerTopicosGrupo.setVisibility(View.VISIBLE);
                btnFiltrarGrupos.setVisibility(View.VISIBLE);
                limparTopicos();
                recuperarGrupos(false, null);
            }
        });
    }

    private void recuperarGrupos(Boolean filtragem, String nomeBuscado) {

        if (childEventListener != null) {
            grupoRef.removeEventListener(childEventListener);
            childEventListener = null;
        }

        limparListaGrupoPublico();

        if (nomeBuscado != null) {
            grupoRef = firebaseRef.child("grupos").orderByChild("nomeGrupo")
                    .startAt(nomeBuscado)
                    .endAt(nomeBuscado + "\uf8ff");
        } else {
            grupoRef = firebaseRef.child("grupos").orderByChild("grupoPublico").equalTo(true);
        }

        childEventListener = grupoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo novoGrupo = snapshot.getValue(Grupo.class);

                    if (novoGrupo.getGrupoPublico() != null && novoGrupo.getGrupoPublico()) {
                        //Somente exibe o grupo se ele for público.
                        if (filtragem) {
                            if (adapterTopicosGrupoPublico.getListaTopicosSelecionados() != null
                                    && adapterTopicosGrupoPublico.getListaTopicosSelecionados().size() > 0) {

                                for (String topicoFiltrado : adapterTopicosGrupoPublico.getListaTopicosSelecionados()) {
                                    if (novoGrupo.getTopicos().contains(topicoFiltrado)) {
                                        // Adiciona o grupo na lista mantendo a ordenação
                                        grupoTesteDAO.adicionarGrupo(novoGrupo);

                                        // Notifica o adapter das mudanças usando o DiffUtil
                                        adapterGruposPublicos.updateGroupPublicList(listaGrupos);
                                        break;
                                    }
                                }
                            }
                        } else {
                            // Adiciona o grupo na lista mantendo a ordenação
                            grupoTesteDAO.adicionarGrupo(novoGrupo);

                            // Notifica o adapter das mudanças usando o DiffUtil
                            adapterGruposPublicos.updateGroupPublicList(listaGrupos);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    // Recupera o grupo do snapshot
                    Grupo grupoAtualizado = snapshot.getValue(Grupo.class);

                    // Atualiza o grupo na lista mantendo a ordenação
                    grupoTesteDAO.atualizarGrupo(grupoAtualizado);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterGruposPublicos.updateGroupPublicList(listaGrupos);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    // Recupera o grupo do snapshot
                    Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                    Log.d("TESTE-On Child Removed", "Usuario removido do snapshot: " + grupoRemovido.getNomeGrupo());

                    // Remove o grupo da lista mantendo a ordenação
                    grupoTesteDAO.removerGrupo(grupoRemovido);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterGruposPublicos.updateGroupPublicList(listaGrupos);
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
        setContentView(R.layout.activity_grupos_publicos);
        inicializandoComponentes();
        setSupportActionBar(toolbarGruposPublicos);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        clickListeners();
    }

    private void configRecyclerView() {

        configRecyclerTopicos();

        configRecyclerGrupos();
    }

    private void clickListeners() {

        imgBtnBackGruposPublicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnFiltrarGrupos.setOnClickListener(new View.OnClickListener() {
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

        recyclerTopicosGrupo.setHasFixedSize(true);

        // Defina o gerenciador de layout do RecyclerView como ChipsLayoutManager
        ChipsLayoutManager layoutManager = ChipsLayoutManager.newBuilder(this)
                .setOrientation(ChipsLayoutManager.VERTICAL)
                .setMaxViewsInRow(2)
                .build();

        recyclerTopicosGrupo.setLayoutManager(layoutManager);
        if (adapterTopicosGrupoPublico != null) {

        } else {
            adapterTopicosGrupoPublico = new AdapterTopicosGrupoPublico(getApplicationContext(), listaTopicosGrupo);
        }
        recyclerTopicosGrupo.setAdapter(adapterTopicosGrupoPublico);
        adapterTopicosGrupoPublico.notifyDataSetChanged();
    }

    private void configRecyclerGrupos() {
        //Configuração do recycler de grupos
        if (linearLayoutManagerGrupos != null) {

        } else {
            linearLayoutManagerGrupos = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerGrupos.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerGruposPublicos.setHasFixedSize(true);
        recyclerGruposPublicos.setLayoutManager(linearLayoutManagerGrupos);

        if (adapterGruposPublicos != null) {

        } else {
            adapterGruposPublicos = new AdapterGruposPublicosDiff(getApplicationContext(), listaGrupos, adapterTopicosGrupoPublico.getListaTopicosSelecionados());
        }
        recyclerGruposPublicos.setAdapter(adapterGruposPublicos);
    }

    private void receberTopicos() {
        if (adapterTopicosGrupoPublico.getListaTopicosSelecionados() != null
                && adapterTopicosGrupoPublico.getListaTopicosSelecionados().size() > 0) {

            recuperarGrupos(true, null);

            for (String topicos : adapterTopicosGrupoPublico.getListaTopicosSelecionados()) {
                //ToastCustomizado.toastCustomizadoCurto("Recebido " + topicos, getApplicationContext());
            }
        } else {
            recuperarGrupos(false, null);
        }
    }

    private void limparListaGrupoPublico() {
        if (listaGrupos != null && listaGrupos.size() > 0) {
            grupoTesteDAO.limparListaGrupos();
            adapterGruposPublicos.updateGroupPublicList(listaGrupos);
        }
    }

    private void limparTopicos() {
        if (listaTopicosGrupo != null && listaTopicosGrupo.size() > 0) {
            adapterTopicosGrupoPublico.limparTopicosFiltrados();
        }
    }

    private void inicializandoComponentes() {
        toolbarGruposPublicos = findViewById(R.id.toolbarGruposPublicos);
        imgBtnBackGruposPublicos = findViewById(R.id.imgBtnBackGruposPublicos);
        recyclerTopicosGrupo = findViewById(R.id.recyclerTopicosGrupo);
        btnFiltrarGrupos = findViewById(R.id.btnFiltrarGrupos);
        recyclerGruposPublicos = findViewById(R.id.recyclerGruposPublicos);
        frameGruposPublicos = findViewById(R.id.frameGruposPublicos);
        materialSearch = findViewById(R.id.materialSearchGruposPublicos);
    }
}