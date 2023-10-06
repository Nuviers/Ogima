package com.example.ogima.helper;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.ogima.R;
import com.example.ogima.activity.PostagemActivity;
import com.example.ogima.model.Postagem;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.datatransport.runtime.backends.CreationContextFactory_Factory;
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


public class RecuperarUriUtils {
    private Activity activity;
    private Context context;
    private PictureSelectorStyle selectorStyle;
    private Uri uriSelecionada = null;
    private float crfBeforeCompression = 0.0f;
    //Superfast pois se o vídeo ficou maior do que o
    //tamanho original a recompressão será tão rápida que não irá comprometer
    //a experiência do usuário.
    private static final String VELOCIDADE = "superfast";
    private final GiphyUtils giphyUtils;
    private GiphyDialogFragment gdl;

    public interface UriRecuperadaCallback {
        void onRecuperado(Uri uriRecuperada);

        void onCancelado();

        void onError(String message);
    }

    public interface GifRecuperadaCallback {
        void onRecuperado(String urlGif);
        void onCancelado();
        void onError(String message);
    }

    public RecuperarUriUtils(Activity activity, Context context) {
        selectorStyle = new PictureSelectorStyle();
        giphyUtils = new GiphyUtils();
        this.activity = activity;
        this.context = context;
        configStylePictureSelector();
    }

