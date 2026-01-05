package com.example.ogima.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterAdmsComunidade;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.king.zxing.CaptureActivity;
import com.zhihu.matisse.internal.entity.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommunityDetailsActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private String idUsuario = "";
    private ImageView imgViewFoto, imgViewFundo, imgViewFundador;
    private TextView txtViewNome, txtViewNrParticipantes, txtViewNrAdms,
            txtViewNomeFundador, txtViewDesc, txtViewPrivacidade;
    private SpinKitView spinKitViewFoto, spinKitViewFundo, spinKitViewFundador;
    private Button btnVerParticipantes, btnEditarComunidade, btnDeletarComunidade,
            btnSairDaComunidade, btnGerenciarUsuarios;
    private ImageButton imgBtnVerParticipantes, imgBtnConfigComunidade;
    private boolean statusEpilepsia = true;
    private String msgErroAoExibirDetalhes = "", msgErroAoRecuperarDados = "";
    private Query comunidadeRef, admsRef;
    private ChildEventListener childListenerComunidade, childListenerAdms;
    private String idComunidade = "";
    private FirebaseUtils firebaseUtils = new FirebaseUtils();
    private boolean dadosRecuperados = false;
    private List<Usuario> listaUsuariosAdms = new ArrayList<>();
    private int percorrido = 0;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerViewAdms;
    private AdapterAdmsComunidade adapterAdmsComunidade;
    private UsuarioDiffDAO usuarioDiffDAO;
    private LinearLayout linearLayoutTopicos;
    private CommunityUtils communityUtils;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;
    private BottomSheetDialog bottomSheetDialogSair, bottomSheetDialogGerenciar;
    //Componentes bottomSheetDialogSair
    private TextView txtViewEscolherFundador, txtViewCancelarSaida;
    //Componentes bottomSheetDialogGerenciar
    private Button btnViewAddUserComunidade, btnViewRemoverUserComunidade, btnViewPromoverUserComunidade,
            btnViewDespromoverUserComunidade;
    private MidiaUtils midiaUtils;
    private StorageReference storageRef;

    private boolean founder = false;
    private boolean administrator = false;
    private PopupMenu popupMenuConfig;
    private DatabaseReference verificaBlockRef, verificaDenunciaRef;
    private ValueEventListener listenerBlock, listenerDenuncia;
    private boolean comunidadeBloqueada = false;
    private final Intent intentDenuncia = new Intent(Intent.ACTION_SEND);
    private ActivityResultLauncher<Intent> resultLauncher;
    private int limiteBloqueio = 0;

    public CommunityDetailsActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        msgErroAoExibirDetalhes = "Ocorreu um erro ao exibir os detalhes da comunidade.";
        msgErroAoRecuperarDados = "Ocorreu um erro ao recuperar os dados da comunidade.";
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public boolean isComunidadeBloqueada() {
        return comunidadeBloqueada;
    }

    public void setComunidadeBloqueada(boolean comunidadeBloqueada) {
        this.comunidadeBloqueada = comunidadeBloqueada;
    }

    public interface DadosIniciaisCallback {
        void onConcluido();

        void onError();
    }

    public interface ComunidadeRecuperadaCallback {
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
        firebaseUtils.removerQueryChildListener(comunidadeRef, childListenerComunidade);
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
        setContentView(R.layout.activity_community_details);
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
            builder = new AlertDialog.Builder(this);
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            midiaUtils = new MidiaUtils(CommunityDetailsActivity.this, getApplicationContext());
            btnDeletarComunidade.setVisibility(View.GONE);
            btnEditarComunidade.setVisibility(View.GONE);
            btnSairDaComunidade.setVisibility(View.GONE);
            btnGerenciarUsuarios.setVisibility(View.GONE);
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaUsuariosAdms, adapterAdmsComunidade);
            UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
                @Override
                public void onConcluido(boolean epilepsia) {
                    setStatusEpilepsia(epilepsia);
                    adapterAdmsComunidade.setStatusEpilepsia(epilepsia);
                    configInicial(new DadosIniciaisCallback() {
                        @Override
                        public void onConcluido() {
                            if (idComunidade.isEmpty()) {
                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade", getApplicationContext());
                                onBackPressed();
                                return;
                            }
                            configurarBottomSheetDialog();
                            configMenuSuperior();
                            clickListeners();
                            recuperarComunidade(new ComunidadeRecuperadaCallback() {
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
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        String title = FormatarContadorUtils.abreviarTexto("Detalhes da comunidade", 32);
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
        if (dados.containsKey("idComunidade")) {
            idComunidade = dados.getString("idComunidade");
            callback.onConcluido();
        } else {
            callback.onError();
        }
    }

    private void recuperarComunidade(ComunidadeRecuperadaCallback callback) {
        comunidadeRef = firebaseRef.child("comunidades").orderByChild("idComunidade")
                .equalTo(idComunidade);

        childListenerComunidade = comunidadeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Comunidade comunidade = snapshot.getValue(Comunidade.class);
                    if (comunidade == null) {
                        ToastCustomizado.toastCustomizadoCurto(msgErroAoRecuperarDados, getApplicationContext());
                        onBackPressed();
                        return;
                    }
                    if (comunidade.getFotoComunidade() != null
                            && !comunidade.getFotoComunidade().isEmpty()) {
                        exibirFoto(UsuarioUtils.FIELD_PHOTO, comunidade.getFotoComunidade(), false, false);
                    } else {
                        exibirFoto(UsuarioUtils.FIELD_PHOTO, "", true, false);
                    }
                    if (comunidade.getFundoComunidade() != null
                            && !comunidade.getFundoComunidade().isEmpty()) {
                        exibirFoto(UsuarioUtils.FIELD_BACKGROUND, comunidade.getFundoComunidade(), false, false);
                    } else {
                        exibirFoto(UsuarioUtils.FIELD_BACKGROUND, "", false, false);
                    }

                    if (comunidade.getNomeComunidade() != null
                            && !comunidade.getNomeComunidade().isEmpty()) {
                        exibirNome(comunidade.getNomeComunidade(), false);
                    }

                    int nrParticipantes = 0;
                    if (comunidade.getNrParticipantes() != -1) {
                        nrParticipantes = (int) comunidade.getNrParticipantes();
                    }

                    txtViewNrParticipantes.setText(String.format("%d%s%d", nrParticipantes, "/", CommunityUtils.MAX_NUMBER_PARTICIPANTS));

                    int nrAdms = 0;
                    if (comunidade.getNrAdms() != -1) {
                        nrAdms = (int) comunidade.getNrAdms();
                    }

                    txtViewNrAdms.setText(String.format("%d%s%d", nrAdms, "/", CommunityUtils.MAX_NUMBER_ADMS));

                    if (comunidade.getAdmsComunidade() != null
                            && comunidade.getAdmsComunidade().size() > 0) {
                        recuperarAdms(comunidade.getAdmsComunidade(), new RecuperarAdmsCallback() {
                            @Override
                            public void onConcluido() {

                            }
                        });
                    }

                    if (comunidade.getIdSuperAdmComunidade() == null
                            || comunidade.getIdSuperAdmComunidade().isEmpty()) {
                        callback.onConcluido();
                        return;
                    }

                    recuperarDadosFundador(comunidade.getIdSuperAdmComunidade(), callback);

                    if (comunidade.getDescricaoComunidade() != null
                            && !comunidade.getDescricaoComunidade().isEmpty()) {
                        txtViewDesc.setText(comunidade.getDescricaoComunidade().trim());
                    }

                    if (comunidade.getTopicos() != null
                            && comunidade.getTopicos().size() > 0) {
                        exibirTopicos(comunidade.getTopicos());
                    }

                    String privacidade;
                    if (comunidade.getComunidadePublica()) {
                        privacidade = getString(R.string.public_community);
                    } else {
                        privacidade = getString(R.string.private_community);
                    }
                    txtViewPrivacidade.setText(privacidade);

                    if (comunidade.getIdSuperAdmComunidade().equals(idUsuario)) {
                        imgBtnConfigComunidade.setVisibility(View.GONE);
                    } else {
                        imgBtnConfigComunidade.setVisibility(View.VISIBLE);
                    }

                    if (comunidade.getIdSuperAdmComunidade().equals(idUsuario)) {
                        btnGerenciarUsuarios.setVisibility(View.VISIBLE);
                        btnEditarComunidade.setVisibility(View.VISIBLE);
                        btnDeletarComunidade.setVisibility(View.VISIBLE);
                        if (comunidade.getNrParticipantes() < 1) {
                            btnDeletarComunidade.setText("Sair e excluir comunidade");
                        } else {
                            btnSairDaComunidade.setText("Sair da comunidade");
                            btnSairDaComunidade.setVisibility(View.VISIBLE);
                        }
                    } else if (comunidade.getAdmsComunidade() != null
                            && comunidade.getAdmsComunidade().size() > 0
                            && comunidade.getAdmsComunidade().contains(idUsuario)) {
                        btnGerenciarUsuarios.setVisibility(View.VISIBLE);
                        btnSairDaComunidade.setText("Sair da comunidade");
                        btnSairDaComunidade.setVisibility(View.VISIBLE);
                    } else {
                        communityUtils.verificaSeEParticipante(comunidade.getIdComunidade(), idUsuario, new CommunityUtils.VerificaParticipanteCallback() {
                            @Override
                            public void onParticipante(boolean status) {
                                if (status) {
                                    //Participante
                                    btnSairDaComunidade.setText("Sair da comunidade");
                                    btnSairDaComunidade.setVisibility(View.VISIBLE);
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
                    Comunidade comunidade = snapshot.getValue(Comunidade.class);
                    if (comunidade != null && comunidade.getIdSuperAdmComunidade() != null
                            && !comunidade.getIdSuperAdmComunidade().isEmpty() &&
                            comunidade.getIdSuperAdmComunidade().equals(idUsuario)) {
                        imgBtnConfigComunidade.setVisibility(View.GONE);
                    } else {
                        imgBtnConfigComunidade.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.community_does_not_exist), getApplicationContext());
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
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao carregar a foto da comunidade.", getApplicationContext());
                        }
                    });

        } else if (campo.equals(UsuarioUtils.FIELD_BACKGROUND)) {
            if (padrao) {
                CommunityUtils.exibirFotoPadrao(getApplicationContext(), imgViewFundo, campo, false);
                return;
            }

            if (url == null || url.isEmpty()) {
                return;
            }
            exibirSpinKit(campo, fundador);
            GlideCustomizado.loadUrlComListener(getApplicationContext(),
                    url, imgViewFundo, android.R.color.transparent,
                    GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                            ocultarSpinKit(campo, fundador);
                        }

                        @Override
                        public void onError(String message) {
                            ocultarSpinKit(campo, fundador);
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao carregar o plano de fundo da comunidade.", getApplicationContext());
                        }
                    });
        }
    }

    private void exibirNome(String nomeAlvo, boolean fundador) {
        if (fundador) {
            String nomeFundador = FormatarContadorUtils.abreviarTexto(nomeAlvo, UsuarioUtils.MAX_NAME_LENGHT_FOUNDER);
            txtViewNomeFundador.setText(nomeFundador);
        } else {
            String nomeComunidade = FormatarContadorUtils.abreviarTexto(nomeAlvo, UsuarioUtils.MAX_COMMUNITY_NAME_LENGHT);
            txtViewNome.setText(nomeComunidade);
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
                            adapterAdmsComunidade.updateUsersList(listaUsuariosAdms, new AdapterAdmsComunidade.ListaAtualizadaCallback() {
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
        if (adapterAdmsComunidade == null) {
            adapterAdmsComunidade = new AdapterAdmsComunidade(getApplicationContext(), listaUsuariosAdms);
        }
        recyclerViewAdms.setAdapter(adapterAdmsComunidade);
    }

    private void recuperarDadosFundador(String idFundador, ComunidadeRecuperadaCallback callback) {
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
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do fundador da comunidade.", getApplicationContext());
                callback.onConcluido();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados do fundador da comunidade.", getApplicationContext());
                callback.onConcluido();
            }
        });
    }

    private void exibirSpinKit(String campo, boolean fundador) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals(UsuarioUtils.FIELD_PHOTO)) {
                if (fundador) {
                    ProgressBarUtils.exibirProgressBar(spinKitViewFundador, CommunityDetailsActivity.this);
                } else {
                    ProgressBarUtils.exibirProgressBar(spinKitViewFoto, CommunityDetailsActivity.this);
                }
            } else if (campo.equals(UsuarioUtils.FIELD_BACKGROUND)) {
                ProgressBarUtils.exibirProgressBar(spinKitViewFundo, CommunityDetailsActivity.this);
            }
        }
    }

    private void ocultarSpinKit(String campo, boolean fundador) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals(UsuarioUtils.FIELD_PHOTO)) {
                if (fundador) {
                    ProgressBarUtils.ocultarProgressBar(spinKitViewFundador, CommunityDetailsActivity.this);
                } else {
                    ProgressBarUtils.ocultarProgressBar(spinKitViewFoto, CommunityDetailsActivity.this);
                }
            } else if (campo.equals(UsuarioUtils.FIELD_BACKGROUND)) {
                ProgressBarUtils.ocultarProgressBar(spinKitViewFundo, CommunityDetailsActivity.this);
            }
        }
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
        if (idComunidade.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao ir para edição de comunidade", getApplicationContext());
            onBackPressed();
            return;
        }
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                if (comunidadeAtual.getIdSuperAdmComunidade() != null
                        && !comunidadeAtual.getIdSuperAdmComunidade().isEmpty()
                        && comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {
                    Intent intent = new Intent(getApplicationContext(), CreateCommunityActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("dadosEdicao", comunidadeAtual);
                    intent.putExtra("edit", true);
                    startActivity(intent);
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para editar essa comunidade", getApplicationContext());
                }
            }

            @Override
            public void onNaoExiste() {
                ToastCustomizado.toastCustomizadoCurto("Essa comunidade não existe mais", getApplicationContext());
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao ir para edição de comunidade", getApplicationContext());
            }
        });
    }

    private void dialogSairDaComunidade() {
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                builder.setTitle("Deseja realmente sair da comunidade?");
                communityUtils.recuperaCargo(comunidadeAtual, new CommunityUtils.RecuperaCargoCallback() {
                    @Override
                    public void onConcluido(String cargo) {
                        String message = "";
                        if (cargo.equals(CommunityUtils.FOUNDER_POSITION)) {
                            message = "Você será excluído da comunidade e você terá que escolher um novo fundador.";
                        } else if (cargo.equals(CommunityUtils.ADM_POSITION)) {
                            message = "Você não participará mais dessa comunidade e perderá seu cargo de administrador.";
                        } else if (cargo.equals(CommunityUtils.PARTICIPANT_POSITION)) {
                            message = "Você não participará mais dessa comunidade.";
                        }

                        builder.setMessage(message);
                        builder.setCancelable(true);
                        builder.setPositiveButton("Sair da comunidade", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (cargo.equals(CommunityUtils.FOUNDER_POSITION)) {
                                    sheetDialogSairDaComunidade();
                                } else {
                                    sairDaComunidade(comunidadeAtual);
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
                ToastCustomizado.toastCustomizadoCurto("Essa comunidade não existe mais", getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao sair da comunidade. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void sheetDialogSairDaComunidade() {
        bottomSheetDialogSair.show();
        bottomSheetDialogSair.setCancelable(true);
        txtViewEscolherFundador = bottomSheetDialogSair.findViewById(R.id.txtViewEscolherFundador);
        txtViewCancelarSaida = bottomSheetDialogSair.findViewById(R.id.txtViewCancelarSaida);
        txtViewEscolherFundador.setText("Escolher um novo fundador e sair da comunidade");

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

    private void sairDaComunidade(Comunidade comunidadeAlvo) {
        exibirProgressDialog("sair");
        communityUtils.sairDaComunidade(idComunidade, idUsuario, new CommunityUtils.SairDaComunidadeCallback() {
            @Override
            public void onConcluido() {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Você deixou de participar da comunidade com sucesso.", getApplicationContext());
                irParaListagemDeComunidades();
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizado(String.format("%s %s%s", "Ocorreu um erro ao sair da comunidade.", "Code:", message), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void clickListeners() {
        imgBtnConfigComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupMenuConfig != null) {
                    popupMenuConfig.show();
                }
            }
        });
        btnEditarComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaEdicao();
            }
        });
        btnSairDaComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSairDaComunidade();
            }
        });
        btnDeletarComunidade.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(CommunityDetailsActivity.this, CommunityParticipantsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idComunidade", idComunidade);
                startActivity(intent);
            }
        });
    }

    public void exibirProgressDialog(String tipoMensagem) {
        switch (tipoMensagem) {
            case "sair":
                progressDialog.setMessage("Saindo da comunidade, aguarde....");
                break;
            case "excluir":
                progressDialog.setMessage("Exluindo sua comunidade, aguarde....");
                break;
            case "bloquear":
                progressDialog.setMessage("Bloqueando comunidade, aguarde....");
                break;
            case "desbloquear":
                progressDialog.setMessage("Desbloqueando comunidade, aguarde....");
                break;
            case "denunciar":
                progressDialog.setMessage("Ajustando denúncia, aguarde....");
                break;
        }
        if (!CommunityDetailsActivity.this.isFinishing()) {
            progressDialog.show();
        }
    }


    public void ocultarProgressDialog() {
        if (progressDialog != null && !CommunityDetailsActivity.this.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void irParaListagemDeComunidades() {
        Intent intent = new Intent(CommunityDetailsActivity.this, ListaComunidadesActivityNEW.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void configurarBottomSheetDialog() {
        bottomSheetDialogGerenciar = new BottomSheetDialog(CommunityDetailsActivity.this);
        bottomSheetDialogGerenciar.setContentView(R.layout.bottom_sheet_dialog_gerenciar_grupo);

        bottomSheetDialogSair = new BottomSheetDialog(CommunityDetailsActivity.this);
        bottomSheetDialogSair.setContentView(R.layout.bottom_sheet_sair_do_grupo);
    }

    private void dialogExlusao() {
        builder.setTitle("Deseja realmente excluir sua comunidade?");
        builder.setMessage("A comunidade será excluída permanentemente e seus participantes também.");
        builder.setCancelable(true);
        builder.setPositiveButton("Sair e excluir comunidade", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                excluirComunidade();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        dialog = builder.create();
        dialog.show();
    }

    private void excluirComunidade() {
        if (idComunidade.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade", getApplicationContext());
            onBackPressed();
            return;
        }
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                if (!comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para excluir essa comunidade", getApplicationContext());
                    return;
                }

                exibirProgressDialog("excluir");

                StorageReference imagemRef = storageRef.child("comunidades")
                        .child(comunidadeAtual.getIdComunidade())
                        .child("fotoComunidade.jpeg");

                StorageReference fundoRef = storageRef.child("comunidades")
                        .child(comunidadeAtual.getIdComunidade())
                        .child("fundoComunidade.jpeg");

                midiaUtils.removerDoStorage(imagemRef, new MidiaUtils.RemoverDoStorageCallback() {
                    @Override
                    public void onRemovido() {
                        midiaUtils.removerDoStorage(fundoRef, new MidiaUtils.RemoverDoStorageCallback() {
                            @Override
                            public void onRemovido() {
                                communityUtils.excluirComunidade(getApplicationContext(), idComunidade, new CommunityUtils.ExcluirComunidadeCallback() {
                                    @Override
                                    public void onConcluido() {
                                        ocultarProgressDialog();
                                        ToastCustomizado.toastCustomizadoCurto("Comunidade excluida com sucesso.", getApplicationContext());
                                        irParaListagemDeComunidades();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        ocultarProgressDialog();
                                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir comunidade. Tente novamente.", getApplicationContext());
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                ocultarProgressDialog();
                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir comunidade. Tente novamente.", getApplicationContext());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir comunidade. Tente novamente.", getApplicationContext());
                    }
                });
            }

            @Override
            public void onNaoExiste() {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Essa comunidade não existe mais", getApplicationContext());
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir a sua comunidade", getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void gerenciarUsuarios(String tipoGerenciamento) {
        if (tipoGerenciamento == null || tipoGerenciamento.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao executar a função. Tente novamente.", getApplicationContext());
            return;
        }
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_NEW_FOUNDER)) {
                    irParaGerenciamento(tipoGerenciamento, comunidadeAtual);
                } else if (tipoGerenciamento.equals(CommunityUtils.FUNCTION_SET)) {
                    abrirDialogGerenciamento(comunidadeAtual);
                }
            }

            @Override
            public void onNaoExiste() {
                ToastCustomizado.toastCustomizadoCurto("Essa comunidade não existe mais", getApplicationContext());
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao sair da comunidade. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void abrirDialogGerenciamento(Comunidade comunidadeAtual) {

        if (comunidadeAtual.getIdSuperAdmComunidade().equals(idUsuario)) {
            founder = true;
        } else if (comunidadeAtual.getAdmsComunidade() != null
                && comunidadeAtual.getAdmsComunidade().size() > 0
                && comunidadeAtual.getAdmsComunidade().contains(idUsuario)) {
            administrator = true;
        }

        if (!founder && !administrator) {
            ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para gerenciar essa comunidade.", getApplicationContext());
            return;
        }

        bottomSheetDialogGerenciar.show();
        bottomSheetDialogGerenciar.setCancelable(true);

        btnViewAddUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewAddUserGrupo);
        btnViewRemoverUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewRemoverUserGrupo);
        btnViewPromoverUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewPromoverUserGrupo);
        btnViewDespromoverUserComunidade = bottomSheetDialogGerenciar.findViewById(R.id.btnViewDespromoverUserGrupo);

        //Somente chega nessa etapa o usuário que possuir algum cargo.
        if (comunidadeAtual.getNrParticipantes() < CommunityUtils.MAX_NUMBER_PARTICIPANTS) {
            //Somente é permitido convidar usuários se o limite de participantes não foi atingido.
            btnViewAddUserComunidade.setVisibility(View.VISIBLE);
        }

        if (comunidadeAtual.getNrParticipantes() > 0) {
            if (founder && comunidadeAtual.getNrAdms() < CommunityUtils.MAX_NUMBER_ADMS) {
                //Há participantes que não são adms e o limite ainda não foi atingido.
                if (comunidadeAtual.getNrParticipantes() > comunidadeAtual.getNrAdms()
                        || comunidadeAtual.getNrAdms() <= 0) {
                    btnViewPromoverUserComunidade.setVisibility(View.VISIBLE);
                }
            }
            if (founder || administrator && comunidadeAtual.getNrAdms() > comunidadeAtual.getNrParticipantes()) {
                //ADM e o fundador podem remover usuários, no caso de o usuário
                //atual for um ADM ele só pode remover usuários que não são ADMS.
                btnViewRemoverUserComunidade.setVisibility(View.VISIBLE);
            }

            if (founder && comunidadeAtual.getNrAdms() > 0) {
                btnViewDespromoverUserComunidade.setVisibility(View.VISIBLE);
            }
        }

        btnViewAddUserComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder && !administrator) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para gerenciar essa comunidade.", getApplicationContext());
                    btnGerenciarUsuarios.setVisibility(View.GONE);
                    return;
                }
                Intent intent = new Intent(CommunityDetailsActivity.this, UsersInviteCommunityActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idComunidade", idComunidade);
                startActivity(intent);
            }
        });
        btnViewRemoverUserComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder && !administrator) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para executar essa função.", getApplicationContext());
                    btnViewRemoverUserComunidade.setVisibility(View.GONE);
                    return;
                }
                irParaGerenciamento(CommunityUtils.FUNCTION_REMOVE, comunidadeAtual);
            }
        });
        btnViewPromoverUserComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para executar essa função.", getApplicationContext());
                    btnViewPromoverUserComunidade.setVisibility(View.GONE);
                    return;
                }
                irParaGerenciamento(CommunityUtils.FUNCTION_PROMOTE, comunidadeAtual);
            }
        });
        btnViewDespromoverUserComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!founder) {
                    ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para executar essa função.", getApplicationContext());
                    btnViewDespromoverUserComunidade.setVisibility(View.GONE);
                    return;
                }
                irParaGerenciamento(CommunityUtils.FUNCTION_DEMOTING, comunidadeAtual);
            }
        });
    }

    private void irParaGerenciamento(String tipoGerenciamento, Comunidade comunidadeAtual) {
        if (comunidadeAtual != null && tipoGerenciamento != null && !tipoGerenciamento.isEmpty()) {
            DatabaseReference lockCommunity = firebaseRef.child("lockCommunityManagement")
                    .child(idComunidade).child("idUsuario");
            lockCommunity.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            lockCommunity.removeValue();
                            ToastCustomizado.toastCustomizado("Ocorre um erro ao ir para o gerenciamento da comunidade. Tente novamente.", getApplicationContext());
                        }
                    } else {
                        HashMap<String, Object> operacoes = new HashMap<>();
                        String caminhoLock = "/lockCommunityManagement/" + idComunidade + "/idUsuario";
                        operacoes.put(caminhoLock, idUsuario);
                        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    Intent intent = new Intent(CommunityDetailsActivity.this, ManageCommunityUsersActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("idComunidade", comunidadeAtual.getIdComunidade());
                                    intent.putExtra("tipoGerenciamento", tipoGerenciamento);
                                    startActivity(intent);
                                } else {
                                    ToastCustomizado.toastCustomizado(String.format("%s %s %s", "Ocorreu um erro ao ir para o gerenciamento da comunidade.", "Code:", error.getCode()), getApplicationContext());
                                }
                            }
                        });
                    }
                    lockCommunity.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastCustomizado.toastCustomizado(String.format("%s %s %s", "Ocorreu um erro ao ir para o gerenciamento da comunidade.", "Code:", error.getCode()), getApplicationContext());
                }
            });
        } else {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da comunidade.", getApplicationContext());
            onBackPressed();
        }
    }

    private void configMenuSuperior() {
        popupMenuConfig = new PopupMenu(getApplicationContext(), imgBtnConfigComunidade);
        popupMenuConfig.getMenuInflater().inflate(R.menu.popup_menu_configs_bloquear_denunciar, popupMenuConfig.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenuConfig.setForceShowIcon(true);
        }

        acompanhaBlock();
        acompanhaDenuncia();

        imgBtnConfigComunidade.setOnClickListener(new View.OnClickListener() {
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
                        bloquearComunidade();
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

    private void bloquearComunidade() {
        if (limiteBloqueio == 4) {
            ToastCustomizado.toastCustomizado("Aguarde um momento para o uso dessa função.", getApplicationContext());
            return;
        }
        communityUtils.verificaSeEParticipante(idComunidade, idUsuario, new CommunityUtils.VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    if (!isComunidadeBloqueada()) {
                        ToastCustomizado.toastCustomizadoCurto("Você precisa sair da comunidade para que seja possível bloquear essa comunidade", getApplicationContext());
                    }
                    return;
                }
                exibirProgressDialog("desbloquear");
                if (isComunidadeBloqueada()) {
                    communityUtils.desbloquearComunidade(idComunidade, new CommunityUtils.DesbloquearComunidadeCallback() {
                        @Override
                        public void onDesbloqueado() {
                            limiteBloqueio++;
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("A comunidade foi desbloqueada com sucesso.", getApplicationContext());
                        }

                        @Override
                        public void onError(String message) {
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao desbloquear a comunidade.", getApplicationContext());
                        }
                    });
                } else {
                    exibirProgressDialog("bloquear");
                    communityUtils.bloquearComunidade(idComunidade, new CommunityUtils.BloquearComunidadeCallback() {
                        @Override
                        public void onBloqueado() {
                            limiteBloqueio++;
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Bloqueado com sucesso", getApplicationContext());
                        }

                        @Override
                        public void onError(String message) {
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorre um erro ao bloquear a comunidade. Tente novamente.", getApplicationContext());
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto("Ocorre um erro ao bloquear a comunidade. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void prepararDenuncia() {
        communityUtils.verificaSeEParticipante(idComunidade, idUsuario, new CommunityUtils.VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    ToastCustomizado.toastCustomizadoCurto("Você precisa sair da comunidade para que seja possível bloquear e denunciar essa comunidade", getApplicationContext());
                    return;
                }
                exibirProgressDialog("denunciar");
                if (isComunidadeBloqueada()) {
                    //Não precisa bloquear a comunidade.
                    enviarDenuncia();
                } else {
                    communityUtils.bloquearComunidade(idComunidade, new CommunityUtils.BloquearComunidadeCallback() {
                        @Override
                        public void onBloqueado() {
                            enviarDenuncia();
                        }

                        @Override
                        public void onError(String message) {
                            ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Ocorre um erro ao bloquear a comunidade. Tente novamente.", getApplicationContext());
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
        intentDenuncia.putExtra(Intent.EXTRA_SUBJECT, "Denúncia - " + "Informe o motivo da denúncia" + idComunidade);
        intentDenuncia.putExtra(Intent.EXTRA_TEXT, "Descreva sua denúncia nesse campo e anexe as provas no email," +
                " por favor não apague o identificador da denúncia que está no assunto da mensagem");
        resultLauncher.launch(intentDenuncia);
    }

    private void resultadoIntent() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    irParaListagemDeComunidades();
                });
    }

    private void enviarDenuncia() {
        communityUtils.enviarDenunciaComunidade(idComunidade, new CommunityUtils.EnviarDenunciaCallback() {
            @Override
            public void onConcluido() {
                ocultarProgressDialog();
                configDenuncia();
            }

            @Override
            public void onJaExisteDenuncia() {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizado("Você já denunciou essa comunidade anteriormente.", getApplicationContext());
                popupMenuConfig.getMenu().getItem(1).setVisible(false);
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizado("Ocorreu um erro ao denúnciar a comunidade. Tente novamente.", getApplicationContext());
            }
        });
    }

    private void acompanhaBlock() {
        if (listenerBlock != null) {
            return;
        }
        verificaBlockRef = firebaseRef.child("blockCommunity")
                .child(idUsuario).child(idComunidade);
        listenerBlock = verificaBlockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (popupMenuConfig != null) {
                        setComunidadeBloqueada(true);
                        popupMenuConfig.getMenu().getItem(0).setTitle("Desbloquear");
                        popupMenuConfig.getMenu().getItem(1).setTitle("Denunciar");
                    }
                } else {
                    if (popupMenuConfig != null) {
                        setComunidadeBloqueada(false);
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
        verificaDenunciaRef = firebaseRef.child("communityReports")
                .child(idComunidade).child(idUsuario);
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

    private void inicializarComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgViewFoto = findViewById(R.id.imgViewIncPhoto);
        imgViewFundo = findViewById(R.id.imgViewFundoDetalheComunidade);
        txtViewNome = findViewById(R.id.txtViewNomeComunidadeDetalhes);
        spinKitViewFoto = findViewById(R.id.spinKitLoadPhotoUser);
        spinKitViewFundo = findViewById(R.id.spinKitLoadFundoDetalheComunidade);
        recyclerViewAdms = findViewById(R.id.recyclerViewAdmsComunidade);
        btnVerParticipantes = findViewById(R.id.btnVerParticipantesComunidade);
        imgBtnVerParticipantes = findViewById(R.id.imgBtnVerParticipantes);
        txtViewNrParticipantes = findViewById(R.id.txtViewNrParticipantesComunidade);
        txtViewNrAdms = findViewById(R.id.txtViewNrAdmsComunidadeDetalhes);
        imgViewFundador = findViewById(R.id.imgViewFundadorComunidadeDetalhes);
        txtViewNomeFundador = findViewById(R.id.txtViewNomeFundadorComunidadeDetalhes);
        spinKitViewFundador = findViewById(R.id.spinKitLoadPhotoUserFundador);
        txtViewDesc = findViewById(R.id.txtViewDescComunidadeDetalhes);
        linearLayoutTopicos = findViewById(R.id.linearLayoutTopicosComunidade);
        txtViewPrivacidade = findViewById(R.id.txtViewPrivacidadeComunidadeDetalhes);
        btnEditarComunidade = findViewById(R.id.btnEditarComunidade);
        btnDeletarComunidade = findViewById(R.id.btnDeletarComunidade);
        btnSairDaComunidade = findViewById(R.id.btnSairDaComunidade);
        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarParticipantesComunidade);
        imgBtnConfigComunidade = findViewById(R.id.imgBtnConfigComunidade);
    }
}