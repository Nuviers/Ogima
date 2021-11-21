package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.example.ogima.ui.cadastro.ViewCadastroActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class EditarPerfilActivity extends AppCompatActivity {


    private ImageButton imageButtonSalvarNome;
    private EditText editTextNomeAtual;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //Inicializar componentes
        imageButtonSalvarNome = findViewById(R.id.imageButtonSalvarNome);
        editTextNomeAtual = findViewById(R.id.editTextNome);
        buttonVoltar = findViewById(R.id.buttonVoltar);

        imageViewTeste = findViewById(R.id.imageViewTeste);
        imageViewFundo = findViewById(R.id.imageViewFundo);

        imageButtonSalvarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                testandoLog();

            }
        });

        buttonVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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

    }

