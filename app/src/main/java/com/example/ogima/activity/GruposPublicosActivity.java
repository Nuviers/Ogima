package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGruposPublicosDiff;
import com.example.ogima.adapter.AdapterTopicosGrupoPublico;
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
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GruposPublicosActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Toolbar toolbarGruposPublicos;
    private ImageButton imgBtnBackGruposPublicos;
    private RecyclerView recyclerTopicosGrupo;
    //Mudar para o adapter que vai exibir os grupos de acordo com o filtro selecionado.
    private AdapterTopicosGrupoPublico adapterTopicosGrupoPublico;
    private String[] topicosGrupo = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    private final ArrayList<String> listaTopicosGrupo = new ArrayList<>(Arrays.asList(topicosGrupo));
    private Button btnFiltrarGrupos;

    //Grupos
    private AdapterGruposPublicosDiff adapterGruposPublicos;
    private RecyclerView recyclerGruposPublicos;
    private GrupoTesteDAO grupoTesteDAO;
    private List<Grupo> listaGrupos = new ArrayList<>();
    private Query grupoRef;
    private ChildEventListener childEventListener;
    private LinearLayoutManager linearLayoutManagerGrupos;

    @Override
    protected void onStart() {
        super.onStart();

        configRecyclerView();

        grupoTesteDAO = new GrupoTesteDAO(listaGrupos, adapterGruposPublicos);

        grupoRef = firebaseRef.child("grupos").orderByChild("grupoPublico").equalTo(true);

        recuperarGrupos(false);
    }

    private void recuperarGrupos(Boolean filtragem) {
        childEventListener = grupoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo novoGrupo = snapshot.getValue(Grupo.class);

                    if (filtragem) {
                        if (adapterTopicosGrupoPublico.getListaTopicosSelecionados() != null
                                && adapterTopicosGrupoPublico.getListaTopicosSelecionados().size() > 0) {

                            for (String topicoFiltrado : adapterTopicosGrupoPublico.getListaTopicosSelecionados()) {
                                if (novoGrupo.getTopicos().contains(topicoFiltrado)) {
                                    // Adiciona o grupo na lista mantendo a ordenação
                                    grupoTesteDAO.adicionarGrupo(novoGrupo);

                                    // Notifica o adapter das mudanças usando o DiffUtil
                                    adapterGruposPublicos.updateGroupPublicList(listaGrupos);
                                    break;
                                }
                            }
                        }
                    } else {
                        // Adiciona o grupo na lista mantendo a ordenação
                        grupoTesteDAO.adicionarGrupo(novoGrupo);

                        // Notifica o adapter das mudanças usando o DiffUtil
                        adapterGruposPublicos.updateGroupPublicList(listaGrupos);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {

                    //Colocar um filtro que faz com que se tal grupo esteja na lista
                    //de grupo bloqueado do usuário atual não exibir esse grupo.

                    // Recupera o grupo do snapshot
                    Grupo grupoAtualizado = snapshot.getValue(Grupo.class);

                    // Atualiza o grupo na lista mantendo a ordenação
                    grupoTesteDAO.atualizarGrupo(grupoAtualizado);

                    // Notifica o adapter das mudanças usando o DiffUtil
                    adapterGruposPublicos.updateGroupPublicList(listaGrupos);
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
                    adapterGruposPublicos.updateGroupPublicList(listaGrupos);
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

    @Override
    protected void onStop() {
        super.onStop();

        if (childEventListener != null) {
            grupoRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
        grupoTesteDAO.limparListaGrupos();

        adapterTopicosGrupoPublico.limparTopicosFiltrados();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos_publicos);
        inicializandoComponentes();
        setSupportActionBar(toolbarGruposPublicos);
        setTitle("");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        clickListeners();
/*
        for (String topicos : listaTopicosGrupo) {
            Chip chip = new Chip(this);
            chip.setText(topicos);
            chip.setCheckable(true);
            //chip.setChipBackgroundColor(ColorStateList.valueOf(Color.GRAY));
            chipGroupTopicosGrupo.addView(chip);
        }

 */

    }

    private void configRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerTopicosGrupo.setHasFixedSize(true);

        // Defina o gerenciador de layout do RecyclerView como ChipsLayoutManager
        ChipsLayoutManager layoutManager = ChipsLayoutManager.newBuilder(this)
                .setOrientation(ChipsLayoutManager.VERTICAL)
                .setMaxViewsInRow(2)
                .build();

        recyclerTopicosGrupo.setLayoutManager(layoutManager);
        if (adapterTopicosGrupoPublico != null) {

        } else {
            adapterTopicosGrupoPublico = new AdapterTopicosGrupoPublico(getApplicationContext(), listaTopicosGrupo);
        }
        recyclerTopicosGrupo.setAdapter(adapterTopicosGrupoPublico);
        adapterTopicosGrupoPublico.notifyDataSetChanged();


        //Configuração do recycler de grupos
        if (linearLayoutManagerGrupos != null) {

        } else {
            linearLayoutManagerGrupos = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerGrupos.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerGruposPublicos.setHasFixedSize(true);
        recyclerGruposPublicos.setLayoutManager(linearLayoutManagerGrupos);

        if (adapterGruposPublicos != null) {

        } else {
            adapterGruposPublicos = new AdapterGruposPublicosDiff(getApplicationContext(), listaGrupos, adapterTopicosGrupoPublico.getListaTopicosSelecionados());
        }
        recyclerGruposPublicos.setAdapter(adapterGruposPublicos);
    }

    private void clickListeners() {

        imgBtnBackGruposPublicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnFiltrarGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receberTopicos();
            }
        });
    }

    private void inicializandoComponentes() {
        toolbarGruposPublicos = findViewById(R.id.toolbarGruposPublicos);
        imgBtnBackGruposPublicos = findViewById(R.id.imgBtnBackGruposPublicos);
        recyclerTopicosGrupo = findViewById(R.id.recyclerTopicosGrupo);
        btnFiltrarGrupos = findViewById(R.id.btnFiltrarGrupos);
        recyclerGruposPublicos = findViewById(R.id.recyclerGruposPublicos);
    }

    private void receberTopicos() {
        if (adapterTopicosGrupoPublico.getListaTopicosSelecionados() != null
                && adapterTopicosGrupoPublico.getListaTopicosSelecionados().size() > 0) {

            if (childEventListener != null) {
                grupoRef.removeEventListener(childEventListener);
                childEventListener = null;
            }
            grupoTesteDAO.limparListaGrupos();
            adapterGruposPublicos.updateGroupPublicList(listaGrupos);

            recuperarGrupos(true);

            for (String topicos : adapterTopicosGrupoPublico.getListaTopicosSelecionados()) {
                //ToastCustomizado.toastCustomizadoCurto("Recebido " + topicos, getApplicationContext());
            }
        } else {
            recuperarGrupos(false);
        }
    }
}