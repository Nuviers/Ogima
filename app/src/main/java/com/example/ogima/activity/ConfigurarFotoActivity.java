package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.CommonPosting;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.PostUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Postagem;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class ConfigurarFotoActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private StorageReference storageRef;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private String irParaProfile = null;
    private ImageView imgViewFoto;
    private Uri uriRecuperada = null;
    private String publicoDefinido = null;
    private EditText edtTextDescricao;
    private TextView txtViewLimiteDescricao;
    private Button btnSalvar;
    private boolean edicao = false;
    private ProgressDialog progressDialog;
    private Postagem postagemEdicao;
    private PostUtils postUtils;
    private MidiaUtils midiaUtils;
    private CommonPosting commonPosting;
    private SpinKitView spinKitPost;

    public ConfigurarFotoActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public void onBackPressed() {
        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_foto);
        inicializarComponentes();
        configInicial();
        configBundle();
        clickListeners();
    }

    private void configInicial() {
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText(getString(R.string.configure_photo));
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        postUtils = new PostUtils(ConfigurarFotoActivity.this, getApplicationContext());
        midiaUtils = new MidiaUtils(ConfigurarFotoActivity.this, getApplicationContext());
        commonPosting = new CommonPosting(ConfigurarFotoActivity.this, getApplicationContext(), progressDialog, postUtils, getString(R.string.photo));
        postUtils.limitarCaracteresDescricao(edtTextDescricao, txtViewLimiteDescricao);
    }

    private void configBundle() {
        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }
            edicao = commonPosting.edicao(dados);
            if (edicao) {
                postagemEdicao = new Postagem();
                postagemEdicao = commonPosting.postagemEdicao(dados);
                if (postagemEdicao != null) {
                    exibirDadosEdicao();
                } else {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_post, getString(R.string.photo)), getApplicationContext());
                    commonPosting.finalizarActivity();
                }
                return;
            }
            uriRecuperada = commonPosting.recuperarUri(dados);
            commonPosting.exibirUri(uriRecuperada, spinKitPost, imgViewFoto, GlideCustomizado.CENTER_CROP,
                    true);
        }
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commonPosting.finalizarActivity();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarFoto();
            }
        });
    }

    private void exibirDadosEdicao() {
        commonPosting.exibirPostagemEdicao(postagemEdicao, spinKitPost,
                imgViewFoto, GlideCustomizado.CENTER_CROP, true);
        commonPosting.exibirDescricaoEdicao(postagemEdicao, edtTextDescricao);
    }

    private void salvarFoto() {

        if (uriRecuperada == null && !edicao) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post, getString(R.string.photo)), getApplicationContext());
            commonPosting.finalizarActivity();
            return;
        }

        if (edicao) {
            if (postagemEdicao != null) {
                postUtils.exibirProgressDialog(progressDialog, "edicao");
                commonPosting.salvarEdicaoFoto(postagemEdicao, edtTextDescricao, new CommonPosting.SalvarEdicaoFotoCallback() {
                    @Override
                    public void onConcluido() {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.photo_published_successfully), getApplicationContext());
                        commonPosting.finalizarActivity();
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                        postUtils.ocultarProgressDialog(progressDialog);
                    }
                });
                return;
            } else {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_when_editing_post, getString(R.string.photo)), getApplicationContext());
                commonPosting.finalizarActivity();
                return;
            }
        }
        postUtils.exibirProgressDialog(progressDialog, "upload");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nomeRandomico = UUID.randomUUID().toString();
        String nomeArquivo = String.format("%s%s%s%s", "foto", timestamp, nomeRandomico, ".jpeg");
        StorageReference midiaRef = storageRef.child("fotos")
                .child(idUsuario)
                .child(nomeArquivo);
        midiaUtils.uparFotoNoStorage(midiaRef, uriRecuperada, new MidiaUtils.UparNoStorageCallback() {
            DatabaseReference fotoRef = firebaseRef.child("fotos")
                    .child(idUsuario);
            String idPostagem = postUtils.retornarIdRandom(fotoRef);
            String descricao = edtTextDescricao.getText().toString().trim();

            @Override
            public void onConcluido(String urlUpada) {
                if (idPostagem != null && !idPostagem.isEmpty()) {
                    postUtils.prepararHashMapFoto(idUsuario, idPostagem, "imagem", urlUpada,
                            descricao, new PostUtils.SalvarHashMapCallback() {
                                @Override
                                public void onSalvo() {
                                    ToastCustomizado.toastCustomizado(getString(R.string.photo_published_on_the_wall), getApplicationContext());
                                    commonPosting.finalizarActivity();
                                }

                                @Override
                                public void onError(String message) {
                                    ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
                                    postUtils.ocultarProgressDialog(progressDialog);
                                }
                            });
                } else {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post, getString(R.string.photo)), getApplicationContext());
                    commonPosting.finalizarActivity();
                }
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post, getString(R.string.photo)), getApplicationContext());
                postUtils.ocultarProgressDialog(progressDialog);
            }
        });
    }

    private void inicializarComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgViewFoto = findViewById(R.id.imgViewFoto);
        edtTextDescricao = findViewById(R.id.edtTextDescricaoFoto);
        txtViewLimiteDescricao = findViewById(R.id.txtViewLimiteDescricaoFoto);
        btnSalvar = findViewById(R.id.btnSalvarFoto);
        spinKitPost = findViewById(R.id.spinKitPost);
    }
}