package com.example.ogima.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class SalvarArquivoLocalmente {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference reference;
    private String nomeDoArquivo;
    private String caminhoWallpaper;

    private File dir;

    public interface SalvarArquivoCallback {
        void onFileSaved(File file);
        void onSaveFailed(Exception e);
    }

    public void transformarImagemEmFile(Context context, String caminhoImagem, SalvarArquivoCallback callback){

        reference = storage.getReferenceFromUrl(caminhoImagem);
        nomeDoArquivo = reference.getName();

        Glide.with(context)
                .asBitmap()
                .load(caminhoImagem)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        File file = new File(context.getCacheDir(), nomeDoArquivo);
                        try {
                            FileOutputStream outputStream = new FileOutputStream(file);
                            resource.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                            outputStream.flush();
                            outputStream.close();
                            callback.onFileSaved(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                            callback.onSaveFailed(e);
                        }
                    }
                });
    }

    public void transformarMidiaEmFile(Context context, String caminhoImagem, SalvarArquivoCallback callback){
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

    public void transformarWallpaperEmFile(Context context, String caminhoImagem,String nomeWallpaper, String tipoWallpaper, String idDestinatario, SalvarArquivoCallback callback){

        reference = storage.getReferenceFromUrl(caminhoImagem);
        nomeDoArquivo = nomeWallpaper;

        if (tipoWallpaper.equals("privado")) {
            //caminhoWallpaper = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + idDestinatario + File.separator + "wallpaperPrivado"));
            caminhoWallpaper = "wallpaperPrivado";
        } else if (tipoWallpaper.equals("global")) {
            //caminhoWallpaper = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + idDestinatario + File.separator + "wallpaperGlobal"));
            caminhoWallpaper = "wallpaperGlobal";
        }

        Glide.with(context)
                .asBitmap()
                .load(caminhoImagem)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        if (tipoWallpaper.equals("privado")) {
                            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File.separator + "Ogima" + File.separator + idDestinatario + File.separator + caminhoWallpaper);
                        } else if (tipoWallpaper.equals("global")) {
                            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File.separator + "Ogima" + File.separator + caminhoWallpaper);
                        }

                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        File file = new File(dir, nomeDoArquivo + ".jpg");

                        try {
                            FileOutputStream outputStream = new FileOutputStream(file);
                            resource.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                            outputStream.flush();
                            outputStream.close();
                            callback.onFileSaved(file);
                            //ToastCustomizado.toastCustomizadoCurto("Salvo em: " + file.getAbsolutePath(), context);
                        } catch (IOException e) {
                            e.printStackTrace();
                            callback.onSaveFailed(e);
                            //ToastCustomizado.toastCustomizado("Error: " + e.getMessage(), context);
                        }
                    }
                });
    }
}