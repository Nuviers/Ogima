package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.ogima.R;
import com.example.ogima.fragment.parc.InteressesParceirosFragment;
import com.example.ogima.fragment.parc.NomeParcFragment;
import com.example.ogima.fragment.parc.OpcoesExibirPerfilParcFragment;
import com.example.ogima.fragment.parc.OrientacaoSexualParcFragment;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.model.Usuario;

public class EdicaoGeralParcActivity extends AppCompatActivity implements DataTransferListener {

    private FrameLayout frameLayoutEditParc;
    private String tipoEdicao = "";
    private Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_geral_parc);
        inicializandoComponentes();
        Bundle dados = getIntent().getExtras();
        if (dados != null
                && dados.containsKey("tipoEdicao")) {
            tipoEdicao = dados.getString("tipoEdicao");
            alterarLayoutPorTipo();
        }
    }

    private void alterarLayoutPorTipo(){
       fragment = null;
        switch (tipoEdicao){
            case "nome":
                fragment = new NomeParcFragment();
                break;
            case "exibirPara":
                fragment = new OpcoesExibirPerfilParcFragment();
                break;
            case "orientacao":
                fragment = new OrientacaoSexualParcFragment();
                break;
            case "interesses":
                fragment = new InteressesParceirosFragment();
                break;
        }

        if (fragment != null) {
            Bundle bundle = new Bundle();
            bundle.putString("nomeEdit", "");
            fragment.setArguments(bundle);
            replaceFragment(fragment);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutEditParc, fragment);
        fragmentTransaction.commit();
    }

    private void inicializandoComponentes(){
        frameLayoutEditParc = findViewById(R.id.frameLayoutEditParc);
    }

    @Override
    public void onUsuarioParc(Usuario usuarioParc, String etapa) {
        if (fragment instanceof NomeParcFragment) {
            //Enviar por intent o nome.
            //Repetir o mesmo processo com todos os dados.
        }
    }
}