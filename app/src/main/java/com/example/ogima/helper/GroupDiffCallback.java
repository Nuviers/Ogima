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
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.
        return mOldGroupList.get(oldItemPosition).getIdGrupo()
                .equals(mNewGroupList.get(newItemPosition).getIdGrupo());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado atributos que eu desejo verificar se foi mudado,
        //esse método serve para comparar os atributos do mesmo objeto
        //entre o atributo anterior e o novo, se for diferente algum atributo
        //notifica, se não ele não notifica pois os atributos que eu comparei
        //são iguais tanto anteriormente quanto atualmente
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
