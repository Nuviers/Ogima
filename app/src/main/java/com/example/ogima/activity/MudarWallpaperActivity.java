package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.SalvarArquivoLocalmente;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.example.ogima.model.Usuario;
import com.example.ogima.model.Wallpaper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class MudarWallpaperActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private StorageReference storageRef;
    private String emailUsuario, idUsuario;
    private String wallpaperPlace;
    private ImageButton imgBtnSelecionarWallpaper, imgBtnBackWallpaper;
    private TextView txtViewSelecionarWallpaper;
    private ImageView imgViewPreviewWallpaper;
    private DatabaseReference wallpaperPrivadoRef;
    private DatabaseReference wallpaperGlobalRef;
    private Usuario usuarioDestinatario;
    private ProgressDialog progressDialog;
    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private static final int SELECAO_GALERIA = 200;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private StorageReference wallpaperStorageRef;

    private SalvarArquivoLocalmente salvarArquivoLocalmente = new SalvarArquivoLocalmente();

    private DatabaseReference verificaWalllpaperAnteriorRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference reference;
    private File dirAnterior;

    private VerificaTamanhoArquivo verificaTamanhoArquivo = new VerificaTamanhoArquivo();
    private static final int MAX_FILE_SIZE_IMAGEM = 6;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mudar_wallpaper);
        inicializandoComponentes();
        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Validar permissões necessárias para escolha do wallpaper.
        Permissao.validarPermissoes(permissoesNecessarias, MudarWallpaperActivity.this, 17);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            wallpaperPlace = dados.getString("wallpaperPlace");
            usuarioDestinatario = (Usuario) dados.getSerializable("usuarioDestinatario");
        }

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        imgBtnBackWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Verifica se existe algum wallpaper para essa conversa
        wallpaperPrivadoRef = firebaseRef.child("chatWallpaper")
                .child(idUsuario).child(usuarioDestinatario.getIdUsuario());

        wallpaperGlobalRef = firebaseRef.child("chatGlobalWallpaper")
                .child(idUsuario);

        verificaWallpaper();

        imgBtnSelecionarWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Passando a intenção de selecionar uma foto pela galeria
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Verificando se a intenção foi atendida com sucesso
                if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

        txtViewSelecionarWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Passando a intenção de selecionar uma foto pela galeria
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Verificando se a intenção foi atendida com sucesso
                if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (resultCode == RESULT_OK && requestCode == SELECAO_GALERIA) {
            try {
                String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
                destinoArquivo += ".jpg";
                final Uri localImagemFotoSelecionada = data.getData();

                if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM, localImagemFotoSelecionada, getApplicationContext())) {
                    //*Chamando método responsável pela estrutura do U crop
                    openCropActivity(localImagemFotoSelecionada, Uri.fromFile(new File(getCacheDir(), destinoArquivo)));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK || requestCode == 101 && resultCode == RESULT_OK) {
            try {

                Uri imagemCortada = UCrop.getOutput(data);
                Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemCortada);
                imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

                //Recupera dados da imagem para o firebase
                byte[] dadosImagem = baos.toByteArray();
                progressDialog.setMessage("Alterando papel de parede, aguarde...");
                progressDialog.show();

                String nomeRandomico = UUID.randomUUID().toString();

                if (wallpaperPlace.equals("onlyChat")) {
                    wallpaperStorageRef = storageRef.child("chatWallpaper")
                            .child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                            .child("wallpaper" + nomeRandomico + ".jpeg");

                    removerWallpaperAnterior("privado");

                } else if (wallpaperPlace.equals("allChats")) {

                    wallpaperStorageRef = storageRef.child("chatWallpaper")
                            .child(idUsuario)
                            .child("wallpaper" + nomeRandomico + ".jpeg");

                    removerWallpaperAnterior("global");
                }

                //Verificando progresso do upload
                UploadTask uploadTask = wallpaperStorageRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro " + e.getMessage(), getApplicationContext());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        wallpaperStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                ToastCustomizado.toastCustomizadoCurto("Sucesso ao enviar mensagem", getApplicationContext());
                                Uri url = task.getResult();
                                String urlNewWallpaper = url.toString();
                                HashMap<String, Object> dadosWallpaper = new HashMap<>();

                                if (wallpaperPlace.equals("onlyChat")) {

                                    dadosWallpaper.put("urlWallpaper", urlNewWallpaper);
                                    dadosWallpaper.put("nomeWallpaper", nomeRandomico + ".jpg");
                                    salvarWallpaperLocalmente(nomeRandomico, urlNewWallpaper, "privado", usuarioDestinatario.getIdUsuario());
                                    wallpaperPrivadoRef.setValue(dadosWallpaper).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                verificaWallpaper();
                                            } else {
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                } else if (wallpaperPlace.equals("allChats")) {

                                    dadosWallpaper.put("urlWallpaper", urlNewWallpaper);
                                    dadosWallpaper.put("nomeWallpaper", nomeRandomico + ".jpg");
                                    salvarWallpaperLocalmente(nomeRandomico, urlNewWallpaper, "global", usuarioDestinatario.getIdUsuario());
                                    wallpaperGlobalRef.setValue(dadosWallpaper).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                verificaWallpaper();
                                            } else {
                                                progressDialog.dismiss();
                                            }
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

    private void verificaWallpaper() {
        wallpaperPrivadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Wallpaper wallpaper = snapshot.getValue(Wallpaper.class);
                    GlideCustomizado.montarGlideFoto(getApplicationContext(),
                            wallpaper.getUrlWallpaper(),
                            imgViewPreviewWallpaper,
                            android.R.color.transparent);
                } else {
                    wallpaperGlobalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Wallpaper wallpaperAll = snapshot.getValue(Wallpaper.class);
                                //Wallpaper definido para todos chats
                                if (wallpaperAll.getUrlWallpaper() != null) {
                                    GlideCustomizado.montarGlideFoto(getApplicationContext(),
                                            wallpaperAll.getUrlWallpaper(),
                                            imgViewPreviewWallpaper,
                                            android.R.color.transparent);
                                }
                            } else {
                                //Não existe nenhum wallpaper definido
                                imgViewPreviewWallpaper.setImageResource(R.drawable.wallpaperwaifutwo);
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

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(MudarWallpaperActivity.this);
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

    private void salvarWallpaperLocalmente(String nomeWallpaper, String urlWallpaper, String tipoWallpaper, String idDestinatario) {
        salvarArquivoLocalmente.transformarWallpaperEmFile(getApplicationContext(),
                urlWallpaper, nomeWallpaper, tipoWallpaper, idDestinatario, new SalvarArquivoLocalmente.SalvarArquivoCallback() {
                    @Override
                    public void onFileSaved(File file) {
                        ToastCustomizado.toastCustomizadoCurto("Sucesso wallpaper", getApplicationContext());
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                    }

                    @Override
                    public void onSaveFailed(Exception e) {
                        ToastCustomizado.toastCustomizado("Fail wallpaper " + e.getMessage(), getApplicationContext());
                        Log.i("testewallpaper", "Fail - " + e.getMessage());
                    }
                });
    }

    private void removerWallpaperAnterior(String tipoWallpaper) {

        if (tipoWallpaper.equals("privado")) {
            verificaWalllpaperAnteriorRef = firebaseRef.child("chatWallpaper")
                    .child(idUsuario).child(usuarioDestinatario.getIdUsuario());
        } else if (tipoWallpaper.equals("global")) {
            verificaWalllpaperAnteriorRef = firebaseRef.child("chatGlobalWallpaper")
                    .child(idUsuario);
        }

        verificaWalllpaperAnteriorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Wallpaper wallpaperAnterior = snapshot.getValue(Wallpaper.class);
                    if (wallpaperAnterior.getUrlWallpaper() != null) {
                        String caminhoWallpaperAnterior = wallpaperAnterior.getUrlWallpaper();
                        reference = storage.getReferenceFromUrl(caminhoWallpaperAnterior);

                        if (tipoWallpaper.equals("privado")) {
                            dirAnterior = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File.separator + "Ogima" + File.separator + usuarioDestinatario.getIdUsuario() + File.separator + "wallpaperPrivado");
                        } else if (tipoWallpaper.equals("global")) {
                            dirAnterior = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File.separator + "Ogima" + File.separator + "wallpaperGlobal");
                        }

                        if (dirAnterior.exists()) {
                            File file = new File(dirAnterior, wallpaperAnterior.getNomeWallpaper());
                            file.delete();
                        }

                        reference.delete();
                    }
                }
                verificaWalllpaperAnteriorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializandoComponentes() {
        imgBtnSelecionarWallpaper = findViewById(R.id.imgBtnSelecionarWallpaper);
        imgBtnBackWallpaper = findViewById(R.id.imgBtnBackWallpaper);
        txtViewSelecionarWallpaper = findViewById(R.id.txtViewSelecionarWallpaper);
        imgViewPreviewWallpaper = findViewById(R.id.imgViewPreviewWallpaper);
    }
}