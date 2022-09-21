package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbarConversa;
    private ImageButton imgBtnBackConversa;
    private Button btnTotalMensagensDestinatario;
    private ImageView imgViewFotoDestinatario, imgViewGifDestinatario;
    private TextView txtViewNomeDestinatario, txtViewNivelAmizadeDestinatario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Usuario usuarioDestinatario;
    private Contatos contatoDestinatario;
    private Chip chipInteresse01, chipInteresse02, chipInteresse03, chipInteresse04, chipInteresse05;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);
        inicializandoComponentes();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            contatoDestinatario = (Contatos) dados.getSerializable("contato");
            usuarioDestinatario = (Usuario) dados.getSerializable("usuario");

            if (usuarioDestinatario != null) {
                if (usuarioDestinatario.getEpilepsia().equals("Sim")) {
                    GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                            imgViewFotoDestinatario, android.R.color.transparent);
                } else {
                    GlideCustomizado.montarGlide(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                            imgViewFotoDestinatario, android.R.color.transparent);
                }

                if (usuarioDestinatario.getExibirApelido().equals("sim")) {
                    txtViewNomeDestinatario.setText(usuarioDestinatario.getApelidoUsuario());
                } else {
                    txtViewNomeDestinatario.setText(usuarioDestinatario.getNomeUsuario());
                }

                preencherChipsInteresses();

                txtViewNivelAmizadeDestinatario.setText("Nível de amizade: " + contatoDestinatario.getNivelAmizade());
            }
        }

        GlideCustomizado.montarGlideFoto(getApplicationContext(), "https://media.giphy.com/media/9dtArMyxofHqXhziUk/giphy.gif",
                imgViewGifDestinatario, android.R.color.transparent);

        btnTotalMensagensDestinatario.setText(contatoDestinatario.getTotalMensagens() + " Mensagens");

    }

    private void preencherChipsInteresses() {
        if (usuarioDestinatario.getInteresses().size() == 1) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
        } else if (usuarioDestinatario.getInteresses().size() == 2) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
        } else if (usuarioDestinatario.getInteresses().size() == 3) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
        } else if (usuarioDestinatario.getInteresses().size() == 4) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
            chipInteresse04.setText(usuarioDestinatario.getInteresses().get(3));
        } else if (usuarioDestinatario.getInteresses().size() == 5) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
            chipInteresse04.setText(usuarioDestinatario.getInteresses().get(3));
            chipInteresse05.setText(usuarioDestinatario.getInteresses().get(4));
        }

        imgBtnBackConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        toolbarConversa = findViewById(R.id.toolbarConversa);
        imgBtnBackConversa = findViewById(R.id.imgBtnBackConversa);
        btnTotalMensagensDestinatario = findViewById(R.id.btnTotalMensagensDestinatario);
        imgViewFotoDestinatario = findViewById(R.id.imgViewFotoDestinatario);
        imgViewGifDestinatario = findViewById(R.id.imgViewGifDestinatario);
        txtViewNomeDestinatario = findViewById(R.id.txtViewNomeDestinatario);
        txtViewNivelAmizadeDestinatario = findViewById(R.id.txtViewNivelAmizadeDestinatario);
        chipInteresse01 = findViewById(R.id.chipInteresse01);
        chipInteresse02 = findViewById(R.id.chipInteresse02);
        chipInteresse03 = findViewById(R.id.chipInteresse03);
        chipInteresse04 = findViewById(R.id.chipInteresse04);
        chipInteresse05 = findViewById(R.id.chipInteresse05);
    }
}