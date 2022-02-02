package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SeguidoresActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Usuario> listaSeguidores;
    private RecyclerView recyclerSeguidores;
    private AdapterSeguidores adapterSeguidores;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Usuario usuario, usuarioSeguidor;
    private String idUsuarioSeguidor;
    private ValueEventListener valueEventListenerDados;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ImageButton imageButtonBack;
    private int receber;
    private Button receberText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguidores);
        Toolbar toolbar = findViewById(R.id.toolbarSeguidores);
        setSupportActionBar(toolbar);

        setTitle("");

        imageButtonBack = findViewById(R.id.imageButtonBack);
        recyclerSeguidores = findViewById(R.id.recyclerSeguidores);
        shimmerFrameLayout = findViewById(R.id.shimmerSeguidores);
        recyclerSeguidores.setHasFixedSize(true);
        recyclerSeguidores.setLayoutManager(new LinearLayoutManager(this));
        listaSeguidores = new ArrayList<>();
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        DatabaseReference seguidoresRef = firebaseRef.child("seguidores")
                .child(idUsuarioLogado);
        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        usuario = snapshot.getValue(Usuario.class);
                        //Toast.makeText(getApplicationContext(), "Seguidor Nome " + usuario.getNomeUsuario(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), "Foto " + usuario.getMinhaFoto(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), "Id seguidor " + usuario.getIdUsuario(), Toast.LENGTH_SHORT).show();
                        idUsuarioLogado = usuario.getIdUsuario();

                        recuperarSeguidor(idUsuarioLogado);
                    }
                }
                seguidoresRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ToastCustomizado.toastCustomizado("Ocorreu um erro, tente novamente", getApplicationContext());
            }
        });

        Toast.makeText(getApplicationContext(), "O id " + idUsuarioLogado, Toast.LENGTH_SHORT).show();

        recyclerSeguidores.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        recyclerSeguidores.setHasFixedSize(true);

    }

    private void recuperarSeguidor(String idSeguidor) {

        DatabaseReference recuperarValor = firebaseRef.child("usuarios")
                .child(idSeguidor);

        valueEventListenerDados = recuperarValor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    usuarioSeguidor = snapshot.getValue(Usuario.class);
                    Toast.makeText(getApplicationContext(), "Valor novo " + usuarioSeguidor.getNomeUsuario(), Toast.LENGTH_SHORT).show();

                    //Aqui que se define o que deve ser exibido no recyclerView
                    listaSeguidores.add(usuarioSeguidor);
                    adapterSeguidores = new AdapterSeguidores(listaSeguidores, getApplicationContext());
                    recyclerSeguidores.setAdapter(adapterSeguidores);

                    adapterSeguidores.notifyDataSetChanged();

                    animacaoShimmer();

                    verificaSegueUsuarioAmigo();

                    recuperarValor.removeEventListener(valueEventListenerDados);



                }
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
                try{
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    recyclerSeguidores.setVisibility(View.VISIBLE);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }


    @Override
    public void onClick(View view) {

        Intent intent = new Intent(getApplicationContext(), PersonProfileActivity.class);
        intent.putExtra("usuarioSelecionado", usuarioSeguidor);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        receberText = view.findViewById(R.id.buttonAction);
        switch (view.getId()) {
            case R.id.buttonAction:
                receberText.setText("Seguindo");
                break;
            case R.id.imageSeguidor:
                startActivity(intent);
                break;
            case R.id.textNomeSeguidor:
                startActivity(intent);
                //receberText = view.findViewById(R.id.textNomeSeguidor);
                break;
        }
         }


    private void verificaSegueUsuarioAmigo(){

        DatabaseReference seguindoRef = firebaseRef.child("seguindo")
                .child( idUsuarioLogado )
                .child( usuarioSeguidor.getIdUsuario() );

        DatabaseReference seguidorRef = firebaseRef.child("seguidores")
                .child(usuarioSeguidor.getIdUsuario())
                .child(idUsuarioLogado);

        seguindoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if( dataSnapshot.exists() ){
                            //Já está seguindo
                            //receberText.setText("Seguindo");
                            //receberText.setText("Seguindo");
                            Toast.makeText(getApplicationContext(), "Seguindo", Toast.LENGTH_SHORT).show();
                        }else {
                            //Ainda não está seguindo
                            //receberText.setText("Seguir");
                            //receberText.setText("Seguir");
                            Toast.makeText(getApplicationContext(), "Não seguindo", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }
}