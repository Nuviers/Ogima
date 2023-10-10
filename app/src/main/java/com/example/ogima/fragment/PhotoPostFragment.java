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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PostUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PhotoPostFragment extends Fragment {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private TextView txtViewTitleToolbar, txtViewLimiteCaracteres;
    private Toolbar toolbarIncPadrao;
    private ImageButton imgBtnIncBackPadrao;
    private ImageView imgViewPost;
    private AutoCompleteTextView autoCompleteTextView;
    private LinearLayout linearLayoutInteresses;
    private EditText edtTxtDescricao;
    private Button btnSalvar;
    private Uri uriFoto = null;
    private boolean edicao = false;
    private ProgressDialog progressDialog;
    private PostUtils postUtils;
    private MidiaUtils midiaUtils;
    private StorageReference storageRef;

    public PhotoPostFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_post, container, false);
        inicializarComponentes(view);
        configInicial();
        configBundle();
        clickListeners();
        return view;
    }

    private void configBundle() {
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("uriRecuperada")) {
                uriFoto = args.getParcelable("uriRecuperada");
                if (uriFoto != null) {
                    exibirUri();
                } else if (args.containsKey("edicao")) {
                    edicao = true;
                    exibirDadosEdicao();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao tratar mídia, tente novamente", requireContext());
                    if (!requireActivity().isFinishing()) {
                        requireActivity().onBackPressed();
                    }
                }
            }
        }
    }

    private void exibirDadosEdicao() {

    }

    private void exibirUri() {
        GlideCustomizado.loadUrl(requireContext(), String.valueOf(uriFoto),
                imgViewPost, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false,
                false);
    }

    private void salvarPostagem() {

        if (uriFoto == null) {
            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao salvar sua postagem, tente novamente", requireContext());
            requireActivity().onBackPressed();
        }

        if (postUtils.getInteressesMarcadosComAssento() == null ||
                postUtils.getInteressesMarcadosComAssento() != null
                        && postUtils.interessesMarcadosComAssento.size() <= 0) {
            ToastCustomizado.toastCustomizado("Necessário selecionar pelo menos um interesse relacionado a essa postagem", requireContext());
            return;
        }

        postUtils.exibirProgressDialog(progressDialog, "upload");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nomeRandomico = UUID.randomUUID().toString();
        String nomeArquivo = String.format("%s%s%s%s", "foto", timestamp, nomeRandomico, ".jpeg");
        StorageReference midiaRef = storageRef.child("postagens")
                .child("fotos")
                .child(idUsuario)
                .child(nomeArquivo);

        midiaUtils.uparFotoNoStorage(midiaRef, uriFoto, new MidiaUtils.UparNoStorageCallback() {
            DatabaseReference postagemRef = firebaseRef.child("postagens")
                    .child(idUsuario);
            String idPostagem = postUtils.retornarIdRandom(postagemRef);
            String descricao = edtTxtDescricao.getText().toString().trim();
            DatabaseReference salvarInteresseRef = firebaseRef.child("interessesPostagens")
                    .child(idPostagem);
            @Override
            public void onConcluido(String urlPost) {
                ToastCustomizado.toastCustomizadoCurto("Upload com sucesso " + urlPost, requireContext());
                if (idPostagem != null && !idPostagem.isEmpty()) {
                    postUtils.prepararHashMap(idUsuario, idPostagem, "imagem", urlPost,
                            descricao, new PostUtils.PrepararHashMapPostCallback() {
                                @Override
                                public void onConcluido(HashMap<String, Object> hashMapPost) {
                                    if (hashMapPost != null && !hashMapPost.isEmpty()) {
                                        ToastCustomizado.toastCustomizadoCurto("HashMap OKAY", requireContext());
                                        DatabaseReference postagemFinalRef = firebaseRef.child("postagens")
                                                .child(idUsuario).child(idPostagem);
                                        postUtils.salvarHashMapNoFirebase(postagemFinalRef, hashMapPost, new PostUtils.SalvarHashMapNoFirebaseCallback() {
                                            @Override
                                            public void onSalvo() {
                                                ToastCustomizado.toastCustomizadoCurto("Salvo no firebase com sucesso", requireContext());
                                                postUtils.salvarInteresses(salvarInteresseRef, idUsuario, idPostagem, new PostUtils.SalvarInteressesCallback() {
                                                    @Override
                                                    public void onSalvo() {
                                                        ToastCustomizado.toastCustomizadoCurto("Interesses salvos com sucesso", requireContext());
                                                    }

                                                    @Override
                                                    public void onError(String message) {
                                                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", requireActivity().getString(R.string.an_error_has_occurred), message), requireContext());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(String message) {
                                                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", requireActivity().getString(R.string.an_error_has_occurred), message), requireContext());
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", requireActivity().getString(R.string.an_error_has_occurred), message), requireContext());
                                }
                            });
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao publicar postagem", requireContext());
                    requireActivity().onBackPressed();
                }
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao fazer o upload da sua postagem, tente novamente", requireContext());
                postUtils.ocultarProgressDialog(progressDialog);
            }
        });
    }

    private void configInicial() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbarIncPadrao);
        ((AppCompatActivity) requireActivity()).setTitle("");
        txtViewTitleToolbar.setText("Configurar Postagem");
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        postUtils = new PostUtils(requireActivity(), requireContext());
        midiaUtils = new MidiaUtils(requireActivity(), requireContext());
        progressDialog = new ProgressDialog(requireContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        postUtils.limitarCaracteresDescricao(edtTxtDescricao, txtViewLimiteCaracteres);
        postUtils.configurarTopicos(autoCompleteTextView, linearLayoutInteresses);
    }

    private void clickListeners() {
        imgBtnIncBackPadrao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requireActivity().isFinishing()) {
                    requireActivity().onBackPressed();
                }
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
        imgViewPost = view.findViewById(R.id.imgViewPost);
        autoCompleteTextView = view.findViewById(R.id.autoCompleteInteressesPost);
        linearLayoutInteresses = view.findViewById(R.id.linearLayoutInteressesMarcados);
        edtTxtDescricao = view.findViewById(R.id.edtTxtDescPost);
        txtViewLimiteCaracteres = view.findViewById(R.id.txtViewLimiteDescPost);
        btnSalvar = view.findViewById(R.id.btnSalvarPost);
    }
}