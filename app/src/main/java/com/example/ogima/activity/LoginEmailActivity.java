package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.VerificaEmailActivity;
import com.example.ogima.ui.cadastro.ViewCadastroActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginEmailActivity extends AppCompatActivity {

    private TextView buttonProblemConta;
    private EditText edtLoginEmail, edtLoginSenha;

    //
    private FirebaseAuth autenticarUsuario;

    //////
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String apelido;
    private ProgressBar progressBarLogin;
    private ImageView imageViewLoginEmail;

    private Usuario usuarioPendente = new Usuario();
    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListener;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            usuarioRef.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        buttonProblemConta = findViewById(R.id.buttonProblemConta);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginSenha = findViewById(R.id.edtLoginSenha);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        imageViewLoginEmail = findViewById(R.id.imageViewLoginEmail);

        autenticarUsuario = ConfiguracaoFirebase.getFirebaseAutenticacao();

        buttonProblemConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    public void loginUsuario(Usuario usuario) {

        autenticarUsuario.signInWithEmailAndPassword(
                usuario.getEmailUsuario(),
                usuario.getSenhaUsuario()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    progressBarLogin.setVisibility(View.GONE);
                    verificaUsuario();

                } else {
                    progressBarLogin.setVisibility(View.GONE);
                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        excecao = "Esta conta não existe";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "E-mail e senha não correspondem a um usuário cadastrado";
                    } catch (Exception e) {
                        excecao = "Erro ao logar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    ToastCustomizado.toastCustomizado(excecao, getApplicationContext());
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                try {
                    progressBarLogin.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void validarCredenciaisUsuario(View view) {

        String campoEmail = edtLoginEmail.getText().toString();
        String campoSenha = edtLoginSenha.getText().toString();

        if (campoEmail.isEmpty()) {
            ToastCustomizado.toastCustomizado("Digite seu email!", getApplicationContext());
        }

        if (campoSenha.isEmpty()) {
            ToastCustomizado.toastCustomizado("Digite sua senha!", getApplicationContext());
        }

        if (!campoEmail.isEmpty() && !campoSenha.isEmpty()) {

            try {
                progressBarLogin.setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Usuario usuario = new Usuario();
            usuario.setEmailUsuario(campoEmail);
            usuario.setSenhaUsuario(campoSenha);

            loginUsuario(usuario);

            //startActivity(new Intent(LoginEmailActivity.this, NavigationDrawerActivity.class));
        }

    }

    public void telaCadastro(View view) {
        startActivity(new Intent(LoginEmailActivity.this, ViewCadastroActivity.class));
    }

    public void verificaUsuario() {

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

       valueEventListener = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    SnackbarUtils.showSnackbar(buttonProblemConta, "Login bem-sucedido");

                    Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    ToastCustomizado.toastCustomizado("Conta não cadastrada", getApplicationContext());

                    String campoEmail = edtLoginEmail.getText().toString();

                    usuarioPendente.setEmailUsuario(campoEmail);

                    Intent intent = new Intent(getApplicationContext(), VerificaEmailActivity.class);
                    intent.putExtra("dadosUsuario", usuarioPendente);
                    startActivity(intent);
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado("Ocorreu um erro " + error.getMessage(), getApplicationContext());
            }
        });
    }
}