    public void selecionadoGaleria(boolean layoutCircular) {
        PictureSelector.create(activity)
                .openGallery(SelectMimeType.ofImage())
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1)
                .setSelectorUIStyle(selectorStyle)
                .setSelectMaxFileSize((long) SizeUtils.MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        limparUri();
                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {
                                String path = media.getPath();
                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result), layoutCircular);
                                }
                            }
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao selecionar mídia", context);
                        }
                    }

                    @Override
                    public void onCancel() {
                        limparUri();
                    }
                });
    }

    public void selecionadoCamera(boolean layoutCircular) {
        PictureSelector.create(activity)
                .openCamera(SelectMimeType.ofImage())
                .setSelectMaxFileSize(SizeUtils.MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        limparUri();
                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {
                                String path = media.getPath();
                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result), layoutCircular);
                                }
                            }
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao selecionar mídia", context);
                        }
                    }

                    @Override
                    public void onCancel() {
                        limparUri();
                    }
                });
    }

    public void selecionadoGif(FragmentManager fragmentManager, String tag, String tamanho, GifRecuperadaCallback callback) {
        giphyUtils.selectGifPostagem(context, new GiphyUtils.GifSelectionListener() {
            @Override
            public void onGifSelected(String gifPequena, String gifMedia, String gifOriginal) {
                switch (tamanho) {
                    case SizeUtils.SMALL_GIF:
                        if (gifPequena != null && !gifPequena.isEmpty()) {
                            callback.onRecuperado(gifPequena);
                        }
                        break;
                    case SizeUtils.MEDIUM_GIF:
                        if (gifMedia != null && !gifMedia.isEmpty()) {
                            callback.onRecuperado(gifMedia);
                        }
                        break;
                }
            }
        }, new GiphyUtils.GiphyDialogListener() {
            @Override
            public void onGiphyDialogDismissed() {
                callback.onCancelado();
            }
        });
        gdl = giphyUtils.retornarGiphyDialog();
        gdl.show(fragmentManager, tag);
    }

    public void selecionadoVideo(ProgressDialog progressDialog, UriRecuperadaCallback callback) {
        PictureSelector.create(activity)
                .openGallery(SelectMimeType.ofVideo())
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1)
                .setSelectorUIStyle(selectorStyle)
                .setSelectMaxFileSize(SizeUtils.MAX_FILE_SIZE_VIDEO * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        limparUri();
                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {
                                Uri uriVideo = Uri.parse(media.getPath());
                                // Recupera o caminho completo do vídeo.
                                String caminho = getPathFromUri(uriVideo);
                                // Cria um destino para armazenar o vídeo em cache.
                                String destino = destinoVideo();
                                if (caminho != null && !caminho.isEmpty()) {
                                    //Sem o file:// o caminho não seria acessível.
                                    String caminhoCompleto = "file://" + caminho;
                                    otimizarVideo(progressDialog, caminhoCompleto, destino, uriVideo, 0.0f, callback);
                                }
                            }
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao selecionar mídia", context);
                        }
                    }

                    @Override
                    public void onCancel() {
                        limparUri();
                    }
                });
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri, boolean layoutCircular) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions(layoutCircular))
                .start(activity);
    }

    private UCrop.Options getOptions(boolean layoutCircular) {
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar imagem");
        if (layoutCircular) {
            options.setCircleDimmedLayer(true);
        } else {
            options.setCircleDimmedLayer(false);
        }
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }

    private Uri destinoImagemUri(ArrayList<LocalMedia> result) {
        Uri destinationUri = null;
        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
                File outputFile = new File(activity.getCacheDir(), fileName);
                destinationUri = Uri.fromFile(outputFile);
                break; // Sai do loop após encontrar a primeira imagem
            }
        }
        return destinationUri;
    }

    private String destinoVideo() {
        String fileName = DateUtils.getCreateFileName("videoCompress_") + ".mp4";
        File outputFile = new File(context.getCacheDir(), fileName);
        return outputFile.getPath();
    }

    private String getPathFromUri(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }

    public void handleActivityResultPhoto(int requestCode, int resultCode, Intent data, UriRecuperadaCallback callback) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                Uri imagemRecortada = UCrop.getOutput(data);
                if (imagemRecortada != null) {
                    uriSelecionada = imagemRecortada;
                    callback.onRecuperado(uriSelecionada);
                    limparUri();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            callback.onCancelado();
        }
    }

    private void configStylePictureSelector() {
        TitleBarStyle blueTitleBarStyle = new TitleBarStyle();
        blueTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_blue));
        BottomNavBarStyle numberBlueBottomNavBarStyle = new BottomNavBarStyle();
        numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_blue));
        numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.ps_demo_blue_num_selected);
        numberBlueBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));
        numberBlueBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));
        SelectMainStyle numberBlueSelectMainStyle = new SelectMainStyle();
        numberBlueSelectMainStyle.setStatusBarColor(ContextCompat.getColor(context, R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectNumberStyle(true);
        numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true);
        numberBlueSelectMainStyle.setSelectBackground(R.drawable.ps_demo_blue_num_selector);
        numberBlueSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberBlueSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_preview_blue_num_selector);
        numberBlueSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        numberBlueSelectMainStyle.setSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectText(R.string.ps_completed);
        selectorStyle.setTitleBarStyle(blueTitleBarStyle);
        selectorStyle.setBottomBarStyle(numberBlueBottomNavBarStyle);
    }

    public void limparUri() {
        if (uriSelecionada != null) {
            uriSelecionada = null;
        }
    }

    private void otimizarVideo(ProgressDialog progressDialog, String inputPath, String outputPath, Uri uriVideo, float crfAumentado, UriRecuperadaCallback callback) {

        VideoUtils.getVideoInfo(context, uriVideo, new VideoUtils.VideoInfoCallback() {
            @Override
            public void onVideoInfoReceived(long durationMs, float frameRate, long fileSizeBytes, int width, int height, int bitsRate) {
                if (crfAumentado <= -1 || crfAumentado <= 0.0f) {
                    exibirProgressDialog(progressDialog, "otimizando");
                }
                // Cálculo do CRF com base na taxa de bits média e na resolução do vídeo
                //Quanto maior a porcentagem que nesse caso é 35 maior será a perca de qualidade
                //e menor será o tamanho do arquivo.

                float crf;
                String fpsVideo;

                // Verifica a resolução do vídeo e ajusta o valor inicial do CRF.
                if (crfAumentado != -1 && crfAumentado > 0.0f) {
                    crf = crfAumentado;
                } else {
                    float diferencaCRF = calcularDiferencaCRF(width, height, bitsRate);
                    crf = crfAjustado(width, height, bitsRate, diferencaCRF);
                }

                // Valor do CRF arredondado
                int crfFormatado = Math.round(crf);
                //ToastCustomizado.toastCustomizadoCurto("CRF FINAL " + crfFormatado, context);

                if (frameRate >= 30) {
                    int fpsArrendondado = (int) frameRate;
                    fpsVideo = String.valueOf(fpsArrendondado);
                } else {
                    fpsVideo = "30";
                }

                crfBeforeCompression = crf;

                //ToastCustomizado.toastCustomizadoCurto("FPS " + fpsVideo, context);

                String[] commands = new String[]{
                        "-y",        // Sobrescrever o arquivo de saída, se já existir
                        "-i", inputPath,     // Caminho do arquivo de entrada
                        "-r", fpsVideo,          // Taxa de quadros
                        "-vcodec", "libx264",  // Codec de vídeo
                        "-crf", String.valueOf(crfFormatado),       // Fator de qualidade constante (Quanto menor, melhor qualidade, mas maior tamanho)
                        "-s", width + "x" + height,   // Resolução desejada do vídeo comprimido
                        "-preset", VELOCIDADE,  // Preset de codificação de vídeo
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
                                    //ToastCustomizado.toastCustomizado("MB: " + fileSizeInMB, context);
                                    otimizarVideo(progressDialog, inputPath, outputPath, uriVideo, crfBeforeCompression, callback);
                                } else {
                                    crfBeforeCompression = 0.0f;
                                    //ToastCustomizado.toastCustomizado("MB: " + fileSizeInMB, context);
                                    callback.onRecuperado(compressedVideoUri);
                                }
                            }

                            @Override
                            public void onProgress(int progress, long progressTime) {
                                // Progresso da compressão
                            }

                            @Override
                            public void onCancel() {
                                // Compressão cancelada
                                callback.onCancelado();
                            }

                            @Override
                            public void onError(String message) {
                                // Erro durante a compressão
                                callback.onError(message);
                            }
                        });
            }
        });
    }

    private float calcularDiferencaCRF(int width, int height, int bitsRate) {
        float diferencaCRF;
        if (width >= 1080 && height >= 1920) {
            if (bitsRate >= 3800) {
                diferencaCRF = 4.0f; // Quanto maior a diferença menor será a qualidade do vídeo.
            } else {
                diferencaCRF = 2.0f; // Ajuste que equilibra a qualidade
            }
        } else if (width >= 720 && height >= 1280) {
            if (bitsRate >= 2800) {
                diferencaCRF = 3.0f; // Aumentar a diferença fixa para diminuir mais a qualidade
            } else {
                diferencaCRF = 1.5f; // Ajuste para equilibrar a qualidade
            }
        } else {
            if (bitsRate >= 2000) {
                diferencaCRF = 1.5f; // Aumentar a diferença fixa para diminuir mais a qualidade
            } else {
                diferencaCRF = 1.5f; // Ajuste para equilibrar a qualidade
            }
        }
        return diferencaCRF;
    }

    private float crfAjustado(int width, int height, int bitsRate, float diferencaCRF) {
        float crf;
        if (width >= 1080 && height >= 1920) {
            crf = retornarCrf(VideoUtils.MIN_CRF_1080P, VideoUtils.MAX_CRF_1080P, bitsRate, 3800, 1600, diferencaCRF);
            if (aumentarCrf(bitsRate, 3800)) {
                crf++;
            }
        } else if (width >= 720 && height >= 1280) {
            crf = retornarCrf(VideoUtils.MIN_CRF_720P, VideoUtils.MAX_CRF_720P, bitsRate, 2800, 1500, diferencaCRF);
        } else {
            crf = retornarCrf(VideoUtils.MIN_CRF_480P, VideoUtils.MAX_CRF_480P, bitsRate, 2000, 850, diferencaCRF);
        }
        return crf;
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
        ajustarRetorno += diferencaCRF;
        // Verifica se o valor ajustado está dentro do intervalo [minCrf, maxCrf]
        ajustarRetorno = Math.max(minCrf, Math.min(ajustarRetorno, maxCrf));
        Log.d("VideoUtils", "Retorno: " + ajustarRetorno);
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

    private void exibirProgressDialog(ProgressDialog progressDialog, String tipoMensagem) {
        switch (tipoMensagem) {
            case "otimizando":
                progressDialog.setMessage("Otimizando vídeo, aguarde um momento...");
                break;
        }
        if (!activity.isFinishing()) {
            progressDialog.show();
        }
    }

    public void ocultarProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && !activity.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
