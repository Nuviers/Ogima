package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.FotoVideoExpandidoActivity;
import com.example.ogima.activity.PlayerMusicaChatActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AdapterChatRandom extends FirebaseRecyclerAdapter<Mensagem, AdapterChatRandom.ViewHolder> {

    private Context context;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private static final int LAYOUT_REMETENTE = 0;
    private static final int LAYOUT_DESTINATARIO = 1;
    private boolean statusEpilepsia = true;
    private MediaPlayer mediaPlayer;
    private int lastClickedPosition = -1;
    private Handler handler = new Handler();
    private SeekBar seekBarLast;
    private TextView txtViewLastAudio;

    public AdapterChatRandom(Context c, @NonNull FirebaseRecyclerOptions<Mensagem> options) {
        super(options);
        this.context = c;
        idUsuarioLogado = UsuarioUtils.recuperarIdUserAtual();
        this.mediaPlayer = new MediaPlayer();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public void pauseAudio() {
        if (mediaPlayer != null
                && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void releaseAudio() {
        if (mediaPlayer != null) {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagemType = getItem(position);

        if (mensagemType.getExibirAviso() != null) {
            return LAYOUT_DESTINATARIO;
        } else {
            if (idUsuarioLogado.equals(mensagemType.getIdRemetente())) {
                return LAYOUT_REMETENTE;
            }
        }
        return LAYOUT_DESTINATARIO;
    }


    @NonNull
    @Override
    public AdapterChatRandom.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;

        if (viewType == LAYOUT_REMETENTE) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);
        } else if (viewType == LAYOUT_DESTINATARIO) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }
        return new ViewHolder(item);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Mensagem mensagemAtual) {
        holder.linearLayoutAvisoGrupo.setVisibility(View.GONE);
        holder.linearLayoutMensagem.setVisibility(View.VISIBLE);
        holder.imgViewRemetenteGrupo.setVisibility(View.GONE);
        holder.txtViewNomeRemetenteGrupo.setVisibility(View.GONE);

        if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.TEXT)) {
            holder.txtViewMensagem.setVisibility(View.VISIBLE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            holder.txtViewMensagem.setText(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(mensagemAtual.getConteudoMensagem()));
        } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.GIF)) {
            holder.imgViewGifMensagem.setVisibility(View.VISIBLE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.GONE);
            GlideCustomizado.loadUrl(context,
                    mensagemAtual.getConteudoMensagem(),
                    holder.imgViewGifMensagem, android.R.color.transparent,
                    GlideCustomizado.CENTER_INSIDE, false, isStatusEpilepsia());
        } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.AUDIO)) {
            holder.linearMusicaChat.setVisibility(View.GONE);
            holder.linearDocumentoChat.setVisibility(View.GONE);
            holder.linearAudioChat.setVisibility(View.VISIBLE);
            holder.imgViewGifMensagem.setVisibility(View.GONE);
            holder.imgViewMensagem.setVisibility(View.GONE);
            holder.constraintThumbVideo.setVisibility(View.GONE);
            holder.txtViewMensagem.setVisibility(View.GONE);
            holder.txtViewDuracaoAudioChat.setText(mensagemAtual.getDuracaoMusica());

            holder.seekBarAudio.setMax(100);

            if (mensagemAtual.isPlaying()) {
                holder.imgViewAudioChat.setVisibility(View.GONE);
                holder.imgViewAudioPause.setVisibility(View.VISIBLE);
            } else {
                holder.imgViewAudioChat.setVisibility(View.VISIBLE);
                holder.imgViewAudioPause.setVisibility(View.GONE);
            }

            holder.imgViewAudioChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Se não estiver reproduzindo, inicie a reprodução
                    try {
                        if (mediaPlayer != null && mensagemAtual.isPlaying()
                                && lastClickedPosition == -1 || lastClickedPosition != -1 && lastClickedPosition == position) {
                            //ToastCustomizado.toastCustomizadoCurto("RESUME",context);
                            holder.imgViewAudioChat.setVisibility(View.GONE);
                            holder.imgViewAudioPause.setVisibility(View.VISIBLE);
                            mediaPlayer.start();
                            holder.atualizarSeekBar();
                            return;
                        }
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(mensagemAtual.getConteudoMensagem());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        mensagemAtual.setPlaying(true);
                        // Verificar se outro áudio já estava em andamento
                        if (lastClickedPosition != -1 && lastClickedPosition != position) {
                            // Obtenha a referência ao item anterior
                            Mensagem previousItem = getItem(lastClickedPosition);
                            if (previousItem != null) {
                                previousItem.setPlaying(false);
                                seekBarLast.setProgress(0);
                                notifyItemChanged(lastClickedPosition);
                            }
                        }
                        lastClickedPosition = position;
                        seekBarLast = holder.seekBarAudio;
                        txtViewLastAudio = holder.txtViewAudioChat;
                        holder.atualizarSeekBar();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    holder.imgViewAudioChat.setVisibility(View.GONE);
                    holder.imgViewAudioPause.setVisibility(View.VISIBLE);
                }
            });

            holder.imgViewAudioPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mediaPlayer.isPlaying()) {
                        // Se já estiver reproduzindo, pause o áudio
                        mediaPlayer.pause();
                        holder.imgViewAudioPause.setVisibility(View.GONE);
                        holder.imgViewAudioChat.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        holder.txtViewDataMensagem.setText(mensagemAtual.getDataMensagem());

        holder.imgViewMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("mensagem", mensagemAtual);
                context.startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem, txtViewDataMensagem, txtViewNomeDocumentoChat,
                txtViewMusicaChat, txtViewAudioChat, txtViewDuracaoMusicaChat,
                txtViewDuracaoAudioChat, txtViewDataTrocaMensagens, txtViewNomeRemetenteGrupo,
                txtViewAvisoGrupo;
        private ImageView imgViewMensagem, imgViewGifMensagem, imgViewDocumentoChat,
                imgViewMusicaChat, imgViewAudioChat, imgViewVideoMensagem, imgViewRemetenteGrupo,
                imgViewAudioPause;
        private ImageButton imgButtonExpandirVideo;
        private LinearLayout linearDocumentoChat, linearMusicaChat, linearAudioChat,
                linearLayoutMensagem, linearLayoutAvisoGrupo;
        private ConstraintLayout constraintThumbVideo;
        private SeekBar seekBarAudio;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayoutMensagem = itemView.findViewById(R.id.linearLayoutMensagem);

            txtViewMensagem = itemView.findViewById(R.id.txtViewMensagem);
            txtViewDataMensagem = itemView.findViewById(R.id.txtViewDataMensagem);
            imgViewMensagem = itemView.findViewById(R.id.imgViewMensagem);
            imgViewGifMensagem = itemView.findViewById(R.id.imgViewGifMensagem);
            imgViewVideoMensagem = itemView.findViewById(R.id.imgViewVideoMensagem);
            constraintThumbVideo = itemView.findViewById(R.id.constraintThumbVideo);
            imgButtonExpandirVideo = itemView.findViewById(R.id.imgButtonExpandirVideo);

            linearDocumentoChat = itemView.findViewById(R.id.linearDocumentoChat);
            imgViewDocumentoChat = itemView.findViewById(R.id.imgViewDocumentoChat);
            txtViewNomeDocumentoChat = itemView.findViewById(R.id.txtViewNomeDocumentoChat);

            linearMusicaChat = itemView.findViewById(R.id.linearMusicaChat);
            imgViewMusicaChat = itemView.findViewById(R.id.imgViewMusicaChat);
            txtViewMusicaChat = itemView.findViewById(R.id.txtViewMusicaChat);

            linearAudioChat = itemView.findViewById(R.id.linearAudioChat);
            imgViewAudioChat = itemView.findViewById(R.id.imgViewAudioChat);
            txtViewAudioChat = itemView.findViewById(R.id.txtViewAudioChat);
            txtViewDuracaoAudioChat = itemView.findViewById(R.id.txtViewDuracaoAudioChat);

            txtViewDuracaoMusicaChat = itemView.findViewById(R.id.txtViewDuracaoMusicaChat);
            txtViewDataTrocaMensagens = itemView.findViewById(R.id.txtViewDataTrocaMensagens);

            imgViewRemetenteGrupo = itemView.findViewById(R.id.imgViewRemetenteGrupo);
            txtViewNomeRemetenteGrupo = itemView.findViewById(R.id.txtViewNomeRemetenteGrupo);

            linearLayoutAvisoGrupo = itemView.findViewById(R.id.linearLayoutAvisoGrupo);
            txtViewAvisoGrupo = itemView.findViewById(R.id.txtViewAvisoGrupo);

            seekBarAudio = itemView.findViewById(R.id.seekBarAudio);

            imgViewAudioPause = itemView.findViewById(R.id.imgViewAudioPause);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // O áudio foi concluído, redefina o estado do último item clicado
                    if (lastClickedPosition != -1) {
                        // Obtenha a referência ao item
                        Mensagem mensagem = getItem(lastClickedPosition);
                        if (mensagem != null) {
                            if (handler != null) {
                                handler.removeCallbacksAndMessages(null);
                            }
                            mensagem.setPlaying(false);
                            seekBarAudio.setProgress(0);
                            seekBarLast.setProgress(0);
                            txtViewAudioChat.setText("");
                            txtViewLastAudio.setText("");
                            notifyItemChanged(lastClickedPosition);
                            lastClickedPosition = -1;
                        }
                    }
                }
            });

            seekBarAudio.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    //ToastCustomizado.toastCustomizado("TOUCH",context);
                    SeekBar seekBar = (SeekBar) view;
                    int playPosition = (mediaPlayer.getDuration() / 100) *
                            seekBar.getProgress();
                    mediaPlayer.seekTo(playPosition);
                    txtViewAudioChat.setText(formatarTimer(mediaPlayer.getCurrentPosition()));
                    return false;
                }
            });

            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    seekBarAudio.setSecondaryProgress(i);
                }
            });

            seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Atualização do progresso do SeekBar
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // O usuário começou a interagir com o SeekBar
                    // Você pode não fazer nada aqui, pois não precisa interromper a reprodução
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // O usuário terminou a interação com o SeekBar
                    // Obtenha o novo progresso do SeekBar
                    int progress = seekBar.getProgress();

                    // Calcule a posição de reprodução correspondente com base no progresso
                    int playPosition = (mediaPlayer.getDuration() / 100) * progress;

                    // Atualize a posição de reprodução da mídia
                    mediaPlayer.seekTo(playPosition);

                    // Atualize o texto ou a exibição de tempo, se necessário
                    txtViewAudioChat.setText(formatarTimer(mediaPlayer.getCurrentPosition()));
                }
            });
        }

        public void atualizarSeekBar() {

            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                handler = new Handler();
            }

            if (mediaPlayer.isPlaying() && lastClickedPosition != -1) {
                seekBarAudio.setProgress((int) (((float) mediaPlayer.getCurrentPosition()
                        / mediaPlayer.getDuration()) * 100));
                handler.postDelayed(updater, 1000);
            }
        }

        public void atualizarSeekBarV2() {

            //*ToastCustomizado.toastCustomizado("Last - " + lastClickedPosition, context);

            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                handler = new Handler();
            }

            if (mediaPlayer != null && mediaPlayer.isPlaying() && lastClickedPosition != -1) {
                seekBarLast.setProgress((int) (((float) mediaPlayer.getCurrentPosition()
                        / mediaPlayer.getDuration()) * 100));
                handler.postDelayed(updaterV2, 1000);
            }
        }

        public Runnable updater = new Runnable() {
            @Override
            public void run() {
                atualizarSeekBar();
                long currentDuration = mediaPlayer.getCurrentPosition();
                txtViewAudioChat.setText(formatarTimer(currentDuration));
            }
        };

        public Runnable updaterV2 = new Runnable() {
            @Override
            public void run() {
                atualizarSeekBarV2();
                long currentDuration = mediaPlayer.getCurrentPosition();
                txtViewLastAudio.setText(formatarTimer(currentDuration));
            }
        };

        public String formatarTimer(long milliSeconds) {
            String timerString = "";
            String secondString;

            int hours = (int) (milliSeconds / (1000 * 60 * 60));
            int minutes = (int) (milliSeconds % (1000 * 60 * 60) / (1000 * 60));
            int seconds = (int) (milliSeconds % (1000 * 60 * 60) % (1000 * 60) / 1000);

            if (hours > 0) {
                timerString = hours + ":";
            }

            if (seconds < 10) {
                secondString = "0" + seconds;
            } else {
                secondString = "" + seconds;
            }

            timerString = timerString + minutes + ":" + secondString;

            return timerString;
        }

        public void escutarAudio(Mensagem mensagemAtual) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios" + File.separator + mensagemAtual.getNomeDocumento());
            if (file.exists()) {
                abrirArquivo(mensagemAtual, "audio");
            } else {
                File caminhoDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Ogima" + File.separator + mensagemAtual.getIdDestinatario() + File.separator + "audios");
                baixarArquivo(mensagemAtual, caminhoDestino);
                abrirArquivo(mensagemAtual, "audio");
            }
        }
    }

    private void abrirArquivo(Mensagem mensagem, String keyName) {
        Intent intent = new Intent(context, PlayerMusicaChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(keyName, mensagem);
        context.startActivity(intent);
    }

    private void baixarArquivo(Mensagem mensagem, File caminhoDestino) {
        //Fazer o download pela url do arquivo
        DownloadManager.Request requestDocumento = new DownloadManager.Request(Uri.parse(mensagem.getConteudoMensagem()));
        //Verificando permissões
        requestDocumento.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE);
        //Título
        requestDocumento.setTitle(mensagem.getNomeDocumento());
        //Permissão para acessar os arquivos
        requestDocumento.allowScanningByMediaScanner();
        //Deixando visível o progresso de download
        requestDocumento.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //Salvando arquivo
        caminhoDestino.mkdirs();
        Uri trasnformarUri = Uri.fromFile(new File(caminhoDestino, mensagem.getNomeDocumento()));
        requestDocumento.setDestinationUri(trasnformarUri);
        DownloadManager managerDocumento = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        managerDocumento.enqueue(requestDocumento);
    }

    @Override
    public void updateOptions(@NonNull FirebaseRecyclerOptions<Mensagem> options) {
        super.updateOptions(options);
    }
}
