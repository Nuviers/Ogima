package com.example.ogima.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class CustomGlideModule extends AppGlideModule {

    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {

        // Cache de Disco - limita o cache do glidem 270 mb em bytes.
        int cacheSize100MegaBytes = 283115520;

        builder.setDiskCache(
                new InternalCacheDiskCacheFactory(context, cacheSize100MegaBytes)
        );
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // nothing to do here
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
