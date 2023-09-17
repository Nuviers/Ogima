package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import com.example.ogima.helper.FriendsUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.king.zxing.CaptureActivity;
import com.king.zxing.util.CodeUtils;

import java.util.ArrayList;

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
                                        Bitmap bitmap = CodeUtils.createQRCode(idUsuario, 600, logoBitmap);
                                        runOnUiThread(() -> {
                                            imgViewQRCodeTeste.setImageBitmap(bitmap);
                                        });
                                    }).start();
                                }
                            });
                } else {
                    new Thread(() -> {
                        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_maid_excluir);
                        Bitmap bitmap = CodeUtils.createQRCode(idUsuario, 600, logo);
                        runOnUiThread(() -> {
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

    private void abrirScannerQR() {
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
            } else {
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
            String idUser = data.getStringExtra(CaptureActivity.KEY_RESULT);

            if (idUser == null || idUser != null && !idUser.isEmpty()
                    && idUser.equals(idUsuario)) {
                ToastCustomizado.toastCustomizadoCurto("Recurso não disponível para esse usuário", getApplicationContext());
                return;
            }

            UsuarioUtils.VerificaBlock(idUser, getApplicationContext(), new UsuarioUtils.VerificaBlockCallback() {
                @Override
                public void onBloqueado() {
                    //Utils exibe um toast mostrando a seguinte mensagem - "usuário indisponível"
                }

                @Override
                public void onDisponivel() {
                    tratarAmizade(idUser);
                    ToastCustomizado.toastCustomizadoCurto("RESULTADO " + idUser, getApplicationContext());
                }

                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizadoCurto("Erro ao verificar block " + message, getApplicationContext());
                }
            });
        }
    }

    private void tratarAmizade(String idAlvo) {
        FriendsUtils.VerificaAmizade(idAlvo, new FriendsUtils.VerificaAmizadeCallback() {
            @Override
            public void onAmigos() {
                ToastCustomizado.toastCustomizadoCurto("Vocês já são amigos", getApplicationContext());
            }

            @Override
            public void onNaoSaoAmigos() {
                verificaConvite(idAlvo);
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Erro ao tratar Amizade " + message, getApplicationContext());
            }
        });
    }

    private void verificaConvite(String idAlvo) {
        FriendsUtils.VerificaConvite(idAlvo, new FriendsUtils.VerificaConviteCallback() {
            @Override
            public void onConvitePendente() {
                //Remover convites antes de adicionar o amigo.
                FriendsUtils.RemoverConvites(idAlvo, new FriendsUtils.RemoverConviteCallback() {
                    @Override
                    public void onRemovido() {
                        //Convite de amizade removido e contador de convite diminuido.
                        adicionarAmigo(idAlvo);
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto("Erro ao verifica Convite " + message, getApplicationContext());
                    }
                });
            }

            @Override
            public void onSemConvites() {
                //Adicionar normalmente em friends.
                adicionarAmigo(idAlvo);
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Erro ao verifica Convite " + message, getApplicationContext());
            }
        });
    }

    private void adicionarAmigo(String idAlvo) {
        FriendsUtils.salvarAmigo(idAlvo, new FriendsUtils.SalvarIdAmigoCallback() {
            @Override
            public void onAmigoSalvo() {
                FriendsUtils.AtualizarContadorAmigos(idAlvo, true, new FriendsUtils.AtualizarContadorAmigosCallback() {
                    @Override
                    public void onConcluido() {
                        FriendsUtils.AdicionarContato(idAlvo, new FriendsUtils.AdicionarContatoCallback() {
                            @Override
                            public void onContatoAdicionado() {
                                ToastCustomizado.toastCustomizadoCurto("Agora vocês são amigos", getApplicationContext());
                            }

                            @Override
                            public void onError(String message) {
                                ToastCustomizado.toastCustomizadoCurto("Erro ao adicionar amigo " + message, getApplicationContext());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto("Erro ao adicionar amigo " + message, getApplicationContext());
                    }
                });
            }

            @Override
            public void onError(@NonNull String message) {
                ToastCustomizado.toastCustomizadoCurto("Erro ao adicionar amigo " + message, getApplicationContext());
            }
        });
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