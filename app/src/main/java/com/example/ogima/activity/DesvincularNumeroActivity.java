package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DesvincularNumeroActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Button buttonDesvincularD;
    private String numeroDigitado;
    private String numeroUsuario;
    private EditText editTextNumeroD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desvincular_numero);

        buttonDesvincularD = findViewById(R.id.buttonDesvincularD);
        editTextNumeroD = findViewById(R.id.editTextNumeroD);

        buttonDesvincularD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numeroDigitado = editTextNumeroD.getText().toString();

                if(numeroDigitado != null){
                    verificarNumero();
                }

            }
        });

    }

    private void verificarNumero() {

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    numeroUsuario = usuario.getNumero();

                    if (emailUsuario != null) {

                        if(numeroUsuario.equals(numeroDigitado)){
                            Toast.makeText(getApplicationContext(), "Desvinculando número de telefone...", Toast.LENGTH_SHORT).show();

                            autenticacao.getCurrentUser().unlink("phone").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        autenticacao.getCurrentUser().reload();
                                        String emailUsuario = autenticacao.getCurrentUser().getEmail();
                                        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                                        DatabaseReference numeroRef = firebaseRef.child("usuarios").child(idUsuario).child("numero");
                                        numeroRef.setValue("desvinculado");
                                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        Toast.makeText(getApplicationContext(), "Desvinculado com sucesso", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Erro ao desvincular", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }else{
                            Toast.makeText(getApplicationContext(), "Número de telefone incorreto, informe seu número de telefone vinculado à sua conta", Toast.LENGTH_SHORT).show();
                        }

                        try {
                            usuarioRef.removeEventListener(this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                } else if (snapshot == null) {
                    Toast.makeText(getApplicationContext(), " Nenhum dado localizado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();

            }
        });
    }
}