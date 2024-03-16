package com.example.ogima.helper;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.example.ogima.model.Contatos;
import java.util.List;

public class ContactDiffCallback extends DiffUtil.Callback{

    private final List<Contatos> mOldContactList;
    private final List<Contatos> mNewContactList;

    public ContactDiffCallback(List<Contatos> mOldContactList, List<Contatos> mNewContactList) {
        this.mOldContactList = mOldContactList;
        this.mNewContactList = mNewContactList;
    }

    @Override
    public int getOldListSize() {
        //Retorna o tamanho da lista antiga.
        return mOldContactList.size();
    }

    @Override
    public int getNewListSize() {
        //Retorna o tamanho da lista nova.
        return mNewContactList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        //Deve ser comparado dados que não mudam, por exemplo ids,
        //esse método serve para verificar se trata do mesmo objeto.

        Contatos oldContact = mOldContactList.get(oldItemPosition);
        Contatos newContact = mNewContactList.get(newItemPosition);

        boolean returnAreItemsTheSame = oldContact.equals(newContact);

        Log.d("DIFF", "areItemsTheSame: " + returnAreItemsTheSame);

        return returnAreItemsTheSame;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Somente verifique igualdade entre objetos inteiros e não
        //campos do objeto entre si, se não dará errado.
        final Contatos oldContact = mOldContactList.get(oldItemPosition);
        final Contatos newContact = mNewContactList.get(newItemPosition);

        boolean returnAreContentsTheSame = oldContact.equals(newContact);

        Log.d("DIFF", "areContentsTheSame " + returnAreContentsTheSame);

        return returnAreContentsTheSame;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
