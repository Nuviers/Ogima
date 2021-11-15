package com.example.ogima.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.ui.intro.IntrodActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private TextView txtDeslogar;
    private GoogleSignInClient mSignInClient;
    private ImageView imgFotoUsuario, imgFundoUsuario;


    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

         txtDeslogar = view.findViewById(R.id.txtDeslogar);

         txtDeslogar.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                         .requestIdToken("998572659584-tt3hhp5fb3qtvhctv129536mlgsg3v16.apps.googleusercontent.com")
                         .requestEmail()
                         .build();

                 mSignInClient = GoogleSignIn.getClient(getActivity(), gso);

                 FirebaseAuth.getInstance().signOut();
                 mSignInClient.signOut();
                 Intent intent = new Intent(getActivity(), IntrodActivity.class);
                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                 startActivity(intent);
                 getActivity().finish();

             }
         });

        imgFotoUsuario = view.findViewById(R.id.imgFotoUsuario);
        imgFundoUsuario = view.findViewById(R.id.imgFundoUsuario);


        return view;
    }

}
