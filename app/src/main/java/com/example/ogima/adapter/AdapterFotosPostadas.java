package com.example.ogima.adapter;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.EdicaoFotoActivity;
import com.example.ogima.activity.FotosPostadasActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
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

public class AdapterFotosPostadas extends RecyclerView.Adapter<AdapterFotosPostadas.ViewHolder> {

    private List<Postagem> listaFotosPostadas;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private StorageReference storage;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private String idUsuarioRecebido;
    int contadorAtual;
    Postagem usuarioFotos, usuarioFotosRecentes, postagemArray;
    private DatabaseReference contadorUsuarioRef, listaPostagensRef;
    private String removidoOrdem;
    private String donoPostagem;
    private ArrayList<String> capturarCaminhos = new ArrayList<>();

    public AdapterFotosPostadas(List<Postagem> listFotosPostadas, Context c, String idRecebido) {
        this.listaFotosPostadas = listFotosPostadas;
        this.context = c;
        this.idUsuarioRecebido = idRecebido;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        storage = ConfiguracaoFirebase.getFirebaseStorage();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Captura os componentes do layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_fotos_postadas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //A lógica é executada aqui.

        //Ordenando a lista em ordem decrescente
        Collections.sort(listaFotosPostadas, new Comparator<Postagem>() {
            public int compare(Postagem o1, Postagem o2) {
                return o2.getDataPostagemNova().compareTo(o1.getDataPostagemNova());
            }
        });

        Postagem usuarioFotosPostadas = listaFotosPostadas.get(position);

        if(idUsuarioRecebido != null){
            contadorUsuarioRef = firebaseRef.child("complementoFoto")
                    .child(idUsuarioRecebido);
            holder.buttonExcluirFotoPostagem.setVisibility(View.GONE);
            holder.buttonEditarFotoPostagem.setVisibility(View.GONE);
        }else{
            contadorUsuarioRef = firebaseRef.child("complementoFoto")
                    .child(idUsuarioLogado);
            holder.buttonExcluirFotoPostagem.setVisibility(View.VISIBLE);
            holder.buttonEditarFotoPostagem.setVisibility(View.VISIBLE);
            donoPostagem = idUsuarioLogado;
            listaPostagensRef = firebaseRef
                    .child("complementoFoto").child(idUsuarioLogado).child("listaCaminhoFotos");
        }



        contadorUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                usuarioFotos = snapshot.getValue(Postagem.class);

                if (snapshot.getValue() != null) {
                    try {

                        //Contador
                        contadorAtual = usuarioFotos.getContadorFotos();

                        //Passando os dados para os elementos.
                        GlideCustomizado.fundoGlideEpilepsia(context, usuarioFotosPostadas.getCaminhoPostagem(),
                                holder.imageAdFotoPostada, android.R.color.transparent);
                        holder.textAdDataPostada.setText(usuarioFotosPostadas.getDataPostagem());
                        holder.textViewTituloFoto.setText(usuarioFotosPostadas.getTituloPostagem());
                        holder.textViewDescricaoFoto.setText(usuarioFotosPostadas.getDescricaoPostagem());
                        holder.txtViewPublicoPostagem.setText("Visível para: " + usuarioFotosPostadas.getPublicoPostagem());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    holder.imageAdFotoPostada.setImageResource(R.drawable.avatarfemale);
                    holder.textAdDataPostada.setText("Sem postagens");
                }

                contadorUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Novo
        DatabaseReference organizarArrayFotosRef = firebaseRef.child("complementoFoto")
                .child(idUsuarioLogado);

        organizarArrayFotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    postagemArray = snapshot.getValue(Postagem.class);
                }
                organizarArrayFotosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(idUsuarioRecebido == null){

            //Fazer algum jeito de remover a referencia da foto igual a do usuario atual.

            organizarArrayFotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try{
                        if(snapshot.getValue() != null){

                            //Preenchendo array de fotos postadas com as últimas
                            //adicionadas
                            DatabaseReference listaPostagensRef = firebaseRef
                                    .child("complementoFoto").child(idUsuarioLogado).child("listaCaminhoFotos");

                            usuarioFotosRecentes = listaFotosPostadas.get(position);

                            ArrayList<String> listaPostagens = new ArrayList<>();

                            if (usuarioFotos.getContadorFotos() >= 4) {

                                usuarioFotosRecentes = listaFotosPostadas.get(0);
                                listaPostagens.add(0, usuarioFotosRecentes.getCaminhoPostagem());
                                usuarioFotosRecentes = listaFotosPostadas.get(1);
                                listaPostagens.add(1, usuarioFotosRecentes.getCaminhoPostagem());
                                usuarioFotosRecentes = listaFotosPostadas.get(2);
                                listaPostagens.add(2, usuarioFotosRecentes.getCaminhoPostagem());
                                usuarioFotosRecentes = listaFotosPostadas.get(3);
                                listaPostagens.add(3, usuarioFotosRecentes.getCaminhoPostagem());
                                listaPostagensRef.setValue(listaPostagens);

                            } else if (usuarioFotos.getContadorFotos() == 3) {

                                usuarioFotosRecentes = listaFotosPostadas.get(0);
                                listaPostagens.add(0, usuarioFotosRecentes.getCaminhoPostagem());
                                usuarioFotosRecentes = listaFotosPostadas.get(1);
                                listaPostagens.add(1, usuarioFotosRecentes.getCaminhoPostagem());
                                usuarioFotosRecentes = listaFotosPostadas.get(2);
                                listaPostagens.add(2, usuarioFotosRecentes.getCaminhoPostagem());
                                listaPostagensRef.setValue(listaPostagens);

                            } else if (usuarioFotos.getContadorFotos() == 2) {

                                usuarioFotosRecentes = listaFotosPostadas.get(0);
                                listaPostagens.add(0, usuarioFotosRecentes.getCaminhoPostagem());
                                usuarioFotosRecentes = listaFotosPostadas.get(1);
                                listaPostagens.add(1, usuarioFotosRecentes.getCaminhoPostagem());
                                listaPostagensRef.setValue(listaPostagens);

                            } else if (usuarioFotos.getContadorFotos() == 1) {

                                usuarioFotosRecentes = listaFotosPostadas.get(0);
                                listaPostagens.add(0, usuarioFotosRecentes.getCaminhoPostagem());
                                listaPostagensRef.setValue(listaPostagens);
                            }
                        }
                        organizarArrayFotosRef.removeEventListener(this);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        holder.buttonExcluirFotoPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Dados relacionados a postagem recem adicioandos
                DatabaseReference removerCurtidasFotoRef = firebaseRef
                        .child("curtidasFoto").child(usuarioFotosPostadas.getIdPostagem());

                DatabaseReference removerComentariosFotoRef = firebaseRef
                        .child("comentariosFoto").child(usuarioFotosPostadas.getIdPostagem());

                DatabaseReference removerDenunciaFotoRef = firebaseRef
                        .child("fotosDenunciadas").child(usuarioFotosPostadas.getIdPostagem());

                DatabaseReference removerCurtidasComentarioRef = firebaseRef
                        .child("curtidasComentarioFoto").child(usuarioFotosPostadas.getIdPostagem());

                DatabaseReference fotosUsuarioRef = firebaseRef.child("fotosUsuario")
                        .child(idUsuarioLogado);

                DatabaseReference removerDenunciaComentarioRef = firebaseRef
                        .child("comentariosDenunciadosFoto").child(usuarioFotosPostadas.getIdPostagem());

                //Referência para remoção do contador
                DatabaseReference removerContadorRef = firebaseRef
                        .child("complementoFoto").child(idUsuarioLogado)
                        .child("contadorFotos");

                DatabaseReference removerComentarioRef = firebaseRef.child("comentariosFoto")
                        .child(usuarioFotosPostadas.getIdPostagem());

                //AlertDialog com progressbar
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                ProgressDialog progressDialog = new ProgressDialog(view.getRootView().getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Excluindo, por favor aguarde...");
                builder.setTitle("Deseja prosseguir com a exclusão da foto?");
                builder.setMessage("Assim que excluída não será possível recuperá-la");
                builder.setCancelable(false);
                progressDialog.setCancelable(false);
                builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog.show();

                        //Removendo a foto do storage
                        StorageReference imagemNewRef = storage.child("imagens")
                                .child("complementoFoto").child(idUsuarioLogado).getStorage().getReferenceFromUrl(usuarioFotosPostadas.getCaminhoPostagem());
                        imagemNewRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    try {
                                        contadorAtual = contadorAtual - 1;

                                        //Novo - Excluindo do array o caminho da foto
                                        //da postagem selecionada
                                        capturarCaminhos = postagemArray.getListaCaminhoFotos();
                                        Iterator itr = capturarCaminhos.iterator();
                                        while(itr.hasNext()){
                                            if(itr.next().equals(usuarioFotosPostadas.getCaminhoPostagem()))
                                                itr.remove();
                                        }

                                        //Salvando o array atualizado de caminhos das fotos no DB.
                                        listaPostagensRef.setValue(capturarCaminhos);

                                        removerComentarioRef.removeValue();

                                        DatabaseReference excluirPostagemRef = firebaseRef
                                                .child("fotosUsuario").child(idUsuarioLogado)
                                                .child(usuarioFotosPostadas.getIdPostagem());

                                        //Removendo postagem do usuário pelo id da postagem.
                                        excluirPostagemRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //Atualizando o contador
                                                    removerContadorRef.setValue(contadorAtual).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {

                                                                //Adicionado novas exclusões a partir daqui
                                                                removerCurtidasFotoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            removerComentariosFotoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        removerDenunciaFotoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    removerCurtidasComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                removerDenunciaComentarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                         if(task.isSuccessful()){
                                                                                                                             if (contadorAtual <= 0) {
                                                                                                                                 //removendo o contador seja <= a 0
                                                                                                                                 fotosUsuarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                     @Override
                                                                                                                                     public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                         if (task.isSuccessful()) {
                                                                                                                                             try {
                                                                                                                                                 /*
                                                                                                                                                 DatabaseReference refreshRef = firebaseRef
                                                                                                                                                         .child("usuarios").child(idUsuarioLogado)
                                                                                                                                                         .child("sinalizarRefresh");
                                                                                                                                                 refreshRef.setValue("atualizar").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                                     @Override
                                                                                                                                                     public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                         if (task.isSuccessful()) {
                                                                                                                                                             //Lógica desnecessária em salvar dados para o DB
                                                                                                                                                         }
                                                                                                                                                     }
                                                                                                                                                 });
                                                                                                                                                  */
                                                                                                                                                    //Ajustar a posição
                                                                                                                                                 if (usuarioFotos.getContadorFotos() == 1) {
                                                                                                                                                     Intent intent = new Intent(context, NavigationDrawerActivity.class);
                                                                                                                                                     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                                                                     intent.putExtra("intentPerfilFragment", "intentPerfilFragment");
                                                                                                                                                     context.startActivity(intent);
                                                                                                                                                     ((Activity) view.getContext()).finish();
                                                                                                                                                 } else {
                                                                                                                                                     int qntFotos = usuarioFotos.getContadorFotos();
                                                                                                                                                     if (position == qntFotos - 1) {
                                                                                                                                                         listaFotosPostadas.remove(position);
                                                                                                                                                         notifyItemRemoved(position);
                                                                                                                                                     } else {
                                                                                                                                                         listaFotosPostadas.remove(position);
                                                                                                                                                         notifyDataSetChanged();
                                                                                                                                                     }
                                                                                                                                                     FotosPostadasActivity fotosPostadasActivity = new FotosPostadasActivity();
                                                                                                                                                     if (position == qntFotos - 1) {
                                                                                                                                                         fotosPostadasActivity.reterPosicao(context, qntFotos, position, "ultimo");
                                                                                                                                                     } else {
                                                                                                                                                         fotosPostadasActivity.reterPosicao(context, qntFotos, position, "nãoUltimo");
                                                                                                                                                     }
                                                                                                                                                 }

                                                                                                                                                 DatabaseReference removerFotosUsuarioRef = firebaseRef
                                                                                                                                                         .child("complementoFoto").child(idUsuarioLogado);
                                                                                                                                                 //Removendo o nó complementoFoto quando o contador chega a 0
                                                                                                                                                 removerFotosUsuarioRef.removeValue();

                                                                                                                                             } catch (Exception ex) {
                                                                                                                                                 ex.printStackTrace();
                                                                                                                                             }
                                                                                                                                         }
                                                                                                                                     }
                                                                                                                                 });
                                                                                                                             } else {
                                                                                                                                 try {
                                                                                                                                     if (usuarioFotos.getContadorFotos() == 1) {
                                                                                                                                         ((Activity) view.getContext()).finish();
                                                                                                                                     } else {
                                                                                                                                         int fotosTotal = usuarioFotos.getContadorFotos();
                                                                                                                                         if (position == fotosTotal - 1) {
                                                                                                                                             listaFotosPostadas.remove(position);
                                                                                                                                             notifyItemRemoved(position);
                                                                                                                                         } else {
                                                                                                                                             listaFotosPostadas.remove(position);
                                                                                                                                             notifyDataSetChanged();
                                                                                                                                         }
                                                                                                                                         FotosPostadasActivity fotosPostadasActivity = new FotosPostadasActivity();
                                                                                                                                         if (position == fotosTotal - 1) {
                                                                                                                                             fotosPostadasActivity.reterPosicao(context, fotosTotal, position, "ultimo");
                                                                                                                                         } else {
                                                                                                                                             fotosPostadasActivity.reterPosicao(context, fotosTotal, position, "nãoUltimo");
                                                                                                                                         }
                                                                                                                                     }
                                                                                                                                 } catch (Exception ex) {
                                                                                                                                     ex.printStackTrace();
                                                                                                                                 }
                                                                                                                             }
                                                                                                                         }
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                    ToastCustomizado.toastCustomizadoCurto("Excluido com sucesso", context);
                                                } else {
                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao excluir, tente novamente", context);
                                                }
                                            }
                                        });
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    progressDialog.dismiss();
                                } else {
                                    progressDialog.dismiss();
                                    ToastCustomizado.toastCustomizadoCurto("Erro ao excluir, tente novamente", context);
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        holder.buttonEditarFotoPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(context.getApplicationContext(), EdicaoFotoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", usuarioFotosPostadas.getTituloPostagem());
                    intent.putExtra("descricao", usuarioFotosPostadas.getDescricaoPostagem());
                    intent.putExtra("foto", usuarioFotosPostadas.getCaminhoPostagem());
                    intent.putExtra("idPostagem", usuarioFotosPostadas.getIdPostagem());
                    intent.putExtra("posicao", position);
                    intent.putExtra("publicoPostagem", usuarioFotosPostadas.getPublicoPostagem());
                    context.startActivity(intent);
                    ((Activity) view.getContext()).finish();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        holder.imgButtonDetalhesPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", usuarioFotosPostadas.getTituloPostagem());
                    intent.putExtra("descricao", usuarioFotosPostadas.getDescricaoPostagem());
                    intent.putExtra("foto", usuarioFotosPostadas.getCaminhoPostagem());
                    intent.putExtra("idPostagem", usuarioFotosPostadas.getIdPostagem());
                    intent.putExtra("idRecebido", idUsuarioRecebido);
                    intent.putExtra("dataPostagem", usuarioFotosPostadas.getDataPostagem());
                    intent.putExtra("donoPostagem", donoPostagem);
                    intent.putExtra("publicoPostagem", usuarioFotosPostadas.getPublicoPostagem());
                    context.startActivity(intent);
                    ((Activity) view.getContext()).finish();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        //Retorna o tamanho da lista
        return listaFotosPostadas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //Inicializa os componentes do layout
        private TextView textAdDataPostada, textViewTituloFoto, textViewDescricaoFoto,
                txtViewPublicoPostagem;
        private PhotoView imageAdFotoPostada;
        private Button buttonEditarFotoPostagem, buttonExcluirFotoPostagem;
        private ImageButton imgButtonDetalhesPostagem;

        public ViewHolder(@NonNull View itemView) {
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
}