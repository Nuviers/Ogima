package com.example.ogima.helper;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

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
        //Compara a lista anterior com o índice da posição antiga e verifica
        //se esse dado é igual ao dado da nova lista com a posição nova.
        return mOldGroupList.get(oldItemPosition).getIdGrupo()
                .equals(mNewGroupList.get(newItemPosition).getIdGrupo());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Compara os dois objetos através de seus respectivos dados verificando
        //se existe igualdade, caso tenha, significa que se trata do mesmo objeto.
        final Grupo oldGroup = mOldGroupList.get(oldItemPosition);
        final Grupo newGroup = mNewGroupList.get(newItemPosition);

        return oldGroup.getIdGrupo().equals(newGroup.getIdGrupo())
                && oldGroup.getNomeGrupo().equals(newGroup.getNomeGrupo());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
