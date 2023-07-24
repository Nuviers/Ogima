package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideApp;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.SalvarArquivoLocalmente;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;
import com.king.zxing.CaptureActivity;
import com.king.zxing.CaptureFragment;
import com.king.zxing.ViewfinderView;
import com.king.zxing.util.CodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.annotations.Nullable;

public class TesteQRCodeActivity extends AppCompatActivity {

    private ImageView imgViewQRCodeTeste, imgViewFotoUserQRCode;
    private Button btnScannerQRCodeTeste;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar, txtViewNomeUserQRCode;
    private static final int REQUEST_CODE_QR_CODE = 77;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_qrcode);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Seu QRCode / Scan QRCode");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        toolbarIncPadrao.setBackgroundColor(Color.BLACK);

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {

                GlideCustomizado.loadUrl(getApplicationContext(), fotoUsuario,
                        imgViewFotoUserQRCode, android.R.color.transparent,
                        GlideCustomizado.CIRCLE_CROP, false, epilepsia);

                if (nomeUsuarioAjustado != null && !nomeUsuarioAjustado.isEmpty()) {
                    txtViewNomeUserQRCode.setText(nomeUsuarioAjustado);
                }

                if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
                    GlideCustomizado.getSharedGlideInstance(getApplicationContext())
                            .asBitmap()
                            .load(fotoUsuario)
                            .circleCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap logoBitmap, Transition<? super Bitmap> transition) {
                                    new Thread(() -> {
                                        //****Bitmap logo = BitmapFactory.decodeResource(getResources(),R.drawable.wallpaperwaifutfour);
                                        Bitmap bitmap =  CodeUtils.createQRCode(idUsuario,600, logoBitmap);
                                        runOnUiThread(()->{
                                            imgViewQRCodeTeste.setImageBitmap(bitmap);
                                        });
                                    }).start();
                                }
                            });
                }else{
                    new Thread(() -> {
                        Bitmap logo = BitmapFactory.decodeResource(getResources(),R.drawable.sticker_maid_excluir);
                        Bitmap bitmap =  CodeUtils.createQRCode(idUsuario,600, logo);
                        runOnUiThread(()->{
                            imgViewQRCodeTeste.setImageBitmap(bitmap);
                        });
                    }).start();
                }
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });

        btnScannerQRCodeTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirScannerQR();
            }
        });
    }

    private void abrirScannerQR(){
        boolean cameraPermissionsGranted = PermissionUtils.requestCameraPermissions(this);
        if (cameraPermissionsGranted) {
            // Iniciar a leitura do QR Code
            Intent intent = new Intent(this, CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE_QR_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.checkPermissionResult(grantResults)) {
                abrirScannerQR();
            }else{
                // Permissões negadas.
                PermissionUtils.openAppSettings(this, getApplicationContext());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_QR_CODE && resultCode == RESULT_OK && data != null) {
            // Obter o resultado da leitura do QR Code
            String result = data.getStringExtra(CaptureActivity.KEY_RESULT);
            ToastCustomizado.toastCustomizadoCurto("RESULTADO " + result, getApplicationContext());
        }
    }

    private void inicializandoComponentes() {
        imgViewQRCodeTeste = findViewById(R.id.imgViewQRCodeTeste);
        btnScannerQRCodeTeste = findViewById(R.id.btnScannerQRCodeTeste);
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgViewFotoUserQRCode = findViewById(R.id.imgViewFotoUserQRCode);
        txtViewNomeUserQRCode = findViewById(R.id.txtViewNomeUserQRCode);
    }
}