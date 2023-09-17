package com.example.ogima.helper;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.model.Informacoes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataHoraAtualizado extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private int contador;
    private String apelido;
    private int contadorA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    public static void novaData(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        LocalDateTime agora = LocalDateTime.now();

        // formatar a data
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");
        String dataFormatada = formatterData.format(agora);

        // formatar a hora
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaFormatada = formatterHora.format(agora);

        DatabaseReference dataNova = firebaseRef.child("dataAtual");
        //dataNova.setValue(dataAtual);

        //Teste
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataComparada = LocalDate.parse("23/11/2015", formato);

        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        DateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        //DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss");
        String meuTeste = f.format(date);


        dataNova.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    dataNova.setValue(meuTeste);

                    //dataNova.setValue(agora);
/*
                    if(dataComparada.getYear() > agora.getYear()){
                        Log.i("DATAA", "Data comparada é maior");

                    }else{
                        Log.i("DATAA", "Data comparada é menor");
                    }
 */
                }else{


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void contadorAlteracao(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference contadorLimite = firebaseRef.child("contadorLimite");

        int cont = 1;

        contadorLimite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.getValue() != null) {

                        if (snapshot.getValue().equals(1)) {
                            contadorLimite.setValue(cont + cont);
                        } else {
                            contadorLimite.setValue(cont);
                        }
                    } else {

                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                try{
                    Log.i("Erro Cancelled", error.getMessage());

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    public void armazenarContadorDois(){

        InfoUserDAO infoUserDAO = new InfoUserDAO(getApplicationContext());
        Informacoes informacoes = new Informacoes();

        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        //DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss");
        String meuTeste = f.format(date);
        //String meuTeste = "11/12/2021";

        infoUserDAO.recuperar(informacoes);

        Log.i("INFO DB", "MeuId " + informacoes.getId());
        Log.i("INFO DB", "MeuContador " + informacoes.getContadorAlteracao());
        Log.i("INFO DB", "MinhaData " + informacoes.getDataSalva());

        contadorA = informacoes.getContadorAlteracao();

        // Se a data salva é igual a data atual
        if(meuTeste.equals(informacoes.getDataSalva())){
            //Se o dado já existir
            if(contadorA >=1 && contadorA < 5){
                contadorA++;
                informacoes.setContadorAlteracao(contadorA);
                informacoes.setDataSalva(meuTeste);
                infoUserDAO.atualizar(informacoes); //Atualizando data do servidor
            }

            // Se a data salva é diferente da data atual
        }else{
            informacoes.setContadorAlteracao(10);
            informacoes.setDataSalva(meuTeste);
            infoUserDAO.atualizar(informacoes); //Atualizando data do servidor
        }

        if(contadorA == 0){
            informacoes.setContadorAlteracao(1);
            informacoes.setDataSalva(meuTeste); //Salvando data do servidor
            infoUserDAO.salvar(informacoes);
            Log.i("INFO DB", "Adicionado valor 1");
            Log.i("INFO DB", "Adicionado data " + informacoes.getDataSalva());
        }else if(contadorA == 10){
            informacoes.setContadorAlteracao(1);
            informacoes.setDataSalva(meuTeste); //Salvando data do servidor
            infoUserDAO.atualizar(informacoes);
        }

        infoUserDAO.recuperar(informacoes);

        if(contadorA == 5){
            if(!meuTeste.equals(informacoes.getDataSalva())){
                informacoes.setContadorAlteracao(1);
                informacoes.setDataSalva(meuTeste);
                infoUserDAO.atualizar(informacoes);
            }
        }

        Log.i("INFO DB", "Testeid " + informacoes.getId());
        Log.i("INFO DB", "Testecontador " + informacoes.getContadorAlteracao());
        Log.i("INFO DB", "Testedata " + informacoes.getDataSalva());
    }

    public void testandoLog(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario).child("contadorAlteracao");
        //String numerro = "+5541997290614";
        //DatabaseReference numeroRef = firebaseRef.child("usuarios").child(idUsuario).child("numero");


        //numeroRef.setValue(numerro);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){

                    if(snapshot.getValue().equals(32)){
                        usuarioRef.setValue(1);
                    }

                    }else if(snapshot == null) {


                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }



}
