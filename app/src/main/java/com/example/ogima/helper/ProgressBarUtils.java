package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

public class ProgressBarUtils {

    public static void exibirProgressBar(ProgressBar progressBar, Activity activity) {
        if (progressBar != null && !activity.isFinishing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public static void ocultarProgressBar(ProgressBar progressBar, Activity activity) {
        if (progressBar != null && !activity.isFinishing()) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
