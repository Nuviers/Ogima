package com.example.ogima.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.CustomBottomSheetDialogFragment;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.google.firebase.database.DatabaseReference;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class AllCommunitiesActivity extends AppCompatActivity implements CustomBottomSheetDialogFragment.RecuperarFiltrosCallback {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private CommunityUtils communityUtils;
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private RecyclerView recyclerView;
    private String tipoComunidade = "";
    private String idUsuario = "";
    private LinearLayoutManager linearLayoutManager;
    private static final long limite = 900;
    private CustomBottomSheetDialogFragment bottomSheetDialogFragment;

    private interface ConfigInicialCallback {
        void onConcluido();
    }

    public AllCommunitiesActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        inicializarComponentes();
        if (idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        configInicial(new ConfigInicialCallback() {
            @Override
            public void onConcluido() {
                UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                    @Override
                    public void onConcluido(boolean epilepsia) {
                       configRecycler(epilepsia);
                        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showBottomSheetDialog();
                            }
                        });
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
        });
    }

    private void configInicial(ConfigInicialCallback callback) {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        Bundle dados = getIntent().getExtras();
        if (dados == null || !dados.containsKey("tipoComunidade")) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade.", getApplicationContext());
            onBackPressed();
            return;
        }
        tipoComunidade = dados.getString("tipoComunidade");
        if (tipoComunidade == null || tipoComunidade.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade.", getApplicationContext());
            onBackPressed();
            return;
        }
        communityUtils = new CommunityUtils(getApplicationContext());
        txtViewTitleToolbar.setText(tipoComunidade);
        callback.onConcluido();
    }

    private void configRecycler(boolean epilepsia) {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            //**recyclerView.setAdapter();
        }
    }

    private void showBottomSheetDialog() {
        bottomSheetDialogFragment = new CustomBottomSheetDialogFragment(this);
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    @Override
    public void onRecuperado(ArrayList<String> listaFiltrosRecuperados) {
         for(String conteudo : listaFiltrosRecuperados){
             ToastCustomizado.toastCustomizadoCurto("Filtro: " + conteudo, getApplicationContext());
         }
    }

    @Override
    public void onSemFiltros() {
        ToastCustomizado.toastCustomizado("É necessário selecionar pelo menos um tópico para filtrar as comunidades.", getApplicationContext());
    }

    private void inicializarComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        recyclerView = findViewById(R.id.recyclerViewComunidades);
    }
}