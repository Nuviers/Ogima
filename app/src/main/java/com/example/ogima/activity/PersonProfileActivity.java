package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import com.example.ogima.activity.daily.DailyShortsActivity;
import com.example.ogima.adapter.AdapterGridPostagem;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.FriendsUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.SeguindoUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

    private ValueEventListener listenerBlock, listenerSeguindo, listenerFriend,
            listenerConvite;
    private DatabaseReference verificaBlockRef, verificaSeguindoRef,
            verificaAmizadeRef, verificaConviteRef;

    private DatabaseReference dadosUserAtualRef,
            dadosUserSelecionadoRef;

    private String idAlvo = null;


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

                        verificaVisualizacoes(new VerificarView() {
                            HashMap<String, Object> dadosView = new HashMap<>();

                            DatabaseReference salvarViewRef = firebaseRef
                                    .child("profileViews").child(idDonoDoPerfil)
                                    .child(idVisitante);

                            DatabaseReference salvarViewNoPerfilRef = firebaseRef.child("usuarios")
                                    .child(idDonoDoPerfil).child("viewsPerfil");

                            @Override
                            public void onSalvarView() {
                                dadosView.put("idUsuario", idVisitante);
                                dadosView.put("viewLiberada", false);
                                recuperarTimeStampNegativo(this);
                            }

                            @Override
                            public void onTimeStampRecuperado(long timeStamp, String dataFormatada) {
                                dadosView.put("timeStampView", timeStamp);
                                dadosView.put("dataView", dataFormatada);
                                salvarViewRef.setValue(dadosView).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        atualizarContador.acrescentarContador(salvarViewNoPerfilRef, new AtualizarContador.AtualizarContadorCallback() {
                                            @Override
                                            public void onSuccess(int contadorAtualizado) {
                                                salvarViewNoPerfilRef.setValue(contadorAtualizado);
                                            }

                                            @Override
                                            public void onError(String errorMessage) {

                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                        verificaBlock();
                        verificaSeguindo();
                        verificarRelacionamento();
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

        if (listenerSeguindo != null) {
            verificaSeguindoRef.removeEventListener(listenerSeguindo);
            listenerSeguindo = null;
        }

        if (listenerFriend != null) {
            verificaAmizadeRef.removeEventListener(listenerFriend);
            listenerFriend = null;
        }

        if (listenerConvite != null) {
            verificaConviteRef.removeEventListener(listenerConvite);
            listenerConvite = null;
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

    private interface VerificarView {
        void onSalvarView();

        void onTimeStampRecuperado(long timeStamp, String dataFormatada);

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

        btnSeguirUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tratarSeguindo();
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
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
                    nrSeguidores = snapshot.getChildrenCount();
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

    private void verificaVisualizacoes(VerificarView callback) {

        //Verifica se usuário visitante já visualizou o perfil selecionado.

        DatabaseReference verificaViewRef = firebaseRef
                .child("profileViews").child(idDonoDoPerfil)
                .child(idVisitante);

        verificaViewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Usuário atual já visualizou esse perfil
                } else {
                    //Salvar visualização.
                    callback.onSalvarView();
                }
                verificaViewRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
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
                    Intent intent = new Intent(getApplicationContext(), FollowersAndFollowingActivity.class);
                    intent.putExtra("tipoFragment", getString(R.string.followers));
                    intent.putExtra("idDonoPerfil", idDonoDoPerfil);
                    intent.putExtra("voltarParaProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "seguindo":
                if (existemSeguindo) {
                    Intent intent = new Intent(getApplicationContext(), FollowersAndFollowingActivity.class);
                    intent.putExtra("tipoFragment", getString(R.string.following));
                    intent.putExtra("idDonoPerfil", idDonoDoPerfil);
                    intent.putExtra("voltarParaProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "amigos":
                if (existemAmigos) {
                    Intent intent = new Intent(getApplicationContext(), FriendInteractionsActivity.class);
                    intent.putExtra("tipoFragment", getString(R.string.friends));
                    intent.putExtra("idDonoPerfil", idDonoDoPerfil);
                    intent.putExtra("voltarParaProfile", true);
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

    private void verificaSeguindo() {
        verificaSeguindoRef = firebaseRef.child("seguindo")
                .child(idVisitante).child(idDonoDoPerfil);

        listenerSeguindo = verificaSeguindoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    btnSeguirUsuario.setText("Parar de seguir");
                } else {
                    btnSeguirUsuario.setText("Seguir");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void tratarSeguindo() {
        DatabaseReference statusSeguindoRef = firebaseRef.child("seguindo")
                .child(idVisitante).child(idDonoDoPerfil);

        statusSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    calcularSeguidor("remover");
                } else {
                    calcularSeguidor("adicionar");
                }
                statusSeguindoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calcularSeguidor(String sinalizador) {

        if (sinalizador.equals("adicionar")) {

            ToastCustomizado.toastCustomizadoCurto("Adicionar", getApplicationContext());

            SeguindoUtils.salvarSeguindo(getApplicationContext(), idDonoDoPerfil, new SeguindoUtils.SalvarSeguindoCallback() {
                @Override
                public void onSeguindoSalvo() {
                    ToastCustomizado.toastCustomizadoCurto("Seguindo com sucesso", getApplicationContext());
                }

                @Override
                public void onError(@NonNull String message) {

                }
            });

            DatabaseReference atualizarSeguidoresRef
                    = firebaseRef.child("usuarios")
                    .child(idDonoDoPerfil).child("seguidoresUsuario");

            DatabaseReference atualizarSeguindoRef
                    = firebaseRef.child("usuarios")
                    .child(idVisitante).child("seguindoUsuario");

            atualizarContador.acrescentarContador(atualizarSeguidoresRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    atualizarSeguidoresRef.setValue(contadorAtualizado);
                    txtViewNrSeguidores.setText(String.valueOf(contadorAtualizado));
                }

                @Override
                public void onError(String errorMessage) {

                }
            });

            atualizarContador.acrescentarContador(atualizarSeguindoRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    atualizarSeguindoRef.setValue(contadorAtualizado);
                }

                @Override
                public void onError(String errorMessage) {

                }
            });
        }

        if (sinalizador.equals("remover")) {

            SeguindoUtils.removerSeguindo(idDonoDoPerfil, new SeguindoUtils.RemoverSeguindoCallback() {
                @Override
                public void onRemovido() {
                    ToastCustomizado.toastCustomizadoCurto("Deixou de seguir com sucesso", getApplicationContext());
                }

                @Override
                public void onError(@NonNull String message) {

                }
            });

            DatabaseReference atualizarSeguidoresRef
                    = firebaseRef.child("usuarios")
                    .child(idDonoDoPerfil).child("seguidoresUsuario");

            DatabaseReference atualizarSeguindoRef
                    = firebaseRef.child("usuarios")
                    .child(idVisitante).child("seguindoUsuario");

            atualizarContador.subtrairContador(atualizarSeguidoresRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    atualizarSeguidoresRef.setValue(contadorAtualizado);
                    txtViewNrSeguidores.setText(String.valueOf(contadorAtualizado));
                }

                @Override
                public void onError(String errorMessage) {

                }
            });

            atualizarContador.subtrairContador(atualizarSeguindoRef, new AtualizarContador.AtualizarContadorCallback() {
                @Override
                public void onSuccess(int contadorAtualizado) {
                    atualizarSeguindoRef.setValue(contadorAtualizado);
                }

                @Override
                public void onError(String errorMessage) {

                }
            });
        }
    }


    private void denunciarUsuario() {
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

    private void verificarRelacionamento() {

        //Verifica se são amigos ou não
        verificaAmizadeRef = firebaseRef.child("friends").child(idVisitante)
                .child(idDonoDoPerfil);

        listenerFriend = verificaAmizadeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //São amigos
                    ocultarTodosImgBtnAmizade(imgButtonDeleteFriend);
                    imgButtonDeleteFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Remover amigo
                            desfazerAmizade();
                        }
                    });
                } else {
                    //Não são amigos

                    //Verifica se existe convite de amizade entre eles
                    verificaConvite();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void verificaConvite() {

        //Verificar se o usuário atual é o remetente do convite, se for exibe só o relógio
        //se não for irá ser exibido o button de remover o convite.

        verificaConviteRef = firebaseRef.child("requestsFriendship").child(idVisitante)
                .child(idDonoDoPerfil);

        listenerConvite = verificaConviteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Existe convite de amizade
                    Usuario usuarioConvite = snapshot.getValue(Usuario.class);

                    if (usuarioConvite.getIdRemetente() != null
                            && !usuarioConvite.getIdRemetente().isEmpty()) {
                        if (usuarioConvite.getIdRemetente().equals(idVisitante)) {
                            //Usuário atual é remetente
                            ocultarTodosImgBtnAmizade(imgButtonPendingFriend);
                        } else {
                            //Usuário atual é o destinatário.
                            ocultarTodosImgBtnAmizade(imgButtonAcceptRequest);
                            imgButtonAcceptRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    aceitarAmizade();
                                }
                            });
                        }
                    }
                } else {
                    //Não existe convite de amizade
                    ocultarTodosImgBtnAmizade(imgButtonAddFriend);
                    imgButtonAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            enviarConvite();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void desfazerAmizade() {

        dadosUserAtualRef = null;
        dadosUserSelecionadoRef = null;
        idAlvo = null;

        dadosUserAtualRef = firebaseRef.child("usuarios")
                .child(idVisitante);
        dadosUserSelecionadoRef = firebaseRef.child("usuarios")
                .child(idDonoDoPerfil);
        idAlvo = idDonoDoPerfil;

        FriendsUtils.desfazerAmizade(getApplicationContext(), idAlvo, new FriendsUtils.DesfazerAmizadeCallback() {
            @Override
            public void onAmizadeDesfeita() {
                ToastCustomizado.toastCustomizadoCurto("Amizade desfeita com sucesso", getApplicationContext());
            }

            @Override
            public void onError(@NonNull String message) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao desfazer amizade, tente novamente mais tarde", getApplicationContext());
            }
        });
    }

    private void enviarConvite() {

        dadosUserSelecionadoRef = null;

        FriendsUtils.enviarConvite(getApplicationContext(), idDonoDoPerfil, new FriendsUtils.EnviarConviteCallback() {
            @Override
            public void onConviteEnviado() {
                ToastCustomizado.toastCustomizadoCurto("Convite de amizade enviado com sucesso", getApplicationContext());
            }

            @Override
            public void onJaExisteConvite() {
                tratarConvite();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao enviar o convite de amizade, tente novamente mais tarde", getApplicationContext());
            }
        });
    }

    private void tratarConvite() {
        FriendsUtils.VerificaConvite(idDonoDoPerfil, new FriendsUtils.VerificaConviteCallback() {
            @Override
            public void onConvitePendente(boolean destinatario) {
                if (destinatario) {
                    FriendsUtils.adicionarAmigo(getApplicationContext(), idDonoDoPerfil, false, new FriendsUtils.AdicionarAmigoCallback() {
                        @Override
                        public void onConcluido() {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.now_you_are_friends), getApplicationContext());
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_adding_friend, message), getApplicationContext());
                        }
                    });
                } else {
                    //Usuário atual já enviou um convite de amizade anteriormente
                    //não fazer nada além de mostrar um aviso.
                    ToastCustomizado.toastCustomizadoCurto("Convite de amizade já existe", getApplicationContext());
                }
            }

            @Override
            public void onSemConvites() {
                enviarConvite();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void aceitarAmizade() {
        verificaAmizadeRef = null;
        //new
        dadosUserSelecionadoRef = null;
        dadosUserAtualRef = null;
        //
        FriendsUtils.adicionarAmigo(getApplicationContext(), idDonoDoPerfil, false, new FriendsUtils.AdicionarAmigoCallback() {
            @Override
            public void onConcluido() {
                ToastCustomizado.toastCustomizadoCurto("Adicionado com sucesso", getApplicationContext());
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), getApplicationContext());
            }
        });
    }

    private void ocultarTodosImgBtnAmizade(ImageButton imageButton) {
        //Amizade
        imgButtonAddFriend.setVisibility(View.GONE);
        imgButtonDeleteFriend.setVisibility(View.GONE);
        //Convite
        imgButtonAcceptRequest.setVisibility(View.GONE);
        imgButtonRemoveRequest.setVisibility(View.GONE);
        imgButtonPendingFriend.setVisibility(View.GONE);

        //Exibir imageButton desejado
        imageButton.setVisibility(View.VISIBLE);
    }

    private void recuperarTimeStampNegativo(VerificarView callback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(this, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        //ToastCustomizado.toastCustomizadoCurto("TIMESTAMP: " + timeStampNegativo, getApplicationContext());

                        callback.onTimeStampRecuperado(timestampNegativo, dataFormatada);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastCustomizado.toastCustomizadoCurto("A connection error occurred: " + errorMessage, getApplicationContext());
                        callback.onError(errorMessage);
                    }
                });
            }
        });
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