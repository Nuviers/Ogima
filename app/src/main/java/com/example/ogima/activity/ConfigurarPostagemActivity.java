package com.example.ogima.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ConfigurarPostagemActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private StorageReference storageRef;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private String irParaProfile = null;
    private ImageView imgViewPostagem, imgViewGifNewPostagem;
    private Uri novaUri = null;
    private EditText edtTextDescricao;
    private TextView txtViewLimiteDescricao;
    private Button btnSalvar;
    private boolean edicao = false;
    private ProgressDialog progressDialog;
    private String conteudoDescricao = null;
    private final int MAX_LENGTH_DESCRIPTION = 2000;

    private Postagem dadosPostagemEdicao = new Postagem();
    private Postagem postagemReajustada = new Postagem();
    private String tipoPostagem = null;
    private String novaUrlGif = null;
    private CardView cardViewConfigPostagem;
    private FrameLayout framePreviewPostagem;
    private StyledPlayerView styledPlayer;
    private SpinKitView spinProgressBarExo;
    private ExoPlayer exoPlayer;
    private Player.Listener listenerExo;
    private boolean isControllerVisible = false;

    private boolean dadosExibidos = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!dadosExibidos) {
            Bundle dados = getIntent().getExtras();

            if (dados != null) {

                if (dados.containsKey("irParaProfile")) {
                    irParaProfile = dados.getString("irParaProfile");
                }

                if (dados.containsKey("tipoPostagem")) {
                    tipoPostagem = dados.getString("tipoPostagem");
                }

                if (dados.containsKey("novaGif")) {
                    novaUrlGif = dados.getString("novaGif");
                    exibirNovaPostagem();
                }

                if (dados.containsKey("novaPostagem")) {
                    novaUri = (Uri) dados.get("novaPostagem");
                    exibirNovaPostagem();
                }

                if (dados.containsKey("edicao")) {
                    edicao = true;

                    if (dados.containsKey("dadosPostagemEdicao")) {
                        dadosPostagemEdicao = (Postagem) dados.getSerializable("dadosPostagemEdicao");
                    }

                    exibirDadosEdicao();
                }
            }
            dadosExibidos = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        pauseExoPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeExoPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseExoPlayer();
    }

    private interface UploadCallback {
        void onUploadComplete(String urlPostagem);

        void timeStampRecuperado(long timeStampNegativo, String dataFormatada);

        void onUploadError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_postagem);
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

        limitarCaracteresDescricao();

        clickListeners();
    }

    private void exibirDadosEdicao() {

        if (dadosPostagemEdicao != null) {

            if (dadosPostagemEdicao.getUrlPostagem() != null
                    && !dadosPostagemEdicao.getUrlPostagem().isEmpty()) {

                GlideCustomizado.fundoGlideEpilepsia(getApplicationContext(), dadosPostagemEdicao.getUrlPostagem(),
                        imgViewPostagem, android.R.color.transparent);
            }

            if (dadosPostagemEdicao.getDescricaoPostagem() != null
                    && !dadosPostagemEdicao.getDescricaoPostagem().isEmpty()) {
                edtTextDescricao.setText(dadosPostagemEdicao.getDescricaoPostagem());
                postagemReajustada.setDescricaoPostagem(dadosPostagemEdicao.getDescricaoPostagem());
            }
        }
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

    private void exibirNovaPostagem() {
        if (tipoPostagem != null
                && !tipoPostagem.isEmpty()) {
            switch (tipoPostagem) {
                case "imagem":
                    exibirImagem();
                    break;
                case "gif":
                    exibirGif();
                    break;
                case "video":
                    exibirVideo();
                    break;
                case "texto":
                    exibirTexto();
                    break;
            }
        } else {
            onBackPressed();
        }
    }

    private void salvarFoto() {
        if (novaUri != null) {
            //Nova foto
            exibirProgressDialog("upload");

            DatabaseReference salvarFotoRef = firebaseRef.child("postagens")
                    .child(idUsuario);

            String idNovaFoto = salvarFotoRef.push().getKey();

            uparImagemNoStorage(novaUri, new UploadCallback() {

                String idFotoAtual = idNovaFoto;
                HashMap<String, Object> dadosFotoAtual = new HashMap<>();
                String descricao = edtTextDescricao.getText().toString();

                @Override
                public void onUploadComplete(String urlFoto) {

                    if (urlFoto != null && !urlFoto.isEmpty()) {
                        dadosFotoAtual.put("idPostagem", idFotoAtual);
                        dadosFotoAtual.put("idDonoPostagem", idUsuario);
                        dadosFotoAtual.put("tipoPostagem", tipoPostagem);
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
                ToastCustomizado.toastCustomizadoCurto("DIFERENTE", getApplicationContext());
                postagemReajustada.setDescricaoPostagem(descricaoAtual);

                ToastCustomizado.toastCustomizadoCurto("EDICAO", getApplicationContext());

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
            } else {
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

    private void exibirImagem() {

        if (novaUri != null) {
            imgViewPostagem.setImageURI(novaUri);
        }
        cardViewConfigPostagem.setVisibility(View.VISIBLE);
        imgViewPostagem.setVisibility(View.VISIBLE);
    }

    private void exibirGif() {

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (epilepsia) {
                    GlideCustomizado.montarGlideMensagemEpilepsia(getApplicationContext(),
                            novaUrlGif, imgViewGifNewPostagem, android.R.color.transparent);
                } else {
                    GlideCustomizado.montarGlideMensagem(getApplicationContext(),
                            novaUrlGif, imgViewGifNewPostagem, android.R.color.transparent);
                }
                cardViewConfigPostagem.setVisibility(View.VISIBLE);
                imgViewGifNewPostagem.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void exibirVideo() {
        if (novaUri != null) {
            exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
            iniciarExoPlayer(novaUri);
        }
        framePreviewPostagem.setVisibility(View.VISIBLE);
    }

    private void exibirTexto() {

    }

    private void inicializandoComponentes() {
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        imgViewPostagem = findViewById(R.id.imgViewPostagem);
        edtTextDescricao = findViewById(R.id.edtTextDescricaoPostagem);
        txtViewLimiteDescricao = findViewById(R.id.txtViewLimiteDescricaoPostagem);
        btnSalvar = findViewById(R.id.btnSalvarPostagem);
        imgViewGifNewPostagem = findViewById(R.id.imgViewGifNewPostagem);
        cardViewConfigPostagem = findViewById(R.id.cardViewConfigPostagem);
        framePreviewPostagem = findViewById(R.id.framePreviewPostagem);
        styledPlayer = findViewById(R.id.styledPlayerPostagem);
        spinProgressBarExo = findViewById(R.id.spinProgressBarExo);
    }

    private void pauseExoPlayer() {
        if (exoPlayer != null) {
            if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                // Aguardar até que o player esteja pronto para reprodução
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            // O ExoPlayer está pronto para reprodução, então pausar
                            exoPlayer.pause();
                            exoPlayer.setPlayWhenReady(false);
                            exoPlayer.removeListener(this);
                        }
                    }
                });
            } else {
                // O ExoPlayer não está em buffering, então pausar imediatamente
                exoPlayer.pause();
                exoPlayer.setPlayWhenReady(false);
            }
        }
    }

    private void resumeExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.play();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void releaseExoPlayer() {
        if (exoPlayer != null) {
            if (listenerExo != null) {
                exoPlayer.removeListener(listenerExo);
            }
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void adicionarListenerExoPlayer() {
        listenerExo = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == Player.STATE_READY) {
                    // O vídeo está pronto para reprodução, você pode iniciar a reprodução automática aqui
                    //*ToastCustomizado.toastCustomizadoCurto("READY", context);
                    exoPlayer.setPlayWhenReady(true);
                    spinProgressBarExo.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_BUFFERING) {
                    //*ToastCustomizado.toastCustomizadoCurto("BUFFERING", context);
                    // O vídeo está em buffer, você pode mostrar um indicador de carregamento aqui
                    spinProgressBarExo.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    //* ToastCustomizado.toastCustomizadoCurto("ENDED", context);
                    // O vídeo chegou ao fim, você pode executar ações após a conclusão do vídeo aqui
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
            }
        };

        exoPlayer.addListener(listenerExo);
    }

    private void removerListenerExoPlayer() {
        if (listenerExo != null) {
            //*ToastCustomizado.toastCustomizadoCurto("Removido listener", context);
            exoPlayer.removeListener(listenerExo);
        }
    }

    public void iniciarExoPlayer(@NonNull Uri uriVideo) {
        //Verificação garante que o vídeo não seja montado novamente
        //se ele já estiver em reprodução.
        if (exoPlayer != null
                && styledPlayer.getPlayer() != null &&
                exoPlayer.getMediaItemCount() != -1
                && exoPlayer.getMediaItemCount() > 0) {
            return;
        }

        removerListenerExoPlayer();

        // Configura o ExoPlayer com a nova fonte de mídia para o vídeo
        exoPlayer.setMediaItem(MediaItem.fromUri(uriVideo.toString()));

        // Vincula o ExoPlayer ao StyledPlayerView
        styledPlayer.setPlayer(exoPlayer);

        // Faz com que o vídeo se repita quando ele acabar
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

        // Trata do carregamento e da inicialização do vídeo
        adicionarListenerExoPlayer();

        // Indica para o exoPlayer que ele está com a view e a mídia configurada.
        exoPlayer.prepare();

        //Controla a exibição dos botões do styled.
        styledPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isControllerVisible) {
                    styledPlayer.hideController();
                    styledPlayer.setUseController(false);
                    isControllerVisible = false;
                } else {
                    styledPlayer.setUseController(true);
                    styledPlayer.showController();
                    isControllerVisible = true;
                }
            }
        });
    }

    private void pararExoPlayer() {

        //*Detached
        //Remove o listener do exoPlayer
        removerListenerExoPlayer();
        //Para a reprodução.
        exoPlayer.stop();
        //Limpa a mídia do exoPlayer.
        exoPlayer.clearMediaItems();
        //Volta para o início do vídeo.
        exoPlayer.seekToDefaultPosition();
        //Diz para o exoPlayer que ele não está pronto.
        exoPlayer.setPlayWhenReady(false);
        //Desvincula o exoPlayer anterior.
        styledPlayer.setPlayer(null);

        //Oculta os controladores do styled.
        styledPlayer.hideController();
        styledPlayer.setUseController(false);
        isControllerVisible = false;
    }
}