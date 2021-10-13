package com.example.ogima.ui.cadastro;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.FiltroActivity;
import com.example.ogima.helper.Permissao;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;

public class FotosActivity extends AppCompatActivity {

    private Button btnContinuarFotos;

    //teste
    private Bitmap imagem;

    //teste
    private ImageView imageUsuarioOne;

    private FloatingActionButton fbFotoOne, fbFotoTwo, fbFotoThree, fbFotoFour,
    fbFotoFive, fbFotoSix;

    private static final int SELECTION_GALLERY = 200;
    private static final int SELECTION_CAMERA = 100;

    //Array de permissões
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_fotos);


        //teste
        imageUsuarioOne = findViewById(R.id.imageUsuarioOne);
        //teste





        //Validar Permissões
        Permissao.validarPermissoes(permissoesNecessarias, FotosActivity.this, 1);


        //Componentes para adicionar fotos pela galeria
        btnContinuarFotos = findViewById(R.id.btnContinuarFotos);
        fbFotoOne = findViewById(R.id.fbFotoOne); fbFotoTwo = findViewById(R.id.fbFotoTwo);
        fbFotoThree = findViewById(R.id.fbFotoThree); fbFotoFour = findViewById(R.id.fbFotoFour);
        fbFotoFive = findViewById(R.id.fbFotoFive); fbFotoSix = findViewById(R.id.fbFotoSix);


        //Evento de clique para adicionar fotos
        fbFotoOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i  = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(FotosActivity.this.getPackageManager()) != null){
                    startActivityForResult(i, SELECTION_GALLERY);

                }

            }
        });

        //Evento de clique para abrir camêra
        fbFotoTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(FotosActivity.this.getPackageManager()) != null){
                    startActivityForResult(i, SELECTION_CAMERA);

                }

            }
        });





        btnContinuarFotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(FotosActivity.this, NavigationDrawerActivity.class));

            }
        });

    }

    //Recuperando a imagem e enviado para tela de filtros
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == FotosActivity.RESULT_OK){

            Bitmap imagem = null;

           try{

               switch (requestCode){

                   case SELECTION_CAMERA:
                       imagem = (Bitmap) data.getExtras().get("data");
                       break;

                   case SELECTION_GALLERY:
                       Uri localImagemSelecionada = data.getData();
                       imagem = MediaStore.Images.Media.getBitmap(FotosActivity.this.getContentResolver(), localImagemSelecionada);
                       break;

               }

               //Validar imagem selecionada
               if(imagem != null){

                   //Converte imagem em byte array
                   ByteArrayOutputStream baos = new ByteArrayOutputStream();
                   imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                   byte[] dadosImagem = baos.toByteArray();

                   //teste
                   imagem = BitmapFactory.decodeByteArray(dadosImagem, 0, dadosImagem.length);
                   imageUsuarioOne.setImageBitmap(imagem);
                   //teste

                   /*
                   //Enviando imagem para tela de filtro
                    Intent i = new Intent(FotosActivity.this, FiltroActivity.class);
                    i.putExtra("fotoEscolhida", dadosImagem);
                    startActivity(i);

                    */

               }

           }catch (Exception e){

            e.printStackTrace();
           }

        }

    }

    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }


}
