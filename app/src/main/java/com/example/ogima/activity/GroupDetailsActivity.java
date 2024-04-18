package com.example.ogima.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterAdmsComunidade;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.IntentUtils;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseUtils firebaseUtils = new FirebaseUtils();
    private StorageReference storageRef;
    private String idUsuario = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncBlack;
    private ImageButton imgBtnIncBackBlack;
    private ImageView imgViewFoto, imgViewFundador;
    private TextView txtViewNome, txtViewNrParticipantes, txtViewNrAdms,
            txtViewNomeFundador, txtViewDesc, txtViewPrivacidade;
    private SpinKitView spinKitViewFoto, spinKitViewFundador;
    private Button btnVerParticipantes, btnEditarGrupo, btnDeletarGrupo,
            btnSairDoGrupo, btnGerenciarUsuarios;
    private ImageButton imgBtnVerParticipantes, imgBtnConfigGrupo;
    private boolean statusEpilepsia = true;
    private String msgErroAoExibirDetalhes = "", msgErroAoRecuperarDados = "";
    private Query grupoRef;
    private ChildEventListener childListenerGrupo;
    private String idGrupo = "";
    private boolean dadosRecuperados = false;
    private List<Usuario> listaUsuariosAdms = new ArrayList<>();
    private int percorrido = 0;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerViewAdms;
    private AdapterAdmsComunidade adapterAdmsGrupo;
    private UsuarioDiffDAO usuarioDiffDAO;
    private LinearLayout linearLayoutTopicos;
    private CommunityUtils communityUtils;
    private GroupUtils groupUtils;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;
    private BottomSheetDialog bottomSheetDialogSair, bottomSheetDialogGerenciar;
    //Componentes bottomSheetDialogSair
    private TextView txtViewEscolherFundador, txtViewCancelarSaida;
    //Componentes bottomSheetDialogGerenciar
    private Button btnViewAddUserGrupo, btnViewRemoverUserGrupo, btnViewPromoverUserGrupo,
            btnViewDespromoverUserGrupo;
    private MidiaUtils midiaUtils;
    private boolean founder = false;
    private boolean administrator = false;
    private PopupMenu popupMenuConfig;
    private DatabaseReference verificaBlockRef, verificaDenunciaRef;
    private ValueEventListener listenerBlock, listenerDenuncia;
    private boolean grupoBloqueado = false;
    private final Intent intentDenuncia = new Intent(Intent.ACTION_SEND);
    private ActivityResultLauncher<Intent> resultLauncher;
    private int limiteBloqueio = 0;
    private boolean chatComunidade = false;

    public GroupDetailsActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        msgErroAoExibirDetalhes = "Ocorreu um erro ao exibir os detalhes do grupo.";
        msgErroAoRecuperarDados = "Ocorreu um erro ao recuperar os dados do grupo.";
    }

    public interface DadosIniciaisCallback {
        void onConcluido();

        void onError();
    }

    public interface GrupoRecuperadoCallback {
        void onConcluido();
    }

    public interface RecuperarAdmsCallback {
        void onConcluido();
    }

    @Override
    protected void onStart() {
        super.onStart();
        configOnStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseUtils.removerQueryChildListener(grupoRef, childListenerGrupo);
        firebaseUtils.removerValueListener(verificaBlockRef, listenerBlock);
        firebaseUtils.removerValueListener(verificaDenunciaRef, listenerDenuncia);
        if (usuarioDiffDAO != null) {
            usuarioDiffDAO.limparListaUsuarios();
        }
        if (bottomSheetDialogGerenciar != null && bottomSheetDialogGerenciar.isShowing()) {
            bottomSheetDialogGerenciar.dismiss();
        }
        if (linearLayoutTopicos != null) {
            linearLayoutTopicos.removeAllViews();
        }
        dadosRecuperados = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        inicializarComponentes();
        resultadoIntent();
    }

    private void configOnStart() {
        if (!dadosRecuperados) {
            if (idUsuario.isEmpty()) {
                ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
                onBackPressed();
                return;
            }
            communityUtils = new CommunityUtils(getApplicationContext());
            groupUtils = new GroupUtils(getApplicationContext());
            builder = new AlertDialog.Builder(this);
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            midiaUtils = new MidiaUtils(GroupDetailsActivity.this, getApplicationContext());
            btnDeletarGrupo.setVisibility(View.GONE);
            btnEditarGrupo.setVisibility(View.GONE);
            btnSairDoGrupo.setVisibility(View.GONE);
            btnGerenciarUsuarios.setVisibility(View.GONE);
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaUsuariosAdms, adapterAdmsGrupo);
            UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                @Override
                public void onConcluido(boolean epilepsia) {
                    setStatusEpilepsia(epilepsia);
                    adapterAdmsGrupo.setStatusEpilepsia(epilepsia);
                    configInicial(new DadosIniciaisCallback() {
                        @Override
                        public void onConcluido() {
                            if (idGrupo.isEmpty()) {
                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do grupo", getApplicationContext());
                                onBackPressed();
                                return;
                            }
                            configurarBottomSheetDialog();
                            configMenuSuperior();
                            clickListeners();
                            recuperarGrupo(new GrupoRecuperadoCallback() {
                                @Override
                                public void onConcluido() {

                                }
                            });
                        }

                        @Override
                        public void onError() {
                            ToastCustomizado.toastCustomizadoCurto(msgErroAoRecuperarDados, getApplicationContext());
                            onBackPressed();
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
            dadosRecuperados = true;
        }
    }

    private void configInicial(DadosIniciaisCallback callback) {
        setSupportActionBar(toolbarIncBlack);
        setTitle("");
        String title = FormatarContadorUtils.abreviarTexto("Detalhes do grupo", 32);
        imgViewFoto.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        txtViewTitleToolbar.setText(title);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        configBundle(callback);
    }

    private void configBundle(DadosIniciaisCallback callback) {
        Bundle dados = getIntent().getExtras();
        if (dados == null) {
            callback.onError();
            return;
        }
        if (dados.containsKey("idGrupo")) {
            idGrupo = dados.getString("idGrupo");
            if (dados.containsKey("chatComunidade")) {
                chatComunidade = dados.getBoolean("chatComunidade");
            }
            callback.onConcluido();
        } else {
            callback.onError();
        }
    }

    private void recuperarGrupo(GrupoRecuperadoCallback callback) {
        grupoRef = firebaseRef.child("grupos").orderByChild("idGrupo")
                .equalTo(idGrupo);

        childListenerGrupo = grupoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo grupo = snapshot.getValue(Grupo.class);
                    if (grupo == null) {
                        ToastCustomizado.toastCustomizadoCurto(msgErroAoRecuperarDados, getApplicationContext());
                        onBackPressed();
                        return;
                    }
                    if (grupo.getFotoGrupo() != null
                            && !grupo.getFotoGrupo().isEmpty()) {
                        exibirFoto(UsuarioUtils.FIELD_PHOTO, grupo.getFotoGrupo(), false, false);
                    } else {
                        exibirFoto(UsuarioUtils.FIELD_PHOTO, "", true, false);
                    }

                    if (grupo.getNomeGrupo() != null
                            && !grupo.getNomeGrupo().isEmpty()) {
                        exibirNome(grupo.getNomeGrupo(), false);
                    }

                    int nrParticipantes = 0;
                    if (grupo.getNrParticipantes() != -1) {
                        nrParticipantes = (int) grupo.getNrParticipantes();
                    }

                    txtViewNrParticipantes.setText(String.format("%d%s%d", nrParticipantes, "/", GroupUtils.MAX_NUMBER_PARTICIPANTS));

                    int nrAdms = 0;
                    if (grupo.getNrAdms() != -1) {
                        nrAdms = grupo.getNrAdms();
                    }

                    txtViewNrAdms.setText(String.format("%d%s%d", nrAdms, "/", CommunityUtils.MAX_NUMBER_ADMS));

                    if (grupo.getAdmsGrupo() != null
                            && !grupo.getAdmsGrupo().isEmpty()) {
                        recuperarAdms(grupo.getAdmsGrupo(), new RecuperarAdmsCallback() {
                            @Override
                            public void onConcluido() {

                            }
                        });
                    }

                    if (grupo.getIdSuperAdmGrupo() == null
                            || grupo.getIdSuperAdmGrupo().isEmpty()) {
                        callback.onConcluido();
                        return;
                    }

                    recuperarDadosFundador(grupo.getIdSuperAdmGrupo(), callback);

                    if (grupo.getDescricaoGrupo() != null
                            && !grupo.getDescricaoGrupo().isEmpty()) {
                        txtViewDesc.setText(grupo.getDescricaoGrupo().trim());
                    }

                    if (grupo.getTopicos() != null
                            && !grupo.getTopicos().isEmpty()) {
                        exibirTopicos(grupo.getTopicos());
                    }

                    String privacidade;
                    if (grupo.getGrupoPublico()) {
                        privacidade = getString(R.string.public_group);
                    } else {
                        privacidade = getString(R.string.private_group);
                    }
                    txtViewPrivacidade.setText(privacidade);

                    if (grupo.getIdSuperAdmGrupo().equals(idUsuario)) {
                        imgBtnConfigGrupo.setVisibility(View.GONE);
                    } else {
                        imgBtnConfigGrupo.setVisibility(View.VISIBLE);
                    }

                    if (grupo.getIdSuperAdmGrupo().equals(idUsuario)) {
                        btnEditarGrupo.setVisibility(View.VISIBLE);
                        if (!chatComunidade) {
                            btnGerenciarUsuarios.setVisibility(View.VISIBLE);
                            btnDeletarGrupo.setVisibility(View.VISIBLE);
                        }
                        if (grupo.getNrParticipantes() <= 1) {
                            btnDeletarGrupo.setText("Sair e excluir grupo");
                        } else {
                            btnSairDoGrupo.setText("Sair do grupo");
                            if(!chatComunidade){
                                btnSairDoGrupo.setVisibility(View.VISIBLE);
                            }
                        }
                    } else if (grupo.getAdmsGrupo() != null
                            && !grupo.getAdmsGrupo().isEmpty()
                            && grupo.getAdmsGrupo().contains(idUsuario)) {
                        if(!chatComunidade){
                            btnGerenciarUsuarios.setVisibility(View.VISIBLE);
                        }
                        btnSairDoGrupo.setText("Sair do grupo");
                        btnSairDoGrupo.setVisibility(View.VISIBLE);
                    } else {
                        groupUtils.verificaSeEParticipante(grupo.getIdGrupo(), idUsuario, new GroupUtils.VerificaParticipanteCallback() {
                            @Override
                            public void onParticipante(boolean status) {
                                if (status) {
                                    //Participante
                                    btnSairDoGrupo.setText("Sair do grupo");
                                    btnSairDoGrupo.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(String message) {
                            }
                        });
                    }
                } else {
                    ToastCustomizado.toastCustomizadoCurto(msgErroAoRecuperarDados, getApplicationContext());
                    onBackPressed();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo grupo = snapshot.getValue(Grupo.class);
                    if (grupo != null && grupo.getIdSuperAdmGrupo() != null
                            && !grupo.getIdSuperAdmGrupo().isEmpty() &&
                            grupo.getIdSuperAdmGrupo().equals(idUsuario)) {
                        imgBtnConfigGrupo.setVisibility(View.GONE);
                    } else {
                        imgBtnConfigGrupo.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.group_does_not_exist), getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizadoCurto(String.format("%s%s %s", msgErroAoRecuperarDados, ":", error.getCode()), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void exibirFoto(String campo, String url, boolean padrao, boolean fundador) {

        if (campo == null || campo.isEmpty()) {
            return;
        }

        if (campo.equals(UsuarioUtils.FIELD_PHOTO)) {
            if (padrao) {
                if (fundador) {
                    UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFundador, campo, true);
                } else {
                    CommunityUtils.exibirFotoPadrao(getApplicationContext(), imgViewFoto, campo, true);
                }
                return;
            }

            if (url == null || url.isEmpty()) {
                return;
            }
            exibirSpinKit(campo, fundador);

            if (fundador) {
                GlideCustomizado.loadUrlComListener(getApplicationContext(),
                        url, imgViewFundador, android.R.color.transparent,
                        GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                            @Override
                            public void onCarregado() {
                                ocultarSpinKit(campo, fundador);
                            }

                            @Override
                            public void onError(String message) {
                                ocultarSpinKit(campo, fundador);
                                UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFundador, campo, true);
                            }
                        });
                return;
            }

            GlideCustomizado.loadUrlComListener(getApplicationContext(),
                    url, imgViewFoto, android.R.color.transparent,
                    GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                            ocultarSpinKit(campo, fundador);
                        }

                        @Override
                        public void onError(String message) {
                            ocultarSpinKit(campo, fundador);
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao carregar a foto do grupo.", getApplicationContext());
                        }
                    });

        }
    }

    private void exibirNome(String nomeAlvo, boolean fundador) {
        if (fundador) {
            String nomeFundador = FormatarContadorUtils.abreviarTexto(nomeAlvo, UsuarioUtils.MAX_NAME_LENGHT_FOUNDER);
            txtViewNomeFundador.setText(nomeFundador);
        } else {
            String nomeGrupo = FormatarContadorUtils.abreviarTexto(nomeAlvo, UsuarioUtils.MAX_COMMUNITY_NAME_LENGHT);
            txtViewNome.setText(nomeGrupo);
        }
    }

    private void recuperarAdms(ArrayList<String> listaIdAdms, RecuperarAdmsCallback callback) {
        usuarioDiffDAO.limparListaUsuarios();
        percorrido = 0;
        for (String idAdm : listaIdAdms) {
            percorrido++;
            FirebaseRecuperarUsuario.recuperaUsuario(idAdm, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
                @Override
                public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                    UsuarioUtils.checkBlockingStatus(getApplicationContext(), usuarioAtual.getIdUsuario(), new UsuarioUtils.CheckLockCallback() {
                        @Override
                        public void onBlocked(boolean status) {
                            usuarioAtual.setIndisponivel(status);
                            usuarioDiffDAO.adicionarUsuario(usuarioAtual);
                            adapterAdmsGrupo.updateUsersList(listaUsuariosAdms, new AdapterAdmsComunidade.ListaAtualizadaCallback() {
                                @Override
                                public void onAtualizado() {

                                }
                            });
                            if (percorrido == listaIdAdms.size()) {
                                //Todos adms foram adicionados.
                                callback.onConcluido();
                            }
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                }

                @Override
                public void onError(String mensagem) {
                }
            });
        }
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        }
        recyclerViewAdms.setHasFixedSize(false);
        recyclerViewAdms.setLayoutManager(linearLayoutManager);
        if (adapterAdmsGrupo == null) {
            adapterAdmsGrupo = new AdapterAdmsComunidade(getApplicationContext(), listaUsuariosAdms);
        }
        recyclerViewAdms.setAdapter(adapterAdmsGrupo);
    }


    private void recuperarDadosFundador(String idFundador, GrupoRecuperadoCallback callback) {
        UsuarioUtils.recuperarDadosPadrao(getApplicationContext(), idFundador, new UsuarioUtils.DadosPadraoCallback() {
            @Override
            public void onRecuperado(String nome, String foto, String fundo) {

                if (nome != null && !nome.isEmpty()) {
                    exibirNome(nome, true);
                }
                if (foto == null || foto.isEmpty()) {
                    exibirFoto(UsuarioUtils.FIELD_PHOTO, foto, true, true);
                } else {
                    exibirFoto(UsuarioUtils.FIELD_PHOTO, foto, false, true);
                }

                imgViewFundador.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarPerfil(idFundador);
                    }
                });

                txtViewNomeFundador.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        visitarPerfil(idFundador);
                    }
                });

                callback.onConcluido();
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do fundador do grupo.", getApplicationContext());
                callback.onConcluido();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do fundador do grupo.", getApplicationContext());
                callback.onConcluido();
            }
        });
    }

    private void visitarPerfil(String idFundador) {
        if (idFundador == null
                || idFundador.isEmpty() ||
                idFundador.equals(idUsuario)) {
            return;
        }
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(getApplicationContext(),
                idFundador);
    }

    private void exibirTopicos(ArrayList<String> topicos) {
        // Adiciona um chip para cada hobby
        for (String hobby : topicos) {
            Chip chip = new Chip(linearLayoutTopicos.getContext());
            chip.setText(hobby);
            chip.setChipBackgroundColor(ColorStateList.valueOf(getApplicationContext().getResources().getColor(R.color.friends_color)));
            chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
            chip.setLayoutParams(params);
            chip.setClickable(false);
            linearLayoutTopicos.addView(chip);
        }
    }

    private void irParaEdicao() {
        if (idGrupo.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao ir para a edição do grupo", getApplicationContext());
            onBackPressed();
            return;
        }
        FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                if (grupoAtual.getIdSuperAdmGrupo() != null
                        && !grupoAtual.getIdSuperAdmGrupo().isEmpty()
                        && grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {
                    Intent intent = new Intent(getApplicationContext(), CreateGroupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("dadosEdicao", grupoAtual);
                    intent.putExtra("edit", true);
                    startActivity(intent);
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para editar esse grupo", getApplicationContext());
                }
            }

            @Override
            public void onNaoExiste() {
                ToastCustomizado.toastCustomizadoCurto("Esse grupo não existe mais", getApplicationContext());
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao ir para a edição do grupo", getApplicationContext());
            }
        });
    }

    private void dialogSairDoGrupo() {
        FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                builder.setTitle("Deseja realmente sair do grupo?");
                groupUtils.recuperaCargo(grupoAtual, new GroupUtils.RecuperaCargoCallback() {
                    @Override
                    public void onConcluido(String cargo) {
                        String message = "";
                        if (cargo.equals(CommunityUtils.FOUNDER_POSITION)) {
                            message = "Você será excluído do grupo e você terá que escolher um novo fundador.";
                        } else if (cargo.equals(CommunityUtils.ADM_POSITION)) {
                            message = "Você não participará mais desse grupo e perderá seu cargo de administrador.";
                        } else if (cargo.equals(CommunityUtils.PARTICIPANT_POSITION)) {
                            message = "Você não participará mais desse grupo.";
                        }

                        builder.setMessage(message);
                        builder.setCancelable(true);
                        builder.setPositiveButton("Sair do grupo", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (cargo.equals(CommunityUtils.FOUNDER_POSITION)) {
                                    sheetDialogSairDoGrupo();
                                } else {
                                    sairDoGrupo(grupoAtual);
                                }
                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        dialog = builder.create();
                        dialog.show();
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizado(message, getApplicationContext());
                    }
                });
            }

            @Override
            public void onNaoExiste() {
                ToastCustomizado.toastCustomizadoCurto("Esse grupo não existe mais", getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao sair do grupo. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void sheetDialogSairDoGrupo() {
        bottomSheetDialogSair.show();
        bottomSheetDialogSair.setCancelable(true);
        txtViewEscolherFundador = bottomSheetDialogSair.findViewById(R.id.txtViewEscolherFundador);
        txtViewCancelarSaida = bottomSheetDialogSair.findViewById(R.id.txtViewCancelarSaida);
        txtViewEscolherFundador.setText("Escolher um novo fundador e sair do grupo");

        txtViewEscolherFundador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gerenciarUsuarios(CommunityUtils.FUNCTION_NEW_FOUNDER);
            }
        });

        txtViewCancelarSaida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetDialogSair != null
                        && bottomSheetDialogSair.isShowing()) {
                    bottomSheetDialogSair.dismiss();
                }
            }
        });
    }

    private void sairDoGrupo(Grupo grupoAlvo) {
        exibirProgressDialog("sair");
        groupUtils.sairDoGrupo(idGrupo, idUsuario, new GroupUtils.SairDoGrupoCallback() {
            @Override
            public void onConcluido() {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Você deixou de participar do grupo com sucesso.", getApplicationContext());
                irParaHome();
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao sair do grupo.", "Code:", message), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void clickListeners() {
        imgBtnConfigGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupMenuConfig != null) {
                    popupMenuConfig.show();
                }
            }
        });
        btnEditarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaEdicao();
            }
        });
        btnSairDoGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSairDoGrupo();
            }
        });
        btnDeletarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogExlusao();
            }
        });
        btnGerenciarUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gerenciarUsuarios(CommunityUtils.FUNCTION_SET);
            }
        });
        btnVerParticipantes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailsActivity.this, GroupParticipantsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idGrupo", idGrupo);
                startActivity(intent);
            }
        });
        imgBtnIncBackBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void exibirProgressDialog(String tipoMensagem) {
        switch (tipoMensagem) {
            case "sair":
                progressDialog.setMessage("Saindo do grupo, aguarde....");
                break;
            case "excluir":
                progressDialog.setMessage("Exluindo seu grupo, aguarde....");
                break;
            case "bloquear":
                progressDialog.setMessage("Bloqueando grupo, aguarde....");
                break;
            case "desbloquear":
                progressDialog.setMessage("Desbloqueando grupo, aguarde....");
                break;
            case "denunciar":
                progressDialog.setMessage("Ajustando denúncia, aguarde....");
                break;
        }
        if (!GroupDetailsActivity.this.isFinishing()) {
            progressDialog.show();
        }
    }

    public void ocultarProgressDialog() {
        if (progressDialog != null && !GroupDetailsActivity.this.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void irParaHome() {
        IntentUtils.irParaNavigation(GroupDetailsActivity.this, getApplicationContext());
    }

    private void configurarBottomSheetDialog() {
        bottomSheetDialogGerenciar = new BottomSheetDialog(GroupDetailsActivity.this);
        bottomSheetDialogGerenciar.setContentView(R.layout.bottom_sheet_dialog_gerenciar_grupo);

        bottomSheetDialogSair = new BottomSheetDialog(GroupDetailsActivity.this);
        bottomSheetDialogSair.setContentView(R.layout.bottom_sheet_sair_do_grupo);
    }

    private void dialogExlusao() {
        builder.setTitle("Deseja realmente excluir seu grupo?");
        builder.setMessage("O grupo será excluído permanentemente e seus participantes também.");
        builder.setCancelable(true);
        builder.setPositiveButton("Sair e excluir grupo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                excluirGrupo();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        dialog = builder.create();
        dialog.show();
    }

    private void excluirGrupo() {
        if (idGrupo.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do grupo", getApplicationContext());
            onBackPressed();
            return;
        }
        FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                if (!grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para excluir esse grupo", getApplicationContext());
                    return;
                }

                exibirProgressDialog("excluir");

                StorageReference imagemRef = storageRef.child("grupos")
                        .child(grupoAtual.getIdGrupo())
                        .child("fotoGrupo.jpeg");

                midiaUtils.removerDoStorage(imagemRef, new MidiaUtils.RemoverDoStorageCallback() {
                    @Override
                    public void onRemovido() {
                        groupUtils.excluirGrupo(getApplicationContext(), idGrupo, new GroupUtils.ExcluirGrupoCallback() {
                            @Override
                            public void onConcluido() {
                                ocultarProgressDialog();
                                ToastCustomizado.toastCustomizadoCurto("Grupo excluido com sucesso.", getApplicationContext());
                                irParaHome();
                            }

                            @Override
                            public void onError(String message) {
                                ocultarProgressDialog();
                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir o grupo. Tente novamente.", getApplicationContext());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir o grupo. Tente novamente.", getApplicationContext());
                    }
                });
            }

            @Override
            public void onNaoExiste() {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Esse grupo não existe mais", getApplicationContext());
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir o seu grupo", getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void gerenciarUsuarios(String tipoGerenciamento) {
        if (tipoGerenciamento == null || tipoGerenciamento.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao executar a função. Tente novamente.", getApplicationContext());
            return;
        }
        FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                    irParaGerenciamento(tipoGerenciamento, grupoAtual);
                } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_SET)) {
                    abrirDialogGerenciamento(grupoAtual);
                }
            }

            @Override
            public void onNaoExiste() {
                ToastCustomizado.toastCustomizadoCurto("Esse grupo não existe mais", getApplicationContext());
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao sair do grupo. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void abrirDialogGerenciamento(Grupo grupoAtual) {

        if (grupoAtual.getIdSuperAdmGrupo().equals(idUsuario)) {
            founder = true;
        } else if (grupoAtual.getAdmsGrupo() != null
                && !grupoAtual.getAdmsGrupo().isEmpty()
                && grupoAtual.getAdmsGrupo().contains(idUsuario)) {
            administrator = true;
        }

        if (!founder && !administrator) {
            ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para gerenciar esse grupo.", getApplicationContext());
            return;
        }

        bottomSheetDialogGerenciar.show();
        bottomSheetDialogGerenciar.setCancelable(true);

        btnViewAddUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewAddUserGrupo);
        btnViewRemoverUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewRemoverUserGrupo);
        btnViewPromoverUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewPromoverUserGrupo);
        btnViewDespromoverUserGrupo = bottomSheetDialogGerenciar.findViewById(R.id.btnViewDespromoverUserGrupo);

        //Somente chega nessa etapa o usuário que possuir algum cargo.
        if (grupoAtual.getNrParticipantes() < GroupUtils.MAX_NUMBER_PARTICIPANTS) {
            //Somente é permitido convidar usuários se o limite de participantes não foi atingido.
            if (!chatComunidade) {
                btnViewAddUserGrupo.setVisibility(View.VISIBLE);
            }
        }

        if (grupoAtual.getNrParticipantes() > 0) {
            if (founder && grupoAtual.getNrAdms() < CommunityUtils.MAX_NUMBER_ADMS) {
                //Há participantes que não são adms e o limite ainda não foi atingido.
                if (grupoAtual.getNrParticipantes() > grupoAtual.getNrAdms()
                        || grupoAtual.getNrAdms() <= 0) {
                    if (!chatComunidade) {
                        btnViewPromoverUserGrupo.setVisibility(View.VISIBLE);
                    }
                }
            }
            if (founder || administrator && grupoAtual.getNrAdms() > grupoAtual.getNrParticipantes()) {
                //ADM e o fundador podem remover usuários, no caso de o usuário
                //atual for um ADM ele só pode remover usuários que não são ADMS.
                if (!chatComunidade) {
                    btnViewRemoverUserGrupo.setVisibility(View.VISIBLE);
                }
            }

            if (founder && grupoAtual.getNrAdms() > 0 && !chatComunidade) {
                btnViewDespromoverUserGrupo.setVisibility(View.VISIBLE);
            }
        }

        btnViewAddUserGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder && !administrator) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para gerenciar esse grupo.", getApplicationContext());
                    btnGerenciarUsuarios.setVisibility(View.GONE);
                    return;
                }
                Intent intent = new Intent(GroupDetailsActivity.this, AddGroupUsersActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idGrupo", idGrupo);
                startActivity(intent);
            }
        });
        btnViewRemoverUserGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder && !administrator) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para executar essa função.", getApplicationContext());
                    btnViewRemoverUserGrupo.setVisibility(View.GONE);
                    return;
                }
                irParaGerenciamento(CommunityUtils.FUNCTION_REMOVE, grupoAtual);
            }
        });
        btnViewPromoverUserGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para executar essa função.", getApplicationContext());
                    btnViewPromoverUserGrupo.setVisibility(View.GONE);
                    return;
                }
                irParaGerenciamento(CommunityUtils.FUNCTION_PROMOTE, grupoAtual);
            }
        });
        btnViewDespromoverUserGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para executar essa função.", getApplicationContext());
                    btnViewDespromoverUserGrupo.setVisibility(View.GONE);
                    return;
                }
                irParaGerenciamento(CommunityUtils.FUNCTION_DEMOTING, grupoAtual);
            }
        });
    }

    private void irParaGerenciamento(String tipoGerenciamento, Grupo grupoAtual) {
        if (grupoAtual != null && tipoGerenciamento != null && !tipoGerenciamento.isEmpty()) {
            DatabaseReference lockGroup = firebaseRef.child("lockGroupManagement")
                    .child(idGrupo).child("idUsuario");
            lockGroup.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        String idGerenciador = snapshot.getValue(String.class);
                        if (idGerenciador != null && !idGerenciador.isEmpty()) {
                            UsuarioUtils.recuperarNome(getApplicationContext(), idGerenciador, new UsuarioUtils.RecuperarNomeCallback() {
                                @Override
                                public void onRecuperado(String nome) {
                                    ToastCustomizado.toastCustomizado(String.format("%s %s%s  ", "Gerenciamento já em andamento por", nome, ", aguarde até que seja concluído."), getApplicationContext());
                                }

                                @Override
                                public void onError(String message) {
                                    ToastCustomizado.toastCustomizado(String.format("%s %s%s  ", "Gerenciamento já em andamento por", "outro usuário", ", aguarde até que seja concluído."), getApplicationContext());
                                }
                            });
                        } else {
                            lockGroup.removeValue();
                            ToastCustomizado.toastCustomizado("Ocorre um erro ao ir para o gerenciamento do grupo. Tente novamente.", getApplicationContext());
                        }
                    } else {
                        HashMap<String, Object> operacoes = new HashMap<>();
                        String caminhoLock = "/lockGroupManagement/" + idGrupo + "/idUsuario";
                        operacoes.put(caminhoLock, idUsuario);
                        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    Intent intent = new Intent(GroupDetailsActivity.this, GroupManagementActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("idGrupo", grupoAtual.getIdGrupo());
                                    intent.putExtra("idSuperAdmGrupo", grupoAtual.getIdSuperAdmGrupo());
                                    intent.putExtra("tipoGerenciamento", tipoGerenciamento);
                                    startActivity(intent);
                                } else {
                                    ToastCustomizado.toastCustomizado(String.format("%s %s %s", "Ocorreu um erro ao ir para o gerenciamento do grupo.", "Code:", error.getCode()), getApplicationContext());
                                }
                            }
                        });
                    }
                    lockGroup.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastCustomizado.toastCustomizado(String.format("%s %s %s", "Ocorreu um erro ao ir para o gerenciamento do grupo.", "Code:", error.getCode()), getApplicationContext());
                }
            });
        } else {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do grupo.", getApplicationContext());
            onBackPressed();
        }
    }

    private void configMenuSuperior() {
        popupMenuConfig = new PopupMenu(getApplicationContext(), imgBtnConfigGrupo);
        popupMenuConfig.getMenuInflater().inflate(R.menu.popup_menu_configs_bloquear_denunciar, popupMenuConfig.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenuConfig.setForceShowIcon(true);
        }

        acompanhaBlock();
        acompanhaDenuncia();

        imgBtnConfigGrupo.setOnClickListener(new View.OnClickListener() {
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
                        //Ajustar para desbloquear também.
                        bloquearGrupo();
                        break;
                    case R.id.itemDenunciarBloquear:
                        ToastCustomizado.toastCustomizadoCurto("DENUNCIAR E BLOQUEAR", getApplicationContext());
                        prepararDenuncia();
                        break;
                }
                return false;
            }
        });
    }

    private void bloquearGrupo() {
        if (limiteBloqueio == 4) {
            ToastCustomizado.toastCustomizado("Aguarde um momento para o uso dessa função.", getApplicationContext());
            return;
        }
        groupUtils.verificaSeEParticipante(idGrupo, idUsuario, new GroupUtils.VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    if (!isGrupoBloqueado()) {
                        ToastCustomizado.toastCustomizadoCurto("Você precisa sair do grupo para que seja possível bloquear esse grupo", getApplicationContext());
                    }
                    return;
                }
                exibirProgressDialog("desbloquear");
                if (isGrupoBloqueado()) {
                    groupUtils.desbloquearGrupo(idGrupo, new GroupUtils.DesbloquearGrupoCallback() {
                        @Override
                        public void onDesbloqueado() {
                            limiteBloqueio++;
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("O grupo foi desbloqueado com sucesso.", getApplicationContext());
                        }

                        @Override
                        public void onError(String message) {
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao desbloquear o grupo.", getApplicationContext());
                        }
                    });
                } else {
                    exibirProgressDialog("bloquear");
                    groupUtils.bloquearGrupo(idGrupo, new GroupUtils.BloquearGrupoCallback() {
                        @Override
                        public void onBloqueado() {
                            limiteBloqueio++;
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Bloqueado com sucesso", getApplicationContext());
                        }

                        @Override
                        public void onError(String message) {
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorre um erro ao bloquear o grupo. Tente novamente.", getApplicationContext());
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Ocorre um erro ao bloquear o grupo. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void prepararDenuncia() {
        groupUtils.verificaSeEParticipante(idGrupo, idUsuario, new GroupUtils.VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    ToastCustomizado.toastCustomizadoCurto("Você precisa sair do grupo para que seja possível bloquear e denunciar esse grupo", getApplicationContext());
                    return;
                }
                exibirProgressDialog("denunciar");
                if (isGrupoBloqueado()) {
                    //Não precisa bloquear o grupo.
                    enviarDenuncia();
                } else {
                    groupUtils.bloquearGrupo(idGrupo, new GroupUtils.BloquearGrupoCallback() {
                        @Override
                        public void onBloqueado() {
                            enviarDenuncia();
                        }

                        @Override
                        public void onError(String message) {
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorre um erro ao bloquear o grupo. Tente novamente.", getApplicationContext());
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao executar a função desejada.", getApplicationContext());
            }
        });
    }

    private void configDenuncia() {
        intentDenuncia.setType("message/rfc822");
        intentDenuncia.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
        intentDenuncia.putExtra(Intent.EXTRA_SUBJECT, "Denúncia - " + "Informe o motivo da denúncia" + idGrupo);
        intentDenuncia.putExtra(Intent.EXTRA_TEXT, "Descreva sua denúncia nesse campo e anexe as provas no email," +
                " por favor não apague o identificador da denúncia que está no assunto da mensagem");
        resultLauncher.launch(intentDenuncia);
    }

    private void resultadoIntent() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    irParaHome();
                });
    }

    private void enviarDenuncia() {
        groupUtils.enviarDenunciaGrupo(idGrupo, new GroupUtils.EnviarDenunciaCallback() {
            @Override
            public void onConcluido() {
                ocultarProgressDialog();
                configDenuncia();
            }

            @Override
            public void onJaExisteDenuncia() {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizado("Você já denunciou esse grupo anteriormente.", getApplicationContext());
                popupMenuConfig.getMenu().getItem(1).setVisible(false);
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao denúnciar o grupo. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void acompanhaBlock() {
        if (listenerBlock != null) {
            return;
        }
        verificaBlockRef = firebaseRef.child("blockGroup")
                .child(idUsuario).child(idGrupo);
        listenerBlock = verificaBlockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (popupMenuConfig != null) {
                        setGrupoBloqueado(true);
                        popupMenuConfig.getMenu().getItem(0).setTitle("Desbloquear");
                        popupMenuConfig.getMenu().getItem(1).setTitle("Denunciar");
                    }
                } else {
                    if (popupMenuConfig != null) {
                        setGrupoBloqueado(false);
                        popupMenuConfig.getMenu().getItem(0).setTitle("Bloquear");
                        popupMenuConfig.getMenu().getItem(1).setTitle("Denunciar e bloquear");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void acompanhaDenuncia() {
        if (listenerDenuncia != null || popupMenuConfig == null) {
            return;
        }
        verificaDenunciaRef = firebaseRef.child("groupReports")
                .child(idGrupo).child(idUsuario);
        listenerDenuncia = verificaDenunciaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popupMenuConfig.getMenu().getItem(1).setVisible(snapshot.getValue() == null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                popupMenuConfig.getMenu().getItem(1).setVisible(false);
            }
        });
    }

    private void exibirSpinKit(String campo, boolean fundador) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals(UsuarioUtils.FIELD_PHOTO)) {
                if (fundador) {
                    ProgressBarUtils.exibirProgressBar(spinKitViewFundador, GroupDetailsActivity.this);
                } else {
                    ProgressBarUtils.exibirProgressBar(spinKitViewFoto, GroupDetailsActivity.this);
                }
            }
        }
    }

    private void ocultarSpinKit(String campo, boolean fundador) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals(UsuarioUtils.FIELD_PHOTO)) {
                if (fundador) {
                    ProgressBarUtils.ocultarProgressBar(spinKitViewFundador, GroupDetailsActivity.this);
                } else {
                    ProgressBarUtils.ocultarProgressBar(spinKitViewFoto, GroupDetailsActivity.this);
                }
            }
        }
    }

    private void inicializarComponentes() {
        toolbarIncBlack = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackBlack = findViewById(R.id.imgBtnIncBackBlack);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
        imgViewFoto = findViewById(R.id.imgViewIncPhoto);
        txtViewNome = findViewById(R.id.txtViewNomeGrupoDetalhes);
        spinKitViewFoto = findViewById(R.id.spinKitLoadPhotoUser);
        recyclerViewAdms = findViewById(R.id.recyclerViewAdmsGrupo);
        btnVerParticipantes = findViewById(R.id.btnVerParticipantesGrupo);
        imgBtnVerParticipantes = findViewById(R.id.imgBtnVerParticipantes);
        txtViewNrParticipantes = findViewById(R.id.txtViewNrParticipantesGrupo);
        txtViewNrAdms = findViewById(R.id.txtViewNrAdmsGrupoDetalhes);
        imgViewFundador = findViewById(R.id.imgViewFundadorGrupoDetalhes);
        txtViewNomeFundador = findViewById(R.id.txtViewNomeFundadorGrupoDetalhes);
        spinKitViewFundador = findViewById(R.id.spinKitLoadPhotoUserFundador);
        txtViewDesc = findViewById(R.id.txtViewDescGrupoDetalhes);
        linearLayoutTopicos = findViewById(R.id.linearLayoutTopicosGrupo);
        txtViewPrivacidade = findViewById(R.id.txtViewPrivacidadeGrupoDetalhes);
        btnEditarGrupo = findViewById(R.id.btnEditarGrupo);
        btnDeletarGrupo = findViewById(R.id.btnDeletarGrupo);
        btnSairDoGrupo = findViewById(R.id.btnSairDoGrupo);
        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarParticipantesGrupo);
        imgBtnConfigGrupo = findViewById(R.id.imgBtnConfigGrupo);
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public boolean isGrupoBloqueado() {
        return grupoBloqueado;
    }

    public void setGrupoBloqueado(boolean grupoBloqueado) {
        this.grupoBloqueado = grupoBloqueado;
    }
}