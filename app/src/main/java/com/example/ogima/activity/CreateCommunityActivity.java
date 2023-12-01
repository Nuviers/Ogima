package com.example.ogima.activity;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.example.ogima.R;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.PostUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Comunidade;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class CreateCommunityActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgViewCadFoto, imgViewCadFundo;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "", idComunidade = "";
    private TextView txtViewTitleToolbar;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private SpinKitView progressFoto, progressFundo;
    private Button btnViewSalvar, btnComunidadePublica, btnComunidadeParticular,
            btnViewDefinirTopicos;
    private boolean edicao = false;
    private String tipoMidiaPermissao = "";
    private MidiaUtils midiaUtils;
    private CommunityUtils communityUtils;
    private ProgressDialog progressDialog;
    private String campoSelecionado = "";
    private Uri uriFoto = null, uriFundo = null;
    private final static String TAG = "CreateCommunityActivity";
    private String tamanhoGif = "";
    private boolean midiaFotoGif = false, midiaFundoGif = false;
    private StorageReference storageRef;
    private AlertDialog.Builder builder;
    private boolean statusEpilepsia = true;
    private String msgSalvamento = "", msgEdicao = "", msgError = "";
    private Comunidade comunidadeEdicao;
    private ImageButton imgBtnFotoGaleria, imgBtnFotoCamera,
            imgBtnFotoGif, imgBtnFundoGaleria, imgBtnFundoCamera,
            imgBtnFundoGif, imgBtnDeleteFoto, imgBtnDeleteFundo;
    private EditText edtTxtNomeComunidade, edtTxtDescComunidade;
    private TextView txtViewLimiteNomeComunidade, txtViewLimiteDescComunidade;
    private Boolean limiteTopicosPermitido = false;
    private String[] topicosComunidade = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    //Verifica quais dos tópicos foram selecionados.
    private final boolean[] checkedItems = new boolean[topicosComunidade.length];
    private final ArrayList<String> topicosSelecionados = new ArrayList<>();
    private ArrayList<String> idsComunidadesAtuais = new ArrayList<>();
    private boolean comunidadePublica = true;
    private HashMap<String, Object> dadosComunidade;
    private ArrayList<String> participantes;

    public CreateCommunityActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public interface DadosIniciaisCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface RecuperarTimeStampCallback {
        void onRecuperado(long timestampNegativo, String data);

        void onError(String message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimparCacheUtils limparCacheUtils = new LimparCacheUtils();
        limparCacheUtils.clearAppCache(getApplicationContext());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);
        inicializarComponentes();

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
                        if (comunidadePublica) {
                            btnComunidadePublica.setVisibility(View.VISIBLE);
                            btnComunidadePublica.performClick();
                        } else {
                            btnComunidadeParticular.setVisibility(View.VISIBLE);
                            btnComunidadeParticular.performClick();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_community_setting), getApplicationContext());
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
        setSupportActionBar(toolbarIncPadrao);
        toolbarIncPadrao.setTitle("");
        txtViewTitleToolbar.setText(getString(R.string.community));
        txtViewLimiteNomeComunidade.setText(String.format("%s%s", "0/", CommunityUtils.MAX_LENGHT_NAME));
        txtViewLimiteDescComunidade.setText(String.format("%s%s", "0/", CommunityUtils.MAX_LENGHT_DESCRIPTION));
        String msgDefinirTopicos = getString(R.string.set_topics_community, "0", String.valueOf(CommunityUtils.MAX_LENGTH_TOPICOS));
        btnViewDefinirTopicos.setText(msgDefinirTopicos);
        msgSalvamento = getString(R.string.saved_successfully);
        msgEdicao = getString(R.string.successfully_changed);
        msgError = getString(R.string.an_error_has_occurred);
        dadosComunidade = new HashMap<>();
        participantes = new ArrayList<>();
        builder = new AlertDialog.Builder(CreateCommunityActivity.this);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        progressDialog = new ProgressDialog(CreateCommunityActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        midiaUtils = new MidiaUtils(CreateCommunityActivity.this.getSupportFragmentManager(), progressDialog, CreateCommunityActivity.this,
                getApplicationContext(), SizeUtils.MAX_FILE_SIZE_IMAGEM, 0, 0);
        midiaUtils.limitarCaracteres(edtTxtNomeComunidade, txtViewLimiteNomeComunidade, CommunityUtils.MAX_LENGHT_NAME);
        midiaUtils.limitarCaracteres(edtTxtDescComunidade, txtViewLimiteDescComunidade, CommunityUtils.MAX_LENGHT_DESCRIPTION);
        communityUtils = new CommunityUtils(CreateCommunityActivity.this, getApplicationContext());
        configBundle(callback);
    }

    private void configBundle(DadosIniciaisCallback callback) {
        Bundle dados = getIntent().getExtras();
        if (dados == null) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_community_setting), getApplicationContext());
            onBackPressed();
            return;
        }
        communityUtils.configurarBundle(dados, new CommunityUtils.ConfigBundleCallback() {
            @Override
            public void onCadastro() {
                edicao = false;
                if (dados.containsKey("comunidadePublica")) {
                    comunidadePublica = dados.getBoolean("comunidadePublica");
                }else{
                    comunidadePublica = true;
                }
                btnViewSalvar.setText(getString(R.string.create_community));
                DatabaseReference gerarIdComunidadeRef = firebaseRef.child("comunidades");
                idComunidade = FirebaseUtils.retornarIdRandom(gerarIdComunidadeRef);
                participantes.add(idUsuario);
                dadosComunidade.put("seguidores", participantes);
                dadosComunidade.put("idComunidade", idComunidade);
                callback.onConcluido();
            }

            @Override
            public void onEdicao(Comunidade dadosEdicao) {
                edicao = true;
                comunidadePublica = dadosEdicao.getComunidadePublica();
                btnViewSalvar.setText(getString(R.string.save));
                comunidadeEdicao = dadosEdicao;
                idComunidade = comunidadeEdicao.getIdComunidade();
                exibirFotosEdicao();
                preencherDadosEdicao();
                callback.onConcluido();
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_community_setting), getApplicationContext());
                onBackPressed();
            }
        });
    }

    private void preencherDadosEdicao() {
        if (edicao) {
            String nome = comunidadeEdicao.getNomeComunidade();
            String descricao = comunidadeEdicao.getDescricaoComunidade();
            if (nome != null && !nome.isEmpty()) {
                edtTxtNomeComunidade.setText(comunidadeEdicao.getNomeComunidade());
            }
            if (descricao != null && !descricao.isEmpty()) {
                edtTxtDescComunidade.setText(comunidadeEdicao.getDescricaoComunidade());
            }
            if (comunidadeEdicao.getTopicos() != null
                    && comunidadeEdicao.getTopicos().size() > 0) {
                String msgDefinirTopicos = getString(R.string.set_topics_community, String.valueOf(comunidadeEdicao.getTopicos().size()), String.valueOf(CommunityUtils.MAX_LENGTH_TOPICOS));
                btnViewDefinirTopicos.setText(msgDefinirTopicos);
            }
        }
    }

    private void exibirFotosEdicao() {
        if (comunidadeEdicao.getFotoComunidade() != null
                && !comunidadeEdicao.getFotoComunidade().isEmpty()) {
            campoSelecionado = "foto";
            exibirSpinKit("foto");
            GlideCustomizado.loadUrlComListener(getApplicationContext(),
                    comunidadeEdicao.getFotoComunidade(), imgViewCadFoto, android.R.color.transparent,
                    GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                            ocultarSpinKit("foto");
                        }

                        @Override
                        public void onError(String message) {
                            ocultarSpinKit("foto");
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_community_photo), getApplicationContext());
                        }
                    });
            visibilidadeImgBtnDelete(true, "foto");
        } else {
            visibilidadeImgBtnDelete(false, "foto");
        }

        if (comunidadeEdicao.getFundoComunidade() != null
                && !comunidadeEdicao.getFundoComunidade().isEmpty()) {
            campoSelecionado = "fundo";
            exibirSpinKit("fundo");
            GlideCustomizado.loadUrlComListener(getApplicationContext(),
                    comunidadeEdicao.getFundoComunidade(), imgViewCadFundo, android.R.color.transparent,
                    GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                            ocultarSpinKit("fundo");
                        }

                        @Override
                        public void onError(String message) {
                            ocultarSpinKit("fundo");
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_community_background), getApplicationContext());
                        }
                    });
            visibilidadeImgBtnDelete(true, "fundo");
        } else {
            visibilidadeImgBtnDelete(false, "fundo");
        }
    }

    private void clickListeners() {
        imgBtnFotoGaleria.setOnClickListener(this);
        imgBtnFotoCamera.setOnClickListener(this);
        imgBtnFotoGif.setOnClickListener(this);
        imgBtnFundoGaleria.setOnClickListener(this);
        imgBtnFundoCamera.setOnClickListener(this);
        imgBtnFundoGif.setOnClickListener(this);
        imgBtnDeleteFoto.setOnClickListener(this);
        imgBtnDeleteFundo.setOnClickListener(this);
        imgBtnIncBackPadrao.setOnClickListener(this);
        btnComunidadePublica.setOnClickListener(this);
        btnComunidadeParticular.setOnClickListener(this);
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
            case R.id.imgBtnCadGaleriaFundo:
                tipoMidiaPermissao = "galeria";
                midiaUtils.setProgress(progressFundo);
                checkPermissions("fundo");
                break;
            case R.id.imgBtnCadCameraFundo:
                tipoMidiaPermissao = "camera";
                midiaUtils.setProgress(progressFundo);
                checkPermissions("fundo");
                break;
            case R.id.imgBtnCadGifFundo:
                tipoMidiaPermissao = "gif";
                midiaUtils.setProgress(progressFundo);
                checkPermissions("fundo");
                break;
            case R.id.imgBtnDeleteFotoComunidade:
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
            case R.id.imgBtnDeleteFundoComunidade:
                if (uriFundo != null) {
                    uriFundo = null;
                    campoSelecionado = "fundo";
                    visibilidadeImgBtnDelete(false, "fundo");
                    CommunityUtils.exibirFotoPadrao(getApplicationContext(), imgViewCadFundo, UsuarioUtils.FIELD_BACKGROUND, false);
                } else {
                    if (edicao) {
                        exibirAlertDialog("fundo");
                    }
                }
                break;
            case R.id.btnComunidadePublica:
                aparenciaOriginalBtn("publico");
                desativarBtn("particular");
                break;
            case R.id.btnComunidadeParticular:
                aparenciaOriginalBtn("particular");
                desativarBtn("publico");
                break;
            case R.id.btnViewDefinirTopicosComunidade:
                exibirTopicos();
                break;
            case R.id.btnViewSalvarComunidade:
                verificarDadosComunidade();
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
            if (campoSelecionado.equals("foto")) {
                midiaUtils.setLayoutCircular(true);
                if (tipoMidiaPermissao.equals("gif")) {
                    midiaFotoGif = true;
                } else {
                    midiaFotoGif = false;
                }
            } else if (campoSelecionado.equals("fundo")) {
                midiaUtils.setLayoutCircular(false);
                if (tipoMidiaPermissao.equals("gif")) {
                    midiaFundoGif = true;
                } else {
                    midiaFundoGif = false;
                }
            }
            boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(CreateCommunityActivity.this);
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
            if (campoSelecionado.equals("foto")) {
                tamanhoGif = "pequena";
            } else if (campoSelecionado.equals("fundo")) {
                tamanhoGif = "media";
            }
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
            if (campoSelecionado.equals("foto")) {
                uriFoto = uriRecebida;
                visibilidadeImgBtnDelete(true, "foto");
            } else if (campoSelecionado.equals("fundo")) {
                visibilidadeImgBtnDelete(true, "fundo");
                uriFundo = uriRecebida;
            }
        }
    }

    private void exibirFoto(Uri uri) {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            if (tipoMidiaPermissao != null
                    && !tipoMidiaPermissao.isEmpty()) {
                if (campoSelecionado.equals("foto")) {
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
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_community_photo), getApplicationContext());
                                }
                            });
                } else if (campoSelecionado.equals("fundo")) {
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            String.valueOf(uri), imgViewCadFundo, android.R.color.transparent,
                            GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit(campoSelecionado);
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit(campoSelecionado);
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_community_background), getApplicationContext());
                                }
                            });
                }
            }
        }
    }

    private void exibirAlertDialog(String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                builder.setTitle(getString(R.string.confirm_deletion_photo_community));
                builder.setMessage(getString(R.string.msg_deletion_photo_community));
            } else if (campo.equals("fundo")) {
                builder.setTitle(getString(R.string.confirm_deletion_background_community));
                builder.setMessage(getString(R.string.msg_deletion_background_community));
            }
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    excluirFoto(campo);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void excluirFoto(String campo) {
        if (campo.equals("foto")) {
            DatabaseReference removerFotoRef = firebaseRef.child("comunidades")
                    .child(idComunidade).child("fotoComunidade");
            StorageReference fotoStorage = storageRef.child("comunidades")
                    .child(idComunidade).child("fotoComunidade.jpeg");
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
        } else if (campo.equals("fundo")) {
            DatabaseReference removerFundoRef = firebaseRef.child("comunidades")
                    .child(idComunidade).child("fundoComunidade");
            StorageReference fundoStorage = storageRef.child("comunidades")
                    .child(idComunidade).child("fundoComunidade.jpeg");
            midiaUtils.excluirFoto(removerFundoRef, fundoStorage, new MidiaUtils.ExcluirFotoCallback() {
                @Override
                public void onExcluido() {
                    uriFundo = null;
                    visibilidadeImgBtnDelete(false, "fundo");
                    communityUtils.exibirFotoPadrao(getApplicationContext(), imgViewCadFundo, UsuarioUtils.FIELD_BACKGROUND, false);
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.deletion_background_successfully), getApplicationContext());
                }

                @Override
                public void onError(String message) {
                }
            });
        }
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
                PermissionUtils.openAppSettings(CreateCommunityActivity.this, getApplicationContext());
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

    private void verificarDadosComunidade() {

        String nome = edtTxtNomeComunidade.getText().toString().trim();
        nome = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nome);
        String descricao = edtTxtDescComunidade.getText().toString().trim();
        if (nome.isEmpty() || descricao.isEmpty()) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.data_needed_community), getApplicationContext());
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
        dadosComunidade.put("nomeComunidade", nome);
        dadosComunidade.put("descricaoComunidade", descricao);
        dadosComunidade.put("topicos", topicosSelecionados);
        dadosComunidade.put("comunidadePublica", comunidadePublica);

        if (edicao && uriFoto == null
                && uriFundo == null) {
            salvarComunidade(dadosComunidade);
            return;
        }

        if (!edicao && uriFoto == null && uriFundo == null) {
            //Não é edição e o usuário não selecionou nenhuma foto.
            alertDialogSemFotos();
            return;
        }

        if (uriFoto != null) {
            midiaUtils.exibirProgressDialog(getString(R.string.community).toLowerCase(Locale.ROOT), "salvamento");
            if (midiaFotoGif) {
                StorageReference fotoStorage = storageRef.child("comunidades")
                        .child(idComunidade).child("fotoComunidade.jpeg");
                midiaUtils.removerDoStorage(fotoStorage, new MidiaUtils.RemoverDoStorageCallback() {
                    @Override
                    public void onRemovido() {
                        dadosComunidade.put("fotoComunidade", String.valueOf(uriFoto));
                        if (uriFundo == null) {
                            //Não há mais o que salvar, finalizar activity.
                            salvarComunidade(dadosComunidade);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.error_saving_gif), message), getApplicationContext());
                    }
                });
            } else {
                //Não é gif.
                StorageReference fotoStorage = storageRef.child("comunidades")
                        .child(idComunidade).child("fotoComunidade.jpeg");
                midiaUtils.uparFotoNoStorage(fotoStorage, uriFoto, new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        uriFoto = Uri.parse(urlUpada);
                        dadosComunidade.put("fotoComunidade", urlUpada);
                        if (uriFundo == null) {
                            //Não há mais o que salvar, finalizar activity.
                            midiaUtils.ocultarProgressDialog();
                            salvarComunidade(dadosComunidade);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizado(getString(R.string.error_saving_community_photo), getApplicationContext());
                    }
                });
            }
        }

        if (uriFundo != null) {
            midiaUtils.exibirProgressDialog(getString(R.string.community).toLowerCase(Locale.ROOT), "salvamento");
            if (midiaFundoGif) {
                StorageReference fundoStorage = storageRef.child("comunidades")
                        .child(idComunidade).child("fundoComunidade.jpeg");
                midiaUtils.removerDoStorage(fundoStorage, new MidiaUtils.RemoverDoStorageCallback() {
                    @Override
                    public void onRemovido() {
                        dadosComunidade.put("fundoComunidade", String.valueOf(uriFundo));
                        midiaUtils.ocultarProgressDialog();
                        salvarComunidade(dadosComunidade);
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.error_saving_gif), message), getApplicationContext());
                    }
                });
            } else {
                //Não é gif.
                StorageReference fundoStorage = storageRef.child("comunidades")
                        .child(idComunidade).child("fundoComunidade.jpeg");
                midiaUtils.uparFotoNoStorage(fundoStorage, uriFundo, new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        uriFundo = Uri.parse(urlUpada);
                        dadosComunidade.put("fundoComunidade", urlUpada);
                        salvarComunidade(dadosComunidade);
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizado(getString(R.string.error_saving_community_background), getApplicationContext());
                    }
                });
            }
        }
    }

    private void alertDialogSemFotos() {
        builder.setTitle(getString(R.string.title_without_choosing_community_photos));
        builder.setMessage(getString(R.string.warning_without_choosing_community_photos));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.continue_message), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                salvarComunidade(dadosComunidade);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void salvarComunidade(HashMap<String, Object> dadosAlvo) {
        if (dadosAlvo == null) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adjusting_community), getApplicationContext());
            onBackPressed();
            return;
        }
        midiaUtils.exibirProgressDialog(getString(R.string.community).toLowerCase(Locale.ROOT), "salvamento");
        recuperarTimestampNegativo(new RecuperarTimeStampCallback() {
            @Override
            public void onRecuperado(long timestampNegativo, String data) {
                if (!edicao) {
                    dadosAlvo.put("timestampCriacao", timestampNegativo);
                    dadosAlvo.put("idSuperAdmComunidade", idUsuario);
                }
                DatabaseReference salvarComunidadeRef = firebaseRef.child("comunidades")
                        .child(idComunidade);
                salvarComunidadeRef.updateChildren(dadosAlvo, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        midiaUtils.ocultarProgressDialog();
                        if (error == null) {
                            onBackPressed();
                        } else {
                            ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.error_saving_community), error.getCode()), getApplicationContext());
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adjusting_community), getApplicationContext());
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
                    if (comunidadeEdicao.getTopicos().size() > 0) {
                        topicosSelecionados.addAll(comunidadeEdicao.getTopicos());
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
            btnComunidadePublica.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadePublica.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnComunidadePublica.setTextColor(getResources().getColor(android.R.color.black));
            btnComunidadePublica.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6D716D")));
            comunidadePublica = false;
        } else {
            btnComunidadeParticular.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadeParticular.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnComunidadeParticular.setTextColor(getResources().getColor(android.R.color.black));
            btnComunidadeParticular.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6D716D")));
            comunidadePublica = true;
        }
    }

    private void aparenciaOriginalBtn(String buttonRecebido) {
        if (buttonRecebido.equals("publico")) {
            String corBackground = "#4CAF50";
            btnComunidadePublica.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadePublica.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnComunidadePublica.setTextColor(Color.WHITE);
        } else {
            String corBackground = "#005488";
            btnComunidadeParticular.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadeParticular.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnComunidadeParticular.setTextColor(Color.WHITE);
        }
    }

    private void exibirTopicos() {

        if (edicao) {
            for (int i = 0; i < topicosComunidade.length; i++) {
                String topicosAnteriores = topicosComunidade[i];
                if (comunidadeEdicao.getTopicos().contains(topicosAnteriores)) {
                    checkedItems[i] = true;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Define o título do dialog
        builder.setTitle(getString(R.string.select_your_topics));

        builder.setMultiChoiceItems(topicosComunidade, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
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
                        topicosSelecionados.add(topicosComunidade[j]);
                    }
                }
                if (topicosSelecionados != null) {
                    String msgTopicos = getString(R.string.set_topics_community, String.valueOf(topicosSelecionados.size()), String.valueOf(CommunityUtils.MAX_LENGTH_TOPICOS));
                    btnViewDefinirTopicos.setText(msgTopicos);
                    for (int e = 0; e < checkedItems.length; e++) {
                        if (checkedItems[e]) {
                            // ToastCustomizado.toastCustomizadoCurto("Tópico: " + topicosComunidade[e], getApplicationContext());
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

    private void exibirSpinKit(String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                ProgressBarUtils.exibirProgressBar(progressFoto, CreateCommunityActivity.this);
            } else if (campo.equals("fundo")) {
                ProgressBarUtils.exibirProgressBar(progressFundo, CreateCommunityActivity.this);
            }
        }
    }

    private void ocultarSpinKit(String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                ProgressBarUtils.ocultarProgressBar(progressFoto, CreateCommunityActivity.this);
            } else if (campo.equals("fundo")) {
                ProgressBarUtils.ocultarProgressBar(progressFundo, CreateCommunityActivity.this);
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
            } else if (campo.equals("fundo")) {
                if (exibir) {
                    imgBtnDeleteFundo.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFundo.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void inicializarComponentes() {
        imgViewCadFoto = findViewById(R.id.imgViewCadFotoComunidade);
        imgViewCadFundo = findViewById(R.id.imgViewCadFundoComunidade);
        txtViewTitleToolbar = findViewById(R.id.txtViewIncTituloToolbarBlack);
        toolbarIncPadrao = findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackBlack);
        progressFoto = findViewById(R.id.progressBarCadFotoComunidade);
        progressFundo = findViewById(R.id.progressBarCadFundoComunidade);
        btnViewSalvar = findViewById(R.id.btnViewSalvarComunidade);
        imgBtnFotoGaleria = findViewById(R.id.imgBtnCadGaleria);
        imgBtnFotoCamera = findViewById(R.id.imgBtnCadCamera);
        imgBtnFotoGif = findViewById(R.id.imgBtnCadGif);
        imgBtnFundoGaleria = findViewById(R.id.imgBtnCadGaleriaFundo);
        imgBtnFundoCamera = findViewById(R.id.imgBtnCadCameraFundo);
        imgBtnFundoGif = findViewById(R.id.imgBtnCadGifFundo);
        imgBtnDeleteFoto = findViewById(R.id.imgBtnDeleteFotoComunidade);
        imgBtnDeleteFundo = findViewById(R.id.imgBtnDeleteFundoComunidade);
        edtTxtNomeComunidade = findViewById(R.id.edtTxtNomeComunidade);
        edtTxtDescComunidade = findViewById(R.id.edtTxtDescComunidade);
        txtViewLimiteNomeComunidade = findViewById(R.id.txtViewLimiteNomeComunidade);
        txtViewLimiteDescComunidade = findViewById(R.id.txtViewLimiteDescComunidade);
        btnComunidadePublica = findViewById(R.id.btnComunidadePublica);
        btnComunidadeParticular = findViewById(R.id.btnComunidadeParticular);
        btnViewDefinirTopicos = findViewById(R.id.btnViewDefinirTopicosComunidade);
    }
}