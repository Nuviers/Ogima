package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
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

public class ProfileViewsActivity extends AppCompatActivity {

    private SearchView searchViewProfileViews;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ImageButton imageButtonBackViews;
    private TextView textViewTitleViews, textViewSemViewsProfile;
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
    private DatabaseReference consultarViewer;

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
        searchViewProfileViews.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewProfileViews.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String dadoDigitado = query.toUpperCase(Locale.ROOT);
                pesquisarViewer(dadoDigitado);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String dadoDigitado = newText.toUpperCase(Locale.ROOT);
                pesquisarViewer(dadoDigitado);
                return true;
            }
        });

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
                            animacaoShimmer();
                            usuarioViewer = snapshot.getValue(Usuario.class);
                            adapterProfileViews = new AdapterProfileViews(listaViewers,getApplicationContext());
                            listaViewers.add(usuarioViewer);
                            recyclerProfileViews.setAdapter(adapterProfileViews);
                            try{
                                adapterProfileViews.notifyDataSetChanged();
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }else{
                        textViewSemViewsProfile.setVisibility(View.VISIBLE);
                        recyclerProfileViews.setVisibility(View.GONE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.hideShimmer();
                        textViewSemViewsProfile.setText("Você não tem" +
                                " visualizações no seu perfil no momento");
                    }
                    profileViewsRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
            consultarViewer = firebaseRef.child("profileViews").child(idUsuarioLogado);
    }

    private void pesquisarViewer(String s) {
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);

        listaViewers.clear();

        if(exibirViewsPerfil != null){
            if (s.length() > 0) {
                Query queryOne = consultarViewer.orderByChild("nomeUsuarioPesquisa")
                        .startAt(s)
                        .endAt(s + "\uf8ff");

                try {
                    queryOne.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() == null) {
                                textViewSemViewsProfile.setVisibility(View.VISIBLE);
                                textViewSemViewsProfile.setText("Você não tem" +
                                        " visualizações no seu perfil no momento");
                                listaViewers.clear();
                            }else{
                                textViewSemViewsProfile.setVisibility(View.GONE);
                            }
                            listaViewers.clear();
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Usuario usuarioQuery = snap.getValue(Usuario.class);

                                if (idUsuarioLogado.equals(usuarioViewer.getIdUsuario()))
                                    continue;
                                listaViewers.add(usuarioQuery);
                            }
                            try{
                                adapterProfileViews.notifyDataSetChanged();
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
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
            } else {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }

    private void inicializarComponentes(){
        searchViewProfileViews = findViewById(R.id.searchViewProfileViews);
        textViewTitleViews = findViewById(R.id.textViewTitleViews);
        imageButtonBackViews = findViewById(R.id.imageButtonBackViews);
        recyclerProfileViews = findViewById(R.id.recyclerProfileViews);
        textViewSemViewsProfile = findViewById(R.id.textViewSemViewsProfile);
        shimmerFrameLayout = findViewById(R.id.shimmerProfileViews);
    }

    public void animacaoShimmer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    recyclerProfileViews.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }
}