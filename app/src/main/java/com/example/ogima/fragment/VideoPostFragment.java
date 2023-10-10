package com.example.ogima.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ogima.R;
import com.example.ogima.helper.UsuarioUtils;

public class VideoPostFragment extends Fragment {

    private String idUsuario = "";

    public VideoPostFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_post, container, false);
        inicializarComponentes(view);
        return view;
    }

    private void configBundle(){
        Bundle args = getArguments();
        if (args != null) {

        }
    }

    private void inicializarComponentes(View view){

    }
}