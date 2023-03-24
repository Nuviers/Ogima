package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterParticipantesGrupo;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DadosUserPadrao;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DetalhesGrupoActivity extends AppCompatActivity {

    private Toolbar toolbarDetalhesGrupo;
    private ImageButton imgBtnBackDetalhesGrupo, imgBtnConfigsDetalhesGrupo;
    private ImageView imgViewFotoGrupoDetalhes, imgViewFundadorGrupoDetalhes;
    private TextView txtViewNomeGrupoDetalhes, txtViewPrivacidadeGrupoDetalhes,
            txtViewDescricaoGrupoDetalhes, txtViewNrParticipantesGrupoDetalhes,
            txtViewNomeFundadorGrupo, txtViewNrAdmsGrupoDetalhes;
    private Grupo grupoAtual;
    private LinearLayout linearLayoutTopicosGrupo;
    private RecyclerView recyclerViewParticipantesGrupo, recyclerViewAdmsGrupo;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private AdapterParticipantesGrupo adapterParticipantesGrupo;
    private AdapterParticipantesGrupo adapterParticipantesAdms;
    private HashSet<String> participantesGrupo = new HashSet<>();

    private List<Usuario> listaParticipantes = new ArrayList<>();
    private List<Usuario> listaAdms = new ArrayList<>();
    private List<String> listaAdmsAdicaoTeste = new ArrayList<>();

    private Button btnEditarGrupo, btnDeletarGrupo, btnSairDoGrupo;

    private LinearLayout linearLayoutAdmsDetalhes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_grupo);
        inicializandoComponentes();
        setSupportActionBar(toolbarDetalhesGrupo);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("grupoAtual")) {
                grupoAtual = (Grupo) dados.getSerializable("grupoAtual");

                /*
                listaAdmsAdicaoTeste.add("Z2Vuc2hpbmZlckBvdXRsb29rLmNvbQ==");
                listaAdmsAdicaoTeste.add("ZWxpc2FiZW5lZGV0MjAyMkBnbWFpbC5jb20=");
                DatabaseReference salvarAdmsRef = firebaseRef.child("grupos")
                                .child(grupoAtual.getIdGrupo()).child("admsGrupo");
                salvarAdmsRef.setValue(listaAdmsAdicaoTeste);
                 */
                eventosClickListeners();
                detalhesGrupo();
            }
        }
    }


    private void detalhesGrupo() {
        VerificaEpilpesia.verificarEpilpesiaSelecionadoGrupo(getApplicationContext(),
                grupoAtual, imgViewFotoGrupoDetalhes);

        txtViewNomeGrupoDetalhes.setText(grupoAtual.getNomeGrupo());
        ToastCustomizado.toastCustomizadoCurto("Nome - " + grupoAtual.getNomeGrupo(), getApplicationContext());

        configuracaoRecyclerView();

        exibirTopicos();

        if (grupoAtual.getGrupoPublico()) {
            txtViewPrivacidadeGrupoDetalhes.setText("Público");
        } else {
            txtViewPrivacidadeGrupoDetalhes.setText("Particular");
        }

        txtViewDescricaoGrupoDetalhes.setText(grupoAtual.getDescricaoGrupo());
        txtViewNrParticipantesGrupoDetalhes.setText("" + grupoAtual.getParticipantes().size() + "/" + "40");

        dadosFundadorGrupo();

        if (grupoAtual.getAdmsGrupo() != null) {
            if (grupoAtual.getAdmsGrupo().size() > 0) {
                linearLayoutAdmsDetalhes.setVisibility(View.VISIBLE);
                txtViewNrAdmsGrupoDetalhes.setText("" + grupoAtual.getAdmsGrupo().size() + "/" + "5");
            }else{
                linearLayoutAdmsDetalhes.setVisibility(View.GONE);
            }
        }else{
            linearLayoutAdmsDetalhes.setVisibility(View.GONE);
        }
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

        if (!grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {
            //Caso o usuário não seja o fundador do grupo ele não pode excluir o grupo.
            btnDeletarGrupo.setVisibility(View.GONE);
            btnEditarGrupo.setVisibility(View.GONE);
        }else{
            btnDeletarGrupo.setVisibility(View.VISIBLE);
            btnEditarGrupo.setVisibility(View.VISIBLE);
        }

        imgBtnBackDetalhesGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnEditarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CriarGrupoActivity.class);
                intent.putExtra("grupoEdicao", grupoAtual);
                intent.putExtra("listaEdicaoParticipantes", (Serializable) listaParticipantes);
                startActivity(intent);
                finish();
            }
        });
    }

    private void dadosFundadorGrupo() {
        DatabaseReference dadosFundadorRef = firebaseRef.child("usuarios")
                .child(grupoAtual.getIdSuperAdmGrupo());

        dadosFundadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioFundador = snapshot.getValue(Usuario.class);
                    DadosUserPadrao.preencherDadosUser(getApplicationContext(),
                            usuarioFundador, txtViewNomeFundadorGrupo,
                            imgViewFundadorGrupoDetalhes);
                }
                dadosFundadorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configuracaoRecyclerView() {

        //Todos participantes
        configRecyclerParticipantes();

        //Somente adms
        if (grupoAtual.getAdmsGrupo() != null) {
            if (grupoAtual.getAdmsGrupo().size() > 0) {
                linearLayoutAdmsDetalhes.setVisibility(View.VISIBLE);
                configRecyclerAdms();
            }else{
                linearLayoutAdmsDetalhes.setVisibility(View.GONE);
            }
        }else{
            linearLayoutAdmsDetalhes.setVisibility(View.GONE);
        }
    }

    private void configRecyclerParticipantes() {
        for (String todosParticipantes : grupoAtual.getParticipantes()) {
            DatabaseReference verificaParticipanteRef = firebaseRef.child("usuarios")
                    .child(todosParticipantes);
            verificaParticipanteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioParticipante = snapshot.getValue(Usuario.class);
                        //ToastCustomizado.toastCustomizadoCurto("Nome user " + usuarioParticipante.getNomeUsuario(), getApplicationContext());
                        listaParticipantes.add(usuarioParticipante);
                        adapterParticipantesGrupo.notifyDataSetChanged();
                    }
                    verificaParticipanteRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewParticipantesGrupo.setLayoutManager(linearLayoutManager);
        recyclerViewParticipantesGrupo.setHasFixedSize(true);

        if (adapterParticipantesGrupo != null) {

        } else {
            adapterParticipantesGrupo = new AdapterParticipantesGrupo(null, getApplicationContext(), grupoAtual, true, listaParticipantes);
        }
        recyclerViewParticipantesGrupo.setAdapter(adapterParticipantesGrupo);
    }

    private void configRecyclerAdms() {
        //Somente administradores
        for (String todosAdms : grupoAtual.getAdmsGrupo()) {
            if (!todosAdms.equals(grupoAtual.getIdSuperAdmGrupo())) {
                DatabaseReference verificaAdmRef = firebaseRef.child("usuarios")
                        .child(todosAdms);
                verificaAdmRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioAdm = snapshot.getValue(Usuario.class);
                            listaAdms.add(usuarioAdm);
                            adapterParticipantesAdms.notifyDataSetChanged();
                        }
                        verificaAdmRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

        LinearLayoutManager linearLayoutManagerAdms = new LinearLayoutManager(getApplicationContext());
        linearLayoutManagerAdms.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewAdmsGrupo.setLayoutManager(linearLayoutManagerAdms);
        recyclerViewAdmsGrupo.setHasFixedSize(true);

        if (adapterParticipantesAdms != null) {

        } else {
            adapterParticipantesAdms = new AdapterParticipantesGrupo(null, getApplicationContext(), grupoAtual, true, listaAdms);
        }
        recyclerViewAdmsGrupo.setAdapter(adapterParticipantesAdms);
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
        imgViewFundadorGrupoDetalhes = findViewById(R.id.imgViewFundadorGrupoDetalhes);
        txtViewNomeFundadorGrupo = findViewById(R.id.txtViewNomeFundadorGrupoDetalhes);
        txtViewNrAdmsGrupoDetalhes = findViewById(R.id.txtViewNrAdmsGrupoDetalhes);
        recyclerViewAdmsGrupo = findViewById(R.id.recyclerViewAdmsGrupoDetalhes);
        btnEditarGrupo = findViewById(R.id.btnEditarGrupo);
        btnDeletarGrupo = findViewById(R.id.btnDeletarGrupo);
        btnSairDoGrupo = findViewById(R.id.btnSairDoGrupo);
        linearLayoutAdmsDetalhes = findViewById(R.id.linearLayoutAdmsDetalhes);
    }
}