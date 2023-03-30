package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DetalhesGrupoActivity extends AppCompatActivity implements View.OnClickListener {

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

    private List<Usuario> listaParticipantes = new ArrayList<>();
    private List<Usuario> listaAtualizadaParticipantes = new ArrayList<>();
    private List<Usuario> listaAdms = new ArrayList<>();
    //listaUsersPromocao - Somente participantes que ainda não são adms.
    private List<Usuario> listaUsersPromocao = new ArrayList<>();

    private List<Usuario> listaUsersAdicao = new ArrayList<>();
    private HashSet<Usuario> hashSetUsersAdicao = new HashSet<>();

    private Button btnEditarGrupo, btnDeletarGrupo, btnSairDoGrupo,
            btnGerenciarUsuarios;

    private LinearLayout linearLayoutAdmsDetalhes;
    private BottomSheetDialog bottomSheetDialogGerenciar, bottomSheetDialogSairDoGrupo;
    //Dialog
    private Button btnViewAddUserGrupo, btnViewRemoverUserGrupo, btnViewPromoverUserGrupo,
            btnViewDespromoverUserGrupo;

    private DatabaseReference grupoAtualRef;
    private ArrayList<String> listaUsuarioAtualRemovido = new ArrayList<>();

    //Componentes bottomSheetDialogSairDoGrupo
    private TextView txtViewEscolherFundador, txtViewFundadorAleatorio, txtViewCancelarSaida;

    private AlertDialog.Builder builderExclusao;
    private AlertDialog dialogExclusao;
    private StorageReference storageRef;
    private StorageReference imagemRef;
    private ProgressDialog progressDialog;
    private DatabaseReference adicionaMsgExclusaoRef;
    private String idConversaGrupo;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(DetalhesGrupoActivity.this, ConversaGrupoActivity.class);
        intent.putExtra("grupo", grupoAtual);
        intent.putExtra("voltarChatFragment", "ChatInicioActivity.class");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bottomSheetDialogGerenciar != null && bottomSheetDialogGerenciar.isShowing()) {
            bottomSheetDialogGerenciar.dismiss();
        }

        if (bottomSheetDialogSairDoGrupo != null && bottomSheetDialogSairDoGrupo.isShowing()) {
            bottomSheetDialogSairDoGrupo.dismiss();
        }

        if (dialogExclusao != null && dialogExclusao.isShowing()) {
            dialogExclusao.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_grupo);
        inicializandoComponentes();
        setSupportActionBar(toolbarDetalhesGrupo);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("grupoAtual")) {
                grupoAtual = (Grupo) dados.getSerializable("grupoAtual");
                grupoAtualRef = firebaseRef.child("grupos").child(grupoAtual.getIdGrupo());
                builderExclusao = new AlertDialog.Builder(this);

                recuperarContato();
                recuperarConversa();
                configurarBottomSheetDialog();
                eventosClickListeners();
                detalhesGrupo();
            }
        }
    }

    private void configurarBottomSheetDialog() {
        bottomSheetDialogGerenciar = new BottomSheetDialog(DetalhesGrupoActivity.this);
        bottomSheetDialogGerenciar.setContentView(R.layout.bottom_sheet_dialog_gerenciar_grupo);

        bottomSheetDialogSairDoGrupo = new BottomSheetDialog(DetalhesGrupoActivity.this);
        bottomSheetDialogSairDoGrupo.setContentView(R.layout.bottom_sheet_sair_do_grupo);
    }


    private void detalhesGrupo() {
        VerificaEpilpesia.verificarEpilpesiaSelecionadoGrupo(getApplicationContext(),
                grupoAtual, imgViewFotoGrupoDetalhes);

        txtViewNomeGrupoDetalhes.setText(grupoAtual.getNomeGrupo());
        //ToastCustomizado.toastCustomizadoCurto("Nome - " + grupoAtual.getNomeGrupo(), getApplicationContext());


        //Todos participantes
        configRecyclerParticipantes();

        if (grupoAtual.getAdmsGrupo() != null && grupoAtual.getAdmsGrupo().size() > 0) {
            //Existe admistrador, então exibir o adapter de adms.
            linearLayoutAdmsDetalhes.setVisibility(View.VISIBLE);
            configRecyclerAdms();
            txtViewNrAdmsGrupoDetalhes.setText("" + grupoAtual.getAdmsGrupo().size() + "/" + "5");
        } else {
            //Oculta o layout de adms, pois não existe nenhum administrador.
            linearLayoutAdmsDetalhes.setVisibility(View.GONE);
        }

        exibirTopicos();

        if (grupoAtual.getGrupoPublico()) {
            txtViewPrivacidadeGrupoDetalhes.setText("Público");
        } else {
            txtViewPrivacidadeGrupoDetalhes.setText("Particular");
        }

        txtViewDescricaoGrupoDetalhes.setText(grupoAtual.getDescricaoGrupo());
        txtViewNrParticipantesGrupoDetalhes.setText("" + grupoAtual.getParticipantes().size() + "/" + "40");

        dadosFundadorGrupo();
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

        if (grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {
            //Usuário atual é o fundador
            btnDeletarGrupo.setVisibility(View.VISIBLE);
            btnEditarGrupo.setVisibility(View.VISIBLE);
            btnGerenciarUsuarios.setVisibility(View.VISIBLE);

            if (grupoAtual.getParticipantes() != null &&
                    grupoAtual.getParticipantes().size() == 1) {
                btnSairDoGrupo.setVisibility(View.GONE);
                btnDeletarGrupo.setText("Sair e excluir grupo");
            }

        } else if (grupoAtual.getAdmsGrupo() != null && grupoAtual.getAdmsGrupo().size() > 0
                && grupoAtual.getAdmsGrupo().contains(idUsuario)) {
            //Administrador pode gerenciar participantes, porém com limitações.
            btnGerenciarUsuarios.setVisibility(View.VISIBLE);
            btnDeletarGrupo.setVisibility(View.GONE);
            btnEditarGrupo.setVisibility(View.GONE);
        } else {
            //Caso o usuário não possua nenhum cargo
            btnDeletarGrupo.setVisibility(View.GONE);
            btnEditarGrupo.setVisibility(View.GONE);
            btnGerenciarUsuarios.setVisibility(View.GONE);
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

        btnGerenciarUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (listaParticipantes != null && listaParticipantes.size() > 0) {

                    for (Usuario usuarioParticipante : listaParticipantes) {

                        if (usuarioParticipante.getIdUsuario().equals(idUsuario)) {
                            //Remove o usuário atual da listagem
                            listaAtualizadaParticipantes.remove(usuarioParticipante);
                            listaUsersPromocao.remove(usuarioParticipante);
                        }

                        if (usuarioParticipante.getIdUsuario().equals(grupoAtual.getIdSuperAdmGrupo())) {
                            //Remove o fundador da listagem
                            listaAtualizadaParticipantes.remove(usuarioParticipante);
                            listaUsersPromocao.remove(usuarioParticipante);
                        }

                        if (!idUsuario.equals(grupoAtual.getIdSuperAdmGrupo())) {
                            if (grupoAtual.getAdmsGrupo() != null && grupoAtual.getAdmsGrupo().size() > 0) {
                                if (grupoAtual.getAdmsGrupo().contains(usuarioParticipante.getIdUsuario())) {
                                    //Caso o usuário atual não seja o fundador, os outros adms não serão listados
                                    listaAtualizadaParticipantes.remove(usuarioParticipante);
                                    listaUsersPromocao.remove(usuarioParticipante);
                                }
                            }
                        }
                    }
                }

                if (listaAtualizadaParticipantes != null && listaAtualizadaParticipantes.size() > 0
                || grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {
                    abrirDialogGerenciamento();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Não existem usuários que possam ser gerenciados no momento", getApplicationContext());
                }
            }
        });

        btnSairDoGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {
                    alertaSairDoGrupo(true);
                } else {
                    alertaSairDoGrupo(false);
                }
            }
        });

        btnDeletarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertaExclusaoGrupo();
            }
        });
    }

    private void irParaTelaInicial() {
        Intent intent = new Intent(DetalhesGrupoActivity.this, NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
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
                        listaAtualizadaParticipantes.add(usuarioParticipante);
                        listaUsersPromocao.add(usuarioParticipante);
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

    private void abrirDialogGerenciamento() {
        bottomSheetDialogGerenciar.show();
        bottomSheetDialogGerenciar.setCancelable(true);

        btnViewAddUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewAddUserGrupo);
        btnViewRemoverUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewRemoverUserGrupo);
        btnViewPromoverUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewPromoverUserGrupo);
        btnViewDespromoverUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewDespromoverUserGrupo);


        if (grupoAtual.getIdSuperAdmGrupo().equals(idUsuario) ||
                grupoAtual.getAdmsGrupo() != null && grupoAtual.getAdmsGrupo().size() > 0
                        && grupoAtual.getAdmsGrupo().contains(idUsuario)) {

            //Lógica somente se o usuário atual é adm ou fundador

            if (listaUsersAdicao != null && listaUsersAdicao.size() > 0) {
                //Existem usuário a serem adicionados
                btnViewAddUserGrupo.setVisibility(View.VISIBLE);
            }

            if (listaAtualizadaParticipantes.size() >= 1) {
                //Existem usuários a serem removidos
                btnViewRemoverUserGrupo.setVisibility(View.VISIBLE);
            }
        }

        if (grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {

            if (listaUsersPromocao != null && listaUsersPromocao.size() > 0
                    && grupoAtual.getAdmsGrupo() != null && grupoAtual.getAdmsGrupo().size() > 0) {
                for (Usuario usuarioAdm : listaAdms) {
                    //Deixa somente usuários que ainda não foram promovidos.
                    listaUsersPromocao.remove(usuarioAdm);
                }
            }

            if (listaUsersPromocao != null && listaUsersPromocao.size() > 0) {
                btnViewPromoverUserGrupo.setVisibility(View.VISIBLE);
            }

            if (grupoAtual.getAdmsGrupo() != null && grupoAtual.getAdmsGrupo().size() > 0) {
                btnViewDespromoverUserGrupo.setVisibility(View.VISIBLE);
            }
        }

        btnViewAddUserGrupo.setOnClickListener(this);
        btnViewRemoverUserGrupo.setOnClickListener(this);
        btnViewPromoverUserGrupo.setOnClickListener(this);
        btnViewDespromoverUserGrupo.setOnClickListener(this);
    }

    private void gerenciarUsuarios(String tipoGerenciamento) {

        Intent intent = new Intent(DetalhesGrupoActivity.this, GerenciarUsersGrupoActivity.class);
        intent.putExtra("grupoAtual", grupoAtual);
        if (tipoGerenciamento.equals("despromover")) {
            intent.putExtra("listaAdms", (Serializable) listaAdms);
        } else if (tipoGerenciamento.equals("promover")) {
            intent.putExtra("listaParticipantes", (Serializable) listaUsersPromocao);
        } else if (tipoGerenciamento.equals("adicionar")) {
            intent.putExtra("listaParticipantes", (Serializable) listaUsersAdicao);
        } else {
            intent.putExtra("listaParticipantes", (Serializable) listaAtualizadaParticipantes);
        }
        intent.putExtra("tipoGerenciamento", tipoGerenciamento);
        startActivity(intent);
        finish();
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
        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarParticipantesGrupo);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnViewAddUserGrupo:
                gerenciarUsuarios("adicionar");
                break;
            case R.id.btnViewRemoverUserGrupo:
                gerenciarUsuarios("remover");
                break;
            case R.id.btnViewPromoverUserGrupo:
                gerenciarUsuarios("promover");
                break;
            case R.id.btnViewDespromoverUserGrupo:
                gerenciarUsuarios("despromover");
                break;
        }
    }

    private void recuperarContato() {
        DatabaseReference recuperaContatosRef = firebaseRef.child("contatos")
                .child(idUsuario);
        recuperaContatosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotContato : snapshot.getChildren()) {
                    if (snapshotContato.getValue() != null) {
                        Contatos contatos = snapshotContato.getValue(Contatos.class);
                        recuperarUsuarios(contatos.getIdContato(), "contato");
                    }
                }
                recuperaContatosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarConversa() {
        DatabaseReference recuperaConversasRef = firebaseRef.child("conversas")
                .child(idUsuario);

        recuperaConversasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotConversa : snapshot.getChildren()) {
                    if (snapshotConversa != null) {
                        recuperarUsuarios(snapshotConversa.getKey(), "conversa");
                    }
                }
                recuperaConversasRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarUsuarios(String idRecebido, String tipoUsuario) {
        DatabaseReference recuperUsuarioRef = firebaseRef.child("usuarios")
                .child(idRecebido);

        recuperUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);
                    if (tipoUsuario.equals("conversa")
                            && usuarioRecuperado.getGruposSomentePorAmigos() != null
                            && usuarioRecuperado.getGruposSomentePorAmigos()) {
                        //Caso o usuário seja recuperado pela conversa e tal usuário
                        // não aceite ser convidado para grupos onde ele não tenha vínculo.
                    } else {
                        if (!grupoAtual.getParticipantes().contains(usuarioRecuperado.getIdUsuario())) {
                            listaUsersAdicao.add(usuarioRecuperado);
                        }
                        hashSetUsersAdicao.addAll(listaUsersAdicao);
                        listaUsersAdicao.clear();
                        listaUsersAdicao.addAll(hashSetUsersAdicao);
                    }
                }
                recuperUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sairDoGrupo() {
        grupoAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Grupo grupoAtualizado = snapshot.getValue(Grupo.class);
                    if (grupoAtualizado.getParticipantes() != null
                            && grupoAtualizado.getParticipantes().size() > 0
                            && grupoAtualizado.getParticipantes().contains(idUsuario)) {
                        listaUsuarioAtualRemovido.clear();
                        listaUsuarioAtualRemovido.addAll(grupoAtualizado.getParticipantes());
                        listaUsuarioAtualRemovido.remove(idUsuario);

                        grupoAtualRef.child("participantes").setValue(listaUsuarioAtualRemovido).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                if (grupoAtualizado.getAdmsGrupo() != null
                                        && grupoAtualizado.getAdmsGrupo().size() > 0
                                        && grupoAtualizado.getAdmsGrupo().contains(idUsuario)) {
                                    listaUsuarioAtualRemovido.clear();
                                    listaUsuarioAtualRemovido.addAll(grupoAtualizado.getAdmsGrupo());
                                    listaUsuarioAtualRemovido.remove(idUsuario);
                                    if (listaUsuarioAtualRemovido != null && listaUsuarioAtualRemovido.size() > 0) {
                                        grupoAtualRef.child("admsGrupo").setValue(listaUsuarioAtualRemovido);
                                    } else {
                                        grupoAtualRef.child("admsGrupo").removeValue();
                                    }
                                }
                                salvarAvisoSaida();
                                irParaTelaInicial();
                            }
                        });
                    }
                }
                grupoAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void abrirDialogSairDoGrupo() {
        bottomSheetDialogSairDoGrupo.show();
        bottomSheetDialogSairDoGrupo.setCancelable(true);

        txtViewEscolherFundador = bottomSheetDialogSairDoGrupo.findViewById(R.id.txtViewEscolherFundador);
        txtViewFundadorAleatorio = bottomSheetDialogSairDoGrupo.findViewById(R.id.txtViewFundadorAleatorio);
        txtViewCancelarSaida = bottomSheetDialogSairDoGrupo.findViewById(R.id.txtViewCancelarSaida);


        txtViewEscolherFundador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gerenciarUsuarios("novoFundador");
            }
        });

        txtViewFundadorAleatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gerenciarUsuarios("novoFundadorAleatorio");
            }
        });

        txtViewCancelarSaida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetDialogSairDoGrupo != null
                        && bottomSheetDialogSairDoGrupo.isShowing()) {
                    bottomSheetDialogSairDoGrupo.dismiss();
                }
            }
        });
    }

    private void alertaExclusaoGrupo() {
        builderExclusao.setTitle("Deseja realmente excluir seu grupo?");
        builderExclusao.setMessage("O grupo será excluído permamentemente e seus participantes também.");
        builderExclusao.setCancelable(true);
        builderExclusao.setPositiveButton("Sair e excluir grupo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                excluirGrupo();
            }
        });
        builderExclusao.setNegativeButton("Cancelar", null);
        dialogExclusao = builderExclusao.create();
        dialogExclusao.show();
    }

    private void excluirGrupo() {

        progressDialog.setMessage("Excluindo dados do grupo, aguarde um momento...");
        progressDialog.show();

        DatabaseReference deletarGrupoRef = firebaseRef.child("grupos")
                .child(grupoAtual.getIdGrupo());
        DatabaseReference atualizarIdsGrupoRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("idMeusGrupos");
        ArrayList<String> listaIdsGrupos = new ArrayList<>();

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdMeusGrupos() != null
                        && usuarioAtual.getIdMeusGrupos().size() > 0) {
                    listaIdsGrupos.clear();
                    listaIdsGrupos.addAll(usuarioAtual.getIdMeusGrupos());
                    listaIdsGrupos.remove(grupoAtual.getIdGrupo());
                    if (listaIdsGrupos.size() > 0) {
                        atualizarIdsGrupoRef.setValue(listaIdsGrupos);
                    } else {
                        atualizarIdsGrupoRef.removeValue();
                    }

                    removerArquivosGrupo();

                    deletarGrupoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pararProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Grupo excluído com sucesso", getApplicationContext());
                            irParaTelaInicial();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pararProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir o grupo, tente novamente mais tarde!", getApplicationContext());
                            irParaTelaInicial();
                        }
                    });
                }
            }

            @Override
            public void onError(String mensagem) {
                pararProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir o grupo, tente novamente mais tarde!", getApplicationContext());
            }
        });
    }


    private void removerArquivosGrupo() {
        imagemRef = storageRef.child("grupos")
                .child("imagemGrupo")
                .child(grupoAtual.getIdGrupo()).getStorage()
                .getReferenceFromUrl(grupoAtual.getFotoGrupo());
        imagemRef.delete();
    }

    private void pararProgressDialog(){
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void alertaSairDoGrupo(Boolean fundador) {
        builderExclusao.setTitle("Deseja realmente sair do grupo?");
        builderExclusao.setMessage("Você será excluído do grupo e você terá que escolher" +
                "um novo fundador ou deixará com que o novo fundador seja escolhido de forma" +
                "aleatória");
        builderExclusao.setCancelable(true);
        builderExclusao.setPositiveButton("Sair do grupo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (fundador) {
                    abrirDialogSairDoGrupo();
                }else{
                    sairDoGrupo();
                }
            }
        });
        builderExclusao.setNegativeButton("Cancelar", null);
        dialogExclusao = builderExclusao.create();
        dialogExclusao.show();
    }

    private void salvarAvisoSaida (){
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {

                String conteudoAviso = nomeAjustado + " saiu do grupo";

                adicionaMsgExclusaoRef = firebaseRef.child("conversas");

                idConversaGrupo = adicionaMsgExclusaoRef.push().getKey();

                HashMap<String, Object> dadosMensagem = new HashMap<>();
                dadosMensagem.put("idConversa", idConversaGrupo);
                dadosMensagem.put("exibirAviso", true);
                dadosMensagem.put("conteudoMensagem", conteudoAviso);

                adicionaMsgExclusaoRef = adicionaMsgExclusaoRef.child(grupoAtual.getIdGrupo())
                        .child(idConversaGrupo);

                adicionaMsgExclusaoRef.setValue(dadosMensagem);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }
}