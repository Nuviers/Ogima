package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

public class DailyShortsActivity extends AppCompatActivity {

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private ChipGroup chipGroupDailyShorts;
    private Chip chipDailyShortsFavoritos, chipDailyShortsAmigos,
            chipDailyShortsSeguidores, chipDailyShortsSeguindo;
    private MaterialSearchView materialSearchDailyShorts;
    private RecyclerView recyclerViewDailyShorts;

    @Override
    protected void onStart() {
        super.onStart();

        recuperarDailyShorts();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_shorts);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");

    }

    private void recuperarDailyShorts() {

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
}