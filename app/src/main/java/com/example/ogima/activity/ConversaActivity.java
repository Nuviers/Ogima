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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterMensagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GiphyUtils;
import com.example.ogima.helper.GlideEngineMatisse;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.helper.VerificaTamanhoArquivo;
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


import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
            //Manifest.permission.MANAGE_EXTERNAL_STORAGE
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
    //private String somenteInicio;

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

    //public Boolean exibirPermissaoNegada = false;
    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();
    private String filtrarSomenteTexto;

    private String entradaChat;
    private Boolean novaMensagem = false;
    private int lastVisibleItemPosition;

    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private static final int MAX_FILE_SIZE_VIDEO = 17;
    private static final int MAX_FILE_SIZE_DOCUMENTO = 17;
    private static final int MAX_FILE_SIZE_MUSICA = 14;
    VerificaTamanhoArquivo verificaTamanhoArquivo = new VerificaTamanhoArquivo();

    private Boolean isRecording = false;
    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private static final int MAX_DURATION = 300000; // 5 minutos em milissegundos
    private static final int MIN_DURATION = 3000; // 3 segundos em milissegundos


    private String caminhoWallpaper;
    private File wallpaperLocal;


    //SharedPreferences
    private SharedPreferences sharedWallpaper;
    private SharedPreferences.Editor editorWallpaper;

    private String nomeWallpaperLocal;
    private String urlWallpaperLocal;
    private String idDestinatarioWallpaper;

    private Wallpaper wallpaperShared = new Wallpaper();

    private DatabaseReference conversaPushRef = firebaseRef.child("conversas");
    private String idConversa;

    private final GiphyUtils giphyUtils = new GiphyUtils();
    private GiphyDialogFragment gdl;

    @Override
    protected void onStart() {
        super.onStart();

        adapterMensagem.startListening();

        buscarMensagens();

        //Busca wallpaper pelo shared, caso não tenha ele tenta buscar pelo servidor e ai localmente
        //porém mesmo assim se o usuário chegou a limpar os dados ou não existe mais o arquivo local
        //ou tá em outro dispositivo o usuário tera que colocar um novo wallpaper, a lógica é essa.
        buscarWallpaperShared();

        //Configura lógica de pesquisa de mensagens.
        configurarMaterialSearchView();

        if (edtTextMensagemChat.getOnFocusChangeListener() == null) {
            edtTextMensagemChat.setOnFocusChangeListener(this::onFocusChange);
        }

        //Primeira entrada na activity, string sinalizadora para descer até o último elemento.
        entradaChat = "sim";

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
    protected void onDestroy() {
        super.onDestroy();

        liberarRecursoAudio();
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

        //Abrir layout suspenso para gravar áudio
        imgButtonSheetAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imgButtonStopAudio.setVisibility(View.VISIBLE);

                //Verifica permissões de áudio e armazenamento local
                if (checkRecordingPermission()) {
                    //Todas funções de tratamento de áudio.
                    funcoesAudio();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("É necessário aceitar as permissões para que seja possível gravar seu áudio", getApplicationContext());
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
            adapterMensagem = new AdapterMensagem(getApplicationContext(), options, ConversaActivity.this, false, null);
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
    //Métodos
    private void verificaWallpaper() {
        wallpaperPrivadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Wallpaper wallpaper = snapshot.getValue(Wallpaper.class);
                    if (wallpaper.getUrlWallpaper() != null) {
                        verificaWallpaperLocal("privado", wallpaper);
                    }
                } else {
                    wallpaperGlobalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Wallpaper wallpaperAll = snapshot.getValue(Wallpaper.class);
                                //Wallpaper definido para todos chats
                                if (wallpaperAll.getUrlWallpaper() != null) {
                                    verificaWallpaperLocal("global", wallpaperAll);
                                }
                            } else {
                                //Não existe nenhum wallpaper definido
                                recuperarWallpaperPadrao();
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

    private void buscarWallpaperShared() {

        idDestinatarioWallpaper = usuarioDestinatario.getIdUsuario();

        sharedWallpaper = getSharedPreferences("WallpaperPrivado" + idDestinatarioWallpaper, Context.MODE_PRIVATE);

        urlWallpaperLocal = sharedWallpaper.getString("urlWallpaper", null);
        nomeWallpaperLocal = sharedWallpaper.getString("nomeWallpaper", null);

        if (urlWallpaperLocal != null) {
            //Verifica se existe wallpaper para essa conversa
            wallpaperShared.setNomeWallpaper(nomeWallpaperLocal);
            wallpaperShared.setUrlWallpaper(urlWallpaperLocal);
            verificaWallpaperLocal("privado", wallpaperShared);
        } else {
            ToastCustomizado.toastCustomizadoCurto("2", getApplicationContext());
            //Não existe wallpaper para essa conversa, então recuperar o wallpaper global caso ele exista.
            sharedWallpaper = getSharedPreferences("WallpaperGlobal", Context.MODE_PRIVATE);

            urlWallpaperLocal = sharedWallpaper.getString("urlWallpaper", null);
            nomeWallpaperLocal = sharedWallpaper.getString("nomeWallpaper", null);

            if (urlWallpaperLocal != null) {
                wallpaperShared.setNomeWallpaper(nomeWallpaperLocal);
                wallpaperShared.setUrlWallpaper(urlWallpaperLocal);
                verificaWallpaperLocal("global", wallpaperShared);
            } else {
                //Não foi localizado nenhum tipo de wallpaper salvo no shared, procurar pelo servidor.
                verificaWallpaper();
            }
        }
    }

    private void verificaWallpaperLocal(String tipoWallpaper, Wallpaper wallpaperInfo) {

        String idDestinatario = usuarioDestinatario.getIdUsuario();
        String nomeWallpaper = wallpaperInfo.getNomeWallpaper();

        if (tipoWallpaper.equals("privado")) {
            caminhoWallpaper = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + idDestinatario + File.separator + "wallpaperPrivado"));
        } else if (tipoWallpaper.equals("global")) {
            caminhoWallpaper = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + "wallpaperGlobal"));
        }

        wallpaperLocal = new File(caminhoWallpaper);

        if (wallpaperLocal.exists()) {
            File file = new File(wallpaperLocal, nomeWallpaper);
            //ToastCustomizado.toastCustomizadoCurto("Existe",getApplicationContext());
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        } else {
            //Ou não existe mais o arquivo no dispositivo ou não existe mais o dado no shared.
            recuperarWallpaperPadrao();
        }
    }

    private void recuperarWallpaperPadrao() {
        // Obtém o drawable a ser utilizado como background
        Drawable drawable = getResources().getDrawable(R.drawable.wallpaperwaifutfour);

        // Define o drawable como background da janela
        getWindow().setBackgroundDrawable(drawable);
    }

    private void infosDestinatario() {

        txtViewNomeDestinatario.setText(usuarioDestinatario.getNomeUsuario());

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
    // Runnable para atualizar o timer
    private Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                txtViewTempoAudio.setText(timeString);

                // Verificar se a gravação atingiu a duração máxima

                // Atualizar o timer novamente em 1 segundo
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    private void enviarGif() {
        giphyUtils.selectGif(getApplicationContext(), new GiphyUtils.GifSelectionListener() {
            @Override
            public void onGifSelected(String gifPequena, String gifMedio, String gifOriginal) {
                progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                progressDialog.show();

                idConversa = conversaPushRef.push().getKey();

                HashMap<String, Object> dadosMensagem = new HashMap<>();
                dadosMensagem.put("idConversa", idConversa);
                dadosMensagem.put("tipoMensagem", "gif");
                dadosMensagem.put("idRemetente", idUsuario);
                dadosMensagem.put("idDestinatario", usuarioDestinatario.getIdUsuario());
                dadosMensagem.put("conteudoMensagem", gifOriginal);

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
                        .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                    edtTextMensagemChat.setText("");
                                }
                            }
                        });

                scrollLast = "sim";
                //somenteInicio = null;
            }
        });
        gdl = giphyUtils.retornarGiphyDialog();
        gdl.show(ConversaActivity.this.getSupportFragmentManager(), "ConversaActivity");
    }

    private void enviarVideo() {
        Matisse.from(ConversaActivity.this)
                .choose(MimeType.ofVideo())
                .countable(true)
                .maxSelectable(1)
                .showSingleMediaType(true)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngineMatisse())
                .forResult(SELECAO_VIDEO);
    }

    private void buscarMensagens() {

        childEventListener = recuperarMensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);

                if (entradaChat != null) {
                    if (entradaChat.equals("sim")) {
                        recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                    }
                }


                if (idUsuario.equals(mensagem.getIdRemetente())) {
                    novaMensagem = true;
                } else {
                    novaMensagem = false;
                }

                  /*
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
                        //somenteInicio = null;
                    }
                } else {
                    if (scrollLast != null) {
                        if (scrollLast.equals("sim")) {
                            recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                            //somenteInicio = null;
                        }
                    }
                }
                 */
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String
                    previousChildName) {

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
        //Limpando string sinalizadora.
        entradaChat = null;
    }

    private void enviarMensagem() {

        if (!edtTextMensagemChat.getText().toString().isEmpty()) {

            String conteudoMensagem = edtTextMensagemChat.getText().toString();
            idConversa = conversaPushRef.push().getKey();

            HashMap<String, Object> dadosMensagem = new HashMap<>();
            dadosMensagem.put("idConversa", idConversa);
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
                    .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                atualizarContador();
                            }
                        }
                    });

            salvarMensagemRef.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                    .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });

            scrollLast = "sim";
            //somenteInicio = null;

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
                        if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM, localImagemFotoSelecionada, getApplicationContext())) {
                            // Procede com o upload do arquivo
                            //*Chamando método responsável pela estrutura do U crop
                            openCropActivity(localImagemFotoSelecionada, Uri.fromFile(new File(getCacheDir(), destinoArquivo)));
                        }
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

                                idConversa = conversaPushRef.push().getKey();

                                HashMap<String, Object> dadosMensagem = new HashMap<>();
                                dadosMensagem.put("idConversa", idConversa);
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
                                        .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                        .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    edtTextMensagemChat.setText("");
                                                }
                                            }
                                        });

                                scrollLast = "sim";
                                //somenteInicio = null;
                            }
                        });
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (requestCode == SELECAO_VIDEO && resultCode == RESULT_OK) {

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

            if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_VIDEO, videoUri, getApplicationContext())) {

                progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                progressDialog.show();

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

                                idConversa = conversaPushRef.push().getKey();

                                HashMap<String, Object> dadosMensagem = new HashMap<>();
                                dadosMensagem.put("idConversa", idConversa);
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
                                        .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                        .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    edtTextMensagemChat.setText("");
                                                }
                                            }
                                        });

                                scrollLast = "sim";
                                //somenteInicio = null;
                            }
                        });
                    }
                });
            }

        } else if (requestCode == SELECAO_DOCUMENTO && resultCode == RESULT_OK) {
            if (data != null) {

                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                String path = files.get(0).getName();

                imagemRef = storageRef.child("mensagens")
                        .child("documentos")
                        .child(idUsuario)
                        .child(usuarioDestinatario.getIdUsuario())
                        .child(path);

                if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_DOCUMENTO, files.get(0).getUri(), getApplicationContext())) {
                    progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                    progressDialog.show();

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

                                    idConversa = conversaPushRef.push().getKey();

                                    HashMap<String, Object> dadosMensagem = new HashMap<>();
                                    dadosMensagem.put("idConversa", idConversa);
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
                                            .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                            .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                        edtTextMensagemChat.setText("");
                                                    }
                                                }
                                            });

                                    scrollLast = "sim";
                                    //somenteInicio = null;
                                }
                            });
                        }
                    });
                }
            }
        } else if (requestCode == SELECAO_MUSICA && resultCode == RESULT_OK) {


            if (data != null) {

                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                String path = files.get(0).getName();
                String duracao = formatarTimer(files.get(0).getDuration());

                imagemRef = storageRef.child("mensagens")
                        .child("musicas")
                        .child(idUsuario)
                        .child(usuarioDestinatario.getIdUsuario())
                        .child(path);

                if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_MUSICA, files.get(0).getUri(), getApplicationContext())) {
                    progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
                    progressDialog.show();

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

                                    idConversa = conversaPushRef.push().getKey();

                                    HashMap<String, Object> dadosMensagem = new HashMap<>();
                                    dadosMensagem.put("idConversa", idConversa);
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
                                            .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                            .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                        edtTextMensagemChat.setText("");
                                                    }
                                                }
                                            });

                                    scrollLast = "sim";
                                    //somenteInicio = null;
                                }
                            });
                        }
                    });
                }
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
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(getRecordingFilePath());
            mediaRecorder.setMaxDuration(MAX_DURATION);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        stopRecording();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
                        }
                    }
                }
            });
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(updateTimerRunnable, 1000);
            ToastCustomizado.toastCustomizadoCurto("Começando a gravação", getApplicationContext());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void executarAudio() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {

                } else {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(getRecordingFilePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    ToastCustomizado.toastCustomizadoCurto("Reproduzindo áudio", getApplicationContext());
                }
            } else {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(getRecordingFilePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                ToastCustomizado.toastCustomizadoCurto("Reproduzindo áudio", getApplicationContext());
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

        filtrarSomenteTexto = "comFiltro";
        adapterMensagem.verificarFiltragem(filtrarSomenteTexto);

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

        filtrarSomenteTexto = "semFiltro";
        adapterMensagem.verificarFiltragem(filtrarSomenteTexto);

        queryRecuperaMensagem = firebaseRef.child("conversas").child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario());

        options =
                new FirebaseRecyclerOptions.Builder<Mensagem>()
                        .setQuery(queryRecuperaMensagem, Mensagem.class)
                        .build();

        adapterMensagem.updateOptions(options);

        queryRecuperaMensagem.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Garante que a rolagem só seja feita depois de serem adicionados todos os elementos
                    recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                }
                queryRecuperaMensagem.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        liberarRecursoAudio();

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

    private void liberarRecursoAudio() {
        excluirAudioAnterior();
        // Parar o timer e o MediaRecorder
        timerHandler.removeCallbacks(updateTimerRunnable);
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void referenciasUsuarioAtual() {

        dadosAtuaisRef = firebaseRef.child("usuarios").child(idUsuario);

        wallpaperGlobalRef = firebaseRef.child("chatGlobalWallpaper")
                .child(idUsuario);

        salvarMensagemRef = firebaseRef.child("conversas");
    }

    private void logicaScroll() {

        if (recyclerViewOnScrollListener == null) {

            recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {
                        if (novaMensagem) {
                            recyclerView.smoothScrollToPosition(adapterMensagem.getItemCount() - 1);
                        }
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
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

                    if (!novaMensagem) {
                        // Verifica se o usuário está vendo os últimos 5 elementos
                        if (!recyclerView.canScrollVertically(1) && totalItemCount - lastVisible <= 5 && totalItemCount > 0) {
                            // Rola até o último elemento
                            //ToastCustomizado.toastCustomizadoCurto("Rolagem",getApplicationContext());
                            recyclerView.smoothScrollToPosition(totalItemCount - 1);
                        }
                    }
                }
            };
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

        queryRecuperaMensagem.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                }
                queryRecuperaMensagem.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        dadosAtuaisRef = firebaseRef.child("usuarios").child(idUsuario);

        dadosAtuaisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioLogado = snapshot.getValue(Usuario.class);

                    if (usuarioLogado.getIdUsuario().equals(idUsuario)) {

                        jitsiMeetUserInfo.setDisplayName(usuarioLogado.getNomeUsuario());

                        if (usuarioLogado.getEpilepsia().equals("Não") &&
                                usuarioDestinatario.getEpilepsia().equals("Não")) {
                            try {
                                if (idUsuario.equals(usuarioLogado.getIdUsuario())) {
                                    jitsiMeetUserInfo.setAvatar(new URL(usuarioLogado.getMinhaFoto()));
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("Epilepsia", getApplicationContext());
                            //String gifUsuarioAtual = usuarioLogado.getMinhaFoto();
                            //transformarGifEmImagem(jitsiMeetUserInfo, gifUsuarioAtual);
                        }
                    }
                }
                dadosAtuaisRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void transformarGifEmImagem(JitsiMeetUserInfo jitsiMeetUserInfo, String gifUsuarioAtual) {
        //Fazer uma lógica de verificar permissões para
        //poder salvar a gif no dispositivo do usuário salvando como png
        //e recuperar a url dessa imagem e exibir no jitsiMeetUserInfo.setAvatar(new URL());
        solicitaPermissoes("imagem");
        if (!solicitaPermissoes.exibirPermissaoNegada) {

            /*
            new DownloadImageTask(new DownloadImageTask.Listener() {
                @Override
                public void onImageDownloaded(Bitmap image) {

                    // Converte o Bitmap em uma string base64
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String avatar = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    ToastCustomizado.toastCustomizado("Avatar " + avatar, getApplicationContext());

                    imagemRef = storageRef.child("teste")
                            .child("imagem")
                            .child(idUsuario)
                            .child("imagem" + ".png");
                    UploadTask uploadTask = imagemRef.putBytes(byteArray);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    ToastCustomizado.toastCustomizadoCurto("Salvo", getApplicationContext());
                                    Uri url = task.getResult();
                                    String urlGifConvertida = url.toString();
                                    try {
                                        jitsiMeetUserInfo.setAvatar(new URL(urlGifConvertida));
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }
            }).execute(gifUsuarioAtual);
              */

        }
    }

    private void solicitaPermissoes(String permissao) {
        //Verifica quais permissões falta a ser solicitadas, caso alguma seja negada, exibe um toast.
        if (!solicitaPermissoes.verificaPermissoes(permissoesNecessarias, ConversaActivity.this, permissao)) {
            if (permissao != null) {
                solicitaPermissoes.tratarResultadoPermissoes(permissao, ConversaActivity.this);
            }
        }
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
                solicitaPermissoes("wallpaper");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    Intent intent = new Intent(getApplicationContext(), MudarWallpaperActivity.class);
                    intent.putExtra("wallpaperPlace", "onlyChat");
                    intent.putExtra("usuarioDestinatario", usuarioDestinatario);
                    startActivity(intent);
                }
            }
        });

        txtViewDialogAllChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogWallpaper.dismiss();
                bottomSheetDialogWallpaper.cancel();
                solicitaPermissoes("wallpaper");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    Intent intent = new Intent(getApplicationContext(), MudarWallpaperActivity.class);
                    intent.putExtra("wallpaperPlace", "allChats");
                    intent.putExtra("usuarioDestinatario", usuarioDestinatario);
                    startActivity(intent);
                }
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
                        if (!solicitaPermissoes.exibirPermissaoNegada) {
                            ImagePicker.Companion.with(ConversaActivity.this)
                                    .cameraOnly()
                                    .crop()                    //Crop image(Optional), Check Customization for more option
                                    .compress(1024)            //Final image size will be less than 1 MB(Optional)
                                    //.maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                                    .start(101);
                        }
                        return true;
                    case R.id.anexoGaleria:
                        //Atribuindo null na selecionadoCamera só assim evita problema no envio de imagem
                        //pela galeria, ao clicar em camera e depois em galeria a string tem que ser limpa, resolvido atribuindo null.
                        selecionadoCamera = null;
                        solicitaPermissoes("galeria");
                        if (!solicitaPermissoes.exibirPermissaoNegada) {
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
                        if (!solicitaPermissoes.exibirPermissaoNegada) {
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
                        if (!solicitaPermissoes.exibirPermissaoNegada) {
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
                        if (!solicitaPermissoes.exibirPermissaoNegada) {
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
        imgButtonStopAudio.setVisibility(View.VISIBLE);
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
                stopRecording();
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

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        txtViewTempoAudio.setText("00:00");

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

                        idConversa = conversaPushRef.push().getKey();

                        HashMap<String, Object> dadosMensagem = new HashMap<>();
                        dadosMensagem.put("idConversa", idConversa);
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

                            String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                            dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
                        }

                        salvarMensagemRef.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                                .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                .child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                            edtTextMensagemChat.setText("");
                                        }
                                    }
                                });

                        scrollLast = "sim";
                        //somenteInicio = null;
                    }
                });
            }
        });
    }


    private void funcaoCancelarAudio() {

        imgButtonGravarAudio.setClickable(true);
        excluirAudioAnterior();
        // Parar o timer e o MediaRecorder
        timerHandler.removeCallbacks(updateTimerRunnable);
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
        bottomSheetDialog.cancel();
        txtViewTempoAudio.setText("00:00");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
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

    // Método para parar a gravação
    private void stopRecording() {

        imgButtonStopAudio.setVisibility(View.GONE);
        // Parar o timer e o MediaRecorder
        timerHandler.removeCallbacks(updateTimerRunnable);
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;

        // Verificar se a gravação atingiu a duração mínima
        long duration = System.currentTimeMillis() - startTime;
        if (duration < MIN_DURATION) {
            // Liberar recurso e exibir um toast
            excluirAudioAnterior();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
            }
            ToastCustomizado.toastCustomizadoCurto("A gravação deve ter no mínimo 3 segundos", getApplicationContext());
        } else if (duration < MAX_DURATION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
            }
        }

        ToastCustomizado.toastCustomizadoCurto("Áudio finalizado", getApplicationContext());
    }
}