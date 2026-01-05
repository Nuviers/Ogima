package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.AutoStartHelper;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

public class PermissaoSegundoPlanoActivity extends AppCompatActivity {

    private TextView txtViewTitle, txtViewDesc;
    private Button btnViewInicioAutomatico, btnViewOtimizacaoBateria,
            btnPularEtapa;
    private ImageButton imgBtnStatusInicioAutomatico, imgBtnStatusPermissaoOtimizacao,
            imgBtnPularEtapa, imgBtnInfoInicioAutomatico, imgBtnInfoOtimizacao,
            imgBtnInfoPularEtapa;
    private AutoStartHelper autoStartHelper;

    private String INFO_PERMISSAO = "";
    private String INFO_IGNORAR = "";

    @Override
    protected void onStart() {
        super.onStart();
        verificarPermissaoConcedida();
    }

    public PermissaoSegundoPlanoActivity() {
        //Dispositivos de certas marcas não funciona as notificações
        //e serviços em segundo plano quando a inicialização automatica do app estiver
        //desativada. Essa é a única "solução" por agora.

        // Inicializar a classe AutoStartHelper
        autoStartHelper = AutoStartHelper.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissao_segundo_plano);
        INFO_PERMISSAO = getString(R.string.permission_information);
        INFO_IGNORAR = getString(R.string.information_when_ignoring);
        inicializandoComponentes();
        clickListeners();
        //TooltipCompat.setTooltipText(imgBtnInfoInicioAutomatico, INFO_INICIO_AUTOMATICO);
    }

    private void inicializandoComponentes() {
        txtViewTitle = findViewById(R.id.txtViewTitlePermissaoNotif);
        txtViewDesc = findViewById(R.id.txtViewDescPermissaoNotif);
        btnViewInicioAutomatico = findViewById(R.id.btnViewPermissaoInicioAutomatico);
        imgBtnStatusInicioAutomatico = findViewById(R.id.imgBtnStatusInicioAutomatico);
        btnViewOtimizacaoBateria = findViewById(R.id.btnViewPermissaoOtimizacaoBateria);
        imgBtnStatusPermissaoOtimizacao = findViewById(R.id.imgBtnStatusPermissaoOtimizacao);
        btnPularEtapa = findViewById(R.id.btnPularEtapaPermissaoNotif);
        imgBtnPularEtapa = findViewById(R.id.imgBtnPularEtapaPermissaoNotif);
        imgBtnInfoInicioAutomatico = findViewById(R.id.imgBtnInfoPermissaoInicioAutomatico);
        imgBtnInfoOtimizacao = findViewById(R.id.imgBtnInfoPermissaoOtimizacao);
        imgBtnInfoPularEtapa = findViewById(R.id.imgBtnInfoPularEtapa);
    }

    private void clickListeners() {
        btnViewInicioAutomatico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaPermissaoInicioAutomatico();
            }
        });

        btnViewOtimizacaoBateria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaPermissaoOtimizacaoBateria();
            }
        });

        imgBtnInfoInicioAutomatico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTooltip(imgBtnInfoInicioAutomatico, INFO_PERMISSAO);
            }
        });

        imgBtnInfoPularEtapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTooltip(imgBtnInfoPularEtapa, INFO_IGNORAR);
            }
        });

        btnPularEtapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pularEtapa();
            }
        });

        imgBtnPularEtapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pularEtapa();
            }
        });
    }

    private void verificaPermissaoInicioAutomatico() {
        autoStartHelper.getAutoStartPermission(PermissaoSegundoPlanoActivity.this,
                true, false);
    }

    private void verificaPermissaoOtimizacaoBateria() {
        autoStartHelper.getAutoStartPermission(PermissaoSegundoPlanoActivity.this,
                false, true);
    }

    private void verificarPermissaoConcedida() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            btnViewOtimizacaoBateria.setVisibility(View.INVISIBLE);
            return;
        }
        btnViewOtimizacaoBateria.setVisibility(View.VISIBLE);
        //Verifica a permissão de bateria
        if (autoStartHelper.isBatteryOptimizationDisabled(PermissaoSegundoPlanoActivity.this)) {
            //Permissão de inicialização concedida
            imgBtnStatusPermissaoOtimizacao.setBackgroundResource(R.drawable.bg_color_permissao_checked);
        } else {
            imgBtnStatusPermissaoOtimizacao.setBackgroundResource(R.drawable.bg_color_permissao);
        }
    }

    private void showTooltip(View anchorView, String tooltipText) {
        View tooltipView = getLayoutInflater().inflate(R.layout.layout_tooltip, null);
        PopupWindow popupWindow = new PopupWindow(tooltipView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        TextView textViewContent = tooltipView.findViewById(R.id.textViewContent);
        textViewContent.setText(tooltipText);

        // Configura a posição do balãozinho acima do botão
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY,
                location[0], location[1] - anchorView.getHeight() - 16); // Ajuste a posição vertical aqui conforme necessário

        // Fecha o balãozinho quando o usuário clicar fora dele
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOnDismissListener(() -> {
            // Aqui você pode executar alguma ação quando o tooltip for fechado
        });
    }

    private void pularEtapa() {
        Intent intent = new Intent(PermissaoSegundoPlanoActivity.this, NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
    }
}