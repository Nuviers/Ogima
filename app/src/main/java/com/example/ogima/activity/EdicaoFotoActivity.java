package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;

public class EdicaoFotoActivity extends AppCompatActivity {

    private ImageView imageViewFotoEditada;
    private byte[] dadosImagem;
    //byteArray convertido para Bitmap
    private Bitmap fotoFormatada;
    private Button buttonSalvarEdicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_foto);
        inicializarComponentes();

        //Recuperando a foto a ser editada.
        Bundle dados = getIntent().getExtras();

        if(dados != null){
            //dadosImagem = dados.getByteArray("fotoOriginal");
            String fotoTeste = dados.getString("fotoOriginal");
            //Convertendo byteArray para Bitmap.
            //fotoFormatada = BitmapFactory.decodeByteArray(dadosImagem,0,dadosImagem.length);
            GlideCustomizado.montarGlideFoto(getApplicationContext(),fotoTeste,imageViewFotoEditada, android.R.color.transparent);

            buttonSalvarEdicao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                    startActivity(intent);

                }
            });
        }
    }

    private void inicializarComponentes() {
        imageViewFotoEditada = findViewById(R.id.imageViewFotoEditada);
        buttonSalvarEdicao = findViewById(R.id.buttonSalvarEdicao);
    }
}