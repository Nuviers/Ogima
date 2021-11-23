package com.example.ogima.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.helper.Base64Custom;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private TextView txtDeslogar, textTeste;
    private GoogleSignInClient mSignInClient;
    private ImageView imgFotoUsuario, imgFundoUsuario, imageViewGif;

    private String urlGifTeste = "";

    private Button buttonEditarPerfil;
    private Usuario usuario;

    private String minhaFoto;
    private String meuFundo;
    private String apelido;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();


    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

         txtDeslogar = view.findViewById(R.id.txtDeslogar);
         imageViewGif = view.findViewById(R.id.imageViewGif);

        try{
            testandoLog();
        }
        catch (Exception e){
            e.printStackTrace();
        }

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


        buttonEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


        urlGifTeste = "https://media.giphy.com/media/a4aAKvUXYgiRuEqRsc/giphy.gif";
        //urlGifTeste = "https://media2.giphy.com/media/jdFm2bcWlj4EUVCpc0/200w.gif?cid=afffb5fem1wzyqezeel1hv6gdxm4cks8voez0fc8nyehh3og&rid=200w.gif&ct=g";

        //Glide.with(PerfilFragment.this).asGif().load(urlGifTeste).into(imageViewGif);

        return view;
    }

    public void testandoLog(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);


        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    Log.i("FIREBASE", usuario.getIdUsuario());
                    Log.i("FIREBASEA", usuario.getNomeUsuario());
                    apelido = usuario.getApelidoUsuario();
                    meuFundo = usuario.getMeuFundo();
                    minhaFoto = usuario.getMinhaFoto();

                    if(apelido != null){

                        Toast.makeText(getActivity(), " Okay", Toast.LENGTH_SHORT).show();

                        if(minhaFoto != null){

                            Glide.with(PerfilFragment.this)
                                   .load(minhaFoto)
                                   .centerCrop()
                                   .circleCrop()
                                   .into(imgFotoUsuario);

                            Log.i("IMAGEM", "Sucesso ao atualizar foto de perfil");
                        }else{
                            Log.i("IMAGEM", "Falha ao atualizar foto de perfil");
                        }

                        if(meuFundo != null){
                            Glide.with(PerfilFragment.this)
                                    .load(meuFundo)
                                    .centerCrop()
                                    .into(imgFundoUsuario);

                            Log.i("IMAGEM", "Sucesso ao atualizar fundo de perfil");
                        }else{
                            Log.i("IMAGEM", "Falha ao atualizar fundo de perfil");
                        }

                    }else if(snapshot == null) {

                        Toast.makeText(getActivity(), " Conta falta ser cadastrada", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "Por favor termine seu cadastro", Toast.LENGTH_SHORT).show();
                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(), "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
