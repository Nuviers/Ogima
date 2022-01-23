package com.example.ogima.ui.cadastro;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private FloatingActionButton floatingVoltarEpilepsia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_epilepsia);

        buttonSim = findViewById(R.id.buttonSim);
        buttonNao = findViewById(R.id.buttonNao);
        floatingVoltarEpilepsia = findViewById(R.id.floatingVoltarEpilepsia);

        buttonSim.setOnClickListener(this);
        buttonNao.setOnClickListener(this);
        floatingVoltarEpilepsia.setOnClickListener(this);

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
                epilepsia = "Não";
                break;
            }
            case R.id.floatingVoltarEpilepsia:{
                onBackPressed();
                break;
            }
        }
        if(epilepsia == "Sim" || epilepsia =="Não"){
            receberDados();
        }

    }


    private void receberDados(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecionado " + epilepsia + " para epilepsia");
        builder.setMessage("Você confirma mesmo sua escolha? Uma vez selecionado não há como mudar a escolha.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                usuario.setEpilepsia(epilepsia);
                Intent intent = new Intent(getApplicationContext(), FotoPerfilActivity.class);
                intent.putExtra("dadosUsuario", usuario);
                intent.putExtra("epilepsiaRecebida", epilepsia);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        }

        //Método de volta
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}









