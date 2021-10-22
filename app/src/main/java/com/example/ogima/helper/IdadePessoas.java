package com.example.ogima.helper;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import java.time.LocalDate;

public class IdadePessoas extends AppCompatActivity {


    private Button btnContinuarIdade;
    private EditText edt_AnoNascimento;
    String anoN;
    //Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idade_pessoa);

        btnContinuarIdade = findViewById(R.id.btnContinuarIdade);
        edt_AnoNascimento = findViewById(R.id.edt_AnoNascimento);



        btnContinuarIdade.setOnClickListener(new View.OnClickListener() {

//////////////////////////////

            //Calendar c = Calendar.getInstance();
           // SimpleDateFormat formataData = new SimpleDateFormat("yyyy");
            //Date data = new Date();

//////////////////////////// Essa poha de localdatetime da erro tentar arrumar, e um jeito de bloquear a data mutavel do simpledateformat

           // LocalDateTime agora = LocalDateTime.now(); Esse pega o time também

            //LocalDate agora = LocalDate.now(); esse pega só a data

            //DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");


            //String dataFormatada = formatterData.format(agora);

            @Override
            public void onClick(View view) {


                // data/hora atual
                //LocalDateTime agora = LocalDateTime.now();

                // formatar a data
                //DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/uuuu");
                //String dataFormatada = formatterData.format(agora);


                LocalDate hoje = LocalDate.now();



                Toast.makeText(getApplicationContext(), " Oiii " + hoje, Toast.LENGTH_SHORT).show();
                ////////////////////////////////////////////////// De cima é so teste


                //Pegando o ano digitado pelo usuario
                //anoN = edt_AnoNascimento.getText().toString();

                //Convertendo a data String em inteiro
                //int anoAtual = Integer.parseInt(dataFormatada);

                //Convertendo o valor digitado do ano em um inteiro
                //int anoNascimento = Integer.parseInt(anoN);

                //Recupera a idade do usuario
                //int idade = anoAtual - anoNascimento;

               // Toast.makeText(getApplicationContext(),"Ano Nascimento " + anoNascimento
                    //    + "Ano Atual " + anoAtual + " idade " + idade, Toast.LENGTH_SHORT).show();



            }
        });


    }


}