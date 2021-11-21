package com.example.ogima.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.example.ogima.ui.intro.IntrodActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private TextView txtDeslogar, textTeste;
    private GoogleSignInClient mSignInClient;
    private ImageView imgFotoUsuario, imgFundoUsuario;

    private Button buttonEditarPerfil;
    private Usuario usuario;


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
                         .requestIdToken(getString(R.string.default_web_client_ids))
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
        textTeste = view.findViewById(R.id.textTeste);

        buttonEditarPerfil = view.findViewById(R.id.buttonEditarPerfil);

        //Recuperar dados do usuário
        FirebaseUser userProfile = UsuarioFirebase.getUsuarioAtual();

        usuario = new Usuario();

        try{
            if(userProfile.getDisplayName() != null){
                textTeste.setText(userProfile.getDisplayName());
            }else if(userProfile.getDisplayName() == null){
                textTeste.setText(usuario.getNomeUsuario());
            }

        }catch (Exception e){
            e.printStackTrace();
        }




        //Recuperando imagem do usuário e fundo tambem
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        FirebaseUser userFundo = UsuarioFirebase.getUsuarioAtual();

        //Uri url = user.getPhotoUrl();
        //Uri urlFundo = userFundo.getPhotoUrl();


        //if(url != null){
            //Glide.with(PerfilFragment.this)
                   // .load(url)
                    //.into(imgFotoUsuario);
       // }else{
           // imgFotoUsuario.setImageResource(R.drawable.animewomanavatar);
        //}

        //Recuperando imagem de fundo do usuário
       // if(urlFundo != null){
           // Glide.with(PerfilFragment.this)
                    //.load(urlFundo)
                    //.into(imgFundoUsuario);
        //}else{
            //imgFundoUsuario.setImageResource(R.drawable.animewomanavatar);
        //}


        buttonEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });



        return view;
    }

}
