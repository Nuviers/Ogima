package com.example.ogima;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ogima.ui.cadastro.InteresseActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerStatus;
    private StatusAdapter statusAdapter;

    ArrayList<StatusModel> statusModels;

    private TextView textResultado;
    private Button buttonMostrar;
    private ArrayList<String> arrayLista = new ArrayList<>();
    InteresseActivity interesseActivity = new InteresseActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_inicio);

        //arrayLista.add("Maça");
        //arrayLista.add("Banana");

        //Recebendo dados

        textResultado = findViewById(R.id.textResultado);
        buttonMostrar = findViewById(R.id.buttonMostrar);

        //Localizando o recyclerStatus
        recyclerStatus = findViewById(R.id.recyclerStatus);

        //Array pictures && videos for Status
        Integer[] resourceStatus = {R.drawable.boywomam,R.drawable.testewomam,
        R.drawable.testeboy, R.drawable.testeboyblue, R.drawable.testeboytres,
        R.drawable.testeboyy, R.drawable.testewomamtwo};

        //Array name for Status
        String[] nameUser = {"Mario","Laura","Marcelo","Gray","Cal",
        "Rick","Steve"};


        //Inicializar ArrayList
        statusModels = new ArrayList<>();
        for (int i =0; i <resourceStatus.length; i++){
            StatusModel model = new StatusModel(resourceStatus[i], nameUser[i]);
            statusModels.add(model);
        }

        //Design HorizontalScrollView
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                MainActivity.this, LinearLayoutManager.HORIZONTAL,false
        );
        recyclerStatus.setLayoutManager(layoutManager);
        recyclerStatus.setItemAnimator(new DefaultItemAnimator());


        //Adapter
        statusAdapter = new StatusAdapter(MainActivity.this, statusModels);
        recyclerStatus.setAdapter(statusAdapter);
    }




}
