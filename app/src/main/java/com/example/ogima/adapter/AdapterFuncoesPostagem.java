package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.DetalhesPostagemActivity;
import com.example.ogima.activity.EdicaoFotoActivity;
import com.example.ogima.activity.FotosPostadasActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AdapterFuncoesPostagem extends RecyclerView.Adapter {

    private List<Postagem> listaPostagemImagem;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private StorageReference storage;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private String idUsuarioRecebido;
    //
    private static final String TAG = "RecyclerAdapter";
    private int totalPostagens;
    private ArrayList<String> capturarCaminhos = new ArrayList<>();


    public AdapterFuncoesPostagem(List<Postagem> listPostagemImagem, Context c, String idRecebido) {
        this.context = c;
        this.idUsuarioRecebido = idRecebido;
        this.listaPostagemImagem = listPostagemImagem;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        storage = ConfiguracaoFirebase.getFirebaseStorage();
    }

    @Override
    public int getItemViewType(int position) {
        if (listaPostagemImagem.get(position).getTipoPostagem().equals("Gif")) {
            return 1;
        } else if (listaPostagemImagem.get(position).getTipoPostagem().equals("Video")) {
            return 2;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == 1) {
            view = layoutInflater.inflate(R.layout.adapter_postagem_gif, parent, false);
            return new ViewHolderGif(view);
        } else if (viewType == 2) {
            view = layoutInflater.inflate(R.layout.adapter_postagem_video, parent, false);
            return new ViewHolderVideo(view);
        }
        view = layoutInflater.inflate(R.layout.adapter_fotos_postadas, parent, false);
        return new ViewHolderImagem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (listaPostagemImagem.get(position).getTipoPostagem().equals("Gif")) {
            ViewHolderGif viewHolderGif = (ViewHolderGif) holder;
            if(!idUsuarioRecebido.equals(idUsuarioLogado)){

            }
        } else if (listaPostagemImagem.get(position).getTipoPostagem().equals("Video")) {
            ViewHolderVideo viewHolderVideo = (ViewHolderVideo) holder;
        } else if (listaPostagemImagem.get(position).getTipoPostagem().equals("imagem")) {
            ViewHolderImagem viewHolderImagem = (ViewHolderImagem) holder;
            Postagem postagemImagem = listaPostagemImagem.get(position);

            //Tratando da exibição do button de excluir e editar postagem
            if(!idUsuarioRecebido.equals(idUsuarioLogado)){
                viewHolderImagem.buttonExcluirFotoPostagem.setVisibility(View.GONE);
                viewHolderImagem.buttonEditarFotoPostagem.setVisibility(View.GONE);
            }else{
                viewHolderImagem.buttonExcluirFotoPostagem.setVisibility(View.VISIBLE);
                viewHolderImagem.buttonEditarFotoPostagem.setVisibility(View.VISIBLE);
            }

            //Referência dos dados do usuário recebido - referência
            DatabaseReference dadosUsuarioRef = firebaseRef.child("usuarios")
                    .child(idUsuarioLogado);

            dadosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioRecebido = snapshot.getValue(Usuario.class);

                        //Exibindo conteúdo da postagem
                        GlideCustomizado.montarGlideFoto(context, postagemImagem.getUrlPostagem(),
                                viewHolderImagem.imageAdFotoPostada, android.R.color.transparent);

                        //Exibindo título da postagem
                        if (postagemImagem.getTipoPostagem() != null) {
                            viewHolderImagem.textViewTituloFoto.setText(postagemImagem.getTituloPostagem());
                        }
                        //Exibindo descrição da postagem
                        if (postagemImagem.getDescricaoPostagem() != null) {
                            viewHolderImagem.textViewDescricaoFoto.setText(postagemImagem.getDescricaoPostagem());
                        }
                        //Exibindo data da publicação da postagem
                        viewHolderImagem.textAdDataPostada.setText(postagemImagem.getDataPostagem());
                        //Exibindo público da postagem
                        viewHolderImagem.txtViewPublicoPostagem.setText("Visível para: " + postagemImagem.getPublicoPostagem());

                        //Organizando array de exibição de postagens - referência
                        DatabaseReference organizarArrayUrlRef = firebaseRef.child("complementoPostagem")
                                .child(idUsuarioLogado);

                        //Salvar array atualizado - referência
                        DatabaseReference atualizandoArrayUrl = organizarArrayUrlRef
                                .child("listaUrlPostagens");

                        organizarArrayUrlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    try{
                                        Postagem postagemComplemento = snapshot.getValue(Postagem.class);

                                        ArrayList<String> listaUrlPostagemUpdate = new ArrayList<>();

                                        //Percorrendo array e populando com dados atualizados.
                                        if (postagemComplemento.getTotalPostagens() >= 0) {
                                            for (int i = 0; listaUrlPostagemUpdate.size() < 4; i++) {
                                                Postagem postagemUrl = listaPostagemImagem.get(i);
                                                listaUrlPostagemUpdate.add(i, postagemUrl.getUrlPostagem());
                                                //Log.i("Lista - ",  i  + listaUrlPostagemUpdate.get(i));
                                            }
                                            atualizandoArrayUrl.setValue(listaUrlPostagemUpdate);
                                        }
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }
                                }
                                organizarArrayUrlRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                           //Excluindo postagem somente se o usuário atual logado for o dono dela.
                            if(idUsuarioRecebido.equals(idUsuarioLogado)){
                                viewHolderImagem.buttonExcluirFotoPostagem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ToastCustomizado.toastCustomizadoCurto("Exluindo",context);

                                        //Referências para exclusão da postagem por completo.
                                        DatabaseReference removerCurtidasPostagemRef = firebaseRef
                                                .child("curtidasPostagem").child(postagemImagem.getIdPostagem());
                                        DatabaseReference removerComentariosRef = firebaseRef
                                                .child("comentariosPostagem").child(postagemImagem.getIdPostagem());
                                        DatabaseReference removerDenunciaPostagemRef = firebaseRef
                                                .child("postagensDenunciadasPostagem").child(postagemImagem.getIdPostagem());
                                        DatabaseReference removerCurtidasComentarioRef = firebaseRef
                                                .child("curtidasComentarioPostagem").child(postagemImagem.getIdPostagem());
                                        DatabaseReference postagensUsuarioRef = firebaseRef.child("postagens")
                                                .child(idUsuarioLogado);
                                        DatabaseReference removerDenunciaComentarioRef = firebaseRef
                                                .child("comentariosDenunciadosPostagem").child(postagemImagem.getIdPostagem());
                                        DatabaseReference removerContadorRef = firebaseRef
                                                .child("complementoPostagem").child(idUsuarioLogado)
                                                .child("totalPostagens");
                                        DatabaseReference removerComentarioRef = firebaseRef.child("comentariosPostagem")
                                                .child(postagemImagem.getIdPostagem());
                                        //Referência que precisa ser adicionado no AdapterFotosPostadas
                                        DatabaseReference removerViewPostagemRef = firebaseRef.child("visualizacoesPostagem")
                                                .child(postagemImagem.getIdPostagem()).child(idUsuarioLogado);
                                        //Referência para excluir a postagem
                                        DatabaseReference excluirPostagemRef = firebaseRef.child("postagens")
                                                .child(idUsuarioLogado).child(postagemImagem.getIdPostagem());

                                        //AlertDialog com progressbar
                                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                                        ProgressDialog progressDialog = new ProgressDialog(view.getRootView().getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        progressDialog.setMessage("Excluindo, por favor aguarde...");
                                        builder.setTitle("Deseja prosseguir com a exclusão da postagem?");
                                        builder.setMessage("Assim que excluída não será possível recuperá-la");
                                        builder.setCancelable(false);
                                        progressDialog.setCancelable(false);
                                        builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                progressDialog.show();

                                                DatabaseReference complementoPostagemRef = firebaseRef.child("complementoPostagem")
                                                        .child(idUsuarioLogado);

                                                complementoPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.getValue() != null){
                                                            Postagem postagemComplementoV2 = snapshot.getValue(Postagem.class);
                                                            //Removendo a foto do storage
                                                            StorageReference storageExcluirimagem = storage
                                                                    .child("postagens").child("fotos")
                                                                    .child(idUsuarioLogado).getStorage()
                                                                    .getReferenceFromUrl(postagemImagem.getUrlPostagem());
                                                            storageExcluirimagem.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        totalPostagens = postagemComplementoV2.getTotalPostagens() - 1;
                                                                        //Excluindo do array o caminho da foto
                                                                        //da postagem selecionada
                                                                        capturarCaminhos = postagemComplementoV2.getListaUrlPostagens();
                                                                        Iterator itr = capturarCaminhos.iterator();
                                                                        while(itr.hasNext()){
                                                                            if(itr.next().equals(postagemImagem.getUrlPostagem()))
                                                                                itr.remove();
                                                                        }
                                                                        //Salvando o array atualizado de caminhos das fotos no DB.
                                                                        atualizandoArrayUrl.setValue(capturarCaminhos);
                                                                        //Removendo comentários da postagem
                                                                        removerComentarioRef.removeValue();
                                                                        //Removendo visualizações dessa postagem
                                                                        removerViewPostagemRef.removeValue();
                                                                        //Removendo a postagem
                                                                        excluirPostagemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                removerContadorRef.setValue(totalPostagens).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        removerCurtidasPostagemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void unused) {
                                                                                                removerComentariosRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void unused) {
                                                                                                        removerDenunciaPostagemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void unused) {
                                                                                                                removerDenunciaComentarioRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Void unused) {
                                                                                                                        removerCurtidasComentarioRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onSuccess(Void unused) {
                                                                                                                                if(totalPostagens <= 0){
                                                                                                                                    postagensUsuarioRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onSuccess(Void unused) {
                                                                                                                                            try{
                                                                                                                                                if (postagemComplementoV2.getTotalPostagens() == 1) {
                                                                                                                                                    Intent intent = new Intent(context, NavigationDrawerActivity.class);
                                                                                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                                                                    intent.putExtra("intentPerfilFragment", "intentPerfilFragment");
                                                                                                                                                    context.startActivity(intent);
                                                                                                                                                    ((Activity) view.getContext()).finish();
                                                                                                                                                }else{
                                                                                                                                                    int qntFotos = postagemComplementoV2.getTotalPostagens();
                                                                                                                                                    if (position == qntFotos - 1) {
                                                                                                                                                        listaPostagemImagem.remove(position);
                                                                                                                                                        notifyItemRemoved(position);
                                                                                                                                                    } else {
                                                                                                                                                        listaPostagemImagem.remove(position);
                                                                                                                                                        notifyDataSetChanged();
                                                                                                                                                    }
                                                                                                                                                    DetalhesPostagemActivity detalhesPostagemActivity = new DetalhesPostagemActivity();
                                                                                                                                                    if (position == qntFotos - 1) {
                                                                                                                                                        detalhesPostagemActivity.reterPosicao(context, qntFotos, position, "ultimo");
                                                                                                                                                    } else {
                                                                                                                                                        detalhesPostagemActivity.reterPosicao(context, qntFotos, position, "nãoUltimo");
                                                                                                                                                    }
                                                                                                                                                }

                                                                                                                                                DatabaseReference removerDetalhesPostagemRef = firebaseRef
                                                                                                                                                        .child("complementoPostagem").child(idUsuarioLogado);
                                                                                                                                                //Removendo o nó complementoPostagem quando o contador chega a 0
                                                                                                                                                removerDetalhesPostagemRef.removeValue();

                                                                                                                                            }catch (Exception ex){
                                                                                                                                                ex.printStackTrace();
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                }else{
                                                                                                                                    try{
                                                                                                                                        if(postagemComplementoV2.getTotalPostagens() == 1){
                                                                                                                                            Intent intent = new Intent(context, NavigationDrawerActivity.class);
                                                                                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                                                            intent.putExtra("intentPerfilFragment", "intentPerfilFragment");
                                                                                                                                            context.startActivity(intent);
                                                                                                                                            ((Activity) view.getContext()).finish();
                                                                                                                                        }else{
                                                                                                                                            int fotosTotal = postagemComplementoV2.getContadorFotos();
                                                                                                                                            if (position == fotosTotal - 1) {
                                                                                                                                                listaPostagemImagem.remove(position);
                                                                                                                                                notifyItemRemoved(position);
                                                                                                                                            } else {
                                                                                                                                                listaPostagemImagem.remove(position);
                                                                                                                                                notifyDataSetChanged();
                                                                                                                                            }
                                                                                                                                            DetalhesPostagemActivity detalhesPostagemActivity = new DetalhesPostagemActivity();
                                                                                                                                            if (position == fotosTotal - 1) {
                                                                                                                                                detalhesPostagemActivity.reterPosicao(context, fotosTotal, position, "ultimo");
                                                                                                                                            } else {
                                                                                                                                                detalhesPostagemActivity.reterPosicao(context, fotosTotal, position, "nãoUltimo");
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }catch (Exception ex){
                                                                                                                                        ex.printStackTrace();
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        });
                                                                                        progressDialog.dismiss();
                                                                                        ToastCustomizado.toastCustomizadoCurto("Excluido com sucesso", context);
                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        progressDialog.dismiss();
                                                                                        ToastCustomizado.toastCustomizadoCurto("Erro ao excluir, tente novamente", context);
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                            progressDialog.dismiss();
                                                        }
                                                        complementoPostagemRef.removeEventListener(this);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }).setNegativeButton("Cancelar", null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                });

                                viewHolderImagem.buttonEditarFotoPostagem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(context.getApplicationContext(), EdicaoFotoActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("tipoPublicacao", "postagem");
                                        intent.putExtra("titulo", postagemImagem.getTituloPostagem());
                                        intent.putExtra("descricao", postagemImagem.getDescricaoPostagem());
                                        intent.putExtra("foto", postagemImagem.getUrlPostagem());
                                        intent.putExtra("idPostagem", postagemImagem.getIdPostagem());
                                        intent.putExtra("posicao", position);
                                        intent.putExtra("publicoPostagem", postagemImagem.getPublicoPostagem());
                                        intent.putExtra("tipoPostagem", postagemImagem.getTipoPostagem());
                                        context.startActivity(intent);
                                        ((Activity) view.getContext()).finish();
                                    }
                                });

                                viewHolderImagem.imgButtonDetalhesPostagem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(context, TodasFotosUsuarioActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("titulo", postagemImagem.getTituloPostagem());
                                        intent.putExtra("descricao", postagemImagem.getDescricaoPostagem());
                                        intent.putExtra("foto", postagemImagem.getUrlPostagem());
                                        intent.putExtra("idPostagem", postagemImagem.getIdPostagem());
                                        intent.putExtra("idRecebido", idUsuarioRecebido);
                                        intent.putExtra("dataPostagem", postagemImagem.getDataPostagem());
                                        intent.putExtra("donoPostagem", postagemImagem.getIdDonoPostagem());
                                        intent.putExtra("publicoPostagem", postagemImagem.getPublicoPostagem());
                                        intent.putExtra("irParaPerfil", "irParaPerfil");
                                        context.startActivity(intent);
                                        ((Activity) view.getContext()).finish();
                                    }
                                });
                            }
                    }
                    dadosUsuarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaPostagemImagem.size();
    }

    class ViewHolderImagem extends RecyclerView.ViewHolder {

        //Layout - adapter_fotos_postadas
        private TextView textAdDataPostada, textViewTituloFoto, textViewDescricaoFoto,
                txtViewPublicoPostagem;
        private PhotoView imageAdFotoPostada;
        private Button buttonEditarFotoPostagem, buttonExcluirFotoPostagem;
        private ImageButton imgButtonDetalhesPostagem;

        public ViewHolderImagem(@NonNull View itemView) {
            super(itemView);

            textAdDataPostada = itemView.findViewById(R.id.textAdDataPostada);
            imageAdFotoPostada = itemView.findViewById(R.id.imageAdFotoPostada);
            textViewTituloFoto = itemView.findViewById(R.id.textViewTituloFoto);
            textViewDescricaoFoto = itemView.findViewById(R.id.textViewDescricaoFoto);
            txtViewPublicoPostagem = itemView.findViewById(R.id.txtViewPublicoPostagem);
            buttonEditarFotoPostagem = itemView.findViewById(R.id.buttonEditarFotoPostagem);
            buttonExcluirFotoPostagem = itemView.findViewById(R.id.buttonExcluirFotoPostagem);
            imgButtonDetalhesPostagem = itemView.findViewById(R.id.imgButtonDetalhesPostagem);
        }
    }

    class ViewHolderGif extends RecyclerView.ViewHolder {

        //Layout - adapter_postagem_gif
        private TextView txtViewPostagemGif;

        public ViewHolderGif(@NonNull View itemView) {
            super(itemView);

            txtViewPostagemGif = itemView.findViewById(R.id.txtViewPostagemGif);
        }
    }

    class ViewHolderVideo extends RecyclerView.ViewHolder {

        //Layout - adapter_postagem_video
        private TextView txtViewPostagemVideo;

        public ViewHolderVideo(@NonNull View itemView) {
            super(itemView);

            txtViewPostagemVideo = itemView.findViewById(R.id.txtViewPostagemVideo);
        }
    }
}


