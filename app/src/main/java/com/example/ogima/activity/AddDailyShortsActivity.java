package com.example.ogima.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.picture.lib.utils.FileDirMap;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddDailyShortsActivity extends AppCompatActivity {

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
    private StorageReference imagemRef, videoRef;
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

    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_shorts);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        clickListeners();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            handleSelectedImages(result.getData());
                        }
                    }
                });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissoesNecessarias) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 17);
            } else {
                selecionarFoto();
            }
        } else {
            selecionarFoto();
        }
    }

    private void clickListeners() {
        imgBtnAddGaleriaDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissions();
                /*
                solicitaPermissoes("galeria");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    selecionarFoto();
                }
                 */
            }
        });
    }

    private void selecionarFoto() {

        // Configurar o estilo da interface do PictureSelector (opcional)
        PictureSelector.create(this)
                .openGallery(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setMaxSelectNum(10) // Permitir seleção múltipla de fotos
                .setSelectMaxFileSize(MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine()) // Substitua GlideEngine pelo seu próprio mecanismo de carregamento de imagem, se necessário
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {

                        ToastCustomizado.toastCustomizado("RESULT",getApplicationContext());

                        for (LocalMedia media : result) {
                            // Faça o que for necessário com cada foto selecionada
                            String path = media.getPath(); // Obter o caminho do arquivo da foto
                            openCropActivity(Uri.parse(path), criarDestinoUri(result));
                            //*GlideCustomizado.montarGlide(getApplicationContext(), String.valueOf(path), imgViewPreviewDailyShorts, android.R.color.transparent);
                            // ...
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });

        /*
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        launcher.launch(intent);
         */

        /*
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(true)
                .showSingleMediaType(true)
                .theme(R.style.Matisse_Dracula)
                .maxSelectable(10)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .addFilter(new Filter() {
                    @Override
                    protected Set<MimeType> constraintTypes() {
                        return null;
                    }

                    @Override
                    public IncapableCause filter(Context context, Item item) {
                        long maxSizeInBytes = 1 * 1024 * 1024; // Limite de tamanho em bytes (10 MB)

                        if (item.size > maxSizeInBytes) {
                            return new IncapableCause(IncapableCause.TOAST, "A imagem selecionada excede o tamanho máximo permitido.");
                        }

                        return null; // Retorna null se a imagem atender aos requisitos
                    }
                })
                .imageEngine(new GlideEngine())
                .forResult(SELECAO_GALERIA);
         */
    }

    private void handleSelectedImages(Uri uri) {
        // Handle the selected image URI here
        GlideCustomizado.montarGlide(getApplicationContext(),
                String.valueOf(uri), imgViewPreviewDailyShorts, android.R.color.transparent);
    }

    private void handleSelectedImages(Intent intent) {
        /*
        if (intent != null) {
            if (intent.getClipData() != null) {
                int count = intent.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = intent.getClipData().getItemAt(i).getUri();
                    handleSelectedImages(imageUri);
                }
            } else if (intent.getData() != null) {
                Uri imageUri = intent.getData();
                handleSelectedImages(imageUri);
            }
        }
         */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 17) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                selecionarFoto();
            } else {
                // Permissions were not granted, handle it accordingly
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECAO_GALERIA && resultCode == RESULT_OK) {
            List<LocalMedia> selectedPhotos = PictureSelector.obtainSelectorList(data);
            // Faça o que for necessário com as fotos selecionadas
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == UCrop.REQUEST_CROP){

            ToastCustomizado.toastCustomizadoCurto("CROP",getApplicationContext());

            Uri imagemCortada = UCrop.getOutput(data);

            GlideCustomizado.montarGlide(getApplicationContext(),
                    String.valueOf(imagemCortada), imgViewPreviewDailyShorts, android.R.color.transparent);
        }

    }

    private void solicitaPermissoes(String permissao) {
        //Verifica quais permissões falta a ser solicitadas, caso alguma seja negada, exibe um toast.
        if (!solicitaPermissoes.verificaPermissoes(permissoesNecessarias, AddDailyShortsActivity.this, permissao)) {
            if (permissao != null) {
                solicitaPermissoes.tratarResultadoPermissoes(permissao, AddDailyShortsActivity.this);
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
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar imagem");
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }

    private Uri criarDestinoUri(ArrayList<LocalMedia> result) {
        Uri srcUri = null;
        Uri destinationUri = null;

        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);

            if (PictureMimeType.isHasImage(media.getMimeType())) {
                String currentCropPath = media.getAvailablePath();
                if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(currentCropPath)) {
                    srcUri = Uri.parse(currentCropPath);
                } else {
                    srcUri = Uri.fromFile(new File(currentCropPath));
                }

                String fileName = DateUtils.getCreateFileName("CROPRTESTE_") + ".jpg";
                File externalFilesDir = new File(FileDirMap.getFileDirPath(getApplicationContext(), SelectMimeType.TYPE_IMAGE));
                //*File outputFile = new File(externalFilesDir.getAbsolutePath(), fileName);
                File outputFile = new File(getCacheDir(), fileName);
                destinationUri = Uri.fromFile(outputFile);
                //ToastCustomizado.toastCustomizado("Caminho: " + destinationUri, getApplicationContext());
                Log.d("Caminho ", String.valueOf(destinationUri));
                break; // Sai do loop após encontrar a primeira imagem
            }
        }

        return destinationUri;
    }
}