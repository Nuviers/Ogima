package com.example.ogima.activity;

import static com.iceteck.silicompressorr.videocompression.MediaController.selectCodec;
import static com.luck.picture.lib.utils.PictureFileUtils.isDownloadsDocument;
import static com.luck.picture.lib.utils.PictureFileUtils.isExternalStorageDocument;
import static com.luck.picture.lib.utils.PictureFileUtils.isMediaDocument;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.iceteck.silicompressorr.Util;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.microshow.rxffmpeg.RxFFmpegCommandList;
import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddDailyShortsActivity extends AppCompatActivity {

    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/mp4";
    private static final int OUTPUT_VIDEO_BIT_RATE = 800000; // Taxa de bits do vídeo de saída em bits por segundo
    private static final int OUTPUT_VIDEO_FRAME_RATE = 24; // Taxa de quadros do vídeo de saída em quadros por segundo
    private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 5; // Intervalo de quadros I no vídeo de saída
    private static final int OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private ImageView imgViewPreviewDailyShorts;
    private CardView cardViewDailyShorts;
    private ImageButton imgBtnAddGaleriaDaily, imgBtnAddCameraDaily,
            imgBtnAddGifDaily, imgBtnAddVideoDaily;

    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET};
    private Boolean selecionadoGaleria = false,
            selecionadoCamera = false;
    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private static final int MAX_FILE_SIZE_VIDEO = 17;
    private static final int CODE_PERMISSION_GALERIA = 22;
    private StorageReference videoRef;
    private StorageReference storageRef;
    private static final int SELECAO_GALERIA = 100,
            SELECAO_GIF = 200,
            SELECAO_VIDEO = 300,
            SELECAO_CAMERA = 400;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private ProgressDialog progressDialog;
    private VerificaTamanhoArquivo verificaTamanhoArquivo = new VerificaTamanhoArquivo();
    private Bitmap imagemGaleriaSelecionada, imagemCameraSelecionada;
    private Uri videoSelecionado;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private ArrayList<Uri> urisSelecionadas = new ArrayList<>();
    private ImageView imgViewDailyShorts;
    private PictureSelectorStyle selectorStyle;
    private Button btnSalvarDailyShorts;
    private TextView txtViewNrDailyShorts;

    private long nrDailyAtual;
    private boolean existemDailyShorts = false;
    private int nrDailyAnterior, nrDailyRecuperado;

    private String tipoMidiaPermissao = "";
    private boolean selecaoExistente = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Limpa o cache do app, recomendado fazer o mesmo com todas activity
        //que possuam interações com o CacheDir.
        File cacheDir = getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                    Log.d("CACHE DELETED ", "Successfully Deleteded Cache");
                }
            }
        }
    }

    private interface UploadCallback {
        void onUploadComplete(String urlDaily);

        void timeStampRecuperado(long timeStampNegativo);

        void onUploadError(String message);
    }

    private interface NrDailyCallback {
        void onRecovered(int nrRecuperado);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_shorts);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("DailyShorts");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        verificarDailyShorts(null);

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        selectorStyle = new PictureSelectorStyle();

        configStylePictureSelector();

        clickListeners();
    }

    private void checkPermissions(String tipoMidia) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissoesNecessarias) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), CODE_PERMISSION_GALERIA);
            } else {
                configClickListenerPorMidia(tipoMidia);
            }
        } else {
            configClickListenerPorMidia(tipoMidia);
        }
    }

    private void clickListeners() {
        imgBtnAddGaleriaDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verifica se o usuário já selecinou algo nessa sessão.
                if (!selecaoAnteriorExistente()) {
                    //tipoMidiaPermissao:
                    //Serve somente para que o requestPermission saiba como tratar
                    //do evento se todas permissões foram aceitas.
                    tipoMidiaPermissao = "imagem";
                    checkPermissions("imagem");
                }
            }
        });

        imgBtnAddVideoDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verifica se o usuário já selecinou algo nessa sessão.
                if (!selecaoAnteriorExistente()) {
                    //tipoMidiaPermissao:
                    //Serve somente para que o requestPermission saiba como tratar
                    //do evento se todas permissões foram aceitas.
                    tipoMidiaPermissao = "video";
                    checkPermissions("video");
                }
            }
        });

        btnSalvarDailyShorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarImagem();
            }
        });
    }

    private void selecionarFoto() {

        verificarDailyShorts(new NrDailyCallback() {
            @Override
            public void onRecovered(int nrRecuperado) {
                PictureSelector.create(AddDailyShortsActivity.this)
                        .openGallery(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                        .setSelectionMode(SelectModeConfig.MULTIPLE)
                        .setMaxSelectNum(nrRecuperado) // Permitir seleção múltipla de fotos
                        .setSelectorUIStyle(selectorStyle)
                        .setSelectMaxFileSize(MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                        .setImageEngine(GlideEngineCustomizado.createGlideEngine()) // Substitua GlideEngine pelo seu próprio mecanismo de carregamento de imagem, se necessário
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(ArrayList<LocalMedia> result) {

                                //Caso aconteça de alguma forma que a lista que já foi manipulada
                                //retorne com dados nela, ela é limpa para evitar duplicações.
                                if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
                                    ToastCustomizado.toastCustomizado("CLEAR", getApplicationContext());
                                    urisSelecionadas.clear();
                                }

                                ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                                if (result != null && result.size() > 0) {
                                    for (LocalMedia media : result) {

                                        // Faça o que for necessário com cada foto selecionada
                                        String path = media.getPath(); // Obter o caminho do arquivo da foto

                                        if (PictureMimeType.isHasImage(media.getMimeType())) {
                                            openCropActivity(Uri.parse(path), criarDestinoUri(result));
                                        } else {
                                            //Não suporte o recorte, tratar lógica de salvamento aqui.
                                            ToastCustomizado.toastCustomizadoCurto("Não suporta recorte", getApplicationContext());
                                        }
                                    }
                                    //*GlideCustomizado.montarGlide(getApplicationContext(), String.valueOf(path), imgViewPreviewDailyShorts, android.R.color.transparent);
                                }
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
            }
        });
    }

    private void selecionarVideo() {
        verificarDailyShorts(new NrDailyCallback() {
            @Override
            public void onRecovered(int nrRecuperado) {
                PictureSelector.create(AddDailyShortsActivity.this)
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
                                    //Caso aconteça de alguma forma que a lista que já foi manipulada
                                    //retorne com dados nela, ela é limpa para evitar duplicações.
                                    if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
                                        ToastCustomizado.toastCustomizado("CLEAR", getApplicationContext());
                                        urisSelecionadas.clear();
                                    }

                                    ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                                    if (result != null && result.size() > 0) {
                                        for (LocalMedia media : result) {
                                            //urisSelecionadas.add(uriVideo);
                                            //uparVideoTeste();

                                            // Faça o que for necessário com cada foto selecionada
                                            Uri uriVideo = Uri.parse(media.getPath()); // Obter o caminho do arquivo do video.

                                            //Salvando localmente temporariamente para que seja possível a compressão.
                                            File moviesDirectory = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                                            String fileName = DateUtils.getCreateFileName("videoCompressTk_") + ".mp4";
                                            File outputFile = new File(getCacheDir(), fileName);

                                            if (urisSelecionadas != null && urisSelecionadas.size() > 0
                                                    && urisSelecionadas.contains(uriVideo)) {
                                                //Evita duplicatas.
                                                return;
                                            }

                                            urisSelecionadas.add(uriVideo);

                                            //String uriComFile = "file://"+uriVideo.toString();

                                            Log.d("CAMINHO1", uriVideo.toString());
                                            ToastCustomizado.toastCustomizado("Caminho " + uriVideo.toString(), getApplicationContext());

                                            String caminhoReal = getPathFromUri(uriVideo);
                                            Log.d("CAMINHO2", caminhoReal);

                                            File file = new File(caminhoReal);
                                            File parentDir = file.getParentFile();

                                            if (parentDir != null) {

                                                String caminhoFull = "file://" + caminhoReal;
                                                Log.d("CAMINHO3", caminhoFull);

                                                Log.d("CAMINHO4", outputFile.getPath());

                                                otimizarVideo(caminhoFull, outputFile.getPath());
                                            }



                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                                //otimizarVideo(uriComFile, outputFile.getPath());
                                            }

                                            ToastCustomizado.toastCustomizado("Type: " + media.getMimeType(), getApplicationContext());
                                            //uparVideoTeste(uriVideo);

                                            //ToastCustomizado.toastCustomizadoCurto("Compressão feita com sucesso", getApplicationContext());
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
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_PERMISSION_GALERIA) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                if (tipoMidiaPermissao != null && !tipoMidiaPermissao.isEmpty()) {
                    configClickListenerPorMidia(tipoMidiaPermissao);
                }
            } else {
                // Permissions were not granted, handle it accordingly
                ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", getApplicationContext());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            ToastCustomizado.toastCustomizadoCurto("CROP", getApplicationContext());

            if (data != null) {
                try {
                    //Somente fotos chamaram o UCrop.REQUEST_CROP.
                    Uri imagemCortada = UCrop.getOutput(data);

                    if (imagemCortada != null) {
                        if (urisSelecionadas != null && urisSelecionadas.size() > 0
                                && urisSelecionadas.contains(imagemCortada)) {
                            ToastCustomizado.toastCustomizadoCurto("Return já existe", getApplicationContext());
                            return;
                        }
                        urisSelecionadas.add(imagemCortada);
                        //Falta considerar os dailys existentes.
                        if (existemDailyShorts) {
                            nrDailyRecuperado++;
                            txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyRecuperado + "/10");
                        } else {
                            nrDailyAtual++;
                            txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyAtual + "/10");
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void inicializandoComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        imgViewPreviewDailyShorts = findViewById(R.id.imgViewPreviewDailyShorts);
        cardViewDailyShorts = findViewById(R.id.cardViewDailyShorts);
        imgBtnAddGaleriaDaily = findViewById(R.id.imgBtnAddGaleriaDaily);
        imgBtnAddCameraDaily = findViewById(R.id.imgBtnAddCameraDaily);
        imgBtnAddGifDaily = findViewById(R.id.imgBtnAddGifDaily);
        imgBtnAddVideoDaily = findViewById(R.id.imgBtnAddVideoDaily);
        imgViewDailyShorts = findViewById(R.id.imgViewDailyShorts);
        btnSalvarDailyShorts = findViewById(R.id.btnSalvarDailyShorts);
        txtViewNrDailyShorts = findViewById(R.id.txtViewNrDailyShorts);
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(AddDailyShortsActivity.this);

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

    private Uri criarDestinoUri(ArrayList<LocalMedia> result) {

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

    private void salvarImagem() {

        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            for (Uri uriConfigurada : urisSelecionadas) {

                DatabaseReference dailyShortsRef = firebaseRef.child("dailyShorts");
                String idDailyShort = dailyShortsRef.push().getKey();

                uparNoStorage(uriConfigurada, new UploadCallback() {
                    //Sempre coloque as variáveis que são usadas no callback
                    //dentro do callback para não ter problemas
                    //assim é garantido que cada callback terá seu dado correto e não irá
                    //ter mistura de dados e erros.
                    String idDailyAtual = idDailyShort;
                    HashMap<String, Object> dadosDailyAtual = new HashMap<>();

                    @Override
                    public void onUploadComplete(String urlDaily) {
                        dadosDailyAtual.put("idDailyShort", idDailyAtual);
                        dadosDailyAtual.put("idDonoDailyShort", idUsuario);
                        dadosDailyAtual.put("urlMidia", urlDaily);
                        dadosDailyAtual.put("tipoMidia", "imagem");
                        recuperarTimestampNegativo(this);
                    }

                    @Override
                    public void timeStampRecuperado(long timeStampNegativo) {
                        dadosDailyAtual.put("timestampCriacaoDaily", timeStampNegativo);
                        //Passado por parâmetro para garantir os dados atuais ao callback
                        //correto.
                        salvarNoFirebase(idDailyAtual, dadosDailyAtual);
                    }

                    @Override
                    public void onUploadError(String mensagemError) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao salvar o dailyshort: " + mensagemError, getApplicationContext());
                    }
                });
            }
        } else {
            SnackbarUtils.showSnackbar(btnSalvarDailyShorts, "Selecione pelo uma mídia para que seja possível salvar o dailyShort");
        }
    }

    private void uparNoStorage(Uri uriAtual, UploadCallback uploadCallback) {

        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            String nomeRandomico = UUID.randomUUID().toString();
            //Criado storage aqui pois se o storage for reutilizado ele
            //ainda conterá configuração do storage e duplicara os dados anteriores.
            StorageReference imagemRef = storageRef.child("dailyShorts")
                    .child("imagens")
                    .child(idUsuario)
                    .child("imagem" + nomeRandomico + ".jpeg");
            //Verificando progresso do upload
            UploadTask uploadTask = imagemRef.putFile(uriAtual);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizado("FAIL", getApplicationContext());
                    uploadCallback.onUploadError(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ToastCustomizado.toastCustomizado("Upload com sucesso", getApplicationContext());

                    imagemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String urlDaily = uri.toString();
                            ToastCustomizado.toastCustomizado("Uri configurada " + urlDaily, getApplicationContext());
                            uploadCallback.onUploadComplete(urlDaily);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadCallback.onUploadError(e.getMessage());
                        }
                    });
                }
            });

                        /*
                        if (urisRecortadas != null && urisRecortadas.size() > 0) {
                            ToastCustomizado.toastCustomizado("Size " + urisRecortadas.size(), getApplicationContext());
                            imgViewDailyShorts.setVisibility(View.GONE);
                            GlideCustomizado.montarGlide(getApplicationContext(),
                                    String.valueOf(urisRecortadas.get(0)), imgViewPreviewDailyShorts, android.R.color.transparent);
                            imgViewPreviewDailyShorts.setVisibility(View.VISIBLE);
                        }
                         */
        }
    }

    private void uparVideoTeste(Uri uriAtual) {
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            String nomeRandomico = UUID.randomUUID().toString();
            //Criado storage aqui pois se o storage for reutilizado ele
            //ainda conterá configuração do storage e duplicara os dados anteriores.
            StorageReference videoRef = storageRef.child("dailyShorts")
                    .child("videos")
                    .child(idUsuario)
                    .child("video" + nomeRandomico + ".mp4");
            //Verificando progresso do upload
            UploadTask uploadTask = videoRef.putFile(uriAtual);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizado("FAIL", getApplicationContext());
                    //uploadCallback.onUploadError(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ToastCustomizado.toastCustomizado("Upload com sucesso", getApplicationContext());

                    videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String urlDaily = uri.toString();
                            ToastCustomizado.toastCustomizado("Uri configurada " + urlDaily, getApplicationContext());
                            //uploadCallback.onUploadComplete(urlDaily);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //uploadCallback.onUploadError(e.getMessage());
                        }
                    });
                }
            });
        }
    }

    private void salvarNoFirebase(String idDailyShort, HashMap<String, Object> dadosDaily) {
        DatabaseReference salvarDailyRef = firebaseRef.child("dailyShorts")
                .child(idUsuario).child(idDailyShort);

        salvarDailyRef.setValue(dadosDaily).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                ToastCustomizado.toastCustomizadoCurto("DailyShort salvo com sucesso", getApplicationContext());
                //finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao salvar dailyshort " + e.getMessage(), getApplicationContext());
            }
        });
    }

    private void recuperarTimestampNegativo(UploadCallback uploadCallback) {

        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(this, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timestampNegativo, getApplicationContext());
                        uploadCallback.timeStampRecuperado(timestampNegativo);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, getApplicationContext());
                        uploadCallback.onUploadError(errorMessage);
                    }
                });
            }
        });
    }

    private void verificarDailyShorts(NrDailyCallback nrDailyCallback) {

        DatabaseReference verificaDailyRef = firebaseRef.child("dailyShorts")
                .child(idUsuario);

        verificaDailyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {
                    long nrDailyShorts = snapshot.getChildrenCount();
                    txtViewNrDailyShorts.setText("DailyShorts: " + nrDailyShorts + "/10");
                    existemDailyShorts = true;
                    nrDailyAnterior = Math.toIntExact(10 - nrDailyShorts);
                    nrDailyRecuperado = 10 - nrDailyAnterior;
                    //ToastCustomizado.toastCustomizadoCurto("Nr inicial: " + nrDailyRecuperado, getApplicationContext());
                    if (nrDailyCallback != null) {
                        nrDailyCallback.onRecovered(nrDailyAnterior);
                    }
                    //ToastCustomizado.toastCustomizadoCurto("Selecao: " + nrDailyAnterior, getApplicationContext());
                } else {
                    txtViewNrDailyShorts.setText("DailyShorts: 0/10");
                    existemDailyShorts = false;
                    nrDailyAnterior = 10;
                    if (nrDailyCallback != null) {
                        nrDailyCallback.onRecovered(10);
                    }
                }
                verificaDailyRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configClickListenerPorMidia(String tipoMidia) {
        switch (tipoMidia) {
            case "imagem":
                selecionarFoto();
                break;
            case "video":
                selecionarVideo();
                break;
            case "gif":
                break;
        }
    }

    private boolean selecaoAnteriorExistente() {
        //Verifica se nessa sessão já foram selecionadas algumas mídias.
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            ToastCustomizado.toastCustomizado("Já foram selecionadas uma ou" +
                    " mais mídias, é necessário salvar elas antes que você" +
                    " selecione mais mídias", getApplicationContext());
            selecaoExistente = true;
        } else {
            selecaoExistente = false;
        }
        return selecaoExistente;
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


    private void otimizarVideo(String inputPath, String outputPath) {

        String[] commands = new String[]{
                "-y",        // Sobrescrever o arquivo de saída, se já existir
                "-i", inputPath,     // Caminho do arquivo de entrada
                "-c:v", "libx264",   // Codec de vídeo
                "-b:v", "2097k",     // Taxa de bits de vídeo
                "-r", "30",          // Taxa de quadros
                "-preset", "superfast",  // Preset de codificação de vídeo
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
                        ToastCustomizado.toastCustomizado("Caminho: " + compressedVideoUri, getApplicationContext());
                        uparVideoTeste(compressedVideoUri);
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
}