package com.example.ogima.fragment;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.helper.GlideCustomizado;

/**
 * A simple {@link Fragment} subclass.
 */
public class FrameSuporteInicioFragment extends Fragment {

    private Fragment selectedFragment;
    private FrameLayout frameInicio;
    private DailyShortsFragment dailyShortsFragment;
    private TextView txtInicioPostagens, txtDailyShorts;
    private ImageView imgViewGifFireDestaque;
    private InicioFragment inicioFragment = new InicioFragment();

    public FrameSuporteInicioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frame_suporte_inicio, container, false);
        inicializandoComponentes(view);

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameInicio, inicioFragment).commit();

        Glide.with(getContext()).load(R.drawable.gif_ic_sticker_destaque).centerCrop()
                .into(imgViewGifFireDestaque);

        txtDailyShorts.setBackgroundColor(Color.parseColor("#ffffff"));
        txtDailyShorts.setTextColor(Color.parseColor("#000000"));

        txtInicioPostagens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFragment = null;
                selectedFragment = new InicioFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameInicio, selectedFragment).commit();

                txtDailyShorts.setBackgroundColor(Color.parseColor("#ffffff"));
                txtDailyShorts.setTextColor(Color.parseColor("#000000"));

                txtInicioPostagens.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.estilo_background_inicio));
                txtInicioPostagens.setTextColor(Color.parseColor("#ffffff"));
                //String styledText = "<u><font color='#000080'>Postagens</font></u>";
                //txtInicioPostagens.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
            }
        });

        txtDailyShorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFragment = null;
                selectedFragment = new DailyShortsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameInicio, selectedFragment).commit();

                txtInicioPostagens.setBackgroundColor(Color.parseColor("#ffffff"));
                txtInicioPostagens.setTextColor(Color.parseColor("#000000"));

                txtDailyShorts.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.estilo_background_inicio_v2));
                txtDailyShorts.setTextColor(Color.parseColor("#ffffff"));
            }
        });

        return view;
    }

    private void inicializandoComponentes(View view) {
        frameInicio = view.findViewById(R.id.frameInicio);
        txtInicioPostagens = view.findViewById(R.id.txtInicioPostagens);
        txtDailyShorts = view.findViewById(R.id.txtDailyShorts);
        imgViewGifFireDestaque = view.findViewById(R.id.imgViewGifFireDestaque);
    }

}
