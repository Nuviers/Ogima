package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
import com.example.ogima.helper.CoinsUtils;
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

import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.blurry.Blurry;

public class AdapterHiddenViews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    public interface VerificaCoinsCallback{
        void onQuantidade(long coins);
        void onError(String message);
    }

    public AdapterHiddenViews(Context c, List<Usuario> listaViewerOrigem,
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_hidden_viewer, parent, false);
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
                /*
                GlideCustomizado.loadUrlComListener(context,
                        dadoUser.getMinhaFoto(), holderPrincipal.imgViewIncPhoto,
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
                 */
                GlideCustomizado.loadUrlBITMAPCircular(context, dadoUser.getMinhaFoto(), new GlideCustomizado.OnBitmapLoadedListener() {
                    @Override
                    public void onBitmapLoaded(Bitmap resource) {
                        if (!viewer.isViewLiberada()) {
                            originalBitmapPhoto = Bitmap.createBitmap(resource.getWidth(), resource.getHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(originalBitmapPhoto);
                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setColor(Color.WHITE);
                            float radius = Math.min(originalBitmapPhoto.getWidth(), originalBitmapPhoto.getHeight()) / 2f;
                            canvas.drawCircle(originalBitmapPhoto.getWidth() / 2f, originalBitmapPhoto.getHeight() / 2f, radius, paint);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                            Rect srcRect = new Rect(0, 0, resource.getWidth(), resource.getHeight());
                            Rect destRect = new Rect(0, 0, originalBitmapPhoto.getWidth(), originalBitmapPhoto.getHeight());
                            canvas.drawBitmap(resource, srcRect, destRect, paint);
                            holderPrincipal.imgViewIncPhoto.setImageBitmap(originalBitmapPhoto);
                            holderPrincipal.applyBlur("photo");
                        }
                    }
                });
            } else {
                UsuarioUtils.exibirFotoPadrao(context, holderPrincipal.imgViewIncPhoto, UsuarioUtils.FIELD_PHOTO, true);
            }

            if (dadoUser.getMeuFundo() != null && !dadoUser.getMeuFundo().isEmpty()
                    && !viewer.isIndisponivel()) {
            /*
                GlideCustomizado.loadUrlComListener(context,
                        dadoUser.getMeuFundo(), holderPrincipal.imgViewIncBackground,
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
             */
                GlideCustomizado.loadUrlBITMAP(context, dadoUser.getMeuFundo(), new GlideCustomizado.OnBitmapLoadedListener() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        if (!viewer.isViewLiberada()) {
                            originalBitmapBackground = bitmap;
                            holderPrincipal.imgViewIncBackground.setImageBitmap(originalBitmapBackground);
                            holderPrincipal.applyBlur("background");
                        }
                    }
                });
            } else {
                UsuarioUtils.exibirFotoPadrao(context, holderPrincipal.imgViewIncBackground, UsuarioUtils.FIELD_BACKGROUND, false);
            }

            //Exibir a quanto tempo foi feito tal visualização
            holderPrincipal.tempoVisualizacao(Math.abs(viewer.getTimeStampView()));

            holderPrincipal.txtViewIncName.setText("*******");

            holderPrincipal.btnDesbloquearViewer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (interacaoEmAndamento) {
                        ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                        return;
                    }

                    holderPrincipal.verificarCoins(new VerificaCoinsCallback() {
                        @Override
                        public void onQuantidade(long coins) {
                            if (coins <= 0) {
                                ToastCustomizado.toastCustomizado("OgimaCoins insuficientes, assista vídeos e ganhe ogimaCoins!", context);
                            }else{
                               TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                                   @Override
                                   public void onRecuperado(long timestampNegativo) {
                                       interacaoEmAndamento = true;
                                       HashMap<String, Object> coinsOperacao = new HashMap<>();
                                       String caminhoCoins = "/usuarios/" + idUsuario + "/";
                                       String caminhoView = "/profileViews/" + idUsuario + "/" + viewer.getIdUsuario();
                                       String caminhoViewLiberada = "/profileViewsLiberada/" + idUsuario + "/" + viewer.getIdUsuario() + "/";
                                       String caminhoPesquisa = "/viewers_by_name/" + idUsuario + "/" + viewer.getIdUsuario() + "/";
                                       coinsOperacao.put(caminhoCoins + "ogimaCoins", ServerValue.increment(-125));
                                       coinsOperacao.put(caminhoViewLiberada + "idUsuario", viewer.getIdUsuario());
                                       coinsOperacao.put(caminhoViewLiberada + "dataView", viewer.getDataView());
                                       coinsOperacao.put(caminhoViewLiberada + "timeStampView", viewer.getTimeStampView());
                                       coinsOperacao.put(caminhoViewLiberada + "timestampRevealed", timestampNegativo);
                                       coinsOperacao.put(caminhoViewLiberada + "viewLiberada", true);
                                       coinsOperacao.put(caminhoPesquisa + "idUsuario", viewer.getIdUsuario());
                                       coinsOperacao.put(caminhoPesquisa + "nomeUsuarioPesquisa", dadoUser.getNomeUsuarioPesquisa());
                                       coinsOperacao.put(caminhoView, null);
                                       holderPrincipal.linearLayoutDesbloquear.setVisibility(View.GONE);
                                       viewer.setViewLiberada(true);
                                       ToastCustomizado.toastCustomizado("Coins: " + coins, context);

                                       DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
                                       firebaseRef.updateChildren(coinsOperacao, new DatabaseReference.CompletionListener() {
                                           @Override
                                           public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                               String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(dadoUser);
                                               nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_NAME_LENGHT);

                                               holderPrincipal.txtViewIncName.setText(nomeConfigurado);

                                               holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
                                               holderPrincipal.spinKitLoadBackground.setVisibility(View.VISIBLE);

                                               holderPrincipal.removeBlur("background");

                                               GlideCustomizado.loadUrlComListener(context,
                                                       dadoUser.getMeuFundo(), holderPrincipal.imgViewIncBackground,
                                                       android.R.color.transparent,
                                                       GlideCustomizado.CENTER_CROP,
                                                       false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                                           @Override
                                                           public void onCarregado() {
                                                               holderPrincipal.spinKitLoadBackground.setVisibility(View.GONE);
                                                               holderPrincipal.removeBlur("photo");
                                                               GlideCustomizado.loadUrlComListener(context,
                                                                       dadoUser.getMinhaFoto(), holderPrincipal.imgViewIncPhoto,
                                                                       android.R.color.transparent,
                                                                       GlideCustomizado.CIRCLE_CROP,
                                                                       false, isStatusEpilepsia(), new GlideCustomizado.ListenerLoadUrlCallback() {
                                                                           @Override
                                                                           public void onCarregado() {
                                                                               holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                                                                               interacaoEmAndamento = false;
                                                                           }

                                                                           @Override
                                                                           public void onError(String message) {
                                                                               holderPrincipal.spinKitLoadPhoto.setVisibility(View.GONE);
                                                                           }
                                                                       });
                                                           }

                                                           @Override
                                                           public void onError(String message) {
                                                               holderPrincipal.spinKitLoadBackground.setVisibility(View.GONE);
                                                           }
                                                       });
                                           }
                                       });
                                   }

                                   @Override
                                   public void onError(String message) {
                                       ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar suas ogimaCoins", context);
                                   }
                               });
                            }
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar suas ogimaCoins", context);
                        }
                    });
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

        private ImageView imgViewIncPhoto, imgViewIncBackground, imgViewIncHiddenFundoProfileOverlay;
        private TextView txtViewIncName, txtViewTempoDaView;
        private SpinKitView spinKitLoadPhoto, spinKitLoadBackground;
        private Button btnDesbloquearViewer;
        private LinearLayout linearLayoutDesbloquear;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncHiddenFotoProfile);
            imgViewIncBackground = itemView.findViewById(R.id.imgViewIncHiddenFundoProfile);
            txtViewIncName = itemView.findViewById(R.id.txtViewHiddenNameProfile);
            txtViewTempoDaView = itemView.findViewById(R.id.txtViewTempoDaView);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhoto);
            spinKitLoadBackground = itemView.findViewById(R.id.spinKitLoadBackground);
            imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexCircle));
            btnDesbloquearViewer = itemView.findViewById(R.id.btnDesbloquearViewer);
            imgViewIncHiddenFundoProfileOverlay = itemView.findViewById(R.id.imgViewIncHiddenFundoProfileOverlay);
            linearLayoutDesbloquear = itemView.findViewById(R.id.linearLayoutDesbloquearViewer);
        }

        private void tempoVisualizacao(long timestampView){
            TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                @Override
                public void onRecuperado(long timestampNegativo) {
                    long timestampAtual = Math.abs(timestampNegativo);
                    // Calcular a diferença de tempo
                    Instant timestampViewEpoch = Instant.ofEpochMilli(timestampView);
                    Instant timestampAtualEpoch = Instant.ofEpochMilli(timestampAtual);

                    long days = ChronoUnit.DAYS.between(timestampViewEpoch, timestampAtualEpoch);
                    long hours = ChronoUnit.HOURS.between(timestampViewEpoch, timestampAtualEpoch) % 24;
                    long minutes = ChronoUnit.MINUTES.between(timestampViewEpoch, timestampAtualEpoch) % 60;
                    long seconds = ChronoUnit.SECONDS.between(timestampViewEpoch, timestampAtualEpoch) % 60;

                    // Construir a string
                    StringBuilder timePassedBuilder = new StringBuilder("Visualizado há ");
                    if (days > 0) {
                        timePassedBuilder.append(days).append(days == 1 ? " dia" : " dias");
                    } else if (hours > 0) {
                        timePassedBuilder.append(hours).append(hours == 1 ? " hora" : " horas");
                    } else if (minutes > 0) {
                        timePassedBuilder.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
                    } else {
                        timePassedBuilder.append(seconds).append(seconds == 1 ? " segundo" : " segundos");
                    }

                    String timePassed = timePassedBuilder.toString();
                    txtViewTempoDaView.setText(timePassed);
                }

                @Override
                public void onError(String message) {
                }
            });
        }

        private void applyBlur(String campo) {
            if (campo.equals("photo")) {
                if (originalBitmapPhoto != null) {
                    Blurry.with(context)
                            .radius(60)
                            .sampling(2)
                            .from(originalBitmapPhoto)
                            .into(imgViewIncPhoto);
                }
            }else{
                if (originalBitmapBackground != null) {
                    Blurry.with(context)
                            .radius(60)
                            .sampling(2)
                            .from(originalBitmapBackground)
                            .into(imgViewIncBackground);
                }
            }
        }

        private void removeBlur(String campo) {
            if (campo.equals("photo")) {
                if (originalBitmapPhoto != null) {
                    imgViewIncPhoto.setImageBitmap(originalBitmapPhoto);
                }
            }else{
                if (originalBitmapBackground != null) {
                    imgViewIncBackground.setImageBitmap(originalBitmapBackground);
                }
            }
        }

        private void verificarCoins(VerificaCoinsCallback callback){
            CoinsUtils.recuperaCoins(idUsuario, new CoinsUtils.RecuperarCoinsCallback() {
                @Override
                public void onRecuperado(long coins) {
                    callback.onQuantidade(coins);
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
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
