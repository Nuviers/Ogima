package com.example.ogima.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Placeholder;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.ogima.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;

public class GlideCustomizado {

    private static RequestManager sharedGlide;

    public static synchronized RequestManager getSharedGlideInstance(Context context) {
        if (sharedGlide == null) {
            sharedGlide = GlideApp.with(context.getApplicationContext());

            /*
            Serva para limpar o cache do glide.
            try{
                Glide.get(context).clearDiskCache();
                Glide.get(context).clearMemory();

                GlideApp.get(context).clearDiskCache();
                GlideApp.get(context).clearMemory();
            }catch (Exception ex){
                ex.printStackTrace();
            }
             */

            //Cache do glide é armazenado internamente.
            //*File glideCacheDir = GlideApp.getPhotoCacheDir(context);
            //*long cacheSize = getFolderSize(glideCacheDir) / 1048576;
            //*Log.d("CACHEGLIDE", "Tamanho do cache: " + cacheSize);
        }
        return sharedGlide;
    }

    public static void montarGlideEpilepsia(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop();

            getSharedGlideInstance(contexto)
                    .asBitmap()
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlide(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void fundoGlideEpilepsia(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .asBitmap()
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void fundoGlide(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideFoto(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideBitmap(Context contexto, Bitmap arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideBitMapCenterInside(Context contexto, Bitmap arquivo, RoundedImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerInside();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideFotoEpilepsia(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .asBitmap()
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideCenterInside(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerInside();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideCenterInsideEpilepsia(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerInside();

            getSharedGlideInstance(contexto)
                    .asBitmap()
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideCircularBitmap(Context contexto, Bitmap arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideGifLocalPorDrawable(Context contexto, int arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .asDrawable()
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void montarGlideGifLocalPorDrawableEpilepsia(Context contexto, int arquivo, ImageView componente, int placeholder) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerInside();

            getSharedGlideInstance(contexto)
                    .asBitmap()
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void loadDrawableImage(Context context, int drawable, ImageView imageView, int placeholder) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .encodeQuality(100)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop();

        getSharedGlideInstance(context)
                .load(drawable)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void loadDrawableImageEpilepsia(Context context, int drawable, ImageView imageView, int placeholder) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .encodeQuality(100)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop();

        getSharedGlideInstance(context)
                .asBitmap()
                .load(drawable)
                .apply(options)
                .into(imageView);
    }

    public static void loadDrawableCircular(Context context, int drawable, ImageView imageView, int placeholder) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .encodeQuality(100)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop();

        getSharedGlideInstance(context)
                .asDrawable()
                .load(drawable)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void loadDrawableCircularEpilepsia(Context context, int drawable, ImageView imageView, int placeholder) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .encodeQuality(100)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop();

        getSharedGlideInstance(context)
                .asBitmap()
                .load(drawable)
                .apply(options)
                .into(imageView);
    }

    public static void engineCenterInside(Context contexto, String arquivo, ImageView componente, int placeholder, int width, int height) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .override(width, height)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerInside();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void engineAlbumCover(Context contexto, String arquivo, ImageView componente, int placeholder, int width, int height) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .override(width, height)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .sizeMultiplier(0.5f)
                    .transform(new CenterCrop(), new RoundedCorners(8));

            getSharedGlideInstance(contexto)
                    .asBitmap()
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void engineGridImage(Context contexto, String arquivo, ImageView componente, int placeholder, int width, int height) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .override(width, height)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void eginePauseRequest(Context contexto) {
        getSharedGlideInstance(contexto).pauseRequests();
    }

    public static void egineResumeRequest(Context contexto) {
        getSharedGlideInstance(contexto).resumeRequests();
    }

    public static void engineLoadThumbnail(Context contexto, Uri arquivo, ImageView componente, Drawable placeholder, int width, int height) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .override(width, height)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop();

            getSharedGlideInstance(contexto)
                    .asBitmap()
                    .load(arquivo)
                    .apply(options)
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void engineLoadImage(Context contexto, Uri arquivo, ImageView componente, int placeholder, int width, int height) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .override(width, height)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .priority(Priority.HIGH)
                    .fitCenter();

            getSharedGlideInstance(contexto)
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void engineLoadGif(Context contexto, Uri arquivo, ImageView componente, int placeholder, int width, int height) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholder)
                    .encodeQuality(100)
                    .override(width, height)
                    .error(android.R.color.transparent)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .priority(Priority.HIGH)
                    .fitCenter();

            getSharedGlideInstance(contexto)
                    .asGif()
                    .load(arquivo)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(componente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void loadGif(Context contexto, String arquivo, ImageView componente, int placeholder) {
        try {

            //Verifica a extensão do arquivo na URL
            String extension = MimeTypeMap.getFileExtensionFromUrl(arquivo);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            if (mimeType != null) {
                if (mimeType.startsWith("image")) {
                    if (mimeType.equals("image/gif")) {
                        ToastCustomizado.toastCustomizadoCurto("gif", contexto);

                        RequestOptions options = new RequestOptions()
                                .placeholder(placeholder)
                                .encodeQuality(100)
                                .error(android.R.color.transparent)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .priority(Priority.HIGH)
                                .circleCrop();

                        getSharedGlideInstance(contexto)
                                .asGif()
                                .load(arquivo)
                                .apply(options)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(componente);

                    } else {
                        // É uma imagem
                        RequestOptions options = new RequestOptions()
                                .placeholder(placeholder)
                                .encodeQuality(100)
                                .error(android.R.color.transparent)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .priority(Priority.HIGH)
                                .circleCrop();

                        getSharedGlideInstance(contexto)
                                .load(arquivo)
                                .apply(options)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(componente);
                        ToastCustomizado.toastCustomizadoCurto("Imagem", contexto);
                    }
                } else if (mimeType.startsWith("video")) {
                    // É um vídeo
                    // Faça o tratamento adequado para carregar e exibir o vídeo
                    RequestOptions options = new RequestOptions()
                            .placeholder(placeholder)
                            .encodeQuality(100)
                            .error(android.R.color.transparent)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .priority(Priority.HIGH)
                            .frame(1000000) // Especifica o momento do vídeo para capturar a thumbnail (em microssegundos)
                            .centerCrop();

                    getSharedGlideInstance(contexto)
                            .asBitmap()
                            .load(arquivo)
                            .apply(options)
                            .transition(BitmapTransitionOptions.withCrossFade())
                            .into(componente);
                    ToastCustomizado.toastCustomizadoCurto("Video", contexto);
                }
            } else {
                // Não foi possível determinar o tipo de conteúdo
                // Trate de acordo com a necessidade do seu aplicativo
                ToastCustomizado.toastCustomizadoCurto("Desconhecido", contexto);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static long getFolderSize(File directory) {
        long size = 0;
        if (directory != null && directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getFolderSize(file);
                    }
                }
            }
        }
        return size;
    }
}