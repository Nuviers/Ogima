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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
                    // if OTP field is not empty calling
                    // method to verify the OTP.
                    verifyCode(edtOTP.getText().toString());
                }
            }
        });
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            Intent i = new Intent(NumeroActivity.this, NomeActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Toast.makeText(NumeroActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


        btnContinuarNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoNumero = editNumero.getText().toString();

                if(!textoNumero.isEmpty()){
                    Intent intent = new Intent(getApplicationContext(), CodigoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }else{
                    txtMensagemNumero.setText("Digite seu número de telefone");
                }
            }
        });

    }



        public void voltarNumero (View view){
            onBackPressed();
        }





        }


