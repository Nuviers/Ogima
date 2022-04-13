package com.example.ogima.adapter;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.activity.EdicaoFotoActivity;
import com.example.ogima.activity.FotosPostadasActivity;
import com.example.ogima.activity.ProblemasLogin;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import java.util.HashMap;
import java.util.List;

public class AdapterFotosPostadas extends RecyclerView.Adapter<AdapterFotosPostadas.ViewHolder> {

    private List<Usuario> listaFotosPostadas;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private StorageReference storage;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;

    ArrayList<String> listaTitulo = new ArrayList<>();
    ArrayList<String> listaDescricao = new ArrayList<>();
    ArrayList<String> listaFotos = new ArrayList<>();
    ArrayList<String> listaData = new ArrayList<>();
    ArrayList<String> listaIdPostagem = new ArrayList<>();
    ArrayList<Integer> novaListaOrdem = new ArrayList<>();
    String verificarExclusao;
    int contadorAtual;
    int posicaoFoto;
    Usuario usuarioFotos, usuarioUpdate;
    private String indiceItem;

    interface AdapterInteractions {
        public void refreshActivity();
    }

    public AdapterFotosPostadas(List<Usuario> listFotosPostadas, Context c) {
        //Configura os paramêtros do construtor.
        this.listaFotosPostadas = listFotosPostadas;
        //Talvez de pra fazer o sort aqui no adapter pra ver se muda algo
        // ou usar o reverseorder algo do tipo aqui, ou cria um novo objeto
        // e tenta fazer a ordem na classe model
        this.context = c;
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
        Collections.sort(listaFotosPostadas,Usuario.UsuarioDataEF);

        Usuario usuarioFotosPostadas = listaFotosPostadas.get(position);

        DatabaseReference postagensUsuarioRef = firebaseRef.child("postagensUsuario")
                .child(idUsuarioLogado);

        DatabaseReference contadorUsuarioRef = firebaseRef.child("fotosUsuario")
                .child(idUsuarioLogado);

        contadorUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    usuarioFotos = snapshot.getValue(Usuario.class);

                    if(snapshot.getValue() != null){

                        //Contador
                        contadorAtual = usuarioFotos.getContadorFotos();

                        //Configurações para ordenação
                        try {

                                Uri uri = Uri.parse(usuarioFotosPostadas.getCaminhoPostagem());
                                Glide.with(context).load(uri).centerCrop()
                                        .into(holder.imageAdFotoPostada);
                                holder.textAdDataPostada.setText(usuarioFotosPostadas.getDataPostagem());
                                holder.textViewTituloFoto.setText(usuarioFotosPostadas.getTituloPostagem());
                                holder.textViewDescricaoFoto.setText(usuarioFotosPostadas.getDescricaoPostagem());

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }   else {
                        holder.imageAdFotoPostada.setImageResource(R.drawable.avatarfemale);
                        holder.textAdDataPostada.setText("Sem postagens");
                    }

                contadorUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.buttonExcluirFotoPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Referência base
                DatabaseReference fotosUsuarioRef = firebaseRef
                        .child("fotosUsuario").child(idUsuarioLogado);

                DatabaseReference postagensUsuarioRef = firebaseRef.child("postagensUsuario")
                        .child(idUsuarioLogado);

                HashMap<String, Object> dadosPostagem = new HashMap<>();
                dadosPostagem.put("idPostagem", usuarioFotosPostadas.getIdPostagem());
                dadosPostagem.put("caminhoPostagem", usuarioFotosPostadas.getCaminhoPostagem());
                dadosPostagem.put("tituloPostagem", usuarioFotosPostadas.getTituloPostagem());
                dadosPostagem.put("descricaoPostagem", usuarioFotosPostadas.getDescricaoPostagem());
                dadosPostagem.put("dataPostagem", usuarioFotosPostadas.getDataPostagem());
                dadosPostagem.put("ordemPostagem", usuarioFotosPostadas.getOrdemPostagem());

                //Remove o título
                DatabaseReference removerTituloRef = postagensUsuarioRef
                        .child("listaTituloFotoPostada");

                //Remove a descrição
                DatabaseReference removerDescricaoRef = postagensUsuarioRef
                        .child("listaDescricaoFotoPostada");

                //Remove a data
                DatabaseReference removerDataRef = postagensUsuarioRef
                        .child("listaDatasFotos");

                //Remove a foto
                DatabaseReference removerFotoRef = postagensUsuarioRef
                        .child("listaFotosUsuario");

                //Remove o índice na ordenação
                DatabaseReference removerOrdemRef = postagensUsuarioRef
                        .child("listaOrdenacaoFotoPostada");

                //Remove o contador
                DatabaseReference removerContadorRef = firebaseRef
                        .child("fotosUsuario").child(idUsuarioLogado)
                        .child("contadorFotos");

                //Remove o contador
                DatabaseReference removerIdPostagemRef = postagensUsuarioRef
                        .child("listaIdPostagem");


                //AlertDialog com progressbar
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                ProgressDialog progressDialog = new ProgressDialog(view.getRootView().getContext(),ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
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
                        //Referência do storage
                        //Aqui deve ter sido colocado depois de remover as parada

                        //ToastCustomizado.toastCustomizadoCurto("Posição " + posicaoFoto,context);
                        StorageReference imagemRef = storage
                                .getStorage().getReferenceFromUrl(usuarioFotosPostadas.getCaminhoPostagem());
                        imagemRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete()){
                                    try{
                                        contadorAtual = contadorAtual - 1;

                                        DatabaseReference excluirPostagemRef = firebaseRef
                                                .child("postagensUsuario").child(idUsuarioLogado)
                                                .child(usuarioFotosPostadas.getIdPostagem());

                                        //ToastCustomizado.toastCustomizadoCurto("IdPostagem " + usuarioFotosPostadas.getIdPostagem(),context);
                                        //ToastCustomizado.toastCustomizadoCurto("contador " + contadorAtual, context);

                                        //Removendo postagem do usuário pelo id da postagem.
                                        excluirPostagemRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    ToastCustomizado.toastCustomizadoCurto("Removido com sucesso", context);

                                                    //Removendo o contador
                                                    removerContadorRef.setValue(contadorAtual).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                if(contadorAtual <= 0){
                                                                    //removendo o contador seja <= a 0
                                                                    postagensUsuarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                try{
                                                                                    //Ajustar a posição
                                                                                    DatabaseReference refreshRef = firebaseRef
                                                                                            .child("usuarios").child(idUsuarioLogado)
                                                                                            .child("sinalizarRefresh");
                                                                                    refreshRef.setValue("atualizar").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                progressDialog.dismiss();
                                                                                                if(usuarioFotos.getContadorFotos() == 1){
                                                                                                    ((Activity)view.getContext()).finish();
                                                                                                }else{
                                                                                                    int qntFotos = usuarioFotos.getContadorFotos();
                                                                                                    if(position == qntFotos - 1){
                                                                                                        listaFotosPostadas.remove(position);
                                                                                                        notifyItemRemoved(position);
                                                                                                    }else{
                                                                                                        listaFotosPostadas.remove(position);
                                                                                                        notifyDataSetChanged();
                                                                                                    }
                                                                                                    FotosPostadasActivity fotosPostadasActivity = new FotosPostadasActivity();
                                                                                                    if(position == qntFotos - 1){
                                                                                                        fotosPostadasActivity.reterPosicao(context,qntFotos, position, "ultimo");
                                                                                                    }else{
                                                                                                        fotosPostadasActivity.reterPosicao(context,qntFotos, position, "nãoUltimo");
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }catch (Exception ex){
                                                                                    ex.printStackTrace();
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                }else{
                                                                    try{
                                                                        //Ajustar a posição
                                                                        progressDialog.dismiss();
                                                                        if(usuarioFotos.getContadorFotos() == 1){
                                                                            ((Activity)view.getContext()).finish();
                                                                        }else{
                                                                            int fotosTotal = usuarioFotos.getContadorFotos();
                                                                            if(position == fotosTotal - 1){
                                                                                listaFotosPostadas.remove(position);
                                                                                notifyItemRemoved(position);
                                                                            }else{
                                                                                listaFotosPostadas.remove(position);
                                                                                notifyDataSetChanged();
                                                                            }
                                                                            FotosPostadasActivity fotosPostadasActivity = new FotosPostadasActivity();
                                                                            if(position == fotosTotal - 1){
                                                                                fotosPostadasActivity.reterPosicao(context,fotosTotal, position, "ultimo");
                                                                            }else{
                                                                                fotosPostadasActivity.reterPosicao(context,fotosTotal, position, "nãoUltimo");
                                                                            }
                                                                            ToastCustomizado.toastCustomizadoCurto("Posição adapter " + position,context);
                                                                        }
                                                                    }catch (Exception ex){
                                                                        ex.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    ToastCustomizado.toastCustomizadoCurto("Erro ao remover", context);
                                                }
                                            }
                                        });

                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }
                                }else{
                                    ToastCustomizado.toastCustomizadoCurto("Erro ao excluir, tente novamente",context);
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                //progressDialog.dismiss();


                //String mensagem = usuarioFotos.getListaTituloFotoPostada().get(listaOrdem.get(position));
                //ToastCustomizado.toastCustomizadoCurto("Clicado excluir",context);
            }
        });

        holder.buttonEditarFotoPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{

                    //ToastCustomizado.toastCustomizadoCurto("Posição " + listaOrdem.get(position),context);
                    //notifyItemRemoved(position);
                    //notifyItemChanged(position);
                    Intent intent = new Intent(context.getApplicationContext(), EdicaoFotoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", usuarioFotosPostadas.getTituloPostagem());
                    intent.putExtra("descricao", usuarioFotosPostadas.getDescricaoPostagem());
                    intent.putExtra("foto", usuarioFotosPostadas.getCaminhoPostagem());
                    context.startActivity(intent);
                    ((Activity)view.getContext()).finish();
                    //notifyDataSetChanged();
                    //ToastCustomizado.toastCustomizadoCurto("Posição atual " + listaOrdem.get(position),context);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                /*
                        Intent intent = new Intent(context.getApplicationContext(), EdicaoFotoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("titulo", usuarioFotos.getListaTituloFotoPostada().get(listaOrdem.get(position)));
                        intent.putExtra("descricao", usuarioFotos.getListaDescricaoFotoPostada().get(listaOrdem.get(position)));
                        intent.putExtra("foto", usuarioFotos.getListaFotosUsuario().get(listaOrdem.get(position)));
                        context.startActivity(intent);
                        ((Activity)view.getContext()).finish();
                 */
            }
        });

        holder.imgButtonDetalhesPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", usuarioFotosPostadas.getTituloPostagem());
                    intent.putExtra("descricao", usuarioFotosPostadas.getDescricaoPostagem());
                    intent.putExtra("foto", usuarioFotosPostadas.getCaminhoPostagem());
                    intent.putExtra("idPostagem", usuarioFotosPostadas.getIdPostagem());
                    intent.putExtra("dataPostagem", usuarioFotosPostadas.getDataPostagem());
                    context.startActivity(intent);
                    ((Activity)view.getContext()).finish();
                }catch (Exception ex){
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
        private TextView textAdDataPostada, textViewTituloFoto, textViewDescricaoFoto;
        private PhotoView imageAdFotoPostada;
        private Button buttonEditarFotoPostagem, buttonExcluirFotoPostagem;
        private ImageButton imgButtonDetalhesPostagem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textAdDataPostada = itemView.findViewById(R.id.textAdDataPostada);
            imageAdFotoPostada = itemView.findViewById(R.id.imageAdFotoPostada);
            textViewTituloFoto = itemView.findViewById(R.id.textViewTituloFoto);
            textViewDescricaoFoto = itemView.findViewById(R.id.textViewDescricaoFoto);
            buttonEditarFotoPostagem = itemView.findViewById(R.id.buttonEditarFotoPostagem);
            buttonExcluirFotoPostagem = itemView.findViewById(R.id.buttonExcluirFotoPostagem);
            imgButtonDetalhesPostagem = itemView.findViewById(R.id.imgButtonDetalhesPostagem);
        }
    }
}