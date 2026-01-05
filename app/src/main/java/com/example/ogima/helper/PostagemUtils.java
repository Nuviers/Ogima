package com.example.ogima.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.ogima.model.Postagem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PostagemUtils {

    private Context context;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private AtualizarContador atualizarContador;

    public interface VerificaCurtidaCallback {
        void onJaCurtido();

        void onNaoCurtido();

        void onError(String message);
    }

    public interface CurtirPostagemCallback {
        void onCurtido(int nrLikeAtual);

        void onError(String message);
    }

    public interface DescurtirPostagemCallback {
        void onDescurtido(int nrLikeAtual);

        void onError(String message);
    }

    public PostagemUtils(Context context) {
        this.context = context;
        this.atualizarContador = new AtualizarContador();
    }

    public void VerificaCurtida(String idUser, String idPost, VerificaCurtidaCallback callback) {
        DatabaseReference verificaCurtidaPostagemRef = firebaseRef
                .child("curtidasPostagem").child(idPost)
                .child(idUser);
        verificaCurtidaPostagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onJaCurtido();
                } else {
                    callback.onNaoCurtido();
                }
                verificaCurtidaPostagemRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void CurtirPostagem(String idUser, Postagem postagem, CurtirPostagemCallback callback) {
        DatabaseReference salvarCurtidaRef = firebaseRef
                .child("curtidasPostagem").child(postagem.getIdPostagem())
                .child(idUser).child("idUsuarioInterativo");
        //Salva a curtida em curtidasPostagem
        salvarCurtidaRef.setValue(idUser).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                acrescentarContador(postagem, callback);
            }
        });
    }

    public void acrescentarContador(Postagem postagem, CurtirPostagemCallback callback) {

        DatabaseReference atualizaNrCurtidaRef = firebaseRef
                .child("postagens").child(postagem.getIdDonoPostagem())
                .child(postagem.getIdPostagem()).child("totalCurtidasPostagem");

        atualizarContador.acrescentarContador(atualizaNrCurtidaRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                AtualizaTotalNrLikePostagem(true, contadorAtualizado, postagem, callback);
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

    public void AtualizaTotalNrLikePostagem(boolean acrescentar, int contador, Postagem postagem, CurtirPostagemCallback callback) {
        DatabaseReference dadosLikeRef = firebaseRef
                .child("totalCurtidas").child(postagem.getIdPostagem());
        DatabaseReference totalLikeRef = firebaseRef
                .child("totalCurtidas").child(postagem.getIdPostagem())
                .child("totalCurtidasPostagem");
        dadosLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Não é necessário salvar os dados do like, eles já existem
                    totalLikeRef.setValue(contador).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError(e.getMessage());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            callback.onCurtido(contador);
                        }
                    });
                }else{
                    //Salvar dados do like
                    HashMap<String, Object> dadosLike = new HashMap<>();
                    dadosLike.put("idDonoPostagem",postagem.getIdDonoPostagem());
                    dadosLike.put("idPostagem",postagem.getIdPostagem());
                    dadosLike.put("totalCurtidasPostagem", contador);
                    dadosLikeRef.setValue(dadosLike).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError(e.getMessage());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            callback.onCurtido(contador);
                        }
                    });
                }
                dadosLikeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void descurtirPostagem(String idUser, Postagem postagem, DescurtirPostagemCallback callback){
        DatabaseReference curtidasRef = firebaseRef
                .child("curtidasPostagem").child(postagem.getIdPostagem())
                .child(idUser).child("idUsuarioInterativo");
        curtidasRef.removeValue().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                subtrairContadorLikePostagem(postagem, callback);
            }
        });
    }

    public void subtrairContadorLikePostagem(Postagem postagem, DescurtirPostagemCallback callback){
        DatabaseReference atualizaNrCurtidaRef = firebaseRef
                .child("postagens").child(postagem.getIdDonoPostagem())
                .child(postagem.getIdPostagem()).child("totalCurtidasPostagem");

        atualizarContador.subtrairContador(atualizaNrCurtidaRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                diminuirTotalCurtidas(postagem, contadorAtualizado, callback);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void diminuirTotalCurtidas(Postagem postagem, int contador, DescurtirPostagemCallback callback){
        DatabaseReference dadosLikeRef = firebaseRef
                .child("totalCurtidas").child(postagem.getIdPostagem())
                .child("totalCurtidasPostagem");
        atualizarContador.subtrairContador(dadosLikeRef, new AtualizarContador.AtualizarContadorCallback() {
            @Override
            public void onSuccess(int contadorAtualizado) {
                callback.onDescurtido(contadorAtualizado);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
