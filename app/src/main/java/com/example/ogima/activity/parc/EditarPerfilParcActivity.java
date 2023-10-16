package com.example.ogima.activity.parc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.activity.parc.EdicaoGeralParcActivity;
import com.example.ogima.adapter.AdapterFotosPerfilParcEdicao;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ItemTouchHelperCallback;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;

public class EditarPerfilParcActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerViewFotos;
    private LinearLayoutManager linearLayoutManager;
    private Usuario usuarioOriginal;
    private AdapterFotosPerfilParcEdicao adapterFotosPerfilParc;
    private GridLayoutManager gridLayoutManagerFoto;
    private TextView txtViewNameEditParc, txtViewAlvoExibicaoPerfil,
            txtViewOrientacao;
    private LinearLayout linearLayoutHobbies;
    private Button btnEditarFotosParc, btnEditarHobbiesParc, btnEditarIdsEscondidosParc;
    private ImageButton imgBtnEditNomeParc, imgBtnEditExibirParaParc,
            imgBtnEditOrientacaoParc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_parc);
        inicializandoComponentes();

        imgBtnEditNomeParc.setOnClickListener(this);
        imgBtnEditExibirParaParc.setOnClickListener(this);
        imgBtnEditOrientacaoParc.setOnClickListener(this);
        btnEditarHobbiesParc.setOnClickListener(this);
        btnEditarFotosParc.setOnClickListener(this);

        usuarioOriginal = new Usuario();

        Bundle dados = getIntent().getExtras();

        if (dados != null && dados.containsKey("usuarioParc")) {
            usuarioOriginal = (Usuario) dados.getSerializable("usuarioParc");
            txtViewNameEditParc.setText(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioOriginal.getNomeParc()));
            txtViewAlvoExibicaoPerfil.setText(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioOriginal.getExibirPerfilPara()));
            txtViewOrientacao.setText(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioOriginal.getOrientacaoSexual()));
            configRecyclerView();
            exibirHobbies();
            btnEditarIdsEscondidosParc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irParaEdicao("idsEsconder");
                }
            });
        }
    }

    private void configRecyclerView() {
        if (gridLayoutManagerFoto == null) {
            for(String foto : usuarioOriginal.getFotosParc()){
                Log.d("fotoParc", foto);
            }
            gridLayoutManagerFoto =  new GridLayoutManager(getApplicationContext(), 2); // Número de colunas

            recyclerViewFotos.setHasFixedSize(true);
            recyclerViewFotos.setLayoutManager(gridLayoutManagerFoto);

            if (recyclerViewFotos.getOnFlingListener() == null) {
                PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                pagerSnapHelper.attachToRecyclerView(recyclerViewFotos);
            }

            if (adapterFotosPerfilParc == null) {
                adapterFotosPerfilParc = new AdapterFotosPerfilParcEdicao(getApplicationContext(),
                        usuarioOriginal.getFotosParc());
                recyclerViewFotos.setAdapter(adapterFotosPerfilParc);
                ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapterFotosPerfilParc);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(recyclerViewFotos);
                adapterFotosPerfilParc.notifyDataSetChanged();
            }
        }
    }

    private void exibirHobbies(){
        // Adiciona um chip para cada hobby
        for (String hobby : usuarioOriginal.getListaInteressesParc()) {
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

    private void inicializandoComponentes() {
        recyclerViewFotos = findViewById(R.id.recyclerViewFotosEditParc);
        txtViewNameEditParc = findViewById(R.id.txtViewNameEditParc);
        txtViewAlvoExibicaoPerfil = findViewById(R.id.textViewEditExibirPerfilPara);
        txtViewOrientacao = findViewById(R.id.textViewOrientacaoEditParc);
        linearLayoutHobbies = findViewById(R.id.linearLayoutHobbiesEditParc);

        btnEditarFotosParc = findViewById(R.id.btnEditarFotosParc);
        btnEditarHobbiesParc = findViewById(R.id.btnEditarHobbiesParc);
        imgBtnEditNomeParc = findViewById(R.id.imgBtnEditNomeParc);
        imgBtnEditExibirParaParc = findViewById(R.id.imgBtnEditExibirParaParc);
        imgBtnEditOrientacaoParc = findViewById(R.id.imgBtnEditOrientacaoParc);
        btnEditarIdsEscondidosParc = findViewById(R.id.btnEditarIdsEscondidosParc);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgBtnEditNomeParc:
                irParaEdicao("nome");
                break;
            case R.id.imgBtnEditExibirParaParc:
                irParaEdicao("exibirPara");
                break;
            case R.id.imgBtnEditOrientacaoParc:
                irParaEdicao("orientacao");
                break;
            case R.id.btnEditarHobbiesParc:
                irParaEdicao("interesses");
                break;
            case R.id.btnEditarFotosParc:
                irParaEdicao("fotos");
                break;
        }
    }

    private void irParaEdicao(String telaEdicao){
        Intent intent = new Intent(getApplicationContext(), EdicaoGeralParcActivity.class);
        intent.putExtra("tipoEdicao", telaEdicao);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}