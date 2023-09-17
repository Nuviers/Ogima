package com.example.ogima.helper;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

    public static void showSnackbar(@NonNull View view, @NonNull String message) {
       Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
       View snackbarView = snackbar.getView();
       TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
       textView.setTextSize(20);
       snackbar.show();
    }

    public static void showSnackbarWithAction(@NonNull View view, @NonNull String message, @NonNull String actionText, @NonNull View.OnClickListener actionClickListener) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, actionClickListener)
                .show();
    }
}
