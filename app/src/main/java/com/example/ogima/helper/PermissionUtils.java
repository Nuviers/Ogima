package com.example.ogima.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    // Código de solicitação de permissão
    public static final int PERMISSION_REQUEST_CODE = 100;

    // Permissões necessárias para selecionar uma imagem da galeria
    private static final String[] GALLERY_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    // Permissões necessárias para capturar uma imagem usando a câmera
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
    };

    // Método para verificar se as permissões foram concedidas
    public static boolean arePermissionsGranted(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int result = ContextCompat.checkSelfPermission(activity, permission);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // Método para solicitar permissões
    public static void requestPermissions(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    // Método para verificar o resultado da solicitação de permissões
    public static boolean checkPermissionResult(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Métodos específicos para cada situação de permissão
    public static boolean areGalleryPermissionsGranted(Activity activity) {
        return arePermissionsGranted(activity, GALLERY_PERMISSIONS);
    }

    public static boolean requestGalleryPermissions(Activity activity) {
        if (areGalleryPermissionsGranted(activity)) {
            return true; // Permissões já concedidas
        } else {
            requestPermissions(activity, GALLERY_PERMISSIONS);
            return false; // Permissões solicitadas
        }
    }

    public static boolean areCameraPermissionsGranted(Activity activity) {
        return arePermissionsGranted(activity, CAMERA_PERMISSIONS);
    }

    public static boolean requestCameraPermissions(Activity activity) {
        if (areCameraPermissionsGranted(activity)) {
            return true; // Permissões já concedidas
        } else {
            requestPermissions(activity, CAMERA_PERMISSIONS);
            return false; // Permissões solicitadas
        }
    }

    public static void openAppSettings(Activity activity, Context context) {
        ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", context);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
