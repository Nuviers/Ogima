package com.example.ogima.ui.cadastro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Postagem;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class FotoPerfilActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private FloatingActionButton fabBack;
    private ImageView imgViewFoto, imgViewFundo;
    private SpinKitView progressFoto, progressFundo;
    private ImageButton imgBtnFotoGaleria, imgBtnFotoCamera,
            imgBtnFotoGif, imgBtnFundoGaleria, imgBtnFundoCamera,
            imgBtnFundoGif;
    private Button btnContinuarCad;
    private String idUsuario = "";
    private boolean edicao = false;
    private String tipoMidiaPermissao = "";
    private MidiaUtils midiaUtils;
    private ProgressDialog progressDialog;
    private String campoSelecionado = "";
    private Uri uriFoto = null, uriFundo = null;
    private final static String TAG = "FotoPerfilActivity";
    private String tamanhoGif = "";

    public FotoPerfilActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_foto_perfil);
        inicializarComponentes();
        configInicial();
        clickListeners();
    }

    private void configInicial() {
        progressDialog = new ProgressDialog(FotoPerfilActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        midiaUtils = new MidiaUtils(FotoPerfilActivity.this.getSupportFragmentManager(), progressDialog, FotoPerfilActivity.this,
                getApplicationContext(), SizeUtils.MAX_FILE_SIZE_IMAGEM, 0, 0);

        Bundle dados = getIntent().getExtras();
        if (dados != null && dados.containsKey("edit")) {
            edicao = true;
            fabBack.setVisibility(View.VISIBLE);
        } else {
            fabBack.setVisibility(View.INVISIBLE);
        }

        imgBtnFotoGaleria.setOnClickListener(this);
        imgBtnFotoCamera.setOnClickListener(this);
        imgBtnFotoGif.setOnClickListener(this);
        imgBtnFundoGaleria.setOnClickListener(this);
        imgBtnFundoCamera.setOnClickListener(this);
        imgBtnFundoGif.setOnClickListener(this);
    }

    private void checkPermissions(String campoEscolhido) {
        if (tipoMidiaPermissao != null) {
            campoSelecionado = campoEscolhido;
            if (campoSelecionado != null
                    && !campoSelecionado.isEmpty()) {
                if (campoSelecionado.equals("foto")) {
                    midiaUtils.setLayoutCircular(true);
                } else if (campoSelecionado.equals("fundo")) {
                    midiaUtils.setLayoutCircular(false);
                }
            }
            boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(this);
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
                PermissionUtils.openAppSettings(FotoPerfilActivity.this, getApplicationContext());
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
                if (campoSelecionado != null && !campoSelecionado.isEmpty()) {
                    if (campoSelecionado.equals("foto")) {
                        ProgressBarUtils.ocultarProgressBar(progressFoto, FotoPerfilActivity.this);
                    } else if (campoSelecionado.equals("fundo")) {
                        ProgressBarUtils.ocultarProgressBar(progressFundo, FotoPerfilActivity.this);
                    }
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void exibirFoto(Uri uri) {
        if (campoSelecionado.equals("foto")) {
            GlideCustomizado.loadUrl(getApplicationContext(),
                    String.valueOf(uri), imgViewFoto, android.R.color.transparent,
                    GlideCustomizado.CIRCLE_CROP, false, false);
            ocultarProgressComDelay(progressFoto);
        } else if (campoSelecionado.equals("fundo")) {
            GlideCustomizado.loadUrl(getApplicationContext(),
                    String.valueOf(uri), imgViewFundo, android.R.color.transparent,
                    GlideCustomizado.CENTER_CROP, false, false);
            ocultarProgressComDelay(progressFundo);
        }
    }

    private void selecionadoGif() {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            if (campoSelecionado.equals("foto")) {
                tamanhoGif = "pequena";
                ProgressBarUtils.exibirProgressBar(progressFoto, FotoPerfilActivity.this);
            } else if (campoSelecionado.equals("fundo")) {
                tamanhoGif = "media";
                ProgressBarUtils.exibirProgressBar(progressFundo, FotoPerfilActivity.this);
            }
            midiaUtils.selecionarGif(TAG, tamanhoGif, new MidiaUtils.UriRecuperadaCallback() {
                @Override
                public void onRecuperado(Uri uriRecuperada) {
                    atribuirUri(uriRecuperada);
                    exibirFoto(uriRecuperada);
                }

                @Override
                public void onCancelado() {
                    if (campoSelecionado != null && !campoSelecionado.isEmpty()) {
                        if (campoSelecionado.equals("foto")) {
                            ProgressBarUtils.ocultarProgressBar(progressFoto, FotoPerfilActivity.this);
                        } else if (campoSelecionado.equals("fundo")) {
                            ProgressBarUtils.ocultarProgressBar(progressFundo, FotoPerfilActivity.this);
                        }
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void atribuirUri(Uri uriRecebida) {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            if (campoSelecionado.equals("foto")) {
                uriFoto = uriRecebida;
            } else if (campoSelecionado.equals("fundo")) {
                uriFundo = uriRecebida;
            }
        }
    }

    private void ocultarProgressComDelay(SpinKitView spinKitAlvo) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBarUtils.ocultarProgressBar(spinKitAlvo, FotoPerfilActivity.this);
            }
        }, 5000);
    }

    private void clickListeners() {

    }

    private void inicializarComponentes() {
        fabBack = findViewById(R.id.fabBack);
        imgViewFoto = findViewById(R.id.imgViewCadFotoUser);
        imgViewFundo = findViewById(R.id.imgViewCadFundoUser);
        progressFoto = findViewById(R.id.progressBarCadFotoUser);
        progressFundo = findViewById(R.id.progressBarCadFundoUser);
        imgBtnFotoGaleria = findViewById(R.id.imgBtnCadGaleria);
        imgBtnFotoCamera = findViewById(R.id.imgBtnCadCamera);
        imgBtnFotoGif = findViewById(R.id.imgBtnCadGif);
        imgBtnFundoGaleria = findViewById(R.id.imgBtnCadGaleriaFundo);
        imgBtnFundoCamera = findViewById(R.id.imgBtnCadCameraFundo);
        imgBtnFundoGif = findViewById(R.id.imgBtnCadGifFundo);
        btnContinuarCad = findViewById(R.id.btnContinuarCadFoto);
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
        }
    }
}