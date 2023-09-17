package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.ogima.R;
import com.example.ogima.fragment.parc.EsconderPerfilParcFragment;
import com.example.ogima.fragment.parc.FotosParceirosFragment;
import com.example.ogima.fragment.parc.InteressesParceirosFragment;
import com.example.ogima.fragment.parc.NomeParcFragment;
import com.example.ogima.fragment.parc.OpcoesExibirPerfilParcFragment;
import com.example.ogima.fragment.parc.OrientacaoSexualParcFragment;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;

import java.util.ArrayList;

public class EdicaoGeralParcActivity extends AppCompatActivity implements DataTransferListener {

    private FrameLayout frameLayoutEditParc;
    private String tipoEdicao = "";
    private Fragment fragment = null;
    private String idUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_geral_parc);
        inicializandoComponentes();
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        Bundle dados = getIntent().getExtras();
        if (dados != null
                && dados.containsKey("tipoEdicao")) {
            tipoEdicao = dados.getString("tipoEdicao");
            alterarLayoutPorTipo();
        }
    }

    private void alterarLayoutPorTipo() {
        fragment = null;
        switch (tipoEdicao) {
            case "nome":
                fragment = new NomeParcFragment();
                bundleNome();
                break;
            case "exibirPara":
                fragment = new OpcoesExibirPerfilParcFragment();
                bundleExibirPara();
                break;
            case "orientacao":
                fragment = new OrientacaoSexualParcFragment();
                bundleOrientacao();
                break;
            case "interesses":
                fragment = new InteressesParceirosFragment();
                bundleHobbies();
                break;
            case "idsEsconder":
                fragment = new EsconderPerfilParcFragment();
                bundleIdsEsconder();
                break;
            case "fotos":
                fragment = new FotosParceirosFragment();
                bundleFotos();
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutEditParc, fragment);
        fragmentTransaction.commit();
    }

    private void bundleNome() {
        if (fragment != null) {
            ParceiroUtils.recuperarNome(idUsuario, new ParceiroUtils.RecuperarNomeCallback() {
                @Override
                public void onRecuperado(String nome) {
                    bundleString("edit", nome);
                }

                @Override
                public void onSemDados() {

                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void bundleExibirPara() {
        ParceiroUtils.recuperarExibirPerfilAlvo(idUsuario, new ParceiroUtils.RecuperarExibirPerfilAlvoCallback() {
            @Override
            public void onRecuperado(String exibirPerfilPara) {
                bundleString("edit", exibirPerfilPara);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void bundleOrientacao() {
        ParceiroUtils.recuperarOrientacao(idUsuario, new ParceiroUtils.RecuperarOrientacaoCallback() {
            @Override
            public void onRecuperado(String orientacaoSexual) {
                bundleString("edit", orientacaoSexual);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void bundleHobbies() {
        ParceiroUtils.recuperarHobbies(idUsuario, new ParceiroUtils.RecuperarHobbiesCallback() {
            @Override
            public void onRecuperado(ArrayList<String> listaHobbies) {
                bundleArrayList("edit", listaHobbies);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void bundleIdsEsconder(){
        ParceiroUtils.recuperarIdsEscondidos(idUsuario, new ParceiroUtils.RecuperarIdsEscondidosCallback() {
            @Override
            public void onRecuperado(ArrayList<String> idsEscondidos) {
                bundleArrayList("edit", idsEscondidos);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void bundleFotos(){
        ParceiroUtils.recuperarFotos(idUsuario, new ParceiroUtils.RecuperarFotosCallback() {
            @Override
            public void onRecuperado(ArrayList<String> listaFotos) {
                bundleArrayList("edit", listaFotos);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void bundleString(String key, String conteudo) {
        Bundle bundle = new Bundle();
        bundle.putString(key, conteudo);
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }

    private void bundleArrayList(String key, ArrayList<String> lista){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(key, lista);
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }

    private void inicializandoComponentes() {
        frameLayoutEditParc = findViewById(R.id.frameLayoutEditParc);
    }

    @Override
    public void onUsuarioParc(Usuario usuarioParc, String etapa) {
        if (fragment instanceof NomeParcFragment) {
            //Enviar por intent o nome.
            //Repetir o mesmo processo com todos os dados para a activity EditarPerfilParc.
        }
    }
}