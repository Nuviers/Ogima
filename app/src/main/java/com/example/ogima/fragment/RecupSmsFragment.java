package com.example.ogima.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.activity.LoginEmailActivity;
import com.example.ogima.activity.ProblemasLogin;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.InfoUserDAO;
import com.example.ogima.model.Informacoes;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.NumeroActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RecupSmsFragment extends Fragment {

    private EditText editTextNumero, editTextDDIR;
    private Button buttonContinuarNumero;
    private ImageView imageViewFotoUser;
    private String numeroRecuperacao, ddiRecuperacao, numeroCompleto,fotoUsuario, mensagem;
    private TextView textViewMensagem;
    private ProgressBar progressBarRecup;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String usuarioLocalizado;

    private int contadorEnvio;


    public RecupSmsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_recup_sms, container, false);

        editTextNumero = view.findViewById(R.id.editTextNumero);
        editTextDDIR = view.findViewById(R.id.editTextDDIR);
        buttonContinuarNumero = view.findViewById(R.id.buttonContinuarNumero);
        imageViewFotoUser = view.findViewById(R.id.imageViewFotoUser);
        textViewMensagem = view.findViewById(R.id.textViewMensagem);
        progressBarRecup = view.findViewById(R.id.progressBarRecup);


        buttonContinuarNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Número completo com ddi e o número de telefone.
                numeroRecuperacao = editTextNumero.getText().toString();
                ddiRecuperacao = editTextDDIR.getText().toString();

                numeroCompleto = editTextDDIR.getText().toString() + editTextNumero.getText().toString() ;

                if(!numeroRecuperacao.isEmpty() && !ddiRecuperacao.isEmpty()){

                    Toast.makeText(getActivity(), "Numero " + numeroCompleto, Toast.LENGTH_SHORT).show();

                    progressBarRecup.setVisibility(View.VISIBLE);

                    testandoLog();

                }else{
                    textViewMensagem.setText("Por favor insira seu número completo conforme o exemplo: +XX XXXXXXXXX");
                }

            }
        });
        return view;
    }


    public void testandoLog(){

        DatabaseReference usuarioRef = firebaseRef.child("usuarios");

        //Verificando se existe no banco de dados o número inserido
        usuarioRef.orderByChild("numero").equalTo(numeroCompleto).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Procura valores fornecidos pel orderbychild
                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    usuarioLocalizado = snapshot.getChildren().iterator().next().getKey();

                    Toast.makeText(getActivity(), "Identificador " + childDataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getActivity(), "Dado localizado " + childDataSnapshot.child("numero").getValue(), Toast.LENGTH_SHORT).show();
                }

                if(snapshot.exists()){
                    progressBarRecup.setVisibility(View.INVISIBLE);

                    mensagem = "Conta localizada com sucesso";
                    textViewMensagem.setText(mensagem);

                    //Recuperar foto do usuario com outra referencia aqui
                    recuperandoFoto();

                    //Trabalha em função de limitar alterações disparadamente.
                    limiteEnvio();

                }else{
                    Glide.with(RecupSmsFragment.this)
                            .load(R.drawable.avatarfemale)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerCrop()
                            .circleCrop()
                            .into(imageViewFotoUser);

                    progressBarRecup.setVisibility(View.INVISIBLE);
                    mensagem = "Nenhuma conta vinculada ao número informado";
                    textViewMensagem.setText(mensagem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Erro " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void recuperandoFoto(){

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(usuarioLocalizado);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    progressBarRecup.setVisibility(View.INVISIBLE);

                    Usuario usuario = snapshot.getValue(Usuario.class);
                    fotoUsuario = usuario.getMinhaFoto();

                    if (fotoUsuario != null) {

                        Glide.with(RecupSmsFragment.this)
                                .load(fotoUsuario)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .centerCrop()
                                .circleCrop()
                                .into(imageViewFotoUser);
                    } else {
                        Glide.with(RecupSmsFragment.this)
                                .load(R.drawable.avatarfemale)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .centerCrop()
                                .circleCrop()
                                .into(imageViewFotoUser);
                    }

                } else if (snapshot == null) {
                    progressBarRecup.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarRecup.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), "Erro " + error.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void limiteEnvio(){

        InfoUserDAO infoUserDAO = new InfoUserDAO(getActivity());
        Informacoes informacoes = new Informacoes();

        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        //DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss");

        //*Passando a data atual para uma string
        String dataRecuperada = f.format(date);

        //Recuperando dados anteriores caso tenha
        infoUserDAO.recuperar(informacoes);

        //Exibindo dados iniciais ou caso tenha recuperado algum serão exibidos

        //Passando valores do contador do DB para um inteiro
        contadorEnvio = informacoes.getContadorAlteracao();

        //Se contador for igual a 10, verifica se a dataSalva é igual a data atual.
        if(contadorEnvio == 10){
            if(dataRecuperada.equals(informacoes.getDataSalva())){
                //Log.i("INFO DB", "Espere 24 horas");
                textViewMensagem.setText("Conta localizada com sucesso, limite de envios " +
                        "atingido, espere até amanhã para poder alterar novamente!");
                Toast.makeText(getActivity(), "Espere 24 horas", Toast.LENGTH_SHORT).show();
            }else{
                //Se as datas forem diferentes, significa que o dado salvo
                //foi antes da data atual assim, resetar o contador.
                informacoes.setContadorAlteracao(1);
                infoUserDAO.atualizar(informacoes);
            }
        }

        //Se o contador já existir e as datas forem diferentes,
        //ele vai reiniciar o contador.
        if(contadorEnvio != 0){
            if(!dataRecuperada.equals(informacoes.getDataSalva())){
                informacoes.setContadorAlteracao(1);
                informacoes.setDataSalva(dataRecuperada);
                infoUserDAO.atualizar(informacoes);
            }
        }

        if(contadorEnvio >=1 && contadorEnvio < 10){
            contadorEnvio++;
            informacoes.setContadorAlteracao(contadorEnvio);
            informacoes.setDataSalva(dataRecuperada);
            infoUserDAO.atualizar(informacoes); //Atualizando data do servidor

            Intent intent = new Intent(getActivity(), NumeroActivity.class);
            intent.putExtra("alterarSenha", "newPass");
            intent.putExtra("numeroEnviado", numeroRecuperacao);
            intent.putExtra("ddiEnviado", ddiRecuperacao);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }
}

