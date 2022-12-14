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
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ListaComunidadesActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarListaComunidade;
    private ImageButton imgButtonBackListaComunidade;
    private ImageView imgViewPerfilMinhaComunidade;
    private TextView txtViewMInhaComunidade;
    private Button btnVisitarMinhaComunidade;
    private List<Comunidade> listaComunidades = new ArrayList<>();
    private RecyclerView recyclerViewComunidades;

    @Override
    protected void onStart() {
        super.onStart();

        recuperarMinhaComunidade();
        recuperarComunidades();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Falta remover o valueEvent
        listaComunidades.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_comunidades);
        setSupportActionBar(toolbarListaComunidade);
        inicializarComponentes();
        toolbarListaComunidade.setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    private void recuperarMinhaComunidade() {

    }

    private void recuperarComunidades() {

    }

    private void inicializarComponentes() {
        toolbarListaComunidade = findViewById(R.id.toolbarListaComunidade);
        imgButtonBackListaComunidade = findViewById(R.id.imgButtonBackListaComunidade);
        imgViewPerfilMinhaComunidade = findViewById(R.id.imgViewPerfilMinhaComunidade);
        txtViewMInhaComunidade = findViewById(R.id.txtViewMInhaComunidade);
        btnVisitarMinhaComunidade = findViewById(R.id.btnVisitarMinhaComunidade);
        recyclerViewComunidades = findViewById(R.id.recyclerViewComunidades);
    }
}