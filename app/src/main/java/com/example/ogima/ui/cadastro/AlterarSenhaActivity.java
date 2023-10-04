package com.example.ogima.ui.cadastro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.activity.LoginEmailActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DbHelper;
import com.example.ogima.helper.InfoUserDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Informacoes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class AlterarSenhaActivity extends AppCompatActivity {

    private Button btnAlterarSenha;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private EditText edtTxtNovaSenha, edtTxtConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha);
        inicializarComponentes();
        clickListeners();
    }

    private void alterarSenha(String novaSenha){
        if (autenticacao != null && autenticacao.getCurrentUser() != null) {
            autenticacao.getCurrentUser().updatePassword(novaSenha).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        UsuarioUtils.deslogarUsuario(getApplicationContext(), new UsuarioUtils.DeslogarUsuarioCallback() {
                            @Override
                            public void onDeslogado() {
                                ToastCustomizado.toastCustomizadoCurto(getString(R.string.successfully_changed), getApplicationContext());
                                Intent intent = new Intent(getApplicationContext(), LoginEmailActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.error_changing_password), task.getException()), getApplicationContext());
                    }
                }
            });
        }else{
            ToastCustomizado.toastCustomizado(getString(R.string.error_changing_password), getApplicationContext());
            UsuarioUtils.deslogarUsuario(getApplicationContext(), new UsuarioUtils.DeslogarUsuarioCallback() {
                @Override
                public void onDeslogado() {
                    finish();
                }
            });
        }
    }

    private void clickListeners(){
        btnAlterarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novaSenha = edtTxtNovaSenha.getText().toString().trim();
                String confirmarSenha = edtTxtConfirmar.getText().toString().trim();
                if (novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.enter_your_password_on_both),getApplicationContext());
                } else if (!novaSenha.equals(confirmarSenha)) {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.passwords_do_not_match), getApplicationContext());
                }else if(novaSenha.length() < 6 || confirmarSenha.length() < 6){
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.password_missing_digit), getApplicationContext());
                }else {
                    alterarSenha(novaSenha);
                }
            }
        });
    }

    private void inicializarComponentes() {
        edtTxtNovaSenha = findViewById(R.id.edtTxtNovaSenha);
        edtTxtConfirmar = findViewById(R.id.edtTxtConfirmarNovaSenha);
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha);
    }
}