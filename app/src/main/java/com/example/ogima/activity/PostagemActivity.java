package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GiphyUtils;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.IntentUtils;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.RecuperarUriUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VideoUtils;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.BottomNavBarStyle;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.DateUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PostagemActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private StorageReference storageRef;
    private String idUsuario = "";
    private ImageButton imgBtnAddCameraPostagem,
            imgBtnAddGaleriaPostagem, imgBtnAddGifPostagem,
            imgBtnAddVideoPostagem, imgBtnAddTextPostagem;
    private ProgressDialog progressDialog;
    private String irParaProfile = null;
    private String selecaoPreDefinida = null;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private String tipoMidiaPermissao = null;
    private Uri uriSelecionada = null;
    private String urlGifSelecionada = null;
    private RecuperarUriUtils recuperarUriUtils;
    private static final String TAG = "PostagemActivity";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimparCacheUtils limparCacheUtils = new LimparCacheUtils();
        limparCacheUtils.clearAppCache(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        if (irParaProfile != null && !irParaProfile.isEmpty()) {
            IntentUtils.irParaProfile(PostagemActivity.this, getApplicationContext());
        } else {
            super.onBackPressed();
        }
    }

    public PostagemActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postagem);
        inicializandoComponentes();
        configInicial();
        clickListeners();
    }

    private void verificarSelecaoPreDefinida() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (selecaoPreDefinida != null && !selecaoPreDefinida.isEmpty()) {
                    switch (selecaoPreDefinida) {
                        case "video":
                            tipoMidiaPermissao = "video";
                            imgBtnAddVideoPostagem.performClick();
                            break;
                        case "gif":
                            tipoMidiaPermissao = "gif";
                            imgBtnAddGifPostagem.performClick();
                            break;
                        case "galeria":
                            tipoMidiaPermissao = "galeria";
                            imgBtnAddGaleriaPostagem.performClick();
                            break;
                        case "camera":
                            tipoMidiaPermissao = "camera";
                            imgBtnAddCameraPostagem.performClick();
                            break;
                        case "texto":
                            tipoMidiaPermissao = "texto";
                            imgBtnAddTextPostagem.performClick();
                            break;
                    }
                }
            }
        }, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        recuperarUriUtils.handleActivityResultPhoto(requestCode, resultCode, data, new RecuperarUriUtils.UriRecuperadaCallback() {
            @Override
            public void onRecuperado(Uri uriRecuperada) {
                limparUriEOcultarProgress();
                enviarUri(uriRecuperada, "foto");
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void enviarUri(Uri uriRecuperada, String tipoMidia) {
        Intent intent = new Intent(PostagemActivity.this, ConfigurePostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("tipoMidia", tipoMidia);
        intent.putExtra("uriRecuperada", uriRecuperada);
        startActivity(intent);
    }

    private void enviarUrlGif(String urlGif) {
        Intent intent = new Intent(PostagemActivity.this, ConfigurePostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("tipoMidia", "gif");
        intent.putExtra("urlGif", urlGif);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtnAddGaleriaPostagem:
                tipoMidiaPermissao = "galeria";
                checkPermissions();
                break;
            case R.id.imgBtnAddCameraPostagem:
                tipoMidiaPermissao = "camera";
                checkPermissions();
                break;
            case R.id.imgBtnAddGifPostagem:
                tipoMidiaPermissao = "gif";
                checkPermissions();
                break;
            case R.id.imgBtnAddVideoPostagem:
                tipoMidiaPermissao = "video";
                checkPermissions();
                break;
            case R.id.imgBtnAddTextPostagem:
                tipoMidiaPermissao = "texto";
                checkPermissions();
                break;
        }
    }

    private void checkPermissions() {
        if (tipoMidiaPermissao != null) {
            //Gif e texto não precisam de permissões especificas, por isso
            //a lógica de seleção de mídia é chamada nesse momento.
            if (tipoMidiaPermissao.equals("gif")) {
                selecionadoGif();
            } else if (tipoMidiaPermissao.equals("texto")) {
                selecionadoTexto();
            } else {
                boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(PostagemActivity.this);
                if (galleryPermissionsGranted) {
                    // Permissões da galeria já concedidas.
                    switch (tipoMidiaPermissao) {
                        case "video":
                            selecionadoVideo();
                            break;
                        case "galeria":
                            selecionadoGaleria();
                            break;
                        case "camera":
                            boolean cameraPermissionsGranted = PermissionUtils.requestCameraPermissions(PostagemActivity.this);
                            if (cameraPermissionsGranted) {
                                selecionadoCamera();
                            }
                            break;
                    }
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
                if (tipoMidiaPermissao != null) {
                    if (!tipoMidiaPermissao.equals("gif")) {
                        selecionadoGif();
                    } else {
                        // Permissões da galeria já concedidas.
                        switch (tipoMidiaPermissao) {
                            case "video":
                                selecionadoVideo();
                                break;
                            case "galeria":
                                selecionadoGaleria();
                                break;
                            case "camera":
                                selecionadoCamera();
                                break;
                        }
                    }
                }
            } else {
                // Permissões negadas.
                PermissionUtils.openAppSettings(PostagemActivity.this, getApplicationContext());
            }
        }
    }

    private void limparUri() {
        if (uriSelecionada != null) {
            uriSelecionada = null;
        }
    }

    private void selecionadoGaleria() {
        recuperarUriUtils.selecionadoGaleria(false);
    }

    private void selecionadoCamera() {
        recuperarUriUtils.selecionadoCamera(false);
    }

    private void selecionadoGif() {
        recuperarUriUtils.selecionadoGif(PostagemActivity.this.getSupportFragmentManager(), TAG, SizeUtils.MEDIUM_GIF, new RecuperarUriUtils.GifRecuperadaCallback() {
            @Override
            public void onRecuperado(String urlGif) {
                enviarUrlGif(urlGif);
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void selecionadoTexto(){
        Intent intent = new Intent(PostagemActivity.this, ConfigurePostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("tipoMidia", "texto");
        startActivity(intent);
    }

    private void selecionadoVideo() {
        recuperarUriUtils.selecionadoVideo(progressDialog, new RecuperarUriUtils.UriRecuperadaCallback() {
            @Override
            public void onRecuperado(Uri uriRecuperada) {
                limparUriEOcultarProgress();
                enviarUri(uriRecuperada, "video");
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void limparUriEOcultarProgress() {
        recuperarUriUtils.limparUri();
        recuperarUriUtils.ocultarProgressDialog(progressDialog);
    }

    private void configInicial() {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Adicionar Postagem");
        recuperarUriUtils = new RecuperarUriUtils(PostagemActivity.this, getApplicationContext());
        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }
            if (dados.containsKey("selecaoPreDefinida")) {
                selecaoPreDefinida = dados.getString("selecaoPreDefinida");
                verificarSelecaoPreDefinida();
            }
        }
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        progressDialog = new ProgressDialog(PostagemActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    private void clickListeners() {
        imgBtnAddCameraPostagem.setOnClickListener(this);
        imgBtnAddGaleriaPostagem.setOnClickListener(this);
        imgBtnAddGifPostagem.setOnClickListener(this);
        imgBtnAddVideoPostagem.setOnClickListener(this);
        imgBtnAddTextPostagem.setOnClickListener(this);

        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        imgBtnAddCameraPostagem = findViewById(R.id.imgBtnAddCameraPostagem);
        imgBtnAddGaleriaPostagem = findViewById(R.id.imgBtnAddGaleriaPostagem);
        imgBtnAddGifPostagem = findViewById(R.id.imgBtnAddGifPostagem);
        imgBtnAddVideoPostagem = findViewById(R.id.imgBtnAddVideoPostagem);
        imgBtnAddTextPostagem = findViewById(R.id.imgBtnAddTextPostagem);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
    }
}
