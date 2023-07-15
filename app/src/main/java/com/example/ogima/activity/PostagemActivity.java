package com.example.ogima.activity;

import static android.app.Activity.RESULT_OK;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.load.resource.gif.GifBitmapProvider;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GiphyUtils;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VideoUtils;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PostagemActivity extends AppCompatActivity implements View.OnClickListener {

    //Referências
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private StorageReference storageRef;
    //Dados para o usuário atual
    private String emailUsuario, idUsuario;
    //Componentes
    private ImageButton imgBtnAddCameraPostagem,
            imgBtnAddGaleriaPostagem, imgBtnAddGifPostagem,
            imgBtnAddVideoPostagem;
    private ProgressDialog progressDialog;

    //Giphy
    private final GiphyUtils giphyUtils = new GiphyUtils();
    private GiphyDialogFragment gdl;
    private String irParaProfile = null;
    private String selecaoPreDefinida = null;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private String tipoMidiaPermissao = null;
    private PictureSelectorStyle selectorStyle;
    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private static final int MAX_FILE_SIZE_VIDEO = 17;
    private static final int SELECAO_GIF = 200;
    private Uri uriSelecionada = null;
    private String urlGifSelecionada = null;
    private float crfBeforeCompression = 0.0f;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimparCacheUtils limparCacheUtils = new LimparCacheUtils();
        limparCacheUtils.clearAppCache(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaPerfil", "irParaPerfil");
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postagem);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Adicionar Postagem");

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

        //Configurações iniciais
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        selectorStyle = new PictureSelectorStyle();
        configStylePictureSelector();

        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imgBtnAddCameraPostagem.setOnClickListener(this);
        imgBtnAddGaleriaPostagem.setOnClickListener(this);
        imgBtnAddGifPostagem.setOnClickListener(this);
        imgBtnAddVideoPostagem.setOnClickListener(this);
    }

    private void configStylePictureSelector() {
        TitleBarStyle blueTitleBarStyle = new TitleBarStyle();
        blueTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_blue));

        BottomNavBarStyle numberBlueBottomNavBarStyle = new BottomNavBarStyle();
        numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_9b));
        numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_blue));
        numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_white));
        numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.ps_demo_blue_num_selected);
        numberBlueBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_53575e));
        numberBlueBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_53575e));

        SelectMainStyle numberBlueSelectMainStyle = new SelectMainStyle();
        numberBlueSelectMainStyle.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectNumberStyle(true);
        numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true);

        numberBlueSelectMainStyle.setSelectBackground(R.drawable.ps_demo_blue_num_selector);
        numberBlueSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_white));
        numberBlueSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_preview_blue_num_selector);

        numberBlueSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_9b));
        numberBlueSelectMainStyle.setSelectTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectText(R.string.ps_completed);

        selectorStyle.setTitleBarStyle(blueTitleBarStyle);
        selectorStyle.setBottomBarStyle(numberBlueBottomNavBarStyle);
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
                    }
                }
            }
        }, 100);
    }

    private void inicializandoComponentes() {
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        imgBtnAddCameraPostagem = findViewById(R.id.imgBtnAddCameraPostagem);
        imgBtnAddGaleriaPostagem = findViewById(R.id.imgBtnAddGaleriaPostagem);
        imgBtnAddGifPostagem = findViewById(R.id.imgBtnAddGifPostagem);
        imgBtnAddVideoPostagem = findViewById(R.id.imgBtnAddVideoPostagem);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                Uri imagemRecortada = UCrop.getOutput(data);
                if (imagemRecortada != null) {
                    uriSelecionada = imagemRecortada;
                    ToastCustomizado.toastCustomizadoCurto("Uri recuperada " + uriSelecionada, getApplicationContext());
                    enviarDadoParaConfig("foto");
                }
            }
        }
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
        }
    }

    private void checkPermissions() {
        if (tipoMidiaPermissao != null) {
            if (tipoMidiaPermissao.equals("gif")) {
                selecionarGif();
            } else {
                boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(this);
                if (galleryPermissionsGranted) {
                    // Permissões da galeria já concedidas.
                    switch (tipoMidiaPermissao) {
                        case "video":
                            selecionarVideo();
                            break;
                        case "galeria":
                            selecionarGaleria();
                            break;
                        case "camera":
                            boolean cameraPermissionsGranted = PermissionUtils.requestCameraPermissions(this);
                            if (cameraPermissionsGranted) {
                                selecionarCamera();
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
                        selecionarGif();
                    } else {
                        // Permissões da galeria já concedidas.
                        switch (tipoMidiaPermissao) {
                            case "video":
                                selecionarVideo();
                                break;
                            case "galeria":
                                selecionarGaleria();
                                break;
                            case "camera":
                                selecionarCamera();
                                break;
                        }
                    }
                }
            } else {
                // Permissões negadas.
                PermissionUtils.openAppSettings(this, getApplicationContext());
            }
        }
    }

    private void selecionarGaleria() {
        PictureSelector.create(PostagemActivity.this)
                .openGallery(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1)
                .setSelectorUIStyle(selectorStyle)
                .setSelectMaxFileSize(MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine()) // Substitua GlideEngine pelo seu próprio mecanismo de carregamento de imagem, se necessário
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {

                        //Caso aconteça de alguma forma que a lista que já foi manipulada
                        //retorne com dados nela, ela é limpa para evitar duplicações.
                        limparUri();

                        //ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {

                                // Faça o que for necessário com cada foto selecionada
                                String path = media.getPath(); // Obter o caminho do arquivo da foto

                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void limparUri() {
        if (uriSelecionada != null) {
            uriSelecionada = null;
        }
    }

    private void selecionarCamera() {
        PictureSelector.create(PostagemActivity.this)
                .openCamera(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setSelectMaxFileSize(MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {

                        limparUri();

                        //ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {

                                // Faça o que for necessário com cada foto selecionada
                                String path = media.getPath(); // Obter o caminho do arquivo da foto

                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void selecionarVideo() {
        PictureSelector.create(PostagemActivity.this)
                .openGallery(SelectMimeType.ofVideo()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1) // Permitir seleção múltipla de fotos
                .setSelectorUIStyle(selectorStyle)
                .setSelectMaxFileSize(MAX_FILE_SIZE_VIDEO * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine()) // Substitua GlideEngine pelo seu próprio mecanismo de carregamento de imagem, se necessário
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        try {

                            limparUri();

                            //ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                            if (result != null && result.size() > 0) {

                                for (LocalMedia media : result) {

                                    // Faça o que for necessário com cada foto selecionada
                                    Uri uriVideo = Uri.parse(media.getPath()); // Obter o caminho do arquivo do video.
                                    Log.d("CONFIG URI", "Caminho original: " + uriVideo.toString());

                                    //Caminho do destino da uri.
                                    String fileName = DateUtils.getCreateFileName("videoCompress_") + ".mp4";
                                    File outputFile = new File(getCacheDir(), fileName);
                                    Log.d("CONFIG URI", "Destino: " + outputFile.getPath());

                                    //Recupera o caminho real da uri que está localizada no dispositivo.
                                    String caminhoReal = getPathFromUri(uriVideo);
                                    Log.d("CONFIG URI", "Caminho configurado: " + caminhoReal);

                                    if (caminhoReal != null) {

                                        //Adicionado file:// na frente do caminho da uri, pois o
                                        //RxFFmpeg necessita dessa configuração para funcionar.
                                        String caminhoConfigurado = "file://" + caminhoReal;
                                        Log.d("CONFIG URI", "Caminho com a nomenclatura file " + caminhoConfigurado);


                                        otimizarVideo(caminhoConfigurado, outputFile.getPath(), uriVideo, 0.0f);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastCustomizado.toastCustomizado("Erro: " + e.getMessage(), getApplicationContext());
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void selecionarGif() {

        giphyUtils.selectGif(getApplicationContext(), new GiphyUtils.GifSelectionListener() {
            @Override
            public void onGifSelected(String gifPequena, String gifMedio, String gifOriginal) {
                if (gifMedio != null && !gifMedio.isEmpty()) {
                    urlGifSelecionada = gifMedio;
                    enviarDadoParaConfig("gif");
                }
            }
        });
        gdl = giphyUtils.retornarGiphyDialog();
        gdl.show(PostagemActivity.this.getSupportFragmentManager(), "PostagemActivity");
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(PostagemActivity.this);

    }

    //*Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptions() {
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar imagem");
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }

    private Uri destinoImagemUri(ArrayList<LocalMedia> result) {

        Uri destinationUri = null;

        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
                File outputFile = new File(getCacheDir(), fileName);
                destinationUri = Uri.fromFile(outputFile);
                //ToastCustomizado.toastCustomizado("Caminho: " + destinationUri, getApplicationContext());
                Log.d("Caminho ", String.valueOf(destinationUri));
                break; // Sai do loop após encontrar a primeira imagem
            }
        }

        return destinationUri;
    }

    private void enviarDadoParaConfig(String tipoMidia) {
        if (tipoMidiaPermissao != null
                && !tipoMidiaPermissao.isEmpty()) {

            if (tipoMidia.equals("gif")) {
                if (urlGifSelecionada != null && !urlGifSelecionada.isEmpty()) {
                    Intent intent = new Intent(this, ConfigurarPostagemActivity.class);
                    intent.putExtra("novaGif", urlGifSelecionada);
                    intent.putExtra("tipoPostagem", tipoMidia);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent(this, ConfigurarPostagemActivity.class);
                intent.putExtra("novaPostagem", uriSelecionada);
                intent.putExtra("tipoPostagem", tipoMidia);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }

    private void otimizarVideo(String inputPath, String outputPath, Uri uriVideo, float crfAumentado) {

        //Criar lógica que envolve pegar informações do vídeo
        // e com base nelas fazer uma lógica com a qualidade desejada
        //e assim chegar a uma taxa de bits equilibrada.

        //

        VideoUtils.getVideoInfo(getApplicationContext(), uriVideo, new VideoUtils.VideoInfoCallback() {
            @Override
            public void onVideoInfoReceived(long durationMs, float frameRate, long fileSizeBytes, int width, int height, int bitsRate) {

                if (crfAumentado != -1 && crfAumentado > 0.0f) {

                } else {
                    exibirProgressDialog("otimizando");
                }

                // Cálculo do CRF com base na taxa de bits média e na resolução do vídeo
                //Quanto maior a porcentagem que nesse caso é 35 maior será a perca de qualidade
                //e menor será o tamanho do arquivo. Falta ajustar o bitsRate também e encontrar
                //uma boa porcentagem de perca de qualidade.

                /*
                float crf = 18.0f + (20.0f / 100.0f) * (51.0f - 18.0f);
                crf = Math.max(18.0f, Math.min(crf, 51.0f)); // Limita o valor do CRF entre 18 e 51
                int crfFormatado = Math.round(crf);
                 */

                // Verificar se o tamanho do arquivo é maior ou igual a 10 MB (em bytes)
                boolean isFileSizeGreaterOrEqual10MB = fileSizeBytes >= 10 * 1024 * 1024;

                float crf;

                String velocidade;
                String fpsVideo;

                int minCrf1080p = 20;
                int maxCrf1080p = 26;
                int minCrf720p = 23;
                int maxCrf720p = 28;
                int minCrf480p = 24;
                int maxCrf480p = 30;

                if (isFileSizeGreaterOrEqual10MB) {
                    // Diminuir a qualidade em 45% para vídeos pesados (>= 10 MB)
                    velocidade = "superfast";

                    Log.d("VideoUtils", "DIMINUIR CRF");
                } else {
                    // Aumentar a qualidade em 55% para vídeos leves (< 10 MB)
                    velocidade = "superfast";

                    Log.d("VideoUtils", "AUMENTAR CRF");
                }

                //Superfast pois se o vídeo ficou maior do que o
                //tamanho original a compressão será tão rápida que não irá comprometer
                //a experiência do usuário caso o vídeo tenha que processado novamente.

                float diferencaCRF;

                boolean aumentarCrf = false;

                // Verificar a resolução do vídeo e ajustar o valor inicial do CRF

                if (crfAumentado != -1 && crfAumentado > 0.0f) {
                    crf = crfAumentado;
                } else {
                    if (width >= 1080 && height >= 1920) {

                        if (bitsRate >= 3800) {
                            diferencaCRF = 4.0f; // Aumentar a diferença fixa para diminuir mais a qualidade
                        } else {
                            diferencaCRF = 2.0f; // Ajuste para equilibrar a qualidade
                        }

                        crf = retornarCrf(minCrf1080p, maxCrf1080p, bitsRate, 3800, 1600, diferencaCRF);

                        if (aumentarCrf(bitsRate, 3800)) {
                            crf++;
                        }

                        ToastCustomizado.toastCustomizadoCurto("CRF: " + crf, getApplicationContext());

                    } else if (width >= 720 && height >= 1280) {

                        if (bitsRate >= 2800) {
                            diferencaCRF = 3.0f; // Aumentar a diferença fixa para diminuir mais a qualidade
                        } else {
                            diferencaCRF = 1.5f; // Ajuste para equilibrar a qualidade
                        }

                        crf = retornarCrf(minCrf720p, maxCrf720p, bitsRate, 2800, 1500, diferencaCRF);
                    } else {

                        if (bitsRate >= 2000) {
                            diferencaCRF = 1.5f; // Aumentar a diferença fixa para diminuir mais a qualidade
                        } else {
                            diferencaCRF = 1.5f; // Ajuste para equilibrar a qualidade
                        }

                        crf = retornarCrf(minCrf480p, maxCrf480p, bitsRate, 2000, 850, diferencaCRF);
                    }
                }

                int crfFormatado = Math.round(crf); // Valor do CRF arredondado

                Log.d("VideoUtils", "CRF: " + crfFormatado);

                Log.d("VideoUtils", "Velocidade: " + velocidade);

                if (frameRate >= 30) {
                    fpsVideo = String.valueOf(frameRate);
                } else {
                    fpsVideo = "30";
                }

                crfBeforeCompression = crf;

                Log.d("VideoUtils", "FPS: " + fpsVideo);

                String[] commands = new String[]{
                        "-y",        // Sobrescrever o arquivo de saída, se já existir
                        "-i", inputPath,     // Caminho do arquivo de entrada
                        "-r", fpsVideo,          // Taxa de quadros
                        "-vcodec", "libx264",  // Codec de vídeo
                        "-crf", String.valueOf(crfFormatado),       // Fator de qualidade constante (Quanto menor, melhor qualidade, mas maior tamanho)
                        "-s", width + "x" + height,   // Resolução desejada do vídeo comprimido
                        "-preset", velocidade,  // Preset de codificação de vídeo
                        //"-b:v", String.valueOf(targetBitrateFormatado)+"k",    // Taxa de bits de vídeo
                        outputPath       // Caminho do arquivo de saída
                };

                RxFFmpegInvoke.getInstance().setDebug(true);

                RxFFmpegInvoke.getInstance()
                        .runCommandRxJava(commands)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new RxFFmpegSubscriber() {
                            @Override
                            public void onFinish() {
                                // Compressão concluída com sucesso
                                Uri compressedVideoUri = Uri.fromFile(new File(outputPath));

                                File file = new File(outputPath);
                                long fileSizeInBytesCompress = file.length();
                                long fileSizeInKB = fileSizeInBytesCompress / 1024;
                                long fileSizeInMB = fileSizeInKB / 1024;

                                if (fileSizeInBytesCompress >= fileSizeBytes) {
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    crfBeforeCompression += 1;
                                    otimizarVideo(inputPath, outputPath, uriVideo, crfBeforeCompression);
                                } else {
                                    crfBeforeCompression = 0.0f;
                                    uriSelecionada = compressedVideoUri;
                                    ocultarProgressDialog();
                                    enviarDadoParaConfig("video");
                                }

                                ToastCustomizado.toastCustomizado("Size: " + fileSizeInMB, getApplicationContext());
                                Log.d("Tamanho do Arquivo", "Tamanho: " + fileSizeInMB + " MB");

                                //ToastCustomizado.toastCustomizado("Caminho: " + compressedVideoUri, getApplicationContext());
                            }

                            @Override
                            public void onProgress(int progress, long progressTime) {
                                // Progresso da compressão
                            }

                            @Override
                            public void onCancel() {
                                // Compressão cancelada
                            }

                            @Override
                            public void onError(String message) {
                                // Erro durante a compressão
                                ToastCustomizado.toastCustomizado("Error: " + message, getApplicationContext());
                                Log.d("MPEG COM ERRO ", message);
                            }
                        });
            }
        });
    }

    private void exibirProgressDialog(String tipoMensagem) {

        switch (tipoMensagem) {
            case "config":
                progressDialog.setMessage("Ajustando mídia, aguarde um momento...");
                break;
            case "otimizando":
                progressDialog.setMessage("Otimizando vídeo, aguarde um momento...");
                break;
        }
        if (!isFinishing()) {
            progressDialog.show();
        }
    }

    private void ocultarProgressDialog() {
        if (progressDialog != null && !isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private int retornarCrf(int minCrf, int maxCrf, int bitrate, int maxBitrate, int minBitrate, float diferencaCRF) {

        Log.d("VideoUtils", "Min: " + minCrf);
        Log.d("VideoUtils", "Max: " + maxCrf);

        float retorno = minCrf + ((float) (maxCrf - minCrf) * (1 - ((float) bitrate - minBitrate) / (maxBitrate - minBitrate)));

        int ajustarRetorno;

        if (retorno < minCrf) {
            ajustarRetorno = minCrf;
        } else if (retorno > maxCrf) {
            ajustarRetorno = maxCrf;
        } else {
            ajustarRetorno = Math.round(retorno);
        }

        //float diferencaCRF = 1.5f; // Diferença fixa entre os valores mínimo e máximo do CRF para diminuir a qualidade

        ajustarRetorno += diferencaCRF;

        // Verifica se o valor ajustado está dentro do intervalo [minCrf, maxCrf]
        ajustarRetorno = Math.max(minCrf, Math.min(ajustarRetorno, maxCrf));

        //int retorno = (minCrf + maxCrf);
        Log.d("VideoUtils", "Retorno: " + ajustarRetorno);
        //return (minCrf + maxCrf) / 2;
        return ajustarRetorno;
    }

    private boolean aumentarCrf(int bitsRateAtual, int bitsRateMax) {
        boolean retorno;
        if (bitsRateAtual != 1 && bitsRateAtual > bitsRateMax) {
            retorno = true;
        } else {
            retorno = false;
        }
        return retorno;
    }
}
