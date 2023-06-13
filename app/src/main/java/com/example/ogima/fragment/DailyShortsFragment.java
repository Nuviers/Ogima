package com.example.ogima.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyShortsFragment extends Fragment {


    public DailyShortsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_shorts, container, false);


        return view;
    }
}
