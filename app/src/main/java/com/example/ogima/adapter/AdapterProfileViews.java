package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.AtualizarContador;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffCallback;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.alterac.blurkit.BlurLayout;

public class AdapterProfileViews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Usuario> listaViewers;
    private Context context;
    private RecuperaPosicaoAnterior recuperaPosicaoAnteriorListener;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    //Sempre inicializar o sinalizador de epilepsia como true, assim
    //mesmo que tenha algum problema na consulta no servidor não trará problemas.
    private boolean dadosUserAtualRecuperado = false;

    private boolean statusEpilepsia = true;
    private AtualizarContador atualizarContador;

    private AtualizarView atualizarViewListener;
    private AnimacaoIntent animacaoIntentListener;

    public AdapterProfileViews(Context c, List<Usuario> listaUsuarioOrigem,
                               RecuperaPosicaoAnterior recuperaPosicaoListener,
                               AtualizarView atualizarView,
                               AnimacaoIntent animacaoIntent) {
        this.listaViewers = listaUsuarioOrigem = new ArrayList<>();
        this.context = c;
        this.recuperaPosicaoAnteriorListener = recuperaPosicaoListener;
        this.emailUsuario = autenticacao.getCurrentUser().getEmail();
        this.idUsuario = Base64Custom.codificarBase64(emailUsuario);
        this.atualizarViewListener = atualizarView;
        this.animacaoIntentListener = animacaoIntent;

        atualizarContador = new AtualizarContador();
    }

    public void updateViewersList(List<Usuario> listaUsuariosAtualizada) {
        //Totalmente funcional, porém em atualizações granulares não é recomendado.
        UsuarioDiffCallback diffCallback = new UsuarioDiffCallback(listaViewers, listaUsuariosAtualizada);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        listaViewers.clear();
        listaViewers.addAll(listaUsuariosAtualizada);

        diffResult.dispatchUpdatesTo(this);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface DadosViewer {
        void onRecuperado(Usuario usuarioViewer);

        void onSemDados();

        void onError(String message);
    }

    private interface EsquemaDesbloqueio {
        void onSaldoValido();

        void onViewerExiste();

        void onViewerNaoExiste();

        void onSaldoInsuficiente();

        void onError(String message);
    }

    public interface AnimacaoIntent {
        void onExecutarAnimacao();
    }

    public interface AtualizarView{
        void onAtualizar(Usuario usuarioAlvo, boolean newStatus);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_profile_views, parent, false);
        return new AdapterProfileViews.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {

        Usuario usuarioViewer = listaViewers.get(position);

        if (!payloads.isEmpty()) {

            ToastCustomizado.toastCustomizadoCurto("PAYLOAD", context);

            for (Object payload : payloads) {
                if (payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                    if (bundle.containsKey("viewLiberada")) {
                        boolean statusView = bundle.getBoolean("viewLiberada");
                        usuarioViewer.setViewLiberada(statusView);
                        if (holder instanceof ViewHolder) {
                            AdapterProfileViews.ViewHolder holderPrincipal = (AdapterProfileViews.ViewHolder) holder;
                            holderPrincipal.desbloquearUsuario(usuarioViewer);
                        }
                    }
                }
            }
        }

        if (holder instanceof AdapterProfileViews.ViewHolder) {
            AdapterProfileViews.ViewHolder holderPrincipal = (AdapterProfileViews.ViewHolder) holder;

            recuperarUser(usuarioViewer.getIdUsuario(), new DadosViewer() {
                @Override
                public void onRecuperado(Usuario usuarioRecuperado) {

                    if (usuarioRecuperado.getMinhaFoto() != null
                            && !usuarioRecuperado.getMinhaFoto().isEmpty()) {
                        GlideCustomizado.montarGlideEpilepsia(context,
                                usuarioRecuperado.getMinhaFoto(), holderPrincipal.imgViewFotoProfile,
                                android.R.color.transparent);
                    }

                    if (usuarioRecuperado.getMeuFundo() != null
                            && !usuarioRecuperado.getMeuFundo().isEmpty()) {
                        GlideCustomizado.montarGlideFotoEpilepsia(context,
                                usuarioRecuperado.getMeuFundo(), holderPrincipal.imgViewFundoProfile,
                                android.R.color.transparent);
                    }

                    String nomeConfigurado = UsuarioUtils.recuperarNomeConfigurado(usuarioRecuperado);

                    holderPrincipal.txtViewNameProfile.setText(nomeConfigurado);

                    holderPrincipal.btnDesbloquearViewer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!usuarioViewer.isViewLiberada()) {
                                holderPrincipal.liberarViewer(usuarioViewer);
                            }
                        }
                    });
                }

                @Override
                public void onSemDados() {

                }

                @Override
                public void onError(String message) {

                }
            });

            if (usuarioViewer.getDataView() != null
                    && !usuarioViewer.getDataView().isEmpty()) {
                holderPrincipal.txtViewDataView.setText(usuarioViewer.getDataView());
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return listaViewers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFundoProfile, imgViewFotoProfile;
        private TextView txtViewNameProfile, txtViewDataView;
        private BlurLayout blurLayoutNome, blurLayoutFoto, blurLayoutFundo;

        //inc_desbloqueio_viewer
        private Button btnDesbloquearViewer, btnVisitarPerfil;
        private ImageButton imgBtnCoins;
        private TextView txtViewCusto;

        public ViewHolder(View itemView) {
            super(itemView);

            //inc_desbloqueio_viewer
            btnDesbloquearViewer = itemView.findViewById(R.id.btnDesbloquearViewer);
            imgBtnCoins = itemView.findViewById(R.id.imgBtnCoinsDesbloqueioViewer);
            txtViewCusto = itemView.findViewById(R.id.txtViewCustoDesbloqueioViewer);

            imgViewFundoProfile = itemView.findViewById(R.id.imgViewIncFundoProfile);
            imgViewFotoProfile = itemView.findViewById(R.id.imgViewIncFotoProfile);
            txtViewNameProfile = itemView.findViewById(R.id.txtViewNameProfile);
            txtViewDataView = itemView.findViewById(R.id.txtViewDataView);
            blurLayoutNome = itemView.findViewById(R.id.blurLayoutNome);
            blurLayoutFoto = itemView.findViewById(R.id.blurLayoutFoto);
            blurLayoutFundo = itemView.findViewById(R.id.blurLayoutFundo);
            btnVisitarPerfil = itemView.findViewById(R.id.btnVisitarPerfilDesbloqueado);
        }

        private void liberarViewer(Usuario usuarioViewer) {

           dadosDesbloqueio(new EsquemaDesbloqueio() {
               @Override
               public void onSaldoValido() {
                    //Possui saldo, verifica existência do viewer.
                    recupDadosViewer(usuarioViewer.getIdUsuario(), this);
               }

               @Override
               public void onViewerExiste() {
                   //Efetuar compra
                   DatabaseReference diminuirSaldoRef = firebaseRef.child("usuarios")
                           .child(idUsuario).child("ogimaCoins");

                   atualizarContador.diminuirCoins(diminuirSaldoRef, Usuario.CUSTO_VIEWER, new AtualizarContador.AtualizarCoinsCallback() {
                       @Override
                       public void onSuccess(int coinsAtualizado) {
                           diminuirSaldoRef.setValue(coinsAtualizado).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void unused) {

                                   DatabaseReference atualizarViewerRef = firebaseRef.child("profileViews")
                                           .child(idUsuario).child(usuarioViewer.getIdUsuario())
                                           .child("viewLiberada");

                                   DatabaseReference viewDesbloqueadoRef = firebaseRef.child("profileViewsDesbloqueados")
                                           .child(idUsuario).child(usuarioViewer.getIdUsuario());

                                   atualizarViewerRef.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void unused) {
                                           int posicao = getBindingAdapterPosition();
                                           if (posicao != -1) {
                                               usuarioViewer.setViewLiberada(true);
                                               HashMap<String, Object> dadosDesbloqueados = new HashMap<>();
                                               dadosDesbloqueados.put("dataView", usuarioViewer.getDataView());
                                               dadosDesbloqueados.put("idUsuario", usuarioViewer.getIdUsuario());
                                               dadosDesbloqueados.put("timeStampView", usuarioViewer.getTimeStampView());
                                               dadosDesbloqueados.put("viewLiberada", usuarioViewer.isViewLiberada());
                                               viewDesbloqueadoRef.setValue(dadosDesbloqueados).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void unused) {
                                                       DatabaseReference removerViewerRef = firebaseRef.child("profileViews")
                                                               .child(idUsuario).child(usuarioViewer.getIdUsuario());
                                                       removerViewerRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                           @Override
                                                           public void onSuccess(Void unused) {
                                                               atualizarViewListener.onAtualizar(usuarioViewer, true);
                                                           }
                                                       });
                                                   }
                                               });
                                           }
                                       }
                                   });
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   ToastCustomizado.toastCustomizado("Ocorreu um erro ao efetuar desbloqueio, tente novamente", context);
                               }
                           });
                       }

                       @Override
                       public void onError(String errorMessage) {

                       }
                   });
               }

               @Override
               public void onViewerNaoExiste() {
                   ToastCustomizado.toastCustomizado("Usuário selecionado não existe mais", context);
               }

               @Override
               public void onSaldoInsuficiente() {
                    ToastCustomizado.toastCustomizado("Ogima coins insuficientes", context);
               }

               @Override
               public void onError(String message) {
                    ToastCustomizado.toastCustomizado("Erro ao efetuar desbloqueio: " + message, context);
               }
           });
        }

        private void desbloquearUsuario(Usuario usuarioViewer){

            if (usuarioViewer.isViewLiberada()) {
                FirebaseRecuperarUsuario.recuperaUsuarioCompleto(usuarioViewer.getIdUsuario(), new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
                    @Override
                    public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUser, String fundoUser) {
                        //Desbloqueado
                        blurLayoutNome.setVisibility(View.GONE);
                        blurLayoutFoto.setVisibility(View.GONE);
                        blurLayoutFundo.setVisibility(View.GONE);
                        txtViewCusto.setVisibility(View.GONE);
                        btnDesbloquearViewer.setVisibility(View.GONE);
                        imgBtnCoins.setVisibility(View.GONE);

                        GlideCustomizado.getSharedGlideInstance(context)
                                .clear(imgViewFotoProfile);

                        GlideCustomizado.getSharedGlideInstance(context)
                                .clear(imgViewFundoProfile);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(fotoUser != null && !fotoUser.isEmpty()){
                                    GlideCustomizado.loadUrl(context,
                                            fotoUser, imgViewFotoProfile,
                                            android.R.color.transparent, GlideCustomizado.CIRCLE_CROP,
                                            false, isStatusEpilepsia());
                                }

                                if (fundoUser != null && !fundoUser.isEmpty()) {
                                    GlideCustomizado.loadUrl(context,
                                            fundoUser, imgViewFundoProfile,
                                            android.R.color.transparent, GlideCustomizado.CENTER_CROP,
                                            false, isStatusEpilepsia());
                                }

                                btnVisitarPerfil.setVisibility(View.VISIBLE);

                                btnVisitarPerfil.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        visitarPerfil(usuarioViewer.getIdUsuario());
                                    }
                                });
                            }
                        }, 100);
                    }

                    @Override
                    public void onSemDados() {

                    }

                    @Override
                    public void onError(String mensagem) {

                    }
                });
            }
        }
    }

    private void visitarPerfil(String idDonoPerfil) {
        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context,
                idDonoPerfil);
        animacaoIntentListener.onExecutarAnimacao();
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    private void recuperarUser(String idViewer, DadosViewer callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idViewer, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                callback.onRecuperado(usuarioAtual);
            }

            @Override
            public void onSemDados() {
                callback.onSemDados();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private void dadosDesbloqueio(EsquemaDesbloqueio callback) {
        DatabaseReference diminuirSaldoRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("ogimaCoins");

        diminuirSaldoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    int ogimaCoins = snapshot.getValue(Integer.class);
                    if (ogimaCoins != -1
                            && ogimaCoins >= Usuario.CUSTO_VIEWER) {
                        callback.onSaldoValido();
                    }else{
                        callback.onSaldoInsuficiente();
                    }
                } else {
                    callback.onSaldoInsuficiente();
                }
                diminuirSaldoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void recupDadosViewer(String idViewer, EsquemaDesbloqueio callback){

        DatabaseReference verificaViewerRef = firebaseRef.child("usuarios")
                        .child(idViewer);

        verificaViewerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    callback.onViewerExiste();
                }else{
                    callback.onViewerNaoExiste();
                }
                verificaViewerRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}