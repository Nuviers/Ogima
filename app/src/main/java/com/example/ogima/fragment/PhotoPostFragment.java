package com.example.ogima.fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.PostUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Postagem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private ProgressDialog progressDialog;
    private PostUtils postUtils;
    private MidiaUtils midiaUtils;
    private StorageReference storageRef;
    private boolean edicao = false;
    private Postagem postagemEdicao;

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
            if (args.containsKey("edicao")) {
                edicao = args.getBoolean("edicao");
                if (edicao) {
                    postagemEdicao = new Postagem();
                    if (args.containsKey("postagemEdicao")) {
                        postagemEdicao = (Postagem) args.getSerializable("postagemEdicao");
                        exibirDadosEdicao();
                    }
                    return;
                }
            }

            if (args.containsKey("uriRecuperada")) {
                uriFoto = args.getParcelable("uriRecuperada");
                if (uriFoto != null) {
                    exibirUri();
                } else {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_occurred_creating_post), requireContext());
                    finalizarActivity();
                }
            }
        }
    }

    private void exibirDadosEdicao() {
        if (postagemEdicao != null) {
            if (postagemEdicao.getUrlPostagem() != null
                    && !postagemEdicao.getUrlPostagem().isEmpty()) {
                String urlEdicao = postagemEdicao.getUrlPostagem();
                GlideCustomizado.loadUrl(requireContext(), urlEdicao,
                        imgViewPost, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false,
                        true);
            } else {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_when_editing_post), requireContext());
                finalizarActivity();
            }
            if (postagemEdicao.getDescricaoPostagem() != null
                    && !postagemEdicao.getDescricaoPostagem().isEmpty()) {
                String descricaoEdicao = postagemEdicao.getDescricaoPostagem();
                edtTxtDescricao.setText(descricaoEdicao);
            }
            if (postagemEdicao.getListaInteressesPostagem() != null
                    && postagemEdicao.getListaInteressesPostagem().size() > 0) {
                ArrayList<String> listaInteressesEdicao = new ArrayList<>();
                listaInteressesEdicao = postagemEdicao.getListaInteressesPostagem();
                postUtils.setInteressesMarcadosComAssento(listaInteressesEdicao);
                postUtils.preencherTopicoEdicao(autoCompleteTextView, linearLayoutInteresses);
            }
        }
    }

    private void exibirUri() {
        GlideCustomizado.loadUrl(requireContext(), String.valueOf(uriFoto),
                imgViewPost, android.R.color.transparent, GlideCustomizado.CENTER_CROP, false,
                true);
    }

    private void salvarPostagem() {
        if (uriFoto == null && !edicao) {
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post), requireContext());
            finalizarActivity();
        }

        if (postUtils.getInteressesMarcadosComAssento() == null ||
                postUtils.getInteressesMarcadosComAssento() != null
                        && postUtils.interessesMarcadosComAssento.size() <= 0) {
            ToastCustomizado.toastCustomizado(getString(R.string.minimum_interest_in_post), requireContext());
            return;
        }

        if (edicao && postagemEdicao != null) {
            postUtils.exibirProgressDialog(progressDialog, "edicao");
            salvarEdicao();
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
                if (idPostagem != null && !idPostagem.isEmpty()) {
                    postUtils.prepararHashMap(idUsuario, idPostagem, "imagem", urlPost,
                            descricao, new PostUtils.PrepararHashMapPostCallback() {
                                @Override
                                public void onConcluido(HashMap<String, Object> hashMapPost) {
                                    if (hashMapPost != null && !hashMapPost.isEmpty()) {
                                        DatabaseReference postagemFinalRef = firebaseRef.child("postagens")
                                                .child(idUsuario).child(idPostagem);
                                        postUtils.salvarHashMapNoFirebase(postagemFinalRef, hashMapPost, new PostUtils.SalvarHashMapNoFirebaseCallback() {
                                            @Override
                                            public void onSalvo() {
                                                postUtils.salvarInteresses(salvarInteresseRef, idUsuario, idPostagem, new PostUtils.SalvarInteressesCallback() {
                                                    @Override
                                                    public void onSalvo() {
                                                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.post_published_successfully), requireContext());
                                                        postUtils.ocultarProgressDialog(progressDialog);
                                                    }

                                                    @Override
                                                    public void onError(String message) {
                                                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                                                        postUtils.ocultarProgressDialog(progressDialog);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(String message) {
                                                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                                                postUtils.ocultarProgressDialog(progressDialog);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                                    postUtils.ocultarProgressDialog(progressDialog);
                                }
                            });
                } else {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post), requireContext());
                    finalizarActivity();
                }
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_saving_post), requireContext());
                postUtils.ocultarProgressDialog(progressDialog);
            }
        });
    }

    private void salvarEdicao() {
        String descricaoAtual = edtTxtDescricao.getText().toString().trim();
        if (descricaoAtual != null && !descricaoAtual.isEmpty()) {
            DatabaseReference salvarDescricaoRef = firebaseRef.child("postagens")
                    .child(postagemEdicao.getIdDonoPostagem())
                    .child(postagemEdicao.getIdPostagem())
                    .child("descricaoPostagem");
            salvarDescricaoRef.setValue(descricaoAtual).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    salvarInteressesEdicao(false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_updating_post_description), requireContext());
                    postUtils.ocultarProgressDialog(progressDialog);
                }
            });
        } else {
            salvarInteressesEdicao(true);
        }
    }

    private void salvarInteressesEdicao(boolean removerDescricao) {
        DatabaseReference listaInteressesPostagemRef = firebaseRef.child("postagens")
                .child(postagemEdicao.getIdDonoPostagem())
                .child(postagemEdicao.getIdPostagem())
                .child("listaInteressesPostagem");
        listaInteressesPostagemRef.setValue(postUtils.getInteressesMarcadosComAssento()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DatabaseReference interesseRef = firebaseRef.child("interessesPostagens")
                        .child(postagemEdicao.getIdPostagem());
                postUtils.salvarInteresses(interesseRef, postagemEdicao.getIdDonoPostagem(), postagemEdicao.getIdPostagem(), new PostUtils.SalvarInteressesCallback() {
                    @Override
                    public void onSalvo() {
                        if (!removerDescricao) {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.post_published_successfully), requireContext());
                            finalizarActivity();
                            return;
                        }
                        removerDescricao();
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                        postUtils.ocultarProgressDialog(progressDialog);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_updating_post_interest), requireContext());
                postUtils.ocultarProgressDialog(progressDialog);
            }
        });
    }

    private void removerDescricao() {
        DatabaseReference removerDescricaoRef = firebaseRef.child("postagens")
                .child(postagemEdicao.getIdDonoPostagem())
                .child(postagemEdicao.getIdPostagem())
                .child("descricaoPostagem");
        removerDescricaoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.post_published_successfully), requireContext());
                finalizarActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_updating_post_description), requireContext());
                postUtils.ocultarProgressDialog(progressDialog);
            }
        });
    }

    private void finalizarActivity() {
        postUtils.ocultarProgressDialog(progressDialog);
        if (!requireActivity().isFinishing()) {
            requireActivity().onBackPressed();
        }
    }

    private void configInicial() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbarIncPadrao);
        ((AppCompatActivity) requireActivity()).setTitle("");
        txtViewTitleToolbar.setText(getString(R.string.configure_post));
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
                finalizarActivity();
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