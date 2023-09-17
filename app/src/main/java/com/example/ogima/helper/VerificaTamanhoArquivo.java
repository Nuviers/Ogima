package com.example.ogima.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;

public class VerificaTamanhoArquivo {

    private long tamanhoArquivo;
    private double tamanhoMB;
    private Boolean limitePermitido = false;

    public boolean verificaLimiteMB(int MAX_FILE_SIZE, Uri uriRecebida, Context context) {

        tamanhoArquivo = getFileSize(uriRecebida, context);
        tamanhoMB = (double) tamanhoArquivo / (1024 * 1024);
        if (tamanhoMB > MAX_FILE_SIZE) {
            // Mostra uma mensagem de erro
            ToastCustomizado.toastCustomizadoCurto("O arquivo selecionado excede o limite de tamanho permitido: " + MAX_FILE_SIZE + " MB", context);
            limitePermitido = false;
        } else {
            // Procede com o upload do arquivo
            //ToastCustomizado.toastCustomizadoCurto("Limite permitido", context);
            limitePermitido = true;
        }
        return limitePermitido;
    }

    private long getFileSize(Uri uri, Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        long size = cursor.getLong(sizeIndex);
        cursor.close();
        return size;
    }
}
