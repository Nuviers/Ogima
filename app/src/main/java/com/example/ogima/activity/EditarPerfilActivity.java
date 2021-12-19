package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.ApelidoActivity;
import com.example.ogima.ui.cadastro.GeneroActivity;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class EditarPerfilActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageButton imageButtonAlterarGenero, imageButtonAlterarApelido,
    imageButtonAlterarNome, imageButtonAlterarLink, imageButtonAlterarNumero;
    private TextView textViewApelidoAtual, textViewNomeAtual, textViewGeneroAtual,
            textViewNumeroAtual;
    private ListView listaInteresses;
    private Button buttonVoltar;
    private Usuario usuarioLogado;

    private String emailUser;
    public Usuario usuario;
    private ImageView imageViewPerfilAlterar, imageViewFundoPerfilAlterar;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String nome, genero, apelido, numero, fotoPerfil, fundoPerfil,
    link, dadoModificado;
    private ArrayList<String> arrayInteresse = new ArrayList<>();
    ArrayAdapter<String> adapterInteresse;

    private BottomSheetDialog bottomSheetDialog;
    private ImageButton imageButtonAlterar;

    private String generoRecebido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

       try{
            //autenticacao.getCurrentUser().reload();
           // Bundle dados = getIntent().getExtras();
           // if(dados != null){
               // generoRecebido = dados.getString("generoEnviado");
              //  dadosRecuperados(generoRecebido, "generoUsuario");
           // }else{
                dadosRecuperados("inicio", "inicio");
           // }
        }catch (Exception ex){
            ex.printStackTrace();
        }

         //if(generoRecebido != null){
             //Toast.makeText(getApplicationContext(), "Genero novo " + generoRecebido, Toast.LENGTH_SHORT).show();
         //}

        //Inicializar componentes
        textViewNomeAtual = findViewById(R.id.textViewNomeAtual);
        textViewApelidoAtual = findViewById(R.id.textViewApelidoAtual);
        textViewGeneroAtual = findViewById(R.id.textViewGeneroAtual);
        textViewNumeroAtual = findViewById(R.id.textViewNumeroAtual);
        imageViewPerfilAlterar = findViewById(R.id.imageViewPerfilAlterar);
        imageViewFundoPerfilAlterar = findViewById(R.id.imageViewFundoPerfilAlterar);
        listaInteresses = findViewById(R.id.listViewInteresses);
        buttonVoltar = findViewById(R.id.buttonVoltar);

        imageButtonAlterarNome = findViewById(R.id.imageButtonAlterarNome);
        imageButtonAlterarApelido = findViewById(R.id.imageButtonAlterarApelido);
        imageButtonAlterarGenero = findViewById(R.id.imageButtonAlterarGenero);
        imageButtonAlterarLink = findViewById(R.id.imageButtonAlterarLink);
        imageButtonAlterarNumero = findViewById(R.id.imageButtonAlterarNumero);


        //Configurando clique dos botões
        imageButtonAlterarNome.setOnClickListener(this);
        imageButtonAlterarApelido.setOnClickListener(this);
        imageButtonAlterarGenero.setOnClickListener(this);
        imageButtonAlterarLink.setOnClickListener(this);
        imageButtonAlterarNumero.setOnClickListener(this);


        buttonVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
               // Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
               // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
               // startActivity(intent);
               // finish();
            }
        });

    }

    private void showBottomSheetDialog(String dadoAtual, String filho){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(EditarPerfilActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_alterar_layout);
        //bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);
        //LinearLayout copy = bottomSheetDialog.findViewById(R.id.copyLinearLayout);
        //LinearLayout share = bottomSheetDialog.findViewById(R.id.shareLinearLayout);
        //LinearLayout upload = bottomSheetDialog.findViewById(R.id.uploadLinearLayout);
        //LinearLayout download = bottomSheetDialog.findViewById(R.id.download);
        //LinearLayout delete = bottomSheetDialog.findViewById(R.id.delete);

        LinearLayout alterarDado = bottomSheetDialog.findViewById(R.id.alterarLinearLayout);
        ImageView imageViewAlterarDado = bottomSheetDialog.findViewById(R.id.imageViewAlterar);
        EditText editTextNomeAlterar = bottomSheetDialog.findViewById(R.id.editTextNomeAlterar);

        //*dadosRecuperados("inicio","inicio");
        try{
            editTextNomeAlterar.setText(dadoAtual);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        alterarDado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        imageViewAlterarDado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dadoModificado = editTextNomeAlterar.getText().toString();
                Toast.makeText(getApplicationContext(), "Novo dado " + dadoModificado, Toast.LENGTH_SHORT).show();

                if(!dadoAtual.equals(dadoModificado)){
                    Toast.makeText(getApplicationContext(), "São diferentes " + dadoAtual + " e " + dadoModificado, Toast.LENGTH_SHORT).show();

                    try{
                        dadosRecuperados(dadoModificado, filho);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

                //dadosRecuperados(dadoModificado);
            }
        });

        bottomSheetDialog.show();
    }

    public void dadosRecuperados(String novoDado, String filho){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        //DatabaseReference filhoRef = firebaseRef.child("usuarios").child(idUsuario).child(filho);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    //Log.i("FIREBASE", usuario.getIdUsuario());
                    //Log.i("FIREBASEA", usuario.getNomeUsuario());
                    //meuFundo = usuario.getMeuFundo();
                    //minhaFoto = usuario.getMinhaFoto();
                    nome = usuario.getNomeUsuario();
                    apelido = usuario.getApelidoUsuario();
                    arrayInteresse = usuario.getInteresses();
                    genero = usuario.getGeneroUsuario();
                    numero = usuario.getNumero();
                    //link = usuario.getLinkUsuario();
                    fotoPerfil = usuario.getMinhaFoto();
                    fundoPerfil = usuario.getMeuFundo();

                    //Criando adaptador para listview
                    adapterInteresse = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            arrayInteresse);

                    if(emailUsuario != null){

                        try {
                            textViewNomeAtual.setText(nome);
                            textViewApelidoAtual.setText(apelido);
                            textViewGeneroAtual.setText(genero);
                            textViewNumeroAtual.setText(numero);
                            listaInteresses.setAdapter(adapterInteresse);

                            if(fotoPerfil != null){
                                Glide.with(EditarPerfilActivity.this)
                                        .load(fotoPerfil)
                                        .placeholder(R.drawable.testewomamtwo)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageViewPerfilAlterar);
                            }else{
                                Glide.with(EditarPerfilActivity.this)
                                        .load(R.drawable.testewomamtwo)
                                        .placeholder(R.drawable.testewomamtwo)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageViewPerfilAlterar);
                            }

                            if(fundoPerfil != null){
                                Glide.with(EditarPerfilActivity.this)
                                        .load(fundoPerfil)
                                        .placeholder(R.drawable.placeholderuniverse)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imageViewFundoPerfilAlterar);
                            }else{
                                Glide.with(EditarPerfilActivity.this)
                                        .load(R.drawable.placeholderuniverse)
                                        .placeholder(R.drawable.placeholderuniverse)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imageViewFundoPerfilAlterar);
                            }
                            usuarioRef.removeEventListener(this);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }

                    /*
                    if(!filho.equals("inicio") && !novoDado.equals("inicio")){
                        if (filho != null && novoDado != null) {

                            try{

                                filhoRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        filhoRef.setValue(novoDado);
                                        //autenticacao.getCurrentUser().reload();
                                        Toast.makeText(getApplicationContext(), "Alterado com sucesso!", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                     */



                    }else if(snapshot == null) {

                        Toast.makeText(getApplicationContext(), " Nenhum dado localizado", Toast.LENGTH_SHORT).show();


                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();

            }
        });

        /*
        if(!filho.equals("inicio") && !novoDado.equals("inicio")){
            if (filho != null && novoDado != null) {
                try{
                    filhoRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            filhoRef.setValue(novoDado);
                            //autenticacao.getCurrentUser().reload();
                            Toast.makeText(getApplicationContext(), "Alterado com sucesso!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
         */
    }


    @Override
    public void onBackPressed() {

        // Método para bloquear o retorno.
          Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
          startActivity(intent);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.imageButtonAlterarNome:{
                    //Toast.makeText(getApplicationContext(), "Clicado alterar nome", Toast.LENGTH_SHORT).show();
                    //showBottomSheetDialog(nome, "nomeUsuario");
                    Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("alterarNome", nome);
                    startActivity(intent);
                break;
            }

            case R.id.imageButtonAlterarApelido:{

                Intent intent = new Intent(getApplicationContext(), ApelidoActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("alterarApelido", apelido);
                startActivity(intent);
                break;
               /*
                try{
                    Toast.makeText(getApplicationContext(), "Clicado alterar apelido", Toast.LENGTH_SHORT).show();
                    showBottomSheetDialog(apelido, "apelidoUsuario");
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                 */
            }

            case R.id.imageButtonAlterarGenero:{

                //Toast.makeText(getApplicationContext(), "Clicado alterar genero", Toast.LENGTH_SHORT).show();
                //showBottomSheetDialog(genero, "generoUsuario");
                Intent intent = new Intent(getApplicationContext(), GeneroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("alterarGenero", genero);
                startActivity(intent);
                break;
            }

            case R.id.imageButtonAlterarLink:{

                try {
                    Toast.makeText(getApplicationContext(), "Clicado alterar link", Toast.LENGTH_SHORT).show();
                    showBottomSheetDialog(link, "linkUsuario");
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                break;
            }

            case R.id.imageButtonAlterarNumero:{

                try {
                    Toast.makeText(getApplicationContext(), "Clicado alterar numero", Toast.LENGTH_SHORT).show();
                    showBottomSheetDialog(numero, "numero");
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                break;
            }
        }
    }
}

