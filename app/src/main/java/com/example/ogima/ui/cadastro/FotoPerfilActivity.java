package com.example.ogima.ui.cadastro;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FotoPerfilActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private FloatingActionButton fabBack;
    private ImageView imgViewFoto, imgViewFundo;
    private SpinKitView progressFoto, progressFundo;
    private ImageButton imgBtnFotoGaleria, imgBtnFotoCamera,
            imgBtnFotoGif, imgBtnFundoGaleria, imgBtnFundoCamera,
            imgBtnFundoGif, imgBtnDeleteFoto, imgBtnDeleteFundo;
    private Button btnContinuarCad;
    private String idUsuario = "";
    private boolean edicao = false;
    private String tipoMidiaPermissao = "";
    private MidiaUtils midiaUtils;
    private ProgressDialog progressDialog;
    private String campoSelecionado = "";
    private Uri uriFoto = null, uriFundo = null;
    private final static String TAG = "FotoPerfilActivity";
    private String tamanhoGif = "";
    private String urlFoto = "";
    private String urlFundo = "";
    private boolean midiaFotoGif = false;
    private boolean midiaFundoGif = false;
    private StorageReference storageRef;
    private AlertDialog.Builder builder;

    public interface SalvarGifCallback {
        void onSalvo();

        void onError(String message);
    }

    public FotoPerfilActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_foto_perfil);
        inicializarComponentes();
        configInicial();
        clickListeners();
    }

    private void configInicial() {
        builder = new AlertDialog.Builder(FotoPerfilActivity.this);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        progressDialog = new ProgressDialog(FotoPerfilActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        midiaUtils = new MidiaUtils(FotoPerfilActivity.this.getSupportFragmentManager(), progressDialog, FotoPerfilActivity.this,
                getApplicationContext(), SizeUtils.MAX_FILE_SIZE_IMAGEM, 0, 0);

        Bundle dados = getIntent().getExtras();
        if (dados != null && dados.containsKey("edit")) {
            edicao = true;
            fabBack.setVisibility(View.VISIBLE);
            exibirFotosEdicao();
        } else {
            edicao = false;
            fabBack.setVisibility(View.INVISIBLE);
        }
    }

    private void checkPermissions(String campoEscolhido) {
        if (tipoMidiaPermissao != null
                && !tipoMidiaPermissao.isEmpty()
                && campoEscolhido != null
                && !campoEscolhido.isEmpty()) {
            campoSelecionado = campoEscolhido;
            if (campoSelecionado.equals("foto")) {
                midiaUtils.setLayoutCircular(true);
                if (tipoMidiaPermissao.equals("gif")) {
                    midiaFotoGif = true;
                } else {
                    midiaFotoGif = false;
                }
            } else if (campoSelecionado.equals("fundo")) {
                midiaUtils.setLayoutCircular(false);
                if (tipoMidiaPermissao.equals("gif")) {
                    midiaFundoGif = true;
                } else {
                    midiaFundoGif = false;
                }
            }
            boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(FotoPerfilActivity.this);
            if (galleryPermissionsGranted) {
                // Permissões da galeria já concedidas.
                switch (tipoMidiaPermissao) {
                    case "galeria":
                        midiaUtils.selecionarGaleria();
                        break;
                    case "gif":
                        selecionadoGif();
                        break;
                    case "camera":
                        boolean cameraPermissionsGranted = PermissionUtils.requestCameraPermissions(this);
                        if (cameraPermissionsGranted) {
                            midiaUtils.selecionarCamera();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.checkPermissionResult(grantResults)) {
                // Permissões concedidas.
                if (tipoMidiaPermissao != null
                        && !tipoMidiaPermissao.isEmpty()) {
                    switch (tipoMidiaPermissao) {
                        case "galeria":
                            midiaUtils.selecionarGaleria();
                            break;
                        case "camera":
                            midiaUtils.selecionarCamera();
                            break;
                    }
                }
            } else {
                // Permissões negadas.
                PermissionUtils.openAppSettings(FotoPerfilActivity.this, getApplicationContext());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        midiaUtils.handleActivityResult(requestCode, resultCode, data, new MidiaUtils.UriRecuperadaCallback() {
            @Override
            public void onRecuperado(Uri uriRecuperada) {
                atribuirUri(uriRecuperada);
                exibirFoto(uriRecuperada);
            }

            @Override
            public void onCancelado() {
                ocultarSpinKit(false);
            }

            @Override
            public void onError(String message) {
                ocultarSpinKit(false);
            }
        });
    }

    private void exibirFoto(Uri uri) {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            if (tipoMidiaPermissao != null
                    && !tipoMidiaPermissao.isEmpty()) {
                if (campoSelecionado.equals("foto")) {
                    urlFoto = String.valueOf(uri);
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            String.valueOf(uri), imgViewFoto, android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP, false, false, new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit(false);
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit(false);
                                }
                            });
                } else if (campoSelecionado.equals("fundo")) {
                    urlFundo = String.valueOf(uri);
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            String.valueOf(uri), imgViewFundo, android.R.color.transparent,
                            GlideCustomizado.CENTER_CROP, false, false, new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit(false);
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit(false);
                                }
                            });
                }
            }
        }
    }

    private void selecionadoGif() {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            exibirSpinKit();
            if (campoSelecionado.equals("foto")) {
                tamanhoGif = "pequena";
            } else if (campoSelecionado.equals("fundo")) {
                tamanhoGif = "media";
            }
            midiaUtils.selecionarGif(TAG, tamanhoGif, new MidiaUtils.UriRecuperadaCallback() {
                @Override
                public void onRecuperado(Uri uriRecuperada) {
                    atribuirUri(uriRecuperada);
                    exibirFoto(uriRecuperada);
                }

                @Override
                public void onCancelado() {
                    ocultarSpinKit(false);
                }

                @Override
                public void onError(String message) {
                    ocultarSpinKit(false);
                }
            });
        }
    }

    private void atribuirUri(Uri uriRecebida) {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            if (campoSelecionado.equals("foto")) {
                uriFoto = uriRecebida;
                visibilidadeImgBtnDelete(true, "foto");
            } else if (campoSelecionado.equals("fundo")) {
                visibilidadeImgBtnDelete(true, "fundo");
                uriFundo = uriRecebida;
            }
        }
    }

    private void exibirSpinKit() {
        if (campoSelecionado != null && !campoSelecionado.isEmpty()) {
            if (campoSelecionado.equals("foto")) {
                ProgressBarUtils.exibirProgressBar(progressFoto, FotoPerfilActivity.this);
            } else if (campoSelecionado.equals("fundo")) {
                ProgressBarUtils.exibirProgressBar(progressFundo, FotoPerfilActivity.this);
            }
        }
    }

    private void ocultarSpinKit(boolean comDelay) {
        if (campoSelecionado != null && !campoSelecionado.isEmpty()) {
            if (!comDelay) {
                if (campoSelecionado.equals("foto")) {
                    ProgressBarUtils.ocultarProgressBar(progressFoto, FotoPerfilActivity.this);
                } else if (campoSelecionado.equals("fundo")) {
                    ProgressBarUtils.ocultarProgressBar(progressFundo, FotoPerfilActivity.this);
                }
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (campoSelecionado.equals("foto")) {
                        ProgressBarUtils.ocultarProgressBar(progressFoto, FotoPerfilActivity.this);
                    } else if (campoSelecionado.equals("fundo")) {
                        ProgressBarUtils.ocultarProgressBar(progressFundo, FotoPerfilActivity.this);
                    }
                }
            }, 5000);
        }
    }

    private void tratarMidias() {
        if (edicao
                && urlFoto.isEmpty()
                && urlFundo.isEmpty()) {
            //É edição e nada foi alterado.
            finish();
            return;
        }

        if (urlFoto.isEmpty() && urlFundo.isEmpty()) {
            //Não é edição e o usuário não selecionou nenhuma foto.
            return;
        }

        if (urlFoto != null
                && !urlFoto.isEmpty()) {
            Log.d(TAG, "FOTOPROGRESS");
            midiaUtils.exibirProgressDialog("foto", "salvamento");
            if (midiaFotoGif) {
                salvarGifFoto(new SalvarGifCallback() {
                    @Override
                    public void onSalvo() {
                        if (urlFundo != null
                                && urlFundo.isEmpty()) {
                            //Não há mais o que salvar, finalizar activity.
                            midiaUtils.ocultarProgressDialog();
                            ToastCustomizado.toastCustomizadoCurto("Salvo com sucesso", getApplicationContext());
                        }
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                    }
                });
            } else {
                //Não é gif.
                StorageReference fotoStorage = storageRef.child("usuarios")
                        .child(idUsuario).child("minhaFoto.jpeg");
                midiaUtils.uparFotoNoStorage(fotoStorage, Uri.parse(urlFoto), new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        if (edicao) {
                            //Salvar somente a foto no firebase.
                            DatabaseReference fotoRef = firebaseRef.child("usuarios")
                                    .child(idUsuario).child("minhaFoto");
                            midiaUtils.salvarFotoNoStorage(fotoRef, urlUpada, new MidiaUtils.SalvarNoFirebaseCallback() {
                                @Override
                                public void onSalvo() {
                                    //Foto editada com sucesso.
                                    if (urlFundo != null
                                            && urlFundo.isEmpty()) {
                                        //Não há mais o que salvar, finalizar activity.
                                        midiaUtils.ocultarProgressDialog();
                                        ToastCustomizado.toastCustomizadoCurto("Salvo com sucesso", getApplicationContext());
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    midiaUtils.ocultarProgressDialog();
                                }
                            });
                        } else {
                            //Salvar todos os dados do usuário na referencia do firebase.
                        }
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                    }
                });
            }
        }

        if (urlFundo != null
                && !urlFundo.isEmpty()) {
            Log.d(TAG, "FUNDOPROGRESS");
            midiaUtils.exibirProgressDialog("fundo", "salvamento");
            if (midiaFundoGif) {
                salvarGifFundo(new SalvarGifCallback() {
                    @Override
                    public void onSalvo() {
                        ToastCustomizado.toastCustomizadoCurto("Salvo com sucesso", getApplicationContext());
                        midiaUtils.ocultarProgressDialog();
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                    }
                });
            } else {
                //Não é gif.
                StorageReference fundoStorage = storageRef.child("usuarios")
                        .child(idUsuario).child("meuFundo.jpeg");
                midiaUtils.uparFotoNoStorage(fundoStorage, Uri.parse(urlFundo), new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        if (edicao) {
                            //Salvar somente a foto no firebase.
                            DatabaseReference fundoRef = firebaseRef.child("usuarios")
                                    .child(idUsuario).child("meuFundo");
                            midiaUtils.salvarFotoNoStorage(fundoRef, urlUpada, new MidiaUtils.SalvarNoFirebaseCallback() {
                                @Override
                                public void onSalvo() {
                                    //Foto editada com sucesso.
                                    midiaUtils.ocultarProgressDialog();
                                    ToastCustomizado.toastCustomizadoCurto("Salvo com sucesso", getApplicationContext());
                                }

                                @Override
                                public void onError(String message) {
                                    midiaUtils.ocultarProgressDialog();
                                }
                            });
                        } else {
                            //Salvar todos os dados do usuário na referencia do firebase.
                        }
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                    }
                });
            }
        }
    }

    private void salvarGifFoto(SalvarGifCallback callback) {
        if (midiaFotoGif) {
            DatabaseReference fotoRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("minhaFoto");
            StorageReference fotoStorage = storageRef.child("usuarios")
                    .child(idUsuario).child("minhaFoto.jpeg");
            prepararGifParaSalvamento(fotoRef, fotoStorage, urlFoto, callback);
        }
    }

    private void salvarGifFundo(SalvarGifCallback callback) {
        if (midiaFundoGif) {
            DatabaseReference fundoRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("meuFundo");
            StorageReference fundoStorage = storageRef.child("usuarios")
                    .child(idUsuario).child("meuFundo.jpeg");
            prepararGifParaSalvamento(fundoRef, fundoStorage, urlFundo, callback);
        }
    }

    private void prepararGifParaSalvamento(DatabaseReference reference, StorageReference gifStorage, String url, SalvarGifCallback callback) {
        midiaUtils.salvarGif(reference, gifStorage, url, new MidiaUtils.SalvarGifCallback() {
            @Override
            public void onSalvo() {
                callback.onSalvo();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void exibirFotosEdicao() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (fotoUsuario != null
                        && !fotoUsuario.isEmpty()) {
                    campoSelecionado = "foto";
                    exibirSpinKit();
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            fotoUsuario, imgViewFoto, android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP, false, epilepsia, new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit(false);
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit(false);
                                }
                            });
                    visibilidadeImgBtnDelete(true, "foto");
                }else{
                    visibilidadeImgBtnDelete(false, "foto");
                }
                if (fundoUsuario != null
                        && !fundoUsuario.isEmpty()) {
                    campoSelecionado = "fundo";
                    exibirSpinKit();
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            fundoUsuario, imgViewFundo, android.R.color.transparent,
                            GlideCustomizado.CENTER_CROP, false, epilepsia, new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit(false);
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit(false);
                                }
                            });
                    visibilidadeImgBtnDelete(true, "fundo");
                }else{
                    visibilidadeImgBtnDelete(false, "fundo");
                }
            }

            @Override
            public void onSemDados() {
                finish();
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void visibilidadeImgBtnDelete(boolean exibir, String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                if (exibir) {
                    imgBtnDeleteFoto.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFoto.setVisibility(View.INVISIBLE);
                }
            } else if (campo.equals("fundo")) {
                if (exibir) {
                    imgBtnDeleteFundo.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFundo.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void exibirAlertDialog(String campo){
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                builder.setTitle("Deseja realmente excluir sua foto?");
                builder.setMessage("Sua foto será excluída permamentemente");
            }else if(campo.equals("fundo")){
                builder.setTitle("Deseja realmente excluir seu fundo?");
                builder.setMessage("Seu fundo será excluído permamentemente");
            }
            builder.setCancelable(false);
            builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    excluirFoto(campo);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void excluirFoto(String campo){
        if (campo.equals("foto")) {
            DatabaseReference removerFotoRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("minhaFoto");
            StorageReference fotoStorage = storageRef.child("usuarios")
                    .child(idUsuario).child("minhaFoto.jpeg");
            midiaUtils.excluirFoto(removerFotoRef, fotoStorage, new MidiaUtils.ExcluirFotoCallback() {
                @Override
                public void onExcluido() {
                    uriFoto = null;
                    visibilidadeImgBtnDelete(false, "foto");
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            "https://media.tenor.com/ko6th8u_HxEAAAAd/kutaka-niwatari-touhou.gif",
                            imgViewFoto, android.R.color.transparent, GlideCustomizado.CIRCLE_CROP, false,
                            true, new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {

                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                    ToastCustomizado.toastCustomizadoCurto("Foto excluída com sucesso",getApplicationContext());
                }

                @Override
                public void onError(String message) {

                }
            });
        } else if (campo.equals("fundo")) {
            DatabaseReference removerFundoRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("meuFundo");
            StorageReference fundoStorage = storageRef.child("usuarios")
                    .child(idUsuario).child("meuFundo.jpeg");
            midiaUtils.excluirFoto(removerFundoRef, fundoStorage, new MidiaUtils.ExcluirFotoCallback() {
                @Override
                public void onExcluido() {
                    uriFundo = null;
                    visibilidadeImgBtnDelete(false, "fundo");
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            "https://media.tenor.com/xxyyhZhI3KsAAAAd/yae-miko-yae-sakura.gif",
                            imgViewFundo, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false,
                            true, new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {

                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                    ToastCustomizado.toastCustomizadoCurto("Fundo excluído com sucesso",getApplicationContext());
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void clickListeners() {
        imgBtnFotoGaleria.setOnClickListener(this);
        imgBtnFotoCamera.setOnClickListener(this);
        imgBtnFotoGif.setOnClickListener(this);
        imgBtnFundoGaleria.setOnClickListener(this);
        imgBtnFundoCamera.setOnClickListener(this);
        imgBtnFundoGif.setOnClickListener(this);
        imgBtnDeleteFoto.setOnClickListener(this);
        imgBtnDeleteFundo.setOnClickListener(this);
        btnContinuarCad.setOnClickListener(this);
    }

    private void inicializarComponentes() {
        fabBack = findViewById(R.id.fabBack);
        imgViewFoto = findViewById(R.id.imgViewCadFotoUser);
        imgViewFundo = findViewById(R.id.imgViewCadFundoUser);
        progressFoto = findViewById(R.id.progressBarCadFotoUser);
        progressFundo = findViewById(R.id.progressBarCadFundoUser);
        imgBtnFotoGaleria = findViewById(R.id.imgBtnCadGaleria);
        imgBtnFotoCamera = findViewById(R.id.imgBtnCadCamera);
        imgBtnFotoGif = findViewById(R.id.imgBtnCadGif);
        imgBtnFundoGaleria = findViewById(R.id.imgBtnCadGaleriaFundo);
        imgBtnFundoCamera = findViewById(R.id.imgBtnCadCameraFundo);
        imgBtnFundoGif = findViewById(R.id.imgBtnCadGifFundo);
        imgBtnDeleteFoto = findViewById(R.id.imgBtnDeleteFotoUser);
        imgBtnDeleteFundo = findViewById(R.id.imgBtnDeleteFundoUser);
        btnContinuarCad = findViewById(R.id.btnContinuarCadFoto);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtnCadGaleria:
                tipoMidiaPermissao = "galeria";
                midiaUtils.setProgress(progressFoto);
                checkPermissions("foto");
                break;
            case R.id.imgBtnCadCamera:
                tipoMidiaPermissao = "camera";
                midiaUtils.setProgress(progressFoto);
                checkPermissions("foto");
                break;
            case R.id.imgBtnCadGif:
                tipoMidiaPermissao = "gif";
                midiaUtils.setProgress(progressFoto);
                checkPermissions("foto");
                break;
            case R.id.imgBtnCadGaleriaFundo:
                tipoMidiaPermissao = "galeria";
                midiaUtils.setProgress(progressFundo);
                checkPermissions("fundo");
                break;
            case R.id.imgBtnCadCameraFundo:
                tipoMidiaPermissao = "camera";
                midiaUtils.setProgress(progressFundo);
                checkPermissions("fundo");
                break;
            case R.id.imgBtnCadGifFundo:
                tipoMidiaPermissao = "gif";
                midiaUtils.setProgress(progressFundo);
                checkPermissions("fundo");
                break;
            case R.id.imgBtnDeleteFotoUser:
                if (uriFoto != null) {
                    uriFoto = null;
                    visibilidadeImgBtnDelete(false, "foto");
                    exibirFoto(Uri.parse("https://media.tenor.com/ko6th8u_HxEAAAAd/kutaka-niwatari-touhou.gif"));
                }else{
                    if (edicao) {
                        //Excluir do storage e a url do nó.
                        exibirAlertDialog("foto");
                    }
                }
                break;
            case R.id.imgBtnDeleteFundoUser:
                if (uriFundo != null) {
                    uriFundo = null;
                    visibilidadeImgBtnDelete(false, "fundo");
                    exibirFoto(Uri.parse("https://media.tenor.com/xxyyhZhI3KsAAAAd/yae-miko-yae-sakura.gif"));
                }else{
                    if (edicao) {
                        //Excluir do storage e a url do nó.
                        exibirAlertDialog("fundo");
                    }
                }
                break;
            case R.id.btnContinuarCadFoto:
                tratarMidias();
                break;
        }
    }
}