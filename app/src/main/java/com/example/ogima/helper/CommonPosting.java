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
import androidx.annotation.Nullable;

import com.example.ogima.R;
import com.example.ogima.model.Postagem;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class CommonPosting {
    private Activity activity;
    private Context context;
    private ProgressDialog progressDialog;
    private PostUtils postUtils;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String stringParaToast = "";

    public interface SalvarEdicaoFotoCallback{
        void onConcluido();
        void onError(String message);
    }

    public CommonPosting(Activity activity, Context context, ProgressDialog progressDialog, PostUtils postUtils, String stringParaToast) {
        this.activity = activity;
        this.context = context;
        this.progressDialog = progressDialog;
        this.postUtils = postUtils;
        this.stringParaToast = stringParaToast;
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

    public String recuperarUrlGif(Bundle args) {
        if (args.containsKey("urlGif")) {
            return args.getString("urlGif");
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
                            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_displaying_post, stringParaToast), context);
                        }
                    });
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_occurred_creating_post, stringParaToast), context);
            finalizarActivity();
        }
    }

    public void exibirGif(String urlGif, SpinKitView spinKitPost, ImageView imgViewPost, String tipoCorte, boolean epilepsia) {
        if (urlGif != null && !urlGif.isEmpty()) {
            ProgressBarUtils.exibirProgressBar(spinKitPost, activity);
            GlideCustomizado.loadUrlComListener(context, urlGif,
                    imgViewPost, android.R.color.transparent, tipoCorte, false,
                    epilepsia, new GlideCustomizado.ListenerLoadUrlCallback() {
                        @Override
                        public void onCarregado() {
                            ProgressBarUtils.ocultarProgressBar(spinKitPost, activity);
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_displaying_post, stringParaToast), context);
                        }
                    });
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_occurred_creating_post, stringParaToast), context);
            finalizarActivity();
        }
    }

    public void exibirUriVideo(ExoPlayer exoPlayer, StyledPlayerView styledPlayerView, SpinKitView spinKitPost, Uri uriVideo) {
        if (exoPlayer != null && uriVideo != null) {
            ProgressBarUtils.exibirProgressBar(spinKitPost, activity);
            styledPlayerView.setPlayer(exoPlayer);
            MediaItem mediaItem = MediaItem.fromUri(uriVideo);
            exoPlayer.addMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
            ProgressBarUtils.ocultarProgressBar(spinKitPost, activity);
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_occurred_creating_post, stringParaToast), context);
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
                                ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_displaying_post, stringParaToast), context);
                            }
                        });
            } else {
                ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_when_editing_post, stringParaToast), context);
                finalizarActivity();
            }
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_when_editing_post, stringParaToast), context);
            finalizarActivity();
        }
    }

    public void exibirVideoEdicao(Postagem postagemEdicao, ExoPlayer exoPlayer, StyledPlayerView styledPlayerView, SpinKitView spinKitPost) {
        if (postagemEdicao != null && exoPlayer != null
                && postagemEdicao.getUrlPostagem() != null && !postagemEdicao.getUrlPostagem().isEmpty()) {
            ProgressBarUtils.exibirProgressBar(spinKitPost, activity);
            styledPlayerView.setPlayer(exoPlayer);
            MediaItem mediaItem = MediaItem.fromUri(postagemEdicao.getUrlPostagem());
            exoPlayer.addMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
            ProgressBarUtils.ocultarProgressBar(spinKitPost, activity);
        } else {
            ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.error_when_editing_post, stringParaToast), context);
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

    public void salvarEdicao(Postagem postagemEdicao, EditText edtTxtDescricao) {
        String descricaoAtual = edtTxtDescricao.getText().toString().trim();
        HashMap<String, Object> operacoes = new HashMap<>();
        String caminhoPostagem = "/postagens/" + postagemEdicao.getIdDonoPostagem() + "/" + postagemEdicao.getIdPostagem() + "/";
        operacoes.put(caminhoPostagem + "listaInteressesPostagem/", postUtils.getInteressesMarcadosComAssento());
        if (descricaoAtual != null && !descricaoAtual.isEmpty()) {
            operacoes.put(caminhoPostagem + "descricaoPostagem", descricaoAtual);
        } else {
            operacoes.put(caminhoPostagem + "descricaoPostagem", null);
        }
        salvarInteressesEdicao(operacoes, postagemEdicao);
    }

    public void salvarEdicaoFoto(Postagem postagemEdicao, EditText edtTxtDescricao, SalvarEdicaoFotoCallback callback) {
        String descricaoAtual = edtTxtDescricao.getText().toString().trim();
        HashMap<String, Object> operacoes = new HashMap<>();
        String caminhoFoto = "/fotos/" + postagemEdicao.getIdDonoPostagem() + "/" + postagemEdicao.getIdPostagem() + "/";
        if (descricaoAtual != null && !descricaoAtual.isEmpty()) {
            operacoes.put(caminhoFoto + "descricaoPostagem", descricaoAtual);
        } else {
            operacoes.put(caminhoFoto + "descricaoPostagem", null);
        }
        firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    callback.onConcluido();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    public void salvarInteressesEdicao(HashMap<String, Object> operacoes, Postagem postagemEdicao) {
        DatabaseReference deletarInteresseAnteriorRef = firebaseRef.child("interessesPostagens")
                .child(postagemEdicao.getIdPostagem());
        deletarInteresseAnteriorRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                postUtils.salvarHashMapPost(operacoes, postagemEdicao.getIdDonoPostagem(), postagemEdicao.getIdPostagem(), new PostUtils.SalvarInteressesCallback() {
                    @Override
                    public void onSalvo() {
                        ToastCustomizado.toastCustomizadoCurto(activity.getString(R.string.post_published_successfully), context);
                        finalizarActivity();
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
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", activity.getString(R.string.an_error_has_occurred), e.getMessage()), context);
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
