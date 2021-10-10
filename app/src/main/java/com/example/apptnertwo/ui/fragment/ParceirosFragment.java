package com.example.apptnertwo.ui.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.apptnertwo.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParceirosFragment extends Fragment {


    public ParceirosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parceiros, container, false);
        return view;
    }

}
