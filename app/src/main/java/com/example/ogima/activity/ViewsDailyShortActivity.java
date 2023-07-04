package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewsDailyShortActivity extends AppCompatActivity {

    private String idDailyShortAtual = null;
    private Toolbar toolbarInc;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private RecyclerView recyclerViewsDaily;
    private Query queryLoadMore;
    private Query queryInicial;
    private ArrayList<String> listaIdViewers = new ArrayList<>();
    private int mCurrentPosition = -1;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    private interface RecuperarListaViews{
        void onRecuperado(ArrayList<String> listaIdsVisualizadores);
        void onSemViews();
        void onErroAoRecuperar(String message);
    }

    @Override
    protected void onStart() {
        super.onStart();

        buscarDadosIniciais();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_daily_short);
        inicializandoComponentes();
        setSupportActionBar(toolbarInc);
        setTitle("");
        txtViewIncTituloToolbar.setText("Visualizações DailyShort");
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null && dados.containsKey("idDailyShort")) {
            idDailyShortAtual = dados.getString("idDailyShort");
        }

        clickListeners();
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void inicializandoComponentes() {
        toolbarInc = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        recyclerViewsDaily = findViewById(R.id.recyclerViewsDaily);
    }

    private void buscarDadosIniciais(){
        if (idDailyShortAtual != null) {
            recuperarIdsViews(new RecuperarListaViews() {
                @Override
                public void onRecuperado(ArrayList<String> listaIdsVisualizadores) {

                }

                @Override
                public void onSemViews() {
                    ToastCustomizado.toastCustomizado("Não existem visualizações para esse dailyShort no momento", getApplicationContext());
                    finish();
                }

                @Override
                public void onErroAoRecuperar(String message) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar as visualizações, tente novamente mais tarde.", getApplicationContext());
                    finish();
                }
            });
        }else{
            finish();
        }
    }

    private void recuperarIdsViews(RecuperarListaViews callback){

        DatabaseReference dadosDailyAtualRef = firebaseRef.child("dailyShorts")
                .child(idUsuario).child(idDailyShortAtual);

        dadosDailyAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    DailyShort dailyShortAtual = snapshot.getValue(DailyShort.class);
                    if (dailyShortAtual.getListaIdsVisualizadores() != null
                            && dailyShortAtual.getListaIdsVisualizadores().size() > 0) {
                        callback.onRecuperado(dailyShortAtual.getListaIdsVisualizadores());
                    }else{
                      callback.onSemViews();
                    }
                }else{
                    callback.onSemViews();
                }
                dadosDailyAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 callback.onErroAoRecuperar(error.getMessage());
            }
        });
    }
}