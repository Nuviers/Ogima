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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterConviteComunidade;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Convite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ConvitesComunidadeActivity extends AppCompatActivity implements AdapterConviteComunidade.RemocaoComunidadeConviteListener, AdapterConviteComunidade.RecuperaPosicaoConviteListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private FrameLayout frameConviteComunidade;
    private Toolbar toolbarConviteComunidade;
    private ImageButton imgBtnBackConviteComunidade;
    private TextView txtTituloToolbarConviteComunidade;
    private RecyclerView recyclerConviteComunidade;
    private MaterialSearchView materialSearchView;
    private LinearLayoutManager linearLayoutManagerConvite;

    private List<Comunidade> listaComunidades = new ArrayList<>();
    private Query comunidadeRef;
    private Query convitesRef;
    private ComunidadeDiffDAO comunidadeDiffDAO;
    private ChildEventListener childEventListener;
    private AdapterConviteComunidade adapterConviteComunidade;

    //Retorna para posição anterior
    private int mCurrentPosition = 0;

    private Boolean pesquisaAtivada = false;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchview_grupo_publico, menu);
        MenuItem item = menu.findItem(R.id.menu_icon_search_grupo_publico);
        materialSearchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 && mCurrentPosition > 0) {
            recyclerConviteComunidade.scrollToPosition(mCurrentPosition);
            mCurrentPosition = 0;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        configRecyclerView();

        comunidadeDiffDAO = new ComunidadeDiffDAO(listaComunidades, adapterConviteComunidade);

        recuperaConvite(null);

        configurarMaterialSearchView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (materialSearchView != null) {
            materialSearchView.closeSearch();
        }

        if (childEventListener != null) {
            comunidadeRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
        comunidadeDiffDAO.limparListaComunidades();
    }

    private void configRecyclerView() {
        //Configuração do recycler de comunidades
        if (linearLayoutManagerConvite != null) {

        } else {
            linearLayoutManagerConvite = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerConvite.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerConviteComunidade.setHasFixedSize(true);
        recyclerConviteComunidade.setLayoutManager(linearLayoutManagerConvite);

        if (adapterConviteComunidade != null) {

        } else {
            adapterConviteComunidade = new AdapterConviteComunidade(getApplicationContext(), listaComunidades, this, this);
        }
        recyclerConviteComunidade.setAdapter(adapterConviteComunidade);
    }

    private void recuperaConvite(String nomeBuscado) {

        if (childEventListener != null) {
            comunidadeRef.removeEventListener(childEventListener);
            childEventListener = null;
        }

        if (nomeBuscado != null) {
            pesquisaAtivada = true;
        } else {
            pesquisaAtivada = false;
        }

        limparListaComunidade();

        ArrayList<String> listaIdsComunidade = new ArrayList<>();

        convitesRef = firebaseRef.child("convitesComunidade")
                .child(idUsuario);

        convitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Convite convite = snapshot1.getValue(Convite.class);
                    listaIdsComunidade.add(convite.getIdComunidade());
                }
                for (String idComunidade : listaIdsComunidade) {
                    recuperarComunidade(idComunidade, nomeBuscado);
                }

                convitesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarComunidade(String idComunidade, String nomeBuscado) {

        if (pesquisaAtivada) {
            comunidadeRef = firebaseRef.child("comunidades")
                    .orderByChild("nomeComunidade")
                    .startAt(nomeBuscado)
                    .endAt(nomeBuscado + "\uf8ff");
        } else {
            comunidadeRef = firebaseRef.child("comunidades")
                    .orderByChild("idDestinatario");
        }

        childEventListener = comunidadeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Comunidade novoComunidade = snapshot.getValue(Comunidade.class);

                    if (!novoComunidade.getIdComunidade().equals(idComunidade)) {
                        //Ignorar comunidades que tenham vinculo com o convite.
                    } else {
                        // Adiciona o comunidade na lista mantendo a ordenação
                        comunidadeDiffDAO.adicionarComunidade(novoComunidade);

                        // Notifica o adapter das mudanças usando o DiffUtil
                        adapterConviteComunidade.updateComunidadeConviteList(listaComunidades);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    //ToastCustomizado.toastCustomizadoCurto("Atualizado",getApplicationContext());

                    // Recupera o comunidade do snapshot
                    Comunidade comunidadeAtualizado = snapshot.getValue(Comunidade.class);

                    // Atualiza o comunidade na lista mantendo a ordenação
                    comunidadeDiffDAO.atualizarComunidade(comunidadeAtualizado);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterConviteComunidade.updateComunidadeConviteList(listaComunidades);
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
                    adapterConviteComunidade.updateComunidadeConviteList(listaComunidades);
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

    private void configurarMaterialSearchView() {

        configFocoComponentes();

        materialSearchView.setHint("Pesquisar comunidades");
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
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
                    recuperaConvite(dadoDigitado);
                }
                return true;
            }
        });
    }

    private void configFocoComponentes() {
        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                recuperaConvite(null);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convites_comunidade);
        inicializandoComponentes();
        setSupportActionBar(toolbarConviteComunidade);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    private void inicializandoComponentes() {
        toolbarConviteComunidade = findViewById(R.id.toolbarConviteComunidade);
        imgBtnBackConviteComunidade = findViewById(R.id.imgBtnBackConviteComunidade);
        recyclerConviteComunidade = findViewById(R.id.recyclerConviteComunidade);
        frameConviteComunidade = findViewById(R.id.frameConviteComunidade);
        materialSearchView = findViewById(R.id.materialSearchConviteComunidade);
        txtTituloToolbarConviteComunidade = findViewById(R.id.txtTituloToolbarConviteComunidade);
    }

    private void limparListaComunidade() {
        if (listaComunidades != null && listaComunidades.size() > 0) {
            comunidadeDiffDAO.limparListaComunidades();
            adapterConviteComunidade.updateComunidadeConviteList(listaComunidades);
        }
    }

    @Override
    public void onComunidadeExcluida(Comunidade comunidadeRemovida) {
        comunidadeDiffDAO.removerComunidade(comunidadeRemovida);
        Log.d("TESTE-On", "Comunidade removida com sucesso");

        // Notifica o adapter das mudanças usando o DiffUtil
        adapterConviteComunidade.updateComunidadeConviteList(listaComunidades);
        Log.d("TESTE-On Child Removed", "Adapter notificado com sucesso");
    }

    @Override
    public void onRecuperaPosicao(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }
}