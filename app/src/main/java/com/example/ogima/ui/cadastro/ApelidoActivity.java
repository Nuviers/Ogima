package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class ApelidoActivity extends AppCompatActivity {


    private Button btnContinuarApelido;
    private EditText editApelido;
    private TextView txtMensagemApelido;
    Usuario usuario;

    private GoogleSignInClient mSignInClient;
    private String apelidoRecebido;

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    private FloatingActionButton floatingVoltarApelido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_apelido);

        btnContinuarApelido = findViewById(R.id.btnContinuarApelido);
        editApelido = findViewById(R.id.editApelido);
        txtMensagemApelido = findViewById(R.id.txtMensagemApelido);
        floatingVoltarApelido = findViewById(R.id.floatingVoltarApelido);

        //Recebendo Email/Senha/Nome
        Bundle dados = getIntent().getExtras();
        if(dados != null){
            apelidoRecebido = dados.getString("alterarApelido");
            usuario = (Usuario) dados.getSerializable("dadosUsuario");
        }

        if (apelidoRecebido != null) {
            try {
                floatingVoltarApelido.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                         */
                        onBackPressed();
                    }
                });
                editApelido.setText(apelidoRecebido);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            floatingVoltarApelido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        //Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");

        btnContinuarApelido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(apelidoRecebido != null){
                    alterarApelido();
                }else {
                    String textoApelido = editApelido.getText().toString();

                    if (!textoApelido.isEmpty()) {

                        if (textoApelido.length() > 30) {
                            txtMensagemApelido.setText("Limite de caracteres excedido, limite máximo são 30 caracteres");
                        } else {
                            //Enviando apelido
                            usuario.setApelidoUsuario(textoApelido);

                            Intent intent = new Intent(ApelidoActivity.this, IdadePessoas.class);
                            intent.putExtra("dadosUsuario", usuario);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            //finish();
                        }

                    } else {
                        txtMensagemApelido.setText("Digite seu apelido");
                    }
                }
            }
        });

    }

    public void alterarApelido(){
        String textoApelido = editApelido.getText().toString();

        if(!textoApelido.isEmpty()){

            if(textoApelido.length() > 30){
                txtMensagemApelido.setText("Limite de caracteres excedido, limite máximo são 30 caracteres");
            }else {
                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                DatabaseReference nomeRef = firebaseRef.child("usuarios").child(idUsuario);
                nomeRef.child("apelidoUsuario").setValue(textoApelido).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Alterado com sucesso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            finish();
                        }else{
                            Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            finish();
                            Toast.makeText(getApplicationContext(), "Ocorreu um erro ao atualizar dado, tente novamente!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }else{
            txtMensagemApelido.setText("Digite seu apelido");
        }
    }

    /*
    public void voltarApelido(View view){
        onBackPressed();
    }
     */

}


