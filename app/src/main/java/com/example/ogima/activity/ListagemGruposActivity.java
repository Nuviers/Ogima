package com.example.ogima.activity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.example.ogima.adapter.AdapterLstcTitleHeader;
import com.example.ogima.adapter.AdapterPreviewCommunity;
import com.example.ogima.adapter.AdapterPreviewGroup;
import com.example.ogima.adapter.AdapterTesteFirebaseUi;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDiffDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.GrupoDiffDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ListagemGruposActivity extends AppCompatActivity {

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

    public ListagemGruposActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        grupoComparator = new Grupo(false, true);
    }

    public interface VerificaBlockCallback {
        void onAjustado(Grupo grupoAjustado);
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
        //**removeValueEventListenerMy();
        //**removeValueEventListenerBlocked();
        //**removeValueEventListenerFollowing();
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
    }

    private void inicializandoComponentes(){
        recyclerView = findViewById(R.id.recyclerViewListCommunity);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
    }
}