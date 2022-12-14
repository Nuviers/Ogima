package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Comunidade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ComunidadeActivity extends AppCompatActivity {

    private Toolbar toolbarComunidade;
    private ImageButton imgButtonBackComunidade;
    private ImageView imgViewPerfilComunidade;
    private TextView txtViewNomeComunidade, txtViewDescricaoComunidade;
    private Button btnEntrarComunidade, btnMembrosComunidade;
    private RecyclerView recyclerViewTopicosComunidade, recyclerViewPostagensComunidade;
    private List<Comunidade> listaTopicosComunidade = new ArrayList<>();
    private List<Comunidade> listaPostagensComunidade = new ArrayList<>();
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();


    @Override
    protected void onStart() {
        super.onStart();

        recuperarTopicosComunidade();
        recuperarPostagensComunidade();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Falta os valueevent, aqui eu removo eles.
        listaTopicosComunidade.clear();
        listaPostagensComunidade.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunidade);
        setSupportActionBar(toolbarComunidade);
        inicializandoComponentes();

        toolbarComunidade.setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

    }

    private void recuperarTopicosComunidade() {

    }

    private void recuperarPostagensComunidade() {

    }

    private void inicializandoComponentes() {
        toolbarComunidade = findViewById(R.id.toolbarComunidade);
        imgButtonBackComunidade = findViewById(R.id.imgButtonBackComunidade);
        imgViewPerfilComunidade = findViewById(R.id.imgViewPerfilComunidade);
        txtViewNomeComunidade = findViewById(R.id.txtViewNomeComunidade);
        txtViewDescricaoComunidade = findViewById(R.id.txtViewDescricaoComunidade);
        btnEntrarComunidade = findViewById(R.id.btnEntrarComunidade);
        btnMembrosComunidade = findViewById(R.id.btnMembrosComunidade);
        recyclerViewTopicosComunidade = findViewById(R.id.recyclerViewTopicosComunidade);
        recyclerViewPostagensComunidade = findViewById(R.id.recyclerViewPostagensComunidade);
    }
}