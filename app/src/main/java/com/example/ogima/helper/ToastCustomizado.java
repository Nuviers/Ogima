package com.example.ogima.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.ogima.R;
import com.example.ogima.fragment.PerfilFragment;

public class ToastCustomizado {

    public static void toastCustomizado(String mensagem, Context context) {

        SpannableStringBuilder biggerText = new SpannableStringBuilder(mensagem);
        biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, mensagem.length(), 0);
        Toast.makeText(context, biggerText, Toast.LENGTH_LONG).show();

    }

    public static void toastCustomizadoCurto(String mensagem, Context context) {

        SpannableStringBuilder biggerText = new SpannableStringBuilder(mensagem);
        biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, mensagem.length(), 0);
        Toast.makeText(context, biggerText, Toast.LENGTH_SHORT).show();

    }



}