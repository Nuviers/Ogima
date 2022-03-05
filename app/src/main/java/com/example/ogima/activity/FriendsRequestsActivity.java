package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
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
    private String idUsuarioLogado, idUsuarioUser, idUsuarioPedido;
    private String emailUsuarioAtual;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Usuario usuarioAmigo, usuarioPedido, usuarioUser;
    private ValueEventListener valueEventListenerDados, valueEventPedidos, valueEventAmigos;
    private DatabaseReference consultarAmigos;
    private DatabaseReference consultarPedidosAmigos;
    private DatabaseReference findFriendsRef, findPedidosRef, usuarioRef;
    private String sinalizador, sinalizadorPedidos;
    private ShimmerFrameLayout shimmerFrameLayout;
    private DatabaseReference pesquisaUsuarioRef;
    private String exibirApelido;

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
        usuarioRef = firebaseRef.child("usuarios");
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

        if (sinalizador != null) {
            if (sinalizador.equals("exibirAmigos")) {
                txtViewTitleToolbar.setText("Amigos");
                try {
                    buttonAmigos.setTextColor(Color.BLACK);
                    buttonAmigos.setBackgroundColor(Color.WHITE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                DatabaseReference amigosRef = firebaseRef.child("friends")
                        .child(idUsuarioLogado);

                amigosRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            //ToastCustomizado.toastCustomizado("Amizade existente", getApplicationContext());
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                animacaoShimmer();
                                //recyclerAmigos.setVisibility(View.VISIBLE);
                                textViewSemPEA.setVisibility(View.GONE);
                                usuarioAmigo = snapshot.getValue(Usuario.class);
                                idUsuarioLogado = usuarioAmigo.getIdUsuario();
                                idUsuarioUser = usuarioAmigo.getIdUsuario();
                                listaAmigos.add(usuarioAmigo);
                                adapterFriends.notifyDataSetChanged();
                                //ToastCustomizado.toastCustomizado("Id amigo " + usuarioAmigo.getIdUsuario(),getApplicationContext());
                            }
                        } else {
                            listaAmigos.clear();
                            adapterFriends.notifyDataSetChanged();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.hideShimmer();
                            recyclerAmigos.setVisibility(View.GONE);
                            textViewSemPEA.setVisibility(View.VISIBLE);
                            textViewSemPEA.setText("Você não possui amigos no momento.");
                            //ToastCustomizado.toastCustomizado("Não existe amizades", getApplicationContext());
                        }
                        amigosRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                findFriendsRef = firebaseRef.child("friends").child(idUsuarioLogado);
            }
        }

        if (sinalizadorPedidos != null) {
            if (sinalizadorPedidos.equals("exibirPedidosAmigos")) {
                txtViewTitleToolbar.setText("Pedidos de amizade");
                try {
                    buttonPedidosAmigos.setTextColor(Color.BLACK);
                    buttonPedidosAmigos.setBackgroundColor(Color.WHITE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                DatabaseReference pedidosRef = firebaseRef.child("pendenciaFriend")
                        .child(idUsuarioLogado);
                pedidosRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            //ToastCustomizado.toastCustomizadoCurto("Pedido de amizade existente", getApplicationContext());
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                animacaoShimmer();
                                //recyclerAmigos.setVisibility(View.VISIBLE);
                                textViewSemPEA.setVisibility(View.GONE);
                                usuarioPedido = snapshot.getValue(Usuario.class);
                                idUsuarioPedido = usuarioPedido.getIdUsuario();
                                listaAmigos.add(usuarioPedido);
                                adapterFriends.notifyDataSetChanged();

                                //*idUsuarioLogado = usuarioAmigo.getIdUsuario();
                                //recuperarAmigo(idUsuarioLogado);
                            }
                        } else {
                            listaAmigos.clear();
                            adapterFriends.notifyDataSetChanged();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.hideShimmer();
                            recyclerAmigos.setVisibility(View.GONE);
                            textViewSemPEA.setVisibility(View.VISIBLE);
                            textViewSemPEA.setText("Você não possui pedidos de amizade no momento.");
                            //ToastCustomizado.toastCustomizadoCurto("Nenhum pedido de amizade", getApplicationContext());
                        }
                        pedidosRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                findPedidosRef = firebaseRef.child("pendenciaFriend").child(idUsuarioLogado);
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
        shimmerFrameLayout = findViewById(R.id.shimmerFriendsRequests);
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

    private void pesquisarAmigos(String s) {
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        listaAmigos.clear();

        //ToastCustomizado.toastCustomizadoCurto("Nome do amigo " + usuarioAmigo.getNomeUsuarioPesquisa(),getApplicationContext());
        if (sinalizador != null) {

            if (s.length() > 0) {
                DatabaseReference searchUsuarioRef = usuarioRef;
                Query queryOne = searchUsuarioRef.orderByChild("nomeUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");
                try {
                    queryOne.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            listaAmigos.clear();
                            /*
                            if (snapshot.getValue() == null) {
                                listaAmigos.clear();
                                adapterFriends.notifyDataSetChanged();
                            }
                             */

                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Usuario usuarioQuery = snap.getValue(Usuario.class);
                                exibirApelido = usuarioQuery.getExibirApelido();

                                //Talvez seja melhor mudar o objeto usuario de baixo
                                //*ToastCustomizado.toastCustomizado("iD USER " + usuarioQuery.getIdUsuario(),getApplicationContext());
                                DatabaseReference verificaUser = firebaseRef.child("friends")
                                        .child(idUsuarioLogado);
                                //Navegando no nó friends para capturar o id do usuário.
                                verificaUser.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getValue() != null) {
                                            for (DataSnapshot snapVerifica : snapshot.getChildren()) {
                                                Usuario usuarioRecept = snapVerifica.getValue(Usuario.class);

                                                    if (idUsuarioLogado.equals(usuarioQuery.getIdUsuario())) {
                                                        continue;
                                                    }
                                                    if (usuarioQuery.getExibirApelido().equals("sim")) {
                                                        continue;
                                                    }
                                                    if (!usuarioQuery.getIdUsuario().equals(usuarioRecept.getIdUsuario())) {
                                                        continue;
                                                    } else {
                                                        listaAmigos.add(usuarioRecept);
                                                        adapterFriends.notifyDataSetChanged();
                                                    }
                                                //*ToastCustomizado.toastCustomizadoCurto("Recept " + usuarioRecept.getIdUsuario(),getApplicationContext());
                                            }
                                        }
                                        verificaUser.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //if (!usuarioAmigo.getIdUsuario().equals(usuarioQuery.getIdUsuario())){
                                // continue;
                                // }
                                //ToastCustomizado.toastCustomizadoCurto("Nome do usuário " + usuarioQuery.getNomeUsuarioPesquisa(),getApplicationContext());

                            }
                            //adapterFriends.notifyDataSetChanged();
                            queryOne.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                                    //Buscando por apelido
                    DatabaseReference searchApelidoRef = usuarioRef;
                    Query queryApelido = searchApelidoRef.orderByChild("apelidoUsuarioPesquisa")
                            .startAt(s)
                            .endAt(s + "\uf8ff");

                    queryApelido.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotApelido) {
                            listaAmigos.clear();
                            for(DataSnapshot snapApelido : snapshotApelido.getChildren()){
                                Usuario usuarioApelido = snapApelido.getValue(Usuario.class);
                                DatabaseReference verificaUserApelido = firebaseRef.child("friends")
                                        .child(idUsuarioLogado);
                                verificaUserApelido.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshotVerificaApelido) {
                                        if(snapshotVerificaApelido.getValue() != null){
                                            for(DataSnapshot snapVerificaApelido : snapshotVerificaApelido.getChildren()){
                                                Usuario usuarioReceptApelido = snapVerificaApelido.getValue(Usuario.class);
                                                if (idUsuarioLogado.equals(usuarioApelido.getIdUsuario())) {
                                                    continue;
                                                }
                                                if (usuarioApelido.getExibirApelido().equals("não")) {
                                                    continue;
                                                }
                                                if (!usuarioApelido.getIdUsuario().equals(usuarioReceptApelido.getIdUsuario())) {
                                                    continue;
                                                } else {
                                                    listaAmigos.add(usuarioReceptApelido);
                                                    adapterFriends.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                        verificaUserApelido.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            queryApelido.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        } else {
            if (s.length() > 0) {
                DatabaseReference searchUsuarioRef = usuarioRef;
                Query queryTwo = searchUsuarioRef.orderByChild("nomeUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");

                try {
                    queryTwo.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            listaAmigos.clear();
                            /*
                            if (snapshot.getValue() == null) {
                                listaAmigos.clear();
                                adapterFriends.notifyDataSetChanged();
                            }
                             */

                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Usuario usuarioQuery = snap.getValue(Usuario.class);
                                exibirApelido = usuarioQuery.getExibirApelido();

                                //Talvez seja melhor mudar o objeto usuario de baixo
                                //*ToastCustomizado.toastCustomizado("iD USER " + usuarioQuery.getIdUsuario(),getApplicationContext());
                                DatabaseReference verificaUser = firebaseRef.child("pendenciaFriend")
                                        .child(idUsuarioLogado);
                                //Navegando no nó friends para capturar o id do usuário.
                                verificaUser.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getValue() != null) {
                                            for (DataSnapshot snapVerifica : snapshot.getChildren()) {
                                                Usuario usuarioRecept = snapVerifica.getValue(Usuario.class);

                                                if (idUsuarioLogado.equals(usuarioQuery.getIdUsuario())) {
                                                    continue;
                                                }
                                                if (usuarioQuery.getExibirApelido().equals("sim")) {
                                                    continue;
                                                }
                                                if (!usuarioQuery.getIdUsuario().equals(usuarioRecept.getIdUsuario())) {
                                                    continue;
                                                } else {
                                                    listaAmigos.add(usuarioRecept);
                                                    adapterFriends.notifyDataSetChanged();
                                                }
                                                //*ToastCustomizado.toastCustomizadoCurto("Recept " + usuarioRecept.getIdUsuario(),getApplicationContext());
                                            }
                                        }
                                        verificaUser.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //if (!usuarioAmigo.getIdUsuario().equals(usuarioQuery.getIdUsuario())){
                                // continue;
                                // }
                                //ToastCustomizado.toastCustomizadoCurto("Nome do usuário " + usuarioQuery.getNomeUsuarioPesquisa(),getApplicationContext());

                            }
                            //adapterFriends.notifyDataSetChanged();
                            queryTwo.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //Buscando por apelido
                    DatabaseReference searchApelidoRef = usuarioRef;
                    Query queryApelido = searchApelidoRef.orderByChild("apelidoUsuarioPesquisa")
                            .startAt(s)
                            .endAt(s + "\uf8ff");

                    queryApelido.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotApelido) {
                            listaAmigos.clear();
                            for(DataSnapshot snapApelido : snapshotApelido.getChildren()){
                                Usuario usuarioApelido = snapApelido.getValue(Usuario.class);
                                DatabaseReference verificaUserApelido = firebaseRef.child("pendenciaFriend")
                                        .child(idUsuarioLogado);
                                verificaUserApelido.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshotVerificaApelido) {
                                        if(snapshotVerificaApelido.getValue() != null){
                                            for(DataSnapshot snapVerificaApelido : snapshotVerificaApelido.getChildren()){
                                                Usuario usuarioReceptApelido = snapVerificaApelido.getValue(Usuario.class);
                                                if (idUsuarioLogado.equals(usuarioApelido.getIdUsuario())) {
                                                    continue;
                                                }
                                                if (usuarioApelido.getExibirApelido().equals("não")) {
                                                    continue;
                                                }
                                                if (!usuarioApelido.getIdUsuario().equals(usuarioReceptApelido.getIdUsuario())) {
                                                    continue;
                                                } else {
                                                    listaAmigos.add(usuarioReceptApelido);
                                                    adapterFriends.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                        verificaUserApelido.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            queryApelido.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }
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

    public void animacaoShimmer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    recyclerAmigos.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }

}