package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.DailyShort;

import java.util.List;

import io.reactivex.annotations.Nullable;

public class DailyShortDiffCallback extends DiffUtil.Callback {

    private final List<DailyShort> mOldPostList;
    private final List<DailyShort> mNewPostList;

    public DailyShortDiffCallback(List<DailyShort> mOldPostList, List<DailyShort> mNewPostList) {
        this.mOldPostList = mOldPostList;
        this.mNewPostList = mNewPostList;
    }


    @Override
    public int getOldListSize() {
        //Retorna o tamanho da lista antiga.
        return mOldPostList.size();
    }

    @Override
    public int getNewListSize() {
        //Retorna o tamanho da lista nova.
        return mNewPostList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.

        DailyShort oldDaily = mOldPostList.get(oldItemPosition);
        DailyShort newDaily = mNewPostList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldDaily.equals(newDaily);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final DailyShort oldDaily = mOldPostList.get(oldItemPosition);
        final DailyShort newDaily = mNewPostList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldDaily.equals(newDaily);

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
