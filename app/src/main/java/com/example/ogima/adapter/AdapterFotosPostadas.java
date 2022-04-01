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
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
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
import java.util.List;

public class AdapterFotosPostadas extends RecyclerView.Adapter<AdapterFotosPostadas.ViewHolder> {

    private List<Usuario> listaFotosPostadas;
    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private StorageReference storage;
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    ArrayList<Integer> listaOrdem = new ArrayList<>();
    ArrayList<String> listaTitulo = new ArrayList<>();
    ArrayList<String> listaDescricao = new ArrayList<>();
    ArrayList<String> listaFotos = new ArrayList<>();
    ArrayList<String> listaData = new ArrayList<>();
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

        Usuario usuarioFotosPostadas = listaFotosPostadas.get(position);

        DatabaseReference fotosUsuarioRef = firebaseRef.child("fotosUsuario")
                .child(idUsuarioLogado);

        fotosUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    usuarioFotos = snapshot.getValue(Usuario.class);

                    //Ordem
                    listaOrdem = usuarioFotos.getListaOrdenacaoFotoPostada();
                    Comparator<Integer> comparatorOrdem = Collections.reverseOrder();
                    Collections.sort(listaOrdem, comparatorOrdem);

                    //Título
                    listaTitulo = usuarioFotos.getListaTituloFotoPostada();

                    //Foto
                    listaFotos = usuarioFotos.getListaFotosUsuario();

                    //Descrição
                    listaDescricao = usuarioFotos.getListaDescricaoFotoPostada();

                    //Data
                    listaData = usuarioFotos.getListaDatasFotos();

                    //Contador
                    contadorAtual = usuarioFotos.getContadorFotos();

