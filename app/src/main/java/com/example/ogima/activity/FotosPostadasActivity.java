package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterFotosPostadas;
import com.example.ogima.adapter.AdapterSeguidores;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FotosPostadasActivity extends AppCompatActivity {

    private ImageView imgViewFt1,imgViewFt2,imgViewFt3,imgViewFt4,imgViewFt5;
    private String emailUsuario, idUsuario;
    private Usuario usuarioFotos;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    //Variaveis do recycler
    private RecyclerView recyclerFotosPostadas;
    private AdapterFotosPostadas adapterFotosPostadas;
    private List<Usuario> listaFotosPostadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos_postadas);
        inicializarComponentes();
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurações iniciais
        recyclerFotosPostadas.setLayoutManager(new LinearLayoutManager(this));
        listaFotosPostadas = new ArrayList<>();
        adapterFotosPostadas = new AdapterFotosPostadas(listaFotosPostadas, getApplicationContext());
        recyclerFotosPostadas.setAdapter(adapterFotosPostadas);
        recyclerFotosPostadas.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        recyclerFotosPostadas.setHasFixedSize(true);

        DatabaseReference fotosUsuarioRef = firebaseRef.child("fotosUsuario")
                .child(idUsuario);

        fotosUsuarioRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    try{
                        usuarioFotos = snapshot.getValue(Usuario.class);
                        adapterFotosPostadas.notifyDataSetChanged();
                        if(usuarioFotos.getContadorFotos() >= 4){
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(0) ,imgViewFt1, android.R.color.transparent);
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(1) ,imgViewFt2, android.R.color.transparent);
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(2) ,imgViewFt3, android.R.color.transparent);
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(3) ,imgViewFt4, android.R.color.transparent);
                        }

                        else if(usuarioFotos.getContadorFotos() == 1){
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(0) ,imgViewFt1, android.R.color.transparent);
                        }
                        else if (usuarioFotos.getContadorFotos() == 2){
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(0) ,imgViewFt1, android.R.color.transparent);
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(1) ,imgViewFt2, android.R.color.transparent);
                        }
                        else if (usuarioFotos.getContadorFotos() == 3){
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(0) ,imgViewFt1, android.R.color.transparent);
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(1) ,imgViewFt2, android.R.color.transparent);
                            //GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(2) ,imgViewFt3, android.R.color.transparent);
                        }

                        else if (usuarioFotos.getContadorFotos() <=0){
                            //ToastCustomizado.toastCustomizadoCurto("Sem fotos", getApplicationContext());
                        }

                        ArrayList<String> listaData = new ArrayList<>();
                        ArrayList<Long> listaLong = new ArrayList<>();
                        listaData = usuarioFotos.getListaDatasFotos();
                        listaLong = usuarioFotos.getDatasFotosPostadas();

                        Comparator<Long> comparator = Collections.reverseOrder();
                        Collections.sort(listaLong, comparator);

                        Comparator<String> comparator2 = Collections.reverseOrder();
                        Collections.sort(listaData, comparator2);

                        //Funcionou a ordenação
                        //Elaborar uma lógica em outro for ou nesse mesmo
                        //e fazer uma relação entre eles pra exibir as data de acordo
                        //com os long.
                        for(int i = 0; i < listaLong.size(); i ++){
                            listaFotosPostadas.add(usuarioFotos);
                            //ToastCustomizado.toastCustomizado("Ordem " + listaLong.get(i),getApplicationContext());
                            ToastCustomizado.toastCustomizado("Data " + listaData.get(i),getApplicationContext());
                            //ToastCustomizado.toastCustomizado("Valor i " + i,getApplicationContext());
                            //ToastCustomizado.toastCustomizado("Size " + listaData.size(),getApplicationContext());

                            /*
                            if(usuarioFotos.getContadorFotos() > 0){
                                if(i == 0){
                                    //Lógica para pegar a última foto adicionada
                                    GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(listaData.size()-1) ,imgViewFt1, android.R.color.transparent);
                                }
                                else if (i == 1){
                                    GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(i) ,imgViewFt2, android.R.color.transparent);
                                }
                                else if (i == 2){
                                    GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(i) ,imgViewFt3, android.R.color.transparent);
                                }
                                else if (i >= 3){
                                    GlideCustomizado.montarGlideFoto(getApplicationContext(), usuarioFotos.getListaFotosUsuario().get(i) ,imgViewFt4, android.R.color.transparent);
                                }
                            }else{
                                ToastCustomizado.toastCustomizadoCurto("Sem fotos", getApplicationContext());
                            }
                              */
                            if(i == listaData.size() - 1){
                                ToastCustomizado.toastCustomizado("Stop",getApplicationContext());
                                break;
                            }

                        }



                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                fotosUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        //imgViewFt1 = findViewById(R.id.imgViewFt1);
        //imgViewFt2 = findViewById(R.id.imgViewFt2);
        //imgViewFt3 = findViewById(R.id.imgViewFt3);
        //imgViewFt4 = findViewById(R.id.imgViewFt4);
        //imgViewFt5 = findViewById(R.id.imgViewFt5);
        recyclerFotosPostadas = findViewById(R.id.recyclerViewFotosPostadas);
    }
}