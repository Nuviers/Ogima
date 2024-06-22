package com.example.ogima.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterMensagem;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FcmUtils;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.NtpSyncService;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.RecuperarUriUtils;
import com.example.ogima.helper.SizeUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.MessageNotificacao;
import com.example.ogima.model.Usuario;
import com.example.ogima.model.Wallpaper;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class ConversationActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private ImageView imgViewWallpaper, imgViewFotoDestinatario;
    private MaterialSearchView materialSearchConversa;
    private ImageButton imgBtnBackConversa, imgBtnSearchConversa, imgBtnConfigsChat,
            imgBtnVideoCall, imgBtnVoiceCall, imgBtnStatusOnline, imgBtnScrollFirstMsg,
            imgBtnScrollLastMsg, imgButtonEnviarFotoChat, imgButtonSheetAudio,
            imgButtonEnviarMensagemChat, imgBtnAvisoNovaMsg;
    private TextView txtViewNomeDestinatario, txtViewTempoAudio;
    private EditText edtTextMensagemChat;
    private LinearLayout linearLayoutLigacao;
    private RecyclerView recyclerMensagensChat;
    private Usuario usuarioDestinatario;
    private Bundle dados;
    private SharedPreferences sharedWallpaper;
    private Wallpaper wallpaperShared;
    private File wallpaperLocal;
    private String caminhoWallpaper = "";
    private boolean primeiroCarregamento = true;
    private ValueEventListener listenerStatusOnline;
    private DatabaseReference verificaStatusOnlineRef, recuperarMensagensRef;
    private FirebaseUtils firebaseUtils;
    private BottomSheetDialog bottomSheetDialog, bottomSheetDialogWallpaper,
            bottomSheetDialogApagarConversa;

    private ImageButton imgButtonCancelarAudio, imgButtonEnviarAudio,
            imgButtonGravarAudio, imgButtonStopAudio, imgButtonPlayAudio,
            imgButtonPauseAudio;
    private PopupMenu popupMenuConfig, popupMenuMidias;
    private AdapterMensagem adapterMensagem;
    private ChildEventListener childEventListenerMensagens;
    private boolean rolarMsgs = false;
    private boolean novaMensagem = false;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerOptions<Mensagem> options;
    private int expectedItemCount;
    int currentItemCount;
    private int notificacaoId = -1;

    private ProgressDialog progressDialog;
    private static final int REQUEST_PERMISSION = 100;
    private String FILE_NAME = "audioTempTeste.mp3";

    private MediaPlayer mediaPlayer;
    private static final int REQUEST_AUDIO_PERMISSION = 701;
    private boolean isRecording = false;
    private long startTimeMillis = 0;
    private Handler timerHandler = new Handler();
    private MediaRecorder mediaRecorder;
    private static final int MAX_DURATION = 300000; // 5 minutos em milissegundos
    private static final int MIN_DURATION = 3000; // 3 segundos em milissegundos
    private TextView txtViewDialogOnlyChat, txtViewDialogAllChats;
    private FragmentManager fm = getFragmentManager();
    private ZoneId zoneId;
    private RecuperarUriUtils recuperarUriUtils;
    private String tipoMidiaPermissao = "";
    private ArrayList<Uri> urisSelecionadas = new ArrayList<>();
    private ArrayList<String> urlMidiaUpada = new ArrayList<>();
    private StorageReference storageRef;
    private MidiaUtils midiaUtils;
    private static final String TAG = "ConversationActivity";

    public ConversationActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        wallpaperShared = new Wallpaper();
        firebaseUtils = new FirebaseUtils();
    }

    private interface ConfigInicialCallback {
        void onConcluido();
    }

    private interface VerificaViewConversaCallback {
        void onStatus(boolean status);
    }

    @Override
    public void onBackPressed() {
        liberarRecursoAudio();
        atualizarStatusViewConversa();
        if (dados != null && dados.containsKey("notificacao")
                && !dados.getString("notificacao").isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), ChatInteractionsActivity.class);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            rolarMsgs = true;
            atualizarStatusOnline();
            adapterMensagem.startListening();
            recuperarMensagens();
            configSearchView();
            primeiroCarregamento = false;
        }

        //Busca wallpaper pelo shared, caso não tenha ele tenta buscar pelo servidor e ai localmente
        //porém mesmo assim se o usuário chegou a limpar os dados ou não existe mais o arquivo local
        //ou tá em outro dispositivo o usuário tera que colocar um novo wallpaper, a lógica é essa.
        buscarWallpaperLocal();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseUtils.removerValueListener(verificaStatusOnlineRef, listenerStatusOnline);
        firebaseUtils.removerRefChildListener(recuperarMensagensRef, childEventListenerMensagens);
        adapterMensagem.stopListening();
        removerFoco();
        liberarRecursoAudio();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        inicializarComponentes();
        AndroidThreeTen.init(this);
        NtpSyncService.startSync(this);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        midiaUtils = new MidiaUtils(ConversationActivity.this, getApplicationContext());
        recuperarUriUtils = new RecuperarUriUtils(ConversationActivity.this, getApplicationContext());
        configInicial(new ConfigInicialCallback() {
            @Override
            public void onConcluido() {

            }
        });

        String fotoUserTeste = "https://firebasestorage.googleapis.com/v0/b/ogima-7.appspot.com/o/apenasteste%2Fimgteste2.jpg?alt=media&token=1df60806-ba2f-416b-875d-c0a98dac0916";
        String fotoWallpaperTeste = "https://firebasestorage.googleapis.com/v0/b/ogima-7.appspot.com/o/apenasteste%2Fdragon.jpg?alt=media&token=bf93103a-e0ee-4c3d-80b3-037d0b816430";

        /*
        GlideCustomizado.loadUrlComListener(getApplicationContext(), fotoWallpaperTeste,
                imgViewWallpaper, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false, false, new GlideCustomizado.ListenerLoadUrlCallback() {
                    @Override
                    public void onCarregado() {

                    }

                    @Override
                    public void onError(String message) {

                    }
                });

         */
    }

    private void configInicial(ConfigInicialCallback callback) {
        if (idUsuario.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.error_retrieving_user_data), getApplicationContext());
            onBackPressed();
            return;
        }

        dados = getIntent().getExtras();

        if (dados == null || !dados.containsKey("usuarioDestinatario")) {
            ToastCustomizado.toastCustomizado("Não foi possível recuperar os dados do usuário selecionado", getApplicationContext());
            onBackPressed();
            return;
        }

        usuarioDestinatario = (Usuario) dados.getSerializable("usuarioDestinatario");

        if (usuarioDestinatario == null) {
            ToastCustomizado.toastCustomizado("Não foi possível recuperar os dados do usuário selecionado", getApplicationContext());
            onBackPressed();
            return;
        }

        configNotificacao();

        preencherDadosDestinatario();

        if (edtTextMensagemChat.getOnFocusChangeListener() == null) {
            edtTextMensagemChat.setOnFocusChangeListener(this);
        }

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        // Obter o fuso horário do usuário
        zoneId = ZoneId.systemDefault();

        configurarBottomSheet();

        //Exclui audio local anterior
        excluirAudioTemp(FILE_NAME);

        configOptionsSemFiltro(false);

        configRecyclerView();

        clickListeners();

        configMenuMidias();

        //Limpar msgs perdidas pois já foram visualizadas nesse chat atualmente.
        limparMensagensPerdidas();
    }

    private void recuperarMensagens() {

        DatabaseReference verificaNrMensagensRef = firebaseRef.child("conversas")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        recuperarMensagensRef = firebaseRef.child("conversas")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        verificaNrMensagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                expectedItemCount = (int) dataSnapshot.getChildrenCount();
                ToastCustomizado.toastCustomizado("Expected: " + expectedItemCount, getApplicationContext());
                childEventListenerMensagens = recuperarMensagensRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (rolarMsgs) {
                            // Este método será chamado para cada filho adicionado ao nó
                            currentItemCount++; // Incrementa o número atual de itens no adaptador

                            // Verifica se todos os dados foram carregados inicialmente
                            if (currentItemCount == expectedItemCount) {
                                // Todos os dados foram carregados inicialmente
                                // Agora você pode rolar para a última posição
                                ToastCustomizado.toastCustomizado("RolarMsgLast", getApplicationContext());
                                recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                                rolarMsgs = false;
                            }
                        } else if (isLastFourItemsVisible()) {
                            ToastCustomizado.toastCustomizado("Nova mensagem", getApplicationContext());
                            recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                        } else {
                            ToastCustomizado.toastCustomizado("Mensagem não lida", getApplicationContext());
                            imgBtnAvisoNovaMsg.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Este método será chamado se ocorrer um erro ao carregar os dados

            }
        });
    }

    private void configSearchView() {
        materialSearchConversa.setHint("Pesquisar mensagem");
        materialSearchConversa.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    String dadoDigitado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(newText);
                    dadoDigitado = FormatarNomePesquisaUtils.removeAcentuacao(dadoDigitado).toUpperCase(Locale.ROOT);
                    configOptionsComFiltro(dadoDigitado);
                }
                return true;
            }
        });
    }

    private void configOptionsSemFiltro(boolean update) {
        Query queryRecuperaMensagem = firebaseRef.child("conversas").child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario());

        options =
                new FirebaseRecyclerOptions.Builder<Mensagem>()
                        .setQuery(queryRecuperaMensagem, Mensagem.class)
                        .build();

        if (update) {
            adapterMensagem.setFiltrarSomenteTexto(false);
            adapterMensagem.updateOptions(options);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                }
            }, 100);
        }
    }

    private void configOptionsComFiltro(String dadoDigitado) {
        Query queryRecuperaMensagemFiltrada = firebaseRef.child("conversas").child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario()).orderByChild("conteudoMensagemPesquisa")
                .startAt(dadoDigitado)
                .endAt(dadoDigitado + "\uf8ff");

        options =
                new FirebaseRecyclerOptions.Builder<Mensagem>()
                        .setQuery(queryRecuperaMensagemFiltrada, Mensagem.class)
                        .build();

        adapterMensagem.updateOptions(options);
        adapterMensagem.setFiltrarSomenteTexto(true);
    }

    private void configRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagensChat.setLayoutManager(linearLayoutManager);
        if (adapterMensagem != null) {
        } else {
            adapterMensagem = new AdapterMensagem(getApplicationContext(), options, ConversationActivity.this, false, null);
        }
        recyclerMensagensChat.setAdapter(adapterMensagem);

        recyclerMensagensChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Verifica se os últimos quatro itens estão visíveis na tela
                if (adapterMensagem != null && adapterMensagem.getItemCount() >= 4) {
                    if (isFirstFourItemsVisible()) {
                        imgBtnScrollFirstMsg.setVisibility(View.INVISIBLE);
                        imgBtnScrollLastMsg.setVisibility(View.VISIBLE);
                    } else if (isLastFourItemsVisible()) {
                        // Os últimos quatro itens estão visíveis, oculte o botão
                        imgBtnAvisoNovaMsg.setVisibility(View.INVISIBLE);
                        imgBtnScrollLastMsg.setVisibility(View.INVISIBLE);
                        imgBtnScrollFirstMsg.setVisibility(View.VISIBLE);
                    } else {
                        imgBtnScrollFirstMsg.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private void configNotificacao() {
        if (dados.containsKey("idNotificacao")) {
            notificacaoId = dados.getInt("idNotificacao");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificacaoId);
        }

        DatabaseReference salvarViewEmConversaRef = firebaseRef.child("viewConversa")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                .child("viewConversa");
        salvarViewEmConversaRef.setValue(true);
    }

    private void preencherDadosDestinatario() {
        if (usuarioDestinatario.getMinhaFoto() == null || usuarioDestinatario.getMinhaFoto().isEmpty()) {
            UsuarioUtils.exibirFotoPadrao(getApplicationContext(), imgViewFotoDestinatario, UsuarioUtils.FIELD_PHOTO, true);
        } else {
            GlideCustomizado.loadUrlComListener(getApplicationContext(), usuarioDestinatario.getMinhaFoto(), imgViewFotoDestinatario,
                    android.R.color.transparent, GlideCustomizado.CIRCLE_CROP, false, usuarioDestinatario.isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
        }
        String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(usuarioDestinatario);
        nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_NAME_LENGHT);
        txtViewNomeDestinatario.setText(nomeConfigurado);
    }

    private void buscarWallpaperLocal() {
        String idDestinatarioWallpaper = usuarioDestinatario.getIdUsuario();

        sharedWallpaper = getSharedPreferences("WallpaperPrivado" + idDestinatarioWallpaper, Context.MODE_PRIVATE);

        String urlWallpaperLocal = sharedWallpaper.getString("urlWallpaper", null);
        String nomeWallpaperLocal = sharedWallpaper.getString("nomeWallpaper", null);

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
                buscarWallpaperServidor();
            }
        }
    }

    private void verificaWallpaperLocal(String tipoWallpaper, Wallpaper wallpaperInfo) {

        String nomeWallpaper = wallpaperInfo.getNomeWallpaper();

        configurarDiretorioWallpaper(tipoWallpaper);

        wallpaperLocal = new File(caminhoWallpaper);

        if (wallpaperLocal.exists()) {
            File file = new File(wallpaperLocal, nomeWallpaper);
            //ToastCustomizado.toastCustomizadoCurto("Existe",getApplicationContext());
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            GlideCustomizado.loadDrawableImageWallpaper(getApplicationContext(), drawable, imgViewWallpaper, android.R.color.transparent);
        } else {
            //Não existe mais o arquivo no dispositivo ou não existe mais os dados no shared.
            recuperarWallpaperPadrao();
        }
    }

    private void recuperarWallpaperPadrao() {
        // Obtém o drawable a ser utilizado como background
        Drawable drawable = getResources().getDrawable(R.drawable.tb1);

        // Define o drawable como background da janela
        GlideCustomizado.loadDrawableImageWallpaper(getApplicationContext(), drawable, imgViewWallpaper, android.R.color.transparent);
    }

    private void buscarWallpaperServidor() {

        DatabaseReference wallpaperPrivadoRef = firebaseRef.child("chatWallpaper")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        DatabaseReference wallpaperGlobalRef = firebaseRef.child("chatGlobalWallpaper")
                .child(idUsuario);
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

    private void configurarBottomSheet() {
        //Wallpaper
        bottomSheetDialogWallpaper = new BottomSheetDialog(ConversationActivity.this);
        bottomSheetDialogWallpaper.setContentView(R.layout.bottom_sheet_dialog_wallpaper);
        //Wallpaper

        //Apagar conversa
        bottomSheetDialogApagarConversa = new BottomSheetDialog(ConversationActivity.this);
        bottomSheetDialogApagarConversa.setContentView(R.layout.bottom_sheet_dialog_apagar_conversa);
        //Apagar conversa

        //Audio
        bottomSheetDialog = new BottomSheetDialog(ConversationActivity.this);
        bottomSheetDialog.setContentView(R.layout.audio_bottom_sheet_dialog);
        imgButtonCancelarAudio = bottomSheetDialog.findViewById(R.id.imgButtonCancelarAudio);
        imgButtonEnviarAudio = bottomSheetDialog.findViewById(R.id.imgButtonEnviarAudio);
        imgButtonGravarAudio = bottomSheetDialog.findViewById(R.id.imgButtonGravarAudio);
        imgButtonStopAudio = bottomSheetDialog.findViewById(R.id.imgButtonStopAudio);
        imgButtonPlayAudio = bottomSheetDialog.findViewById(R.id.imgButtonPlayAudio);
        imgButtonPauseAudio = bottomSheetDialog.findViewById(R.id.imgButtonPauseAudio);
        txtViewTempoAudio = bottomSheetDialog.findViewById(R.id.txtViewTempoAudio);
        //Audio

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

    private void excluirAudioTemp(String fileName) {
        File customDirectory = new File(getFilesDir(), "Download");
        File fileToDelete = new File(customDirectory, fileName);

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                ToastCustomizado.toastCustomizadoCurto("AudioTemp excluído com sucesso", getApplicationContext());
            } else {
                ToastCustomizado.toastCustomizado("Falha ao excluir o arquivo " + fileToDelete.getAbsolutePath(), getApplicationContext());
            }
        } else {
            ToastCustomizado.toastCustomizadoCurto("Arquivo não encontrado", getApplicationContext());
        }
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
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
    }

    private void clickListeners() {
        imgBtnSearchConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (materialSearchConversa.isSearchOpen()) {
                    materialSearchConversa.closeSearch();
                    configOptionsSemFiltro(true);
                } else {
                    materialSearchConversa.showSearch();
                }
            }
        });

        imgBtnBackConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imgBtnScrollFirstMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterMensagem != null && adapterMensagem.getItemCount() > 0) {
                    recyclerMensagensChat.scrollToPosition(0);
                    imgBtnScrollFirstMsg.setVisibility(View.GONE);
                    imgBtnScrollLastMsg.setVisibility(View.VISIBLE);
                }
            }
        });

        imgBtnScrollLastMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterMensagem != null && adapterMensagem.getItemCount() > 0) {
                    recyclerMensagensChat.scrollToPosition(adapterMensagem.getItemCount() - 1);
                    imgBtnScrollLastMsg.setVisibility(View.GONE);
                    imgBtnScrollFirstMsg.setVisibility(View.VISIBLE);
                }
            }
        });

        imgButtonSheetAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgButtonStopAudio.setVisibility(View.VISIBLE);
                //Verifica permissões de áudio.
                if (checkRecordingPermission()) {
                    //Todas funções de tratamento de áudio.
                    funcoesAudio();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("É necessário aceitar as permissões para que seja possível gravar seu áudio", getApplicationContext());
                    checkRecordingPermission();
                }
            }
        });

        imgButtonEnviarMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMsgTexto();
            }
        });

        imgButtonEnviarFotoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenuMidias.show();
            }
        });
    }

    private void atualizarStatusOnline() {
        if (listenerStatusOnline == null) {
            verificaStatusOnlineRef = firebaseRef.child("usuarios")
                    .child(usuarioDestinatario.getIdUsuario()).child("online");
            listenerStatusOnline = verificaStatusOnlineRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        boolean statusOnlineUser = snapshot.getValue(Boolean.class);
                        if (statusOnlineUser) {
                            imgBtnStatusOnline.setColorFilter(Color.parseColor("#27CC2E"));
                        } else {
                            imgBtnStatusOnline.setColorFilter(Color.parseColor("#615D5F"));
                        }
                    } else {
                        imgBtnStatusOnline.setColorFilter(Color.parseColor("#615D5F"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    imgBtnStatusOnline.setColorFilter(Color.parseColor("#615D5F"));
                }
            });
        }
    }

    private boolean checkRecordingPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ConversationActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
            return false;
        }
        return true;
    }

    private void funcoesAudio() {
        imgButtonEnviarMensagemChat.setVisibility(View.GONE);
        imgButtonPlayAudio.setVisibility(View.GONE);
        imgButtonStopAudio.setVisibility(View.GONE);
        imgButtonCancelarAudio.setVisibility(View.VISIBLE);
        imgButtonEnviarAudio.setVisibility(View.GONE);
        edtTextMensagemChat.clearFocus();
        bottomSheetDialog.show();
        bottomSheetDialog.setCancelable(false);

        imgButtonGravarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                funcaoGravarAudio();
            }
        });

        imgButtonStopAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });

        imgButtonPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executarAudio();
            }
        });

        imgButtonCancelarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                funcaoCancelarAudio();
            }
        });
    }

    private void funcaoGravarAudio() {
        //Verifica presença de microfone antes de continuar.
        if (isMicrophonePresent()) {
            imgButtonGravarAudio.setClickable(false);
            imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 255, 0, 0));
            gravarAudio();
        } else {
            ToastCustomizado.toastCustomizadoCurto("Não a presença de microfone", getApplicationContext());
        }
    }

    private void gravarAudio() {
        if (isRecording) {
            return;
        }
        imgButtonPlayAudio.setVisibility(View.GONE);

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        File customDirectory = new File(getFilesDir(), "Download");
        File internalFile = new File(customDirectory, FILE_NAME);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000); // High bitrate for better quality
        mediaRecorder.setAudioSamplingRate(44100); // Sample rate for better quality
        mediaRecorder.setOutputFile(internalFile.getPath());
        mediaRecorder.setMaxDuration(MAX_DURATION);

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                    ToastCustomizado.toastCustomizado("Tempo limite atingido", getApplicationContext());
                    imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
                }
            }
        });

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            startTimeMillis = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0); // Inicia o timer
            imgButtonStopAudio.setVisibility(View.VISIBLE);
            ToastCustomizado.toastCustomizado("Gravação iniciada", getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
            ToastCustomizado.toastCustomizado("Erro ao iniciar gravação", getApplicationContext());
            liberarRecursoAudio();
        }
    }

    private void stopRecording() {
        timerHandler.removeCallbacks(timerRunnable); // Para o timer
        imgButtonStopAudio.setVisibility(View.GONE);
        imgButtonPlayAudio.setVisibility(View.VISIBLE);

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        imgButtonGravarAudio.setClickable(true);

        // Verificar se a gravação atingiu a duração mínima
        long duration = System.currentTimeMillis() - startTimeMillis;
        if (duration < MIN_DURATION) {
            // Liberar recurso e exibir um toast
            excluirAudioTemp(FILE_NAME);
            imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
            ToastCustomizado.toastCustomizadoCurto("A gravação deve ter no mínimo 3 segundos", getApplicationContext());
            txtViewTempoAudio.setText("00:00");
        } else if (duration <= MAX_DURATION) {
            imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
            imgButtonEnviarAudio.setVisibility(View.VISIBLE);
        }
    }

    private void executarAudio() {
        try {

            File customDirectory = new File(getFilesDir(), "Download");
            File internalFile = new File(customDirectory, FILE_NAME);

            if (mediaPlayer != null) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(internalFile.getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    ToastCustomizado.toastCustomizadoCurto("Reproduzindo áudio", getApplicationContext());
                }
            } else {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(internalFile.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                ToastCustomizado.toastCustomizadoCurto("Reproduzindo áudio", getApplicationContext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                long millis = System.currentTimeMillis() - startTimeMillis;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                txtViewTempoAudio.setText(String.format("%d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000); // Atualiza a cada segundo
            }
        }
    };

    private void funcaoCancelarAudio() {
        imgButtonCancelarAudio.setVisibility(View.GONE);
        imgButtonGravarAudio.setClickable(true);
        excluirAudioTemp(FILE_NAME);
        // Parar o timer e o MediaRecorder
        timerHandler.removeCallbacks(timerRunnable);
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
        bottomSheetDialog.cancel();
        txtViewTempoAudio.setText("00:00");
        imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void liberarRecursoAudio() {
        excluirAudioTemp(FILE_NAME);
        // Parar o timer e o MediaRecorder
        timerHandler.removeCallbacks(timerRunnable);
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

    private boolean isMicrophonePresent() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isLastFourItemsVisible() {
        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        return lastVisibleItemPosition >= adapterMensagem.getItemCount() - 4;
    }

    private boolean isFirstFourItemsVisible() {
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        return firstVisibleItemPosition < 4;
    }

    private boolean isActivityActive() {
        // Implemente sua lógica para verificar se a Activity/Fragment está ativa
        return !isFinishing() && !isDestroyed();
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
    }

    private void configurarDiretorioWallpaper(String tipoWallpaper) {
        if (tipoWallpaper.equals("privado")) {
            caminhoWallpaper = getFilesDir() + File.separator + "wallpaperPrivado" + File.separator + usuarioDestinatario.getIdUsuario();
        } else if (tipoWallpaper.equals("global")) {
            caminhoWallpaper = getFilesDir() + File.separator + "wallpaperGlobal";
        }
    }

    private void atualizarStatusViewConversa() {
        DatabaseReference salvarViewEmConversaRef = firebaseRef.child("viewConversa")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                .child("viewConversa");
        salvarViewEmConversaRef.onDisconnect().setValue(false);
        salvarViewEmConversaRef.setValue(false);
    }

    private void configMenuMidias() {
        //Seleção de envio de arquivos - foto/camêra/gif/música/documento
        popupMenuMidias = new PopupMenu(getApplicationContext(), imgButtonEnviarFotoChat);
        popupMenuMidias.getMenuInflater().inflate(R.menu.popup_menu_anexo, popupMenuMidias.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenuMidias.setForceShowIcon(true);
        }
        popupMenuMidias.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.anexoCamera:
                        tipoMidiaPermissao = MidiaUtils.CAMERA;
                        checkPermissions();
                        ToastCustomizado.toastCustomizado("Camera", getApplicationContext());
                        return true;
                    case R.id.anexoGaleria:
                        tipoMidiaPermissao = MidiaUtils.GALLERY;
                        checkPermissions();
                        ToastCustomizado.toastCustomizado("Galeria", getApplicationContext());
                        return true;
                    case R.id.anexoMusica:
                        tipoMidiaPermissao = MidiaUtils.MUSIC;
                        checkPermissions();
                        ToastCustomizado.toastCustomizado("Musica", getApplicationContext());
                        return true;
                    case R.id.anexoDocumento:
                        tipoMidiaPermissao = MidiaUtils.DOCUMENT;
                        checkPermissions();
                        ToastCustomizado.toastCustomizado("Documento", getApplicationContext());
                        return true;
                    case R.id.anexoVideo:
                        tipoMidiaPermissao = MidiaUtils.VIDEO;
                        checkPermissions();
                        ToastCustomizado.toastCustomizado("Video", getApplicationContext());
                        return true;
                    case R.id.anexoGif:
                        tipoMidiaPermissao = MidiaUtils.GIF;
                        selecionadoGif();
                        ToastCustomizado.toastCustomizado("Gif", getApplicationContext());
                        return true;
                }
                return false;
            }
        });
    }

    private void limparMensagensPerdidas() {
        HashMap<String, Object> hashMapMsgsPerdidas = new HashMap<>();
        String caminhoContador = "/detalhesChat/" + idUsuario + "/" + usuarioDestinatario.getIdUsuario() + "/";
        hashMapMsgsPerdidas.put(caminhoContador + "totalMsgNaoLida", 0);
        firebaseRef.updateChildren(hashMapMsgsPerdidas, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
            }
        });
    }

    private void enviarMsgTexto() {
        if (edtTextMensagemChat.getText().toString().isEmpty()) {
            return;
        }
        String conteudoMensagem = edtTextMensagemChat.getText().toString().trim();
        configPadraoMensagem(MidiaUtils.TEXT, conteudoMensagem, null, null);
    }

    private void configPadraoMensagem(String tipoMidia, String conteudoMensagem, String nomeDocumento, MediaFile mediaFileDoc) {

        if (tipoMidia == null || tipoMidia.isEmpty() || conteudoMensagem == null || conteudoMensagem.isEmpty()) {
            return;
        }
        verificaViewConversa(new VerificaViewConversaCallback() {
            HashMap<String, Object> dadosMsg = new HashMap<>();

            @Override
            public void onStatus(boolean statusViewConversa) {
                long timestampNegativo = -1 * NtpSyncService.getAdjustedCurrentTime();
                long timestampPositivo = Math.abs(timestampNegativo);
                // Converter o timestamp para a data e hora local do usuário
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampPositivo), zoneId);
                // Formatar a data de acordo com o local do usuário
                String dataMsg = getFormattedDateForLocale(localDateTime, Locale.getDefault());
                if (tipoMidia.equals(MidiaUtils.TEXT)) {
                    edtTextMensagemChat.setText("");
                }

                String idBaseAtual = retornarIdConversa("atual");
                String idBaseDestinatario = retornarIdConversa("destinatario");
                String idConversa = generateChatId(idBaseAtual, idBaseDestinatario);

                //-> CaminhoDetalhes
                String caminhoDetalhesAtual = "/detalhesChat/" + idUsuario + "/" + usuarioDestinatario.getIdUsuario() + "/";
                String caminhoDetalhesDestinatario = "/detalhesChat/" + usuarioDestinatario.getIdUsuario() + "/" + idUsuario + "/";
                configurarHashMapDetalhes(dadosMsg, caminhoDetalhesAtual, "atual", idConversa, timestampNegativo, dataMsg, statusViewConversa, conteudoMensagem, tipoMidia);
                configurarHashMapDetalhes(dadosMsg, caminhoDetalhesDestinatario, "destinatario", idConversa, timestampNegativo, dataMsg, statusViewConversa, conteudoMensagem, tipoMidia);

                if (statusViewConversa) {
                    dadosMsg.put(caminhoDetalhesDestinatario + "totalMsgNaoLida", 0);
                } else {
                    dadosMsg.put(caminhoDetalhesDestinatario + "totalMsgNaoLida", ServerValue.increment(1));
                }

                //-> Conversas
                String caminhoConversaUserAtual = "/conversas/" + idUsuario + "/" + usuarioDestinatario.getIdUsuario() + "/" + idConversa + "/";
                String caminhoConversaUserDestinatario = "/conversas/" + usuarioDestinatario.getIdUsuario() + "/" + idUsuario + "/" + idConversa + "/";
                configurarHashMapConversa(dadosMsg, caminhoConversaUserAtual, "atual", idConversa, conteudoMensagem, timestampNegativo, dataMsg, tipoMidia, nomeDocumento, mediaFileDoc);
                configurarHashMapConversa(dadosMsg, caminhoConversaUserDestinatario, "destinatario", idConversa, conteudoMensagem, timestampNegativo, dataMsg, tipoMidia, nomeDocumento, mediaFileDoc);

                //-> ContadorTotalMsg
                String caminhoTotalMsgAtual = "/contadorMensagens/" + idUsuario + "/" + usuarioDestinatario.getIdUsuario() + "/";
                String caminhoTotalMsgDestinatario = "/contadorMensagens/" + usuarioDestinatario.getIdUsuario() + "/" + idUsuario + "/";

                configurarHashMapTotalMsg(dadosMsg, caminhoTotalMsgAtual, statusViewConversa);
                configurarHashMapTotalMsg(dadosMsg, caminhoTotalMsgDestinatario, statusViewConversa);

                if (statusViewConversa) {
                    dadosMsg.put(caminhoTotalMsgDestinatario + "mensagensPerdidas", 0);
                } else {
                    dadosMsg.put(caminhoTotalMsgDestinatario + "mensagensPerdidas", ServerValue.increment(1));
                }

                firebaseRef.updateChildren(dadosMsg, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (statusViewConversa) {
                            if (tipoMidia.equals(MidiaUtils.DOCUMENT) || tipoMidia.equals(MidiaUtils.MUSIC) || tipoMidia.equals(MidiaUtils.AUDIO)) {
                                enviarNotificacao("mensagem", usuarioDestinatario.getIdUsuario(),
                                        timestampPositivo, tipoMidia, "Você possui novas mensagens");
                            } else {
                                enviarNotificacao("mensagem", usuarioDestinatario.getIdUsuario(),
                                        timestampPositivo, tipoMidia, conteudoMensagem);
                            }
                        } else {
                            limparUriEOcultarProgress();
                        }
                    }
                });
            }
        });
    }

    private void configurarHashMapDetalhes(HashMap<String, Object> dadosMsg, String caminhoAlvo, String tipoUsuario, String idConversa, long timestampNegativo, String dataMsg, boolean viewConversa, String conteudoMensagem, String tipoMidia) {
        dadosMsg.put(caminhoAlvo + "idConversa", idConversa);
        dadosMsg.put(caminhoAlvo + "conteudoLastMsg", conteudoMensagem);
        dadosMsg.put(caminhoAlvo + "timestampLastMsg", timestampNegativo);
        dadosMsg.put(caminhoAlvo + "idUsuario", idUsuarioDetalhes(tipoUsuario));
        dadosMsg.put(caminhoAlvo + "tipoMidiaLastMsg", tipoMidia);
        dadosMsg.put(caminhoAlvo + "totalMsg", ServerValue.increment(1));
        dadosMsg.put(caminhoAlvo + "dateLastMsg", dataMsg);
    }

    private String idUsuarioDetalhes(String tipoUsuario) {
        if (tipoUsuario.equals("atual")) {
            return usuarioDestinatario.getIdUsuario();
        }
        return idUsuario;
    }

    private void configurarHashMapConversa(HashMap<String, Object> dadosMsg, String caminhoAlvo, String tipoUsuario, String idConversa, String conteudoMensagem, long timestampNegativo, String dataMsg, String tipoMidia, String nomeDocumento, MediaFile mediaFile) {
        dadosMsg.put(caminhoAlvo + "idConversa", idConversa);
        dadosMsg.put(caminhoAlvo + "tipoMensagem", tipoMidia);
        dadosMsg.put(caminhoAlvo + "idRemetente", idUsuario);
        dadosMsg.put(caminhoAlvo + "idDestinatario", usuarioDestinatario.getIdUsuario());
        dadosMsg.put(caminhoAlvo + "conteudoMensagem", conteudoMensagem);
        if (tipoMidia.equals(MidiaUtils.TEXT)) {
            dadosMsg.put(caminhoAlvo + "conteudoMensagemPesquisa", conteudoMensagem.toUpperCase(Locale.ROOT));
        }
        if (tipoMidia.equals(MidiaUtils.MUSIC)) {
            String duracao = recuperarUriUtils.formatarTimer(mediaFile.getDuration());
            dadosMsg.put(caminhoAlvo + "duracaoMusica", duracao);
        }
        dadosMsg.put(caminhoAlvo + "timestampinteracao", timestampNegativo);
        dadosMsg.put(caminhoAlvo + "dataMensagem", dataMsg);
        if (nomeDocumento != null) {
            dadosMsg.put(caminhoAlvo + "nomeDocumento", nomeDocumento);
        }
        if (mediaFile != null) {
            dadosMsg.put(caminhoAlvo + "tipoArquivo", mediaFile.getMimeType());
        }
    }

    private void configurarHashMapTotalMsg(HashMap<String, Object> dadosMsg, String caminhoAlvo, boolean statusViewConversa) {
        dadosMsg.put(caminhoAlvo + "totalMensagens", ServerValue.increment(1));
    }

    private String generateChatId(String user1Id, String user2Id) {
        String idBase = user2Id.replace("-", "");
        // Ordenar os IDs dos usuários para garantir uma combinação única e consistente
        if (user1Id.compareTo(idBase) < 0) {
            return user1Id + "_" + idBase;
        } else {
            return idBase + "_" + user1Id;
        }
    }

    private String retornarIdConversa(String tipoUsuario) {
        DatabaseReference conversaPushRefAtual = firebaseRef.child("conversas")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());
        DatabaseReference conversaPushRefDestinatario = firebaseRef.child("conversas")
                .child(usuarioDestinatario.getIdUsuario()).child(idUsuario);
        if (tipoUsuario.equals("atual")) {
            return conversaPushRefAtual.push().getKey();
        }
        return conversaPushRefDestinatario.push().getKey();
    }

    private String getFormattedDateForLocale(LocalDateTime dateTime, Locale locale) {
        DateTimeFormatter formatter;
        Locale brazilLocale = new Locale("pt", "BR");

        // Verificar se o local é do Brasil
        if (locale.getCountry().equals(brazilLocale.getCountry())) {
            // Formato de data brasileiro
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        } else {
            // Outros locais, formato "yyyy/MM/dd HH:mm:ss"
            formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        }
        return dateTime.format(formatter);
    }

    private void verificaViewConversa(VerificaViewConversaCallback callback) {
        DatabaseReference viewEmConversaRef = firebaseRef.child("viewConversa")
                .child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                .child("viewConversa");
        viewEmConversaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    boolean status = snapshot.getValue(Boolean.class);
                    callback.onStatus(status);
                } else {
                    callback.onStatus(false);
                }
                viewEmConversaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onStatus(false);
            }
        });
    }

    private void enviarNotificacao(String tipoOperacao, String idDestinatario, long timeStampOperacao, String tipoMensagem, String body) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                FcmUtils fcmUtils = new FcmUtils();
                MessageNotificacao messageNotificacao = new MessageNotificacao(idUsuario,
                        body, tipoMensagem, timeStampOperacao, fotoUsuario, nomeUsuarioAjustado, tipoOperacao, idDestinatario);
                fcmUtils.prepararNotificacaoMensagem(getApplicationContext(),
                        tipoOperacao, messageNotificacao, new FcmUtils.NotificacaoCallback() {
                            @Override
                            public void onEnviado() {
                                limparUriEOcultarProgress();
                                ToastCustomizado.toastCustomizadoCurto("Enviado", getApplicationContext());
                            }

                            @Override
                            public void onError(String message) {
                                limparUriEOcultarProgress();
                            }
                        });
            }

            @Override
            public void onSemDados() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String mensagem) {
                limparUriEOcultarProgress();
            }
        });
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

    private void checkPermissions() {
        if (tipoMidiaPermissao == null || tipoMidiaPermissao.isEmpty()) {
            return;
        }
        boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(ConversationActivity.this);
        if (galleryPermissionsGranted) {
            // Permissões da galeria já concedidas.
            switch (tipoMidiaPermissao) {
                case MidiaUtils.VIDEO:
                    selecionadoVideo();
                    break;
                case MidiaUtils.GALLERY:
                    selecionadoGaleria();
                    break;
                case MidiaUtils.CAMERA:
                    boolean cameraPermissionsGranted = PermissionUtils.requestCameraPermissions(ConversationActivity.this);
                    if (cameraPermissionsGranted) {
                        selecionadoCamera();
                    }
                    break;
                case MidiaUtils.DOCUMENT:
                    selecionadoDocumento();
                    break;
                case MidiaUtils.MUSIC:
                    selecionadoMusica();
                    break;
            }
        }
    }

    private void selecionadoGaleria() {
        limparLista();
        recuperarUriUtils.selecionadoGaleriaMultiple(false);
    }

    private void selecionadoCamera() {
        limparLista();
        recuperarUriUtils.selecionadoCamera(false);
    }

    private void selecionadoDocumento() {
        limparLista();
        recuperarUriUtils.selecionadoDocumentoMultiple();
    }

    private void selecionadoMusica() {
        limparLista();
        recuperarUriUtils.selecionadoMusicaMultiple();
    }

    private void selecionadoVideo() {
        limparLista();
        recuperarUriUtils.selecionadoVideo(progressDialog, new RecuperarUriUtils.UriRecuperadaCallback() {
            @Override
            public void onRecuperado(Uri uriRecuperada) {
                limparLista();
                exibirProgressDialog();
                if (uriRecuperada != null) {
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String nomeRandomico = UUID.randomUUID().toString();
                    String nomeArquivo = String.format("%s%s%s%s", "video", timestamp, nomeRandomico, ".mp4");

                    StorageReference mensagemRef = storageRef.child("mensagens")
                            .child("videos")
                            .child(idUsuario)
                            .child(usuarioDestinatario.getIdUsuario())
                            .child(nomeArquivo);
                    midiaUtils.uparFotoNoStorage(mensagemRef, uriRecuperada, new MidiaUtils.UparNoStorageCallback() {
                        @Override
                        public void onConcluido(String urlUpada) {
                            ToastCustomizado.toastCustomizado("Upada: " + urlUpada, getApplicationContext());
                            configPadraoMensagem(MidiaUtils.VIDEO, urlUpada, nomeArquivo, null);
                        }

                        @Override
                        public void onError(String message) {
                            limparUriEOcultarProgress();
                        }
                    });
                }
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void limparUriEOcultarProgress() {
        recuperarUriUtils.limparUri();
        if (urisSelecionadas != null && !urisSelecionadas.isEmpty()) {
            urisSelecionadas.clear();
        }
        if (urlMidiaUpada != null && !urlMidiaUpada.isEmpty()) {
            urlMidiaUpada.clear();
        }
        recuperarUriUtils.ocultarProgressDialog(progressDialog);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0) {
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord) {
                    ToastCustomizado.toastCustomizadoCurto("Permissão concedida", getApplicationContext());
                    funcoesAudio();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Permissão negada", getApplicationContext());
                }
            }
        } else if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.checkPermissionResult(grantResults)) {
                // Permissões concedidas.
                if (tipoMidiaPermissao != null) {
                    // Permissões da galeria já concedidas.
                    switch (tipoMidiaPermissao) {
                        case MidiaUtils.VIDEO:
                            selecionadoVideo();
                            break;
                        case MidiaUtils.GALLERY:
                            selecionadoGaleria();
                            break;
                        case MidiaUtils.CAMERA:
                            selecionadoCamera();
                            break;
                        case MidiaUtils.DOCUMENT:
                            selecionadoDocumento();
                            break;
                        case MidiaUtils.MUSIC:
                            selecionadoMusica();
                            break;
                    }
                }
            } else {
                // Permissões negadas.
                PermissionUtils.openAppSettings(ConversationActivity.this, getApplicationContext());
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        recuperarUriUtils.handleActivityResultPhoto(requestCode, resultCode, data, new RecuperarUriUtils.UriRecuperadaCallback() {
            @Override
            public void onRecuperado(Uri uriRecuperada) {
                limparLista();
                exibirProgressDialog();

                if (uriRecuperada != null) {
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String nomeRandomico = UUID.randomUUID().toString();
                    String nomeArquivo = String.format("%s%s%s%s", "foto", timestamp, nomeRandomico, ".jpeg");

                    StorageReference mensagemRef = storageRef.child("mensagens")
                            .child("fotos")
                            .child(idUsuario)
                            .child(usuarioDestinatario.getIdUsuario())
                            .child(nomeArquivo);

                    midiaUtils.uparFotoNoStorage(mensagemRef, uriRecuperada, new MidiaUtils.UparNoStorageCallback() {
                        @Override
                        public void onConcluido(String urlUpada) {
                            ToastCustomizado.toastCustomizado("Upada: " + urlUpada, getApplicationContext());
                            configPadraoMensagem(MidiaUtils.IMAGE, urlUpada, nomeArquivo, null);
                        }

                        @Override
                        public void onError(String message) {
                            limparUriEOcultarProgress();
                        }
                    });
                }
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });

        recuperarUriUtils.handleActivityResultDoc(requestCode, resultCode, data, new RecuperarUriUtils.MediaFileRecuperadoCallback() {
            @Override
            public void onRecuperado(MediaFile mediaFileDoc) {
                String nomeDoc = mediaFileDoc.getName();
                limparLista();
                exibirProgressDialog();

                StorageReference mensagemRef = storageRef.child("mensagens")
                        .child("documentos")
                        .child(idUsuario)
                        .child(usuarioDestinatario.getIdUsuario())
                        .child(nomeDoc);
                midiaUtils.uparFotoNoStorage(mensagemRef, mediaFileDoc.getUri(), new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        ToastCustomizado.toastCustomizado("Upada: " + urlUpada, getApplicationContext());
                        enviarMsgDoc(mediaFileDoc, urlUpada);
                    }

                    @Override
                    public void onError(String message) {
                        limparUriEOcultarProgress();
                    }
                });
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onOversized() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });

        recuperarUriUtils.handleActivityResultMusic(requestCode, resultCode, data, new RecuperarUriUtils.MediaFileRecuperadoCallback() {
            @Override
            public void onRecuperado(MediaFile mediaFileMusic) {
                String nomeMusic = mediaFileMusic.getName();
                limparLista();
                exibirProgressDialog();

                StorageReference mensagemRef = storageRef.child("mensagens")
                        .child("musicas")
                        .child(idUsuario)
                        .child(usuarioDestinatario.getIdUsuario())
                        .child(nomeMusic);
                midiaUtils.uparFotoNoStorage(mensagemRef, mediaFileMusic.getUri(), new MidiaUtils.UparNoStorageCallback() {
                    @Override
                    public void onConcluido(String urlUpada) {
                        ToastCustomizado.toastCustomizado("Upada: " + urlUpada, getApplicationContext());
                        enviarMsgMusica(mediaFileMusic, urlUpada);
                    }

                    @Override
                    public void onError(String message) {
                        limparUriEOcultarProgress();
                    }
                });
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onOversized() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void enviarMsgDoc(MediaFile mediaFileDoc, String urlUpada) {
        configPadraoMensagem(MidiaUtils.DOCUMENT, urlUpada, mediaFileDoc.getName(), mediaFileDoc);
    }

    private void enviarMsgMusica(MediaFile mediaFileMusic, String urlUpada) {
        configPadraoMensagem(MidiaUtils.MUSIC, urlUpada, mediaFileMusic.getName(), mediaFileMusic);
    }

    private void selecionadoGif() {
        recuperarUriUtils.selecionadoGif(ConversationActivity.this.getSupportFragmentManager(), TAG, SizeUtils.MEDIUM_GIF, new RecuperarUriUtils.GifRecuperadaCallback() {
            @Override
            public void onRecuperado(String urlGif) {
                exibirProgressDialog();
                enviarMsgGif(urlGif);
            }

            @Override
            public void onCancelado() {
                limparUriEOcultarProgress();
            }

            @Override
            public void onError(String message) {
                limparUriEOcultarProgress();
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void enviarMsgGif(String conteudoMensagem) {
        String dataNome = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
        String nomeDocumento = replaceAll + ".gif";
        configPadraoMensagem(MidiaUtils.GIF, conteudoMensagem, nomeDocumento, null);
    }

    private void limparLista() {
        if (urisSelecionadas != null && !urisSelecionadas.isEmpty()) {
            urisSelecionadas.clear();
        }

        if (urlMidiaUpada != null && !urlMidiaUpada.isEmpty()) {
            urlMidiaUpada.clear();
        }

        if (recuperarUriUtils != null) {
            recuperarUriUtils.limparUri();
        }
    }

    private void exibirProgressDialog() {
        if (progressDialog != null) {
            progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
            progressDialog.show();
        }
    }

    private void inicializarComponentes() {
        imgViewWallpaper = findViewById(R.id.imgViewWallpaperConversa);
        imgBtnSearchConversa = findViewById(R.id.imgBtnSearchConversa);
        materialSearchConversa = findViewById(R.id.materialSearchConversa);
        imgViewFotoDestinatario = findViewById(R.id.imgViewFotoDestinatario);
        txtViewNomeDestinatario = findViewById(R.id.txtViewNomeDestinatario);
        imgBtnStatusOnline = findViewById(R.id.imgBtnStatusOnline);
        imgBtnScrollLastMsg = findViewById(R.id.imgBtnScrollLastMsg);
        imgBtnScrollFirstMsg = findViewById(R.id.imgBtnScrollFirstMsg);
        recyclerMensagensChat = findViewById(R.id.recyclerMensagensChat);
        imgButtonSheetAudio = findViewById(R.id.imgButtonSheetAudio);
        edtTextMensagemChat = findViewById(R.id.edtTextMensagemChat);
        imgButtonEnviarMensagemChat = findViewById(R.id.imgButtonEnviarMensagemChat);
        imgButtonEnviarFotoChat = findViewById(R.id.imgButtonEnviarFotoChat);
        imgBtnVideoCall = findViewById(R.id.imgBtnVideoCall);
        imgBtnVoiceCall = findViewById(R.id.imgBtnVoiceCall);
        imgBtnConfigsChat = findViewById(R.id.imgBtnConfigsChat);
        imgBtnBackConversa = findViewById(R.id.imgBtnBackConversa);
        linearLayoutLigacao = findViewById(R.id.linearLayoutLigacao);
        imgBtnAvisoNovaMsg = findViewById(R.id.imgBtnAvisoNovaMsg);
    }
}