                    //Configurações para ordenação
                    try {
                        if (usuarioFotos.getContadorFotos() > 0) {
                            Uri uri = Uri.parse(String.valueOf(listaFotos.get(listaOrdem.get(position))));
                            Glide.with(context).load(uri).centerCrop()
                                    .into(holder.imageAdFotoPostada);
                            holder.textAdDataPostada.setText(usuarioFotos.getListaDatasFotos().get(listaOrdem.get(position)));
                            holder.textViewTituloFoto.setText(usuarioFotos.getListaTituloFotoPostada().get(listaOrdem.get(position)));
                            holder.textViewDescricaoFoto.setText(usuarioFotos.getListaDescricaoFotoPostada().get(listaOrdem.get(position)));
                        } else {
                            holder.imageAdFotoPostada.setImageResource(R.drawable.avatarfemale);
                            holder.textAdDataPostada.setText("Sem postagens");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                fotosUsuarioRef.removeEventListener(this);
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

                //Remove o título
                DatabaseReference removerTituloRef = fotosUsuarioRef
                        .child("listaTituloFotoPostada");

                //Remove a descrição
                DatabaseReference removerDescricaoRef = fotosUsuarioRef
                        .child("listaDescricaoFotoPostada");

                //Remove a data
                DatabaseReference removerDataRef = fotosUsuarioRef
                        .child("listaDatasFotos");

                //Remove a foto
                DatabaseReference removerFotoRef = fotosUsuarioRef
                        .child("listaFotosUsuario");

                //Remove o índice na ordenação
                DatabaseReference removerOrdemRef = fotosUsuarioRef
                        .child("listaOrdenacaoFotoPostada");

                //Remove o contador
                DatabaseReference removerContadorRef = fotosUsuarioRef
                        .child("contadorFotos");



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
                        ArrayList<Integer> listaOrdemNova = new ArrayList<>();
                        listaOrdemNova = usuarioFotos.getListaOrdenacaoFotoPostada();
                        Comparator<Integer> comparatorOrdemNew = Collections.reverseOrder();
                        Collections.sort(listaOrdemNova, comparatorOrdemNew);
                        //ToastCustomizado.toastCustomizadoCurto("Posição " + posicaoFoto,context);
                        StorageReference imagemRef = storage
                                .getStorage().getReferenceFromUrl(usuarioFotos.getListaFotosUsuario().get(listaOrdemNova.get(position)));
                        imagemRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete()){
                                    //ToastCustomizado.toastCustomizadoCurto("Foto do storage excluida",context);
                                    try{
                                        listaTitulo.remove(usuarioFotos.getListaTituloFotoPostada().get(listaOrdem.get(position)));
                                        listaDescricao.remove(usuarioFotos.getListaDescricaoFotoPostada().get(listaOrdem.get(position)));
                                        listaData.remove(usuarioFotos.getListaDatasFotos().get(listaOrdem.get(position)));
                                        listaFotos.remove(usuarioFotos.getListaFotosUsuario().get(listaOrdem.get(position)));
                                        listaOrdem.removeAll(listaOrdem);
                                        contadorAtual = contadorAtual - 1;
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }

                                    //Populando o array de ordenação com dados atualizados.
                                    for(int i = 0; i < contadorAtual; i ++) {
                                        listaOrdem.add(i);
                                        if (i == contadorAtual) {
                                            break;
                                        }
                                    }

                                    //Removendo o título
                                    removerTituloRef.setValue(listaTitulo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //ToastCustomizado.toastCustomizadoCurto("Titulo excluido", context);
                                                //Removendo a descrição - OK
                                                removerDescricaoRef.setValue(listaDescricao).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //ToastCustomizado.toastCustomizadoCurto("Descrição excluida", context);
                                                            //Removendo a data - OK
                                                            removerDataRef.setValue(listaData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        //ToastCustomizado.toastCustomizadoCurto("Data excluida",context);
                                                                        //Removendo a foto
                                                                        removerFotoRef.setValue(listaFotos).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    //ToastCustomizado.toastCustomizadoCurto("Foto excluida",context);
                                                                                    //Removendo a ordem
                                                                                    removerOrdemRef.setValue(listaOrdem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                //ToastCustomizado.toastCustomizadoCurto("Ordem excluida",context);
                                                                                                //Removendo o contador
                                                                                                removerContadorRef.setValue(contadorAtual).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){
                                                                                                            //ToastCustomizado.toastCustomizadoCurto("Contador excluido",context);
                                                                                                            if(contadorAtual <= 0){
                                                                                                                //ToastCustomizado.toastCustomizadoCurto("É zero",context);
                                                                                                                fotosUsuarioRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                                                                                                                                //notifyItemRemoved(position);
                                                                                                                                                FotosPostadasActivity fotosPostadasActivity = new FotosPostadasActivity();
                                                                                                                                                //TAVA AQUI O NOTIFY
                                                                                                                                                if(position == qntFotos - 1){
                                                                                                                                                    fotosPostadasActivity.reterPosicao(listaFotos, context,qntFotos, position, "ultimo");
                                                                                                                                                    //ToastCustomizado.toastCustomizadoCurto("último item ", context);
                                                                                                                                                }else{
                                                                                                                                                    fotosPostadasActivity.reterPosicao(listaFotos, context,qntFotos, position, "nãoUltimo");
                                                                                                                                                    //ToastCustomizado.toastCustomizadoCurto("não é último item ", context);
                                                                                                                                                }
                                                                                                                                                //ToastCustomizado.toastCustomizadoCurto("Posição adapter " + position,context);

                                                                                                                                                //fotosPostadasActivity.reterPosicao(listaFotos, context,qntFotos, position);
                                                                                                                                                //notifyItemChanged(position);

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
                                                                                                                        //notifyItemRemoved(position);
                                                                                                                        FotosPostadasActivity fotosPostadasActivity = new FotosPostadasActivity();
                                                                                                                        //TAVA AQUI O NOTIFYSDATASET
                                                                                                                        //Envia o contador anterior
                                                                                                                        if(position == fotosTotal - 1){
                                                                                                                            fotosPostadasActivity.reterPosicao(listaFotos, context,fotosTotal, position, "ultimo");
                                                                                                                            //ToastCustomizado.toastCustomizadoCurto("último item ", context);
                                                                                                                        }else{
                                                                                                                            fotosPostadasActivity.reterPosicao(listaFotos, context,fotosTotal, position, "nãoUltimo");
                                                                                                                            //ToastCustomizado.toastCustomizadoCurto("não é último item ", context);
                                                                                                                        }
                                                                                                                        ToastCustomizado.toastCustomizadoCurto("Posição adapter " + position,context);
                                                                                                                        //fotosPostadasActivity.reterPosicao(listaFotos,context,fotosTotal,position);
                                                                                                                        //notifyItemChanged(position);
                                                                                                                    }
                                                                                                                }catch (Exception ex){
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
                    listaOrdem = usuarioFotos.getListaOrdenacaoFotoPostada();
                    Comparator<Integer> comparatorOrdem = Collections.reverseOrder();
                    Collections.sort(listaOrdem, comparatorOrdem);
                    //ToastCustomizado.toastCustomizadoCurto("Posição " + listaOrdem.get(position),context);
                    //notifyItemRemoved(position);
                    //notifyItemChanged(position);
                    Intent intent = new Intent(context.getApplicationContext(), EdicaoFotoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", usuarioFotos.getListaTituloFotoPostada().get(listaOrdem.get(position)));
                    intent.putExtra("descricao", usuarioFotos.getListaDescricaoFotoPostada().get(listaOrdem.get(position)));
                    intent.putExtra("foto", usuarioFotos.getListaFotosUsuario().get(listaOrdem.get(position)));
                    intent.putExtra("posicao", listaOrdem.get(position));
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

    }

    @Override
    public int getItemCount() {
        //Retorna o tamanho da lista
        return listaFotosPostadas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //Inicializa os componentes do layout
        private TextView textAdDataPostada, textViewTituloFoto, textViewDescricaoFoto;
        private ImageView imageAdFotoPostada;
        private Button buttonEditarFotoPostagem, buttonExcluirFotoPostagem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textAdDataPostada = itemView.findViewById(R.id.textAdDataPostada);
            imageAdFotoPostada = itemView.findViewById(R.id.imageAdFotoPostada);
            textViewTituloFoto = itemView.findViewById(R.id.textViewTituloFoto);
            textViewDescricaoFoto = itemView.findViewById(R.id.textViewDescricaoFoto);
            buttonEditarFotoPostagem = itemView.findViewById(R.id.buttonEditarFotoPostagem);
            buttonExcluirFotoPostagem = itemView.findViewById(R.id.buttonExcluirFotoPostagem);
        }
    }
}