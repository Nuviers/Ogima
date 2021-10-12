package com.example.ogima.ui.fragment;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ogima.R;
import com.example.ogima.StatusAdapter;
import com.example.ogima.StatusModel;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class InicioFragment extends Fragment  {

    //


    private RecyclerView recyclerStatus;
    private StatusAdapter statusAdapter;

    ArrayList<StatusModel> statusModels;



    //


    public InicioFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        //Localizando o recyclerStatus
        recyclerStatus = view.findViewById(R.id.recyclerStatus);

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
                view.getContext(), LinearLayoutManager.HORIZONTAL,false
        );
        recyclerStatus.setLayoutManager(layoutManager);
        recyclerStatus.setItemAnimator(new DefaultItemAnimator());


        //Adapter
        statusAdapter = new StatusAdapter(view.getContext(), statusModels);
        recyclerStatus.setAdapter(statusAdapter);


        //



        // Inflate the layout for this fragment

        return view;
    }

}
