package com.example.ogima.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.CortaImagemActivity;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.example.ogima.ui.cadastro.InteresseActivity;
import com.example.ogima.ui.cadastro.NumeroActivity;
import com.example.ogima.ui.intro.IntrodActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private TextView txtDeslogar, textTeste;
    private GoogleSignInClient mSignInClient;
    private ImageView imgFotoUsuario, imgFundoUsuario, imageViewGif, imageBorda;

    private String urlGifTeste = "";

    private Button buttonEditarPerfil, buttonVincularNumero, buttonDesvincularNumero;
    private ImageButton imageButtonEditar;
    private Usuario usuario;

    private String minhaFoto;
    private String meuFundo;
    private String apelido, nome;
    private Button buttonTelaCortar;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

            //testandoLog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

         txtDeslogar = view.findViewById(R.id.txtDeslogar);
         imageViewGif = view.findViewById(R.id.imageViewGif);
         imageBorda = view.findViewById(R.id.imageBorda);
         imgFundoUsuario = view.findViewById(R.id.imgFundoUsuario);
         textTeste = view.findViewById(R.id.textTeste);
         buttonEditarPerfil = view.findViewById(R.id.buttonEditarPerfil);
         imageButtonEditar = view.findViewById(R.id.imageButtonEditar);

         buttonVincularNumero = view.findViewById(R.id.buttonVincularNumero);
         buttonDesvincularNumero = view.findViewById(R.id.buttonDesvincularNumero);

        buttonTelaCortar = view.findViewById(R.id.buttonTelaCortar);

        try{
            testandoLog();
        }
        catch (Exception e){
            e.printStackTrace();
        }

         txtDeslogar.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 usuario = new Usuario();

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

                     onBackPressed();

             }
         });

        imageButtonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


        buttonEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


        buttonVincularNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), NumeroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("vincularNumero", "vincularN");
                startActivity(intent);

            }
        });

        buttonDesvincularNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desvincularNumero();
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
        //String numerro = "+5541997290614";
        //DatabaseReference numeroRef = firebaseRef.child("usuarios").child(idUsuario).child("numero");


        //numeroRef.setValue(numerro);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    //Log.i("FIREBASE", usuario.getIdUsuario());
                    //Log.i("FIREBASEA", usuario.getNomeUsuario());
                    nome = usuario.getNomeUsuario();
                    apelido = usuario.getApelidoUsuario();
                    meuFundo = usuario.getMeuFundo();
                    minhaFoto = usuario.getMinhaFoto();

                    if(emailUsuario != null){
                        //Toast.makeText(getActivity(), " Okay", Toast.LENGTH_SHORT).show();
                        try{
                            textTeste.setText(nome);

                            if(minhaFoto != null){

                                Glide.with(PerfilFragment.this)
                                        .load(minhaFoto)
                                        .placeholder(R.drawable.passarowhite)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageBorda);

                                Log.i("IMAGEM", "Sucesso ao atualizar foto de perfil");
                            }else{

                                Glide.with(PerfilFragment.this)
                                        .load(R.drawable.secretarybirdpicture)
                                        .placeholder(R.drawable.passarowhite)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageBorda);

                                Log.i("IMAGEM", "Falha ao atualizar foto de perfil");
                            }

                            if(meuFundo != null){
                                Glide.with(PerfilFragment.this)
                                        .load(meuFundo)
                                        .placeholder(R.drawable.placeholderuniverse)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imgFundoUsuario);

                                Log.i("IMAGEM", "Sucesso ao atualizar fundo de perfil");
                            }else{

                                Glide.with(PerfilFragment.this)
                                        .load(R.drawable.placeholderuniverse)
                                        .placeholder(R.drawable.placeholderuniverse)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imgFundoUsuario);
                                Log.i("IMAGEM", "Falha ao atualizar fundo de perfil");
                            }
                            usuarioRef.removeEventListener(this);
                        }catch (Exception ex){
                            ex.printStackTrace();
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

    private void desvincularNumero() {

        List<? extends UserInfo> providerData = autenticacao.getCurrentUser().
                getProviderData();

        for (UserInfo userInfo : providerData ) {

            String providerId = userInfo.getProviderId();

            if(providerId.equals("phone")){
                alertaDesvinculacao();
                //break;
            }else{
                //Toast.makeText(getActivity(), "Não existe número de telefone vinculado a essa conta", Toast.LENGTH_SHORT).show();
                //break;
            }
        }
    }

    private void alertaDesvinculacao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Deseja desvincular seu número de telefone?");
        builder.setMessage("Para sua segurança, aconselhamos que vincule posteriormente outro número a sua conta");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                autenticacao.getCurrentUser().unlink("phone").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            autenticacao.getCurrentUser().reload();
                            String emailUsuario = autenticacao.getCurrentUser().getEmail();
                            String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                            DatabaseReference numeroRef = firebaseRef.child("usuarios").child(idUsuario).child("numero");
                            numeroRef.setValue("desvinculado");
                            Toast.makeText(getActivity(), "Desvinculado", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), "Erro ao desvincular", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Cancelar",null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onBackPressed() {
        // Método para retorno
        Intent intent = new Intent(getActivity(), IntrodActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        getActivity().finish();
    }

}

