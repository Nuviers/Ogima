package com.example.ogima.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.AddGroupUsersActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.TimestampUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AdapterHeaderHiddenViews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String idUsuario = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Context context;
    private RewardedAd rewardedAd;
    private String TAG = "Header";
    private Activity activity;
    private boolean loadingCoins = false;
    public boolean interacaoEmAndamento = true;
    public int nrAds = 0;
    public long nrCoins = 0;

    public AdapterHeaderHiddenViews(Activity activity, Context c) {
        this.context = c;
        this.activity = activity;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    private interface VerificaAdsVencidasCallback {
        void onConcluido();

        void onLimiteAtingido(String tempoRestante);

        void onError(String message);
    }

    public int getNrAds() {
        return nrAds;
    }

    public void setNrAds(int nrAds) {
        this.nrAds = nrAds;
        notifyDataSetChanged();
    }

    public long getNrCoins() {
        return nrCoins;
    }

    public void setNrCoins(long nrCoins) {
        this.nrCoins = nrCoins;
        notifyDataSetChanged();
    }

    public boolean isInteracaoEmAndamento() {
        return interacaoEmAndamento;
    }

    public void setInteracaoEmAndamento(boolean interacaoEmAndamento) {
        this.interacaoEmAndamento = interacaoEmAndamento;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_header_banner_ad_viewers, parent, false);
        return new AdapterHeaderHiddenViews.HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
       HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
       headerHolder.btnVerAdsPorView.setText("Assistido: " + getNrAds()+"/5");
        if (getNrCoins() != -1) {
            headerHolder.txtViewQntCoins.setText(String.valueOf(getNrCoins()));
        }else{
            headerHolder.txtViewQntCoins.setText("0");
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private Button btnVerAdsPorView;
        private SpinKitView spinKitAdViewer;
        private TextView txtViewQntCoins;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            btnVerAdsPorView = itemView.findViewById(R.id.btnVerAdsPorView);
            spinKitAdViewer = itemView.findViewById(R.id.spinKitAdViewer);
            txtViewQntCoins = itemView.findViewById(R.id.txtViewQntCoins);

            //Inicializa o AdMob.
            inicializarAdMob();

            btnVerAdsPorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (interacaoEmAndamento) {
                        ToastCustomizado.toastCustomizadoCurto("Carregando vídeo, aguarde um momento.", context);
                        return;
                    }

                    ToastCustomizado.toastCustomizado("Click",context);

                    detalhesAdsAnteriores(new VerificaAdsVencidasCallback() {
                        @Override
                        public void onConcluido() {
                            showRewardedVideo();
                        }

                        @Override
                        public void onLimiteAtingido(String tempoRestante) {
                            ToastCustomizado.toastCustomizado("Limite de anúncios atingidos, próximo anúncio as: " + tempoRestante, context);
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizado("Ocorreu um erro ao carregar o anúncio: " + message, context);
                        }
                    });
                }
            });
        }

        private void inicializarAdMob() {
            MobileAds.initialize(context, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    carregarAnuncio();
                }
            });
        }

        private void carregarAnuncio() {
            btnVerAdsPorView.setVisibility(View.INVISIBLE);
            spinKitAdViewer.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(context, "ca-app-pub-3940256099942544/5224354917",
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.toString());
                            ToastCustomizado.toastCustomizado("Erro: " + loadAdError.toString(), context);
                            rewardedAd = null;
                            interacaoEmAndamento = false;
                            spinKitAdViewer.setVisibility(View.INVISIBLE);
                            btnVerAdsPorView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            rewardedAd = ad;
                            Log.d(TAG, "Ad was loaded.");
                            spinKitAdViewer.setVisibility(View.INVISIBLE);
                            btnVerAdsPorView.setVisibility(View.VISIBLE);
                            ToastCustomizado.toastCustomizadoCurto("Ad was loaded", context);
                            interacaoEmAndamento = false;
                        }
                    });
        }

        private void showRewardedVideo() {

            if (rewardedAd == null) {
                Log.d("TAG", "The rewarded ad wasn't ready yet.");
                ToastCustomizado.toastCustomizadoCurto("Carregando vídeo, aguarde um momento.", context);
                interacaoEmAndamento = false;
                return;
            }

            rewardedAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "onAdShowedFullScreenContent");
                            ToastCustomizado.toastCustomizado("onAdShowedFullScreenContent", context);
                            interacaoEmAndamento = false;
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when ad fails to show.
                            Log.d(TAG, "onAdFailedToShowFullScreenContent");
                            // Don't forget to set the ad reference to null so you
                            // don't show the ad a second time.
                            rewardedAd = null;
                            ToastCustomizado.toastCustomizado("onAdFailedToShowFullScreenContent", context);
                            interacaoEmAndamento = false;
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Don't forget to set the ad reference to null so you
                            // don't show the ad a second time.
                            rewardedAd = null;
                            Log.d(TAG, "onAdDismissedFullScreenContent");
                            ToastCustomizado.toastCustomizado("onAdDismissedFullScreenContent", context);
                            // Preload the next rewarded ad.
                            carregarAnuncio();
                        }
                    });
            Activity activityContext = activity;
            rewardedAd.show(
                    activityContext,
                    new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
                            Log.d("TAG", "The user earned the reward.");
                            int rewardAmount = rewardItem.getAmount();
                            String rewardType = rewardItem.getType();

                            ToastCustomizado.toastCustomizado("rewardAmount: " + rewardAmount, context);
                            ToastCustomizado.toastCustomizado("rewardType: " + rewardType, context);

                            addCoins(rewardAmount);
                        }
                    });
        }

        private void addCoins(int moreCoins) {
            loadingCoins = true;
            ToastCustomizado.toastCustomizado("More coins: " + moreCoins, context);
            TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                @Override
                public void onRecuperado(long timestampNegativo) {
                    long timestampPositivo = Math.abs(timestampNegativo);
                    long twelveHoursInMillis = 12 * 60 * 60 * 1000;
                    long timestamp12Horas = timestampPositivo + twelveHoursInMillis;
                    String caminhoAd = "/adsPrevious/" + idUsuario + "/";
                    String caminhoCoins = "/usuarios/" + idUsuario + "/" + "ogimaCoins";
                    HashMap<String, Object> operacoes = new HashMap<>();
                    operacoes.put(caminhoAd + "nrAdsVisualizadas", ServerValue.increment(1));
                    operacoes.put(caminhoAd + "timestampLastAd", timestampPositivo);
                    operacoes.put(caminhoAd + "timestampValidity", timestamp12Horas);
                    operacoes.put(caminhoCoins, ServerValue.increment(25));
                    firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            interacaoEmAndamento = false;
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    interacaoEmAndamento = false;
                }
            });
        }

        private void detalhesAdsAnteriores(VerificaAdsVencidasCallback callback) {
            TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                @Override
                public void onRecuperado(long timestampNegativo) {
                    long timestampAtual = Math.abs(timestampNegativo);
                    ToastCustomizado.toastCustomizado("Atual: " + timestampAtual, context);
                    Log.d("ATUALTIME", "ATUAL " + timestampAtual);
                    DatabaseReference queryAdAnterior = firebaseRef.child("adsPrevious")
                            .child(idUsuario);
                    queryAdAnterior.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Usuario detalhesAd = snapshot.getValue(Usuario.class);
                                if (timestampAtual >= detalhesAd.getTimestampValidity()) {
                                    //>= 12 horas referente ao último anúncio visto (Resetar).
                                    ToastCustomizado.toastCustomizado("Resetar",context);
                                    snapshot.getRef().removeValue();
                                    setNrAds(0);
                                } else if (detalhesAd.getNrAdsVisualizadas() >= 5) {
                                    //Limite de anúncios atingido
                                    long tempoFaltante = detalhesAd.getTimestampValidity() - timestampAtual;
                                    btnVerAdsPorView.setText("Próximo anúncio em: " + formatarTempoRestante(tempoFaltante));
                                    callback.onLimiteAtingido(formatarTempoRestante(tempoFaltante));
                                } else {
                                    btnVerAdsPorView.setText("Assistido: " + detalhesAd.getNrAdsVisualizadas()+"/5");
                                    callback.onConcluido();
                                }
                            } else {
                                callback.onConcluido();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            callback.onError(databaseError.getMessage());
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        }

        private String formatarTempoRestante(long tempoFaltante) {
            // Formate a string, ignorando unidades de tempo que são zero

            long days = TimeUnit.MILLISECONDS.toDays(tempoFaltante);
            long hours = TimeUnit.MILLISECONDS.toHours(tempoFaltante) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(tempoFaltante) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(tempoFaltante) % 60;


            StringBuilder timeRemainingBuilder = new StringBuilder();
            if (days > 0) {
                timeRemainingBuilder.append(days).append(" dias");
            }
            if (hours > 0) {
                if (timeRemainingBuilder.length() > 0) {
                    timeRemainingBuilder.append(", ");
                }
                timeRemainingBuilder.append(hours).append(" horas");
            }
            if (minutes > 0) {
                if (timeRemainingBuilder.length() > 0) {
                    timeRemainingBuilder.append(", ");
                }
                timeRemainingBuilder.append(minutes).append(" minutos");
            }
            if (seconds > 0) {
                if (timeRemainingBuilder.length() > 0) {
                    timeRemainingBuilder.append(", ");
                }
                timeRemainingBuilder.append(seconds).append(" segundos");
            }

            return timeRemainingBuilder.toString();
        }
    }
}
