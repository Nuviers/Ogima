package com.example.ogima.helper;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.widget.Toast;

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