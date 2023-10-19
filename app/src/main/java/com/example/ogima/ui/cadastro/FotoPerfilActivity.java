package com.example.ogima.ui.cadastro;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.activity.PermissaoSegundoPlanoActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

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
    private boolean midiaFotoGif = false;
    private boolean midiaFundoGif = false;
    private StorageReference storageRef;
    private AlertDialog.Builder builder;
    private Usuario usuarioCad;
    private boolean statusEpilepsia = true;
    private String msgSalvamento = "", msgEdicao = "", msgError = "";

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public interface SalvarGifCallback {
        void onSalvo();

        void onError(String message);
    }

    public interface DadosIniciaisCallback{
        void onConcluido();
        void onError(String message);
    }

    public interface SalvarIdQRCodeCallback{
        void onSalvo();
        void onError(String message);
    }

    public FotoPerfilActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimparCacheUtils limparCacheUtils = new LimparCacheUtils();
        limparCacheUtils.clearAppCache(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_foto_perfil);
        inicializarComponentes();
        fabBack.hide();
        configInicial(new DadosIniciaisCallback() {
            @Override
            public void onConcluido() {
                clickListeners();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                finish();
            }
        });
    }

    private void configInicial(DadosIniciaisCallback callback) {
        msgSalvamento = getString(R.string.saved_successfully);
        msgEdicao = getString(R.string.successfully_changed);
        msgError = getString(R.string.an_error_has_occurred);
        builder = new AlertDialog.Builder(FotoPerfilActivity.this);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        progressDialog = new ProgressDialog(FotoPerfilActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        midiaUtils = new MidiaUtils(FotoPerfilActivity.this.getSupportFragmentManager(), progressDialog, FotoPerfilActivity.this,
                getApplicationContext(), SizeUtils.MAX_FILE_SIZE_IMAGEM, 0, 0);

        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            if (dados.containsKey("edit")) {
                edicao = true;
                fabBack.show();
                exibirFotosEdicao(callback);
            } else if (dados.containsKey("dadosCadastro")) {
                edicao = false;
                fabBack.hide();
                usuarioCad = new Usuario();
                usuarioCad = (Usuario) dados.getSerializable("dadosCadastro");
                setStatusEpilepsia(usuarioCad.isStatusEpilepsia());
                callback.onConcluido();
            }
        } else {
            finish();
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
                ocultarSpinKit();
            }

            @Override
            public void onError(String message) {
                ocultarSpinKit();
            }
        });
    }

    private void exibirFoto(Uri uri) {
        if (campoSelecionado != null
                && !campoSelecionado.isEmpty()) {
            if (tipoMidiaPermissao != null
                    && !tipoMidiaPermissao.isEmpty()) {
                if (campoSelecionado.equals("foto")) {
                    Drawable circle = getDrawable(R.drawable.circle);
                    imgViewFoto.setBackground(circle);
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            String.valueOf(uri), imgViewFoto, android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit();
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit();
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_user_photo), getApplicationContext());
                                }
                            });
                } else if (campoSelecionado.equals("fundo")) {
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            String.valueOf(uri), imgViewFundo, android.R.color.transparent,
                            GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit();
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit();
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_user_background), getApplicationContext());
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
                    ocultarSpinKit();
                }

                @Override
                public void onError(String message) {
                    ocultarSpinKit();
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_gif), getApplicationContext());
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

    private void ocultarSpinKit() {
        if (campoSelecionado != null && !campoSelecionado.isEmpty()) {
            if (campoSelecionado.equals("foto")) {
                ProgressBarUtils.ocultarProgressBar(progressFoto, FotoPerfilActivity.this);
            } else if (campoSelecionado.equals("fundo")) {
                ProgressBarUtils.ocultarProgressBar(progressFundo, FotoPerfilActivity.this);
            }
        }
    }

    private void tratarMidias() {
        if (edicao
                && uriFoto == null
                && uriFundo == null) {
            //É edição e nada foi alterado.
            finish();
            return;
        }

        if (!edicao && uriFoto == null && uriFundo == null) {
            //Não é edição e o usuário não selecionou nenhuma foto.
            alertDialogSemFotos();
            return;
        }

        if (uriFoto != null) {
            Log.d(TAG, "FOTOPROGRESS");
            midiaUtils.exibirProgressDialog("foto", "salvamento");
            if (midiaFotoGif) {
                salvarGifFoto(new SalvarGifCallback() {
                    @Override
                    public void onSalvo() {
                        if (uriFundo == null) {
                            //Não há mais o que salvar, finalizar activity.
                            midiaUtils.ocultarProgressDialog();
                            if (edicao) {
                                ToastCustomizado.toastCustomizadoCurto(msgEdicao, getApplicationContext());
                                finish();
                            } else {
                                salvarUsuario();
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.error_saving_gif), message), getApplicationContext());
                    }
                });
            } else {
                //Não é gif.
                StorageReference fotoStorage = storageRef.child("usuarios")
                        .child(idUsuario).child("minhaFoto.jpeg");
                midiaUtils.uparFotoNoStorage(fotoStorage, uriFoto, new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        //Salvar somente a foto no firebase.
                        DatabaseReference fotoRef = firebaseRef.child("usuarios")
                                .child(idUsuario).child("minhaFoto");
                        midiaUtils.salvarFotoNoStorage(fotoRef, urlUpada, new MidiaUtils.SalvarNoFirebaseCallback() {
                            @Override
                            public void onSalvo(String urlUpada) {
                                uriFoto = Uri.parse(urlUpada);
                                //Foto editada com sucesso.
                                if (uriFundo == null) {
                                    //Não há mais o que salvar, finalizar activity.
                                    midiaUtils.ocultarProgressDialog();
                                    if (edicao) {
                                        ToastCustomizado.toastCustomizado(msgEdicao, getApplicationContext());
                                        finish();
                                    } else {
                                        salvarUsuario();
                                    }
                                }
                            }

                            @Override
                            public void onError(String message) {
                                midiaUtils.ocultarProgressDialog();
                                ToastCustomizado.toastCustomizado(getString(R.string.error_saving_photo), getApplicationContext());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizado(getString(R.string.error_saving_photo), getApplicationContext());
                    }
                });
            }
        }

        if (uriFundo != null) {
            midiaUtils.exibirProgressDialog("fundo", "salvamento");
            if (midiaFundoGif) {
                salvarGifFundo(new SalvarGifCallback() {
                    @Override
                    public void onSalvo() {
                        midiaUtils.ocultarProgressDialog();
                        if (edicao) {
                            ToastCustomizado.toastCustomizadoCurto(msgEdicao, getApplicationContext());
                            finish();
                        } else {
                            salvarUsuario();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.error_saving_gif), message), getApplicationContext());
                    }
                });
            } else {
                //Não é gif.
                StorageReference fundoStorage = storageRef.child("usuarios")
                        .child(idUsuario).child("meuFundo.jpeg");
                midiaUtils.uparFotoNoStorage(fundoStorage, uriFundo, new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        //Salvar somente a foto no firebase.
                        DatabaseReference fundoRef = firebaseRef.child("usuarios")
                                .child(idUsuario).child("meuFundo");
                        midiaUtils.salvarFotoNoStorage(fundoRef, urlUpada, new MidiaUtils.SalvarNoFirebaseCallback() {
                            @Override
                            public void onSalvo(String urlUpada) {
                                uriFundo = Uri.parse(urlUpada);
                                //Foto editada com sucesso.
                                midiaUtils.ocultarProgressDialog();
                                if (edicao) {
                                    ToastCustomizado.toastCustomizadoCurto(msgEdicao, getApplicationContext());
                                    finish();
                                } else {
                                    salvarUsuario();
                                }
                            }

                            @Override
                            public void onError(String message) {
                                midiaUtils.ocultarProgressDialog();
                                ToastCustomizado.toastCustomizado(getString(R.string.error_saving_user_background), getApplicationContext());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        midiaUtils.ocultarProgressDialog();
                        ToastCustomizado.toastCustomizado(getString(R.string.error_saving_user_background), getApplicationContext());
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
            prepararGifParaSalvamento(fotoRef, fotoStorage, String.valueOf(uriFoto),"foto",callback);
        }
    }

    private void salvarGifFundo(SalvarGifCallback callback) {
        if (midiaFundoGif) {
            DatabaseReference fundoRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("meuFundo");
            StorageReference fundoStorage = storageRef.child("usuarios")
                    .child(idUsuario).child("meuFundo.jpeg");
            prepararGifParaSalvamento(fundoRef, fundoStorage, String.valueOf(uriFundo),"fundo",callback);
        }
    }

    private void prepararGifParaSalvamento(DatabaseReference reference, StorageReference gifStorage, String url, String campo, SalvarGifCallback callback) {
        midiaUtils.salvarGif(reference, gifStorage, url, new MidiaUtils.SalvarGifCallback() {
            @Override
            public void onSalvo(String urlUpada) {
                if (campo != null && !campo.isEmpty()) {
                    if (campo.equals("foto")) {
                        uriFoto = Uri.parse(urlUpada);
                    } else if (campo.equals("fundo")) {
                        uriFundo = Uri.parse(urlUpada);
                    }
                    callback.onSalvo();
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void exibirFotosEdicao(DadosIniciaisCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {

                setStatusEpilepsia(epilepsia);

                if (fotoUsuario != null
                        && !fotoUsuario.isEmpty()) {
                    campoSelecionado = "foto";
                    exibirSpinKit();
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            fotoUsuario, imgViewFoto, android.R.color.transparent,
                            GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit();
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit();
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_user_photo), getApplicationContext());
                                }
                            });
                    visibilidadeImgBtnDelete(true, "foto");
                } else {
                    visibilidadeImgBtnDelete(false, "foto");
                }
                if (fundoUsuario != null
                        && !fundoUsuario.isEmpty()) {
                    campoSelecionado = "fundo";
                    exibirSpinKit();
                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                            fundoUsuario, imgViewFundo, android.R.color.transparent,
                            GlideCustomizado.CENTER_CROP, false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                @Override
                                public void onCarregado() {
                                    ocultarSpinKit();
                                }

                                @Override
                                public void onError(String message) {
                                    ocultarSpinKit();
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_loading_user_background), getApplicationContext());
                                }
                            });
                    visibilidadeImgBtnDelete(true, "fundo");
                } else {
                    visibilidadeImgBtnDelete(false, "fundo");
                }
                callback.onConcluido();
            }

            @Override
            public void onSemDados() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), getApplicationContext());
                finish();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
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

    private void exibirAlertDialog(String campo) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                builder.setTitle("Deseja realmente excluir sua foto?");
                builder.setMessage("Sua foto será excluída permamentemente");
            } else if (campo.equals("fundo")) {
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

    private void excluirFoto(String campo) {
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
                    UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFoto, UsuarioUtils.FIELD_PHOTO, true);
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.deleted_photo), getApplicationContext());
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
                    UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFundo, UsuarioUtils.FIELD_BACKGROUND, false);
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.deleted_profile_background), getApplicationContext());
                }

                @Override
                public void onError(String message) {
                }
            });
        }
    }

    private void alertDialogSemFotos() {
        builder.setTitle(getString(R.string.photo_user_alert_dialog_title));
        builder.setMessage(getString(R.string.photo_user_alert_dialog_message));
        builder.setCancelable(true);
        builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                salvarUsuario();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void salvarUsuario() {
        if (usuarioCad != null) {
            salvarIdQRCode(new SalvarIdQRCodeCallback() {
                @Override
                public void onSalvo() {
                    if (uriFoto != null) {
                        usuarioCad.setMinhaFoto(String.valueOf(uriFoto));
                    }
                    if (uriFundo != null) {
                        usuarioCad.setMeuFundo(String.valueOf(uriFundo));
                    }
                    DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                            .child(idUsuario);
                    usuarioRef.setValue(usuarioCad).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.registration_completed), getApplicationContext());
                            Intent intent = new Intent(FotoPerfilActivity.this, PermissaoSegundoPlanoActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), e.getMessage()), getApplicationContext());
                            deslogarUsuario();
                        }
                    });
                }
                @Override
                public void onError(String message) {
                    ToastCustomizado.toastCustomizadoCurto(message,getApplicationContext());
                }
            });
        } else {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_in_registration_cad), getApplicationContext());
            deslogarUsuario();
        }
    }

    private void deslogarUsuario() {
        if (autenticacao.getCurrentUser() != null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                    .requestEmail()
                    .build();
            GoogleSignInClient mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
            FirebaseAuth.getInstance().signOut();
            mSignInClient.signOut();
            finish();
        } else {
            finish();
        }
    }

    private void salvarIdQRCode(SalvarIdQRCodeCallback callback){
        DatabaseReference qrcodeRef = firebaseRef.child("qrcode");
        String idQRCode = qrcodeRef.push().getKey();
        if (idQRCode != null && !idQRCode.isEmpty()) {
            DatabaseReference salvarIdQRCodeRef = firebaseRef.child("qrcode")
                    .child(idQRCode);
            HashMap<String, Object> dadosQRcode = new HashMap<>();
            dadosQRcode.put("idUsuario",idUsuario);
            dadosQRcode.put("idQRCode", idQRCode);
            salvarIdQRCodeRef.setValue(dadosQRcode).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    usuarioCad.setIdQRCode(idQRCode);
                    callback.onSalvo();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            });
        }else{
            callback.onError(getString(R.string.error_in_registration_cad));
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
        fabBack.setOnClickListener(this);
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
                    campoSelecionado = "foto";
                    visibilidadeImgBtnDelete(false, "foto");
                    UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFoto, UsuarioUtils.FIELD_PHOTO, true);
                } else {
                    if (edicao) {
                        exibirAlertDialog("foto");
                    }
                }
                break;
            case R.id.imgBtnDeleteFundoUser:
                if (uriFundo != null) {
                    uriFundo = null;
                    campoSelecionado = "fundo";
                    visibilidadeImgBtnDelete(false, "fundo");
                    UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFundo, UsuarioUtils.FIELD_BACKGROUND, false);
                } else {
                    if (edicao) {
                        exibirAlertDialog("fundo");
                    }
                }
                break;
            case R.id.btnContinuarCadFoto:
                tratarMidias();
                break;
            case R.id.fabBack:
                if (edicao) {
                    onBackPressed();
                }
                break;
        }
    }
}