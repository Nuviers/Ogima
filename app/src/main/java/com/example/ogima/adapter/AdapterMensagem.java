package com.example.ogima.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.activity.ConversaActivity;
import com.example.ogima.activity.FotoVideoExpandidoActivity;
import com.example.ogima.activity.PlayerMusicaChatActivity;
import com.example.ogima.activity.ShareMessageActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.CommunityUtils;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.CoresRandomicas;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GroupUtils;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Chat;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdapterMensagem extends FirebaseRecyclerAdapter<Mensagem, AdapterMensagem.MyViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;

    private static final int LAYOUT_REMETENTE = 0;
    private static final int LAYOUT_DESTINATARIO = 1;

    //Variáveis para exclusão da mensagem
    private DatabaseReference deleteMessageForMeRef;
    private DatabaseReference contadorMessageForMeRef;

    private DatabaseReference deleteMessageReceiverRef;
    private DatabaseReference contadorMessageReceiverRef;
    private String nomePasta;
    private StorageReference storageRef;
    private StorageReference removerArquivoRef;

    public String stringTeste;
    private ConversaActivity conversaActivity = new ConversaActivity();
    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();
    //Verifição de permissões necessárias
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };
    private Activity activity;
    private boolean filtrarSomenteTexto = false;
    private List<Mensagem> listaSelecionados = new ArrayList<>();
    private Boolean chatGrupo;

    private CoresRandomicas coresRandomicas = new CoresRandomicas();
    private List<Integer> coresRandom = coresRandomicas.getCores();

    private int corPadraoMensagemDestinatario = Color.parseColor("#7EC2E1");
    private int corPadraoNome = Color.WHITE;
    private Grupo grupoRecebido;
    private HashMap<String, Object> fotosRemetentes;
    private boolean statusEpilpesia;
    private ProgressDialog progressDialog;
    private boolean interacaoEmAndamento = false;
    private GroupUtils groupUtils;

    private interface VerificaLastMsgCallback {
        void onRecuperado(Mensagem lastMsg);

        void onError(String message);
    }

    private interface RecuperaDetalhesChatCallback {
        void onRecuperado(Chat chat);

        void onError(String message);
    }

    private interface MontarHashmapForAllCallback {
        void onAjustado(HashMap<String, Object> hashmapAjustado);

        void onError(String message);
    }

    public boolean isFiltrarSomenteTexto() {
        return filtrarSomenteTexto;
    }

    public void setFiltrarSomenteTexto(boolean filtrarSomenteTexto) {
        this.filtrarSomenteTexto = filtrarSomenteTexto;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilpesia() {
        return statusEpilpesia;
    }

    public void setStatusEpilpesia(boolean statusEpilpesia) {
        this.statusEpilpesia = statusEpilpesia;
        notifyDataSetChanged();
    }

    public AdapterMensagem(Context c, @NonNull FirebaseRecyclerOptions<Mensagem> options, Activity activityRecebida, Boolean chatGrupo, Grupo grupoAtual, ProgressDialog progressDialog) {
        super(options);
        this.context = c;
        this.activity = activityRecebida;
        this.chatGrupo = chatGrupo;
        this.grupoRecebido = grupoAtual;
        this.fotosRemetentes = new HashMap<>();
        this.progressDialog = progressDialog;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        groupUtils = new GroupUtils(activityRecebida, context);
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;

        if (viewType == LAYOUT_REMETENTE) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);
        } else if (viewType == LAYOUT_DESTINATARIO) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Mensagem mensagemAtual) {

        if (mensagemAtual.getExibirAviso() != null) {
            holder.linearLayoutAvisoGrupo.setVisibility(View.VISIBLE);
            holder.linearLayoutMensagem.setVisibility(View.GONE);
            holder.txtViewAvisoGrupo.setText(mensagemAtual.getConteudoMensagem());
        } else {
            holder.linearLayoutAvisoGrupo.setVisibility(View.GONE);
            holder.linearLayoutMensagem.setVisibility(View.VISIBLE);
            if (chatGrupo) {
                if (mensagemAtual.getIdRemetente().equals(idUsuarioLogado)) {
                    holder.imgViewRemetenteGrupo.setVisibility(View.GONE);
                    holder.txtViewNomeRemetenteGrupo.setVisibility(View.GONE);
                } else {
                    holder.imgViewRemetenteGrupo.setVisibility(View.VISIBLE);
                    holder.txtViewNomeRemetenteGrupo.setVisibility(View.VISIBLE);

                    if (fotosRemetentes != null && !fotosRemetentes.isEmpty()
                            && fotosRemetentes.containsKey(mensagemAtual.getIdRemetente())) {
                        if (fotosRemetentes.get(mensagemAtual.getIdRemetente()).equals(UsuarioUtils.NO_PHOTO)) {
                            UsuarioUtils.exibirFotoPadrao(context, holder.imgViewRemetenteGrupo, UsuarioUtils.FIELD_PHOTO, true);
                        } else {
                            String fotoRemetente = fotosRemetentes.get(mensagemAtual.getIdRemetente()).toString();
                            GlideCustomizado.loadUrl(context, fotoRemetente, holder.imgViewRemetenteGrupo,
                                    android.R.color.transparent, GlideCustomizado.CIRCLE_CROP, false, isStatusEpilpesia());
                        }
                    } else {
                        holder.recuperarFotoRemetente(mensagemAtual.getIdRemetente());
                    }
                }
            } else {
                holder.imgViewRemetenteGrupo.setVisibility(View.GONE);
                holder.txtViewNomeRemetenteGrupo.setVisibility(View.GONE);
            }

            if (filtrarSomenteTexto) {
                if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.TEXT)) {
                    holder.linearLayoutMensagem.setVisibility(View.VISIBLE);
                    holder.txtViewMensagem.setVisibility(View.VISIBLE);
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                    holder.txtViewMensagem.setText(mensagemAtual.getConteudoMensagem());
                } else {
                    holder.linearLayoutMensagem.setVisibility(View.GONE);
                }
            } else {
                holder.linearLayoutMensagem.setVisibility(View.VISIBLE);
                if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.TEXT) ||
                        mensagemAtual.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
                    if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.TEXT)) {
                        holder.txtViewMensagem.setVisibility(View.VISIBLE);
                        holder.txtViewMensagem.setText(mensagemAtual.getConteudoMensagem());
                        holder.txtViewNotice.setVisibility(View.GONE);
                    } else {
                        holder.txtViewNotice.setVisibility(View.VISIBLE);
                        holder.txtViewNotice.setText(mensagemAtual.getConteudoMensagem());
                        holder.txtViewMensagem.setVisibility(View.GONE);

                        if (mensagemAtual.getIdRemetente().equals(idUsuarioLogado)) {
                            holder.txtViewNotice.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5D6D7E")));
                        }else{
                            holder.txtViewNotice.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BFC9CA")));
                        }
                    }
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.IMAGE)) {
                    holder.imgViewMensagem.setVisibility(View.VISIBLE);
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.txtViewMensagem.setVisibility(View.GONE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                    GlideCustomizado.loadUrl(context, mensagemAtual.getConteudoMensagem(),
                            holder.imgViewMensagem, android.R.color.transparent, GlideCustomizado.CENTER_INSIDE, false, true);
                } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.GIF)) {
                    holder.imgViewGifMensagem.setVisibility(View.VISIBLE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.txtViewMensagem.setVisibility(View.GONE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                    GlideCustomizado.loadUrl(context, mensagemAtual.getConteudoMensagem(),
                            holder.imgViewGifMensagem, android.R.color.transparent,
                            GlideCustomizado.CENTER_INSIDE, false, isStatusEpilpesia());
                } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.VIDEO)) {
                    holder.constraintThumbVideo.setVisibility(View.VISIBLE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.txtViewMensagem.setVisibility(View.GONE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                    GlideCustomizado.loadUrl(context,
                            mensagemAtual.getConteudoMensagem(),
                            holder.imgViewVideoMensagem,
                            android.R.color.transparent,
                            GlideCustomizado.CENTER_CROP, false, true);
                } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.DOCUMENT)) {
                    holder.linearDocumentoChat.setVisibility(View.VISIBLE);
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.txtViewMensagem.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                    holder.txtViewNomeDocumentoChat.setText(mensagemAtual.getNomeDocumento());
                } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.MUSIC)) {
                    holder.linearMusicaChat.setVisibility(View.VISIBLE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.GONE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.txtViewMensagem.setVisibility(View.GONE);
                    holder.txtViewMusicaChat.setText(mensagemAtual.getNomeDocumento());
                    holder.txtViewDuracaoMusicaChat.setText(mensagemAtual.getDuracaoMusica());
                } else if (mensagemAtual.getTipoMensagem().equals(MidiaUtils.AUDIO)) {
                    holder.linearMusicaChat.setVisibility(View.GONE);
                    holder.linearDocumentoChat.setVisibility(View.GONE);
                    holder.linearAudioChat.setVisibility(View.VISIBLE);
                    holder.imgViewGifMensagem.setVisibility(View.GONE);
                    holder.imgViewMensagem.setVisibility(View.GONE);
                    holder.constraintThumbVideo.setVisibility(View.GONE);
                    holder.txtViewMensagem.setVisibility(View.GONE);
                    holder.txtViewAudioChat.setText(mensagemAtual.getNomeDocumento());
                    holder.txtViewDuracaoAudioChat.setText(mensagemAtual.getDuracaoMusica());
                }
            }

            //Data mensagem a cada dia
            //Diferencia a data pelo getDay.

            //Não é possível usar o position do firebaseAdapter ele é invertido
            //qualquer lógica com a postion dele vai ser errado, utilizar a
            //listaReordenada no lugar se possível porém a position vai atrapalhar.

            //

            holder.txtViewDataMensagem.setText(mensagemAtual.getDataMensagem());

            holder.linearDocumentoChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (arquivoExiste(context, mensagemAtual)) {
                            ToastCustomizado.toastCustomizadoCurto("ABRIR", context);
                            abrirDocumento(mensagemAtual);
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("BAIXAR", context);
                            baixarArquivo(mensagemAtual, true);
                        }

                    } catch (ActivityNotFoundException e) {
                        ToastCustomizado.toastCustomizadoCurto("Não foi possível abrir esse arquivo", context);
                    }
                }
            });


            holder.imgViewMusicaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arquivoExiste(context, mensagemAtual)) {
                        abrirArquivo(mensagemAtual, "audio");
                    } else {
                        baixarArquivo(mensagemAtual, true);
                    }
                }
            });

            holder.txtViewMusicaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arquivoExiste(context, mensagemAtual)) {
                        abrirArquivo(mensagemAtual, "audio");
                    } else {
                        baixarArquivo(mensagemAtual, true);
                    }
                }
            });

            holder.linearMusicaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arquivoExiste(context, mensagemAtual)) {
                        abrirArquivo(mensagemAtual, "audio");
                    } else {
                        baixarArquivo(mensagemAtual, true);
                    }
                }
            });

            holder.linearAudioChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arquivoExiste(context, mensagemAtual)) {
                        abrirArquivo(mensagemAtual, "audio");
                    } else {
                        baixarArquivo(mensagemAtual, true);
                    }
                }
            });

            holder.txtViewAudioChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arquivoExiste(context, mensagemAtual)) {
                        abrirArquivo(mensagemAtual, "audio");
                    } else {
                        baixarArquivo(mensagemAtual, true);
                    }
                }
            });

            holder.imgViewAudioChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arquivoExiste(context, mensagemAtual)) {
                        abrirArquivo(mensagemAtual, "audio");
                    } else {
                        baixarArquivo(mensagemAtual, true);
                    }
                }
            });

            holder.imgViewMensagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("mensagem", mensagemAtual);
                    context.startActivity(intent);
                }
            });

            holder.imgButtonExpandirVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, FotoVideoExpandidoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("mensagem", mensagemAtual);
                    context.startActivity(intent);
                }
            });

            //Eventos de clique longo.
            holder.imgViewMensagem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //ToastCustomizado.toastCustomizadoCurto("Long",context);
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.txtViewMensagem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, holder.txtViewMensagem);
                    return true;
                }
            });

            holder.txtViewNotice.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, holder.txtViewNotice);
                    return true;
                }
            });

            holder.imgViewGifMensagem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.imgButtonExpandirVideo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.imgViewVideoMensagem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.imgViewMusicaChat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.txtViewMusicaChat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.imgViewAudioChat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.txtViewAudioChat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.imgViewDocumentoChat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });

            holder.txtViewNomeDocumentoChat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mostrarOpcoes(view, mensagemAtual, position, null);
                    return true;
                }
            });
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewMensagem, txtViewDataMensagem, txtViewNomeDocumentoChat,
                txtViewMusicaChat, txtViewAudioChat, txtViewDuracaoMusicaChat,
                txtViewDuracaoAudioChat, txtViewDataTrocaMensagens, txtViewNomeRemetenteGrupo,
                txtViewAvisoGrupo, txtViewNotice;
        private ImageView imgViewMensagem, imgViewGifMensagem, imgViewDocumentoChat,
                imgViewMusicaChat, imgViewAudioChat, imgViewVideoMensagem, imgViewRemetenteGrupo;
        private ImageButton imgButtonExpandirVideo;
        private LinearLayout linearDocumentoChat, linearMusicaChat, linearAudioChat,
                linearLayoutMensagem, linearLayoutAvisoGrupo;
        private ConstraintLayout constraintThumbVideo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutMensagem = itemView.findViewById(R.id.linearLayoutMensagem);

            txtViewMensagem = itemView.findViewById(R.id.txtViewMensagem);
            txtViewNotice = itemView.findViewById(R.id.txtViewNotice);
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
        }

        private void recuperarFotoRemetente(String idAlvo) {
            DatabaseReference recuperaUserGrupoRef = firebaseRef.child("usuarios")
                    .child(idAlvo);
            recuperaUserGrupoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioGrupo = snapshot.getValue(Usuario.class);
                        // Calcule um número a partir do id do usuário usando a função hashCode()
                        int numero = Math.abs(usuarioGrupo.getIdUsuario().hashCode());
                        // Selecione uma cor a partir da lista de cores usando o operador % (resto da divisão)
                        int cor = coresRandom.get(numero % coresRandom.size());
                        txtViewMensagem.setBackgroundTintList(ColorStateList.valueOf(cor));
                        txtViewNomeRemetenteGrupo.setTextColor(cor);
                        linearMusicaChat.setBackgroundTintList(ColorStateList.valueOf(cor));
                        linearDocumentoChat.setBackgroundTintList(ColorStateList.valueOf(cor));
                        linearAudioChat.setBackgroundTintList(ColorStateList.valueOf(cor));

                        if (usuarioGrupo.getMinhaFoto() != null && !usuarioGrupo.getMinhaFoto().isEmpty()) {
                            GlideCustomizado.loadUrl(context, usuarioGrupo.getMinhaFoto(),
                                    imgViewRemetenteGrupo, android.R.color.transparent,
                                    GlideCustomizado.CIRCLE_CROP, false, isStatusEpilpesia());
                            fotosRemetentes.put(idAlvo, usuarioGrupo.getMinhaFoto());
                        } else {
                            UsuarioUtils.exibirFotoPadrao(context, imgViewRemetenteGrupo, UsuarioUtils.FIELD_PHOTO, true);
                            fotosRemetentes.put(idAlvo, UsuarioUtils.NO_PHOTO);
                        }
                    }
                    recuperaUserGrupoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void mostrarOpcoes(View v, Mensagem mensagem, int position, TextView txtViewMensagem) {

        if (interacaoEmAndamento) {
            ToastCustomizado.toastCustomizadoCurto("Aguarde um momento", context);
            return;
        }

        Context context = v.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.bottom_sheet_dialog_opcoes_mensagem, null);
        final Dialog mBottomSheetDialog = new Dialog(context);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

        TextView txtViewExcluirMsg = mBottomSheetDialog.findViewById(R.id.txtViewExcluirMsg);
        TextView txtViewExcluirMsgTodos = mBottomSheetDialog.findViewById(R.id.txtViewExcluirMsgTodos);
        TextView txtViewShareMsg = mBottomSheetDialog.findViewById(R.id.txtViewShareMsg);
        TextView txtViewBaixarMsg = mBottomSheetDialog.findViewById(R.id.txtViewBaixarMsg);
        TextView txtViewCopiarTexto = mBottomSheetDialog.findViewById(R.id.txtViewCopiarTexto);

        ImageView imgViewExcluirMsg = mBottomSheetDialog.findViewById(R.id.imgViewExcluirMsg);
        ImageView imgViewExcluirMsgTodos = mBottomSheetDialog.findViewById(R.id.imgViewExcluirMsgTodos);
        ImageView imgViewShareMsg = mBottomSheetDialog.findViewById(R.id.imgViewShareMsg);
        ImageView imgViewBaixarMsg = mBottomSheetDialog.findViewById(R.id.imgViewBaixarMsg);
        ImageView imgViewCopiarTexto = mBottomSheetDialog.findViewById(R.id.imgViewCopiarTexto);

        LinearLayout uploadLinearLayout = mBottomSheetDialog.findViewById(R.id.uploadLinearLayout);
        LinearLayout deleteForMeLayout = mBottomSheetDialog.findViewById(R.id.deleteForMeLayout);
        LinearLayout deleteForAllLayout = mBottomSheetDialog.findViewById(R.id.deleteForAllLayout);
        LinearLayout copiarTextoLinearLayout = mBottomSheetDialog.findViewById(R.id.copiarTextoLinearLayout);
        LinearLayout shareLinearLayout = mBottomSheetDialog.findViewById(R.id.shareLinearLayout);


        if (chatGrupo) {
            deleteForMeLayout.setVisibility(View.GONE);
            if (mensagem.getIdRemetente().equals(idUsuarioLogado)) {
                //Pode excluir para todos pois se trata de uma mensagem minha
                deleteForAllLayout.setVisibility(View.VISIBLE);
            } else {
                //Não é minha mensagem
                groupUtils.recuperaCargo(grupoRecebido, new GroupUtils.RecuperaCargoCallback() {
                    @Override
                    public void onConcluido(String cargo) {
                        if (cargo.equals(CommunityUtils.FOUNDER_POSITION) ||
                                cargo.equals(CommunityUtils.ADM_POSITION)) {
                            //Tem autoridade para excluír a mensagem dos outros
                            deleteForAllLayout.setVisibility(View.VISIBLE);
                        } else {
                            deleteForAllLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da mensagem, tente novamente", context);
                        ocultarProgressDialog();
                    }
                });
            }
        } else {
            if (!idUsuarioLogado.equals(mensagem.getIdRemetente())) {
                deleteForAllLayout.setVisibility(View.GONE);
            } else {
                deleteForMeLayout.setVisibility(View.VISIBLE);
                deleteForAllLayout.setVisibility(View.VISIBLE);
            }
        }

        if (mensagem.getTipoMensagem().equals(MidiaUtils.TEXT)) {
            shareLinearLayout.setVisibility(View.GONE);
            copiarTextoLinearLayout.setVisibility(View.VISIBLE);
        } else if (mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
            copiarTextoLinearLayout.setVisibility(View.GONE);
            shareLinearLayout.setVisibility(View.GONE);
        } else {
            copiarTextoLinearLayout.setVisibility(View.GONE);
            shareLinearLayout.setVisibility(View.VISIBLE);
        }

        if (mensagem.getTipoMensagem().equals(MidiaUtils.TEXT)
                || mensagem.getTipoMensagem().equals(MidiaUtils.GIF)
                || mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
            uploadLinearLayout.setVisibility(View.GONE);
        } else {
            uploadLinearLayout.setVisibility(View.VISIBLE);
        }

        imgViewCopiarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copiarTexto(mensagem, txtViewMensagem, mBottomSheetDialog);
            }
        });

        txtViewCopiarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copiarTexto(mensagem, txtViewMensagem, mBottomSheetDialog);
            }
        });

        copiarTextoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copiarTexto(mensagem, txtViewMensagem, mBottomSheetDialog);
            }
        });

        txtViewBaixarMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                baixarPeloSheet(mensagem);
            }
        });

        imgViewBaixarMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                baixarPeloSheet(mensagem);
            }
        });

        imgViewShareMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compartilharMensagem(mensagem);
            }
        });

        txtViewShareMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compartilharMensagem(mensagem);
            }
        });

        txtViewExcluirMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();

                if (mensagem.getTipoMensagem().equals(MidiaUtils.TEXT)
                        || mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
                    deleteMessageForMe(mensagem, position, false);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para mim");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, true);
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, false);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        imgViewExcluirMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                if (!mensagem.getTipoMensagem().equals(MidiaUtils.TEXT) && !mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para mim");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, true);
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForMe(mensagem, position, false);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    deleteMessageForMe(mensagem, position, false);
                }
            }
        });

        //Evento de clique para excluir mensagem para todos
        txtViewExcluirMsgTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                if (!mensagem.getTipoMensagem().equals(MidiaUtils.TEXT) && !mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para todos");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, true);
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, false);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    deleteMessageForAll(mensagem, position, false);
                }
            }
        });

        imgViewExcluirMsgTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                mBottomSheetDialog.cancel();
                if (!mensagem.getTipoMensagem().equals(MidiaUtils.TEXT) && !mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Excluir mensagem para todos");
                    builder.setMessage("Deseja remover também esse arquivo do seu dispositivo ?");
                    builder.setPositiveButton("Remover arquivo também do dispositivo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, true);
                        }
                    }).setNegativeButton("Remover arquivo somente da conversa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessageForAll(mensagem, position, false);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    deleteMessageForAll(mensagem, position, false);
                }
            }
        });

        mBottomSheetDialog.show();
    }

    //Exclui a mensagem somente para o próprio usuário
    private void deleteMessageForMe(Mensagem mensagem, int position, boolean excluirLocalmente) {

        if (chatGrupo) {
            ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para essa ação", context);
            ocultarProgressDialog();
            return;
        }

        exibirProgressDialog();

        verificaLastMsg(mensagem, false, new VerificaLastMsgCallback() {
            HashMap<String, Object> apagarConversa = new HashMap<>();

            //Somente para chats normais
            //-> CaminhoDetalhes
            String caminhoDetalhesAtual = "/detalhesChat/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem) + "/";
            //-> Conversas
            String caminhoConversaUserAtual = "/conversas/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem) + "/" + mensagem.getIdConversa();
            //-> ContadorTotalMsg
            String caminhoTotalMsgAtual = "/contadorMensagens/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem) + "/";

            @Override
            public void onRecuperado(Mensagem lastMsg) {
                recuperaDetalhesChat(mensagem, false, new RecuperaDetalhesChatCallback() {
                    @Override
                    public void onRecuperado(Chat chat) {
                        if (chat != null) {
                            if (chat.getTotalMsg() <= 0) {
                                apagarConversa.put(caminhoDetalhesAtual + "totalMsg", 0);
                                apagarConversa.put(caminhoTotalMsgAtual + "totalMensagens", 0);
                            } else {
                                apagarConversa.put(caminhoDetalhesAtual + "totalMsg", ServerValue.increment(-1));
                                apagarConversa.put(caminhoTotalMsgAtual + "totalMensagens", ServerValue.increment(-1));
                            }
                        }

                        if (lastMsg != null && chat != null && mensagem.getIdConversa().equals(chat.getIdConversa())) {
                            //Mensagem a ser excluída se trata da última mensagem, então deve ser substituido
                            //nos detalhes
                            if (chat.getTotalMsg() <= 1) {
                                //Não há mais mensagens além dessa, então excluir detalhes.
                                String excluirDetalhe = "/detalhesChat/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem);
                                apagarConversa.put(excluirDetalhe, null);
                                terminarExclusaoForMe(apagarConversa, caminhoConversaUserAtual, mensagem, excluirLocalmente);
                            } else {
                                //Atualizar os detalhes para a nova última msg.
                                recuperaPenultimaMsg(mensagem, lastMsg.getTimestampinteracao(), false, new VerificaLastMsgCallback() {
                                    @Override
                                    public void onRecuperado(Mensagem penultimaMsg) {
                                        apagarConversa.put(caminhoDetalhesAtual + "conteudoLastMsg", penultimaMsg.getConteudoMensagem());
                                        apagarConversa.put(caminhoDetalhesAtual + "tipoMidiaLastMsg", penultimaMsg.getTipoMensagem());
                                        apagarConversa.put(caminhoDetalhesAtual + "idConversa", penultimaMsg.getIdConversa());
                                        apagarConversa.put(caminhoDetalhesAtual + "timestampLastMsg", penultimaMsg.getTimestampinteracao());
                                        apagarConversa.put(caminhoDetalhesAtual + "idUsuario", penultimaMsg.getIdRemetente());
                                        apagarConversa.put(caminhoDetalhesAtual + "dateLastMsg", penultimaMsg.getDataMensagem());
                                        terminarExclusaoForMe(apagarConversa, caminhoConversaUserAtual, mensagem, excluirLocalmente);
                                    }

                                    @Override
                                    public void onError(String message) {

                                    }
                                });
                            }
                        } else {
                            //Não precisa mudar os detalhes pois não se trata da última msg
                            terminarExclusaoForMe(apagarConversa, caminhoConversaUserAtual, mensagem, excluirLocalmente);
                        }
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void terminarExclusaoForMe(HashMap<String, Object> apagarConversa, String caminhoConversaUserAtual, Mensagem mensagem, boolean excluirLocalmente) {
        apagarConversa.put(caminhoConversaUserAtual, null);

        firebaseRef.updateChildren(apagarConversa, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (!mensagem.getTipoMensagem().equals(MidiaUtils.TEXT) && !mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
                    if (excluirLocalmente) {
                        excluirArquivoLocal(mensagem);
                    } else {
                        ToastCustomizado.toastCustomizadoCurto("Excluído com sucesso", context);
                        ocultarProgressDialog();
                    }
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Excluído com sucesso", context);
                    ocultarProgressDialog();
                }
            }
        });
    }

    private void excluirArquivoLocal(Mensagem mensagem) {

        nomePasta = null;

        File caminhoDestino = retornarDiretorioOrigem(mensagem);

        if (caminhoDestino == null) {
            return;
        }

        solicitaPermissoes("permissoesDelete");

        //ToastCustomizado.toastCustomizado("Id da pasta " + File.separator + "Ogima" + File.separator + mensagem.getIdDestinatario() + File.separator + nomePasta + File.separator + mensagem.getNomeDocumento(), context);
        if (!solicitaPermissoes.exibirPermissaoNegada) {
            try {
                boolean caminhoexiste = arquivoExiste(context, mensagem);
                boolean canread = caminhoDestino.canRead();
                boolean canwrite = caminhoDestino.canWrite();
                ToastCustomizado.toastCustomizadoCurto("Caminho Existe " + caminhoexiste, context);
                ToastCustomizado.toastCustomizadoCurto("CanRead " + canread, context);
                ToastCustomizado.toastCustomizadoCurto("CanWrite " + canwrite, context);
                if (caminhoexiste) {
                    caminhoDestino.delete();
                    ToastCustomizado.toastCustomizadoCurto("Arquivo excluído de seu dispositivo com sucesso", context);
                    ocultarProgressDialog();
                } else {
                    ToastCustomizado.toastCustomizadoCurto("Arquivo não localizado em seu dispositivo", context);
                    ocultarProgressDialog();
                }
            } catch (Exception ex) {
                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao excluir mídia do seu dispositivo", context);
                Log.i("App", "Exception while deleting file " + ex.getMessage());
                ocultarProgressDialog();
            }
        } else {
            ToastCustomizado.toastCustomizado("Permissões essencias para o funcionamento desse recurso foram recusadas, caso seja necessário permita às nas configurações do seu dispositivo.", context);
            ocultarProgressDialog();
        }
    }


    private void deleteMessageForAll(Mensagem mensagem, int position, boolean excluirLocalmente) {
        try {
            if (chatGrupo) {
                if (mensagem.getIdRemetente().equals(idUsuarioLogado)) {
                    //Pode excluir para todos pois se trata de uma mensagem minha
                    terminarExclusaoForAll(mensagem, excluirLocalmente);
                } else {
                    //Não é minha mensagem
                    groupUtils.recuperaCargo(grupoRecebido, new GroupUtils.RecuperaCargoCallback() {
                        @Override
                        public void onConcluido(String cargo) {
                            if (cargo.equals(CommunityUtils.FOUNDER_POSITION) ||
                                    cargo.equals(CommunityUtils.ADM_POSITION)) {
                                //Tem autoridade para excluír a mensagem dos outros
                                terminarExclusaoForAll(mensagem, excluirLocalmente);
                            } else {
                                ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para excluír essa mensagem", context);
                                ocultarProgressDialog();
                            }
                        }

                        @Override
                        public void onError(String message) {
                            ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recuperar os dados da mensagem, tente novamente", context);
                            ocultarProgressDialog();
                        }
                    });
                }
            } else if (mensagem.getIdRemetente().equals(idUsuarioLogado)) {
                terminarExclusaoForAll(mensagem, excluirLocalmente);
            } else {
                ToastCustomizado.toastCustomizadoCurto("Você não tem permissão para excluír essa mensagem", context);
                ocultarProgressDialog();
            }
        } catch (Exception ex) {
            ToastCustomizado.toastCustomizadoCurto("Erro " + ex.getMessage(), context);
            ocultarProgressDialog();
        }
    }

    private void terminarExclusaoForAll(Mensagem mensagem, boolean excluirLocalmente) {

        exibirProgressDialog();

        if (mensagem.getTipoMensagem().equals(MidiaUtils.IMAGE)) {
            nomePasta = "fotos";
        } else if (mensagem.getTipoMensagem().equals(MidiaUtils.GIF)) {
            nomePasta = "gifs";
        } else if (mensagem.getTipoMensagem().equals(MidiaUtils.VIDEO)) {
            nomePasta = "videos";
        } else if (mensagem.getTipoMensagem().equals(MidiaUtils.MUSIC)) {
            nomePasta = "musicas";
        } else if (mensagem.getTipoMensagem().equals(MidiaUtils.AUDIO)) {
            nomePasta = "audios";
        } else if (mensagem.getTipoMensagem().equals(MidiaUtils.DOCUMENT)) {
            nomePasta = "documentos";
        }

        if (!mensagem.getTipoMensagem().equals(MidiaUtils.GIF)
                && !mensagem.getTipoMensagem().equals(MidiaUtils.TEXT)
                && !mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
            //Remover primeiro do storage
            removerArquivoRef = storageRef.child("mensagens")
                    .child(nomePasta)
                    .child(idUsuarioLogado)
                    .child(mensagem.getIdDestinatario())
                    .getStorage().getReferenceFromUrl(mensagem.getConteudoMensagem());
            removerArquivoRef.delete();
        }

        HashMap<String, Object> apagarConversa = new HashMap<>();
        montarHashmapExclusaoForMe(mensagem, apagarConversa, false, new MontarHashmapForAllCallback() {
            @Override
            public void onAjustado(HashMap<String, Object> hashmapAjustadoAtual) {
                montarHashmapExclusaoForMe(mensagem, hashmapAjustadoAtual, true, new MontarHashmapForAllCallback() {
                    @Override
                    public void onAjustado(HashMap<String, Object> hashmapOtherSide) {
                        if (chatGrupo) {
                            String conversaGrupo = "/conversas/" + grupoRecebido.getIdGrupo() + "/" + mensagem.getIdConversa();
                            ajustarMensagemApagada(hashmapOtherSide, conversaGrupo);
                        }
                        firebaseRef.updateChildren(hashmapOtherSide, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (!mensagem.getTipoMensagem().equals(MidiaUtils.TEXT) && !mensagem.getTipoMensagem().equals(MidiaUtils.NOTICE)) {
                                    if (excluirLocalmente) {
                                        excluirArquivoLocal(mensagem);
                                    } else {
                                        ToastCustomizado.toastCustomizadoCurto("Excluído com sucesso", context);
                                        ocultarProgressDialog();
                                    }
                                } else {
                                    ToastCustomizado.toastCustomizadoCurto("Excluído com sucesso", context);
                                    ocultarProgressDialog();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void montarHashmapExclusaoForMe(Mensagem mensagem, HashMap<String, Object> apagarConversa, boolean otherSide, MontarHashmapForAllCallback callback) {
        verificaLastMsg(mensagem, otherSide, new VerificaLastMsgCallback() {
            @Override
            public void onRecuperado(Mensagem lastMsg) {
                recuperaDetalhesChat(mensagem, otherSide, new RecuperaDetalhesChatCallback() {
                    @Override
                    public void onRecuperado(Chat chat) {

                        //Somente para chats normais
                        //-> CaminhoDetalhes
                        String caminhoDetalhes;
                        //-> ContadorTotalMsg
                        String caminhoTotalMsg;
                        String caminhoConversa = "";

                        if (otherSide) {
                            if (chatGrupo) {
                                caminhoDetalhes = "/detalhesChatGrupo/" + retornarIdParaExclusao(mensagem) + "/" + idUsuarioLogado + "/";
                            } else {
                                caminhoConversa = "/conversas/" + retornarIdParaExclusao(mensagem) + "/" + idUsuarioLogado + "/" + mensagem.getIdConversa() + "/";
                                caminhoDetalhes = "/detalhesChat/" + retornarIdParaExclusao(mensagem) + "/" + idUsuarioLogado + "/";
                            }
                            caminhoTotalMsg = "/contadorMensagens/" + retornarIdParaExclusao(mensagem) + "/" + idUsuarioLogado + "/";
                        } else {
                            if (chatGrupo) {
                                caminhoDetalhes = "/detalhesChatGrupo/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem) + "/";
                            } else {
                                caminhoConversa = "/conversas/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem) + "/" + mensagem.getIdConversa() + "/";
                                caminhoDetalhes = "/detalhesChat/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem) + "/";
                            }
                            caminhoTotalMsg = "/contadorMensagens/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem) + "/";
                        }

                        if (!chatGrupo) {
                            ajustarMensagemApagada(apagarConversa, caminhoConversa);
                        }


                        if (chat != null) {
                            if (chat.getTotalMsg() <= 0) {
                                apagarConversa.put(caminhoDetalhes + "totalMsg", 0);
                                apagarConversa.put(caminhoTotalMsg + "totalMensagens", 0);
                            } else {
                                apagarConversa.put(caminhoDetalhes + "totalMsg", ServerValue.increment(-1));
                                apagarConversa.put(caminhoTotalMsg + "totalMensagens", ServerValue.increment(-1));
                            }
                        }

                        if (lastMsg != null && chat != null && mensagem.getIdConversa().equals(chat.getIdConversa())) {
                            //Mensagem a ser excluída se trata da última mensagem, então deve ser substituido
                            //nos detalhes
                            if (chat.getTotalMsg() <= 1) {
                                //Não há mais mensagens além dessa, então excluir detalhes.
                                String excluirDetalhe;
                                if (chatGrupo) {
                                    excluirDetalhe = "/detalhesChatGrupo/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem);
                                } else {
                                    excluirDetalhe = "/detalhesChat/" + idUsuarioLogado + "/" + retornarIdParaExclusao(mensagem);
                                }
                                apagarConversa.put(excluirDetalhe, null);
                                callback.onAjustado(apagarConversa);
                            } else {
                                //Atualizar os detalhes para a nova última msg.
                                apagarConversa.put(caminhoDetalhes + "conteudoLastMsg", "Mensagem apagada");
                                apagarConversa.put(caminhoDetalhes + "tipoMidiaLastMsg", MidiaUtils.NOTICE);
                                apagarConversa.put(caminhoDetalhes + "idConversa", mensagem.getIdConversa());
                                apagarConversa.put(caminhoDetalhes + "timestampLastMsg", mensagem.getTimestampinteracao());

                                if (otherSide) {
                                    apagarConversa.put(caminhoDetalhes + "idUsuario", idUsuarioLogado);
                                }else{
                                    apagarConversa.put(caminhoDetalhes + "idUsuario", retornarIdParaExclusao(mensagem));
                                }

                                apagarConversa.put(caminhoDetalhes + "dateLastMsg", mensagem.getDataMensagem());
                                apagarConversa.put(caminhoDetalhes + "nomeDocumento", null);
                                apagarConversa.put(caminhoDetalhes + "tipoArquivo", null);
                                callback.onAjustado(apagarConversa);
                            }
                        } else {
                            //Não precisa mudar os detalhes pois não se trata da última msg
                            callback.onAjustado(apagarConversa);
                        }
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void ajustarMensagemApagada(HashMap<String, Object> mensagemApagada, String caminho) {
        mensagemApagada.put(caminho + "conteudoMensagem", "Mensagem apagada");
        mensagemApagada.put(caminho + "conteudoMensagemPesquisa", null);
        mensagemApagada.put(caminho + "nomeDocumento", null);
        mensagemApagada.put(caminho + "tipoArquivo", null);
        mensagemApagada.put(caminho + "tipoMensagem", MidiaUtils.NOTICE);
        mensagemApagada.put(caminho + "duracaoMusica", null);
    }

    private String retornarIdParaExclusao(Mensagem mensagem) {
        if (mensagem == null) {
            return null;
        }
        if (mensagem.getIdRemetente().equals(idUsuarioLogado)) {
            return mensagem.getIdDestinatario();
        }
        return mensagem.getIdRemetente();
    }

    private File retornarDiretorioOrigem(Mensagem mensagem) {
        //Diretório onde está localizado o arquivo alvo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.IMAGE:
                case MidiaUtils.GIF:
                    return new File(Environment.DIRECTORY_PICTURES + File.separator + "Ogima" + File.separator + "Ogima Images" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.VIDEO:
                    return new File(Environment.DIRECTORY_MOVIES + File.separator + "Ogima" + File.separator + "Ogima Videos" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.MUSIC:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.AUDIO:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.DOCUMENT:
                    return new File(Environment.DIRECTORY_DOCUMENTS + File.separator + "Ogima" + File.separator + "Ogima Documents" + File.separator + mensagem.getNomeDocumento());
            }
        } else {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.IMAGE:
                case MidiaUtils.GIF:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Ogima" + File.separator + "Ogima Images" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.VIDEO:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES + File.separator + "Ogima" + File.separator + "Ogima Videos" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.MUSIC:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.AUDIO:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios" + File.separator + mensagem.getNomeDocumento());
                case MidiaUtils.DOCUMENT:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + "Ogima" + File.separator + "Ogima Documents" + File.separator + mensagem.getNomeDocumento());
            }
        }
        return null;
    }

    private File retornarDiretorioDestino(Mensagem mensagem) {
        //Diretório onde será salvo o arquivo alvo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.IMAGE:
                case MidiaUtils.GIF:
                    return new File(Environment.DIRECTORY_PICTURES + File.separator + "Ogima" + File.separator + "Ogima Images");
                case MidiaUtils.VIDEO:
                    return new File(Environment.DIRECTORY_MOVIES + File.separator + "Ogima" + File.separator + "Ogima Videos");
                case MidiaUtils.MUSIC:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima");
                case MidiaUtils.AUDIO:
                    return new File(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios");
                case MidiaUtils.DOCUMENT:
                    return new File(Environment.DIRECTORY_DOCUMENTS + File.separator + "Ogima" + File.separator + "Ogima Documents");
            }
        } else {
            switch (mensagem.getTipoMensagem()) {
                case MidiaUtils.IMAGE:
                case MidiaUtils.GIF:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Ogima" + File.separator + "Ogima Images");
                case MidiaUtils.VIDEO:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES + File.separator + "Ogima" + File.separator + "Ogima Videos");
                case MidiaUtils.MUSIC:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima");
                case MidiaUtils.AUDIO:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + File.separator + "Ogima" + File.separator + "Audios");
                case MidiaUtils.DOCUMENT:
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + "Ogima" + File.separator + "Ogima Documents");
            }
        }
        return null;
    }

    private String retornarIdAlvo(Mensagem mensagem) {
        if (idUsuarioLogado.equals(mensagem.getIdRemetente())) {
            //Se trata de uma mídia enviada pelo usuário atual.
            return idUsuarioLogado;
        }
        return mensagem.getIdDestinatario();
    }

    private void abrirArquivo(Mensagem mensagem, String keyName) {
        Intent intent = new Intent(context, PlayerMusicaChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(keyName, mensagem);
        context.startActivity(intent);
    }

    private void abrirDocumento(Mensagem mensagem) {
        String nomeDocumento = mensagem.getNomeDocumento();
        Uri contentUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Para Android Q e superiores, use MediaStore
            ContentResolver resolver = context.getContentResolver();
            String diretorio = String.valueOf(retornarDiretorioDestino(mensagem));
            Uri baseUri = MediaStore.Files.getContentUri("external");

            Cursor cursor = resolver.query(baseUri, null,
                    MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                    new String[]{nomeDocumento, diretorio + "/"}, null);

            if (cursor != null && cursor.moveToFirst()) {
                contentUri = ContentUris.withAppendedId(baseUri, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                cursor.close();
            } else {
                ToastCustomizado.toastCustomizadoCurto("Falha ao localizar arquivo", context);
                return;
            }
        } else {
            // Para versões anteriores, use FileProvider
            File fileOk = retornarDiretorioOrigem(mensagem);
            contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", fileOk);
            Log.d("TESTEJUN", "FILE: " + fileOk);
        }

        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(contentUri, mensagem.getTipoArquivo());
        Intent intent = Intent.createChooser(target, "Abrir arquivo");
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (target.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }

        Log.d("TESTEJUN", "URI: " + contentUri);

    }

    private void baixarPeloSheet(Mensagem mensagem) {
        switch (mensagem.getTipoMensagem()) {
            case MidiaUtils.IMAGE: {
                ToastCustomizado.toastCustomizadoCurto("Imagem", context);
                solicitaPermissoes("imagem");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    baixarArquivo(mensagem, false);
                }
                break;
            }
            /*
            case "gif":{
                 ToastCustomizado.toastCustomizadoCurto("Gif",context);
                 baixarArquivo(mensagem, false);
                break;
            }
             */
            case MidiaUtils.VIDEO: {
                ToastCustomizado.toastCustomizadoCurto("Video", context);
                solicitaPermissoes("video");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    baixarArquivo(mensagem, false);
                }
                break;
            }
            case MidiaUtils.MUSIC: {
                ToastCustomizado.toastCustomizadoCurto("Musica", context);
                solicitaPermissoes("musica");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    baixarArquivo(mensagem, false);
                }
                break;
            }
            case MidiaUtils.AUDIO: {
                ToastCustomizado.toastCustomizadoCurto("Audio", context);
                solicitaPermissoes("audio");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    baixarArquivo(mensagem, false);
                }
                break;
            }
            case MidiaUtils.DOCUMENT: {
                ToastCustomizado.toastCustomizadoCurto("Documento", context);
                solicitaPermissoes("documento");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    baixarArquivo(mensagem, false);
                }
                break;
            }
        }
    }

    private boolean arquivoExiste(Context context, Mensagem mensagem) {
        String nomeDocumento = mensagem.getNomeDocumento();
        boolean existe = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Para Android Q e superiores, use MediaStore
            ContentResolver resolver = context.getContentResolver();
            String diretorio = String.valueOf(retornarDiretorioDestino(mensagem));
            Uri baseUri = MediaStore.Files.getContentUri("external");

            String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            String[] selectionArgs = new String[]{nomeDocumento, diretorio + "/"};

            Cursor cursor = resolver.query(baseUri, null, selection, selectionArgs, null);

            if (cursor != null) {
                existe = cursor.getCount() > 0;
                cursor.close();
            }
        } else {
            // Para versões anteriores, use o método tradicional
            File file = new File(retornarDiretorioDestino(mensagem), nomeDocumento);
            existe = file.exists();
        }

        return existe;
    }


    private void baixarArquivo(Mensagem mensagem, boolean abrirDoc) {
        if (context == null) return;

        Handler mainHandler = new Handler(Looper.getMainLooper());
        // Baixar e salvar a imagem
        new Thread(() -> {
            try {
                File diretorio = retornarDiretorioDestino(mensagem);
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, mensagem.getNomeDocumento());
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mensagem.getTipoArquivo());
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        String.valueOf(diretorio));

                // Obtém o ContentResolver
                ContentResolver resolver = context.getContentResolver();
                // Baixar a imagem
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(mensagem.getConteudoMensagem()).build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Preparar o MediaStore para salvar a imagem
                    Uri contentUri;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentUri =
                                resolver.insert(retornarContentUri(mensagem), contentValues);
                    } else {
                        if (diretorio != null && !arquivoExiste(context, mensagem) && !diretorio.mkdirs()) {
                            throw new IOException("Falha ao criar diretório de destino");
                        }
                        contentUri = Uri.fromFile(new File(diretorio, mensagem.getNomeDocumento()));
                    }

                    if (contentUri != null) {
                        try (InputStream inputStream = response.body().byteStream();
                             OutputStream outputStream = resolver.openOutputStream(contentUri)) {

                            byte[] buffer = new byte[4096];
                            int bytesRead;

                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            mainHandler.post(() -> {
                                if (!abrirDoc) {
                                    ToastCustomizado.toastCustomizado("Download com sucesso", context);
                                } else {
                                    if (mensagem.getTipoMensagem().equals(MidiaUtils.DOCUMENT)) {
                                        abrirDocumento(mensagem);
                                    } else if (mensagem.getTipoMensagem().equals(MidiaUtils.MUSIC)
                                            || mensagem.getTipoMensagem().equals(MidiaUtils.AUDIO)) {
                                        abrirArquivo(mensagem, "audio");
                                    }
                                }
                            });
                        } catch (IOException e) {
                            resolver.delete(contentUri, null, null);
                            throw new IOException("Falha ao salvar a mídia", e);
                        }
                    } else {
                        throw new IOException("Falha ao salvar a mídia no dispositivo");
                    }
                } else {
                    throw new IOException("Falha ao fazer download da mídia: " + response.message());
                }
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> ToastCustomizado.toastCustomizado("Falha ao fazer download da mídia: " + e.getMessage(), context));
            }
        }).start();
    }

    private Uri retornarContentUri(Mensagem mensagem) {
        switch (mensagem.getTipoMensagem()) {
            case MidiaUtils.IMAGE:
            case MidiaUtils.GIF:
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            case MidiaUtils.VIDEO:
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case MidiaUtils.MUSIC:
            case MidiaUtils.AUDIO:
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            case MidiaUtils.DOCUMENT:
                return MediaStore.Files.getContentUri("external");
        }
        return null;
    }

    private void compartilharMensagem(Mensagem mensagemRecebida) {

        solicitaPermissoes("galeria");
        //Verifica se usuário tem as permissões necessárias para compartilhar.
        if (!solicitaPermissoes.exibirPermissaoNegada) {
            Intent intent = new Intent(context, ShareMessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("mensagemCompartilhada", mensagemRecebida);
            context.startActivity(intent);
        }
    }

    private void solicitaPermissoes(String permissao) {
        //Se alguma permissão não foi aceita, então a seguinte lógica é acionada.
        if (!solicitaPermissoes.verificaPermissoes(permissoesNecessarias, activity, permissao)) {
            if (permissao != null) {
                solicitaPermissoes.tratarResultadoPermissoes(permissao, activity);
            }
        }
    }

    @Override
    public void updateOptions(@NonNull FirebaseRecyclerOptions<Mensagem> options) {
        super.updateOptions(options);
    }

    private void copiarTexto(Mensagem mensagem, TextView textView, Dialog dialog) {
        String text = textView.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Texto copiado", text);
        clipboard.setPrimaryClip(clip);
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        ToastCustomizado.toastCustomizadoCurto("Texto copiado com sucesso", context);
    }

    private void exibirProgressDialog() {
        if (progressDialog != null && !activity.isFinishing()) {
            interacaoEmAndamento = true;
            progressDialog.setMessage("Excluíndo mensagem, aguarde um momento...");
            progressDialog.show();
        }
    }

    private void ocultarProgressDialog() {
        if (progressDialog != null && !activity.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        interacaoEmAndamento = false;
    }

    private void verificaLastMsg(Mensagem mensagemAlvo, boolean otherSide, VerificaLastMsgCallback callback) {

        Query queryRecuperaLastMsg;

        if (otherSide) {
            queryRecuperaLastMsg = firebaseRef.child("conversas")
                    .child(retornarIdParaExclusao(mensagemAlvo)).child(idUsuarioLogado)
                    .orderByChild("timestampinteracao").limitToFirst(1);
        } else {
            queryRecuperaLastMsg = firebaseRef.child("conversas")
                    .child(idUsuarioLogado).child(retornarIdParaExclusao(mensagemAlvo))
                    .orderByChild("timestampinteracao").limitToFirst(1);
        }

        queryRecuperaLastMsg.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Mensagem lastMsg = snapshotChildren.getValue(Mensagem.class);
                        callback.onRecuperado(lastMsg);
                    }
                } else {
                    //Não tem mensagens além dessa
                    callback.onRecuperado(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    private void recuperaPenultimaMsg(Mensagem mensagemAlvo, long timestamp, boolean otherSide, VerificaLastMsgCallback callback) {

        Query queryRecuperarPenultimaMensagem;

        if (otherSide) {
            queryRecuperarPenultimaMensagem = firebaseRef.child("conversas")
                    .child(retornarIdParaExclusao(mensagemAlvo)).child(idUsuarioLogado)
                    .orderByChild("timestampinteracao").startAfter(timestamp)
                    .limitToFirst(1);
        } else {
            queryRecuperarPenultimaMensagem = firebaseRef.child("conversas")
                    .child(idUsuarioLogado).child(retornarIdParaExclusao(mensagemAlvo))
                    .orderByChild("timestampinteracao").startAfter(timestamp)
                    .limitToFirst(1);
        }

        queryRecuperarPenultimaMensagem.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshotChildren : snapshot.getChildren()) {
                        Mensagem mensagemPenultima = snapshotChildren.getValue(Mensagem.class);
                        callback.onRecuperado(mensagemPenultima);
                    }
                } else {
                    //Não tem mensagens além dessa
                    callback.onRecuperado(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    private void recuperaDetalhesChat(Mensagem mensagemAlvo, boolean otherSide, RecuperaDetalhesChatCallback callback) {

        DatabaseReference recuperaDetalhesChat;

        if (otherSide) {
            recuperaDetalhesChat = firebaseRef.child("detalhesChat")
                    .child(retornarIdParaExclusao(mensagemAlvo)).child(idUsuarioLogado);
        } else {
            recuperaDetalhesChat = firebaseRef.child("detalhesChat")
                    .child(idUsuarioLogado).child(retornarIdParaExclusao(mensagemAlvo));
        }

        recuperaDetalhesChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Chat chat = snapshot.getValue(Chat.class);
                    callback.onRecuperado(chat);
                } else {
                    callback.onRecuperado(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }
}


