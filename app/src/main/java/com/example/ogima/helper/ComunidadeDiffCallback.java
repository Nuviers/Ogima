package com.example.ogima.helper;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;

import java.util.List;

public class ComunidadeDiffCallback extends DiffUtil.Callback{

    private final List<Comunidade> mOldGroupList;
    private final List<Comunidade> mNewGroupList;

    public ComunidadeDiffCallback(List<Comunidade> mOldGroupList, List<Comunidade> mNewGroupList) {
        this.mOldGroupList = mOldGroupList;
        this.mNewGroupList = mNewGroupList;
    }


    @Override
    public int getOldListSize() {
        //Retorna o tamanho da lista antiga.
        return mOldGroupList.size();
    }

    @Override
    public int getNewListSize() {
        //Retorna o tamanho da lista nova.
        return mNewGroupList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.

        Comunidade oldComunidade = mOldGroupList.get(oldItemPosition);
        Comunidade newComunidade = mNewGroupList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldComunidade.equals(newComunidade);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Comunidade oldComunidade = mOldGroupList.get(oldItemPosition);
        final Comunidade newComunidade = mNewGroupList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldComunidade.equals(newComunidade);

        Log.d("DIFF", "areContentsTheSame " + returnAreContentsTheSame);

        return returnAreContentsTheSame;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
