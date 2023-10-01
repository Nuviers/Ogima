package com.example.ogima.helper;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.ogima.R;
import com.example.ogima.activity.PostagemActivity;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
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

import java.io.File;
import java.util.ArrayList;

public class MidiaUtils {

    private Activity activity;
    private Context context;
    private StorageReference storageRef;
    private int SIZE_IMAGEM, SIZE_VIDEO, SIZE_GIF;
    private PictureSelectorStyle selectorStyle;
    private Uri uriSelecionada = null;
    public SpinKitView progress;
    private ProgressDialog progressDialog;
    private final GiphyUtils giphyUtils;
    private GiphyDialogFragment gdl;
    private FragmentManager fragmentManager;
    public boolean layoutCircular = false;

    public interface UriRecuperadaCallback {
        void onRecuperado(Uri uriRecuperada);

        void onCancelado();

        void onError(String message);
    }

    public interface SalvarGifCallback {
        void onSalvo();

        void onError(String message);
    }

    public interface SalvarNoFirebaseCallback {
        void onSalvo();

        void onError(String message);
    }

    public interface UparNoStorageCallback {
        void onConcluido(String urlUpada);

        void onError(String message);
    }

    public interface RemoverDoStorageCallback {
        void onRemovido();

        void onError(String message);
    }

    public interface ExcluirFotoCallback {
        void onExcluido();

        void onError(String message);
    }

    public MidiaUtils(FragmentManager fragmentManager, ProgressDialog progressDialog, Activity activity, Context context, int SIZE_IMAGEM, int SIZE_VIDEO, int SIZE_GIF) {
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        selectorStyle = new PictureSelectorStyle();
        giphyUtils = new GiphyUtils();
        this.context = context;
        this.activity = activity;
        this.SIZE_IMAGEM = SIZE_IMAGEM;
        this.SIZE_VIDEO = SIZE_VIDEO;
        this.SIZE_GIF = SIZE_GIF;
        this.progressDialog = progressDialog;
        this.fragmentManager = fragmentManager;
        configStylePictureSelector();
    }

    public boolean isLayoutCircular() {
        return layoutCircular;
    }

    public void setLayoutCircular(boolean layoutCircular) {
        this.layoutCircular = layoutCircular;
    }

    public SpinKitView getProgress() {
        return progress;
    }

    public void setProgress(SpinKitView progress) {
        this.progress = progress;
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

    public void selecionarGaleria() {
        PictureSelector.create(activity)
                .openGallery(SelectMimeType.ofImage())
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1)
                .setSelectorUIStyle(selectorStyle)
                .setSelectMaxFileSize((long) SIZE_IMAGEM * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        limparUri();
                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {
                                String path = media.getPath();
                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {
                        limparUri();
                    }
                });
    }

    public void selecionarCamera() {
        PictureSelector.create(activity)
                .openCamera(SelectMimeType.ofImage())
                .setSelectMaxFileSize((long) SIZE_IMAGEM * 1024 * 1024)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        limparUri();
                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {
                                String path = media.getPath();
                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {
                        limparUri();
                    }
                });
    }

    public void selecionarGif(String tag, String tamanho, UriRecuperadaCallback callback) {

        giphyUtils.selectGifPostagem(context, new GiphyUtils.GifSelectionListener() {
            @Override
            public void onGifSelected(String gifPequena, String gifMedio, String gifOriginal) {
                switch (tamanho) {
                    case "pequena":
                        if (gifPequena != null && !gifPequena.isEmpty()) {
                            callback.onRecuperado(Uri.parse(gifPequena));
                        }
                        break;
                    case "media":
                        if (gifMedio != null && !gifMedio.isEmpty()) {
                            callback.onRecuperado(Uri.parse(gifMedio));
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

    public void limparUri() {
        if (uriSelecionada != null) {
            uriSelecionada = null;
        }
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(activity);
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
        if (isLayoutCircular()) {
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

    public void handleActivityResult(int requestCode, int resultCode, Intent data, UriRecuperadaCallback callback) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                Uri imagemRecortada = UCrop.getOutput(data);
                if (imagemRecortada != null) {
                    uriSelecionada = imagemRecortada;
                    callback.onRecuperado(uriSelecionada);
                    tratarMidia("foto");
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            callback.onCancelado();
        }
    }

    private void tratarMidia(String tipoMidia) {
        if (tipoMidia != null
                && !tipoMidia.isEmpty()) {

        }
    }

    public void salvarGif(DatabaseReference reference, StorageReference storageARemover, String url, SalvarGifCallback callback) {
        reference.setValue(url).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                removerDoStorage(storageARemover, new RemoverDoStorageCallback() {
                    @Override
                    public void onRemovido() {
                        callback.onSalvo();
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
            }
        });
    }

    public void salvarFotoNoStorage(DatabaseReference reference, String urlConfigurada, SalvarNoFirebaseCallback callback) {
        reference.setValue(urlConfigurada).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                callback.onSalvo();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void uparFotoNoStorage(StorageReference storageAlvo, Uri uriAlvo, UparNoStorageCallback callback) {
        UploadTask uploadTask = storageAlvo.putFile(uriAlvo);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageAlvo.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        callback.onConcluido(String.valueOf(uri));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
            }
        });
    }

    public void removerDoStorage(StorageReference storageARemover, RemoverDoStorageCallback callback) {
        try {
            storageARemover.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete()) {
                        callback.onRemovido();
                    }
                    if (!task.isSuccessful()) {
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void excluirFoto(DatabaseReference reference, StorageReference storageReference, ExcluirFotoCallback callback) {
        removerDoStorage(storageReference, new RemoverDoStorageCallback() {
            @Override
            public void onRemovido() {
                reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onExcluido();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void exibirProgressDialog(String campo, String tipoMensagem) {
        switch (tipoMensagem) {
            case "salvamento":
                progressDialog.setMessage(String.format("%s %s %s",
                        "Salvando", campo, "aguarde um momento...."));
                break;
        }
        if (!activity.isFinishing()) {
            progressDialog.show();
        }
    }

    public void ocultarProgressDialog() {
        if (progressDialog != null && !activity.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
