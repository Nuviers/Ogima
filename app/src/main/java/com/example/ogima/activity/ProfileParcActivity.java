package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterDailyShortsSelecao;
import com.example.ogima.adapter.AdapterFotosPerfilParc;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;

public class ProfileParcActivity extends AppCompatActivity {

    private ImageView imgViewFoto;
    private TextView txtViewName;
    private Usuario usuarioParc;
    private Button btnEditarPerfilParc;
    private LinearLayout linearLayoutHobbies;
    private RecyclerView recyclerViewFotos;
    private LinearLayoutManager linearLayoutManager;
    private AdapterFotosPerfilParc adapterFotosPerfilParc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_parc);
        inicializandoComponentes();

        usuarioParc = new Usuario();

        Bundle dados = getIntent().getExtras();

        if (dados.containsKey("usuarioParc")) {
            usuarioParc = (Usuario) dados.getSerializable("usuarioParc");
            GlideCustomizado.loadUrl(getApplicationContext(),
                    usuarioParc.getFotosParc().get(0).toString(),
                    imgViewFoto,
                    android.R.color.transparent,
                    GlideCustomizado.CIRCLE_CROP, false, true);
            txtViewName.setText(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioParc.getNomeParc()));
            exibirHobbies();
            configRecyclerView();
            btnEditarPerfilParc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ProfileParcActivity.this, EditarPerfilParcActivity.class);
                    intent.putExtra("usuarioParc",usuarioParc);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }
    }

    private void exibirHobbies(){
        // Adiciona um chip para cada hobby
        for (String hobby : usuarioParc.getListaInteressesParc()) {
            Chip chip = new Chip(linearLayoutHobbies.getContext());
            chip.setText(hobby);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.DKGRAY));
            chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
            chip.setLayoutParams(params);
            chip.setClickable(false);
            linearLayoutHobbies.addView(chip);
        }
    }

    private void configRecyclerView() {
        if (linearLayoutManager == null) {
            for(String foto : usuarioParc.getFotosParc()){
                Log.d("fotoParc", foto);
            }
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewFotos.setHasFixedSize(true);
            recyclerViewFotos.setLayoutManager(linearLayoutManager);

            if (recyclerViewFotos.getOnFlingListener() == null) {
                PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                pagerSnapHelper.attachToRecyclerView(recyclerViewFotos);
            }

            if (adapterFotosPerfilParc == null) {
                adapterFotosPerfilParc = new AdapterFotosPerfilParc(getApplicationContext(),
                        usuarioParc.getFotosParc());
                recyclerViewFotos.setAdapter(adapterFotosPerfilParc);
                adapterFotosPerfilParc.notifyDataSetChanged();
            }
        }
    }

    private void inicializandoComponentes() {
        imgViewFoto = findViewById(R.id.imgViewFotoPerfilParc);
        txtViewName = findViewById(R.id.txtViewNamePerfilParc);
        btnEditarPerfilParc = findViewById(R.id.btnEditarPerfilParc);
        linearLayoutHobbies = findViewById(R.id.linearLayoutHobbiesParc);
        recyclerViewFotos = findViewById(R.id.recyclerViewFotosPerfilParc);
    }
}