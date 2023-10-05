package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;

import java.io.IOException;

public class OfflineActivity extends AppCompatActivity {

    private Button btnReconectar;
    private ProgressBar progressBarOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        inicializarComponentes();
        clickListeners();
    }

    private void irParaSplashActivity(){
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -i 5 -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    private void clickListeners() {
        btnReconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ProgressBarUtils.exibirProgressBar(progressBarOffline, OfflineActivity.this);
                    if (isConnected()) {
                        ProgressBarUtils.ocultarProgressBar(progressBarOffline, OfflineActivity.this);
                        irParaSplashActivity();
                    } else {
                        ProgressBarUtils.ocultarProgressBar(progressBarOffline, OfflineActivity.this);
                        String mensagemToast = getString(R.string.warning_no_connection);
                        SpannableStringBuilder biggerText = new SpannableStringBuilder(mensagemToast);
                        biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, mensagemToast.length(), 0);
                        Toast.makeText(getApplicationContext(), biggerText, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ProgressBarUtils.ocultarProgressBar(progressBarOffline, OfflineActivity.this);
                    ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.an_error_has_occurred), ex.getMessage()), getApplicationContext());
                    ex.printStackTrace();
                }
            }
        });
    }

    private void inicializarComponentes() {
        btnReconectar = findViewById(R.id.btnTentarReconectar);
        progressBarOffline = findViewById(R.id.progressBarOffline);
    }
}