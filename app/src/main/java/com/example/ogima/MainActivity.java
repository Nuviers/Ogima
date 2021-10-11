package com.example.ogima;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogima.ui.cadastro.InteresseActivity;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView textResultado;
    private Button buttonMostrar;
    private ArrayList<String> arrayLista = new ArrayList<>();
    InteresseActivity interesseActivity = new InteresseActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textResultado = findViewById(R.id.textResultado);
        buttonMostrar = findViewById(R.id.buttonMostrar);


        arrayLista.add("Maça");
        arrayLista.add("Banana");

        //Recebendo dados



    }

    public void exibirDados(View view){
        Bundle dados = getIntent().getExtras();

        String lista = dados.getStringArrayList("listaInteresse").toString();
/*
        for(int i =0; i <arrayLista.size(); i++){
            textResultado.setText(arrayLista.get(0) + arrayLista.get(1));
        }
 */

        textResultado.setText(lista);

       //Verifica se dentro do ArrayList de string possui tal elemento.
        if(lista.contains("Animais")){
            Toast.makeText(getApplicationContext(), "Tem animais", Toast.LENGTH_SHORT).show();
        }

    }


}
