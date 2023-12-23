package com.example.ogima.helper;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.Chat;

import java.util.List;

public class ChatDiffCallback extends DiffUtil.Callback{

    private final List<Chat> mOldChatList;
    private final List<Chat> mNewChatList;

    public ChatDiffCallback(List<Chat> mOldChatList, List<Chat> mNewChatList) {
        this.mOldChatList = mOldChatList;
        this.mNewChatList = mNewChatList;
    }


    @Override
    public int getOldListSize() {
        //Retorna o tamanho da lista antiga.
        return mOldChatList.size();
    }

    @Override
    public int getNewListSize() {
        //Retorna o tamanho da lista nova.
        return mNewChatList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.

        Chat oldChat = mOldChatList.get(oldItemPosition);
        Chat newChat = mNewChatList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldChat.equals(newChat);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Chat oldChat = mOldChatList.get(oldItemPosition);
        final Chat newChat = mNewChatList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldChat.equals(newChat);

        Log.d("DIFF", "areContentsTheSame " + returnAreContentsTheSame);

        return returnAreContentsTheSame;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
