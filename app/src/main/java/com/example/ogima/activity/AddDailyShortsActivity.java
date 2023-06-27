package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterDailyShortsSelecao;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.example.ogima.helper.VideoUtils;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.utils.GifSizeFilter;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddDailyShortsActivity extends AppCompatActivity implements AdapterDailyShortsSelecao.RemoverDailyListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private CardView cardViewDailyShorts;
    private ImageButton imgBtnAddGaleriaDaily,
            imgBtnAddGifDaily, imgBtnAddVideoDaily;

    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET};
    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private static final int MAX_FILE_SIZE_VIDEO = 17;
    private static final int CODE_PERMISSION_GALERIA = 22;
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

    private float crfBeforeCompression = 0.0f;

    private RecyclerView recyclerViewDailyShorts;
    private LinearLayoutManager linearLayoutManager;

    private AdapterDailyShortsSelecao adapterDailyShorts;
    private CardView cardRedondoDaily;
    private FrameLayout framePreviewDaily;
    private StyledPlayerView styledPlayer;
    private SpinKitView spinProgressBarExo;
    private ExoPlayer exoPlayer;
    private Player.Listener listenerExo;
    private boolean isControllerVisible = false;
    private boolean dailyShortAtivo = false;

    private ArrayList<String> urlMidiaUpada = new ArrayList<>();

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

        releaseExoPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pauseExoPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeExoPlayer();
    }

    @Override
    public void onDailyRemocao(Uri uriRemovido, int posicao) {

    }

    private interface UploadCallback {
        void onUploadComplete(String urlDaily);

        void timeStampRecuperado(long timeStampNegativo, String dataFormatada);

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

        configRecyclerView();
    }

    private void configRecyclerView() {
        if (linearLayoutManager != null) {

        } else {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        }

        recyclerViewDailyShorts.setHasFixedSize(true);
        recyclerViewDailyShorts.setLayoutManager(linearLayoutManager);

        if (recyclerViewDailyShorts.getOnFlingListener() == null) {
            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(recyclerViewDailyShorts);
        }

        if (adapterDailyShorts != null) {

        } else {
            adapterDailyShorts = new AdapterDailyShortsSelecao(getApplicationContext(),
                    urisSelecionadas, this::onDailyRemocao);
        }

        recyclerViewDailyShorts.setAdapter(adapterDailyShorts);
        adapterDailyShorts.notifyDataSetChanged();
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

        imgBtnAddGifDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selecaoAnteriorExistente()) {
                    //tipoMidiaPermissao:
                    //Serve somente para que o requestPermission saiba como tratar
                    //do evento se todas permissões foram aceitas.
                    tipoMidiaPermissao = "gif";
                    checkPermissions("gif");
                }
            }
        });

        btnSalvarDailyShorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (tipoMidiaPermissao != null) {
                    switch (tipoMidiaPermissao) {
                        case "imagem":
                            salvarImagem();
                            break;
                        case "video":
                            salvarVideo();
                            break;
                        case "gif":
                            salvarGif();
                            break;
                    }
                } else {
                    SnackbarUtils.showSnackbar(btnSalvarDailyShorts, "Selecione pelo menos uma mídia para que seja possível salvar o dailyShort");
                }
            }
        });
    }

    private void selecionarFoto() {

        verificarDailyShorts(new NrDailyCallback() {
            @Override
            public void onRecovered(int nrRecuperado) {

                if (nrRecuperado == -1 || nrRecuperado == 0) {
                    ToastCustomizado.toastCustomizadoCurto("Você já antingiu o limite de seleção de mídias", getApplicationContext());
                } else {
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
                                    limparLista();

                                    ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

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
            }
        });
    }

    private void selecionarVideo() {
        verificarDailyShorts(new NrDailyCallback() {
            @Override
            public void onRecovered(int nrRecuperado) {

                if (nrRecuperado == -1 || nrRecuperado == 0) {
                    ToastCustomizado.toastCustomizadoCurto("Você já antingiu o limite de seleção de mídias", getApplicationContext());
                } else {
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

                                        exibirProgressDialog("config");

                                        //Caso aconteça de alguma forma que a lista que já foi manipulada
                                        //retorne com dados nela, ela é limpa para evitar duplicações.
                                        limparLista();

                                        ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                                        if (result != null && result.size() > 0) {

                                            if (existemDailyShorts) {
                                                nrDailyRecuperado++;
                                                txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyRecuperado + "/10");
                                            } else {
                                                nrDailyAtual++;
                                                txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyAtual + "/10");
                                            }

                                            for (LocalMedia media : result) {

                                                // Faça o que for necessário com cada foto selecionada
                                                Uri uriVideo = Uri.parse(media.getPath()); // Obter o caminho do arquivo do video.
                                                Log.d("CONFIG URI", "Caminho original: " + uriVideo.toString());

                                                procuraDuplicata(uriVideo);

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
                                        ocultarProgressDialog();
                                        ToastCustomizado.toastCustomizado("Erro: " + e.getMessage(), getApplicationContext());
                                    }
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                }
            }
        });
    }

    private void selecionarGif() {

        verificarDailyShorts(new NrDailyCallback() {
            @Override
            public void onRecovered(int nrRecuperado) {

                if (nrRecuperado == -1 || nrRecuperado == 0) {
                    ocultarProgressDialog();
                    ToastCustomizado.toastCustomizadoCurto("Você já antingiu o limite de seleção de mídias", getApplicationContext());
                } else {
                    Matisse.from(AddDailyShortsActivity.this)
                            .choose(MimeType.of(MimeType.GIF), false)
                            .countable(true)
                            .maxSelectable(nrRecuperado)
                            .addFilter(new GifSizeFilter(MAX_FILE_SIZE_IMAGEM * 1024 * 1024))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            .thumbnailScale(0.85f)
                            .imageEngine(new GlideEngine())
                            .showSingleMediaType(true)
                            .originalEnable(true)
                            .autoHideToolbarOnSingleTap(true)
                            .forResult(SELECAO_GIF);
                }
            }
        });

        /*
        verificarDailyShorts(new NrDailyCallback() {
            @Override
            public void onRecovered(int nrRecuperado) {
                if (nrRecuperado == -1 || nrRecuperado == 0
                        || nrRecuperado != -1 && urisSelecionadas != null
                        && urisSelecionadas.size() >= nrRecuperado) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de dailyShorts atingido", getApplicationContext());
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("image/gif");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        startActivityForResult(intent, SELECAO_GIF);
                    }
                }
            }
        });
         */

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
                        exibirProgressDialog("config");
                        if (urisSelecionadas != null && urisSelecionadas.size() > 0
                                && urisSelecionadas.contains(imagemCortada)) {
                            ToastCustomizado.toastCustomizadoCurto("Return já existe", getApplicationContext());
                            return;
                        }
                        adicionarUri(imagemCortada);

                        if (existemDailyShorts) {
                            nrDailyRecuperado++;
                            txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyRecuperado + "/10");
                        } else {
                            nrDailyAtual++;
                            txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyAtual + "/10");
                        }

                        ocultarProgressDialog();
                    } else {
                        ocultarProgressDialog();
                    }
                } catch (Exception ex) {
                    ocultarProgressDialog();
                    ex.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == SELECAO_GIF) {
            if (data != null) {

                exibirProgressDialog("config");
                List<Uri> selectedUris = Matisse.obtainResult(data);

                for (int i = 0; i < selectedUris.size(); i++) {

                    Uri gifUri = selectedUris.get(i);

                    adicionarUri(gifUri);

                    if (existemDailyShorts) {
                        nrDailyRecuperado++;
                        txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyRecuperado + "/10");
                    } else {
                        nrDailyAtual++;
                        txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyAtual + "/10");
                    }

                }

                ocultarProgressDialog();
                /*

                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    // Várias gifs foram selecionadas ao mesmo tempo.
                    int count = clipData.getItemCount();

                    for (int i = 0; i < count; i++) {
                        Uri gifUri = clipData.getItemAt(i).getUri();

                        if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM,
                                gifUri, getApplicationContext())) {

                            urisSelecionadas.add(gifUri);

                            if (existemDailyShorts) {
                                nrDailyRecuperado++;
                                txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyRecuperado + "/10");
                            } else {
                                nrDailyAtual++;
                                txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyAtual + "/10");
                            }

                        } else if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
                            contRecusado++;
                            urisSelecionadas.remove(gifUri);
                            if (contRecusado != -1 && contRecusado > 1) {
                                ToastCustomizado.toastCustomizadoCurto(String.valueOf(contRecusado) + " gifs foram excederam o limite de MB permitido", getApplicationContext());
                            } else {
                                ToastCustomizado.toastCustomizadoCurto("Sua gif excedeu o limite de MB permitido", getApplicationContext());
                            }
                        }
                    }

                } else {
                    // Apenas uma gif foi selecionada
                    Uri gifUri = data.getData();

                    if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM,
                            gifUri, getApplicationContext())) {

                        urisSelecionadas.add(gifUri);

                        if (existemDailyShorts) {
                            nrDailyRecuperado++;
                            txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyRecuperado + "/10");
                        } else {
                            nrDailyAtual++;
                            txtViewNrDailyShorts.setText("DailyShorts:" + nrDailyAtual + "/10");
                        }
                    } else {
                        ToastCustomizado.toastCustomizadoCurto("Sua gif excedeu o limite de MB permitido", getApplicationContext());
                    }
                }
                   */
            }
        }
    }

    private void inicializandoComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        cardViewDailyShorts = findViewById(R.id.cardViewDailyShorts);
        imgBtnAddGaleriaDaily = findViewById(R.id.imgBtnAddGaleriaDaily);
        imgBtnAddGifDaily = findViewById(R.id.imgBtnAddGifDaily);
        imgBtnAddVideoDaily = findViewById(R.id.imgBtnAddVideoDaily);
        imgViewDailyShorts = findViewById(R.id.imgViewDailyShorts);
        btnSalvarDailyShorts = findViewById(R.id.btnSalvarDailyShorts);
        txtViewNrDailyShorts = findViewById(R.id.txtViewNrDailyShorts);
        recyclerViewDailyShorts = findViewById(R.id.recyclerViewDailyShortsSelecao);
        cardRedondoDaily = findViewById(R.id.cardRedondoDaily);

        framePreviewDaily = findViewById(R.id.framePreviewDaily);
        styledPlayer = findViewById(R.id.styledPlayerPreviewDaily);
        spinProgressBarExo = findViewById(R.id.spinProgressBarExo);
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

    private void salvarImagem() {

        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {

            exibirProgressDialog("upload");

            for (Uri uriConfigurada : urisSelecionadas) {

                DatabaseReference dailyShortsRef = firebaseRef.child("dailyShorts");
                String idDailyShort = dailyShortsRef.push().getKey();

                uparImagemNoStorage(uriConfigurada, new UploadCallback() {
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
                        urlMidiaUpada.add(urlDaily);
                        recuperarTimestampNegativo(this);
                    }

                    @Override
                    public void timeStampRecuperado(long timeStampNegativo, String dataFormatada) {
                        dadosDailyAtual.put("timestampCriacaoDaily", timeStampNegativo);
                        //Passado por parâmetro para garantir os dados atuais ao callback
                        //correto.
                        salvarNoFirebase(idDailyAtual, dadosDailyAtual, timeStampNegativo, dataFormatada);
                    }

                    @Override
                    public void onUploadError(String mensagemError) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao salvar o dailyshort: " + mensagemError, getApplicationContext());
                    }
                });
            }
        } else {
            ocultarProgressDialog();
            SnackbarUtils.showSnackbar(btnSalvarDailyShorts, "Selecione pelo menos uma mídia para que seja possível salvar o dailyShort");
        }
    }

    private void uparImagemNoStorage(Uri uriAtual, UploadCallback uploadCallback) {

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
        }
    }

    private void salvarVideo() {
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {

            exibirProgressDialog("upload");

            for (Uri uriConfigurada : urisSelecionadas) {

                DatabaseReference dailyShortsRef = firebaseRef.child("dailyShorts");
                String idDailyShort = dailyShortsRef.push().getKey();

                uparVideoStorage(uriConfigurada, new UploadCallback() {
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
                        dadosDailyAtual.put("tipoMidia", "video");
                        urlMidiaUpada.add(urlDaily);
                        recuperarTimestampNegativo(this);
                    }

                    @Override
                    public void timeStampRecuperado(long timeStampNegativo, String dataFormatada) {
                        dadosDailyAtual.put("timestampCriacaoDaily", timeStampNegativo);
                        //Passado por parâmetro para garantir os dados atuais ao callback
                        //correto.
                        salvarNoFirebase(idDailyAtual, dadosDailyAtual, timeStampNegativo, dataFormatada);
                    }

                    @Override
                    public void onUploadError(String mensagemError) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao salvar o dailyshort: " + mensagemError, getApplicationContext());
                    }
                });
            }
        } else {
            ocultarProgressDialog();
            SnackbarUtils.showSnackbar(btnSalvarDailyShorts, "Selecione pelo menos uma mídia para que seja possível salvar o dailyShort");
        }
    }

    private void salvarGif() {

        ToastCustomizado.toastCustomizadoCurto("Salvar gif", getApplicationContext());
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            exibirProgressDialog("upload");
            for (Uri uriConfigurada : urisSelecionadas) {

                DatabaseReference dailyShortsRef = firebaseRef.child("dailyShorts");
                String idDailyShort = dailyShortsRef.push().getKey();

                uparGifStorage(uriConfigurada, new UploadCallback() {
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
                        dadosDailyAtual.put("tipoMidia", "gif");
                        urlMidiaUpada.add(urlDaily);
                        recuperarTimestampNegativo(this);
                    }

                    @Override
                    public void timeStampRecuperado(long timeStampNegativo, String dataFormatada) {
                        dadosDailyAtual.put("timestampCriacaoDaily", timeStampNegativo);
                        //Passado por parâmetro para garantir os dados atuais ao callback
                        //correto.
                        salvarNoFirebase(idDailyAtual, dadosDailyAtual, timeStampNegativo, dataFormatada);
                    }

                    @Override
                    public void onUploadError(String mensagemError) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao salvar o dailyshort: " + mensagemError, getApplicationContext());
                    }
                });
            }
        } else {
            ocultarProgressDialog();
            SnackbarUtils.showSnackbar(btnSalvarDailyShorts, "Selecione pelo menos uma mídia para que seja possível salvar o dailyShort");
        }
    }

    private void uparVideoStorage(Uri uriAtual, UploadCallback uploadCallback) {
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            exibirProgressDialog("upload");
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
                    uploadCallback.onUploadError(e.getMessage());
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
        }
    }

    private void uparGifStorage(Uri uriAtual, UploadCallback uploadCallback) {
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            String nomeRandomico = UUID.randomUUID().toString();
            //Criado storage aqui pois se o storage for reutilizado ele
            //ainda conterá configuração do storage e duplicara os dados anteriores.
            StorageReference gifRef = storageRef.child("dailyShorts")
                    .child("gifs")
                    .child(idUsuario)
                    .child("gif" + nomeRandomico + ".gif");
            //Verificando progresso do upload
            UploadTask uploadTask = gifRef.putFile(uriAtual);

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

                    gifRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        }
    }

    private void salvarNoFirebase(String idDailyShort, HashMap<String, Object> dadosDaily, long timestampDaily, String dataFormatada) {
        DatabaseReference salvarDailyRef = firebaseRef.child("dailyShorts")
                .child(idUsuario).child(idDailyShort);

        salvarDailyRef.setValue(dadosDaily).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                ToastCustomizado.toastCustomizadoCurto("DailyShort salvo com sucesso", getApplicationContext());

                if (urlMidiaUpada != null && urlMidiaUpada.size() > 0) {

                    int indexUltimoDaily = urlMidiaUpada.size() - 1;
                    String urlUltimoDaily = urlMidiaUpada.get(indexUltimoDaily);

                    DatabaseReference salvarLastDailyRef = firebaseRef.child("usuarios")
                            .child(idUsuario).child("urlLastDaily");
                    salvarLastDailyRef
                            .setValue(urlUltimoDaily);

                    DatabaseReference salvarTipoMidiaLastDailyRef = firebaseRef.child("usuarios")
                            .child(idUsuario).child("tipoMidia");

                    salvarTipoMidiaLastDailyRef.setValue(tipoMidiaPermissao);

                    DatabaseReference salvarTimeLastDailyRef = firebaseRef.child("usuarios")
                            .child(idUsuario).child("dataLastDaily");

                    salvarTimeLastDailyRef.setValue(dataFormatada);
                }

                if (!dailyShortAtivo) {
                    //Somente deixa o daily como ativo se não estiver ativo.
                    salvarStatusDaily();
                }

                resetarConfigSelecao();
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
                        uploadCallback.timeStampRecuperado(timestampNegativo, dataFormatada);
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

        if (exoPlayer != null) {
            pararExoPlayer();
        }

        switch (tipoMidia) {
            case "imagem":
                selecionarFoto();
                break;
            case "video":
                selecionarVideo();
                break;
            case "gif":
                selecionarGif();
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

                int minCrf1080p = 18;
                int maxCrf1080p = 23;
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

                // Verificar a resolução do vídeo e ajustar o valor inicial do CRF

                if (crfAumentado != -1 && crfAumentado > 0.0f) {
                    crf = crfAumentado;
                } else {
                    if (width >= 1080 && height >= 1920) {

                        if (bitsRate >= 4000) {
                            diferencaCRF = 2.5f; // Aumentar a diferença fixa para diminuir mais a qualidade
                        } else {
                            diferencaCRF = 2.0f; // Ajuste para equilibrar a qualidade
                        }

                        crf = retornarCrf(minCrf1080p, maxCrf1080p, bitsRate, 4000, 1600, diferencaCRF);
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
                                    adicionarUri(compressedVideoUri);
                                    ocultarProgressDialog();
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

        //
    }

    private void resetarConfigSelecao() {

        limparLista();
        tipoMidiaPermissao = null;
        selecaoExistente = false;

        ocultarProgressDialog();

        ToastCustomizado.toastCustomizadoCurto("Resetado", getApplicationContext());
    }

    private void exibirProgressDialog(String tipoMensagem) {

        switch (tipoMensagem) {
            case "upload":
                progressDialog.setMessage("Salvando dailyShort, aguarde um momento...");
                break;
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

    private void limparLista() {
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            ToastCustomizado.toastCustomizado("CLEAR", getApplicationContext());
            urisSelecionadas.clear();
        }

        if (urlMidiaUpada != null && urlMidiaUpada.size() > 0) {
            urlMidiaUpada.clear();
        }
    }

    private void procuraDuplicata(Uri uri) {
        if (urisSelecionadas != null && urisSelecionadas.size() > 0) {
            //Evita duplicatas.
            if (urisSelecionadas.contains(uri)) {
                return;
            }
        }
    }

    private void adicionarUri(Uri uri) {
        ToastCustomizado.toastCustomizadoCurto("Adicionado", getApplicationContext());

        imgViewDailyShorts.setVisibility(View.GONE);
        cardRedondoDaily.setVisibility(View.GONE);
        recyclerViewDailyShorts.setVisibility(View.GONE);
        framePreviewDaily.setVisibility(View.GONE);

        urisSelecionadas.add(uri);

        if (tipoMidiaPermissao != null) {
            switch (tipoMidiaPermissao) {
                case "imagem":
                case "gif":
                    cardRedondoDaily.setVisibility(View.VISIBLE);
                    recyclerViewDailyShorts.setVisibility(View.VISIBLE);
                    adapterDailyShorts.notifyDataSetChanged();
                    break;
                case "video":
                    exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
                    iniciarExoPlayer(uri);
                    framePreviewDaily.setVisibility(View.VISIBLE);
                    break;
                case "texto":
                    break;
            }
        }
    }

    private void pauseExoPlayer() {
        if (exoPlayer != null) {
            if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                // Aguardar até que o player esteja pronto para reprodução
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            // O ExoPlayer está pronto para reprodução, então pausar
                            exoPlayer.pause();
                            exoPlayer.setPlayWhenReady(false);
                            exoPlayer.removeListener(this);
                        }
                    }
                });
            } else {
                // O ExoPlayer não está em buffering, então pausar imediatamente
                exoPlayer.pause();
                exoPlayer.setPlayWhenReady(false);
            }
        }
    }

    private void resumeExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.play();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void releaseExoPlayer() {
        if (exoPlayer != null) {
            if (listenerExo != null) {
                exoPlayer.removeListener(listenerExo);
            }
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void adicionarListenerExoPlayer() {
        listenerExo = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == Player.STATE_READY) {
                    // O vídeo está pronto para reprodução, você pode iniciar a reprodução automática aqui
                    //*ToastCustomizado.toastCustomizadoCurto("READY", context);
                    exoPlayer.setPlayWhenReady(true);
                    spinProgressBarExo.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_BUFFERING) {
                    //*ToastCustomizado.toastCustomizadoCurto("BUFFERING", context);
                    // O vídeo está em buffer, você pode mostrar um indicador de carregamento aqui
                    spinProgressBarExo.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    //* ToastCustomizado.toastCustomizadoCurto("ENDED", context);
                    // O vídeo chegou ao fim, você pode executar ações após a conclusão do vídeo aqui
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
            }
        };

        exoPlayer.addListener(listenerExo);
    }

    private void removerListenerExoPlayer() {
        if (listenerExo != null) {
            //*ToastCustomizado.toastCustomizadoCurto("Removido listener", context);
            exoPlayer.removeListener(listenerExo);
        }
    }

    public void iniciarExoPlayer(@NonNull Uri uriVideo) {

        //Verificação garante que o vídeo não seja montado novamente
        //se ele já estiver em reprodução.
        if (exoPlayer != null
                && styledPlayer.getPlayer() != null &&
                exoPlayer.getMediaItemCount() != -1
                && exoPlayer.getMediaItemCount() > 0) {
            return;
        }

        removerListenerExoPlayer();

        // Configura o ExoPlayer com a nova fonte de mídia para o vídeo
        exoPlayer.setMediaItem(MediaItem.fromUri(uriVideo.toString()));

        // Vincula o ExoPlayer ao StyledPlayerView
        styledPlayer.setPlayer(exoPlayer);

        // Faz com que o vídeo se repita quando ele acabar
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

        // Trata do carregamento e da inicialização do vídeo
        adicionarListenerExoPlayer();

        // Indica para o exoPlayer que ele está com a view e a mídia configurada.
        exoPlayer.prepare();

        //Controla a exibição dos botões do styled.
        styledPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isControllerVisible) {
                    styledPlayer.hideController();
                    styledPlayer.setUseController(false);
                    isControllerVisible = false;
                } else {
                    styledPlayer.setUseController(true);
                    styledPlayer.showController();
                    isControllerVisible = true;
                }
            }
        });
    }

    private void pararExoPlayer() {

        //*Detached
        //Remove o listener do exoPlayer
        removerListenerExoPlayer();
        //Para a reprodução.
        exoPlayer.stop();
        //Limpa a mídia do exoPlayer.
        exoPlayer.clearMediaItems();
        //Volta para o início do vídeo.
        exoPlayer.seekToDefaultPosition();
        //Diz para o exoPlayer que ele não está pronto.
        exoPlayer.setPlayWhenReady(false);
        //Desvincula o exoPlayer anterior.
        styledPlayer.setPlayer(null);

        //Oculta os controladores do styled.
        styledPlayer.hideController();
        styledPlayer.setUseController(false);
        isControllerVisible = false;
    }

    private void salvarStatusDaily() {
        DatabaseReference statusDailyUserRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("dailyShortAtivo");

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                statusDailyUserRef.setValue(true);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }
}