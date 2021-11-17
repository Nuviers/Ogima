package com.example.ogima.ui.cadastro;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.Permissao;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class FotoPerfilActivity extends AppCompatActivity implements View.OnClickListener {
//public class FotoPerfilActivity extends AppCompatActivity {

    private Button btnCadastrar, btnFotoPerfil, btnFotoFundo;

    //
    private static final int SELECAO_GALERIA = 200;

    private ImageView imageViewPerfilUsuario, imageViewFundoUsuario;

    private StorageReference storageRef;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_foto_perfil);

        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnFotoPerfil = findViewById(R.id.btnFotoPerfil);
        btnFotoFundo = findViewById(R.id.btnFotoFundo);

        imageViewPerfilUsuario = findViewById(R.id.imageViewPerfilUsuario);
        imageViewFundoUsuario = findViewById(R.id.imageViewFundoUsuario);

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Validando permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        btnFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Passando a intenção de selecionar uma foto pela galeria
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Verificando se a intenção foi atendida com sucesso
                if (intent.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(intent, SELECAO_GALERIA);
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
                try{
                    String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmailUsuario());
                    usuario.setIdUsuario(identificadorUsuario);
                    usuario.salvar();
                }catch (Exception e){
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
        if(resultCode == RESULT_OK){

            Bitmap imagemFotoUsuario = null;

            try{

                //Seleção apenas da galeria

                switch (requestCode){

                    case SELECAO_GALERIA:

                        Uri localImagemFotoSelecionada = data.getData();

                        imagemFotoUsuario = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemFotoSelecionada);

                        break;
                }

                // Caso tenha selecionado uma imagem
                if(imagemFotoUsuario != null){

                    //Enviar por intent pelo usuario a imagem pro fragment
                    imageViewPerfilUsuario.setImageBitmap(imagemFotoUsuario);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagemFotoUsuario.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte [] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemFotoRef = storageRef
                            .child("perfil")
                            .child("fotoPerfil")
                            .child("<id-usuario>.jpeg");

                    UploadTask uploadTask = imagemFotoRef.putBytes(dadosImagem);
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

            }catch (Exception e){
                e.printStackTrace();
            }


        }



    }

    @Override
    public void onClick(View view) {

    }




    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }
}
