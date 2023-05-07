package com.example.ogima.helper;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class AtualizarContador {


    public interface AtualizarContadorCallback {
        void onSuccess(int contadorAtualizado);
        void onError(String errorMessage);
    }

    public void acrescentarContador(DatabaseReference reference, AtualizarContadorCallback callback){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // Verifica se o valor atual existe
                Integer valorAtual = mutableData.getValue(Integer.class);
                if (valorAtual == null) {
                    // O nó não existe, defina o valor inicial como 1
                    mutableData.setValue(1);
                } else {
                    // O nó existe, incrementa o valor
                    mutableData.setValue(valorAtual + 1);
                }

                // Retorna o novo valor para finalizar a transação
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    // A transação foi bem-sucedida
                    int novoValor = currentData.getValue(Integer.class);
                    callback.onSuccess(novoValor);
                    // Faça algo com o novo valor
                } else {
                    // A transação falhou
                    // Lida com o erro
                  callback.onError("Ocorreu um erro ao atualizar contador");
                }
            }
        });
    }

    public void subtrairContador(DatabaseReference reference, AtualizarContadorCallback callback){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // Verifica se o valor atual existe
                Integer valorAtual = mutableData.getValue(Integer.class);
                if (valorAtual == null) {
                    // O nó não existe, defina o valor inicial como 1
                    mutableData.setValue(0);
                } else if(valorAtual > 0) {
                    // O nó existe, incrementa o valor
                    mutableData.setValue(valorAtual - 1);
                }else{
                    mutableData.setValue(0);
                }

                // Retorna o novo valor para finalizar a transação
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    // A transação foi bem-sucedida
                    int novoValor = currentData.getValue(Integer.class);

                    callback.onSuccess(novoValor);
                    // Faça algo com o novo valor
                } else {
                    // A transação falhou
                    // Lida com o erro
                    callback.onError("Ocorreu um erro ao atualizar contador");
                }
            }
        });
    }
}
