package com.example.ogima.fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.CommonPosting;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ExoPlayerUnicoUtils;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PostUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Postagem;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

public class VideoPostFragment extends Fragment {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private TextView txtViewTitleToolbar, txtViewLimiteCaracteres;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private AutoCompleteTextView autoCompleteTextView;
    private LinearLayout linearLayoutInteresses;
    private EditText edtTxtDescricao;
    private Button btnSalvar;
    private Uri uriRecuperada = null;
    private ProgressDialog progressDialog;
    private PostUtils postUtils;
    private MidiaUtils midiaUtils;
    private StorageReference storageRef;
    private boolean edicao = false;
    private Postagem postagemEdicao;
    private SpinKitView spinKitPost;
    private CommonPosting commonPosting;

    private StyledPlayerView styledPlayer;
    private ExoPlayer exoPlayer;
    private boolean isControllerVisible = false;
    private FrameLayout framePreviewPostagem;
    private ExoPlayerUnicoUtils exoPlayerUnicoUtils;

    public VideoPostFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (exoPlayerUnicoUtils != null) {
            exoPlayerUnicoUtils.pauseExo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayerUnicoUtils != null) {
            exoPlayerUnicoUtils.resumeExo();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayerUnicoUtils != null) {
            exoPlayerUnicoUtils.releaseExo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_post, container, false);
        inicializarComponentes(view);
        configInicial();
        configBundle();
        clickListeners();
        return view;
    }

    private void configBundle() {
        Bundle args = getArguments();
        if (args != null) {
            edicao = commonPosting.edicao(args);
            if (edicao) {
                postagemEdicao = new Postagem();
                postagemEdicao = commonPosting.postagemEdicao(args);
                if (postagemEdicao != null) {
                    exibirDadosEdicao();
                } else {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_post), requireContext());
                    finalizarExoPlayer();
                    commonPosting.finalizarActivity();
                }
                return;
            }
            uriRecuperada = commonPosting.recuperarUri(args);
            commonPosting.exibirUriVideo(exoPlayer, styledPlayer, spinKitPost, uriRecuperada);
        }
    }

    private void exibirDadosEdicao() {
        commonPosting.exibirVideoEdicao(postagemEdicao, exoPlayer,
                styledPlayer, spinKitPost);
        commonPosting.exibirDescricaoEdicao(postagemEdicao, edtTxtDescricao);
        commonPosting.exibirInteressesEdicao(postagemEdicao, autoCompleteTextView, linearLayoutInteresses);
    }

    private void salvarPostagem() {
        if (uriRecuperada == null && !edicao) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post), requireContext());
            finalizarExoPlayer();
            commonPosting.finalizarActivity();
        }

        if (postUtils.getInteressesMarcadosComAssento() == null ||
                postUtils.getInteressesMarcadosComAssento() != null
                        && postUtils.interessesMarcadosComAssento.size() <= 0) {
            ToastCustomizado.toastCustomizado(getString(R.string.minimum_interest_in_post), requireContext());
            return;
        }

        if (edicao) {
            if (postagemEdicao != null) {
                if (exoPlayerUnicoUtils != null) {
                    exoPlayerUnicoUtils.pauseExo();
                }
                postUtils.exibirProgressDialog(progressDialog, "edicao");
                commonPosting.salvarEdicao(postagemEdicao, edtTxtDescricao);
                return;
            } else {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_when_editing_post), requireContext());
                finalizarExoPlayer();
                commonPosting.finalizarActivity();
                return;
            }
        }
        if (exoPlayerUnicoUtils != null) {
            exoPlayerUnicoUtils.pauseExo();
        }
        postUtils.exibirProgressDialog(progressDialog, "upload");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nomeRandomico = UUID.randomUUID().toString();
        String nomeArquivo = String.format("%s%s%s%s", "video", timestamp, nomeRandomico, ".mp4");
        StorageReference midiaRef = storageRef.child("postagens")
                .child("videos")
                .child(idUsuario)
                .child(nomeArquivo);

        midiaUtils.uparFotoNoStorage(midiaRef, uriRecuperada, new MidiaUtils.UparNoStorageCallback() {
            DatabaseReference postagemRef = firebaseRef.child("postagens")
                    .child(idUsuario);
            String idPostagem = postUtils.retornarIdRandom(postagemRef);
            String descricao = edtTxtDescricao.getText().toString().trim();
            @Override
            public void onConcluido(String urlPost) {
                if (idPostagem != null && !idPostagem.isEmpty()) {
                    postUtils.prepararHashMap(idUsuario, idPostagem, "video", urlPost,
                            descricao, new PostUtils.PrepararHashMapPostCallback() {
                                @Override
                                public void onConcluido(HashMap<String, Object> hashMapPost) {
                                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.post_published_successfully), requireContext());
                                    finalizarExoPlayer();
                                    commonPosting.finalizarActivity();
                                }

                                @Override
                                public void onError(String message) {
                                    ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                                    postUtils.ocultarProgressDialog(progressDialog);
                                }
                            });
                } else {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post), requireContext());
                    finalizarExoPlayer();
                    commonPosting.finalizarActivity();
                }
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post), requireContext());
                postUtils.ocultarProgressDialog(progressDialog);
            }
        });
    }

    private void finalizarExoPlayer(){
        if (exoPlayerUnicoUtils != null) {
            exoPlayerUnicoUtils.releaseExo();
        }
    }

    private void configInicial() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbarIncPadrao);
        ((AppCompatActivity) requireActivity()).setTitle("");
        txtViewTitleToolbar.setText(getString(R.string.configure_post));
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        progressDialog = new ProgressDialog(requireContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        exoPlayerUnicoUtils = new ExoPlayerUnicoUtils(exoPlayer);
        postUtils = new PostUtils(requireActivity(), requireContext());
        midiaUtils = new MidiaUtils(requireActivity(), requireContext());
        commonPosting = new CommonPosting(requireActivity(), requireContext(), progressDialog, postUtils);
        postUtils.limitarCaracteresDescricao(edtTxtDescricao, txtViewLimiteCaracteres);
        postUtils.configurarTopicos(autoCompleteTextView, linearLayoutInteresses);
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalizarExoPlayer();
                commonPosting.finalizarActivity();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarPostagem();
            }
        });

        styledPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isControllerVisible) {
                    styledPlayer.hideController();
                    styledPlayer.setUseController(false);
                    isControllerVisible = false;
                } else {
                    styledPlayer.setUseController(true);
                    styledPlayer.showController();
                    isControllerVisible = true;
                }
            }
        });
    }

    private void inicializarComponentes(View view) {
        txtViewTitleToolbar = view.findViewById(R.id.txtViewIncTituloToolbarBlack);
        toolbarIncPadrao = view.findViewById(R.id.toolbarIncBlack);
        imgBtnIncBackPadrao = view.findViewById(R.id.imgBtnIncBackBlack);
        autoCompleteTextView = view.findViewById(R.id.autoCompleteInteressesPost);
        linearLayoutInteresses = view.findViewById(R.id.linearLayoutInteressesMarcados);
        edtTxtDescricao = view.findViewById(R.id.edtTxtDescPost);
        txtViewLimiteCaracteres = view.findViewById(R.id.txtViewLimiteDescPost);
        btnSalvar = view.findViewById(R.id.btnSalvarPost);
        spinKitPost = view.findViewById(R.id.spinKitPost);
        framePreviewPostagem = view.findViewById(R.id.framePreviewPostagem);
        styledPlayer = view.findViewById(R.id.styledPlayerPostagem);
    }
}