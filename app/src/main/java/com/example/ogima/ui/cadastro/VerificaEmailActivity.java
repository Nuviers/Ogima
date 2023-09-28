package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.CadastroActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

public class VerificaEmailActivity extends AppCompatActivity {

    private Button btnContinuar;
    private Usuario usuario;
    private TextView btnEnviarLink, txtViewEmail, txtViewMsg;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Bundle dados;
    private FirebaseUser currentUser;
    private boolean canSendVerificationLink = true;
    private boolean isVerificationChecking = false;
    private boolean primeiroEnvio = true;
    private Handler handler;

    private interface VerificarEmailCallback {
        void onVerificacao(boolean status);

        void onError(String message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (primeiroEnvio) {
            verificarStatus(new VerificarEmailCallback() {
                @Override
                public void onVerificacao(boolean status) {
                    if (status) {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.verified_account), VerificaEmailActivity.this);
                        continuarCadastro();
                        primeiroEnvio = false;
                    } else {
                        enviarLink();
                        primeiroEnvio = false;
                    }
                }

                @Override
                public void onError(String message) {
                    txtViewMsg.setText(String.format("%s %s", R.string.an_error_has_occurred, message));
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        deslogarUsuario();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!primeiroEnvio) {
            verificarStatus(new VerificarEmailCallback() {
                @Override
                public void onVerificacao(boolean status) {
                    if (status) {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.verified_account), VerificaEmailActivity.this);
                        continuarCadastro();
                    }
                }

                @Override
                public void onError(String message) {
                    txtViewMsg.setText(String.format("%s %s", R.string.an_error_has_occurred, message));
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_verifica_email);
        inicializandoComponentes();
        dados = getIntent().getExtras();
        if (dados != null && dados.containsKey("dadosUsuario")) {
            usuario = new Usuario();
            usuario = (Usuario) dados.getSerializable("dadosUsuario");
            txtViewEmail.setText(usuario.getEmailUsuario());
        }
        currentUser = autenticacao.getCurrentUser();
        clickListeners();

        handler = new Handler();
        final int delay = 20000; // 20 segundos

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!primeiroEnvio) {
                    verificarStatus(new VerificarEmailCallback() {
                        @Override
                        public void onVerificacao(boolean status) {
                            if (status) {
                                ToastCustomizado.toastCustomizadoCurto(getString(R.string.verified_account), VerificaEmailActivity.this);
                                continuarCadastro();
                            }
                        }

                        @Override
                        public void onError(String message) {
                            txtViewMsg.setText(String.format("%s %s", R.string.an_error_has_occurred, message));
                        }
                    });
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void enviarLink() {
        if (currentUser != null && canSendVerificationLink) {
            currentUser.sendEmailVerification()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ToastCustomizado.toastCustomizado(getString(R.string.verification_link_sent), VerificaEmailActivity.this);
                                canSendVerificationLink = false;
                                // Ativar um temporizador de 40 segundos
                                ativarTemporizador();
                            } else {
                                ToastCustomizado.toastCustomizado(getString(R.string.error_sending_verification_link), VerificaEmailActivity.this);
                            }
                        }
                    });
        }
    }

    private void ativarTemporizador() {
        final int segundos = 40;
        new CountDownTimer(segundos * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                txtViewMsg.setText(String.format("%s %d %s", R.string.wait, millisUntilFinished/1000, R.string.seconds_sending_email));
                btnEnviarLink.setEnabled(false);
            }

            public void onFinish() {
                txtViewMsg.setText("");
                btnEnviarLink.setEnabled(true);
                canSendVerificationLink = true;
            }
        }.start();
    }

    private void verificarStatus(VerificarEmailCallback callback) {
        if (currentUser != null && !isVerificationChecking) {
            isVerificationChecking = true;
            currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        isVerificationChecking = false;
                        currentUser = autenticacao.getCurrentUser(); // Atualize o objeto de usuário
                        if (currentUser != null && currentUser.isEmailVerified()) {
                            callback.onVerificacao(true);
                        } else {
                            callback.onVerificacao(false);
                        }
                    } else {
                        callback.onVerificacao(false);
                    }
                }
            });
        }
    }

    private void deslogarUsuario() {
        if (autenticacao.getCurrentUser() != null) {
            autenticacao.signOut();
        }
    }

    private void continuarCadastro() {
        Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
        usuario.setStatusEmail(true);
        intent.putExtra("dadosUsuario", usuario);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void clickListeners() {
        btnEnviarLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarLink();
            }
        });
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarStatus(new VerificarEmailCallback() {
                    @Override
                    public void onVerificacao(boolean status) {
                        if (status) {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.verified_account), VerificaEmailActivity.this);
                            continuarCadastro();
                        } else {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.email_not_verified), VerificaEmailActivity.this);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        txtViewMsg.setText(String.format("%s %s", R.string.an_error_has_occurred, message));
                    }
                });
            }
        });
    }

    private void inicializandoComponentes() {
        btnContinuar = findViewById(R.id.btnContinuarCad);
        txtViewMsg = findViewById(R.id.txtViewMsgVerifEmail);
        btnEnviarLink = findViewById(R.id.btnEnviarLinkVerif);
        txtViewEmail = findViewById(R.id.txtViewEmailVerif);
    }
}