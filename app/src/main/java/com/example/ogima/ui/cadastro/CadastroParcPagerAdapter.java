package com.example.ogima.ui.cadastro;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ogima.fragment.FaqFragment;
import com.example.ogima.fragment.RecupEmailFragment;
import com.example.ogima.fragment.StickersFragment;

import io.reactivex.annotations.NonNull;

public class CadastroParcPagerAdapter extends FragmentStateAdapter {
    public CadastroParcPagerAdapter(@androidx.annotation.NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new StickersFragment();
            case 1:
                return new FaqFragment();
            case 2:
                return new RecupEmailFragment();
            default:
                return new StickersFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Número de telas de cadastro
    }
}
