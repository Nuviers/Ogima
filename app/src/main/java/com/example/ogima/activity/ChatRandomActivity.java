package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterChatRandom;

import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GiphyUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class ChatRandomActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private ImageButton imgBtnBackSairDaConversa;
    private ImageView imgViewFotoDestinatario;
    private String idUserD = "", idUserLogado = "";
    private TextView txtViewTimerChatRestante, txtViewNomeDestinatario;
    private Usuario usuarioD;
    private boolean statusEpilepsia = true;
    private CountDownTimer countDownTimer;
    private RecyclerView recyclerViewChatRandom;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private EditText edtTextMensagemChat;
    //Variáveis para data
    private DateFormat dateFormat;
    private Date date;
    private String localConvertido;
    private Locale current;
    private ChildEventListener childEventListener;
    private ImageButton imgButtonSheetAudio, imgButtonEnviarMensagemChat, imgBtnEnviarGif;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerDuration;
    private TextView txtViewTempoAudio;
    private ImageButton imgButtonCancelarAudio, imgButtonEnviarAudio,
            imgButtonGravarAudio, imgButtonStopAudio, imgButtonPlayAudio,
            imgButtonPauseAudio;
    private LinearLayoutManager linearLayoutManager;
    private ImageButton imgBtnScrollLastMsg, imgBtnScrollFirstMsg;
    private FragmentManager fm = getFragmentManager();
    private RecyclerView.OnScrollListener recyclerViewOnScrollListener;
    private FirebaseRecyclerOptions<Mensagem> options;
    private String entradaChat;
    private Boolean novaMensagem = false;
    private BottomSheetDialog bottomSheetDialog;
    private Boolean isRecording = false;
    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private static final int MAX_DURATION = 300000; // 5 minutos em milissegundos
    private static final int MIN_DURATION = 3000; // 3 segundos em milissegundos
    private final GiphyUtils giphyUtils = new GiphyUtils();
    private GiphyDialogFragment gdl;
    private boolean primeiroCarregamento = true;
    private AdapterChatRandom adapterChatRandom;
    private ProgressDialog progressDialog;
    private static final int REQUEST_AUDIO_PERMISSION = 701;
    private DatabaseReference mensagensRef;
    private AtualizarContador atualizarContador = new AtualizarContador();
    private StorageReference storageRef, audioAtualRef, audioDRef;
    private ValueEventListener valueEventListener;
    private DatabaseReference verificaAddRandomRef;
    private AlertDialog.Builder builder;

    @Override
    public void onBackPressed() {
        exibirAlertDialog();
        super.onBackPressed();
    }

    public interface LimparConversaCallback {
        void onConversaExcluida();

        void onContadorLimpo();

        void onError(String message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (countDownTimer == null) {
            configInicialTimer();
        }
        if (primeiroCarregamento) {

            buscarMensagens();

            if (edtTextMensagemChat.getOnFocusChangeListener() == null) {
                edtTextMensagemChat.setOnFocusChangeListener(this::onFocusChange);
            }
            //Primeira entrada na activity, string sinalizadora para descer até o último elemento.
            entradaChat = "sim";
            //Cuida da lógica do scroll.
            logicaScroll();
            primeiroCarregamento = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterChatRandom != null) {
            adapterChatRandom.pauseAudio();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapterChatRandom != null) {
            adapterChatRandom.resumeAudio();

            // Percorra os ViewHolders e atualize os SeekBars
            int itemCount = adapterChatRandom.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                AdapterChatRandom.ViewHolder viewHolder = (AdapterChatRandom.ViewHolder) recyclerViewChatRandom.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    viewHolder.atualizarSeekBarV2();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (adapterChatRandom != null) {
            adapterChatRandom.releaseAudio();
        }

        removeChildEventListener(mensagensRef, childEventListener);

        adapterChatRandom.stopListening();

        //Remove foco do editText, bottomSheet, materialSearchView e do scrollListener.
        removerFoco();
        liberarRecursoAudio();

        limparConversa(new LimparConversaCallback() {
            @Override
            public void onConversaExcluida() {
                limparContador(this);
            }

            @Override
            public void onContadorLimpo() {

            }

            @Override
            public void onError(String message) {
                ocultarProgressDialog();
                finish();
            }
        });
    }

    public interface MensagemEnviadaCallback {
        void onEnviadoRemetente(HashMap<String, Object> dadosMensagem);

        void onError(String message);
    }

    public interface DadosUserDestinatarioCallback {
        void onRecuperado(Usuario usuarioD);

        void onError(String message);
    }

    public ChatRandomActivity() {
        idUserLogado = UsuarioUtils.recuperarIdUserAtual();
        usuarioD = new Usuario();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_random);
        inicializandoComponentes();
        wallpaperPadrao();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        builder = new AlertDialog.Builder(ChatRandomActivity.this);

        //Configurando data de acordo com local do usuário.
        current = getResources().getConfiguration().locale;
        localConvertido = localConvertido.valueOf(current);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("idUserD")) {
                idUserD = dados.getString("idUserD");
                recuperarDadosDestinatario(new DadosUserDestinatarioCallback() {
                    @Override
                    public void onRecuperado(Usuario usuarioDestinatario) {
                        usuarioD = usuarioDestinatario;
                        exibirDadosD();
                        bottomSheetDialog = new BottomSheetDialog(ChatRandomActivity.this);
                        bottomSheetDialog.setContentView(R.layout.audio_bottom_sheet_dialog);
                        configBottomSheetDialog();
                        clickListeners();
                        configRecycler();
                        rolagemScrollManual();
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        }
    }

    private void wallpaperPadrao() {
        Drawable drawable = getResources().getDrawable(R.drawable.backgroundblueteste);

        // Define o drawable como background da janela
        getWindow().setBackgroundDrawable(drawable);
    }

    private void recuperarDadosDestinatario(DadosUserDestinatarioCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUserD, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                setStatusEpilepsia(epilepsia);
                callback.onRecuperado(usuarioAtual);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void exibirDadosD() {
        GlideCustomizado.loadUrl(getApplicationContext(),
                usuarioD.getMinhaFoto(),
                imgViewFotoDestinatario, android.R.color.transparent,
                GlideCustomizado.CIRCLE_CROP, false, isStatusEpilepsia());
        String nomeAjustado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioD.getNomeUsuario());
        txtViewNomeDestinatario.setText(FormatarContadorUtils.abreviarTexto(nomeAjustado, 22));
    }

    private void configInicialTimer() {
        // Defina o tempo inicial do temporizador para 10 minutos (10 * 60 * 1000 milissegundos)
        long tempoTotal = 10 * 60 * 1000;
        // Inicialize o CountDownTimer
        countDownTimer = new CountDownTimer(tempoTotal, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // Atualize o TextView a cada segundo com o tempo restante
                int minutos = (int) (millisUntilFinished / 1000) / 60;
                int segundos = (int) (millisUntilFinished / 1000) % 60;

                String tempoRestante = String.format("%02d:%02d", minutos, segundos);
                txtViewTimerChatRestante.setText("Tempo restante: " + tempoRestante);
            }

            @Override
            public void onFinish() {
                // Quando o temporizador terminar, você pode realizar a ação desejada aqui
                txtViewTimerChatRestante.setText("Tempo de conversa chegou ao fim!");

                Intent intent = new Intent(ChatRandomActivity.this, EndLobbyActivity.class);
                intent.putExtra("idUserD", idUserD);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        };

        // Inicie o temporizador
        countDownTimer.start();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    private boolean checkRecordingPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            requestRecordingPermission();
            return false;
        }
        return true;
    }

    private void requestRecordingPermission() {
        ActivityCompat.requestPermissions(ChatRandomActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION);
    }

    private void configRecycler() {
        //Configurando recycler
        if (linearLayoutManager == null) {

            Query queryRecuperaMensagem = firebaseRef.child("chatRandom")
                    .child(idUserLogado)
                    .child(idUserD);

            options =
                    new FirebaseRecyclerOptions.Builder<Mensagem>()
                            .setQuery(queryRecuperaMensagem, Mensagem.class)
                            .build();

            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerViewChatRandom.setLayoutManager(linearLayoutManager);
            if (adapterChatRandom == null) {
                adapterChatRandom = new AdapterChatRandom(getApplicationContext(), options);
                adapterChatRandom.updateOptions(options);
                adapterChatRandom.setStatusEpilepsia(isStatusEpilepsia());
                recyclerViewChatRandom.setAdapter(adapterChatRandom);
                adapterChatRandom.startListening();
            }
        }
    }

    private void rolagemScrollManual() {
        imgBtnScrollLastMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewChatRandom.scrollToPosition(adapterChatRandom.getItemCount() - 1);
                imgBtnScrollLastMsg.setVisibility(View.GONE);
                imgBtnScrollFirstMsg.setVisibility(View.VISIBLE);
            }
        });

        imgBtnScrollFirstMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewChatRandom.scrollToPosition(0);
                imgBtnScrollFirstMsg.setVisibility(View.GONE);
                imgBtnScrollLastMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    private void logicaScroll() {
        if (recyclerViewOnScrollListener == null) {
            recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {
                        if (novaMensagem) {
                            recyclerView.smoothScrollToPosition(adapterChatRandom.getItemCount() - 1);
                        }
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

                    //Exibe o botão de ir para última mensagem somente se o último item estiver visível.
                    if (lastVisible == adapterChatRandom.getItemCount() - 1) {
                        imgBtnScrollLastMsg.setVisibility(View.GONE);
                        imgBtnScrollFirstMsg.setVisibility(View.VISIBLE);
                    } else {
                        imgBtnScrollFirstMsg.setVisibility(View.GONE);
                        imgBtnScrollLastMsg.setVisibility(View.VISIBLE);
                    }
                }
            };
            recyclerViewChatRandom.addOnScrollListener(recyclerViewOnScrollListener);
        }
    }

    private void clickListeners() {
        imgButtonEnviarMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference referencia = firebaseRef.child("chatRandom")
                        .child(idUserLogado).child(idUserD);
                enviarMensagem(referencia, new MensagemEnviadaCallback() {
                    @Override
                    public void onEnviadoRemetente(HashMap<String, Object> dadosMensagem) {
                        DatabaseReference referenciaDestinatario = firebaseRef.child("chatRandom")
                                .child(idUserD).child(idUserLogado);
                        String idConversa = referenciaDestinatario.push().getKey();
                        dadosMensagem.put("idConversa", idConversa);
                        referenciaDestinatario.child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    edtTextMensagemChat.setText("");
                                    if (edtTextMensagemChat.getOnFocusChangeListener() != null) {
                                        edtTextMensagemChat.clearFocus();
                                        edtTextMensagemChat.setOnFocusChangeListener(null);
                                    }
                                    atualizarContador();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        });
        imgBtnEnviarGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecionarGif();
            }
        });

        imgBtnBackSairDaConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void buscarMensagens() {
        mensagensRef = firebaseRef.child("chatRandom")
                .child(idUserLogado).child(idUserD);
        childEventListener = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);

                if (entradaChat != null) {
                    if (entradaChat.equals("sim")) {
                        recyclerViewChatRandom.scrollToPosition(adapterChatRandom.getItemCount() - 1);
                        //Limpando string sinalizadora.
                        entradaChat = null;
                    }
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            novaMensagem = true;
                            if (entradaChat == null && novaMensagem) {
                                // Verifica se o usuário está vendo os últimos 4 itens
                                if (isLastFourItemsVisible()) {
                                    // O usuário está vendo os últimos 4 itens
                                    // Faça o que você precisa fazer aqui
                                    //ToastCustomizado.toastCustomizadoCurto("Final recycler", getApplicationContext());
                                    recyclerViewChatRandom.scrollToPosition(adapterChatRandom.getItemCount() - 1);
                                } else {
                                    // O usuário não está vendo os últimos 4 itens
                                    // Faça outra coisa, se necessário
                                    //ToastCustomizado.toastCustomizadoCurto("Not final", getApplicationContext());
                                }
                                novaMensagem = false;
                            }
                        }
                    }, 100);
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

    private void enviarMensagem(DatabaseReference referencia, MensagemEnviadaCallback callback) {

        if (!edtTextMensagemChat.getText().toString().isEmpty()) {

            String conteudoMensagem = edtTextMensagemChat.getText().toString();

            String idConversa = referencia.push().getKey();

            HashMap<String, Object> dadosMensagem = new HashMap<>();
            dadosMensagem.put("idConversa", idConversa);
            dadosMensagem.put("tipoMensagem", "texto");
            dadosMensagem.put("idRemetente", idUserLogado);
            dadosMensagem.put("idDestinatario", idUserD);
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

            referencia.child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        callback.onEnviadoRemetente(dadosMensagem);
                    }
                }
            });
        }
    }

    private void enviarGif(String urlGif) {
        progressDialog.setMessage("Enviando mensagem, por favor aguarde...");
        progressDialog.show();

        DatabaseReference referenciaRemetente = firebaseRef.child("chatRandom")
                .child(idUserLogado).child(idUserD);

        String idConversaRemetente;
        idConversaRemetente = referenciaRemetente.push().getKey();

        HashMap<String, Object> dadosMensagem = new HashMap<>();
        dadosMensagem.put("idConversa", idConversaRemetente);
        dadosMensagem.put("tipoMensagem", "gif");
        dadosMensagem.put("idRemetente", idUserLogado);
        dadosMensagem.put("idDestinatario", idUserD);
        dadosMensagem.put("conteudoMensagem", urlGif);

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

        referenciaRemetente.child(idConversaRemetente).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    DatabaseReference referenciaD = firebaseRef.child("chatRandom")
                            .child(idUserD).child(idUserLogado);

                    String idConversaD = referenciaD.push().getKey();

                    dadosMensagem.put("idConversa", idConversaD);

                    referenciaD.child(idConversaD).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                atualizarContador();
                                progressDialog.dismiss();
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });
                } else {
                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void atualizarContador() {
        DatabaseReference contadorRemetenteRef = firebaseRef.child("contadorMensagensRandom")
                .child(idUserLogado)
                .child(idUserD).child("totalMensagens");
        atualizarContador.acrescentarContador(contadorRemetenteRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                DatabaseReference contadorDRef = firebaseRef.child("contadorMensagensRandom")
                        .child(idUserD)
                        .child(idUserLogado).child("totalMensagens");
                atualizarContador.acrescentarContador(contadorDRef, new AtualizarContador.AtualizarContadorCallback() {
                    @Override
                    public void onSuccess(int contadorAtualizado) {
                    }

                    @Override
                    public void onError(String errorMessage) {

                    }
                });
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

    private boolean isLastFourItemsVisible() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewChatRandom.getLayoutManager();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        int totalItemCount = layoutManager.getItemCount();
        return totalItemCount - lastVisibleItemPosition <= 4;
    }

    private void selecionarGif() {
        giphyUtils.selectGif(getApplicationContext(), new GiphyUtils.GifSelectionListener() {
            @Override
            public void onGifSelected(String gifPequena, String gifMedio, String gifOriginal) {
                enviarGif(gifMedio);
            }
        });
        gdl = giphyUtils.retornarGiphyDialog();
        gdl.show(ChatRandomActivity.this.getSupportFragmentManager(), "ChatRandomActivity");
    }

    private void configBottomSheetDialog() {
        imgButtonCancelarAudio = bottomSheetDialog.findViewById(R.id.imgButtonCancelarAudio);
        imgButtonEnviarAudio = bottomSheetDialog.findViewById(R.id.imgButtonEnviarAudio);
        imgButtonGravarAudio = bottomSheetDialog.findViewById(R.id.imgButtonGravarAudio);
        imgButtonStopAudio = bottomSheetDialog.findViewById(R.id.imgButtonStopAudio);
        imgButtonPlayAudio = bottomSheetDialog.findViewById(R.id.imgButtonPlayAudio);
        imgButtonPauseAudio = bottomSheetDialog.findViewById(R.id.imgButtonPauseAudio);
        txtViewTempoAudio = bottomSheetDialog.findViewById(R.id.txtViewTempoAudio);

        //Exclui audio local anterior
        excluirAudioAnterior();

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

    private void removeChildEventListener(DatabaseReference reference, ChildEventListener
            childEventListener) {
        if (childEventListener != null) {
            reference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    private void removerFoco() {
        if (edtTextMensagemChat.getOnFocusChangeListener() != null) {
            edtTextMensagemChat.clearFocus();
            edtTextMensagemChat.setOnFocusChangeListener(null);
        }

        if (recyclerViewOnScrollListener != null) {
            recyclerViewChatRandom.removeOnScrollListener(recyclerViewOnScrollListener);
            recyclerViewOnScrollListener = null;
        }
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

    private void excluirAudioAnterior() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDirectory, "audioTemp" + ".mp3");
        Uri uriFile = Uri.fromFile(new File(file.getPath()));

        File fdelete = new File(uriFile.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                ToastCustomizado.toastCustomizadoCurto("Áudio descartado com sucesso", getApplicationContext());
                //System.out.println("file Deleted :" + uriFile.getPath());
            } else {
                ToastCustomizado.toastCustomizadoCurto("Erro ao descartar áudio, tente novamente", getApplicationContext());
                //System.out.println("file not Deleted :" + uriFile.getPath());
            }
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        ToastCustomizado.toastCustomizadoCurto("AAAAAAAA", getApplicationContext());

        switch (view.getId()) {
            case R.id.edtTextMensagemChatRandom:
                if (b) {
                    imgButtonSheetAudio.setVisibility(View.GONE);
                    imgButtonEnviarMensagemChat.setVisibility(View.VISIBLE);
                } else {
                    imgButtonEnviarMensagemChat.setVisibility(View.GONE);
                    imgButtonSheetAudio.setVisibility(View.VISIBLE);
                }
                break;
        }
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

    private String duracaoAudio() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDirectory, "audioTemp" + ".mp3");
        return file.getPath();
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

    private void enviarAudioSalvo() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        txtViewTempoAudio.setText("00:00");

        String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");

        StorageReference audioRef;

        audioRef = storageRef.child("chatRandom")
                .child("audios")
                .child(idUserLogado)
                .child(idUserD)
                .child("audio" + replaceAll + ".mp3");

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDirectory, "audioTemp" + ".mp3");

        Uri uriFile = Uri.fromFile(new File(file.getAbsolutePath()));

        //Verificando progresso do upload
        UploadTask uploadTask = audioRef.putFile(uriFile);
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
                audioRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        bottomSheetDialog.cancel();
                        ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar mensagem", getApplicationContext());
                        Uri url = task.getResult();
                        String urlNewPostagem = url.toString();

                        DatabaseReference referenciaRemetente = firebaseRef.child("chatRandom")
                                .child(idUserLogado).child(idUserD);

                        String idConversa = referenciaRemetente.push().getKey();

                        HashMap<String, Object> dadosMensagem = new HashMap<>();
                        dadosMensagem.put("idConversa", idConversa);
                        dadosMensagem.put("tipoMensagem", "audio");
                        dadosMensagem.put("idRemetente", idUserLogado);
                        dadosMensagem.put("idDestinatario", idUserD);
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
                            String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,\\s]", "");
                            dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
                        } else {
                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                            date = new Date();
                            String novaData = dateFormat.format(date);
                            dadosMensagem.put("dataMensagem", novaData);
                            dadosMensagem.put("dataMensagemCompleta", date);

                            String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,\\s]", "");
                            dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
                        }

                        referenciaRemetente.child(idConversa).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    DatabaseReference referenciaD = firebaseRef.child("chatRandom")
                                            .child(idUserD).child(idUserLogado);

                                    String idConversaD = referenciaD.push().getKey();

                                    dadosMensagem.put("idConversa", idConversaD);

                                    referenciaD.child(idConversaD).setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                atualizarContador();
                                                progressDialog.dismiss();
                                                edtTextMensagemChat.setText("");
                                            }
                                        }
                                    });
                                } else {
                                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                    progressDialog.dismiss();
                                }
                            }
                        });
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

    private void exibirAlertDialog() {
        builder.setTitle("Sair da conversa aleatória")
                .setMessage("Você não poderá voltar a essa conversa aleatória posteriormente.")
                .setPositiveButton("Sair da conversa", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog.setMessage("Ajustando chat random, aguarde um momento...");
                        if (!isFinishing()) {
                            progressDialog.show();
                        }
                        limparConversa(new LimparConversaCallback() {
                            @Override
                            public void onConversaExcluida() {
                                limparContador(this);
                            }

                            @Override
                            public void onContadorLimpo() {

                            }

                            @Override
                            public void onError(String message) {
                                ocultarProgressDialog();
                                finish();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Ação a ser executada quando o botão "Negative" for clicado
                        // Por exemplo, você pode cancelar alguma operação aqui
                        dialog.dismiss();
                    }
                });

        // Crie o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        // Exiba o AlertDialog
        alertDialog.show();
    }

    private void limparConversa(LimparConversaCallback callback) {
        DatabaseReference removerConversaAtualRef = firebaseRef.child("chatRandom")
                .child(idUserLogado).child(idUserD);
        DatabaseReference removerConversaDRef = firebaseRef.child("chatRandom")
                .child(idUserD).child(idUserLogado);
        removerConversaAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Remover do destinatário
                removerConversaDRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        removerDadosStorage(callback);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private void limparContador(LimparConversaCallback callback) {
        DatabaseReference removerContadorAtualRef = firebaseRef.child("contadorMensagensRandom")
                .child(idUserLogado).child(idUserD);
        DatabaseReference removerContadorDRef = firebaseRef.child("contadorMensagensRandom")
                .child(idUserD).child(idUserLogado);
        removerContadorAtualRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Remover do destinatário
                removerContadorDRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onContadorLimpo();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private void removerDadosStorage(LimparConversaCallback callback) {
        try {
            audioAtualRef = storageRef.child("chatRandom").child("audios")
                    .child(idUserLogado).child(idUserD);
            audioDRef = storageRef.child("chatRandom").child("audios")
                    .child(idUserD).child(idUserLogado);

            audioAtualRef.listAll().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    for (StorageReference item : listResult.getItems()) {
                        item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Arquivo excluído com sucesso
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Ocorreu um erro ao excluir o arquivo
                                callback.onError(e.getMessage());
                            }
                        });
                    }
                }
            });

            audioDRef.listAll().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    for (StorageReference item : listResult.getItems()) {
                        item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Arquivo excluído com sucesso
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Ocorreu um erro ao excluir o arquivo
                                callback.onError(e.getMessage());
                            }
                        });
                    }
                }
            });

            // Após excluir todos os arquivos no diretório, exclua o diretório em si
            audioAtualRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Diretório excluído com sucesso
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Ocorreu um erro ao excluir o diretório
                    callback.onError(e.getMessage());
                }
            });

            // Após excluir todos os arquivos no diretório, exclua o diretório em si
            audioDRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Diretório excluído com sucesso
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Ocorreu um erro ao excluir o diretório
                    callback.onError(e.getMessage());
                }
            });

            callback.onConversaExcluida();

        } catch (Exception ex) {
            callback.onConversaExcluida();
            ex.printStackTrace();
        }
    }

    private void removerValueEventListener() {
        if (valueEventListener != null) {
            verificaAddRandomRef.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    private void ocultarProgressDialog() {
        if (progressDialog != null && !isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void inicializandoComponentes() {
        imgBtnBackSairDaConversa = findViewById(R.id.imgBtnBackSairDaConversa);
        imgViewFotoDestinatario = findViewById(R.id.imgViewFotoDestinatarioRandom);
        txtViewNomeDestinatario = findViewById(R.id.txtViewNomeDestinatarioRandom);
        txtViewTimerChatRestante = findViewById(R.id.txtViewTimerChatRestante);

        recyclerViewChatRandom = findViewById(R.id.recyclerViewChatRandom);

        imgBtnEnviarGif = findViewById(R.id.imgButtonEnviarGifChat);
        edtTextMensagemChat = findViewById(R.id.edtTextMensagemChatRandom);
        imgButtonSheetAudio = findViewById(R.id.imgButtonSheetAudioRandom);
        imgButtonEnviarMensagemChat = findViewById(R.id.imgButtonEnviarMsgRandom);

        imgBtnScrollFirstMsg = findViewById(R.id.imgBtnScrollFirstMsgRandom);
        imgBtnScrollLastMsg = findViewById(R.id.imgBtnScrollLastMsgRandom);
    }
}