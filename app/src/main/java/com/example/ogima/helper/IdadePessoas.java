package com.example.ogima.helper;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.ApelidoActivity;
import com.example.ogima.ui.cadastro.GeneroActivity;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class IdadePessoas extends AppCompatActivity {


    private Button btnContinuarIdade;
    private EditText edt_AnoNascimento;
    public LocalDate anoN;
    Usuario usuario;

    int ebaNice;

    public String dataNascimento;
    public String dataExtra;

    //Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idade_pessoa);

        btnContinuarIdade = findViewById(R.id.btnContinuarIdade);
        edt_AnoNascimento = findViewById(R.id.edt_AnoNascimento);


        Bundle dados = getIntent().getExtras();
        usuario = (Usuario) dados.getSerializable("dadosUsuario");

        Toast.makeText(IdadePessoas.this, "Email "
                + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                + " Nome " + usuario.getNomeUsuario() + " Apelido "
                + usuario.getApelidoUsuario(), Toast.LENGTH_LONG).show();



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
               //* anoN = edt_AnoNascimento.getText().toString();

                // data/hora atual
                //LocalDateTime agora = LocalDateTime.now();

                // formatar a data
                //DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/uuuu");
                //String dataFormatada = formatterData.format(agora);

/////////////////////////________________________________________






/////////////////////////________________________________________


//*************************************************************************************//

                //String dataS recebendo o que está dentro do edt_AnoNascimento
                dataNascimento = edt_AnoNascimento.getText().toString();

                //Formatando o DateTime para o padrão de data brasileiro
                DateTimeFormatter formatoPtbr = DateTimeFormatter.ofPattern("dd-MM-uuuu");

                //A data de nascimento do usuário é o dataNPtbr
                LocalDate dataNPtbr = LocalDate.parse(dataNascimento, formatoPtbr);

                //String da data com formato de data brasileiro
                String dataExemplo = formatoPtbr.format(dataNPtbr);

                //Chamando o método para calcular a idade e passando a Data como paramêtro
                idade(dataNPtbr);







                //String dataExtra recebendo
                //dataExtra = dataS;


                //anoN =

                //*LocalDate data = LocalDate.parse(dataS);
                //LocalDate data = LocalDate.parse(dataExtra);

                //DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu");
                //String dataConvertida = fmt.format(data);
                //*DateTimeFormatter formataEn = DateTimeFormatter.ofPattern("uuuu-MM-dd");
                //*String dataEn = formataEn.format(data);

                //DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu");
               // String dataConvertida = fmt.format(data);

                //idade(data);


                //Toast.makeText(getApplicationContext(), " Teste formatoEx " + dataExtra, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), " Teste formatoEx " + dataNascimento, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), " Teste formatoExe " + dataExemplo, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), " Teste formatoIda " + idade(dataNPtbr), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), " Data usuario " + usuario.getIdadeTeste(), Toast.LENGTH_SHORT).show();

                //*Toast.makeText(getApplicationContext(), " Teste formato " + dataEn, Toast.LENGTH_SHORT).show();
                //*Toast.makeText(getApplicationContext(), " Teste formato " + dataConvertida, Toast.LENGTH_SHORT).show();
                //*Toast.makeText(getApplicationContext(), " Teste idade " + idade(data), Toast.LENGTH_SHORT).show();
                //*Toast.makeText(getApplicationContext(), " Teste data " + data, Toast.LENGTH_SHORT).show();


























//*************************************************************************************//


                //*LocalDate hoje = LocalDate.now();

                //Formata o modelo da data
                //*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                //Converte date para string
                //*String hojeFormatado = hoje.format(formatter);

                //Converte String para date
                //LocalDate agoraParseado = LocalDate.parse(hojeFormatado, formatter);


                //Toast.makeText(getApplicationContext(), " Oiii " + hoje, Toast.LENGTH_SHORT).show();
                ////////////////////////////////////////////////// De cima é so teste

                //Pegando o ano de nascimento digitado pelo usuario


                //Convertendo a data String em inteiro e armazenando na variavel anoAtual
                //*int anoAtual = Integer.parseInt(hojeFormatado);

                //Convertendo ano de nascimento em inteiro
                //*int anoNascimento = Integer.parseInt(anoN);

                //*int idade = anoAtual - anoNascimento;

                //*String seila = String.valueOf(idade);

                //Usuario usuario = new Usuario();
                //usuario.setIdadeTeste(seila);

                //*Toast.makeText(getApplicationContext(), "Sua idade é: " +
                //*seila, Toast.LENGTH_SHORT).show();


                //////////////////////////////////////////////////////

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

    public final void enviarDados(Usuario usuario){

        //Recebendo Email/Senha/Nome/Apelido



        Intent intent = new Intent(IdadePessoas.this, GeneroActivity.class);
        intent.putExtra("dadosUsuario", usuario);
        startActivity(intent);
    }

    public int idade(final LocalDate aniversario) {
        final LocalDate dataAtual = LocalDate.now(ZoneId.systemDefault());
        final Period periodo = Period.between(aniversario, dataAtual);


        usuario.setIdade(periodo.getYears());


        enviarDados(usuario);

        return periodo.getYears();
    }





}