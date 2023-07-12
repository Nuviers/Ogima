package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
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

public class AdapterFriends extends FirebaseRecyclerAdapter<Usuario, AdapterFriends.MyViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String emailUsuarioAtual, idUsuarioLogado;
    private DatabaseReference usuarioRecebidoRef;
    private PersonProfileActivity personProfileActivity = new PersonProfileActivity();

    public AdapterFriends(Context c, @NonNull FirebaseRecyclerOptions<Usuario> options) {
        super(options);
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdapterFriends.MyViewHolder holder, int position, @NonNull Usuario model) {

        usuarioRecebidoRef = firebaseRef.child("usuarios")
                .child(model.getIdUsuario());

        usuarioRecebidoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecebido = snapshot.getValue(Usuario.class);
                    //Preenche o nome, trata da condição do usuário atual em relação a gifs e exibe a foto
                    //do usuário recebido.
                    DadosUserPadrao.preencherDadosUser(context, usuarioRecebido, holder.textNomeFriend, holder.imageFriend);

                    holder.imageFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Verifica se o usuário atual está bloqueado, se não então prosseguir para o perfil
                            //do usuário selecionado.
                            VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context, usuarioRecebido);
                        }
                    });

                    holder.textNomeFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Verifica se o usuário atual está bloqueado, se não então prosseguir para o perfil
                            //do usuário selecionado.
                            VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(context, usuarioRecebido);
                        }
                    });

                    holder.btnDesfazerAmizade.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            desfazerAmizade(usuarioRecebido.getIdUsuario());
                        }
                    });

                }
                usuarioRecebidoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lista_friends, parent, false);
        return new MyViewHolder(view);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageFriend;
        private TextView textNomeFriend;
        private Button btnDesfazerAmizade;
        private LinearLayout linearLayoutFriends;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutFriends = itemView.findViewById(R.id.linearLayoutFriends);
            imageFriend = itemView.findViewById(R.id.imageFriend);
            textNomeFriend = itemView.findViewById(R.id.textNomeFriend);
            btnDesfazerAmizade = itemView.findViewById(R.id.btnDesfazerAmizade);
        }
    }

    private void desfazerAmizade(String idFriend) {
        //*personProfileActivity.desfazerAmizade(true, idFriend, idUsuarioLogado, context);
    }

    @Override
    public void updateOptions(@NonNull FirebaseRecyclerOptions<Usuario> options) {
        super.updateOptions(options);
    }
}
