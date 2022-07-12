package com.example.ogima.fragment;


import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
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
import com.example.ogima.activity.DetalhesPostagemActivity;
import com.example.ogima.activity.EdicaoFotoActivity;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.activity.FotosPostadasActivity;
import com.example.ogima.activity.FriendsRequestsActivity;
import com.example.ogima.activity.PostagemActivity;
import com.example.ogima.activity.ProfileViewsActivity;
import com.example.ogima.activity.SeguidoresActivity;
import com.example.ogima.adapter.AdapterGridPostagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.Permissao;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.intro.IntrodActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private TextView txtDeslogar, nickUsuario,
            txtSeguidores, txtSeguindo, txtAmigos, txtTituloSeguidores,
            txtTituloSeguindo, txtTituloAmigos, txtTituloPedidos, txtPedidos,
            txtVisualizacoesPerfil, textViewVerView, textViewMsgSemFotos;
    private GoogleSignInClient mSignInClient;
    private ImageView imgFotoUsuario, imgFundoUsuario, imageViewGif, imageBorda,
            imageViewViewer, imageViewFotoTwo, imageViewFotoOne, imageViewFotoThree,
            imageViewFotoFour, imageViewEfeito;

    private String urlGifTeste = "";

    private Button btnAddPostagensPerfil;
    private ImageButton imageButtonEditar, imgButtonCamFoto,
            imgButtonGaleriaFoto, imageButtonMaisFotos, imageButtonMaisFotos2,
            imageButtonTodasFotos, imageButtonTodasFotos1;
    private Postagem usuarioFotos;

    private String minhaFoto;
    private String meuFundo;
    private String apelido, nome, epilepsia;

    private String emailUsuario, idUsuario;
    private DatabaseReference usuarioRef, usuarioRefs;
    private String exibirApelido;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private ShimmerFrameLayout shimmerFrameLayout;
    private StorageReference imagemRef;
    private StorageReference storageRef;
    private DateFormat dateFormat;
    private Date date;
    private String localConvertido;
    private Locale current;
    private ProgressDialog progressDialog;
    //Dados para o corte de foto
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    //Constantes passando um result code
    private static final int SELECAO_CAMERA = 100, SELECAO_GALERIA = 200;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    //Somente é preenchida quando a camêra é selecionada.
    private String selecionadoCamera, selecionadoGaleria;
    private DatabaseReference baseFotosPostagemRef;
    private Usuario usuarioPostagem, usuarioExistente;
    private  int contadorAtual;
    private String contadorExistente;
    private Postagem postagemChildren;
    private ArrayList<String> listaCaminhoUpdate = new ArrayList<>();
    //Componentes para postagem
    private ImageView imgViewOnePostagem, imgViewTwoPostagem,
            imgViewThreePostagem, imgViewFourPostagem, imgViewEfeitoPostagem;
    private ImageButton imgButtonVideoPostagem, imgButtonGifPostagem,
            imgButtonGaleriaPostagem, imgButtonCameraPostagem,
            imgButtonMusicaPostagem, imgButtonAllPostagens1, imgButtonAllPostagens2;
    private RecyclerView recyclerPostagem;
    private AdapterGridPostagem adapterGridPostagem;
    private List<Postagem> listaPostagem = new ArrayList<>();
    private TextView txtViewSemPostagemMsg;
    private Button btnTodasPostagens;

    public PerfilFragment() {
        // Required empty public constructor
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        baseFotosPostagemRef = firebaseRef
        .child("fotosUsuario").child(idUsuario).child(idUsuario+1);
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
        txtTituloSeguidores = view.findViewById(R.id.textTituloSeguidores2);
        txtSeguindo = view.findViewById(R.id.textSeguindo);
        txtTituloSeguindo = view.findViewById(R.id.textTituloSeguindo2);
        txtTituloAmigos = view.findViewById(R.id.textTituloAmigos2);
        txtAmigos = view.findViewById(R.id.textAmigos);
        txtTituloPedidos = view.findViewById(R.id.txtViewTitlePedidos);
        txtPedidos = view.findViewById(R.id.txtViewPedidos);
        txtVisualizacoesPerfil = view.findViewById(R.id.textViewVisualizacoes);
        imageViewViewer = view.findViewById(R.id.imageViewViewer);
        textViewVerView = view.findViewById(R.id.textViewVerView);
        imgButtonCamFoto = view.findViewById(R.id.imgButtonCamFoto);
        imgButtonGaleriaFoto = view.findViewById(R.id.imgButtonGaleriaFoto);
        imageViewFotoOne = view.findViewById(R.id.imageViewFotoOne);
        imageViewFotoTwo = view.findViewById(R.id.imageViewFotoTwo);
        imageViewFotoThree = view.findViewById(R.id.imageViewFotoThree);
        imageViewFotoFour = view.findViewById(R.id.imageViewFotoFour);
        imageButtonMaisFotos = view.findViewById(R.id.imageButtonMaisFotos);
        imageButtonMaisFotos2 = view.findViewById(R.id.imageButtonMaisFotos2);
        imageButtonTodasFotos = view.findViewById(R.id.imageButtonTodasFotos);
        imageButtonTodasFotos1 = view.findViewById(R.id.imageButtonTodasFotos1);
        imageViewEfeito = view.findViewById(R.id.imageViewEfeito);
        textViewMsgSemFotos = view.findViewById(R.id.textViewMsgSemFotos);

        //Componentes para a postagem
        imgButtonVideoPostagem = view.findViewById(R.id.imgButtonVideoPostagem);
        imgButtonGifPostagem = view.findViewById(R.id.imgButtonGifPostagem);
        imgButtonGaleriaPostagem = view.findViewById(R.id.imgButtonGaleriaPostagem);
        imgButtonCameraPostagem = view.findViewById(R.id.imgButtonCameraPostagem);
        imgButtonMusicaPostagem = view.findViewById(R.id.imgButtonMusicaPostagem);
        btnAddPostagensPerfil = view.findViewById(R.id.btnAddPostagensPerfil);
        recyclerPostagem = view.findViewById(R.id.recyclerPostagem);
        txtViewSemPostagemMsg = view.findViewById(R.id.txtViewSemPostagemMsg);
        btnTodasPostagens = view.findViewById(R.id.btnTodasPostagens);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),2);
        recyclerPostagem.setHasFixedSize(true);
        recyclerPostagem.setLayoutManager(layoutManager);

        progressDialog = new ProgressDialog(view.getContext(),ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        //Validar permissões necessárias para adição de fotos.
        Permissao.validarPermissoes(permissoesNecessarias, getActivity(), 1);
        //Configurando storage
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        exibirPostagens();

        btnTodasPostagens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DetalhesPostagemActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        textViewVerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("viewsPerfil");
            }
        });

        imageViewViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("viewsPerfil");
            }
        });

        txtVisualizacoesPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("viewsPerfil");
            }
        });

        txtPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("pedidoAmigos");
            }
        });

        txtTituloPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("pedidoAmigos");
            }
        });

        txtAmigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("amigos");
            }
        });

        txtTituloAmigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navegarSeguidores("amigos");
            }
        });

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

        //Evento de clique da câmera para adicionar fotos por ela.
        imgButtonCamFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Chama o crop de camêra
                selecionadoCamera = "sim";
                CropImage.activity()
                        .setMinCropWindowSize(510 , 612)
                        .start(getContext(), PerfilFragment.this);
            }
        });

        //Evento de clique da câmera para adicionar postagem
        btnAddPostagensPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PostagemActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        imgButtonCameraPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PostagemActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        imgButtonGaleriaPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PostagemActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        imgButtonGifPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PostagemActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        imgButtonVideoPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PostagemActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        //Evento de clique da galeria para adicionar fotos por ela.
        imgButtonGaleriaFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Passando a intenção de selecionar uma foto pela galeria
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Verificando se a intenção foi atendida com sucesso
                if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

        imageButtonMaisFotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FotosPostadasActivity.class);
                startActivity(intent);
            }
        });

        imageButtonMaisFotos2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FotosPostadasActivity.class);
                startActivity(intent);
            }
        });

        imageButtonTodasFotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FotosPostadasActivity.class);
                startActivity(intent);
            }
        });

        imageButtonTodasFotos1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FotosPostadasActivity.class);
                startActivity(intent);
            }
        });

        //urlGifTeste = "https://media.giphy.com/media/a4aAKvUXYgiRuEqRsc/giphy.gif";

        //Glide.with(PerfilFragment.this).asGif().load(urlGifTeste).into(imageViewGif);
        // Usar algum meio que o glide trave as gif tipo diz pra ele que é tudo png

        return view;
    }

    public void testandoLog() {

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

                    String amigos = String.valueOf(usuario.getAmigosUsuario());
                    String seguindo = String.valueOf(usuario.getSeguindoUsuario());
                    String seguidores = String.valueOf(usuario.getSeguidoresUsuario());
                    String pedidos = String.valueOf(usuario.getPedidosAmizade());
                    String visualizacoes = String.valueOf(usuario.getViewsPerfil());

                    if (emailUsuario != null) {
                        try {
                            txtSeguidores.setText(seguidores);
                            txtAmigos.setText(amigos);
                            txtSeguindo.setText(seguindo);
                            txtPedidos.setText(pedidos);
                            if (usuario.getViewsPerfil() > 1) {
                                txtVisualizacoesPerfil.setText(visualizacoes + " visualizações no seu perfil atualmente!");
                            } else if (usuario.getViewsPerfil() <= 1) {
                                txtVisualizacoesPerfil.setText(visualizacoes + " visualização no seu perfil atualmente!");
                            }

                            if (minhaFoto != null) {
                                if (epilepsia.equals("Sim")) {
                                    GlideCustomizado.montarGlideEpilepsia(getActivity(), minhaFoto, imageBorda, R.color.gph_transparent);
                                    animacaoShimmer();
                                    //Organizar o glide estático e os if/else aqui para exibir as fotos
                                    // de forma estática para usuários com epilepsia
                                } else {
                                    animacaoShimmer();
                                    GlideCustomizado.montarGlide(getActivity(), minhaFoto, imageBorda, R.color.gph_transparent);

                                    DatabaseReference complementoFotoRef = firebaseRef.child("complementoFoto")
                                            .child(idUsuario);

                                    complementoFotoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue() != null) {
                                                try {
                                                    Postagem usuarioFotos = snapshot.getValue(Postagem.class);

                                                    DatabaseReference baseFotosPostagemRef = firebaseRef
                                                            .child("fotosUsuario").child(idUsuario);

                                                    baseFotosPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                if (snapshot.getValue() != null) {
                                                                    try{

                                                                        if (usuarioFotos.getContadorFotos() >= 4) {
                                                                            textViewMsgSemFotos.setVisibility(View.GONE);
                                                                            imageButtonMaisFotos.setVisibility(View.VISIBLE);
                                                                            imageButtonMaisFotos2.setVisibility(View.VISIBLE);
                                                                            imageViewFotoOne.setVisibility(View.VISIBLE);
                                                                            imageViewFotoTwo.setVisibility(View.VISIBLE);
                                                                            imageViewFotoThree.setVisibility(View.VISIBLE);
                                                                            imageViewFotoFour.setVisibility(View.VISIBLE);
                                                                            imageViewEfeito.setVisibility(View.VISIBLE);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(0), imageViewFotoOne, android.R.color.transparent);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(1), imageViewFotoTwo, android.R.color.transparent);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(2), imageViewFotoThree, android.R.color.transparent);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(3), imageViewFotoFour, android.R.color.transparent);

                                                                        } else if (usuarioFotos.getContadorFotos() == 1) {

                                                                            textViewMsgSemFotos.setVisibility(View.GONE);
                                                                            imageViewFotoOne.setVisibility(View.VISIBLE);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(0), imageViewFotoOne, android.R.color.transparent);

                                                                        } else if (usuarioFotos.getContadorFotos() == 2) {

                                                                            textViewMsgSemFotos.setVisibility(View.GONE);
                                                                            imageViewFotoOne.setVisibility(View.VISIBLE);
                                                                            imageViewFotoTwo.setVisibility(View.VISIBLE);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(0), imageViewFotoOne, android.R.color.transparent);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(1), imageViewFotoTwo, android.R.color.transparent);

                                                                        } else if (usuarioFotos.getContadorFotos() == 3) {

                                                                            textViewMsgSemFotos.setVisibility(View.GONE);
                                                                            imageViewFotoOne.setVisibility(View.VISIBLE);
                                                                            imageViewFotoTwo.setVisibility(View.VISIBLE);
                                                                            imageViewFotoThree.setVisibility(View.VISIBLE);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(0), imageViewFotoOne, android.R.color.transparent);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(1), imageViewFotoTwo, android.R.color.transparent);
                                                                            GlideCustomizado.montarGlideFoto(getActivity(), usuarioFotos.getListaCaminhoFotos().get(2), imageViewFotoThree, android.R.color.transparent);

                                                                        } else if (usuarioFotos.getContadorFotos() <= 0) {
                                                                            textViewMsgSemFotos.setVisibility(View.VISIBLE);

                                                                        }

                                                                    }catch (Exception ex){
                                                                        ex.printStackTrace();
                                                                    }
                                                                }


                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            } else {
                                                textViewMsgSemFotos.setVisibility(View.VISIBLE);
                                            }
                                            complementoFotoRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            } else {
                                animacaoShimmer();

                                Glide.with(PerfilFragment.this)
                                        .load(R.drawable.testewomamtwo)
                                        .placeholder(R.color.gph_transparent)
                                        .error(android.R.color.transparent)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageBorda);
                            }

                            if (meuFundo != null) {
                                if (epilepsia.equals("Sim")) {
                                    GlideCustomizado.fundoGlideEpilepsia(getActivity(), meuFundo, imgFundoUsuario, R.color.gph_transparent);
                                    animacaoShimmer();
                                } else {
                                    GlideCustomizado.fundoGlide(getActivity(), meuFundo, imgFundoUsuario, R.color.gph_transparent);
                                    animacaoShimmer();
                                }

                            } else {
                                animacaoShimmer();

                                Glide.with(PerfilFragment.this)
                                        .load(R.drawable.placeholderuniverse)
                                        .placeholder(R.color.gph_transparent)
                                        .error(android.R.color.transparent)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imgFundoUsuario);
                            }

                            if (exibirApelido.equals("não")) {
                                nickUsuario.setText(nome);
                            } else if (exibirApelido.equals("sim")) {
                                nickUsuario.setText(apelido);
                            } else if (exibirApelido == null) {
                                nickUsuario.setText(nome);
                            }
                            usuarioRef.removeEventListener(this);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
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
                try {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.hideShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    imageBorda.setVisibility(View.VISIBLE);
                    imgFundoUsuario.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 1200);
    }

    private void navegarSeguidores(String destino) {

        if (destino.equals("seguidores")) {
            Intent intent = new Intent(getActivity(), SeguidoresActivity.class);
            intent.putExtra("exibirSeguidores", "exibirSeguidores");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (destino.equals("seguindo")) {
            Intent intent = new Intent(getActivity(), SeguidoresActivity.class);
            intent.putExtra("exibirSeguindo", "exibirSeguindo");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (destino.equals("amigos")) {
            Intent intent = new Intent(getActivity(), FriendsRequestsActivity.class);
            intent.putExtra("exibirAmigos", "exibirAmigos");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (destino.equals("pedidoAmigos")) {
            Intent intent = new Intent(getActivity(), FriendsRequestsActivity.class);
            intent.putExtra("exibirPedidosAmigos", "exibirPedidosAmigos");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (destino.equals("viewsPerfil")) {
            Intent intent = new Intent(getActivity(), ProfileViewsActivity.class);
            intent.putExtra("viewsPerfil", "viewsPerfil");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Verificando se foi possível recuperar a foto do usuario
        //*if (resultCode == getActivity().RESULT_OK) {
        if(resultCode == RESULT_OK && requestCode == SELECAO_GALERIA) {

            try {

                switch (requestCode) {
                    //Seleção pela galeria
                    case SELECAO_GALERIA:
                        selecionadoGaleria = "sim";
                        //*Salvando uma imagem em cache para obter a Uri dela
                        String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
                        destinoArquivo += ".jpg";
                        final Uri localImagemFotoSelecionada = data.getData();
                        //*Chamando método responsável pela estrutura do U crop
                        openCropActivity(localImagemFotoSelecionada, Uri.fromFile(new File(getContext().getCacheDir(), destinoArquivo)));
                        break;
                }

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            try{
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if(selecionadoCamera != null){
                    selecionadoCamera = null;
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri resultUri = result.getUri();
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), resultUri);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                }else if (selecionadoGaleria != null){
                    Uri imagemCortada = UCrop.getOutput(data);
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imagemCortada);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    selecionadoGaleria = null;
                }

                //Recuperar dados da imagem para o firebase
                byte[] dadosImagem = baos.toByteArray();

                //Verifica se existe o arquivo no db.
                DatabaseReference dadosFotosUsuarioRef = firebaseRef
                        .child("complementoFoto").child(idUsuario);
                DatabaseReference contadorFotosRef = dadosFotosUsuarioRef
                        .child("contadorFotos");

                current = getResources().getConfiguration().locale;
                localConvertido = localConvertido.valueOf(current);

                dadosFotosUsuarioRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Caso exista alguma foto já postada pelo usuário.
                        if (snapshot.getValue() != null) {
                            progressDialog.setMessage("Fazendo upload da imagem, por favor aguarde...");
                            progressDialog.show();
                            usuarioFotos = snapshot.getValue(Postagem.class);

                            contadorFotosRef.setValue(usuarioFotos.getContadorFotos() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        try{
                                            int numeroNovo = usuarioFotos.getContadorFotos();

                                            imagemRef = storageRef
                                                    .child("imagens")
                                                    .child("fotosUsuario")
                                                    .child(idUsuario)
                                                    .child("fotoUsuario" + numeroNovo + ".jpeg");

                                            UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getContext());
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                    ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da imagem", getContext());

                                                    imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            Uri url = task.getResult();

                                                            String caminhoFotoPerfil = url.toString();

                                                            int contadorNovo = usuarioFotos.getContadorFotos() + 1;

                                                            DatabaseReference pesquisarDadoExistenteNewRef = firebaseRef
                                                                    .child("fotosUsuario").child(idUsuario).child(idUsuario+contadorNovo);

                                                            pesquisarDadoExistenteNewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if(snapshot.exists()){
                                                                        int contadorNovo = usuarioFotos.getContadorFotos() + 2;

                                                                        baseFotosPostagemRef = firebaseRef
                                                                                .child("fotosUsuario").child(idUsuario).child(idUsuario+contadorNovo);

                                                                        HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                        dadosPostagemExistente.put("idPostagem", idUsuario+contadorNovo);
                                                                        dadosPostagemExistente.put("caminhoPostagem", caminhoFotoPerfil);
                                                                        if (localConvertido.equals("pt_BR")) {
                                                                            try{
                                                                                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                                date = new Date();
                                                                                String novaData = dateFormat.format(date);
                                                                                dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                dadosPostagemExistente.put("dataPostagemNova", date);
                                                                            }catch (Exception ex){
                                                                                ex.printStackTrace();
                                                                            }
                                                                        } else {
                                                                            try{
                                                                                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                                date = new Date();
                                                                                String novaData = dateFormat.format(date);
                                                                                dadosPostagemExistente.put("dataPostagem", novaData);
                                                                                dadosPostagemExistente.put("dataPostagemNova", date);
                                                                            }catch (Exception ex){
                                                                                ex.printStackTrace();
                                                                            }
                                                                        }
                                                                        dadosPostagemExistente.put("tituloPostagem", "");
                                                                        dadosPostagemExistente.put("descricaoPostagem", "");
                                                                        dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                        dadosPostagemExistente.put("publicoPostagem", "Todos");
                                                                        dadosPostagemExistente.put("totalViewsFotoPostagem", 0);
                                                                        //Novo
                                                                        DatabaseReference complementoFotoRef = firebaseRef.child("complementoFoto")
                                                                                .child(idUsuario).child("listaCaminhoFotos");

                                                                        if(usuarioFotos.getContadorFotos() < 4){
                                                                            listaCaminhoUpdate = usuarioFotos.getListaCaminhoFotos();
                                                                            listaCaminhoUpdate.add(caminhoFotoPerfil);
                                                                            Collections.sort(listaCaminhoUpdate, Collections.reverseOrder());
                                                                            complementoFotoRef.setValue(listaCaminhoUpdate);
                                                                        }else{
                                                                            listaCaminhoUpdate = usuarioFotos.getListaCaminhoFotos();
                                                                            Collections.sort(listaCaminhoUpdate, Collections.reverseOrder());
                                                                            ArrayList<String> arrayReordenado = new ArrayList<>();
                                                                            arrayReordenado.add(0, caminhoFotoPerfil);
                                                                            arrayReordenado.add(1,listaCaminhoUpdate.get(0));
                                                                            arrayReordenado.add(2,listaCaminhoUpdate.get(1));
                                                                            arrayReordenado.add(3,listaCaminhoUpdate.get(2));
                                                                            complementoFotoRef.setValue(arrayReordenado);
                                                                        }


                                                                        baseFotosPostagemRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    progressDialog.dismiss();

                                                                                    //Enviando imagem para edição de foto em outra activity.
                                                                                    Intent i = new Intent(getActivity(), EdicaoFotoActivity.class);
                                                                                    i.putExtra("fotoUsuario", caminhoFotoPerfil);
                                                                                    i.putExtra("idPostagem", idUsuario+contadorNovo);
                                                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(i);
                                                                                }else{
                                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getContext());
                                                                                }
                                                                            }
                                                                        });

                                                                    }else{
                                                                        int contadorNovo = usuarioFotos.getContadorFotos() + 1;

                                                                        baseFotosPostagemRef = firebaseRef
                                                                                .child("fotosUsuario").child(idUsuario).child(idUsuario+contadorNovo);

                                                                        HashMap<String, Object> dadosPostagemExistente = new HashMap<>();
                                                                        dadosPostagemExistente.put("idPostagem", idUsuario+contadorNovo);
                                                                        dadosPostagemExistente.put("caminhoPostagem", caminhoFotoPerfil);
                                                                        if (localConvertido.equals("pt_BR")) {
                                                                            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                                            date = new Date();
                                                                            String novaData = dateFormat.format(date);
                                                                            dadosPostagemExistente.put("dataPostagem", novaData);
                                                                            dadosPostagemExistente.put("dataPostagemNova", date);
                                                                        } else {
                                                                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                                            date = new Date();
                                                                            String novaData = dateFormat.format(date);
                                                                            dadosPostagemExistente.put("dataPostagem", novaData);
                                                                            dadosPostagemExistente.put("dataPostagemNova", date);
                                                                        }
                                                                        dadosPostagemExistente.put("tituloPostagem", "");
                                                                        dadosPostagemExistente.put("descricaoPostagem", "");
                                                                        dadosPostagemExistente.put("idDonoPostagem", idUsuario);
                                                                        dadosPostagemExistente.put("totalViewsFotoPostagem", 0);
                                                                        dadosPostagemExistente.put("publicoPostagem", "Todos");

                                                                        //Novo
                                                                        DatabaseReference complementoFotoRef = firebaseRef.child("complementoFoto")
                                                                                .child(idUsuario).child("listaCaminhoFotos");

                                                                        if(usuarioFotos.getContadorFotos() < 4){
                                                                            listaCaminhoUpdate = usuarioFotos.getListaCaminhoFotos();
                                                                            listaCaminhoUpdate.add(caminhoFotoPerfil);
                                                                            Collections.sort(listaCaminhoUpdate, Collections.reverseOrder());
                                                                            complementoFotoRef.setValue(listaCaminhoUpdate);
                                                                        }else{
                                                                            listaCaminhoUpdate = usuarioFotos.getListaCaminhoFotos();
                                                                            Collections.sort(listaCaminhoUpdate, Collections.reverseOrder());
                                                                            ArrayList<String> arrayReordenado = new ArrayList<>();
                                                                            arrayReordenado.add(0, caminhoFotoPerfil);
                                                                            arrayReordenado.add(1,listaCaminhoUpdate.get(0));
                                                                            arrayReordenado.add(2,listaCaminhoUpdate.get(1));
                                                                            arrayReordenado.add(3,listaCaminhoUpdate.get(2));
                                                                            complementoFotoRef.setValue(arrayReordenado);
                                                                        }

                                                                        baseFotosPostagemRef.setValue(dadosPostagemExistente).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    progressDialog.dismiss();

                                                                                    //Enviando imagem para edição de foto em outra activity.
                                                                                    Intent i = new Intent(getActivity(), EdicaoFotoActivity.class);
                                                                                    i.putExtra("fotoUsuario", caminhoFotoPerfil);
                                                                                    i.putExtra("idPostagem", idUsuario+contadorNovo);
                                                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(i);
                                                                                }else{
                                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getContext());
                                                                                }
                                                                            }
                                                                        });

                                                                    }
                                                                    pesquisarDadoExistenteNewRef.removeEventListener(this);
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }
                                                    });

                                                }
                                            });
                                        }catch (Exception ex){
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } else {
                            //Caso usuário não tenha postado nenhuma foto.
                            progressDialog.setMessage("Fazendo upload da imagem, por favor aguarde...");
                            progressDialog.show();

                            //Primeira postagem do usuário
                            HashMap<String, Object> dadosPostagemNovas = new HashMap<>();
                            dadosPostagemNovas.put("idPostagem", idUsuario+1);

                            //Salvar imagem no firebase
                            imagemRef = storageRef
                                    .child("imagens")
                                    .child("fotosUsuario")
                                    .child(idUsuario)
                                    .child("fotoUsuario" + 0 + ".jpeg");


                            UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    ToastCustomizado.toastCustomizadoCurto("Erro ao fazer upload da imagem", getContext());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    ToastCustomizado.toastCustomizadoCurto("Sucesso ao fazer upload da imagem", getContext());

                                    contadorFotosRef.setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                        Uri url = task.getResult();

                                                        String caminhoFotoPerfil = url.toString();

                                                        //Salvando o referência da imagem.
                                                        dadosPostagemNovas.put("caminhoPostagem", caminhoFotoPerfil);

                                                        //Salvando a data de acordo com a região do usuário.
                                                        //(America/Sao_Paulo -- America/Montreal)
                                                        if (localConvertido.equals("pt_BR")) {
                                                            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                            date = new Date();
                                                            String novaData = dateFormat.format(date);
                                                        //Salvando a data em formato português.
                                                            dadosPostagemNovas.put("dataPostagem", novaData);
                                                            dadosPostagemNovas.put("dataPostagemNova", date);
                                                        } else {
                                                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                                            date = new Date();
                                                            String novaData = dateFormat.format(date);
                                                        //Salvando a data em formato inglês.
                                                            dadosPostagemNovas.put("dataPostagem", novaData);
                                                            dadosPostagemNovas.put("dataPostagemNova", date);
                                                        }

                                                        //Salvando o título da postagem.
                                                        dadosPostagemNovas.put("tituloPostagem", "");
                                                        //Salvando a descrição da postagem.
                                                        dadosPostagemNovas.put("descricaoPostagem", "");
                                                        //Salvando o id do usuario
                                                        dadosPostagemNovas.put("idDonoPostagem", idUsuario);
                                                        dadosPostagemNovas.put("totalViewsFotoPostagem", 0);
                                                        dadosPostagemNovas.put("publicoPostagem", "Todos");
                                                        //Novo
                                                        DatabaseReference complementoFotoRef = firebaseRef.child("complementoFoto")
                                                                .child(idUsuario).child("listaCaminhoFotos");
                                                        listaCaminhoUpdate.add(caminhoFotoPerfil);
                                                        complementoFotoRef.setValue(listaCaminhoUpdate);

                                                        //Salvando todos dados do nó fotosUsuario
                                                        baseFotosPostagemRef.setValue(dadosPostagemNovas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    progressDialog.dismiss();
                                                                    //Enviando imagem para edição de foto para outra activity.
                                                                    Intent i = new Intent(getActivity(), EdicaoFotoActivity.class);
                                                                    i.putExtra("fotoUsuario", caminhoFotoPerfil);
                                                                    i.putExtra("idPostagem", idUsuario+1);
                                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(i);
                                                                }else{
                                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao salvar, tente novamente!", getContext());
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        dadosFotosUsuarioRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(getContext(), this);
    }

    //*Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptions(){
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar foto");
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }

    private void exibirPostagens(){

        DatabaseReference dadosUsuarioAtualRef = firebaseRef
                .child("usuarios").child(idUsuario);

        dadosUsuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                   Usuario usuario = snapshot.getValue(Usuario.class);

                    if (adapterGridPostagem != null) {

                    }else{
                        adapterGridPostagem = new AdapterGridPostagem(listaPostagem, getContext(), usuario.getEpilepsia());
                    }

                    recyclerPostagem.setAdapter(adapterGridPostagem);

                    DatabaseReference verificarPostagemRef = firebaseRef
                            .child("complementoPostagem").child(idUsuario);

                    verificarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null){
                                try{
                                    Postagem postagemComplemento = snapshot.getValue(Postagem.class);
                                    if(postagemComplemento.getTotalPostagens() <= 0){
                                        recyclerPostagem.setVisibility(View.GONE);
                                        txtViewSemPostagemMsg.setVisibility(View.VISIBLE);
                                        btnTodasPostagens.setVisibility(View.INVISIBLE);
                                    }else{
                                        txtViewSemPostagemMsg.setVisibility(View.GONE);
                                        recyclerPostagem.setVisibility(View.VISIBLE);
                                        btnTodasPostagens.setVisibility(View.VISIBLE);
                                        listaPostagem.clear(); //Adicionado hoje - 12/07/2022 - 10:32
                                        DatabaseReference adicionarPostagemRef = firebaseRef.child("postagens")
                                                .child(idUsuario);
                                        adicionarPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.getValue() != null){
                                                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                                        Postagem postagemExibida = snapshot1.getValue(Postagem.class);
                                                        listaPostagem.add(postagemExibida);
                                                        Collections.sort(listaPostagem, new Comparator<Postagem>() {
                                                            public int compare(Postagem o1, Postagem o2) {
                                                                return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
                                                            }
                                                        });
                                                        adapterGridPostagem.notifyDataSetChanged();
                                                    }
                                                }
                                                adicionarPostagemRef.removeEventListener(this);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }else{
                                recyclerPostagem.setVisibility(View.GONE);
                                txtViewSemPostagemMsg.setVisibility(View.VISIBLE);
                                btnTodasPostagens.setVisibility(View.INVISIBLE);
                            }
                            verificarPostagemRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                dadosUsuarioAtualRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

