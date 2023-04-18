package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterMinhasComunidades;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ComunidadeDAO;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Usuario;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListaComunidadesActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarListaComunidade;
    private ImageButton imgButtonBackListaComunidade;
    private ImageView imgViewPerfilMinhaComunidade;
    private TextView txtViewMInhaComunidade;
    private Button btnVisitarMinhaComunidade, btnComunidadesComVinculo;
    private RecyclerView recyclerViewComunidades;

    //Criar comunidade
    private ImageButton imgBtnCadastroComunidade;
    private Button btnCadastroComunidade;

    //Minhas comunidades
    private RecyclerView recyclerViewMinhasComunidades;
    private List<Comunidade> listaMinhasComunidades = new ArrayList<>();
    private List<Comunidade> listaComunidades = new ArrayList<>();
    private ComunidadeDAO minhaComunidadeDAO;
    private ComunidadeDAO comunidadeDAO;
    private LinearLayoutManager layoutManagerMinhasComunidades, layoutManagerComunidades;
    private AdapterMinhasComunidades adapterMinhasComunidades;
    private AdapterMinhasComunidades adapterComunidades;
    private Query minhasComunidadesRef;
    private Query comunidadesRef;
    private Boolean limiteComunidadeAtingido = false;
    private Snackbar snackbarLimiteComunidade;

    @Override
    protected void onStart() {
        super.onStart();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        recuperarMinhaComunidade();
        recuperarComunidades();
    }

    @Override
    protected void onStop() {
        super.onStop();

        minhaComunidadeDAO.limparListaComunidade(adapterMinhasComunidades);
        comunidadeDAO.limparListaComunidade(adapterComunidades);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_comunidades);
        setSupportActionBar(toolbarListaComunidade);
        inicializarComponentes();
        toolbarListaComunidade.setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        clickListeners();
    }

    private void clickListeners() {

        verificaLimiteComunidades();

        imgBtnCadastroComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaCadastro();
            }
        });

        btnCadastroComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaCadastro();
            }
        });

        btnComunidadesComVinculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListaComunidadesActivity.this, ComunidadesComVinculoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void verificaLimiteComunidades() {
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdMinhasComunidades() != null
                        && usuarioAtual.getIdMinhasComunidades().size() >= 5) {
                    limiteComunidadeAtingido = true;
                } else {
                    limiteComunidadeAtingido = false;
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void irParaCadastro() {
        if (limiteComunidadeAtingido) {
            //Usuário atual já tem 5 comunidades criadas.
            snackbarLimiteComunidade = Snackbar.make(btnCadastroComunidade, "Limite de criação de comunidades atingido," +
                            " por favor exclua uma delas para que seja possível criar uma nova comunidade!",
                    Snackbar.LENGTH_LONG);
            View snackbarView = snackbarLimiteComunidade.getView();
            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            textView.setMaxLines(5); // altera o número máximo de linhas exibidas
            snackbarLimiteComunidade.show();
        } else {
            Intent intent = new Intent(ListaComunidadesActivity.this, UsuariosGrupoActivity.class);
            intent.putExtra("tipoCadastro", "comunidade");
            startActivity(intent);
        }
    }

    private void recuperarMinhaComunidade() {

        //Configuração do recycler das minhas comunidades
        configRecyclerMinhasComunidades();

        minhaComunidadeDAO = new ComunidadeDAO(listaMinhasComunidades, getApplicationContext());

        minhasComunidadesRef = firebaseRef.child("comunidades")
                .orderByChild("idSuperAdmComunidade")
                .equalTo(idUsuario);

        minhasComunidadesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshotChild : snapshot.getChildren()) {
                    if (snapshotChild.getValue() != null) {
                        Comunidade minhaComunidade = snapshotChild.getValue(Comunidade.class);
                        //ToastCustomizado.toastCustomizadoCurto("Achou", getApplicationContext());
                        minhaComunidadeDAO.adicionarComunidade(minhaComunidade, adapterMinhasComunidades);
                    }
                }
                minhasComunidadesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configRecyclerMinhasComunidades() {

        if (layoutManagerMinhasComunidades == null) {
            layoutManagerMinhasComunidades = new LinearLayoutManager(getApplicationContext());
        }
        layoutManagerMinhasComunidades.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewMinhasComunidades.setHasFixedSize(true);
        recyclerViewMinhasComunidades.setScrollbarFadingEnabled(false);
        recyclerViewMinhasComunidades.setLayoutManager(layoutManagerMinhasComunidades);

        if (adapterMinhasComunidades == null) {
            adapterMinhasComunidades = new AdapterMinhasComunidades(getApplicationContext(), listaMinhasComunidades);
        }
        recyclerViewMinhasComunidades.setAdapter(adapterMinhasComunidades);
    }

    private void recuperarComunidades() {

        configRecyclerComunidades();

        comunidadeDAO = new ComunidadeDAO(listaComunidades, getApplicationContext());

        comunidadesRef = firebaseRef.child("comunidades")
                .orderByChild("nomeGrupo");

        comunidadesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comunidade> comunidadesComVinculo = new ArrayList<>();
                for (DataSnapshot snapshotChild : snapshot.getChildren()) {
                    if (snapshotChild.getValue() != null) {
                        Comunidade comunidade = snapshotChild.getValue(Comunidade.class);
                        if (comunidade.getParticipantes() != null && comunidade.getParticipantes().contains(idUsuario)
                        && comunidade.getIdSuperAdmComunidade() != null && !comunidade.getIdSuperAdmComunidade().equals(idUsuario)) {
                            //Lista temporária com somente comunidades que o usuário atual não é dono
                            //e que é participante.
                            comunidadesComVinculo.add(comunidade);
                        }
                    }
                }

                //Deixa a lista em ordem decrescente
                Collections.reverse(comunidadesComVinculo);
                // Pega os 5 últimos registros
                List<Comunidade> comunidadeOrdenada = comunidadesComVinculo.subList(0, Math.min(comunidadesComVinculo.size(), 5));
                // Adiciona os registros ao RecyclerView
                for (Comunidade comunidade : comunidadeOrdenada) {
                    comunidadeDAO.adicionarComunidade(comunidade, adapterComunidades);
                }
                comunidadesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configRecyclerComunidades() {

        if (layoutManagerComunidades == null) {
            layoutManagerComunidades = new LinearLayoutManager(getApplicationContext());
        }
        layoutManagerComunidades.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewComunidades.setHasFixedSize(true);
        recyclerViewComunidades.setScrollbarFadingEnabled(false);
        recyclerViewComunidades.setLayoutManager(layoutManagerComunidades);

        if (adapterComunidades == null) {
            adapterComunidades = new AdapterMinhasComunidades(getApplicationContext(), listaComunidades);
        }
        recyclerViewComunidades.setAdapter(adapterComunidades);
    }

    private void inicializarComponentes() {
        toolbarListaComunidade = findViewById(R.id.toolbarListaComunidade);
        imgButtonBackListaComunidade = findViewById(R.id.imgButtonBackListaComunidade);
        btnVisitarMinhaComunidade = findViewById(R.id.btnVisitarMinhaComunidade);
        recyclerViewComunidades = findViewById(R.id.recyclerViewComunidades);
        imgBtnCadastroComunidade = findViewById(R.id.imgBtnCadastroComunidade);
        btnCadastroComunidade = findViewById(R.id.btnCadastroComunidade);

        //Minhas comunidades
        recyclerViewMinhasComunidades = findViewById(R.id.recyclerViewMinhasComunidades);

        //Comunidades com vínculo
        btnComunidadesComVinculo = findViewById(R.id.btnComunidadesComVinculo);
    }
}