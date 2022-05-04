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
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
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
    private Button buttonSalvarEdicao;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    //Dados da edição de postagem
    private String tituloPostagem, descricaoPostagem,
            fotoPostagem, posicaoOriginal;
    private int  posicaoRecebida;
    private String idPostagem;
    private DatabaseReference verificaDescricaoRef,verificaTituloRef;

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
                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.putExtra("atualize","atualize");
                startActivity(intent);
                finish();
            }
        });

        //Recuperando a foto a ser editada.
        Bundle dados = getIntent().getExtras();

        if(dados != null){

            //Dados da edição de postagem
            //Recebido através do AdapterFotosPostadas
            tituloPostagem = dados.getString("titulo");
            descricaoPostagem = dados.getString("descricao");
            posicaoOriginal = String.valueOf(dados.getInt("posicao"));
            posicaoRecebida = Integer.parseInt(posicaoOriginal);
            fotoPostagem = dados.getString("foto");
            //Recebido através da PerfilFragment
            idPostagem = dados.getString("idPostagem");

            if(fotoPostagem != null){
                //Exibindo título da postagem a ser editado
                edtTextTituloFoto.setText(tituloPostagem);
                //Exibindo descrição da postagem a ser editada
                edtTextDescricaoFoto.setText(descricaoPostagem);
                //Exibindo foto a ser exibida na edição
                GlideCustomizado.montarGlideFoto(getApplicationContext(),fotoPostagem,imageViewFotoEditada, android.R.color.transparent);
            }else{
                //Caso o usuário esteja somente adicionando uma foto
                //ele cai aqui, somente se fosse uma edição ele cairia no if
                String fotoTeste = dados.getString("fotoOriginal");
                GlideCustomizado.montarGlideFoto(getApplicationContext(),fotoTeste,imageViewFotoEditada, android.R.color.transparent);
            }

            edtTextTituloFoto.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    contadorTitulo.setText(charSequence.length() + "/122");
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
        }
            verificaTituloRef = firebaseRef.child("postagensUsuario")
                    .child(idUsuario).child(idPostagem).child("tituloPostagem");
            verificaDescricaoRef = firebaseRef.child("postagensUsuario")
                    .child(idUsuario).child(idPostagem).child("descricaoPostagem");

        buttonSalvarEdicao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoTitulo = edtTextTituloFoto.getText().toString();
                String textoDescricao = edtTextDescricaoFoto.getText().toString();
                try{
                    String tituloVazio;
                    //Caso o título e descrição estejam preenchidos
                    if(!textoTitulo.isEmpty() && !textoDescricao.isEmpty()){
                        if(textoTitulo.length() > 122 || textoDescricao.length() > 2000){
                            ToastCustomizado.toastCustomizadoCurto("Limite máximo de caracteres atingido!",getApplicationContext());
                        }else{
                            if(tituloPostagem != null && descricaoPostagem != null){
                                if(textoTitulo.equals(tituloPostagem) && textoDescricao.equals(descricaoPostagem)){
                                    Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                    intent.putExtra("atualizarEdicao", posicaoRecebida);
                                    startActivity(intent);
                                    finish();
                                }else {
                                   tituloVazio = textoTitulo;
                                    verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful()){
                                               String descricaoVazia;
                                               descricaoVazia = textoDescricao;
                                               verificaDescricaoRef.setValue(descricaoVazia).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                       if(task.isSuccessful()){
                                                           Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                                           intent.putExtra("atualizarEdicao", posicaoRecebida);
                                                           startActivity(intent);
                                                           finish();
                                                       }
                                                   }
                                               });
                                           }
                                       }
                                   });
                                }
                            }else{
                                tituloVazio = textoTitulo;
                                verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            String descricaoVaziaNew;
                                            descricaoVaziaNew = textoDescricao;
                                            verificaDescricaoRef.setValue(descricaoVaziaNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
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
                        }

                    }  else if (!textoTitulo.isEmpty() || !textoDescricao.isEmpty()){
                        if(textoTitulo.length() > 122 || textoDescricao.length() > 2000){
                            ToastCustomizado.toastCustomizadoCurto("Limite máximo de caracteres atingido!",getApplicationContext());
                        }else{
                            if(tituloPostagem != null && descricaoPostagem != null){
                                if(textoTitulo.equals(tituloPostagem) && textoDescricao.equals(descricaoPostagem)){
                                    Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                    intent.putExtra("atualizarEdicao", posicaoRecebida);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    tituloVazio = textoTitulo;
                                    verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                String descricaoVazia;
                                                descricaoVazia = textoDescricao;
                                                verificaDescricaoRef.setValue(descricaoVazia).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                                            intent.putExtra("atualizarEdicao", posicaoRecebida);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }else{
                                tituloVazio = textoTitulo;
                                verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            if(!textoDescricao.isEmpty()){
                                                String descricaoVaziaNova;
                                                descricaoVaziaNova = textoDescricao;
                                                verificaDescricaoRef.setValue(descricaoVaziaNova).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }else{
                                                String descricaoVaziaNova;
                                                descricaoVaziaNova = textoDescricao;
                                                verificaDescricaoRef.setValue(descricaoVaziaNova).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
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
                    }

                    //Caso o título e descrição estejam vazios
                    else if(textoTitulo.isEmpty() && textoDescricao.isEmpty()){
                        if(tituloPostagem != null && descricaoPostagem != null){
                            verificaTituloRef.setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        verificaDescricaoRef.setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                                    intent.putExtra("atualizarEdicao", posicaoRecebida);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }else{
                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    else if(textoTitulo.isEmpty()){
                        if(tituloPostagem != null && descricaoPostagem != null){
                            verificaTituloRef.setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                        intent.putExtra("atualizarEdicao", posicaoRecebida);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }else{
                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    else if (textoDescricao.isEmpty()){
                        if(tituloPostagem != null && descricaoPostagem != null){
                            verificaDescricaoRef.setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                        intent.putExtra("atualizarEdicao", posicaoRecebida);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }else{
                            Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        intent.putExtra("atualize","atualize");
        startActivity(intent);
        finish();
    }
}