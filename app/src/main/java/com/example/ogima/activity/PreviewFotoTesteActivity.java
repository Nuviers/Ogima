package com.example.ogima.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.IntentUtils;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.RecuperarUriUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PreviewFotoTesteActivity extends AppCompatActivity {

    private ImageView imgPreviewTeste1, imgPreviewTeste2, imgPreviewTeste3, imgPreviewTeste4;
    private Bundle dados;
    private ArrayList<String> urisRecuperadas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_foto_teste);
        inicializandoComponentes();

        dados = getIntent().getExtras();

        if (dados != null && dados.containsKey("urisSelecionadas")) {
            urisRecuperadas = dados.getStringArrayList("urisSelecionadas");
        }

        if (dados != null && dados.containsKey("urlTeste")) {
            String urlTeste = dados.getString("urlTeste");
            mostrarPreview(imgPreviewTeste1, urlTeste);
        }

        if (urisRecuperadas != null && !urisRecuperadas.isEmpty()) {
            mostrarPreview(imgPreviewTeste1, urisRecuperadas.get(0));
            if (urisRecuperadas.size() >= 2) {
                mostrarPreview(imgPreviewTeste2, urisRecuperadas.get(1));
            }
            if (urisRecuperadas.size() >= 3) {
                mostrarPreview(imgPreviewTeste3, urisRecuperadas.get(2));
            }
            if (urisRecuperadas.size() >= 4) {
                mostrarPreview(imgPreviewTeste4, urisRecuperadas.get(3));
            }
        }
    }

    private void mostrarPreview(ImageView imgViewAlvo, String uriAlvo) {
        GlideCustomizado.loadUrlComListener(getApplicationContext(),
                uriAlvo, imgViewAlvo, android.R.color.transparent,
                GlideCustomizado.CENTER_INSIDE, false, false, new GlideCustomizado.ListenerLoadUrlCallback() {
                    @Override
                    public void onCarregado() {
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
    }

    private void inicializandoComponentes() {
        imgPreviewTeste1 = findViewById(R.id.imgPreviewTeste1);
        imgPreviewTeste2 = findViewById(R.id.imgPreviewTeste2);
        imgPreviewTeste3 = findViewById(R.id.imgPreviewTeste3);
        imgPreviewTeste4 = findViewById(R.id.imgPreviewTeste4);
    }
}
