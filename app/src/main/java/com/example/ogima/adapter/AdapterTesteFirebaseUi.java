package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AdapterTesteFirebaseUi extends FirebaseRecyclerAdapter<Usuario, AdapterTesteFirebaseUi.MyViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String emailUsuarioAtual, idUsuarioLogado;

    public AdapterTesteFirebaseUi(Context c, @NonNull FirebaseRecyclerOptions<Usuario> options) {
        super(options);
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Usuario model) {

        //Talvez o mais correto seja enviar o objeto Mensagem aqui
        //e com os dados da mensagem eu pego as infos do usuário e exibo aqui
        //pois só assim ele vai tratar do CRUD.

        //ToastCustomizado.toastCustomizadoCurto("Nome " + model.getNomeUsuario(), context);
        if (idUsuarioLogado.equals(model.getIdUsuario())) {
            holder.imgViewUserTesteFireUi.setVisibility(View.GONE);
        }

        //Com essa simples linha mesmo quando onStop chama adapterTesteFirebaseUi.stopListening();
        //os elementos colocado depois de outra classe era misturado, porêm se você limpa
        //o campo desejado que não seja da classe que tá sendo usado no FirebaseRecyclerAdapter
        //ele funciona e não mistura pois estou limpando o dado anterior.
        holder.txtViewTesteFire.setText("");
        //^^^^^^^
        DatabaseReference verificaContato = firebaseRef.child("conversas")
                .child(idUsuarioLogado).child(model.getIdUsuario());

        //VVVVVVVVVV mudar para addValue e verificar como se comporta
        //remover o listener no onStop
        verificaContato.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                    if (mensagem.getIdDestinatario().equals(model.getIdUsuario())) {
                        holder.txtViewTesteFire.setText("Midia" + mensagem.getTipoMensagem());
                    }
                    //holder.txtViewTesteFire.setText("Data " + horarioUltimaMensagem.getHours() + ":" + horarioUltimaMensagem.getMinutes());holder.txtViewHoraMensagem.setText("" + mod);
                }
                verificaContato.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GlideCustomizado.montarGlideFoto(context, model.getMinhaFoto(),
                holder.imgViewUserTesteFireUi, android.R.color.transparent);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_layout_firetesteui, parent, false);
        return new MyViewHolder(view);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgViewUserTesteFireUi;
        private TextView txtViewTesteFire;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewUserTesteFireUi = itemView.findViewById(R.id.imgViewUserTesteFireUi);
            txtViewTesteFire = itemView.findViewById(R.id.txtViewTesteFire);
        }
    }
}
