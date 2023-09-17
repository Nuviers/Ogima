package com.example.ogima.helper;

import static android.content.Context.MODE_PRIVATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;


import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoStartHelper {

    // Informações específicas das marcas
    private final Map<String, AutoStartInfo> brandInfoMap = new HashMap<>();

    // Construtor privado para garantir que somente a classe possa criar uma instância
    private AutoStartHelper() {
        setupBrandInfo();
    }

    public static AutoStartHelper getInstance() {
        return new AutoStartHelper();
    }

    // Configura as informações específicas das marcas e seus respectivos componentes para inicialização automática
    private void setupBrandInfo() {
        // Adicione aqui as informações para outras marcas, se necessário
        // ...

        // Xiaomi
        AutoStartInfo xiaomiInfo = new AutoStartInfo("xiaomi", "com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
        brandInfoMap.put("xiaomi", xiaomiInfo);
        // Letv
        AutoStartInfo letvInfo = new AutoStartInfo("letv", "com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity");
        brandInfoMap.put("letv", letvInfo);
        // ASUS ROG
        AutoStartInfo asusInfo = new AutoStartInfo("asus", "com.asus.mobilemanager", "com.asus.mobilemanager.powersaver.PowerSaverSettings");
        brandInfoMap.put("asus", asusInfo);
        // Honor
        AutoStartInfo honorInfo = new AutoStartInfo("honor", "com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");
        brandInfoMap.put("honor", honorInfo);
        // Oppo
        AutoStartInfo oppoInfo = new AutoStartInfo("oppo", "com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity");
        brandInfoMap.put("oppo", oppoInfo);
        // Vivo
        AutoStartInfo vivoInfo = new AutoStartInfo("vivo", "com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
        brandInfoMap.put("vivo", vivoInfo);
        // Nokia
        AutoStartInfo nokiaInfo = new AutoStartInfo("nokia", "com.evenwell.powersaving.g3", "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity");
        brandInfoMap.put("nokia", nokiaInfo);
    }

    // Estrutura para armazenar informações sobre a inicialização automática de cada marca
    private static class AutoStartInfo {
        String brand;
        String mainPackage;
        String component;

        AutoStartInfo(String brand, String mainPackage, String component) {
            this.brand = brand;
            this.mainPackage = mainPackage;
            this.component = component;
        }
    }

    public void getAutoStartPermission(Context context, boolean permissaoInicio, boolean permissaoBateria) {
        String buildManufacturer = Build.MANUFACTURER.toLowerCase();
        String device = Build.MODEL.toLowerCase();

        ToastCustomizado.toastCustomizado("Device - " + device, context);
        ToastCustomizado.toastCustomizado("Manufacturer - " + buildManufacturer, context);

        if (permissaoInicio) {
            // Verifica a permissão de inicialização automática
            AutoStartInfo brandInfo = brandInfoMap.get(buildManufacturer);
            if (brandInfo != null) {
                autoStartForBrand(context, brandInfo);
            }
        }

        if (permissaoBateria) {
            // Verifica a permissão de otimização de bateria
            openBatteryOptimizationSettings(context);
        }
    }

    private void autoStartForBrand(final Context context, final AutoStartInfo brandInfo) {
        if (isPackageExists(context, brandInfo.mainPackage)) {
            showAlert(context, "Permissão de Inicialização Automática",
                    String.format("Para receber notificações, ative a opção de inicialização automática para o aplicativo no dispositivo %s.", brandInfo.brand),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                context.startActivity(intent);
                                startIntent(context, brandInfo.mainPackage, brandInfo.component); // Aqui é chamado o método startIntent
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastCustomizado.toastCustomizado("Não foi possível ativar a inicialização automática. Verifique as configurações do dispositivo.", context);
                            }
                            dialog.dismiss();
                        }
                    });
        }
    }

    private void showAlert(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Permitir", onClickListener)
                .show()
                .setCancelable(false);
    }

    private void startIntent(Context context, String packageName, String componentName) throws Exception {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, componentName));
            context.startActivity(intent);
        } catch (Exception var5) {
            var5.printStackTrace();
            throw var5;
        }
    }

    private Boolean isPackageExists(Context context, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBatteryOptimizationDisabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true; // Se a versão for anterior ao Android Marshmallow, não há otimização de bateria para verificar
    }

    public void openBatteryOptimizationSettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }
}
