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
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterContato;
import com.example.ogima.adapter.AdapterMensagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConversaActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private Toolbar toolbarConversa;
    private ImageButton imgBtnBackConversa, imgButtonEnviarFotoChat,
    imgBtnVideoCall, imgBtnVoiceCall;
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
    private List<Mensagem> listaMensagem = new ArrayList<>();
    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListenerTeste;
    private DatabaseReference recuperarMensagensRef;

    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
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
    private BottomSheetDialog bottomSheetDialog;
    private TextView txtViewTempoAudio, txtViewNomeDestinatario;
    private ImageView imgViewFotoDestinatario;
    private ImageButton imgButtonCancelarAudio, imgButtonEnviarAudio,
            imgButtonGravarAudio, imgButtonStopAudio, imgButtonPlayAudio,
            imgButtonPauseAudio;

    private String scrollLast;
    private LinearLayoutManager linearLayoutManager;
    private String somenteInicio;
    private DatabaseReference usuarioRef;

    @Override
    protected void onStop() {
        super.onStop();
        /*
        try {
            if (adapterMensagem.exoPlayerMensagem != null) {
                adapterMensagem.pausePlayer();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         */
        recuperarMensagensRef.removeEventListener(childEventListener);
        listaMensagem.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        buscarMensagens();
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

    /*

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (adapterMensagem.exoPlayerMensagem != null) {
                adapterMensagem.pausePlayer();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            if (adapterMensagem.exoPlayerMensagem != null) {
                adapterMensagem.startPlayer();
                adapterMensagem.seekTo();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (adapterMensagem.exoPlayerMensagem != null) {
                adapterMensagem.releasePlayer();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
     */

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);
        inicializandoComponentes();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurando data de acordo com local do usuário.
        current = getResources().getConfiguration().locale;
        localConvertido = localConvertido.valueOf(current);

        //Validar permissões necessárias para adição de fotos.
        Permissao.validarPermissoes(permissoesNecessarias, ConversaActivity.this, 17);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            contatoDestinatario = (Contatos) dados.getSerializable("contato");
            usuarioDestinatario = (Usuario) dados.getSerializable("usuario");
            recuperarMensagensRef = firebaseRef.child("conversas")
                    .child(idUsuario).child(usuarioDestinatario.getIdUsuario());
        }

        infosDestinatario();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        edtTextMensagemChat.setOnFocusChangeListener(this::onFocusChange);

        bottomSheetDialog = new BottomSheetDialog(ConversaActivity.this);
        bottomSheetDialog.setContentView(R.layout.audio_bottom_sheet_dialog);

        imgButtonCancelarAudio = bottomSheetDialog.findViewById(R.id.imgButtonCancelarAudio);
        imgButtonEnviarAudio = bottomSheetDialog.findViewById(R.id.imgButtonEnviarAudio);
        imgButtonGravarAudio = bottomSheetDialog.findViewById(R.id.imgButtonGravarAudio);
        imgButtonStopAudio = bottomSheetDialog.findViewById(R.id.imgButtonStopAudio);
        imgButtonPlayAudio = bottomSheetDialog.findViewById(R.id.imgButtonPlayAudio);
        imgButtonPauseAudio = bottomSheetDialog.findViewById(R.id.imgButtonPauseAudio);
        txtViewTempoAudio = bottomSheetDialog.findViewById(R.id.txtViewTempoAudio);

        imgBtnBackConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

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

                    imgButtonEnviarMensagemChat.setVisibility(View.GONE);
                    edtTextMensagemChat.clearFocus();

                    imgButtonCancelarAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imgButtonGravarAudio.setClickable(true);
                            bottomSheetDialog.cancel();
                            excluirAudioAnterior();
                            txtViewTempoAudio.setText("00:00");
                            imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));

                            running = false;
                            wasRunning = running;
                            seconds = 0;
                            handler.removeCallbacksAndMessages(null);

                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                        }
                    });

                    imgButtonGravarAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isMicrophonePresent()) {
                                imgButtonGravarAudio.setClickable(false);
                                imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 255, 0, 0));
                                gravarAudio();
                                runTimer();
                            }
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

                    imgButtonEnviarAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            running = false;
                            wasRunning = running;
                            seconds = 0;
                            handler.removeCallbacksAndMessages(null);

                            String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

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

                                                String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
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

                                                String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                                                String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                                dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
                                            }

                                            DatabaseReference salvarMensagem = firebaseRef.child("conversas");

                                            salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
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

                                            salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
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
                    });
                    bottomSheetDialog.show();
                    bottomSheetDialog.setCancelable(false);
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Aceite as permissões para que seja possível gravar seu áudio", getApplicationContext());
                    checkRecordingPermission();
                }
            }
        });

        //Seleção de envio de arquivos - foto/camêra/gif/música/documento
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), imgButtonEnviarFotoChat);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_anexo, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.anexoCamera:
                        //Chama o crop de camêra
                        selecionadoCamera = "sim";
                        ImagePicker.Companion.with(ConversaActivity.this)
                                .cameraOnly()
                                .crop()                    //Crop image(Optional), Check Customization for more option
                                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                                //.maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                                .start(101);
                        return true;
                    case R.id.anexoGaleria:
                        //Passando a intenção de selecionar uma foto pela galeria
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        //Verificando se a intenção foi atendida com sucesso
                        if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                            startActivityForResult(i, SELECAO_GALERIA);
                        }
                        return true;
                    case R.id.anexoMusica:
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
                        return true;
                    case R.id.anexoDocumento:
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
                        return true;
                    case R.id.anexoVideo:
                        enviarVideo();
                        return true;
                    case R.id.anexoGif:
                        enviarGif();
                        return true;
                }
                return false;
            }
        });

        imgButtonEnviarFotoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });

        imgButtonEnviarMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensagem();
            }
        });

        somenteInicio = "sim";

        recyclerMensagensChat.addOnScrollListener(recyclerViewOnScrollListener);

        //Configurando recycler
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagensChat.setHasFixedSize(true);
        //linearLayoutManager.setStackFromEnd(true);
        recyclerMensagensChat.setLayoutManager(linearLayoutManager);
        if (adapterMensagem != null) {
        } else {
            adapterMensagem = new AdapterMensagem(getApplicationContext(), listaMensagem);
        }
        recyclerMensagensChat.setAdapter(adapterMensagem);

        //Chamada de vídeo
        imgBtnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), VideoCallActivity.class);
                intent.putExtra("usuario",usuarioDestinatario);
                startActivity(intent);
            }
        });

        //Chamada de voz
        imgBtnVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference remetenteTalkKey = firebaseRef.child("keyConversation")
                        .child(idUsuario).child(usuarioDestinatario.getIdUsuario());
                remetenteTalkKey.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Mensagem mensagem = snapshot.getValue(Mensagem.class);
                            Intent intent = new Intent(getApplicationContext(), VoiceCallActivity.class);
                            intent.putExtra("usuario",usuarioDestinatario);
                            intent.putExtra("talkKeyMensagem", mensagem);
                            startActivity(intent);
                        }else{
                          String randomKey = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                            remetenteTalkKey.child("talkKey").setValue(randomKey+idUsuario);

                            DatabaseReference destinatarioTalkKey = firebaseRef.child("keyConversation")
                                    .child(usuarioDestinatario.getIdUsuario()).child(idUsuario);
                            destinatarioTalkKey.child("talkKey").setValue(randomKey+idUsuario);
                            Mensagem mensagemNova = new Mensagem();
                            mensagemNova.setTalkKey(randomKey+idUsuario);
                            Intent intent = new Intent(getApplicationContext(), VoiceCallActivity.class);
                            intent.putExtra("usuario",usuarioDestinatario);
                            intent.putExtra("talkKeyMensagem", mensagemNova);
                            startActivity(intent);
                        }
                        remetenteTalkKey.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void infosDestinatario() {
        if (usuarioDestinatario.getExibirApelido().equals("sim")) {
            txtViewNomeDestinatario.setText(usuarioDestinatario.getApelidoUsuario());
        }else{
            txtViewNomeDestinatario.setText(usuarioDestinatario.getNomeUsuario());
        }

        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                    if (usuarioAtual.getEpilepsia().equals("Sim")) {
                        GlideCustomizado.montarGlideEpilepsia(getApplicationContext(),
                                usuarioDestinatario.getMinhaFoto(),
                                imgViewFotoDestinatario,
                                android.R.color.transparent);
                    } else if (usuarioAtual.getEpilepsia().equals("Não")) {
                        GlideCustomizado.montarGlide(getApplicationContext(),
                                usuarioDestinatario.getMinhaFoto(),
                                imgViewFotoDestinatario,
                                android.R.color.transparent);
                    }
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

                // Formatado com minutos e segundos com limite de 5 minutos
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
                    String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                    dadosMensagem.put("nomeDocumento", replaceAll + ".gif");
                } else {
                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                    date = new Date();
                    String novaData = dateFormat.format(date);
                    dadosMensagem.put("dataMensagem", novaData);
                    dadosMensagem.put("dataMensagemCompleta", date);
                    String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                    dadosMensagem.put("nomeDocumento", replaceAll + ".gif");
                }

                DatabaseReference salvarMensagem = firebaseRef.child("conversas");

                salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
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

                salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
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

        //ToastCustomizado.toastCustomizadoCurto("Oi " + scrollLast, getApplicationContext());

        childEventListener = recuperarMensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                adapterMensagem.adicionarItem(mensagem);

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
                }else{
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

            DatabaseReference salvarMensagem = firebaseRef.child("conversas");

            salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                atualizarContador();
                            }
                        }
                    });

            salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
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
        DatabaseReference verificaContadorRef = firebaseRef.child("contadorMensagens")
                .child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario());
        DatabaseReference verificaContadorDestinatarioRef = firebaseRef.child("contadorMensagens")
                .child(usuarioDestinatario.getIdUsuario())
                .child(idUsuario);
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
        recyclerMensagensChat = findViewById(R.id.recyclerMensagensChat);
        imgButtonSheetAudio = findViewById(R.id.imgButtonSheetAudio);
        txtViewNomeDestinatario = findViewById(R.id.txtViewNomeDestinatario);
        imgViewFotoDestinatario = findViewById(R.id.imgViewFotoDestinatario);
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
                                    String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
                                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                    dadosMensagem.put("nomeDocumento", replaceAll + ".jpg");
                                } else {
                                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosMensagem.put("dataMensagem", novaData);
                                    dadosMensagem.put("dataMensagemCompleta", date);
                                    String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                                    String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                    dadosMensagem.put("nomeDocumento", replaceAll + ".jpg");
                                }

                                DatabaseReference salvarMensagem = firebaseRef.child("conversas");

                                salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
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

                                salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
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
                                String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
                                String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                dadosMensagem.put("nomeDocumento", replaceAll + ".mp4");
                            } else {
                                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                date = new Date();
                                String novaData = dateFormat.format(date);
                                dadosMensagem.put("dataMensagem", novaData);
                                dadosMensagem.put("dataMensagemCompleta", date);
                                String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                                String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");
                                dadosMensagem.put("nomeDocumento", replaceAll + ".mp4");
                            }

                            DatabaseReference salvarMensagem = firebaseRef.child("conversas");

                            salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
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

                            salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
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

                                DatabaseReference salvarMensagem = firebaseRef.child("conversas");

                                salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
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

                                salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
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

                                DatabaseReference salvarMensagem = firebaseRef.child("conversas");

                                salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
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

                                salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
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
                } else {
                    imgButtonEnviarMensagemChat.setVisibility(View.GONE);
                }
                break;
        }
    }

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
            handler.removeCallbacksAndMessages(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgButtonGravarAudio.getBackground().setTint(Color.argb(100, 0, 115, 255));
            }
            ToastCustomizado.toastCustomizadoCurto("Audio finalizado", getApplicationContext());
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
                new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
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

            boolean endHasBeenReached = lastVisible + 5 >= totalItemCount;
            if (totalItemCount > 0 && endHasBeenReached) {
                //ToastCustomizado.toastCustomizadoCurto("Ultimo",getApplicationContext());
                scrollLast = "sim";
            }else{
                scrollLast = null;
            }
        }
    };
}