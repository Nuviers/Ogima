package com.example.ogima.adapter;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.ConfigurarFotoActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.PostagemDiffCallback;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterFotosPostadas extends RecyclerView.Adapter<AdapterFotosPostadas.ViewHolder> {

    private List<Postagem> listaFotosPostadas;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private StorageReference storageRef;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private String idUsuarioDonoFoto;
    int contadorAtual;
    Postagem usuarioFotos, usuarioFotosRecentes, postagemArray;
    private DatabaseReference contadorUsuarioRef, listaPostagensRef;
    private String removidoOrdem;
    private ArrayList<String> capturarCaminhos = new ArrayList<>();
    private boolean visitante = false;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private RemocaoPostagemListener remocaoPostagemListener;
    private RemoverListenerRecycler removerListenerRecycler;

    public AdapterFotosPostadas(List<Postagem> listFotosPostadas, Context c, String idUsuarioDonoFoto,
                                boolean visitante, RecuperaPosicaoAnterior recuperaPosicaoAnterior,
                                RemocaoPostagemListener remocaoPostagemListener,
                                RemoverListenerRecycler removerListenerRecycler) {
        this.listaFotosPostadas = listFotosPostadas = new ArrayList<>();
        this.context = c;
        this.idUsuarioDonoFoto = idUsuarioDonoFoto;
        this.visitante = visitante;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoAnterior;
        this.remocaoPostagemListener = remocaoPostagemListener;
        this.removerListenerRecycler = removerListenerRecycler;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface RemocaoPostagemListener {
        void onPostagemRemocao(Postagem postagemRemovida, int posicao, Button BtnExcluir);
    }

    public interface RemoverListenerRecycler {
        void onRemoverListener();

        void onError();
    }

    public void updatePostagemList(List<Postagem> listaPostagensAtualizada, ListaAtualizadaCallback callback) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        PostagemDiffCallback diffCallback = new PostagemDiffCallback(listaFotosPostadas, listaPostagensAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaFotosPostadas.clear();
        listaFotosPostadas.addAll(listaPostagensAtualizada);

        diffResult.dispatchUpdatesTo(this);

        if (callback != null) {
            callback.onAtualizado();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Captura os componentes do layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_fotos_postadas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {

        Postagem fotoPostada = listaFotosPostadas.get(position);

        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;

                    if (bundle.containsKey("descricaoPostagem")) {
                        String novaDescricao = bundle.getString("descricaoPostagem");
                        fotoPostada.setDescricaoPostagem(novaDescricao);
                        holder.txtViewDescPostagem.setText(novaDescricao);
                    }
                }
            }
        } else {
            if (visitante && idUsuarioDonoFoto == null) {
                holder.buttonExcluirFotoPostagem.setVisibility(View.GONE);
                holder.buttonEditarFotoPostagem.setVisibility(View.GONE);
                ((Activity) context).finish();
            }

            holder.mudarAlinhamentoRelative();

            GlideCustomizado.fundoGlideEpilepsia(context, fotoPostada.getUrlPostagem(),
                    holder.imageAdFotoPostada, android.R.color.transparent);
            holder.textAdDataPostada.setText(fotoPostada.getDataPostagem());

            if (fotoPostada.getDescricaoPostagem() != null
                    && !fotoPostada.getDescricaoPostagem().isEmpty()) {
                holder.txtViewDescPostagem.setText(fotoPostada.getDescricaoPostagem());
            }

            if (!visitante && fotoPostada.getIdDonoPostagem()
                    .equals(idUsuarioLogado)) {

                holder.buttonEditarFotoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.irParaEdicao(fotoPostada, position);
                    }
                });

                holder.buttonExcluirFotoPostagem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.excluirPostagem(fotoPostada, position);
                    }
                });
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
    }

    @Override
    public int getItemCount() {
        //Retorna o tamanho da lista
        return listaFotosPostadas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //Inicializa os componentes do layout
        private TextView textAdDataPostada;
        private PhotoView imageAdFotoPostada;
        private Button buttonEditarFotoPostagem, buttonExcluirFotoPostagem;
        private ImageButton imgButtonDetalhesPostagem;
        private RelativeLayout relativeLayout;
        private TextView txtViewDescPostagem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayout = itemView.findViewById(R.id.relativeLayoutFotosPostadas);
            textAdDataPostada = itemView.findViewById(R.id.textAdDataPostada);
            imageAdFotoPostada = itemView.findViewById(R.id.imageAdFotoPostada);
            buttonEditarFotoPostagem = itemView.findViewById(R.id.buttonEditarFotoPostagem);
            buttonExcluirFotoPostagem = itemView.findViewById(R.id.buttonExcluirFotoPostagem);
            imgButtonDetalhesPostagem = itemView.findViewById(R.id.imgButtonDetalhesPostagem);
            txtViewDescPostagem = itemView.findViewById(R.id.txtViewDescPostagem);
        }

        private void mudarAlinhamentoRelative() {
            if (visitante) {
                buttonEditarFotoPostagem.setVisibility(View.GONE);
                buttonExcluirFotoPostagem.setVisibility(View.GONE);
                //Mudar above da imgView
                RelativeLayout.LayoutParams paramsVisitante = (RelativeLayout.LayoutParams) imageAdFotoPostada.getLayoutParams();
                paramsVisitante.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                imageAdFotoPostada.setLayoutParams(paramsVisitante);
            } else {
                buttonEditarFotoPostagem.setVisibility(View.VISIBLE);
                buttonExcluirFotoPostagem.setVisibility(View.VISIBLE);
                //Mudar above da imgView
                RelativeLayout.LayoutParams paramsDefault = (RelativeLayout.LayoutParams) imageAdFotoPostada.getLayoutParams();
                paramsDefault.addRule(RelativeLayout.ABOVE, R.id.buttonEditarFotoPostagem);
                imageAdFotoPostada.setLayoutParams(paramsDefault);
            }
        }

        private void irParaEdicao(Postagem postagemEdicao, int position) {
            recuperaPosicaoAnteriorListener.onPosicaoAnterior(position);
            Intent intent = new Intent(context, ConfigurarFotoActivity.class);
            intent.putExtra("edicao", true);
            intent.putExtra("postagemEdicao", postagemEdicao);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private void excluirPostagem(Postagem postagemSelecionada, int posicao) {
            buttonExcluirFotoPostagem.setEnabled(false);
            removerListenerRecycler.onRemoverListener();

            String idPostagem = postagemSelecionada.getIdPostagem();
            String tipoPostagem = postagemSelecionada.getTipoPostagem();
            String urlPostagem = postagemSelecionada.getUrlPostagem();

            DatabaseReference excluirPostagemRef = firebaseRef.child("fotos")
                    .child(idUsuarioLogado).child(idPostagem);

            excluirPostagemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    if (urlPostagem != null && !urlPostagem.isEmpty()
                            && tipoPostagem != null) {
                        try {
                            storageRef = storageRef.child("fotos")
                                    .child(idUsuarioLogado)
                                    .getStorage()
                                    .getReferenceFromUrl(urlPostagem);
                            storageRef.delete();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

            remocaoPostagemListener.onPostagemRemocao(postagemSelecionada, posicao, buttonExcluirFotoPostagem);
        }
    }
}