package com.example.ogima.ui.cadastro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;

import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.activity.LoginEmailActivity;
import com.example.ogima.activity.LoginUiActivity;
import com.example.ogima.fragment.RecupSmsFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.intro.IntrodActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.security.Provider;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NumeroActivity extends AppCompatActivity {


    private Button btnContinuarNumero;
    private EditText editNumero;
    private TextView txtMensagem, textViewTituloN;
    private int METODO_ALTERAR_ACTIVITY;
    private GoogleSignInClient mSignInClient;
    private String usuarioLocalizado;


    //**

    // variavel da classe FirebaseAuth
    private FirebaseAuth mAuth;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    // variável para nossa entrada de texto
    // campo para telefone e OTP.
    private EditText edtPhone, edtOTP, editTextDDI;

    // buttons para gerar o código via OTP e verificar o código
    private Button verifyOTPBtn, generateOTPBtn;

    // string para armazenar o ID de verificação
    private String verificationId, phone;

    //private Usuario usuario;

    Usuario usuario = new Usuario();

    private String testeSenha;
    private String vincularNumero;
    private String desvincularNumero;
    private String numeroRecebido;
    private String ddiRecebido;

    CountDownTimer teste = null;
    private String contadorEnvio;
    private String contadorInicio;
    private ProgressBar progressBarN;
    //**

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_numero);

        Bundle dados = getIntent().getExtras();

        if(dados != null){
            testeSenha = dados.getString("alterarSenha");
            vincularNumero = dados.getString("vincularNumero");
            desvincularNumero = dados.getString("desvincularNumero");
            ddiRecebido = dados.getString("ddiEnviado");
            numeroRecebido = dados.getString("numeroEnviado");
        }

        //progressBarN.setVisibility(View.GONE);


        //* setContentView(R.layout.cad_numero);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtMensagem = findViewById(R.id.txtMensagem);
        editTextDDI = findViewById(R.id.editTextDDI);

        // a linha abaixo é para obter instância
        //do FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        // iniciando as variaveis do button e Edittext
        edtPhone = findViewById(R.id.idEdtPhoneNumber);
        edtOTP = findViewById(R.id.idEdtOtp);
        verifyOTPBtn = findViewById(R.id.idBtnVerify);
        generateOTPBtn = findViewById(R.id.idBtnGetOtp);
        progressBarN = findViewById(R.id.progressBarN);
        textViewTituloN = findViewById(R.id.textViewTituloN);

        if (ddiRecebido != null && numeroRecebido != null) {

            try {
                textViewTituloN.setTextSize(14);
                textViewTituloN.setText("   Clique para enviar o código por sms para prosseguir com   a alteração.");
                textViewTituloN.setPadding(16, 8, 8, 8);
                editTextDDI.setText(ddiRecebido);
                edtPhone.setText(numeroRecebido);
                editTextDDI.setEnabled(false);
                edtPhone.setEnabled(false);
                editTextDDI.setTextColor(Color.BLACK);
                edtPhone.setTextColor(Color.BLACK);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // configurando o ouvinte onclick para gerar o botão OTP.
        generateOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // a linha abaixo é para verificar o tempo do usuário
                // inseriu seu número de celular ou não.
                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    // quando o campo de texto do número do celular está vazio
                    // exibindo uma mensagem de aviso.
                    Toast.makeText(NumeroActivity.this, "Por favor insira um número de telefone válido.", Toast.LENGTH_SHORT).show();
                } else {
                    progressBarN.setVisibility(View.VISIBLE);
                    //exibirContador();
                    // se o campo de texto não estiver vazio, estamos chamando nosso
                    // enviar método OTP para obter OTP do Firebase.
                    String DDI = editTextDDI.getText().toString();
                    //String phone = "+55" + edtPhone.getText().toString();
                    phone = DDI + edtPhone.getText().toString();
                    verificarNumero();

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
                    Toast.makeText(NumeroActivity.this, "Por favor insira o código recebido pelo SMS", Toast.LENGTH_SHORT).show();
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

                            if (testeSenha != null) {

                                Toast.makeText(getApplicationContext(), "Logado e recebido", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getApplicationContext(), AlterarSenhaActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {

                                Intent i = new Intent(NumeroActivity.this, NavigationDrawerActivity.class);
                                i.putExtra("dadosUsuario", usuario);
                                startActivity(i);
                                finish();

                            }


                        } else {
                            // se o código não estiver correto, então será
                            // exibido uma mensagem de erro para o usuário.
                            Toast.makeText(NumeroActivity.this, "Código inválido", Toast.LENGTH_LONG).show();
                            //Toast.makeText(NumeroActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }


    //Método modificado para vincular telefone
    private void vincularNumero(PhoneAuthCredential credential) {
        // dentro deste método estamos verificando se
        // o código inserido está correto ou não.

        AuthCredential authCredential = PhoneAuthProvider.getCredential(verificationId, edtOTP.getText().toString());

        autenticacao.getCurrentUser().linkWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(getApplicationContext(), "Vinculado com sucesso o numero de telefone", Toast.LENGTH_SHORT).show();

                    //Salvando número de telefone no banco de dados
                    String emailUsuario = autenticacao.getCurrentUser().getEmail();
                    String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                    String numero = phone;
                    DatabaseReference numeroRef = firebaseRef.child("usuarios").child(idUsuario).child("numero");
                    numeroRef.setValue(numero);

                    Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(getApplicationContext(), "Insira o código corretamente", Toast.LENGTH_SHORT).show();
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
            progressBarN.setVisibility(View.GONE);
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
            String mensagemErro = e.getMessage();
            if (mensagemErro != null) {
                if(mensagemErro.contains("We have blocked")){
                    Toast.makeText(NumeroActivity.this, "Limite de envios de sms para esse número de telefone atingido, tente novamente mais tarde!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(NumeroActivity.this, "Erro ao enviar o código, verifique o número inserido. Se o erro persistir tente novamente mais tarde!", Toast.LENGTH_LONG).show();
                }
            }
            try {
                progressBarN.setVisibility(View.GONE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //Toast.makeText(NumeroActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // o método abaixo é usado para verificar o código do Firebase.
    private void verifyCode(String code) {
        // a linha abaixo é usada para obter
        // credenciais de identificação e código de verificação.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // depois de obter a credencial, obtêm
        // método de login de chamada.

        if (testeSenha != null) {

            Toast.makeText(getApplicationContext(), "Dado recebido Pass", Toast.LENGTH_SHORT).show();
            try {
                progressBarN.setVisibility(View.GONE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            signInWithCredential(credential);
        }

        if (vincularNumero != null) {
            Toast.makeText(getApplicationContext(), "Dado recebido Vincular", Toast.LENGTH_SHORT).show();
            try {
                progressBarN.setVisibility(View.GONE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            vincularNumero(credential);
        }
    }

    public void exibirContador() {

        try {
            generateOTPBtn.setClickable(false);
            generateOTPBtn.setText("Aguarde para enviar outro sms");
        } catch (Exception e) {
            e.printStackTrace();
        }

        teste = new CountDownTimer(50000, 1000) {

            public void onTick(long millisUntilFinished) {
                txtMensagem.setText("Espere " + millisUntilFinished / 1000 + " segundos para enviar outro sms");
                generateOTPBtn.setEnabled(false);
            }

            public void onFinish() {

                try {
                    generateOTPBtn.setClickable(true);
                    generateOTPBtn.setEnabled(true);
                    generateOTPBtn.setText("Enviar código por SMS");
                    txtMensagem.setText(" ");
                    progressBarN.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                contadorEnvio = "Okay";

                contadorInicio = "concluiu";

                if (teste != null) {
                    teste.cancel();
                }

                /*
                if(textViewMensagem.equals("Okay")){

                    buttonContinuarEmail.setClickable(true);
                    //btnContinuarCodigo.setClickable(true);
                }
                 */

            }
        }.start();
    }


    public void voltarNumero(View view) {
        onBackPressed();
    }

    private void verificarNumero(){

        DatabaseReference usuarioRef = firebaseRef.child("usuarios");

        //Verificando se existe no banco de dados o número inserido
        usuarioRef.orderByChild("numero").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Procura valores fornecidos pel orderbychild
                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    usuarioLocalizado = snapshot.getChildren().iterator().next().getKey();
                }

                if(snapshot.exists() && testeSenha == null){
                    Toast.makeText(getApplicationContext(), "Esse número já foi vinculado a outra conta, por favor insira outro número de telefone!", Toast.LENGTH_LONG).show();
                    try{
                        progressBarN.setVisibility(View.INVISIBLE);
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(edtPhone.getWindowToken(), 0);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }else{
                    exibirContador();
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(edtPhone.getWindowToken(), 0);
                    sendVerificationCode(phone);
                    usuario.setNumero(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Erro " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

}