package com.example.ogima.fragment;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.InfoUserDAO;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ProgressBarUtils;
import com.example.ogima.helper.TextUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Informacoes;
import com.example.ogima.model.RecoveryCounter;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.NumeroActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class RecupSmsFragment extends Fragment {

    private EditText edtTxtPhone, edtTxtDDIR;
    private Button btnRecupConta;
    private ImageView imgViewFoto;
    private String numeroRecuperacao, ddiRecuperacao, numeroCompleto, fotoUsuario, mensagem;
    private TextView txtViewMsgRecup;
    private ProgressBar progressBarRecup;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Usuario usuarioLocalizado;
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
        View view = inflater.inflate(R.layout.fragment_recup_sms, container, false);
        inicializandoComponentes(view);
        atualizarContador = new AtualizarContador();
        clickListeners();
        return view;
    }

    private void procurarContaPorNumero() {
        DatabaseReference usuarioRef = firebaseRef.child("usuarios");
        //Verificando se existe no banco de dados o número inserido
        usuarioRef.orderByChild("numero").equalTo(numeroCompleto).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                    txtViewMsgRecup.setText(getString(R.string.localized_account));
                    //Recuperar foto do usuario com outra referencia aqui
                    for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                        usuarioLocalizado = childDataSnapshot.getValue(Usuario.class);
                        boolean epilepsia = true;
                        if (usuarioLocalizado.getEpilepsia().equals("Sim")) {
                            epilepsia = true;
                        } else if (usuarioLocalizado.getEpilepsia().equals("Não")) {
                            epilepsia = false;
                        }
                        recuperarFoto(usuarioLocalizado.getMinhaFoto(), epilepsia);
                    }
                    //Trabalha em função de limitar alterações disparadamente.
                    verificaLimite(usuarioLocalizado.getIdUsuario());
                } else {
                    ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                    txtViewMsgRecup.setText(R.string.phone_number_not_found);
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.an_error_has_occurred), error.getMessage()), getActivity());
            }
        });
    }

    private void recuperarFoto(String url, boolean statusEpilepsia) {
        GlideCustomizado.loadUrl(requireContext(), url,
                imgViewFoto, android.R.color.transparent, GlideCustomizado.CIRCLE_CROP,
                false, statusEpilepsia);
    }

    private void verificaLimite(String idUserAlvo) {
        //Verifica se já existe alguma tentativa de recuperação nessas últimas 24 horas.
        DatabaseReference recoveryCounterRef = firebaseRef.child("smsRecoveryCounter")
                .child(idUserAlvo);

        DatabaseReference counterRef = firebaseRef.child("smsRecoveryCounter")
                .child(idUserAlvo).child("counter");

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
                                atualizarContador(idUserAlvo, counterRef, 0, -1);
                            }

                            @Override
                            public void onNaoResetar() {
                                if (contador < 10) {
                                    atualizarContador(idUserAlvo, counterRef, contador, recoveryCounter.getTimeStampValidity());
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

                                            ToastCustomizado.toastCustomizado(String.format("%s %s %s %s", getString(R.string.limit_of_attempts_reached), horasRestantes,":", minutosRestantes), requireContext());
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
                    atualizarContador(idUserAlvo, counterRef, 0, -1);
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

    private void atualizarContador(String idUserAlvo, DatabaseReference reference, int contador, long timeStampAnterior) {
        atualizarContador.acrescentarContadorPorValor(reference, contador, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                if (timeStampAnterior != -1) {
                    HashMap<String, Object> dados = new HashMap<>();
                    dados.put("userId", idUserAlvo);
                    dados.put("counter", contadorAtualizado);
                    dados.put("timeStampValidity", timeStampAnterior);
                    salvarDados(idUserAlvo, dados);
                } else {
                    recuperarTimestamp(new RecuperarTimeStampCallback() {
                        @Override
                        public void onRecuperado(long timeStamp) {
                            long timestampValidade = validade24Hours(timeStamp);
                            HashMap<String, Object> dados = new HashMap<>();
                            dados.put("userId", idUserAlvo);
                            dados.put("counter", contadorAtualizado);
                            dados.put("timeStampValidity", timestampValidade);
                            salvarDados(idUserAlvo, dados);
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

    private void salvarDados(String idUserAlvo, HashMap<String, Object> dados) {
        DatabaseReference recoveryCounterRef = firebaseRef.child("smsRecoveryCounter")
                .child(idUserAlvo);
        recoveryCounterRef.setValue(dados).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ProgressBarUtils.ocultarProgressBar(progressBarRecup, requireActivity());
                txtViewMsgRecup.setText(String.format("%s %s", getString(R.string.an_error_has_occurred), e.getMessage()));
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                irParaAlterarSenha();
            }
        });
    }

    private void irParaAlterarSenha(){
        Intent intent = new Intent(getActivity(), NumeroActivity.class);
        intent.putExtra("alterarSenha", "newPass");
        intent.putExtra("numeroEnviado", numeroRecuperacao);
        intent.putExtra("ddiEnviado", ddiRecuperacao);
        startActivity(intent);
        requireActivity().finish();
    }

    private void clickListeners() {
        btnRecupConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numeroRecuperacao = edtTxtPhone.getText().toString().trim();
                ddiRecuperacao = edtTxtDDIR.getText().toString().trim();
                if (TextUtils.isStringNotEmpty(numeroRecuperacao) && TextUtils.isStringNotEmpty(ddiRecuperacao)) {
                    numeroCompleto = ddiRecuperacao + numeroRecuperacao;
                    ProgressBarUtils.exibirProgressBar(progressBarRecup, requireActivity());
                    procurarContaPorNumero();
                } else {
                    txtViewMsgRecup.setText(getString(R.string.invalid_phone_number_format));
                }
            }
        });
    }

    private void inicializandoComponentes(View view) {
        edtTxtPhone = view.findViewById(R.id.edtTxtPhone);
        edtTxtDDIR = view.findViewById(R.id.edtTxtDDIR);
        btnRecupConta = view.findViewById(R.id.btnRecupContaPorSMS);
        imgViewFoto = view.findViewById(R.id.imgViewFotoRecupPorSMS);
        txtViewMsgRecup = view.findViewById(R.id.txtViewMsgRecupPorSMS);
        progressBarRecup = view.findViewById(R.id.progressBarRecupPorSMS);
    }
}

