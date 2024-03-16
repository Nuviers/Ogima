package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ContactDiffCallback;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.TimestampUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Usuario;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterContactList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Contatos> listaContatos;
    private Context context;
    private String idUsuario = "";
    private boolean statusEpilepsia = true;
    private HashMap<String, Object> listaDadosUser;
    private int hexCircle = -1;
    private int hexFavoritado = -1;
    private int hexSemFavorito = -1;
    private static final int MAX_LENGHT = 20;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private AnimacaoIntent animacaoIntentListener;
    private RemoverContatoListener removerContatoListener;
    private Contatos contatoComparator = new Contatos();
    private boolean interacaoEmAndamento = false;

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface RemoverContatoListener {
        void onRemocao(Contatos contatoAlvo, int posicao);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    public AdapterContactList(Context c, List<Contatos> listaContatoOrigem,
                              HashMap<String, Object> listaDadosUser, int hexCircle,
                              RecuperaPosicaoAnterior recuperaPosicaoListener,
                              RemoverContatoListener removerContatoListener,
                              AnimacaoIntent animacaoIntent) {
        this.context = c;
        this.listaContatos = listaContatoOrigem = new ArrayList<>();
        this.listaDadosUser = listaDadosUser;
        this.hexCircle = hexCircle;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.removerContatoListener = removerContatoListener;
        this.animacaoIntentListener = animacaoIntent;
        this.idUsuario = UsuarioUtils.recuperarIdUserAtual();

        hexSemFavorito = context.getResources().getColor(R.color.contato_sem_favorito);
        hexFavoritado = context.getResources().getColor(R.color.contato_favoritado);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        ViewHolder holderPrincipal = (ViewHolder) holder;
        Contatos contato = listaContatos.get(position);
        String idUser = listaContatos.get(position).getIdContato();
        Usuario dadoUser = (Usuario) listaDadosUser.get(idUser);

        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                    if (bundle.containsKey("totalMensagens")) {
                        long totalMsg = bundle.getLong("totalMensagens");
                        contato.setTotalMensagens(totalMsg);
                    } else if (bundle.containsKey("timestampContato")) {
                        long timestampLastMsg = bundle.getLong("timestampContato");
                        contato.setTimestampContato(timestampLastMsg);
                    } else if (bundle.containsKey("nivelAmizade")) {
                        String nivelAmizade = bundle.getString("nivelAmizade");
                        contato.setNivelAmizade(nivelAmizade);
                    } else if (bundle.containsKey("contatoFavorito")) {
                        boolean contatoFavorito = bundle.getBoolean("contatoFavorito");
                        contato.setContatoFavorito(contatoFavorito);
                    }
                }
            }
        }

        //Falta criar um tipo de FormatarContadorUtils
        //para números para exibir um + quando tiver atingido o limite de caracteres numéricos.
        if (dadoUser != null) {
            if (dadoUser.getMinhaFoto() != null && !dadoUser.getMinhaFoto().isEmpty()
                    && !contato.isIndisponivel()) {
                holderPrincipal.spinKitLoadPhoto.setVisibility(View.VISIBLE);
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
            } else {
                UsuarioUtils.exibirFotoPadrao(context, holderPrincipal.imgViewIncPhoto, UsuarioUtils.FIELD_PHOTO, true);
            }
            String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(dadoUser);
            nomeConfigurado = FormatarContadorUtils.abreviarTexto(nomeConfigurado, UsuarioUtils.MAX_NAME_LENGHT);
            holderPrincipal.txtViewIncName.setText(nomeConfigurado);

            if (contato.isContatoFavorito()) {
                holderPrincipal.imgBtnFavorito.setBackgroundTintList(ColorStateList.valueOf(hexFavoritado));
            }else{
                holderPrincipal.imgBtnFavorito.setBackgroundTintList(ColorStateList.valueOf(hexSemFavorito));
            }

            holderPrincipal.imgBtnFavorito.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (interacaoEmAndamento) {
                        ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.wait_a_moment), context);
                        return;
                    }

                    interacaoEmAndamento = true;

                    DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
                    HashMap<String, Object> operacoes = new HashMap<>();
                    String caminho = "/contatos/" + idUsuario + "/" + contato.getIdContato() + "/" + "contatoFavorito";

                    if (contato.isContatoFavorito()) {
                        operacoes.put(caminho, false);
                    }else{
                        operacoes.put(caminho, true);
                    }

                    firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            interacaoEmAndamento = false;
                        }
                    });
                }
            });


            if (contato.getNivelAmizade() != null
                    && !contato.getNivelAmizade().isEmpty()) {
                String conteudo = "";
                holderPrincipal.txtViewAmizade.setTextColor(Color.BLACK);
                conteudo = contato.getNivelAmizade();
                holderPrincipal.txtViewAmizade.setText(FormatarContadorUtils.abreviarTexto(conteudo, 28));
            }

            long totalMsg = 0;
            if (contato.getTotalMensagens() > 0) {
                totalMsg = contato.getTotalMensagens();
            }

            holderPrincipal.btnViewTodasMsgs.setText(FormatarContadorUtils.abreviarTexto(String.valueOf(totalMsg), 8));

            if (contato.getTimestampContato() != -1) {
                TimestampUtils.RecuperarHoraDoTimestamp(context, true, contato.getTimestampContato(), new TimestampUtils.RecuperarHoraTimestampCallback() {
                    @Override
                    public void onConcluido(String horaMinuto) {
                        holderPrincipal.txtViewHoraLastMsg.setText(horaMinuto);
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
            }

            holderPrincipal.relativeLayoutPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    visitarPerfil(dadoUser, position);
                }
            });
        }

        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return listaContatos.size();
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

    public void updateContatoList(List<Contatos> listaContatosAtualizados, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        ContactDiffCallback diffCallback = new ContactDiffCallback(listaContatos, listaContatosAtualizados);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        listaContatos.clear();
        listaContatos.addAll(listaContatosAtualizados);

        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewIncPhoto;
        private TextView txtViewIncName, txtViewAmizade, txtViewHoraLastMsg;
        private SpinKitView spinKitLoadPhoto;
        private RelativeLayout relativeLayoutPreview;
        private Button btnViewTodasMsgs;
        private ImageButton imgBtnFavorito;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewIncPhoto = itemView.findViewById(R.id.imgViewIncPhoto);
            txtViewIncName = itemView.findViewById(R.id.txtViewIncName);
            txtViewAmizade = itemView.findViewById(R.id.txtViewAmizadeContato);
            txtViewHoraLastMsg = itemView.findViewById(R.id.txtViewHoraLastMsg);
            spinKitLoadPhoto = itemView.findViewById(R.id.spinKitLoadPhotoUser);
            relativeLayoutPreview = itemView.findViewById(R.id.relativeLayoutPreview);
            btnViewTodasMsgs = itemView.findViewById(R.id.btnViewTodasMsgs);
            imgViewIncPhoto.setBackgroundTintList(ColorStateList.valueOf(hexCircle));
            imgBtnFavorito = itemView.findViewById(R.id.imgBtnFavoritoContato);
        }

        private void atualizarMsg(long dado) {
            btnViewTodasMsgs.setText(FormatarContadorUtils.formatarNumeroAbreviado(Math.toIntExact(dado)));
        }

        private void atualizarTipoMidiaLastMsg(String dado) {
            if (dado.isEmpty()) {

                return;
            }
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
        for (int i = 0; i < listaContatos.size(); i++) {
            Contatos contato = listaContatos.get(i);
            if (contato.getIdContato().equals(userId)) {
                return i; // Retorna a posição na lista quando o ID corresponder
            }
        }
        return -1; // Retorna -1 se o ID não for encontrado na lista
    }
}
