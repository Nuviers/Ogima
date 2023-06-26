package com.example.ogima.helper;

import android.util.Log;

import androidx.recyclerview.widget.DiffUtil;

import com.example.ogima.model.DailyShort;
import com.example.ogima.model.Usuario;

import java.util.List;

import io.reactivex.annotations.Nullable;

public class UsuarioDiffCallback extends DiffUtil.Callback {

    private final List<Usuario> mOldUserList;
    private final List<Usuario> mNewUserList;

    public UsuarioDiffCallback(List<Usuario> mOldUserList, List<Usuario> mNewUserList) {
        this.mOldUserList = mOldUserList;
        this.mNewUserList = mNewUserList;
    }


    @Override
    public int getOldListSize() {
        //Retorna o tamanho da lista antiga.
        return mOldUserList.size();
    }

    @Override
    public int getNewListSize() {
        //Retorna o tamanho da lista nova.
        return mNewUserList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.

        Usuario oldUser = mOldUserList.get(oldItemPosition);
        Usuario newUser = mNewUserList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldUser.equals(newUser);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Usuario oldUser = mOldUserList.get(oldItemPosition);
        final Usuario newUser = mNewUserList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldUser.equals(newUser);

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
