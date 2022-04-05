package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TodasFotosUsuarioActivity extends AppCompatActivity {

    private static final int SELECAO_CAMERA = 100, SELECAO_GALERIA = 200;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private Button buttonTesteCamera, buttonTesteGaleria;
    private ImageView imageViewTesteCrop;
    private StorageReference imagemRef;
    private StorageReference storageRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todas_fotos_usuario);

        buttonTesteCamera = findViewById(R.id.buttonTesteCamera);
        buttonTesteGaleria = findViewById(R.id.buttonTesteGaleria);
        imageViewTesteCrop = findViewById(R.id.imageViewTesteCrop);

        buttonTesteCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        buttonTesteGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGaleria();
            }
        });

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
    }

    private void openCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_CAMERA);
        }
    }

    private void openGaleria(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //Verificando se a intenção foi atendida com sucesso
        if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {

            startActivityForResult(i, SELECAO_GALERIA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Só está tratando quando a opção escolhida foi galeria,
        //falta tratar da camêra no lugar de request code SELECAO_GALERIA
        //colocar SELECAO_CAMERA
        if (resultCode == RESULT_OK && requestCode == SELECAO_GALERIA) {
            Bitmap imagem = null;
            try{
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        //Salvando uma imagem em cache para obter a Uri dela
                        String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
                        destinoArquivo += ".jpg";
                        //Uri obtido através do cache.
                        final  Uri resultUri = data.getData();
                        openCropActivity(resultUri,Uri.fromFile(new File(getCacheDir(),destinoArquivo)));
                        break;
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
            // Resultado recebido quando usuário seleciona uma foto para ajustar
        }else if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            try {
                ToastCustomizado.toastCustomizadoCurto("Corta",getApplicationContext());
                //Capturando uri enviada anteriormente e recebida pelo
                //onActivityResult
                Uri imagemCortada = UCrop.getOutput(data);
                //Convertendo Uri recebido para bitmap
                Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagemCortada);
                //Exibindo bitmap para ver se o processo foi realizado com sucesso.
                imageViewTesteCrop.setImageBitmap(imagemBitmap);
                //Fazendo compressão do bitmap para JPEG
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                //Passando os dados para um byte Array.
                byte[] dadosImagem = baos.toByteArray();
                //Referência do storage
                imagemRef = storageRef
                        .child("imagens")
                        .child("fotosUsuario")
                        .child(idUsuario)
                        .child("fotoUsuarioCortada" +  ".jpeg");
                //Fazendo upload no storage
                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getApplicationContext());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da imagem", getApplicationContext());
                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Uri url = task.getResult();
                                //Capturando Uri da foto que foi feito o upload.
                                String caminhoFotoPerfil = url.toString();
                                try{
                                    DatabaseReference fotoUsuarioOneRef = firebaseRef
                                            .child("fotosUsuario")
                                            .child(idUsuario)
                                            .child("listaFotosUsuario");
                                    //Criando um array para poder adicionar a uri obtida.
                                    ArrayList<String> testeFotoCortada = new ArrayList<>();
                                    testeFotoCortada.add(caminhoFotoPerfil);
                                    //Salvando array com a uri obtida.
                                    fotoUsuarioOneRef.setValue(testeFotoCortada).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                ToastCustomizado.toastCustomizadoCurto("Salvo com sucesso",getApplicationContext());
                                            }else{
                                                ToastCustomizado.toastCustomizadoCurto("Erro ao salvar",getApplicationContext());
                                            }
                                        }
                                    });
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Caso ocorra um erro ao tentar cortar a foto ele cai nesse else if.
        }else if(resultCode == UCrop.RESULT_ERROR){
            final Throwable cropError = UCrop.getError(data);
            ToastCustomizado.toastCustomizadoCurto("Erro " + cropError,getApplicationContext());
        }
    }

    //Método responsável por ajustar as proporções do corte
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(5f, 5f)
                .withMaxResultSize ( 499 , 614 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(TodasFotosUsuarioActivity.this);
    }

    //Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptions(){
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar foto");
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }
}