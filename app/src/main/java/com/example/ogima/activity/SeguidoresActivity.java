package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterSeguidores;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.RecyclerItemClickListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeguidoresActivity extends AppCompatActivity {

    private List<Usuario> listaSeguidores;
    private RecyclerView recyclerSeguidores;
    private AdapterSeguidores adapterSeguidores;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Usuario usuario, usuarioSeguidor;
    private ValueEventListener valueEventListenerDados;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ImageButton imageButtonBack;
    private TextView textSemSeguidores, textView13;
    private String exibirDados;
    private SearchView searchViewSeguidores;
    private DatabaseReference consultarSeguidores;
    private DatabaseReference consultarSeguindo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguidores);
        Toolbar toolbar = findViewById(R.id.toolbarSeguidores);
        setSupportActionBar(toolbar);

        //Implementar método de swipe refresh, puxando o botão pra baixo.

        setTitle("");

        imageButtonBack = findViewById(R.id.imageButtonBack);
        recyclerSeguidores = findViewById(R.id.recyclerSeguidores);
        shimmerFrameLayout = findViewById(R.id.shimmerSeguidores);
        recyclerSeguidores.setHasFixedSize(true);
        recyclerSeguidores.setLayoutManager(new LinearLayoutManager(this));
        listaSeguidores = new ArrayList<>();
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        textSemSeguidores = findViewById(R.id.textSemSeguidores);
        textView13 = findViewById(R.id.textView13);
        searchViewSeguidores = findViewById(R.id.searchViewFindSeguidores);

        searchViewSeguidores.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewSeguidores.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String dadoDigitado = query.toUpperCase(Locale.ROOT);
                pesquisarSeguidor(dadoDigitado);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String dadoDigitado = newText.toUpperCase(Locale.ROOT);
                pesquisarSeguidor(dadoDigitado);
                return true;
            }
        });

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            exibirDados = dados.getString("exibirSeguindo");
            textView13.setText("Seguindo");
        }

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (exibirDados != null) {
            DatabaseReference seguindoRef = firebaseRef.child("seguindo")
                    .child(idUsuarioLogado);
            seguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            animacaoShimmer();
                            usuario = snapshot.getValue(Usuario.class);
                            //Toast.makeText(getApplicationContext(), "Seguidor Nome " + usuario.getNomeUsuario(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), "Foto " + usuario.getMinhaFoto(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), "Id seguidor " + usuario.getIdUsuario(), Toast.LENGTH_SHORT).show();
                            idUsuarioLogado = usuario.getIdUsuario();

                            recuperarSeguidor(idUsuarioLogado);
                        }
                    } else {
                        //Caso usuário não tenha seguidores.
                        textSemSeguidores.setVisibility(View.VISIBLE);
                        recyclerSeguidores.setVisibility(View.GONE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.hideShimmer();
                        textSemSeguidores.setText("Você não está seguindo" +
                                " ninguém no momento");
                    }
                    seguindoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro, tente novamente", getApplicationContext());
                }
            });
        } else {
            DatabaseReference seguidoresRef = firebaseRef.child("seguidores")
                    .child(idUsuarioLogado);
            seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            animacaoShimmer();
                            usuario = snapshot.getValue(Usuario.class);
                            //Toast.makeText(getApplicationContext(), "Seguidor Nome " + usuario.getNomeUsuario(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), "Foto " + usuario.getMinhaFoto(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), "Id seguidor " + usuario.getIdUsuario(), Toast.LENGTH_SHORT).show();
                            idUsuarioLogado = usuario.getIdUsuario();

                            recuperarSeguidor(idUsuarioLogado);
                        }
                    } else {
                        //Caso usuário não tenha seguidores.
                        textSemSeguidores.setVisibility(View.VISIBLE);
                        recyclerSeguidores.setVisibility(View.GONE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.hideShimmer();
                    }
                    seguidoresRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro, tente novamente", getApplicationContext());
                }
            });
        }
        //Toast.makeText(getApplicationContext(), "O id " + idUsuarioLogado, Toast.LENGTH_SHORT).show();

        recyclerSeguidores.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        recyclerSeguidores.setHasFixedSize(true);

        consultarSeguidores = firebaseRef.child("seguidores")
                .child(idUsuarioLogado);

        consultarSeguindo = firebaseRef.child("seguindo")
                .child(idUsuarioLogado);

    }

    private void recuperarSeguidor(String idSeguidor) {

        DatabaseReference recuperarValor = firebaseRef.child("usuarios")
                .child(idSeguidor);

        valueEventListenerDados = recuperarValor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    usuarioSeguidor = snapshot.getValue(Usuario.class);
                    //Toast.makeText(getApplicationContext(), "Valor novo " + usuarioSeguidor.getNomeUsuario(), Toast.LENGTH_SHORT).show();

                    adapterSeguidores = new AdapterSeguidores(listaSeguidores, getApplicationContext());

                    //Aqui que se define o que deve ser exibido no recyclerView
                    listaSeguidores.add(usuarioSeguidor);

                    recyclerSeguidores.setAdapter(adapterSeguidores);

                    adapterSeguidores.notifyDataSetChanged();

                    //Configura evento de clique no recyclerView
                    recyclerSeguidores.addOnItemTouchListener(new RecyclerItemClickListener(
                            getApplicationContext(),
                            recyclerSeguidores,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    usuarioSeguidor = listaSeguidores.get(position);
                                    recuperarValor.removeEventListener(valueEventListenerDados);
                                    listaSeguidores.clear();
                                    if (exibirDados != null) {
                                        Intent intent = new Intent(getApplicationContext(), PersonProfileActivity.class);
                                        intent.putExtra("usuarioSelecionado", usuarioSeguidor);
                                        intent.putExtra("backIntent", "seguindoActivity");
                                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), PersonProfileActivity.class);
                                        intent.putExtra("usuarioSelecionado", usuarioSeguidor);
                                        intent.putExtra("backIntent", "seguidoresActivity");
                                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                }
                            }
                    ));
                }
                recuperarValor.removeEventListener(valueEventListenerDados);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void animacaoShimmer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    recyclerSeguidores.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }

    //Localiza tanto seguidores quanto seguindo
    private void pesquisarSeguidor(String s) {

        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);

        listaSeguidores.clear();

        //Toast.makeText(getApplicationContext(), "Valor digitado " + s, Toast.LENGTH_SHORT).show();

        //Trocar os toast pela escrita no txt e mude a visibilidade dele e oculte depois

        if(exibirDados != null){
            if (s.length() > 0) {
                Query queryOne = consultarSeguindo.orderByChild("nomeUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");

                try {
                    queryOne.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.getValue() == null) {
                                textSemSeguidores.setVisibility(View.VISIBLE);
                                textSemSeguidores.setText("Você não está" +
                                        " seguindo ninguém com esse nome");
                                //ToastCustomizado.toastCustomizadoCurto("Você não esta seguindo ninguém com esse nome", getApplicationContext());
                                listaSeguidores.clear();
                            }else{
                                textSemSeguidores.setVisibility(View.GONE);
                            }
                            listaSeguidores.clear();
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Usuario usuarioQuery = snap.getValue(Usuario.class);

                                if (idUsuarioLogado.equals(usuario.getIdUsuario()))
                                    continue;

                                listaSeguidores.add(usuarioQuery);
                                //Toast.makeText(getApplicationContext(), "localizado " + usuarioQuery.getNomeUsuarioPesquisa(), Toast.LENGTH_SHORT).show();
                            }
                            adapterSeguidores.notifyDataSetChanged();
                            queryOne.removeEventListener(this);
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

        }else {

            if (s.length() > 0) {
                Query queryTwo = consultarSeguidores.orderByChild("nomeUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");

                try {
                    queryTwo.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.getValue() == null) {
                                textSemSeguidores.setVisibility(View.VISIBLE);
                                textSemSeguidores.setText("Você não tem nenhum" +
                                        " seguidor com esse nome");
                                //ToastCustomizado.toastCustomizadoCurto("Você não tem nenhum seguidor com esse nome", getApplicationContext());
                                listaSeguidores.clear();
                            }else{
                                textSemSeguidores.setVisibility(View.GONE);
                            }
                            listaSeguidores.clear();
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Usuario usuarioQuery = snap.getValue(Usuario.class);

                                if (idUsuarioLogado.equals(usuario.getIdUsuario()))
                                    continue;

                                listaSeguidores.add(usuarioQuery);
                                //Toast.makeText(getApplicationContext(), "localizado " + usuarioQuery.getNomeUsuarioPesquisa(), Toast.LENGTH_SHORT).show();
                            }
                            adapterSeguidores.notifyDataSetChanged();
                            queryTwo.removeEventListener(this);
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
}