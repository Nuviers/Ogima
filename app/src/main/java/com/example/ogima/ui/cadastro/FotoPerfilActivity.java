package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

public class FotoPerfilActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnCadastrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_foto_perfil);

        btnCadastrar = findViewById(R.id.btnCadastrar);


        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle dados = getIntent().getExtras();

                Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");


                Toast.makeText(FotoPerfilActivity.this, "Email "
                        + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                        + " Nome " + usuario.getNomeUsuario() + " Apelido "
                        + usuario.getApelidoUsuario() + " Idade " + usuario.getIdade()
                        + " Nascimento " + usuario.getDataNascimento() + " Genêro " + usuario.getGeneroUsuario()
                        + " Interesses " + usuario.getInteresses(), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                startActivity(intent);


            }
        });


    }

    @Override
    public void onClick(View view) {

    }




    @Override
    public void onBackPressed() {
        // Método para bloquear o retorno.
    }
}
