package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.ogima.activity.EditarPerfilActivity;

public class IrParaEdicaoDePerfil {
    public static void intentEdicao(Activity activity){
        Intent intent = new Intent(activity.getApplicationContext(), EditarPerfilActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
