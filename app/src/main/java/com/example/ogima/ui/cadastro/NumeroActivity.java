package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class NumeroActivity extends AppCompatActivity {


    private Button btnContinuarNumero;
    private EditText editNumero;
    private TextView txtMensagemNumero;

    //**

    // variable for FirebaseAuth class
    private FirebaseAuth mAuth;

    // variable for our text input
    // field for phone and OTP.
    private EditText edtPhone, edtOTP;

    // buttons for generating OTP and verifying OTP
    private Button verifyOTPBtn, generateOTPBtn;

    // string for storing our verification ID
    private String verificationId;

    //**

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_telefone);
       //* setContentView(R.layout.cad_numero);


        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNumero = findViewById(R.id.btnContinuarNumero);
        editNumero = findViewById(R.id.editNumero);
        txtMensagemNumero = findViewById(R.id.txtMensagemNumero);

         // a linha abaixo é para obter instância
        //do FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        // iniciando as variaveis do button e Edittext
        edtPhone = findViewById(R.id.idEdtPhoneNumber);
        edtOTP = findViewById(R.id.idEdtOtp);
        verifyOTPBtn = findViewById(R.id.idBtnVerify);
        generateOTPBtn = findViewById(R.id.idBtnGetOtp);

        // configurando o ouvinte onclick para gerar o botão OTP.
        generateOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // a linha abaixo é para verificar o tempo do usuário
                // inseriu seu número de celular ou não.
                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    // quando o campo de texto do número do celular está vazio
                    // exibindo uma mensagem de aviso.
                    Toast.makeText(NumeroActivity.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
                } else {
                    // se o campo de texto não estiver vazio, estamos chamando nosso
                    // enviar método OTP para obter OTP do Firebase.
                    //Depois colocar um campo com spinner que identifique o dd
                    //no lugar de colocar um valor solto
                    String phone = "+55" + edtPhone.getText().toString();
                    sendVerificationCode(phone);
                }
            }
        });

        // inicializando no ouvinte de clique
        // para verificar o botão OTP
        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validando se o campo de texto OTP está vazio ou não.
                if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                    // se o campo de texto OTP estiver vazio, exibir
                    // uma mensagem para o usuário entrar no OTP
                    Toast.makeText(NumeroActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    // se o campo OTP não estiver vazio, chamando
                    // método para verificar o OTP.
                    verifyCode(edtOTP.getText().toString());
                }
            }
        });
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        // dentro deste método estamos verificando se
        // o código inserido está correto ou não.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // se o código estiver correto e a tarefa for bem-sucedida
                            // então é enviado o usuário para uma nova atividade.
                            Intent i = new Intent(NumeroActivity.this, CodigoActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            // se o código não estiver correto, então será
                            // exibido uma mensagem de erro para o usuário.
                            Toast.makeText(NumeroActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void sendVerificationCode(String number) {
        // este método é usado para obter
        // OTP no número de telefone do usuário.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Número de telefone para verificar
                        .setTimeout(60L, TimeUnit.SECONDS) // Tempo limite e unidade
                        .setActivity(this)                 // Activity (para ligação de retorno de chamada)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    // método de retorno de chamada é chamado no provedor de autenticação do telefone.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // inicializando nossos callbacks para em
            // método de retorno de chamada de verificação.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // o método abaixo é usado quando
        // OTP é enviado do Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // quando recebemos o OTP ele
            // contém um id único que
            // estamos armazenando em nossa string
            // que já criamos.
            verificationId = s;
        }

        // este método é chamado quando o usuário
        // recebe OTP do Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // a linha abaixo é usada para obter o código OTP
            // que é enviado nas credenciais de autenticação do telefone.
            final String code = phoneAuthCredential.getSmsCode();

            // verificando se o código
            // é nulo ou não.
            if (code != null) {
                // se o código não for nulo, então
                // está definindo esse código para
                // o campo de texto de edição OTP.
                edtOTP.setText(code);

                // depois de definir este código
                // para o campo de texto de edição OTP então
                // é chamado o método verifycode.
                verifyCode(code);
            }
        }

        // este método é chamado quando o firebase não
        // envia o código OTP devido a qualquer erro ou problema.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // exibindo mensagem de erro com exceção do firebase.
            Toast.makeText(NumeroActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // o método abaixo é usado para verificar o código do Firebase.
    private void verifyCode(String code) {
        // a linha abaixo é usada para obter
        // credenciais de identificação e código de verificação.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // depois de obter a credencial, obtêm
        // método de login de chamada.
        signInWithCredential(credential);
    }



        public void voltarNumero (View view){
            onBackPressed();
        }



 }


