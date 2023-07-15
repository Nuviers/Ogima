package com.example.ogima.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class GlideCustomized {

    private static RequestManager sharedGlide;

    public static synchronized RequestManager getSharedGlideInstance(Context context) {
        if (sharedGlide == null) {
            sharedGlide = GlideApp.with(context.getApplicationContext());
        }
        return sharedGlide;
    }

    public static void loadImage(Context context, String url, ImageView imageView, int placeholder) {

        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .encodeQuality(100)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop();

        getSharedGlideInstance(context)
                .load(url)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void loadCircularImage(Context context, String url, ImageView imageView, int placeholder) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .encodeQuality(100)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .transform(new CircleCrop());

        getSharedGlideInstance(context)
                .load(url)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void loadBitmapImage(Context context, Bitmap bitmap, ImageView imageView, int placeholder) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .encodeQuality(100)
                .error(android.R.color.transparent)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop();

        getSharedGlideInstance(context)
                .load(bitmap)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }
}
