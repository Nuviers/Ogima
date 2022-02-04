package com.example.ogima.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.activity.SeguidoresActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.intro.IntrodActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private TextView txtDeslogar, nickUsuario,
            txtSeguidores, txtSeguindo, txtAmigos, txtTituloSeguidores,
            txtTituloSeguindo;
    private GoogleSignInClient mSignInClient;
    private ImageView imgFotoUsuario, imgFundoUsuario, imageViewGif, imageBorda;

    private String urlGifTeste = "";

    private Button buttonEditarPerfil;
    private ImageButton imageButtonEditar;
    private Usuario usuario;

    private String minhaFoto;
    private String meuFundo;
    private String apelido, nome, epilepsia;

    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef, usuarioRefs;
    private String exibirApelido;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private ShimmerFrameLayout shimmerFrameLayout;


    public PerfilFragment() {
        // Required empty public constructor
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            testandoLog();
            //*exibirNick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        imageBorda = view.findViewById(R.id.imageBorda);
        imgFundoUsuario = view.findViewById(R.id.imgFundoUsuario);
        nickUsuario = view.findViewById(R.id.textNickUsuario);
        imageButtonEditar = view.findViewById(R.id.imageButtonEditar);
        shimmerFrameLayout = view.findViewById(R.id.shimmer);
        txtSeguidores = view.findViewById(R.id.textSeguidores);
        txtSeguindo = view.findViewById(R.id.textSeguindo);
        txtAmigos = view.findViewById(R.id.textAmigos);
        txtTituloSeguidores = view.findViewById(R.id.textTituloSeguidores2);
        txtTituloSeguindo = view.findViewById(R.id.textTituloSeguindo2);
        //view18 = view.findViewById(R.id.view18);

        txtSeguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("seguidores");
            }
        });

        txtSeguindo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("seguindo");
            }
        });

        txtTituloSeguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("seguidores");
            }
        });

        txtTituloSeguindo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("seguindo");
            }
        });

        imageButtonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


        //urlGifTeste = "https://media.giphy.com/media/a4aAKvUXYgiRuEqRsc/giphy.gif";

        //Glide.with(PerfilFragment.this).asGif().load(urlGifTeste).into(imageViewGif);
        // Usar algum meio que o glide trave as gif tipo diz pra ele que é tudo png

        return view;
    }

    public void testandoLog() {
        //emailUsuario = autenticacao.getCurrentUser().getEmail();
        //idUsuario = Base64Custom.codificarBase64(emailUsuario);
        //usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    nome = usuario.getNomeUsuario();
                    apelido = usuario.getApelidoUsuario();
                    meuFundo = usuario.getMeuFundo();
                    minhaFoto = usuario.getMinhaFoto();
                    exibirApelido = usuario.getExibirApelido();
                    epilepsia = usuario.getEpilepsia();

                    String amigos = String.valueOf( usuario.getAmigosUsuario() );
                    String seguindo = String.valueOf( usuario.getSeguindoUsuario() );
                    String seguidores = String.valueOf( usuario.getSeguidoresUsuario() );

                    if (emailUsuario != null) {
                        try {

                            txtSeguidores.setText( seguidores );
                            txtAmigos.setText( amigos );
                            txtSeguindo.setText( seguindo );

                            if (minhaFoto != null) {
                                if (epilepsia.equals("Sim")) {
                                    GlideCustomizado.montarGlideEpilepsia(getActivity(), minhaFoto, imageBorda, R.drawable.testewomamtwo);
                                    animacaoShimmer();
                                } else {
                                    animacaoShimmer();
                                    GlideCustomizado.montarGlide(getActivity(), minhaFoto, imageBorda, R.drawable.testewomamtwo);
                                }
                            }
                            else {
                                animacaoShimmer();

                                Glide.with(PerfilFragment.this)
                                        .load(R.drawable.testewomamtwo)
                                        .placeholder(R.drawable.testewomamtwo)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageBorda);
                            }

                            if (meuFundo != null) {
                                if (epilepsia.equals("Sim")) {
                                    GlideCustomizado.fundoGlideEpilepsia(getActivity(), meuFundo, imgFundoUsuario, R.drawable.placeholderuniverse);
                                    animacaoShimmer();
                                } else {
                                    GlideCustomizado.fundoGlide(getActivity(), meuFundo, imgFundoUsuario, R.drawable.placeholderuniverse);
                                    animacaoShimmer();
                                }

                            } else {
                                animacaoShimmer();

                                Glide.with(PerfilFragment.this)
                                        .load(R.drawable.placeholderuniverse)
                                        .placeholder(R.drawable.placeholderuniverse)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imgFundoUsuario);
                            }

                            if (exibirApelido.equals("não")) {
                                nickUsuario.setText(nome);
                                //Toast.makeText(getActivity(), "Igual a não " + resultadoNick, Toast.LENGTH_SHORT).show();
                            } else if (exibirApelido.equals("sim")) {
                                nickUsuario.setText(apelido);
                                //Toast.makeText(getActivity(), "Igual a sim " + resultadoNick, Toast.LENGTH_SHORT).show();
                            } else if (exibirApelido == null) {
                                nickUsuario.setText(nome);
                                //Toast.makeText(getActivity(), "Igual a nulo", Toast.LENGTH_SHORT).show();
                            }
                            usuarioRef.removeEventListener(this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    } else if (snapshot == null) {

                        //Toast.makeText(getActivity(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Toast.makeText(getActivity(), "Por favor termine seu cadastro", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado("Ocorreu um erro: " + error.getMessage(), getActivity());
            }
        });
    }

    public void onBackPressed() {
        // Método para retorno
        Intent intent = new Intent(getActivity(), IntrodActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        getActivity().finish();
    }

    public void animacaoShimmer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    imageBorda.setVisibility(View.VISIBLE);
                    imgFundoUsuario.setVisibility(View.VISIBLE);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }

    private void navegarSeguidores(String destino){

        if(destino.equals("seguidores")){
            Intent intent = new Intent(getActivity(), SeguidoresActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else{
            Intent intent = new Intent(getActivity(), SeguidoresActivity.class);
            intent.putExtra("exibirSeguindo", "exibirSeguindo");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}

