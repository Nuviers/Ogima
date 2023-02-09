package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterContato;
import com.example.ogima.adapter.AdapterMensagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.model.Wallpaper;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;


import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class ConversaActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private Bundle dados;
    private Toolbar toolbarConversa;
    private ImageButton imgBtnBackConversa, imgButtonEnviarFotoChat,
            imgBtnVideoCall, imgBtnVoiceCall, imgBtnConfigsChat;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Usuario usuarioDestinatario;
    private Contatos contatoDestinatario;
    private EditText edtTextMensagemChat;
    private RecyclerView recyclerMensagensChat;
    //Variáveis para data
    private DateFormat dateFormat;
    private Date date;
    private String localConvertido;
    private Locale current;
    private AdapterMensagem adapterMensagem;
    private ChildEventListener childEventListener;

    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    private StorageReference imagemRef, videoRef;
    private StorageReference storageRef;
    private static final int SELECAO_CAMERA = 100,
            SELECAO_GALERIA = 200,
            SELECAO_GIF = 300,
            SELECAO_VIDEO = 400,
            SELECAO_DOCUMENTO = 500,
            SELECAO_MUSICA = 600,
            SELECAO_AUDIO = 700,
            REQUEST_AUDIO_PERMISSION = 701;
    private String selecionadoCamera, selecionadoGaleria;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private ProgressDialog progressDialog;
    private ImageButton imgButtonSheetAudio, imgButtonEnviarMensagemChat;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerDuration;

    //Cronometro
    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;
    private Handler handler;

    //BottomSheet
    private BottomSheetDialog bottomSheetDialog, bottomSheetDialogWallpaper,
            bottomSheetDialogApagarConversa;
    private TextView txtViewTempoAudio, txtViewNomeDestinatario;
    private ImageView imgViewFotoDestinatario;
    private ImageButton imgButtonCancelarAudio, imgButtonEnviarAudio,
            imgButtonGravarAudio, imgButtonStopAudio, imgButtonPlayAudio,
            imgButtonPauseAudio;

    //BottomSheetWallpaper
    private TextView txtViewDialogOnlyChat, txtViewDialogAllChats;

    private String scrollLast;
    private LinearLayoutManager linearLayoutManager;
    private String somenteInicio;

    private ImageView imgViewWallpaperChat;

    private ImageButton imgBtnScrollLastMsg, imgBtnScrollFirstMsg;
    private TextView txtViewDialogApagaConversa, txtViewDialogApagaConversMidia;

    private DatabaseReference conversaAtualRef, wallpaperChatAtualRef,
            contadorMensagensAtuaisRef, wallpaperPrivadoRef, wallpaperGlobalRef,
            dadosAtuaisRef, salvarMensagemRef, recuperarMensagensRef,
            remetenteTalkKeyRef, destinatarioTalkKeyRef, remetenteTalkKeyRefV2,
            destinatarioTalkKeyRefV2, verificaContadorRef, verificaContadorDestinatarioRef;

    private Boolean exibirToast = true;
    private String voltarChatFragment;

    private FragmentManager fm = getFragmentManager();

    //Busca mensagens
    private Toolbar toolbarConversaSearch;
    private MaterialSearchView materialSearchConversa;
    private List<Mensagem> listaMensagemBuscada = new ArrayList<>();

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener;

    private Query queryRecuperaMensagem;
    private Query queryRecuperaMensagemFiltrada;
    private FirebaseRecyclerOptions<Mensagem> options;
    private PopupMenu popupMenuConfig, popupMenuMidias;
    private JitsiMeetUserInfo infosUserVideo, infosUserVoz;
    private JitsiMeetConferenceOptions configChamadaVideo, configChamadaVoz;
    private JitsiMeetConferenceOptions.Builder builderVideo, builderVoz;

    public Boolean exibirPermissaoNegada = false;

    @Override
    protected void onStart() {
        super.onStart();

        adapterMensagem.startListening();

        buscarMensagens();
        verificaWallpaper();

        //Configura lógica de pesquisa de mensagens.
        configurarMaterialSearchView();

        if (edtTextMensagemChat.getOnFocusChangeListener() == null) {
            edtTextMensagemChat.setOnFocusChangeListener(this::onFocusChange);
        }

        //Cuida da lógica do scroll.
        logicaScroll();
    }

    @Override
    protected void onStop() {
        super.onStop();

        removeChildEventListener(recuperarMensagensRef, childEventListener);

        adapterMensagem.stopListening();

        //Remove foco do editText, bottomSheet, materialSearchView e do scrollListener.
        removerFoco();
    }

    @Override
    protected void onPause() {
        super.onPause();

        running = false;
        wasRunning = running;

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (wasRunning) {
            running = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchview_conversa, menu);
        MenuItem item = menu.findItem(R.id.menu_icon_search_conversa);
        materialSearchConversa.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);
        inicializandoComponentes();
        setSupportActionBar(toolbarConversaSearch);

        toolbarConversaSearch.setTitle("");
        //Oculta o texto da toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurando data de acordo com local do usuário.
        current = getResources().getConfiguration().locale;
        localConvertido = localConvertido.valueOf(current);

        //Validar permissões necessárias para adição de fotos.
        //*Permissao.validarPermissoes(permissoesNecessarias, ConversaActivity.this, 17);

        solicitaPermissoes(null);

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        imgBtnBackConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        dados = getIntent().getExtras();

        if (dados != null) {
            contatoDestinatario = (Contatos) dados.getSerializable("contato");
            usuarioDestinatario = (Usuario) dados.getSerializable("usuario");
            voltarChatFragment = dados.getString("voltarChatFragment");
        }

        //Referências do usuário atual.
        referenciasUsuarioAtual();

        if (usuarioDestinatario != null) {
            //Referência do usuário atual com o usuário selecionado, também inclui a lógica
            //do query para o firebaseAdapter
            referenciasDestinatario();
            //Informações do usuário selecionado.
            infosDestinatario();
        }

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        bottomSheetDialogWallpaper = new BottomSheetDialog(ConversaActivity.this);
        bottomSheetDialogWallpaper.setContentView(R.layout.bottom_sheet_dialog_wallpaper);

        bottomSheetDialogApagarConversa = new BottomSheetDialog(ConversaActivity.this);
        bottomSheetDialogApagarConversa.setContentView(R.layout.bottom_sheet_dialog_apagar_conversa);

        bottomSheetDialog = new BottomSheetDialog(ConversaActivity.this);
        bottomSheetDialog.setContentView(R.layout.audio_bottom_sheet_dialog);

        imgButtonCancelarAudio = bottomSheetDialog.findViewById(R.id.imgButtonCancelarAudio);
        imgButtonEnviarAudio = bottomSheetDialog.findViewById(R.id.imgButtonEnviarAudio);
        imgButtonGravarAudio = bottomSheetDialog.findViewById(R.id.imgButtonGravarAudio);
        imgButtonStopAudio = bottomSheetDialog.findViewById(R.id.imgButtonStopAudio);
        imgButtonPlayAudio = bottomSheetDialog.findViewById(R.id.imgButtonPlayAudio);
        imgButtonPauseAudio = bottomSheetDialog.findViewById(R.id.imgButtonPauseAudio);
        txtViewTempoAudio = bottomSheetDialog.findViewById(R.id.txtViewTempoAudio);

        //Exclui audio local anterior
        excluirAudioAnterior();

        //Timer do audio
        if (savedInstanceState != null) {
            seconds
                    = savedInstanceState
                    .getInt("seconds");
            running
                    = savedInstanceState
                    .getBoolean("running");
            wasRunning
                    = savedInstanceState
                    .getBoolean("wasRunning");
        }

        //Abrir layout suspenso para gravar áudio
        imgButtonSheetAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Verifica permissões de áudio e armazenamento local
                if (checkRecordingPermission()) {
                    //Todas funções de tratamento de áudio.
                    funcoesAudio();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Aceite as permissões para que seja possível gravar seu áudio", getApplicationContext());
                    checkRecordingPermission();
                }
            }
        });

        //Configurações do menu superior direito (Wallpaper/ApagarConversa).
        configuracoesMenuSuperior();

        //Configurações do menu inferior esquerdo (Envio de mídias).
        configuracoesMenuMidias();

        imgButtonEnviarMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensagem();
            }
        });

        // Estava aqui o addonscrolllistener e a variável acima e somente inicio igual a sim


        //Configurando recycler
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagensChat.setLayoutManager(linearLayoutManager);
        if (adapterMensagem != null) {
        } else {
            adapterMensagem = new AdapterMensagem(getApplicationContext(), options);
        }
        recyclerMensagensChat.setAdapter(adapterMensagem);

        //Informações do usuário armazenado nesses JitsiMeetUserInfo.
        infosUserVideo = new JitsiMeetUserInfo();
        infosUserVoz = new JitsiMeetUserInfo();

        builderVideo = new JitsiMeetConferenceOptions.Builder();
        builderVoz = new JitsiMeetConferenceOptions.Builder();

        //Configurações da chamada de vídeo pelo JitsiMeet.
        imgBtnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chamadaDeVideo();
            }
        });

        //Configurações da chamada de voz pelo JitsiMeet.
        imgBtnVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chamadaDeVoz();
            }
        });

        rolagemScrollManual();
    }

    //Métodos
    private void verificaWallpaper() {
        wallpaperPrivadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Wallpaper wallpaper = snapshot.getValue(Wallpaper.class);
                    if (wallpaper.getUrlWallpaper() != null) {
                        GlideCustomizado.montarGlideFoto(
                                getApplicationContext(),
                                wallpaper.getUrlWallpaper(),
                                imgViewWallpaperChat,
                                android.R.color.transparent);
                    }
                } else {
                    wallpaperGlobalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Wallpaper wallpaperAll = snapshot.getValue(Wallpaper.class);
                                //Wallpaper definido para todos chats
                                if (wallpaperAll.getWallpaperGlobal() != null) {
                                    if (wallpaperAll.getWallpaperGlobal().equals("sim")) {
                                        GlideCustomizado.montarGlideFoto(
                                                getApplicationContext(),
                                                wallpaperAll.getUrlGlobalWallpaper(),
                                                imgViewWallpaperChat,
                                                android.R.color.transparent);
                                    } else {
                                        imgViewWallpaperChat.setImageResource(R.drawable.wallpaperwaifutwo);
                                    }
                                }
                            } else {
                                //Não existe nenhum wallpaper definido
                                wallpaperGlobalRef.child("wallpaperGlobal").setValue("não");
                                imgViewWallpaperChat.setImageResource(R.drawable.wallpaperwaifutwo);
                            }
                            wallpaperGlobalRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                wallpaperPrivadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void infosDestinatario() {
        if (usuarioDestinatario.getExibirApelido().equals("sim")) {
            txtViewNomeDestinatario.setText(usuarioDestinatario.getApelidoUsuario());
        } else {
            txtViewNomeDestinatario.setText(usuarioDestinatario.getNomeUsuario());
        }

        //Verifica se usuário atual tem epilpesia, para ambos resultados essa classe
        //trata da exibição da foto do usuário conforme o necessário.
        VerificaEpilpesia.verificarEpilpesiaSelecionado(getApplicationContext(),
                usuarioDestinatario, imgViewFotoDestinatario);
    }

    private void excluirAudioAnterior() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDirectory, "audioTemp" + ".mp3");
        Uri uriFile = Uri.fromFile(new File(file.getPath()));

        File fdelete = new File(uriFile.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //ToastCustomizado.toastCustomizadoCurto("Áudio descartado com sucesso",getApplicationContext());
                //System.out.println("file Deleted :" + uriFile.getPath());
            } else {
                //ToastCustomizado.toastCustomizadoCurto("Erro ao descartar áudio, tente novamente",getApplicationContext());
                //System.out.println("file not Deleted :" + uriFile.getPath());
            }
        }
    }

    //Timer do áudio
    private void runTimer() {

        handler
                = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                //int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Formatado com minutos e segundos com limite de 5 minutos de áudio
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%02d:%02d",
                                minutes, secs);

                // Exibe o timer em um textView
                txtViewTempoAudio.setText(time);

                if (minutes == 5) {
                    pararAudio();
                }

                // Enquanto ele está ativo, ele incrementa nos segundos.

                if (running) {
                    seconds++;
                }

                //Verifica a cada 1 segundo
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void enviarGif() {
        Giphy.INSTANCE.configure(ConversaActivity.this, "qQg4j9NKDfl4Vqh84iaTcQEMfZcH5raY", false);
        GPHSettings gphSettings = new GPHSettings();
        GiphyDialogFragment gdl = GiphyDialogFragment.Companion.newInstance(gphSettings);
        gdl.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
            @Override
            public void onGifSelected(@NonNull Media media, @Nullable String s, @NonNull GPHContentType gphContentType) {
                onGifSelected(media);
            }

            private void onGifSelected(Media media) {
                Image image = media.getImages().getFixedWidth();
                String gif_url = image.getGifUrl();
                progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                progressDialog.show();

                HashMap<String, Object> dadosMensagem = new HashMap<>();
                dadosMensagem.put("tipoMensagem", "gif");
                dadosMensagem.put("idRemetente", idUsuario);
                dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
                dadosMensagem.put("conteudoMensagem", gif_url);

                if (localConvertido.equals("pt_BR")) {
                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                    date = new Date();
                    String novaData = dateFormat.format(date);
                    dadosMensagem.put("dataMensagem", novaData);
                    dadosMensagem.put("dataMensagemCompleta", date);
                    String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                    dadosMensagem.put("nomeDocumento", replaceAll + ".gif");
                } else {
                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                    date = new Date();
                    String novaData = dateFormat.format(date);
                    dadosMensagem.put("dataMensagem", novaData);
                    dadosMensagem.put("dataMensagemCompleta", date);
                    String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                    dadosMensagem.put("nomeDocumento", replaceAll + ".gif");
                }

                salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                    atualizarContador();
                                    progressDialog.dismiss();
                                    edtTextMensagemChat.setText("");
                                } else {
                                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                    progressDialog.dismiss();
                                }
                            }
                        });

                salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                    edtTextMensagemChat.setText("");
                                }
                            }
                        });

                scrollLast = "sim";
                somenteInicio = null;

            }

            @Override
            public void onDismissed(@NonNull GPHContentType gphContentType) {

            }

            @Override
            public void didSearchTerm(@NonNull String s) {

            }
        });
        gdl.show(ConversaActivity.this.getSupportFragmentManager(), "this");
    }

    private void enviarVideo() {
        Matisse.from(ConversaActivity.this)
                .choose(MimeType.ofVideo())
                .countable(true)
                .maxSelectable(1)
                .showSingleMediaType(true)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(SELECAO_VIDEO);
    }

    private void buscarMensagens() {

        childEventListener = recuperarMensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);

                if (somenteInicio != null) {
                    if (somenteInicio.equals("sim")) {
                        recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                    }
                }

                //Verifica se o usuário atual é o remetente
                if (adapterMensagem.stringTeste != null) {
                    //Se é diferente de nulo ele caiu como remetente
                    if (adapterMensagem.stringTeste.equals("sim")) {
                        //ToastCustomizado.toastCustomizadoCurto("Remetente",getApplicationContext());
                        recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                        somenteInicio = null;
                    }
                } else {
                    if (scrollLast != null) {
                        if (scrollLast.equals("sim")) {
                            recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                            somenteInicio = null;
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void enviarMensagem() {

        if (!edtTextMensagemChat.getText().toString().isEmpty()) {

            String conteudoMensagem = edtTextMensagemChat.getText().toString();
            HashMap<String, Object> dadosMensagem = new HashMap<>();
            dadosMensagem.put("tipoMensagem", "texto");
            dadosMensagem.put("idRemetente", idUsuario);
            dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
            dadosMensagem.put("conteudoMensagem", conteudoMensagem);

            edtTextMensagemChat.setText("");

            if (localConvertido.equals("pt_BR")) {
                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                date = new Date();
                String novaData = dateFormat.format(date);
                dadosMensagem.put("dataMensagem", novaData);
                dadosMensagem.put("dataMensagemCompleta", date);
            } else {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                date = new Date();
                String novaData = dateFormat.format(date);
                dadosMensagem.put("dataMensagem", novaData);
                dadosMensagem.put("dataMensagemCompleta", date);
            }

            salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                atualizarContador();
                            }
                        }
                    });

            salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });

            scrollLast = "sim";
            somenteInicio = null;

        }
    }

    private void atualizarContador() {

        verificaContadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem mensagem1 = snapshot.getValue(Mensagem.class);
                    ToastCustomizado.toastCustomizadoCurto("Total " + mensagem1.getTotalMensagens(), getApplicationContext());
                    verificaContadorRef.child("totalMensagens").setValue(mensagem1.getTotalMensagens() + 1);
                } else {
                    ToastCustomizado.toastCustomizadoCurto("primeiro", getApplicationContext());
                    verificaContadorRef.child("totalMensagens").setValue(1);
                }
                verificaContadorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        verificaContadorDestinatarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem mensagemDestinatario = snapshot.getValue(Mensagem.class);
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(mensagemDestinatario.getTotalMensagens() + 1);
                } else {
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(1);
                }
                verificaContadorDestinatarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializandoComponentes() {
        toolbarConversa = findViewById(R.id.toolbarConversa);
        imgBtnBackConversa = findViewById(R.id.imgBtnBackConversa);
        edtTextMensagemChat = findViewById(R.id.edtTextMensagemChat);
        imgButtonEnviarMensagemChat = findViewById(R.id.imgButtonEnviarMensagemChat);
        imgButtonEnviarFotoChat = findViewById(R.id.imgButtonEnviarFotoChat);
        imgBtnVideoCall = findViewById(R.id.imgBtnVideoCall);
        imgBtnVoiceCall = findViewById(R.id.imgBtnVoiceCall);
        imgBtnConfigsChat = findViewById(R.id.imgBtnConfigsChat);
        recyclerMensagensChat = findViewById(R.id.recyclerMensagensChat);
        imgButtonSheetAudio = findViewById(R.id.imgButtonSheetAudio);
        txtViewNomeDestinatario = findViewById(R.id.txtViewNomeDestinatario);
        imgViewFotoDestinatario = findViewById(R.id.imgViewFotoDestinatario);
        imgViewWallpaperChat = findViewById(R.id.imgViewWallpaperChat);

        imgBtnScrollLastMsg = findViewById(R.id.imgBtnScrollLastMsg);
        imgBtnScrollFirstMsg = findViewById(R.id.imgBtnScrollFirstMsg);

        toolbarConversaSearch = findViewById(R.id.toolbarConversaSearch);
        materialSearchConversa = findViewById(R.id.materialSearchConversa);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (resultCode == RESULT_OK && requestCode == SELECAO_GALERIA) {
            try {
                switch (requestCode) {
                    //Seleção pela galeria
                    case SELECAO_GALERIA:
                        String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
                        selecionadoGaleria = "sim";
                        destinoArquivo += ".jpg";
                        final Uri localImagemFotoSelecionada = data.getData();
                        //*Chamando método responsável pela estrutura do U crop
                        openCropActivity(localImagemFotoSelecionada, Uri.fromFile(new File(getCacheDir(), destinoArquivo)));
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK || requestCode == 101 && resultCode == RESULT_OK) {
            try {
                if (selecionadoCamera != null) {
                    selecionadoCamera = null;
                    Uri uri = data.getData();
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                } else if (selecionadoGaleria != null) {
                    Uri imagemCortada = UCrop.getOutput(data);
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemCortada);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    selecionadoGaleria = null;
                }

                //Recupera dados da imagem para o firebase
                byte[] dadosImagem = baos.toByteArray();
                //Adicionado dia 21/12/2022 - não sei se realmente precisa,
                //caso ocorra algum erro verifique essa linha de código.VV
                baos.close();
                //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                progressDialog.show();
                String nomeRandomico = UUID.randomUUID().toString();
                imagemRef = storageRef.child("mensagens")
                        .child("fotos")
                        .child(idUsuario)
                        .child(usuarioDestinatario.getIdUsuario())
                        .child("foto" + nomeRandomico + ".jpeg");
                //Verificando progresso do upload
                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar mensagem", getApplicationContext());
                                Uri url = task.getResult();
                                String urlNewPostagem = url.toString();

                                HashMap<String, Object> dadosMensagem = new HashMap<>();
                                dadosMensagem.put("tipoMensagem", "imagem");
                                dadosMensagem.put("idRemetente", idUsuario);
                                dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
                                dadosMensagem.put("conteudoMensagem", urlNewPostagem);

                                if (localConvertido.equals("pt_BR")) {
                                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosMensagem.put("dataMensagem", novaData);
                                    dadosMensagem.put("dataMensagemCompleta", date);
                                    String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                    dadosMensagem.put("nomeDocumento", replaceAll + ".jpg");
                                } else {
                                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosMensagem.put("dataMensagem", novaData);
                                    dadosMensagem.put("dataMensagemCompleta", date);
                                    String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                    dadosMensagem.put("nomeDocumento", replaceAll + ".jpg");
                                }

                                salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    atualizarContador();
                                                    progressDialog.dismiss();
                                                    edtTextMensagemChat.setText("");
                                                } else {
                                                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                                salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    edtTextMensagemChat.setText("");
                                                }
                                            }
                                        });

                                scrollLast = "sim";
                                somenteInicio = null;
                            }
                        });
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (requestCode == SELECAO_VIDEO && resultCode == RESULT_OK) {
            progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
            progressDialog.show();
            String nomeRandomico = UUID.randomUUID().toString();

            //Caminho para o storage
            videoRef = storageRef
                    .child("mensagens")
                    .child("videos")
                    .child(idUsuario)
                    .child(usuarioDestinatario.getIdUsuario())
                    .child("video" + nomeRandomico + ".mp4");

            String path = String.valueOf(Matisse.obtainResult(data).get(0));
            Uri videoUri;
            videoUri = Uri.parse(path);
            UploadTask uploadTask = videoRef.putFile(videoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem, tente novamente", getApplicationContext());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    videoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Uri url = task.getResult();
                            String urlNewPostagem = url.toString();

                            HashMap<String, Object> dadosMensagem = new HashMap<>();
                            dadosMensagem.put("tipoMensagem", "video");
                            dadosMensagem.put("idRemetente", idUsuario);
                            dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
                            dadosMensagem.put("conteudoMensagem", urlNewPostagem);

                            if (localConvertido.equals("pt_BR")) {
                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                date = new Date();
                                String novaData = dateFormat.format(date);
                                dadosMensagem.put("dataMensagem", novaData);
                                dadosMensagem.put("dataMensagemCompleta", date);
                                String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                                String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                dadosMensagem.put("nomeDocumento", replaceAll + ".mp4");
                            } else {
                                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                date = new Date();
                                String novaData = dateFormat.format(date);
                                dadosMensagem.put("dataMensagem", novaData);
                                dadosMensagem.put("dataMensagemCompleta", date);
                                String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                dadosMensagem.put("nomeDocumento", replaceAll + ".mp4");
                            }

                            salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                atualizarContador();
                                                progressDialog.dismiss();
                                                edtTextMensagemChat.setText("");
                                            } else {
                                                //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });

                            salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                edtTextMensagemChat.setText("");
                                            }
                                        }
                                    });

                            scrollLast = "sim";
                            somenteInicio = null;
                        }
                    });
                }
            });
        } else if (requestCode == SELECAO_DOCUMENTO && resultCode == RESULT_OK) {
            if (data != null) {
                //final Uri localdoc = data.getData();

                //Tentativa de capturar o nome
                //File myFile = new File(localdoc.toString());
                //String path = myFile.getAbsolutePath();
                //String path = localdoc.getPath().toString();
                //String path  = new File(localdoc.toString()).getName();
                //String path = localdoc.getPath();
                //String extension;
                //ContentResolver contentResolver = getContentResolver();
                //MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                //extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(files.get(0).getUri()));
                //ToastCustomizado.toastCustomizado("doc " + path, getApplicationContext());
                //

                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                String path = files.get(0).getName();

                progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                progressDialog.show();
                //String nomeRandomico = UUID.randomUUID().toString();
                imagemRef = storageRef.child("mensagens")
                        .child("documentos")
                        .child(idUsuario)
                        .child(usuarioDestinatario.getIdUsuario())
                        //.child("documento" + nomeRandomico + "." + extension);
                        .child(path);
                UploadTask uploadTask = imagemRef.putFile(files.get(0).getUri());
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar mensagem", getApplicationContext());
                                Uri url = task.getResult();
                                String urlNewPostagem = url.toString();


                                HashMap<String, Object> dadosMensagem = new HashMap<>();
                                dadosMensagem.put("tipoMensagem", "documento");
                                dadosMensagem.put("tipoArquivo", files.get(0).getMimeType());
                                //Pega o tipo do arquivo se é pdf,doc etc...
                                //dadosMensagem.put("nomeDocumento", "doc"+nomeRandomico+"."+extension);
                                dadosMensagem.put("nomeDocumento", path);
                                dadosMensagem.put("idRemetente", idUsuario);
                                dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
                                dadosMensagem.put("conteudoMensagem", urlNewPostagem);

                                if (localConvertido.equals("pt_BR")) {
                                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosMensagem.put("dataMensagem", novaData);
                                    dadosMensagem.put("dataMensagemCompleta", date);
                                } else {
                                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosMensagem.put("dataMensagem", novaData);
                                    dadosMensagem.put("dataMensagemCompleta", date);
                                }

                                salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    atualizarContador();
                                                    progressDialog.dismiss();
                                                    edtTextMensagemChat.setText("");
                                                } else {
                                                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                                salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    edtTextMensagemChat.setText("");
                                                }
                                            }
                                        });

                                scrollLast = "sim";
                                somenteInicio = null;
                            }
                        });
                    }
                });
            }
        } else if (requestCode == SELECAO_MUSICA && resultCode == RESULT_OK) {


            if (data != null) {
                //final Uri localdoc = data.getData();

                //Tentativa de capturar o nome
                //File myFile = new File(localdoc.toString());
                //String path = myFile.getAbsolutePath();
                //String path = localdoc.getPath().toString();
                //String path  = new File(localdoc.toString()).getName();
                //String path = localdoc.getPath();
                //String extension;
                //ContentResolver contentResolver = getContentResolver();
                //MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                //extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(files.get(0).getUri()));
                //ToastCustomizado.toastCustomizado("musica " + path, getApplicationContext());
                //

                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                String path = files.get(0).getName();
                String duracao = formatarTimer(files.get(0).getDuration());

                progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                progressDialog.show();
                //String nomeRandomico = UUID.randomUUID().toString();
                imagemRef = storageRef.child("mensagens")
                        .child("musicas")
                        .child(idUsuario)
                        .child(usuarioDestinatario.getIdUsuario())
                        //.child("documento" + nomeRandomico + "." + extension);
                        .child(path);
                UploadTask uploadTask = imagemRef.putFile(files.get(0).getUri());
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar mensagem", getApplicationContext());
                                Uri url = task.getResult();
                                String urlNewPostagem = url.toString();

                                HashMap<String, Object> dadosMensagem = new HashMap<>();
                                dadosMensagem.put("tipoMensagem", "musica");
                                //dadosMensagem.put("nomeDocumento", "doc"+nomeRandomico+"."+extension);
                                dadosMensagem.put("nomeDocumento", path);
                                dadosMensagem.put("idRemetente", idUsuario);
                                dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
                                dadosMensagem.put("conteudoMensagem", urlNewPostagem);
                                dadosMensagem.put("duracaoMusica", duracao);

                                if (localConvertido.equals("pt_BR")) {
                                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosMensagem.put("dataMensagem", novaData);
                                    dadosMensagem.put("dataMensagemCompleta", date);
                                } else {
                                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosMensagem.put("dataMensagem", novaData);
                                    dadosMensagem.put("dataMensagemCompleta", date);
                                }

                                salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    atualizarContador();
                                                    progressDialog.dismiss();
                                                    edtTextMensagemChat.setText("");
                                                } else {
                                                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                                salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    edtTextMensagemChat.setText("");
                                                }
                                            }
                                        });

                                scrollLast = "sim";
                                somenteInicio = null;
                            }
                        });
                    }
                });
            }
        }
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(ConversaActivity.this);
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

    @Override
    public void onFocusChange(View view, boolean b) {

        switch (view.getId()) {
            case R.id.edtTextMensagemChat:
                if (b) {
                    imgButtonEnviarMensagemChat.setVisibility(View.VISIBLE);
                    materialSearchConversa.setQuery("", false);
                    materialSearchConversa.clearFocus();
                    materialSearchConversa.closeSearch();
                    //Dados da conversa sem filtragem.
                    conversaSemFiltragem();
                } else {
                    imgButtonEnviarMensagemChat.setVisibility(View.GONE);
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0) {
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord) {
                    ToastCustomizado.toastCustomizadoCurto("Permissão concedida", getApplicationContext());
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Permissão negada", getApplicationContext());
                }
            }
        }
    }

    private void rolagemScrollManual() {
        imgBtnScrollLastMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastCustomizado.toastCustomizadoCurto("Size last " + adapterMensagem.getItemCount(), getApplicationContext());
                recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                imgBtnScrollLastMsg.setVisibility(View.GONE);
                imgBtnScrollFirstMsg.setVisibility(View.VISIBLE);
            }
        });

        imgBtnScrollFirstMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerMensagensChat.scrollToPosition(0);
                imgBtnScrollFirstMsg.setVisibility(View.GONE);
                imgBtnScrollLastMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    private void gravarAudio() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(getRecordingFilePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
            mediaRecorder.start();
            seconds = 0;
            running = true;
            ToastCustomizado.toastCustomizadoCurto("Começando a gravação", getApplicationContext());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void pararAudio() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            running = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
            }
            mediaPlayerDuration = new MediaPlayer();
            mediaPlayerDuration.setDataSource(duracaoAudio());
            mediaPlayerDuration.prepare();
            Boolean duracaoteste = verificarSegundos(mediaPlayerDuration.getDuration());
            mediaPlayerDuration.release();
            if (duracaoteste) {
                ToastCustomizado.toastCustomizadoCurto("Duração do áudio deve ser maior que 2 segundos", getApplicationContext());
                bottomSheetDialog.cancel();
                excluirAudioAnterior();
                txtViewTempoAudio.setText("00:00");

                wasRunning = running;
                seconds = 0;

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
            ToastCustomizado.toastCustomizadoCurto("Áudio finalizado", getApplicationContext());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void executarAudio() {
        try {
            if (mediaPlayer != null) {

            } else {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(getRecordingFilePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                running = false;
                ToastCustomizado.toastCustomizadoCurto("Reproduzindo audio", getApplicationContext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isMicrophonePresent() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            return true;
        } else {
            return false;
        }
    }

    //Armazena localmente o audio temporario, logo em seguida é excluido.
    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDirectory, "audioTemp" + ".mp3");
        return file.getPath();
    }

    private boolean checkRecordingPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            requestRecordingPermission();
            return false;
        }
        return true;
    }

    private void requestRecordingPermission() {
        ActivityCompat.requestPermissions(ConversaActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle
            outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState
                .putInt("seconds", seconds);
        outState
                .putBoolean("running", running);
        outState
                .putBoolean("wasRunning", wasRunning);
    }

    private String formatarTimer(long milliSeconds) {
        String timerString = "";
        String secondString;

        int hours = (int) (milliSeconds / (1000 * 60 * 60));
        int minutes = (int) (milliSeconds % (1000 * 60 * 60) / (1000 * 60));
        int seconds = (int) (milliSeconds % (1000 * 60 * 60) % (1000 * 60) / 1000);

        if (hours > 0) {
            timerString = hours + ":";
        }

        if (seconds < 10) {
            secondString = "0" + seconds;
        } else {
            secondString = "" + seconds;
        }

        timerString = timerString + minutes + ":" + secondString;

        return timerString;
    }

    private String duracaoAudio() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDirectory, "audioTemp" + ".mp3");
        return file.getPath();
    }

    /*
    //Funciona porém tá no onStart agora
    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int totalItemCount = linearLayoutManager.getItemCount();
            int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

            //Exibe o botão de ir para última mensagem somente se o último item estiver visível.
            if (lastVisible == listaMensagem.size() - 1) {
                imgBtnScrollLastMsg.setVisibility(View.GONE);
                imgBtnScrollFirstMsg.setVisibility(View.VISIBLE);
            } else {
                imgBtnScrollFirstMsg.setVisibility(View.GONE);
                imgBtnScrollLastMsg.setVisibility(View.VISIBLE);
            }

            boolean endHasBeenReached = lastVisible + 5 >= totalItemCount;
            if (totalItemCount > 0 && endHasBeenReached) {
                //ToastCustomizado.toastCustomizadoCurto("Ultimo",getApplicationContext());
                scrollLast = "sim";
            } else {
                scrollLast = null;
            }
        }
    };
//Funciona porém tá no onStart agora
     */

    private boolean verificarSegundos(long milliSeconds) {
        Boolean verificaLimte;

        int seconds = (int) (milliSeconds % (1000 * 60 * 60) % (1000 * 60) / 1000);

        if (seconds <= 2) {
            verificaLimte = true;
        } else {
            verificaLimte = false;
        }

        return verificaLimte;
    }

    private void apagarSomenteConversa(Boolean apagarMidia) {

        conversaAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    progressDialog.setMessage("Apagando conversa, aguarde um momento");
                    progressDialog.setCancelable(true);
                    if (ConversaActivity.this.getWindow().getDecorView().isShown()) {
                        progressDialog.show();
                    }

                    //Apagando conversa
                    conversaAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            ToastCustomizado.toastCustomizadoCurto("Apagado conversa com sucesso", getApplicationContext());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            ToastCustomizado.toastCustomizadoCurto("Erro ao apagar conversa", getApplicationContext());
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    ToastCustomizado.toastCustomizadoCurto("Não existem mensagens nessa conversa ", getApplicationContext());
                }
                conversaAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        wallpaperChatAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Criar um alertdialog perguntando se o usuário deseja excluir o wallpaper também
                    progressDialog.setMessage("Excluindo wallpaper da conversa atual, aguarde um momento");
                    progressDialog.setCancelable(true);
                    if (ConversaActivity.this.getWindow().getDecorView().isShown()) {
                        progressDialog.show();
                    }
                    //Removendo wallpaper do chat atual
                    wallpaperChatAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            ToastCustomizado.toastCustomizadoCurto("Apagado wallpaper com sucesso", getApplicationContext());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            ToastCustomizado.toastCustomizadoCurto("Erro ao apagar wallpaper", getApplicationContext());
                        }
                    });
                } else {
                    progressDialog.dismiss();
                }
                wallpaperChatAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        contadorMensagensAtuaisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    progressDialog.setMessage("Finalizando ajustes, aguarde um momento");
                    progressDialog.setCancelable(true);
                    if (ConversaActivity.this.getWindow().getDecorView().isShown()) {
                        progressDialog.show();
                    }
                    //Removendo contador atual
                    contadorMensagensAtuaisRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                }
                contadorMensagensAtuaisRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (apagarMidia) {
            apagarConversaEMidia();
        }
            /*
        } else {
            finish();
        }
             */

    }

    private void apagarConversaEMidia() {
        File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + usuarioDestinatario.getIdUsuario());
        progressDialog.setMessage("Excluindo mídias da conversa de seu dispositivo, aguarde um momento");
        progressDialog.setCancelable(true);
        if (ConversaActivity.this.getWindow().getDecorView().isShown()) {
            progressDialog.show();
        }

        /*
        boolean caminhoexiste = caminhoDestino.exists();
        boolean canread = caminhoDestino.canRead();
        boolean canwrite = caminhoDestino.canWrite();
        ToastCustomizado.toastCustomizadoCurto("Caminho Existe " + caminhoexiste, getApplicationContext());
        ToastCustomizado.toastCustomizadoCurto("CanRead " + canread, getApplicationContext());
        ToastCustomizado.toastCustomizadoCurto("CanWrite " + canwrite, getApplicationContext());
         */

        if (caminhoDestino.exists()) {
            deleteRecursive(caminhoDestino);
            progressDialog.dismiss();
        } else {
            progressDialog.dismiss();
            ToastCustomizado.toastCustomizadoCurto("Arquivos não localizados em seu dispositivo", getApplicationContext());
        }

        //finish();
    }


    //Deleta as subpastas
    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        boolean deleted = fileOrDirectory.delete();

        if (deleted) {
            if (exibirToast) {
                exibirToast = false;
                ToastCustomizado.toastCustomizadoCurto("Arquivos excluídos de seu dispositivo com sucesso", getApplicationContext());
            }
        } else {
            if (exibirToast) {
                exibirToast = false;
                ToastCustomizado.toastCustomizadoCurto("Erro ao excluir arquivos do seu dispositivo", getApplicationContext());
            }
        }
    }

    private void removeChildEventListener(DatabaseReference reference, ChildEventListener
            childEventListener) {
        if (childEventListener != null) {
            reference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    private void removeValueEventListener(DatabaseReference reference, ValueEventListener
            valueEventListener) {
        if (valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    private void mensagemFiltrada(String dadoDigitado) {

        //Query usando firebaseAdapter é sensitivo a busca, os dados tem que estar escrito
        //igualmente ao que está no banco de dados.

        queryRecuperaMensagemFiltrada = firebaseRef.child("conversas").child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario()).orderByChild("conteudoMensagem")
                .startAt(dadoDigitado)
                .endAt(dadoDigitado + "\uf8ff");

        options =
                new FirebaseRecyclerOptions.Builder<Mensagem>()
                        .setQuery(queryRecuperaMensagemFiltrada, Mensagem.class)
                        .build();

        adapterMensagem.updateOptions(options);
    }

    private void conversaSemFiltragem() {

        queryRecuperaMensagem = firebaseRef.child("conversas").child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario());

        options =
                new FirebaseRecyclerOptions.Builder<Mensagem>()
                        .setQuery(queryRecuperaMensagem, Mensagem.class)
                        .build();

        adapterMensagem.updateOptions(options);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fm.popBackStack();

        //VVVVVVVVVVV funciona porem não é uma boa prática.
        /*
        if (dados != null) {
            if (voltarChatFragment != null) {
                voltarChatFragment = null;
                Intent intent = new Intent(getApplicationContext(), ChatInicioActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
           finish();
        }
         */
    }

    private void referenciasUsuarioAtual() {

        dadosAtuaisRef = firebaseRef.child("usuarios").child(idUsuario);

        wallpaperGlobalRef = firebaseRef.child("chatGlobalWallpaper")
                .child(idUsuario);

        salvarMensagemRef = firebaseRef.child("conversas");
    }

    private void logicaScroll() {
        if (recyclerViewOnScrollListener == null) {
            somenteInicio = "sim";

            recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

                    //Exibe o botão de ir para última mensagem somente se o último item estiver visível.
                    if (lastVisible == adapterMensagem.getItemCount() - 1) {
                        imgBtnScrollLastMsg.setVisibility(View.GONE);
                        imgBtnScrollFirstMsg.setVisibility(View.VISIBLE);
                    } else {
                        imgBtnScrollFirstMsg.setVisibility(View.GONE);
                        imgBtnScrollLastMsg.setVisibility(View.VISIBLE);
                    }

                    boolean endHasBeenReached = lastVisible + 5 >= totalItemCount;
                    if (totalItemCount > 0 && endHasBeenReached) {
                        //ToastCustomizado.toastCustomizadoCurto("Ultimo",getApplicationContext());
                        scrollLast = "sim";
                    } else {
                        scrollLast = null;
                    }
                }
            };
            //ToastCustomizado.toastCustomizadoCurto("Nulo",getApplicationContext());
            recyclerMensagensChat.addOnScrollListener(recyclerViewOnScrollListener);
        }
    }

    private void referenciasDestinatario() {

        //Passa o query com os dados do nó de conversas para o adapter
        queryRecuperaMensagem = firebaseRef.child("conversas").child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario());

        options =
                new FirebaseRecyclerOptions.Builder<Mensagem>()
                        .setQuery(queryRecuperaMensagem, Mensagem.class)
                        .build();
        //

        //Referências
        recuperarMensagensRef = firebaseRef.child("conversas")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        //Verifica se existe algum wallpaper para essa conversa
        wallpaperPrivadoRef = firebaseRef.child("chatWallpaper")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        wallpaperChatAtualRef = firebaseRef.child("chatWallpaper")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        contadorMensagensAtuaisRef = firebaseRef.child("contadorMensagens")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        conversaAtualRef = firebaseRef.child("conversas")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        remetenteTalkKeyRef = firebaseRef.child("keyConversation")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        destinatarioTalkKeyRef = firebaseRef.child("keyConversation")
                .child(usuarioDestinatario.getIdUsuario()).child(idUsuario);

        remetenteTalkKeyRefV2 = firebaseRef.child("keyConversation")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        destinatarioTalkKeyRefV2 = firebaseRef.child("keyConversation")
                .child(usuarioDestinatario.getIdUsuario()).child(idUsuario);

        verificaContadorRef = firebaseRef.child("contadorMensagens")
                .child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario());

        verificaContadorDestinatarioRef = firebaseRef.child("contadorMensagens")
                .child(usuarioDestinatario.getIdUsuario())
                .child(idUsuario);
    }


    //Chamada de Video
    private void chamadaDeVideo() {

        dadosUserAtualJitsi(infosUserVideo);

        remetenteTalkKeyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem mensagem = snapshot.getValue(Mensagem.class);

                    builderVideo
                            .setUserInfo(infosUserVideo)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setFeatureFlag("chat.enabled", false)
                            .setFeatureFlag("add-people.enabled", false)
                            .setFeatureFlag("invite.enabled", false)
                            .setFeatureFlag("meeting-name.enabled", false)
                            .setFeatureFlag("recording.enabled", false)
                            .setFeatureFlag("reactions.enabled", false)
                            .setFeatureFlag("settings.enabled", false)
                            .setFeatureFlag("server-url-change.enabled", false)
                            .setFeatureFlag("live-streaming.enabled", false)
                            .setFeatureFlag("help.enabled", false)
                            .setFeatureFlag("speakerstats.enabled", false)
                            .setFeatureFlag("prejoinpage.enabled", false)
                            .setRoom("Room " + mensagem.getTalkKey())
                            .setVideoMuted(false);
                    configChamadaVideo = builderVideo.build();
                    JitsiMeetActivity.launch(getApplicationContext(), configChamadaVideo);
                    //finish();
                } else {
                    String randomKey = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

                    remetenteTalkKeyRef.child("talkKey").setValue(randomKey + idUsuario);

                    destinatarioTalkKeyRef.child("talkKey").setValue(randomKey + idUsuario);

                    Mensagem mensagemNova = new Mensagem();
                    mensagemNova.setTalkKey(randomKey + idUsuario);

                    builderVideo
                            .setUserInfo(infosUserVideo)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setFeatureFlag("chat.enabled", false)
                            .setFeatureFlag("add-people.enabled", false)
                            .setFeatureFlag("invite.enabled", false)
                            .setFeatureFlag("meeting-name.enabled", false)
                            .setFeatureFlag("recording.enabled", false)
                            .setFeatureFlag("reactions.enabled", false)
                            .setFeatureFlag("settings.enabled", false)
                            .setFeatureFlag("server-url-change.enabled", false)
                            .setFeatureFlag("live-streaming.enabled", false)
                            .setFeatureFlag("help.enabled", false)
                            .setFeatureFlag("speakerstats.enabled", false)
                            .setFeatureFlag("prejoinpage.enabled", false)
                            .setRoom("Room " + mensagemNova.getTalkKey())
                            .setVideoMuted(false);
                    configChamadaVideo = builderVideo.build();
                    JitsiMeetActivity.launch(getApplicationContext(), configChamadaVideo);
                    //finish();
                }
                remetenteTalkKeyRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Chamada de Voz
    private void chamadaDeVoz() {

        dadosUserAtualJitsi(infosUserVoz);

        remetenteTalkKeyRefV2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem mensagem = snapshot.getValue(Mensagem.class);

                    builderVoz
                            .setUserInfo(infosUserVoz)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setFeatureFlag("chat.enabled", false)
                            .setFeatureFlag("add-people.enabled", false)
                            .setFeatureFlag("invite.enabled", false)
                            .setFeatureFlag("meeting-name.enabled", false)
                            .setFeatureFlag("video-mute.enabled", false)

                            .setFeatureFlag("recording.enabled", false)
                            .setFeatureFlag("reactions.enabled", false)
                            .setFeatureFlag("settings.enabled", false)
                            .setFeatureFlag("server-url-change.enabled", false)
                            .setFeatureFlag("live-streaming.enabled", false)
                            .setFeatureFlag("help.enabled", false)
                            .setFeatureFlag("speakerstats.enabled", false)
                            .setFeatureFlag("prejoinpage.enabled", false)
                            .setRoom("Room " + mensagem.getTalkKey())
                            .setVideoMuted(true);
                    configChamadaVoz = builderVoz.build();
                    JitsiMeetActivity.launch(getApplicationContext(), configChamadaVoz);
                    //finish();
                } else {
                    String randomKey = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                    remetenteTalkKeyRefV2.child("talkKey").setValue(randomKey + idUsuario);

                    destinatarioTalkKeyRefV2.child("talkKey").setValue(randomKey + idUsuario);

                    Mensagem mensagemNova = new Mensagem();
                    mensagemNova.setTalkKey(randomKey + idUsuario);

                    builderVoz
                            .setUserInfo(infosUserVoz)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setFeatureFlag("chat.enabled", false)
                            .setFeatureFlag("add-people.enabled", false)
                            .setFeatureFlag("invite.enabled", false)
                            .setFeatureFlag("meeting-name.enabled", false)
                            .setFeatureFlag("video-mute.enabled", false)
                            .setFeatureFlag("recording.enabled", false)
                            .setFeatureFlag("reactions.enabled", false)
                            .setFeatureFlag("settings.enabled", false)
                            .setFeatureFlag("server-url-change.enabled", false)
                            .setFeatureFlag("live-streaming.enabled", false)
                            .setFeatureFlag("help.enabled", false)
                            .setFeatureFlag("speakerstats.enabled", false)
                            .setFeatureFlag("prejoinpage.enabled", false)
                            .setRoom("Room " + mensagemNova.getTalkKey())
                            .setVideoMuted(true);
                    configChamadaVoz = builderVoz.build();
                    JitsiMeetActivity.launch(getApplicationContext(), configChamadaVoz);
                    //finish();
                }
                remetenteTalkKeyRefV2.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dadosUserAtualJitsi(JitsiMeetUserInfo jitsiMeetUserInfo) {

        dadosAtuaisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioLogado = snapshot.getValue(Usuario.class);
                    if (usuarioLogado.getExibirApelido().equals("sim")) {
                        jitsiMeetUserInfo.setDisplayName(usuarioLogado.getApelidoUsuario());
                    } else {
                        jitsiMeetUserInfo.setDisplayName(usuarioLogado.getNomeUsuario());
                    }

                    if (usuarioLogado.getEpilepsia().equals("Não")) {
                        try {
                            jitsiMeetUserInfo.setAvatar(new URL(usuarioLogado.getMinhaFoto()));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastCustomizado.toastCustomizadoCurto("Epilepsia", getApplicationContext());
                        transformarGifEmImagem();
                    }
                }
                dadosAtuaisRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void transformarGifEmImagem() {
        //Fazer uma lógica de verificar permissões para
        //poder salvar a gif no dispositivo do usuário salvando como png
        //e recuperar a url dessa imagem e exibir no jitsiMeetUserInfo.setAvatar(new URL());


    }

    private void solicitaPermissoes(String permissao) {
        //Se alguma permissão não foi aceita, então a seguinte lógica é acionada.
        if (!verificaPermissoes()) {

            exibirPermissaoNegada = false;

            if (permissao != null) {

                if (permissao.equals("camera")) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        // Permissão não concedida
                        if (!exibirPermissaoNegada) {
                            exibirPermissaoNegada = true;
                        }
                    }
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        if (!exibirPermissaoNegada) {
                            exibirPermissaoNegada = true;
                        }
                    }

                    if (exibirPermissaoNegada) {
                        ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", getApplicationContext());
                    }

                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        if (!exibirPermissaoNegada) {
                            exibirPermissaoNegada = true;
                        }
                    }
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        if (!exibirPermissaoNegada) {
                            exibirPermissaoNegada = true;
                        }
                    }

                    if (exibirPermissaoNegada) {
                        ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", getApplicationContext());
                    }
                }

                /*
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    ToastCustomizado.toastCustomizadoCurto("CAMERA DENIED", getApplicationContext());
                }

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    ToastCustomizado.toastCustomizadoCurto("WRITE DENIED", getApplicationContext());
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    ToastCustomizado.toastCustomizadoCurto("READ DENIED", getApplicationContext());
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    ToastCustomizado.toastCustomizadoCurto("MANAGE DENIED", getApplicationContext());
                }
                 */
            }
        }
    }

    private boolean verificaPermissoes() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissoesNecessarias) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 17);
            return false;
        }
        return true;
    }

    private void removerFoco() {

        if (bottomSheetDialogApagarConversa != null) {
            bottomSheetDialogApagarConversa.dismiss();
        }

        if (materialSearchConversa.getOnFocusChangeListener() != null) {
            materialSearchConversa.setOnQueryTextListener(null);
        }

        materialSearchConversa.setQuery("", false);

        if (edtTextMensagemChat.getOnFocusChangeListener() != null) {
            edtTextMensagemChat.clearFocus();
            edtTextMensagemChat.setOnFocusChangeListener(null);
        }

        if (recyclerViewOnScrollListener != null) {
            recyclerMensagensChat.removeOnScrollListener(recyclerViewOnScrollListener);
            recyclerViewOnScrollListener = null;
        }
    }

    private void configurarMaterialSearchView() {
        materialSearchConversa.setHint("Pesquisar mensagem");
        materialSearchConversa.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    String dadoDigitado = Normalizer.normalize(newText, Normalizer.Form.NFD);
                    dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    mensagemFiltrada(dadoDigitado);
                }
                return true;
            }
        });

        materialSearchConversa.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                //Dados da conversa sem filtragem.
                conversaSemFiltragem();
            }
        });
    }

    private void configuracoesMenuSuperior() {
        //Menu de configs do chat
        popupMenuConfig = new PopupMenu(getApplicationContext(), imgBtnConfigsChat);
        popupMenuConfig.getMenuInflater().inflate(R.menu.popup_menu_configs_chat, popupMenuConfig.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenuConfig.setForceShowIcon(true);
        }

        imgBtnConfigsChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenuConfig.show();
            }
        });

        popupMenuConfig.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.alterarWallpaper:
                        //Altera wallpaper para somente essa conversa ou todas conversas.
                        alterarWallpaperConversa();
                        break;
                    case R.id.apagarConversa:
                        //Apaga toda conversa + mídias locais ou somente a conversa.
                        apagarConversa();
                        break;
                }
                return false;
            }
        });
    }

    private void alterarWallpaperConversa() {

        bottomSheetDialogWallpaper.show();
        bottomSheetDialogWallpaper.setCancelable(true);

        txtViewDialogOnlyChat = bottomSheetDialogWallpaper.findViewById(R.id.txtViewDialogOnlyChat);
        txtViewDialogAllChats = bottomSheetDialogWallpaper.findViewById(R.id.txtViewDialogAllChats);

        txtViewDialogOnlyChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogWallpaper.dismiss();
                bottomSheetDialogWallpaper.cancel();
                Intent intent = new Intent(getApplicationContext(), MudarWallpaperActivity.class);
                intent.putExtra("wallpaperPlace", "onlyChat");
                intent.putExtra("usuarioDestinatario", usuarioDestinatario);
                startActivity(intent);
            }
        });

        txtViewDialogAllChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogWallpaper.dismiss();
                bottomSheetDialogWallpaper.cancel();
                Intent intent = new Intent(getApplicationContext(), MudarWallpaperActivity.class);
                intent.putExtra("wallpaperPlace", "allChats");
                intent.putExtra("usuarioDestinatario", usuarioDestinatario);
                startActivity(intent);
            }
        });
    }

    private void apagarConversa() {

        bottomSheetDialogApagarConversa.show();
        bottomSheetDialogApagarConversa.setCancelable(true);

        txtViewDialogApagaConversa = bottomSheetDialogApagarConversa.findViewById(R.id.txtViewDialogApagaConversa);
        txtViewDialogApagaConversMidia = bottomSheetDialogApagarConversa.findViewById(R.id.txtViewDialogApagaConversMidia);

        txtViewDialogApagaConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apagarSomenteConversa(false);
            }
        });

        txtViewDialogApagaConversMidia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apagarSomenteConversa(true);
            }
        });
    }

    private void configuracoesMenuMidias() {

        //Seleção de envio de arquivos - foto/camêra/gif/música/documento
        popupMenuMidias = new PopupMenu(getApplicationContext(), imgButtonEnviarFotoChat);
        popupMenuMidias.getMenuInflater().inflate(R.menu.popup_menu_anexo, popupMenuMidias.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenuMidias.setForceShowIcon(true);
        }

        imgButtonEnviarFotoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenuMidias.show();
            }
        });

        popupMenuMidias.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.anexoCamera:
                        solicitaPermissoes("camera");
                        //Chama o crop de camêra
                        selecionadoCamera = "sim";
                        if (!exibirPermissaoNegada) {
                            ImagePicker.Companion.with(ConversaActivity.this)
                                    .cameraOnly()
                                    .crop()                    //Crop image(Optional), Check Customization for more option
                                    .compress(1024)            //Final image size will be less than 1 MB(Optional)
                                    //.maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                                    .start(101);
                        }
                        return true;
                    case R.id.anexoGaleria:
                        solicitaPermissoes("galeria");
                        if (!exibirPermissaoNegada) {
                            //Passando a intenção de selecionar uma foto pela galeria
                            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            //Verificando se a intenção foi atendida com sucesso
                            if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                                startActivityForResult(i, SELECAO_GALERIA);
                            }
                        }
                        return true;
                    case R.id.anexoMusica:
                        solicitaPermissoes("musica");
                        if (!exibirPermissaoNegada) {
                            Intent intentMusica = new Intent(ConversaActivity.this, FilePickerActivity.class);
                            intentMusica.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                                    .setShowAudios(true)
                                    .setShowImages(false)
                                    .setShowVideos(false)
                                    .setShowFiles(false)
                                    .setMaxSelection(1)
                                    .setSkipZeroSizeFiles(true)
                                    .build());
                            startActivityForResult(intentMusica, SELECAO_MUSICA);
                        }
                        return true;
                    case R.id.anexoDocumento:
                        solicitaPermissoes("documento");
                        if (!exibirPermissaoNegada) {
                            Intent intentDoc = new Intent(ConversaActivity.this, FilePickerActivity.class);
                            intentDoc.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                                    .setShowFiles(true)
                                    .setShowImages(false)
                                    .setShowVideos(false)
                                    .setMaxSelection(1)
                                    .setSkipZeroSizeFiles(true)
                                    .build());
                            //intentDoc.addCategory(Intent.CATEGORY_OPENABLE);
                            //intentDoc.setType("application/*");
                            startActivityForResult(intentDoc, SELECAO_DOCUMENTO);
                        }
                        return true;
                    case R.id.anexoVideo:
                        solicitaPermissoes("video");
                        if (!exibirPermissaoNegada) {
                            enviarVideo();
                        }
                        return true;
                    case R.id.anexoGif:
                        enviarGif();
                        return true;
                }
                return false;
            }
        });
    }

    private void funcoesAudio() {

        imgButtonEnviarMensagemChat.setVisibility(View.GONE);
        edtTextMensagemChat.clearFocus();

        imgButtonGravarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                funcaoGravarAudio();
            }
        });

        imgButtonEnviarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarAudioSalvo();
            }
        });

        imgButtonPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executarAudio();
            }
        });

        imgButtonStopAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pararAudio();
            }
        });

        imgButtonCancelarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                funcaoCancelarAudio();
            }
        });

        bottomSheetDialog.show();
        bottomSheetDialog.setCancelable(false);
    }

    private void enviarAudioSalvo() {
        running = false;
        wasRunning = running;
        seconds = 0;
        txtViewTempoAudio.setText("00:00");
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");

        imagemRef = storageRef.child("mensagens")
                .child("audios")
                .child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario())
                .child("audio" + replaceAll + ".mp3");

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDirectory, "audioTemp" + ".mp3");

        Uri uriFile = Uri.fromFile(new File(file.getAbsolutePath()));

        //Verificando progresso do upload
        UploadTask uploadTask = imagemRef.putFile(uriFile);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                bottomSheetDialog.cancel();
                excluirAudioAnterior();
                //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        bottomSheetDialog.cancel();
                        ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar mensagem", getApplicationContext());
                        Uri url = task.getResult();
                        String urlNewPostagem = url.toString();

                        HashMap<String, Object> dadosMensagem = new HashMap<>();
                        dadosMensagem.put("tipoMensagem", "audio");
                        dadosMensagem.put("idRemetente", idUsuario);
                        dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
                        dadosMensagem.put("conteudoMensagem", urlNewPostagem);

                        try {
                            mediaPlayerDuration = new MediaPlayer();
                            mediaPlayerDuration.setDataSource(duracaoAudio());
                            mediaPlayerDuration.prepare();
                            String duracao = formatarTimer(mediaPlayerDuration.getDuration());
                            dadosMensagem.put("duracaoMusica", duracao);
                            mediaPlayerDuration.release();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        excluirAudioAnterior();

                        if (localConvertido.equals("pt_BR")) {
                            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                            date = new Date();
                            String novaData = dateFormat.format(date);
                            dadosMensagem.put("dataMensagem", novaData);
                            dadosMensagem.put("dataMensagemCompleta", date);

                            String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                            String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                            dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
                        } else {
                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                            date = new Date();
                            String novaData = dateFormat.format(date);
                            dadosMensagem.put("dataMensagem", novaData);
                            dadosMensagem.put("dataMensagemCompleta", date);
                            dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");

                            String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                            dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
                        }

                        salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                                .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                            atualizarContador();
                                            progressDialog.dismiss();
                                            edtTextMensagemChat.setText("");
                                        } else {
                                            //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                            progressDialog.dismiss();
                                        }
                                    }
                                });

                        salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                                .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                            edtTextMensagemChat.setText("");
                                        }
                                    }
                                });

                        scrollLast = "sim";
                        somenteInicio = null;
                    }
                });
            }
        });
    }


    private void funcaoCancelarAudio() {

        imgButtonGravarAudio.setClickable(true);
        bottomSheetDialog.cancel();
        excluirAudioAnterior();
        txtViewTempoAudio.setText("00:00");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
        }
        running = false;
        wasRunning = running;
        seconds = 0;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void funcaoGravarAudio() {
        if (isMicrophonePresent()) {
            imgButtonGravarAudio.setClickable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 255, 0, 0));
            }
            gravarAudio();
            runTimer();
        }
    }
        /*
    //Pega a extensão do arquivo
    private String getfileExtension(Uri uri)
    {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        ToastCustomizado.toastCustomizadoCurto("Retorno " + extension, getApplicationContext());
        return extension;
    }
     */
}