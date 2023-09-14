package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.DetalhesPostagemActivity;
import com.example.ogima.activity.FotosPostadasActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.FormatarContadorUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterGridPostagem extends RecyclerView.Adapter<AdapterGridPostagem.MyViewHolder> {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private List<Postagem> listaPostagem;
    private Context context;
    private Postagem postagemSelecionada;
    private final int limit = 4;
    private boolean statusEpilepsia = true;
    private boolean isFoto = false;

    public AdapterGridPostagem(List<Postagem> listPostagem, Context c, boolean isFoto) {
        this.listaPostagem = listPostagem;
        this.context = c;
        this.isFoto = isFoto;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);

        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                if (epilepsia != null) {
                    statusEpilepsia = epilepsia;
                } else {
                    statusEpilepsia = true;
                }
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grid_postagem, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        postagemSelecionada = listaPostagem.get(position);

        if (postagemSelecionada.getTipoPostagem().equals("texto")) {
            holder.imgViewGridPostagem.setVisibility(View.GONE);
            holder.txtViewGridPostagem.setVisibility(View.VISIBLE);
            String textoPost = FormatarContadorUtils.abreviarTexto(postagemSelecionada.getDescricaoPostagem(), 170);
            holder.txtViewGridPostagem.setText(textoPost);
        } else {
            holder.txtViewGridPostagem.setVisibility(View.GONE);
            holder.imgViewGridPostagem.setVisibility(View.VISIBLE);
        }

        if (postagemSelecionada.getTipoPostagem().equals("video")) {
            holder.viewDecIndiceVideo.setVisibility(View.VISIBLE);
            holder.imageButtonIndiceVideo.setVisibility(View.VISIBLE);
        } else {
            holder.viewDecIndiceVideo.setVisibility(View.GONE);
            holder.imageButtonIndiceVideo.setVisibility(View.GONE);
        }

        GlideCustomizado.loadUrl(context,
                postagemSelecionada.getUrlPostagem(), holder.imgViewGridPostagem,
                android.R.color.white, GlideCustomizado.CENTER_CROP, false, statusEpilepsia);

        if (position != -1 && listaPostagem != null &&
                listaPostagem.size() > 0 && position == listaPostagem.size() - 1) {
            holder.viewDecEfeitoTranspFoto.setVisibility(View.VISIBLE);
            holder.viewDecMaisFotos.setVisibility(View.VISIBLE);
            holder.imgBtnMaisFotosProfile.setVisibility(View.VISIBLE);
        } else {
            holder.viewDecEfeitoTranspFoto.setVisibility(View.GONE);
            holder.viewDecMaisFotos.setVisibility(View.GONE);
            holder.imgBtnMaisFotosProfile.setVisibility(View.GONE);
        }

        holder.imgBtnMaisFotosProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (postagemSelecionada.getTipoPostagem() == null) {
                    return;
                }

                if (!postagemSelecionada.getIdDonoPostagem()
                        .equals(idUsuarioLogado)) {
                    if (isFoto) {
                        Intent intent = new Intent(context, FotosPostadasActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("idDonoPerfil", postagemSelecionada.getIdDonoPostagem());
                        //*intent.putExtra("irParaProfile", "irParaProfile");
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, DetalhesPostagemActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("idDonoPerfil", postagemSelecionada.getIdDonoPostagem());
                        //*intent.putExtra("irParaProfile", "irParaProfile");
                        context.startActivity(intent);
                    }
                } else {
                    if (isFoto) {
                        Intent intent = new Intent(context, FotosPostadasActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("irParaProfile", "irParaProfile");
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, DetalhesPostagemActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("irParaProfile", "irParaProfile");
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listaPostagem.size() > limit) {
            return limit;
        } else {
            return listaPostagem.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewGridPostagem;
        private ImageButton imageButtonIndiceVideo, imgBtnMaisFotosProfile;
        private View viewDecIndiceVideo, viewDecMaisFotos, viewDecEfeitoTranspFoto;
        private TextView txtViewGridPostagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewGridPostagem = itemView.findViewById(R.id.imgViewGridPostagem);
            txtViewGridPostagem = itemView.findViewById(R.id.txtViewGridPostagem);
            imageButtonIndiceVideo = itemView.findViewById(R.id.imageButtonIndiceVideo);
            viewDecIndiceVideo = itemView.findViewById(R.id.viewDecIndiceVideo);
            imgBtnMaisFotosProfile = itemView.findViewById(R.id.imgBtnMaisFotosProfile);
            viewDecMaisFotos = itemView.findViewById(R.id.viewDecMaisFotos);
            viewDecEfeitoTranspFoto = itemView.findViewById(R.id.viewDecEfeitoTranspFoto);
        }
    }
}
