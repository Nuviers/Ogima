package com.example.ogima.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.HeaderComunidade;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class HeaderAdapterPostagemComunidade extends RecyclerView.Adapter<HeaderAdapterPostagemComunidade.HeaderViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private HeaderComunidade headerComunidade;


    public HeaderAdapterPostagemComunidade(Context c, HeaderComunidade header) {
        this.context = c;
        this.emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        this.idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        this.headerComunidade = header;
    }

    // Implemente os métodos necessários para o adapter do cabeçalho
    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_header_postagem_comunidade, parent, false);
        return new HeaderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {

        //falta a lógica do botão para entrar na comunidade.

        FirebaseRecuperarUsuario.recuperaUsuario(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (epilepsia) {
                    GlideCustomizado.montarGlideEpilepsia(context, headerComunidade.getUrlImagem(),
                            holder.imgViewIncFotoUser, android.R.color.transparent);

                    GlideCustomizado.montarGlideFotoEpilepsia(context,
                            headerComunidade.getUrlFundo(), holder.imgViewIncFundoUser, android.R.color.transparent);
                } else {
                    GlideCustomizado.montarGlide(context, headerComunidade.getUrlImagem(),
                            holder.imgViewIncFotoUser, android.R.color.transparent);

                    GlideCustomizado.montarGlideFoto(context,
                            headerComunidade.getUrlFundo(), holder.imgViewIncFundoUser, android.R.color.transparent);
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });

        holder.txtViewIncNomeUser.setText(headerComunidade.getNome());

        if (headerComunidade.getNrParticipantes() != -1) {
            holder.txtViewNrParticipantes.setText("" + headerComunidade.getNrParticipantes());
        } else {
            holder.txtViewNrParticipantes.setText("0");
        }

        if (headerComunidade.getTopicos() != null && headerComunidade.getTopicos().size() > 0) {
            holder.linearLayoutTopicos.setVisibility(View.VISIBLE);
            configChipsTopicos(holder.linearLayoutTopicos);
        } else {
            holder.linearLayoutTopicos.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 1; // Apenas um item para o cabeçalho
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutTopicos;
        private ImageView imgViewIncFotoUser, imgViewIncFundoUser;
        private View viewIncBackOpcoes;
        private TextView txtViewIncNomeUser;
        private ImageButton imgBtnParticipantes;
        private TextView txtViewNrParticipantes;
        private Button btnViewEntrarComunidade;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Realize a vinculação dos elementos do layout do cabeçalho, se necessário
            linearLayoutTopicos = itemView.findViewById(R.id.linearLayoutTopicosHeaderComunidadePostagem);
            imgViewIncFotoUser = itemView.findViewById(R.id.imgViewIncFotoUser);
            imgViewIncFundoUser = itemView.findViewById(R.id.imgViewIncFundoUser);
            viewIncBackOpcoes = itemView.findViewById(R.id.viewIncBackOpcoes);
            txtViewIncNomeUser = itemView.findViewById(R.id.txtViewIncNomeUser);
            imgBtnParticipantes = itemView.findViewById(R.id.imgBtnParticipantesComunidade);
            txtViewNrParticipantes = itemView.findViewById(R.id.txtViewNrParticipantesComunidade);
            btnViewEntrarComunidade = itemView.findViewById(R.id.btnViewEntrarComunidade);
        }
    }

    private void configChipsTopicos(LinearLayout linearLayoutTopicos) {
        linearLayoutTopicos.removeAllViews();

        for (String topico : headerComunidade.getTopicos()) {
            Chip chip = new Chip(linearLayoutTopicos.getContext());
            chip.setText(topico);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.DKGRAY));
            chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
            chip.setLayoutParams(params);
            chip.setClickable(false);
            linearLayoutTopicos.addView(chip);
        }
    }
}
