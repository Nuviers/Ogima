package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class EditarPerfilActivity extends AppCompatActivity {


    private ImageButton imageButtonSalvarNome;
    private TextView textViewApelidoAtual, textViewNomeAtual;
    private ListView listaInteresses;
    private Button buttonVoltar;
    private Usuario usuarioLogado;

    private String apelido;
    private String emailUser;
    public Usuario usuario;
    private String minhaFoto;
    private String meuFundo;
    private ImageView imageViewTeste, imageViewFundo;
    private DatabaseReference firebaseRefN = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacaoN = ConfiguracaoFirebase.getFirebaseAutenticacao();


    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String nome;
    private ArrayList<String> arrayInteresse = new ArrayList<>();
    ArrayAdapter<String> adapterInteresse;

    private BottomSheetDialog bottomSheetDialog;
    private ImageButton imageButtonAlterar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //Inicializar componentes
        textViewNomeAtual = findViewById(R.id.textViewNomeAtual);
        textViewApelidoAtual = findViewById(R.id.textViewApelidoAtual);
        listaInteresses = findViewById(R.id.listViewInteresses);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        imageButtonAlterar = findViewById(R.id.imageButtonAlterarG);

        dadosRecuperados();

        imageButtonAlterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             showBottomSheetDialog();
            }
        });

        buttonVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    //

    public void testandoLog(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);


        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    Log.i("FIREBASE", usuario.getIdUsuario());
                    Log.i("FIREBASEA", usuario.getNomeUsuario());
                    apelido = usuario.getApelidoUsuario();
                    meuFundo = usuario.getMeuFundo();
                    minhaFoto = usuario.getMinhaFoto();

                    if(apelido != null){

                        Toast.makeText(getApplicationContext(), " Okay", Toast.LENGTH_SHORT).show();

                        if(minhaFoto != null){
                            Picasso.get().load(minhaFoto).into(imageViewTeste);
                            Log.i("IMAGEM", "Sucesso ao atualizar foto de perfil");
                        }else{
                            Log.i("IMAGEM", "Falha ao atualizar foto de perfil");
                        }

                        if(meuFundo != null){
                            Picasso.get().load(meuFundo).into(imageViewFundo);
                            Log.i("IMAGEM", "Sucesso ao atualizar fundo de perfil");
                        }else{
                            Log.i("IMAGEM", "Falha ao atualizar fundo de perfil");
                        }

                    }else if(snapshot == null) {

                        Toast.makeText(getApplicationContext(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();


                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Por favor termine seu cadastro", Toast.LENGTH_SHORT).show();
                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void showBottomSheetDialog(){
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


        alterarDado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        imageViewAlterarDado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dadosRecuperados();
                try{
                    editTextNomeAlterar.setText(nome);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        bottomSheetDialog.show();
    }

    public void dadosRecuperados(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

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

                    //Criando adaptador para listview
                    adapterInteresse = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            arrayInteresse);

                    if(nome != null){
                        nome = usuario.getNomeUsuario();
                        textViewNomeAtual.setText(nome);
                        textViewApelidoAtual.setText(apelido);
                        listaInteresses.setAdapter(adapterInteresse);
                    }

                    }else if(snapshot == null) {

                        Toast.makeText(getApplicationContext(), " Nenhum dado localizado", Toast.LENGTH_SHORT).show();


                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }

    }

