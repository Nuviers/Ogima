package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class ConfigurarFotoActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private StorageReference storageRef;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private String irParaProfile = null;
    private ImageView imgViewFoto;
    private Uri novaUri = null;
    private String[] configExibicao
            = {"Todos", "Somente amigos", "Somente seguidores",
            "Somente amigos e seguidores", "Privado"};
    private ArrayAdapter<String> opcoesExibicao;
    private AutoCompleteTextView autoCompleteTxt;
    private String publicoDefinido = null;
    private TextInputLayout txtInputLayout;
    private EditText edtTextDescricao;
    private TextView txtViewLimiteDescricao;
    private Button btnSalvar;
    private boolean edicao = false;
    private ProgressDialog progressDialog;
    private String conteudoDescricao = null;
    private final int MAX_LENGTH_DESCRIPTION = 2000;

    private Postagem dadosPostagemEdicao = new Postagem();
    private Postagem postagemReajustada = new Postagem();

    @Override
    public void onBackPressed() {
        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {

            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }

            if (dados.containsKey("novaFoto")) {
                novaUri = (Uri) dados.get("novaFoto");
                exibirNovaUri();
            }

            if (dados.containsKey("edicao")) {
                edicao = true;

                if (dados.containsKey("dadosPostagemEdicao")) {
                    dadosPostagemEdicao = (Postagem) dados.getSerializable("dadosPostagemEdicao");
                }

                exibirDadosEdicao();
            }
        }
    }

    private void exibirDadosEdicao() {

        if (dadosPostagemEdicao != null) {

            postagemReajustada.setIdPostagem(dadosPostagemEdicao.getIdPostagem());

            if (dadosPostagemEdicao.getUrlPostagem() != null
                    && !dadosPostagemEdicao.getUrlPostagem().isEmpty()) {

                GlideCustomizado.fundoGlideEpilepsia(getApplicationContext(), dadosPostagemEdicao.getUrlPostagem(),
                        imgViewFoto, android.R.color.transparent);

                postagemReajustada.setUrlPostagem(dadosPostagemEdicao.getUrlPostagem());
            }

            if (dadosPostagemEdicao.getDescricaoPostagem() != null
                    && !dadosPostagemEdicao.getDescricaoPostagem().isEmpty()) {
                edtTextDescricao.setText(dadosPostagemEdicao.getDescricaoPostagem());
                postagemReajustada.setDescricaoPostagem(dadosPostagemEdicao.getDescricaoPostagem());
            }
        }
    }

    private interface UploadCallback {
        void onUploadComplete(String urlFoto);

        void timeStampRecuperado(long timeStampNegativo, String dataFormatada);

        void onUploadError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_foto);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Publicar foto");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        configurarOpcoesPublico();
        limitarCaracteresDescricao();

        clickListeners();
    }

    private void limitarCaracteresDescricao() {
        InputFilter[] filtersDescricao = new InputFilter[1];
        filtersDescricao[0] = new InputFilter.LengthFilter(MAX_LENGTH_DESCRIPTION); // Define o limite máximo de 2.000 caracteres.
        edtTextDescricao.setFilters(filtersDescricao);

        edtTextDescricao.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int currentLength = charSequence.length();

                txtViewLimiteDescricao.setText(currentLength + "/" + MAX_LENGTH_DESCRIPTION);

                if (currentLength >= MAX_LENGTH_DESCRIPTION) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarFoto();
            }
        });
    }

    private void configurarOpcoesPublico() {
        opcoesExibicao = new ArrayAdapter<String>(this, R.layout.lista_opcoes_exibir_postagem, configExibicao);
        autoCompleteTxt.setAdapter(opcoesExibicao);
        if (publicoDefinido != null) {
            autoCompleteTxt.setText(publicoDefinido);
            opcoesExibicao.getFilter().filter(null);
        } else {
            autoCompleteTxt.setText(autoCompleteTxt.getAdapter().getItem(0).toString());
            opcoesExibicao.getFilter().filter(null);
        }
    }

    private void exibirNovaUri() {
        if (novaUri != null) {
            GlideCustomizado.loadUrl(ConfigurarFotoActivity.this,
                    novaUri.toString(), imgViewFoto, android.R.color.transparent,
                    GlideCustomizado.CENTER_INSIDE, false, true);
        }
    }

    private void salvarFoto() {
        if (novaUri != null) {
            //Nova foto
            exibirProgressDialog("upload");

            DatabaseReference salvarFotoRef = firebaseRef.child("fotos")
                    .child(idUsuario);

            String idNovaFoto = salvarFotoRef.push().getKey();

            uparImagemNoStorage(novaUri, new UploadCallback() {

                String idFotoAtual = idNovaFoto;
                HashMap<String, Object> dadosFotoAtual = new HashMap<>();
                String tipoPublico = autoCompleteTxt.getText().toString();
                String descricao = edtTextDescricao.getText().toString();

                @Override
                public void onUploadComplete(String urlFoto) {

                    if (urlFoto != null && !urlFoto.isEmpty()) {
                        dadosFotoAtual.put("idPostagem", idFotoAtual);
                        dadosFotoAtual.put("idDonoPostagem", idUsuario);
                        dadosFotoAtual.put("publicoPostagem", tipoPublico);
                        dadosFotoAtual.put("tipoPostagem", "imagem");
                        dadosFotoAtual.put("totalViewsFotoPostagem", 0);
                        dadosFotoAtual.put("urlPostagem", urlFoto);

                        if (descricao != null && !descricao.isEmpty()) {
                            dadosFotoAtual.put("descricaoPostagem", descricao);
                        }
                        recuperarTimestampNegativo(this);
                    } else {
                        ToastCustomizado.toastCustomizado("Ocorreu um erro ao publicar sua foto, tente novamente", getApplicationContext());
                        onBackPressed();
                    }
                }

                @Override
                public void timeStampRecuperado(long timeStampNegativo, String dataFormatada) {
                    dadosFotoAtual.put("timeStampNegativo", timeStampNegativo);
                    dadosFotoAtual.put("dataPostagem", dataFormatada);
                    salvarNoFirebase(idFotoAtual, dadosFotoAtual);
                }

                @Override
                public void onUploadError(String message) {

                }
            });

        } else if (edicao) {

            String descricaoAtual = edtTextDescricao.getText().toString();

            if (descricaoAtual != null) {
                ToastCustomizado.toastCustomizadoCurto("DIFERENTE",getApplicationContext());
                postagemReajustada.setDescricaoPostagem(descricaoAtual);

                ToastCustomizado.toastCustomizadoCurto("EDICAO",getApplicationContext());

                if (!descricaoAtual.equals(dadosPostagemEdicao.getDescricaoPostagem())) {
                    DatabaseReference salvarDescricaoRef = firebaseRef.child("fotos")
                            .child(dadosPostagemEdicao.getIdDonoPostagem())
                            .child(dadosPostagemEdicao.getIdPostagem())
                            .child("descricaoPostagem");

                    salvarDescricaoRef.setValue(descricaoAtual).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            onBackPressed();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            onBackPressed();
                        }
                    });
                }
            }else{
                onBackPressed();
            }
        }
    }

    private void uparImagemNoStorage(Uri uriAtual, UploadCallback uploadCallback) {
        String nomeRandomico = UUID.randomUUID().toString();

        StorageReference fotoRef = storageRef.child("fotos")
                .child(idUsuario)
                .child("foto" + nomeRandomico + ".jpeg");

        //Verificando progresso do upload
        UploadTask uploadTask = fotoRef.putFile(uriAtual);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadCallback.onUploadError(e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ToastCustomizado.toastCustomizado("Upload com sucesso", getApplicationContext());

                fotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String urlUpada = uri.toString();
                        uploadCallback.onUploadComplete(urlUpada);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadCallback.onUploadError(e.getMessage());
                    }
                });
            }
        });
    }

    private void salvarNoFirebase(String idFoto, HashMap<String, Object> dadosFoto) {
        DatabaseReference salvarFotoRef = firebaseRef.child("fotos")
                .child(idUsuario).child(idFoto);

        if (dadosFoto != null) {
            salvarFotoRef.setValue(dadosFoto).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    ToastCustomizado.toastCustomizado("Foto publicada no seu mural de fotos com sucesso", getApplicationContext());
                    onBackPressed();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro ao publicar sua foto, tente novamente mais tarde", getApplicationContext());
                    onBackPressed();
                }
            });
        }
    }

    private void exibirProgressDialog(String tipoMensagem) {

        switch (tipoMensagem) {
            case "upload":
                progressDialog.setMessage("Publicando a foto, aguarde um momento...");
                break;
            case "config":
                progressDialog.setMessage("Ajustando mídia, aguarde um momento...");
                break;
        }
        if (!isFinishing()) {
            progressDialog.show();
        }
    }

    private void recuperarTimestampNegativo(UploadCallback uploadCallback) {

        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(this, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        //ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timestampNegativo, getApplicationContext());
                        uploadCallback.timeStampRecuperado(timestampNegativo, dataFormatada);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, getApplicationContext());
                        uploadCallback.onUploadError(errorMessage);
                    }
                });
            }
        });
    }


    private void inicializandoComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgViewFoto = findViewById(R.id.imgViewFoto);
        txtInputLayout = findViewById(R.id.txtInputLayoutFoto);
        autoCompleteTxt = findViewById(R.id.autoCompleteTxtTipoPublicoFoto);
        edtTextDescricao = findViewById(R.id.edtTextDescricaoFoto);
        txtViewLimiteDescricao = findViewById(R.id.txtViewLimiteDescricaoFoto);
        btnSalvar = findViewById(R.id.btnSalvarFoto);
    }
}