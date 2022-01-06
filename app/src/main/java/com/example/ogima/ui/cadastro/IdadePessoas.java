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

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class IdadePessoas extends AppCompatActivity {

    private Button btnContinuarIdade;
    private EditText edt_AnoNascimento;
    Usuario usuario;
    public String dataNascimento;

    //Campo de texto para mensagens de erro.
    private TextView txtMensagemIdade, textViewExemploFormato;
    private String localConvertido;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idade_pessoa);

        btnContinuarIdade = findViewById(R.id.btnContinuarIdade);
        edt_AnoNascimento = findViewById(R.id.edt_AnoNascimento);
        txtMensagemIdade = findViewById(R.id.txtMensagemIdade);
        textViewExemploFormato = findViewById(R.id.textViewExemploFormato);

        //Recebendo Email/Senha/Nome/Apelido
        Bundle dados = getIntent().getExtras();
        usuario = (Usuario) dados.getSerializable("dadosUsuario");

        Locale current = getResources().getConfiguration().locale;

        localConvertido = localConvertido.valueOf(current);
        Toast.makeText(getApplicationContext(), "Está " + current, Toast.LENGTH_SHORT).show();

        try {
            if (localConvertido.equals("pt_BR")) {
                textViewExemploFormato.setText("27/12/2000");
                edt_AnoNascimento.setHint("dd/mm/yyyy");
            } else {
                textViewExemploFormato.setText("2000/12/17");
                edt_AnoNascimento.setHint("yyyy/mm/dd");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

                if (!dataNascimento.isEmpty()) {

                    //Se usúario for do Brasil data será no padrão brasileiro.
                    try {
                        if (localConvertido.equals("pt_BR")) {
                            converterData("dd/MM/uuuu");
                        }
                    } catch (Exception ex) {
                        txtMensagemIdade.setText("Digite a data da seguinte maneira: dd/mm/yyyy");
                        ex.printStackTrace();
                    }

                    //Se usúario não for do Brasil data será no padrão americano.
                    try {
                        if (!localConvertido.equals("pt_BR")) {
                            converterData("uuuu/MM/dd");
                        }
                    } catch (Exception ex) {
                        txtMensagemIdade.setText("Digite a data da seguinte maneira: yyyy/mm/dd");
                        ex.printStackTrace();
                    }
                    //Toast.makeText(getApplicationContext(), " Data inicial " + dataNascimento, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), " Data formatada " + dataFormatada, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), " Idade " + idade(dataNPtbr), Toast.LENGTH_SHORT).show();
                } else {
                    txtMensagemIdade.setText("Insira a sua data de nascimento!");
                }
            }
        });
    }


    private void converterData(String estiloData) {

        DateTimeFormatter formatoData;
        LocalDate dataConvertida;
        String dataFormatada;

        formatoData = DateTimeFormatter.ofPattern(estiloData);
        //Passando a data para variavel LocalDate dataNPtbr com o formato definido
        dataConvertida = LocalDate.parse(dataNascimento, formatoData.withResolverStyle(ResolverStyle.SMART));
        //String da data com formato de data brasileiro
        dataFormatada = formatoData.format(dataConvertida);
        usuario.setDataNascimento(dataFormatada);
        //Chamando o método para calcular a idade e passando a Data como paramêtro
        idade(dataConvertida);
    }

    //Leva os dados para GeneroActivity
    public final void enviarDados(Usuario usuario) {

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
        if (periodo.getYears() < 13) {
            txtMensagemIdade.setText("Idade mínima para cadastro é de 13 anos de idade");

        } else if (periodo.getYears() > 150) {
            txtMensagemIdade.setText("Insira uma data válida");
        } else {
            enviarDados(usuario);
        }

        return periodo.getYears();
    }

    public void voltarIdade(View view) {
        onBackPressed();
    }


}