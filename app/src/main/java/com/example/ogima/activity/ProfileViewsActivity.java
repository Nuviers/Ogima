package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterProfileViews;
import com.example.ogima.adapter.AdapterSeguidores;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewsActivity extends AppCompatActivity {

    private SearchView searchViewProfileViews;
    private ImageButton imageButtonBackViews;
    private TextView textViewTitleViews;
    private RecyclerView recyclerProfileViews;
    private AdapterProfileViews adapterProfileViews;
    private List<Usuario> listaViewers;
    private Usuario usuarioViewer;
    private String exibirViewsPerfil, receberUsuario;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private DatabaseReference profileViewsRef = firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_views);
        Toolbar toolbar = findViewById(R.id.toolbarViewsPerfil);
        setSupportActionBar(toolbar);

        inicializarComponentes();

        //Configurações iniciais
        setTitle("");
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        listaViewers = new ArrayList<>();
        recyclerProfileViews.setHasFixedSize(true);
        recyclerProfileViews.setLayoutManager(new LinearLayoutManager(this));
        recyclerProfileViews.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        imageButtonBackViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Bundle dados = getIntent().getExtras();

        if(dados != null){
            exibirViewsPerfil = dados.getString("viewsPerfil");
        }

        if(exibirViewsPerfil != null){
            //Captura quem viu o perfil do usuário atual
            profileViewsRef.child("profileViews").child(idUsuarioLogado).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            usuarioViewer = snapshot.getValue(Usuario.class);
                            adapterProfileViews = new AdapterProfileViews(listaViewers,getApplicationContext());
                            listaViewers.add(usuarioViewer);
                            recyclerProfileViews.setAdapter(adapterProfileViews);
                            //Configurar um shimmerEffect
                            try{
                                adapterProfileViews.notifyDataSetChanged();
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                            //ToastCustomizado.toastCustomizadoCurto("Teste " + usuarioViewer.getNomeUsuario(), getApplicationContext());
                        }
                    }else{
                        //Fazer o mesmo esquema de ocultar um txt com a mensagem e exibir caso caia aqui
                        ToastCustomizado.toastCustomizadoCurto("Sem visualizações no seu perfil atualmente. ", getApplicationContext());
                    }
                    profileViewsRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void inicializarComponentes(){
        searchViewProfileViews = findViewById(R.id.searchViewProfileViews);
        textViewTitleViews = findViewById(R.id.textViewTitleViews);
        imageButtonBackViews = findViewById(R.id.imageButtonBackViews);
        recyclerProfileViews = findViewById(R.id.recyclerProfileViews);
    }

    private void recuperarViewer(){

    }
}