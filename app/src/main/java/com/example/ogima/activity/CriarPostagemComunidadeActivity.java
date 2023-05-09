package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GiphyUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.RemoverEspacosTexto;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class CriarPostagemComunidadeActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    //Toolbar include
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;

    private String tipoPostagem;
    //Verifição de permissões necessárias
    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private static final int MAX_FILE_SIZE_VIDEO = 17;
    private StorageReference imagemRef, videoRef;
    private StorageReference storageRef;
    private static final int SELECAO_GALERIA = 100,
            SELECAO_GIF = 200,
            SELECAO_VIDEO = 300;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private ProgressDialog progressDialog;
    private VerificaTamanhoArquivo verificaTamanhoArquivo = new VerificaTamanhoArquivo();
    private Bitmap imagemSelecionada;
    private Uri videoSelecionado;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private boolean arquivoMudado = false;
    private RoundedImageView imgViewFoto;
    private ImageView imgViewGif;
    private EditText edtTextTitulo, edtTextDescricao;
    private TextView txtViewLimiteNome, txtViewLimiteDescricao;
    private Button btnPostagemPublica, btnPostagemParticular, btnPublicarPostagem;
    private String idComunidade;
    private Postagem postagem = new Postagem();
    private Postagem postagemEdicao;
    private final int MAX_LENGTH_TITLE = 100;
    private final int MIN_LENGTH_TITLE = 10;
    private final int MAX_LENGTH_DESCRIPTION = 250;
    private final int MIN_LENGTH_DESCRIPTION = 0;
    private Boolean limiteCaracteresPermitido = false;
    private Boolean edicaoPostagem = false;
    private HashMap<String, Object> dadosPostagem = new HashMap<>();
    private DatabaseReference contadorPostagemRef;
    private GiphyUtils giphyUtils = new GiphyUtils();
    private GiphyDialogFragment gdl;
    private DatabaseReference postagemComunidadeRef;

    //Configs ExoPlayer
    private StyledPlayerView videoExpandido;
    private ExoPlayer exoPlayerExpandido;
    private RelativeLayout relativeLayout;
    private TextView txtViewTituloPostagemTexto;

    private String tituloAjustado, descricaoAjustada;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayerExpandido != null) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayerExpandido != null) {
            pausePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (exoPlayerExpandido != null) {
            startPlayer();
            seekTo();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_postagem_comunidade);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Publicar postagem");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {

            if (dados.containsKey("tipoPostagem")) {
                idComunidade = dados.getString("idComunidade");
                tipoPostagem = dados.getString("tipoPostagem");
                contadorPostagemRef = firebaseRef.child("contadorPostagensComunidade")
                        .child(idComunidade).child("totalPostagens");
            }

            if (dados.containsKey("postagemEdicao")) {
                postagemEdicao = (Postagem) dados.getSerializable("postagemEdicao");
            }
        }

        if (idComunidade != null) {
            dadosPostagem.put("idPostagem", postagem.getIdPostagem());
        }

        if (postagemEdicao != null) {
            edicaoPostagem = true;
        }

        switch (tipoPostagem) {
            case "imagem":
                exibirComponentesPostagem();
                imgViewFoto.setVisibility(View.VISIBLE);
                if (edicaoPostagem) {

                } else {
                    solicitaPermissoes("galeria");
                    if (!solicitaPermissoes.exibirPermissaoNegada) {
                        selecionarFoto();
                    }
                }
                break;
            case "gif":
                mudarLayoutBelowParaGif();
                exibirComponentesPostagem();
                imgViewGif.setVisibility(View.VISIBLE);
                if (edicaoPostagem) {

                } else {
                    solicitaPermissoes("gif");
                    if (!solicitaPermissoes.exibirPermissaoNegada) {
                        selecionarGif();
                    }
                }
                break;
            case "video":
                mudarLayoutBelowParaVideo();
                exibirComponentesPostagem();
                videoExpandido.setVisibility(View.VISIBLE);
                if (edicaoPostagem) {

                } else {
                    solicitaPermissoes("video");
                    if (!solicitaPermissoes.exibirPermissaoNegada) {
                        selecionarVideo();
                    }
                }
                break;
            case "texto":
                mudarLayoutBelowParaTexto();
                txtViewTituloPostagemTexto.setVisibility(View.VISIBLE);
                edtTextDescricao.setHint("Descreva sua postagem:");
                edtTextDescricao.setVisibility(View.VISIBLE);
                txtViewLimiteDescricao.setVisibility(View.VISIBLE);
                if (edicaoPostagem) {

                }
                break;
        }

        configLinhasEditText();
        clickListeners();
        limitadorCaracteres();
    }

    private void limitadorCaracteres() {

        edtTextTitulo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();

                txtViewLimiteNome.setText(currentLength + "/" + MAX_LENGTH_TITLE);

                if (currentLength >= MAX_LENGTH_TITLE) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

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

    private boolean verificaLimiteCaracteres() {

        if (edtTextTitulo != null && !edtTextTitulo.getText().toString().isEmpty()) {
            tituloAjustado = RemoverEspacosTexto.removeExtraSpaces(edtTextTitulo);
        }

        if (edtTextDescricao != null && !edtTextDescricao.getText().toString().isEmpty()) {
            descricaoAjustada = RemoverEspacosTexto.removeExtraSpaces(edtTextDescricao);
        }

        if (tituloAjustado != null && tituloAjustado.length() > MAX_LENGTH_TITLE || descricaoAjustada != null && descricaoAjustada.length() > MAX_LENGTH_DESCRIPTION) {
            ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
            limiteCaracteresPermitido = false;
        } else {
            limiteCaracteresPermitido = true;
        }
        return limiteCaracteresPermitido;
    }

    private void clickListeners() {
        btnPublicarPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postagemEdicao != null) {
                    //Edição
                } else {
                    //Nova postagem
                    if (verificaLimiteCaracteres()) {

                        if (tipoPostagem.equals("texto")) {
                            if (descricaoAjustada == null || descricaoAjustada.isEmpty()) {
                                ToastCustomizado.toastCustomizadoCurto("Necessário que você escreva algo na descrição da postagem para que seja possível a publicação da postagem", getApplicationContext());
                                return;
                            }

                            if (descricaoAjustada != null && descricaoAjustada.length() < 10) {
                                ToastCustomizado.toastCustomizadoCurto("Necessário que a descrição tenha pelo menos 10 caracteres", getApplicationContext());
                                return;
                            }
                        }

                        if (tituloAjustado != null && !tituloAjustado.isEmpty()) {
                            dadosPostagem.put("tituloPostagem", tituloAjustado);
                        }

                        if (descricaoAjustada != null && !descricaoAjustada.isEmpty()) {
                            dadosPostagem.put("descricaoPostagem", descricaoAjustada);
                        }

                        dadosPostagem.put("idDonoPostagem", idUsuario);
                        dadosPostagem.put("tipoPostagem", tipoPostagem);

                        if (edicaoPostagem) {
                            salvarDadosNoFirebase();
                        } else {
                            recuperarTimestampNegativo();
                        }
                    }
                }
            }
        });
    }

    private void configLinhasEditText() {

        InputFilter[] filtersTitulo = new InputFilter[1];
        filtersTitulo[0] = new InputFilter.LengthFilter(100); // Define o limite de 30 caracteres por linha
        edtTextTitulo.setFilters(filtersTitulo);

        InputFilter[] filtersDescricao = new InputFilter[1];
        filtersDescricao[0] = new InputFilter.LengthFilter(250); // Define o limite de 30 caracteres por linha
        edtTextDescricao.setFilters(filtersDescricao);

    }

    private void inicializandoComponentes() {
        //inc_toolbar_padrao
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        //

        imgViewFoto = findViewById(R.id.imgViewPostagemComunidade);
        imgViewGif = findViewById(R.id.imgViewGifPostagemComunidade);
        edtTextTitulo = findViewById(R.id.edtTextTituloPostagemComunidade);
        edtTextDescricao = findViewById(R.id.edtTextDescPostagemComunidade);
        txtViewLimiteNome = findViewById(R.id.txtViewLimiteNomePostagemComunidade);
        txtViewLimiteDescricao = findViewById(R.id.txtViewLimiteDescPostagemComunidade);
        btnPostagemPublica = findViewById(R.id.btnPostagemComunidadePublica);
        btnPostagemParticular = findViewById(R.id.btnPostagemComunidadeParticular);
        btnPublicarPostagem = findViewById(R.id.btnPublicarComunidadePostagem);
        relativeLayout = findViewById(R.id.relativeLayoutPostagemComunidade);
        txtViewTituloPostagemTexto = findViewById(R.id.txtViewTituloPostagemTexto);

        //video
        videoExpandido = findViewById(R.id.videoExpandidoPostagemComunidade);
    }

    private void selecionarFoto() {
        //Passando a intenção de selecionar uma foto pela galeria
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //Verificando se a intenção foi atendida com sucesso
        if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_GALERIA);
        }
    }

    private void selecionarGif() {
        giphyUtils.selectGifPostagem(this, new GiphyUtils.GifSelectionListener() {
            @Override
            public void onGifSelected(String gifPequena, String gifMedio, String gifOriginal) {
                dadosPostagem.put("urlPostagem", gifOriginal);
                exibirGif(gifMedio);
            }
        }, new GiphyUtils.GiphyDialogListener() {
            @Override
            public void onGiphyDialogDismissed() {
                ToastCustomizado.toastCustomizado("Cancelado", getApplicationContext());
                finish();
            }
        });
        gdl = giphyUtils.retornarGiphyDialog();
        gdl.show(CriarPostagemComunidadeActivity.this.getSupportFragmentManager(), "CriarPostagemComunidadeActivity");

        arquivoMudado = true;
    }

    private void selecionarVideo() {
        Matisse.from(CriarPostagemComunidadeActivity.this)
                .choose(MimeType.ofVideo())
                .countable(true)
                .maxSelectable(1)
                .showSingleMediaType(true)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(SELECAO_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        arquivoMudado = false;

        if (resultCode == RESULT_OK) {

            if (requestCode == SELECAO_GALERIA) {

                arquivoMudado = true;

                String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
                destinoArquivo += ".jpg";
                final Uri localImagemFotoSelecionada = data.getData();

                if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM, localImagemFotoSelecionada, getApplicationContext())) {
                    // Procede com o upload do arquivo
                    //*Chamando método responsável pela estrutura do U crop

                    String mimeType = getContentResolver().getType(localImagemFotoSelecionada);

                    if (mimeType != null && mimeType.startsWith("image/gif")) {
                        // É uma GIF, exibir mensagem de aviso ao usuário
                        ToastCustomizado.toastCustomizado("Postagem de imagem selecionada, não é possível selecionar" +
                                " gifs para esse tipo de postagem", getApplicationContext());
                        finish();
                    } else {
                        openCropActivity(localImagemFotoSelecionada, Uri.fromFile(new File(getCacheDir(), destinoArquivo)));
                    }

                } else {
                    arquivoMudado = false;
                    ToastCustomizado.toastCustomizadoCurto("Selecione uma foto menor que " + MAX_FILE_SIZE_IMAGEM + " MB", getApplicationContext());
                    solicitaPermissoes("galeria");
                    if (!solicitaPermissoes.exibirPermissaoNegada) {
                        selecionarFoto();
                    }
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == UCrop.REQUEST_CROP
                    || requestCode == 101) {

                arquivoMudado = true;

                try {

                    if (baos != null) {
                        baos.reset();
                    }

                    Uri imagemCortada = UCrop.getOutput(data);
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemCortada);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

                    imagemSelecionada = imagemBitmap;

                    exibirImagemSelecionada();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else if (requestCode == SELECAO_VIDEO) {

                arquivoMudado = true;

                String path = String.valueOf(Matisse.obtainResult(data).get(0));
                Uri videoUri;
                videoUri = Uri.parse(path);

                videoSelecionado = videoUri;

                if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_VIDEO, videoUri, getApplicationContext())) {
                    exibirVideoSelecionado(videoSelecionado);
                } else {
                    arquivoMudado = false;
                    ToastCustomizado.toastCustomizadoCurto("Selecione um video menor que " + MAX_FILE_SIZE_VIDEO + " MB", getApplicationContext());
                    solicitaPermissoes("video");
                    if (!solicitaPermissoes.exibirPermissaoNegada) {
                        selecionarVideo();
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            arquivoMudado = false;
            ToastCustomizado.toastCustomizadoCurto("Cancelado", getApplicationContext());
            finish();
        }
    }

    private void salvarImagem() {
        try {
            //Recupera dados da imagem para o firebase

            if (arquivoMudado) {
                byte[] dadosImagem = baos.toByteArray();

                baos.close();

                String nomeRandomico = UUID.randomUUID().toString();
                imagemRef = storageRef.child("postagensComunidade")
                        .child("imagens")
                        .child(idComunidade)
                        .child("imagem" + nomeRandomico + ".jpeg");
                //Verificando progresso do upload
                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadNoStorage(uploadTask, imagemRef);
            } else {
                dadosPostagem.put("urlPostagem", postagemEdicao.getUrlPostagem());
                salvarDadosNoFirebase();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void salvarVideo() {
        if (arquivoMudado) {

            String nomeRandomico = UUID.randomUUID().toString();
            videoRef = storageRef.child("postagensComunidade")
                    .child("videos")
                    .child(idComunidade)
                    .child("video" + nomeRandomico + ".mp4");

            UploadTask uploadTask = videoRef.putFile(videoSelecionado);
            uploadNoStorage(uploadTask, videoRef);
        } else {
            dadosPostagem.put("urlPostagem", postagemEdicao.getUrlPostagem());
            salvarDadosNoFirebase();
        }
    }

    private void atualizarContador() {
        if (postagemEdicao == null) {
            //Somente atualiza contador de postagem se for uma nova
            //postagem
            // Incrementar o valor
            AtualizarContador atualizarContador = new AtualizarContador();
            atualizarContador.acrescentarContador(contadorPostagemRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    contadorPostagemRef.setValue(contadorAtualizado).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            ToastCustomizado.toastCustomizadoCurto("Postagem publicada com sucesso", getApplicationContext());
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao publicar a postagem " + e.getMessage(), getApplicationContext());
                            finish();
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    ToastCustomizado.toastCustomizadoCurto("Fail " + errorMessage, getApplicationContext());
                }
            });
        }
    }

    private void uploadNoStorage(UploadTask uploadTask, StorageReference arquivoStorageRef) {
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                arquivoStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri url = task.getResult();
                        String urlNewPostagem = url.toString();
                        dadosPostagem.put("urlPostagem", urlNewPostagem);
                        salvarDadosNoFirebase();
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    private void exibirVideoSelecionado(Uri uriVideo) {
        exoPlayerExpandido = new ExoPlayer.Builder(getApplicationContext()).build();
        videoExpandido.setPlayer(exoPlayerExpandido);
        MediaItem mediaItem = MediaItem.fromUri(uriVideo);
        exoPlayerExpandido.addMediaItem(mediaItem);
        exoPlayerExpandido.prepare();
        exoPlayerExpandido.setPlayWhenReady(true);
    }

    private void exibirImagemSelecionada() {
        GlideCustomizado.montarGlideRoundedBitmap(getApplicationContext(),
                imagemSelecionada, imgViewFoto, android.R.color.transparent);
    }

    private void exibirGif(String urlGif) {

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (epilepsia) {
                    GlideCustomizado.montarGlideFotoEpilepsia(getApplicationContext(),
                            urlGif, imgViewGif, android.R.color.transparent);
                } else {
                    GlideCustomizado.montarGlideFoto(getApplicationContext(),
                            urlGif, imgViewGif, android.R.color.transparent);
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void solicitaPermissoes(String permissao) {
        //Verifica quais permissões falta a ser solicitadas, caso alguma seja negada, exibe um toast.
        if (!solicitaPermissoes.verificaPermissoes(permissoesNecessarias, CriarPostagemComunidadeActivity.this, permissao)) {
            if (permissao != null) {
                solicitaPermissoes.tratarResultadoPermissoes(permissao, CriarPostagemComunidadeActivity.this);
            }
        }
    }

    public void startPlayer() {
        exoPlayerExpandido.setPlayWhenReady(true);
        exoPlayerExpandido.getPlaybackState();
    }

    public void seekTo() {
        exoPlayerExpandido.seekTo(exoPlayerExpandido.getCurrentPosition());
    }

    public void pausePlayer() {
        exoPlayerExpandido.setPlayWhenReady(false);
        exoPlayerExpandido.getPlaybackState();
    }

    public void releasePlayer() {
        exoPlayerExpandido.release();
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(CriarPostagemComunidadeActivity.this);
    }

    //*Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptions() {
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar foto");
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }

    private void recuperarTimestampNegativo() {

        progressDialog.setMessage("Publicando postagem, aguarde um momento...");
        progressDialog.show();

        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(this, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timestamps, getApplicationContext());
                        long timestampNegativo = -1 * timestamps;
                        dadosPostagem.put("timestampNegativo", timestampNegativo);
                        dadosPostagem.put("dataPostagem", dataFormatada);
                        if (tipoPostagem.equals("imagem")) {
                            salvarImagem();
                        } else if (tipoPostagem.equals("gif")) {
                            salvarDadosNoFirebase();
                        } else if (tipoPostagem.equals("video")) {
                            salvarVideo();
                        } else if (tipoPostagem.equals("texto")) {
                            salvarDadosNoFirebase();
                        }
                        ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timestampNegativo, getApplicationContext());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, getApplicationContext());
                        finish();
                    }
                });
            }
        });
    }

    private void mudarLayoutBelowParaGif() {
        // Obtém os parâmetros de layout atuais
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) edtTextDescricao.getLayoutParams();

        // Altera o ID da View abaixo
        layoutParams.addRule(RelativeLayout.BELOW, R.id.imgViewGifPostagemComunidade);

        // Aplica os novos parâmetros de layout
        edtTextDescricao.setLayoutParams(layoutParams);
    }

    private void mudarLayoutBelowParaVideo() {
        // Obtém os parâmetros de layout atuais
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) edtTextDescricao.getLayoutParams();

        // Altera o ID da View abaixo
        layoutParams.addRule(RelativeLayout.BELOW, R.id.videoExpandidoPostagemComunidade);

        // Aplica os novos parâmetros de layout
        edtTextDescricao.setLayoutParams(layoutParams);
    }

    private void mudarLayoutBelowParaTexto() {
        // Obtém os parâmetros de layout atuais
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) edtTextDescricao.getLayoutParams();

        // Altera o ID da View abaixo
        layoutParams.addRule(RelativeLayout.BELOW, R.id.txtViewTituloPostagemTexto);

        // Aplica os novos parâmetros de layout
        edtTextDescricao.setLayoutParams(layoutParams);
    }

    private void salvarDadosNoFirebase() {

        if (edicaoPostagem) {
            postagemComunidadeRef = firebaseRef.child("postagensComunidade")
                    .child(idComunidade)
                    .child(postagemEdicao.getIdPostagem());
        } else {
            postagemComunidadeRef = firebaseRef.child("postagensComunidade")
                    .child(idComunidade)
                    .child(postagem.getIdPostagem());
        }

        postagemComunidadeRef.setValue(dadosPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!edicaoPostagem) {
                    atualizarContador();
                } else {
                    finish();
                }
                ToastCustomizado.toastCustomizadoCurto("Publicado com sucesso", getApplicationContext());
            }
        });
    }

    private void exibirComponentesPostagem() {
        edtTextTitulo.setVisibility(View.VISIBLE);
        edtTextDescricao.setVisibility(View.VISIBLE);
        txtViewLimiteNome.setVisibility(View.VISIBLE);
        txtViewLimiteDescricao.setVisibility(View.VISIBLE);
    }
}