package com.example.ogima.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ogima.activity.ConversaActivity;

import java.util.ArrayList;
import java.util.List;

public class SolicitaPermissoes {

    public Boolean exibirPermissaoNegada = false;

    public boolean verificaPermissoes(String[] permissoesSolicitadas, Activity activityRecebida, String tipoPermissao) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissoesSolicitadas) {
            if (tipoPermissao != null) {
                if (tipoPermissao.equals("camera")) {
                    if (ContextCompat.checkSelfPermission(activityRecebida, permission) == PackageManager.PERMISSION_DENIED) {
                        listPermissionsNeeded.add(permission);
                    }
                } else {
                    //Ignorar permissão da camera quando somente é solicitado acesso a tratamento da galeria.
                    if (!permission.equals("android.permission.CAMERA")) {
                        if (ContextCompat.checkSelfPermission(activityRecebida, permission) == PackageManager.PERMISSION_DENIED) {
                            listPermissionsNeeded.add(permission);
                        }
                    }
                }
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activityRecebida, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 17);
            return false;
        }
        //Permissão não foi negada.
        return true;
    }

    public void tratarResultadoPermissoes(String tipoPermissao, Activity activityRecebida) {

        exibirPermissaoNegada = false;

        if (tipoPermissao.equals("camera")) {
            if (ContextCompat.checkSelfPermission(activityRecebida, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                // Permissão não concedida
                exibirPermissaoNegada = true;
            }
            if (ContextCompat.checkSelfPermission(activityRecebida, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                exibirPermissaoNegada = true;
            }
        } else {
            //Trata as permissões de armazenamento
            if (ContextCompat.checkSelfPermission(activityRecebida, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                exibirPermissaoNegada = true;
            }
            if (ContextCompat.checkSelfPermission(activityRecebida, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                exibirPermissaoNegada = true;
            }
        }

        if (exibirPermissaoNegada) {
            ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", activityRecebida);
        }
    }
}
