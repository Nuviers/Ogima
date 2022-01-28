package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.fragment.PerfilFragment;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.model.Usuario;
import com.facebook.shimmer.ShimmerFrameLayout;

public class PersonProfileActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private ImageButton denunciarPerfil;
    private TextView nomeProfile, seguidoresProfile, seguindoProfile, amigosProfile;
    private ImageView fotoProfile, fundoProfile;
    private ShimmerFrameLayout shimmerFrameLayout;
    private String totalSeguidores, totalAmigos, totalSeguindo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);

        denunciarPerfil = findViewById(R.id.imageButtonEditarProfile);
        nomeProfile = findViewById(R.id.textNickProfile);
        fotoProfile = findViewById(R.id.imageBordaPeople);
        fundoProfile = findViewById(R.id.imgFundoProfile);
        shimmerFrameLayout = findViewById(R.id.shimmerProfile);
        seguidoresProfile = findViewById(R.id.textSeguidoresProfile);
        seguindoProfile = findViewById(R.id.textSeguindoProfile);
        amigosProfile = findViewById(R.id.textAmigosProfile);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            usuarioSelecionado = (Usuario) dados.getSerializable("usuarioSelecionado");

            totalSeguidores = String.valueOf(usuarioSelecionado.getSeguidoresUsuario());
            totalAmigos = String.valueOf(usuarioSelecionado.getAmigosUsuario());
            totalSeguindo = String.valueOf(usuarioSelecionado.getSeguindoUsuario());

            seguidoresProfile.setText(totalSeguidores);
            amigosProfile.setText(totalAmigos);
            seguindoProfile.setText(totalSeguindo);

            if (usuarioSelecionado.getExibirApelido().equals("sim")) {
                nomeProfile.setText(usuarioSelecionado.getApelidoUsuario());
                setTitle(usuarioSelecionado.getApelidoUsuario());
            } else {
                nomeProfile.setText(usuarioSelecionado.getNomeUsuario());
                setTitle(usuarioSelecionado.getNomeUsuario());
            }

            try {
                if (usuarioSelecionado.getMinhaFoto() != null) {
                    if (usuarioSelecionado.getEpilepsia().equals("Sim")) {
                        animacaoShimmer();
                        GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), usuarioSelecionado.getMinhaFoto(), fotoProfile, R.drawable.testewomamtwo);
                    }

                    if (usuarioSelecionado.getEpilepsia().equals("Não")) {
                        animacaoShimmer();
                        GlideCustomizado.montarGlide(getApplicationContext(), usuarioSelecionado.getMinhaFoto(), fotoProfile, R.drawable.testewomamtwo);
                    }
                } else {
                    animacaoShimmer();
                    Glide.with(PersonProfileActivity.this)
                            .load(R.drawable.testewomamtwo)
                            .placeholder(R.drawable.testewomamtwo)
                            .error(R.drawable.errorimagem)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerCrop()
                            .circleCrop()
                            .into(fotoProfile);
                }

                if (usuarioSelecionado.getMeuFundo() != null) {
                    if (usuarioSelecionado.getMeuFundo() != null) {
                        if (usuarioSelecionado.getEpilepsia().equals("Sim")) {
                            animacaoShimmer();
                            GlideCustomizado.fundoGlideEpilepsia(getApplicationContext(), usuarioSelecionado.getMeuFundo(), fundoProfile, R.drawable.placeholderuniverse);
                        }

                        if (usuarioSelecionado.getEpilepsia().equals("Não")) {
                            animacaoShimmer();
                            GlideCustomizado.fundoGlide(getApplicationContext(), usuarioSelecionado.getMeuFundo(), fundoProfile, R.drawable.placeholderuniverse);
                        }
                    } else {
                        animacaoShimmer();
                        Glide.with(PersonProfileActivity.this)
                                .load(R.drawable.placeholderuniverse)
                                .placeholder(R.drawable.placeholderuniverse)
                                .error(R.drawable.errorimagem)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .centerCrop()
                                .into(fundoProfile);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }


        } else {
            setTitle("Voltar para pesquisa");
        }

        denunciarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        //Configurando toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    public void animacaoShimmer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    fotoProfile.setVisibility(View.VISIBLE);
                    fundoProfile.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }
}