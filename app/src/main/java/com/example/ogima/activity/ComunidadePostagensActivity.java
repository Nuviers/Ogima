package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterMinhasComunidades;
import com.example.ogima.adapter.AdapterPostagens;
import com.example.ogima.adapter.AdapterPostagensComunidade;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PostagemDiffDAO;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComunidadePostagensActivity extends AppCompatActivity implements View.OnClickListener, AdapterPostagensComunidade.RecuperaPosicaoAnterior, AdapterPostagensComunidade.RemoverPostagemListener {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    //Componentes - inc_cabecalho_user
    private ImageView imgViewIncFotoUser, imgViewIncFundoUser;
    private View viewIncBackOpcoes;
    private TextView txtViewIncNomeUser;

    //Componentes - inc_toolbar_padrao
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private TextView txtViewIncTituloToolbar;


    private ImageButton imgBtnParticipantes;
    private TextView txtViewNrParticipantes;
    private Button btnViewEntrarComunidade;
    private LinearLayout linearLayoutTopicos;

    //Dados para postagens da comunidade
    private ImageButton imgBtnIncOpcoes;
    private String idComunidade;
    private RecyclerView recyclerViewPostagensComunidade;

    private LinearLayoutManager linearLayoutManagerComunidade;

    private List<Postagem> listaPostagens = new ArrayList<>();
    private AdapterPostagensComunidade adapterPostagens;

    //config fab
    private FloatingActionButton fabVideoComunidadePostagem, fabGaleriaComunidadePostagem,
            fabGifComunidadePostagem, fabTextComunidadePostagem;
    private ImageButton imgBtnOpcoesPostagem;
    private Float translationY = 100f;
    private Boolean isMenuOpen = false;
    private OvershootInterpolator interpolator = new OvershootInterpolator();
    private ProgressBar progressBarComunidadePostagem;

    //Retorna para posição anterior
    private int mCurrentPosition = 0;
    private PostagemDiffDAO postagemDiffDAO;


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPosition = savedInstanceState.getInt("current_position");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //*configRecyclerView();
        infoComunidade();
        testeMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 && mCurrentPosition > 0) {
            recyclerViewPostagensComunidade.scrollToPosition(mCurrentPosition);
            mCurrentPosition = 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunidade_postagens);
        inicializandoComponentes();
        setSupportActionBar(toolbarIncPadrao);
        setTitle("");
        txtViewIncTituloToolbar.setText("Postagens da comunidade");

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null && dados.containsKey("idComunidade")) {
            idComunidade = dados.getString("idComunidade");
        }
        configRecyclerView();

        postagemDiffDAO = new PostagemDiffDAO(listaPostagens, adapterPostagens);
    }


    private void configRecyclerView() {
        //Configuração do recycler de comunidades
        if (linearLayoutManagerComunidade != null) {

        } else {
            linearLayoutManagerComunidade = new LinearLayoutManager(getApplicationContext());
            linearLayoutManagerComunidade.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerViewPostagensComunidade.setHasFixedSize(true);
        recyclerViewPostagensComunidade.setLayoutManager(linearLayoutManagerComunidade);

        if (adapterPostagens != null) {

        } else {
            adapterPostagens = new AdapterPostagensComunidade(listaPostagens, getApplicationContext(), this::onComunidadeRemocao, this::onPosicaoAnterior);
        }
        recyclerViewPostagensComunidade.setAdapter(adapterPostagens);

    }

    private void infoComunidade() {
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {

                FirebaseRecuperarUsuario.recuperaComunidade(idComunidade, new FirebaseRecuperarUsuario.RecuperaComunidadeCallback() {
                    @Override
                    public void onComunidadeRecuperada(Comunidade comunidadeAtual) {

                        if (epilepsia) {
                            GlideCustomizado.montarGlideEpilepsia(getApplicationContext(),
                                    comunidadeAtual.getFotoComunidade(), imgViewIncFotoUser, android.R.color.transparent);

                            GlideCustomizado.montarGlideFotoEpilepsia(getApplicationContext(),
                                    comunidadeAtual.getFundoComunidade(), imgViewIncFundoUser, android.R.color.transparent);
                        } else {
                            GlideCustomizado.montarGlide(getApplicationContext(),
                                    comunidadeAtual.getFotoComunidade(), imgViewIncFotoUser, android.R.color.transparent);

                            GlideCustomizado.montarGlideFoto(getApplicationContext(),
                                    comunidadeAtual.getFundoComunidade(), imgViewIncFundoUser, android.R.color.transparent);
                        }

                        txtViewIncNomeUser.setText(comunidadeAtual.getNomeComunidade());
                        if (comunidadeAtual.getSeguidores() != null
                                && comunidadeAtual.getSeguidores().size() > 0) {
                            txtViewNrParticipantes.setText("" + comunidadeAtual.getSeguidores().size());
                        } else {
                            txtViewNrParticipantes.setText("0");
                        }


                        exibirTopicos(comunidadeAtual);
                    }

                    @Override
                    public void onError(String mensagem) {

                    }
                });
            }

            @Override
            public void onError(String mensagem) {

            }
        });

        //Apagar
        imgViewIncFundoUser.setBackgroundResource(R.drawable.placeholderuniverse);
    }

    private void inicializandoComponentes() {

        //inc_cabecalho_user
        imgViewIncFotoUser = findViewById(R.id.imgViewIncFotoUser);
        imgViewIncFundoUser = findViewById(R.id.imgViewIncFundoUser);
        viewIncBackOpcoes = findViewById(R.id.viewIncBackOpcoes);
        imgBtnIncOpcoes = findViewById(R.id.imgBtnIncOpcoes);
        txtViewIncNomeUser = findViewById(R.id.txtViewIncNomeUser);

        //inc_toolbar_padrao
        toolbarIncPadrao = findViewById(R.id.toolbarIncPadrao);
        imgBtnIncBackPadrao = findViewById(R.id.imgBtnIncBackPadrao);
        txtViewIncTituloToolbar = findViewById(R.id.txtViewIncTituloToolbarPadrao);
        //

        //Componentes do próprio layout atual
        recyclerViewPostagensComunidade = findViewById(R.id.recyclerViewPostagensComunidade);
        imgBtnParticipantes = findViewById(R.id.imgBtnParticipantesComunidade);
        txtViewNrParticipantes = findViewById(R.id.txtViewNrParticipantesComunidade);
        btnViewEntrarComunidade = findViewById(R.id.btnViewEntrarComunidade);
        linearLayoutTopicos = findViewById(R.id.linearLayoutTopicosComunidadePostagem);

        imgBtnOpcoesPostagem = findViewById(R.id.fabOpcoesPostagemComunidade);
        fabVideoComunidadePostagem = findViewById(R.id.fabVideoComunidadePostagem);
        fabGaleriaComunidadePostagem = findViewById(R.id.fabGaleriaComunidadePostagem);
        fabGifComunidadePostagem = findViewById(R.id.fabGifComunidadePostagem);
        fabTextComunidadePostagem = findViewById(R.id.fabTextComunidadePostagem);

        progressBarComunidadePostagem = findViewById(R.id.progressBarComunidadePostagem);

    }

    private void exibirTopicos(Comunidade comunidadeAtual) {
        for (String hobby : comunidadeAtual.getTopicos()) {
            Chip chip = new Chip(linearLayoutTopicos.getContext());
            chip.setText(hobby);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.DKGRAY));
            chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
            chip.setLayoutParams(params);
            chip.setClickable(false);
            linearLayoutTopicos.addView(chip);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabOpcoesPostagemComunidade:
                ToastCustomizado.toastCustomizadoCurto("Clicado fab", getApplicationContext());
                if (isMenuOpen) {
                    fecharFabMenu();
                } else {
                    abrirFabMenu();
                }
                break;
            case R.id.fabVideoComunidadePostagem:

                break;
            case R.id.fabGaleriaComunidadePostagem:
                irParaCriacaoDaPostagem("imagem");
                break;
            case R.id.fabGifComunidadePostagem:
                irParaCriacaoDaPostagem("gif");
                break;
            case R.id.fabTextComunidadePostagem:

                break;
        }
    }

    private void testeMenu() {

        fabVideoComunidadePostagem.setAlpha(0f);
        fabGaleriaComunidadePostagem.setAlpha(0f);
        fabGifComunidadePostagem.setAlpha(0f);
        fabTextComunidadePostagem.setAlpha(0f);

        fabVideoComunidadePostagem.setTranslationY(translationY);
        fabGaleriaComunidadePostagem.setTranslationY(translationY);
        fabGifComunidadePostagem.setTranslationY(translationY);
        fabTextComunidadePostagem.setTranslationY(translationY);

        imgBtnOpcoesPostagem.setOnClickListener(this);
        fabVideoComunidadePostagem.setOnClickListener(this);
        fabGaleriaComunidadePostagem.setOnClickListener(this);
        fabGifComunidadePostagem.setOnClickListener(this);
        fabTextComunidadePostagem.setOnClickListener(this);
    }

    private void abrirFabMenu() {
        isMenuOpen = !isMenuOpen;

        imgBtnOpcoesPostagem.animate().setInterpolator(interpolator)
                .rotationBy(45f).setDuration(300).start();

        fabVideoComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fabGaleriaComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fabGifComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fabTextComunidadePostagem.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    private void fecharFabMenu() {
        isMenuOpen = !isMenuOpen;

        imgBtnOpcoesPostagem.animate().setInterpolator(interpolator)
                .rotation(0f).setDuration(300).start();

        fabVideoComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fabGaleriaComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fabGifComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fabTextComunidadePostagem.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
    }


    @Override
    public void onComunidadeRemocao(Postagem postagemRemovida) {
        postagemDiffDAO.removerPostagem(postagemRemovida);
        Log.d("PAG-On", "Postagem removida com sucesso");

        // Notifica o adapter das mudanças usando o DiffUtil
        adapterPostagens.updatePostagemList(listaPostagens);
        Log.d("PAG-On Child Removed", "Adapter notificado com sucesso");
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            //ToastCustomizado.toastCustomizado("Position: " + posicaoAnterior, getApplicationContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    private void irParaCriacaoDaPostagem(String tipoPostagem) {
        Intent intent = new Intent(getApplicationContext(), CriarPostagemComunidadeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("idComunidade", idComunidade);
        intent.putExtra("tipoPostagem", tipoPostagem);
        startActivity(intent);
    }
}