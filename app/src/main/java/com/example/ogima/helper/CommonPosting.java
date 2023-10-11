package com.example.ogima.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.ogima.R;
import com.example.ogima.model.Postagem;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class CommonPosting {
    private Activity activity;
    private Context context;
    private ProgressDialog progressDialog;
    private PostUtils postUtils;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    public CommonPosting(Activity activity, Context context, ProgressDialog progressDialog, PostUtils postUtils) {
        this.activity = activity;
        this.context = context;
        this.progressDialog = progressDialog;
        this.postUtils = postUtils;
    }

    public boolean edicao(Bundle args) {
        if (args.containsKey("edicao")) {
            return args.getBoolean("edicao");
        }
        return false;
    }

    public Postagem postagemEdicao(Bundle args) {
        if (args.containsKey("postagemEdicao")) {
            Postagem postagemEdicao = new Postagem();
            postagemEdicao = (Postagem) args.getSerializable("postagemEdicao");
            return postagemEdicao;
        }
        return null;
    }

    public Uri recuperarUri(Bundle args) {
        if (args.containsKey("uriRecuperada")) {
            return args.getParcelable("uriRecuperada");
        }
        return null;
    }

    public void exibirUri(Uri uriRecuperada, SpinKitView spinKitPost, ImageView imgViewPost, String tipoCorte, boolean epilepsia) {
        if (uriRecuperada != null) {
            ProgressBarUtils.exibirProgressBar(spinKitPost, activity);
            GlideCustomizado.loadUrlComListener(context, String.valueOf(uriRecuperada),
                    imgViewPost, android.R.color.transparent, tipoCorte, false,
                    epilepsia, new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                            ProgressBarUtils.ocultarProgressBar(spinKitPost, activity);
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_displaying_post), context);
                        }
                    });
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_occurred_creating_post), context);
            finalizarActivity();
        }
    }

    public void exibirPostagemEdicao(Postagem postagemEdicao, SpinKitView spinKitPost, ImageView imgViewPost, String tipoCorte, boolean epilepsia) {
        if (postagemEdicao != null) {
            if (postagemEdicao.getUrlPostagem() != null
                    && !postagemEdicao.getUrlPostagem().isEmpty()) {
                ProgressBarUtils.exibirProgressBar(spinKitPost, activity);
                String urlEdicao = postagemEdicao.getUrlPostagem();
                GlideCustomizado.loadUrlComListener(context, urlEdicao,
                        imgViewPost, android.R.color.transparent, tipoCorte, false,
                        epilepsia, new GlideCustomizado.ListenerLoadUrlCallback() {
                            @Override
                            public void onCarregado() {
                                ProgressBarUtils.ocultarProgressBar(spinKitPost, activity);
                            }

                            @Override
                            public void onError(String message) {
                                ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_displaying_post), context);
                            }
                        });
            } else {
                ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_when_editing_post), context);
                finalizarActivity();
            }
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_when_editing_post), context);
            finalizarActivity();
        }
    }

    public void exibirDescricaoEdicao(Postagem postagemEdicao, EditText edtTxtDescricao) {
        if (postagemEdicao.getDescricaoPostagem() != null
                && !postagemEdicao.getDescricaoPostagem().isEmpty()) {
            String descricaoEdicao = postagemEdicao.getDescricaoPostagem().trim();
            edtTxtDescricao.setText(descricaoEdicao);
        }
    }

    public void exibirInteressesEdicao(Postagem postagemEdicao, AutoCompleteTextView autoCompleteTextView, LinearLayout linearLayoutInteresses) {
        if (postagemEdicao.getListaInteressesPostagem() != null
                && postagemEdicao.getListaInteressesPostagem().size() > 0) {
            ArrayList<String> listaInteressesEdicao = new ArrayList<>();
            listaInteressesEdicao = postagemEdicao.getListaInteressesPostagem();
            postUtils.setInteressesMarcadosComAssento(listaInteressesEdicao);
            postUtils.preencherTopicoEdicao(autoCompleteTextView, linearLayoutInteresses);
        }
    }

    public void removerDescricao(Postagem postagemEdicao) {
        if (postagemEdicao != null) {
            DatabaseReference removerDescricaoRef = firebaseRef.child("postagens")
                    .child(postagemEdicao.getIdDonoPostagem())
                    .child(postagemEdicao.getIdPostagem())
                    .child("descricaoPostagem");
            removerDescricaoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.post_published_successfully), context);
                    finalizarActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_updating_post_description), context);
                    postUtils.ocultarProgressDialog(progressDialog);
                }
            });
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_updating_post_description), context);
            postUtils.ocultarProgressDialog(progressDialog);
        }
    }

    public void salvarEdicao(Postagem postagemEdicao, EditText edtTxtDescricao) {
        String descricaoAtual = edtTxtDescricao.getText().toString().trim();
        if (descricaoAtual != null && !descricaoAtual.isEmpty()) {
            DatabaseReference salvarDescricaoRef = firebaseRef.child("postagens")
                    .child(postagemEdicao.getIdDonoPostagem())
                    .child(postagemEdicao.getIdPostagem())
                    .child("descricaoPostagem");
            salvarDescricaoRef.setValue(descricaoAtual).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    salvarInteressesEdicao(postagemEdicao, false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_updating_post_description), context);
                    postUtils.ocultarProgressDialog(progressDialog);
                }
            });
        } else {
            salvarInteressesEdicao(postagemEdicao, true);
        }
    }

    public void salvarInteressesEdicao(Postagem postagemEdicao, boolean removerDescricao) {
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
                            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.post_published_successfully), context);
                            finalizarActivity();
                        } else {
                            removerDescricao(postagemEdicao);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", activity.getString(R.string.an_error_has_occurred), message), context);
                        postUtils.ocultarProgressDialog(progressDialog);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_updating_post_interest), context);
                postUtils.ocultarProgressDialog(progressDialog);
            }
        });
    }

    public void finalizarActivity() {
        postUtils.ocultarProgressDialog(progressDialog);
        if (!activity.isFinishing()) {
            activity.onBackPressed();
        }
    }
}
