package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFriendsRequests;
import com.example.ogima.adapter.AdapterSeguidores;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.RecyclerItemClickListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FriendsRequestsActivity extends AppCompatActivity implements View.OnClickListener {

    private AdapterFriendsRequests adapterFriends;
    private TextView txtViewTitleToolbar, textViewSemPEA;
    private Button buttonAmigos, buttonPedidosAmigos;
    private SearchView searchViewFindAmigos;
    private RecyclerView recyclerAmigos;
    private ImageButton imgButtonBackF;
    private List<Usuario> listaAmigos = new ArrayList<>();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Usuario usuario, usuarioAmigo;
    private ValueEventListener valueEventListenerDados, valueEventPedidos, valueEventAmigos;
    private DatabaseReference consultarAmigos;
    private DatabaseReference consultarPedidosAmigos;
    private String sinalizador, sinalizadorPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_requests);
        Toolbar toolbarAmigos = findViewById(R.id.toolbarFriends);
        setSupportActionBar(toolbarAmigos);
        inicializarComponentes();

        setTitle("");

        //Configurações iniciais
        buttonAmigos.setOnClickListener(this);
        buttonPedidosAmigos.setOnClickListener(this);
        imgButtonBackF.setOnClickListener(this);
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        searchViewFindAmigos.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewFindAmigos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String dadoDigitado = query.toUpperCase(Locale.ROOT);
                pesquisarAmigos(dadoDigitado);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String dadoDigitado = newText.toUpperCase(Locale.ROOT);
                pesquisarAmigos(dadoDigitado);
                return true;
            }
        });

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            sinalizador = dados.getString("exibirAmigos");
            sinalizadorPedidos = dados.getString("exibirPedidosAmigos");
        }

        if(sinalizador != null) {
            if (sinalizador.equals("exibirAmigos")) {
                txtViewTitleToolbar.setText("Amigos");
                DatabaseReference amigosRef = firebaseRef.child("friends")
                        .child(idUsuarioLogado);

              amigosRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            //ToastCustomizado.toastCustomizado("Amizade existente", getApplicationContext());
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                //animacaoShimmer();
                                recyclerAmigos.setVisibility(View.VISIBLE);
                                textViewSemPEA.setVisibility(View.GONE);
                                usuario = snapshot.getValue(Usuario.class);
                                idUsuarioLogado = usuario.getIdUsuario();
                                listaAmigos.add(usuario);
                                adapterFriends.notifyDataSetChanged();

                            }
                        } else {
                            listaAmigos.clear();
                            adapterFriends.notifyDataSetChanged();
                            recyclerAmigos.setVisibility(View.GONE);
                            textViewSemPEA.setVisibility(View.VISIBLE);
                            textViewSemPEA.setText("Você não possui amigos no momento.");
                            //ToastCustomizado.toastCustomizado("Não existe amizades", getApplicationContext());
                        }
                        //amigosRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

            if(sinalizadorPedidos != null) {
                if (sinalizadorPedidos.equals("exibirPedidosAmigos")) {
                    txtViewTitleToolbar.setText("Pedidos de amizade");
                    DatabaseReference pedidosRef = firebaseRef.child("pendenciaFriend")
                            .child(idUsuarioLogado);
                   pedidosRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                //ToastCustomizado.toastCustomizadoCurto("Pedido de amizade existente", getApplicationContext());
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    recyclerAmigos.setVisibility(View.VISIBLE);
                                    textViewSemPEA.setVisibility(View.GONE);
                                    usuarioAmigo = snapshot.getValue(Usuario.class);
                                    listaAmigos.add(usuarioAmigo);
                                    adapterFriends.notifyDataSetChanged();

                                    //*idUsuarioLogado = usuarioAmigo.getIdUsuario();
                                    //recuperarAmigo(idUsuarioLogado);
                                }
                            } else {
                                listaAmigos.clear();
                                adapterFriends.notifyDataSetChanged();
                                recyclerAmigos.setVisibility(View.GONE);
                                textViewSemPEA.setVisibility(View.VISIBLE);
                                textViewSemPEA.setText("Você não possui pedidos de amizade no momento.");
                                //ToastCustomizado.toastCustomizadoCurto("Nenhum pedido de amizade", getApplicationContext());
                            }
                            //pedidosRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        adapterFriends = new AdapterFriendsRequests(listaAmigos, getApplicationContext());
        recyclerAmigos.setAdapter(adapterFriends);

        recyclerAmigos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAmigos.setHasFixedSize(true);
        recyclerAmigos.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        //Consulta para query
        consultarAmigos = firebaseRef.child("friends")
                .child(idUsuarioLogado);

        consultarPedidosAmigos = firebaseRef.child("pendenciaFriend")
                .child(idUsuarioLogado);

        adapterFriends.notifyDataSetChanged();

    }

    private void inicializarComponentes() {
        txtViewTitleToolbar = findViewById(R.id.txtViewTitleToolbar);
        buttonAmigos = findViewById(R.id.buttonAmigos);
        buttonPedidosAmigos = findViewById(R.id.buttonPedidosAmigos);
        searchViewFindAmigos = findViewById(R.id.searchViewFindAmigos);
        recyclerAmigos = findViewById(R.id.recyclerAmigos);
        imgButtonBackF = findViewById(R.id.imgButtonBackF);
        textViewSemPEA = findViewById(R.id.textViewSemPEA);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), FriendsRequestsActivity.class);
        switch (view.getId()) {
            case R.id.buttonAmigos:
                txtViewTitleToolbar.setText("Amigos");
                finish();
                intent.putExtra("exibirAmigos", "exibirAmigos");
                overridePendingTransition(0, 0);
                overridePendingTransition(0, 0);
                startActivity(intent);
                break;
            case R.id.buttonPedidosAmigos:
                txtViewTitleToolbar.setText("Pedidos de amizade");
                finish();
                //intent = new Intent(getApplicationContext(), FriendsRequestsActivity.class);
                intent.putExtra("exibirPedidosAmigos", "exibirPedidosAmigos");
                overridePendingTransition(0, 0);
                overridePendingTransition(0, 0);
                startActivity(intent);
                break;
            case R.id.imgButtonBackF:
                listaAmigos.clear();
                adapterFriends.notifyDataSetChanged();
                finish();
                break;
        }
    }

    private void pesquisarAmigos(String dadoCapturado) {

    }

    /*
    @Override
    protected void onStop() {
        super.onStop();

        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        DatabaseReference amigosRef = firebaseRef.child("friends")
                .child(idUsuarioLogado);
        DatabaseReference pedidosRef = firebaseRef.child("pendenciaFriend")
                .child(idUsuarioLogado);
        pedidosRef.removeEventListener(valueEventPedidos);
        amigosRef.removeEventListener(valueEventAmigos);

    }
     */

}