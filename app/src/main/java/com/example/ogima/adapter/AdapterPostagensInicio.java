package com.example.ogima.adapter;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.fragment.InicioFragment;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdapterPostagensInicio extends RecyclerView.Adapter<AdapterPostagensInicio.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private DatabaseReference meusDadosRef, dadosSelecionadoRef;
    private Usuario usuarioAtual, usuarioEnviado;
    private List<Postagem> listaFotosPostagens;
    private List<Usuario> listaUsuarioFotosPostagens;
    private Context context;

    private int contadorCurtidasPostagem, contadorCurtidaV2;
    private DatabaseReference verificaCurtidaRef,
            adicionarCurtidaRef, caminhoCurtidaRef,
            verificaContadorCurtidaRef,verificaContadorCurtidaV3Ref;
    private String localUsuario, curtidaDiminuida;
    private Locale localAtual;
    private DateFormat dateFormat;
    private Date date;
    private Postagem postagemV2, postagemV3;

    public AdapterPostagensInicio(List<Postagem> listFotosPostagens, Context c, List<Usuario> listUsuarioFotosPostagens) {
        this.context = c;
        this.listaFotosPostagens = listFotosPostagens;
        this.listaUsuarioFotosPostagens = listUsuarioFotosPostagens;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        localAtual = context.getResources().getConfiguration().locale;
        localUsuario = localUsuario.valueOf(localAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_postagens_inicio, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        try {
            //Collections.sort(listaFotosPostagens, Postagem.PostagemDataEF);
            //Collections.sort(listaUsuarioFotosPostagens, Collections.reverseOrder());

            Postagem postagemSelecionada = listaFotosPostagens.get(position);
            Usuario usuarioSelecionado = listaUsuarioFotosPostagens.get(position);



            //Verifica se usuário atual já curtiu essa postagem.
            verificaCurtidaRef = firebaseRef.child("curtidasPostagem")
                    .child(postagemSelecionada.getIdPostagem()).child(idUsuarioLogado);

            verificaCurtidaRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        holder.imgButtonLikeFotoPostagemInicio.setImageResource(R.drawable.ic_heart_like_comentario_preenchido);
                        holder.imgButtonLikeFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                verificaContadorCurtidaRef = firebaseRef.child("postagensUsuario")
                                        .child(postagemSelecionada.getIdDonoPostagem())
                                        .child(postagemSelecionada.getIdPostagem());

                                verificaContadorCurtidaRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.getValue() != null){
                                            postagemV2 = snapshot.getValue(Postagem.class);

                                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                                            builder.setTitle("Desfazer curtida");
                                            builder.setMessage("Deseja remover sua curtida da postagem ?");
                                            builder.setPositiveButton("Remover curtida", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    DatabaseReference removerCurtidaRef = firebaseRef.child("curtidasPostagem")
                                                            .child(postagemSelecionada.getIdPostagem()).child(idUsuarioLogado);

                                                    removerCurtidaRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                holder.imgButtonLikeFotoPostagemInicio.setClickable(false);
                                                                //Atualizando o contador de curtidas
                                                                ToastCustomizado.toastCustomizado("Curtida antes da remoção " + postagemV2.getTotalCurtidasPostagem(), context);

                                                                if(postagemV2.getTotalCurtidasPostagem() <= 1){
                                                                    contadorCurtidaV2 = 0;
                                                                }else{
                                                                    contadorCurtidaV2 = 0;
                                                                    contadorCurtidaV2 = postagemV2.getTotalCurtidasPostagem() - 1;
                                                                }

                                                                ToastCustomizado.toastCustomizado("Curtida depois da remoção " + contadorCurtidaV2, context);

                                                                DatabaseReference atualizarCurtidaRef = firebaseRef.child("postagensUsuario")
                                                                        .child(postagemSelecionada.getIdDonoPostagem())
                                                                        .child(postagemSelecionada.getIdPostagem())
                                                                        .child("totalCurtidasPostagem");
                                                                atualizarCurtidaRef.setValue(contadorCurtidaV2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            ToastCustomizado.toastCustomizadoCurto("Curtida removida com sucesso",context);
                                                                            holder.imgButtonLikeFotoPostagemInicio.setImageResource(R.drawable.ic_heart_like_comentario);
                                                                            holder.txtViewContadorLikesFotoPostagemInicio.setText("" + contadorCurtidaV2);
                                                                            holder.imgButtonLikeFotoPostagemInicio.setClickable(true);
                                                                        }else{
                                                                            ToastCustomizado.toastCustomizadoCurto("Erro ao remover curtida, tente novamente",context);
                                                                            holder.imgButtonLikeFotoPostagemInicio.setClickable(true);
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                            builder.setNegativeButton("Cancelar", null);
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                        verificaContadorCurtidaRef.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    } else {

                        holder.imgButtonLikeFotoPostagemInicio.setImageResource(R.drawable.ic_heart_like_comentario);
                        holder.imgButtonLikeFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Setando os dados para salvar no caminho da curtidasPostagem
                                HashMap<String, Object> dadosCurtida = new HashMap<>();
                                if (localUsuario.equals("pt_BR")) {
                                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosCurtida.put("dataCurtidaPostagem", novaData);
                                } else {
                                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                                    date = new Date();
                                    String novaData = dateFormat.format(date);
                                    dadosCurtida.put("dataCurtidaPostagem", novaData);
                                }
                                dadosCurtida.put("idPostagem", postagemSelecionada.getIdPostagem());
                                dadosCurtida.put("idUsuarioInterativo", idUsuarioLogado);
                                dadosCurtida.put("idDonoPostagem", postagemSelecionada.getIdDonoPostagem());

                                caminhoCurtidaRef = firebaseRef.child("curtidasPostagem")
                                        .child(postagemSelecionada.getIdPostagem()).child(idUsuarioLogado);

                                caminhoCurtidaRef.setValue(dadosCurtida).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            holder.imgButtonLikeFotoPostagemInicio.setClickable(false);
                                            //Verificando o total de curtidas da postagem

                                            verificaContadorCurtidaV3Ref = firebaseRef.child("postagensUsuario")
                                                    .child(postagemSelecionada.getIdDonoPostagem())
                                                    .child(postagemSelecionada.getIdPostagem());

                                            verificaContadorCurtidaV3Ref.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.getValue() != null){
                                                        postagemV3 = snapshot.getValue(Postagem.class);
                                                        if(postagemV3.getTotalCurtidasPostagem() >= 1){
                                                            ToastCustomizado.toastCustomizado("Contador curtida antes da soma " + postagemV3.getTotalCurtidasPostagem(), context);
                                                            contadorCurtidaV2 = 0;
                                                            contadorCurtidaV2 = postagemV3.getTotalCurtidasPostagem() + 1;
                                                            ToastCustomizado.toastCustomizado("Depois da soma " + contadorCurtidaV2, context);
                                                        }else{
                                                            contadorCurtidaV2 = 1;
                                                            ToastCustomizado.toastCustomizadoCurto("Contador normal",context);
                                                        }
                                                    }else{
                                                        contadorCurtidaV2 = 0;
                                                    }


                                                    //Atualizando o contador de curtidas
                                                    adicionarCurtidaRef = firebaseRef.child("postagensUsuario")
                                                            .child(postagemSelecionada.getIdDonoPostagem())
                                                            .child(postagemSelecionada.getIdPostagem())
                                                            .child("totalCurtidasPostagem");
                                                    adicionarCurtidaRef.setValue(contadorCurtidaV2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                ToastCustomizado.toastCustomizadoCurto("Curtido com sucesso", context);
                                                                holder.imgButtonLikeFotoPostagemInicio.setImageResource(R.drawable.ic_heart_like_comentario_preenchido);
                                                                holder.txtViewContadorLikesFotoPostagemInicio.setText("" + contadorCurtidaV2);
                                                                holder.imgButtonLikeFotoPostagemInicio.setClickable(true);
                                                            }else{
                                                                ToastCustomizado.toastCustomizadoCurto("Erro ao curtir postagem, tente novamente", context);
                                                                holder.imgButtonLikeFotoPostagemInicio.setClickable(true);
                                                            }
                                                        }
                                                    });

                                                    verificaContadorCurtidaV3Ref.removeEventListener(this);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });


                                            /*
                                            if(postagemV3.getTotalCurtidasPostagem() >= 1){
                                                ToastCustomizado.toastCustomizado("Contador curtida antes da soma " + postagemV2.getTotalCurtidasPostagem(), context);
                                                contadorCurtidaV2 = 0;
                                                contadorCurtidaV2 = postagemV3.getTotalCurtidasPostagem() + 1;
                                                ToastCustomizado.toastCustomizado("Depois da soma " + contadorCurtidaV2, context);
                                            }else{
                                                contadorCurtidaV2 = 1;
                                                ToastCustomizado.toastCustomizadoCurto("Contador normal",context);
                                            }
                                             */
                                        }
                                    }
                                });
                            }
                        });
                    }
                    //verificaCurtidaRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            //Referência dos dados atuais
            meusDadosRef = firebaseRef.child("usuarios").child(idUsuarioLogado);
            //Referência dos dados do usuário selecionado.
            dadosSelecionadoRef = firebaseRef.child("usuarios").child(postagemSelecionada.getIdDonoPostagem());

            //Verificando dados do usuário atual.
            meusDadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {

                        usuarioAtual = snapshot.getValue(Usuario.class);

                        if (usuarioAtual.getEpilepsia().equals("Sim")) {

                            GlideCustomizado.fundoGlideEpilepsia(context, postagemSelecionada.getCaminhoPostagem(),
                                    holder.imgViewFotoPostagemInicio, android.R.color.transparent);

                        } else {
                            GlideCustomizado.montarGlideFoto(context, postagemSelecionada.getCaminhoPostagem(),
                                    holder.imgViewFotoPostagemInicio, android.R.color.transparent);

                            if (usuarioSelecionado.getMinhaFoto() != null) {
                                GlideCustomizado.montarGlide(context, usuarioSelecionado.getMinhaFoto(),
                                        holder.imgViewDonoFotoPostagemInicio, android.R.color.transparent);
                            }
                            if (usuarioSelecionado.getMeuFundo() != null) {
                                GlideCustomizado.fundoGlide(context, usuarioSelecionado.getMeuFundo(),
                                        holder.imgViewFundoUserInicio, android.R.color.transparent);
                            }
                        }

                        if (usuarioSelecionado.getExibirApelido().equals("sim")) {
                            holder.txtViewNomeDonoPostagemInicio.setText(usuarioSelecionado.getApelidoUsuario());
                        } else {
                            holder.txtViewNomeDonoPostagemInicio.setText(usuarioSelecionado.getNomeUsuario());
                        }

                        //Exibição do título da postagem
                        if (postagemSelecionada.getTituloPostagem() != null && !postagemSelecionada.getTituloPostagem().equals("")) {
                            holder.txtViewTituloFotoPostadaInicio.setVisibility(View.VISIBLE);
                            holder.txtViewTituloFotoPostadaInicio.setText(postagemSelecionada.getTituloPostagem());
                        } else {
                            holder.txtViewTituloFotoPostadaInicio.setVisibility(View.GONE);
                        }

                        //Exibição da descrição da postagem
                        if (postagemSelecionada.getDescricaoPostagem() != null && !postagemSelecionada.getDescricaoPostagem().equals("")) {
                            holder.txtViewDescricaoFotoPostagemInicio.setVisibility(View.VISIBLE);
                            holder.txtViewDescricaoFotoPostagemInicio.setText(postagemSelecionada.getDescricaoPostagem());
                        } else {
                            holder.txtViewDescricaoFotoPostagemInicio.setVisibility(View.GONE);
                        }

                        //Exibindo o total de curtidas da postagem
                        if (postagemSelecionada.getTotalCurtidasPostagem() > 0) {
                            holder.txtViewContadorLikesFotoPostagemInicio.setText("" + postagemSelecionada.getTotalCurtidasPostagem());
                        } else {
                            holder.txtViewContadorLikesFotoPostagemInicio.setText("0");
                        }

                        //Exibindo total de comentários da postagem
                        if (postagemSelecionada.getTotalComentarios() > 0) {
                            holder.txtViewContadorComentarioFotoPostagemInicio.setText("" + postagemSelecionada.getTotalComentarios());
                        } else {
                            holder.txtViewContadorComentarioFotoPostagemInicio.setText("0");
                        }

                        //Exibindo total de views da postagem
                        if (postagemSelecionada.getTotalViewsFotoPostagem() > 0) {
                            holder.txtViewContadorViewsFotoPostagemInicio
                                    .setText(postagemSelecionada.getTotalViewsFotoPostagem() + " Visualizações");
                        } else {
                            holder.txtViewContadorViewsFotoPostagemInicio
                                    .setText("0 Visualizações");
                        }
                    }
                    meusDadosRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            dadosSelecionadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        usuarioEnviado = snapshot.getValue(Usuario.class);
                    }
                    dadosSelecionadoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.txtViewNomeDonoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("usuarioSelecionado", usuarioSelecionado);
                    context.startActivity(intent);
                }
            });

            holder.imgViewDonoFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("usuarioSelecionado", usuarioSelecionado);
                    context.startActivity(intent);
                }
            });

            holder.btnVisitarPerfilFotoPostagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("usuarioSelecionado", usuarioSelecionado);
                    context.startActivity(intent);
                }
            });

            //Eventos de botões para ir na postagem
            holder.imgViewFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", postagemSelecionada.getTituloPostagem());
                    intent.putExtra("descricao", postagemSelecionada.getDescricaoPostagem());
                    intent.putExtra("foto", postagemSelecionada.getCaminhoPostagem());
                    intent.putExtra("idPostagem", postagemSelecionada.getIdPostagem());
                    intent.putExtra("dataPostagem", postagemSelecionada.getDataPostagem());
                    intent.putExtra("donoPostagem", postagemSelecionada.getIdDonoPostagem());
                    intent.putExtra("publicoPostagem", postagemSelecionada.getPublicoPostagem());
                    intent.putExtra("idRecebido", postagemSelecionada.getIdDonoPostagem());
                    context.startActivity(intent);
                }
            });

            holder.txtViewContadorViewsFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", postagemSelecionada.getTituloPostagem());
                    intent.putExtra("descricao", postagemSelecionada.getDescricaoPostagem());
                    intent.putExtra("foto", postagemSelecionada.getCaminhoPostagem());
                    intent.putExtra("idPostagem", postagemSelecionada.getIdPostagem());
                    intent.putExtra("dataPostagem", postagemSelecionada.getDataPostagem());
                    intent.putExtra("donoPostagem", postagemSelecionada.getIdDonoPostagem());
                    intent.putExtra("publicoPostagem", postagemSelecionada.getPublicoPostagem());
                    intent.putExtra("idRecebido", postagemSelecionada.getIdDonoPostagem());
                    context.startActivity(intent);
                }
            });

            holder.imgButtonComentariosFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", postagemSelecionada.getTituloPostagem());
                    intent.putExtra("descricao", postagemSelecionada.getDescricaoPostagem());
                    intent.putExtra("foto", postagemSelecionada.getCaminhoPostagem());
                    intent.putExtra("idPostagem", postagemSelecionada.getIdPostagem());
                    intent.putExtra("dataPostagem", postagemSelecionada.getDataPostagem());
                    intent.putExtra("donoPostagem", postagemSelecionada.getIdDonoPostagem());
                    intent.putExtra("publicoPostagem", postagemSelecionada.getPublicoPostagem());
                    intent.putExtra("idRecebido", postagemSelecionada.getIdDonoPostagem());
                    context.startActivity(intent);
                }
            });

            holder.txtViewContadorComentarioFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", postagemSelecionada.getTituloPostagem());
                    intent.putExtra("descricao", postagemSelecionada.getDescricaoPostagem());
                    intent.putExtra("foto", postagemSelecionada.getCaminhoPostagem());
                    intent.putExtra("idPostagem", postagemSelecionada.getIdPostagem());
                    intent.putExtra("dataPostagem", postagemSelecionada.getDataPostagem());
                    intent.putExtra("donoPostagem", postagemSelecionada.getIdDonoPostagem());
                    intent.putExtra("publicoPostagem", postagemSelecionada.getPublicoPostagem());
                    intent.putExtra("idRecebido", postagemSelecionada.getIdDonoPostagem());
                    context.startActivity(intent);
                }
            });

            holder.txtViewContadorLikesFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), TodasFotosUsuarioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("titulo", postagemSelecionada.getTituloPostagem());
                    intent.putExtra("descricao", postagemSelecionada.getDescricaoPostagem());
                    intent.putExtra("foto", postagemSelecionada.getCaminhoPostagem());
                    intent.putExtra("idPostagem", postagemSelecionada.getIdPostagem());
                    intent.putExtra("dataPostagem", postagemSelecionada.getDataPostagem());
                    intent.putExtra("donoPostagem", postagemSelecionada.getIdDonoPostagem());
                    intent.putExtra("publicoPostagem", postagemSelecionada.getPublicoPostagem());
                    intent.putExtra("idRecebido", postagemSelecionada.getIdDonoPostagem());
                    context.startActivity(intent);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            //ToastCustomizado.toastCustomizadoCurto("Erro " + ex.getMessage(),context);
        }

    }

    @Override
    public int getItemCount() {
        return listaFotosPostagens.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPostagemInicio, imgViewDonoFotoPostagemInicio,
                imgViewFundoUserInicio;
        private TextView txtViewNomeDonoPostagemInicio, txtViewTituloFotoPostadaInicio,
                txtViewDescricaoFotoPostagemInicio, txtViewContadorLikesFotoPostagemInicio,
                txtViewContadorComentarioFotoPostagemInicio, txtViewContadorViewsFotoPostagemInicio;
        private ImageButton imgButtonLikeFotoPostagemInicio, imgButtonComentariosFotoPostagemInicio,
                imgButtonViewsFotoPostagemInicio;
        private Button btnVisitarPerfilFotoPostagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //Foto da postagem;
            imgViewFotoPostagemInicio = itemView.findViewById(R.id.imgViewFotoPostagemInicio);
            //Foto do dono da fotoPostagem;
            imgViewDonoFotoPostagemInicio = itemView.findViewById(R.id.imgViewDonoFotoPostagemInicio);
            //Fundo do dono da fotoPostagem
            imgViewFundoUserInicio = itemView.findViewById(R.id.imgViewFundoUserInicio);
            //Nome do dono da fotoPostagem;
            txtViewNomeDonoPostagemInicio = itemView.findViewById(R.id.txtViewNomeDonoPostagemInicio);
            txtViewTituloFotoPostadaInicio = itemView.findViewById(R.id.txtViewTituloFotoPostadaInicio);
            txtViewDescricaoFotoPostagemInicio = itemView.findViewById(R.id.txtViewDescricaoFotoPostagemInicio);
            imgButtonLikeFotoPostagemInicio = itemView.findViewById(R.id.imgButtonLikeFotoPostagemInicio);
            txtViewContadorLikesFotoPostagemInicio = itemView.findViewById(R.id.txtViewContadorLikesFotoPostagemInicio);
            txtViewContadorComentarioFotoPostagemInicio = itemView.findViewById(R.id.txtViewContadorComentarioFotoPostagemInicio);
            txtViewContadorViewsFotoPostagemInicio = itemView.findViewById(R.id.txtViewContadorViewsFotoPostagemInicio);
            //Button para visitar perfil do usuário selecionado
            btnVisitarPerfilFotoPostagem = itemView.findViewById(R.id.btnVisitarPerfilFotoPostagem);
            //Buttons para ver as postagens
            imgButtonComentariosFotoPostagemInicio = itemView.findViewById(R.id.imgButtonComentariosFotoPostagemInicio);
        }
    }
}