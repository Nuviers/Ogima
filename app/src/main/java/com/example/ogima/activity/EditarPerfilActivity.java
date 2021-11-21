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
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
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
    private ImageView imageViewTeste;
    private DatabaseReference firebaseRefN = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacaoN = ConfiguracaoFirebase.getFirebaseAutenticacao();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //Inicializar componentes
        imageButtonSalvarNome = findViewById(R.id.imageButtonSalvarNome);
        editTextNomeAtual = findViewById(R.id.editTextNome);
        buttonVoltar = findViewById(R.id.buttonVoltar);

        imageViewTeste = findViewById(R.id.imageViewTeste);


        //Recuperar dados do usuário
        FirebaseUser userProfile = UsuarioFirebase.getUsuarioAtual();
        editTextNomeAtual.setText(userProfile.getDisplayName());

        //Configurando dados do usuario
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //



        imageButtonSalvarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nome = editTextNomeAtual.getText().toString();

                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);

                if (retorno) {

                    usuarioLogado.setNomeUsuario(nome);
                    //usuarioLogado.atualizar();

                    testandoLog();

                    Toast.makeText(getApplicationContext(), " Sucesso ao atualizar nome", Toast.LENGTH_SHORT).show();


                } else {

                    Toast.makeText(getApplicationContext(), " Erro ao atualizar nome", Toast.LENGTH_SHORT).show();
                }
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
    public void testandoLog(){
        String emailUsuario = autenticacaoN.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRefN.child("usuarios").child(idUsuario);

        usuario = new Usuario();

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    Log.i("FIREBASE", usuario.getIdUsuario());
                    Log.i("FIREBASEA", usuario.getNomeUsuario());
                    apelido = usuario.getApelidoUsuario();
                    minhaFoto = usuario.getMinhaFoto();


                    if (apelido != null) {

                        Toast.makeText(getApplicationContext(), " Meu apelido " + apelido, Toast.LENGTH_SHORT).show();


                        if(minhaFoto != null){
                            Picasso.get().load(minhaFoto).into(imageViewTeste);
                           Log.i("IMAGEM", "Sucesoooo");
                        }else{
                            Log.i("IMAGEM", "Faillllll");
                        }

                        //imageViewTeste.set




                    } else if (snapshot == null) {

                        Toast.makeText(getApplicationContext(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();

                        Toast.makeText(getApplicationContext(), "Sem apelido", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "SEI LA", Toast.LENGTH_SHORT).show();


                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), " Cancelado ", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
