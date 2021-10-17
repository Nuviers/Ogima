package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.ui.cadastro.ViewCadastroActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

public class LoginEmailActivity extends AppCompatActivity {

    private Button buttonProblemConta;
    private EditText editTextEmail;
    private EditText editTextSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        buttonProblemConta = findViewById(R.id.buttonProblemConta);
        editTextEmail = findViewById(R.id.campoEmail);
        editTextSenha = findViewById(R.id.campoSenha);



    }

        public void telaInicial(View view){

            String editEmail = editTextEmail.getText().toString();
            String editSenha = editTextSenha.getText().toString();

        if(!editEmail.isEmpty() && !editSenha.isEmpty()){
            //startActivity(new Intent(LoginEmailActivity.this, FotosActivity.class));
            startActivity(new Intent(LoginEmailActivity.this, NavigationDrawerActivity.class));
        }else{
            Toast.makeText(getApplicationContext(), "Digite seu email e senha!",Toast.LENGTH_LONG).show();
        }

        }

        public void telaCadastro(View view){
            startActivity(new Intent(LoginEmailActivity.this, ViewCadastroActivity.class));
        }
}
