package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.DadosUserPadrao;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Grupo;
import com.google.android.material.chip.Chip;

public class DetalhesGrupoActivity extends AppCompatActivity {

    private Toolbar toolbarDetalhesGrupo;
    private ImageButton imgBtnBackDetalhesGrupo, imgBtnConfigsDetalhesGrupo;
    private ImageView imgViewFotoGrupoDetalhes;
    private TextView txtViewNomeGrupoDetalhes, txtViewPrivacidadeGrupoDetalhes,
            txtViewDescricaoGrupoDetalhes, txtViewNrParticipantesGrupoDetalhes;
    private Grupo grupoAtual;
    private LinearLayout linearLayoutTopicosGrupo;
    private RecyclerView recyclerViewParticipantesGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_grupo);
        inicializandoComponentes();
        setSupportActionBar(toolbarDetalhesGrupo);
        setTitle("");

        eventosClickListeners();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("grupoAtual")) {
                grupoAtual = (Grupo) dados.getSerializable("grupoAtual");

                VerificaEpilpesia.verificarEpilpesiaSelecionadoGrupo(getApplicationContext(),
                        grupoAtual, imgViewFotoGrupoDetalhes);

                txtViewNomeGrupoDetalhes.setText(grupoAtual.getNomeGrupo());
                ToastCustomizado.toastCustomizadoCurto("Nome - " + grupoAtual.getNomeGrupo(), getApplicationContext());

                exibirTopicos();
            }
        }

        if (grupoAtual.getGrupoPublico()) {
            txtViewPrivacidadeGrupoDetalhes.setText("Público");
        } else {
            txtViewPrivacidadeGrupoDetalhes.setText("Particular");
        }

        txtViewDescricaoGrupoDetalhes.setText(grupoAtual.getDescricaoGrupo());
        txtViewNrParticipantesGrupoDetalhes.setText("" + grupoAtual.getParticipantes().size());
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void exibirTopicos() {
        // Itere sobre a lista de hobbies e crie um chip para cada um
        for (String topico : grupoAtual.getTopicos()) {
            Chip chip = new Chip(this);
            chip.setText(topico);
            chip.setChipBackgroundColorResource(R.color.chip_background_color);
            chip.setChipCornerRadiusResource(R.dimen.chip_corner_radius);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 8, 8, 8);
            chip.setLayoutParams(layoutParams);
            linearLayoutTopicosGrupo.addView(chip);
        }

        linearLayoutTopicosGrupo.setOrientation(LinearLayout.HORIZONTAL);
    }

    private void eventosClickListeners() {
        imgBtnBackDetalhesGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        toolbarDetalhesGrupo = findViewById(R.id.toolbarDetalhesGrupo);
        imgBtnBackDetalhesGrupo = findViewById(R.id.imgBtnBackDetalhesGrupo);
        imgBtnConfigsDetalhesGrupo = findViewById(R.id.imgBtnConfigsDetalhesGrupo);
        imgViewFotoGrupoDetalhes = findViewById(R.id.imgViewFotoGrupoDetalhes);
        txtViewNomeGrupoDetalhes = findViewById(R.id.txtViewNomeGrupoDetalhes);
        linearLayoutTopicosGrupo = findViewById(R.id.linearLayoutTopicosGrupo);
        txtViewPrivacidadeGrupoDetalhes = findViewById(R.id.txtViewPrivacidadeGrupoDetalhes);
        txtViewDescricaoGrupoDetalhes = findViewById(R.id.txtViewDescricaoGrupoDetalhes);
        txtViewNrParticipantesGrupoDetalhes = findViewById(R.id.txtViewNrParticipantesGrupoDetalhes);
        recyclerViewParticipantesGrupo = findViewById(R.id.recyclerViewParticipantesGrupoDetalhes);
    }
}