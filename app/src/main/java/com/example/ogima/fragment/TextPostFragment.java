package com.example.ogima.fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.CommonPosting;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PostUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Postagem;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class TextPostFragment extends Fragment {

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
    private boolean edicao = false;
    private Postagem postagemEdicao;
    private SpinKitView spinKitPost;
    private CommonPosting commonPosting;
    private boolean epilepsia = true;
    private String urlGif = "";


    public TextPostFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_post, container, false);
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
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_displaying_post, getString(R.string.post)), requireContext());
                    commonPosting.finalizarActivity();
                }
                return;
            }
            urlGif = commonPosting.recuperarUrlGif(args);
        }
    }

    private void exibirDadosEdicao() {
        commonPosting.exibirDescricaoEdicao(postagemEdicao, edtTxtDescricao);
        commonPosting.exibirInteressesEdicao(postagemEdicao, autoCompleteTextView, linearLayoutInteresses);
    }

    private void salvarPostagem() {
        String textoPostagem = edtTxtDescricao.getText().toString().trim();

        if (textoPostagem == null || textoPostagem != null
                && textoPostagem.isEmpty()) {
            ToastCustomizado.toastCustomizado(getString(R.string.character_limit_reached, Postagem.MIN_LENGTH_DESCRIPTION, Postagem.MAX_LENGTH_DESCRIPTION), requireContext());
            return;
        }

        if (textoPostagem != null && !textoPostagem.isEmpty()
                && textoPostagem.length() <= Postagem.MIN_LENGTH_DESCRIPTION) {
            ToastCustomizado.toastCustomizado(getString(R.string.character_limit_reached, Postagem.MIN_LENGTH_DESCRIPTION, Postagem.MAX_LENGTH_DESCRIPTION), requireContext());
            return;
        }

        if (postUtils.getInteressesMarcadosComAssento() == null ||
                postUtils.getInteressesMarcadosComAssento() != null
                        && postUtils.interessesMarcadosComAssento.size() <= 0) {
            ToastCustomizado.toastCustomizado(getString(R.string.minimum_interest_in_post), requireContext());
            return;
        }

        if (edicao) {
            if (postagemEdicao != null) {
                postUtils.exibirProgressDialog(progressDialog, "edicao");
                commonPosting.salvarEdicao(postagemEdicao, edtTxtDescricao);
                return;
            } else {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_when_editing_post, getString(R.string.post)), requireContext());
                commonPosting.finalizarActivity();
                return;
            }
        }

        postUtils.exibirProgressDialog(progressDialog, "config");
        DatabaseReference postagemRef = firebaseRef.child("postagens")
                .child(idUsuario);
        String idPostagem = postUtils.retornarIdRandom(postagemRef);
        if (idPostagem != null && !idPostagem.isEmpty()) {
            postUtils.prepararHashMap(idUsuario, idPostagem, "texto", "",
                    textoPostagem, new PostUtils.PrepararHashMapPostCallback() {
                        @Override
                        public void onConcluido(HashMap<String, Object> hashMapPost) {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.post_published_successfully), requireContext());
                            commonPosting.finalizarActivity();
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                            postUtils.ocultarProgressDialog(progressDialog);
                        }
                    });
        } else {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post, getString(R.string.post)), requireContext());
            commonPosting.finalizarActivity();
        }
    }

    private void configInicial() {
        configEdtTextParaTexto();
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbarIncPadrao);
        ((AppCompatActivity) requireActivity()).setTitle("");
        txtViewTitleToolbar.setText(getString(R.string.configure_post));
        progressDialog = new ProgressDialog(requireContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        postUtils = new PostUtils(requireActivity(), requireContext());
        midiaUtils = new MidiaUtils(requireActivity(), requireContext());
        commonPosting = new CommonPosting(requireActivity(), requireContext(), progressDialog, postUtils, getString(R.string.post));
        postUtils.limitarCaracteresDescricao(edtTxtDescricao, txtViewLimiteCaracteres);
        postUtils.configurarTopicos(autoCompleteTextView, linearLayoutInteresses);
    }

    private void configEdtTextParaTexto() {
        edtTxtDescricao.setMaxLines(18);
        int alturaDp = 400;
        int alturaPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, alturaDp, getResources().getDisplayMetrics()
        );
        ViewGroup.LayoutParams params = edtTxtDescricao.getLayoutParams();
        params.height = alturaPixels;
        edtTxtDescricao.setLayoutParams(params);
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commonPosting.finalizarActivity();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarPostagem();
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
    }
}