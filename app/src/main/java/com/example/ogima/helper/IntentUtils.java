package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

public class IntentUtils {
    public static void irParaProfile(Activity activity, Context context){
        if (activity != null && context != null) {
            Intent intent = new Intent(context, NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
