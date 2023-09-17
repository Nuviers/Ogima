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

    private EditText editTextNovaSenha;
    private Button buttonAlterarSenha;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private String novaSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha);

        //Inicializando componentes
        inicializarComponentes();

        FirebaseUser usuarioLogado = autenticacao.getCurrentUser();

        buttonAlterarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                novaSenha = editTextNovaSenha.getText().toString();

                if (!novaSenha.isEmpty() && usuarioLogado != null) {

                    usuarioLogado.updatePassword(novaSenha).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                ToastCustomizado.toastCustomizado("Alterado com sucesso", getApplicationContext());
                                //Ver se realmente é necessário

                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                                        .requestEmail()
                                        .build();

                                GoogleSignInClient mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                                FirebaseAuth.getInstance().signOut();
                                mSignInClient.signOut();

                                Intent intent = new Intent(getApplicationContext(), LoginEmailActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                ToastCustomizado.toastCustomizado("Erro ao atualizar senha " + task.getException(), getApplicationContext());
                            }
                        }
                    });

                } else {
                    ToastCustomizado.toastCustomizado("Insira sua nova senha para prosseguir", getApplicationContext());
                }

            }
        });

    }

    public void inicializarComponentes() {
        editTextNovaSenha = findViewById(R.id.editTextNovaSenha);
        buttonAlterarSenha = findViewById(R.id.buttonAlterarSenha);
    }

}