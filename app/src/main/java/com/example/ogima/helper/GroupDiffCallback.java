package com.example.ogima.helper;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.Chat;
import com.example.ogima.model.Grupo;

import java.util.List;

public class GroupDiffCallback extends DiffUtil.Callback{

    private final List<Grupo> mOldGroupList;
    private final List<Grupo> mNewGroupList;

    public GroupDiffCallback(List<Grupo> mOldGroupList, List<Grupo> mNewGroupList) {
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
        //Esse método serve para verificar se trata do mesmo objeto.

        Grupo oldGroup = mOldGroupList.get(oldItemPosition);
        Grupo newGroup = mNewGroupList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldGroup.equals(newGroup);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Grupo oldGroup = mOldGroupList.get(oldItemPosition);
        final Grupo newGroup = mNewGroupList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldGroup.equals(newGroup);

        Log.d("DIFF", "areContentsTheSame " + returnAreContentsTheSame);

        return returnAreContentsTheSame;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
