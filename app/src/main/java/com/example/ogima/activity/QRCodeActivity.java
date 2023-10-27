package com.example.ogima.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
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
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FriendsUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.king.zxing.CaptureActivity;
import com.king.zxing.util.CodeUtils;

import java.util.ArrayList;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView imgViewQRCodeTeste, imgViewFotoUserQRCode;
    private Button btnScannerQRCodeTeste;
    private String idUsuario = "";
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar, txtViewNomeUserQRCode;
    private ActivityResultLauncher<Intent> resultLauncher;
    private ProgressDialog progressDialog;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    public QRCodeActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    public interface RecuperarIdAlvoCallback {
        void onRecuperado(String idUser);

        void onSemDado();

        void onError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        configInicial();
        clickListeners();
        resultadoIntent();
    }

    private void abrirScannerQR() {
        boolean cameraPermissionsGranted = PermissionUtils.requestCameraPermissions(this);
        if (cameraPermissionsGranted) {
            // Inicia a leitura do QR Code
            Intent intent = new Intent(QRCodeActivity.this, CaptureActivity.class);
            resultLauncher.launch(intent);
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
                PermissionUtils.openAppSettings(QRCodeActivity.this, getApplicationContext());
            }
        }
    }

    private void resultadoIntent() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        exibirProgressDialog("analisando");
                        Intent data = result.getData();
                        if (data != null) {

                            String idQRCode = data.getStringExtra(CaptureActivity.KEY_RESULT);

                            recuperarIdAlvo(idQRCode, new RecuperarIdAlvoCallback() {
                                @Override
                                public void onRecuperado(String idUser) {

                                    if (idUser == null || idUser != null && !idUser.isEmpty()
                                            && idUser.equals(idUsuario)) {
                                        ocultarProgressDialog();
                                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.resource_unavailable_qr_code), getApplicationContext());
                                        return;
                                    }

                                    UsuarioUtils.verificaBlock(idUser, getApplicationContext(), new UsuarioUtils.VerificaBlockCallback() {
                                        @Override
                                        public void onBloqueado() {
                                            ocultarProgressDialog();
                                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.user_unavailable), getApplicationContext());
                                        }

                                        @Override
                                        public void onDisponivel() {
                                            tratarAmizade(idUser);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            ocultarProgressDialog();
                                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_when_verifying_user, message), getApplicationContext());
                                        }
                                    });
                                }

                                @Override
                                public void onSemDado() {
                                    ocultarProgressDialog();
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.user_unavailable), getApplicationContext());
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarProgressDialog();
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_when_verifying_user, message), getApplicationContext());
                                }
                            });
                        }
                    }
                });
    }

    private void tratarAmizade(String idAlvo) {
        exibirProgressDialog("verificando");
        FriendsUtils.VerificaAmizade(idAlvo, new FriendsUtils.VerificaAmizadeCallback() {
            @Override
            public void onAmigos() {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.are_already_friends), getApplicationContext());
            }

            @Override
            public void onNaoSaoAmigos() {
                verificaConvite(idAlvo);
            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adding_friend, message), getApplicationContext());
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
                        ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adding_friend, message), getApplicationContext());
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
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adding_friend, message), getApplicationContext());
            }
        });
    }

    private void adicionarAmigo(String idAlvo) {
        FriendsUtils.salvarAmigo(getApplicationContext(), idAlvo, new FriendsUtils.SalvarIdAmigoCallback() {
            @Override
            public void onAmigoSalvo() {
                FriendsUtils.AtualizarContadorAmigos(idAlvo, true, new FriendsUtils.AtualizarContadorAmigosCallback() {
                    @Override
                    public void onConcluido() {
                        FriendsUtils.AdicionarContato(idAlvo, new FriendsUtils.AdicionarContatoCallback() {
                            @Override
                            public void onContatoAdicionado() {
                                ocultarProgressDialog();
                                ToastCustomizado.toastCustomizadoCurto(getString(R.string.now_you_are_friends), getApplicationContext());
                            }

                            @Override
                            public void onError(String message) {
                                ocultarProgressDialog();
                                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adding_friend, message), getApplicationContext());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adding_friend, message), getApplicationContext());
                    }
                });
            }

            @Override
            public void onError(@NonNull String message) {
                ocultarProgressDialog();
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adding_friend, message), getApplicationContext());
            }
        });
    }

    private void exibirProgressDialog(String tipoMensagem) {
        if (tipoMensagem != null && !tipoMensagem.isEmpty()) {
            switch (tipoMensagem) {
                case "analisando":
                    progressDialog.setMessage(getString(R.string.analyzing_qrcode));
                    break;
                case "verificando":
                    progressDialog.setMessage(getString(R.string.verifying_user));
                    break;
            }
        }
        if (!QRCodeActivity.this.isFinishing()) {
            progressDialog.show();
        }
    }

    private void ocultarProgressDialog() {
        if (progressDialog != null && !QRCodeActivity.this.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void configInicial() {
        txtViewIncTituloToolbar.setText(getString(R.string.your_qr_code_scan_qr_code));
        toolbarIncPadrao.setBackgroundColor(Color.BLACK);
        progressDialog = new ProgressDialog(QRCodeActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {

                if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
                    GlideCustomizado.loadUrl(getApplicationContext(), fotoUsuario,
                            imgViewFotoUserQRCode, android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP, false, epilepsia);
                } else {
                    UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFotoUserQRCode,
                            "foto", true);
                }

                if (nomeUsuarioAjustado != null && !nomeUsuarioAjustado.isEmpty()) {
                    txtViewNomeUserQRCode.setText(FormatarContadorUtils.abreviarTexto(nomeUsuarioAjustado, 20));
                }

                if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
                    //Configura a arte do QRCode.
                    GlideCustomizado.getSharedGlideInstance(getApplicationContext())
                            .asBitmap()
                            .load(fotoUsuario)
                            .circleCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap logoBitmap, Transition<? super Bitmap> transition) {
                                    new Thread(() -> {
                                        Bitmap bitmap = CodeUtils.createQRCode(usuarioAtual.getIdQRCode(), 600, logoBitmap);
                                        runOnUiThread(() -> {
                                            imgViewQRCodeTeste.setImageBitmap(bitmap);
                                        });
                                    }).start();
                                }
                            });
                } else {
                    new Thread(() -> {
                        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_profile);
                        Bitmap bitmap = CodeUtils.createQRCode(usuarioAtual.getIdQRCode(), 600, logo);
                        runOnUiThread(() -> {
                            imgViewQRCodeTeste.setImageBitmap(bitmap);
                        });
                    }).start();
                }
            }

            @Override
            public void onSemDados() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_generating_qrcode), getApplicationContext());
                finish();
            }

            @Override
            public void onError(String mensagem) {
                ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.an_error_has_occurred), mensagem), getApplicationContext());
            }
        });
    }

    private void recuperarIdAlvo(String idQRCode, RecuperarIdAlvoCallback callback) {
        Query recuperarIdUserRef = firebaseRef.child("qrcode")
                .orderByChild("idQRCode").equalTo(idQRCode);

        recuperarIdUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Usuario usuarioId = snapshot1.getValue(Usuario.class);

                        if (usuarioId != null && usuarioId.getIdUsuario() != null
                                && !usuarioId.getIdUsuario().isEmpty()) {
                            callback.onRecuperado(usuarioId.getIdUsuario());
                        } else {
                            callback.onSemDado();
                        }
                    }
                } else {
                    callback.onSemDado();
                }
                recuperarIdUserRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnScannerQRCodeTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirScannerQR();
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