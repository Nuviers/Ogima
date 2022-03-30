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
    private byte[] dadosImagem;
    //byteArray convertido para Bitmap
    private Bitmap fotoFormatada;
    private Button buttonSalvarEdicao;
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Usuario usuarioFoto;
    //Dados da edição de postagem
    private String tituloPostagem, descricaoPostagem, fotoPostagem, posicaoOriginal;
    private int  posicaoRecebida;

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
            tituloPostagem = dados.getString("titulo");
            descricaoPostagem = dados.getString("descricao");
            fotoPostagem = dados.getString("foto");
            posicaoOriginal = String.valueOf(dados.getInt("posicao"));
            posicaoRecebida = Integer.parseInt(posicaoOriginal);

            //ToastCustomizado.toastCustomizadoCurto("Posição recebida " + posicaoRecebida,getApplicationContext());

            if(tituloPostagem != null){

                //Exibindo título da postagem a ser editado
                edtTextTituloFoto.setText(tituloPostagem);
                //Exibindo descrição da postagem a ser editada
                edtTextDescricaoFoto.setText(descricaoPostagem);
                //Exibindo foto a ser exibida na edição
                GlideCustomizado.montarGlideFoto(getApplicationContext(),fotoPostagem,imageViewFotoEditada, android.R.color.transparent);

            }else{
                //dadosImagem = dados.getByteArray("fotoOriginal");
                String fotoTeste = dados.getString("fotoOriginal");
                //Convertendo byteArray para Bitmap.
                //fotoFormatada = BitmapFactory.decodeByteArray(dadosImagem,0,dadosImagem.length);
                GlideCustomizado.montarGlideFoto(getApplicationContext(),fotoTeste,imageViewFotoEditada, android.R.color.transparent);
            }


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
        }

        DatabaseReference dadosBaseFotosRef = firebaseRef.child("fotosUsuario").child(idUsuario);
        DatabaseReference verificaTituloRef = dadosBaseFotosRef.child("listaTituloFotoPostada");
        DatabaseReference verificaDescricaoRef = dadosBaseFotosRef.child("listaDescricaoFotoPostada");

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
                    //Caso o título e descrição estejam preenchidos
                    if(!textoTitulo.isEmpty() && !textoDescricao.isEmpty()){
                        if(textoTitulo.length() > 200 || textoDescricao.length() > 2000){
                            ToastCustomizado.toastCustomizadoCurto("Limite máximo de caracteres atingido!",getApplicationContext());
                        }else{
                            if(tituloPostagem != null && descricaoPostagem != null){
                                if(textoTitulo.equals(tituloPostagem) && textoDescricao.equals(descricaoPostagem)){
                                    //ToastCustomizado.toastCustomizadoCurto("Iguais 1",getApplicationContext());
                                    Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                    intent.putExtra("atualizarEdicao", posicaoRecebida);
                                    startActivity(intent);
                                    finish();
                                }else {
                                   //ToastCustomizado.toastCustomizadoCurto("Não iguais 1",getApplicationContext());
                                   tituloVazio = usuarioFoto.getListaTituloFotoPostada();
                                   tituloVazio.remove(posicaoRecebida);
                                   tituloVazio.add(posicaoRecebida,textoTitulo);
                                    verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful()){
                                               ArrayList<String> descricaoVazia = new ArrayList<>();
                                               descricaoVazia = usuarioFoto.getListaDescricaoFotoPostada();
                                               descricaoVazia.remove(posicaoRecebida);
                                               descricaoVazia.add(posicaoRecebida,textoDescricao);
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
                                //ToastCustomizado.toastCustomizadoCurto("Sem recebidos1",getApplicationContext());
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
                        }

                    }  else if (!textoTitulo.isEmpty() || !textoDescricao.isEmpty()){
                        if(textoTitulo.length() > 200 || textoDescricao.length() > 2000){
                            ToastCustomizado.toastCustomizadoCurto("Limite máximo de caracteres atingido!",getApplicationContext());
                        }else{
                            if(tituloPostagem != null && descricaoPostagem != null){
                                if(textoTitulo.equals(tituloPostagem) && textoDescricao.equals(descricaoPostagem)){
                                    Intent intent = new Intent(getApplicationContext(), FotosPostadasActivity.class);
                                    intent.putExtra("atualizarEdicao", posicaoRecebida);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    tituloVazio.clear();
                                    tituloVazio = usuarioFoto.getListaTituloFotoPostada();
                                    tituloVazio.remove(posicaoRecebida);
                                    tituloVazio.add(posicaoRecebida,textoTitulo);
                                    verificaTituloRef.setValue(tituloVazio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                ArrayList<String> descricaoVazia = new ArrayList<>();
                                                descricaoVazia.clear();
                                                descricaoVazia = usuarioFoto.getListaDescricaoFotoPostada();
                                                descricaoVazia.remove(posicaoRecebida);
                                                descricaoVazia.add(posicaoRecebida,textoDescricao);
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
                    }

                    //Caso o título e descrição estejam vazios
                    else if(textoTitulo.isEmpty() && textoDescricao.isEmpty()){
                        if(tituloPostagem != null && descricaoPostagem != null){
                            ArrayList<String> tituloNovo = new ArrayList<>();
                            tituloNovo = usuarioFoto.getListaTituloFotoPostada();
                            tituloNovo.remove(posicaoRecebida);
                            tituloNovo.add(posicaoRecebida,"");
                            verificaTituloRef.setValue(tituloNovo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        ArrayList<String> descricaoNova = new ArrayList<>();
                                        descricaoNova = usuarioFoto.getListaDescricaoFotoPostada();
                                        descricaoNova.remove(posicaoRecebida);
                                        descricaoNova.add(posicaoRecebida, "");
                                        verificaDescricaoRef.setValue(descricaoNova).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            ArrayList<String> tituloNovo = new ArrayList<>();
                            tituloNovo = usuarioFoto.getListaTituloFotoPostada();
                            tituloNovo.remove(posicaoRecebida);
                            tituloNovo.add(posicaoRecebida, "");
                            verificaTituloRef.setValue(tituloNovo).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            ArrayList<String> descricaoNova = new ArrayList<>();
                            descricaoNova = usuarioFoto.getListaDescricaoFotoPostada();
                            descricaoNova.remove(posicaoRecebida);
                            descricaoNova.add(posicaoRecebida, "");
                            verificaDescricaoRef.setValue(descricaoNova).addOnCompleteListener(new OnCompleteListener<Void>() {
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