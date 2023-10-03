package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.ogima.R;
import com.example.ogima.fragment.cad.GeneroFragment;
import com.example.ogima.fragment.cad.InteressesFragment;
import com.example.ogima.fragment.cad.NomeFragment;
import com.example.ogima.fragment.parc.EsconderPerfilParcFragment;
import com.example.ogima.fragment.parc.FotosParceirosFragment;
import com.example.ogima.fragment.parc.InteressesParceirosFragment;
import com.example.ogima.fragment.parc.NomeParcFragment;
import com.example.ogima.fragment.parc.OpcoesExibirPerfilParcFragment;
import com.example.ogima.fragment.parc.OrientacaoSexualParcFragment;
import com.example.ogima.helper.DadosUserUtils;
import com.example.ogima.helper.DataCadListener;
import com.example.ogima.helper.LockedViewPager;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;

import java.util.ArrayList;

public class EdicaoCadActivity extends AppCompatActivity implements DataCadListener {

    private String idUsuario = "";
    private FrameLayout frameLayoutEdit;
    private String tipoEdicao = "";
    private Fragment fragment = null;
    private DadosUserUtils dadosUserUtils;

    public EdicaoCadActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        dadosUserUtils = new DadosUserUtils();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_cad);
        inicializandoComponentes();
        configInicial();
    }

    private void configInicial() {
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
                fragment = new NomeFragment();
                bundleNome();
                break;
            case "genero":
                fragment = new GeneroFragment();
                bundleGenero();
                break;
            case "interesses":
                fragment = new InteressesFragment();
                bundleInteresses();
                break;
        }
    }

    private void bundleNome() {
        if (fragment != null) {
            dadosUserUtils.recuperarNome(idUsuario, new DadosUserUtils.RecuperarNomeCallback() {
                @Override
                public void onRecuperado(String nome) {
                    bundleString("edit", nome);
                }

                @Override
                public void onSemDado() {

                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void bundleGenero() {
        dadosUserUtils.recuperarGenero(idUsuario, new DadosUserUtils.RecuperarGeneroCallback() {
            @Override
            public void onRecuperado(String genero) {
                bundleString("edit", genero);
            }

            @Override
            public void onSemDado() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void bundleInteresses() {
        dadosUserUtils.recuperarInteresses(idUsuario, new DadosUserUtils.RecuperarInteressesCallback() {
            @Override
            public void onRecuperado(ArrayList<String> listaInteresses) {
                bundleArrayList("edit", listaInteresses);
            }

            @Override
            public void onSemDado() {

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

    private void bundleArrayList(String key, ArrayList<String> lista) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(key, lista);
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutEditCad, fragment);
        fragmentTransaction.commit();
    }

    private void inicializandoComponentes() {
        frameLayoutEdit = findViewById(R.id.frameLayoutEditCad);
    }

    @Override
    public void onUsuario(Usuario usuario, String etapa) {

    }
}