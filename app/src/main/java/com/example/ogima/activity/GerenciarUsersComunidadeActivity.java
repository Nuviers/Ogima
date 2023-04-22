package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGerenciarUsersComunidade;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Convite;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class GerenciarUsersComunidadeActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarGerenciarComunidade;
    private ImageButton imgBtnBackGerenciarComunidade;
    private Comunidade comunidade;
    private RecyclerView recyclerViewGerenciarComunidade;
    private AdapterGerenciarUsersComunidade adapterGerenciarUsersComunidade;
    private List<Usuario> listaParticipantes = new ArrayList<>();
    private List<Usuario> listaAtualizadaParticipantes = new ArrayList<>();
    private TextView txtViewLimiteGerenciamento;
    private Button btnSalvarGerenciamento;
    private int limiteSelecao;
    private String tipoGerenciamento;
    private String idConversaComunidade;
    private DatabaseReference adicionaMsgExclusaoRef;
    private HashSet<Usuario> hashSetUsuario = new HashSet<>();
    private String conteudoAviso;
    private Boolean removerDespromover = false;

    private DatabaseReference novoFundadorRef = firebaseRef.child("comunidades");
    private String novoFundador;
    private Random random;
    private int index;

    private List<Usuario> listaFundadorAleatorio = new ArrayList<>();
    private DatabaseReference enviarConviteRef;
    private Convite convite = new Convite();

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(GerenciarUsersComunidadeActivity.this, DetalhesComunidadeActivity.class);
        intent.putExtra("comunidadeAtual", comunidade);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_users_grupo);
        inicializandoComponentes();
        setSupportActionBar(toolbarGerenciarComunidade);
        setTitle("");

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {

            if (dados.containsKey("comunidadeAtual")) {
                comunidade = (Comunidade) dados.getSerializable("comunidadeAtual");
            }

            if (dados.containsKey("tipoGerenciamento")) {
                tipoGerenciamento = dados.getString("tipoGerenciamento");
            }

            switch (tipoGerenciamento) {
                case "adicionar":
                    //ToastCustomizado.toastCustomizadoCurto("Adicionar", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    limiteSelecao = 40 - comunidade.getSeguidores().size();
                    break;
                case "remover":
                    //ToastCustomizado.toastCustomizadoCurto("Remover", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    limiteSelecao = listaParticipantes.size();
                    break;
                case "promover":
                    //ToastCustomizado.toastCustomizadoCurto("Promover", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    if (comunidade.getAdmsComunidade() != null) {
                        limiteSelecao = 5 - comunidade.getAdmsComunidade().size();
                    } else {
                        limiteSelecao = 5;
                    }
                    break;
                case "despromover":
                    //ToastCustomizado.toastCustomizadoCurto("Despromover", getApplicationContext());
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaAdms");
                    break;
                case "novoFundador":
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    limiteSelecao = 1;
                    break;
                case "novoFundadorAleatorio":
                    listaParticipantes = (List<Usuario>) dados.getSerializable("listaParticipantes");
                    listaParticipantes.remove(idUsuario);
                    random = new Random();
                    index = random.nextInt(listaParticipantes.size());
                    listaFundadorAleatorio.add(listaParticipantes.get(index));
                    listaParticipantes.clear();
                    listaParticipantes.addAll(listaFundadorAleatorio);
                    limiteSelecao = 1;
                    break;
            }


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewGerenciarComunidade.setLayoutManager(linearLayoutManager);
            recyclerViewGerenciarComunidade.setHasFixedSize(true);

            if (adapterGerenciarUsersComunidade != null) {

            } else {
                adapterGerenciarUsersComunidade = new AdapterGerenciarUsersComunidade(listaParticipantes, getApplicationContext(), txtViewLimiteGerenciamento, btnSalvarGerenciamento, limiteSelecao, tipoGerenciamento);
            }
            recyclerViewGerenciarComunidade.setAdapter(adapterGerenciarUsersComunidade);
            adapterGerenciarUsersComunidade.notifyDataSetChanged();

            clickListeners();

            btnSalvarGerenciamento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapterGerenciarUsersComunidade.participantesSelecionados() != null) {
                        if (adapterGerenciarUsersComunidade.participantesSelecionados().size() > 0) {
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
                                case "novoFundador":
                                case "novoFundadorAleatorio":
                                    salvarNovoFundador();
                                    break;
                            }
                            //ToastCustomizado.toastCustomizadoCurto("Selecionado " + adapterGerenciarUsersComunidade.participantesSelecionados().size(), getApplicationContext());
                        }
                    }
                }
            });
        }
    }

    private void clickListeners() {
        imgBtnBackGerenciarComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void adicionarUsuarios() {
        for (String idDestinatario : adapterGerenciarUsersComunidade.participantesSelecionados()) {
            enviarConviteRef = firebaseRef.child("convitesComunidade")
                    .child(idDestinatario).child(comunidade.getIdComunidade());

            String idRandomicoConvite = enviarConviteRef.push().getKey();
            convite.setIdConvite(idRandomicoConvite);
            convite.setIdDestinatario(idDestinatario);
            convite.setIdComunidade(comunidade.getIdComunidade());
            convite.setIdRemetente(idUsuario);
            HashMap<String, Object> timestampNow = new HashMap<>();
            timestampNow.put("timeStampConvite", ServerValue.TIMESTAMP);
            convite.setTimeStampConvite(timestampNow);
            enviarConviteRef.setValue(convite).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    ToastCustomizado.toastCustomizado("Convite enviado com sucesso",getApplicationContext());
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro ao enviar o convite " + e.getMessage(), getApplicationContext());
                    finish();
                }
            });
        }
    }

    private void removerUsuarios() {
        ArrayList<String> listaNova = new ArrayList<>();
        listaNova.addAll(comunidade.getSeguidores());
        for (String idRemovido : adapterGerenciarUsersComunidade.participantesSelecionados()) {
            listaNova.remove(idRemovido);
            //*salvarAviso(idRemovido, "remoção");

            if (comunidade.getAdmsComunidade() != null && comunidade.getAdmsComunidade().size() > 0) {
                if (comunidade.getAdmsComunidade().contains(idRemovido)) {
                    //Usuário removido também é adm
                    removerDespromover = true;
                }
            }
        }
        //ToastCustomizado.toastCustomizadoCurto("Lista atualizada - " + listaNova.size(), getApplicationContext());
        DatabaseReference adicionarUsuariosRef = firebaseRef.child("comunidades")
                .child(comunidade.getIdComunidade()).child("seguidores");
        adicionarUsuariosRef.setValue(listaNova).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (removerDespromover) {
                    removerDespromover = false;
                    despromoverUsuarios();
                } else {
                    finish();
                }
            }
        });
    }

    private void promoverUsuarios() {
        ArrayList<String> listaNova = new ArrayList<>();
        if (comunidade.getAdmsComunidade() != null) {
            if (comunidade.getAdmsComunidade().size() > 0) {
                listaNova.addAll(comunidade.getAdmsComunidade());
            }
        }
        listaNova.addAll(adapterGerenciarUsersComunidade.participantesSelecionados());

        for (String idPromovido : adapterGerenciarUsersComunidade.participantesSelecionados()) {
            //*salvarAviso(idPromovido, "promoção");
        }

        //ToastCustomizado.toastCustomizadoCurto("Lista atualizada - " + listaNova.size(), getApplicationContext());
        DatabaseReference adicionarUsuariosRef = firebaseRef.child("comunidades")
                .child(comunidade.getIdComunidade()).child("admsComunidade");
        adicionarUsuariosRef.setValue(listaNova).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                finish();
            }
        });
    }

    private void despromoverUsuarios() {

        ArrayList<String> listaNova = new ArrayList<>();
        listaNova.addAll(comunidade.getAdmsComunidade());
        for (String idDespromovido : adapterGerenciarUsersComunidade.participantesSelecionados()) {
            listaNova.remove(idDespromovido);
            //*salvarAviso(idDespromovido, "despromoção");
        }
        //ToastCustomizado.toastCustomizadoCurto("Lista atualizada - " + listaNova.size(), getApplicationContext());

        if (listaNova.size() > 0) {
            DatabaseReference adicionarUsuariosRef = firebaseRef.child("comunidades")
                    .child(comunidade.getIdComunidade()).child("admsComunidade");
            adicionarUsuariosRef.setValue(listaNova).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                }
            });
        } else {
            DatabaseReference adicionarUsuariosRef = firebaseRef.child("comunidades")
                    .child(comunidade.getIdComunidade()).child("admsComunidade");
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

                switch (tipoAviso) {
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
                    case "novoFundador":
                        conteudoAviso = nomeAfetado + " é o novo Fundador do comunidade " + "e " + nomeExecutor + " saiu do comunidade";
                        break;
                }

                /*
                adicionaMsgExclusaoRef = firebaseRef.child("conversas");

                idConversaComunidade = adicionaMsgExclusaoRef.push().getKey();

                HashMap<String, Object> dadosMensagem = new HashMap<>();
                dadosMensagem.put("idConversa", idConversaComunidade);
                dadosMensagem.put("exibirAviso", true);
                dadosMensagem.put("conteudoMensagem", conteudoAviso);

                adicionaMsgExclusaoRef = adicionaMsgExclusaoRef.child(comunidade.getIdComunidade())
                        .child(idConversaComunidade);

                adicionaMsgExclusaoRef.setValue(dadosMensagem);
                 */
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void salvarNovoFundador() {

        FirebaseRecuperarUsuario.recuperaComunidade(comunidade.getIdComunidade(), new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeFundador) {
                if (comunidadeFundador.getIdSuperAdmComunidade() != null) {

                    novoFundador = adapterGerenciarUsersComunidade.retornarIdNovoFundador();

                    novoFundadorRef = novoFundadorRef.child(comunidadeFundador.getIdComunidade());

                    removerAdmAnterior(comunidadeFundador, novoFundador);

                    novoFundadorRef.child("idSuperAdmComunidade").setValue(novoFundador).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            atualizarIdsMeusComunidades(comunidadeFundador);

                            atualizarIdsComunidadesNovoFundador(novoFundador, comunidadeFundador);

                            //*salvarAviso(novoFundador, "novoFundador");

                            sairDaComunidade(comunidadeFundador.getIdComunidade());
                        }
                    });
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void irParaTelaInicial() {
        Intent intent = new Intent(GerenciarUsersComunidadeActivity.this, NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
    }

    private void sairDaComunidade(String idComunidadeFundador) {

        DatabaseReference comunidadeAtualRef = firebaseRef.child("comunidades").child(idComunidadeFundador);

        ArrayList<String> listaUsuarioAtualRemovido = new ArrayList<>();

        FirebaseRecuperarUsuario.recuperaComunidade(idComunidadeFundador, new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtualizado) {
                if (comunidadeAtualizado.getSeguidores() != null
                        && comunidadeAtualizado.getSeguidores().size() > 0
                        && comunidadeAtualizado.getSeguidores().contains(idUsuario)) {
                    listaUsuarioAtualRemovido.clear();
                    listaUsuarioAtualRemovido.addAll(comunidadeAtualizado.getSeguidores());
                    listaUsuarioAtualRemovido.remove(idUsuario);

                    comunidadeAtualRef.child("seguidores").setValue(listaUsuarioAtualRemovido).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            if (comunidadeAtualizado.getAdmsComunidade() != null
                                    && comunidadeAtualizado.getAdmsComunidade().size() > 0
                                    && comunidadeAtualizado.getAdmsComunidade().contains(idUsuario)) {
                                listaUsuarioAtualRemovido.clear();
                                listaUsuarioAtualRemovido.addAll(comunidadeAtualizado.getAdmsComunidade());
                                listaUsuarioAtualRemovido.remove(idUsuario);
                                comunidadeAtualRef.child("admsComunidade").setValue(listaUsuarioAtualRemovido);
                            }
                            irParaTelaInicial();
                        }
                    });
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void removerAdmAnterior(Comunidade comunidadeFundador, String idNovoFundador) {

        ArrayList<String> novaListaAdmsComunidade = new ArrayList<>();

        if (comunidadeFundador.getAdmsComunidade() != null
                && comunidadeFundador.getAdmsComunidade().size() > 0
                && comunidadeFundador.getAdmsComunidade().contains(idNovoFundador)) {
            novaListaAdmsComunidade.clear();
            novaListaAdmsComunidade.addAll(comunidadeFundador.getAdmsComunidade());
            novaListaAdmsComunidade.remove(idNovoFundador);
            if (novaListaAdmsComunidade != null && novaListaAdmsComunidade.size() > 0) {
                novoFundadorRef.child("admsComunidade").setValue(novaListaAdmsComunidade);
            } else {
                novoFundadorRef.child("admsComunidade").removeValue();
            }
        }
    }

    private void atualizarIdsMeusComunidades(Comunidade comunidadeFundador) {

        ArrayList<String> novaListaIdsComunidade = new ArrayList<>();

        DatabaseReference usuarioComunidadeRef = firebaseRef.child("usuarios").child(idUsuario);

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdMinhasComunidades() != null &&
                        usuarioAtual.getIdMinhasComunidades().size() > 0) {
                    novaListaIdsComunidade.clear();
                    novaListaIdsComunidade.addAll(usuarioAtual.getIdMinhasComunidades());
                    if (novaListaIdsComunidade.contains(comunidadeFundador.getIdComunidade())) {
                        novaListaIdsComunidade.remove(comunidadeFundador.getIdComunidade());
                    }

                    if (novaListaIdsComunidade != null && novaListaIdsComunidade.size() > 0) {
                        usuarioComunidadeRef.child("idMinhasComunidades").setValue(novaListaIdsComunidade);
                    } else {
                        usuarioComunidadeRef.child("idMinhasComunidades").removeValue();
                    }
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void atualizarIdsComunidadesNovoFundador(String idNovoFundador, Comunidade comunidadeFundador) {

        ArrayList<String> novaListaIdsComunidade = new ArrayList<>();

        DatabaseReference novoFundadorComunidadeRef = firebaseRef.child("usuarios").child(idNovoFundador);

        FirebaseRecuperarUsuario.recuperaUsuario(idNovoFundador, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdMinhasComunidades() != null &&
                        usuarioAtual.getIdMinhasComunidades().size() > 0) {
                    novaListaIdsComunidade.clear();
                    novaListaIdsComunidade.addAll(usuarioAtual.getIdMinhasComunidades());
                    novaListaIdsComunidade.add(comunidadeFundador.getIdComunidade());
                    novoFundadorComunidadeRef.child("idMinhasComunidades").setValue(novaListaIdsComunidade);
                } else {
                    novaListaIdsComunidade.clear();
                    novaListaIdsComunidade.add(comunidadeFundador.getIdComunidade());
                    novoFundadorComunidadeRef.child("idMinhasComunidades").setValue(novaListaIdsComunidade);
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void inicializandoComponentes() {
        toolbarGerenciarComunidade = findViewById(R.id.toolbarGerenciarGrupo);
        imgBtnBackGerenciarComunidade = findViewById(R.id.imgBtnBackGerenciarGrupo);
        recyclerViewGerenciarComunidade = findViewById(R.id.recyclerViewGerenciarGrupo);
        txtViewLimiteGerenciamento = findViewById(R.id.txtViewLimiteGerenciamento);
        btnSalvarGerenciamento = findViewById(R.id.btnSalvarGerenciamento);
    }
}