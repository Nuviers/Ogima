package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.ProblemasLogin;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;

import java.util.Locale;
import java.util.Objects;

public class CadastroUserActivity extends AppCompatActivity {

    private Button btnCadastrar, btnCadWithGoogle;
    private TextView txtViewAccountProblemCad;
    public Usuario usuario;
    private EditText edtTxtCadEmail, edtTxtCadPass;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private ProgressBar progressBarCadEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);
        inicializandoComponentes();
        clickListeners();
    }

    private void clickListeners() {
        txtViewAccountProblemCad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressBarUtils.ocultarProgressBar(progressBarCadEmail, CadastroUserActivity.this);
                irParaProblemasDeLogin();
            }
        });

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoEmail = edtTxtCadEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
                String textoSenha = edtTxtCadPass.getText().toString().trim();

                if (!textoEmail.isEmpty() && !textoSenha.isEmpty()) {
                    ProgressBarUtils.exibirProgressBar(progressBarCadEmail, CadastroUserActivity.this);
                    if (isValidEmail(textoEmail)) {
                        cadastrarUsuario(textoEmail, textoSenha);
                    } else {
                        ProgressBarUtils.ocultarProgressBar(progressBarCadEmail, CadastroUserActivity.this);
                        ToastCustomizado.toastCustomizado(getString(R.string.invalid_email), getApplicationContext());
                    }
                } else {
                    ProgressBarUtils.ocultarProgressBar(progressBarCadEmail, CadastroUserActivity.this);
                    ToastCustomizado.toastCustomizado(getString(R.string.register_filling_notice), getApplicationContext());
                }
            }
        });

        btnCadWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaCadastroPeloGoogle();
            }
        });
    }

    //Verifica se é um email válido.
    private static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void cadastrarUsuario(String email, String password) {
        if (autenticacao != null) {
            autenticacao.createUserWithEmailAndPassword(
                    email,
                    password
            ).addOnCompleteListener(
                    CadastroUserActivity.this,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                ProgressBarUtils.ocultarProgressBar(progressBarCadEmail, CadastroUserActivity.this);
                                ToastCustomizado.toastCustomizado(getString(R.string.registered_successfully), getApplicationContext());
                                usuario = new Usuario();
                                usuario.setEmailUsuario(email);
                                irParaVerificarEmail();
                            } else {
                                //Tratando exceções
                                ProgressBarUtils.ocultarProgressBar(progressBarCadEmail, CadastroUserActivity.this);
                                String excecao = "";
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    excecao = getString(R.string.password_exception);
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    excecao = getString(R.string.invalid_credential_exception);
                                } catch (FirebaseAuthUserCollisionException e) {
                                    excecao = getString(R.string.collision_exception);
                                } catch (Exception e) {
                                    excecao = getString(R.string.error_in_registration) + ": " + e.getMessage();
                                    e.printStackTrace();
                                }
                                ToastCustomizado.toastCustomizado(excecao, getApplicationContext());
                            }
                        }
                    }
            );
        }
    }

    private void irParaVerificarEmail() {
        Intent intent = new Intent(CadastroUserActivity.this, VerificaEmailActivity.class);
        intent.putExtra("dadosUsuario", usuario);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void irParaProblemasDeLogin() {
        Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void irParaCadastroPeloGoogle() {
        Intent intent = new Intent(getApplicationContext(), ViewCadastroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void inicializandoComponentes() {
        btnCadastrar = findViewById(R.id.btnCadastrarEmail);
        btnCadWithGoogle = findViewById(R.id.btnCadWithGoogle);
        txtViewAccountProblemCad = findViewById(R.id.txtViewAccountProblemCad);
        progressBarCadEmail = findViewById(R.id.progressBarCadEmail);
        edtTxtCadEmail = findViewById(R.id.edtTxtCadEmail);
        edtTxtCadPass = findViewById(R.id.edtTxtCadPass);
    }
}

