package com.example.ogima.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Placeholder;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.ogima.R;
import com.example.ogima.fragment.PerfilFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class GlideCustomizado {

    public static void montarGlideEpilepsia(Context contexto, String arquivo, ImageView componente, int placeholder) {
        PerfilFragment perfilFragment = new PerfilFragment();
        try{
            Glide.with(contexto)
                    .asBitmap().load(arquivo).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }
            }).placeholder(placeholder)
                    .error(R.drawable.errorimagem)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop()
                    .into(componente);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void montarGlide(Context contexto, String arquivo, ImageView componente, int placeholder) {
        PerfilFragment perfilFragment = new PerfilFragment();
        perfilFragment.animacaoShimmer();
        try{
            Glide.with(contexto).load(arquivo).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }
            }).placeholder(placeholder)
                    .error(R.drawable.errorimagem)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop()
                    .into(componente);;
    }catch (Exception ex){
            perfilFragment.animacaoShimmer();
            ex.printStackTrace();
        }
    }

    public static void fundoGlideEpilepsia(Context contexto, String arquivo, ImageView componente, int placeholder) {
        PerfilFragment perfilFragment = new PerfilFragment();
        perfilFragment.animacaoShimmer();
        try{
            Glide.with(contexto)
                    .asBitmap().load(arquivo).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }
            }).placeholder(placeholder)
                    .error(R.drawable.errorimagem)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(componente);
        }catch (Exception ex){
            perfilFragment.animacaoShimmer();
            ex.printStackTrace();
        }
    }

    public static void fundoGlide(Context contexto, String arquivo, ImageView componente, int placeholder) {
        PerfilFragment perfilFragment = new PerfilFragment();
        perfilFragment.animacaoShimmer();
        try{
            Glide.with(contexto).load(arquivo).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    perfilFragment.animacaoShimmer();
                    return false;
                }
            }).placeholder(placeholder)
                    .error(R.drawable.errorimagem)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(componente);
        }catch (Exception ex){
            perfilFragment.animacaoShimmer();
            ex.printStackTrace();
        }
    }
}