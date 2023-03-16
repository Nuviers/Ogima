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
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterChatGrupo;
import com.example.ogima.adapter.AdapterUsuariosGrupo;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UsuariosGrupoActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarCadastroGrupo;
    private RecyclerView recyclerUsuariosGrupo;
    private AdapterUsuariosGrupo adapterUsuariosGrupo;
    private List<Usuario> listaUsuario = new ArrayList<>();
    private HashSet<Usuario> hashSetUsuario = new HashSet<>();
    private TextView txtViewSelecaoParticipantes;
    private Button btnProximaEtapaGrupo;
    private HashSet<String> listaParticipantesSelecionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios_grupo);
        inicializarComponentes();
        setSupportActionBar(toolbarCadastroGrupo);
        toolbarCadastroGrupo.setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        recuperarContatos();
        recuperarConversa();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerUsuariosGrupo.setLayoutManager(linearLayoutManager);
        recyclerUsuariosGrupo.setHasFixedSize(true);

        if (adapterUsuariosGrupo != null) {

        } else {
            adapterUsuariosGrupo = new AdapterUsuariosGrupo(listaUsuario, getApplicationContext(), txtViewSelecaoParticipantes, btnProximaEtapaGrupo);
        }
        recyclerUsuariosGrupo.setAdapter(adapterUsuariosGrupo);


        btnProximaEtapaGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaParticipantesSelecionados = adapterUsuariosGrupo.participantesSelecionados();
                exibirLista();
            }
        });

    }

    private void recuperarContatos() {
        DatabaseReference recuperaContatosRef = firebaseRef.child("contatos")
                .child(idUsuario);

        recuperaContatosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Contatos contatos = snapshot1.getValue(Contatos.class);

                    DatabaseReference recuperaUsuarioRef = firebaseRef.child("usuarios")
                            .child(contatos.getIdContato());
                    recuperaUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Usuario usuario = snapshot.getValue(Usuario.class);
                                listaUsuario.add(usuario);
                                hashSetUsuario.addAll(listaUsuario);
                                listaUsuario.clear();
                                listaUsuario.addAll(hashSetUsuario);
                                adapterUsuariosGrupo.notifyDataSetChanged();
                            }
                            recuperaUsuarioRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                recuperaContatosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarConversa() {
        DatabaseReference recuperaContatosRef = firebaseRef.child("conversas")
                .child(idUsuario);

        recuperaContatosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    DatabaseReference recuperaUsuarioRef = firebaseRef.child("usuarios")
                            .child(snapshot1.getKey());
                    recuperaUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Usuario usuario = snapshot.getValue(Usuario.class);
                                listaUsuario.add(usuario);
                                hashSetUsuario.addAll(listaUsuario);
                                listaUsuario.clear();
                                listaUsuario.addAll(hashSetUsuario);
                                adapterUsuariosGrupo.notifyDataSetChanged();
                            }
                            recuperaUsuarioRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                recuperaContatosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exibirLista() {
        if (listaParticipantesSelecionados != null) {
            if (listaParticipantesSelecionados.size() >= 1) {
                Intent intent = new Intent(getApplicationContext(), CriarGrupoActivity.class);
                intent.putExtra("listaParticipantes", listaParticipantesSelecionados);
                startActivity(intent);
                finish();
            } else {
                ToastCustomizado.toastCustomizadoCurto("Necessário selecionar pelo menos um usuário para criar um grupo!", getApplicationContext());
            }
        }
    }

    private void inicializarComponentes() {
        toolbarCadastroGrupo = findViewById(R.id.toolbarUsersGrupo);
        recyclerUsuariosGrupo = findViewById(R.id.recyclerUsuariosGrupo);
        txtViewSelecaoParticipantes = findViewById(R.id.txtViewSelecaoParticipantes);
        btnProximaEtapaGrupo = findViewById(R.id.btnProximaEtapaGrupo);
    }
}