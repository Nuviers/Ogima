package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.model.Usuario;

public class GeneroActivity extends AppCompatActivity implements View.OnClickListener {


    //private Button btnContinuarGenero;
    private Button buttonHomem;
    private Button buttonMulher;
    private Button buttonOutros;
    private String euSou;


    //
    private Usuario usuario;
    private String generoRecebido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_genero);

        buttonHomem = findViewById(R.id.buttonHomem);
        buttonMulher = findViewById(R.id.buttonMulher);
        buttonOutros = findViewById(R.id.buttonOutros);


        buttonHomem.setOnClickListener(this);
        buttonMulher.setOnClickListener(this);
        buttonOutros.setOnClickListener(this);

        Bundle dados = getIntent().getExtras();
        usuario = (Usuario) dados.getSerializable("dadosUsuario");

        if(dados != null){
            generoRecebido = dados.getString("alterarGenero");
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
            Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
            intent.putExtra("generoEnviado", euSou);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

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

    public void voltarGenero(View view) {
        onBackPressed();
    }

}








