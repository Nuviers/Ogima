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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TodasFotosUsuarioActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef;
    private String tituloPostagem, descricaoPostagem, fotoPostagem,
            posicaoOriginal, idPostagem, dataPostagem;
    private int  posicaoRecebida;

    //Componentes
    private ImageView imgViewFotoPostada;
    private TextView txtViewDescricaoPostada,txtViewTituloPostado;
    private CollapsingToolbarLayout collapsingToolbarPostada;
    private ImageButton imageButtonDenuncia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todas_fotos_usuario);
        inicializandoComponentes();

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        Bundle dados = getIntent().getExtras();
        try{
            if(dados != null){
                //Dados da exibição da postagem
                tituloPostagem = dados.getString("titulo");
                descricaoPostagem = dados.getString("descricao");
                fotoPostagem = dados.getString("foto");
                posicaoOriginal = String.valueOf(dados.getInt("posicao"));
                posicaoRecebida = Integer.parseInt(posicaoOriginal);
                idPostagem = dados.getString("idPostagem");
                dataPostagem = dados.getString("dataPostagem");
                //Exibindo título da postagem
                txtViewTituloPostado.setText(tituloPostagem);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        GlideCustomizado.montarGlideFoto(getApplicationContext(),
             fotoPostagem, imgViewFotoPostada, android.R.color.transparent);

        //Setando a descrição da postagem
        txtViewDescricaoPostada.setText(descricaoPostagem);

        //Teste para salvamento de dados
        DatabaseReference baseFotosPostagemRef = firebaseRef
           .child("postagensUsuario").child(idUsuario).child(idPostagem);

        HashMap<String, Object> dadosPostagem = new HashMap<>();
        dadosPostagem.put("idPostagem", idPostagem);
        dadosPostagem.put("caminhoPostagem", fotoPostagem);
        dadosPostagem.put("tituloPostagem", tituloPostagem);
        dadosPostagem.put("descricaoPostagem", descricaoPostagem);
        dadosPostagem.put("dataPostagem", dataPostagem);
        dadosPostagem.put("idUsuario", idUsuario);

        //id/caminho/data/titulo/descricao/idUsuario
        //*id/caminho/data/titulo/descricao/idUsuario

        baseFotosPostagemRef.setValue(dadosPostagem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    DatabaseReference contadorFotosPostagemRef = firebaseRef
                            .child("postagensUsuario").child(idUsuario);

                    ToastCustomizado.toastCustomizadoCurto("Dados novos incriveis salvos com sucesso!", getApplicationContext());
                }else{
                    ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getApplicationContext());
                }
            }
        });

        imageButtonDenuncia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference denunciarPostagemRef = firebaseRef
                        .child("denunciaPostagem").child(idUsuario)
                        .child("listaIdPostagem");

                DatabaseReference baseDenunciarPostagemRef = firebaseRef
                        .child("denunciaPostagem").child(idUsuario);

                baseDenunciarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            Usuario usuarioUpdate = snapshot.getValue(Usuario.class);
                            ArrayList<String> postagensDenunciadas = new ArrayList<>();

                            postagensDenunciadas = usuarioUpdate.getListaIdPostagem();
                            postagensDenunciadas.add(idPostagem);

                            for(int i = 0; i < postagensDenunciadas.size(); i ++){
                                ToastCustomizado.toastCustomizadoCurto("Item " + postagensDenunciadas.get(i),getApplicationContext());
                                if(postagensDenunciadas.get(i).equals("cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t1")){
                                    ToastCustomizado.toastCustomizadoCurto("Igual",getApplicationContext());
                                }else{
                                    ToastCustomizado.toastCustomizadoCurto("Não é igual",getApplicationContext());
                                }
                                if(i == postagensDenunciadas.size()){
                                    break;
                                }
                            }

                            denunciarPostagemRef.setValue(postagensDenunciadas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        ToastCustomizado.toastCustomizadoCurto("Denunciado",getApplicationContext());
                                    }
                                }
                            });
                        }else{
                            ArrayList<String> postagensDenunciadas = new ArrayList<>();
                            postagensDenunciadas.add(idPostagem);
                            denunciarPostagemRef.setValue(postagensDenunciadas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        ToastCustomizado.toastCustomizadoCurto("Denunciado",getApplicationContext());
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        DatabaseReference pesquisarPostagemRef = firebaseRef
                .child("denunciaPostagem");

        pesquisarPostagemRef.orderByChild("listaIdPostagem")
                .equalTo("cmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshotChild : snapshot.getChildren()){
                    String dado = snapshot.getChildren().iterator().next().getKey();
                    ToastCustomizado.toastCustomizadoCurto("Dado " + snapshotChild.child("listaIdPostagem").getValue(),getApplicationContext());
                    //Toast.makeText(getActivity(), "Dado localizado " + childDataSnapshot.child("numero").getValue(), Toast.LENGTH_SHORT).show();
                    if(snapshot.exists()){
                        ToastCustomizado.toastCustomizadoCurto("Existe", getApplicationContext());
                    }else{
                        ToastCustomizado.toastCustomizadoCurto("Não Existe",getApplicationContext());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void inicializandoComponentes() {
        imgViewFotoPostada = findViewById(R.id.imgViewFotoPostada);
        txtViewTituloPostado = findViewById(R.id.txtViewTituloPostado);
        txtViewDescricaoPostada = findViewById(R.id.txtViewDescricaoPostada);
        collapsingToolbarPostada = findViewById(R.id.collapsingToolbarPostada);
        imageButtonDenuncia = findViewById(R.id.imageButtonDenuncia);
    }
}