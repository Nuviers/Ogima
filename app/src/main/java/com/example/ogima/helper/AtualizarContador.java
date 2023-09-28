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

    public interface AtualizarCoinsCallback {
        void onSuccess(int coinsAtualizado);
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
                    // O nó não existe, defina o valor inicial como 0
                    mutableData.setValue(0);
                } else if(valorAtual > 0) {
                    // O nó existe, subtraia o valor
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

    public void adicionarCoins(DatabaseReference reference,final int recompensa, AtualizarCoinsCallback callback){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // Verifica se o valor atual existe
                Integer valorAtual = mutableData.getValue(Integer.class);
                if (valorAtual == null) {
                    // O nó não existe, defina o valor inicial como 1
                    mutableData.setValue(0);
                } else {
                    // O nó existe, incrementa o valor
                    mutableData.setValue(valorAtual + recompensa);
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
                    callback.onError("Ocorreu um erro ao receber a recompensa");
                }
            }
        });
    }

    public void diminuirCoins(DatabaseReference reference, final int custo, AtualizarCoinsCallback callback){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // Verifica se o valor atual existe
                Integer valorAtual = mutableData.getValue(Integer.class);
                if (valorAtual == null) {
                    // O nó não existe, defina o valor inicial como 1
                    mutableData.setValue(0);
                } else {
                    // O nó existe, subtrai o valor
                    mutableData.setValue(valorAtual - custo);
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
                    callback.onError("Ocorreu um erro ao efetuar a compra");
                }
            }
        });
    }

    public void zerarContador(DatabaseReference reference, AtualizarContadorCallback callback){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // Verifica se o valor atual existe
                mutableData.setValue(0);
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

    public void acrescentarContadorPorValor(DatabaseReference reference, int valor, AtualizarContadorCallback callback){
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.setValue(valor + 1);
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
                    callback.onError("Ocorreu um erro ao atualizar contador " + error.getMessage());
                }
            }
        });
    }

}
