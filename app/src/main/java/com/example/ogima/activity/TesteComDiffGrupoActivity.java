package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGrupoDiff;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GrupoTesteDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Grupo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TesteComDiffGrupoActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private ImageView imgViewFotoGrupoTesteSing;
    private TextView txtViewNomeGrupoTesteSing;
    private Button btnExcluirGrupoTesteSing;

    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;

    private List<Grupo> listaGrupos = new ArrayList<>();
    private List<Grupo> listaGruposV2 = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerLiveDataTeste;
    private AdapterGrupoDiff adapterGrupoDiff;

    private DatabaseReference grupoRef;
    private GrupoTesteDAO grupoTesteDAO;

    @Override
    protected void onStart() {
        super.onStart();

        configRecyclerView();

        grupoTesteDAO = new GrupoTesteDAO(listaGrupos, adapterGrupoDiff);

        grupoRef = firebaseRef.child("grupos");

        comChildEventListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (childEventListener != null) {
            grupoRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
        grupoTesteDAO.limparListaGrupos();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_singleton);
        inicializandoComponentes();

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);


    }


    private void configRecyclerView() {
        if (linearLayoutManager != null) {

        } else {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerLiveDataTeste.setHasFixedSize(true);
        recyclerLiveDataTeste.setLayoutManager(linearLayoutManager);

        if (adapterGrupoDiff != null) {

        } else {
            adapterGrupoDiff = new AdapterGrupoDiff(getApplicationContext(), listaGrupos);
        }

        recyclerLiveDataTeste.setAdapter(adapterGrupoDiff);
        //adapterGrupoDiff.notifyDataSetChanged();
    }

    private void inicializandoComponentes() {
        imgViewFotoGrupoTesteSing = findViewById(R.id.imgViewFotoGrupoTesteSing);
        txtViewNomeGrupoTesteSing = findViewById(R.id.txtViewNomeGrupoTesteSing);
        btnExcluirGrupoTesteSing = findViewById(R.id.btnExcluirGrupoTesteSing);
        recyclerLiveDataTeste = findViewById(R.id.recyclerLiveDataTeste);
    }

    private void comChildEventListener() {
        childEventListener = grupoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo novoGrupo = snapshot.getValue(Grupo.class);

                    // Adiciona o grupo na lista mantendo a ordenação
                    grupoTesteDAO.adicionarGrupo(novoGrupo);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterGrupoDiff.updateGroupListItems(listaGrupos);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    // Recupera o grupo do snapshot
                    Grupo grupoAtualizado = snapshot.getValue(Grupo.class);

                    // Atualiza o grupo na lista mantendo a ordenação
                    grupoTesteDAO.atualizarGrupo(grupoAtualizado);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterGrupoDiff.updateGroupListItems(listaGrupos);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    // Recupera o grupo do snapshot
                    Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                    Log.d("TESTE-On Child Removed", "Usuario removido do snapshot: " + grupoRemovido.getNomeGrupo());

                    // Remove o grupo da lista mantendo a ordenação
                    grupoTesteDAO.removerGrupo(grupoRemovido);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterGrupoDiff.updateGroupListItems(listaGrupos);
                    Log.d("TESTE-On Child Removed", "Adapter notificado com sucesso");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}