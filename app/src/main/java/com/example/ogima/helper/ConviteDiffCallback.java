package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.Convite;

import java.util.List;

import io.reactivex.annotations.Nullable;

public class ConviteDiffCallback extends DiffUtil.Callback {

    private final List<Convite> mOldConviteList;
    private final List<Convite> mNewConviteList;

    public ConviteDiffCallback(List<Convite> mOldConviteList, List<Convite> mNewConviteList) {
        this.mOldConviteList = mOldConviteList;
        this.mNewConviteList = mNewConviteList;
    }


    @Override
    public int getOldListSize() {
        //Retorna o tamanho da lista antiga.
        return mOldConviteList.size();
    }

    @Override
    public int getNewListSize() {
        //Retorna o tamanho da lista nova.
        return mNewConviteList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.

        Convite oldConvite = mOldConviteList.get(oldItemPosition);
        Convite newConvite = mNewConviteList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldConvite.equals(newConvite);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Convite oldConvite = mOldConviteList.get(oldItemPosition);
        final Convite newConvite = mNewConviteList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldConvite.equals(newConvite);

        Log.d("DIFF", "areContentsTheSame " + returnAreContentsTheSame);

        return returnAreContentsTheSame;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Log.d("ChangePayload", "Change payload called");
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
