package com.example.ogima.helper;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class SalvarArquivoLocalmente {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference reference;
    private String nomeDoArquivo;
    private String caminhoWallpaper;

    private File dir;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor salvarDadosEditor;

    private String nomeWallpaperLocal;
    private String urlWallpaperLocal;

    private Context context;

    public interface SalvarArquivoCallback {
        void onFileSaved(File file);
        void onSaveFailed(Exception e);
    }

    public SalvarArquivoLocalmente(Context context) {
        this.context = context;
    }

    public void transformarImagemEmFile(String caminhoImagem, SalvarArquivoCallback callback) {
        reference = storage.getReferenceFromUrl(caminhoImagem);
        nomeDoArquivo = reference.getName();

        new Thread(() -> {
            try {
                Bitmap bitmap = loadBitmapFromUrl(caminhoImagem);
                File file = salvarBitmapEmFile(bitmap, nomeDoArquivo);
                callback.onFileSaved(file);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onSaveFailed(e);
            }
        }).start();
    }

    public void transformarMidiaEmFile(String caminhoImagem, SalvarArquivoCallback callback) {
        reference = storage.getReferenceFromUrl(caminhoImagem);
        nomeDoArquivo = reference.getName();

        File file = new File(context.getCacheDir(), nomeDoArquivo);
        reference.getFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    callback.onFileSaved(file);
                })
                .addOnFailureListener(e -> {
                    callback.onSaveFailed(e);
                });
    }

    public void transformarWallpaperEmFile(String caminhoImagem, String nomeWallpaper, String tipoWallpaper, String idDestinatario, SalvarArquivoCallback callback) {
        reference = storage.getReferenceFromUrl(caminhoImagem);
        nomeDoArquivo = nomeWallpaper;

        if (tipoWallpaper.equals("privado")) {
            sharedPreferences = context.getSharedPreferences("WallpaperPrivado" + idDestinatario, Context.MODE_PRIVATE);
            caminhoWallpaper = "wallpaperPrivado";
        } else if (tipoWallpaper.equals("global")) {
            sharedPreferences = context.getSharedPreferences("WallpaperGlobal", Context.MODE_PRIVATE);
            caminhoWallpaper = "wallpaperGlobal";
        }

        salvarDadosEditor = sharedPreferences.edit();

        urlWallpaperLocal = sharedPreferences.getString("urlWallpaper", null);
        nomeWallpaperLocal = sharedPreferences.getString("nomeWallpaper", null);

        if (urlWallpaperLocal != null) {
            salvarDadosEditor.putString("urlWallpaper", caminhoImagem);
            salvarDadosEditor.putString("nomeWallpaper", nomeDoArquivo + ".jpg");
            salvarDadosEditor.apply();
        } else {
            salvarDadosEditor.putString("urlWallpaper", caminhoImagem);
            salvarDadosEditor.putString("nomeWallpaper", nomeDoArquivo + ".jpg");
            salvarDadosEditor.apply();
        }

        new Thread(() -> {
            try {
                Bitmap bitmap = loadBitmapFromUrl(caminhoImagem);
                File file = salvarBitmapEmFile(bitmap, nomeDoArquivo + ".jpg");

                if (tipoWallpaper.equals("privado")) {
                    dir = new File(context.getFilesDir(), "wallpaperPrivado" + File.separator + idDestinatario);
                } else if (tipoWallpaper.equals("global")) {
                    dir = new File(context.getFilesDir(), "wallpaperGlobal");
                }

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File wallpaperFile = new File(dir, nomeDoArquivo + ".jpg");
                copyFile(file, wallpaperFile);

                // Postar o resultado na thread principal
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onFileSaved(wallpaperFile);
                });
            } catch (IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSaveFailed(e);
                });
            }
        }).start();
    }

    public void downloadFile(String url, String directory, String fileName, SalvarArquivoCallback callback) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file));

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            callback.onFileSaved(file);
        } else {
            callback.onSaveFailed(new Exception("Failed to initialize DownloadManager"));
        }
    }

    private File salvarBitmapEmFile(Bitmap bitmap, String nomeArquivo) throws IOException {
        File file = new File(context.getCacheDir(), nomeArquivo);
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        outputStream.flush();
        outputStream.close();
        return file;
    }

    private static Bitmap loadBitmapFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        return bitmap;
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(sourceFile);
        FileOutputStream outputStream = new FileOutputStream(destFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }
}