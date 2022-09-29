package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbarConversa;
    private ImageButton imgBtnBackConversa, imgButtonEnviarFotoChat;
    private Button btnTotalMensagensDestinatario;
    private ImageView imgViewFotoDestinatario, imgViewGifDestinatario;
    private TextView txtViewNomeDestinatario, txtViewNivelAmizadeDestinatario, txtViewNomeRecolhidoChat;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Usuario usuarioDestinatario;
    private Contatos contatoDestinatario;
    private Chip chipInteresse01, chipInteresse02, chipInteresse03, chipInteresse04, chipInteresse05;
    private EditText edtTextMensagemChat;
    private FloatingActionButton fabEnviarMensagemChat;
    private RecyclerView recyclerMensagensChat;
    //Variáveis para data
    private DateFormat dateFormat;
    private Date date;
    private String localConvertido;
    private Locale current;
    private AdapterMensagem adapterMensagem;
    private List<Mensagem> listaMensagem = new ArrayList<>();
    private ChildEventListener childEventListener;
    private DatabaseReference recuperarMensagensRef;
    private LinearLayout linearInfosDestinatario, linearInfosRecolhidas;
    private ImageView imgViewRecolherInfo, imgViewExpandirInfo, imgViewFotoRecolhidaChat;

    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private StorageReference imagemRef, videoRef;
    private StorageReference storageRef;
    private static final int SELECAO_CAMERA = 100,
            SELECAO_GALERIA = 200,
            SELECAO_GIF = 300,
            SELECAO_VIDEO = 400;
    private String selecionadoCamera, selecionadoGaleria;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private ProgressDialog progressDialog;

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
        adapterMensagem.releasePlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        buscarMensagens();
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
        Permissao.validarPermissoes(permissoesNecessarias, ConversaActivity.this, 1);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            contatoDestinatario = (Contatos) dados.getSerializable("contato");
            usuarioDestinatario = (Usuario) dados.getSerializable("usuario");
            exibirDadosDestinatario();
            recuperarMensagensRef = firebaseRef.child("conversas")
                    .child(idUsuario).child(usuarioDestinatario.getIdUsuario());
        }

        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), imgButtonEnviarFotoChat);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_anexo, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.anexoCamera:
                        ToastCustomizado.toastCustomizadoCurto("Câmera", getApplicationContext());
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
                        ToastCustomizado.toastCustomizadoCurto("Galeria", getApplicationContext());
                        //Passando a intenção de selecionar uma foto pela galeria
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        //Verificando se a intenção foi atendida com sucesso
                        if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                            startActivityForResult(i, SELECAO_GALERIA);
                        }
                        return true;
                    case R.id.anexoDocumento:
                        ToastCustomizado.toastCustomizadoCurto("Documento", getApplicationContext());
                        return true;
                    case R.id.anexoVideo:
                        ToastCustomizado.toastCustomizadoCurto("Video", getApplicationContext());
                        enviarVideo();
                        return true;
                    case R.id.anexoGif:
                        ToastCustomizado.toastCustomizadoCurto("Gif", getApplicationContext());
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

        imgViewRecolherInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearInfosDestinatario.setVisibility(View.GONE);
                imgViewRecolherInfo.setVisibility(View.GONE);
                imgViewExpandirInfo.setVisibility(View.VISIBLE);

                linearInfosRecolhidas.setVisibility(View.VISIBLE);
            }
        });

        imgViewExpandirInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearInfosRecolhidas.setVisibility(View.GONE);
                imgViewExpandirInfo.setVisibility(View.GONE);
                imgViewRecolherInfo.setVisibility(View.VISIBLE);
                linearInfosDestinatario.setVisibility(View.VISIBLE);
            }
        });

        fabEnviarMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensagem();
            }
        });

        //Configurando recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagensChat.setLayoutManager(linearLayoutManager);
        recyclerMensagensChat.setHasFixedSize(true);
        if (adapterMensagem != null) {

        } else {
            adapterMensagem = new AdapterMensagem(getApplicationContext(), listaMensagem);
        }
        recyclerMensagensChat.setAdapter(adapterMensagem);
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
                                    ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                    atualizarContador();
                                    progressDialog.dismiss();
                                    edtTextMensagemChat.setText("");
                                } else {
                                    ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                    progressDialog.dismiss();
                                }
                            }
                        });

                salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                    edtTextMensagemChat.setText("");
                                }
                            }
                        });

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
                listaMensagem.add(mensagem);
                adapterMensagem.notifyDataSetChanged();
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
                                ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                atualizarContador();
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });

            salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });
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
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(mensagem1.getTotalMensagens() + 1);
                } else {
                    ToastCustomizado.toastCustomizadoCurto("primeiro", getApplicationContext());
                    verificaContadorRef.child("totalMensagens").setValue(1);
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(1);
                }
                verificaContadorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exibirDadosDestinatario() {
        if (usuarioDestinatario != null) {
            if (usuarioDestinatario.getEpilepsia().equals("Sim")) {
                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                        imgViewFotoDestinatario, android.R.color.transparent);
                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                        imgViewFotoRecolhidaChat, android.R.color.transparent);
            } else {
                GlideCustomizado.montarGlide(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                        imgViewFotoDestinatario, android.R.color.transparent);
                GlideCustomizado.montarGlide(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                        imgViewFotoRecolhidaChat, android.R.color.transparent);
            }
            if (usuarioDestinatario.getExibirApelido().equals("sim")) {
                txtViewNomeDestinatario.setText(usuarioDestinatario.getApelidoUsuario());
                txtViewNomeRecolhidoChat.setText(usuarioDestinatario.getApelidoUsuario());
            } else {
                txtViewNomeDestinatario.setText(usuarioDestinatario.getNomeUsuario());
                txtViewNomeRecolhidoChat.setText(usuarioDestinatario.getNomeUsuario());
            }
            txtViewNivelAmizadeDestinatario.setText("Nível de amizade: " + contatoDestinatario.getNivelAmizade());
            GlideCustomizado.montarGlideFoto(getApplicationContext(), "https://media.giphy.com/media/9dtArMyxofHqXhziUk/giphy.gif",
                    imgViewGifDestinatario, android.R.color.transparent);

            btnTotalMensagensDestinatario.setText(contatoDestinatario.getTotalMensagens() + " Mensagens");
            preencherChipsInteresses();
        }
    }

    private void preencherChipsInteresses() {
        if (usuarioDestinatario.getInteresses().size() == 1) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
        } else if (usuarioDestinatario.getInteresses().size() == 2) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
        } else if (usuarioDestinatario.getInteresses().size() == 3) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
        } else if (usuarioDestinatario.getInteresses().size() == 4) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
            chipInteresse04.setText(usuarioDestinatario.getInteresses().get(3));
        } else if (usuarioDestinatario.getInteresses().size() == 5) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
            chipInteresse04.setText(usuarioDestinatario.getInteresses().get(3));
            chipInteresse05.setText(usuarioDestinatario.getInteresses().get(4));
        }

        imgBtnBackConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        toolbarConversa = findViewById(R.id.toolbarConversa);
        imgBtnBackConversa = findViewById(R.id.imgBtnBackConversa);
        btnTotalMensagensDestinatario = findViewById(R.id.btnTotalMensagensDestinatario);
        imgViewFotoDestinatario = findViewById(R.id.imgViewFotoDestinatario);
        imgViewGifDestinatario = findViewById(R.id.imgViewGifDestinatario);
        txtViewNomeDestinatario = findViewById(R.id.txtViewNomeDestinatario);
        txtViewNivelAmizadeDestinatario = findViewById(R.id.txtViewNivelAmizadeDestinatario);
        chipInteresse01 = findViewById(R.id.chipInteresse01);
        chipInteresse02 = findViewById(R.id.chipInteresse02);
        chipInteresse03 = findViewById(R.id.chipInteresse03);
        chipInteresse04 = findViewById(R.id.chipInteresse04);
        chipInteresse05 = findViewById(R.id.chipInteresse05);
        edtTextMensagemChat = findViewById(R.id.edtTextMensagemChat);
        fabEnviarMensagemChat = findViewById(R.id.fabEnviarMensagemChat);
        imgButtonEnviarFotoChat = findViewById(R.id.imgButtonEnviarFotoChat);
        recyclerMensagensChat = findViewById(R.id.recyclerMensagensChat);
        linearInfosDestinatario = findViewById(R.id.linearInfosDestinatario);
        imgViewRecolherInfo = findViewById(R.id.imgViewRecolherInfo);
        imgViewExpandirInfo = findViewById(R.id.imgViewExpandirInfo);
        linearInfosRecolhidas = findViewById(R.id.linearInfosRecolhidas);
        imgViewFotoRecolhidaChat = findViewById(R.id.imgViewFotoRecolhidaChat);
        txtViewNomeRecolhidoChat = findViewById(R.id.txtViewNomeRecolhidoChat);
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
                        .child("foto" + nomeRandomico + ".jpeg");
                //Verificando progresso do upload
                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
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
                                                    ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    atualizarContador();
                                                    progressDialog.dismiss();
                                                    edtTextMensagemChat.setText("");
                                                } else {
                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                                salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                                        .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                    edtTextMensagemChat.setText("");
                                                }
                                            }
                                        });
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
                    .child("video" + nomeRandomico + ".mp4");

            String path = String.valueOf(Matisse.obtainResult(data).get(0));
            Uri videoUri;
            videoUri = Uri.parse(path);
            UploadTask uploadTask = videoRef.putFile(videoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem, tente novamente",getApplicationContext());
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
                                                ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                atualizarContador();
                                                progressDialog.dismiss();
                                                edtTextMensagemChat.setText("");
                                            } else {
                                                ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });

                            salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                                edtTextMensagemChat.setText("");
                                            }
                                        }
                                    });
                        }
                    });
                }
            });
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
}