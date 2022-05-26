package com.example.ogima.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.activity.TodasFotosUsuarioActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterPostagensInicio extends RecyclerView.Adapter<AdapterPostagensInicio.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private DatabaseReference meusDadosRef, dadosSelecionadoRef;
    private Postagem postagem;
    private Usuario usuarioAtual;
    private Usuario usuarioSelecionado;
    private List<Postagem> listaFotosPostagens;
    private List<Usuario> listaUsuarioFotosPostagens;
    private Context context;
    private String idUsuarioSelecionado;


    public AdapterPostagensInicio(List<Postagem> listFotosPostagens, Context c, List<Usuario> listUsuarioFotosPostagens) {
        this.context = c;
        this.listaFotosPostagens = listFotosPostagens;
        this.listaUsuarioFotosPostagens = listUsuarioFotosPostagens;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_postagens_inicio, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        try {
            Postagem postagemSelecionada = listaFotosPostagens.get(position);
            final Usuario[] usuarioSelecionado = {listaUsuarioFotosPostagens.get(position)};

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

                            if (usuarioSelecionado[0].getMinhaFoto() != null) {
                                GlideCustomizado.montarGlide(context, usuarioSelecionado[0].getMinhaFoto(),
                                        holder.imgViewDonoFotoPostagemInicio, android.R.color.transparent);
                            }
                            if (usuarioSelecionado[0].getMeuFundo() != null) {
                                GlideCustomizado.fundoGlide(context, usuarioSelecionado[0].getMeuFundo(),
                                        holder.imgViewFundoUserInicio, android.R.color.transparent);
                            }
                        }

                        if (usuarioSelecionado[0].getExibirApelido().equals("sim")) {
                            holder.txtViewNomeDonoPostagemInicio.setText(usuarioSelecionado[0].getApelidoUsuario());
                        } else {
                            holder.txtViewNomeDonoPostagemInicio.setText(usuarioSelecionado[0].getNomeUsuario());
                        }

                        //Exibição do título da postagem
                        if (postagemSelecionada.getTituloPostagem() != null && !postagemSelecionada.getTituloPostagem().equals("")) {
                            holder.txtViewTituloFotoPostadaInicio.setVisibility(View.VISIBLE);
                            holder.txtViewTituloFotoPostadaInicio.setText(postagemSelecionada.getTituloPostagem());
                        }else{
                            holder.txtViewTituloFotoPostadaInicio.setVisibility(View.GONE);
                        }

                        //Exibição da descrição da postagem
                        if (postagemSelecionada.getDescricaoPostagem() != null && !postagemSelecionada.getDescricaoPostagem().equals("")) {
                            holder.txtViewDescricaoFotoPostagemInicio.setVisibility(View.VISIBLE);
                            holder.txtViewDescricaoFotoPostagemInicio.setText(postagemSelecionada.getDescricaoPostagem());
                        }else{
                            holder.txtViewDescricaoFotoPostagemInicio.setVisibility(View.GONE);
                        }

                        //Exibindo o total de curtidas da postagem
                        if (postagemSelecionada.getTotalCurtidasPostagem() > 0) {
                            holder.txtViewContadorLikesFotoPostagemInicio.setText(""+postagemSelecionada.getTotalCurtidasPostagem());
                        } else {
                            holder.txtViewContadorLikesFotoPostagemInicio.setText("0");
                        }

                        //Exibindo total de comentários da postagem
                        if (postagemSelecionada.getTotalComentarios() > 0) {
                            holder.txtViewContadorComentarioFotoPostagemInicio.setText(""+postagemSelecionada.getTotalComentarios());
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
                    if(snapshot.getValue() != null){
                        usuarioSelecionado[0] = snapshot.getValue(Usuario.class);
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
                    intent.putExtra("usuarioSelecionado", usuarioSelecionado[0]);
                    context.startActivity(intent);
                }
            });

            holder.imgViewDonoFotoPostagemInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("usuarioSelecionado", usuarioSelecionado[0]);
                    context.startActivity(intent);
                }
            });

            holder.btnVisitarPerfilFotoPostagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), PersonProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("usuarioSelecionado", usuarioSelecionado[0]);
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
