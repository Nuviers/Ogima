package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGruposBloqueados;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GrupoBloqueadoDAO;
import com.example.ogima.model.Grupo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class GruposBloqueadosActivity extends AppCompatActivity implements AdapterGruposBloqueados.RemocaoGrupoBloqueadoListener {

    private Toolbar toolbarGruposBloqueados;
    private ImageButton imgBtnBackGruposBloqueados;
    private RecyclerView recyclerGruposBloqueados;
    private AdapterGruposBloqueados adapterGruposBloqueados;
    private List<Grupo> listaGruposBloqueados = new ArrayList<>();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private GrupoBloqueadoDAO grupoBloqueadoDAO;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(GruposBloqueadosActivity.this, EditarPerfilActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        clickListeners();
        configRecyclerView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        grupoBloqueadoDAO.limparListaGrupoBloqueado();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos_bloqueados);
        inicializandoComponentes();
        setSupportActionBar(toolbarGruposBloqueados);
        setTitle("");
    }


    private void configRecyclerView() {
        if (linearLayoutManager != null) {

        } else {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerGruposBloqueados.setHasFixedSize(true);
        recyclerGruposBloqueados.setLayoutManager(linearLayoutManager);

        if (adapterGruposBloqueados != null) {

        } else {
            adapterGruposBloqueados = new AdapterGruposBloqueados(getApplicationContext(), listaGruposBloqueados, this);
        }

        grupoBloqueadoDAO = new GrupoBloqueadoDAO(adapterGruposBloqueados, listaGruposBloqueados, getApplicationContext());

        recyclerGruposBloqueados.setAdapter(adapterGruposBloqueados);
        adapterGruposBloqueados.notifyDataSetChanged();
    }

    private void clickListeners() {
        imgBtnBackGruposBloqueados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        toolbarGruposBloqueados = findViewById(R.id.toolbarGruposBloqueados);
        imgBtnBackGruposBloqueados = findViewById(R.id.imgBtnBackGruposBloqueados);
        recyclerGruposBloqueados = findViewById(R.id.recyclerGruposBloqueados);
    }

    @Override
    public void onGrupoExcluido(Grupo grupoRemovido) {
        grupoBloqueadoDAO.removerGrupoBloqueado(grupoRemovido);
    }
}