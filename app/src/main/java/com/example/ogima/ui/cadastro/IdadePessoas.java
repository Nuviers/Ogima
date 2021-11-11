package com.example.ogima.ui.cadastro;


import android.content.Intent;
import android.os.Bundle;
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

public class IdadePessoas extends AppCompatActivity {

    private Button btnContinuarIdade;
    private EditText edt_AnoNascimento;
    Usuario usuario;
    public String dataNascimento;

    //Campo de texto para mensagens de erro.
    private TextView txtMensagemIdade;


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

        Toast.makeText(IdadePessoas.this, "Email "
                + usuario.getEmailUsuario() + " Senha "
                + usuario.getSenhaUsuario() + " Número " + usuario.getNumero()
                + " Nome " + usuario.getNomeUsuario() + " Apelido "
                + usuario.getApelidoUsuario(), Toast.LENGTH_LONG).show();


        btnContinuarIdade.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                //String dataNascimento recebendo o que está dentro do edt_AnoNascimento
                dataNascimento = edt_AnoNascimento.getText().toString();
                try{

                if(!dataNascimento.isEmpty()){

                //Formatando o DateTime para o padrão de data brasileiro
                DateTimeFormatter formatoPtbr = DateTimeFormatter.ofPattern("dd/MM/uuuu");

                //Passando a data para variavel LocalDate dataNPtbr com o formato definido
                LocalDate dataNPtbr = LocalDate.parse(dataNascimento, formatoPtbr.withResolverStyle(ResolverStyle.SMART));

                //String da data com formato de data brasileiro
                String dataFormatada = formatoPtbr.format(dataNPtbr);

                    usuario.setDataNascimento(dataFormatada);

                    //Chamando o método para calcular a idade e passando a Data como paramêtro
                    idade(dataNPtbr);

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

    //Leva os dados para GeneroActivity
    public final void enviarDados(Usuario usuario){

        Intent intent = new Intent(getApplicationContext(), GeneroActivity.class);
        intent.putExtra("dadosUsuario", usuario);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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