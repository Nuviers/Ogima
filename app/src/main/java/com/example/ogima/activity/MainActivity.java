package com.example.ogima.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogima.R;
import com.example.ogima.ui.cadastro.FotosActivity;
import com.example.ogima.ui.cadastro.InteresseActivity;
import com.example.ogima.model.StatusModel;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //private RecyclerView recyclerStatus;
    //private StatusAdapter statusAdapter;

    ArrayList<StatusModel> statusModels;

    private TextView textResultado;
    private Button buttonMostrar;
    private ArrayList<String> arrayLista = new ArrayList<>();
    InteresseActivity interesseActivity = new InteresseActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //arrayLista.add("Maça");
        //arrayLista.add("Banana");

        //Recebendo dados

        textResultado = findViewById(R.id.textResultado);
        buttonMostrar = findViewById(R.id.buttonMostrar);


    }

        public static void MyTest(Context context){

            Toast.makeText(context, "Hehe boy", Toast.LENGTH_LONG).show();

        }




}
