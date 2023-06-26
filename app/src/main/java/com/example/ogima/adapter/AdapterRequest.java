package com.example.ogima.adapter;

import android.content.Context;
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
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.helper.AdicionarIdAmigoUtils;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DadosUserPadrao;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AdapterRequest extends FirebaseRecyclerAdapter<Usuario, AdapterRequest.MyViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String emailUsuarioAtual, idUsuarioLogado;
    private DatabaseReference usuarioRecebidoRef, dadosUserLogadoRef;
    private DatabaseReference recusarConviteRef, recusarConviteSelecionadoRef,
            adicionarAmigoRef, adicionarAmigoSelecionadoRef, atualizarContadorAmigoRef,
            atualizarContadorAmigoSelecionadoRef;
    private PersonProfileActivity personProfileActivity = new PersonProfileActivity();

    public AdapterRequest(Context c, @NonNull FirebaseRecyclerOptions<Usuario> options) {
        super(options);
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Usuario model) {

        usuarioRecebidoRef = firebaseRef.child("usuarios")
                .child(model.getIdRemetente());

        usuarioRecebidoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecebido = snapshot.getValue(Usuario.class);
                    //Preenche o nome, trata da condição do usuário atual em relação a gifs e exibe a foto
                    //do usuário recebido.
                    DadosUserPadrao.preencherDadosUser(context, usuarioRecebido, holder.textNomeAmigo, holder.imageAmigo);


                    holder.imageAmigo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Verifica se o usuário atual está bloqueado, se não então prosseguir para o perfil
                            //do usuário selecionado.
                            VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context, usuarioRecebido);
                        }
                    });

                    holder.textNomeAmigo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Verifica se o usuário atual está bloqueado, se não então prosseguir para o perfil
                            //do usuário selecionado.
                            VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context, usuarioRecebido);
                        }
                    });
                }
                usuarioRecebidoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.btnVerStatusAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aceitarAmizade(model.getIdRemetente());
            }
        });

        holder.imgButtonRejeitarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recusarConvite(model.getIdRemetente(), false, true);
            }
        });
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_friends_requests, parent, false);
        return new MyViewHolder(view);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageAmigo;
        private TextView textNomeAmigo;
        private Button btnVerStatusAmigo;
        private ImageButton imgButtonRejeitarPedido;
        private LinearLayout linearLayoutSolicitacoes;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutSolicitacoes = itemView.findViewById(R.id.linearLayoutSolicitacoes);
            imageAmigo = itemView.findViewById(R.id.imageAmigo);
            textNomeAmigo = itemView.findViewById(R.id.textNomeAmigo);
            btnVerStatusAmigo = itemView.findViewById(R.id.btnVerStatusAmigo);
            imgButtonRejeitarPedido = itemView.findViewById(R.id.imgButtonRejeitarPedido);
        }
    }

    @Override
    public void updateOptions(@NonNull FirebaseRecyclerOptions<Usuario> options) {
        super.updateOptions(options);
    }

    private void aceitarAmizade(String idRemetente) {

        adicionarAmigoRef = firebaseRef.child("friends")
                .child(idUsuarioLogado).child(idRemetente).child("idUsuario");

        adicionarAmigoSelecionadoRef = firebaseRef.child("friends")
                .child(idRemetente).child(idUsuarioLogado).child("idUsuario");

        AdicionarIdAmigoUtils.salvarAmigo(idRemetente, new AdicionarIdAmigoUtils.SalvarIdAmigoCallback() {
            @Override
            public void onAmigoSalvo() {
                recusarConvite(idRemetente, true, false);
            }

            @Override
            public void onError(@NonNull String message) {

            }
        });
    }

    private void recusarConvite(String idRemetente, Boolean atualizarContadorAmizade, Boolean exibirToast) {

        recusarConviteRef = firebaseRef.child("requestsFriendship")
                .child(idUsuarioLogado).child(idRemetente);

        recusarConviteSelecionadoRef = firebaseRef.child("requestsFriendship")
                .child(idRemetente).child(idUsuarioLogado);

        dadosUserLogadoRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado);

        atualizarContadorAmigoRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado).child("amigosUsuario");

        atualizarContadorAmigoSelecionadoRef = firebaseRef.child("usuarios")
                .child(idRemetente).child("amigosUsuario");

        dadosUserLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioAtual = snapshot.getValue(Usuario.class);
                    int pedidosAmizade = usuarioAtual.getPedidosAmizade();
                    pedidosAmizade = pedidosAmizade - 1;
                    dadosUserLogadoRef = dadosUserLogadoRef.child("pedidosAmizade");
                    dadosUserLogadoRef.setValue(pedidosAmizade).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            recusarConviteRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    recusarConviteSelecionadoRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            if (exibirToast) {
                                                ToastCustomizado.toastCustomizadoCurto("Pedido de amizade recusado com sucesso", context);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if (exibirToast) {
                                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao recusar o pedido de amizade, tente novamente mais tarde", context);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });

                    //tratar contador de amizades
                    if (atualizarContadorAmizade) {
                        int contadorAmizadeAtual = usuarioAtual.getAmigosUsuario();
                        contadorAmizadeAtual = contadorAmizadeAtual + 1;
                        atualizarContadorAmigoRef.setValue(contadorAmizadeAtual);
                    }
                }
                dadosUserLogadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Adiciona aos contatos e aos amigos somente se o convite de amizade foi aceito.
        if (atualizarContadorAmizade) {
            usuarioRecebidoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioContadorRecebido = snapshot.getValue(Usuario.class);
                        int contadorAmizadeRecebido = usuarioContadorRecebido.getAmigosUsuario();
                        contadorAmizadeRecebido = contadorAmizadeRecebido + 1;
                        atualizarContadorAmigoSelecionadoRef.setValue(contadorAmizadeRecebido);
                        adicionarContato(idRemetente);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void adicionarContato(String idRemetente) {
        personProfileActivity.adicionarContato(true, idRemetente, idUsuarioLogado);
    }
}
