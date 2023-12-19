package com.example.ogima.helper;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.Comunidade;
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
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.

        Grupo oldGrupo = mOldGroupList.get(oldItemPosition);
        Grupo newGrupo = mNewGroupList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldGrupo.equals(newGrupo);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Grupo oldGrupo = mOldGroupList.get(oldItemPosition);
        final Grupo newGrupo = mNewGroupList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldGrupo.equals(newGrupo);

        Log.d("DIFF", "areContentsTheSame " + returnAreContentsTheSame);

        return returnAreContentsTheSame;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
