package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.fragment.cad.EpilepsiaFragment;
import com.example.ogima.fragment.cad.GeneroFragment;
import com.example.ogima.fragment.cad.IdadeFragment;
import com.example.ogima.fragment.cad.InteressesFragment;
import com.example.ogima.fragment.cad.NomeFragment;
import com.example.ogima.helper.DataCadListener;
import com.example.ogima.helper.LockedViewPager;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class CadastroActivity extends AppCompatActivity implements DataCadListener {

    private String idUsuario = "";
    private SmartTabLayout smartTab;
    private LockedViewPager viewpager;
    private FragmentPagerItemAdapter fragmentPager;
    private String fragmentDesejado;
    private Boolean retornarAoItem = false;
    private int itemAtual;
    private String emailUsuario = "";
    private int sairDoCadastro = 0;
    private FloatingActionButton fabBack;

    @Override
    public void onBackPressed() {
        if (emailUsuario != null && !emailUsuario.isEmpty()) {
            sairDoCadastro++;
            if (sairDoCadastro == 2) {
                UsuarioUtils.deslogarUsuario(getApplicationContext(), new UsuarioUtils.DeslogarUsuarioCallback() {
                    @Override
                    public void onDeslogado() {
                        finish();
                    }
                });
                return;
            }
            if (sairDoCadastro < 2) {
                ToastCustomizado.toastCustomizado(String.format("%s %d %s", "Necessário pressionar mais", 2 - sairDoCadastro, "vez para sair do cadastro"), getApplicationContext());
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        retornarAoItem = true;
        itemAtual = viewpager.getCurrentItem();
    }

    public CadastroActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        inicializandoComponentes();

        Bundle dados = getIntent().getExtras();

        if (dados != null
                && dados.containsKey("dadosUsuario")) {
            Usuario usuarioCad = (Usuario) dados.getSerializable("dadosUsuario");
            if (usuarioCad != null && usuarioCad.getEmailUsuario() != null
                    && !usuarioCad.getEmailUsuario().isEmpty()) {
                emailUsuario = usuarioCad.getEmailUsuario();
            }
        }

        fragmentPager = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(CadastroActivity.this)
                .add("", NomeFragment.class)
                .add("", IdadeFragment.class)
                .add("", GeneroFragment.class)
                .add("", InteressesFragment.class)
                .add("", EpilepsiaFragment.class)
                .create());

        viewpager.setAdapter(fragmentPager);
        viewpager.setPagingEnabled(false);
        smartTab.setClickable(false);

        if (retornarAoItem) {
            viewpager.setCurrentItem(itemAtual);
            retornarAoItem = false;
        }

        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewpager.getCurrentItem() != -1
                        && viewpager.getCurrentItem() >= 1) {
                    viewpager.setCurrentItem(viewpager.getCurrentItem() - 1);
                }
            }
        });
    }

    private void inicializandoComponentes() {
        viewpager = findViewById(R.id.viewpagerCad);
        smartTab = findViewById(R.id.smartTabCad);
        fabBack = findViewById(R.id.fabBack);
    }

    @Override
    public void onUsuario(Usuario usuario, String etapa) {

        sairDoCadastro = 0;
        //Basicamente ele recebe por interface em qual etapa ele está no momento
        //e de acordo com isso eu envio o usuario por interface para a etapa seguinte.

        if (emailUsuario != null && !emailUsuario.isEmpty()) {
            usuario.setEmailUsuario(emailUsuario);
            ToastCustomizado.toastCustomizado("Email " + usuario.getEmailUsuario(), getApplicationContext());
        }

        Fragment fragmentNome = fragmentPager.getPage(0);
        Fragment fragmentIdade = fragmentPager.getPage(1);
        Fragment fragmentGenero = fragmentPager.getPage(2);
        Fragment fragmentInteresses = fragmentPager.getPage(3);
        Fragment fragmentEpilepsia = fragmentPager.getPage(4);

        switch (etapa) {
            case "nome":
                if (fragmentIdade instanceof IdadeFragment) {
                    ((IdadeFragment) fragmentIdade).setUserCad(usuario);
                }
                viewpager.setCurrentItem(1);
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuario.getNomeUsuario(), getApplicationContext());
                break;
            case "idade":
                if (fragmentGenero instanceof GeneroFragment) {
                    ((GeneroFragment) fragmentGenero).setUserCad(usuario);
                }
                viewpager.setCurrentItem(2);
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuario.getNomeUsuario(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Idade " + usuario.getIdade(), getApplicationContext());
                break;
            case "genero":
                if (fragmentInteresses instanceof InteressesFragment) {
                    ((InteressesFragment) fragmentInteresses).setUserCad(usuario);
                }
                viewpager.setCurrentItem(3);
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuario.getNomeUsuario(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Idade " + usuario.getIdade(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Genero " + usuario.getGeneroUsuario(), getApplicationContext());
                break;
            case "interesses":
                if (fragmentEpilepsia instanceof EpilepsiaFragment) {
                    ((EpilepsiaFragment) fragmentEpilepsia).setUserCad(usuario);
                }
                viewpager.setCurrentItem(4);
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuario.getNomeUsuario(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Idade " + usuario.getIdade(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Genero " + usuario.getGeneroUsuario(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Interesses " + usuario.getInteresses().size(), getApplicationContext());
                break;
            case "epilepsia":
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuario.getNomeUsuario(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Idade " + usuario.getIdade(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Genero " + usuario.getGeneroUsuario(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Interesses " + usuario.getInteresses().size(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Epilepsia " + usuario.isStatusEpilepsia(), getApplicationContext());
                break;
        }
    }
}