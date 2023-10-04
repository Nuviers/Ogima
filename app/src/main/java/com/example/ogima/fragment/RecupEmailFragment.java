package com.example.ogima.fragment;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.RecoveryCounter;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class RecupEmailFragment extends Fragment {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private EditText editTextEmail;
    private Button btnRecupConta;
    private ImageView imgViewFotoRecup;
    private String recuperarDado, emailCriptografado, emailConvertido, fotoUsuario;
    private TextView txtViewMsgRecup;
    private ProgressBar progressBarRecup;
    private CountDownTimer countDownTimer = null;
    private AtualizarContador atualizarContador;

    private interface RecuperarTimeStampCallback {
        void onRecuperado(long timeStamp);

        void onError(String message);
    }

    private interface VerificaValidadeCallback {
        void onResetar();

        void onNaoResetar();

        void onError(String message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recup_email, container, false);
        inicializandoComponentes(view);
        atualizarContador = new AtualizarContador();
        clickListeners();
        return view;
    }

    private void procurarConta() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(emailCriptografado, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                ToastCustomizado.toastCustomizado(getString(R.string.localized_account), getActivity());
                txtViewMsgRecup.setText(getString(R.string.localized_account));
                exibirFotoDaConta(fotoUsuario, epilepsia);
                verificaLimite();
            }

            @Override
            public void onSemDados() {
                ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                txtViewMsgRecup.setText(getString(R.string.email_not_found));
            }

            @Override
            public void onError(String mensagem) {
                ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.error_finding_account), mensagem));
            }
        });
    }

    private void verificaLimite() {

        //Verifica se já existe alguma tentativa de recuperação nessas últimas 24 horas.
        DatabaseReference recoveryCounterRef = firebaseRef.child("emailRecoveryCounter")
                .child(emailCriptografado);

        DatabaseReference counterRef = firebaseRef.child("emailRecoveryCounter")
                .child(emailCriptografado).child("counter");

        recoveryCounterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    RecoveryCounter recoveryCounter = snapshot.getValue(RecoveryCounter.class);
                    if (recoveryCounter != null && recoveryCounter.getCounter() != -1) {
                        int contador = recoveryCounter.getCounter();
                        long timestamp = recoveryCounter.getTimeStampValidity();

                        verificaValidade(timestamp, new VerificaValidadeCallback() {
                            @Override
                            public void onResetar() {
                                atualizarContador(counterRef, 0, -1);
                            }

                            @Override
                            public void onNaoResetar() {
                                if (contador < 10) {
                                    atualizarContador(counterRef, contador, recoveryCounter.getTimeStampValidity());
                                } else {
                                    recuperarTimestamp(new RecuperarTimeStampCallback() {
                                        @Override
                                        public void onRecuperado(long timestampAtual) {
                                            // Calcula a diferença em milissegundos entre o timestamp atual e o anterior
                                            long diferencaEmMilissegundos = timestampAtual - recoveryCounter.getTimeStampValidity();

                                            // Converte a diferença em milissegundos para horas
                                            int horasDeEspera = (int) (diferencaEmMilissegundos / (60 * 60 * 1000));

                                            // Calcula o tempo restante em milissegundos até o reset
                                            long tempoRestanteEmMilissegundos = (24 * 60 * 60 * 1000) - diferencaEmMilissegundos;

                                            // Converte o tempo restante em horas e minutos
                                            int horasRestantes = (int) (tempoRestanteEmMilissegundos / (60 * 60 * 1000));
                                            int minutosRestantes = (int) ((tempoRestanteEmMilissegundos % (60 * 60 * 1000)) / (60 * 1000));

                                            ToastCustomizado.toastCustomizado(String.format("%s %s %s %s", getString(R.string.limit_of_attempts_reached),horasRestantes,":",minutosRestantes), requireContext());
                                        }

                                        @Override
                                        public void onError(String message) {
                                            ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                                            txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.an_error_has_occurred), message));
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(String message) {
                                ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                                txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.an_error_has_occurred), message));
                            }
                        });
                    }
                } else {
                    atualizarContador(counterRef, 0, -1);
                }
                recoveryCounterRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.an_error_has_occurred), error.getMessage()));
            }
        });
    }


    private void exibirContador() {

        try {
            btnRecupConta.setClickable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        countDownTimer = new CountDownTimer(50000, 1000) {

            public void onTick(long millisUntilFinished) {
                txtViewMsgRecup.setText(String.format("%s %d %s", getString(R.string.wait), millisUntilFinished/1000, getString(R.string.seconds_sending_email)));
                btnRecupConta.setEnabled(false);
            }

            public void onFinish() {

                try {
                    btnRecupConta.setClickable(true);
                    btnRecupConta.setEnabled(true);
                    txtViewMsgRecup.setText(" ");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
            }
        }.start();
    }

    private void exibirFotoDaConta(String url, boolean statusEpilepsia) {
        GlideCustomizado.loadUrl(requireContext(),
                url, imgViewFotoRecup, android.R.color.transparent,
                GlideCustomizado.CIRCLE_CROP, false, statusEpilepsia);
    }

    private void atualizarContador(DatabaseReference reference, int contador, long timeStampAnterior) {
        atualizarContador.acrescentarContadorPorValor(reference, contador, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                if (timeStampAnterior != -1) {
                    HashMap<String, Object> dados = new HashMap<>();
                    dados.put("userId", emailCriptografado);
                    dados.put("counter", contadorAtualizado);
                    dados.put("timeStampValidity", timeStampAnterior);
                    salvarDados(dados);
                } else {
                    recuperarTimestamp(new RecuperarTimeStampCallback() {
                        @Override
                        public void onRecuperado(long timeStamp) {
                            long timestampValidade = validade24Hours(timeStamp);
                            HashMap<String, Object> dados = new HashMap<>();
                            dados.put("userId", emailCriptografado);
                            dados.put("counter", contadorAtualizado);
                            dados.put("timeStampValidity", timestampValidade);
                            salvarDados(dados);
                        }

                        @Override
                        public void onError(String message) {
                            ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                            txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.an_error_has_occurred), message));
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.an_error_has_occurred), errorMessage));
            }
        });
    }

    private void recuperarConta() {
        FirebaseAuth.getInstance().sendPasswordResetEmail(emailConvertido)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                            txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.email_sent), emailConvertido));
                            exibirContador();
                        } else {
                            ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                            txtViewMsgRecup.setText(getString(R.string.error_sending_reset_link));
                        }
                    }
                });
    }

    private void recuperarTimestamp(RecuperarTimeStampCallback callback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(requireContext(), new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onRecuperado(timestamps);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.connection_error_occurred), errorMessage), requireContext());
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }

    private long validade24Hours(long timestampAlvo) {
        //Usado negativo por causa que se trata de um timestamp negativo
        return timestampAlvo + (24 * 60 * 60 * 1000);
    }

    private void verificaValidade(long timestampValidade, VerificaValidadeCallback callback) {
        recuperarTimestamp(new RecuperarTimeStampCallback() {
            @Override
            public void onRecuperado(long timeStamp) {
                if (timeStamp >= timestampValidade) {
                    callback.onResetar();
                } else {
                    callback.onNaoResetar();
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void salvarDados(HashMap<String, Object> dados) {
        DatabaseReference recoveryCounterRef = firebaseRef.child("emailRecoveryCounter")
                .child(emailCriptografado);
        recoveryCounterRef.setValue(dados).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.an_error_has_occurred), e.getMessage()));
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                recuperarConta();
            }
        });
    }

    private void clickListeners() {
        btnRecupConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperarDado = editTextEmail.getText().toString().trim();
                if (!recuperarDado.isEmpty()) {
                    emailConvertido = recuperarDado.toLowerCase(Locale.ROOT);
                    ProgressBarUtils.exibirProgressBar(progressBarRecup, requireActivity());
                    emailCriptografado = Base64Custom.codificarBase64(emailConvertido);
                    procurarConta();
                }
            }
        });
    }

    private void inicializandoComponentes(View view) {
        editTextEmail = view.findViewById(R.id.edtTxtEmailRecup);
        btnRecupConta = view.findViewById(R.id.btnRecupContaPorEmail);
        imgViewFotoRecup = view.findViewById(R.id.imgViewFotoRecupPorEmail);
        txtViewMsgRecup = view.findViewById(R.id.txtViewMsgRecupPorEmail);
        progressBarRecup = view.findViewById(R.id.progressBarRecupPorEmail);
    }
}