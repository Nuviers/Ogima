package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.Permissao;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class PostagemActivity extends AppCompatActivity {

    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    //Referências
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private StorageReference imagemRef;
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
    private String selecionadoCameraPostagem;
    //Componentes
    private ImageButton imgButtonVoltarPostagemPerfil, imgBtnAddCameraPostagem,
            imgBtnAddGaleriaPostagem, imgBtnAddGifPostagem,
            imgBtnAddVideoPostagem;
    private ProgressDialog progressDialog;

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
        progressDialog = new ProgressDialog(getApplicationContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        imgButtonVoltarPostagemPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        imgButtonVoltarPostagemPerfil = findViewById(R.id.imgButtonVoltarPostagemPerfil);
        imgBtnAddCameraPostagem = findViewById(R.id.imgBtnAddCameraPostagem);
        imgBtnAddGaleriaPostagem = findViewById(R.id.imgBtnAddGaleriaPostagem);
        imgBtnAddGifPostagem = findViewById(R.id.imgBtnAddGifPostagem);
        imgBtnAddVideoPostagem = findViewById(R.id.imgBtnAddVideoPostagem);
    }
}