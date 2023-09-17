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

import java.io.IOException;

public class OfflineActivity extends AppCompatActivity {

    private Button buttonReconectar;
    private ProgressBar progressBarOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        buttonReconectar = findViewById(R.id.buttonReconectar);
        progressBarOffline = findViewById(R.id.progressBarOffline);

        buttonReconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    progressBarOffline.setVisibility(View.VISIBLE);
                    if (isConnected()) {
                        progressBarOffline.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        progressBarOffline.setVisibility(View.GONE);
                        String mensagemToast = "Sem conexão, por favor conecte seu wifi ou seus dados móveis e tente novamente";
                        SpannableStringBuilder biggerText = new SpannableStringBuilder(mensagemToast);
                        biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, mensagemToast.length(), 0);
                        Toast.makeText(getApplicationContext(), biggerText, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception ex){
                    progressBarOffline.setVisibility(View.GONE);
                    ex.printStackTrace();
                }
            }
        });



    }

    public boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -i 5 -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
}