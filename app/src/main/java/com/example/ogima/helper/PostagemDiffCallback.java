package com.example.ogima.helper;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Postagem;

import java.util.List;

public class PostagemDiffCallback extends DiffUtil.Callback {

    private final List<Postagem> mOldPostList;
    private final List<Postagem> mNewPostList;

    public PostagemDiffCallback(List<Postagem> mOldPostList, List<Postagem> mNewPostList) {
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

        Postagem oldPost = mOldPostList.get(oldItemPosition);
        Postagem newPost = mNewPostList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldPost.equals(newPost);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Postagem oldGroup = mOldPostList.get(oldItemPosition);
        final Postagem newPost = mNewPostList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldGroup.equals(newPost);

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