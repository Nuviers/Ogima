package com.example.ogima.ui.cadastro;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.MainActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.Permissao;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class FotosActivity extends AppCompatActivity implements View.OnClickListener {



    private Button btnCadastrar;

    private StorageReference storage;

    Usuario usuario;

    private List<String> listaURLFotos = new ArrayList<>();

    private ImageView imageUsuario1, imageUsuario2, imageUsuario3, imageUsuario4,
    imageUsuario5, imageUsuario6;

    private FloatingActionButton fbFotoOne, fbFotoTwo, fbFotoThree, fbFotoFour,
            fbFotoFive, fbFotoSix;

    private static final int SELECTION_GALLERY = 200;
    private static final int SELECTION_CAMERA = 100;

    //Array de permissões
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
    };

    private List<String> listaFotosRecuperadas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_fotos);

        MainActivity mainActivity = new MainActivity();


        //Inicializando componentes imageView
        imageUsuario1 = findViewById(R.id.imageUsuario1); imageUsuario2 = findViewById(R.id.imageUsuario2);
        imageUsuario3 = findViewById(R.id.imageUsuario3); imageUsuario4 = findViewById(R.id.imageUsuario4);
        imageUsuario5 = findViewById(R.id.imageUsuario5); imageUsuario6 = findViewById(R.id.imageUsuario6);


        imageUsuario1.setOnClickListener(this);
        imageUsuario2.setOnClickListener(this);
        imageUsuario3.setOnClickListener(this);
        imageUsuario4.setOnClickListener(this);
        imageUsuario5.setOnClickListener(this);
        imageUsuario6.setOnClickListener(this);

        //Configurações iniciais do storage
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar Permissões
        Permissao.validarPermissoes(permissoesNecessarias, FotosActivity.this, 1);


        //Componentes para adicionar fotos pela galeria
        btnCadastrar = findViewById(R.id.btnCadastrar);
        fbFotoOne = findViewById(R.id.fbFotoOne);
        fbFotoTwo = findViewById(R.id.fbFotoTwo);
        fbFotoThree = findViewById(R.id.fbFotoThree);
        fbFotoFour = findViewById(R.id.fbFotoFour);
        fbFotoFive = findViewById(R.id.fbFotoFive);
        fbFotoSix = findViewById(R.id.fbFotoSix);


/*
        //Evento de clique para abrir camêra
        fbFotoTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(FotosActivity.this.getPackageManager()) != null) {
                    startActivityForResult(i, SELECTION_CAMERA);

                }

            }
        });

 */

/*
        btnContinuarFotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                startActivity(new Intent(FotosActivity.this, NavigationDrawerActivity.class));

            }
        });

 */

    }

    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){

        //Criar nó no storage
        StorageReference imagemUsuario = storage.child("imagens")
                .child("fotosUsuario")
                .child(usuario.getIdUsuario())
                .child("imagem"+contador);

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemUsuario.putFile( Uri.parse(urlString) );
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                String urlConvertida = firebaseUrl.toString();

                listaURLFotos.add( urlConvertida );



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //exibirMensagemErro("Falha ao fazer upload");
                Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());
            }
        });

    }


    //Listener, ao clicar nas imagens ele vai ter um ouvinte.
    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.imageUsuario1:
                escolherImagem(1);
                break;
            case R.id.imageUsuario2:
                escolherImagem(2);
                break;
            case R.id.imageUsuario3:
                escolherImagem(3);
                break;
            case R.id.imageUsuario4:
                escolherImagem(4);
                break;
            case R.id.imageUsuario5:
                escolherImagem(5);
                break;
            case R.id.imageUsuario6:
                escolherImagem(6);
                break;

        }

    }



    //Coloque em uma classe separada esse método
    public void salvarFotos(){

        /*
        Salvar imagens no storage
         */

        for(int i = 0; i < listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i );
        }

    }


    public void validarDadosFotos(View view){


        //Verifica se o usuário pelo menos selecionou uma foto.
    if(listaFotosRecuperadas.size() != 0 ){
        salvarFotos();
        //startActivity(new Intent(FotosActivity.this, NavigationDrawerActivity.class));
    }else {

    }


    }




    public void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            //Chamando o método do AlertDialog
        for(int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaPermissao();
            }
        }
    }
            //AlerDialog para permissões
        private void alertaPermissao(){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permissões Necessárias");
            builder.setMessage("Para utilizar esse recurso é necessário aceitar as persmissões");
            builder.setCancelable(false);
            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == FotosActivity.RESULT_OK){

            //Recupera imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if(requestCode == 1){
                imageUsuario1.setImageURI(imagemSelecionada);
            }else if(requestCode == 2){
                imageUsuario2.setImageURI(imagemSelecionada);
            }else if(requestCode == 3){
                imageUsuario3.setImageURI(imagemSelecionada);
            }else if(requestCode == 4){
                imageUsuario4.setImageURI(imagemSelecionada);
            }else if(requestCode == 5){
                imageUsuario5.setImageURI(imagemSelecionada);
            }else if(requestCode == 6){
                imageUsuario6.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImagem);

        }


    }


    //Recuperando a imagem e enviado para tela de filtros


   //até aqui


//

    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }


}
