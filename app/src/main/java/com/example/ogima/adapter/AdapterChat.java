
package com.example.ogima.adapter;

import static com.google.android.material.badge.BadgeUtils.attachBadgeDrawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.ConversaActivity;
import com.example.ogima.activity.ConversaGrupoActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado;
    private String emailUsuarioAtual;
    private List<Usuario> listaChat;
    private List<Mensagem> listaMensagem = new ArrayList<>();
    private Context context;

    private DatabaseReference infosUsuarioAtualRef;
    public DatabaseReference contadorMsgRef;
    private DatabaseReference infosUsuarioContatoRef;
    public DatabaseReference mensagensAdapterChatRef;
    public ValueEventListener listenerMensagensAdapterChat;
    public ValueEventListener listenerContadorMsgRef;
    private HashMap<String, Object> dadosComListenerinfosUsuarioAtualRef;
    private HashMap<String, Object> dadosComListenerinfosUsuarioContatoRef;
    private HashMap<String, Object> dadosComListenerContadorMsgRef;
    private HashMap<String, Object> dadosComListenerMensagensAdapterChat;

    public AdapterChat(List<Usuario> listChat, Context c) {
        this.context = c;
        this.listaChat = listChat;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
        this.dadosComListenerinfosUsuarioAtualRef = new HashMap<>();
        this.dadosComListenerinfosUsuarioContatoRef = new HashMap<>();
        this.dadosComListenerContadorMsgRef = new HashMap<>();
        this.dadosComListenerMensagensAdapterChat = new HashMap<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        //Ordena a lista
        Collections.sort(listaChat, new Comparator<Usuario>() {
            public int compare(Usuario o1, Usuario o2) {
                return o2.getDataMensagemCompleta().compareTo(o1.getDataMensagemCompleta());
            }
        });

        Usuario usuarioContato = listaChat.get(position);
        String idContatoNew = usuarioContato.getIdUsuario();

        infosUsuarioAtualRef = firebaseRef.child("usuarios").child(idUsuarioLogado);

        infosUsuarioContatoRef = firebaseRef.child("usuarios").child(usuarioContato.getIdUsuario());

        contadorMsgRef = firebaseRef.child("contadorMensagens")
                .child(idUsuarioLogado).child(usuarioContato.getIdUsuario());

        mensagensAdapterChatRef = firebaseRef.child("conversas")
                .child(idUsuarioLogado).child(usuarioContato.getIdUsuario());


       Usuario dadosUserInfo = (Usuario) dadosComListenerinfosUsuarioAtualRef.get(idContatoNew);

        if (dadosComListenerinfosUsuarioAtualRef != null &&
                dadosComListenerinfosUsuarioAtualRef.containsKey(usuarioContato.getIdUsuario())) {
        }else{
            infosUsuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        //Verifica epilepsia
                        Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                        atribuirAoHashMapUsuario("infosUsuarioAtualRef", usuarioAtual);
                        if (usuarioAtual.getEpilepsia().equals("Sim")) {
                            GlideCustomizado.montarGlideEpilepsia(context, usuarioContato.getMinhaFoto(),
                                    holder.imgViewFotoPerfilChat, android.R.color.transparent);
                        } else if (usuarioAtual.getEpilepsia().equals("Não")) {
                            GlideCustomizado.montarGlide(context, usuarioContato.getMinhaFoto(),
                                    holder.imgViewFotoPerfilChat, android.R.color.transparent);
                        }
                    }
                    infosUsuarioAtualRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        Usuario dadosUserComContato = (Usuario) dadosComListenerinfosUsuarioContatoRef.get(idContatoNew);

        if (dadosComListenerinfosUsuarioContatoRef != null &&
                dadosComListenerinfosUsuarioContatoRef.containsKey(usuarioContato.getIdUsuario())) {

        }else{
            infosUsuarioContatoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioContato = snapshot.getValue(Usuario.class);
                        atribuirAoHashMapUsuario("infosUsuarioContatoRef", usuarioContato);
                        holder.txtViewNomePerfilChat.setText(usuarioContato.getNomeUsuario());

                        holder.imgViewFotoPerfilChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                abrirConversa(usuarioContato);
                            }
                        });

                        holder.txtViewNomePerfilChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                abrirConversa(usuarioContato);
                            }
                        });

                        holder.txtViewHoraMensagem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                abrirConversa(usuarioContato);
                            }
                        });

                        holder.btnNumeroMensagem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                abrirConversa(usuarioContato);
                            }
                        });

                        holder.txtViewLastMensagemChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                abrirConversa(usuarioContato);
                            }
                        });
                    }
                    infosUsuarioContatoRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

            listenerContadorMsgRef = contadorMsgRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Contatos contatos = snapshot.getValue(Contatos.class);
                        //ToastCustomizado.toastCustomizadoCurto("TESTE",context);
                        dadosComListenerContadorMsgRef.put(usuarioContato.getIdUsuario(), contatos);
                        holder.btnNumeroMensagem.setText("" + contatos.getTotalMensagens());
                        if (contatos != null && contatos.getMensagensPerdidas() != -1
                                && contatos.getMensagensPerdidas() > 0) {
                            holder.viewEspacoBadge.setVisibility(View.VISIBLE);
                            Badge badge = new QBadgeView(context).bindTarget(holder.viewEspacoBadge);
                            badge.setBadgeGravity(Gravity.CENTER);
                            badge.setBadgeTextSize(20,true);
                            badge.setBadgeBackgroundColor(Color.parseColor("#973e95"));
                            badge.setGravityOffset(10, 0, true);
                            badge.setBadgeNumber(contatos.getMensagensPerdidas());
                        }else{
                            holder.viewEspacoBadge.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        if (dadosComListenerMensagensAdapterChat != null &&
                dadosComListenerMensagensAdapterChat.containsKey(usuarioContato.getIdUsuario())) {
        }else{
            //Pegar a última mensagem e exibir e o horário da última mensagem
            listenerMensagensAdapterChat = mensagensAdapterChatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Mensagem mensagemCompleta = snapshot1.getValue(Mensagem.class);

                            atribuirAoHashMapMensagem("listenerMensagensAdapterChat", mensagemCompleta.getIdRemetente(), mensagemCompleta);

                            listaMensagem.add(mensagemCompleta);

                            String tipoMidiaUltimaMensagem = listaMensagem.get(listaMensagem.size() - 1).getTipoMensagem();
                            if (!tipoMidiaUltimaMensagem.equals("texto")) {
                                holder.txtViewLastMensagemChat.setTextColor(Color.BLUE);
                                holder.txtViewLastMensagemChat.setText("Mídia - " + tipoMidiaUltimaMensagem);
                            } else {
                                holder.txtViewLastMensagemChat.setTextColor(Color.BLACK);
                                holder.txtViewLastMensagemChat.setText("" + listaMensagem.get(listaMensagem.size() - 1).getConteudoMensagem());
                            }
                            Date horarioUltimaMensagem = usuarioContato.getDataMensagemCompleta();
                            holder.txtViewHoraMensagem.setText("" + horarioUltimaMensagem.getHours() + ":" + horarioUltimaMensagem.getMinutes());

                        /*caso tenha algum problema ao trazer a última mensagem dessa forma somente pelo for,
                         ai é só pegar o último elemento da lista que é para funcionar.
                        holder.txtViewLastMensagemChat.setText(""+listaMensagem.get(listaMensagem.size() - 1).getConteudoMensagem());
                        Date horarioUltimaMensagem = listaMensagem.get(listaMensagem.size() -1).getDataMensagemCompleta();
                        holder.txtViewHoraMensagem.setText(""+horarioUltimaMensagem.getHours()+":"+horarioUltimaMensagem.getMinutes());
                         */
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaChat.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewFotoPerfilChat;
        private TextView txtViewNomePerfilChat, txtViewLastMensagemChat,
                txtViewHoraMensagem;
        private Button btnNumeroMensagem;
        private View viewEspacoBadge;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewFotoPerfilChat = itemView.findViewById(R.id.imgViewFotoPerfilChat);
            txtViewNomePerfilChat = itemView.findViewById(R.id.txtViewNomePerfilChat);
            txtViewLastMensagemChat = itemView.findViewById(R.id.txtViewLastMensagemChat);
            txtViewHoraMensagem = itemView.findViewById(R.id.txtViewHoraMensagem);
            btnNumeroMensagem = itemView.findViewById(R.id.btnNumeroMensagem);
            viewEspacoBadge = itemView.findViewById(R.id.viewEspacoBadge);
        }
    }

    public void adicionarUsuario(HashSet<Usuario> listaChatSemDuplicatas) {

        listaChat.addAll(listaChatSemDuplicatas);
        notifyItemRangeRemoved(0, listaChat.size());
        notifyItemRangeInserted(0, listaChat.size() - 1);

        //ToastCustomizado.toastCustomizadoCurto("Size atual - " + listaChat.size(),context);
    }

    public void removerItemConversa(Usuario usuario) {
        HashMap<String, Usuario> map = new HashMap<String, Usuario>();
        for (int i = 0; i < listaChat.size(); i++) {
            Usuario obj = listaChat.get(i);
            map.put(obj.getIdUsuario(), obj);
        }
        int position = listaChat.indexOf(map.get(usuario.getIdUsuario()));
        listaChat.remove(position);
        notifyItemRemoved(position);
    }

    private void abrirConversa(Usuario usuarioSelecionado) {
        Intent intent = new Intent(context, ConversaActivity.class);
        intent.putExtra("usuario", usuarioSelecionado);
        intent.putExtra("voltarChatFragment", "ChatInicioActivity.class");
        context.startActivity(intent);
    }

    private void atribuirAoHashMapUsuario(String tipoListener, Usuario usuario){
        if (tipoListener != null && !tipoListener.isEmpty()) {
            switch (tipoListener){
                case "infosUsuarioAtualRef":
                    dadosComListenerinfosUsuarioAtualRef.put(usuario.getIdUsuario(), usuario);
                    break;
                case "infosUsuarioContatoRef":
                    dadosComListenerinfosUsuarioContatoRef.put(usuario.getIdUsuario(), usuario);
                    break;
            }
        }
    }

    private void atribuirAoHashMapContato(String tipoListener, String key, Contatos contatos){
        if (tipoListener != null && !tipoListener.isEmpty()) {
            switch (tipoListener){
                case "listenerContadorMsgRef":
                    dadosComListenerContadorMsgRef.put(key, contatos);
                    break;
            }
        }
    }

    private void atribuirAoHashMapMensagem(String tipoListener, String key, Mensagem mensagem){
        if (tipoListener != null && !tipoListener.isEmpty()) {
            switch (tipoListener){
                case "listenerMensagensAdapterChat":
                    dadosComListenerMensagensAdapterChat.put(key, mensagem);
                    break;
            }
        }
    }
}
