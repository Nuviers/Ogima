package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.TimestampUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterViewers extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaViewer;
    private Context context;
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private HashMap<String, Object> listaDadosUser;
    private int hexCircle = -1;
    private static final int MAX_LENGHT = 20;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private AnimacaoIntent animacaoIntentListener;
    private RemoverViewerListener removerViewerListener;
    private boolean interacaoEmAndamento = false;

    private Bitmap originalBitmapBackground;
    private Bitmap originalBitmapPhoto;

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface RemoverViewerListener {
        void onRemocao(Usuario viewerAlvo, int posicao);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    public interface VerificaCoinsCallback {
        void onQuantidade(long coins);

        void onError(String message);
    }

    public AdapterViewers(Context c, List<Usuario> listaViewerOrigem,
                          HashMap<String, Object> listaDadosUser, int hexCircle,
                          RecuperaPosicaoAnterior recuperaPosicaoListener,
                          RemoverViewerListener removerViewerListener,
                          AnimacaoIntent animacaoIntent) {
        this.context = c;
        this.listaViewer = listaViewerOrigem = new ArrayList<>();
        this.listaDadosUser = listaDadosUser;
        this.hexCircle = hexCircle;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.removerViewerListener = removerViewerListener;
        this.animacaoIntentListener = animacaoIntent;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();
        AndroidThreeTen.init(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_viewers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        ViewHolder holderPrincipal = (ViewHolder) holder;
        Usuario viewer = listaViewer.get(position);
        String idUser = listaViewer.get(position).getIdUsuario();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);

        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                    if (bundle.containsKey("contatoFavorito")) {
                        boolean contatoFavorito = bundle.getBoolean("contatoFavorito");
                        viewer.setContatoFavorito(contatoFavorito);
                    }
                }
            }
        }

        //Falta criar um tipo de FormatarContadorUtils
        //para números para exibir um + quando tiver atingido o limite de caracteres numéricos.
        if (dadoUser != null) {
            if (dadoUser.getMinhaFoto() != null && !dadoUser.getMinhaFoto().isEmpty()
                    && !viewer.isIndisponivel()) {
                GlideCustomizado.loadUrlComListener(context,
                        dadoUser.getMinhaFoto(), holderPrincipal.imgViewFotoProfile,
                        android.R.color.transparent,
                        GlideCustomizado.CIRCLE_CROP,
                        false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                            @Override
                            public void onCarregado() {
                                holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(String message) {
                                holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                            }
                        });
            } else {
                UsuarioUtils.exibirFotoPadrao(context, holderPrincipal.imgViewFotoProfile, UsuarioUtils.FIELD_PHOTO, true);
            }

            if (dadoUser.getMeuFundo() != null && !dadoUser.getMeuFundo().isEmpty()
                    && !viewer.isIndisponivel()) {
                GlideCustomizado.loadUrlComListener(context,
                        dadoUser.getMeuFundo(), holderPrincipal.imgViewFundoProfile,
                        android.R.color.transparent,
                        GlideCustomizado.CENTER_CROP,
                        false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                            @Override
                            public void onCarregado() {
                                holderPrincipal.spinKitLoadBackground.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(String message) {
                                holderPrincipal.spinKitLoadBackground.setVisibility(View.GONE);
                            }
                        });
            } else {
                UsuarioUtils.exibirFotoPadrao(context, holderPrincipal.imgViewFundoProfile, UsuarioUtils.FIELD_BACKGROUND, false);
            }

            String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(dadoUser);
            nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_NAME_LENGHT);

            holderPrincipal.txtViewNameProfile.setText(nomeConfigurado);

            holderPrincipal.txtViewDataView.setText("Visualizado: " + viewer.getDataView());

            holderPrincipal.btnVisitarPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (interacaoEmAndamento) {
                        ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                        return;
                    }
                    interacaoEmAndamento = true;
                    visitarPerfil(dadoUser, position);
                }
            });
        }

        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return listaViewer.size();
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void updateViewerList(List<Usuario> listaViewersAtualizados, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaViewer, listaViewersAtualizados);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaViewer.clear();
        listaViewer.addAll(listaViewersAtualizados);

        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFundoProfile, imgViewFotoProfile;
        private TextView txtViewNameProfile, txtViewDataView;
        private Button btnVisitarPerfil;
        private SpinKitView spinKitLoadPhoto, spinKitLoadBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgViewFundoProfile = itemView.findViewById(R.id.imgViewIncFundoProfile);
            imgViewFotoProfile = itemView.findViewById(R.id.imgViewIncFotoProfile);
            txtViewNameProfile = itemView.findViewById(R.id.txtViewNameProfile);
            txtViewDataView = itemView.findViewById(R.id.txtViewDataView);
            btnVisitarPerfil = itemView.findViewById(R.id.btnVisitarPerfilDesbloqueado);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhoto);
            spinKitLoadBackground = itemView.findViewById(R.id.spinKitLoadBackground);
        }
    }

    private void visitarPerfil(Usuario usuarioAlvo, int posicao) {
        String idDonoPerfil = usuarioAlvo.getIdUsuario();
        if (idDonoPerfil != null
                && !idDonoPerfil.isEmpty()
                && idUsuario != null && !idUsuario.isEmpty()
                && idDonoPerfil.equals(idUsuario)) {
            return;
        }
        recuperaPosicaoAnteriorListener.onPosicaoAnterior(posicao);
        interacaoEmAndamento = false;
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                idDonoPerfil);
        animacaoIntentListener.onExecutarAnimacao();
    }

    public int findPositionInList(String userId) {
        for (int i = 0; i < listaViewer.size(); i++) {
            Usuario usuario = listaViewer.get(i);
            if (usuario.getIdUsuario().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}
