package com.example.ogima.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Postagem;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class AdapterFeed extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuarioLogado, emailUsuarioAtual;
    private Context context;
    private List<Postagem> listaPostagens;

    //Interfaces listeners
    private RemoverPostagemListener removerPostagemListener;
    private RecuperaPosicaoAnterior recuperaPosicaoListener;

    private ExoPlayer exoPlayer;
    private Player.Listener listenerExo;
    private boolean atualizarPrimeiraPostagem = false;



    public interface RemoverPostagemListener{
        void onPostagemRemocao(Postagem postagemRemovida, int posicao, ImageButton imgBtnExcluir);
    }

    public interface RecuperaPosicaoAnterior {
        void onPosicaoAnterior(int posicaoAnterior);
    }

    public interface RemocaoDadosServidor {
        void onConcluido();
    }

    public interface RemoverListenerRecycler {
        void onRemoverListener();
        void onError();
    }

    public interface ListaAtualizadaCallback {
        void onAtualizado();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return listaPostagens.size();
    }

    private class PhotoViewHolder extends RecyclerView.ViewHolder {

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
