package com.example.ogima.ui.cadastro;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.model.Usuario;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class IdadePessoas extends AppCompatActivity {

    private Button btnContinuarIdade;
    private EditText edt_AnoNascimento;
    Usuario usuario;
    public String dataNascimento;

    //Campo de texto para mensagens de erro.
    private TextView txtMensagemIdade;
    private String localConvertido;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idade_pessoa);

        btnContinuarIdade = findViewById(R.id.btnContinuarIdade);
        edt_AnoNascimento = findViewById(R.id.edt_AnoNascimento);
        txtMensagemIdade = findViewById(R.id.txtMensagemIdade);

        //Recebendo Email/Senha/Nome/Apelido
        Bundle dados = getIntent().getExtras();
        usuario = (Usuario) dados.getSerializable("dadosUsuario");

        TelephonyManager tm = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        }
        String countryCodeValue = tm.getSimCountryIso();
        Locale current = getResources().getConfiguration().locale;

        localConvertido = localConvertido.valueOf(current);
        Toast.makeText(getApplicationContext(), "Isso " + countryCodeValue, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "Está " + current, Toast.LENGTH_SHORT).show();


        /*
        Toast.makeText(IdadePessoas.this, "Email "
                + usuario.getEmailUsuario() + " Senha "
                + usuario.getSenhaUsuario() + " Número " + usuario.getNumero()
                + " Nome " + usuario.getNomeUsuario() + " Apelido "
                + usuario.getApelidoUsuario(), Toast.LENGTH_LONG).show();
         */

        btnContinuarIdade.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                //String dataNascimento recebendo o que está dentro do edt_AnoNascimento
                dataNascimento = edt_AnoNascimento.getText().toString();
                try{

                if(!dataNascimento.isEmpty()){

                //Formatando o DateTime para o padrão de data brasileiro
                    if (localConvertido.startsWith("br")) {
                        DateTimeFormatter formatoPtbr = DateTimeFormatter.ofPattern("dd/MM/uuuu");

                        //Passando a data para variavel LocalDate dataNPtbr com o formato definido
                        LocalDate dataNPtbr = LocalDate.parse(dataNascimento, formatoPtbr.withResolverStyle(ResolverStyle.SMART));

                        //String da data com formato de data brasileiro
                        String dataFormatada = formatoPtbr.format(dataNPtbr);

                        usuario.setDataNascimento(dataFormatada);

                        //Chamando o método para calcular a idade e passando a Data como paramêtro
                        idade(dataNPtbr);
                    }

                    if (localConvertido.startsWith("en")) {


                        try{

                            //Colocar programaticamente a mask app:mask="##/##/####"

                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        DateTimeFormatter formatoEn = DateTimeFormatter.ofPattern("uuuu/MM/dd");

                        //Passando a data para variavel LocalDate dataNPtbr com o formato definido
                        LocalDate dataEn = LocalDate.parse(dataNascimento, formatoEn.withResolverStyle(ResolverStyle.SMART));

                        //String da data com formato de data brasileiro
                        String dataFormatada = formatoEn.format(dataEn);

                        usuario.setDataNascimento(dataFormatada);

                        //Chamando o método para calcular a idade e passando a Data como paramêtro
                        idade(dataEn);
                    }

                    //Toast.makeText(getApplicationContext(), " Data inicial " + dataNascimento, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), " Data formatada " + dataFormatada, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), " Idade " + idade(dataNPtbr), Toast.LENGTH_SHORT).show();
                }

                else {
                        txtMensagemIdade.setText("Insira a data conforme o padrão dd/mm/yyyy");
                }

                } catch (Exception e){
                    txtMensagemIdade.setText("Digite a data da seguinte maneira: dd/mm/yyyy");
                }

            }
        });


    }

    private String MaskedFormatter(String your_mask) {
      your_mask = "####/##/##";
        return your_mask;
    }

    //Leva os dados para GeneroActivity
    public final void enviarDados(Usuario usuario){

        Intent intent = new Intent(getApplicationContext(), GeneroActivity.class);
        //Intent intent = new Intent(getApplicationContext(), NumeroActivity.class);
        //Intent intent = new Intent(getApplicationContext(), RecuperarUIActivity.class);
        intent.putExtra("dadosUsuario", usuario);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        //finish();
    }

    //Calcula a idade da pessoa
    public int idade(final LocalDate aniversario) {
        final LocalDate dataAtual = LocalDate.now(ZoneId.systemDefault());
        final Period periodo = Period.between(aniversario, dataAtual);

        usuario.setIdade(periodo.getYears());

        //Restrição de idade
        if(periodo.getYears() < 13){
            txtMensagemIdade.setText("Idade mínima para cadastro é de 13 anos de idade");

        }else if(periodo.getYears() > 150){
            txtMensagemIdade.setText("Insira uma data válida");
        } else{
            enviarDados(usuario);
        }

        return periodo.getYears();
    }

    public void voltarIdade (View view){
        onBackPressed();
    }


}