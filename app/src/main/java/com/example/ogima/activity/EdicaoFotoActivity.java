package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EdicaoFotoActivity extends AppCompatActivity {

    private ImageView imageViewFotoEditada;
    private ImageButton imageButtonBackEdicaoFoto;
    private TextView contadorDescricao,contadorTitulo;
    private EditText edtTextTituloFoto, edtTextDescricaoFoto;
    private byte[] dadosImagem;
    //byteArray convertido para Bitmap
    private Bitmap fotoFormatada;
    private Button buttonSalvarEdicao;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Usuario usuarioFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_foto);
        Toolbar toolbar = findViewById(R.id.toolbarEdicaoFoto);
        setSupportActionBar(toolbar);
        inicializarComponentes();
        setTitle("");
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        imageButtonBackEdicaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Recuperando a foto a ser editada.
        Bundle dados = getIntent().getExtras();

        if(dados != null){
            //dadosImagem = dados.getByteArray("fotoOriginal");
            String fotoTeste = dados.getString("fotoOriginal");
            //Convertendo byteArray para Bitmap.
            //fotoFormatada = BitmapFactory.decodeByteArray(dadosImagem,0,dadosImagem.length);
            GlideCustomizado.montarGlideFoto(getApplicationContext(),fotoTeste,imageViewFotoEditada, android.R.color.transparent);
            DatabaseReference dadosBaseFotosRef = firebaseRef.child("fotosUsuario").child(idUsuario);
            DatabaseReference fotosPostadasRef = firebaseRef.child("fotosUsuario").child(idUsuario).child("listaTituloFotoPostada");
            DatabaseReference dadosFotosPostadasRef = firebaseRef.child("fotosUsuario").child(idUsuario).child("listaTituloFotoPostada");
            DatabaseReference recuperarFotos = firebaseRef.child("fotosUsuario").child(idUsuario);

            DatabaseReference verificaTituloRef = dadosBaseFotosRef.child("listaTituloFotoPostada");
            DatabaseReference verificaDescricaoRef = dadosBaseFotosRef.child("listaDescricaoFotoPostada");

            edtTextTituloFoto.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    contadorTitulo.setText(charSequence.length() + "/200");
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            edtTextDescricaoFoto.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    contadorDescricao.setText(charSequence.length() + "/2000");
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            dadosBaseFotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() != null){
                        usuarioFoto = snapshot.getValue(Usuario.class);
                    }
                    dadosBaseFotosRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            buttonSalvarEdicao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String textoTitulo = edtTextTituloFoto.getText().toString();
                    String textoDescricao = edtTextDescricaoFoto.getText().toString();
                    try{
                        ArrayList<String> tituloVazio = new ArrayList<>();
                        ArrayList<String> descricaoVazia = new ArrayList<>();
                        //Caso o título e descrição estejam preenchidos
                        if(!textoTitulo.isEmpty() && !textoDescricao.isEmpty()){
                            if(textoTitulo.length() > 200 || textoDescricao.length() > 2000){
                                ToastCustomizado.toastCustomizadoCurto("Limite máximo de caracteres atingido!",getApplicationContext());
                            }else{
                                tituloVazio = usuarioFoto.getListaTituloFotoPostada();
                                int posicao = usuarioFoto.getListaTituloFotoPostada().size() - 1;
                                tituloVazio.remove(posicao);
                                tituloVazio.add(textoTitulo);
                                verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isComplete()){
                                            ArrayList<String> descricaoVaziaNew = new ArrayList<>();
                                            descricaoVaziaNew = usuarioFoto.getListaDescricaoFotoPostada();
                                            int posicao = usuarioFoto.getListaDescricaoFotoPostada().size()-1;
                                            descricaoVaziaNew.remove(posicao);
                                            descricaoVaziaNew.add(textoDescricao);
                                            verificaDescricaoRef.setValue(descricaoVaziaNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isComplete()){
                                                        Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                        }  else if (!textoTitulo.isEmpty() || !textoDescricao.isEmpty()){
                            if(textoTitulo.length() > 200 || textoDescricao.length() > 2000){
                                ToastCustomizado.toastCustomizadoCurto("Limite máximo de caracteres atingido!",getApplicationContext());
                            }else{
                              tituloVazio = usuarioFoto.getListaTituloFotoPostada();
                              int posicao = usuarioFoto.getListaTituloFotoPostada().size() - 1;
                              tituloVazio.remove(posicao);
                                tituloVazio.add(textoTitulo);
                                verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isComplete()){
                                            if(!textoDescricao.isEmpty()){
                                                ArrayList<String> descricaoVaziaNova = new ArrayList<>();
                                                descricaoVaziaNova = usuarioFoto.getListaDescricaoFotoPostada();
                                                int posicaoNova = usuarioFoto.getListaDescricaoFotoPostada().size() - 1;
                                                descricaoVaziaNova.remove(posicaoNova);
                                                descricaoVaziaNova.add(textoDescricao);
                                                verificaDescricaoRef.setValue(descricaoVaziaNova).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isComplete()){
                                                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }else{
                                                ArrayList<String> descricaoVaziaNova = new ArrayList<>();
                                                descricaoVaziaNova = usuarioFoto.getListaDescricaoFotoPostada();
                                                int posicao = usuarioFoto.getListaDescricaoFotoPostada().size() - 1;
                                                descricaoVaziaNova.remove(posicao);
                                                descricaoVaziaNova.add(textoDescricao);
                                                verificaDescricaoRef.setValue(descricaoVaziaNova).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isComplete()){
                                                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        //Caso o título e descrição estejam vazios
                       else if(textoTitulo.isEmpty() && textoDescricao.isEmpty()){
                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        else if(textoTitulo.isEmpty()){
                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        else if (textoDescricao.isEmpty()){
                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private void inicializarComponentes() {
        imageViewFotoEditada = findViewById(R.id.imageViewFotoEditada);
        buttonSalvarEdicao = findViewById(R.id.buttonSalvarEdicao);
        imageButtonBackEdicaoFoto = findViewById(R.id.imageButtonBackEdicaoFoto);
        edtTextTituloFoto = findViewById(R.id.edtTextTituloFoto);
        edtTextDescricaoFoto = findViewById(R.id.edtTextDescricaoFoto);
        contadorTitulo = findViewById(R.id.textViewContadorTitulo);
        contadorDescricao = findViewById(R.id.textViewContadorDescricao);
    }
}