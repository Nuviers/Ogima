package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Usuario;

import java.util.ArrayList;

public class ChatRandomActivity extends AppCompatActivity {

    private ImageView imgViewUser1, imgViewUser2;
    private String user1Id = "", user2Id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_random);
        inicializandoComponentes();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("user1Id")) {
                user1Id = dados.getString("user1Id");
                dadosUser(user1Id, imgViewUser1);
            }

            if (dados.containsKey("user2Id")) {
                user2Id = dados.getString("user2Id");
                dadosUser(user2Id, imgViewUser2);
            }
        }
    }

    private void dadosUser(String idAlvo, ImageView imgViewAlvo){
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idAlvo, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                GlideCustomizado.loadUrl(getApplicationContext(),
                        fotoUsuario, imgViewAlvo, android.R.color.transparent,
                        GlideCustomizado.CIRCLE_CROP, false, epilepsia);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void inicializandoComponentes() {
        imgViewUser1 = findViewById(R.id.imgViewUser1);
        imgViewUser2 = findViewById(R.id.imgViewUser2);
    }
}