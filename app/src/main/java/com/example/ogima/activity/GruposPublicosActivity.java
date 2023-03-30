package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterTopicosGrupoPublico;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;

public class GruposPublicosActivity extends AppCompatActivity {

    private Toolbar toolbarGruposPublicos;
    private ImageButton imgBtnBackGruposPublicos;
    private RecyclerView recyclerTopicosGrupo;
    //Mudar para o adapter que vai exibir os grupos de acordo com o filtro selecionado.
    private AdapterTopicosGrupoPublico adapterTopicosGrupoPublico;
    private String[] topicosGrupo = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    private ArrayList<String> listaTopicosGrupo = new ArrayList<>();
    private ChipGroup chipGroupTopicosGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos_publicos);
        inicializandoComponentes();
        setSupportActionBar(toolbarGruposPublicos);
        setTitle("");

        listaTopicosGrupo.addAll(Arrays.asList(topicosGrupo));
        configRecyclerView();
        clickListeners();

        for (String topicos : listaTopicosGrupo) {
            Chip chip = new Chip(this);
            chip.setText(topicos);
            chip.setCheckable(true);
            //chip.setChipBackgroundColor(ColorStateList.valueOf(Color.GRAY));
            chipGroupTopicosGrupo.addView(chip);
        }
    }

    private void configRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerTopicosGrupo.setHasFixedSize(true);
        recyclerTopicosGrupo.setLayoutManager(linearLayoutManager);
        if (adapterTopicosGrupoPublico != null) {

        }else{
            adapterTopicosGrupoPublico = new AdapterTopicosGrupoPublico(getApplicationContext(), listaTopicosGrupo);
        }
        recyclerTopicosGrupo.setAdapter(adapterTopicosGrupoPublico);
        adapterTopicosGrupoPublico.notifyDataSetChanged();
    }

    private void clickListeners() {

        imgBtnBackGruposPublicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        toolbarGruposPublicos = findViewById(R.id.toolbarGruposPublicos);
        imgBtnBackGruposPublicos = findViewById(R.id.imgBtnBackGruposPublicos);
        recyclerTopicosGrupo = findViewById(R.id.recyclerTopicosGrupo);
        chipGroupTopicosGrupo = findViewById(R.id.chipGroupTopicosGrupo);
    }
}