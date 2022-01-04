package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class EpilepsiaActivity extends AppCompatActivity implements View.OnClickListener {


    private Button buttonSim, buttonNao;
    private String epilepsia;

    //
    private Usuario usuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_epilepsia);

        buttonSim = findViewById(R.id.buttonSim);
        buttonNao = findViewById(R.id.buttonNao);

        buttonSim.setOnClickListener(this);
        buttonNao.setOnClickListener(this);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            usuario = (Usuario) dados.getSerializable("dadosUsuario");
        }

    }


    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonSim: {
                    epilepsia = "Sim";
                break;
            }
            case R.id.buttonNao: {
                epilepsia = "Nao";
                break;
            }
        }
        if(epilepsia == "Sim" || epilepsia =="Nao"){
            receberDados();
        }

    }


    private void receberDados(){
        usuario.setEpilepsia(epilepsia);
        Intent intent = new Intent(getApplicationContext(), FotoPerfilActivity.class);
        intent.putExtra("dadosUsuario", usuario);
        intent.putExtra("epilepsiaRecebida", epilepsia);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        //finish();
        }
    }









