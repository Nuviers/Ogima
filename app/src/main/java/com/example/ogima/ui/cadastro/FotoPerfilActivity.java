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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class FotoPerfilActivity extends AppCompatActivity implements View.OnClickListener {
//public class FotoPerfilActivity extends AppCompatActivity {

    private Button btnCadastrar, btnFotoFundo;

    private ImageButton imgButtonGaleria, imgButtonCamera;

    //Constantes passando um result code
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private static final int FUNDO_GALERIA = 300;


    private ImageView imageViewPerfilUsuario, imageViewFundoUsuario;

    //Referencia do storage do firebase
    private StorageReference storageRef;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    //
    private String identificadorUsuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_foto_perfil);

        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnFotoFundo = findViewById(R.id.btnFotoFundo);

        imageViewPerfilUsuario = findViewById(R.id.imageViewPerfilUsuario);
        imageViewFundoUsuario = findViewById(R.id.imageViewFundoUsuario);

        imgButtonGaleria = findViewById(R.id.imgButtonGaleria);
        imgButtonCamera = findViewById(R.id.imgButtonCamera);

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdUsuarioCriptografado();

        //Validando permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);


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



        btnFotoFundo.setOnClickListener(new View.OnClickListener() {
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

                //Recebendo dados Email/Senha/Nome/Apelido/Idade/Nascimento/Genero/Interesses

                Bundle dados = getIntent().getExtras();

                Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");


                Toast.makeText(FotoPerfilActivity.this, "Email "
                        + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario() + " Número " + usuario.getNumero()
                        + " Nome " + usuario.getNomeUsuario() + " Apelido "
                        + usuario.getApelidoUsuario() + " Idade " + usuario.getIdade()
                        + " Nascimento " + usuario.getDataNascimento() + " Genêro " + usuario.getGeneroUsuario()
                        + " Interesses " + usuario.getInteresses(), Toast.LENGTH_LONG).show();


                //Salvando a maioria dos dados do usuario no firebase
                try {
                    String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmailUsuario());
                    usuario.setIdUsuario(identificadorUsuario);
                    usuario.salvar();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();

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
                    imageViewPerfilUsuario.setImageBitmap(imagem);

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

                        }
                    });


                } if(imagemFundo != null){

                    //Enviar por intent pelo usuario a imagem pro fragment
                    imageViewFundoUsuario.setImageBitmap(imagemFundo);

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
}
