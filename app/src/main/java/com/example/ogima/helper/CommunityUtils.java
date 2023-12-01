package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.model.Comunidade;

public class CommunityUtils {
    private Activity activity;
    private Context context;
    public static final String FIELD_PHOTO = "photo";
    public static final String FIELD_BACKGROUND = "background";
    public static final int MAX_LENGHT_NAME = 100;
    public static final int MIN_LENGHT_NAME = 10;
    public static final int MAX_LENGHT_DESCRIPTION = 200;
    public static final int MIN_LENGHT_DESCRIPTION = 10;
    public static final int MAX_LENGTH_TOPICOS = 15;
    public static final int MIN_LENGTH_TOPICOS = 1;

    public CommunityUtils(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public interface ConfigBundleCallback {
        void onCadastro();

        void onEdicao(Comunidade comunidadeEdicao);

        void onSemDado();
    }

    public void configurarBundle(Bundle dados, ConfigBundleCallback callback) {
        if (dados != null) {
            if (dados.containsKey("edit")) {
                boolean edicao;
                edicao = dados.getBoolean("edit");
                if (edicao) {
                    if (dados.containsKey("dadosEdicao")
                            && dados.getSerializable("dadosEdicao") != null) {
                        Comunidade comunidadeEdicao = (Comunidade) dados.getSerializable("dadosEdicao");
                        callback.onEdicao(comunidadeEdicao);
                    }else{
                       callback.onSemDado();
                    }
                    return;
                }
                callback.onCadastro();
            } else {
                callback.onSemDado();
            }
        } else {
            callback.onSemDado();
        }
    }

    public static void exibirFotoPadrao(Context context, ImageView imgViewAlvo, String campo, boolean removerBackground){
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals(FIELD_PHOTO)) {
                if (removerBackground) {
                    imgViewAlvo.setBackgroundResource(android.R.color.transparent);
                }
                GlideCustomizado.loadDrawableCircular(context,
                        R.drawable.ic_comunidade_padrao, imgViewAlvo, android.R.color.transparent);
            } else if (campo.equals(FIELD_BACKGROUND)) {
                GlideCustomizado.loadDrawableImage(context,
                        R.drawable.placeholderuniverse, imgViewAlvo, android.R.color.transparent);
            }
        }
    }
}
