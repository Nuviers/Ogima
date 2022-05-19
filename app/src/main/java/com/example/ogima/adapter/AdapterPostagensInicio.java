package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
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

public class AdapterPostagensInicio extends RecyclerView.Adapter<AdapterPostagensInicio.MyViewHolder>{

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private DatabaseReference meusDadosRef, dadosUsuarioSeguindoRef;
    private Postagem postagem;
    private Usuario usuarioAtual, usuarioSeguindo;
    private List<Postagem> listaFotosPostagens;
    private Context context;

    public AdapterPostagensInicio(List<Postagem> listFotosPostagens, Context c) {
        this.context = c;
        this.listaFotosPostagens = listFotosPostagens;
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

        Postagem postagemSelecionada = listaFotosPostagens.get(position);

        //Referência dos dados atuais
        meusDadosRef = firebaseRef.child("usuarios").child(idUsuarioLogado);
        dadosUsuarioSeguindoRef = firebaseRef.child("usuarios").child(postagemSelecionada.getIdDonoPostagem());

        dadosUsuarioSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    usuarioSeguindo = snapshot.getValue(Usuario.class);
                }
                dadosUsuarioSeguindoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Verificando dados do usuário atual.
        meusDadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    try{
                        usuarioAtual = snapshot.getValue(Usuario.class);

                        if(usuarioAtual.getEpilepsia().equals("Sim")){

                            GlideCustomizado.fundoGlideEpilepsia(context, postagemSelecionada.getCaminhoPostagem(),
                                    holder.imgViewFotoPostagemInicio, android.R.color.transparent);

                            if(usuarioSeguindo.getMinhaFoto() != null){
                                GlideCustomizado.montarGlideEpilepsia(context, usuarioSeguindo.getMinhaFoto(),
                                        holder.imgViewDonoFotoPostagemInicio, android.R.color.transparent);
                            }
                            if(usuarioSeguindo.getMeuFundo() != null){
                                GlideCustomizado.fundoGlideEpilepsia(context, usuarioSeguindo.getMeuFundo(),
                                        holder.imgViewFundoUserInicio, android.R.color.transparent);
                            }
                        }else{
                            GlideCustomizado.montarGlideFoto(context, postagemSelecionada.getCaminhoPostagem(),
                                    holder.imgViewFotoPostagemInicio, android.R.color.transparent);

                            if(usuarioSeguindo.getMinhaFoto() != null){
                                GlideCustomizado.montarGlide(context, usuarioSeguindo.getMinhaFoto(),
                                        holder.imgViewDonoFotoPostagemInicio, android.R.color.transparent);
                            }
                            if(usuarioSeguindo.getMeuFundo() != null){
                                GlideCustomizado.fundoGlide(context, usuarioSeguindo.getMeuFundo(),
                                        holder.imgViewFundoUserInicio, android.R.color.transparent);
                            }
                        }

                        if(usuarioSeguindo.getExibirApelido().equals("sim")){
                            holder.txtViewNomeDonoPostagemInicio.setText(usuarioSeguindo.getApelidoUsuario());
                        }else{
                            holder.txtViewNomeDonoPostagemInicio.setText(usuarioSeguindo.getNomeUsuario());
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                meusDadosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listaFotosPostagens.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPostagemInicio, imgViewDonoFotoPostagemInicio,
                imgViewFundoUserInicio;
        private TextView txtViewNomeDonoPostagemInicio;

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
        }
    }
}
