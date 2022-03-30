package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFotosPostadas;
import com.example.ogima.adapter.AdapterSeguidores;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FotosPostadasActivity extends AppCompatActivity {

    private ImageButton imageButtonBackFtPostada;
    private String emailUsuario, idUsuario;
    private Usuario usuarioFotos;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    //Variaveis do recycler
    private RecyclerView recyclerFotosPostadas;
    private AdapterFotosPostadas adapterFotosPostadas;
    private List<Usuario> listaFotosPostadas;
    private int receberPosicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos_postadas);
        inicializarComponentes();
        Toolbar toolbar = findViewById(R.id.toolbarFotosPostadas);
        setSupportActionBar(toolbar);
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurações iniciais
        setTitle("");
        recyclerFotosPostadas.setLayoutManager(new LinearLayoutManager(this));
        listaFotosPostadas = new ArrayList<>();

        //recyclerFotosPostadas.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        recyclerFotosPostadas.setHasFixedSize(true);

        Bundle dados = getIntent().getExtras();

        if(dados != null){
            receberPosicao = dados.getInt("atualizarEdicao");
        }

        imageButtonBackFtPostada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                intent.putExtra("atualize","atualize");
                startActivity(intent);
                finish();
            }
        });

        DatabaseReference fotosUsuarioRef = firebaseRef.child("fotosUsuario")
                .child(idUsuario);

        fotosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    try{
                        usuarioFotos = snapshot.getValue(Usuario.class);

                        if(adapterFotosPostadas != null){
                            adapterFotosPostadas.notifyDataSetChanged();
                        }else{
                            adapterFotosPostadas = new AdapterFotosPostadas(listaFotosPostadas, getApplicationContext());
                            recyclerFotosPostadas.setAdapter(adapterFotosPostadas);
                        }

                        ArrayList<Integer> listaOrdem = new ArrayList<>();
                        listaOrdem = usuarioFotos.getListaOrdenacaoFotoPostada();


                        //Arruma a ordem da lista ao editar
                            if(receberPosicao >= 1){
                                Comparator<Integer> comparatorOrdem = Collections.reverseOrder();
                                Collections.sort(listaOrdem, comparatorOrdem);
                                adapterFotosPostadas.notifyDataSetChanged();
                                //ToastCustomizado.toastCustomizadoCurto("Posição rodada " + receberPosicao, getApplicationContext());
                                recyclerFotosPostadas.smoothScrollToPosition(listaOrdem.get(receberPosicao));
                            }


/*
                        for(int i = 0; i < listaData.size(); i ++){
                            listaFotosPostadas.add(usuarioFotos);
                            if(i == listaData.size() - 1){
                                break;
                            }
                        }
 */
                        for(int i = 0; i < listaOrdem.size(); i ++){
                            listaFotosPostadas.add(usuarioFotos);
                            if(i == listaOrdem.size()){
                                break;
                            }
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                fotosUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        recyclerFotosPostadas = findViewById(R.id.recyclerViewFotosPostadas);
        imageButtonBackFtPostada = findViewById(R.id.imageButtonBackFtPostada);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        intent.putExtra("atualize","atualize");
        startActivity(intent);
        finish();
    }

    public void reterPosicao(ArrayList<String> lista){
            if(lista.size() >= 1){
                recyclerFotosPostadas.smoothScrollToPosition(lista.size() - 1);
        }
    }
}