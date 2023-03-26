package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGerenciarUsersGrupo;
import com.example.ogima.adapter.AdapterParticipantesGrupo;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GerenciarUsersGrupoActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarGerenciarGrupo;
    private ImageButton imgBtnBackGerenciarGrupo;
    private Grupo grupo;
    private RecyclerView recyclerViewGerenciarGrupo;
    private AdapterGerenciarUsersGrupo adapterGerenciarUsersGrupo;
    private List<Usuario> listaParticipantes = new ArrayList<>();
    private TextView txtViewLimiteGerenciamento;
    private Button btnSalvarGerenciamento;
    private int limiteSelecao;

    private HashSet<Usuario> hashSetUsuario = new HashSet<>();

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(GerenciarUsersGrupoActivity.this, DetalhesGrupoActivity.class);
        intent.putExtra("grupoAtual", grupo);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_users_grupo);
        inicializandoComponentes();
        setSupportActionBar(toolbarGerenciarGrupo);
        setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {

            if (dados.containsKey("grupoAtual")) {
                grupo = (Grupo) dados.getSerializable("grupoAtual");
            }

            if (dados.containsKey("adicionar")) {
                ToastCustomizado.toastCustomizadoCurto("Adicionar", getApplicationContext());
                limiteSelecao = 40 - grupo.getParticipantes().size();
                recuperarContato();
                recuperarConversa();
            } else if (dados.containsKey("remover")) {
                ToastCustomizado.toastCustomizadoCurto("Remover", getApplicationContext());
                listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                limiteSelecao = grupo.getParticipantes().size() - 2;
            } else if (dados.containsKey("promover")) {
                ToastCustomizado.toastCustomizadoCurto("Promover", getApplicationContext());
                listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                if (grupo.getAdmsGrupo() != null) {
                    limiteSelecao = 5 - grupo.getAdmsGrupo().size();
                } else {
                    limiteSelecao = 5;
                }
            } else if (dados.containsKey("despromover")) {
                ToastCustomizado.toastCustomizadoCurto("Despromover", getApplicationContext());
                listaParticipantes = (List<Usuario>) dados.getSerializable("listaAdms");
                limiteSelecao = 5 - grupo.getAdmsGrupo().size();
            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewGerenciarGrupo.setLayoutManager(linearLayoutManager);
            recyclerViewGerenciarGrupo.setHasFixedSize(true);

            if (adapterGerenciarUsersGrupo != null) {

            } else {
                adapterGerenciarUsersGrupo = new AdapterGerenciarUsersGrupo(listaParticipantes, getApplicationContext(), txtViewLimiteGerenciamento, btnSalvarGerenciamento, limiteSelecao);
            }
            recyclerViewGerenciarGrupo.setAdapter(adapterGerenciarUsersGrupo);

            clickListeners();

            btnSalvarGerenciamento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapterGerenciarUsersGrupo.participantesSelecionados() != null) {
                        ToastCustomizado.toastCustomizadoCurto("Selecionado " + adapterGerenciarUsersGrupo.participantesSelecionados().size(), getApplicationContext());
                    }
                }
            });
        }
    }

    private void clickListeners() {
        imgBtnBackGerenciarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void recuperarContato() {
        DatabaseReference recuperaContatosRef = firebaseRef.child("contatos")
                .child(idUsuario);
        recuperaContatosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotContato : snapshot.getChildren()) {
                    if (snapshotContato.getValue() != null) {
                        Contatos contatos = snapshotContato.getValue(Contatos.class);
                        recuperarUsuarios(contatos.getIdContato());
                    }
                }
                recuperaContatosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarConversa() {
        DatabaseReference recuperaConversasRef = firebaseRef.child("conversas")
                .child(idUsuario);

        recuperaConversasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotConversa : snapshot.getChildren()) {
                    if (snapshotConversa != null) {
                       recuperarUsuarios(snapshotConversa.getKey());
                    }
                }
                recuperaConversasRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarUsuarios(String idRecebido){
        DatabaseReference recuperUsuarioRef = firebaseRef.child("usuarios")
                .child(idRecebido);

        recuperUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);
                    if (!grupo.getParticipantes().contains(usuarioRecuperado.getIdUsuario())) {
                        listaParticipantes.add(usuarioRecuperado);
                    }
                    hashSetUsuario.addAll(listaParticipantes);
                    listaParticipantes.clear();
                    listaParticipantes.addAll(hashSetUsuario);
                    adapterGerenciarUsersGrupo.notifyDataSetChanged();
                }
                recuperUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializandoComponentes() {
        toolbarGerenciarGrupo = findViewById(R.id.toolbarGerenciarGrupo);
        imgBtnBackGerenciarGrupo = findViewById(R.id.imgBtnBackGerenciarGrupo);
        recyclerViewGerenciarGrupo = findViewById(R.id.recyclerViewGerenciarGrupo);
        txtViewLimiteGerenciamento = findViewById(R.id.txtViewLimiteGerenciamento);
        btnSalvarGerenciamento = findViewById(R.id.btnSalvarGerenciamento);
    }
}