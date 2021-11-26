package com.example.ogima.ui.cadastro;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class FotoPerfilActivity extends AppCompatActivity implements View.OnClickListener {
//public class FotoPerfilActivity extends AppCompatActivity {

    private Button btnCadastrar;

    private ImageButton imgButtonGaleria, imgButtonCamera, imgButtonFotoFundo,
            imgButtonGifPerfil, imgButtonGifFundo;

    private ImageView imageViewPerfilUsuario, imageViewFundoUsuario;

    //Constantes passando um result code
    private static final int SELECAO_CAMERA = 100, SELECAO_GALERIA = 200,
            FUNDO_GALERIA = 300;

    private Usuario usuario;

    //Referencia do storage do firebase
    private StorageReference storageRef;

    private ProgressBar progressBar, progressBarFundo;

    private TextView progressTextView, progressTextViewFundo;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private String identificadorUsuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_foto_perfil);

        //Inicializar componentes
        inicializarComponentes();

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdUsuarioCriptografado();

        //Validando permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //Instanciando usuario
        usuario = new Usuario();

        //Recebendo dados Email/Senha/Nome/Apelido/Idade/Nascimento/Genero/Interesses
        Bundle dados = getIntent().getExtras();

        usuario = (Usuario) dados.getSerializable("dadosUsuario");

        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmailUsuario());
        usuario.setIdUsuario(identificadorUsuario);

        Glide.with(FotoPerfilActivity.this)
                .load(R.drawable.testewomamtwo)
                .centerCrop()
                .circleCrop()
                .into(imageViewPerfilUsuario);

        //Button para selecionar gif no campo de foto de perfil do usuário
        imgButtonGifPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Passando imagem a ser setada por paramêtro
                fotoPerfilGif(imageViewPerfilUsuario, "gifPerfil");

            }
        });

        //Button para selecionar gif no campo de fundo de perfil do usuário
        imgButtonGifFundo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Passando imagem a ser setada por paramêtro
                fotoPerfilGif(imageViewFundoUsuario, "gifFundo");

            }
        });


        //Evento de clique do botão de camera
        imgButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Passando a intenção de tirar uma foto pela camera
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //Verificando se a intenção foi atendida com sucesso
                if (i.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(i, SELECAO_CAMERA);
                }
            }
        });

        //Evento de clique do botão de seleção de galeria
        imgButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Passando a intenção de selecionar uma foto pela galeria
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Verificando se a intenção foi atendida com sucesso
                if (i.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

        imgButtonFotoFundo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Passando a intenção de selecionar uma foto pela galeria
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Verificando se a intenção foi atendida com sucesso
                if (intent.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(intent, FUNDO_GALERIA);
                }

            }

        });


        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(FotoPerfilActivity.this, "Email "
                        + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario() + " Número " + usuario.getNumero()
                        + " Nome " + usuario.getNomeUsuario() + " Apelido "
                        + usuario.getApelidoUsuario() + " Idade " + usuario.getIdade()
                        + " Nascimento " + usuario.getDataNascimento() + " Genêro " + usuario.getGeneroUsuario()
                        + " Interesses " + usuario.getInteresses(), Toast.LENGTH_LONG).show();

                verificarFotosSelecionadas();

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Verificando se foi possível recuperar a foto do usuario
        if (resultCode == RESULT_OK) {

            Bitmap imagem = null;
            Bitmap imagemFundo = null;

            try {

                //Seleção apenas da galeria

                switch (requestCode) {

                    //Seleção pela camera
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;

                    //Seleção pela galeria
                    case SELECAO_GALERIA:

                        Uri localImagemFotoSelecionada = data.getData();

                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemFotoSelecionada);

                        break;

                    //Seleção pela galeria para fundo do perfil
                    case FUNDO_GALERIA:

                        Uri localImagemFotoFundo = data.getData();

                        imagemFundo = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemFotoFundo);

                        break;


                }

                // Caso tenha selecionado uma imagem
                if (imagem != null) {

                    //Enviar por intent pelo usuario a imagem pro fragment
                    //*imageViewPerfilUsuario.setImageBitmap(imagem);

                    Glide.with(FotoPerfilActivity.this)
                            .load(imagem)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerCrop()
                            .circleCrop()
                            .into(imageViewPerfilUsuario);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario)
                            .child("fotoPerfil.jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getApplicationContext(), " Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(getApplicationContext(), " Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();

                                    String caminhoFotoPerfil = url.toString();

                                    //Toast.makeText(getApplicationContext(), "Cami" + url, Toast.LENGTH_SHORT).show();

                                    //Salvando a maioria dos dados do usuario no firebase
                                    try {

                                        usuario.setMinhaFoto(caminhoFotoPerfil);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressBar.setProgress((int) progress);

                            if (((int) progress == 100)) {
                                String progressoCarregamento = "Salva com sucesso";
                                progressTextView.setText(progressoCarregamento);
                            } else {
                                String progressString = "upload " + ((int) progress) + "%";
                                progressTextView.setText(progressString);
                            }
                        }
                    });


                }
                if (imagemFundo != null) {

                    //Enviar por intent pelo usuario a imagem pro fragment
                    //*imageViewFundoUsuario.setImageBitmap(imagemFundo);

                    Glide.with(FotoPerfilActivity.this)
                            .load(imagemFundo)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerCrop()
                            .into(imageViewFundoUsuario);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagemFundo.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemFundoRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario)
                            .child("fotoFundo.jpeg");

                    UploadTask uploadTask = imagemFundoRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getApplicationContext(), " Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(getApplicationContext(), " Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                            imagemFundoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri urlFundo = task.getResult();

                                    String caminhoFundo = urlFundo.toString();

                                    //Toast.makeText(getApplicationContext(), "CFund" + urlFundo, Toast.LENGTH_SHORT).show();

                                    usuario.setMeuFundo(caminhoFundo);

                                    //atualizarFotoFundoUsuario(urlFundo);

                                }
                            });

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressBarFundo.setProgress((int) progress);

                            if (((int) progress == 100)) {
                                String progressoCarregamento = "Salva com sucesso";
                                progressTextViewFundo.setText(progressoCarregamento);
                            } else {
                                String progressString = "upload " + ((int) progress) + "%";
                                progressTextViewFundo.setText(progressString);
                            }
                        }
                    });


                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }

    @Override
    public void onClick(View view) {

    }


    //Verificar se permissão foi negada
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Percorrendo array de inteiros para ver se alguma delas foi negada
        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(FotoPerfilActivity.this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "Necessário aceitar as permissões para utilização desses recursos", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), InteresseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

    public void fotoPerfilGif(ImageView imageView, String campoGif) {

        Giphy.INSTANCE.configure(FotoPerfilActivity.this, "qQg4j9NKDfl4Vqh84iaTcQEMfZcH5raY", false);

        GPHSettings gphSettings = new GPHSettings();

        GiphyDialogFragment gdl = GiphyDialogFragment.Companion.newInstance(gphSettings);

        gdl.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
            @Override
            public void onGifSelected(@NonNull Media media, @Nullable String s, @NonNull GPHContentType gphContentType) {
                Toast.makeText(getApplicationContext(), gphContentType + " selecionado com sucesso!", Toast.LENGTH_SHORT).show();

                onGifSelected(media);
            }

            @Override
            public void onDismissed(@NonNull GPHContentType gphContentType) {

            }

            @Override
            public void didSearchTerm(@NonNull String s) {

            }

            public void onGifSelected(@NotNull Media media) {

                Image image = media.getImages().getFixedWidth();
                assert image != null;
                String gif_url = image.getGifUrl();

                if (campoGif == "gifPerfil") {

                    usuario.setMinhaFoto(gif_url);
                    Glide.with(FotoPerfilActivity.this).load(gif_url)
                            .centerCrop()
                            .circleCrop()
                            .into(imageView);
                }

                if (campoGif == "gifFundo") {

                    usuario.setMeuFundo(gif_url);
                    Glide.with(FotoPerfilActivity.this).load(gif_url)
                            .centerCrop()
                            .into(imageView);
                }
            }

            public void onDismissed() {
            }
        });

        gdl.show(FotoPerfilActivity.this.getSupportFragmentManager(), "this");

    }

    public void verificarFotosSelecionadas() {

        if (usuario.getMinhaFoto() == null && usuario.getMeuFundo() == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(FotoPerfilActivity.this);
            builder.setTitle("As fotos de perfil não foram selecionadas");
            builder.setMessage("Deseja prosseguir mesmo assim?");
            builder.setCancelable(true);
            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    usuario.salvar();

                    Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    finish();

                }
            }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();


        }
        if (usuario.getMinhaFoto() == null || usuario.getMeuFundo() == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(FotoPerfilActivity.this);
            builder.setTitle("Uma das fotos de perfil não foi selecionada");
            builder.setMessage("Deseja prosseguir mesmo assim?");
            builder.setCancelable(true);
            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    usuario.salvar();

                    Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    finish();

                }
            }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }


            //if(usuario.getMinhaFoto() != null && usuario.getMeuFundo() != null && progressBar.getProgress() == 100 && progressBarFundo.getProgress() == 100){
            if (usuario.getMinhaFoto() != null && usuario.getMeuFundo() != null) {

                usuario.salvar();

                Toast.makeText(getApplicationContext(), " Fotos salvas com sucesso", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();

                //}else if(usuario.getMinhaFoto() != null && usuario.getMeuFundo() != null && progressBar.getProgress() <= 100 || progressBarFundo.getProgress() <=100 ){
            } else {

                /*
                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();

                 */

             //Toast.makeText(getApplicationContext(), "Espere as fotos serem carregadas", Toast.LENGTH_SHORT).show();

            }


    }


    public void inicializarComponentes(){

        //Button
        btnCadastrar = findViewById(R.id.btnCadastrar);

        //Image Button
        imgButtonGifPerfil = findViewById(R.id.imgButtonGifPerfil);
        imgButtonGifFundo = findViewById(R.id.imgButtonGifFundo);

        imgButtonCamera = findViewById(R.id.imgButtonCamera);
        imgButtonGaleria = findViewById(R.id.imgButtonGaleria);
        imgButtonFotoFundo = findViewById(R.id.imgButtonFotoFundo);

        //Image View
        imageViewPerfilUsuario = findViewById(R.id.imageViewPerfilUsuario);
        imageViewFundoUsuario = findViewById(R.id.imageViewFundoUsuario);

        //ProgressBar
        progressBar = findViewById(R.id.progressBar);
        progressBarFundo = findViewById(R.id.progressBarFundo);
        progressTextView = findViewById(R.id.progressTextView);
        progressTextViewFundo = findViewById(R.id.progressTextViewFundo);
    }
}
