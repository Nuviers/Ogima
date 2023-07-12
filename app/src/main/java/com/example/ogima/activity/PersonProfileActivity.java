package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.PopupMenu;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterGridPostagem;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersonProfileActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idVisitante;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar, txtViewTituloFotos;
    private ImageView imgViewFundoProfile, imgViewFotoProfile, imgViewDailyShortInc;
    private TextView txtViewNameProfile, txtViewTitleSeguidores,
            txtViewTitleAmigos, txtViewTitleSeguindo, txtViewNrSeguidores,
            txtViewNrAmigos, txtViewNrSeguindo,
            txtViewMsgSemFotos, txtViewSemPostagemMsg,
            txtViewTitlePostagem;
    private View viewBarraFundo;
    private RecyclerView recyclerViewFotos, recyclerViewPostagens;
    private ImageButton imgBtnOpcoesBlock, imgButtonAddFriend,
            imgButtonDeleteFriend, imgButtonPendingFriend,
            imgButtonRemoveRequest, imgButtonAcceptRequest,
            imgButtonIniciarConversa;
    private Button btnSeguirUsuario, btnVerFotos, btnVerPostagens;

    private String idDonoDoPerfil = null;

    private boolean possuiEpilepsia = true;
    private boolean atualizarDados = true;
    private long nrSeguidores = 0;
    private boolean existemSeguidores = false;
    private boolean existemAmigos = false;
    private boolean existemSeguindo = false;
    private boolean existemSolicitacoes = false;
    private boolean existemVisualizacoes = false;
    private AtualizarContador atualizarContador = new AtualizarContador();

    private GridLayoutManager gridLayoutManagerFoto;
    private GridLayoutManager gridLayoutManagerPostagem;
    private AdapterGridPostagem adapterGridFoto;
    private AdapterGridPostagem adapterGridPostagem;
    private List<Postagem> listaFotos = new ArrayList<>();
    private List<Postagem> listaPostagens = new ArrayList<>();
    private PopupMenu popupMenu;
    private MenuItem bedMenuItem;
    private boolean bloquearUsuario = false;

    private ValueEventListener listenerBlock;
    private DatabaseReference verificaBlockRef;

    @Override
    protected void onStart() {
        super.onStart();

        if (atualizarDados) {

            Bundle dados = getIntent().getExtras();

            if (dados != null) {
                if (dados.containsKey("idDonoPerfil")) {
                    idDonoDoPerfil = dados.getString("idDonoPerfil");
                }
            } else {
                finish();
            }

            if (idDonoDoPerfil != null && !idDonoDoPerfil.isEmpty()) {
                recuperarDadosVisitante(new DadosVisitante() {
                    @Override
                    public void onRecuperado() {
                        recuperarDadosDonoPerfil(this);
                        recuperarNrSeguidores();
                        verificaVisualizacoes();
                        verificaBlock();
                    }

                    @Override
                    public void onSemDados() {
                        ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar os dados do usuário selecionado", getApplicationContext());
                        finish();
                    }

                    @Override
                    public void onExibirPostagensFotos() {
                        configRecyclers();
                        recuperarFotos();
                        recuperarPostagens();
                    }

                    @Override
                    public void onPostagensPrivadas(String privacidadePostagem) {
                        mudarParametroLayoutFoto(true);
                        mudarParametroLayoutPostagem(true);

                        txtViewTituloFotos.setText("Postagens e fotos exclusivas" +
                                " para: " + privacidadePostagem);
                        txtViewSemPostagemMsg.setVisibility(View.GONE);
                        txtViewMsgSemFotos.setVisibility(View.GONE);
                        txtViewTitlePostagem.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizado("Ocorreu um erro ao recuperar os dados do usuário selecionado", getApplicationContext());
                        finish();
                    }
                });
            }

            atualizarDados = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listenerBlock != null) {
            verificaBlockRef.removeEventListener(listenerBlock);
            listenerBlock = null;
        }
    }

    public PersonProfileActivity() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idVisitante = Base64Custom.codificarBase64(emailUsuario);
    }

    private interface DadosVisitante {
        void onRecuperado();

        void onSemDados();

        void onExibirPostagensFotos();

        void onPostagensPrivadas(String tipoPrivacidade);

        void onError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");

        //Inicializando componentes
        inicializandoComponentes();

        clickListeners();
    }

    private void clickListeners() {
        txtViewTitleSeguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("seguidores");
            }
        });

        txtViewTitleAmigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("amigos");
            }
        });

        txtViewTitleSeguindo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("seguindo");
            }
        });

        txtViewNrSeguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("seguidores");
            }
        });

        txtViewNrAmigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("amigos");
            }
        });

        txtViewNrSeguindo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("seguindo");
            }
        });

        imgViewDailyShortInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verDailyShort();
            }
        });

        btnVerFotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listaFotos != null
                        && listaFotos.size() > 0) {
                    verTodasFotos();
                }
            }
        });

        btnVerPostagens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listaPostagens != null
                        && listaPostagens.size() > 0) {
                    verTodasPostagens();
                }
            }
        });

        imgBtnOpcoesBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });
    }

    private void recuperarDadosVisitante(DadosVisitante callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idVisitante, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                possuiEpilepsia = epilepsia;
                callback.onRecuperado();
            }

            @Override
            public void onSemDados() {
                callback.onSemDados();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private void recuperarDadosDonoPerfil(DadosVisitante callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idDonoDoPerfil, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {

                if (nomeUsuarioAjustado != null) {
                    txtViewNameProfile.setText(nomeUsuarioAjustado);
                    txtViewIncTituloToolbar.setText(nomeUsuarioAjustado);
                }

                if (listaIdAmigos != null && listaIdAmigos.size() > 0) {
                    txtViewNrAmigos.setText(String.valueOf(listaIdAmigos.size()));
                    existemAmigos = true;
                } else {
                    txtViewNrAmigos.setText("0");
                    existemAmigos = false;
                }

                if (listaIdSeguindo != null && listaIdSeguindo.size() > 0) {
                    txtViewNrSeguindo.setText(String.valueOf(listaIdSeguindo.size()));
                    existemSeguindo = true;
                } else {
                    txtViewNrSeguindo.setText("0");
                    existemSeguindo = false;
                }

                if (usuarioAtual.getUrlLastDaily() != null
                        && !usuarioAtual.getUrlLastDaily().isEmpty()) {
                    exibirUltimoDaily(usuarioAtual.getUrlLastDaily());
                }

                if (usuarioAtual.getPrivacidadePostagens() != null
                        && !usuarioAtual.getPrivacidadePostagens().isEmpty()) {
                    verificaPrivacidadePostagens(usuarioAtual.getPrivacidadePostagens(), callback);
                } else {
                    callback.onExibirPostagensFotos();
                }

                imgButtonIniciarConversa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ConversaActivity.class);
                        intent.putExtra("usuario", usuarioAtual);
                        startActivity(intent);
                        //finish();
                    }
                });

                exibirFotoFundo(fotoUsuario, fundoUsuario);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }


    private void exibirFotoFundo(String fotoUsuario, String fundoUsuario) {

        boolean fotoExistente = false;
        boolean fundoExistente = false;

        if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
            fotoExistente = true;
        }

        if (fundoUsuario != null && !fundoUsuario.isEmpty()) {
            fundoExistente = true;
        }

        if (possuiEpilepsia) {

            if (fotoExistente) {
                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(),
                        fotoUsuario, imgViewFotoProfile, android.R.color.transparent);
            }

            if (fundoExistente) {
                GlideCustomizado.montarGlideFotoEpilepsia(getApplicationContext(),
                        fundoUsuario, imgViewFundoProfile, android.R.color.transparent);
            }

        } else {

            if (fotoExistente) {
                GlideCustomizado.montarGlide(getApplicationContext(),
                        fotoUsuario, imgViewFotoProfile, android.R.color.transparent);
            }

            if (fundoExistente) {
                GlideCustomizado.montarGlideFoto(getApplicationContext(),
                        fundoUsuario, imgViewFundoProfile, android.R.color.transparent);
            }
        }
    }

    private void recuperarNrSeguidores() {
        DatabaseReference verificaSeguidoresRef = firebaseRef.child("seguidores")
                .child(idDonoDoPerfil);

        verificaSeguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        nrSeguidores = snapshot1.getChildrenCount();
                    }
                    txtViewNrSeguidores.setText(String.valueOf(nrSeguidores));
                    existemSeguidores = true;
                } else {
                    existemSeguidores = false;
                }
                verificaSeguidoresRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void verificaVisualizacoes() {
        DatabaseReference viewsRef = firebaseRef.child("profileViews")
                .child(idDonoDoPerfil).child(idVisitante)
                .child("idUsuario");

        DatabaseReference salvarViewNoPerfilRef = firebaseRef.child("usuarios")
                .child(idDonoDoPerfil).child("viewsPerfil");

        viewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Já visualizou o perfil atual.
                } else {
                    //Salvar visualização.
                    viewsRef.setValue(idVisitante).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            atualizarContador.acrescentarContador(salvarViewNoPerfilRef, new AtualizarContador.AtualizarContadorCallback() {
                                @Override
                                public void onSuccess(int contadorAtualizado) {
                                    salvarViewNoPerfilRef.setValue(contadorAtualizado);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    verificaVisualizacoes();
                                }
                            });
                        }
                    });
                }
                viewsRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exibirUltimoDaily(String urlLastDaily) {
        if (possuiEpilepsia) {
            GlideCustomizado.montarGlideEpilepsia(getApplicationContext(),
                    urlLastDaily, imgViewDailyShortInc, android.R.color.transparent);
        } else {
            GlideCustomizado.montarGlide(getApplicationContext(),
                    urlLastDaily, imgViewDailyShortInc, android.R.color.transparent);
        }
    }

    private void irParaRelacoes(String destino) {
        switch (destino) {
            case "seguidores":
                if (existemSeguidores) {
                    Intent intent = new Intent(PersonProfileActivity.this, SeguidoresActivity.class);
                    intent.putExtra("exibirSeguidores", "exibirSeguidores");
                    intent.putExtra("idDonoPerfil", idDonoDoPerfil);
                    //*intent.putExtra("irParaProfile", "irParaProfile");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "seguindo":
                if (existemSeguindo) {
                    Intent intent = new Intent(PersonProfileActivity.this, SeguidoresActivity.class);
                    intent.putExtra("exibirSeguindo", "exibirSeguindo");
                    intent.putExtra("idDonoPerfil", idDonoDoPerfil);
                    //*intent.putExtra("irParaProfile", "irParaProfile");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "amigos":
                if (existemAmigos) {
                    Intent intent = new Intent(PersonProfileActivity.this, FriendshipInteractionsInicioActivity.class);
                    intent.putExtra("fragmentEscolhido", "exibirAmigos");
                    intent.putExtra("idDonoPerfil", idDonoDoPerfil);
                    //*intent.putExtra("irParaProfile", "irParaProfile");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
        }
    }

    private void verDailyShort() {
        if (idVisitante != null && !idVisitante.isEmpty()) {
            Intent intent = new Intent(PersonProfileActivity.this, DailyShortsActivity.class);
            //intent.putExtra("irParaProfile", "irParaProfile");
            intent.putExtra("idUsuarioDaily", idDonoDoPerfil);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void verificaPrivacidadePostagens(String privacidadePostagem, DadosVisitante callback) {
        if (privacidadePostagem != null
                && !privacidadePostagem.isEmpty()) {

            DatabaseReference verificaAmizadeRef = firebaseRef
                    .child("friends").child(idVisitante).child(idDonoDoPerfil);

            DatabaseReference verificaSeguindoRef = firebaseRef
                    .child("seguindo").child(idVisitante).child(idDonoDoPerfil);

            switch (privacidadePostagem) {
                case "Todos":
                    callback.onExibirPostagensFotos();
                    break;
                case "Somente amigos e seguidores":

                    verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                callback.onExibirPostagensFotos();
                            } else {
                                verificaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            callback.onExibirPostagensFotos();
                                        } else {
                                            callback.onPostagensPrivadas(privacidadePostagem);
                                        }
                                        verificaSeguindoRef.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            verificaAmizadeRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    break;
                case "Somente amigos":
                    verificaAmizadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                callback.onExibirPostagensFotos();
                            } else {
                                callback.onPostagensPrivadas(privacidadePostagem);
                            }
                            verificaAmizadeRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    break;
                case "Somente seguidores":
                    verificaSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                callback.onExibirPostagensFotos();
                            } else {
                                callback.onPostagensPrivadas(privacidadePostagem);
                            }
                            verificaSeguindoRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    break;
            }
        }
    }

    private void configRecyclers() {

        //Fotos
        if (gridLayoutManagerFoto == null) {
            gridLayoutManagerFoto = new GridLayoutManager(getApplicationContext(), 2);
        }

        recyclerViewFotos.setHasFixedSize(true);
        recyclerViewFotos.setLayoutManager(gridLayoutManagerFoto);

        if (adapterGridFoto == null) {
            adapterGridFoto = new AdapterGridPostagem(listaFotos, getApplicationContext(), true);
        }

        recyclerViewFotos.setAdapter(adapterGridFoto);

        //Postagens
        if (gridLayoutManagerPostagem == null) {
            gridLayoutManagerPostagem = new GridLayoutManager(getApplicationContext(), 2);
        }

        recyclerViewPostagens.setHasFixedSize(true);
        recyclerViewPostagens.setLayoutManager(gridLayoutManagerPostagem);

        if (adapterGridPostagem == null) {
            adapterGridPostagem = new AdapterGridPostagem(listaPostagens, getApplicationContext(), false);
        }
        recyclerViewPostagens.setAdapter(adapterGridPostagem);
    }

    private void recuperarFotos() {
        Query recuperarFotos = firebaseRef.child("fotos")
                .child(idDonoDoPerfil).orderByChild("timeStampNegativo")
                .limitToFirst(4);

        recuperarFotos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Postagem fotoPostagem = snapshot1.getValue(Postagem.class);
                        adicionarFotoNaLista(fotoPostagem);
                    }
                    mudarParametroLayoutFoto(false);
                    adapterGridFoto.notifyDataSetChanged();
                } else {
                    mudarParametroLayoutFoto(false);
                }
                recuperarFotos.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarPostagens() {
        Query recuperarPostagens = firebaseRef.child("postagens")
                .child(idDonoDoPerfil).orderByChild("timeStampNegativo")
                .limitToFirst(4);

        recuperarPostagens.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Postagem postagem = snapshot1.getValue(Postagem.class);
                        adicionarPostagemNaLista(postagem);
                    }
                    mudarParametroLayoutPostagem(false);
                    adapterGridPostagem.notifyDataSetChanged();
                } else {
                    mudarParametroLayoutPostagem(false);
                }
                recuperarPostagens.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void adicionarFotoNaLista(Postagem fotoPostagem) {
        if (listaFotos != null && listaFotos.size() > 0
                && listaFotos.contains(fotoPostagem)) {
            return;
        }

        listaFotos.add(fotoPostagem);
    }

    private void adicionarPostagemNaLista(Postagem newPostagem) {

        if (listaPostagens != null && listaPostagens.size() == 4) {
            return;
        }

        if (listaPostagens != null && listaPostagens.size() > 0
                && listaPostagens.contains(newPostagem)) {
            return;
        }

        listaPostagens.add(newPostagem);
    }

    private void mudarParametroLayoutFoto(boolean postagemPrivada) {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtViewTitlePostagem.getLayoutParams();

        if (listaFotos != null && listaFotos.size() > 0) {
            // Define uma nova regra de layout_below com um novo componente
            params.addRule(RelativeLayout.BELOW, R.id.recyclerViewFotosProfile);

            // Aplica as alterações nos parâmetros de layout
            txtViewTitlePostagem.setLayoutParams(params);
            recyclerViewFotos.setVisibility(View.VISIBLE);
            txtViewMsgSemFotos.setVisibility(View.INVISIBLE);
            btnVerFotos.setVisibility(View.VISIBLE);

        } else {

            // Define uma nova regra de layout_below com um novo componente
            params.addRule(RelativeLayout.BELOW, R.id.txtViewMsgSemFotos);

            // Aplica as alterações nos parâmetros de layout
            txtViewTitlePostagem.setLayoutParams(params);
            recyclerViewFotos.setVisibility(View.GONE);
            txtViewMsgSemFotos.setVisibility(View.VISIBLE);
            btnVerFotos.setVisibility(View.GONE);
        }
    }

    private void mudarParametroLayoutPostagem(boolean postagemPrivada) {
        if (listaPostagens != null && listaPostagens.size() > 0) {
            txtViewSemPostagemMsg.setVisibility(View.INVISIBLE);
            recyclerViewPostagens.setVisibility(View.VISIBLE);
            btnVerPostagens.setVisibility(View.VISIBLE);
        } else {
            txtViewSemPostagemMsg.setVisibility(View.VISIBLE);
            recyclerViewPostagens.setVisibility(View.GONE);
            btnVerPostagens.setVisibility(View.GONE);
        }
    }

    private void verTodasFotos() {
        Intent intent = new Intent(PersonProfileActivity.this, FotosPostadasActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("idDonoPerfil", idDonoDoPerfil);
        //*intent.putExtra("irParaProfile", "irParaProfile");
        startActivity(intent);
    }

    private void verTodasPostagens() {
        Intent intent = new Intent(PersonProfileActivity.this, DetalhesPostagemActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("idDonoPerfil", idDonoDoPerfil);
        //*intent.putExtra("irParaProfile", "irParaProfile");
        startActivity(intent);
    }

    private void verificaBlock() {

        bloquearUsuario = false;

        popupMenu = new PopupMenu(getApplicationContext(), imgBtnOpcoesBlock);

        popupMenu.getMenuInflater().inflate(R.menu.popup_block, popupMenu.getMenu());

        bedMenuItem = popupMenu.getMenu().findItem(R.id.blockUser);

        verificaBlockRef = firebaseRef.child("blockUser")
                .child(idDonoDoPerfil).child(idVisitante);

        listenerBlock = verificaBlockRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    bedMenuItem.setTitle("Desbloquear usuário");
                    bloquearUsuario = false;
                } else {
                    bedMenuItem.setTitle("Bloquear usuário");
                    bloquearUsuario = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.blockUser:
                        tratarBlock(bloquearUsuario);
                        ToastCustomizado.toastCustomizadoCurto("Block", getApplicationContext());
                        break;
                    case R.id.denunciaBlockUser:
                        ToastCustomizado.toastCustomizadoCurto("Denunciar", getApplicationContext());
                        denunciarUsuario();
                        break;
                }
                return false;
            }
        });
    }

    private void tratarBlock(boolean bloquear) {
        DatabaseReference salvarBlockRef = firebaseRef.child("blockUser")
                .child(idDonoDoPerfil).child(idVisitante);

        if (bloquear) {
            ToastCustomizado.toastCustomizadoCurto("Bloquear", getApplicationContext());


            //Salvando dados do block
            HashMap<String, Object> dadosBlock = new HashMap<>();
            dadosBlock.put("idUsuario", idDonoDoPerfil);
            salvarBlockRef.setValue(dadosBlock).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        ToastCustomizado.toastCustomizadoCurto("Usuário bloqueado com sucesso", getApplicationContext());
                        bloquearUsuario = false;
                    } else {
                        ToastCustomizado.toastCustomizadoCurto("Erro ao bloquear usuário, tente novamente", getApplicationContext());
                        bloquearUsuario = true;
                    }
                }
            });

        } else {
            ToastCustomizado.toastCustomizadoCurto("Desbloquear", getApplicationContext());
            salvarBlockRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        ToastCustomizado.toastCustomizadoCurto("Usuário desbloqueado com sucesso", getApplicationContext());
                        bloquearUsuario = true;
                    } else {
                        ToastCustomizado.toastCustomizadoCurto("Erro ao desbloquear usuário, tente novamente", getApplicationContext());
                        bloquearUsuario = false;
                    }
                }
            });
        }
    }

    private void denunciarUsuario(){
        //Será necessário limitar essa função
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Denúncia - " + "Informe o nome do usuário denunciado");
        intent.putExtra(Intent.EXTRA_TEXT, "Descreva sua denúncia nesse campo e anexe as provas no email.");
        try {
            startActivity(Intent.createChooser(intent, "Selecione seu app de envio de email."));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void inicializandoComponentes() {
        //inc_perfil_cabecalho
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);

        //inc_perfil_cabecalho
        imgViewFundoProfile = findViewById(R.id.imgViewIncFundoProfile);
        imgViewFotoProfile = findViewById(R.id.imgViewIncFotoProfile);
        txtViewNameProfile = findViewById(R.id.txtViewNameProfile);

        //inc_daily_short
        imgViewDailyShortInc = findViewById(R.id.imgViewDailyShortInc);

        //inc_perfil_card_relacoes
        txtViewTitleSeguidores = findViewById(R.id.txtViewTitleSeguidores);
        txtViewTitleAmigos = findViewById(R.id.txtViewTitleAmigos);
        txtViewTitleSeguindo = findViewById(R.id.txtViewTitleSeguindo);
        txtViewNrSeguidores = findViewById(R.id.txtViewNrSeguidores);
        txtViewNrAmigos = findViewById(R.id.txtViewNrAmigos);
        txtViewNrSeguindo = findViewById(R.id.txtViewNrSeguindo);

        //layout
        viewBarraFundo = findViewById(R.id.viewBarraFundo);
        recyclerViewFotos = findViewById(R.id.recyclerViewFotosProfile);
        recyclerViewPostagens = findViewById(R.id.recyclerViewPostagensProfile);
        txtViewMsgSemFotos = findViewById(R.id.txtViewMsgSemFotos);
        txtViewSemPostagemMsg = findViewById(R.id.txtViewSemPostagemMsg);
        txtViewTitlePostagem = findViewById(R.id.txtViewTitlePostagem);
        imgBtnOpcoesBlock = findViewById(R.id.imgBtnOpcoesBlock);
        imgButtonAddFriend = findViewById(R.id.imgButtonAddFriend);
        imgButtonDeleteFriend = findViewById(R.id.imgButtonDeleteFriend);
        imgButtonPendingFriend = findViewById(R.id.imgButtonPendingFriend);
        imgButtonRemoveRequest = findViewById(R.id.imgButtonRemoveRequest);
        imgButtonAcceptRequest = findViewById(R.id.imgButtonAcceptRequest);
        imgButtonIniciarConversa = findViewById(R.id.imgButtonIniciarConversa);
        btnSeguirUsuario = findViewById(R.id.btnSeguirUsuario);
        btnVerFotos = findViewById(R.id.btnVerFotosPerson);
        btnVerPostagens = findViewById(R.id.btnVerPostagensPerson);
        txtViewTituloFotos = findViewById(R.id.txtViewTituloFotosProfile);
    }
}