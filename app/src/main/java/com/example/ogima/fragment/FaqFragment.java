package com.example.ogima.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.ogima.R;

public class FaqFragment extends Fragment {

    ImageButton arrowSenha, arrowEmail, arrowVincular;
    LinearLayout hiddenViewSenha, hiddenViewEmail, hiddenViewVincular;
    CardView cardViewSenha, cardViewEmail, cardViewVincular;

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

        arrowSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenViewSenha.getVisibility() == View.VISIBLE) {
                    hiddenViewSenha.setVisibility(View.GONE);
                    arrowSenha.setImageResource(R.drawable.ic_baseline_expand_more_24);
                }
                else {
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
                }
                else {
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
                }
                else {
                    hiddenViewVincular.setVisibility(View.VISIBLE);
                    arrowVincular.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        return view;
    }

}