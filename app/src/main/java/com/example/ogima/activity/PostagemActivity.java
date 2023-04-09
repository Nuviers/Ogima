package com.example.ogima.activity;

import static android.app.Activity.RESULT_OK;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.bumptech.glide.load.resource.gif.GifBitmapProvider;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GiphyUtils;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
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
import com.google.firebase.auth.FirebaseAuth;
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
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class PostagemActivity extends AppCompatActivity {

    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    //Referências
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private StorageReference imagemRef, videoRef;
    private StorageReference storageRef;
    //Dados para o usuário atual
    private String emailUsuario, idUsuario;
    //Variáveis para data
    private DateFormat dateFormat;
    private Date date;
    private String localConvertido;
    private Locale current;
    //Dados para o corte de foto
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    //Constantes passando um result code
    private static final int SELECAO_CAMERA_POSTAGEM = 100,
            SELECAO_GALERIA_POSTAGEM = 200,
            SELECAO_GIF_POSTAGEM = 300,
            SELECAO_VIDEO_POSTAGEM = 400;
    //Somente é preenchida quando a camêra é selecionada.
    private String selecionadoCameraPostagem, selecionadoGaleriaPostagem;
    //Componentes
    private ImageButton imgButtonVoltarPostagemPerfil, imgBtnAddCameraPostagem,
            imgBtnAddGaleriaPostagem, imgBtnAddGifPostagem,
            imgBtnAddVideoPostagem;
    private ProgressDialog progressDialog;
    private ArrayList<String> listaUrlPostagemUpdate = new ArrayList<>();
    private int novoContador;
    private DatabaseReference atualizarContadorPostagemRef;

    private Uri uriss;
    private Intent datas;

    //Giphy
    private final GiphyUtils giphyUtils = new GiphyUtils();
    private GiphyDialogFragment gdl;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        intent.putExtra("irParaPerfil", "irParaPerfil");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postagem);
        inicializandoComponentes();

        //TesteCommitNewConfig

        //Configurações iniciais
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        //Validar permissões necessárias para adição de fotos.
        Permissao.validarPermissoes(permissoesNecessarias, PostagemActivity.this, 1);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        //Configurando data de acordo com local do usuário.
        current = getResources().getConfiguration().locale;
        localConvertido = localConvertido.valueOf(current);
        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        imgButtonVoltarPostagemPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Evento de clique para adicionar uma postagem pela camêra
        imgBtnAddCameraPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Chama o crop de camêra
                selecionadoCameraPostagem = "sim";
                ImagePicker.Companion.with(PostagemActivity.this)
                        .cameraOnly()
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        //.maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start(101);
            }
        });

        //Evento de clique para adicionar uma postagem pela galeria
        imgBtnAddGaleriaPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Passando a intenção de selecionar uma foto pela galeria
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Verificando se a intenção foi atendida com sucesso
                if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA_POSTAGEM);
                }
            }
        });

        //Evento de clique para adicionar uma gif
        imgBtnAddGifPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postarGif();
            }
        });

        imgBtnAddVideoPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postarVideo();
            }
        });
    }

    private void postarGif() {

        giphyUtils.selectGif(getApplicationContext(), new GiphyUtils.GifSelectionListener() {
            @Override
            public void onGifSelected(String gifPequena, String gifMedio, String gifOriginal) {
                DatabaseReference contadorPostagensRef = firebaseRef
                        .child("complementoPostagem").child(idUsuario);
                atualizarContadorPostagemRef = firebaseRef
                        .child("complementoPostagem").child(idUsuario)
                        .child("totalPostagens");

                //Verificando se existem postagens ou fotos
                DatabaseReference verificaPostagensRef = firebaseRef.child("todasPostagens")
                        .child(idUsuario);
                verificaPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Postagem postagemTotal = snapshot.getValue(Postagem.class);
                            contadorPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        //Se cair nessa condição, já existem postagens
                                        //desse usuário (Existem postagens)
                                        Postagem contadorPostagem = snapshot.getValue(Postagem.class);
                                        progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                                        progressDialog.show();
                                        verificaPostagensRef.child("totalPostagens").setValue(postagemTotal.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    atualizarContadorPostagemRef.setValue(contadorPostagem.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            int receberContador = postagemTotal.getTotalPostagens() + 1;
                                                            //Caminho para o storage
                                                            imagemRef = storageRef
                                                                    .child("postagens")
                                                                    .child("gifs")
                                                                    .child(idUsuario)
                                                                    .child("gif" + receberContador + ".gif");
                                                            //Verificando progresso do upload

                                                            ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());
                                                            //ToastCustomizado.toastCustomizadoCurto("URI " + gif_url, getApplicationContext());
                                                            //Log.i("GIF","gif url " + gif_url);

                                                            int atualizarContador = postagemTotal.getTotalPostagens() + 1;
                                                            DatabaseReference salvarPostagemRef = firebaseRef
                                                                    .child("postagens").child(idUsuario).child(idUsuario + atualizarContador);

                                                            salvarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        novoContador = postagemTotal.getTotalPostagens() + 2;
                                                                    } else {
                                                                        novoContador = postagemTotal.getTotalPostagens() + 1;
                                                                    }

                                                                    HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                    dadosPostagemExistente.put("idPostagem", idUsuario + novoContador);
                                                                    dadosPostagemExistente.put("urlPostagem", gifOriginal);
                                                                    dadosPostagemExistente.put("tipoPostagem", "Gif");
                                                                    if (localConvertido.equals("pt_BR")) {
                                                                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                        date = new Date();
                                                                        String novaData = dateFormat.format(date);
                                                                        dadosPostagemExistente.put("dataPostagem", novaData);
                                                                        dadosPostagemExistente.put("dataPostagemNova", date);
                                                                    } else {
                                                                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                        date = new Date();
                                                                        String novaData = dateFormat.format(date);
                                                                        dadosPostagemExistente.put("dataPostagem", novaData);
                                                                        dadosPostagemExistente.put("dataPostagemNova", date);
                                                                    }
                                                                    dadosPostagemExistente.put("tituloPostagem", "");
                                                                    dadosPostagemExistente.put("descricaoPostagem", "");
                                                                    dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                    dadosPostagemExistente.put("publicoPostagem", "Todos");
                                                                    dadosPostagemExistente.put("totalViewsFotoPostagem", 0);

                                                                    DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                            .child(idUsuario).child("listaUrlPostagens");

                                                                    if (contadorPostagem.getTotalPostagens() < 4) {
                                                                        listaUrlPostagemUpdate = contadorPostagem.getListaUrlPostagens();
                                                                        listaUrlPostagemUpdate.add(gifOriginal);
                                                                        Collections.sort(listaUrlPostagemUpdate, Collections.reverseOrder());
                                                                        postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                                    } else {
                                                                        listaUrlPostagemUpdate = contadorPostagem.getListaUrlPostagens();
                                                                        Collections.sort(listaUrlPostagemUpdate, Collections.reverseOrder());
                                                                        ArrayList<String> arrayReordenado = new ArrayList<>();
                                                                        arrayReordenado.add(0, gifOriginal);
                                                                        arrayReordenado.add(1, listaUrlPostagemUpdate.get(0));
                                                                        arrayReordenado.add(2, listaUrlPostagemUpdate.get(1));
                                                                        arrayReordenado.add(3, listaUrlPostagemUpdate.get(2));
                                                                        postagensExibidasRef.setValue(arrayReordenado);
                                                                    }


                                                                    salvarPostagemRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                progressDialog.dismiss();
                                                                                //Enviando imagem postada para edição de foto em outra activity.
                                                                                Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                i.putExtra("fotoOriginal", gifOriginal);
                                                                                i.putExtra("idPostagem", idUsuario + novoContador);
                                                                                i.putExtra("postagemGif", "postagemGif");
                                                                                i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                startActivity(i);
                                                                            } else {
                                                                                ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                            }
                                                                        }
                                                                    });

                                                                    salvarPostagemRef.removeEventListener(this);
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else {
                                        //Se cair nessa condição, não existem postagens
                                        //mas podem existir fotos
                                        progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                                        progressDialog.show();

                                        DatabaseReference dadosFotosUsuarioRef = firebaseRef
                                                .child("complementoFoto").child(idUsuario);
                                        dadosFotosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.getValue() != null) {
                                                    //Existem fotos
                                                    verificaPostagensRef.child("totalPostagens").setValue(postagemTotal.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            try {
                                                                                int receberContador = 1;
                                                                                //Caminho para o storage
                                                                                imagemRef = storageRef
                                                                                        .child("postagens")
                                                                                        .child("gifs")
                                                                                        .child(idUsuario)
                                                                                        .child("gif" + receberContador + ".gif");
                                                                                int atualizarContador = postagemTotal.getTotalPostagens() + 1;
                                                                                DatabaseReference salvarPostagemRef = firebaseRef
                                                                                        .child("postagens").child(idUsuario).child(idUsuario + atualizarContador);
                                                                                salvarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                        if (snapshot.exists()) {
                                                                                            novoContador = postagemTotal.getTotalPostagens() + 2;
                                                                                        } else {
                                                                                            novoContador = postagemTotal.getTotalPostagens() + 1;
                                                                                        }

                                                                                        HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                                        dadosPostagemExistente.put("idPostagem", idUsuario + novoContador);
                                                                                        dadosPostagemExistente.put("urlPostagem", gifOriginal);
                                                                                        dadosPostagemExistente.put("tipoPostagem", "Gif");
                                                                                        if (localConvertido.equals("pt_BR")) {
                                                                                            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                            date = new Date();
                                                                                            String novaData = dateFormat.format(date);
                                                                                            dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                            dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                        } else {
                                                                                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                            date = new Date();
                                                                                            String novaData = dateFormat.format(date);
                                                                                            dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                            dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                        }
                                                                                        dadosPostagemExistente.put("tituloPostagem", "");
                                                                                        dadosPostagemExistente.put("descricaoPostagem", "");
                                                                                        dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                                        dadosPostagemExistente.put("publicoPostagem", "Todos");
                                                                                        dadosPostagemExistente.put("totalViewsFotoPostagem", 0);

                                                                                        DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                                .child(idUsuario).child("listaUrlPostagens");

                                                                                        listaUrlPostagemUpdate.add(gifOriginal);
                                                                                        postagensExibidasRef.setValue(listaUrlPostagemUpdate);

                                                                                        postagensExibidasRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        progressDialog.dismiss();
                                                                                                        //Enviando imagem postada para edição de foto em outra activity.
                                                                                                        Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                                        i.putExtra("fotoOriginal", gifOriginal);
                                                                                                        i.putExtra("idPostagem", idUsuario + novoContador);
                                                                                                        i.putExtra("postagemGif", "postagemGif");
                                                                                                        i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                        startActivity(i);
                                                                                                    } else {
                                                                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                        salvarPostagemRef.removeEventListener(this);
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                                    }
                                                                                });
                                                                            } catch (Exception ex) {
                                                                                ex.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    //Não existe nenhum tipo de postagem
                                                    HashMap<String, Object> dadosNovaPostagem = new HashMap<>();
                                                    dadosNovaPostagem.put("idPostagem", idUsuario + 1);
                                                    //Salvar imagem no firebase
                                                    imagemRef = storageRef
                                                            .child("postagens")
                                                            .child("gifs")
                                                            .child(idUsuario)
                                                            .child("gif" + 1 + ".gif");

                                                    verificaPostagensRef.child("totalPostagens").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());

                                                                        dadosNovaPostagem.put("urlPostagem", gifOriginal);
                                                                        if (localConvertido.equals("pt_BR")) {
                                                                            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                            date = new Date();
                                                                            String novaData = dateFormat.format(date);
                                                                            dadosNovaPostagem.put("dataPostagem", novaData);
                                                                            dadosNovaPostagem.put("dataPostagemNova", date);
                                                                        } else {
                                                                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                            date = new Date();
                                                                            String novaData = dateFormat.format(date);
                                                                            dadosNovaPostagem.put("dataPostagem", novaData);
                                                                            dadosNovaPostagem.put("dataPostagemNova", date);
                                                                        }
                                                                        dadosNovaPostagem.put("tipoPostagem", "Gif");
                                                                        //Salvando o título da postagem.
                                                                        dadosNovaPostagem.put("tituloPostagem", "");
                                                                        //Salvando a descrição da postagem.
                                                                        dadosNovaPostagem.put("descricaoPostagem", "");
                                                                        //Salvando o id do usuario
                                                                        dadosNovaPostagem.put("idDonoPostagem", idUsuario);
                                                                        dadosNovaPostagem.put("totalViewsFotoPostagem", 0);
                                                                        dadosNovaPostagem.put("publicoPostagem", "Todos");

                                                                        DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                .child(idUsuario).child("listaUrlPostagens");
                                                                        listaUrlPostagemUpdate.add(gifOriginal);
                                                                        postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                                        DatabaseReference salvarPostagemRef = firebaseRef
                                                                                .child("postagens").child(idUsuario).child(idUsuario + 1);
                                                                        salvarPostagemRef.setValue(dadosNovaPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    progressDialog.dismiss();
                                                                                    //Enviando imagem para edição de foto para outra activity.
                                                                                    Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                    i.putExtra("fotoOriginal", gifOriginal);
                                                                                    i.putExtra("idPostagem", idUsuario + 1);
                                                                                    i.putExtra("postagemGif", "postagemGif");
                                                                                    i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(i);
                                                                                } else {
                                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                                dadosFotosUsuarioRef.removeEventListener(this);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    contadorPostagensRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            //Se cair nessa condição, não existem postagens
                            //desse usuário (Não existem postagens)
                            progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                            progressDialog.show();
                            HashMap<String, Object> dadosNovaPostagem = new HashMap<>();
                            dadosNovaPostagem.put("idPostagem", idUsuario + 1);
                            //Salvar imagem no firebase
                            imagemRef = storageRef
                                    .child("postagens")
                                    .child("gifs")
                                    .child(idUsuario)
                                    .child("gif" + 1 + ".gif");

                            verificaPostagensRef.child("totalPostagens").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());

                                                dadosNovaPostagem.put("urlPostagem", gifOriginal);
                                                if (localConvertido.equals("pt_BR")) {
                                                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                    date = new Date();
                                                    String novaData = dateFormat.format(date);
                                                    dadosNovaPostagem.put("dataPostagem", novaData);
                                                    dadosNovaPostagem.put("dataPostagemNova", date);
                                                } else {
                                                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                    date = new Date();
                                                    String novaData = dateFormat.format(date);
                                                    dadosNovaPostagem.put("dataPostagem", novaData);
                                                    dadosNovaPostagem.put("dataPostagemNova", date);
                                                }
                                                dadosNovaPostagem.put("tipoPostagem", "Gif");
                                                //Salvando o título da postagem.
                                                dadosNovaPostagem.put("tituloPostagem", "");
                                                //Salvando a descrição da postagem.
                                                dadosNovaPostagem.put("descricaoPostagem", "");
                                                //Salvando o id do usuario
                                                dadosNovaPostagem.put("idDonoPostagem", idUsuario);
                                                dadosNovaPostagem.put("totalViewsFotoPostagem", 0);
                                                dadosNovaPostagem.put("publicoPostagem", "Todos");

                                                DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                        .child(idUsuario).child("listaUrlPostagens");
                                                listaUrlPostagemUpdate.add(gifOriginal);
                                                postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                DatabaseReference salvarPostagemRef = firebaseRef
                                                        .child("postagens").child(idUsuario).child(idUsuario + 1);
                                                salvarPostagemRef.setValue(dadosNovaPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            progressDialog.dismiss();
                                                            //Enviando imagem para edição de foto para outra activity.
                                                            Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                            i.putExtra("fotoOriginal", gifOriginal);
                                                            i.putExtra("idPostagem", idUsuario + 1);
                                                            i.putExtra("postagemGif", "postagemGif");
                                                            i.putExtra("tipoPostagem", "tipoPostagem");
                                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                        } else {
                                                            ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                        }
                                                    }
                                                });

                                            }
                                        });
                                    }
                                }
                            });
                        }
                        verificaPostagensRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        gdl = giphyUtils.retornarGiphyDialog();
        gdl.show(PostagemActivity.this.getSupportFragmentManager(), "PostagemActivity");
    }

    private void postarVideo() {

        //Para Video
        Matisse.from(PostagemActivity.this)
                .choose(MimeType.ofVideo())
                .countable(true)
                .maxSelectable(1)
                .showSingleMediaType(true)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(SELECAO_VIDEO_POSTAGEM);
    }

    private void postarGifMatisse() {
        //Para gif
        Matisse.from(PostagemActivity.this)
                .choose(MimeType.ofVideo())
                .countable(true)
                .maxSelectable(1)
                .showSingleMediaType(true)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(SELECAO_GIF_POSTAGEM);
    }


    private void inicializandoComponentes() {
        imgButtonVoltarPostagemPerfil = findViewById(R.id.imgButtonVoltarPostagemPerfil);
        imgBtnAddCameraPostagem = findViewById(R.id.imgBtnAddCameraPostagem);
        imgBtnAddGaleriaPostagem = findViewById(R.id.imgBtnAddGaleriaPostagem);
        imgBtnAddGifPostagem = findViewById(R.id.imgBtnAddGifPostagem);
        imgBtnAddVideoPostagem = findViewById(R.id.imgBtnAddVideoPostagem);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (resultCode == RESULT_OK && requestCode == SELECAO_GALERIA_POSTAGEM) {

            try {
                switch (requestCode) {
                    //Seleção pela galeria
                    case SELECAO_GALERIA_POSTAGEM:
                        String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
                        selecionadoGaleriaPostagem = "sim";
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

                if (selecionadoCameraPostagem != null) {
                    //ToastCustomizado.toastCustomizadoCurto("Selecionado camera", getApplicationContext());
                    selecionadoCameraPostagem = null;
                    //ToastCustomizado.toastCustomizadoCurto("Camera",getApplicationContext());
                    Uri uri = data.getData();
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                } else if (selecionadoGaleriaPostagem != null) {
                    //ToastCustomizado.toastCustomizadoCurto("Galeria",getApplicationContext());
                    Uri imagemCortada = UCrop.getOutput(data);
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemCortada);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    selecionadoGaleriaPostagem = null;
                }

                //Recupera dados da imagem para o firebase
                byte[] dadosImagem = baos.toByteArray();

                DatabaseReference contadorPostagensRef = firebaseRef
                        .child("complementoPostagem").child(idUsuario);
                atualizarContadorPostagemRef = firebaseRef
                        .child("complementoPostagem").child(idUsuario)
                        .child("totalPostagens");

                DatabaseReference dadosFotosUsuarioRef = firebaseRef
                        .child("complementoFoto").child(idUsuario);

                DatabaseReference verificaPostagensRef = firebaseRef.child("todasPostagens")
                        .child(idUsuario);
                verificaPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Postagem postagemTotal = snapshot.getValue(Postagem.class);
                            contadorPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        //Se cair nessa condição, já existem postagens
                                        //desse usuário (Existem postagens)
                                        Postagem contadorPostagem = snapshot.getValue(Postagem.class);
                                        progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                                        progressDialog.show();
                                        verificaPostagensRef.child("totalPostagens").setValue(postagemTotal.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    atualizarContadorPostagemRef.setValue(contadorPostagem.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                int receberContador = contadorPostagem.getTotalPostagens() + 1;
                                                                //Caminho para o storage
                                                                imagemRef = storageRef
                                                                        .child("postagens")
                                                                        .child("fotos")
                                                                        .child(idUsuario)
                                                                        .child("foto" + receberContador + ".jpeg");
                                                                //Verificando progresso do upload
                                                                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                                                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        progressDialog.dismiss();
                                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da postagem", getApplicationContext());
                                                                    }
                                                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Uri> task) {
                                                                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());
                                                                                Uri url = task.getResult();
                                                                                String urlNewPostagem = url.toString();
                                                                                int atualizarContador = postagemTotal.getTotalPostagens() + 1;
                                                                                DatabaseReference salvarPostagemRef = firebaseRef
                                                                                        .child("postagens").child(idUsuario).child(idUsuario + atualizarContador);
                                                                                salvarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                        if (snapshot.exists()) {
                                                                                            novoContador = postagemTotal.getTotalPostagens() + 2;
                                                                                        } else {
                                                                                            novoContador = postagemTotal.getTotalPostagens() + 1;
                                                                                        }

                                                                                        HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                                        dadosPostagemExistente.put("idPostagem", idUsuario + novoContador);
                                                                                        dadosPostagemExistente.put("urlPostagem", urlNewPostagem);
                                                                                        dadosPostagemExistente.put("tipoPostagem", "imagem");
                                                                                        if (localConvertido.equals("pt_BR")) {
                                                                                            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                            date = new Date();
                                                                                            String novaData = dateFormat.format(date);
                                                                                            dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                            dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                        } else {
                                                                                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                            date = new Date();
                                                                                            String novaData = dateFormat.format(date);
                                                                                            dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                            dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                        }
                                                                                        dadosPostagemExistente.put("tituloPostagem", "");
                                                                                        dadosPostagemExistente.put("descricaoPostagem", "");
                                                                                        dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                                        dadosPostagemExistente.put("publicoPostagem", "Todos");
                                                                                        dadosPostagemExistente.put("totalViewsFotoPostagem", 0);

                                                                                        DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                                .child(idUsuario).child("listaUrlPostagens");

                                                                                        if (contadorPostagem.getTotalPostagens() < 4) {
                                                                                            listaUrlPostagemUpdate = contadorPostagem.getListaUrlPostagens();
                                                                                            listaUrlPostagemUpdate.add(urlNewPostagem);
                                                                                            Collections.sort(listaUrlPostagemUpdate, Collections.reverseOrder());
                                                                                            postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                                                        } else {
                                                                                            listaUrlPostagemUpdate = contadorPostagem.getListaUrlPostagens();
                                                                                            Collections.sort(listaUrlPostagemUpdate, Collections.reverseOrder());
                                                                                            ArrayList<String> arrayReordenado = new ArrayList<>();
                                                                                            arrayReordenado.add(0, urlNewPostagem);
                                                                                            arrayReordenado.add(1, listaUrlPostagemUpdate.get(0));
                                                                                            arrayReordenado.add(2, listaUrlPostagemUpdate.get(1));
                                                                                            arrayReordenado.add(3, listaUrlPostagemUpdate.get(2));
                                                                                            postagensExibidasRef.setValue(arrayReordenado);
                                                                                        }


                                                                                        salvarPostagemRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    progressDialog.dismiss();
                                                                                                    //Enviando imagem postada para edição de foto em outra activity.
                                                                                                    Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                                    i.putExtra("fotoOriginal", urlNewPostagem);
                                                                                                    i.putExtra("idPostagem", idUsuario + novoContador);
                                                                                                    i.putExtra("postagemImagem", "postagemImagem");
                                                                                                    i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                    startActivity(i);
                                                                                                } else {
                                                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else {

                                        //Se cair nessa condição, não existem postagens
                                        //mas podem existir fotos
                                        progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                                        progressDialog.show();

                                        dadosFotosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.getValue() != null) {
                                                    //Existem fotos
                                                    Postagem postagemExistente = snapshot.getValue(Postagem.class);
                                                    verificaPostagensRef.child("totalPostagens").setValue(postagemTotal.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            try {
                                                                                int receberContador = 1;
                                                                                //Caminho para o storage
                                                                                imagemRef = storageRef
                                                                                        .child("postagens")
                                                                                        .child("fotos")
                                                                                        .child(idUsuario)
                                                                                        .child("foto" + receberContador + ".jpeg");
                                                                                //Verificando progresso do upload
                                                                                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                                                                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        progressDialog.dismiss();
                                                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getApplicationContext());
                                                                                    }
                                                                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da imagem", getApplicationContext());
                                                                                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Uri> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    Uri url = task.getResult();
                                                                                                    String urlNewPostagem = url.toString();
                                                                                                    int atualizarContador = postagemTotal.getTotalPostagens() + 1;
                                                                                                    DatabaseReference salvarPostagemRef = firebaseRef
                                                                                                            .child("postagens").child(idUsuario).child(idUsuario + atualizarContador);
                                                                                                    salvarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                        @Override
                                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                            if (snapshot.exists()) {
                                                                                                                novoContador = postagemTotal.getTotalPostagens() + 2;
                                                                                                            } else {
                                                                                                                novoContador = postagemTotal.getTotalPostagens() + 1;
                                                                                                            }

                                                                                                            HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                                                            dadosPostagemExistente.put("idPostagem", idUsuario + novoContador);
                                                                                                            dadosPostagemExistente.put("urlPostagem", urlNewPostagem);
                                                                                                            dadosPostagemExistente.put("tipoPostagem", "imagem");
                                                                                                            if (localConvertido.equals("pt_BR")) {
                                                                                                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                                                date = new Date();
                                                                                                                String novaData = dateFormat.format(date);
                                                                                                                dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                                                dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                                            } else {
                                                                                                                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                                                date = new Date();
                                                                                                                String novaData = dateFormat.format(date);
                                                                                                                dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                                                dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                                            }
                                                                                                            dadosPostagemExistente.put("tituloPostagem", "");
                                                                                                            dadosPostagemExistente.put("descricaoPostagem", "");
                                                                                                            dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                                                            dadosPostagemExistente.put("publicoPostagem", "Todos");
                                                                                                            dadosPostagemExistente.put("totalViewsFotoPostagem", 0);

                                                                                                            DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                                                    .child(idUsuario).child("listaUrlPostagens");

                                                                                                            listaUrlPostagemUpdate.add(urlNewPostagem);
                                                                                                            postagensExibidasRef.setValue(listaUrlPostagemUpdate);

                                                                                                            salvarPostagemRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()) {
                                                                                                                        progressDialog.dismiss();
                                                                                                                        //Enviando imagem postada para edição de foto em outra activity.
                                                                                                                        Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                                                        i.putExtra("fotoOriginal", urlNewPostagem);
                                                                                                                        i.putExtra("idPostagem", idUsuario + novoContador);
                                                                                                                        i.putExtra("postagemImagem", "postagemImagem");
                                                                                                                        i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                                        startActivity(i);
                                                                                                                    } else {
                                                                                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                            salvarPostagemRef.removeEventListener(this);
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            } catch (Exception ex) {
                                                                                ex.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    //Não existe nenhum tipo de postagem
                                                    HashMap<String, Object> dadosNovaPostagem = new HashMap<>();
                                                    dadosNovaPostagem.put("idPostagem", idUsuario + 1);
                                                    //Salvar imagem no firebase
                                                    imagemRef = storageRef
                                                            .child("postagens")
                                                            .child("fotos")
                                                            .child(idUsuario)
                                                            .child("foto" + 1 + ".jpeg");
                                                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getApplicationContext());
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());

                                                            verificaPostagensRef.child("totalPostagens").setValue(postagemTotal.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                                                        Uri url = task.getResult();
                                                                                        String urlPostagem = url.toString();
                                                                                        dadosNovaPostagem.put("urlPostagem", urlPostagem);
                                                                                        if (localConvertido.equals("pt_BR")) {
                                                                                            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                            date = new Date();
                                                                                            String novaData = dateFormat.format(date);
                                                                                            dadosNovaPostagem.put("dataPostagem", novaData);
                                                                                            dadosNovaPostagem.put("dataPostagemNova", date);
                                                                                        } else {
                                                                                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                            date = new Date();
                                                                                            String novaData = dateFormat.format(date);
                                                                                            dadosNovaPostagem.put("dataPostagem", novaData);
                                                                                            dadosNovaPostagem.put("dataPostagemNova", date);
                                                                                        }
                                                                                        dadosNovaPostagem.put("tipoPostagem", "imagem");
                                                                                        //Salvando o título da postagem.
                                                                                        dadosNovaPostagem.put("tituloPostagem", "");
                                                                                        //Salvando a descrição da postagem.
                                                                                        dadosNovaPostagem.put("descricaoPostagem", "");
                                                                                        //Salvando o id do usuario
                                                                                        dadosNovaPostagem.put("idDonoPostagem", idUsuario);
                                                                                        dadosNovaPostagem.put("totalViewsFotoPostagem", 0);
                                                                                        dadosNovaPostagem.put("publicoPostagem", "Todos");

                                                                                        DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                                .child(idUsuario).child("listaUrlPostagens");
                                                                                        listaUrlPostagemUpdate.add(urlPostagem);
                                                                                        postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                                                        DatabaseReference salvarPostagemRef = firebaseRef
                                                                                                .child("postagens").child(idUsuario).child(idUsuario + 1);
                                                                                        salvarPostagemRef.setValue(dadosNovaPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    progressDialog.dismiss();
                                                                                                    //Enviando imagem para edição de foto para outra activity.
                                                                                                    Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                                    i.putExtra("fotoOriginal", urlPostagem);
                                                                                                    i.putExtra("idPostagem", idUsuario + 1);
                                                                                                    i.putExtra("postagemImagem", "postagemImagem");
                                                                                                    i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                    startActivity(i);
                                                                                                } else {
                                                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                dadosFotosUsuarioRef.removeEventListener(this);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    contadorPostagensRef.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            //Não existe nenhum tipo de postagem
                            progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                            progressDialog.show();

                            verificaPostagensRef.child("totalPostagens").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        HashMap<String, Object> dadosNovaPostagem = new HashMap<>();
                                        dadosNovaPostagem.put("idPostagem", idUsuario + 1);
                                        //Salvar imagem no firebase
                                        imagemRef = storageRef
                                                .child("postagens")
                                                .child("fotos")
                                                .child(idUsuario)
                                                .child("foto" + 1 + ".jpeg");
                                        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getApplicationContext());
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());
                                                atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Uri> task) {
                                                                Uri url = task.getResult();
                                                                String urlPostagem = url.toString();
                                                                dadosNovaPostagem.put("urlPostagem", urlPostagem);
                                                                if (localConvertido.equals("pt_BR")) {
                                                                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                    date = new Date();
                                                                    String novaData = dateFormat.format(date);
                                                                    dadosNovaPostagem.put("dataPostagem", novaData);
                                                                    dadosNovaPostagem.put("dataPostagemNova", date);
                                                                } else {
                                                                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                    date = new Date();
                                                                    String novaData = dateFormat.format(date);
                                                                    dadosNovaPostagem.put("dataPostagem", novaData);
                                                                    dadosNovaPostagem.put("dataPostagemNova", date);
                                                                }
                                                                dadosNovaPostagem.put("tipoPostagem", "imagem");
                                                                //Salvando o título da postagem.
                                                                dadosNovaPostagem.put("tituloPostagem", "");
                                                                //Salvando a descrição da postagem.
                                                                dadosNovaPostagem.put("descricaoPostagem", "");
                                                                //Salvando o id do usuario
                                                                dadosNovaPostagem.put("idDonoPostagem", idUsuario);
                                                                dadosNovaPostagem.put("totalViewsFotoPostagem", 0);
                                                                dadosNovaPostagem.put("publicoPostagem", "Todos");

                                                                DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                        .child(idUsuario).child("listaUrlPostagens");
                                                                listaUrlPostagemUpdate.add(urlPostagem);
                                                                postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                                DatabaseReference salvarPostagemRef = firebaseRef
                                                                        .child("postagens").child(idUsuario).child(idUsuario + 1);
                                                                salvarPostagemRef.setValue(dadosNovaPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            progressDialog.dismiss();
                                                                            //Enviando imagem para edição de foto para outra activity.
                                                                            Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                            i.putExtra("fotoOriginal", urlPostagem);
                                                                            i.putExtra("idPostagem", idUsuario + 1);
                                                                            i.putExtra("postagemImagem", "postagemImagem");
                                                                            i.putExtra("tipoPostagem", "tipoPostagem");
                                                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(i);
                                                                        } else {
                                                                            ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });

                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        verificaPostagensRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (requestCode == SELECAO_VIDEO_POSTAGEM && resultCode == RESULT_OK) {

            DatabaseReference contadorPostagensRef = firebaseRef
                    .child("complementoPostagem").child(idUsuario);
            atualizarContadorPostagemRef = firebaseRef
                    .child("complementoPostagem").child(idUsuario)
                    .child("totalPostagens");

            DatabaseReference verificaPostagensRef = firebaseRef.child("todasPostagens")
                    .child(idUsuario);
            verificaPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        //Existem postagens
                        Postagem postagemTotal = snapshot.getValue(Postagem.class);
                        contadorPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //Existem postagens
                                if (snapshot.getValue() != null) {
                                    Postagem contadorPostagem = snapshot.getValue(Postagem.class);
                                    progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                                    progressDialog.show();
                                    verificaPostagensRef.child("totalPostagens").setValue(postagemTotal.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                atualizarContadorPostagemRef.setValue(contadorPostagem.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            int receberContador = contadorPostagem.getTotalPostagens() + 1;
                                                            //Caminho para o storage
                                                            videoRef = storageRef
                                                                    .child("postagens")
                                                                    .child("videos")
                                                                    .child(idUsuario)
                                                                    .child("video" + receberContador + ".mp4");

                                                            String path = String.valueOf(Matisse.obtainResult(data).get(0));
                                                            Uri videoUri;
                                                            videoUri = Uri.parse(path);
                                                            UploadTask uploadTask = videoRef.putFile(videoUri);
                                                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog.dismiss();
                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da postagem", getApplicationContext());
                                                                }
                                                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    videoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                                            ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());
                                                                            Uri url = task.getResult();
                                                                            String urlNewPostagem = url.toString();
                                                                            int atualizarContador = postagemTotal.getTotalPostagens() + 1;
                                                                            DatabaseReference salvarPostagemRef = firebaseRef
                                                                                    .child("postagens").child(idUsuario).child(idUsuario + atualizarContador);
                                                                            salvarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    if (snapshot.exists()) {
                                                                                        novoContador = postagemTotal.getTotalPostagens() + 2;
                                                                                    } else {
                                                                                        novoContador = postagemTotal.getTotalPostagens() + 1;
                                                                                    }

                                                                                    HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                                    dadosPostagemExistente.put("idPostagem", idUsuario + novoContador);
                                                                                    dadosPostagemExistente.put("urlPostagem", urlNewPostagem);
                                                                                    dadosPostagemExistente.put("tipoPostagem", "video");
                                                                                    if (localConvertido.equals("pt_BR")) {
                                                                                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                        date = new Date();
                                                                                        String novaData = dateFormat.format(date);
                                                                                        dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                        dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                    } else {
                                                                                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                        date = new Date();
                                                                                        String novaData = dateFormat.format(date);
                                                                                        dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                        dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                    }
                                                                                    dadosPostagemExistente.put("tituloPostagem", "");
                                                                                    dadosPostagemExistente.put("descricaoPostagem", "");
                                                                                    dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                                    dadosPostagemExistente.put("publicoPostagem", "Todos");
                                                                                    dadosPostagemExistente.put("totalViewsFotoPostagem", 0);

                                                                                    DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                            .child(idUsuario).child("listaUrlPostagens");

                                                                                    if (contadorPostagem.getTotalPostagens() < 4) {
                                                                                        listaUrlPostagemUpdate = contadorPostagem.getListaUrlPostagens();
                                                                                        listaUrlPostagemUpdate.add(urlNewPostagem);
                                                                                        Collections.sort(listaUrlPostagemUpdate, Collections.reverseOrder());
                                                                                        postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                                                    } else {
                                                                                        listaUrlPostagemUpdate = contadorPostagem.getListaUrlPostagens();
                                                                                        Collections.sort(listaUrlPostagemUpdate, Collections.reverseOrder());
                                                                                        ArrayList<String> arrayReordenado = new ArrayList<>();
                                                                                        arrayReordenado.add(0, urlNewPostagem);
                                                                                        arrayReordenado.add(1, listaUrlPostagemUpdate.get(0));
                                                                                        arrayReordenado.add(2, listaUrlPostagemUpdate.get(1));
                                                                                        arrayReordenado.add(3, listaUrlPostagemUpdate.get(2));
                                                                                        postagensExibidasRef.setValue(arrayReordenado);
                                                                                    }

                                                                                    salvarPostagemRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                progressDialog.dismiss();
                                                                                                //Enviando imagem postada para edição de foto em outra activity.
                                                                                                Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                                i.putExtra("fotoOriginal", urlNewPostagem);
                                                                                                i.putExtra("idPostagem", idUsuario + novoContador);
                                                                                                i.putExtra("postagemVideo", "postagemVideo");
                                                                                                i.putExtra("uriVideoPostagem", urlNewPostagem);
                                                                                                i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                startActivity(i);
                                                                                            } else {
                                                                                                ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                            }
                                                                                        }
                                                                                    });

                                                                                    salvarPostagemRef.removeEventListener(this);
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    //Não existem postagens, mas podem
                                    //existir fotos

                                    DatabaseReference dadosFotosUsuarioRef = firebaseRef
                                            .child("complementoFoto").child(idUsuario);
                                    dadosFotosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue() != null) {
                                                //Existem fotos
                                                Postagem postagemExistente = snapshot.getValue(Postagem.class);
                                                progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                                                progressDialog.show();
                                                verificaPostagensRef.child("totalPostagens").setValue(postagemTotal.getTotalPostagens() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        //Caminho para o storage
                                                                        videoRef = storageRef
                                                                                .child("postagens")
                                                                                .child("videos")
                                                                                .child(idUsuario)
                                                                                .child("video" + 1 + ".mp4");
                                                                        String path = String.valueOf(Matisse.obtainResult(data).get(0));
                                                                        Uri videoUri;
                                                                        videoUri = Uri.parse(path);
                                                                        UploadTask uploadTask = videoRef.putFile(videoUri);
                                                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                progressDialog.dismiss();
                                                                                ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da postagem", getApplicationContext());
                                                                            }
                                                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                videoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());
                                                                                            Uri url = task.getResult();
                                                                                            String urlNewPostagem = url.toString();
                                                                                            int atualizarContador = postagemTotal.getTotalPostagens() + 1;
                                                                                            DatabaseReference salvarPostagemRef = firebaseRef
                                                                                                    .child("postagens").child(idUsuario).child(idUsuario + atualizarContador);
                                                                                            salvarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                @Override
                                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                    if (snapshot.exists()) {
                                                                                                        novoContador = postagemTotal.getTotalPostagens() + 2;
                                                                                                    } else {
                                                                                                        novoContador = postagemTotal.getTotalPostagens() + 1;
                                                                                                    }

                                                                                                    HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                                                    dadosPostagemExistente.put("idPostagem", idUsuario + novoContador);
                                                                                                    dadosPostagemExistente.put("urlPostagem", urlNewPostagem);
                                                                                                    dadosPostagemExistente.put("tipoPostagem", "video");
                                                                                                    if (localConvertido.equals("pt_BR")) {
                                                                                                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                                        date = new Date();
                                                                                                        String novaData = dateFormat.format(date);
                                                                                                        dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                                        dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                                    } else {
                                                                                                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                                        date = new Date();
                                                                                                        String novaData = dateFormat.format(date);
                                                                                                        dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                                        dadosPostagemExistente.put("dataPostagemNova", date);
                                                                                                    }
                                                                                                    dadosPostagemExistente.put("tituloPostagem", "");
                                                                                                    dadosPostagemExistente.put("descricaoPostagem", "");
                                                                                                    dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                                                    dadosPostagemExistente.put("publicoPostagem", "Todos");
                                                                                                    dadosPostagemExistente.put("totalViewsFotoPostagem", 0);

                                                                                                    DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                                            .child(idUsuario).child("listaUrlPostagens");

                                                                                                    listaUrlPostagemUpdate.add(urlNewPostagem);
                                                                                                    postagensExibidasRef.setValue(listaUrlPostagemUpdate);

                                                                                                    salvarPostagemRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                progressDialog.dismiss();
                                                                                                                //Enviando imagem postada para edição de foto em outra activity.
                                                                                                                Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                                                i.putExtra("fotoOriginal", urlNewPostagem);
                                                                                                                i.putExtra("idPostagem", idUsuario + novoContador);
                                                                                                                i.putExtra("postagemVideo", "postagemVideo");
                                                                                                                i.putExtra("uriVideoPostagem", urlNewPostagem);
                                                                                                                i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                                startActivity(i);
                                                                                                            } else {
                                                                                                                ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                                            }
                                                                                                        }
                                                                                                    });

                                                                                                    salvarPostagemRef.removeEventListener(this);
                                                                                                }

                                                                                                @Override
                                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }else{
                                                //Não existe nenhum tipo de postagem
                                                progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                                                progressDialog.show();
                                                HashMap<String, Object> dadosNovaPostagem = new HashMap<>();
                                                dadosNovaPostagem.put("idPostagem", idUsuario + 1);
                                                //Salvar imagem no firebase
                                                videoRef = storageRef
                                                        .child("postagens")
                                                        .child("videos")
                                                        .child(idUsuario)
                                                        .child("video" + 1 + ".mp4");

                                                String path = String.valueOf(Matisse.obtainResult(data).get(0));
                                                Uri videoUri;
                                                videoUri = Uri.parse(path);
                                                UploadTask uploadTask = videoRef.putFile(videoUri);
                                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getApplicationContext());
                                                    }
                                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());
                                                        verificaPostagensRef.child("totalPostagens").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            videoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Uri> task) {
                                                                                    Uri url = task.getResult();
                                                                                    String urlPostagem = url.toString();
                                                                                    dadosNovaPostagem.put("urlPostagem", urlPostagem);
                                                                                    if (localConvertido.equals("pt_BR")) {
                                                                                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                        date = new Date();
                                                                                        String novaData = dateFormat.format(date);
                                                                                        dadosNovaPostagem.put("dataPostagem", novaData);
                                                                                        dadosNovaPostagem.put("dataPostagemNova", date);
                                                                                    } else {
                                                                                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                        date = new Date();
                                                                                        String novaData = dateFormat.format(date);
                                                                                        dadosNovaPostagem.put("dataPostagem", novaData);
                                                                                        dadosNovaPostagem.put("dataPostagemNova", date);
                                                                                    }
                                                                                    dadosNovaPostagem.put("tipoPostagem", "video");
                                                                                    //Salvando o título da postagem.
                                                                                    dadosNovaPostagem.put("tituloPostagem", "");
                                                                                    //Salvando a descrição da postagem.
                                                                                    dadosNovaPostagem.put("descricaoPostagem", "");
                                                                                    //Salvando o id do usuario
                                                                                    dadosNovaPostagem.put("idDonoPostagem", idUsuario);
                                                                                    dadosNovaPostagem.put("totalViewsFotoPostagem", 0);
                                                                                    dadosNovaPostagem.put("publicoPostagem", "Todos");

                                                                                    DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                                            .child(idUsuario).child("listaUrlPostagens");
                                                                                    listaUrlPostagemUpdate.add(urlPostagem);
                                                                                    postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                                                    DatabaseReference salvarPostagemRef = firebaseRef
                                                                                            .child("postagens").child(idUsuario).child(idUsuario + 1);
                                                                                    salvarPostagemRef.setValue(dadosNovaPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                progressDialog.dismiss();
                                                                                                //Enviando imagem para edição de foto para outra activity.
                                                                                                Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                                                i.putExtra("fotoOriginal", urlPostagem);
                                                                                                i.putExtra("idPostagem", idUsuario + 1);
                                                                                                i.putExtra("postagemVideo", "postagemVideo");
                                                                                                i.putExtra("uriVideoPostagem", urlPostagem);
                                                                                                i.putExtra("tipoPostagem", "tipoPostagem");
                                                                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                startActivity(i);
                                                                                            } else {
                                                                                                ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                            dadosFotosUsuarioRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                contadorPostagensRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        //Não existe nenhum tipo de postagem
                        progressDialog.setMessage("Fazendo upload da postagem, por favor aguarde...");
                        progressDialog.show();
                        HashMap<String, Object> dadosNovaPostagem = new HashMap<>();
                        dadosNovaPostagem.put("idPostagem", idUsuario + 1);
                        //Salvar imagem no firebase
                        videoRef = storageRef
                                .child("postagens")
                                .child("videos")
                                .child(idUsuario)
                                .child("video" + 1 + ".mp4");

                        String path = String.valueOf(Matisse.obtainResult(data).get(0));
                        Uri videoUri;
                        videoUri = Uri.parse(path);
                        UploadTask uploadTask = videoRef.putFile(videoUri);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getApplicationContext());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da postagem", getApplicationContext());
                                verificaPostagensRef.child("totalPostagens").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            atualizarContadorPostagemRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    videoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            Uri url = task.getResult();
                                                            String urlPostagem = url.toString();
                                                            dadosNovaPostagem.put("urlPostagem", urlPostagem);
                                                            if (localConvertido.equals("pt_BR")) {
                                                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                date = new Date();
                                                                String novaData = dateFormat.format(date);
                                                                dadosNovaPostagem.put("dataPostagem", novaData);
                                                                dadosNovaPostagem.put("dataPostagemNova", date);
                                                            } else {
                                                                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                date = new Date();
                                                                String novaData = dateFormat.format(date);
                                                                dadosNovaPostagem.put("dataPostagem", novaData);
                                                                dadosNovaPostagem.put("dataPostagemNova", date);
                                                            }
                                                            dadosNovaPostagem.put("tipoPostagem", "video");
                                                            //Salvando o título da postagem.
                                                            dadosNovaPostagem.put("tituloPostagem", "");
                                                            //Salvando a descrição da postagem.
                                                            dadosNovaPostagem.put("descricaoPostagem", "");
                                                            //Salvando o id do usuario
                                                            dadosNovaPostagem.put("idDonoPostagem", idUsuario);
                                                            dadosNovaPostagem.put("totalViewsFotoPostagem", 0);
                                                            dadosNovaPostagem.put("publicoPostagem", "Todos");

                                                            DatabaseReference postagensExibidasRef = firebaseRef.child("complementoPostagem")
                                                                    .child(idUsuario).child("listaUrlPostagens");
                                                            listaUrlPostagemUpdate.add(urlPostagem);
                                                            postagensExibidasRef.setValue(listaUrlPostagemUpdate);
                                                            DatabaseReference salvarPostagemRef = firebaseRef
                                                                    .child("postagens").child(idUsuario).child(idUsuario + 1);
                                                            salvarPostagemRef.setValue(dadosNovaPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        progressDialog.dismiss();
                                                                        //Enviando imagem para edição de foto para outra activity.
                                                                        Intent i = new Intent(getApplicationContext(), EdicaoFotoActivity.class);
                                                                        i.putExtra("fotoOriginal", urlPostagem);
                                                                        i.putExtra("idPostagem", idUsuario + 1);
                                                                        i.putExtra("postagemVideo", "postagemVideo");
                                                                        i.putExtra("uriVideoPostagem", urlPostagem);
                                                                        i.putExtra("tipoPostagem", "tipoPostagem");
                                                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(i);
                                                                    } else {
                                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                    verificaPostagensRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


                /*
                //Log.i("Matisse", "Uris: " + Matisse.obtainResult(data));
            //Log.i("Matisse", "Paths: " + Matisse.obtainPathResult(data));
            //Log.i("Matisse", "Use the selected photos with original: "+String.valueOf(Matisse.obtainOriginalState(data)));
               StorageReference videoRef = storageRef
                        .child("postagens")
                        .child("videos")
                        .child(idUsuario)
                        .child("video" + 0 + ".mp4");
                String path = String.valueOf(Matisse.obtainResult(data).get(0));
               Uri videoUri;
               videoUri = Uri.parse(path);
                videoRef.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ToastCustomizado.toastCustomizadoCurto("Sucess",getApplicationContext());
                    }
                });
                 */
        }
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(PostagemActivity.this);
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
