package com.example.ogima.helper;

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
        final boolean retorno;

        String novoIdPostagem = null;
        String antigoIdPostagem = null;

        if (mNewPostList != null
                && mNewPostList.get(newItemPosition) != null) {
            novoIdPostagem = mNewPostList.get(newItemPosition).getIdPostagem();
        }

        if (mOldPostList != null
                && mOldPostList.get(oldItemPosition) != null) {
            antigoIdPostagem = mOldPostList.get(oldItemPosition).getIdPostagem();
        }


        if (novoIdPostagem != null && antigoIdPostagem != null
                && antigoIdPostagem.equals(novoIdPostagem)) {
            retorno = true;
        } else {
            retorno = false;
        }

        return retorno;
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

        final String novoIdPostagem = newPost.getIdPostagem();
        final String antigoIdPostagem = oldGroup.getIdPostagem();

        final Boolean novoStatusEdicao = newPost.getEdicaoEmAndamento();
        final Boolean antigoStatusEdicao = oldGroup.getEdicaoEmAndamento();

        final boolean retorno;

        if (novoIdPostagem != null && antigoIdPostagem != null
                && novoStatusEdicao != null && antigoStatusEdicao != null
                && antigoIdPostagem.equals(novoIdPostagem)
                && antigoStatusEdicao.equals(novoStatusEdicao)) {
            retorno = true;
        } else {
            retorno = false;
        }
        return retorno;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}