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
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AdapterGrupoFirebaseTeste extends FirebaseRecyclerAdapter<Grupo, AdapterGrupoFirebaseTeste.MyViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String emailUsuarioAtual, idUsuarioLogado;

    public AdapterGrupoFirebaseTeste(Context c, @NonNull FirebaseRecyclerOptions<Grupo> options) {
        super(options);
        this.context = c;
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuarioAtual);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Grupo model) {
            holder.txtViewTesteFire.setText(model.getNomeGrupo());
            GlideCustomizado.montarGlideMensagem(context,
                    model.getFotoGrupo(), holder.imgViewUserTesteFireUi, android.R.color.transparent);
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
