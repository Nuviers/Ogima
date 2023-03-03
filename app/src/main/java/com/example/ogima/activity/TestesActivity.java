package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.ogima.R;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;

import java.util.List;

public class TestesActivity extends AppCompatActivity {

    private List<Mensagem> usuariosSelecionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testes);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            usuariosSelecionados = (List<Mensagem>) dados.getSerializable("listaUsuariosSelecionados");


            for(int i = 0; i < usuariosSelecionados.size(); i ++){
                ToastCustomizado.toastCustomizadoCurto("Ids " + usuariosSelecionados.get(i).getConteudoMensagem(), getApplicationContext());
            }
        }
    }
}