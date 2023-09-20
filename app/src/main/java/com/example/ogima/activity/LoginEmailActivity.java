package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseUtils;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.SnackbarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
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

import java.util.Objects;

public class LoginEmailActivity extends AppCompatActivity {

    private Button btnSignInEmail, btnNoAccount, btnAccountProblem;
    private EditText edtTxtLoginEmail, edtTxtLoginPass;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private ProgressBar progressBarLogin;
    private Usuario usuarioPendente;
    private String idUsuario = "", emailUsuario = "";
    private DatabaseReference usuarioRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        inicializandoComponentes();
        clickListeners();
    }

    public void validarCredenciaisUsuario(String email, String password) {
        ProgressBarUtils.exibirProgressBar(progressBarLogin, LoginEmailActivity.this);
        if (auth != null) {
            auth.signInWithEmailAndPassword(
                    email,
                    password
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Usuário foi logado com sucesso.
                        ProgressBarUtils.ocultarProgressBar(progressBarLogin, LoginEmailActivity.this);
                        verificarUsuario();
                    } else {
                        ProgressBarUtils.ocultarProgressBar(progressBarLogin, LoginEmailActivity.this);
                        String excecao = "";
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidUserException e) {
                            excecao = getString(R.string.account_does_not_exist);
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            excecao = getString(R.string.email_and_password_do_not_match);
                        } catch (Exception e) {
                            excecao = getString(R.string.error_login_with_email) + ": " + e.getMessage();
                            e.printStackTrace();
                        }
                        ToastCustomizado.toastCustomizado(excecao, getApplicationContext());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ProgressBarUtils.ocultarProgressBar(progressBarLogin, LoginEmailActivity.this);
                    ToastCustomizado.toastCustomizado(getString(R.string.error_login_with_email) + " " + e.getMessage(), getApplicationContext());
                }
            });
        }
    }

    public void verificarUsuario() {
        if (auth != null && auth.getCurrentUser() != null) {
            emailUsuario = auth.getCurrentUser().getEmail();
            idUsuario = Base64Custom.codificarBase64(emailUsuario);
            //Verifica se existe dados do usuário no servidor.
            usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        SnackbarUtils.showSnackbar(btnAccountProblem, getString(R.string.successful_sign_in));
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //Email ainda não verificado.
                        ToastCustomizado.toastCustomizado(getString(R.string.unregistered_account), getApplicationContext());
                        irParaVerificarEmail();
                    }
                    usuarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    ToastCustomizado.toastCustomizado(getString(R.string.error_login_with_email) + " " + error.getMessage(), getApplicationContext());
                }
            });
        } else {
            ToastCustomizado.toastCustomizado(getString(R.string.error_login_with_email), LoginEmailActivity.this);
        }
    }

    private void clickListeners() {
        btnSignInEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtTxtLoginEmail.getText().toString().trim();
                String password = edtTxtLoginPass.getText().toString().trim();
                if (email != null && !email.isEmpty()
                        && password != null && !password.isEmpty()) {
                    validarCredenciaisUsuario(email, password);
                } else {
                    String faltaPreencher = getString(R.string.login_filling_notice);
                    ToastCustomizado.toastCustomizadoCurto(faltaPreencher, LoginEmailActivity.this);
                }
            }
        });

        btnAccountProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        btnNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaCadastro();
            }
        });
    }

    private void irParaVerificarEmail() {
        String campoEmail = edtTxtLoginEmail.getText().toString().trim();
        usuarioPendente = new Usuario();
        usuarioPendente.setEmailUsuario(campoEmail);
        Intent intent = new Intent(getApplicationContext(), VerificaEmailActivity.class);
        intent.putExtra("dadosUsuario", usuarioPendente);
        startActivity(intent);
        finish();
    }

    private void irParaCadastro() {
        Intent intent = new Intent(LoginEmailActivity.this, ViewCadastroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void inicializandoComponentes() {
        btnSignInEmail = findViewById(R.id.btnSignInEmail);
        btnNoAccount = findViewById(R.id.btnNoAccount);
        btnAccountProblem = findViewById(R.id.btnAccountProblem);
        edtTxtLoginEmail = findViewById(R.id.edtTxtLoginEmail);
        edtTxtLoginPass = findViewById(R.id.edtTxtLoginPass);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }
}
