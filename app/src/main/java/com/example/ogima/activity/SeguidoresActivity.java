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
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
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
    private ImageButton imageButtonBack;
    private TextView textSemSeguidores, textView13;
    private String exibirSeguindo, exibirSeguidores;
    private SearchView searchViewSeguidores;
    private DatabaseReference consultarSeguidores;
    private DatabaseReference consultarSeguindo;
    private Handler handler = new Handler();

    private String irParaProfile = null;
    private String idDonoPerfil = null;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (irParaProfile != null) {
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            startActivity(intent);
            finish();
        }else{
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguidores);
        Toolbar toolbar = findViewById(R.id.toolbarSeguidores);
        setSupportActionBar(toolbar);

        //Implementar método de swipe refresh, puxando o botão pra baixo.

        setTitle("");

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            exibirSeguindo = dados.getString("exibirSeguindo");
            exibirSeguidores = dados.getString("exibirSeguidores");

            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }

            if (dados.containsKey("idDonoPerfil")) {
                idDonoPerfil = dados.getString("idDonoPerfil");
            }
        }

        imageButtonBack = findViewById(R.id.imageButtonBack);
        recyclerSeguidores = findViewById(R.id.recyclerSeguidores);
        recyclerSeguidores.setHasFixedSize(true);
        recyclerSeguidores.setLayoutManager(new LinearLayoutManager(this));
        listaSeguidores = new ArrayList<>();
        adapterSeguidores = new AdapterSeguidores(listaSeguidores, getApplicationContext());
        recyclerSeguidores.setAdapter(adapterSeguidores);
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        textSemSeguidores = findViewById(R.id.textSemSeguidores);
        textView13 = findViewById(R.id.textView13);
        searchViewSeguidores = findViewById(R.id.searchViewFindSeguidores);

        searchViewSeguidores.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewSeguidores.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String dadoDigitado =  Normalizer.normalize(query, Normalizer.Form.NFD);
                dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                String dadoDigitadoOk = dadoDigitado.toUpperCase(Locale.ROOT);
                //ToastCustomizado.toastCustomizado("Dado digitado " + dadoDigitadoOk, getContext());
                pesquisarSeguidor(dadoDigitadoOk);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String dadoDigitado =  Normalizer.normalize(newText, Normalizer.Form.NFD);
                dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                String dadoDigitadoOk = dadoDigitado.toUpperCase(Locale.ROOT);

                handler.removeCallbacksAndMessages(null);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pesquisarSeguidor(dadoDigitadoOk);
                    }
                }, 400);
                return true;
            }
        });

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (exibirSeguindo != null) {
            textView13.setText("Seguindo");
            DatabaseReference seguindoRef = firebaseRef.child("seguindo")
                    .child(idDonoPerfil);
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
                            recuperarSeguidor(usuario.getIdUsuario());
                        }
                    } else {
                        //Caso usuário não esteja seguindo ninguém.
                        textSemSeguidores.setVisibility(View.VISIBLE);
                        recyclerSeguidores.setVisibility(View.GONE);
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
        } else if(exibirSeguidores != null){
            textView13.setText("Seguidores");
            DatabaseReference seguidoresRef = firebaseRef.child("seguidores")
                    .child(idDonoPerfil);
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

                            recuperarSeguidor(usuario.getIdUsuario());
                        }
                    } else {
                        //Caso usuário não tenha seguidores.
                        textSemSeguidores.setVisibility(View.VISIBLE);
                        recyclerSeguidores.setVisibility(View.GONE);
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
                .child(idDonoPerfil);

        consultarSeguindo = firebaseRef.child("seguindo")
                .child(idDonoPerfil);

    }

    private void recuperarSeguidor(String idSeguidor) {

        /*
        try{
            listaSeguidores.clear();
            adapterSeguidores.notifyDataSetChanged();
        }catch (Exception ex){
            ex.printStackTrace();
            adapterSeguidores.notifyDataSetChanged();
        }
         */

        DatabaseReference recuperarValor = firebaseRef.child("usuarios")
                .child(idSeguidor);

        valueEventListenerDados = recuperarValor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    try{
                        usuarioSeguidor = snapshot.getValue(Usuario.class);
                        //Aqui que se define o que deve ser exibido no recyclerView
                        listaSeguidores.add(usuarioSeguidor);
                        adapterSeguidores.notifyDataSetChanged();

                        //Configura evento de clique no recyclerView
                        recyclerSeguidores.addOnItemTouchListener(new RecyclerItemClickListener(
                                getApplicationContext(),
                                recyclerSeguidores,
                                new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        try{
                                            usuarioSeguidor = listaSeguidores.get(position);
                                            recuperarValor.removeEventListener(valueEventListenerDados);
                                            listaSeguidores.clear();
                                            DatabaseReference verificaBlock = firebaseRef
                                                    .child("blockUser").child(idUsuarioLogado).child(usuarioSeguidor.getIdUsuario());

                                            verificaBlock.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.getValue() != null){
                                                        ToastCustomizado.toastCustomizadoCurto("Perfil do usuário indisponível!", getApplicationContext());
                                                    } else if (!usuarioSeguidor.getIdUsuario().equals(idUsuarioLogado)){
                                                        if(exibirSeguindo != null){
                                                            handler.removeCallbacksAndMessages(null);
                                                            Intent intent = new Intent(getApplicationContext(), PersonProfileActivity.class);
                                                            intent.putExtra("usuarioSelecionado", usuarioSeguidor);
                                                            intent.putExtra("backIntent", "seguindoActivity");
                                                            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }else{
                                                            handler.removeCallbacksAndMessages(null);
                                                            Intent intent = new Intent(getApplicationContext(), PersonProfileActivity.class);
                                                            intent.putExtra("usuarioSelecionado", usuarioSeguidor);
                                                            intent.putExtra("backIntent", "seguidoresActivity");
                                                            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                    verificaBlock.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }catch (Exception ex){
                                            ex.printStackTrace();
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
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
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
                    searchViewSeguidores.setVisibility(View.VISIBLE);
                    recyclerSeguidores.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }

    //Localiza tanto seguidores quanto seguindo
    private void pesquisarSeguidor(String s) {
        try{
            listaSeguidores.clear();
            adapterSeguidores.notifyDataSetChanged();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if(exibirSeguindo != null){
            if (s.length() > 0) {
                DatabaseReference consultaSeguindoNew = firebaseRef.child("usuarios");
                Query queryOne = consultaSeguindoNew.orderByChild("nomeUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");

                try {
                    queryOne.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            listaSeguidores.clear();

                            if (snapshot.getValue() == null) {
                                //textSemSeguidores.setVisibility(View.VISIBLE);
                                //textSemSeguidores.setText("Você não está" +
                                //        " seguindo ninguém com esse nome ou apelido");
                                //ToastCustomizado.toastCustomizadoCurto("Você não esta seguindo ninguém com esse nome", getApplicationContext());
                                //listaSeguidores.clear();
                            }else{
                                textSemSeguidores.setVisibility(View.GONE);
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    Usuario usuarioQuery = snap.getValue(Usuario.class);

                                    DatabaseReference searchSeguindo = firebaseRef.child("seguindo")
                                            .child(idDonoPerfil);

                                    searchSeguindo.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.getValue() != null){
                                                for(DataSnapshot snapOne : snapshot.getChildren()){
                                                    Usuario usuarioSeguidorNew = snapOne.getValue(Usuario.class);
                                                    if (idUsuarioLogado.equals(usuarioQuery.getIdUsuario())) {
                                                        continue;
                                                    }
                                                    if (usuarioQuery.getExibirApelido().equals("sim")) {
                                                        continue;
                                                    }
                                                    if (!usuarioQuery.getIdUsuario().equals(usuarioSeguidorNew.getIdUsuario())) {
                                                        continue;
                                                    } else {
                                                        //*listaSeguidores.clear();
                                                        recuperarSeguidor(usuarioSeguidorNew.getIdUsuario());
                                                        //listaSeguidores.add(usuarioSeguidorNew);
                                                        //adapterSeguidores.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                            searchSeguindo.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    // if (idUsuarioLogado.equals(usuario.getIdUsuario()))
                                    //continue;

                                    //listaSeguidores.add(usuarioQuery);
                                    //Toast.makeText(getApplicationContext(), "localizado " + usuarioQuery.getNomeUsuarioPesquisa(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            queryOne.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    DatabaseReference searchApelidoRef = firebaseRef.child("usuarios");
                    Query querySeguidorApelido = searchApelidoRef.orderByChild("apelidoUsuarioPesquisa")
                            .startAt(s)
                            .endAt(s + "\uf8ff");

                    querySeguidorApelido.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotSeguidor) {
                            listaSeguidores.clear();

                            if (snapshotSeguidor.getValue() == null) {
                                //textSemSeguidores.setVisibility(View.VISIBLE);
                                //textSemSeguidores.setText("Você não está" +
                                //        " seguindo ninguém com esse nome ou apelido");
                                //ToastCustomizado.toastCustomizadoCurto("Você não esta seguindo ninguém com esse nome", getApplicationContext());
                                //listaSeguidores.clear();
                            }else{
                                textSemSeguidores.setVisibility(View.GONE);
                                for(DataSnapshot snapApelidoSeguidor : snapshotSeguidor.getChildren()){
                                    Usuario usuarioSeguidorApelido = snapApelidoSeguidor.getValue(Usuario.class);
                                    DatabaseReference verificaUserApelido = firebaseRef.child("seguindo")
                                            .child(idDonoPerfil);

                                    verificaUserApelido.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshotVerificaApelido) {
                                            if(snapshotVerificaApelido.getValue() != null){
                                                for(DataSnapshot snapVerificaApelido : snapshotVerificaApelido.getChildren()){
                                                    Usuario usuarioReceptApelido = snapVerificaApelido.getValue(Usuario.class);
                                                    if (idUsuarioLogado.equals(usuarioSeguidorApelido.getIdUsuario())) {
                                                        continue;
                                                    }
                                                    if (usuarioSeguidorApelido.getExibirApelido().equals("não")) {
                                                        continue;
                                                    }
                                                    if (!usuarioSeguidorApelido.getIdUsuario().equals(usuarioReceptApelido.getIdUsuario())) {
                                                        continue;
                                                    } else {
                                                        //*listaSeguidores.clear();
                                                        recuperarSeguidor(usuarioReceptApelido.getIdUsuario());
                                                        //listaSeguidores.add(usuarioReceptApelido);
                                                        //adapterSeguidores.notifyDataSetChanged();
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
                            }
                            querySeguidorApelido.removeEventListener(this);
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

                DatabaseReference consultaSeguidoresNew = firebaseRef.child("usuarios");
                Query queryTwo = consultaSeguidoresNew.orderByChild("nomeUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");

                try {
                    queryTwo.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            listaSeguidores.clear();

                            if (snapshot.getValue() == null) {
                                //textSemSeguidores.setVisibility(View.VISIBLE);
                                //textSemSeguidores.setText("Você não tem" +
                                //        " seguidores com esse nome ou apelido");
                                //ToastCustomizado.toastCustomizadoCurto("Você não esta seguindo ninguém com esse nome", getApplicationContext());
                                //listaSeguidores.clear();
                            }else{
                                textSemSeguidores.setVisibility(View.GONE);
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    Usuario usuarioQuery = snap.getValue(Usuario.class);

                                    DatabaseReference searchSeguidores = firebaseRef.child("seguidores")
                                            .child(idDonoPerfil);

                                    searchSeguidores.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.getValue() != null){
                                                for(DataSnapshot snapOne : snapshot.getChildren()){
                                                    Usuario usuarioSeguidorNew = snapOne.getValue(Usuario.class);
                                                    if (idUsuarioLogado.equals(usuarioQuery.getIdUsuario())) {
                                                        continue;
                                                    }
                                                    if (usuarioQuery.getExibirApelido().equals("sim")) {
                                                        continue;
                                                    }
                                                    if (!usuarioQuery.getIdUsuario().equals(usuarioSeguidorNew.getIdUsuario())) {
                                                        continue;
                                                    } else {
                                                        //*listaSeguidores.clear();
                                                        recuperarSeguidor(usuarioSeguidorNew.getIdUsuario());
                                                        //listaSeguidores.add(usuarioSeguidorNew);
                                                        //adapterSeguidores.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                            searchSeguidores.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    // if (idUsuarioLogado.equals(usuario.getIdUsuario()))
                                    //continue;

                                    //listaSeguidores.add(usuarioQuery);
                                    //Toast.makeText(getApplicationContext(), "localizado " + usuarioQuery.getNomeUsuarioPesquisa(), Toast.LENGTH_SHORT).show();
                                }
                            }


                            queryTwo.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    DatabaseReference searchApelidoRef = firebaseRef.child("usuarios");
                    Query querySeguidorApelido = searchApelidoRef.orderByChild("apelidoUsuarioPesquisa")
                            .startAt(s)
                            .endAt(s + "\uf8ff");

                    querySeguidorApelido.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotSeguidor) {
                            listaSeguidores.clear();

                            if (snapshotSeguidor.getValue() == null) {
                                //textSemSeguidores.setVisibility(View.VISIBLE);
                                //textSemSeguidores.setText("Você não tem" +
                                //        " seguidores com esse nome ou apelido");
                                //ToastCustomizado.toastCustomizadoCurto("Você não esta seguindo ninguém com esse nome", getApplicationContext());
                                //
                                // listaSeguidores.clear();
                            }else{
                                textSemSeguidores.setVisibility(View.GONE);
                                for(DataSnapshot snapApelidoSeguidor : snapshotSeguidor.getChildren()){
                                    Usuario usuarioSeguidorApelido = snapApelidoSeguidor.getValue(Usuario.class);
                                    DatabaseReference verificaUserApelido = firebaseRef.child("seguidores")
                                            .child(idDonoPerfil);

                                    verificaUserApelido.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshotVerificaApelido) {
                                            if(snapshotVerificaApelido.getValue() != null){
                                                for(DataSnapshot snapVerificaApelido : snapshotVerificaApelido.getChildren()){
                                                    Usuario usuarioReceptApelido = snapVerificaApelido.getValue(Usuario.class);
                                                    if (idUsuarioLogado.equals(usuarioSeguidorApelido.getIdUsuario())) {
                                                        continue;
                                                    }
                                                    if (usuarioSeguidorApelido.getExibirApelido().equals("não")) {
                                                        continue;
                                                    }
                                                    if (!usuarioSeguidorApelido.getIdUsuario().equals(usuarioReceptApelido.getIdUsuario())) {
                                                        continue;
                                                    } else {
                                                        //*listaSeguidores.clear();
                                                        recuperarSeguidor(usuarioReceptApelido.getIdUsuario());
                                                        //listaSeguidores.add(usuarioReceptApelido);
                                                        //adapterSeguidores.notifyDataSetChanged();
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
                            }


                            querySeguidorApelido.removeEventListener(this);
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