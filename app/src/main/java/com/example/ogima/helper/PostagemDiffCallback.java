package com.example.ogima.helper;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Postagem;

import java.util.List;

public class PostagemDiffCallback extends DiffUtil.Callback{

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
        //Compara a lista anterior com o índice da posição antiga e verifica
        //se esse dado é igual ao dado da nova lista com a posição nova.
        return mOldPostList.get(oldItemPosition).getIdPostagem()
                .equals(mNewPostList.get(newItemPosition).getIdPostagem());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Compara os dois objetos através de seus respectivos dados verificando
        //se existe igualdade, caso tenha, significa que se trata do mesmo objeto.
        final Postagem oldGroup = mOldPostList.get(oldItemPosition);
        final Postagem newPost = mNewPostList.get(newItemPosition);

        return oldGroup.getIdPostagem().equals(newPost.getIdPostagem())
                && oldGroup.getDataPostagem().equals(newPost.getDataPostagem());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
