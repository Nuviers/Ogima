package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterTesteFirebaseUi;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;

public class TesteFirebaseUiActivity extends AppCompatActivity {


    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Query usuarioRef;
    private AdapterTesteFirebaseUi adapterTesteFirebaseUi;
    private RecyclerView recyclerViewTesteFirebaseUi;

    @Override
    protected void onStart() {
        super.onStart();
        adapterTesteFirebaseUi.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterTesteFirebaseUi.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_firebase_ui);

        recyclerViewTesteFirebaseUi = findViewById(R.id.recyclerTesteFirebaseUi);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Limita quantos vão aparecer, se eu usar o last então os últimos X devem aparecer
        //se eu usar o ToFirst então os primeiros X devem aparecer.
        //limitToFirst(2) limitToLast(2)
        usuarioRef = firebaseRef.child("usuarios").orderByChild("nomeUsuarioPesquisa");

        //O melhor de tudo de usar Firebase UI/FirebaseRecyclerAdapter ele cuida de
        //de todoo CRUD sozinho. Remoção,alteração,mudança de lógica, adição.
        FirebaseRecyclerOptions<Usuario> options =
                new FirebaseRecyclerOptions.Builder<Usuario>()
                        .setQuery(usuarioRef, Usuario.class)
                        .build();

        adapterTesteFirebaseUi = new AdapterTesteFirebaseUi(getApplicationContext(),options);
        recyclerViewTesteFirebaseUi.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTesteFirebaseUi.setAdapter(adapterTesteFirebaseUi);
    }
}