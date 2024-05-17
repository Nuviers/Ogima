package com.example.ogima.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ogima.R;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Grupo;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "", idGrupo = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private ImageView imgViewCadFoto;
    private SpinKitView progressFoto;
    private Button btnViewSalvar, btnGrupoPublico, btnGrupoParticular,
            btnViewDefinirTopicos;
    private boolean edicao = false;
    private String tipoMidiaPermissao = "";
    private MidiaUtils midiaUtils;
    private GroupUtils groupUtils;
    private ProgressDialog progressDialog;
    private String campoSelecionado = "";
    private Uri uriFoto = null;
    private final static String TAG = "CreateGroupActivity";
    private String tamanhoGif = "";
    private boolean midiaFotoGif = false;
    private StorageReference storageRef;
    private AlertDialog.Builder builder;
    private boolean statusEpilepsia = true;
    private Grupo grupoEdicao;
    private ImageButton imgBtnGaleria, imgBtnCamera,
            imgBtnGif, imgBtnDeleteFoto;
    private EditText edtTxtNome, edtTxtDesc;
    private TextView txtViewLimiteNome, txtViewLimiteDesc;
    private Boolean limiteTopicosPermitido = false;
    private String[] topicosGrupo;
    //Verifica quais dos tópicos foram selecionados.
    private boolean[] checkedItems;
    private ArrayList<String> topicosSelecionados;
    private boolean grupoPublico = true;
    private HashMap<String, Object> dadosGrupo;
    private ArrayList<String> participantes;
    private String caminhoGrupo = "";
    private int interessesConcluidos = 0;
    private HashMap<String, Object> interessesAnteriores;
    private String msgSalvamento = "", msgEdicao = "", msgError = "";
    private CommunityUtils communityUtils;
    private int contadorParticipantes = 0;
    private ArrayList<String> idsARemover;
    private int contadorRemocao = 0;

    private interface DadosIniciaisCallback {
        void onConcluido();

        void onError(String message);
    }

    private interface RecuperarTimeStampCallback {
        void onRecuperado(long timestampNegativo, String data);

        void onError(String message);
    }

    private interface AjustarTopicosCallback {
        void onConcluido();

        void onError(String message);
    }

    private interface PrepararSalvamentoCallback {
        void onConcluido();

        void onError(String message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimparCacheUtils limparCacheUtils = new LimparCacheUtils();
        limparCacheUtils.clearAppCache(getApplicationContext());
    }

    public CreateGroupActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        inicializarComponentes();
        configInicial();
    }

    private void configInicial() {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        toolbarIncPadrao.setTitle("");
        txtViewTitleToolbar.setText(getString(R.string.group));
        String urlTeste = "https://media1.tenor.com/m/Jec0CtCDRCsAAAAC/nero-devil-may-cry.gif";
        GlideCustomizado.loadUrl(getApplicationContext(),
                urlTeste, imgViewCadFoto, android.R.color.transparent,
                GlideCustomizado.CIRCLE_CROP, false, false);
        topicosGrupo = getResources().getStringArray(R.array.interests_array);
        checkedItems = new boolean[topicosGrupo.length];
        topicosSelecionados = new ArrayList<>();
        interessesAnteriores = new HashMap<>();
        if (idUsuario == null || idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }
        clickListeners();
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean epilepsia) {
                setStatusEpilepsia(epilepsia);
                configInicial(new DadosIniciaisCallback() {
                    @Override
                    public void onConcluido() {
                        if (grupoPublico) {
                            btnGrupoPublico.setVisibility(View.VISIBLE);
                            btnGrupoPublico.performClick();
                        } else {
                            btnGrupoParticular.setVisibility(View.VISIBLE);
                            btnGrupoParticular.performClick();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_group_setting), getApplicationContext());
                        onBackPressed();
                    }
                });
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), getApplicationContext());
                onBackPressed();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void configInicial(DadosIniciaisCallback callback) {
        txtViewLimiteNome.setText(String.format("%s%s", "0/", CommunityUtils.MAX_LENGHT_NAME));
        txtViewLimiteDesc.setText(String.format("%s%s", "0/", CommunityUtils.MAX_LENGHT_DESCRIPTION));
        String msgDefinirTopicos = getString(R.string.set_topics_community, "0", String.valueOf(CommunityUtils.MAX_LENGTH_TOPICOS));
        btnViewDefinirTopicos.setText(msgDefinirTopicos);
        msgSalvamento = getString(R.string.saved_successfully);
        msgEdicao = getString(R.string.successfully_changed);
        msgError = getString(R.string.an_error_has_occurred);
        dadosGrupo = new HashMap<>();
        participantes = new ArrayList<>();
        builder = new AlertDialog.Builder(CreateGroupActivity.this);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        progressDialog = new ProgressDialog(CreateGroupActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        midiaUtils = new MidiaUtils(CreateGroupActivity.this.getSupportFragmentManager(), progressDialog, CreateGroupActivity.this,
                getApplicationContext(), SizeUtils.MAX_FILE_SIZE_IMAGEM, 0, 0);
        midiaUtils.limitarCaracteres(edtTxtNome, txtViewLimiteNome, CommunityUtils.MAX_LENGHT_NAME);
        midiaUtils.limitarCaracteres(edtTxtDesc, txtViewLimiteDesc, CommunityUtils.MAX_LENGHT_DESCRIPTION);
        groupUtils = new GroupUtils(CreateGroupActivity.this, getApplicationContext());
        communityUtils = new CommunityUtils(CreateGroupActivity.this, getApplicationContext());
        configBundle(callback);
    }

    private void configBundle(DadosIniciaisCallback callback) {
        Bundle dados = getIntent().getExtras();
        if (dados == null) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_group_setting), getApplicationContext());
            onBackPressed();
            return;
        }

        if (dados.containsKey("idParticipantes")) {
            participantes = dados.getStringArrayList("idParticipantes");
        }

        groupUtils.configurarBundle(dados, new GroupUtils.ConfigBundleCallback() {
            @Override
            public void onCadastro() {
                edicao = false;
                if (dados.containsKey("grupoPublico")) {
                    grupoPublico = dados.getBoolean("grupoPublico");
                } else {
                    grupoPublico = true;
                }
                btnViewSalvar.setText(getString(R.string.create_group));
                DatabaseReference gerarIdGrupoRef = firebaseRef.child("grupos");
                idGrupo = FirebaseUtils.retornarIdRandom(gerarIdGrupoRef);
                caminhoGrupo = "/grupos/" + idGrupo + "/";
                dadosGrupo.put(caminhoGrupo + "idGrupo", idGrupo);
                callback.onConcluido();
            }

            @Override
            public void onEdicao(Grupo dadosEdicao) {
                edicao = true;
                grupoPublico = dadosEdicao.getGrupoPublico();
                btnViewSalvar.setText(getString(R.string.save));
                grupoEdicao = dadosEdicao;
                idGrupo = grupoEdicao.getIdGrupo();
                caminhoGrupo = "/grupos/" + idGrupo + "/";
                exibirFotosEdicao();
                preencherDadosEdicao();
                callback.onConcluido();
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_group_setting), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void exibirFotosEdicao() {
        if (grupoEdicao.getFotoGrupo() != null
                && !grupoEdicao.getFotoGrupo().isEmpty()) {
            campoSelecionado = "foto";
            exibirSpinKit("foto");
            GlideCustomizado.loadUrlComListener(getApplicationContext(),
                    grupoEdicao.getFotoGrupo(), imgViewCadFoto, android.R.color.transparent,
                    GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                            ocultarSpinKit("foto");
                        }

                        @Override
                        public void onError(String message) {
                            ocultarSpinKit("foto");
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_group_photo), getApplicationContext());
                        }
                    });
            visibilidadeImgBtnDelete(true, "foto");
        } else {
            visibilidadeImgBtnDelete(false, "foto");
        }
    }

    private void preencherDadosEdicao() {
        if (edicao) {
            String nome = grupoEdicao.getNomeGrupo();
            String descricao = grupoEdicao.getDescricaoGrupo();
            if (nome != null && !nome.isEmpty()) {
                edtTxtNome.setText(grupoEdicao.getNomeGrupo());
            }
            if (descricao != null && !descricao.isEmpty()) {
                edtTxtDesc.setText(grupoEdicao.getDescricaoGrupo());
            }
            if (grupoEdicao.getTopicos() != null
                    && grupoEdicao.getTopicos().size() > 0) {
                String msgDefinirTopicos = getString(R.string.set_topics_community, String.valueOf(grupoEdicao.getTopicos().size()), String.valueOf(CommunityUtils.MAX_LENGTH_TOPICOS));
                btnViewDefinirTopicos.setText(msgDefinirTopicos);
            }

            for (int i = 0; i < topicosGrupo.length; i++) {
                String topicosAnteriores = topicosGrupo[i];
                if (grupoEdicao.getTopicos().contains(topicosAnteriores)) {
                    checkedItems[i] = true;
                }
            }
        }
    }

    private void visibilidadeImgBtnDelete(boolean exibir, String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                if (exibir) {
                    imgBtnDeleteFoto.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFoto.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void exibirSpinKit(String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                ProgressBarUtils.exibirProgressBar(progressFoto, CreateGroupActivity.this);
            }
        }
    }

    private void ocultarSpinKit(String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                ProgressBarUtils.ocultarProgressBar(progressFoto, CreateGroupActivity.this);
            }
        }
    }

    private void clickListeners() {
        imgBtnGaleria.setOnClickListener(this);
        imgBtnCamera.setOnClickListener(this);
        imgBtnGif.setOnClickListener(this);
        imgBtnGaleria.setOnClickListener(this);
        imgBtnCamera.setOnClickListener(this);
        imgBtnGif.setOnClickListener(this);
        imgBtnDeleteFoto.setOnClickListener(this);
        imgBtnIncBackPadrao.setOnClickListener(this);
        btnGrupoPublico.setOnClickListener(this);
        btnGrupoParticular.setOnClickListener(this);
        btnViewDefinirTopicos.setOnClickListener(this);
        btnViewSalvar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtnCadGaleria:
                tipoMidiaPermissao = "galeria";
                midiaUtils.setProgress(progressFoto);
                checkPermissions("foto");
                break;
            case R.id.imgBtnCadCamera:
                tipoMidiaPermissao = "camera";
                midiaUtils.setProgress(progressFoto);
                checkPermissions("foto");
                break;
            case R.id.imgBtnCadGif:
                tipoMidiaPermissao = "gif";
                midiaUtils.setProgress(progressFoto);
                checkPermissions("foto");
                break;
            case R.id.imgBtnDeleteFotoGrupo:
                if (uriFoto != null) {
                    uriFoto = null;
                    campoSelecionado = "foto";
                    visibilidadeImgBtnDelete(false, "foto");
                    CommunityUtils.exibirFotoPadrao(getApplicationContext(), imgViewCadFoto, UsuarioUtils.FIELD_PHOTO, true);
                } else {
                    if (edicao) {
                        exibirAlertDialog("foto");
                    }
                }
                break;
            case R.id.btnGrupoPublico:
                aparenciaOriginalBtn("publico");
                desativarBtn("particular");
                break;
            case R.id.btnGrupoParticular:
                aparenciaOriginalBtn("particular");
                desativarBtn("publico");
                break;
            case R.id.btnViewDefinirTopicosGrupo:
                exibirTopicos();
                break;
            case R.id.btnViewSalvarGrupo:
                verificarDadosGrupo();
                break;
            case R.id.imgBtnIncBackBlack:
                onBackPressed();
                break;
        }
    }

    private void checkPermissions(String campoEscolhido) {
        if (tipoMidiaPermissao != null
                && !tipoMidiaPermissao.isEmpty()
                && campoEscolhido != null
                && !campoEscolhido.isEmpty()) {
            campoSelecionado = campoEscolhido;
            midiaUtils.setLayoutCircular(true);
            if (tipoMidiaPermissao.equals("gif")) {
                midiaFotoGif = true;
            } else {
                midiaFotoGif = false;
            }
            boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(CreateGroupActivity.this);
            if (galleryPermissionsGranted) {
                // Permissões da galeria já concedidas.
                switch (tipoMidiaPermissao) {
                    case "galeria":
                        midiaUtils.selecionarGaleria();
                        break;
                    case "gif":
                        selecionadoGif();
                        break;
                    case "camera":
                        boolean cameraPermissionsGranted = PermissionUtils.requestCameraPermissions(this);
                        if (cameraPermissionsGranted) {
                            midiaUtils.selecionarCamera();
                        }
                        break;
                }
            }
        }
    }

    private void selecionadoGif() {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            exibirSpinKit(campoSelecionado);
            tamanhoGif = "pequena";
            midiaUtils.selecionarGif(TAG, tamanhoGif, new MidiaUtils.UriRecuperadaCallback() {
                @Override
                public void onRecuperado(Uri uriRecuperada) {
                    atribuirUri(uriRecuperada);
                    exibirFoto(uriRecuperada);
                }

                @Override
                public void onCancelado() {
                    ocultarSpinKit(campoSelecionado);
                }

                @Override
                public void onError(String message) {
                    ocultarSpinKit(campoSelecionado);
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_gif), getApplicationContext());
                }
            });
        }
    }

    private void atribuirUri(Uri uriRecebida) {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            uriFoto = uriRecebida;
            visibilidadeImgBtnDelete(true, "foto");
        }
    }

    private void exibirFoto(Uri uri) {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            if (tipoMidiaPermissao != null
                    && !tipoMidiaPermissao.isEmpty()) {
                Drawable circle = getDrawable(R.drawable.circle);
                imgViewCadFoto.setBackground(circle);
                GlideCustomizado.loadUrlComListener(getApplicationContext(),
                        String.valueOf(uri), imgViewCadFoto, android.R.color.transparent,
                        GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                            @Override
                            public void onCarregado() {
                                ocultarSpinKit(campoSelecionado);
                            }

                            @Override
                            public void onError(String message) {
                                ocultarSpinKit(campoSelecionado);
                                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_group_photo), getApplicationContext());
                            }
                        });
            }
        }
    }

    private void exibirAlertDialog(String campo) {
        if (campo != null && !campo.isEmpty()) {
            builder.setTitle(getString(R.string.confirm_deletion_photo_group));
            builder.setMessage(getString(R.string.msg_deletion_photo_group));
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    excluirFoto();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void excluirFoto() {
        DatabaseReference removerFotoRef = firebaseRef.child("grupos")
                .child(idGrupo).child("fotoGrupo");
        StorageReference fotoStorage = storageRef.child("grupos")
                .child(idGrupo).child("fotoGrupo.jpeg");
        midiaUtils.excluirFoto(removerFotoRef, fotoStorage, new MidiaUtils.ExcluirFotoCallback() {
            @Override
            public void onExcluido() {
                uriFoto = null;
                visibilidadeImgBtnDelete(false, "foto");
                communityUtils.exibirFotoPadrao(getApplicationContext(), imgViewCadFoto, UsuarioUtils.FIELD_PHOTO, true);
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.deleted_photo), getApplicationContext());
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.checkPermissionResult(grantResults)) {
                // Permissões concedidas.
                if (tipoMidiaPermissao != null
                        && !tipoMidiaPermissao.isEmpty()) {
                    switch (tipoMidiaPermissao) {
                        case "galeria":
                            midiaUtils.selecionarGaleria();
                            break;
                        case "camera":
                            midiaUtils.selecionarCamera();
                            break;
                    }
                }
            } else {
                // Permissões negadas.
                PermissionUtils.openAppSettings(CreateGroupActivity.this, getApplicationContext());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        midiaUtils.handleActivityResult(requestCode, resultCode, data, new MidiaUtils.UriRecuperadaCallback() {
            @Override
            public void onRecuperado(Uri uriRecuperada) {
                atribuirUri(uriRecuperada);
                exibirFoto(uriRecuperada);
            }

            @Override
            public void onCancelado() {
                ocultarSpinKit(campoSelecionado);
            }

            @Override
            public void onError(String message) {
                ocultarSpinKit(campoSelecionado);
            }
        });
    }

    private void verificarDadosGrupo() {

        String nome = edtTxtNome.getText().toString().trim();
        nome = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nome);
        String descricao = edtTxtDesc.getText().toString().trim();
        if (nome.isEmpty() || descricao.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.data_needed_group), getApplicationContext());
            return;
        }
        if (nome.length() < CommunityUtils.MIN_LENGHT_NAME) {
            String msgLimiteMinimoNome = getString(R.string.warning_minimum_character_limit_name, CommunityUtils.MIN_LENGHT_NAME);
            ToastCustomizado.toastCustomizadoCurto(msgLimiteMinimoNome, getApplicationContext());
            return;
        }
        if (descricao.length() < CommunityUtils.MIN_LENGHT_DESCRIPTION) {
            String msgLimiteMinimoDescricao = getString(R.string.warning_minimum_character_limit_name, CommunityUtils.MIN_LENGHT_DESCRIPTION);
            ToastCustomizado.toastCustomizadoCurto(msgLimiteMinimoDescricao, getApplicationContext());
            return;
        }
        if (!verificaLimiteTopicos()) {
            return;
        }

        midiaUtils.exibirProgressDialog(getString(R.string.group).toLowerCase(Locale.ROOT), "ajustando");

        dadosGrupo.put(caminhoGrupo + "nomeGrupo", nome);
        dadosGrupo.put(caminhoGrupo + "nomeGrupoPesquisa", FormatarNomePesquisaUtils.removeAcentuacao(nome).toUpperCase(Locale.ROOT));
        dadosGrupo.put(caminhoGrupo + "descricaoGrupo", descricao);
        dadosGrupo.put(caminhoGrupo + "topicos", topicosSelecionados);
        dadosGrupo.put(caminhoGrupo + "grupoPublico", grupoPublico);

        String caminhoPesquisa = "/groups_by_name/" + idGrupo + "/";
        dadosGrupo.put(caminhoPesquisa + "idGrupo", idGrupo);
        dadosGrupo.put(caminhoPesquisa + "nomeGrupoPesquisa", FormatarNomePesquisaUtils.removeAcentuacao(nome).toUpperCase(Locale.ROOT));

        prepararSalvamento(new PrepararSalvamentoCallback() {
            @Override
            public void onConcluido() {
                if (edicao && uriFoto == null) {
                    midiaUtils.ocultarProgressDialog();
                    salvarGrupo(dadosGrupo);
                    return;
                }

                if (!edicao && uriFoto == null) {
                    //Não é edição e o usuário não selecionou nenhuma foto.
                    midiaUtils.ocultarProgressDialog();
                    alertDialogSemFotos();
                    return;
                }

                if (uriFoto != null) {
                    midiaUtils.exibirProgressDialog(getString(R.string.group).toLowerCase(Locale.ROOT), "salvamento");
                    if (midiaFotoGif) {
                        StorageReference fotoStorage = storageRef.child("grupos")
                                .child(idGrupo).child("fotoGrupo.jpeg");
                        midiaUtils.removerDoStorage(fotoStorage, new MidiaUtils.RemoverDoStorageCallback() {
                            @Override
                            public void onRemovido() {
                                dadosGrupo.put(caminhoGrupo + "fotoGrupo", String.valueOf(uriFoto));
                                //Não há mais o que salvar, finalizar activity.
                                salvarGrupo(dadosGrupo);
                            }

                            @Override
                            public void onError(String message) {
                                midiaUtils.ocultarProgressDialog();
                                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.error_saving_gif), message), getApplicationContext());
                            }
                        });
                    } else {
                        //Não é gif.
                        StorageReference fotoStorage = storageRef.child("grupos")
                                .child(idGrupo).child("fotoGrupo.jpeg");
                        midiaUtils.uparFotoNoStorage(fotoStorage, uriFoto, new MidiaUtils.UparNoStorageCallback() {
                            @Override
                            public void onConcluido(String urlUpada) {
                                uriFoto = Uri.parse(urlUpada);
                                dadosGrupo.put(caminhoGrupo + "fotoGrupo", urlUpada);
                                //Não há mais o que salvar, finalizar activity.
                                midiaUtils.ocultarProgressDialog();
                                salvarGrupo(dadosGrupo);
                            }

                            @Override
                            public void onError(String message) {
                                midiaUtils.ocultarProgressDialog();
                                ToastCustomizado.toastCustomizado(getString(R.string.error_saving_group_photo), getApplicationContext());
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(String message) {
                midiaUtils.ocultarProgressDialog();
                ToastCustomizado.toastCustomizado(getString(R.string.error_saving_group), getApplicationContext());
            }
        });
    }

    private void prepararSalvamento(PrepararSalvamentoCallback callback) {
        if (edicao) {
            prepararRemocaoTopicos(new AjustarTopicosCallback() {
                @Override
                public void onConcluido() {
                    ajustarTopicos(new AjustarTopicosCallback() {
                        @Override
                        public void onConcluido() {
                            callback.onConcluido();
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        } else {
            ajustarTopicos(new AjustarTopicosCallback() {
                @Override
                public void onConcluido() {
                    callback.onConcluido();
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        }
    }

    private void alertDialogSemFotos() {
        builder.setTitle(getString(R.string.title_without_choosing_group_photos));
        builder.setMessage(getString(R.string.warning_without_choosing_group_photos));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.continue_message), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                salvarGrupo(dadosGrupo);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void salvarGrupo(HashMap<String, Object> dadosAlvo) {
        if (dadosAlvo == null) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adjusting_group), getApplicationContext());
            onBackPressed();
            return;
        }
        midiaUtils.exibirProgressDialog(getString(R.string.group).toLowerCase(Locale.ROOT), "salvamento");

        recuperarTimestampNegativo(new RecuperarTimeStampCallback() {

            String caminhoGrupoPublico = "/publicGroups/" + idGrupo + "/";
            String caminhoGrupoPrivado = "/privateGroups/" + idGrupo + "/";

            @Override
            public void onRecuperado(long timestampNegativo, String data) {

                if (!edicao) {

                    HashMap<String, Object> dadosOperacao = new HashMap<>();
                    for (String idParticipante : participantes) {
                        GroupUtils.ajustarDadoParaPesquisa(getApplicationContext(),
                                dadosOperacao, idGrupo, idParticipante, new GroupUtils.AjustarDadoParaPesquisaCallback() {
                                    String caminhoGrupo = "/grupos/" + idGrupo + "/";
                                    @Override
                                    public void onConcluido(HashMap<String, Object> dadosOperacao) {
                                        groupUtils.verificaBlock(idParticipante, idGrupo, new GroupUtils.VerificaBlockCallback() {
                                            @Override
                                            public void onBlock(boolean status) {
                                                if (status) {
                                                    //Bloqueado
                                                    contadorParticipantes++;
                                                    idsARemover.add(idParticipante);
                                                } else {
                                                    String caminhoFollowers = "/groupFollowers/" + idGrupo + "/" + idParticipante + "/";
                                                    String caminhoFollowing = "/groupFollowing/" + idParticipante + "/" + idGrupo + "/";
                                                    dadosOperacao.put(caminhoGrupo + "nrParticipantes", ServerValue.increment(1));
                                                    dadosOperacao.put(caminhoFollowers + "timestampinteracao", timestampNegativo);
                                                    dadosOperacao.put(caminhoFollowers + "idParticipante", idParticipante);
                                                    dadosOperacao.put(caminhoFollowers + "administrator", false);
                                                    dadosOperacao.put(caminhoFollowing + "idGrupo", idGrupo);
                                                    dadosOperacao.put(caminhoFollowing + "timestampinteracao", timestampNegativo);
                                                    firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                            contadorParticipantes++;

                                                            if (contadorParticipantes == participantes.size()) {
                                                                contadorParticipantes = 0;

                                                                if (idsARemover != null && !idsARemover.isEmpty()) {
                                                                    for (String idRemover : idsARemover) {
                                                                        contadorRemocao++;
                                                                        participantes.remove(idRemover);
                                                                        if (contadorRemocao == idsARemover.size()) {
                                                                            idsARemover.clear();
                                                                            contadorRemocao = 0;
                                                                        }
                                                                    }
                                                                }

                                                                if (grupoPublico) {
                                                                    dadosGrupo.put(caminhoGrupoPrivado, null);
                                                                    dadosGrupo.put(caminhoGrupoPublico + "idGrupo", idGrupo);
                                                                    dadosGrupo.put(caminhoGrupoPublico + "timestampinteracao", timestampNegativo);
                                                                } else {
                                                                    dadosGrupo.put(caminhoGrupoPublico, null);
                                                                    dadosGrupo.put(caminhoGrupoPrivado + "idGrupo", idGrupo);
                                                                    dadosGrupo.put(caminhoGrupoPrivado + "timestampinteracao", timestampNegativo);
                                                                }
                                                                dadosAlvo.put(caminhoGrupo + "timestampinteracao", timestampNegativo);
                                                                dadosAlvo.put(caminhoGrupo + "idSuperAdmGrupo", idUsuario);
                                                                dadosAlvo.put(caminhoGrupo + "participantes", participantes);

                                                                UsuarioUtils.recuperarIdsGrupos(getApplicationContext(), idUsuario, new UsuarioUtils.RecuperarIdsMeusGruposCallback() {
                                                                    ArrayList<String> listaMeusGrupos = new ArrayList<>();

                                                                    @Override
                                                                    public void onRecuperado(ArrayList<String> idsGrupos) {
                                                                        listaMeusGrupos = idsGrupos;
                                                                        listaMeusGrupos.add(idGrupo);
                                                                        dadosAlvo.put("/usuarios/" + idUsuario + "/idMeusGrupos/", listaMeusGrupos);
                                                                        salvarHashmap(dadosAlvo);
                                                                    }

                                                                    @Override
                                                                    public void onNaoExiste() {
                                                                        listaMeusGrupos.add(idGrupo);
                                                                        dadosAlvo.put("/usuarios/" + idUsuario + "/idMeusGrupos/", listaMeusGrupos);
                                                                        salvarHashmap(dadosAlvo);
                                                                    }

                                                                    @Override
                                                                    public void onError(String message) {
                                                                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_group), getApplicationContext());
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String message) {

                                    }
                                });
                    }
                } else {
                    salvarHashmap(dadosAlvo);
                }
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adjusting_group), getApplicationContext());
            }
        });
    }

    private void salvarHashmap(HashMap<String, Object> dadosAlvo) {
        firebaseRef.updateChildren(dadosAlvo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                midiaUtils.ocultarProgressDialog();
                if (error == null) {
                    onBackPressed();
                } else {
                    ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.error_saving_group), error.getCode()), getApplicationContext());
                }
            }
        });
    }

    private boolean verificaLimiteTopicos() {
        if (topicosSelecionados != null) {
            if (topicosSelecionados.size() > CommunityUtils.MAX_LENGTH_TOPICOS) {
                limiteTopicosPermitido = false;
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.topic_limit_exceeded), getApplicationContext());
            } else if (topicosSelecionados.size() < CommunityUtils.MIN_LENGTH_TOPICOS) {
                if (edicao) {
                    if (!grupoEdicao.getTopicos().isEmpty()) {
                        topicosSelecionados.addAll(grupoEdicao.getTopicos());
                        limiteTopicosPermitido = true;
                    } else {
                        limiteTopicosPermitido = false;
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.select_at_least_one_topic), getApplicationContext());
                    }
                } else {
                    limiteTopicosPermitido = false;
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.select_at_least_one_topic), getApplicationContext());
                }
            } else {
                limiteTopicosPermitido = true;
            }
        }
        return limiteTopicosPermitido;
    }

    private void desativarBtn(String buttonRecebido) {
        if (buttonRecebido.equals("publico")) {
            btnGrupoPublico.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoPublico.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnGrupoPublico.setTextColor(getResources().getColor(android.R.color.black));
            btnGrupoPublico.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6D716D")));
            grupoPublico = false;
        } else {
            btnGrupoParticular.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoParticular.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnGrupoParticular.setTextColor(getResources().getColor(android.R.color.black));
            btnGrupoParticular.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6D716D")));
            grupoPublico = true;
        }
    }

    private void aparenciaOriginalBtn(String buttonRecebido) {
        if (buttonRecebido.equals("publico")) {
            String corBackground = "#4CAF50";
            btnGrupoPublico.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoPublico.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnGrupoPublico.setTextColor(Color.WHITE);
        } else {
            String corBackground = "#005488";
            btnGrupoParticular.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoParticular.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnGrupoParticular.setTextColor(Color.WHITE);
        }
    }

    private void exibirTopicos() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Define o título do dialog
        builder.setTitle(getString(R.string.select_your_topics));

        builder.setMultiChoiceItems(topicosGrupo, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                // Verifica se o limite de seleção foi atingido
                int count = 0;
                for (boolean checkedItem : checkedItems) {
                    if (checkedItem) {
                        count++;
                    }
                }

                // Se o limite for atingido, desmarca o item clicado
                if (b && count > CommunityUtils.MAX_LENGTH_TOPICOS) {
                    checkedItems[i] = false;
                    ((AlertDialog) dialogInterface).getListView().setItemChecked(i, false);
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.topic_limit_exceeded), getApplicationContext());
                }
            }
        });
        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Este método é chamado quando o botão "OK" é clicado
                // Aqui você pode percorrer a lista de hobbies e verificar quais itens foram selecionados
                topicosSelecionados.clear();
                for (int j = 0; j < checkedItems.length; j++) {
                    if (checkedItems[j]) {
                        topicosSelecionados.add(topicosGrupo[j]);
                    }
                }
                if (topicosSelecionados != null) {
                    String msgTopicos = getString(R.string.set_topics_community, String.valueOf(topicosSelecionados.size()), String.valueOf(CommunityUtils.MAX_LENGTH_TOPICOS));
                    btnViewDefinirTopicos.setText(msgTopicos);
                    for (int e = 0; e < checkedItems.length; e++) {
                        if (checkedItems[e]) {
                        }
                    }
                    verificaLimiteTopicos();
                }
            }
        }).setNegativeButton(getString(R.string.deselect_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (topicosSelecionados != null) {
                    topicosSelecionados.clear();
                    String msgTopicos = getString(R.string.set_topics_community, String.valueOf(topicosSelecionados.size()), String.valueOf(CommunityUtils.MAX_LENGTH_TOPICOS));
                    btnViewDefinirTopicos.setText(msgTopicos);
                    Arrays.fill(checkedItems, false);
                }
                dialogInterface.cancel();
            }
        });
        // Cria o dialog e o exibe na tela
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void recuperarTimestampNegativo(RecuperarTimeStampCallback callback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(getApplicationContext(), new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        callback.onRecuperado(timestampNegativo, dataFormatada);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }

    private void prepararRemocaoTopicos(AjustarTopicosCallback callback) {
        groupUtils.recuperarTopicosAnteriores(idGrupo, new GroupUtils.TopicosAnterioresCallback() {
            @Override
            public void onConcluido(ArrayList<String> topicosAnteriores) {
                for (String interesseRemover : topicosAnteriores) {
                    String caminhoInteresses = configurarCaminhoInteresses() + interesseRemover + "/" + idGrupo;
                    interessesAnteriores.put(caminhoInteresses, null);
                    interessesConcluidos++;
                    if (interessesConcluidos == topicosAnteriores.size()) {
                        interessesConcluidos = 0;
                        firebaseRef.updateChildren(interessesAnteriores, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    callback.onConcluido();
                                } else {
                                    callback.onError(String.valueOf(error.getCode()));
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void ajustarTopicos(AjustarTopicosCallback callback) {
        groupUtils.recuperarTimestampCriacao(idGrupo, new GroupUtils.RecuperarTimestampCriacaoCallback() {
            @Override
            public void onConcluido(long timestamp) {
                for (String interesseAtual : topicosSelecionados) {
                    String caminhoInteresses = configurarCaminhoInteresses() + interesseAtual + "/" + idGrupo + "/";
                    dadosGrupo.put(caminhoInteresses + "idGrupo", idGrupo);
                    dadosGrupo.put(caminhoInteresses + "timestampinteracao", timestamp);
                    interessesConcluidos++;
                    if (interessesConcluidos == topicosSelecionados.size()) {
                        interessesConcluidos = 0;
                        callback.onConcluido();
                    }
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    private String configurarCaminhoInteresses(){
        if (grupoPublico) {
            return "/groupInterests/";
        }else{
           return  "/groupInterestsPrivate/";
        }
    }

    private void inicializarComponentes() {
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        imgViewCadFoto = findViewById(R.id.imgViewCadFotoGrupo);
        progressFoto = findViewById(R.id.progressBarCadFotoGrupo);
        btnViewSalvar = findViewById(R.id.btnViewSalvarGrupo);
        imgBtnGaleria = findViewById(R.id.imgBtnCadGaleria);
        imgBtnCamera = findViewById(R.id.imgBtnCadCamera);
        imgBtnGif = findViewById(R.id.imgBtnCadGif);
        imgBtnDeleteFoto = findViewById(R.id.imgBtnDeleteFotoGrupo);
        edtTxtNome = findViewById(R.id.edtTxtNomeGrupo);
        edtTxtDesc = findViewById(R.id.edtTxtDescGrupo);
        txtViewLimiteNome = findViewById(R.id.txtViewLimiteNomeGrupo);
        txtViewLimiteDesc = findViewById(R.id.txtViewLimiteDescGrupo);
        btnGrupoPublico = findViewById(R.id.btnGrupoPublico);
        btnGrupoParticular = findViewById(R.id.btnGrupoParticular);
        btnViewDefinirTopicos = findViewById(R.id.btnViewDefinirTopicosGrupo);
    }
}