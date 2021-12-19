package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.fragment.AmigosFragment;
import com.example.ogima.fragment.InicioFragment;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class GeneroActivity extends AppCompatActivity implements View.OnClickListener {


    //private Button btnContinuarGenero;
    private Button buttonHomem;
    private Button buttonMulher;
    private Button buttonOutros;
    private String euSou;


    //
    private Usuario usuario;
    private String generoRecebido;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FloatingActionButton floatingVoltarGenero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_genero);

        buttonHomem = findViewById(R.id.buttonHomem);
        buttonMulher = findViewById(R.id.buttonMulher);
        buttonOutros = findViewById(R.id.buttonOutros);
        floatingVoltarGenero = findViewById(R.id.floatingVoltarGenero);


        buttonHomem.setOnClickListener(this);
        buttonMulher.setOnClickListener(this);
        buttonOutros.setOnClickListener(this);

        Bundle dados = getIntent().getExtras();
        usuario = (Usuario) dados.getSerializable("dadosUsuario");

        if(dados != null){
            generoRecebido = dados.getString("alterarGenero");
        }

        if (generoRecebido != null) {
            try{
                floatingVoltarGenero.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                    }
                });
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            floatingVoltarGenero.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonHomem: {
                    euSou = "Homem";

                break;
            }
            case R.id.buttonMulher: {
                euSou = "Mulher";

                break;
            }
            case R.id.buttonOutros: {
                euSou = "Outros";

                break;
            }
        }
        if(euSou == "Homem" || euSou =="Mulher" || euSou == "Outros"){
            receberDados();
        }

    }

   // @Override
    //public void onPointerCaptureChanged(boolean hasCapture) {

   // }


    public  void receberDados(){

        //Bundle dados = getIntent().getExtras();
        //Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");

        if (generoRecebido != null) {

            String emailUsuario = autenticacao.getCurrentUser().getEmail();
            String idUsuario = Base64Custom.codificarBase64(emailUsuario);
            DatabaseReference generoRef = firebaseRef.child("usuarios").child(idUsuario);
            generoRef.child("generoUsuario").setValue(euSou).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //autenticacao.getCurrentUser().reload();
                        Toast.makeText(getApplicationContext(), "Alterado com sucesso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro ao atualizar dado, tente novamente!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                    }
                }
            });
            //testeRef.setValue(euSou);
            //autenticacao.getCurrentUser().reload();
            //Toast.makeText(getApplicationContext(), "Alterado com sucesso", Toast.LENGTH_SHORT).show();

            //Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //startActivity(intent);

        }else{
        Toast.makeText(GeneroActivity.this, "Email "
                + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario() + " Número " + usuario.getNumero()
                + " Nome " + usuario.getNomeUsuario() + " Apelido "
                + usuario.getApelidoUsuario() + " Idade " + usuario.getIdade()
                + " Nascimento " + usuario.getDataNascimento(), Toast.LENGTH_LONG).show();

        usuario.setGeneroUsuario(euSou);

        Intent intent = new Intent(getApplicationContext(), InteresseActivity.class);
        intent.putExtra("dadosUsuario", usuario);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        //finish();
        }
    }

}








