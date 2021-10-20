package com.example.ogima.ui.cadastro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class NascimentoActivity extends AppCompatActivity {


    private Button btnContinuarNascimento;
    private EditText editNascimento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_nascimento);

        //getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnContinuarNascimento = findViewById(R.id.btnContinuarNascimento);
        editNascimento = findViewById(R.id.edt_AnoNascimento);

        //Configurando lista de seleção para o campo data


        /*Faz com que o botão fique desabilitado, faça um método
        que depois de atender a validação habilite ele e mude de cor
         */

        btnContinuarNascimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               //final Calendar calendar = Calendar.getInstance();
                //final int day = calendar.get(Calendar.DAY_OF_MONTH);
                //final int month = calendar.get(Calendar.MONTH);
                //final int year = calendar.get(Calendar.YEAR);

/*
                //Configurando a seleção de datas (Deixar um ano mais próximo e bloquear data mt perto)
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        NascimentoActivity.this, android.R.style.Theme_DeviceDefault_Dialog,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                editNascimento.setText(day + "/" + (month +1) + "/" + year);
                            }
                        }, day, month, year);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-1000);
                datePickerDialog.show();

 */
                //@String textoNascimento = editNascimento.getText().toString();

                //Bundle receberNome = getIntent().getExtras();
                //String nomeRecebido = receberNome.getString("nomeMeu");

                //@ if(!textoNascimento.isEmpty()){

                //@ int idade = Integer.parseInt(textoNascimento);

                    //Recebendo Email/Senha/Nome/Apelido
                //@ Bundle dados = getIntent().getExtras();
                //@  Usuario usuario = (Usuario) dados.getSerializable("dadosUsuario");

                //@  Toast.makeText(NascimentoActivity.this, "Email "
                //@     + usuario.getEmailUsuario() + " Senha " + usuario.getSenhaUsuario()
                //@      + " Nome " + usuario.getNomeUsuario() + " Apelido "
                //@       + usuario.getApelidoUsuario(), Toast.LENGTH_LONG).show();

                    //Toast.makeText(getApplicationContext(), "Seu nome é esse ai " + nomeRecebido, Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(NascimentoActivity.this, GeneroActivity.class));

                    //Enviando dados
                //@ usuario.setIdade(idade);

                //@ Intent intent = new Intent(NascimentoActivity.this, GeneroActivity.class);
                //@ intent.putExtra("dadosUsuario", usuario);
                //@  startActivity(intent);

                //@ }else{
                //@  Toast.makeText(NascimentoActivity.this,"Digite sua data de nascimento", Toast.LENGTH_SHORT).show();
                }
            //@ }
         });



    }



    public void voltarNascimento (View view){
        onBackPressed();
    }




}


