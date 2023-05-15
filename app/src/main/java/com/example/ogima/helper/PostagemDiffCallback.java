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
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.
        return mOldPostList.get(oldItemPosition).getIdPostagem()
                .equals(mNewPostList.get(newItemPosition).getIdPostagem());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado atributos que eu desejo verificar se foi mudado,
        //esse método serve para comparar os atributos do mesmo objeto
        //entre o atributo anterior e o novo, se for diferente algum atributo
        //notifica, se não ele não notifica pois os atributos que eu comparei
        //são iguais tanto anteriormente quanto atualmente
        final Postagem oldGroup = mOldPostList.get(oldItemPosition);
        final Postagem newPost = mNewPostList.get(newItemPosition);

        return oldGroup.getIdPostagem().equals(newPost.getIdPostagem())
                && oldGroup.getEdicaoEmAndamento().equals(newPost.getEdicaoEmAndamento());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
