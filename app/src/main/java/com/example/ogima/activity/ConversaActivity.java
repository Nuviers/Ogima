package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbarConversa;
    private ImageButton imgBtnBackConversa;
    private ImageView imgViewFotoDestinario, imgViewGifDestinario;
    private TextView txtViewNomeDestinario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);
        inicializandoComponentes();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        GlideCustomizado.montarGlide(getApplicationContext(), "https://firebasestorage.googleapis.com/v0/b/ogima-7.appspot.com/o/imagens%2Fperfil%2FcmFmYXNzYmVuZWRldDIwMDlAZ21haWwuY29t%2FfotoPerfil.jpeg?alt=media&token=c377e74d-aad8-49af-acbe-5f6cf8e316e7",
                imgViewFotoDestinario, android.R.color.transparent);

        GlideCustomizado.montarGlideFoto(getApplicationContext(), "https://media.giphy.com/media/9dtArMyxofHqXhziUk/giphy.gif",
                imgViewGifDestinario, android.R.color.transparent);

    }

    private void inicializandoComponentes() {
        toolbarConversa = findViewById(R.id.toolbarConversa);
        imgBtnBackConversa = findViewById(R.id.imgBtnBackConversa);
        imgViewFotoDestinario = findViewById(R.id.imgViewFotoDestinario);
        imgViewGifDestinario = findViewById(R.id.imgViewGifDestinario);
        txtViewNomeDestinario = findViewById(R.id.txtViewNomeDestinario);
    }
}