package com.example.ogima.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.activity.ConfigurarFotoActivity;
import com.example.ogima.activity.FollowersAndFollowingActivity;
import com.example.ogima.activity.daily.DailyShortsActivity;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.activity.FriendInteractionsActivity;
import com.example.ogima.activity.PostagemActivity;
import com.example.ogima.activity.ProfileViewsActivity;
import com.example.ogima.adapter.AdapterGridPostagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.BottomNavBarStyle;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.DateUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView imgViewFundoProfile, imgViewFotoProfile, imgViewDailyShortInc;
    private TextView txtViewNameProfile, txtViewTitleSeguidores,
            txtViewTitleAmigos, txtViewTitleSeguindo, txtViewNrSeguidores,
            txtViewNrAmigos, txtViewNrSeguindo, txtViewTitleViewsProfile,
            txtViewVerVisualizacoes, txtViewTitleSolicitacao, txtViewNrSolicitacoes,
            txtViewTitleDailyShortInc, txtViewMsgSemFotos, txtViewSemPostagemMsg,
            txtViewTitlePostagem;
    private View viewBarraFundo;
    private ImageButton imgBtnEditarProfile, imgBtnViewsProfile,
            imgBtnSolicitacaoAmizade, imgBtnCamFotoProfile, imgBtnGaleriaFotoProfile,
            imgBtnVideoPostagem, imgBtnGifPostagem, imgBtnGaleriaPostagem, imgBtnCameraPostagem,
            imgBtnTextPostagem;
    private CardView cardSolicitacaoAmizade;
    private RecyclerView recyclerViewFotos, recyclerViewPostagens;
    private Button btnViewAddPostagens;

    private StorageReference storageRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private long nrSeguidores = 0;
    private boolean atualizarProfile = true;
    private GridLayoutManager gridLayoutManagerFoto;
    private GridLayoutManager gridLayoutManagerPostagem;
    private AdapterGridPostagem adapterGridFoto;
    private AdapterGridPostagem adapterGridPostagem;
    private List<Postagem> listaFotos = new ArrayList<>();
    private List<Postagem> listaPostagens = new ArrayList<>();

    private boolean existemSeguidores = false;
    private boolean existemAmigos = false;
    private boolean existemSeguindo = false;
    private boolean existemSolicitacoes = false;
    private boolean existemVisualizacoes = false;

    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private static final int CODE_PERMISSION_GALERIA = 22;
    private ProgressDialog progressDialog;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET};
    private Uri fotoSelecionada = null;
    private PictureSelectorStyle selectorStyle;
    private String tipoMidiaPermissao = "";

    private ImageButton imgBtnCoins;
    private TextView txtViewCoins;

    @Override
    public void onStart() {
        super.onStart();

        recuperarDadosUsuario();


        if (atualizarProfile) {
            atualizarProfile = false;
            configRecyclers();
            recuperarFotos();
            recuperarPostagens();
        }
    }

    public ProfileFragment() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        inicializandoComponentes(view);

        clickListeners();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(requireContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setMessage("Processando a mídia selecionada, aguarde um momento.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        selectorStyle = new PictureSelectorStyle();

        configStylePictureSelector();

        return view;
    }

    private void clickListeners() {
        viewBarraFundo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaEdicaoDePerfil();
            }
        });

        imgBtnEditarProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaEdicaoDePerfil();
            }
        });

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

        txtViewTitleSolicitacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("solicitacoes");
            }
        });

        txtViewTitleViewsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("visualizacoes");
            }
        });

        imgBtnViewsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("visualizacoes");
            }
        });

        txtViewVerVisualizacoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("visualizacoes");
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

        txtViewNrSolicitacoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRelacoes("solicitacoes");
            }
        });

        btnViewAddPostagens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irCriarPostagem(false, null);
            }
        });

        imgBtnVideoPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irCriarPostagem(true, "video");
            }
        });

        imgBtnGifPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irCriarPostagem(true, "gif");
            }
        });

        imgBtnGaleriaPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irCriarPostagem(true, "galeria");
            }
        });

        imgBtnCameraPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irCriarPostagem(true, "camera");
            }
        });

        imgBtnTextPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irCriarPostagem(true, "texto");
            }
        });

        imgViewDailyShortInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verDailyShort();
            }
        });

        imgBtnCamFotoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipoMidiaPermissao = "camera";
                checkPermissions("camera");
            }
        });

        imgBtnGaleriaFotoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipoMidiaPermissao = "galeria";
                checkPermissions("galeria");
            }
        });
    }

    private void recuperarDadosUsuario() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (nomeUsuarioAjustado != null) {
                    txtViewNameProfile.setText(nomeUsuarioAjustado);
                }

                exibirFotoFundo(fotoUsuario, fundoUsuario, epilepsia);

                recuperarNrSeguidores();

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

                if (usuarioAtual.getViewsPerfil() != -1
                        && usuarioAtual.getViewsPerfil() > 0) {
                    txtViewTitleViewsProfile.setText(String.valueOf(usuarioAtual.getViewsPerfil())
                            + " visualizações no seu perfil hoje!");
                    existemVisualizacoes = true;
                } else {
                    txtViewTitleViewsProfile.setText("0 visualizações no seu perfil hoje");
                    existemVisualizacoes = false;
                }

                if (usuarioAtual.getPedidosAmizade() != -1
                        && usuarioAtual.getPedidosAmizade() > 0) {
                    txtViewNrSolicitacoes.setText(String.valueOf(usuarioAtual.getPedidosAmizade()));
                    existemSolicitacoes = true;
                } else {
                    txtViewNrSolicitacoes.setText("0");
                    existemSolicitacoes = false;
                }

                if (usuarioAtual.getUrlLastDaily() != null
                        && !usuarioAtual.getUrlLastDaily().isEmpty()) {
                    exibirUltimoDaily(usuarioAtual.getUrlLastDaily(), epilepsia);
                }
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void exibirUltimoDaily(String urlDaily, boolean epilpesia) {
        GlideCustomizado.loadUrl(requireContext(),
                urlDaily, imgViewDailyShortInc, android.R.color.transparent,
                GlideCustomizado.CIRCLE_CROP, false, epilpesia);
    }

    private void exibirFotoFundo(String fotoUsuario, String fundoUsuario, boolean epilepsia) {

        if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
            GlideCustomizado.loadUrl(requireContext(),
                    fotoUsuario, imgViewFotoProfile, android.R.color.transparent, GlideCustomizado.CIRCLE_CROP,
                    false, epilepsia);
        }

        if (fundoUsuario != null && !fundoUsuario.isEmpty()) {
            GlideCustomizado.loadUrl(requireContext(),
                    fundoUsuario, imgViewFundoProfile, android.R.color.transparent, GlideCustomizado.CENTER_CROP,
                    false, epilepsia);
        }
    }

    private void recuperarNrSeguidores() {
        DatabaseReference verificaSeguidoresRef = firebaseRef.child("seguidores")
                .child(idUsuario);

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

    private void irParaEdicaoDePerfil() {
        Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
        intent.putExtra("irParaProfile", "irParaProfile");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void configRecyclers() {

        //Fotos
        if (gridLayoutManagerFoto == null) {
            gridLayoutManagerFoto = new GridLayoutManager(requireContext(), 2);
        }

        recyclerViewFotos.setHasFixedSize(true);
        recyclerViewFotos.setLayoutManager(gridLayoutManagerFoto);

        if (adapterGridFoto == null) {
            adapterGridFoto = new AdapterGridPostagem(listaFotos, requireContext(), true);
        }

        recyclerViewFotos.setAdapter(adapterGridFoto);

        //Postagens
        if (gridLayoutManagerPostagem == null) {
            gridLayoutManagerPostagem = new GridLayoutManager(requireContext(), 2);
        }

        recyclerViewPostagens.setHasFixedSize(true);
        recyclerViewPostagens.setLayoutManager(gridLayoutManagerPostagem);

        if (adapterGridPostagem == null) {
            adapterGridPostagem = new AdapterGridPostagem(listaPostagens, requireContext(), false);
        }
        recyclerViewPostagens.setAdapter(adapterGridPostagem);
    }

    private void recuperarFotos() {
        Query recuperarFotos = firebaseRef.child("fotos")
                .child(idUsuario).orderByChild("timeStampNegativo")
                .limitToFirst(4);

        recuperarFotos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Postagem fotoPostagem = snapshot1.getValue(Postagem.class);
                        adicionarFotoNaLista(fotoPostagem);
                    }
                    mudarParametroLayoutFoto();
                    adapterGridFoto.notifyDataSetChanged();
                } else {
                    mudarParametroLayoutFoto();
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
                .child(idUsuario).orderByChild("timeStampNegativo")
                .limitToFirst(4);

        recuperarPostagens.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Postagem postagem = snapshot1.getValue(Postagem.class);
                        adicionarPostagemNaLista(postagem);
                    }
                    mudarParametroLayoutPostagem();
                    adapterGridPostagem.notifyDataSetChanged();
                } else {
                    mudarParametroLayoutPostagem();
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

    private void mudarParametroLayoutFoto() {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) txtViewTitlePostagem.getLayoutParams();

        if (listaFotos != null && listaFotos.size() > 0) {
            // Define uma nova regra de layout_below com um novo componente
            params.addRule(RelativeLayout.BELOW, R.id.recyclerViewFotosProfile);

            // Aplica as alterações nos parâmetros de layout
            txtViewTitlePostagem.setLayoutParams(params);
            recyclerViewFotos.setVisibility(View.VISIBLE);
            txtViewMsgSemFotos.setVisibility(View.INVISIBLE);

        } else {
            // Define uma nova regra de layout_below com um novo componente
            params.addRule(RelativeLayout.BELOW, R.id.txtViewMsgSemFotos);

            // Aplica as alterações nos parâmetros de layout
            txtViewTitlePostagem.setLayoutParams(params);
            recyclerViewFotos.setVisibility(View.GONE);
            txtViewMsgSemFotos.setVisibility(View.VISIBLE);
        }
    }

    private void mudarParametroLayoutPostagem() {

        RelativeLayout.LayoutParams paramsPost = (RelativeLayout.LayoutParams) btnViewAddPostagens.getLayoutParams();

        if (listaPostagens != null && listaPostagens.size() > 0) {
            paramsPost.addRule(RelativeLayout.BELOW, R.id.recyclerViewPostagensProfile);
            btnViewAddPostagens.setLayoutParams(paramsPost);
            txtViewSemPostagemMsg.setVisibility(View.INVISIBLE);
            recyclerViewPostagens.setVisibility(View.VISIBLE);
        } else {
            paramsPost.addRule(RelativeLayout.BELOW, R.id.txtViewSemPostagemMsg);
            btnViewAddPostagens.setLayoutParams(paramsPost);
            txtViewSemPostagemMsg.setVisibility(View.VISIBLE);
            recyclerViewPostagens.setVisibility(View.GONE);
        }
    }

    private void irParaRelacoes(String destino) {
        switch (destino) {
            case "seguidores":
                if (existemSeguidores) {
                    Intent intent = new Intent(getActivity(), FollowersAndFollowingActivity.class);
                    intent.putExtra("tipoFragment", getString(R.string.followers));
                    intent.putExtra("idDonoPerfil", idUsuario);
                    intent.putExtra("voltarParaProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "seguindo":
                if (existemSeguindo) {
                    Intent intent = new Intent(getActivity(), FollowersAndFollowingActivity.class);
                    intent.putExtra("tipoFragment", getString(R.string.following));
                    intent.putExtra("idDonoPerfil", idUsuario);
                    intent.putExtra("voltarParaProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "amigos":
                if (existemAmigos) {
                    Intent intent = new Intent(getActivity(), FriendInteractionsActivity.class);
                    intent.putExtra("tipoFragment", getString(R.string.friends));
                    intent.putExtra("idDonoPerfil", idUsuario);
                    intent.putExtra("voltarParaProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "solicitacoes":
                if (existemSolicitacoes) {
                    Intent intent = new Intent(getActivity(), FriendInteractionsActivity.class);
                    intent.putExtra("tipoFragment", getString(R.string.requests));
                    intent.putExtra("idDonoPerfil", idUsuario);
                    intent.putExtra("voltarParaProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case "visualizacoes":
                if (existemVisualizacoes) {
                    Intent intent = new Intent(getActivity(), ProfileViewsActivity.class);
                    intent.putExtra("viewsPerfil", "viewsPerfil");
                    intent.putExtra("irParaProfile", "irParaProfile");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
        }
    }

    private void irCriarPostagem(boolean selecaoPreDefinidade, String tipoPostagem) {
        if (selecaoPreDefinidade && tipoPostagem != null) {
            Intent intent = new Intent(getActivity(), PostagemActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            intent.putExtra("selecaoPreDefinida", tipoPostagem);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), PostagemActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void verDailyShort() {
        if (idUsuario != null && !idUsuario.isEmpty()) {
            Intent intent = new Intent(requireContext(), DailyShortsActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            intent.putExtra("idUsuarioDaily", idUsuario);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void configStylePictureSelector() {
        TitleBarStyle blueTitleBarStyle = new TitleBarStyle();
        blueTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));

        BottomNavBarStyle numberBlueBottomNavBarStyle = new BottomNavBarStyle();
        numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_9b));
        numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));
        numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_white));
        numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.ps_demo_blue_num_selected);
        numberBlueBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_53575e));
        numberBlueBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_53575e));

        SelectMainStyle numberBlueSelectMainStyle = new SelectMainStyle();
        numberBlueSelectMainStyle.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectNumberStyle(true);
        numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true);

        numberBlueSelectMainStyle.setSelectBackground(R.drawable.ps_demo_blue_num_selector);
        numberBlueSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_white));
        numberBlueSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_preview_blue_num_selector);

        numberBlueSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_9b));
        numberBlueSelectMainStyle.setSelectTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectText(R.string.ps_completed);

        selectorStyle.setTitleBarStyle(blueTitleBarStyle);
        selectorStyle.setBottomBarStyle(numberBlueBottomNavBarStyle);
    }


    private void checkPermissions(String tipoMidia) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissoesNecessarias) {
                if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toArray(new String[0]), CODE_PERMISSION_GALERIA);
            } else {
                configClickListenerPorMidia(tipoMidia);
            }
        } else {
            configClickListenerPorMidia(tipoMidia);
        }
    }

    private void configClickListenerPorMidia(String tipoMidia) {

        switch (tipoMidia) {
            case "galeria":
                abrirGaleria();
                break;
            case "camera":
                abrirCamera();
                break;
        }
    }

    private void abrirGaleria() {
        PictureSelector.create(requireContext())
                .openGallery(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1) // Permitir seleção múltipla de fotos
                .setSelectorUIStyle(selectorStyle)
                .setSelectMaxFileSize(MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine()) // Substitua GlideEngine pelo seu próprio mecanismo de carregamento de imagem, se necessário
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {

                        limparUri();

                        //ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {

                                // Faça o que for necessário com cada foto selecionada
                                String path = media.getPath(); // Obter o caminho do arquivo da foto

                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void abrirCamera() {
        PictureSelector.create(requireContext())
                .openCamera(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setSelectMaxFileSize(MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {

                        limparUri();

                        //ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {

                                // Faça o que for necessário com cada foto selecionada
                                String path = media.getPath(); // Obter o caminho do arquivo da foto

                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_PERMISSION_GALERIA) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                if (tipoMidiaPermissao != null && !tipoMidiaPermissao.isEmpty()) {
                    configClickListenerPorMidia(tipoMidiaPermissao);
                }
            } else {
                // Permissions were not granted, handle it accordingly
                ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", requireContext());
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            if (data != null) {
                try {

                    //Somente fotos chamaram o UCrop.REQUEST_CROP.
                    Uri imagemCortada = UCrop.getOutput(data);

                    if (imagemCortada != null) {
                        exibirProgressDialog();

                        ocultarProgressDialog();

                        configurarNovaFoto(imagemCortada);

                    } else {
                        ocultarProgressDialog();
                    }
                } catch (Exception ex) {
                    ocultarProgressDialog();
                    ex.printStackTrace();
                }
            }
        }
    }

    private void limparUri() {
        if (fotoSelecionada != null) {
            fotoSelecionada = null;
        }
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(requireActivity(), this);

    }

    //*Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptions() {
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar imagem");
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }

    private void exibirProgressDialog() {
        if (getActivity() != null && progressDialog != null) {
            progressDialog.show();
        }
    }

    private void ocultarProgressDialog() {
        if (getActivity() != null && progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private Uri destinoImagemUri(ArrayList<LocalMedia> result) {

        Uri destinationUri = null;

        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
                File outputFile = new File(requireContext().getCacheDir(), fileName);
                destinationUri = Uri.fromFile(outputFile);
                //ToastCustomizado.toastCustomizado("Caminho: " + destinationUri, getApplicationContext());
                Log.d("Caminho ", String.valueOf(destinationUri));
                break; // Sai do loop após encontrar a primeira imagem
            }
        }

        return destinationUri;
    }

    private void configurarNovaFoto(Uri novaUri) {
        if (novaUri != null) {
            Intent intent = new Intent(requireContext(), ConfigurarFotoActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            intent.putExtra("uriRecuperada", novaUri);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void inicializandoComponentes(View view) {
        //Include

        //inc_perfil_cabecalho
        imgViewFundoProfile = view.findViewById(R.id.imgViewIncFundoProfile);
        imgViewFotoProfile = view.findViewById(R.id.imgViewIncFotoProfile);
        txtViewNameProfile = view.findViewById(R.id.txtViewNameProfile);

        //inc_perfil_card_relacoes
        txtViewTitleSeguidores = view.findViewById(R.id.txtViewTitleSeguidores);
        txtViewTitleAmigos = view.findViewById(R.id.txtViewTitleAmigos);
        txtViewTitleSeguindo = view.findViewById(R.id.txtViewTitleSeguindo);
        txtViewNrSeguidores = view.findViewById(R.id.txtViewNrSeguidores);
        txtViewNrAmigos = view.findViewById(R.id.txtViewNrAmigos);
        txtViewNrSeguindo = view.findViewById(R.id.txtViewNrSeguindo);

        //inc_daily_short
        imgViewDailyShortInc = view.findViewById(R.id.imgViewDailyShortInc);
        txtViewTitleDailyShortInc = view.findViewById(R.id.txtViewTitleDailyShortInc);

        //layout
        viewBarraFundo = view.findViewById(R.id.viewBarraFundo);
        imgBtnEditarProfile = view.findViewById(R.id.imgBtnEditarProfile);
        imgBtnViewsProfile = view.findViewById(R.id.imgBtnViewsProfile);
        txtViewTitleViewsProfile = view.findViewById(R.id.txtViewTitleViewsProfile);
        txtViewVerVisualizacoes = view.findViewById(R.id.txtViewVerVisualizacoesProfile);
        cardSolicitacaoAmizade = view.findViewById(R.id.cardSolicitacaoAmizade);
        imgBtnSolicitacaoAmizade = view.findViewById(R.id.imgBtnSolicitacaoAmizade);
        txtViewTitleSolicitacao = view.findViewById(R.id.txtViewTitleSolicitacaoAmizade);
        txtViewNrSolicitacoes = view.findViewById(R.id.txtViewNrSolicitacoesAmizade);
        imgBtnCamFotoProfile = view.findViewById(R.id.imgBtnCamFotoProfile);
        imgBtnGaleriaFotoProfile = view.findViewById(R.id.imgBtnGaleriaFotoProfile);
        txtViewMsgSemFotos = view.findViewById(R.id.txtViewMsgSemFotos);
        txtViewSemPostagemMsg = view.findViewById(R.id.txtViewSemPostagemMsg);
        recyclerViewFotos = view.findViewById(R.id.recyclerViewFotosProfile);
        recyclerViewPostagens = view.findViewById(R.id.recyclerViewPostagensProfile);
        btnViewAddPostagens = view.findViewById(R.id.btnViewAddPostagensProfile);
        imgBtnVideoPostagem = view.findViewById(R.id.imgBtnVideoPostagem);
        imgBtnGifPostagem = view.findViewById(R.id.imgBtnGifPostagem);
        imgBtnGaleriaPostagem = view.findViewById(R.id.imgBtnGaleriaPostagem);
        imgBtnCameraPostagem = view.findViewById(R.id.imgBtnCameraPostagem);
        imgBtnTextPostagem = view.findViewById(R.id.imgBtnTextPostagem);
        txtViewTitlePostagem = view.findViewById(R.id.txtViewTitlePostagem);

        imgBtnCoins = view.findViewById(R.id.imgBtnCoins);
        txtViewCoins = view.findViewById(R.id.txtViewCoins);
    }
}