package com.example.ogima.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.ogima.R;
import com.example.ogima.activity.LoginEmailActivity;
import com.example.ogima.activity.ProblemasLogin;

public class FaqFragment extends Fragment {

    ImageButton arrowSenha, arrowEmail, arrowVincular, arrowDesvincular, arrowHackeado,
            arrowDenunciar;
    LinearLayout hiddenViewSenha, hiddenViewEmail, hiddenViewVincular,
            hiddenViewDesvincular, hiddenViewHackeado, hiddenViewDenunciar;
    CardView cardViewSenha, cardViewEmail, cardViewVincular, cardViewDesvincular,
            cardViewHackeado, cardViewDenunciar;
    Button buttonContataSuporte, buttonHackeado, buttonRedefinir, buttonDenunciar;

    public FaqFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        cardViewSenha = view.findViewById(R.id.base_cardviewSenha);
        arrowSenha = view.findViewById(R.id.arrow_buttonSenha);
        hiddenViewSenha = view.findViewById(R.id.hidden_viewSenha);

        cardViewEmail = view.findViewById(R.id.base_cardviewEmail);
        arrowEmail = view.findViewById(R.id.arrow_buttonEmail);
        hiddenViewEmail = view.findViewById(R.id.hidden_viewEmail);

        cardViewVincular = view.findViewById(R.id.base_cardviewVincular);
        arrowVincular = view.findViewById(R.id.arrow_buttonVincular);
        hiddenViewVincular = view.findViewById(R.id.hidden_viewVincular);

        cardViewDesvincular = view.findViewById(R.id.base_cardviewDesvincular);
        arrowDesvincular = view.findViewById(R.id.arrow_buttonDesvincular);
        hiddenViewDesvincular = view.findViewById(R.id.hidden_viewDesvincular);

        cardViewHackeado = view.findViewById(R.id.base_cardviewHackeado);
        arrowHackeado = view.findViewById(R.id.arrow_buttonHackeado);
        hiddenViewHackeado = view.findViewById(R.id.hidden_viewHackeado);

        cardViewDenunciar = view.findViewById(R.id.base_cardviewDenunciar);
        arrowDenunciar = view.findViewById(R.id.arrow_buttonDenunciar);
        hiddenViewDenunciar = view.findViewById(R.id.hidden_viewDenunciar);

        buttonContataSuporte = view.findViewById(R.id.buttonContataSuporte);
        buttonHackeado = view.findViewById(R.id.buttonHackeado);
        buttonRedefinir = view.findViewById(R.id.buttonRedefinir);
        buttonDenunciar = view.findViewById(R.id.buttonDenunciar);

        buttonRedefinir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProblemasLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        buttonHackeado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProblemasLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        arrowSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenViewSenha.getVisibility() == View.VISIBLE) {
                    hiddenViewSenha.setVisibility(View.GONE);
                    arrowSenha.setImageResource(R.drawable.ic_baseline_expand_more_24);
                } else {
                    hiddenViewSenha.setVisibility(View.VISIBLE);
                    arrowSenha.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        arrowEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenViewEmail.getVisibility() == View.VISIBLE) {
                    hiddenViewEmail.setVisibility(View.GONE);
                    arrowEmail.setImageResource(R.drawable.ic_baseline_expand_more_24);
                } else {
                    hiddenViewEmail.setVisibility(View.VISIBLE);
                    arrowEmail.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        arrowVincular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenViewVincular.getVisibility() == View.VISIBLE) {
                    hiddenViewVincular.setVisibility(View.GONE);
                    arrowVincular.setImageResource(R.drawable.ic_baseline_expand_more_24);
                } else {
                    hiddenViewVincular.setVisibility(View.VISIBLE);
                    arrowVincular.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        arrowDesvincular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenViewDesvincular.getVisibility() == View.VISIBLE) {
                    hiddenViewDesvincular.setVisibility(View.GONE);
                    arrowDesvincular.setImageResource(R.drawable.ic_baseline_expand_more_24);
                } else {
                    hiddenViewDesvincular.setVisibility(View.VISIBLE);
                    arrowDesvincular.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        arrowHackeado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenViewHackeado.getVisibility() == View.VISIBLE) {
                    hiddenViewHackeado.setVisibility(View.GONE);
                    arrowHackeado.setImageResource(R.drawable.ic_baseline_expand_more_24);
                } else {
                    hiddenViewHackeado.setVisibility(View.VISIBLE);
                    arrowHackeado.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        arrowDenunciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenViewDenunciar.getVisibility() == View.VISIBLE) {
                    hiddenViewDenunciar.setVisibility(View.GONE);
                    arrowDenunciar.setImageResource(R.drawable.ic_baseline_expand_more_24);
                } else {
                    hiddenViewDenunciar.setVisibility(View.VISIBLE);
                    arrowDenunciar.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        return view;
    }

}