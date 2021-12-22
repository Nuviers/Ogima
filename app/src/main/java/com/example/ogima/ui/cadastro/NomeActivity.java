package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class NomeActivity extends AppCompatActivity {

    private Button btnContinuarNome;
    private EditText editNome;
    //public String textoNome;
    private TextView txtMensagemN;

    private Usuario usuario;

    public String capturedName;

    private GoogleSignInClient mSignInClient;

    private FirebaseAuth autenticacao;

    private FirebaseUser user;
    private String nomeRecebido;

    private FirebaseAuth autenticacaoNova = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    private FloatingActionButton floatingVoltarNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_nome);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        user = autenticacao.getCurrentUser();

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNome = findViewById(R.id.btnCadastrar);
        editNome = findViewById(R.id.editNome);
        txtMensagemN = findViewById(R.id.txtMensagemN);
        floatingVoltarNome = findViewById(R.id.floatingVoltarNome);

        usuario = new Usuario();

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        //Recebendo Email/Senha
        Bundle dados = getIntent().getExtras();
        usuario = (Usuario) dados.getSerializable("dadosUsuario");

        if (dados != null) {
            nomeRecebido = dados.getString("alterarNome");
            //usuario = (Usuario) dados.getSerializable("dadosUsuario");
        }

        if (nomeRecebido != null) {
            try {
                floatingVoltarNome.setVisibility(View.VISIBLE);
                editNome.setText(nomeRecebido);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            floatingVoltarNome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            });

        } else {

            if (signInAccount != null) {
                editNome.setText(signInAccount.getDisplayName());
                capturedName = signInAccount.getDisplayName();
                // Pegar o valor dentro do edit se ele for diferente de nulo
                // e colocar nas regras, não se esqueça de instanciar o usuario
            }

            Toast.makeText(getApplicationContext(), " Nome Google " + capturedName, Toast.LENGTH_SHORT).show();

            usuario.setEmailUsuario(user.getEmail());

            if (user.isEmailVerified()) {
                usuario.setStatusEmail("Verificado");
            }


        }


        btnContinuarNome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (nomeRecebido != null) {

                    alterarNome();

                } else {

                    String textoNome = editNome.getText().toString();

                    Toast.makeText(getApplicationContext(), " Nome campo " + editNome.getText(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), " Nome texto " + textoNome, Toast.LENGTH_SHORT).show();


                    Toast.makeText(NomeActivity.this, " Email "
                            + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                            + " Número " + usuario.getNumero(), Toast.LENGTH_SHORT).show();


                    if (!textoNome.isEmpty()) {
                        if (textoNome.length() > 70) {
                            txtMensagemN.setText("Limite de caracteres excedido, limite máximo são 70 caracteres");
                        } else if (capturedName == null || textoNome != signInAccount.getDisplayName()) {
                            usuario.setNomeUsuario(textoNome);
                            Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                            intent.putExtra("dadosUsuario", usuario);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //*intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            //finish();

                        } else {
                            usuario.setNomeUsuario(capturedName);

                            //Enviando nome pelo objeto
                            Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                            intent.putExtra("dadosUsuario", usuario);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //*intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            //finish();

                        }
                    } else {
                        txtMensagemN.setText("Digite seu nome");
                    }

                    //****
                    //UsuarioFirebase.atualizarNomeUsuario(usuario.getNomeUsuario());

/*
                if(!textoNome.isEmpty()){

                    if(textoNome.length() > 70){
                        txtMensagemN.setText("Limite de caracteres excedido, limite máximo são 70 caracteres");
                    }if(capturedName == null || textoNome != signInAccount.getDisplayName()){
                        usuario.setNomeUsuario(textoNome);

                        Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                        intent.putExtra("dadosUsuario", usuario);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();

                    } else {
                        usuario.setNomeUsuario(capturedName);

                        //Enviando nome pelo objeto
                        Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
                        intent.putExtra("dadosUsuario", usuario);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();

                    }
                }
                else if(textoNome.isEmpty()){
                    txtMensagemN.setText("Digite seu nome");
                }
                */
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        // Método para retorno

        if (nomeRecebido != null) {
            super.onBackPressed();
        } else {

        }
        //Intent intent = new Intent(getApplicationContext(), ViewCadastroActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //startActivity(intent);
        //finish();
    }


    public void alterarNome() {

        if (nomeRecebido != null) {
            String textoNome = editNome.getText().toString();
            if (!textoNome.isEmpty()) {
                if (textoNome.length() > 70) {
                    txtMensagemN.setText("Limite de caracteres excedido, limite máximo são 70 caracteres");
                } else {

                    String emailUsuario = autenticacaoNova.getCurrentUser().getEmail();
                    String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                    DatabaseReference nomeRef = firebaseRef.child("usuarios").child(idUsuario);
                    nomeRef.child("nomeUsuario").setValue(textoNome).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //autenticacaoNova.getCurrentUser().reload();
                                Toast.makeText(getApplicationContext(), "Alterado com sucesso", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Ocorreu um erro ao atualizar dado, tente novamente!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }

            } else {
                txtMensagemN.setText("Digite seu nome");

            }
        }
    }
}

