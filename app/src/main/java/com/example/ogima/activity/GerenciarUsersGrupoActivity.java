package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGerenciarUsersGrupo;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GerenciarUsersGrupoActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarGerenciarGrupo;
    private ImageButton imgBtnBackGerenciarGrupo;
    private Grupo grupo;
    private RecyclerView recyclerViewGerenciarGrupo;
    private AdapterGerenciarUsersGrupo adapterGerenciarUsersGrupo;
    private List<Usuario> listaParticipantes = new ArrayList<>();
    private List<Usuario> listaAtualizadaParticipantes = new ArrayList<>();
    private TextView txtViewLimiteGerenciamento;
    private Button btnSalvarGerenciamento;
    private int limiteSelecao;
    private String tipoGerenciamento;
    private String idConversaGrupo;
    private DatabaseReference adicionaMsgExclusaoRef;
    private HashSet<Usuario> hashSetUsuario = new HashSet<>();
    private String conteudoAviso;
    private Boolean removerDespromover = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(GerenciarUsersGrupoActivity.this, DetalhesGrupoActivity.class);
        intent.putExtra("grupoAtual", grupo);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_users_grupo);
        inicializandoComponentes();
        setSupportActionBar(toolbarGerenciarGrupo);
        setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {

            if (dados.containsKey("grupoAtual")) {
                grupo = (Grupo) dados.getSerializable("grupoAtual");
            }

            if (dados.containsKey("tipoGerenciamento")) {
                tipoGerenciamento = dados.getString("tipoGerenciamento");
            }

            switch (tipoGerenciamento) {
                case "adicionar":
                    //ToastCustomizado.toastCustomizadoCurto("Adicionar", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    limiteSelecao = 40 - grupo.getParticipantes().size();
                    break;
                case "remover":
                    //ToastCustomizado.toastCustomizadoCurto("Remover", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    limiteSelecao = grupo.getParticipantes().size() - 2;
                    break;
                case "promover":
                    //ToastCustomizado.toastCustomizadoCurto("Promover", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    if (grupo.getAdmsGrupo() != null) {
                        limiteSelecao = 5 - grupo.getAdmsGrupo().size();
                    } else {
                        limiteSelecao = 5;
                    }
                    break;
                case "despromover":
                    //ToastCustomizado.toastCustomizadoCurto("Despromover", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaAdms");
                    break;
            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewGerenciarGrupo.setLayoutManager(linearLayoutManager);
            recyclerViewGerenciarGrupo.setHasFixedSize(true);

            if (adapterGerenciarUsersGrupo != null) {

            } else {
                adapterGerenciarUsersGrupo = new AdapterGerenciarUsersGrupo(listaParticipantes, getApplicationContext(), txtViewLimiteGerenciamento, btnSalvarGerenciamento, limiteSelecao, tipoGerenciamento);
            }
            recyclerViewGerenciarGrupo.setAdapter(adapterGerenciarUsersGrupo);

            clickListeners();

            btnSalvarGerenciamento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapterGerenciarUsersGrupo.participantesSelecionados() != null) {
                        if (adapterGerenciarUsersGrupo.participantesSelecionados().size() > 0) {
                            switch (tipoGerenciamento) {
                                case "adicionar":
                                    adicionarUsuarios();
                                    break;
                                case "remover":
                                    removerUsuarios();
                                    break;
                                case "promover":
                                    promoverUsuarios();
                                    break;
                                case "despromover":
                                    despromoverUsuarios();
                                    break;
                            }
                            //ToastCustomizado.toastCustomizadoCurto("Selecionado " + adapterGerenciarUsersGrupo.participantesSelecionados().size(), getApplicationContext());
                        }
                    }
                }
            });
        }
    }

    private void clickListeners() {
        imgBtnBackGerenciarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void adicionarUsuarios() {
        ArrayList<String> listaNova = new ArrayList<>();
        listaNova.addAll(grupo.getParticipantes());
        listaNova.addAll(adapterGerenciarUsersGrupo.participantesSelecionados());
        for(String idAdicionado : adapterGerenciarUsersGrupo.participantesSelecionados()){
            salvarAviso(idAdicionado, "adição");
        }
        //ToastCustomizado.toastCustomizadoCurto("Lista atualizada - " + listaNova.size(), getApplicationContext());
        DatabaseReference adicionarUsuariosRef = firebaseRef.child("grupos")
                .child(grupo.getIdGrupo()).child("participantes");
        adicionarUsuariosRef.setValue(listaNova).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                finish();
            }
        });
    }

    private void removerUsuarios() {
        ArrayList<String> listaNova = new ArrayList<>();
        listaNova.addAll(grupo.getParticipantes());
        for (String idRemovido : adapterGerenciarUsersGrupo.participantesSelecionados()) {
            listaNova.remove(idRemovido);
            salvarAviso(idRemovido, "remoção");

            if(grupo.getAdmsGrupo() != null && grupo.getAdmsGrupo().size() > 0){
                if (grupo.getAdmsGrupo().contains(idRemovido)) {
                    //Usuário removido também é adm
                    removerDespromover = true;
                }
            }
        }
        //ToastCustomizado.toastCustomizadoCurto("Lista atualizada - " + listaNova.size(), getApplicationContext());
        DatabaseReference adicionarUsuariosRef = firebaseRef.child("grupos")
                .child(grupo.getIdGrupo()).child("participantes");
        adicionarUsuariosRef.setValue(listaNova).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (removerDespromover) {
                    removerDespromover = false;
                    despromoverUsuarios();
                }else{
                    finish();
                }
            }
        });
    }

    private void promoverUsuarios() {
        ArrayList<String> listaNova = new ArrayList<>();
        if (grupo.getAdmsGrupo() != null) {
            if (grupo.getAdmsGrupo().size() > 0) {
                listaNova.addAll(grupo.getAdmsGrupo());
            }
        }
        listaNova.addAll(adapterGerenciarUsersGrupo.participantesSelecionados());

        for(String idPromovido : adapterGerenciarUsersGrupo.participantesSelecionados()){
            salvarAviso(idPromovido, "promoção");
        }

        //ToastCustomizado.toastCustomizadoCurto("Lista atualizada - " + listaNova.size(), getApplicationContext());
        DatabaseReference adicionarUsuariosRef = firebaseRef.child("grupos")
                .child(grupo.getIdGrupo()).child("admsGrupo");
        adicionarUsuariosRef.setValue(listaNova).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
               finish();
            }
        });
    }

    private void despromoverUsuarios() {

        ArrayList<String> listaNova = new ArrayList<>();
        listaNova.addAll(grupo.getAdmsGrupo());
        for (String idDespromovido : adapterGerenciarUsersGrupo.participantesSelecionados()) {
            listaNova.remove(idDespromovido);
            salvarAviso(idDespromovido, "despromoção");
        }
        //ToastCustomizado.toastCustomizadoCurto("Lista atualizada - " + listaNova.size(), getApplicationContext());

        if (listaNova.size() > 0) {
            DatabaseReference adicionarUsuariosRef = firebaseRef.child("grupos")
                    .child(grupo.getIdGrupo()).child("admsGrupo");
            adicionarUsuariosRef.setValue(listaNova).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                }
            });
        } else {
            DatabaseReference adicionarUsuariosRef = firebaseRef.child("grupos")
                    .child(grupo.getIdGrupo()).child("admsGrupo");
            adicionarUsuariosRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                }
            });
        }
    }

    private void salvarAviso(String idAlvo, String tipoAviso) {

        FirebaseRecuperarUsuario.montarAvisoChat(idAlvo, idUsuario, new FirebaseRecuperarUsuario.MontarAvisoChatCallback() {
            @Override
            public void onNomesAvisoConfigurado(String nomeAfetado, String nomeExecutor) {

                switch (tipoAviso){
                    case "remoção":
                        conteudoAviso = nomeAfetado + " removido por " + nomeExecutor;
                        break;
                    case "adição":
                        conteudoAviso = nomeAfetado + " adicionado por " + nomeExecutor;
                        break;
                    case "promoção":
                        conteudoAviso = nomeAfetado + " promovido por " + nomeExecutor;
                        break;
                    case "despromoção":
                        conteudoAviso = nomeAfetado + " despromovido por " + nomeExecutor;
                        break;
                }

                adicionaMsgExclusaoRef = firebaseRef.child("conversas");

                idConversaGrupo = adicionaMsgExclusaoRef.push().getKey();

                HashMap<String, Object> dadosMensagem = new HashMap<>();
                dadosMensagem.put("idConversa", idConversaGrupo);
                dadosMensagem.put("exibirAviso", true);
                dadosMensagem.put("conteudoMensagem", conteudoAviso);

                adicionaMsgExclusaoRef = adicionaMsgExclusaoRef.child(grupo.getIdGrupo())
                        .child(idConversaGrupo);

                adicionaMsgExclusaoRef.setValue(dadosMensagem);
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void inicializandoComponentes() {
        toolbarGerenciarGrupo = findViewById(R.id.toolbarGerenciarGrupo);
        imgBtnBackGerenciarGrupo = findViewById(R.id.imgBtnBackGerenciarGrupo);
        recyclerViewGerenciarGrupo = findViewById(R.id.recyclerViewGerenciarGrupo);
        txtViewLimiteGerenciamento = findViewById(R.id.txtViewLimiteGerenciamento);
        btnSalvarGerenciamento = findViewById(R.id.btnSalvarGerenciamento);
    }
}