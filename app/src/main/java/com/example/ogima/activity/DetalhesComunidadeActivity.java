package com.example.ogima.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterParticipantesComunidade;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DadosUserPadrao;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Comunidade;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DetalhesComunidadeActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbarDetalhesComunidade;
    private ImageButton imgBtnBackDetalhesComunidade, imgBtnConfigsDetalhesComunidade;
    private ImageView imgViewFotoComunidadeDetalhes, imgViewFundadorComunidadeDetalhes;
    private TextView txtViewNomeComunidadeDetalhes, txtViewPrivacidadeComunidadeDetalhes,
            txtViewDescricaoComunidadeDetalhes, txtViewNrParticipantesComunidadeDetalhes,
            txtViewNomeFundadorComunidade, txtViewNrAdmsComunidadeDetalhes;
    private Comunidade comunidadeAtual;
    private LinearLayout linearLayoutTopicosComunidade;
    private RecyclerView recyclerViewParticipantesComunidade, recyclerViewAdmsComunidade;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private AdapterParticipantesComunidade adapterParticipantesComunidade;
    private AdapterParticipantesComunidade adapterParticipantesAdms;

    private List<Usuario> listaParticipantes = new ArrayList<>();
    private List<Usuario> listaAtualizadaParticipantes = new ArrayList<>();
    private List<Usuario> listaAdms = new ArrayList<>();
    //listaUsersPromocao - Somente participantes que ainda não são adms.
    private List<Usuario> listaUsersPromocao = new ArrayList<>();

    private List<Usuario> listaUsersAdicao = new ArrayList<>();
    private HashSet<Usuario> hashSetUsersAdicao = new HashSet<>();

    private Button btnEditarComunidade, btnDeletarComunidade, btnSairDaComunidade,
            btnGerenciarUsuarios;

    private LinearLayout linearLayoutAdmsDetalhes;
    private BottomSheetDialog bottomSheetDialogGerenciar, bottomSheetDialogSairDaComunidade;
    //Dialog
    private Button btnViewAddUserComunidade, btnViewRemoverUserComunidade, btnViewPromoverUserComunidade,
            btnViewDespromoverUserComunidade;

    private DatabaseReference comunidadeAtualRef;
    private ArrayList<String> listaUsuarioAtualRemovido = new ArrayList<>();

    //Componentes bottomSheetDialogSairDaComunidade
    private TextView txtViewEscolherFundador, txtViewFundadorAleatorio, txtViewCancelarSaida;

    private AlertDialog.Builder builderExclusao;
    private AlertDialog dialogExclusao;
    private StorageReference storageRef;
    private StorageReference imagemRef;
    private ProgressDialog progressDialog;
    private DatabaseReference adicionaMsgExclusaoRef;
    private String idConversaComunidade;

    //Menu superior
    private PopupMenu popupMenuConfig;

    //Denúncia
    private int EMAIL_REQUEST_CODE = 100;
    private Intent intentDenuncia = new Intent(Intent.ACTION_SEND);
    private Boolean irParaInicio = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        irParaTelaInicial();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bottomSheetDialogGerenciar != null && bottomSheetDialogGerenciar.isShowing()) {
            bottomSheetDialogGerenciar.dismiss();
        }

        if (bottomSheetDialogSairDaComunidade != null && bottomSheetDialogSairDaComunidade.isShowing()) {
            bottomSheetDialogSairDaComunidade.dismiss();
        }

        if (dialogExclusao != null && dialogExclusao.isShowing()) {
            dialogExclusao.dismiss();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_comunidade);
        inicializandoComponentes();
        setSupportActionBar(toolbarDetalhesComunidade);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("comunidadeAtual")) {

                comunidadeAtual = (Comunidade) dados.getSerializable("comunidadeAtual");

                comunidadeAtualRef = firebaseRef.child("comunidades").child(comunidadeAtual.getIdComunidade());
                builderExclusao = new AlertDialog.Builder(this);

                if (comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {
                    imgBtnConfigsDetalhesComunidade.setVisibility(View.GONE);
                }

                recuperarContato();
                recuperarConversa();
                configurarBottomSheetDialog();
                eventosClickListeners();
                configMenuSuperior();
                detalhesComunidade();
            }
        }
    }

    private void configurarBottomSheetDialog() {
        bottomSheetDialogGerenciar = new BottomSheetDialog(DetalhesComunidadeActivity.this);
        bottomSheetDialogGerenciar.setContentView(R.layout.bottom_sheet_dialog_gerenciar_grupo);

        bottomSheetDialogSairDaComunidade = new BottomSheetDialog(DetalhesComunidadeActivity.this);
        bottomSheetDialogSairDaComunidade.setContentView(R.layout.bottom_sheet_sair_do_grupo);
    }


    private void detalhesComunidade() {

        VerificaEpilpesia.verificarEpilpesiaSelecionadaComunidade(getApplicationContext(),
                comunidadeAtual, imgViewFotoComunidadeDetalhes);

        txtViewNomeComunidadeDetalhes.setText(comunidadeAtual.getNomeComunidade());
        //ToastCustomizado.toastCustomizadoCurto("Nome - " + comunidadeAtual.getNomeComunidade(), getApplicationContext());


        //Todos participantes
        configRecyclerParticipantes();

        if (comunidadeAtual.getAdmsComunidade() != null && comunidadeAtual.getAdmsComunidade().size() > 0) {
            //Existe admistrador, então exibir o adapter de adms.
            linearLayoutAdmsDetalhes.setVisibility(View.VISIBLE);
            configRecyclerAdms();
            txtViewNrAdmsComunidadeDetalhes.setText("" + comunidadeAtual.getAdmsComunidade().size() + "/" + "5");
        } else {
            //Oculta o layout de adms, pois não existe nenhum administrador.
            linearLayoutAdmsDetalhes.setVisibility(View.GONE);
        }

        exibirTopicos();

        if (comunidadeAtual.getComunidadePublica()) {
            txtViewPrivacidadeComunidadeDetalhes.setText("Público");
        } else {
            txtViewPrivacidadeComunidadeDetalhes.setText("Particular");
        }

        txtViewDescricaoComunidadeDetalhes.setText(comunidadeAtual.getDescricaoComunidade());
        txtViewNrParticipantesComunidadeDetalhes.setText("" + comunidadeAtual.getParticipantes().size() + "/" + "40");

        dadosFundadorComunidade();
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void exibirTopicos() {
        // Itere sobre a lista de hobbies e crie um chip para cada um
        for (String topico : comunidadeAtual.getTopicos()) {
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
            linearLayoutTopicosComunidade.addView(chip);
        }

        linearLayoutTopicosComunidade.setOrientation(LinearLayout.HORIZONTAL);
    }

    private void eventosClickListeners() {

        if (comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {
            //Usuário atual é o fundador
            btnDeletarComunidade.setVisibility(View.VISIBLE);
            btnEditarComunidade.setVisibility(View.VISIBLE);
            btnGerenciarUsuarios.setVisibility(View.VISIBLE);

            if (comunidadeAtual.getParticipantes() != null &&
                    comunidadeAtual.getParticipantes().size() == 1) {
                btnSairDaComunidade.setVisibility(View.GONE);
                btnDeletarComunidade.setText("Sair e excluir comunidade");
            }

        } else if (comunidadeAtual.getAdmsComunidade() != null && comunidadeAtual.getAdmsComunidade().size() > 0
                && comunidadeAtual.getAdmsComunidade().contains(idUsuario)) {
            //Administrador pode gerenciar participantes, porém com limitações.
            btnGerenciarUsuarios.setVisibility(View.VISIBLE);
            btnDeletarComunidade.setVisibility(View.GONE);
            btnEditarComunidade.setVisibility(View.GONE);
        } else {
            //Caso o usuário não possua nenhum cargo
            btnDeletarComunidade.setVisibility(View.GONE);
            btnEditarComunidade.setVisibility(View.GONE);
            btnGerenciarUsuarios.setVisibility(View.GONE);
        }

        imgBtnBackDetalhesComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnEditarComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CriarComunidadeActivity.class);
                intent.putExtra("comunidadeEdicao", comunidadeAtual);
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

                        if (usuarioParticipante.getIdUsuario().equals(comunidadeAtual.getIdSuperAdmComunidade())) {
                            //Remove o fundador da listagem
                            listaAtualizadaParticipantes.remove(usuarioParticipante);
                            listaUsersPromocao.remove(usuarioParticipante);
                        }

                        if (!idUsuario.equals(comunidadeAtual.getIdSuperAdmComunidade())) {
                            if (comunidadeAtual.getAdmsComunidade() != null && comunidadeAtual.getAdmsComunidade().size() > 0) {
                                if (comunidadeAtual.getAdmsComunidade().contains(usuarioParticipante.getIdUsuario())) {
                                    //Caso o usuário atual não seja o fundador, os outros adms não serão listados
                                    listaAtualizadaParticipantes.remove(usuarioParticipante);
                                    listaUsersPromocao.remove(usuarioParticipante);
                                }
                            }
                        }
                    }
                }

                if (listaAtualizadaParticipantes != null && listaAtualizadaParticipantes.size() > 0
                        || comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {
                    abrirDialogGerenciamento();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Não existem usuários que possam ser gerenciados no momento", getApplicationContext());
                }
            }
        });

        btnSairDaComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {
                    alertaSairDaComunidade(true);
                } else {
                    alertaSairDaComunidade(false);
                }
            }
        });

        btnDeletarComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertaExclusaoComunidade();
            }
        });
    }

    private void irParaTelaInicial() {
        Intent intent = new Intent(DetalhesComunidadeActivity.this, NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
    }

    private void dadosFundadorComunidade() {
        DatabaseReference dadosFundadorRef = firebaseRef.child("usuarios")
                .child(comunidadeAtual.getIdSuperAdmComunidade());

        dadosFundadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioFundador = snapshot.getValue(Usuario.class);
                    DadosUserPadrao.preencherDadosUser(getApplicationContext(),
                            usuarioFundador, txtViewNomeFundadorComunidade,
                            imgViewFundadorComunidadeDetalhes);
                }
                dadosFundadorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configRecyclerParticipantes() {
        for (String todosParticipantes : comunidadeAtual.getParticipantes()) {
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
                        adapterParticipantesComunidade.notifyDataSetChanged();
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
        recyclerViewParticipantesComunidade.setLayoutManager(linearLayoutManager);
        recyclerViewParticipantesComunidade.setHasFixedSize(true);

        if (adapterParticipantesComunidade != null) {

        } else {
            adapterParticipantesComunidade = new AdapterParticipantesComunidade(null, getApplicationContext(), comunidadeAtual, true, listaParticipantes);
        }
        recyclerViewParticipantesComunidade.setAdapter(adapterParticipantesComunidade);
    }

    private void configRecyclerAdms() {
        //Somente administradores
        for (String todosAdms : comunidadeAtual.getAdmsComunidade()) {
            if (!todosAdms.equals(comunidadeAtual.getIdSuperAdmComunidade())) {
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
        recyclerViewAdmsComunidade.setLayoutManager(linearLayoutManagerAdms);
        recyclerViewAdmsComunidade.setHasFixedSize(true);

        if (adapterParticipantesAdms != null) {

        } else {
            adapterParticipantesAdms = new AdapterParticipantesComunidade(null, getApplicationContext(), comunidadeAtual, true, listaAdms);
        }
        recyclerViewAdmsComunidade.setAdapter(adapterParticipantesAdms);
    }

    private void abrirDialogGerenciamento() {
        bottomSheetDialogGerenciar.show();
        bottomSheetDialogGerenciar.setCancelable(true);

        btnViewAddUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewAddUserGrupo);
        btnViewRemoverUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewRemoverUserGrupo);
        btnViewPromoverUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewPromoverUserGrupo);
        btnViewDespromoverUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewDespromoverUserGrupo);


        if (comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario) ||
                comunidadeAtual.getAdmsComunidade() != null && comunidadeAtual.getAdmsComunidade().size() > 0
                        && comunidadeAtual.getAdmsComunidade().contains(idUsuario)) {

            //Lógica somente se o usuário atual é adm ou fundador

            if (listaUsersAdicao != null && listaUsersAdicao.size() > 0) {
                //Existem usuário a serem adicionados
                btnViewAddUserComunidade.setVisibility(View.VISIBLE);
            }

            if (listaAtualizadaParticipantes.size() >= 1) {
                //Existem usuários a serem removidos
                btnViewRemoverUserComunidade.setVisibility(View.VISIBLE);
            }
        }

        if (comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {

            if (listaUsersPromocao != null && listaUsersPromocao.size() > 0
                    && comunidadeAtual.getAdmsComunidade() != null && comunidadeAtual.getAdmsComunidade().size() > 0) {
                for (Usuario usuarioAdm : listaAdms) {
                    //Deixa somente usuários que ainda não foram promovidos.
                    listaUsersPromocao.remove(usuarioAdm);
                }
            }

            if (listaUsersPromocao != null && listaUsersPromocao.size() > 0) {
                btnViewPromoverUserComunidade.setVisibility(View.VISIBLE);
            }

            if (comunidadeAtual.getAdmsComunidade() != null && comunidadeAtual.getAdmsComunidade().size() > 0) {
                btnViewDespromoverUserComunidade.setVisibility(View.VISIBLE);
            }
        }

        btnViewAddUserComunidade.setOnClickListener(this);
        btnViewRemoverUserComunidade.setOnClickListener(this);
        btnViewPromoverUserComunidade.setOnClickListener(this);
        btnViewDespromoverUserComunidade.setOnClickListener(this);
    }

    private void gerenciarUsuarios(String tipoGerenciamento) {

        Intent intent = new Intent(DetalhesComunidadeActivity.this, GerenciarUsersComunidadeActivity.class);
        intent.putExtra("comunidadeAtual", comunidadeAtual);
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
        toolbarDetalhesComunidade = findViewById(R.id.toolbarDetalhesComunidade);
        imgBtnBackDetalhesComunidade = findViewById(R.id.imgBtnBackDetalhesComunidade);
        imgBtnConfigsDetalhesComunidade = findViewById(R.id.imgBtnConfigsDetalhesComunidade);
        imgViewFotoComunidadeDetalhes = findViewById(R.id.imgViewFotoComunidadeDetalhes);
        txtViewNomeComunidadeDetalhes = findViewById(R.id.txtViewNomeComunidadeDetalhes);
        linearLayoutTopicosComunidade = findViewById(R.id.linearLayoutTopicosComunidade);
        txtViewPrivacidadeComunidadeDetalhes = findViewById(R.id.txtViewPrivacidadeComunidadeDetalhes);
        txtViewDescricaoComunidadeDetalhes = findViewById(R.id.txtViewDescricaoComunidadeDetalhes);
        txtViewNrParticipantesComunidadeDetalhes = findViewById(R.id.txtViewNrParticipantesComunidadeDetalhes);
        recyclerViewParticipantesComunidade = findViewById(R.id.recyclerViewParticipantesComunidadeDetalhes);
        imgViewFundadorComunidadeDetalhes = findViewById(R.id.imgViewFundadorComunidadeDetalhes);
        txtViewNomeFundadorComunidade = findViewById(R.id.txtViewNomeFundadorComunidadeDetalhes);
        txtViewNrAdmsComunidadeDetalhes = findViewById(R.id.txtViewNrAdmsComunidadeDetalhes);
        recyclerViewAdmsComunidade = findViewById(R.id.recyclerViewAdmsComunidadeDetalhes);
        btnEditarComunidade = findViewById(R.id.btnEditarComunidade);
        btnDeletarComunidade = findViewById(R.id.btnDeletarComunidade);
        btnSairDaComunidade = findViewById(R.id.btnSairDaComunidade);
        linearLayoutAdmsDetalhes = findViewById(R.id.linearLayoutAdmsDetalhes);
        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarParticipantesComunidade);
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
                            && usuarioRecuperado.getComunidadesSomentePorAmigos() != null
                            && usuarioRecuperado.getComunidadesSomentePorAmigos()
                            || usuarioRecuperado.getIdComunidadesBloqueadas() != null
                            && usuarioRecuperado.getIdComunidadesBloqueadas().size() > 0
                            && usuarioRecuperado.getIdComunidadesBloqueadas().contains(comunidadeAtual.getIdComunidade())) {
                        //Caso o usuário seja recuperado pela conversa e tal usuário
                        // não aceite ser convidado para comunidades onde ele não tenha vínculo.
                    } else {
                        if (!comunidadeAtual.getParticipantes().contains(usuarioRecuperado.getIdUsuario())) {
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

    private void sairDaComunidade() {
        comunidadeAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Comunidade comunidadeAtualizado = snapshot.getValue(Comunidade.class);
                    if (comunidadeAtualizado.getParticipantes() != null
                            && comunidadeAtualizado.getParticipantes().size() > 0
                            && comunidadeAtualizado.getParticipantes().contains(idUsuario)) {
                        listaUsuarioAtualRemovido.clear();
                        listaUsuarioAtualRemovido.addAll(comunidadeAtualizado.getParticipantes());
                        listaUsuarioAtualRemovido.remove(idUsuario);

                        comunidadeAtualRef.child("participantes").setValue(listaUsuarioAtualRemovido).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                if (comunidadeAtualizado.getAdmsComunidade() != null
                                        && comunidadeAtualizado.getAdmsComunidade().size() > 0
                                        && comunidadeAtualizado.getAdmsComunidade().contains(idUsuario)) {
                                    listaUsuarioAtualRemovido.clear();
                                    listaUsuarioAtualRemovido.addAll(comunidadeAtualizado.getAdmsComunidade());
                                    listaUsuarioAtualRemovido.remove(idUsuario);
                                    if (listaUsuarioAtualRemovido != null && listaUsuarioAtualRemovido.size() > 0) {
                                        comunidadeAtualRef.child("admsComunidade").setValue(listaUsuarioAtualRemovido);
                                    } else {
                                        comunidadeAtualRef.child("admsComunidade").removeValue();
                                    }
                                }
                                //*salvarAvisoSaida();

                                if (!irParaInicio) {
                                    irParaTelaInicial();
                                }
                            }
                        });
                    }
                }
                comunidadeAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void abrirDialogSairDaComunidade() {
        bottomSheetDialogSairDaComunidade.show();
        bottomSheetDialogSairDaComunidade.setCancelable(true);

        txtViewEscolherFundador = bottomSheetDialogSairDaComunidade.findViewById(R.id.txtViewEscolherFundador);
        txtViewFundadorAleatorio = bottomSheetDialogSairDaComunidade.findViewById(R.id.txtViewFundadorAleatorio);
        txtViewCancelarSaida = bottomSheetDialogSairDaComunidade.findViewById(R.id.txtViewCancelarSaida);

        txtViewEscolherFundador.setText("Escolher um novo fundador e sair da comunidade");
        txtViewFundadorAleatorio.setText("Sair da comunidade e um usuário aleatoriamente se tornará fundador");


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
                if (bottomSheetDialogSairDaComunidade != null
                        && bottomSheetDialogSairDaComunidade.isShowing()) {
                    bottomSheetDialogSairDaComunidade.dismiss();
                }
            }
        });
    }

    private void alertaExclusaoComunidade() {
        builderExclusao.setTitle("Deseja realmente excluir seu comunidade?");
        builderExclusao.setMessage("O comunidade será excluído permamentemente e seus participantes também.");
        builderExclusao.setCancelable(true);
        builderExclusao.setPositiveButton("Sair e excluir comunidade", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                excluirComunidade();
            }
        });
        builderExclusao.setNegativeButton("Cancelar", null);
        dialogExclusao = builderExclusao.create();
        dialogExclusao.show();
    }

    private void excluirComunidade() {

        progressDialog.setMessage("Excluindo dados da comunidade, aguarde um momento...");
        progressDialog.show();

        DatabaseReference deletarComunidadeRef = firebaseRef.child("comunidades")
                .child(comunidadeAtual.getIdComunidade());
        DatabaseReference atualizarIdsComunidadeRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("idMinhasComunidades");
        ArrayList<String> listaIdsComunidades = new ArrayList<>();

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdMinhasComunidades() != null
                        && usuarioAtual.getIdMinhasComunidades().size() > 0) {
                    listaIdsComunidades.clear();
                    listaIdsComunidades.addAll(usuarioAtual.getIdMinhasComunidades());
                    listaIdsComunidades.remove(comunidadeAtual.getIdComunidade());
                    if (listaIdsComunidades.size() > 0) {
                        atualizarIdsComunidadeRef.setValue(listaIdsComunidades);
                    } else {
                        atualizarIdsComunidadeRef.removeValue();
                    }

                    removerArquivosComunidade();

                    deletarComunidadeRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pararProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Comunidade excluído com sucesso", getApplicationContext());
                            irParaTelaInicial();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pararProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir o comunidade, tente novamente mais tarde!", getApplicationContext());
                            irParaTelaInicial();
                        }
                    });
                }
            }

            @Override
            public void onError(String mensagem) {
                pararProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir o comunidade, tente novamente mais tarde!", getApplicationContext());
            }
        });
    }


    private void removerArquivosComunidade() {
        imagemRef = storageRef.child("comunidades")
                .child("imagemComunidade")
                .child(comunidadeAtual.getIdComunidade()).getStorage()
                .getReferenceFromUrl(comunidadeAtual.getFotoComunidade());
        imagemRef.delete();
    }

    private void pararProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void alertaSairDaComunidade(Boolean fundador) {
        builderExclusao.setTitle("Deseja realmente sair da comunidade?");
        builderExclusao.setMessage("Você será excluído da comunidade e você terá que escolher" +
                " um novo fundador ou deixará com que o novo fundador seja escolhido de forma" +
                "aleatória");
        builderExclusao.setCancelable(true);
        builderExclusao.setPositiveButton("Sair da comunidade", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (fundador) {
                    abrirDialogSairDaComunidade();
                } else {
                    sairDaComunidade();
                }
            }
        });
        builderExclusao.setNegativeButton("Cancelar", null);
        dialogExclusao = builderExclusao.create();
        dialogExclusao.show();
    }

    private void salvarAvisoSaida() {
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {

                String conteudoAviso = nomeAjustado + " saiu da comunidade";

                adicionaMsgExclusaoRef = firebaseRef.child("conversas");

                idConversaComunidade = adicionaMsgExclusaoRef.push().getKey();

                HashMap<String, Object> dadosMensagem = new HashMap<>();
                dadosMensagem.put("idConversa", idConversaComunidade);
                dadosMensagem.put("exibirAviso", true);
                dadosMensagem.put("conteudoMensagem", conteudoAviso);

                adicionaMsgExclusaoRef = adicionaMsgExclusaoRef.child(comunidadeAtual.getIdComunidade())
                        .child(idConversaComunidade);

                adicionaMsgExclusaoRef.setValue(dadosMensagem);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void configMenuSuperior() {
        popupMenuConfig = new PopupMenu(getApplicationContext(), imgBtnConfigsDetalhesComunidade);
        popupMenuConfig.getMenuInflater().inflate(R.menu.popup_menu_configs_bloquear_denunciar, popupMenuConfig.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenuConfig.setForceShowIcon(true);
        }

        imgBtnConfigsDetalhesComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenuConfig.show();
            }
        });

        popupMenuConfig.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.itemBloquear:
                        bloquearComunidade();
                        break;
                    case R.id.itemDenunciarBloquear:
                        denunciarBloquearComunidade();
                        break;
                }
                return false;
            }
        });
    }

    private void bloquearComunidade() {

        DatabaseReference idComunidadesBloqueadosRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("idComunidadesBloqueadas");

        ArrayList<String> idComunidadesBloqueados = new ArrayList<>();

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdComunidadesBloqueadas() != null
                        && usuarioAtual.getIdComunidadesBloqueadas().size() > 0) {
                    idComunidadesBloqueados.addAll(usuarioAtual.getIdComunidadesBloqueadas());
                    idComunidadesBloqueados.add(comunidadeAtual.getIdComunidade());
                } else {
                    idComunidadesBloqueados.add(comunidadeAtual.getIdComunidade());
                }
                idComunidadesBloqueadosRef.setValue(idComunidadesBloqueados).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        sairDaComunidade();
                    }
                });
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void denunciarBloquearComunidade() {

        irParaInicio = true;

        bloquearComunidade();

        //Talvez seja necessário limitar essa função?
        intentDenuncia.setType("message/rfc822");
        intentDenuncia.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
        intentDenuncia.putExtra(Intent.EXTRA_SUBJECT, "Denúncia - " + "Informe o motivo da denúncia" + comunidadeAtual.getIdComunidade());
        intentDenuncia.putExtra(Intent.EXTRA_TEXT, "Descreva sua denúncia nesse campo e anexe as provas no email," +
                " por favor não apague o identificador da denúncia que está no assunto da mensagem");
        try {
            startActivityForResult(Intent.createChooser(intentDenuncia, "Selecione seu app de envio de email"), EMAIL_REQUEST_CODE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Ao selecionar um app para fazer a denúncia, ele vai para tela inicial.
        if (requestCode == EMAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            irParaTelaInicial();
        } else if (resultCode == RESULT_CANCELED) {
            //Mesmo o usuário não prosseguir com a denúncia, ele já foi removido da comunidade
            //e irá retornar para tela inicial.
            irParaTelaInicial();
        }
    }
}